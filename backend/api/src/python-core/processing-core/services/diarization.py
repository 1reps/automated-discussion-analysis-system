"""Diarization services: Rule-based lightweight and Pyannote-based.
"""

from __future__ import annotations

import audioop
import contextlib
import os
import wave
from dataclasses import dataclass
from pathlib import Path
from typing import List, Optional


@dataclass
class DiarizationSegment:
  speaker_label: str
  start_ms: int
  end_ms: int
  confidence: Optional[float] = None
  user_id: str | None = None
  user_name: str | None = None


class DiarizationService:
  def run(self, audio_path: Path, *, language_hint: Optional[str] = None) -> \
  List[DiarizationSegment]:
    raise NotImplementedError


class PyannoteDiarizationService(DiarizationService):
  def __init__(
      self,
      *,
      hf_token: Optional[str],
      model_id: str = "pyannote/speaker-diarization",
      device: Optional[str] = None,
      num_speakers: Optional[int] = None,
      min_speakers: Optional[int] = None,
      max_speakers: Optional[int] = None,
      min_speaker_duration: float = 0.5,
      max_speaker_gap: float = 0.2,
  ) -> None:
    try:
      from pyannote.audio import Pipeline  # type: ignore
    except ImportError as exc:  # pragma: no cover
      raise RuntimeError(
        "pyannote.audio 패키지가 설치되어 있지 않습니다. requirements를 참고해 설치해 주세요."
      ) from exc

    os.environ.setdefault("HF_HUB_DISABLE_SYMLINKS", "1")
    self._pipeline = Pipeline.from_pretrained(model_id, use_auth_token=hf_token)
    if device:
      self._pipeline.to(device)

    self._num_speakers = num_speakers
    self._min_speakers = min_speakers
    self._max_speakers = max_speakers
    self._min_speaker_duration_ms = int(max(min_speaker_duration, 0.0) * 1000)
    self._max_speaker_gap_ms = int(max(max_speaker_gap, 0.0) * 1000)

  def run(self, audio_path: Path, *, language_hint: Optional[str] = None) -> \
  List[DiarizationSegment]:  # noqa: ARG002
    inference_kwargs: dict[str, int] = {}
    if self._num_speakers is not None:
      inference_kwargs["num_speakers"] = self._num_speakers
    if self._min_speakers is not None:
      inference_kwargs["min_speakers"] = self._min_speakers
    if self._max_speakers is not None:
      inference_kwargs["max_speakers"] = self._max_speakers

    diarization = self._pipeline(str(audio_path), **inference_kwargs)

    raw_segments: List[DiarizationSegment] = []
    for turn, _, speaker in diarization.itertracks(yield_label=True):
      if speaker is None:
        continue
      start_ms = max(0, int(round(turn.start * 1000)))
      end_ms = max(start_ms + 1, int(round(turn.end * 1000)))
      raw_segments.append(
        DiarizationSegment(
          speaker_label=str(speaker),
          start_ms=start_ms,
          end_ms=end_ms,
          confidence=None,
        )
      )

    raw_segments.sort(key=lambda segment: segment.start_ms)
    return self._postprocess_segments(raw_segments)

  def _postprocess_segments(self, segments: List[DiarizationSegment]) -> List[
    DiarizationSegment]:
    if not segments:
      return []
    merged: List[DiarizationSegment] = [segments[0]]
    for seg in segments[1:]:
      current = merged[-1]
      if (
          seg.speaker_label == current.speaker_label
          and seg.start_ms - current.end_ms <= self._max_speaker_gap_ms
      ):
        current.end_ms = max(current.end_ms, seg.end_ms)
      else:
        merged.append(seg)
    processed: List[DiarizationSegment] = []
    for seg in merged:
      duration_ms = seg.end_ms - seg.start_ms
      if processed and duration_ms < self._min_speaker_duration_ms:
        processed[-1].end_ms = max(processed[-1].end_ms, seg.end_ms)
      else:
        processed.append(seg)
    return processed


class RuleBasedDiarizationService(DiarizationService):
  def __init__(
      self,
      *,
      sample_rate: int = 16000,
      frame_ms: int = 30,
      min_speech_ms: int = 250,
      min_silence_ms: int = 300,
      merge_gap_ms: int = 200,
      energy_threshold_ratio: float = 2.5,
      max_speakers: int = 2,
  ) -> None:
    self.sample_rate = sample_rate
    self.frame_ms = frame_ms
    self.min_speech_ms = min_speech_ms
    self.min_silence_ms = min_silence_ms
    self.merge_gap_ms = merge_gap_ms
    self.energy_threshold_ratio = energy_threshold_ratio
    self.max_speakers = max(1, max_speakers)

  def run(self, audio_path: Path, *, language_hint: Optional[str] = None) -> \
  List[DiarizationSegment]:  # noqa: ARG002
    pcm, sampwidth, sr = self._read_mono_pcm(audio_path)
    frame_len = int(self.frame_ms * sr / 1000)
    if frame_len <= 0:
      frame_len = 480

    energies: list[float] = []
    step = frame_len * sampwidth
    for i in range(0, len(pcm), step):
      chunk = pcm[i: i + step]
      if len(chunk) < step:
        break
      rms = audioop.rms(chunk, sampwidth)
      energies.append(float(rms))

    if not energies:
      return []

    sorted_e = sorted(energies)
    k = max(1, int(len(sorted_e) * 0.2))
    noise_floor = sum(sorted_e[:k]) / k
    threshold = max(noise_floor * self.energy_threshold_ratio,
                    noise_floor + 50.0)

    speech_regions: list[tuple[int, int]] = []
    in_speech = False
    speech_start = 0
    silence_count = 0
    min_speech_frames = max(1, int(self.min_speech_ms / self.frame_ms))
    min_silence_frames = max(1, int(self.min_silence_ms / self.frame_ms))

    for idx, e in enumerate(energies):
      if e >= threshold:
        if not in_speech:
          in_speech = True
          speech_start = idx
          silence_count = 0
        else:
          silence_count = 0
      else:
        if in_speech:
          silence_count += 1
          if silence_count >= min_silence_frames:
            if idx - speech_start >= min_speech_frames:
              speech_regions.append((speech_start, idx - silence_count))
            in_speech = False
            silence_count = 0

    if in_speech and len(energies) - speech_start >= min_speech_frames:
      speech_regions.append((speech_start, len(energies) - 1))

    segments: List[DiarizationSegment] = []
    last_end_ms: int | None = None
    speaker_toggle = 0
    for start_f, end_f in speech_regions:
      start_ms = start_f * self.frame_ms
      end_ms = max(start_ms + 1, end_f * self.frame_ms)
      if last_end_ms is not None and start_ms - last_end_ms <= self.merge_gap_ms:
        if segments:
          segments[-1].end_ms = max(segments[-1].end_ms, end_ms)
        last_end_ms = end_ms
        continue
      label_idx = (speaker_toggle % self.max_speakers) + 1
      speaker_toggle += 1
      segments.append(
        DiarizationSegment(
          speaker_label=f"SPEAKER_{label_idx}",
          start_ms=start_ms,
          end_ms=end_ms,
          confidence=None,
        )
      )
      last_end_ms = end_ms

    return segments

  @staticmethod
  def _read_mono_pcm(path: Path) -> tuple[bytes, int, int]:
    with contextlib.closing(wave.open(str(path), "rb")) as wf:
      n_channels = wf.getnchannels()
      sampwidth = wf.getsampwidth()
      framerate = wf.getframerate()
      frames = wf.readframes(wf.getnframes())
    if n_channels == 1:
      return frames, sampwidth, framerate
    mono = audioop.tomono(frames, sampwidth, 1, 0)
    return mono, sampwidth, framerate

"""Faster-Whisper based STT Provider."""

from __future__ import annotations

import math
from pathlib import Path
from processing_core.services.diarization import DiarizationSegment
from processing_core.services.stt.base import STTProvider, TranscriptSegment, \
  WordTiming
from typing import Any, Optional, Sequence

try:
  from faster_whisper import WhisperModel  # type: ignore
except ImportError as exc:  # pragma: no cover
  WhisperModel = None  # type: ignore
  _IMPORT_ERROR = exc
else:
  _IMPORT_ERROR = None


class WhisperProvider(STTProvider):
  name = "faster-whisper"

  def __init__(
      self,
      *,
      model_size: str = "small",
      device: str = "auto",
      compute_type: str = "auto",
      download_root: Optional[Path] = None,
      beam_size: int = 5,
      vad_filter: bool = True,
      condition_on_previous_text: bool = True,
      word_timestamps: bool = False,
  ) -> None:
    if WhisperModel is None:
      raise RuntimeError(
        "faster-whisper가 설치되어 있지 않습니다. requirements를 참고해 패키지를 설치하세요."
      ) from _IMPORT_ERROR

    self.model_size = model_size
    self.device = self._resolve_device(device)
    self.compute_type = compute_type
    self.download_root = download_root
    self.beam_size = beam_size
    self.vad_filter = vad_filter
    self.condition_on_previous_text = condition_on_previous_text
    self.word_timestamps = word_timestamps
    self._model: WhisperModel | None = None

  def _resolve_device(self, device: str) -> str:
    if device and device.lower() != "auto":
      return device
    try:
      import torch  # type: ignore

      return "cuda" if torch.cuda.is_available() else "cpu"
    except ImportError:
      return "cpu"

  @property
  def model(self) -> WhisperModel:
    if self._model is None:
      self._model = WhisperModel(
        self.model_size,
        device=self.device,
        compute_type=self.compute_type,
        download_root=str(self.download_root) if self.download_root else None,
      )
    return self._model

  def transcribe(
      self,
      audio_path: Path,
      *,
      segments: Optional[Sequence[DiarizationSegment]] = None,  # noqa: ARG002
      language: Optional[str] = None,
  ) -> Sequence[TranscriptSegment]:
    if not audio_path.exists():
      raise FileNotFoundError(f"오디오 파일을 찾을 수 없습니다: {audio_path}")

    options = {
      "language": language,
      "beam_size": self.beam_size,
      "vad_filter": self.vad_filter,
      "condition_on_previous_text": self.condition_on_previous_text,
      "word_timestamps": self.word_timestamps,
    }

    segment_iter, info = self.model.transcribe(str(audio_path), **options)
    detected_language = language or getattr(info, "language", None)
    if detected_language is not None:
      detected_language = str(detected_language)

    results: list[TranscriptSegment] = []
    for whisper_segment in segment_iter:
      text = whisper_segment.text.strip()
      if not text:
        continue
      confidence = _confidence_from_segment(whisper_segment)
      words: list[WordTiming] | None = None
      if self.word_timestamps:
        words = []
        for w in getattr(whisper_segment, "words", []) or []:
          if not getattr(w, "word", "").strip():
            continue
          words.append(
            WordTiming(
              start_ms=int(w.start * 1000),
              end_ms=int(w.end * 1000),
              text=w.word.strip(),
              confidence=getattr(w, "probability", None),
            )
          )
      results.append(
        TranscriptSegment(
          provider=self.name,
          start_ms=int(whisper_segment.start * 1000),
          end_ms=int(whisper_segment.end * 1000),
          text=text,
          confidence=confidence,
          language=detected_language,
          words=words,
        )
      )

    return results


def _confidence_from_segment(segment: Any) -> Optional[
  float]:  # type: ignore[no-untyped-def]
  no_speech_prob = getattr(segment, "no_speech_prob", None)
  logprob = getattr(segment, "avg_logprob", None)

  if isinstance(no_speech_prob, (int, float)):
    return float(max(0.0, min(1.0, 1.0 - no_speech_prob)))
  if isinstance(logprob, (int, float)):
    try:
      return float(1 / (1 + math.exp(-logprob)))
    except OverflowError:
      return None
  return None

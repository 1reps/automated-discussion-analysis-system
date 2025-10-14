"""Simple alignment between diarization segments and transcript segments."""

from __future__ import annotations

from dataclasses import dataclass
from processing_core.services.diarization import DiarizationSegment
from processing_core.services.stt.base import TranscriptSegment
from typing import Sequence


@dataclass
class SpeakerTurnResult:
  speaker_label: str
  start_ms: int
  end_ms: int
  text: str
  user_id: str | None = None
  user_name: str | None = None


class AlignmentService:
  def __init__(self, overlap_tolerance_ms: int = 200) -> None:
    self.overlap_tolerance_ms = overlap_tolerance_ms

  def align(
      self,
      diar_segments: Sequence[DiarizationSegment],
      transcript_segments: Sequence[TranscriptSegment],
  ) -> list[SpeakerTurnResult]:
    if not diar_segments or not transcript_segments:
      return []
    turns: list[SpeakerTurnResult] = []
    for transcript in transcript_segments:
      matching = self._find_matching_diar(transcript, diar_segments)
      label = matching.speaker_label if matching else "UNKNOWN"
      turns.append(
        SpeakerTurnResult(
          speaker_label=label,
          start_ms=transcript.start_ms,
          end_ms=transcript.end_ms,
          text=transcript.text,
        )
      )
    return turns

  def _find_matching_diar(
      self,
      transcript: TranscriptSegment,
      diar_segments: Sequence[DiarizationSegment],
  ) -> DiarizationSegment | None:
    best_segment: DiarizationSegment | None = None
    best_overlap = -1
    for diar in diar_segments:
      overlap = self._compute_overlap(transcript, diar)
      if overlap > best_overlap and overlap >= -self.overlap_tolerance_ms:
        best_overlap = overlap
        best_segment = diar
    return best_segment

  @staticmethod
  def _compute_overlap(
      transcript: TranscriptSegment,
      diar: DiarizationSegment,
  ) -> int:
    start = max(transcript.start_ms, diar.start_ms)
    end = min(transcript.end_ms, diar.end_ms)
    return end - start

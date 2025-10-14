"""STT Provider interface and DTOs."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from processing_core.services.diarization import DiarizationSegment
from typing import Optional, Protocol, Sequence


@dataclass
class WordTiming:
  start_ms: int
  end_ms: int
  text: str
  confidence: Optional[float] = None


@dataclass
class TranscriptSegment:
  provider: str
  start_ms: int
  end_ms: int
  text: str
  confidence: Optional[float] = None
  language: Optional[str] = None
  speaker_label: Optional[str] = None
  words: Optional[list[WordTiming]] = None


class STTProvider(Protocol):
  name: str

  def transcribe(
      self,
      audio_path: Path,
      *,
      segments: Optional[Sequence[DiarizationSegment]] = None,
      language: Optional[str] = None,
  ) -> Sequence[TranscriptSegment]:
    ...

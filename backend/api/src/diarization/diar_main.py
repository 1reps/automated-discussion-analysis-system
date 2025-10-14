from __future__ import annotations

from pathlib import Path
from typing import Optional

from fastapi import FastAPI, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware

from processing_core.settings import settings
from processing_core.services.recording import RecordingService
from processing_core.services.diarization import RuleBasedDiarizationService, PyannoteDiarizationService


def allowed_origins():
  raw = (settings.ALLOWED_ORIGINS or "").strip()
  return ["*"] if settings.ENV == "dev" or raw in ("", "*") else [x.strip() for x in raw.split(",") if x.strip()]


app = FastAPI(title="Diarization Service", version="0.1.0", root_path="/api/v1")
app.add_middleware(
  CORSMiddleware,
  allow_origins=allowed_origins(),
  allow_credentials=True,
  allow_methods=["*"],
  allow_headers=["*"],
)

recording_service = RecordingService(base_dir=Path(settings.WORK_DIR), max_upload_bytes=settings.MAX_UPLOAD_MB * 1024 * 1024, ffmpeg_bin=settings.FFMPEG_BIN)

if (settings.DIARIZATION_PROVIDER or "rule").lower() == "pyannote":
  diar_service = PyannoteDiarizationService(
    hf_token=settings.HF_TOKEN,
    model_id=settings.DIARIZATION_MODEL_ID,
    device=settings.DIARIZATION_DEVICE or None,
    num_speakers=settings.DIARIZATION_NUM_SPEAKERS,
    min_speakers=settings.DIARIZATION_MIN_SPEAKERS,
    max_speakers=settings.DIARIZATION_MAX_SPEAKERS,
    min_speaker_duration=settings.DIARIZATION_MIN_SPEAKER_DURATION,
    max_speaker_gap=settings.DIARIZATION_MAX_SPEAKER_GAP,
  )
else:
  diar_service = RuleBasedDiarizationService()


@app.post("/diarize")
async def diarize(
    file: UploadFile = File(...),
    language: Optional[str] = Form(None),
    max_speakers: Optional[int] = Form(None),
):
  # store and normalize
  stored = await recording_service.store_upload(file, language=language, source="diarization")
  wav_path = recording_service.get_raw_path(stored.recording_id)

  # override max speakers for rule-based
  if isinstance(diar_service, RuleBasedDiarizationService) and max_speakers:
    diar_service.max_speakers = max_speakers

  segments = diar_service.run(Path(wav_path), language_hint=language)
  return {
    "segments": [
      {
        "start": seg.start_ms,
        "end": seg.end_ms,
        "speaker": seg.speaker_label,
        "confidence": seg.confidence,
      }
      for seg in segments
    ]
  }


@app.get("/health")
def health():
  return {"status": "ok", "env": settings.ENV}


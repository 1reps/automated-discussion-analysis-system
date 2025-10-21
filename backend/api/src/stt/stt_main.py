from __future__ import annotations

from fastapi import FastAPI, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from pathlib import Path
from processing_core.services.recording import RecordingService
from processing_core.services.stt.factory import STTProviderFactory
from processing_core.settings import settings
from typing import Optional


def allowed_origins():
  raw = (settings.ALLOWED_ORIGINS or "").strip()
  return ["*"] if settings.ENV == "dev" or raw in ("", "*") else [x.strip() for
                                                                  x in
                                                                  raw.split(",")
                                                                  if x.strip()]


app = FastAPI(title="STT Service", version="0.1.0", root_path="/api/v1")
app.add_middleware(
  CORSMiddleware,
  allow_origins=allowed_origins(),
  allow_credentials=True,
  allow_methods=["*"],
  allow_headers=["*"],
)

recording_service = RecordingService(base_dir=Path(settings.WORK_DIR),
                                     max_upload_bytes=settings.MAX_UPLOAD_MB * 1024 * 1024,
                                     ffmpeg_bin=settings.FFMPEG_BIN)

# Initialize STT provider using factory pattern
stt_provider = STTProviderFactory.create_provider(settings)


@app.post("/stt/transcribe")
async def transcribe(
    file: UploadFile = File(...),
    language: Optional[str] = Form(None),
):
  stored = await recording_service.store_upload(file, language=language,
                                                source="stt")
  wav_path = recording_service.get_raw_path(stored.recording_id)
  segments = stt_provider.transcribe(Path(wav_path), language=language)
  # Flatten words if present
  words = []
  for seg in segments:
    if seg.words:
      for w in seg.words:
        words.append({
          "start": w.start_ms,
          "end": w.end_ms,
          "text": w.text,
          "confidence": w.confidence,
        })
    else:
      words.append({
        "start": seg.start_ms,
        "end": seg.end_ms,
        "text": seg.text,
        "confidence": seg.confidence,
      })
  return {
    "lang": language or (segments[0].language if segments else None),
    "words": words,
  }


@app.get("/health")
def health():
  return {
    "status": "ok", 
    "env": settings.ENV,
    "stt_provider": settings.STT_PROVIDER,
    "available_providers": STTProviderFactory.get_available_providers()
  }

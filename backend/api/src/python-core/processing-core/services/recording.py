"""
Recording service: stores uploads and ensures WAV 16k mono via ffmpeg.
"""

from __future__ import annotations

import json
import mimetypes
import shutil
import subprocess
from datetime import datetime
from pathlib import Path
from typing import Optional
from uuid import uuid4

from fastapi import UploadFile
from pydantic import BaseModel, Field


class RecordingResult(BaseModel):
  recording_id: str
  file_path: str
  metadata_path: str
  size_bytes: int
  language: Optional[str] = None
  source: Optional[str] = None
  created_at: str = Field(default_factory=lambda: datetime.utcnow().isoformat())


class RecordingService:
  def __init__(self, base_dir: Path, max_upload_bytes: int, *, ffmpeg_bin: str = "ffmpeg") -> None:
    self.base_dir = base_dir
    self.max_upload_bytes = max_upload_bytes
    self.ffmpeg_bin = ffmpeg_bin
    self.recordings_dir = self.base_dir / "recordings"
    self.recordings_dir.mkdir(parents=True, exist_ok=True)

  async def store_upload(
      self,
      upload: UploadFile,
      *,
      language: Optional[str],
      source: Optional[str],
  ) -> RecordingResult:
    recording_id = uuid4().hex
    dest_dir = self.recordings_dir / recording_id
    dest_dir.mkdir(parents=True, exist_ok=True)

    source_filename = upload.filename
    suffix = Path(source_filename or "").suffix.lower()
    if suffix not in {".wav", ".webm", ".mp3", ".m4a", ".ogg", ".flac"}:
      suffix = ".webm"

    dest_path = dest_dir / f"raw{suffix}"
    size = 0
    try:
      with dest_path.open("wb") as out_file:
        while True:
          chunk = await upload.read(1024 * 1024)
          if not chunk:
            break
          size += len(chunk)
          if self.max_upload_bytes and size > self.max_upload_bytes:
            raise ValueError("업로드 파일이 허용된 최대 크기를 초과했습니다.")
          out_file.write(chunk)
    finally:
      await upload.close()

    if size == 0:
      dest_path.unlink(missing_ok=True)
      raise ValueError("빈 파일은 업로드할 수 없습니다.")

    uploaded_size = dest_path.stat().st_size
    try:
      final_path, final_size, converted = self._ensure_wav_format(dest_path)
    except Exception:
      dest_path.unlink(missing_ok=True)
      raise

    created_at = datetime.utcnow().isoformat()
    metadata = {
      "recording_id": recording_id,
      "raw_filename": final_path.name,
      "size_bytes": final_size,
      "language": language,
      "source": source,
      "created_at": created_at,
      "mime_type": mimetypes.guess_type(final_path.name)[0],
    }
    if converted:
      metadata["original_filename"] = source_filename
      metadata["original_mime_type"] = (mimetypes.guess_type(source_filename)[0] if source_filename else None)
      metadata["original_size_bytes"] = uploaded_size
      metadata["converted_to_wav"] = True

    metadata_path = dest_dir / "metadata.json"
    metadata_path.write_text(json.dumps(metadata, ensure_ascii=False, indent=2), encoding="utf-8")

    return RecordingResult(
      recording_id=recording_id,
      file_path=str(final_path),
      metadata_path=str(metadata_path),
      size_bytes=final_size,
      language=language,
      source=source,
      created_at=created_at,
    )

  def get_raw_path(self, recording_id: str) -> Path:
    dest_dir = self.recordings_dir / recording_id
    for candidate in dest_dir.glob("raw.*"):
      return candidate
    # fallback to metadata
    meta_path = dest_dir / "metadata.json"
    if meta_path.exists():
      data = json.loads(meta_path.read_text(encoding="utf-8"))
      name = data.get("raw_filename")
      if name:
        candidate = dest_dir / name
        if candidate.exists():
          return candidate
    raise FileNotFoundError(f"Recording {recording_id} raw file not found")

  def _ensure_wav_format(self, source_path: Path) -> tuple[Path, int, bool]:
    suffix = source_path.suffix.lower()
    if suffix == ".wav":
      return source_path, source_path.stat().st_size, False

    ffmpeg_bin = self._resolve_ffmpeg_bin()
    target_path = source_path.with_suffix(".wav")
    command = [
      ffmpeg_bin,
      "-hide_banner",
      "-loglevel",
      "error",
      "-y",
      "-i",
      str(source_path),
      "-ac",
      "1",
      "-ar",
      "16000",
      str(target_path),
    ]

    completed = subprocess.run(  # noqa: S603
      command,
      stdout=subprocess.PIPE,
      stderr=subprocess.PIPE,
      check=False,
    )
    if completed.returncode != 0 or not target_path.exists():
      stderr = completed.stderr.decode("utf-8", errors="ignore").strip()
      target_path.unlink(missing_ok=True)
      message = "오디오를 WAV 형식으로 변환하지 못했습니다."
      if stderr:
        message = f"{message} (ffmpeg: {stderr})"
      raise ValueError(message)

    source_path.unlink(missing_ok=True)
    return target_path, target_path.stat().st_size, True

  def _resolve_ffmpeg_bin(self) -> str:
    candidate = Path(self.ffmpeg_bin)
    if candidate.exists():
      return str(candidate)
    resolved = shutil.which(self.ffmpeg_bin)
    if resolved:
      return resolved
    raise ValueError("ffmpeg 실행 파일을 찾을 수 없습니다.")


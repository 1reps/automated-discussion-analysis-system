"""Shared settings for processing services (no DB)."""

from typing import Optional

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
  ENV: str = "dev"
  PORT: int = 8000
  LOG_LEVEL: str = "info"

  ALLOWED_ORIGINS: str = "*"

  STT_PROVIDER: str = "whisper"

  DIARIZATION_PROVIDER: str = "rule"
  DIARIZATION_MODEL_ID: str = "pyannote/speaker-diarization"
  DIARIZATION_DEVICE: Optional[str] = None
  DIARIZATION_NUM_SPEAKERS: Optional[int] = None
  DIARIZATION_MIN_SPEAKERS: Optional[int] = None
  DIARIZATION_MAX_SPEAKERS: Optional[int] = None
  DIARIZATION_MIN_SPEAKER_DURATION: float = 0.5
  DIARIZATION_MAX_SPEAKER_GAP: float = 0.2
  HF_TOKEN: Optional[str] = None

  WHISPER_MODEL_SIZE: str = "small"
  WHISPER_COMPUTE_TYPE: str = "auto"
  WHISPER_DEVICE: str = "auto"
  WHISPER_DOWNLOAD_ROOT: Optional[str] = None
  WHISPER_BEAM_SIZE: int = 5
  WHISPER_VAD_FILTER: bool = True
  WHISPER_CONDITION_ON_PREVIOUS_TEXT: bool = True
  WHISPER_WORD_TIMESTAMPS: bool = True

  # Google Cloud STT Configuration
  GOOGLE_PROJECT_ID: Optional[str] = None
  GOOGLE_CREDENTIALS: Optional[str] = None
  GOOGLE_LANGUAGE: str = "ko-KR"
  GOOGLE_SAMPLE_RATE: int = 16000
  
  # Google STT Features
  GOOGLE_ENABLE_WORD_TIME_OFFSETS: bool = True
  GOOGLE_ENABLE_AUTOMATIC_PUNCTUATION: bool = True
  GOOGLE_ENABLE_SPOKEN_PUNCTUATION: bool = False
  GOOGLE_ENABLE_SPOKEN_EMOJIS: bool = False
  GOOGLE_ENABLE_WORD_CONFIDENCE: bool = True
  
  # Google STT Model Configuration
  GOOGLE_MODEL: str = "latest_long"  # latest_long, latest_short, command_and_search, phone_call, video, default
  GOOGLE_USE_ENHANCED: bool = True
  GOOGLE_AUDIO_ENCODING: str = "LINEAR16"  # LINEAR16, FLAC, MULAW, AMR, AMR_WB, OGG_OPUS, SPEEX_WITH_HEADER_BYTE
  
  # Google STT Alternative Settings
  GOOGLE_MAX_ALTERNATIVES: int = 1
  GOOGLE_PROFANITY_FILTER: bool = False
  
  # Google STT Speaker Diarization
  GOOGLE_ENABLE_SPEAKER_DIARIZATION: bool = False
  GOOGLE_DIARIZATION_SPEAKER_COUNT: Optional[int] = None
  GOOGLE_MIN_SPEAKER_COUNT: Optional[int] = None
  GOOGLE_MAX_SPEAKER_COUNT: Optional[int] = None

  WORK_DIR: str = "/tmp/ai-voice"
  FFMPEG_BIN: str = "ffmpeg"
  MAX_UPLOAD_MB: int = 200
  PROCESS_TIMEOUT_SEC: int = 180

  API_AUTH_TOKEN: Optional[str] = None

  model_config = SettingsConfigDict(
    env_file=".env",
    env_file_encoding="utf-8",
    case_sensitive=False,
  )


settings = Settings()


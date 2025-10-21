"""Google Cloud Speech-to-Text configuration settings."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Optional

try:
    from google.cloud import speech  # type: ignore
except ImportError:  # pragma: no cover
    speech = None


@dataclass
class GoogleSTTConfig:
    """Configuration for Google Cloud Speech-to-Text service.
    
    This class encapsulates all the settings needed for Google STT,
    providing sensible defaults and validation.
    """
    
    # Authentication & Project
    credentials_path: Optional[Path] = None
    project_id: Optional[str] = None
    
    # Audio Configuration
    sample_rate_hertz: int = 16000
    audio_channel_count: int = 1
    language_code: str = "ko-KR"
    
    # Recognition Features
    enable_word_time_offsets: bool = True
    enable_automatic_punctuation: bool = True
    enable_spoken_punctuation: bool = False
    enable_spoken_emojis: bool = False
    enable_word_confidence: bool = True
    enable_separate_recognition_per_channel: bool = False
    
    # Model Selection
    model: str = "latest_long"  # latest_long, latest_short, command_and_search, phone_call, video, default
    use_enhanced: bool = True
    
    # Audio Processing
    audio_encoding: str = "LINEAR16"  # LINEAR16, FLAC, MULAW, AMR, AMR_WB, OGG_OPUS, SPEEX_WITH_HEADER_BYTE
    
    # Alternative Settings
    max_alternatives: int = 1
    profanity_filter: bool = False
    
    # Advanced Features
    enable_speaker_diarization: bool = False
    diarization_speaker_count: Optional[int] = None
    min_speaker_count: Optional[int] = None
    max_speaker_count: Optional[int] = None
    
    def __post_init__(self) -> None:
        """Validate configuration after initialization."""
        if self.credentials_path and not self.credentials_path.exists():
            raise FileNotFoundError(f"Google credentials file not found: {self.credentials_path}")
        
        if self.sample_rate_hertz <= 0:
            raise ValueError("Sample rate must be positive")
        
        if self.audio_channel_count <= 0:
            raise ValueError("Audio channel count must be positive")
        
        if self.max_alternatives <= 0:
            raise ValueError("Max alternatives must be positive")
    
    def get_audio_encoding(self) -> "speech.RecognitionConfig.AudioEncoding":
        """Get Google Speech AudioEncoding enum value."""
        if speech is None:
            raise RuntimeError("google-cloud-speech is not installed")
        
        encoding_map = {
            "LINEAR16": speech.RecognitionConfig.AudioEncoding.LINEAR16,
            "FLAC": speech.RecognitionConfig.AudioEncoding.FLAC,
            "MULAW": speech.RecognitionConfig.AudioEncoding.MULAW,
            "AMR": speech.RecognitionConfig.AudioEncoding.AMR,
            "AMR_WB": speech.RecognitionConfig.AudioEncoding.AMR_WB,
            "OGG_OPUS": speech.RecognitionConfig.AudioEncoding.OGG_OPUS,
            "SPEEX_WITH_HEADER_BYTE": speech.RecognitionConfig.AudioEncoding.SPEEX_WITH_HEADER_BYTE,
        }
        
        if self.audio_encoding not in encoding_map:
            available = ", ".join(encoding_map.keys())
            raise ValueError(f"Unsupported audio encoding: {self.audio_encoding}. Available: {available}")
        
        return encoding_map[self.audio_encoding]
    
    def to_recognition_config(self) -> "speech.RecognitionConfig":
        """Convert to Google Speech RecognitionConfig object."""
        if speech is None:
            raise RuntimeError("google-cloud-speech is not installed")
        
        config_kwargs = {
            "encoding": self.get_audio_encoding(),
            "sample_rate_hertz": self.sample_rate_hertz,
            "language_code": self.language_code,
            "enable_word_time_offsets": self.enable_word_time_offsets,
            "enable_automatic_punctuation": self.enable_automatic_punctuation,
            "enable_spoken_punctuation": self.enable_spoken_punctuation,
            "enable_spoken_emojis": self.enable_spoken_emojis,
            "enable_word_confidence": self.enable_word_confidence,
            "enable_separate_recognition_per_channel": self.enable_separate_recognition_per_channel,
            "model": self.model,
            "use_enhanced": self.use_enhanced,
            "audio_channel_count": self.audio_channel_count,
            "max_alternatives": self.max_alternatives,
            "profanity_filter": self.profanity_filter,
        }
        
        # Add speaker diarization if enabled
        if self.enable_speaker_diarization:
            diarization_config = speech.SpeakerDiarizationConfig(
                enable_speaker_diarization=True,
            )
            
            if self.diarization_speaker_count is not None:
                diarization_config.speaker_count = self.diarization_speaker_count
            else:
                if self.min_speaker_count is not None:
                    diarization_config.min_speaker_count = self.min_speaker_count
                if self.max_speaker_count is not None:
                    diarization_config.max_speaker_count = self.max_speaker_count
            
            config_kwargs["diarization_config"] = diarization_config
        
        return speech.RecognitionConfig(**config_kwargs)
    
    @classmethod
    def from_settings(cls, settings: any) -> GoogleSTTConfig:
        """Create configuration from application settings."""
        return cls(
            credentials_path=Path(settings.GOOGLE_CREDENTIALS) if settings.GOOGLE_CREDENTIALS else None,
            project_id=getattr(settings, 'GOOGLE_PROJECT_ID', None),
            sample_rate_hertz=getattr(settings, 'GOOGLE_SAMPLE_RATE', 16000),
            language_code=getattr(settings, 'GOOGLE_LANGUAGE', 'ko-KR'),
            enable_word_time_offsets=getattr(settings, 'GOOGLE_ENABLE_WORD_TIME_OFFSETS', True),
            enable_automatic_punctuation=getattr(settings, 'GOOGLE_ENABLE_AUTOMATIC_PUNCTUATION', True),
            enable_spoken_punctuation=getattr(settings, 'GOOGLE_ENABLE_SPOKEN_PUNCTUATION', False),
            enable_spoken_emojis=getattr(settings, 'GOOGLE_ENABLE_SPOKEN_EMOJIS', False),
            enable_word_confidence=getattr(settings, 'GOOGLE_ENABLE_WORD_CONFIDENCE', True),
            model=getattr(settings, 'GOOGLE_MODEL', 'latest_long'),
            use_enhanced=getattr(settings, 'GOOGLE_USE_ENHANCED', True),
            audio_encoding=getattr(settings, 'GOOGLE_AUDIO_ENCODING', 'LINEAR16'),
            max_alternatives=getattr(settings, 'GOOGLE_MAX_ALTERNATIVES', 1),
            profanity_filter=getattr(settings, 'GOOGLE_PROFANITY_FILTER', False),
            enable_speaker_diarization=getattr(settings, 'GOOGLE_ENABLE_SPEAKER_DIARIZATION', False),
            diarization_speaker_count=getattr(settings, 'GOOGLE_DIARIZATION_SPEAKER_COUNT', None),
            min_speaker_count=getattr(settings, 'GOOGLE_MIN_SPEAKER_COUNT', None),
            max_speaker_count=getattr(settings, 'GOOGLE_MAX_SPEAKER_COUNT', None),
        )
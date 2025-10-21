"""STT Provider Factory for creating appropriate STT providers."""

from __future__ import annotations

from pathlib import Path
from processing_core.services.stt.base import STTProvider
from processing_core.services.stt.whisper_provider import WhisperProvider
from processing_core.services.stt.google_provider import GoogleSTTProvider
from processing_core.services.stt.google_config import GoogleSTTConfig
from processing_core.settings import Settings
from typing import Dict, Type


class STTProviderFactory:
    """Factory for creating STT providers based on configuration."""
    
    _providers: Dict[str, Type[STTProvider]] = {
        "whisper": WhisperProvider,
        "faster-whisper": WhisperProvider,
        "google": GoogleSTTProvider,
        "google-stt": GoogleSTTProvider,
    }
    
    @classmethod
    def create_provider(cls, settings: Settings) -> STTProvider:
        """Create an STT provider based on settings.
        
        Args:
            settings: Application settings containing provider configuration
            
        Returns:
            Configured STT provider instance
            
        Raises:
            ValueError: If provider type is not supported
        """
        provider_name = settings.STT_PROVIDER.lower()
        
        if provider_name not in cls._providers:
            available = ", ".join(cls._providers.keys())
            raise ValueError(
                f"Unsupported STT provider: {provider_name}. "
                f"Available providers: {available}"
            )
        
        if provider_name in ("google", "google-stt"):
            return cls._create_google_provider(settings)
        else:  # whisper, faster-whisper
            return cls._create_whisper_provider(settings)
    
    @classmethod
    def _create_whisper_provider(cls, settings: Settings) -> WhisperProvider:
        """Create a Whisper STT provider."""
        return WhisperProvider(
            model_size=settings.WHISPER_MODEL_SIZE,
            device=settings.WHISPER_DEVICE,
            compute_type=settings.WHISPER_COMPUTE_TYPE,
            download_root=Path(settings.WHISPER_DOWNLOAD_ROOT) if settings.WHISPER_DOWNLOAD_ROOT else None,
            beam_size=settings.WHISPER_BEAM_SIZE,
            vad_filter=settings.WHISPER_VAD_FILTER,
            condition_on_previous_text=settings.WHISPER_CONDITION_ON_PREVIOUS_TEXT,
            word_timestamps=settings.WHISPER_WORD_TIMESTAMPS,
        )
    
    @classmethod
    def _create_google_provider(cls, settings: Settings) -> GoogleSTTProvider:
        """Create a Google STT provider."""
        config = GoogleSTTConfig.from_settings(settings)
        return GoogleSTTProvider(config)
    
    @classmethod
    def register_provider(cls, name: str, provider_class: Type[STTProvider]) -> None:
        """Register a new STT provider.
        
        Args:
            name: Provider name for registration
            provider_class: Provider class to register
        """
        cls._providers[name.lower()] = provider_class
    
    @classmethod
    def get_available_providers(cls) -> list[str]:
        """Get list of available provider names."""
        return list(cls._providers.keys())
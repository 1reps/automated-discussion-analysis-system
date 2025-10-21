"""Google Cloud Speech-to-Text Provider."""

from __future__ import annotations

from pathlib import Path
from processing_core.services.diarization import DiarizationSegment
from processing_core.services.stt.base import STTProvider, TranscriptSegment, WordTiming
from processing_core.services.stt.google_config import GoogleSTTConfig
from typing import Optional, Sequence

try:
    from google.cloud import speech  # type: ignore
except ImportError as exc:  # pragma: no cover
    speech = None  # type: ignore
    _IMPORT_ERROR = exc
else:
    _IMPORT_ERROR = None


class GoogleSTTProvider(STTProvider):
    name = "google-stt"

    def __init__(self, config: GoogleSTTConfig) -> None:
        if speech is None:
            raise RuntimeError(
                "google-cloud-speech가 설치되어 있지 않습니다. requirements를 참고해 패키지를 설치하세요."
            ) from _IMPORT_ERROR

        self.config = config
        self._client: speech.SpeechClient | None = None

    @property
    def client(self) -> speech.SpeechClient:
        if self._client is None:
            if self.config.credentials_path and self.config.credentials_path.exists():
                import os
                os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = str(self.config.credentials_path)
            self._client = speech.SpeechClient()
        return self._client

    def transcribe(
        self,
        audio_path: Path,
        *,
        segments: Optional[Sequence[DiarizationSegment]] = None,  # noqa: ARG002
        language: Optional[str] = None,
    ) -> Sequence[TranscriptSegment]:
        if not audio_path.exists():
            raise FileNotFoundError(f"오디오 파일을 찾을 수 없습니다: {audio_path}")

        # Read audio file
        with open(audio_path, "rb") as audio_file:
            content = audio_file.read()

        # Configure audio
        audio = speech.RecognitionAudio(content=content)
        
        # Use config to create recognition config, override language if provided
        if language:
            # Create a copy of config with updated language
            config_dict = {
                "credentials_path": self.config.credentials_path,
                "project_id": self.config.project_id,
                "sample_rate_hertz": self.config.sample_rate_hertz,
                "audio_channel_count": self.config.audio_channel_count,
                "language_code": self._map_language_code(language),
                "enable_word_time_offsets": self.config.enable_word_time_offsets,
                "enable_automatic_punctuation": self.config.enable_automatic_punctuation,
                "enable_spoken_punctuation": self.config.enable_spoken_punctuation,
                "enable_spoken_emojis": self.config.enable_spoken_emojis,
                "enable_word_confidence": self.config.enable_word_confidence,
                "enable_separate_recognition_per_channel": self.config.enable_separate_recognition_per_channel,
                "model": self.config.model,
                "use_enhanced": self.config.use_enhanced,
                "audio_encoding": self.config.audio_encoding,
                "max_alternatives": self.config.max_alternatives,
                "profanity_filter": self.config.profanity_filter,
                "enable_speaker_diarization": self.config.enable_speaker_diarization,
                "diarization_speaker_count": self.config.diarization_speaker_count,
                "min_speaker_count": self.config.min_speaker_count,
                "max_speaker_count": self.config.max_speaker_count,
            }
            temp_config = GoogleSTTConfig(**config_dict)
            recognition_config = temp_config.to_recognition_config()
        else:
            recognition_config = self.config.to_recognition_config()

        # Perform recognition
        response = self.client.recognize(config=recognition_config, audio=audio)

        results: list[TranscriptSegment] = []
        for result in response.results:
            if not result.alternatives:
                continue
                
            alternative = result.alternatives[0]
            text = alternative.transcript.strip()
            if not text:
                continue

            confidence = alternative.confidence if hasattr(alternative, 'confidence') else None
            
            # Extract word timings
            words: list[WordTiming] | None = None
            if self.config.enable_word_time_offsets and hasattr(alternative, 'words'):
                words = []
                for word_info in alternative.words:
                    start_time = word_info.start_time
                    end_time = word_info.end_time
                    
                    start_ms = int(start_time.total_seconds() * 1000)
                    end_ms = int(end_time.total_seconds() * 1000)
                    
                    words.append(
                        WordTiming(
                            start_ms=start_ms,
                            end_ms=end_ms,
                            text=word_info.word,
                            confidence=getattr(word_info, 'confidence', None),
                        )
                    )

            # Use first and last word timings for segment timing if available
            if words:
                start_ms = words[0].start_ms
                end_ms = words[-1].end_ms
            else:
                # Fallback to segment-level timing if available
                start_ms = 0
                end_ms = 0

            results.append(
                TranscriptSegment(
                    provider=self.name,
                    start_ms=start_ms,
                    end_ms=end_ms,
                    text=text,
                    confidence=confidence,
                    language=language or self.config.language_code,
                    words=words,
                )
            )

        return results

    def _map_language_code(self, language: Optional[str]) -> str:
        """Map common language codes to Google's format."""
        if not language:
            return "ko-KR"
        
        language = language.lower()
        language_map = {
            "ko": "ko-KR",
            "korean": "ko-KR",
            "en": "en-US",
            "english": "en-US",
            "ja": "ja-JP",
            "japanese": "ja-JP",
            "zh": "zh-CN",
            "chinese": "zh-CN",
            "es": "es-ES",
            "spanish": "es-ES",
            "fr": "fr-FR",
            "french": "fr-FR",
            "de": "de-DE",
            "german": "de-DE",
        }
        
        return language_map.get(language, language if "-" in language else "ko-KR")
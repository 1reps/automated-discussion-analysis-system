# Google Cloud Speech-to-Text API 참조

## 공식 문서
- [Google Cloud Speech-to-Text 개요](https://cloud.google.com/speech-to-text/docs)
- [Python Client Library](https://googleapis.dev/python/speech/latest/)
- [API Reference](https://cloud.google.com/speech-to-text/docs/reference/rest/v1/RecognitionConfig)

## 인증 설정
1. [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트 생성
2. Speech-to-Text API 활성화
3. 서비스 계정 키 생성 (JSON 파일)
4. 환경 변수 설정:
   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS="/path/to/credentials.json"
   ```

## 설치
```bash
pip install google-cloud-speech
```

## 주요 API 클래스와 설정

### RecognitionConfig
우리 코드의 `GoogleSTTConfig.to_recognition_config()`에서 사용하는 설정들:

```python
from google.cloud import speech

config = speech.RecognitionConfig(
    encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
    sample_rate_hertz=16000,
    language_code="ko-KR",
    enable_word_time_offsets=True,
    enable_automatic_punctuation=True,
    model="latest_long",
    use_enhanced=True,
)
```

### 지원되는 오디오 인코딩
- `LINEAR16`: 16-bit linear PCM
- `FLAC`: FLAC 무손실 오디오
- `MULAW`: 8-bit μ-law PCM  
- `AMR`: Adaptive Multi-Rate
- `AMR_WB`: Adaptive Multi-Rate Wideband
- `OGG_OPUS`: OGG 컨테이너의 Opus
- `SPEEX_WITH_HEADER_BYTE`: Speex

### 지원되는 모델
- `latest_long`: 긴 오디오용 최신 모델 (기본값)
- `latest_short`: 짧은 오디오용 최신 모델
- `command_and_search`: 음성 명령 및 검색용
- `phone_call`: 전화 통화용
- `video`: 비디오 오디오용
- `default`: 기본 모델

### 언어 코드 예시
- `ko-KR`: 한국어
- `en-US`: 영어 (미국)
- `ja-JP`: 일본어
- `zh-CN`: 중국어 (간체)
- `es-ES`: 스페인어

## 화자 분리 (Speaker Diarization)
```python
diarization_config = speech.SpeakerDiarizationConfig(
    enable_speaker_diarization=True,
    min_speaker_count=2,
    max_speaker_count=10,
)

config = speech.RecognitionConfig(
    # ... 다른 설정들
    diarization_config=diarization_config,
)
```

## 사용 예시
```python
from google.cloud import speech

client = speech.SpeechClient()

with open('audio.wav', 'rb') as audio_file:
    content = audio_file.read()

audio = speech.RecognitionAudio(content=content)
response = client.recognize(config=config, audio=audio)

for result in response.results:
    print(f"Transcript: {result.alternatives[0].transcript}")
    print(f"Confidence: {result.alternatives[0].confidence}")
```

## 가격 정보
- [Speech-to-Text 가격표](https://cloud.google.com/speech-to-text/pricing)
- Standard model: $0.006/15초
- Enhanced model: $0.009/15초
- 매월 첫 60분 무료

## 제한사항
- 파일 크기: 최대 10MB (REST), 무제한 (Streaming)
- 오디오 길이: 최대 480분 (REST)
- 실시간 스트리밍: 지원됨

## 우리 구현에서의 활용
- `GoogleSTTConfig`: 모든 설정을 체계적으로 관리
- `GoogleSTTProvider`: 실제 API 호출 담당
- `STTProviderFactory`: Provider 생성 및 설정 주입

## 환경 변수 설정 예시
```bash
STT_PROVIDER=google
GOOGLE_CREDENTIALS=/path/to/credentials.json
GOOGLE_LANGUAGE=ko-KR
GOOGLE_MODEL=latest_long
GOOGLE_ENABLE_WORD_TIME_OFFSETS=true
GOOGLE_ENABLE_AUTOMATIC_PUNCTUATION=true
```
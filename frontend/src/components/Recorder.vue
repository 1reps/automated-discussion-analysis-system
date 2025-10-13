<template>
  <main class="container">
    <h1>CM-700USB 마이크 업로드 테스트</h1>
    <p>웹 브라우저에서 녹음 후 <code>/ingest/audio</code>로 업로드합니다.</p>

    <label>입력 장치 선택</label>
    <select v-model="selectedDeviceId">
      <option v-for="d in audioInputs" :key="d.deviceId" :value="d.deviceId">
        {{ d.label || '오디오 입력' }}
      </option>
    </select>

    <div class="controls">
      <button :disabled="isRecording" @click="startRecording">녹음 시작</button>
      <button :disabled="!isRecording && !currentBlob" @click="finishAndUpload">
        녹음 종료 및 업로드
      </button>
    </div>

    <label>언어 코드</label>
    <input v-model="language" type="text" />

    <label>소스 식별자</label>
    <input v-model="source" type="text" />

    <div class="status">{{ status }}</div>
    <div v-if="summary" class="summary">{{ summary }}</div>

    <audio ref="audioEl" controls style="width: 100%; margin-top: 1rem"></audio>

    <h2>서버 응답</h2>
    <pre>{{ responseText }}</pre>

    <h2>전사 결과</h2>
    <pre>{{ transcriptText }}</pre>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue';
defineOptions({ name: 'AudioRecorder' });

const apiBase = import.meta.env.VITE_API_BASE || '';
// proxy를 쓰면 '/api' 프리픽스를 권장
const INGEST_URL = apiBase ? `${apiBase}/ingest/audio` : '/api/ingest/audio';

const audioInputs = ref([]);
const selectedDeviceId = ref('');
const isRecording = ref(false);
const status = ref('장치 목록을 불러오는 중...');
const summary = ref('');
const responseText = ref('(응답이 여기에 표시됩니다)');
const transcriptText = ref('(전사 결과가 여기에 표시됩니다)');
const language = ref('ko');
const source = ref('cm-700usb');

const audioEl = ref(null);
let mediaRecorder = null;
let recordedChunks = [];
let currentStream = null;
let currentBlob = null;
let stopResolve = null;

function setStatus(msg) {
  status.value = msg;
}

function setSummary(text) {
  summary.value = text || '';
}

function setTranscript(text) {
  transcriptText.value = text || '';
}

function formatMs(ms) {
  const totalSeconds = Math.max(0, Math.floor(ms / 1000));
  const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, '0');
  const seconds = String(totalSeconds % 60).padStart(2, '0');
  return `${minutes}:${seconds}`;
}

function formatLogs(logs) {
  if (!logs || logs.length === 0) return '(로그 없음)';
  return logs
    .map((l) => {
      const t = new Date(l.created_at).toLocaleTimeString('ko-KR', { hour12: false });
      return `[${t}] ${l.stage}/${l.status}${l.message ? ` - ${l.message}` : ''}`;
    })
    .join('\n');
}

async function initDevices() {
  try {
    await navigator.mediaDevices.getUserMedia({ audio: true });
    const devices = await navigator.mediaDevices.enumerateDevices();
    audioInputs.value = devices.filter((d) => d.kind === 'audioinput');
    if (audioInputs.value.length === 0) {
      setStatus('오디오 입력 장치를 찾을 수 없습니다.');
    } else {
      selectedDeviceId.value = audioInputs.value[0]?.deviceId || '';
      setStatus('장치가 준비되었습니다. 녹음을 시작할 수 있습니다.');
    }
  } catch (e) {
    console.error(e);
    setStatus('마이크 권한을 허용해야 녹음을 진행할 수 있습니다.');
  }
}

async function startRecording() {
  try {
    const constraints = selectedDeviceId.value
      ? { audio: { deviceId: { exact: selectedDeviceId.value } } }
      : { audio: true };
    currentStream = await navigator.mediaDevices.getUserMedia(constraints);
    recordedChunks = [];
    mediaRecorder = new MediaRecorder(currentStream);
    mediaRecorder.ondataavailable = (ev) => {
      if (ev.data && ev.data.size > 0) recordedChunks.push(ev.data);
    };
    mediaRecorder.onstop = () => {
      currentBlob = new Blob(recordedChunks, { type: mediaRecorder.mimeType || 'audio/webm' });
      const url = URL.createObjectURL(currentBlob);
      if (audioEl.value) {
        audioEl.value.src = url;
        audioEl.value.play().catch(() => {});
      }
      setStatus('녹음이 완료되었습니다. 업로드 버튼을 눌러 전송하세요.');
      if (stopResolve) {
        const resolve = stopResolve;
        stopResolve = null;
        resolve();
      }
    };
    mediaRecorder.start();
    isRecording.value = true;
    setStatus('녹음 중... 필요 시 "녹음 종료"를 눌러주세요.');
    setSummary('');
    setTranscript('(전사 대기 중...)');
  } catch (e) {
    console.error(e);
    setStatus('녹음을 시작할 수 없습니다: ' + e.message);
  }
}

function stopRecording() {
  if (!mediaRecorder) return Promise.resolve();
  setStatus('녹음 종료 중...');
  const done = new Promise((resolve) => {
    stopResolve = resolve;
  });
  mediaRecorder.stop();
  if (currentStream) {
    currentStream.getTracks().forEach((t) => t.stop());
    currentStream = null;
  }
  isRecording.value = false;
  return done;
}

async function finishAndUpload() {
  try {
    if (isRecording.value) {
      await stopRecording();
    }
    await uploadRecording();
  } catch (e) {
    console.error(e);
    setStatus('종료/업로드 과정에서 오류가 발생했습니다: ' + e.message);
  }
}

async function uploadRecording() {
  if (!currentBlob) {
    setStatus('먼저 녹음을 완료해주세요.');
    return;
  }
  setTranscript('(전사 대기 중...)');
  setSummary('');
  setStatus('업로드 중...');
  const formData = new FormData();
  // 일부 백엔드는 확장자를 참고하므로 webm 명시
  formData.append('file', currentBlob, 'recording.webm');
  formData.append('language', language.value.trim());
  formData.append('source', source.value.trim() || 'cm-700usb');

  try {
    const res = await fetch(INGEST_URL, { method: 'POST', body: formData });
    const payload = await res.json();
    responseText.value = JSON.stringify(payload, null, 2);

    if (!res.ok) {
      setStatus('업로드 실패: ' + (payload.detail || res.statusText));
      setTranscript('(전사 실패)');
      return;
    }
    setStatus(`업로드 성공! 녹음 ID: ${payload.recording_id} - 전사를 진행 중입니다.`);
    await pollTranscript(payload.recording_id, payload.transcript_url);
  } catch (e) {
    console.error(e);
    setStatus('업로드 중 오류가 발생했습니다: ' + e.message);
    setTranscript('(전사 실패)');
  }
}

function buildSummary(transcripts, speakerTurns, userSummaryArr) {
  if ((!transcripts || transcripts.length === 0) && (!speakerTurns || speakerTurns.length === 0)) {
    setSummary('');
    return;
  }
  let summaryText = '';
  if (userSummaryArr && userSummaryArr.length > 0) {
    summaryText += userSummaryArr
      .map((item) => {
        const name = item.user_name || item.user_id || item.speaker_label || 'UNKNOWN';
        const count = item.turn_count || 0;
        const totalSec = Math.round((item.total_duration_ms || 0) / 1000);
        return `${name}: ${count}회 발화, 총 ${totalSec}초`;
      })
      .join('\n');
  } else {
    summaryText += `${(speakerTurns && speakerTurns.length) || 0}개의 발화가 감지되었습니다.`;
  }
  const lastTurn = speakerTurns && speakerTurns[speakerTurns.length - 1];
  if (lastTurn && lastTurn.text) {
    summaryText += `\n실시간 자막: ${lastTurn.user_name || lastTurn.speaker_label || 'UNKNOWN'}: ${lastTurn.text}`;
  }
  setSummary(summaryText);
}

async function pollTranscript(recordingId, transcriptUrl, attempt = 0) {
  const maxAttempts = 40; // 약 80초
  try {
    const res = await fetch(transcriptUrl);
    if (!res.ok) throw new Error('전사 조회 실패: ' + res.statusText);
    const payload = await res.json();
    responseText.value = JSON.stringify(payload, null, 2);

    const transcripts = payload.transcripts || [];
    const speakerTurns = payload.speaker_turns || [];
    const logs = payload.logs || [];
    const userSummaryArr = payload.user_summary || [];

    if (payload.status === 'failed') {
      setStatus('전사 처리에 실패했습니다. 로그를 확인하세요.');
      setTranscript(`(전사 실패)\n${formatLogs(logs)}`);
      setSummary('');
      return;
    }

    if (transcripts.length > 0) {
      const text = transcripts
        .map((seg) => `[${formatMs(seg.start_ms)}-${formatMs(seg.end_ms)}] ${seg.text}`)
        .join('\n');
      setTranscript(text);
    } else {
      setTranscript(`(전사 결과를 기다리는 중입니다...)\n${formatLogs(logs)}`);
    }

    buildSummary(transcripts, speakerTurns, userSummaryArr);

    if (payload.status === 'completed') {
      setStatus('전사가 완료되었습니다!');
      return;
    }

    if (attempt < maxAttempts) {
      setTimeout(() => {
        void pollTranscript(recordingId, transcriptUrl, attempt + 1);
      }, 2000);
    } else {
      setStatus('전사 결과를 가져오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    }
  } catch (e) {
    console.error(e);
    if (attempt < maxAttempts) {
      setTimeout(() => {
        void pollTranscript(recordingId, transcriptUrl, attempt + 1);
      }, 3000);
    } else {
      setStatus('전사 조회 중 오류가 반복되었습니다: ' + e.message);
      setTranscript('(전사 실패)');
      setSummary('');
    }
  }
}

onMounted(() => {
  void initDevices();
});
</script>

<style scoped>
.container {
  font-family: Arial, sans-serif;
  margin: 2rem;
  background: #f7f9fc;
  color: #1f2933;
}
h1 {
  font-size: 1.8rem;
  margin-bottom: 0.5rem;
}
p {
  margin: 0.5rem 0 1rem;
  line-height: 1.5;
}
label {
  display: block;
  margin: 0.75rem 0 0.25rem;
  font-weight: 600;
}
select,
input,
button {
  padding: 0.5rem;
  font-size: 1rem;
}
button {
  margin-right: 0.5rem;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #2563eb;
  background: #2563eb;
  color: #fff;
}
button[disabled] {
  background: #93c5fd;
  border-color: #93c5fd;
  cursor: not-allowed;
}
.controls {
  margin: 1rem 0;
}
.status {
  margin-top: 1rem;
  padding: 0.75rem;
  background: #e0f2fe;
  border-radius: 4px;
}
.summary {
  margin-top: 1rem;
  padding: 0.75rem;
  background: #e2f7e1;
  border-radius: 4px;
  color: #14532d;
}
pre {
  background: #0f172a;
  color: #e2e8f0;
  padding: 1rem;
  border-radius: 4px;
  overflow: auto;
  white-space: pre-wrap;
}
</style>

let mediaRecorder;
let chunks = [];
let timerInterval;
let seconds = 0;

const startBtn = document.getElementById('startBtn');
const stopBtn = document.getElementById('stopBtn');
const submitBtn = document.getElementById('submitBtn');
const preview = document.getElementById('preview');
const audioInput = document.getElementById('audioInput');
const statusBlock = document.getElementById('statusBlock');

function formatTime(s) {
    return String(Math.floor(s / 60)).padStart(2, '0') + ':' + String(s % 60).padStart(2, '0');
}


function setRecordingStatus(text) {
    const indicator = document.createElement('span');
    indicator.className = 'recording-indicator me-2';
    const textNode = document.createTextNode(text);
    statusBlock.replaceChildren(indicator, textNode);
}

startBtn.addEventListener('click', async () => {
    chunks = [];
    seconds = 0;
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        mediaRecorder = new MediaRecorder(stream);

        mediaRecorder.ondataavailable = e => { if (e.data.size > 0) chunks.push(e.data); };

        mediaRecorder.onstop = () => {
            stream.getTracks().forEach(t => t.stop());
            clearInterval(timerInterval);
            const blob = new Blob(chunks, { type: mediaRecorder.mimeType || 'audio/webm' });
            const file = new File([blob], 'answer.webm', { type: blob.type });
            const dt = new DataTransfer();
            dt.items.add(file);
            audioInput.files = dt.files;
            preview.src = URL.createObjectURL(blob);
            preview.style.display = 'block';
            submitBtn.disabled = false;
            startBtn.disabled = true;
            startBtn.title = 'Перезапись недоступна';
            statusBlock.textContent = 'Запись завершена. Прослушайте и отправьте ответ.';
        };

        mediaRecorder.start(500);
        startBtn.disabled = true;
        stopBtn.disabled = false;
        submitBtn.disabled = true;
        preview.style.display = 'none';

        timerInterval = setInterval(() => {
            seconds++;
            setRecordingStatus('Идёт запись: ' + formatTime(seconds));
        }, 1000);
        setRecordingStatus('Идёт запись: 00:00');

    } catch (err) {
        const span = document.createElement('span');
        span.className = 'text-danger';
        span.textContent = 'Нет доступа к микрофону: ' + err.message;
        statusBlock.replaceChildren(span);
    }
});

stopBtn.addEventListener('click', () => {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop();
        startBtn.disabled = false;
        stopBtn.disabled = true;
    }
});

(function setupAbandonBeacon() {
    const progressId = window.SCREENING_PROGRESS_ID;
    if (!progressId) return;

    let isSubmitting = false;

    document.getElementById('answerForm').addEventListener('submit', function () {
        isSubmitting = true;
    });

    function sendAbandon() {
        if (isSubmitting) return;
        const raw = document.cookie.split('; ').find(c => c.startsWith('XSRF-TOKEN='));
        const csrf = raw ? decodeURIComponent(raw.split('=')[1]) : '';
        const body = new URLSearchParams({ _csrf: csrf });
        navigator.sendBeacon('/screening/' + progressId + '/abandon', body);
    }

    window.addEventListener('beforeunload', sendAbandon);

    document.addEventListener('visibilitychange', function () {
        if (document.hidden) {
            sendAbandon();
        }
    });
})();

(function setupBars() {
    var bars = document.getElementById('recorderBars');
    if (!bars) return;

    document.getElementById('startBtn').addEventListener('click', function () {
        bars.classList.add('recorder-bars--active');
    });
    document.getElementById('stopBtn').addEventListener('click', function () {
        bars.classList.remove('recorder-bars--active');
    });
})();

(async function checkMicOnLoad() {
    var startBtn = document.getElementById('startBtn');
    var statusBlock = document.getElementById('statusBlock');

    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        blockMic('Ваш браузер не поддерживает запись аудио.');
        return;
    }

    if (navigator.permissions) {
        try {
            var status = await navigator.permissions.query({ name: 'microphone' });
            if (status.state === 'denied') {
                blockMic('Доступ к микрофону заблокирован. Разрешите его в настройках браузера (🔒 в адресной строке) и обновите страницу.');
                return;
            }
            status.onchange = function () {
                if (this.state === 'denied') {
                    blockMic('Доступ к микрофону был отозван. Разрешите его и обновите страницу.');
                } else if (this.state === 'granted') {
                    unblockMic();
                }
            };
        } catch (_) {}
    }

    function blockMic(message) {
        startBtn.disabled = true;
        startBtn.title = message;
        var warning = document.createElement('div');
        warning.id = 'micWarning';
        warning.className = 'alert alert-danger d-flex gap-2 align-items-start mb-3';
        warning.style.fontSize = '.875rem';
        warning.innerHTML =
            '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" ' +
            'stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;margin-top:.1rem;">' +
            '<line x1="1" y1="1" x2="23" y2="23"/>' +
            '<path d="M9 9v3a3 3 0 0 0 5.12 2.12M15 9.34V4a3 3 0 0 0-5.94-.6"/>' +
            '<path d="M17 16.95A7 7 0 0 1 5 12v-2m14 0v2a7 7 0 0 1-.11 1.23"/>' +
            '<line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>' +
            '<span>' + message + '</span>';
        if (statusBlock) {
            statusBlock.replaceWith(warning);
        }
    }

    function unblockMic() {
        startBtn.disabled = false;
        startBtn.title = '';
        var existing = document.getElementById('micWarning');
        if (existing) {
            var newStatus = document.createElement('div');
            newStatus.id = 'statusBlock';
            newStatus.style.cssText = 'font-size:.875rem;color:var(--ink-500);margin-bottom:.5rem;min-height:1.5rem;';
            newStatus.textContent = 'Нажмите «Начать запись» для записи ответа';
            existing.replaceWith(newStatus);
        }
    }
})();

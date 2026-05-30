(function () {
    var acceptBtn = document.getElementById('acceptBtn');
    var vacancyId = acceptBtn ? acceptBtn.dataset.vacancyId : '';
    var startUrl = '/screening/start/' + vacancyId;

    acceptBtn.addEventListener('click', async function () {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            return;
        }

        if (!vacancyId) {
            return;
        }

        acceptBtn.disabled = true;
        acceptBtn.innerHTML =
            '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>' +
            'Проверка микрофона…';

        try {
            var stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            stream.getTracks().forEach(function (t) { t.stop(); });
            window.location.href = startUrl;
        } catch (err) {
            acceptBtn.disabled = false;
            acceptBtn.innerHTML =
                '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" ' +
                'stroke-linecap="round" stroke-linejoin="round" class="me-2"><polyline points="20 6 9 17 4 12"/></svg>' +
                'Принимаю';
        }
    });
})();

document.addEventListener('DOMContentLoaded', function () {
    var hash = window.location.hash;
    if (hash === '#feedback') {
        new bootstrap.Tab(document.getElementById('feedback-tab')).show();
    } else if (hash === '#candidate') {
        new bootstrap.Tab(document.getElementById('candidate-tab')).show();
    }

    var hasProcessing = document.querySelector('.spinner-border') !== null;
    if (hasProcessing) {
        setTimeout(function () { location.reload(); }, 5000);
    }
});

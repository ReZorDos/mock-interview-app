var filterToggle = document.getElementById('filterToggle');
var filterSidebar = document.getElementById('filterSidebar');

filterToggle.addEventListener('click', function () {
    filterSidebar.classList.toggle('is-open');
});

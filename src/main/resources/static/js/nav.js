(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    var toggle = document.getElementById('navToggle');
    var links  = document.getElementById('navLinks');
    if (toggle && links) {
      toggle.addEventListener('click', function () {
        links.classList.toggle('is-open');
        toggle.classList.toggle('is-open');
        toggle.setAttribute('aria-expanded',
          links.classList.contains('is-open') ? 'true' : 'false');
      });
      document.addEventListener('click', function (e) {
        if (!toggle.contains(e.target) && !links.contains(e.target)) {
          links.classList.remove('is-open');
          toggle.classList.remove('is-open');
          toggle.setAttribute('aria-expanded', 'false');
        }
      });
    }

    var nav = document.querySelector('.site-nav');
    if (nav) {
      var onScroll = function () {
        nav.classList.toggle('scrolled', window.scrollY > 8);
      };
      window.addEventListener('scroll', onScroll, { passive: true });
      onScroll();
    }
  });
})();

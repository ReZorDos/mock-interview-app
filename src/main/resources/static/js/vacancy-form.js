$(document).ready(function () {
    $('#skills').select2({
        theme: 'bootstrap-5',
        placeholder: 'Выберите или введите навык',
        tags: true,
        tokenSeparators: [',', ' ', ';'],
        width: '100%',
        language: 'ru'
    });
});

document.querySelector('form').addEventListener('submit', function (e) {
    var fromInput = document.getElementById('salaryFrom');
    var toInput   = document.getElementById('salaryTo');
    var errorId   = 'salaryRangeError';

    var existing = document.getElementById(errorId);
    if (existing) existing.remove();
    toInput.classList.remove('is-invalid');

    var from = fromInput.value !== '' ? parseInt(fromInput.value, 10) : null;
    var to   = toInput.value   !== '' ? parseInt(toInput.value,   10) : null;

    if (from !== null && to !== null && from > to) {
        e.preventDefault();
        toInput.classList.add('is-invalid');
        var msg = document.createElement('div');
        msg.id = errorId;
        msg.className = 'invalid-feedback';
        msg.textContent = 'Зарплата «от» не может быть больше зарплаты «до»';
        toInput.insertAdjacentElement('afterend', msg);
        toInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
});

var container = document.getElementById('questionsContainer');
var noQuestionsMsg = document.getElementById('noQuestionsMsg');

function updateNoMsg() {
    noQuestionsMsg.style.display = container.querySelectorAll('.q-build-card').length === 0 ? '' : 'none';
}

function reindex() {
    container.querySelectorAll('.q-build-card').forEach(function (card, idx) {
        card.querySelector('.q-build-num').textContent = 'Вопрос ' + (idx + 1);
        card.querySelectorAll('[data-field]').forEach(function (el) {
            el.name = 'questions[' + idx + '].' + el.getAttribute('data-field');
        });
    });
}

function createQuestionCard(q) {
    q = q || {};
    var idx = container.querySelectorAll('.q-build-card').length;

    var card = document.createElement('div');
    card.className = 'q-build-card';

    var header = document.createElement('div');
    header.className = 'd-flex justify-content-between align-items-center mb-3';

    var numSpan = document.createElement('span');
    numSpan.className = 'q-build-num';
    numSpan.textContent = 'Вопрос ' + (idx + 1);

    var removeBtn = document.createElement('button');
    removeBtn.type = 'button';
    removeBtn.className = 'btn btn-sm btn-outline-danger';
    removeBtn.textContent = 'Удалить';
    removeBtn.addEventListener('click', function () {
        card.remove();
        reindex();
        updateNoMsg();
    });

    header.appendChild(numSpan);
    header.appendChild(removeBtn);

    var textGroup = document.createElement('div');
    textGroup.className = 'mb-3';

    var textLabel = document.createElement('label');
    textLabel.className = 'form-label';
    textLabel.textContent = 'Текст вопроса';

    var textarea = document.createElement('textarea');
    textarea.setAttribute('data-field', 'text');
    textarea.name = 'questions[' + idx + '].text';
    textarea.className = 'form-control';
    textarea.rows = 2;
    textarea.placeholder = 'Введите текст вопроса';
    textarea.value = q.text || '';

    textGroup.appendChild(textLabel);
    textGroup.appendChild(textarea);

    var row = document.createElement('div');
    row.className = 'row g-2';

    var diffCol = document.createElement('div');
    diffCol.className = 'col-md-6';

    var diffLabel = document.createElement('label');
    diffLabel.className = 'form-label';
    diffLabel.textContent = 'Сложность';

    var diffSelect = document.createElement('select');
    diffSelect.setAttribute('data-field', 'difficulty');
    diffSelect.name = 'questions[' + idx + '].difficulty';
    diffSelect.className = 'form-select';

    [{ value: 'EASY', label: 'Лёгкий' }, { value: 'MEDIUM', label: 'Средний' }, { value: 'HARD', label: 'Сложный' }]
        .forEach(function (pair) {
            var opt = document.createElement('option');
            opt.value = pair.value;
            opt.textContent = pair.label;
            if (q.difficulty === pair.value) opt.selected = true;
            diffSelect.appendChild(opt);
        });

    diffCol.appendChild(diffLabel);
    diffCol.appendChild(diffSelect);

    var catCol = document.createElement('div');
    catCol.className = 'col-md-6';

    var catLabel = document.createElement('label');
    catLabel.className = 'form-label';
    catLabel.textContent = 'Категория';

    var catInput = document.createElement('input');
    catInput.type = 'text';
    catInput.setAttribute('data-field', 'category');
    catInput.name = 'questions[' + idx + '].category';
    catInput.className = 'form-control';
    catInput.placeholder = 'Например: Java Core';
    catInput.value = q.category || '';

    catCol.appendChild(catLabel);
    catCol.appendChild(catInput);

    row.appendChild(diffCol);
    row.appendChild(catCol);

    card.appendChild(header);
    card.appendChild(textGroup);
    card.appendChild(row);
    container.appendChild(card);
    updateNoMsg();
}

document.getElementById('addQuestionBtn').addEventListener('click', function () {
    createQuestionCard(null);
});

document.querySelectorAll('#existingQuestionsData .q-data').forEach(function (el) {
    createQuestionCard({
        text: el.getAttribute('data-text'),
        difficulty: el.getAttribute('data-difficulty'),
        category: el.getAttribute('data-category')
    });
});

updateNoMsg();

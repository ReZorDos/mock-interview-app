# MockInterview

Платформа для подготовки к техническим собеседованиям. Кандидаты проходят mock-интервью с записью аудиоответов, интервьюеры создают вакансии, формируют пул вопросов и оставляют обратную связь.

## Технологии

| Слой | Технология |
|------|------------|
| Backend | Java 17, Spring Boot 3.5 |
| Шаблонизатор | Thymeleaf |
| БД | PostgreSQL 15 |
| Миграции | Liquibase |
| Хранилище файлов | MinIO (S3-совместимое) |
| Транскрипция | AssemblyAI API |
| Аутентификация | Spring Security 6, Yandex OAuth |
| Контейнеризация | Docker, Docker Compose |
| Документация API | SpringDoc OpenAPI (Swagger UI) |

## Функциональность

### Для кандидатов (`USER`)
- Регистрация и авторизация (форма / Yandex OAuth)
- Просмотр активных вакансий с фильтрацией по уровню, грейду, навыкам
- Прохождение mock-интервью: последовательные вопросы с записью аудиоответа
- Возможность продолжить прерванный сеанс
- Просмотр расшифровки ответов и обратной связи от интервьюера

### Для интервьюеров (`INTERVIEWER`)
- Управление компаниями и вакансиями (создание, редактирование, архивирование)
- Формирование пула вопросов к вакансии с указанием сложности (`EASY / MEDIUM / HARD`)
- Управление навыками, привязанными к вакансии
- Просмотр сессий кандидатов: аудио + автоматическая расшифровка
- Выставление оценки и решения (`Pass / Fail / Pending`) по каждой сессии

### Прочее
- Автоматическая транскрипция аудио через AssemblyAI (асинхронно, с поллингом статуса)
- Хранение аудиофайлов в MinIO с отслеживанием статуса сжатия
- RBAC: роли `USER` и `INTERVIEWER` с раздельными маршрутами
- CSRF-защита, CSP-заголовки, ограничение до одной активной сессии на пользователя
- Swagger UI для интерактивного тестирования REST API

## Структура проекта

```
src/main/java/com/technokratos/agona/
├── client/          # HTTP-клиент AssemblyAI
├── config/          # Spring-конфигурации (Security, MinIO, Async, OpenAPI)
├── contoller/       # MVC-контроллеры + REST API (/api/**)
├── dto/             # DTO и модели ответов AssemblyAI
├── enums/           # VacancyLevel, Difficulty, TranscriptionStatus, Roles, …
├── exception/       # Кастомные исключения
├── mapper/          # MapStruct: entity ↔ DTO
├── model/           # JPA-сущности (11 таблиц)
├── repository/      # Spring Data JPA репозитории
├── security/        # OAuth-хендлеры, UserDetailsService
├── service/         # Бизнес-логика (12 сервисов)
└── validation/      # Кастомные валидаторы

src/main/resources/
├── application.yaml              # Основная конфигурация
├── db/changelog/                 # Liquibase-миграции (v1, 8 DDL-файлов)
├── static/css/, static/js/       # Стили и фронтенд (запись аудио)
└── templates/                    # Thymeleaf-шаблоны (31 файл)
```

## Запуск

### Требования

- Java 17+
- Maven 3.6+ (или используйте `mvnw`)
- Docker и Docker Compose

### 1. Настройка переменных окружения

Скопируйте `.env.example` в `.env` и заполните значения:

```bash
cp .env.example .env
```

```env
DB_USERNAME=postgres
DB_PASSWORD=your_password

YANDEX_CLIENT_ID=your_yandex_client_id
YANDEX_CLIENT_SECRET=your_yandex_client_secret

ASSEMBLYAI_API_KEY=your_assemblyai_api_key

MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
```

**Получение ключей:**
- **Yandex OAuth** — [OAuth Яндекс](https://oauth.yandex.ru/), redirect URI: `http://localhost:8081/oauth/yandex/callback`
- **AssemblyAI** — [assemblyai.com](https://www.assemblyai.com/), бесплатный tier доступен

### 2. Запуск через Docker Compose (рекомендуется)

```bash
docker-compose up --build
```

Поднимает три сервиса:

| Сервис | URL |
|--------|-----|
| Приложение | http://localhost:8081 |
| MinIO Console | http://localhost:9001 |
| PostgreSQL | localhost:5433 |

### 3. Локальный запуск (без Docker)

Убедитесь, что PostgreSQL запущен на порту `5433` и MinIO на порту `9000`, затем:

```bash
./mvnw clean spring-boot:run
```

Или соберите jar и запустите:

```bash
./mvnw clean package -DskipTests
java -jar target/MockInterview-0.0.1-SNAPSHOT.jar
```

## Схема базы данных

Основные сущности и связи:

```
users ──< user_roles >── role
  │
  ├──< company ──< vacancy ──< question
  │                  │
  │                  └──>── vacancy_skill >── skill
  │
  └──< user_progress ──< user_answer ──── audio_file
           │
           └── recruiter_review

users ──── user_profile
```

| Таблица | Назначение |
|---------|------------|
| `users` | Аккаунты пользователей |
| `company` | Компании интервьюеров |
| `vacancy` | Вакансии (level, schedule, salary, archived) |
| `question` | Вопросы к вакансии (difficulty, category) |
| `skill` | Технологии / навыки |
| `user_progress` | Прогресс кандидата по сессии |
| `user_answer` | Ответ на вопрос + статус транскрипции |
| `audio_file` | Метаданные аудиофайла в MinIO |
| `recruiter_review` | Оценка и решение интервьюера |
| `user_profile` | Расширенный профиль пользователя |

Миграции управляются Liquibase (`src/main/resources/db/changelog/`).

## API

### Web-маршруты

| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| GET/POST | `/register/user` | Public | Регистрация кандидата |
| GET/POST | `/register/interviewer` | Public | Регистрация интервьюера |
| GET/POST | `/login` | Public | Вход |
| GET | `/oauth/yandex/callback` | Public | Yandex OAuth callback |
| GET | `/home` | Auth | Главная страница |
| GET | `/vacancy` | Auth | Список вакансий |
| GET/POST | `/vacancy/create` | INTERVIEWER | Создать вакансию |
| GET/POST | `/vacancy/{id}/edit` | INTERVIEWER | Редактировать вакансию |
| POST | `/vacancy/{id}/archive` | INTERVIEWER | Архивировать вакансию |
| GET/POST | `/company/**` | INTERVIEWER | Управление компаниями |
| GET | `/interviewer/**` | INTERVIEWER | Просмотр сессий кандидатов |
| GET/POST | `/screening/**` | USER | Прохождение интервью |
| GET | `/feedback/**` | USER | Просмотр обратной связи |
| GET | `/profile` | Auth | Профиль пользователя |

### REST API

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/api/answers/{id}` | Получить ответ кандидата |
| POST | `/audio/upload` | Загрузить аудиофайл |
| GET | `/swagger-ui.html` | Swagger UI (INTERVIEWER) |

## Swagger

Swagger UI доступен по адресу http://localhost:8081/swagger-ui.html (требует роль `INTERVIEWER`).

## Переменные конфигурации (`application.yaml`)

| Параметр | Описание | По умолчанию |
|----------|----------|--------------|
| `server.port` | Порт приложения | `8081` |
| `spring.datasource.url` | JDBC URL PostgreSQL | `localhost:5433/mockinterview` |
| `minio.endpoint` | URL MinIO | `http://localhost:9000` |
| `minio.bucket` | Название бакета | `audio-files` |
| `assembly-ai.polling.max-attempts` | Макс. попыток поллинга транскрипции | `30` |
| `assembly-ai.polling.interval` | Интервал поллинга (сек) | `3` |

## Роли и права доступа

```
PUBLIC:     /login, /register/**, /oauth/**, /css/**, /js/**
USER:       /screening/**, /feedback/**
INTERVIEWER:/vacancy/create, /vacancy/*/edit, /company/**, /interviewer/**, /swagger-ui/**
BOTH:       /home, /vacancy (просмотр), /audio/**, /api/**, /profile
```


# gRPC Pet Project

Демонстраційний мультимодульний Gradle-проект з використанням gRPC та Spring Boot 4.

## Опис

Проект містить два сервіси, що спілкуються між собою через gRPC **server-side streaming**:

- **grpc-server** — gRPC-сервер. Після підключення клієнта кожну хвилину відправляє повідомлення `"Хелло уорлд [mm:ss]"`, де `mm:ss` — поточний час у форматі хвилини:секунди.
- **grpc-client** — gRPC-клієнт. Підписується на стрім сервера та виводить кожне повідомлення в консоль. При розриві з'єднання автоматично перепідключається через 5 секунд.

### Схема взаємодії

```
grpc-client  ──── StreamHello() ────►  grpc-server
             ◄─── "Хелло уорлд [mm:ss]" кожну хвилину ───
```

## Стек технологій

| Компонент | Версія |
|---|---|
| Kotlin | 2.2.21 |
| Spring Boot | 4.0.6 |
| Spring gRPC | 1.0.3 |
| Protocol Buffers | 3 |
| Java | 21 |
| Docker Compose | v2 |

## Запуск через Docker Desktop

### Передумови

- Docker Desktop встановлено та запущено

### Перший запуск (збірка + старт)

```bash
docker compose up --build
```

Перший запуск займає кілька хвилин — збирає JAR-файли всередині контейнерів.

### Повторний запуск (без перезбірки)

```bash
docker compose up
```

Після старту:
- `grpc-server` слухає на порту `9090`
- `grpc-client` підключається до сервера і щохвилини виводить повідомлення в лог

### Перегляд логів клієнта

```bash
docker compose logs -f grpc-client
```

Приклад виводу:
```
grpc-client  | ... Connecting to grpc-server:9090
grpc-client  | ... Subscribing to server stream...
grpc-client  | ... >>> Хелло уорлд [11:34]
grpc-client  | ... >>> Хелло уорлд [12:34]
```

### Зупинка

```bash
docker compose down
```

## Конфігурація клієнта

Адреса сервера задається через змінні середовища (за замовчуванням — `localhost:9090`):

| Змінна              | За замовчуванням | Опис                  |
|---------------------|------------------|-----------------------|
| `GRPC_SERVER_HOST`  | `localhost`      | Хост gRPC-сервера     |
| `GRPC_SERVER_PORT`  | `9090`           | Порт gRPC-сервера     |

У `docker-compose.yml` ці змінні автоматично вказують на контейнер `grpc-server`.

## Структура проекту

```
grpc1PetProject/
├── grpc-server/
│   ├── src/main/proto/hello.proto      — контракт gRPC-сервісу
│   ├── src/main/kotlin/.../
│   │   ├── ServerApplication.kt        — точка входу
│   │   └── HelloServiceImpl.kt         — реалізація server-side streaming
│   ├── src/main/resources/application.yaml
│   └── Dockerfile
├── grpc-client/
│   ├── src/main/proto/hello.proto      — контракт (для генерації stubs)
│   ├── src/main/kotlin/.../
│   │   ├── ClientApplication.kt        — точка входу
│   │   └── HelloClient.kt              — підписка на стрім + авторепідключення
│   ├── src/main/resources/application.yaml
│   └── Dockerfile
├── docker-compose.yml
├── .dockerignore
└── settings.gradle.kts
```

## Локальний запуск (без Docker)

```bash
# Термінал 1 — сервер
./gradlew :grpc-server:bootRun

# Термінал 2 — клієнт
./gradlew :grpc-client:bootRun
```

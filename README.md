# branch01 — Unary RPC

Часть серии [grpc-laboratory](plan.md) — учебного репозитория, где каждая ветка демонстрирует одну концепцию gRPC.

---

## Концепция

**Unary RPC** — простейший тип взаимодействия в gRPC.  
Клиент отправляет **один запрос** → сервер возвращает **один ответ**.  
Аналог обычного HTTP GET-запроса, только поверх HTTP/2 с бинарной сериализацией.

```
grpc-client  ──── SayHello("World") ────►  grpc-server
             ◄──── "Hello from server, Maksim!" ────
```

### Proto-контракт

```protobuf
service HelloService {
  rpc SayHello (HelloRequest) returns (HelloResponse);
}

message HelloRequest {
  string name = 1;
}

message HelloResponse {
  string message = 1;
}
```

---

## Что реализовано

### Сервер (`grpc-server`)

- Аннотация `@GrpcService` регистрирует сервис в Spring-контексте
- Метод `sayHello` принимает `HelloRequest`, возвращает `HelloResponse`
- Ответ: `"Hello from server, {name}!"`

```kotlin
@GrpcService
class HelloServiceImpl : HelloServiceGrpc.HelloServiceImplBase() {

    override fun sayHello(
        request: HelloRequest,
        responseObserver: StreamObserver<HelloResponse>
    ) {
        val response = HelloResponse.newBuilder()
            .setMessage("Hello from server, ${request.name}!")
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}
```

### Клиент (`grpc-client`)

- Открывает `ManagedChannel` к серверу
- Использует `blockingStub` — синхронный (блокирующий) вызов
- При старте приложения отправляет запрос и выводит ответ в лог

```kotlin
val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
val stub = HelloServiceGrpc.newBlockingStub(channel)

val response = stub.sayHello(
    HelloRequest.newBuilder().setName("Maksim").build()
)
log.info(">>> ${response.message}")
```

---

## Что демонстрирует пример

| Концепция | Описание |
|---|---|
| `.proto` синтаксис | Структура service, rpc, message |
| Генерация stubs | Gradle плагин `protobuf` генерирует Java/Kotlin классы из `.proto` |
| `@GrpcService` | Регистрация gRPC-сервиса в Spring Boot |
| `blockingStub` | Синхронный вызов — поток ждёт ответа |
| `ManagedChannel` | Долгоживущее HTTP/2-соединение к серверу |
| Один запрос → один ответ | Базовая модель взаимодействия Unary RPC |

---

## Стек технологий

| Компонент | Версия |
|---|---|
| Kotlin | 2.2.21 |
| Spring Boot | 4.0.6 |
| Spring gRPC | 1.0.3 |
| Protocol Buffers | 3 |
| Java | 17 |
| Docker Compose | v2 |

---

## Запуск

### Через Docker (рекомендуется)

```bash
docker compose up --build
```

Первый запуск занимает несколько минут — сборка JAR внутри контейнеров.

После запуска:
- `grpc-server` слушает на порту `9090`
- `grpc-client` подключается, отправляет запрос и выводит ответ в лог

### Просмотр логов

```bash
docker compose logs -f grpc-client
```

Ожидаемый вывод:

```
grpc-client  | ... Connecting to grpc-server:9090
grpc-client  | ... Sending request: name=Maksim
grpc-client  | ... >>> Hello from server, Maksim!
```

### Остановка

```bash
docker compose down
```

### Локальный запуск (без Docker)

```bash
# Терминал 1 — сервер
./gradlew :grpc-server:bootRun

# Терминал 2 — клиент
./gradlew :grpc-client:bootRun
```

---

## Конфигурация клиента

Адрес сервера задаётся через переменные окружения:

| Переменная | По умолчанию | Описание |
|---|---|---|
| `GRPC_SERVER_HOST` | `localhost` | Хост gRPC-сервера |
| `GRPC_SERVER_PORT` | `9090` | Порт gRPC-сервера |

В `docker-compose.yml` эти переменные автоматически указывают на контейнер `grpc-server`.

---

## Структура проекта

```
grpc-laboratory/
├── grpc-server/
│   ├── src/main/proto/hello.proto          — gRPC контракт (HelloService)
│   ├── src/main/kotlin/.../
│   │   ├── ServerApplication.kt            — точка входа
│   │   └── HelloServiceImpl.kt             — реализация SayHello
│   ├── src/main/resources/application.yaml
│   └── Dockerfile
├── grpc-client/
│   ├── src/main/proto/hello.proto          — контракт (для генерации stubs)
│   ├── src/main/kotlin/.../
│   │   ├── ClientApplication.kt            — точка входа
│   │   └── HelloClient.kt                  — blockingStub вызов
│   ├── src/main/resources/application.yaml
│   └── Dockerfile
├── docker-compose.yml
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Навигация по веткам

| Ветка | Концепция |
|---|---|
| `main` | Базовая инфраструктура |
| **`branch01_Unary-RPC`** | **Один запрос → один ответ** ← вы здесь |
| `branch02_Server-Streaming` | Сервер шлёт поток клиенту |
| `branch03_Client-Streaming` | Клиент шлёт поток серверу |
| `branch04_Bidirectional-Streaming` | Двунаправленный стриминг |
| `branch05_Deadlines-Timeouts` | Ограничение времени RPC |
| `branch06_Cancelling-RPC` | Отмена RPC со стороны клиента |
| `branch07_Sync-vs-Async-clients` | blockingStub vs asyncStub |
| `branch08_Channels-lifecycle` | Управление ManagedChannel |
| `branch09_Error-handling` | gRPC Status codes |
| `branch10_Metadata-Interceptors` | Metadata, Auth, Interceptors |
| `branch11_Final-monitoring-system` | Production-like финальный пример |
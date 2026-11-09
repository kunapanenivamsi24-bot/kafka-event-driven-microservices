# Kafka Event-Driven Microservices

A portfolio demo project that shows an event-driven microservices pipeline built with
**Java 17, Spring Boot 3.2.5, Apache Kafka, and Maven (multi-module)**.

> **Demo disclaimer:** This is a portfolio project built to showcase the stack. It is
> not production code and it is not code from any employer — it was written from scratch
> for demonstration purposes. It does not represent Morgan Stanley or any other company.

Built by [Sai Sasi Vamsi Kunapaneni](https://www.linkedin.com/in/vamsi-kunapaneni-6ab48831a/).

## What it does

A REST client posts a payment to the **producer-service**, which publishes a
`PaymentEvent` to the Kafka topic `payments`. The **consumer-service** consumes the
event, validates it, and processes it. Invalid events are retried with exponential
backoff and, after the retries are exhausted, routed to the dead-letter topic
`payments-dlt`.

## Architecture

```
                    +------------------+
  curl / REST client| producer-service  |  POST /api/payments/publish
  ----------------->|  (port 8081)      |  -> PaymentEventPublisher
                    +------------------+
                             |
                             | KafkaTemplate.send()
                             v
                    +------------------+
                    |  Kafka (KRaft)    |  topic: payments
                    |  localhost:9092   |
                    +------------------+
                             |
                             | @KafkaListener
                             v
                    +------------------+
                    | consumer-service  |  PaymentEventProcessor
                    |  (port 8082)      |  (validates amount > 0)
                    +------------------+
                             |
              invalid event  |  @RetryableTopic: 4 attempts,
              (amount <= 0)  |  backoff 1s x2.0 on retry topics
                             v
                    +------------------+
                    | payments-dlt      |  @DltHandler logs the
                    | (dead-letter      |  dead-lettered event
                    |  topic)           |
                    +------------------+
```

## Module layout

| Module | Contents |
| --- | --- |
| `common` | Shared event model: `PaymentEvent`, `PaymentEventType`, `Topics` constants. Plain jar, no Spring Boot plugin. |
| `producer-service` | Spring Boot app (port 8081). REST controller `POST /api/payments/publish`, `PaymentEventPublisher` (KafkaTemplate + JsonSerializer, `acks=all`, idempotent producer). |
| `consumer-service` | Spring Boot app (port 8082). `@KafkaListener` on `payments`, `PaymentEventProcessor` (validates amount > 0), `@RetryableTopic` (4 attempts, backoff 1s × 2.0) with a `@DltHandler` for `payments-dlt`. |

## Prerequisites

- Java 17 (Temurin or any JDK 17)
- Maven 3.9+
- Docker + Docker Compose (only for the full end-to-end run; tests need neither)

## Run locally with Docker Compose

```bash
docker compose up --build
```

This starts Kafka in KRaft mode (no ZooKeeper), the producer on port 8081, and the
consumer on port 8082. Wait for all three containers to be healthy, then publish
a test event.

To stop:

```bash
docker compose down
```

## Publish a test event

```bash
curl -X POST http://localhost:8081/api/payments/publish \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "txn-123",
    "accountId": "acct-456",
    "amount": 250.00,
    "currency": "USD"
  }'
```

Expected response — `202 Accepted`:

```json
{
  "eventId": "3f2a1c9e-...",
  "transactionId": "txn-123",
  "status": "ACCEPTED"
}
```

Watch the consumer logs (`docker compose logs -f consumer-service`) to see the event
processed. To see the retry / dead-letter flow, publish an event that fails
validation — for example with `"amount": -5.00` — and watch it retry on the
`payments-retry-*` topics and finally land in `payments-dlt`.

## Run the tests

Unit tests only — the KafkaTemplate and the processor are mocked, so no broker,
embedded Kafka, or Docker is needed:

```bash
mvn test
```

Tests cover: publisher sends to the right topic keyed by transaction id; the
controller returns 202 with an `eventId` and rejects invalid payloads with 400;
the processor accepts valid events and throws on invalid amounts; the listener
delegates to the processor, propagates failures (which is what triggers retry),
and the DLT handler logs without throwing.

## Configuration

| Property | Default | Description |
| --- | --- | --- |
| `server.port` | 8081 (producer) / 8082 (consumer) | HTTP port |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrap servers (`spring.kafka.bootstrap-servers`) |
| `spring.kafka.consumer.group-id` | `payment-consumers` | Consumer group id |
| `spring.kafka.consumer.auto-offset-reset` | `earliest` | Start from the beginning of the topic on first run |
| `spring.kafka.producer.acks` | `all` | Producer acknowledgement level |
| `spring.json.trusted.packages` | `com.events.common` | Packages trusted for JSON deserialization |

## License

MIT — see [LICENSE](LICENSE).

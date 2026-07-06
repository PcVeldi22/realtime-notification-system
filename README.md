# realtime-notification-system

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.6-black.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

> Enterprise multi-channel real-time notification platform built to handle high-throughput event fan-out across WebSocket, push, email, and SMS channels.

## Overview

This service was designed around a common enterprise problem: dozens of internal systems need to notify users in real time, but each one reinventing delivery logic, retries, and channel preferences leads to duplicated effort and inconsistent behavior. This platform centralizes that concern behind a single event-driven API.

Producers publish a `NotificationEvent` to Kafka. The platform fans that event out to every channel a user has opted into — an active WebSocket session for in-app alerts, a push provider for mobile, email for digest-style updates, and SMS for critical alerts — while respecting per-user rate limits and quiet hours.

## Architecture

```
 Producer Services
 |
 v
 +------------------+
 | Kafka Topic |
 | notification.* |
 +------------------+
 |
 v
 +---------------------------+
 | Notification Consumer |
 | (this service) |
 +---------------------------+
 | | | |
 v v v v
WebSocket Push Email SMS
Session Provider Provider Provider
 (Redis (FCM/APNs) (SES) (Twilio)
 pub/sub)
```

Key design decisions:

- **Kafka as the ingestion boundary** — decouples producers from delivery concerns and gives us replay/backfill for free if a channel provider has an outage.
- **Redis pub/sub for WebSocket fan-out** — because the service runs behind a load balancer with multiple instances, a user's WebSocket connection may be held by a different pod than the one that consumed the Kafka message. Redis pub/sub broadcasts the delivery to every instance so whichever pod holds the session can push it.
- **Per-channel circuit breakers** — if the email or SMS provider starts failing, we degrade gracefully instead of backing up the whole consumer group.
- **Idempotency keys** — every notification carries a UUID so retried Kafka deliveries never double-notify a user.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2, Spring WebSocket (STOMP) |
| Messaging | Apache Kafka |
| Fan-out / Cache | Redis (pub/sub + rate-limit counters) |
| Persistence | PostgreSQL (notification history, user preferences) |
| Delivery Providers | Firebase Cloud Messaging, AWS SES, Twilio (interfaces provided, mocked in dev profile) |
| Containerization | Docker, docker-compose |
| CI | GitHub Actions |

## Project Structure

```
src/main/java/com/pcveldi/notification/
├── NotificationApplication.java
├── config/
│ ├── KafkaConfig.java
│ ├── RedisConfig.java
│ └── WebSocketConfig.java
├── controller/
│ ├── NotificationController.java
│ └── PreferenceController.java
├── service/
│ ├── NotificationDispatchService.java
│ ├── ChannelRouterService.java
│ └── RateLimiterService.java
├── repository/
│ ├── NotificationRepository.java
│ └── UserPreferenceRepository.java
├── model/
│ ├── NotificationEvent.java
│ ├── NotificationRecord.java
│ └── UserPreference.java
├── dto/
│ └── NotificationRequest.java
└── exception/
 └── GlobalExceptionHandler.java
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & docker-compose (for local Kafka, Redis, Postgres)

### Run locally

```bash
# Spin up Kafka, Redis, and Postgres
docker-compose up -d

# Run the application
mvn spring-boot:run
```

The service starts on port `8087`. WebSocket clients connect at `ws://localhost:8087/ws/notifications`.

### Publish a test event

```bash
curl -X POST http://localhost:8087/api/v1/notifications \
 -H "Content-Type: application/json" \
 -d '{"userId": "user-123", "title": "Order shipped", "body": "Your order is on its way", "channels": ["WEBSOCKET", "EMAIL"]}'
```

## Scalability Notes

The consumer group is partitioned by `userId` so ordering is preserved per user while still allowing horizontal scale-out — adding consumer instances increases throughput linearly up to the partition count. Rate limiting is implemented with a Redis-backed sliding window rather than an in-memory counter specifically so limits hold correctly across multiple running instances.

## Running Tests

```bash
mvn test
```

## License

MIT

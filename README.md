# Trading Platform API

This is a trading platform API built with Spring Boot, Java, and PostgreSQL. It supports user management, order entry, and order processing flows.

This project is intended to run alongside the sibling C++ market-data service in `../fluffy-parakeet`. `probable-fiesta` is the trading API, and `fluffy-parakeet` supplies live market prices over HTTP and WebSocket.

## Prerequisites

- Java 17 or higher
- PostgreSQL 16.3
- Gradle 7.6 or higher
- Docker [Docker Installation Guide](https://docs.docker.com/get-docker/)

## Getting Started

### Clone the repository

```bash
git clone https://github.com/yourusername/trading-platform-api.git

cd trading-platform-api

cp .env.example .env

# edit .env if you want different local credentials

docker-compose up -d

```
**Do not commit real secrets.** Local Docker credentials and app secrets should go in `.env`, which is gitignored.
**Use `.env.example` as the committed template and keep real values only in `.env`.**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/trading_platform
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  application:
    name: Trading Platform API

server:
  port: 8081

market-data:
  base-url: http://localhost:8080
  websocket-url: ws://localhost:8080/ws
```

```yaml
services:
  db:
    image: postgres:16
    container_name: trading_platform_db
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-trading_platform}
      POSTGRES_USER: ${POSTGRES_USER:-postgres}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-change-me}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-postgres} -d ${POSTGRES_DB:-trading_platform}"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build:
      context: .
      dockerfile: Dockerfile
    image: trading-platform-api:latest
    container_name: trading_platform_api
    network_mode: host
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/${POSTGRES_DB:-trading_platform}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-postgres}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-change-me}
      JWT_SECRET: ${JWT_SECRET:-change-this-jwt-secret}
      MARKET_DATA_BASE_URL: ${MARKET_DATA_BASE_URL:-http://127.0.0.1:8080}
      MARKET_DATA_WEBSOCKET_URL: ${MARKET_DATA_WEBSOCKET_URL:-ws://127.0.0.1:8080/ws}
    depends_on:
      db:
        condition: service_healthy

volumes:
  postgres_data:
```

## Running With The C++ Service

`probable-fiesta` depends on `../fluffy-parakeet` for market-price lookups used by market-order creation.

Recommended local startup order:

1. Start the C++ market-data service from `../fluffy-parakeet`
2. Confirm it responds on `8080`
3. Start this API on `8081`

### Test the market data API connection

If the C++ market data service is running locally on port `8080`, you can verify the Java client endpoint contract manually with:

```bash
curl -i http://localhost:8080/api/market/prices/BTC%2FUSD
```

Then verify this API can authenticate against the local environment:

```bash
curl -X POST http://localhost:8081/api/v1/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}'
```

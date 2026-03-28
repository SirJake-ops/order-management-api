# Trading Platform API

This is a trading platform API built with Spring Boot, Java, and PostgreSQL. It supports user management, order entry, and order processing flows.

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

docker-compose up -d

```
**You can change the database configuration in the `application.yml` file.**
**You can also change the docker-compose file to use a different database of your choosing.**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/trading_platform
    username: postgres
    password: postgres
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
```

```yaml
version: '3.8'

services:
  db:
    image: postgres:latest
    container_name: trading_platform_db
    environment:
      POSTGRES_DB: trading_platform
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    image: openjdk:17-jdk-slim
    container_name: trading_platform_api
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/trading_platform
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      MARKET_DATA_BASE_URL: http://host.docker.internal:8080
    ports:
      - "8081:8081"
    volumes:
      - .:/app
    working_dir: /app
    command: ./gradlew bootRun
    depends_on:
      - db

volumes:
  postgres_data:
```

### Test the market data API connection

If the C++ market data service is running locally on port `8080`, you can verify the Java client endpoint contract manually with:

```bash
curl -i http://localhost:8080/api/market/prices/BTC%2FUSD
```

Expected result:
- `200 OK`
- JSON response containing `symbol`, `bid`, `ask`, `last`, `volume`, and `timestamp`

This is intended as a manual connectivity check, not an automated test, since the Java and C++ services may not always be running at the same time.

# Integration Roadmap

## Goal

Bring `order-management-api` to the point where it can run cleanly with:

- `trading-exchange-engine` as the market-data, order-book, and matching-engine service.
- `load_balancer` as the TCP entry point for one or more Java API instances.

The first useful milestone is a local three-service stack where a client can authenticate through the Java API, create an order, have market data or execution state sourced from the C++ engine, and reach the Java API through the Rust load balancer.

## Service Boundaries

### Order Management API, Java/Spring Boot

Owns:

- User accounts and authentication.
- JWT issuing and request authorization.
- Public HTTP API for order workflows.
- Durable order records and user-facing order status.
- Translation between API DTOs and exchange-engine requests/responses.
- Resilience behavior when the exchange engine is unavailable.

Should not own long-term:

- Real-time matching logic.
- Authoritative order book state.
- Trade generation.
- Market price simulation.

### Trading Exchange Engine, C++

Owns:

- Market price generation and latest price snapshots.
- WebSocket market-data streaming.
- Order book state by symbol.
- Matching, cancellation, trades, and execution reports.
- Exchange-domain validation that depends on instrument rules or book state.

### Load Balancer, Rust

Owns:

- Public TCP listener for API traffic.
- Backend selection across healthy Java API instances.
- TCP health checks and connection-level telemetry.
- Routing to API backends, not market-data ownership.

## Port Plan

Current observed defaults:

| Service | Current port | Notes |
| --- | ---: | --- |
| C++ exchange engine | `8080` | Java market-data client expects this for HTTP and WebSocket. |
| Java order API | `8081` | Spring Boot server port. |
| Rust load balancer | `8080` | Sample config conflicts with the C++ service. |

Recommended local plan:

| Service | Recommended local port | Reason |
| --- | ---: | --- |
| C++ exchange engine | `8080` | Keep existing Java `MARKET_DATA_*` defaults simple. |
| Java order API instance 1 | `8081` | Existing default. |
| Java order API instance 2 | `8082` | Useful for load-balancer round-robin testing. |
| Rust load balancer | `8090` | Avoids collision with exchange-engine market data. |

## Target Local Flow

1. Start `trading-exchange-engine` on `8080`.
2. Start Postgres and one Java API instance on `8081`.
3. Verify `POST /api/v1/auth/token` on the Java API.
4. Verify the Java API can fetch `GET /api/market/prices/{symbol}` from the C++ engine.
5. Start a second Java API instance on `8082` with the same database and JWT settings.
6. Start `load_balancer` on `8090` with Java API backends `8081` and `8082`.
7. Verify auth and order endpoints through `http://127.0.0.1:8090`.

## Milestones

### Milestone 1: Local Runtime Stability

Purpose: make this API predictable to run before changing domain behavior.

Deliverables:

- Valid `.env.example` values for local startup.
- One documented Docker Compose path for rebuild/recreate.
- Health/readiness endpoints that do not require auth.
- Smoke-test commands for auth, database connectivity, and market-data dependency.

### Milestone 2: Market Data Contract Alignment

Purpose: make Java-to-C++ market-data calls explicit and testable.

Deliverables:

- Documented market-data HTTP response contract.
- Java DTOs aligned with C++ response fields.
- Tests for success, not found, malformed response, and exchange-engine unavailable cases.
- Optional WebSocket stream state surfaced through a clear application service.

### Milestone 3: Order Gateway Shape

Purpose: make Java own API-facing order workflow while C++ owns matching.

Deliverables:

- Canonical order request DTO for market and limit orders.
- Client order id support for idempotency.
- Clear status mapping between Java order statuses and C++ engine statuses.
- Adapter boundary for submitting orders to the C++ engine once its order-entry API is ready.

### Milestone 4: Load-Balancer Compatibility

Purpose: make the Java API safe to run behind the Rust TCP load balancer.

Deliverables:

- Unauthenticated readiness endpoint for backend health checks.
- Stable, instance-safe JWT and database configuration.
- Support for multiple API instances on different ports.
- Smoke tests through the load balancer listener.

### Milestone 5: End-To-End Trading Flow

Purpose: prove the three services work together.

Deliverables:

- Auth token issued by Java API through the load balancer.
- Order submitted through the load balancer.
- Java API persists the order and submits it to C++ engine.
- C++ engine returns accepted/rejected/executed state.
- Java API stores and exposes the resulting order state.
- Market data and order execution behavior are covered by an end-to-end test or scripted smoke test.

## Open Decisions

- Whether the Rust load balancer should remain a raw TCP proxy only, or later become HTTP-aware for health and routing.
- Whether Java should persist every order before C++ acceptance, or persist only after C++ acceptance with a rejected audit record for failures.
- Whether order ids should be generated by Java, C++, or provided as client order ids with an exchange-generated id returned later.
- Whether market-data WebSocket updates should be consumed by Java for cache/state, passed directly to clients from C++, or proxied through another gateway.

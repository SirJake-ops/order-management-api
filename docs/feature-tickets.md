# Order Management API Feature Tickets

These tickets are scoped to `order-management-api`. Related work in the sibling projects should stay in their own backlogs unless the acceptance criteria here explicitly mention an integration contract.

## Status Legend

- `Backlog`: not started.
- `Ready`: requirements are clear enough to implement.
- `In Progress`: active work.
- `Blocked`: waiting on another service or decision.
- `Done`: accepted.

## Epic: Runtime And Developer Experience

### OMS-001: Stabilize Local Runtime Configuration

**Priority:** P0  
**Type:** Chore  
**Status:** Ready

**Story:** As a developer, I want local environment defaults that boot reliably so I can start the API without rediscovering secret, database, or port issues.

**Acceptance Criteria:**

- `.env.example` contains valid local values for Postgres, JWT, and market-data URLs.
- `JWT_SECRET` default is long enough for `JwtService` to initialize without Base64 decoding errors.
- README documents when to use `docker compose up -d --build app` versus `docker compose up -d --force-recreate app`.
- README documents how to align an existing Postgres volume password without deleting data.
- Local startup smoke test is documented.

**Notes:** The app currently requires a JWT secret of at least 32 characters, otherwise it treats the value as Base64.

### OMS-002: Add Unauthenticated Health And Readiness Endpoints

**Priority:** P0  
**Type:** Feature  
**Status:** Ready

**Story:** As the load balancer, I want a lightweight endpoint that reports API readiness without authentication so unhealthy Java instances can be removed from rotation.

**Acceptance Criteria:**

- `GET /health/live` returns `200` when the JVM and HTTP server are alive.
- `GET /health/ready` returns `200` only when required dependencies are usable.
- Readiness checks include Postgres connectivity.
- Readiness includes market-data connectivity only if the dependency is configured as required.
- Spring Security permits these endpoints without JWT auth.
- Tests cover authorized bypass and dependency failure behavior.

**Dependencies:** None.

### OMS-003: Add Scripted Local Smoke Test

**Priority:** P1  
**Type:** Chore  
**Status:** Backlog

**Story:** As a developer, I want one repeatable smoke test command so I can verify the API, database, auth, and market-data configuration after rebuilds.

**Acceptance Criteria:**

- Script or documented command verifies Compose services are running.
- Verifies auth token issuance through `POST /api/v1/auth/token`.
- Verifies database-backed user lookup indirectly through auth.
- Verifies market-data lookup when the C++ service is running.
- Exits non-zero when any required check fails.

**Dependencies:** OMS-002.

## Epic: Market Data Integration

### OMS-010: Document The Java-To-C++ Market Data Contract

**Priority:** P0  
**Type:** Documentation  
**Status:** Ready

**Story:** As an API developer, I want the expected market-data HTTP and WebSocket contracts documented so Java and C++ changes can be made without guessing field names or status behavior.

**Acceptance Criteria:**

- Document `GET /api/market/prices/{symbol}` request format, URL encoding rules, response fields, and error statuses.
- Document WebSocket URL and supported message formats used by Java.
- Include example responses for success and not found.
- Link or reference the C++ backlog item that owns any missing contract behavior.
- Java DTO field names are compared against the documented C++ response.

**Dependencies:** None.

### OMS-011: Harden MarketDataClient Error Handling

**Priority:** P0  
**Type:** Feature  
**Status:** Ready

**Story:** As an order API user, I want clear errors when market data is unavailable so market order failures are predictable and actionable.

**Acceptance Criteria:**

- `404` from C++ maps to `MarketPriceNotFoundException`.
- Connection failures map to `MarketDataUnavailableException`.
- Malformed JSON maps to a clear market-data integration exception.
- Timeout settings are configurable.
- Unit tests cover success, not found, server error, timeout/connection failure, and malformed JSON.

**Dependencies:** OMS-010.

### OMS-012: Add Market Data Dependency Mode

**Priority:** P1  
**Type:** Feature  
**Status:** Backlog

**Story:** As a developer, I want to decide whether market data is required at startup so I can run API-only workflows without the C++ service while still failing fast in integrated mode.

**Acceptance Criteria:**

- Add config such as `market-data.required` with a local default documented in README.
- If required, readiness fails when the C++ service is unreachable.
- If optional, startup succeeds and market orders fail clearly when prices cannot be fetched.
- Tests cover both required and optional modes.

**Dependencies:** OMS-002, OMS-011.

## Epic: Order API Maturity

### OMS-020: Define Canonical Order Request And Response DTOs

**Priority:** P0  
**Type:** Feature  
**Status:** Ready

**Story:** As an API client, I want one consistent order creation contract so market and limit orders are validated the same way before they are persisted or sent to the exchange engine.

**Acceptance Criteria:**

- Define a canonical create-order request with `symbol`, `side`, `type`, `quantity`, optional `price`, and optional `clientOrderId`.
- Market orders do not require `price`.
- Limit orders require positive `price`.
- Quantity must be positive.
- Unsupported symbols are rejected according to the current instrument source.
- Response includes Java order id, client order id if provided, status, symbol, side, type, quantity, price, filled quantity, and timestamps.
- Existing placeholder `/market` and `/limit` behavior is removed or redirected into the canonical flow.
- Controller tests cover valid market, valid limit, invalid quantity, missing limit price, and unsupported enum values.

**Dependencies:** OMS-010.

### OMS-021: Add Order Idempotency With Client Order IDs

**Priority:** P1  
**Type:** Feature  
**Status:** Backlog

**Story:** As an API client, I want repeated submissions with the same client order id to be handled safely so retries do not create duplicate orders.

**Acceptance Criteria:**

- Order create request accepts a `clientOrderId` scoped to the authenticated user.
- Duplicate `clientOrderId` for the same user returns the existing order result or a documented conflict response.
- Duplicate behavior is covered by tests.
- Database constraint prevents duplicate active records for the same user/client order id pair.

**Dependencies:** OMS-020.

### OMS-022: Align Java Order Statuses With Exchange Engine Statuses

**Priority:** P0  
**Type:** Feature  
**Status:** Ready

**Story:** As a developer, I want one status mapping between Java orders and exchange-engine results so API clients see consistent order lifecycle states.

**Acceptance Criteria:**

- Document mapping for pending/new, accepted, rejected, partially filled, filled, cancelled, and failed/unavailable states.
- Java `OrderStatus` is updated if required.
- Persistence migration supports any new statuses.
- Service layer maps exchange responses into Java statuses.
- Tests cover each mapped status.

**Dependencies:** C++ engine order-entry response shape from EX-020/EX-012.

### OMS-023: Add Exchange Engine Order Adapter Interface

**Priority:** P0  
**Type:** Feature  
**Status:** Blocked

**Story:** As the Java order service, I want an adapter boundary for submitting orders to the C++ engine so transport details do not leak into order workflow code.

**Acceptance Criteria:**

- Define an interface such as `ExchangeOrderClient` for submit, cancel, and query operations.
- Provide request/response DTOs separate from public API DTOs.
- Add a no-op or stub implementation for local development until the C++ order-entry API is available.
- Add tests proving `OrderService` depends on the interface, not a concrete HTTP client.
- Map adapter failures to domain exceptions.

**Dependencies:** C++ engine EX-020 for `POST /api/orders` or an equivalent order-entry API.

### OMS-024: Persist Exchange Submission Results

**Priority:** P1  
**Type:** Feature  
**Status:** Blocked

**Story:** As an API user, I want submitted orders to show their exchange result so I can tell whether an order was accepted, rejected, filled, or partially filled.

**Acceptance Criteria:**

- Persist exchange order id when returned by C++.
- Persist rejection reason when an order is rejected.
- Persist filled quantity and average execution price when trades are returned.
- Publish application events for accepted, rejected, matched, and cancelled outcomes.
- Tests cover accepted, rejected, partial fill, full fill, and exchange unavailable cases.

**Dependencies:** OMS-022, OMS-023.

## Epic: Load Balancer Compatibility

### OMS-030: Support Multiple API Instances Locally

**Priority:** P0  
**Type:** Chore  
**Status:** Backlog

**Story:** As a developer, I want to run two Java API instances on separate ports so the Rust load balancer can route real traffic across them.

**Acceptance Criteria:**

- Document how to run instance 1 on `8081` and instance 2 on `8082`.
- Both instances share the same Postgres database and JWT settings.
- Both instances expose health/readiness endpoints.
- Auth token issued by one instance is accepted by the other.
- Smoke test demonstrates requests through both backend ports.

**Dependencies:** OMS-002.

### OMS-031: Add Load Balancer Local Config Example

**Priority:** P1  
**Type:** Documentation  
**Status:** Backlog

**Story:** As a developer, I want a known-good load balancer config for this API so I can avoid port collisions with the C++ engine.

**Acceptance Criteria:**

- Document recommended Rust listener port `8090` for local integration.
- Document backend list with Java API instances `127.0.0.1:8081` and `127.0.0.1:8082`.
- Note that C++ market data remains on `8080`.
- Include smoke test commands through `http://127.0.0.1:8090`.
- Cross-reference the Rust load balancer config file location.

**Dependencies:** OMS-030.

### OMS-032: Verify API Behavior Through TCP Load Balancer

**Priority:** P1  
**Type:** Test  
**Status:** Backlog

**Story:** As a developer, I want to verify the API through the Rust TCP load balancer so integration issues are caught before higher-level order-flow work.

**Acceptance Criteria:**

- Auth token request succeeds through the load balancer listener.
- Authenticated order create request succeeds through the load balancer listener.
- Repeated requests are distributed across Java API instances when two are running.
- Failure of one backend does not route new requests to it once Rust health checks are implemented.
- Test results are documented in this repo.

**Dependencies:** OMS-030, OMS-031, Rust LB-015.

## Epic: End-To-End Trading Flow

### OMS-040: Add Cross-Service Local Runbook

**Priority:** P0  
**Type:** Documentation  
**Status:** Ready

**Story:** As a developer, I want one runbook for starting all three services so I can reproduce integration behavior consistently.

**Acceptance Criteria:**

- Runbook starts C++ exchange engine first.
- Runbook starts Postgres and Java API next.
- Runbook optionally starts a second Java API instance.
- Runbook starts Rust load balancer last.
- Includes verification commands for each service.
- Includes common failure modes: port collision, JWT secret, Postgres volume password mismatch, missing C++ service.

**Dependencies:** OMS-001, OMS-031.

### OMS-041: Add End-To-End Order Submission Smoke Test

**Priority:** P1  
**Type:** Test  
**Status:** Blocked

**Story:** As a developer, I want one scripted flow that authenticates, submits an order, and verifies resulting order state across services.

**Acceptance Criteria:**

- Obtains JWT from Java API through the Rust load balancer.
- Submits a market or limit order through the Rust load balancer.
- Verifies Java persisted the order.
- Verifies C++ exchange engine accepted or rejected the order according to its response.
- Verifies final API response/status is stable and documented.
- Script exits non-zero on any failed step.

**Dependencies:** OMS-023, OMS-024, C++ EX-020, Rust LB-015.

### OMS-042: Define Cross-Service Observability Fields

**Priority:** P2  
**Type:** Chore  
**Status:** Backlog

**Story:** As a developer, I want shared correlation fields so a request can be followed through the load balancer, Java API, and exchange engine.

**Acceptance Criteria:**

- Define request id and client order id propagation rules.
- Java logs include request id, user id, order id, client order id, symbol, and exchange order id when available.
- Rust logs include request/connection id and selected backend id.
- C++ logs include request id or client order id for order-entry paths when available.
- Document example log lines for one successful order flow.

**Dependencies:** OMS-020, Rust LB-016, C++ EX-020.

# Order Management API Docs

This folder tracks the work needed to bring `order-management-api` up to pace with its sibling services:

- `/home/jacobp/RustroverProjects/load_balancer`
- `/home/jacobp/cpp-projects/trading-exchange-engine`

## Documents

- [Integration roadmap](./integration-roadmap.md): target architecture, service boundaries, ports, and sequencing.
- [Feature tickets](./feature-tickets.md): implementation-ready stories for this Java API.

## Current Integration Baseline

- `order-management-api` runs on port `8081` and owns users, authentication, order persistence, and API-facing order workflows.
- `trading-exchange-engine` runs as the market-data and matching-engine service. It already exposes HTTP market-price lookup and WebSocket price streaming, and its backlog is moving toward order-entry and execution reports.
- `load_balancer` is a transparent TCP load balancer. Its sample config listens on `127.0.0.1:8080` and routes to backends including `127.0.0.1:8081`, which currently conflicts with the exchange engine using port `8080` for market data.

## Planning Principle

Do not duplicate matching-engine ownership in this Java API. This service should become the authenticated order gateway and persistence layer, while the C++ engine owns real-time matching, market data, book state, trades, and execution reports.

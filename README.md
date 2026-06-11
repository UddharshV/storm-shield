# StormShield: Weather-Triggered Operational Alerting System

StormShield is a proactive event-driven platform designed to monitor environmental hazards across shipping lanes and automatically execute operational response playbooks. This system mimics the core architecture of enterprise IT Service Management (ITSM) and automation platforms like ServiceNow.

---

## 🏗️ Architecture & Core Concepts Learnt

### 1. Configuration Management & Asset Tracking (Data Layer)
In enterprise IT operations, you cannot protect infrastructure if you do not know what assets exist. I implemented the foundational data blueprint representing a freight carrier's physical assets:
* **The Domain Model (`FreightRoute`):** Represents shipping lanes connecting critical regional hubs. It tracks operational states using dynamic statuses (`OPERATIONAL`, `DELAYED`, `REROUTED`).
* **The Abstraction Layer (`JpaRepository`):** Leveraged Spring Data JPA to decouple the business logic from raw database communication, eliminating brittle, manual SQL plumbing.

### 2. Live API Ingestion & Defensive Engineering
To move from a static system to a living, reactive operational tool, I built a resilient live data ingestion pipeline:
* **Decoupled HTTP Communication:** Implemented Spring Boot's modern `RestClient` using constructor injection. This prevents rigid code structures and allows the entire networking layer to be fully mockable for high-fidelity unit testing.
* **Stable Configuration Management:** Extracted API parameters into centralized configuration properties (`weather.api.base-url`), establishing a stable architectural foundation if upstream providers update their endpoints or resource versions.
* **Automated Polling Daemons:** Leveraged Spring's internal scheduling engine (`@EnableScheduling` and `@Scheduled`) to build a background worker thread that continuously monitors freight lanes without human intervention.

### 3. Enterprise Security & Credential Safeguards
Following defensive security standards, all production variables and API tokens have been completely scrubbed from code tracking. The application relies on environmental abstraction layer placeholding (`${OPENWEATHER_API_KEY}`), feeding active credentials at runtime through untracked operating system environment variables (`.env` files barred via `.gitignore`).

### 4. Centralized Exception Sanitization
Leaking internal system stack traces or downstream network connection drops into production logs or public API responses poses an operational security risk. I implemented a global exception interceptor framework using `@ControllerAdvice`:
* Automatically catches application pipeline failures.
* Logs raw, dirty stack details internally for reliability engineering review.
* Translates the response into a clean, sanitized JSON object before presenting it to the outside world.

---

## 🛠️ Tech Stack Implemented
* **Backend Framework:** Java / Spring Boot 3.x / Spring Web
* **Data Access:** Spring Data JPA (Java Persistence API)
* **Database Engine:** H2 In-Memory Database Engine
* **Testing Frameworks:** JUnit 5 / Mockito (True dependency-mocked unit testing)

---

## 🚦 Verification & Test Suite Progress
The application code enforces a strict Test-Driven testing process. By isolating dependencies (such as fluent client builders and database repositories) via Mockito stubs, the tests validate actual data-parsing logic and state-validation rules:
* **`WeatherServiceTest`:** Verifies URL execution, payload map navigation (`wind.speed`), and proper error raising.
* **`WeatherMonitoringSchedulerTest`:** Validates background control loop traversal across active database entities.
* **Localized Database Controls:** Verified via H2 Web Console wrapper testing (`SELECT * FROM FREIGHT_ROUTE;`) that system state initializes correctly on startup.

**Current Sprint Status:** Phase 1 (Live API Data Ingestion & Security) is complete. Ready to transition to Phase 2 (Event-Driven Automation / ServiceNow Workflows).
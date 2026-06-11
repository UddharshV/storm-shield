# StormShield: Weather-Triggered Operational Alerting System

StormShield is a proactive event-driven platform designed to monitor environmental hazards across shipping lanes and automatically execute operational response playbooks. This system mimics the core architecture of enterprise IT Service Management (ITSM) and automation platforms like ServiceNow.

---

## 🏗️ Architecture & Core Concepts Learnt

### 0. Configuration Management & Asset Tracking (Data Layer)
In enterprise IT operations, you cannot protect infrastructure if you do not know what assets exist. I implemented the foundational data blueprint representing a freight carrier's physical assets:
* **The Domain Model (`FreightRoute`):** Represents shipping lanes connecting critical regional hubs. It tracks operational states using dynamic statuses (`OPERATIONAL`, `DELAYED`, `REROUTED`).
* **The Abstraction Layer (`JpaRepository`):** Leveraged Spring Data JPA to decouple the business logic from raw database communication, eliminating brittle, manual SQL plumbing.

### 1. Live API Ingestion & Defensive Engineering
To move from a static system to a living, reactive operational tool, I built a resilient live data ingestion pipeline:
* **Decoupled HTTP Communication:** Implemented Spring Boot's modern `RestClient` using constructor injection. This prevents rigid code structures and allows the entire networking layer to be fully mockable for high-fidelity unit testing.
* **Stable Configuration Management:** Extracted API parameters into centralized configuration properties (`weather.api.base-url`), establishing a stable architectural foundation if upstream providers update their endpoints or resource versions.
* **Automated Polling Daemons:** Leveraged Spring's internal scheduling engine (`@EnableScheduling` and `@Scheduled`) to build a background worker thread that continuously monitors freight lanes without human intervention.

### 2. Enterprise Security & Credential Safeguards
Following defensive security standards, all production variables and API tokens have been completely scrubbed from code tracking. The application relies on environmental abstraction layer placeholding (`${OPENWEATHER_API_KEY}`), feeding active credentials at runtime through untracked operating system environment variables (`.env` files barred via `.gitignore`).

### 3. Centralized Exception Sanitization
Leaking internal system stack traces or downstream network connection drops into production logs or public API responses poses an operational security risk. I implemented a global exception interceptor framework using `@ControllerAdvice`:
* Automatically catches application pipeline failures.
* Logs raw, dirty stack details internally for reliability engineering review.
* Translates the response into a clean, sanitized JSON object before presenting it to the outside world.

### 4. Asynchronous Event-Driven Automation (ServiceNow Workflow Studio Engine)
To replicate the core mechanics of platforms like **ServiceNow Flow Designer**, I eliminated tightly-coupled component dependencies in favor of an **Event-Driven Architecture (EDA)**:
* **The Telemetry Trigger (`SevereWeatherEvent`):** Created a custom application event DTO that encapsulates real-time operational hazard data (impacted city hub and validated wind velocities) when system thresholds are breached.
* **The Event Broadcaster (`ApplicationEventPublisher`):** Refactored the core background daemon to instantly broadcast hazard signals into the Spring application context, decoupling the tracking mechanisms from downstream response plays.
* **Decoupled Playbook Listeners (`@EventListener`):** Built completely separate, asynchronous worker modules that execute independent mitigation procedures automatically upon event capture:
    * **`RouteRerouterListener` (Automated ITSM Action):** Dynamically alters data state inside the H2 engine, updating matching route records from `OPERATIONAL` to `DELAYED` to maintain network asset integrity.
    * **`DriverDispatchListener` (Automated Communication):** Generates sanitized, structured outbound notification payloads simulating a live transmission advisory for line-haul drivers in the hazardous zone.

---

## 🛠️ Tech Stack Implemented
* **Backend Framework:** Java / Spring Boot 3.x / Spring Web
* **Data Access:** Spring Data JPA (Java Persistence API)
* **Database Engine:** H2 In-Memory Database Engine
* **Testing Frameworks:** JUnit 5 / Mockito (True dependency-mocked unit testing)

---

## 🚦 Verification & Test Suite Progress
The application code enforces a strict Test-Driven process. By isolating dependencies (such as fluent client builders and database repositories) via Mockito stubs, the tests validate actual data-parsing logic and state-validation rules:
* **`WeatherServiceTest`:** Verifies URL execution, base URL composition, payload map navigation (`wind.speed`), and proper error raising.
* **`WeatherMonitoringSchedulerTest`:** Validates background control loop traversal and confirms the event broadcaster correctly launches when threshold rules fail.
* **`RouteRerouterListenerTest`:** Assures that playbook action interceptors cleanly handle captured events and update matching database entity keys appropriately.
* **Localized Database Controls:** Verified via H2 Web Console wrapper testing (`SELECT * FROM FREIGHT_ROUTE;`) that system state initializes correctly on startup.

**Current Sprint Status:** Phase 2 (Event-Driven Automation / ServiceNow Workflows) is complete. Ready to transition to Phase 3 (Control Center Analytics API & Final Deployment Pitch).
# StormShield: Weather-Triggered Operational Alerting System

StormShield is a proactive event-driven platform designed to monitor environmental hazards across shipping lanes and automatically execute operational response playbooks. This system mimics the core architecture of enterprise IT Service Management (ITSM) and automation platforms like ServiceNow.

---

## 🏗️ Architecture & Core Concepts Learnt

### 1. Configuration Management & Asset Tracking (Data Layer)
In enterprise IT operations, you cannot protect infrastructure if you do not know what assets exist. Today, I implemented the foundational data blueprint representing a freight carrier's physical assets:
* **The Domain Model (`FreightRoute`):** Represents shipping lanes connecting critical regional hubs. It tracks operational states using dynamic statuses (`OPERATIONAL`, `DELAYED`, `REROUTED`).
* **The Abstraction Layer (`JpaRepository`):** Leveraged Spring Data JPA to decouple the business logic from raw database communication, eliminating brittle, manual SQL plumbing.

### 2. Deterministic State Initialization
To support rapid prototyping and automated testing, the system features a self-seeding mechanism using Spring Boot's `CommandLineRunner`. The moment the application context boots, it automatically seeds an in-memory database engine with mock hub infrastructure (Memphis, Dallas, Atlanta, Chicago, Raleigh) to establish a baseline operational state.

---

## 🛠️ Tech Stack Implemented (Day 1)
* **Backend Framework:** Java / Spring Boot 3.x
* **Data Access:** Spring Data JPA (Java Persistence API)
* **Database Engine:** H2 In-Memory Database Engine

---

## 🚦 Verification & Current Progress
* Successfully enabled the built-in H2 Web Console wrapper.
* Verified via localized SQL testing (`SELECT * FROM FREIGHT_ROUTE;`) that state initialization is functioning perfectly and data integrity is maintained on application startup.

**Current Sprint Status:** Phase 1 (Data Foundation) is 100% Complete. Ready to transition to Phase 2 (Live API Data Ingestion).
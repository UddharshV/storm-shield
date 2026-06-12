# StormShield

StormShield is a Spring Boot backend that monitors freight routes for severe weather conditions and automatically manages route state when operations become unsafe.

It is built as an event-driven logistics alerting system: a scheduled monitor checks live weather data, publishes events when a route is at risk, and independent listener components react by updating route status and preparing operational dispatch alerts, without any tight coupling between concerns.

## Features

- Full route lifecycle management: create, read, update, and decommission freight lanes via REST
- Track route operational states: `OPERATIONAL`, `DELAYED`, and `REROUTED`
- Poll live weather data on a schedule and evaluate against operational risk thresholds
- Publish severe weather events and trigger independent automated response playbooks
- Expose a Control Center analytics dashboard through a dedicated REST endpoint
- Enforce input validation on all inbound API requests with structured error responses
- Unit tested across all layers with JUnit 5 and Mockito, without loading the full Spring context

## Architecture

### Phase 0: Data layer

- `FreightRoute` represents a shipping lane between logistics hubs
- Spring Data JPA handles persistence through a repository interface
- H2 is used for local development and fast iteration

### Phase 1: Weather monitoring

- A Spring `RestClient` fetches live weather data from the OpenWeatherMap API
- `@Scheduled` jobs poll all active route origin hubs in the background
- API credentials and base URL are externalized in configuration, never hardcoded

### Phase 2: Event-driven automation

When unsafe weather is detected, the application publishes a `SevereWeatherEvent`. Independent listener components each react to the same event without knowing about each other, keeping the system modular and easy to extend with new response playbooks.

```text
[Scheduled Monitor] --> [SevereWeatherEvent] --> [Event Publisher]
                                                        |
                               +------------------------+------------------------+
                               |                                                 |
                               v                                                 v
                     [RouteRerouterListener]                       [DriverDispatchListener]
                     marks impacted lanes DELAYED                  prepares outbound alert payload
```

### Phase 3: Control Center analytics

Operations managers can request a live network snapshot:

```http
GET /api/routes/analytics
```

Example response:

```json
{
  "totalMonitoredRoutes": 4,
  "activeWeatherDelays": 1,
  "activeOperationalLanes": 3,
  "networkAvailabilityPercentage": "75.0%",
  "systemStatus": "HEALTHY"
}
```

The system is marked `DEGRADED` when network availability drops to 70% or below.

### Phase 4: Route lifecycle management

The Control Center exposes a full REST API for managing freight lane records at runtime:

| Method   | Endpoint                  | Description                           |
|----------|---------------------------|---------------------------------------|
| `POST`   | `/api/routes`             | Register a new freight lane           |
| `PATCH`  | `/api/routes/{id}/status` | Manually override a route's status    |
| `DELETE` | `/api/routes/{id}`        | Decommission a route from the network |

All write endpoints enforce input validation via `@Valid` and `@NotBlank`. Invalid requests return a structured `400 Bad Request` body with per-field error detail:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "One or more fields failed validation.",
  "fieldErrors": [
    {
      "field": "sourceCity",
      "message": "Source city must not be blank"
    }
  ]
}
```

## Error handling

All API error responses are sanitized through a centralized `GlobalExceptionHandler`:

- `ExternalServiceException` — catches upstream weather API failures and returns a clean `502 Bad Gateway` without exposing internal traces
- `MethodArgumentNotValidException` — catches validation failures on request bodies and returns structured field-level error detail

Internal errors are logged with consistent severity prefixes for downstream log aggregation.

## Testing

All components are tested with direct unit tests using JUnit 5 and Mockito. No full Spring context is loaded, keeping tests fast and isolated.

Covered scenarios include:

- Healthy, degraded, and empty network analytics states
- Exact 70.0% availability boundary behavior
- Case-insensitive status handling
- Route creation with default and explicit status
- Manual status override: existing and missing route IDs
- Route decommission: existing and missing route IDs
- Validation failure producing correct `fieldErrors` structure
- External service exception sanitization

## Local setup

### Requirements

- Java 17+
- Maven 3.9+

### Set the API key

```bash
export OPENWEATHER_API_KEY="your_secret_api_token_here"
```

### Run tests

```bash
mvn clean test
```

### Run the application

```bash
mvn spring-boot:run
```

### H2 console

When running locally, the H2 console is available at:

```text
http://localhost:8080/h2-console
```

Use:

```text
JDBC URL: jdbc:h2:mem:stormshielddb
Username: sa
Password: (blank)
```

## Tech stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- H2 Database
- Jakarta Bean Validation
- JUnit 5
- Mockito

## Roadmap

- Replace in-memory Spring events with Kafka or ActiveMQ for durable, replayable messaging
- Add outbound integration with ITSM platforms such as ServiceNow for automated incident creation
- Migrate status field to a typed enum with enforced transition rules
- Move from city-based checks to geospatial route monitoring with PostgreSQL and PostGIS

## Author

Built by Uddharsh Vasili as a backend systems engineering project focused on event-driven design, operational automation, and API reliability.
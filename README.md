# StormShield

StormShield is a Spring Boot backend that monitors freight routes for severe weather and automatically updates route state when conditions become unsafe.

It is built as an event-driven logistics alerting system: a scheduled monitor checks weather data, publishes events when a route is at risk, and listener components react by updating route status and preparing operational alerts.

## Features

- Track freight routes with statuses such as `OPERATIONAL`, `DELAYED`, and `REROUTED`
- Poll weather data on a schedule
- Publish severe weather events when thresholds are exceeded
- React through independent listeners instead of tightly coupled service calls
- Expose network analytics through `GET /api/routes/analytics`
- Validate analytics logic with focused JUnit 5 and Mockito tests

## Architecture

### Phase 0: Data layer

- `FreightRoute` represents a shipping lane between logistics hubs
- Spring Data JPA handles persistence through a repository interface
- H2 is used for local development and fast testing

### Phase 1: Weather monitoring

- A Spring `RestClient` fetches weather data from an external API
- `@Scheduled` jobs poll route conditions in the background
- The weather API base URL is externalized in configuration

### Phase 2: Event-driven automation

When unsafe weather is detected, the application publishes a `SevereWeatherEvent`. Listener components react independently, which keeps the design modular and easy to extend.

```text
[Scheduled Monitor] --> [SevereWeatherEvent] --> [Event Publisher]
                                                   |
                              +--------------------+--------------------+
                              |                                         |
                              v                                         v
                    [Route Status Listener]                 [Dispatch Listener]
                    updates route to DELAYED               prepares alert message
```

### Phase 3: Control Center analytics

Managers can request a live system snapshot through:

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

The endpoint calculates:

- Total monitored routes
- Active weather delays
- Active operational lanes
- Network availability percentage
- Overall system status

## Testing

The analytics controller is tested with JUnit 5 and Mockito using direct unit tests instead of loading the full Spring context.

Covered scenarios include:

- Healthy network
- Empty network
- Degraded network
- Exact 70.0% boundary behavior
- Case-insensitive handling of `"DELAYED"`

## Security and error handling

- The OpenWeather API key is read from an environment variable, not hardcoded in source
- A global exception handler keeps API errors clean and consistent
- Sensitive configuration stays out of version control

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
- JUnit 5
- Mockito

## Roadmap

- Replace in-memory events with Kafka or ActiveMQ for durable messaging
- Add outbound integration for ITSM platforms such as ServiceNow
- Move from city-based checks to geospatial route monitoring with PostgreSQL and PostGIS

## Author

Built by Uddharsh Vasili as a backend systems engineering project focused on event-driven design, automation, and operational visibility.
# Custom Redis-like API

This project is a Redis-inspired key-value storage API built with **Java**, **Spring Boot**, **PostgreSQL**, and **Docker**. It supports basic Redis functionalities like setting/getting keys with optional TTL (Time To Live), automatic expiration, and retrieval of all keys with metadata.

---

## Features

- Store key-value pairs with optional TTL
- Retrieve values by key
- Check if a key exists
- Delete a key
- Fetch all stored entries
- Get TTL remaining for a key
- Automatic key expiry using Quartz Scheduler
- Input validation and custom exception handling
- Dockerized setup for app and database
- Swagger UI for API testing
- PostgreSQL persistence layer
- Unit Testing with JUnit and Mockito

---

## Technologies Used

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Quartz Scheduler
- Hibernate Validator
- Docker & Docker Compose
- Swagger (Springdoc OpenAPI)
- JUnit & Mockito

---

## Project Structure
```bash
src/
├── main/
│ ├── java/
│ │ └── com.rhytham.redisapi/
│ │ ├── controller/ # REST controllers
│ │ ├── service/ # Business logic
│ │ ├── model/ # JPA entity
│ │ ├── exception/ # Custom exceptions & handlers
│ │ ├── job/ # Quartz job for key expiry
│ │ ├── repository/ # JPA repository
│ │ └── RedisApiApplication.java
│ └── resources/
│ ├── application.properties # Spring Boot config
│ └── ...
└── test/ # Unit tests
```

---

## API Endpoints


| Method | Endpoint                   | Description                                                               |
|--------|----------------------------|---------------------------------------------------------------------------|
| POST   | `/set`                     | Set a key with value and optional TTL (in seconds)                        |
| GET    | `/get/{key}`               | Get the value for a given key                                             |
| GET    | `/get/details/{key}`       | Get key details along with remaining TTL                                  |
| GET    | `/exists/{key}`            | Check if a key exists and is not expired                                  |
| GET    | `/keys`                    | List all unexpired keys with their TTLs                                   |
| PATCH  | `/expire/{key}/{ttl}`      | Set or update the TTL (in seconds) for a key                              |
| GET    | `/ttl/{key}`               | Get remaining TTL for a key                                               |
| DELETE | `/delete/{key}`            | Delete a specific key                                                     |
| DELETE | `/flushall`                | Delete all keys (flush the entire key-value store)                        |

---

## Request Example

### POST `/set`

```json
{
  "key": "username",
  "value": "rhytham23",
  "ttl": 120
}
```
---

## Validation

All incoming payloads are validated:
- `key`: must not be blank
- `value`: must not be blank
- `ttl` (optional): must be a positive integer if present
---

## Error Handling

Custom exceptions return appropriate HTTP responses:
- `KeyNotFoundException` → 404 Not Found
- `MethodArgumentNotValidException` → 400 Bad Request with validation error messages
---

## Quartz Schedular for TTL Expiry

- A scheduled job runs every 10 seconds
- Automatically deletes keys whose `expiryTime` has passed
- Uses Quartz for job scheduling and task execution
---

## Docker Setup

### Prerequisites

- Docker & Docker Compose installed

### Build and Run

```bash
docker-compose up --build
```
This will:
- Build and run the Spring Boot app in a container
- Start a PostgreSQL container
- Mount volumes and expose ports accordingly

### Access

- API Base URL: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- PostgreSQL DB: `localhost:<port>`, DB: `<db_name>`, user: `<username>`, pass: `<password>`

> ⚠️ Replace `<username>` and `<password>` with your actual PostgreSQL credentials in `application.properties` or via environment variables.
---

## Run Locally (Without Docker)

### Start PostgreSQL

Ensure PostgreSQL is installed and running on your local machine.

### Update `application.properties` (Use environment variables for security)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/redisdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```
### Environment Variables Setup

Create a `.env` file or set the environment variables locally:

```env
DB_USERNAME=your_postgres_username
DB_PASSWORD=your_postgres_password
```
### Run the Application

```bash
mvn spring-boot:run
```
---

### Accessing PostgreSQL Container

To interact with the PostgreSQL database inside the Docker container:

1. Open a terminal and run:

```bash
docker exec -it redis-postgres psql -U postgres -d redisdb
```

---

## Unit Testing

- Unit tests are written for the service layer
- Uses **JUnit** and **Mockito** for testing and mocking

### Run Tests

```bash
mvn test
```
---
## Author

**Rhytham Sharma**

---

## License

This project is licensed under the **MIT License**.











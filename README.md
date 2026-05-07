# AI-based Operational Loss Calculator Backend

This is a complete Spring Boot 3.x backend application for an AI-based Operational Loss Calculator.

## Features
- **Layered Architecture:** Controller, Service, Repository, Entity, Config, Exception, Security, DTO, Util.
- **REST APIs:** Full CRUD operations for Losses and Auth operations.
- **Security:** JWT Authentication and Role-Based Access Control (`@PreAuthorize`).
- **Database:** PostgreSQL with JPA, Hibernate, and Flyway migration scripts.
- **Caching:** Redis integration with `@Cacheable` and `@CacheEvict`.
- **Error Handling:** Global Exception Handling using `@ControllerAdvice`.
- **Scheduled Tasks:** Automated background jobs using `@Scheduled`.
- **Notifications:** Email notifications using JavaMailSender.
- **Testing:** Comprehensive JUnit 5 and Mockito tests (Controllers and Services).
- **Containerization:** Complete Docker support using `docker-compose.yml`.

## Prerequisites
- Java 21
- Maven
- Docker and Docker Compose

## Quick Start with Docker
1. Ensure Docker is running.
2. Build and start the services:
   ```bash
   docker-compose up --build
   ```
3. The API will be available at `http://localhost:8080`.
   - PostgreSQL runs on port `5432`.
   - Redis runs on port `6379`.

## Default Credentials
Flyway automatically seeds the database with the following accounts:
- **Admin:** `admin` / `password123`
- **User:** `user1` / `password123`

## Running Locally (without Docker)
1. Start PostgreSQL and Redis locally.
2. Update `.env` or `application.yml` with your local DB credentials.
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Testing
To execute the test suite:
```bash
mvn test
```

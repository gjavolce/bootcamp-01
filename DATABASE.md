# Database Schema Setup

This document describes the database schema for the bootcamp-01 application.

## Tables

### hello_world_messages
- `id` (BIGSERIAL PRIMARY KEY)
- `name` (VARCHAR(255) NOT NULL)
- `message` (VARCHAR(255) NOT NULL)
- `created_at` (TIMESTAMP)

## Setup Instructions

### 1. Using Docker Compose (Recommended)
```bash
# Start only PostgreSQL
docker-compose up -d postgres

# Start the entire application stack
docker-compose up -d
```

### 2. Manual PostgreSQL Setup
```sql
CREATE DATABASE bootcamp_db;
CREATE USER bootcamp_user WITH PASSWORD 'bootcamp_password';
GRANT ALL PRIVILEGES ON DATABASE bootcamp_db TO bootcamp_user;
```

### 3. Connect to the database
```bash
# Using the Docker container
docker exec -it postgres-db psql -U bootcamp_user -d bootcamp_db

# Using local psql client (if installed)
psql -h localhost -p 5433 -U bootcamp_user -d bootcamp_db
```

## Environment Variables

For local development, you can override database connection settings:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/bootcamp_db
SPRING_DATASOURCE_USERNAME=bootcamp_user
SPRING_DATASOURCE_PASSWORD=bootcamp_password
```

## Testing the API

### Basic Hello World (saves to database)
```bash
curl "http://localhost:8080/hello?name=John"
# Returns: Hello, John!
# Also saves the greeting to the database
```

### Get all messages
```bash
curl http://localhost:8080/hello/messages
```

### Get recent messages (last 10)
```bash
curl http://localhost:8080/hello/messages/recent
```

### Search messages by name
```bash
curl "http://localhost:8080/hello/messages/search?name=John"
```

### Delete a message by ID
```bash
curl -X DELETE http://localhost:8080/hello/messages/1
```

### Original hello endpoint (without name parameter)
```bash
curl http://localhost:8080/hello
# Returns: Hello, World!
```

## Running Tests

The project includes integration tests that use TestContainers to spin up a PostgreSQL instance:

```bash
./mvnw test
```

## Database Configuration Files

- **Main Application**: `src/main/resources/application.properties`
- **Test Configuration**: `src/test/resources/application-test.properties`
- **Docker Setup**: `docker-compose.yml`

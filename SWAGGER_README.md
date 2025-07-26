# Swagger/OpenAPI Documentation for Bootcamp 01

This directory contains the OpenAPI 3.0 specification for the Bootcamp 01 Spring Boot application.

## Available Documentation Formats

- **YAML**: `openapi.yaml` - OpenAPI specification in YAML format
- **JSON**: `openapi.json` - OpenAPI specification in JSON format

## API Documentation Overview

The API includes the following endpoints:

### HelloWorld Endpoints

1. **GET /hello** - Generate a greeting message
   - Parameter: `name` (optional) - Name to greet, defaults to "World"
   - Example: `GET /hello?name=John` → "Hello, John!"

2. **GET /hello/messages** - Get all stored messages
   - Returns: Array of all HelloWorld messages from the database

3. **GET /hello/messages/recent** - Get recent messages
   - Returns: Array of the 10 most recent messages

4. **GET /hello/messages/search** - Search messages by name
   - Parameter: `name` (required) - Name to search for (case-insensitive)
   - Example: `GET /hello/messages/search?name=John`

5. **DELETE /hello/messages/{id}** - Delete a message by ID
   - Parameter: `id` (path) - ID of the message to delete

### Health Check Endpoints

1. **GET /health/live** - Liveness probe
   - Returns: Application liveness status

2. **GET /health/startup** - Startup probe
   - Returns: Application startup status

## How to Use the Documentation

### Option 1: View in Swagger Editor (Recommended)

1. Go to [Swagger Editor](https://editor.swagger.io/)
2. Copy the contents of `openapi.yaml` or import the `openapi.json` file
3. The editor will render an interactive API documentation

### Option 2: Use Swagger UI locally

1. Install Swagger UI:
   ```bash
   npm install -g swagger-ui-serve
   ```

2. Serve the documentation:
   ```bash
   swagger-ui-serve openapi.yaml
   ```

### Option 3: Use with API clients

- **Postman**: Import the `openapi.json` file to generate a collection
- **Insomnia**: Import the OpenAPI specification
- **curl**: Use the examples provided in the specification

## Testing the API

Make sure the application is running on `http://localhost:8081` before testing:

```bash
# Test the greeting endpoint
curl "http://localhost:8081/hello?name=Swagger"

# Get all messages
curl http://localhost:8081/hello/messages

# Get recent messages
curl http://localhost:8081/hello/messages/recent

# Search for messages
curl "http://localhost:8081/hello/messages/search?name=Swagger"

# Check health
curl http://localhost:8081/health/live
```

## Schema Information

### HelloWorld Entity

```json
{
  "id": 1,
  "name": "John",
  "message": "Hello, John!",
  "createdAt": "2023-01-01T12:00:00Z"
}
```

- `id`: Unique identifier (auto-generated)
- `name`: Name of the person being greeted
- `message`: The generated greeting message
- `createdAt`: Timestamp when the message was created

## Development Notes

- The application uses Spring Boot WebFlux (reactive stack)
- Database: PostgreSQL
- Port: 8081 (configurable)
- All timestamps are in ISO 8601 format (UTC)

## Integration with SpringDoc OpenAPI

While the application includes SpringDoc OpenAPI dependencies, the automatic generation may not work perfectly with the current WebFlux setup. Therefore, these manually created specifications serve as the definitive API documentation.

If you need to update the API documentation:

1. Modify the `openapi.yaml` file
2. Regenerate the JSON version if needed
3. Test the endpoints to ensure accuracy
4. Update this README if new endpoints are added

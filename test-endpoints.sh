#!/bin/bash

# Endpoint Testing Script for HelloWorld Spring Boot Application
# This script tests all available endpoints to ensure they're working correctly

BASE_URL="http://localhost:8081"

echo "🚀 Testing HelloWorld Spring Boot Application Endpoints"
echo "=================================================="

# Test 1: Hello endpoint with name parameter
echo "1. Testing /hello endpoint with name parameter..."
response=$(curl -s "$BASE_URL/hello?name=TestUser")
echo "Response: $response"
echo ""

# Test 2: Hello endpoint without name (default)
echo "2. Testing /hello endpoint without name (should use default)..."
response=$(curl -s "$BASE_URL/hello")
echo "Response: $response"
echo ""

# Test 3: Get all messages
echo "3. Testing /hello/messages endpoint (GET all messages)..."
response=$(curl -s "$BASE_URL/hello/messages" | jq .)
echo "Response: $response"
echo ""

# Test 4: Search messages by name
echo "4. Testing /hello/messages/search endpoint..."
response=$(curl -s "$BASE_URL/hello/messages/search?name=TestUser" | jq .)
echo "Response: $response"
echo ""

# Test 5: Health check - liveness
echo "5. Testing /health/live endpoint..."
response=$(curl -s "$BASE_URL/health/live" | jq .)
echo "Response: $response"
echo ""

# Test 6: Health check - startup
echo "6. Testing /health/startup endpoint..."
response=$(curl -s "$BASE_URL/health/startup" | jq .)
echo "Response: $response"
echo ""

# Test 7: Try to access OpenAPI documentation (may not be available in WebFlux)
echo "7. Checking for OpenAPI documentation endpoints..."
echo "   Trying /v3/api-docs..."
response=$(curl -s -w "%{http_code}" -o /dev/null "$BASE_URL/v3/api-docs")
if [ "$response" = "200" ]; then
    echo "   ✅ OpenAPI JSON available at /v3/api-docs"
else
    echo "   ❌ OpenAPI JSON not available (HTTP $response)"
fi

echo "   Trying /swagger-ui.html..."
response=$(curl -s -w "%{http_code}" -o /dev/null "$BASE_URL/swagger-ui.html")
if [ "$response" = "200" ]; then
    echo "   ✅ Swagger UI available at /swagger-ui.html"
else
    echo "   ❌ Swagger UI not available (HTTP $response)"
fi

echo ""
echo "📄 Manual OpenAPI documentation is available in:"
echo "   - openapi.yaml"
echo "   - openapi.json"
echo "   - SWAGGER_README.md"
echo ""
echo "✅ Endpoint testing completed!"

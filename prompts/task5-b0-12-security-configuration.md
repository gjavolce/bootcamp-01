# Task 5: Security Configuration (B0-12)

**Status**: Not Started  
**Dependencies**: All previous tasks (Tasks 1-4) must be complete

## Current State
- ❌ **Missing: CORS configuration for frontend integration**
- ❌ **Missing: Security headers implementation**  
- ❌ **Missing: Rate limiting for authentication endpoints**
- ❌ **Missing: Comprehensive audit logging**
- ❌ **Missing: Security monitoring and alerts**

---

## Step 1: CORS Configuration for Frontend Integration ❌

### Prompt:
```
Create comprehensive CORS configuration for Spring WebMVC to support frontend integration:

CONTEXT:
- Need to support multiple environments (dev, staging, prod)
- Frontend will run on different ports/domains
- Must support credentials (cookies, authorization headers)
- Should be configurable per environment

CORS REQUIREMENTS:
Development environment:
- Allow origins: http://localhost:3000, http://localhost:3001, http://127.0.0.1:3000
- Allow credentials: true
- Allow methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Allow headers: Authorization, Content-Type, X-Requested-With, X-Device-ID, X-Forwarded-For
- Expose headers: X-Total-Count, X-Rate-Limit-Remaining, X-Rate-Limit-Reset
- Max age: 3600 seconds (1 hour)

Production environment:
- Allow specific production domains only (configurable)
- Stricter header controls
- Shorter max age for security

IMPLEMENTATION APPROACH:
Create CorsConfig class:
- @Configuration annotation
- Implement WebMvcConfigurer interface
- Override addCorsMappings method
- Use @Value for environment-specific configuration
- Support multiple allowed origins

CONFIGURATION PROPERTIES:
Add to application.properties:
- cors.allowed-origins (comma-separated list)
- cors.allowed-methods (comma-separated)
- cors.allowed-headers (comma-separated)
- cors.exposed-headers (comma-separated)
- cors.allow-credentials (boolean)
- cors.max-age (seconds)

SECURITY CONSIDERATIONS:
- Never use wildcard (*) origins in production
- Validate Origin header against whitelist
- Handle preflight requests properly
- Log CORS violations for monitoring

ENVIRONMENT-SPECIFIC CONFIG:
- application-dev.properties: localhost origins
- application-prod.properties: production domains
- application-test.properties: test-specific settings

Please create comprehensive CORS configuration for all environments.
```

### Verification:
```bash
# Test CORS preflight request (dev environment)
curl -v -X OPTIONS \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Authorization,Content-Type" \
  http://localhost:8080/api/auth/login
# Expected: CORS headers allowing origin and methods

# Test CORS with actual request
curl -v -X POST \
  -H "Origin: http://localhost:3000" \
  -H "Content-Type: application/json" \
  http://localhost:8080/api/auth/login
# Expected: Access-Control-Allow-Origin header in response

# Test invalid origin (should be blocked)
curl -v -X OPTIONS \
  -H "Origin: http://malicious-site.com" \
  -H "Access-Control-Request-Method: POST" \
  http://localhost:8080/api/auth/login
# Expected: No CORS headers or blocked request

# Test credentials support
curl -v -X POST \
  -H "Origin: http://localhost:3000" \
  -H "Content-Type: application/json" \
  --cookie "sessionid=test" \
  http://localhost:8080/api/auth/login
# Expected: Access-Control-Allow-Credentials: true

# Test different environments
mvn spring-boot:run -Dspring.profiles.active=dev
mvn spring-boot:run -Dspring.profiles.active=prod
```

---

## Step 2: Security Headers Implementation ❌

### Prompt:
```
Implement comprehensive security headers for Spring WebMVC application:

CONTEXT:
- Need protection against XSS, clickjacking, CSRF, and other attacks
- Headers should be applied to all responses
- Some headers are environment-specific (HSTS for HTTPS)
- Must not interfere with API functionality

REQUIRED SECURITY HEADERS:
Standard security headers:
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Referrer-Policy: strict-origin-when-cross-origin
- X-Permitted-Cross-Domain-Policies: none

Content Security Policy:
- default-src 'self'
- script-src 'self' 'unsafe-inline' (minimal for API, adjust for frontend)
- style-src 'self' 'unsafe-inline'
- img-src 'self' data: https:
- connect-src 'self'
- font-src 'self'
- object-src 'none'
- base-uri 'self'
- form-action 'self'

HTTPS-specific headers (production only):
- Strict-Transport-Security: max-age=31536000; includeSubDomains; preload

IMPLEMENTATION OPTIONS:
Option 1: Spring Security Headers Configuration
- Configure in SecurityFilterChain
- Use Spring Security's built-in header support
- Integrate with existing security configuration

Option 2: Custom Security Headers Filter
- @Component SecurityHeadersFilter implements Filter
- Add headers to all responses
- More granular control over header values

ENVIRONMENT-SPECIFIC HEADERS:
- Development: Relaxed CSP for debugging
- Production: Strict CSP and HSTS
- Test: Minimal headers for testing

CONFIGURABLE HEADERS:
Add properties for customization:
- security.headers.csp.default-src
- security.headers.csp.script-src
- security.headers.frame-options
- security.headers.hsts.enabled
- security.headers.hsts.max-age

HEADER CUSTOMIZATION:
Some endpoints may need different headers:
- API endpoints: No X-Frame-Options (not needed)
- File upload endpoints: Different CSP
- OAuth endpoints: Specific CSP requirements

Please implement comprehensive security headers configuration.
```

### Verification:
```bash
# Test security headers on API endpoints
curl -v http://localhost:8080/api/auth/login
# Expected: All security headers present

# Test HSTS header (HTTPS environment)
curl -v https://localhost:8443/api/auth/login
# Expected: Strict-Transport-Security header

# Test CSP header format
curl -s -D- http://localhost:8080/api/auth/login | grep -i content-security-policy
# Expected: Properly formatted CSP header

# Test headers don't interfere with functionality
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}' \
  http://localhost:8080/api/auth/login
# Expected: Normal response with security headers

# Validate headers with security scanner
# Use tools like: securityheaders.com or Mozilla Observatory

# Test different environments have appropriate headers
mvn spring-boot:run -Dspring.profiles.active=prod
curl -v http://localhost:8080/api/auth/login
# Expected: Production-specific headers
```

---

## Step 3: Redis-based Rate Limiting Implementation ❌

### Prompt:
```
Implement comprehensive Redis-based rate limiting for authentication and API endpoints:

CONTEXT:
- Use existing Redis infrastructure
- Need different limits for different endpoint types
- Must prevent brute force attacks on authentication
- Should include rate limit headers in responses

RATE LIMIT RULES:
Authentication endpoints:
- /api/auth/login: 5 attempts per minute per IP
- /api/auth/register: 3 attempts per hour per IP
- /api/auth/refresh: 10 attempts per minute per user
- /api/auth/forgot-password: 2 attempts per hour per email

General API endpoints:
- /api/**: 100 requests per minute per authenticated user
- /api/admin/**: 50 requests per minute per admin user
- /api/users/**: 60 requests per minute per user

RATE LIMITING STRATEGY:
Use sliding window approach with Redis:
- Key pattern: rate_limit:{endpoint}:{identifier}:{window}
- Store request count and window start time
- Use Redis EXPIRE for automatic cleanup
- Implement atomic increment operations

RATE LIMITER SERVICE:
Create RateLimitingService with methods:
- checkRateLimit(String endpoint, String identifier, int limit, Duration window): RateLimitResult
- recordRequest(String endpoint, String identifier): void
- getRemainingAttempts(String endpoint, String identifier, int limit, Duration window): int
- getResetTime(String endpoint, String identifier, Duration window): Instant

RATE LIMIT RESULT DTO:
Create RateLimitResult class:
- allowed: boolean
- remaining: int
- resetTime: Instant
- limit: int

RATE LIMITING FILTER:
Create RateLimitingFilter:
- @Component implementing Filter
- Check rate limits before processing requests
- Skip rate limiting for excluded paths
- Return 429 Too Many Requests when exceeded
- Add rate limit headers to all responses

RATE LIMIT HEADERS:
Include in all responses:
- X-Rate-Limit-Limit: maximum requests allowed in window
- X-Rate-Limit-Remaining: requests remaining in current window
- X-Rate-Limit-Reset: timestamp when window resets (Unix timestamp)
- Retry-After: seconds to wait when rate limited (only for 429 responses)

CONFIGURATION:
Make rate limits configurable:
- rate-limit.auth.login.limit=5
- rate-limit.auth.login.window=60s
- rate-limit.api.general.limit=100
- rate-limit.api.general.window=60s
- rate-limit.enabled=true

IDENTIFIER STRATEGIES:
- IP address for unauthenticated requests
- User ID for authenticated requests
- Email for password reset requests
- Device ID for device-specific limits

ERROR HANDLING:
- Handle Redis connection failures gracefully
- Fall back to in-memory rate limiting if Redis unavailable
- Log rate limit violations for monitoring
- Don't fail requests due to rate limiting infrastructure issues

Please implement comprehensive Redis-based rate limiting.
```

### Verification:
```bash
# Test login rate limiting
for i in {1..6}; do
  echo "Login attempt $i:"
  curl -v -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"wrongpassword"}'
  echo "\\n"
done
# Expected: First 5 return 401, 6th returns 429 Too Many Requests

# Test rate limit headers
curl -v -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
# Expected: X-Rate-Limit-* headers in response

# Test general API rate limiting (need valid token)
TOKEN="..." # Valid access token
for i in {1..101}; do
  curl -s -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/api/users/profile >/dev/null
done
curl -v -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users/profile
# Expected: 429 Too Many Requests after 100 requests

# Check Redis keys for rate limiting
redis-cli KEYS "rate_limit:*"
# Should show rate limit counters

redis-cli GET "rate_limit:/api/auth/login:192.168.1.1:60"
# Should show current count for IP

# Test rate limit reset after window expires
sleep 61  # Wait for window to expire
curl -v -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
# Expected: Rate limit reset, request allowed

# Test different rate limits for different endpoints
curl -v http://localhost:8080/api/auth/register  # Should have different limit
curl -v http://localhost:8080/api/auth/refresh   # Should have different limit
```

---

## Step 4: Comprehensive Security Integration and Monitoring ❌

### Prompt:
```
Integrate all security components and create comprehensive security monitoring:

CONTEXT:
- All security components (CORS, headers, rate limiting) are now available
- Need proper integration with Spring Security filter chain
- Must add comprehensive audit logging and monitoring
- Should detect and alert on security violations

SECURITY FILTER CHAIN INTEGRATION:
Update SecurityConfig to include all security filters in proper order:
1. CorsFilter (if using separate filter)
2. SecurityHeadersFilter
3. RateLimitingFilter  
4. JwtAuthenticationFilter
5. UsernamePasswordAuthenticationFilter
6. Other Spring Security filters

FILTER ORDERING:
Ensure proper filter order using @Order annotations:
- SecurityHeadersFilter: @Order(1)
- RateLimitingFilter: @Order(2)
- JwtAuthenticationFilter: @Order(3)

COMPREHENSIVE AUDIT LOGGING:
Create SecurityAuditService for logging:
- Authentication attempts (success/failure)
- Rate limit violations
- CORS violations
- JWT token validation failures
- Session creation/destruction
- Admin actions
- Suspicious activities

AUDIT LOG FORMAT:
Structured logging with consistent format:
```json
{
  "timestamp": "2025-07-30T10:30:00Z",
  "level": "WARN",
  "event": "authentication_failure",
  "user": "test@example.com",
  "ip": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "endpoint": "/api/auth/login",
  "reason": "invalid_credentials",
  "deviceId": "device123"
}
```

SECURITY MONITORING SERVICE:
Create SecurityMonitoringService:
- detectBruteForceAttacks(String ip, Duration window): boolean
- detectSuspiciousActivity(String userId): List<SecurityEvent>
- getSecurityMetrics(LocalDate date): SecurityMetrics
- generateSecurityReport(): SecurityReport

SECURITY METRICS DTO:
Create SecurityMetrics class:
- totalLoginAttempts: long
- failedLoginAttempts: long
- rateLimitViolations: long
- corsViolations: long
- activeSessions: long
- suspiciousActivities: long

REAL-TIME SECURITY ALERTS:
Implement security event detection:
- Multiple failed logins from same IP (>10 in 5 minutes)
- Token validation failures spike
- Unusual session patterns
- Rate limit violations pattern
- CORS violations from unexpected origins

ALERT MECHANISMS:
- Log critical security events at ERROR level
- Create SecurityAlert events for monitoring systems
- Optional: Email notifications for critical alerts
- Optional: Slack/webhook notifications

SECURITY HEALTH INDICATORS:
Add security-related health checks:
- Rate limiting Redis connectivity
- Session storage health
- Authentication service health
- Security filter chain status

ADMIN SECURITY ENDPOINTS:
Create admin endpoints for security monitoring:
- GET /api/admin/security/metrics
- GET /api/admin/security/alerts
- GET /api/admin/security/events
- POST /api/admin/security/block-ip
- DELETE /api/admin/security/unblock-ip

CONFIGURATION MANAGEMENT:
Security configuration properties:
- security.monitoring.enabled=true
- security.alerts.enabled=true
- security.brute-force.threshold=10
- security.brute-force.window=5m
- security.audit.level=WARN

Please create comprehensive security integration and monitoring.
```

### Verification:
```bash
# Test complete security filter chain
mvn spring-boot:run &
sleep 10

# Test multiple security layers together
curl -v -X POST \
  -H "Origin: http://localhost:3000" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}' \
  http://localhost:8080/api/auth/login
# Expected: CORS headers + security headers + rate limit headers + authentication

# Test security violation detection
# Generate multiple failed login attempts
for i in {1..15}; do
  curl -s -X POST \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"wrong"}' \
    http://localhost:8080/api/auth/login >/dev/null
done
# Expected: Brute force attack detection in logs

# Test admin security endpoints
ADMIN_TOKEN="..." # Admin user token
curl -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8080/api/admin/security/metrics
# Expected: Security metrics JSON response

curl -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8080/api/admin/security/alerts
# Expected: Recent security alerts

# Test security health checks
curl http://localhost:8080/actuator/health/security
# Expected: Security component health status

# Check audit logs
grep "authentication_failure" logs/application.log
grep "rate_limit_violation" logs/application.log
grep "brute_force_detected" logs/application.log

# Test IP blocking (if implemented)
curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ip":"192.168.1.100","reason":"brute_force"}' \
  http://localhost:8080/api/admin/security/block-ip
# Expected: IP blocked, subsequent requests from that IP should be rejected

pkill -f "spring-boot:run"

# Load test to verify performance impact
# Use Apache Bench or similar tool
ab -n 1000 -c 10 http://localhost:8080/api/auth/login
# Verify security filters don't significantly impact performance
```

---

## Definition of Done Checklist

- [ ] CORS configuration supports multiple environments
- [ ] Security headers protect against common attacks
- [ ] Rate limiting prevents brute force attacks
- [ ] All security components integrated in proper filter order
- [ ] Comprehensive audit logging implemented
- [ ] Security monitoring detects suspicious activities
- [ ] Admin endpoints provide security management capabilities
- [ ] Security health checks monitor system security
- [ ] Real-time security alerts functional
- [ ] Performance impact of security filters acceptable
- [ ] All security configurations are environment-specific
- [ ] Security documentation updated

## Files to Create/Modify

**New Files:**
- `src/main/java/com/bojan/bootcamp_01/config/CorsConfig.java`
- `src/main/java/com/bojan/bootcamp_01/security/SecurityHeadersFilter.java`
- `src/main/java/com/bojan/bootcamp_01/security/RateLimitingFilter.java`
- `src/main/java/com/bojan/bootcamp_01/service/RateLimitingService.java`
- `src/main/java/com/bojan/bootcamp_01/service/SecurityAuditService.java`
- `src/main/java/com/bojan/bootcamp_01/service/SecurityMonitoringService.java`
- `src/main/java/com/bojan/bootcamp_01/dto/RateLimitResult.java`
- `src/main/java/com/bojan/bootcamp_01/dto/SecurityMetrics.java`
- `src/main/java/com/bojan/bootcamp_01/dto/SecurityEvent.java`
- `src/main/java/com/bojan/bootcamp_01/dto/SecurityAlert.java`
- `src/main/java/com/bojan/bootcamp_01/controller/SecurityController.java`
- `src/main/java/com/bojan/bootcamp_01/health/SecurityHealthIndicator.java`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`
- `src/test/java/com/bojan/bootcamp_01/security/SecurityIntegrationTest.java`
- `src/test/java/com/bojan/bootcamp_01/security/RateLimitingServiceTest.java`
- `src/test/java/com/bojan/bootcamp_01/security/SecurityMonitoringTest.java`

**Modified Files:**
- `src/main/java/com/bojan/bootcamp_01/config/SecurityConfig.java` (integrate all security filters)
- `src/main/resources/application.properties` (add security configuration properties)

## Epic Completion

This task completes the JWT Authentication & Session Management epic (B0-7). Upon completion, the application will have:

✅ **Production-Ready Security**: CORS, security headers, rate limiting  
✅ **Comprehensive Monitoring**: Audit logging, security metrics, alerts  
✅ **Attack Prevention**: Brute force protection, suspicious activity detection  
✅ **Admin Controls**: Security management endpoints and monitoring  
✅ **Performance Optimized**: Security with minimal performance impact  

## Final Integration Testing

After completing all tasks, run comprehensive end-to-end tests:
- Full authentication flow with all security measures
- Rate limiting under load
- Security violation detection
- Cross-origin requests with CORS
- Admin security management
- Performance benchmarks with security enabled

The epic will be complete when all security measures work together seamlessly while maintaining optimal application performance.
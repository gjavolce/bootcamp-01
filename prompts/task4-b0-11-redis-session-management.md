# Task 4: Redis Session Management (B0-11)

**Status**: Partially Complete - Critical Type Mismatch Issue  
**Dependencies**: Task 1 JWT Service (❌), Task 2 Authentication Filter (❌), Task 3 Refresh Tokens (❌)

## Current State
- ✅ UserSession model exists
- ✅ SessionRepository interface and implementation exist
- ✅ Redis configuration and tests working
- ❌ **CRITICAL: UserSession uses Long userId but User entity uses UUID id**
- ❌ **Missing: SessionManagementService integration**
- ❌ **Missing: Authentication flow integration**
- ❌ **Missing: Session monitoring and cleanup**

---

## Step 1: Fix UserSession Type Mismatch and Enhance Model ❌

### Prompt:
```
Fix the critical type mismatch in UserSession and enhance the model for comprehensive session tracking:

CRITICAL ISSUE:
- UserSession currently uses Long userId
- User entity uses UUID getId()
- This causes runtime errors and data inconsistency

TYPE MISMATCH FIX:
Modify UserSession.java to use String userId (UUID as string) instead of Long:
- Change userId field from Long to String
- Update all related methods and constructors
- Ensure compatibility with User.getId().toString()

ENHANCED SESSION MODEL:
Add missing fields to UserSession:
- loginTime: Instant (when session was created)
- location: String (optional, derived from IP geolocation - can be null for now)
- sessionId: String (unique session identifier, different from userId+deviceId)

SESSION METADATA ENHANCEMENT:
- Add browser: String (parsed from userAgent)
- Add platform: String (parsed from userAgent: Windows, macOS, Linux, etc.)
- Add deviceType: String (Mobile, Desktop, Tablet)
- Add lastEndpoint: String (last API endpoint accessed)

SERIALIZATION UPDATES:
- Ensure all new fields are properly serialized for Redis
- Add Jackson annotations if needed
- Update constructors and toString method
- Maintain backward compatibility with existing Redis data

VALIDATION RULES:
- userId must be valid UUID string format
- deviceId must be non-null and non-empty
- ipAddress should be valid IP format (basic validation)
- createdAt and lastActivity must not be null

Please fix the type mismatch and enhance the UserSession model.
```

### Verification:
```bash
# Test UserSession serialization with new fields
mvn test -Dtest=UserSessionSerializationTest

# Test type compatibility with User entity
mvn test -Dtest=UserSessionCompatibilityTest

# Test existing SessionRepository still works
mvn test -Dtest=SessionRepositoryImplTest

# Verify no runtime errors with UUID conversion
mvn test -Dtest=SessionIntegrationTest

# Check Redis storage format
redis-cli
> GET "session:test-uuid:device1"
# Should show session data with string userId
```

---

## Step 2: Update SessionRepository for Type Compatibility ❌

### Prompt:
```
Update SessionRepository and SessionRepositoryImpl to handle the String userId change and add new session management methods:

REPOSITORY METHOD UPDATES:
Update existing methods for String userId:
- createSession(UserSession session): void
- getSession(String userId, String deviceId): UserSession
- updateActivity(String userId, String deviceId): void
- getAllUserSessions(String userId): List<UserSession>
- deleteSession(String userId, String deviceId): void
- deleteAllUserSessions(String userId): void

NEW REPOSITORY METHODS:
Add methods for enhanced session management:
- findSessionBySessionId(String sessionId): Optional<UserSession>
- updateLastEndpoint(String userId, String deviceId, String endpoint): void
- getSessionsByIpAddress(String ipAddress): List<UserSession>
- getSessionsCreatedAfter(Instant timestamp): List<UserSession>
- deleteInactiveSessions(Duration inactivityThreshold): int

REDIS KEY PATTERN UPDATES:
Update key patterns to handle String userId:
- session:{userId}:{deviceId} → UserSession object
- session_by_id:{sessionId} → session key for reverse lookup
- user_sessions:{userId} → Set of deviceIds
- ip_sessions:{ipAddress} → Set of session keys (for security monitoring)

ENHANCED REDIS OPERATIONS:
- Add session-by-ID lookup for quick access
- Add IP-based session tracking for security monitoring
- Implement batch operations for session cleanup
- Add proper indexing for efficient queries

ERROR HANDLING IMPROVEMENTS:
- Add validation for UUID format in userId
- Handle malformed session keys gracefully
- Add retry logic for Redis connection failures
- Improve error logging with structured format

PERFORMANCE OPTIMIZATIONS:
- Use Redis pipelines for batch operations
- Implement connection pooling optimization
- Add caching for frequently accessed sessions
- Use Redis transactions for atomic updates

Please update the SessionRepository for String userId and add enhanced session management methods.
```

### Verification:
```bash
# Test repository with String userId
mvn test -Dtest=SessionRepositoryImplTest#testStringUserIds

# Test new session management methods
mvn test -Dtest=SessionRepositoryImplTest#testEnhancedMethods

# Test Redis key patterns are correct
mvn test -Dtest=SessionRepositoryImplTest#testKeyPatterns

# Test performance with batch operations
mvn test -Dtest=SessionRepositoryPerformanceTest

# Check Redis keys are created correctly
redis-cli KEYS "session:*"
redis-cli KEYS "session_by_id:*"
redis-cli KEYS "ip_sessions:*"
```

---

## Step 3: Create SessionManagementService with Device Fingerprinting ❌

### Prompt:
```
Create comprehensive SessionManagementService that integrates with authentication flow and provides device fingerprinting:

CONTEXT:
- SessionRepository updated with String userId support
- Need integration with authentication flow (login/logout)
- Must extract device info from HTTP requests
- Need concurrent session limit enforcement

SESSION MANAGEMENT SERVICE:
Create SessionManagementService with methods:
- createSession(User user, String deviceId, HttpServletRequest request): UserSession
- updateActivity(String userId, String deviceId, String endpoint): void
- validateSession(String userId, String deviceId): boolean
- invalidateSession(String userId, String deviceId): void
- invalidateAllUserSessions(String userId): void
- getActiveSessions(String userId): List<UserSession>
- enforceConcurrentSessionLimit(String userId, int maxSessions): void

DEVICE FINGERPRINTING:
Create DeviceFingerprintService for extracting device info:
- extractDeviceInfo(HttpServletRequest request): DeviceInfo
- parseUserAgent(String userAgent): UserAgentInfo
- detectDeviceType(String userAgent): DeviceType (Mobile, Desktop, Tablet)
- generateDeviceFingerprint(HttpServletRequest request): String
- detectBrowser(String userAgent): String
- detectPlatform(String userAgent): String

DEVICE INFO DTOS:
Create data classes:
- DeviceInfo: deviceId, browser, platform, deviceType, fingerprint
- UserAgentInfo: browser, version, platform, isMobile, isBot

SESSION CREATION LOGIC (createSession method):
1. Generate unique sessionId (UUID)
2. Extract device info from request (User-Agent, IP, etc.)
3. Create UserSession with all metadata
4. Enforce concurrent session limits
5. Store session in Redis
6. Update user session tracking
7. Log session creation for audit

SESSION ACTIVITY TRACKING:
- Update lastActivity on each authenticated request
- Track last accessed endpoint
- Monitor session duration
- Detect IP address changes (potential security issue)
- Implement sliding session timeout

CONCURRENT SESSION MANAGEMENT:
- Configure maximum sessions per user (default: 5)
- Implement oldest session eviction when limit reached
- Notify user about new login (optional)
- Handle session conflicts gracefully

SECURITY FEATURES:
- Detect suspicious session activity (IP changes, unusual patterns)
- Monitor session creation rate (potential attacks)
- Track failed session validation attempts
- Implement session invalidation for security breaches

Please create the comprehensive SessionManagementService with device fingerprinting.
```

### Verification:
```bash
# Test session creation with device fingerprinting
mvn test -Dtest=SessionManagementServiceTest#testCreateSession

# Test device fingerprinting
mvn test -Dtest=DeviceFingerprintServiceTest#testDeviceDetection

# Test concurrent session limits
mvn test -Dtest=SessionManagementServiceTest#testConcurrentSessionLimits

# Test session activity tracking
mvn test -Dtest=SessionManagementServiceTest#testActivityTracking

# Test security features
mvn test -Dtest=SessionManagementServiceTest#testSecurityFeatures

# Test with real HTTP requests
mvn test -Dtest=SessionManagementIntegrationTest
```

---

## Step 4: Integration with Authentication Flow and Monitoring ❌

### Prompt:
```
Integrate session management with authentication flow and create session monitoring capabilities:

AUTHENTICATION INTEGRATION:
Integrate SessionManagementService with authentication components:
- Create session on successful login (in AuthController)
- Update session activity in JwtAuthenticationFilter
- Validate session exists for each authenticated request
- Handle session expiration in JWT filter
- Cleanup sessions on logout

LOGIN INTEGRATION:
In AuthController.login() method:
1. Authenticate user credentials
2. Generate JWT tokens
3. Create user session with device info
4. Return tokens + session info
5. Log successful login with session details

JWT FILTER INTEGRATION:
In JwtAuthenticationFilter:
1. Validate JWT token as usual
2. Extract userId and deviceId from token
3. Validate session exists and is active
4. Update session activity with current endpoint
5. Handle session not found scenario

LOGOUT HANDLING:
Create logout endpoints:
- POST /api/auth/logout (single device logout)
- POST /api/auth/logout/all (all devices logout)
- Cleanup sessions and refresh tokens
- Audit log logout events

SESSION MONITORING SERVICE:
Create SessionMonitoringService for admin capabilities:
- getActiveSessionStatistics(): SessionStats
- getUserSessionHistory(String userId): List<SessionEvent>
- getActiveSessions(): List<UserSession>
- getSuspiciousActivity(): List<SecurityEvent>
- generateSessionReport(LocalDate date): SessionReport

MONITORING DTOS:
Create monitoring data classes:
- SessionStats: totalActive, byDeviceType, byLocation, averageDuration
- SessionEvent: userId, action, timestamp, deviceInfo, ipAddress
- SecurityEvent: eventType, severity, details, timestamp, userId
- SessionReport: summary statistics, detailed breakdown

ADMIN ENDPOINTS:
Create admin session monitoring endpoints:
- GET /api/admin/sessions/active
- GET /api/admin/sessions/statistics
- GET /api/admin/sessions/user/{userId}
- GET /api/admin/sessions/suspicious
- DELETE /api/admin/sessions/{sessionId} (force logout)

SESSION ANALYTICS:
Track and report on:
- Login patterns by time of day
- Device type distribution
- Geographic distribution (if location available)
- Session duration statistics
- Concurrent session trends

HEALTH CHECKS:
Add session-related health indicators:
- Redis connectivity for session storage
- Active session count
- Session cleanup job status
- Average session response times

Please create session integration with authentication and monitoring capabilities.
```

### Verification:
```bash
# Test complete authentication flow with sessions
mvn spring-boot:run &
sleep 10

# Test login creates session
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)" \
  -d '{"email":"test@example.com","password":"password"}'
# Expected: tokens returned, session created in Redis

# Test authenticated request updates session activity
TOKEN="..." # From login response
curl -H "Authorization: Bearer $TOKEN" \
  -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)" \
  http://localhost:8080/api/users/profile
# Expected: session lastActivity updated

# Check Redis for session data
redis-cli GET "session:user-uuid:device-id"
# Should show session with updated lastActivity

# Test session monitoring endpoints
ADMIN_TOKEN="..." # Admin user token
curl -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8080/api/admin/sessions/active
# Expected: active session statistics

curl -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8080/api/admin/sessions/statistics
# Expected: comprehensive session statistics

# Test logout cleans up session
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/auth/logout
# Expected: session removed from Redis

redis-cli GET "session:user-uuid:device-id"
# Should return (nil)

# Test concurrent session limits
# Login from multiple devices, verify oldest is evicted

pkill -f "spring-boot:run"
```

---

## Definition of Done Checklist

- [ ] UserSession type mismatch fixed (String userId instead of Long)
- [ ] SessionRepository updated with enhanced methods
- [ ] SessionManagementService implements device fingerprinting
- [ ] Device detection works for major browsers and platforms
- [ ] Authentication flow creates and manages sessions
- [ ] JWT filter validates sessions on each request
- [ ] Concurrent session limits enforced properly
- [ ] Session monitoring provides comprehensive statistics
- [ ] Admin endpoints allow session management
- [ ] Logout properly cleans up sessions and tokens
- [ ] Session analytics track usage patterns
- [ ] All integration tests pass with real Redis
- [ ] Performance acceptable under load

## Files to Create/Modify

**Modified Files:**
- `src/main/java/com/bojan/bootcamp_01/session/UserSession.java` (fix String userId)
- `src/main/java/com/bojan/bootcamp_01/session/SessionRepository.java` (update method signatures)
- `src/main/java/com/bojan/bootcamp_01/session/SessionRepositoryImpl.java` (implement String userId)
- `src/test/java/com/bojan/bootcamp_01/session/SessionRepositoryImplTest.java` (update tests)

**New Files:**
- `src/main/java/com/bojan/bootcamp_01/service/SessionManagementService.java`
- `src/main/java/com/bojan/bootcamp_01/service/DeviceFingerprintService.java`
- `src/main/java/com/bojan/bootcamp_01/service/SessionMonitoringService.java`
- `src/main/java/com/bojan/bootcamp_01/dto/DeviceInfo.java`
- `src/main/java/com/bojan/bootcamp_01/dto/UserAgentInfo.java`
- `src/main/java/com/bojan/bootcamp_01/dto/SessionStats.java`
- `src/main/java/com/bojan/bootcamp_01/dto/SessionEvent.java`
- `src/main/java/com/bojan/bootcamp_01/dto/SecurityEvent.java`
- `src/main/java/com/bojan/bootcamp_01/dto/SessionReport.java`
- `src/main/java/com/bojan/bootcamp_01/controller/SessionController.java`
- `src/test/java/com/bojan/bootcamp_01/service/SessionManagementServiceTest.java`
- `src/test/java/com/bojan/bootcamp_01/service/DeviceFingerprintServiceTest.java`
- `src/test/java/com/bojan/bootcamp_01/integration/SessionIntegrationTest.java`

## Next Task Dependencies

Task 5 (Security Configuration) requires:
- ✅ Session management integrated with authentication
- ✅ Session validation working in JWT filter
- ✅ Session monitoring available for security analysis
- ✅ Audit logging in place for security events

## Critical Notes

**Type Mismatch Priority**: The UserSession type mismatch MUST be fixed first as it affects all session operations and can cause runtime failures.

**Integration Dependency**: This task heavily depends on Tasks 1-3 being complete, as sessions need to integrate with JWT authentication, filters, and refresh tokens.

**Testing Strategy**: Use Redis Testcontainers for all integration tests to ensure Redis operations work correctly with the new session management system.
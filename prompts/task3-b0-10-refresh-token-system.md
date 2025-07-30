# Task 3: Refresh Token System (B0-10)

**Status**: Not Started  
**Dependencies**: Task 1 JWT Service (❌), Task 2 Authentication Filter (❌)

## Current State
- ✅ Redis infrastructure exists (RedisConfig, UserSession, SessionRepository)
- ✅ JWT properties configured with refresh token expiration
- ❌ **Missing: RefreshToken model and repository**
- ❌ **Missing: Refresh token service with rotation logic**
- ❌ **Missing: Refresh endpoint controller**
- ❌ **Missing: Token cleanup mechanism**

---

## Step 1: Create RefreshToken Model and Redis Configuration ❌

### Prompt:
```
Create RefreshToken model and enhance Redis configuration for refresh token storage:

CONTEXT:
- Existing Redis setup with RedisTemplate and UserSession
- Need separate storage for refresh tokens with different TTL
- User entity uses UUID for ID, but UserSession uses Long (type mismatch to fix later)
- Need secure token storage with hashed tokens

REFRESH TOKEN MODEL:
Create RefreshToken class with fields:
- tokenHash: String (SHA-256 hash of actual token for security)
- userId: String (UUID as string to match User entity)
- deviceId: String
- createdAt: Instant
- lastUsedAt: Instant
- expiresAt: Instant
- isActive: boolean
- isRevoked: boolean (for security breach detection)

SERIALIZATION:
- Implement Serializable for Redis storage
- Add Jackson annotations for JSON serialization
- Include proper constructors (no-args and all-args)
- Override toString() without exposing tokenHash

REDIS KEY PATTERNS:
- refresh_token:{userId}:{deviceId} → RefreshToken object
- refresh_token_by_hash:{tokenHash} → RefreshToken object (for lookup by hash)
- user_refresh_tokens:{userId} → Set of deviceIds (for user token management)

REDIS CONFIGURATION ENHANCEMENT:
Enhance existing RedisConfig to support RefreshToken:
- Add RedisTemplate<String, RefreshToken> bean
- Configure Jackson2JsonRedisSerializer for RefreshToken
- Set up TTL matching JWT refresh token expiration (from properties)
- Add ObjectMapper with proper datetime handling

Please create the RefreshToken model and enhance RedisConfig.
```

### Verification:
```bash
# Test RefreshToken serialization
mvn test -Dtest=RefreshTokenSerializationTest

# Test Redis configuration loads RefreshToken template
mvn test -Dtest=RedisConfigTest

# Check Redis can store/retrieve RefreshToken
mvn test -Dtest=RefreshTokenRedisTest

# Verify TTL is set correctly
redis-cli
> SET test:token "value" EX 86400
> TTL test:token
# Should show remaining seconds
```

---

## Step 2: Create RefreshToken Repository ❌

### Prompt:
```
Create RefreshTokenRepository using RedisTemplate with comprehensive token management operations:

CONTEXT:
- RefreshToken model exists with tokenHash, userId, deviceId fields
- RedisTemplate<String, RefreshToken> bean available
- Need atomic operations for token rotation security
- Must handle Redis connection failures gracefully

REPOSITORY INTERFACE:
Create RefreshTokenRepository interface with methods:
- save(RefreshToken token): void
- findByUserIdAndDeviceId(String userId, String deviceId): Optional<RefreshToken>
- findByTokenHash(String tokenHash): Optional<RefreshToken>
- deleteByUserIdAndDeviceId(String userId, String deviceId): boolean
- deleteAllByUserId(String userId): int
- deleteExpiredTokens(): int
- getAllTokensForUser(String userId): List<RefreshToken>

REPOSITORY IMPLEMENTATION:
Create RefreshTokenRepositoryImpl with:
- @Repository annotation
- Inject RedisTemplate<String, RefreshToken>
- Use proper key patterns from model design
- Implement sliding expiration on token use
- Use Redis transactions for atomic operations

REDIS OPERATIONS:
For each method implement:
- save(): Use SET with TTL, update user token set
- findByUserIdAndDeviceId(): GET with key pattern
- findByTokenHash(): GET with hash-based key
- deleteByUserIdAndDeviceId(): DELETE with cleanup of user set
- deleteAllByUserId(): Use SCAN pattern, delete all user tokens
- deleteExpiredTokens(): Use SCAN with TTL check, batch delete

ERROR HANDLING:
- Handle RedisConnectionException gracefully
- Implement retry logic for failed operations (3 attempts)
- Log Redis operation failures with structured logging
- Return empty Optional instead of throwing on not found
- Use @Retryable annotation for transient failures

TRANSACTION SUPPORT:
- Use RedisTemplate.execute() for atomic operations
- Implement token rotation as atomic operation
- Handle concurrent access to same token gracefully

Please create the complete RefreshTokenRepository interface and implementation.
```

### Verification:
```bash
# Test repository operations
mvn test -Dtest=RefreshTokenRepositoryTest

# Test atomic operations
mvn test -Dtest=RefreshTokenRepositoryTest#testAtomicRotation

# Test error handling
mvn test -Dtest=RefreshTokenRepositoryTest#testRedisConnectionFailure

# Test with real Redis instance
mvn test -Dtest=RefreshTokenRepositoryIntegrationTest

# Check Redis keys are created correctly
redis-cli KEYS "refresh_token:*"
redis-cli KEYS "user_refresh_tokens:*"
```

---

## Step 3: Create RefreshToken Service with Rotation Logic ❌

### Prompt:
```
Create RefreshTokenService with secure token rotation logic and comprehensive security measures:

CONTEXT:
- RefreshTokenRepository available for token persistence
- JwtService available for JWT generation and validation
- Need secure token rotation to prevent token theft
- Must detect and handle token reuse attacks

SERVICE INTERFACE AND IMPLEMENTATION:
Create RefreshTokenService with methods:
- createRefreshToken(User user, String deviceId): String
- refreshTokens(String refreshToken, String deviceId): TokenPair
- validateRefreshToken(String token): boolean
- revokeRefreshToken(String token): void
- revokeAllUserTokens(String userId): void
- cleanupExpiredTokens(): int

TOKEN PAIR DTO:
Create TokenPair record/class:
- accessToken: String
- refreshToken: String
- expiresIn: long (access token expiration in seconds)

TOKEN ROTATION LOGIC (refreshTokens method):
1. Hash the provided refresh token (SHA-256)
2. Find existing RefreshToken by hash in Redis
3. Validate token exists, not expired, not revoked, device matches
4. Generate new access token using JwtService
5. Generate new refresh token using JwtService
6. Create new RefreshToken object with new hash
7. Atomically: delete old token + save new token
8. Return TokenPair with new tokens

SECURITY FEATURES:
- Hash all tokens before storage (use SHA-256)
- Detect token reuse: if old token used after rotation, revoke all user tokens
- Device binding validation: token must match original device
- Rate limiting: max 10 refresh attempts per minute per user
- Audit logging: log all refresh attempts with IP, device, success/failure

TOKEN GENERATION:
- Use SecureRandom for token generation entropy
- Store token hash, never plain token
- Set appropriate TTL (30 days default from properties)
- Update lastUsedAt on each use

ERROR HANDLING:
- Custom exceptions: RefreshTokenException, TokenReusedException
- Handle Redis failures with proper fallback
- Log security events (token reuse, validation failures)
- Return appropriate error responses for different scenarios

Please create the complete RefreshTokenService with secure rotation logic.
```

### Verification:
```bash
# Test token creation and validation
mvn test -Dtest=RefreshTokenServiceTest#testCreateAndValidate

# Test token rotation
mvn test -Dtest=RefreshTokenServiceTest#testTokenRotation

# Test token reuse detection
mvn test -Dtest=RefreshTokenServiceTest#testTokenReuseDetection

# Test device binding validation
mvn test -Dtest=RefreshTokenServiceTest#testDeviceBinding

# Test rate limiting
mvn test -Dtest=RefreshTokenServiceTest#testRateLimiting

# Test error scenarios
mvn test -Dtest=RefreshTokenServiceTest#testErrorHandling
```

---

## Step 4: Create Refresh Endpoint Controller and Cleanup Mechanism ❌

### Prompt:
```
Create refresh token endpoint controller and automated cleanup mechanism:

CONTEXT:
- RefreshTokenService available with rotation logic
- Need REST endpoint for token refresh
- Need scheduled cleanup for expired tokens
- Must integrate with existing security configuration

REFRESH TOKEN CONTROLLER:
Create AuthController (or enhance existing) with refresh endpoint:
- @RestController with @RequestMapping("/api/auth")
- POST /api/auth/refresh endpoint
- Accept RefreshTokenRequest in request body
- Return TokenResponse with new token pair
- Handle all error scenarios with proper HTTP status

REQUEST/RESPONSE MODELS:
Create DTOs:
- RefreshTokenRequest: refreshToken (String), deviceId (String)
- TokenResponse: accessToken (String), refreshToken (String), expiresIn (long), tokenType ("Bearer")

CONTROLLER METHOD:
POST /api/auth/refresh implementation:
1. Validate request body (not null, required fields present)
2. Extract device info from request headers (User-Agent, X-Forwarded-For)
3. Call RefreshTokenService.refreshTokens()
4. Return TokenResponse with 200 OK
5. Handle exceptions with appropriate HTTP status codes

ERROR RESPONSES:
- 400 Bad Request: Missing/invalid request data
- 401 Unauthorized: Invalid or expired refresh token
- 403 Forbidden: Token reuse detected (security breach)
- 429 Too Many Requests: Rate limit exceeded
- 500 Internal Server Error: Server error

SCHEDULED CLEANUP MECHANISM:
Create @Component RefreshTokenCleanupService:
- @Scheduled method to clean expired tokens
- Run every 6 hours: @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
- Use RefreshTokenService.cleanupExpiredTokens()
- Log cleanup statistics (tokens removed, execution time)
- Add metrics/monitoring for cleanup operations

SECURITY HEADERS:
Add security headers to refresh responses:
- Cache-Control: no-store, no-cache, must-revalidate
- Pragma: no-cache
- X-Content-Type-Options: nosniff

AUDIT LOGGING:
Log refresh token operations:
- Successful refresh: INFO level with user, device, IP
- Failed refresh: WARN level with reason, IP, device
- Token reuse: ERROR level with security alert
- Use structured logging format for monitoring

Please create the refresh endpoint controller and cleanup mechanism.
```

### Verification:
```bash
# Start application and test refresh endpoint
mvn spring-boot:run &
sleep 10

# Test successful token refresh (need login first to get refresh token)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
# Extract refresh_token from response

REFRESH_TOKEN="..." # From login response
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\",\"deviceId\":\"test-device\"}"
# Expected: 200 OK with new access_token and refresh_token

# Test with invalid refresh token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"invalid-token\",\"deviceId\":\"test-device\"}"
# Expected: 401 Unauthorized

# Test token reuse (use old refresh token after successful refresh)
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$OLD_REFRESH_TOKEN\",\"deviceId\":\"test-device\"}"
# Expected: 403 Forbidden (security breach detected)

# Check Redis storage
redis-cli KEYS "refresh_token:*"
redis-cli GET "refresh_token_by_hash:*"

# Test scheduled cleanup (trigger manually or wait)
# Check logs for cleanup statistics

pkill -f "spring-boot:run"
```

---

## Definition of Done Checklist

- [ ] RefreshToken model created with proper Redis serialization
- [ ] RefreshTokenRepository implemented with atomic operations
- [ ] RefreshTokenService implements secure token rotation
- [ ] Token reuse detection prevents security breaches
- [ ] Refresh endpoint returns proper JSON responses
- [ ] Error handling covers all scenarios with correct HTTP status
- [ ] Scheduled cleanup removes expired tokens
- [ ] Device binding validation prevents token theft
- [ ] Rate limiting prevents brute force attacks
- [ ] Comprehensive audit logging implemented
- [ ] All integration tests pass
- [ ] Redis storage optimized with proper TTL

## Files to Create/Modify

**New Files:**
- `src/main/java/com/bojan/bootcamp_01/model/RefreshToken.java`
- `src/main/java/com/bojan/bootcamp_01/repository/RefreshTokenRepository.java`
- `src/main/java/com/bojan/bootcamp_01/repository/RefreshTokenRepositoryImpl.java`
- `src/main/java/com/bojan/bootcamp_01/service/RefreshTokenService.java`
- `src/main/java/com/bojan/bootcamp_01/service/RefreshTokenCleanupService.java`
- `src/main/java/com/bojan/bootcamp_01/controller/AuthController.java`
- `src/main/java/com/bojan/bootcamp_01/dto/RefreshTokenRequest.java`
- `src/main/java/com/bojan/bootcamp_01/dto/TokenResponse.java`
- `src/main/java/com/bojan/bootcamp_01/dto/TokenPair.java`
- `src/main/java/com/bojan/bootcamp_01/exception/RefreshTokenException.java`
- `src/main/java/com/bojan/bootcamp_01/exception/TokenReusedException.java`
- `src/test/java/com/bojan/bootcamp_01/service/RefreshTokenServiceTest.java`
- `src/test/java/com/bojan/bootcamp_01/controller/AuthControllerTest.java`
- `src/test/java/com/bojan/bootcamp_01/repository/RefreshTokenRepositoryTest.java`

**Modified Files:**
- `src/main/java/com/bojan/bootcamp_01/config/RedisConfig.java` (add RefreshToken support)

## Next Task Dependencies

Task 4 (Redis Session Management) requires:
- ✅ Refresh token system working for session validation
- ✅ Device binding established for session tracking
- ✅ Redis infrastructure proven stable
- ✅ User authentication flow complete

## Security Considerations

This task implements critical security features:
- **Token Rotation**: Prevents long-lived token compromise
- **Reuse Detection**: Detects and mitigates token theft
- **Device Binding**: Prevents cross-device token abuse
- **Rate Limiting**: Prevents brute force attacks
- **Audit Logging**: Enables security monitoring

The refresh token system is the foundation for secure, scalable authentication.
# Task 1: JWT Service Implementation (B0-8)

**Status**: Partially Complete - Missing Core Service Implementation  
**Dependencies**: JWT configuration (✅ Done), Redis setup (✅ Done)

## Current State
- ✅ JWT dependencies added to pom.xml
- ✅ JwtProperties configuration class exists
- ✅ SecretKey bean configured
- ✅ JWT secret key fixed (single, strong key)
- ✅ Basic JWT tests exist
- ❌ **Missing: Actual JwtService implementation**

---

## Step 1: Create JWT Token Generation Service ❌

### Prompt:
```
Create a complete JWT service for token generation using the existing JwtProperties configuration:

CONTEXT:
- Project uses Spring Boot WebMVC (not WebFlux)
- JWT configuration exists in JwtProperties.java with secretKey, accessTokenExpiration, refreshTokenExpiration
- SecretKey bean is already configured
- User entity has UUID getId(), String getEmail(), Set<Role> getRoles()
- Need device tracking with deviceId parameter

SERVICE REQUIREMENTS:
- @Service annotation for Spring component
- Inject JwtProperties and SecretKey beans
- Methods needed:
  * generateAccessToken(User user, String deviceId, String ipAddress, String userAgent): String
  * generateRefreshToken(User user, String deviceId): String

CLAIMS TO INCLUDE:
- Standard: sub (email), iat (issued at), exp (expiration)
- Custom: user_id (UUID as string), device_id, ip_address, user_agent, roles, token_type ("access" or "refresh")

IMPLEMENTATION DETAILS:
- Use JJWT library (already in dependencies)
- Set expiration from JwtProperties configuration
- Handle role conversion to String array for claims
- Use HS256 signing algorithm
- Include proper error handling for token generation

Please create the complete JwtService class with both generation methods.
```

### Verification:
```bash
# Test that the service can be instantiated
mvn test -Dtest=JwtServiceTest

# Check that tokens are generated (add a simple test)
# Token should be non-null and have proper structure
echo "eyJ..." | cut -d'.' -f2 | base64 -d | jq .
# Should show claims with user_id, device_id, token_type, etc.
```

---

## Step 2: Implement JWT Token Validation ❌

### Prompt:
```
Add JWT validation methods to the existing JwtService class:

VALIDATION METHODS NEEDED:
- validateToken(String token): boolean
- extractClaims(String token): Claims
- extractEmail(String token): String  
- extractUserId(String token): String (UUID as string)
- extractDeviceId(String token): String
- extractTokenType(String token): String
- extractRoles(String token): List<String>
- isTokenExpired(String token): boolean

EXCEPTION HANDLING:
- Handle ExpiredJwtException → return false/null gracefully
- Handle MalformedJwtException → return false/null gracefully
- Handle SignatureException → return false/null gracefully
- Handle UnsupportedJwtException → return false/null gracefully
- Add logging for security events (invalid tokens, etc.)

IMPLEMENTATION REQUIREMENTS:
- Use the same SecretKey bean for validation
- Parse claims using Claims object
- Handle missing claims gracefully (return null/empty)
- Add null checks for token parameter
- Use SLF4J logging for security events

SECURITY CONSIDERATIONS:
- Log failed validation attempts (but not the full token)
- Return consistent responses for different error types
- Don't expose internal error details to caller

Please add these validation methods to the JwtService class.
```

### Verification:
```bash
# Test token validation with valid token
mvn test -Dtest=JwtServiceTest#testTokenValidation

# Test token validation with expired token  
mvn test -Dtest=JwtServiceTest#testExpiredTokenValidation

# Test token validation with malformed token
mvn test -Dtest=JwtServiceTest#testMalformedTokenValidation

# Test claims extraction
mvn test -Dtest=JwtServiceTest#testClaimsExtraction
```

---

## Step 3: Add Device Validation and Security Features ❌

### Prompt:
```
Enhance the JwtService with device validation and security features:

DEVICE VALIDATION METHODS:
- validateDeviceBinding(String token, String deviceId): boolean
- validateTokenType(String token, String expectedType): boolean
- extractDeviceFingerprint(String token): DeviceInfo class

DEVICE INFO CLASS:
Create a simple DeviceInfo record/class with:
- deviceId: String
- ipAddress: String  
- userAgent: String
- tokenType: String

SECURITY ENHANCEMENT METHODS:
- isTokenRecentlyIssued(String token, int maxAgeMinutes): boolean
- getTokenRemainingTime(String token): Duration
- validateTokenIntegrity(String token): boolean (checks signature + expiration + format)

UTILITY METHODS:
- createDeviceFingerprint(String ipAddress, String userAgent): String
- parseUserAgent(String userAgent): String (extract browser/device info)

IMPLEMENTATION REQUIREMENTS:
- Add comprehensive null checks
- Use Duration class for time calculations
- Add logging for device validation failures
- Handle timezone considerations for token times
- Add constants for common validation rules

Please enhance the JwtService with these device and security features.
```

### Verification:
```bash
# Test device binding validation
mvn test -Dtest=JwtServiceTest#testDeviceBinding

# Test token type validation  
mvn test -Dtest=JwtServiceTest#testTokenTypeValidation

# Test device fingerprinting
mvn test -Dtest=JwtServiceTest#testDeviceFingerprinting

# Test security validation methods
mvn test -Dtest=JwtServiceTest#testSecurityValidation
```

---

## Step 4: Integration Tests and Error Handling ❌

### Prompt:
```
Create comprehensive integration tests and improve error handling for JwtService:

INTEGRATION TEST REQUIREMENTS:
Create JwtServiceIntegrationTest class with:
- @SpringBootTest annotation for full context loading
- Test JWT service with real Spring configuration
- Test with actual User entities from database
- Test token generation → validation → extraction flow
- Test with different user roles and permissions
- Test device binding with various device scenarios

ERROR HANDLING IMPROVEMENTS:
- Add custom exceptions: InvalidTokenException, ExpiredTokenException, DeviceBindingException
- Improve logging with structured format
- Add metrics/counters for token operations (optional)
- Add validation for JWT configuration on startup

PERFORMANCE TESTS:
- Test token generation performance (should be under 10ms)
- Test token validation performance (should be under 5ms)  
- Test with concurrent token operations
- Memory usage validation for token operations

EDGE CASE TESTS:
- Very long tokens (near size limits)
- Tokens with special characters in claims
- Tokens with null/empty device information
- Tokens issued in different timezones
- Tokens with maximum expiration times

Please create comprehensive integration tests and enhance error handling.
```

### Verification:
```bash
# Run all JWT service tests
mvn test -Dtest=*JwtService*

# Run integration tests specifically
mvn test -Dtest=JwtServiceIntegrationTest

# Check test coverage for JWT service
mvn jacoco:report
# Check target/site/jacoco/index.html for coverage

# Performance test (should complete quickly)
time mvn test -Dtest=JwtServiceIntegrationTest#testTokenGenerationPerformance

# Check logs for proper structured logging
grep "JWT" target/surefire-reports/*.txt
```

---

## Definition of Done Checklist

- [ ] JwtService class created with token generation methods
- [ ] Token validation methods implemented with proper error handling
- [ ] Device binding and security features added
- [ ] Custom exceptions created and properly used
- [ ] Integration tests cover all major scenarios
- [ ] Performance tests validate acceptable response times
- [ ] All existing tests still pass
- [ ] Logging follows structured format
- [ ] Code follows project conventions and style

## Files to Create/Modify

**New Files:**
- `src/main/java/com/bojan/bootcamp_01/service/JwtService.java`
- `src/main/java/com/bojan/bootcamp_01/dto/DeviceInfo.java`
- `src/main/java/com/bojan/bootcamp_01/exception/InvalidTokenException.java`
- `src/main/java/com/bojan/bootcamp_01/exception/ExpiredTokenException.java`
- `src/main/java/com/bojan/bootcamp_01/exception/DeviceBindingException.java`
- `src/test/java/com/bojan/bootcamp_01/service/JwtServiceIntegrationTest.java`

**Modified Files:**
- `src/test/java/com/bojan/bootcamp_01/config/JwtServiceTest.java` (enhance existing tests)

## Next Task Dependencies

Task 2 (Authentication Filter) requires:
- ✅ JwtService.validateToken() method
- ✅ JwtService.extractEmail() method  
- ✅ JwtService.extractClaims() method
- ✅ Proper exception handling from this task
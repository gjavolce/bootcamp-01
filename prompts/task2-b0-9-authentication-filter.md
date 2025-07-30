# Task 2: Authentication Filter (B0-9)

**Status**: Not Started  
**Dependencies**: Task 1 JWT Service (❌ Required)

## Current State
- ✅ Basic SecurityConfig exists (but uses permitAll() - needs replacement)
- ❌ **Missing: JWT Authentication Filter**
- ❌ **Missing: UserDetailsService implementation**
- ❌ **Missing: Proper security filter chain**
- ❌ **Missing: Authentication error handling**

---

## Step 1: Create JWT Authentication Filter ❌

### Prompt:
```
Create a Spring WebMVC JWT authentication filter that integrates with the existing project structure:

CONTEXT:
- Project has User entity with UUID getId(), String getEmail(), Set<Role> getRoles()
- JwtService exists with validateToken(), extractEmail(), extractClaims() methods
- Need to integrate with Spring Security WebMVC (not WebFlux)
- Current SecurityConfig uses permitAll() - will be replaced

FILTER REQUIREMENTS:
- Extend OncePerRequestFilter for Spring WebMVC
- Component name: JwtAuthenticationFilter
- Inject JwtService and UserDetailsService dependencies
- Process requests to /api/** paths (but exclude auth endpoints)

EXCLUDED PATHS (should skip JWT validation):
- /api/auth/login, /api/auth/register, /api/auth/refresh
- /actuator/health, /swagger-ui/**, /v3/api-docs/**
- /api/users (POST only for registration)

FILTER LOGIC:
1. Extract JWT from Authorization header (Bearer token format)
2. Validate token format and Bearer prefix
3. Use JwtService.validateToken() to check token validity
4. Extract email using JwtService.extractEmail()
5. Load UserDetails using UserDetailsService
6. Create Authentication object and set in SecurityContextHolder
7. Continue filter chain

ERROR HANDLING:
- Skip authentication for excluded paths
- Handle missing Authorization header gracefully
- Handle invalid token format (not Bearer)
- Handle expired/invalid tokens
- Log authentication attempts and failures
- Don't expose internal errors to client

Please create the complete JwtAuthenticationFilter class.
```

### Verification:
```bash
# Test that filter is registered as Spring component
mvn test -Dtest=ApplicationContextTest
# Should show JwtAuthenticationFilter bean in context

# Test filter logic with unit test
mvn test -Dtest=JwtAuthenticationFilterTest

# Test excluded paths are skipped
curl -v http://localhost:8080/api/auth/login
# Should process without requiring JWT
```

---

## Step 2: Create UserDetailsService Integration ❌

### Prompt:
```
Create UserDetailsService implementation that integrates with the existing User entity and repository:

CONTEXT:
- User entity exists with UUID getId(), String getEmail(), String getPasswordHash(), Set<Role> getRoles()
- UserRepository exists with findByEmail() method
- Role entity exists with String getName() method
- Need to convert User entity to Spring Security UserDetails

USERDETAILSSERVICE REQUIREMENTS:
- @Service annotation for Spring component
- Implement UserDetailsService interface
- Inject UserRepository dependency
- Method: loadUserByUsername(String email): UserDetails

USER DETAILS IMPLEMENTATION:
- Use email as username (since JWT uses email)
- Map User.getPasswordHash() to password
- Convert Set<Role> to Collection<GrantedAuthority>
- Map role names with "ROLE_" prefix (e.g., "ADMIN" → "ROLE_ADMIN")
- Handle account status (enabled, non-expired, etc.)

AUTHORITY MAPPING:
Create method: convertRolesToAuthorities(Set<Role> roles): Collection<GrantedAuthority>
- Map each role.getName() to SimpleGrantedAuthority
- Add "ROLE_" prefix if not present
- Handle null/empty roles gracefully
- Sort authorities for consistent behavior

ERROR HANDLING:
- Throw UsernameNotFoundException if user not found
- Handle disabled/locked accounts (User entity has lockoutUntil field)
- Add logging for user loading attempts
- Handle deleted users (User entity has deletedAt field)

CACHING CONSIDERATIONS:
- Consider @Cacheable annotation for frequently accessed users
- Use email as cache key
- Set reasonable cache expiration (e.g., 5 minutes)

Please create the complete UserDetailsService implementation.
```

### Verification:
```bash
# Test UserDetailsService implementation
mvn test -Dtest=CustomUserDetailsServiceTest

# Test authority mapping
mvn test -Dtest=CustomUserDetailsServiceTest#testRoleMapping

# Test user not found scenario
mvn test -Dtest=CustomUserDetailsServiceTest#testUserNotFound

# Test with real database data
mvn test -Dtest=CustomUserDetailsServiceIntegrationTest
```

---

## Step 3: Replace SecurityConfig with Proper JWT Configuration ❌

### Prompt:
```
Replace the existing permitAll() SecurityConfig with proper JWT-based Spring Security configuration:

CONTEXT:
- Current SecurityConfig.java uses permitAll() for all requests
- JwtAuthenticationFilter and UserDetailsService are now available
- Need WebMVC-based security (not WebFlux)
- Must integrate with existing Spring Boot 3.5.3 setup

SECURITY CONFIGURATION REQUIREMENTS:
- @Configuration and @EnableWebSecurity annotations
- SecurityFilterChain bean for WebMVC
- Replace permitAll() with proper authentication rules
- Add JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter
- Configure custom AuthenticationEntryPoint for 401 errors

ENDPOINT SECURITY MAPPING:
- Public endpoints: /api/auth/**, /actuator/health, /swagger-ui/**, /v3/api-docs/**
- Public POST: /api/users (for registration)
- Protected: /api/users/** (except POST), /api/** (all other API endpoints)
- Admin only: /api/admin/** (requires ROLE_ADMIN)

SESSION MANAGEMENT:
- SessionCreationPolicy.STATELESS (JWT is stateless)
- Disable session fixation protection
- Disable CSRF (not needed for stateless JWT)

AUTHENTICATION MANAGER:
- AuthenticationManager bean configuration
- PasswordEncoder bean (BCryptPasswordEncoder)
- Integrate UserDetailsService

FILTER CHAIN ORDER:
1. CORS filter (if needed)
2. JwtAuthenticationFilter
3. UsernamePasswordAuthenticationFilter
4. Other default filters

Please create the complete Spring Security WebMVC configuration replacing the current permitAll() setup.
```

### Verification:
```bash
# Test security configuration loads correctly
mvn spring-boot:run &
sleep 5

# Test public endpoints work without token
curl -v http://localhost:8080/api/auth/login
# Expected: Accessible (may return 400 for missing body)

# Test protected endpoints require authentication
curl -v http://localhost:8080/api/users
# Expected: 401 Unauthorized

# Test protected endpoint with valid token (need to implement AuthController first)
# TOKEN="..." 
# curl -v -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users
# Expected: 200 OK

pkill -f "spring-boot:run"
```

---

## Step 4: Create Authentication Error Handling ❌

### Prompt:
```
Create comprehensive authentication error handling for JWT authentication:

CONTEXT:
- JWT authentication can fail for various reasons (expired, invalid, missing)
- Need consistent JSON error responses
- Should not expose internal security details
- Must integrate with Spring Security exception handling

CUSTOM AUTHENTICATION ENTRY POINT:
- Implement AuthenticationEntryPoint interface
- Component name: JwtAuthenticationEntryPoint
- Handle 401 Unauthorized responses
- Return JSON format: {"error": "message", "timestamp": "...", "path": "...", "status": 401}

ERROR SCENARIOS TO HANDLE:
- Missing Authorization header
- Invalid Bearer token format (not "Bearer ...")
- Expired JWT token
- Invalid JWT signature
- Malformed JWT token
- User not found (from UserDetailsService)
- User account locked/disabled

RESPONSE FORMAT:
Create ErrorResponse DTO with fields:
- error: String (user-friendly message)
- timestamp: String (ISO format)
- path: String (request path)
- status: int (HTTP status code)

SECURITY LOGGING:
- Log authentication failures with structured format
- Include IP address, user agent, attempted endpoint
- Log level: WARN for failures, INFO for success
- Don't log sensitive information (full tokens, passwords)

ACCESS DENIED HANDLER:
- Implement AccessDeniedHandler for 403 Forbidden
- Handle insufficient privileges (wrong role)
- Similar JSON response format as 401

INTEGRATION:
- Register AuthenticationEntryPoint in SecurityConfig
- Register AccessDeniedHandler in SecurityConfig
- Add structured logging configuration

Please create comprehensive authentication error handling components.
```

### Verification:
```bash
# Test authentication error responses
mvn spring-boot:run &
sleep 5

# Test missing Authorization header
curl -v http://localhost:8080/api/users
# Expected: 401 with JSON error response

# Test invalid token format
curl -v -H "Authorization: InvalidFormat" http://localhost:8080/api/users  
# Expected: 401 with JSON error response

# Test malformed token
curl -v -H "Authorization: Bearer invalid-token" http://localhost:8080/api/users
# Expected: 401 with JSON error response

# Test access denied (need valid token but insufficient role)
# curl -v -H "Authorization: Bearer $USER_TOKEN" http://localhost:8080/api/admin/test
# Expected: 403 with JSON error response

# Check logs for structured error logging
grep "Authentication" logs/application.log

pkill -f "spring-boot:run"
```

---

## Definition of Done Checklist

- [ ] JwtAuthenticationFilter created and registered as Spring component
- [ ] UserDetailsService implementation converts User entities to UserDetails
- [ ] SecurityConfig replaced permitAll() with proper JWT authentication
- [ ] Authentication error handling returns consistent JSON responses
- [ ] Protected endpoints require valid JWT tokens
- [ ] Public endpoints accessible without authentication
- [ ] Role-based authorization works (USER vs ADMIN)
- [ ] Comprehensive error logging implemented
- [ ] All integration tests pass
- [ ] No security vulnerabilities introduced

## Files to Create/Modify

**New Files:**
- `src/main/java/com/bojan/bootcamp_01/security/JwtAuthenticationFilter.java`
- `src/main/java/com/bojan/bootcamp_01/security/CustomUserDetailsService.java`
- `src/main/java/com/bojan/bootcamp_01/security/JwtAuthenticationEntryPoint.java`
- `src/main/java/com/bojan/bootcamp_01/security/JwtAccessDeniedHandler.java`
- `src/main/java/com/bojan/bootcamp_01/dto/ErrorResponse.java`
- `src/test/java/com/bojan/bootcamp_01/security/JwtAuthenticationFilterTest.java`
- `src/test/java/com/bojan/bootcamp_01/security/CustomUserDetailsServiceTest.java`
- `src/test/java/com/bojan/bootcamp_01/security/SecurityConfigIntegrationTest.java`

**Modified Files:**
- `src/main/java/com/bojan/bootcamp_01/config/SecurityConfig.java` (replace permitAll() configuration)

## Next Task Dependencies

Task 3 (Refresh Token System) requires:
- ✅ JWT authentication working end-to-end
- ✅ SecurityConfig properly configured
- ✅ Error handling in place
- ✅ User authentication flow established

## Integration Notes

This task creates the foundation for all subsequent authentication features. Without proper JWT validation and Spring Security integration, refresh tokens and session management cannot function correctly.
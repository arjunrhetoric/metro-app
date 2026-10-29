# 02 — Authentication and Roles

This file explains **who is allowed in** and **what each person may do**: HTTP Basic authentication, BCrypt passwords, the four roles, `SecurityConfig`, `DbUserDetailsService`, `AuthController`, the Swagger setup, Flyway, and the bootstrap admin.

Related file: `01-USER.md` explains the `User` entity and the admin CRUD at `/api/users`.

---

## 1. A real-life analogy: the office building

Imagine a company building with one entrance.

- At the door there is a **security guard**.
- You show an **ID card** with your name and a password/PIN.
- The guard checks the card against the **employee register**. If your name is not there, or the PIN is wrong, you cannot enter. This is **authentication**: proving who you are.
- Once inside, some doors need a **higher access level**. An intern can enter the cafeteria; only the director can enter the server room. This is **authorization**: what you are allowed to do.
- The guard does **not remember you** between visits. Every time you come back, you show the card again. That is a **stateless** system.
- The register stores a **scrambled fingerprint** of your PIN, not the PIN itself. The guard scrambles what you type and compares the two fingerprints. That is **BCrypt hashing**.
- A person whose card is marked **inactive** is refused at the door, even with the right PIN.

Mapping:

| Building | Project |
|---|---|
| ID card | `Authorization: Basic ...` header |
| Name on the card | `Contact` column |
| PIN | Password |
| Employee register | `User` table |
| Guard | Spring Security filter chain |
| Guard's lookup | `DbUserDetailsService` |
| Fingerprint comparison | `BCryptPasswordEncoder.matches` |
| Floor access list | `Role` and `@PreAuthorize` |
| Rulebook at the door | `SecurityConfig` |
| "Inactive card" | `isActive = false` |
| "Who am I?" desk | `GET /api/auth/me` |

Two words to keep separate for the rest of your life:

- **Authentication** = "Who are you?" → failure is **401 Unauthorized**.
- **Authorization** = "Are you allowed?" → failure is **403 Forbidden**.

---

## 2. Words you need

| Word | Simple meaning |
|---|---|
| Authentication | Proving your identity with a username and password. |
| Authorization | Checking whether an identified user may perform an action. |
| Role | A job label: `ADMIN`, `TICKET_AGENT`, `MAINTENANCE_STAFF`, `PASSENGER`. |
| Authority | The string Spring Security actually checks, for example `ROLE_ADMIN`. |
| HTTP Basic Auth | Send `username:password` in a header on every request. |
| Base64 | A reversible text encoding used by Basic Auth; **not encryption**. |
| Header | Metadata sent with an HTTP request, such as `Authorization`. |
| `WWW-Authenticate` | Response header that tells the client to send Basic credentials. |
| BCrypt | A slow, salted, one-way password hash. |
| Hash | A fixed-length fingerprint of data; cannot be reversed. |
| Salt | Random bytes mixed into a hash so equal passwords differ. |
| `PasswordEncoder` | Spring interface with `encode` and `matches`. |
| `BCryptPasswordEncoder` | The BCrypt implementation used here. |
| Spring Security | The library that guards the API. |
| Filter | Code that runs before the controller and can block a request. |
| Filter chain | The ordered list of security filters. |
| `SecurityFilterChain` | The bean that configures which filters apply and which URLs are protected. |
| `AuthenticationProvider` | The component that verifies credentials. |
| `DaoAuthenticationProvider` | A provider that loads users from a database and checks the password. |
| `UserDetailsService` | Spring interface with `loadUserByUsername`. |
| `UserDetails` | Spring's view of a logged-in user: username, password, authorities, enabled flag. |
| Principal | The currently logged-in identity. |
| `SecurityContext` | Per-request storage for the principal. |
| Stateless | No server-side session; credentials are checked on every request. |
| Session | Server memory tied to a cookie. Not used here. |
| CSRF | An attack on cookie-based sessions; disabled because this API is stateless. |
| `permitAll` | Anyone may call this URL, even without login. |
| `authenticated` | Any logged-in user may call this URL. |
| `hasRole("ADMIN")` | Requires authority `ROLE_ADMIN`. |
| `@EnableWebSecurity` | Turns on Spring Security's web support. |
| `@EnableMethodSecurity` | Turns on `@PreAuthorize` checks. |
| `@Service` | Marks a class as a Spring service bean. |
| `@Override` | Tells the compiler that a method overrides an interface method. |
| `AuthenticationManager` | The component that starts credential verification. |
| `UsernamePasswordAuthenticationToken` | The object that carries the typed username and password into the provider. |
| `SecurityContextHolder` | The per-request storage for the authenticated principal. |
| `DisabledException` | Thrown when the account's enabled flag is false. |
| `HttpMessageNotReadableException` | Jackson's "cannot read this JSON" exception. |
| Logger | An SLF4J object used to write log lines. |
| Springdoc | The library that generates OpenAPI and Swagger UI. |
| `@PreAuthorize` | A role check placed on a controller method. |
| `@Configuration` | Marks a class that defines beans. |
| `@Bean` | A method whose return value is managed by Spring. |
| Dependency injection | Spring passes required objects through a constructor. |
| `@Value` | Reads a property or environment variable into a field. |
| `ApplicationRunner` | Code that runs once after the application has started. |
| Bootstrap | The very first admin account, created automatically. |
| Flyway | Runs numbered SQL migration files. |
| Migration | A versioned SQL change, for example `V1__...sql`. |
| Baseline | Marks an existing database as "already at version N". |
| `ddl-auto=validate` | Hibernate checks the schema but never changes it. |
| Environment variable | An OS-level value such as `DB_PASSWORD`. |
| Placeholder | `${NAME:default}` syntax in `application.properties`. |
| OpenAPI | The machine-readable API description. |
| Swagger UI | The web page generated from OpenAPI. |
| `SecurityRequirement` | OpenAPI metadata that adds a padlock to endpoints. |
| UUID | Long random identifier. |
| 401 Unauthorized | "I do not know who you are." |
| 403 Forbidden | "I know you, but you may not do this." |

---

## 3. Files in this group

Project root: `C:\Users\arjun\Desktop\metro-ticketing-app`

| Role | Exact path |
|---|---|
| Application entry point | `src/main/java/com/metro/ticketing/TicketingApplication.java` |
| Security rules | `src/main/java/com/metro/ticketing/config/SecurityConfig.java` |
| Database login lookup | `src/main/java/com/metro/ticketing/config/DbUserDetailsService.java` |
| Swagger / OpenAPI setup | `src/main/java/com/metro/ticketing/config/OpenApiConfig.java` |
| Flyway setup | `src/main/java/com/metro/ticketing/config/FlywayConfig.java` |
| Bootstrap admin and demo data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` |
| Auth endpoints | `src/main/java/com/metro/ticketing/controller/AuthController.java` |
| Role enum | `src/main/java/com/metro/ticketing/entity/Role.java` |
| User entity | `src/main/java/com/metro/ticketing/entity/User.java` |
| User repository | `src/main/java/com/metro/ticketing/repository/UserRepository.java` |
| Configuration | `src/main/resources/application.properties` |
| User table SQL | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Old token tables | `src/main/resources/db/migration/V3__add_auth_lifecycle_tables.sql`, `V4__widen_auth_token_columns.sql`, `V5__drop_unused_auth_token_tables.sql` |

---

## 4. The database side of authentication

### 4.1 The columns that matter

```sql
CREATE TABLE IF NOT EXISTS "User" (
    "UserId" VARCHAR(50) PRIMARY KEY,
    "FirstName" VARCHAR(100) NOT NULL,
    "LastName" VARCHAR(100) NOT NULL,
    "Password" VARCHAR(255) NOT NULL,
    "Role" VARCHAR(50) NOT NULL,
    "Contact" VARCHAR(50),
    "RegisterationDate" DATE,
    "isActive" BOOLEAN DEFAULT TRUE,
    "HireDate" DATE,
    "StationID" VARCHAR(50)
);

CREATE UNIQUE INDEX IF NOT EXISTS "UX_User_Contact" ON "User" ("Contact");
```

| Column | Role in login | Notes |
|---|---|---|
| `Contact` | The username | Unique index. Login looks here first. |
| `Password` | The stored BCrypt hash | About 60 characters; column allows 255. |
| `Role` | The permission label | Converted to `ROLE_<name>` for Spring Security. |
| `isActive` | Account switch | `false` makes the account "disabled" and blocks login. |
| `UserId` | Not used during login | Used as the identity in tickets, cards, and issues. |
| `FirstName`, `LastName` | Not used during login | Returned by `/api/auth/me`. |
| `HireDate`, `StationID` | Not used during login | Staff details. |

### 4.2 The short history of the auth tables

- `V3__add_auth_lifecycle_tables.sql` created `RefreshToken` and `PasswordResetToken`. That suggests an older JWT/token design.
- `V4__widen_auth_token_columns.sql` made token columns larger.
- `V5__drop_unused_auth_token_tables.sql` drops both tables, with the comment that the JWT flow was removed when authentication moved to HTTP Basic.

So the current project has **no JWT, no refresh token, and no server-side session**. Every request carries the username and password.

### 4.3 Why there is no "sessions" table

Because the server is stateless. Nothing about a login is stored between requests. The database only stores the user record; Spring Security re-checks it on every call.

---

## 5. Entity and enum, line by line

### 5.1 `Role.java` — the complete file

```java
package com.metro.ticketing.entity;   // The enum lives beside the entities.

public enum Role {                    // enum = a fixed set of allowed constants.
    ADMIN,                            // Full control: users, stations, trains, routes, fares, deletes.
    TICKET_AGENT,                     // Sells tickets and tops up cards.
    MAINTENANCE_STAFF,                // Reports and resolves maintenance issues.
    PASSENGER                         // Travels; books tickets and owns cards/payments.
}
```

Why an enum instead of a `String`?

- Java rejects typos such as `"PASSANGER"` at compile time.
- The allowed values are discoverable in one place.
- `switch` over an enum can be checked for completeness by the compiler.
- The database still stores text, via `@Enumerated(EnumType.STRING)`.

### 5.2 The four auth-related fields of `User.java`

The complete entity walkthrough is in `01-USER.md`. Here are the four fields that authentication actually reads:

```java
@NotBlank(message = "Password is required")                     // Password is mandatory on create.
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)          // Accept it in JSON, never return it.
@Column(name = "\"Password\"", length = 255, nullable = false)  // Store the BCrypt hash.
private String password;                                        // Never plain text after the controller runs.

@Enumerated(EnumType.STRING)                                    // Store the role name as text.
@Column(name = "\"Role\"", length = 50, nullable = false)
private Role role;                                              // Drives every permission check.

@NotBlank(message = "Contact is required")                      // Contact is mandatory on create.
@Column(name = "\"Contact\"", length = 50)
private String contact;                                         // The login username.

@Column(name = "\"isActive\"")
@JsonProperty("isActive")                                       // Exact JSON name.
private Boolean isActive = true;                                // false = disabled account.
```

Their accessors:

```java
public String getPassword() {                                   // Spring Security calls this at login.
    return password;                                            // Returns the stored hash.
}

public void setPassword(String password) {                      // Controllers call this with a hash.
    this.password = password;                                   // Never call it with a raw password.
}

public Role getRole() {                                         // Returns the enum.
    return role;                                                // Used in DbUserDetailsService.
}

public void setRole(Role role) {                                // Sets the enum.
    this.role = role;                                           // Changing it changes permissions immediately.
}

public String getContact() {                                    // Returns the username.
    return contact;                                             // Used by login and /me.
}

public void setContact(String contact) {                        // Sets the username.
    this.contact = contact;                                     // Must stay unique.
}

public Boolean getIsActive() {                                  // Returns the flag.
    return isActive;                                            // Could be null in old rows.
}

public void setIsActive(Boolean isActive) {                     // Sets the flag.
    this.isActive = isActive;                                   // false disables login.
}
```

### 5.3 What Spring Security does with these fields

`DbUserDetailsService` converts our `User` into Spring's `UserDetails`:

```java
org.springframework.security.core.userdetails.User
        .withUsername(user.getContact())        // username = contact
        .password(user.getPassword())           // stored BCrypt hash
        .roles(user.getRole().name())           // "ADMIN" becomes authority "ROLE_ADMIN"
        .disabled(!Boolean.TRUE.equals(user.getIsActive())) // null counts as disabled
        .build();
```

That single `.roles(...)` call is why `hasRole('ADMIN')` works. Spring adds the `ROLE_` prefix automatically:

| Our role | Stored authority |
|---|---|
| `ADMIN` | `ROLE_ADMIN` |
| `TICKET_AGENT` | `ROLE_TICKET_AGENT` |
| `MAINTENANCE_STAFF` | `ROLE_MAINTENANCE_STAFF` |
| `PASSENGER` | `ROLE_PASSENGER` |

`hasRole("ADMIN")` also adds the prefix, so both sides match. `hasAuthority("ADMIN")` would **not** match, because the stored value is `ROLE_ADMIN`.

---

## 6. The repository method behind login

```java
package com.metro.ticketing.repository;                       // Repository package.

import com.metro.ticketing.entity.User;                       // Entity type.
import org.springframework.data.jpa.repository.JpaRepository; // Built-in CRUD.
import org.springframework.stereotype.Repository;             // Bean marker.

import java.util.Optional;                                    // Empty-or-value container.

@Repository                                                   // Spring creates the implementation.
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByContact(String contact);             // SELECT ... WHERE "Contact" = ?
}
```

Generated SQL:

```sql
SELECT "UserId", "FirstName", "LastName", "Password", "Role",
       "Contact", "RegisterationDate", "isActive", "HireDate", "StationID"
FROM "User"
WHERE "Contact" = ?;
```

Used by:

1. `DbUserDetailsService.loadUserByUsername` — the actual login.
2. `AuthController.registerPassenger` — duplicate check.
3. `AuthController.hireEmployee` — duplicate check.
4. `AuthController.getCurrentUser` — profile lookup.
5. Ticket, card, and payment controllers — ownership checks.

If the contact is not found, `Optional` is empty and `DbUserDetailsService` throws `UsernameNotFoundException`. Spring hides that from the client and reports a generic `401`, so an attacker cannot tell whether the username exists.

---

## 7. Endpoints in this group

Base URL: `http://localhost:8080`

| Method | URL | Who may call | Purpose | Success |
|---|---|---|---|---|
| POST | `/api/auth/passenger/register` | Public (no login) | Create a passenger account | `200 OK` |
| POST | `/api/auth/employee/hire` | ADMIN | Create an employee account | `200 OK` |
| GET | `/api/auth/me` | Any logged-in user | Return the current profile | `200 OK` |
| POST | `/api/users` | ADMIN | Create any user (see 01) | `201 Created` |
| GET | `/api/users` | ADMIN | List users (see 01) | `200 OK` |
| GET | `/api/users/{id}` | ADMIN | Read a user (see 01) | `200 OK` |
| PUT | `/api/users/{id}` | ADMIN | Update a user (see 01) | `200 OK` |
| DELETE | `/api/users/{id}` | ADMIN | Delete a user (see 01) | `204 No Content` |

`/api/auth/passenger/register` is the only business endpoint open to the public. Everything else needs credentials.

---

## 8. Code line by line

### 8.1 `AuthController` — the whole file

```java
package com.metro.ticketing.controller;                        // Controller package.

import com.metro.ticketing.entity.Role;                        // The enum.
import com.metro.ticketing.entity.Station;                     // Station lookup for hiring.
import com.metro.ticketing.entity.User;                        // Request and response type.
import com.metro.ticketing.repository.StationRepository;       // Finds stations.
import com.metro.ticketing.repository.UserRepository;          // Finds and saves users.
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
                                                               // Swagger-only: remove the global lock.
import jakarta.validation.Valid;                               // Runs @NotBlank checks.
import org.springframework.http.ResponseEntity;                // Status + body wrapper.
import org.springframework.security.access.prepost.PreAuthorize;
                                                               // Method-level role check.
import org.springframework.security.core.Authentication;       // Current logged-in user.
import org.springframework.security.crypto.password.PasswordEncoder;
                                                               // BCrypt encoder.
import org.springframework.transaction.annotation.Transactional;
                                                               // Transaction boundary.
import org.springframework.web.bind.annotation.*;              // All web annotations.

import java.time.LocalDate;                                    // Today's date.
import java.util.UUID;                                         // Random IDs.

@RestController                                                // Returns JSON.
@RequestMapping("/api/auth")                                   // Prefix for all three routes.
public class AuthController {

    private final UserRepository userRepository;               // Users table access.
    private final StationRepository stationRepository;         // Stations table access.
    private final PasswordEncoder passwordEncoder;             // Shared BCrypt bean.

    public AuthController(UserRepository userRepository,
                          StationRepository stationRepository,
                          PasswordEncoder passwordEncoder) {    // Spring injects all three.
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/passenger/register")                        // POST /api/auth/passenger/register
    @Transactional                                             // One transaction.
    @SecurityRequirements                                      // Swagger: no padlock on this method.
    public ResponseEntity<User> registerPassenger(@Valid @RequestBody User request) {
                                                               // @Valid checks first/last/password/contact.
        if (userRepository.findByContact(request.getContact()).isPresent()) {
            throw new IllegalArgumentException("User with this contact already exists.");
        }                                                      // 400 if the contact is taken.

        User user = new User();                                // Build a clean entity.
        user.setUserId("PASS-" + UUID.randomUUID());           // Always a passenger ID.
        user.setFirstName(request.getFirstName());             // Copy the first name.
        user.setLastName(request.getLastName());               // Copy the last name.
        user.setContact(request.getContact());                 // Copy the login name.
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash the password.
        user.setRole(Role.PASSENGER);                          // Force PASSENGER, ignore any role sent.
        user.setRegistrationDate(LocalDate.now());             // Today.
        user.setIsActive(true);                                // Active immediately.

        return ResponseEntity.ok(userRepository.save(user));   // 200 OK + saved user JSON.
    }

    @PostMapping("/employee/hire")                             // POST /api/auth/employee/hire
    @PreAuthorize("hasRole('ADMIN')")                          // Admins only.
    @Transactional                                             // One transaction.
    public ResponseEntity<User> hireEmployee(@Valid @RequestBody User request) {
        if (userRepository.findByContact(request.getContact()).isPresent()) {
            throw new IllegalArgumentException("User with this contact already exists.");
        }                                                      // Duplicate contact -> 400.
        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role is required.");
        }                                                      // Role must be supplied.
        if (request.getStationId() == null) {
            throw new IllegalArgumentException("Station is required.");
        }                                                      // Employees must be posted somewhere.

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Station not found."));
                                                               // Missing station -> 400.

        User user = new User();                                // Build a clean entity.
        user.setUserId("EMP-" + UUID.randomUUID());            // Employee-style ID.
        user.setFirstName(request.getFirstName());             // Copy the first name.
        user.setLastName(request.getLastName());               // Copy the last name.
        user.setContact(request.getContact());                 // Copy the login name.
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash.
        user.setRole(request.getRole());                       // Honour the requested role.
        user.setHireDate(LocalDate.now());                     // Hired today.
        user.setRegistrationDate(LocalDate.now());             // Registered today.
        user.setStation(station);                              // Attach the station object.
        user.setIsActive(true);                                // Active immediately.

        return ResponseEntity.ok(userRepository.save(user));   // 200 OK + saved user JSON.
    }

    @GetMapping("/me")                                         // GET /api/auth/me
    @Transactional(readOnly = true)                            // Read-only transaction.
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
                                                               // Spring injects the current principal.
        User user = userRepository.findByContact(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
                                                               // getName() is the username = contact.
        return ResponseEntity.ok(user);                        // 200 OK + profile JSON.
    }
}
```

Three details worth memorising:

1. **Register ignores the requested role.** Even if the JSON says `"role":"ADMIN"`, the saved role is `PASSENGER`.
2. **Hire does not restrict the role.** An admin could hire an `ADMIN` or even a `PASSENGER` through this endpoint, but the ID still starts with `EMP-`. The generic `POST /api/users` endpoint is more flexible and is the better tool for that.
3. **`/me` uses `Authentication.getName()`.** In Spring Security that is the username passed to `loadUserByUsername`, which here is the contact.

### 8.2 `SecurityConfig` — the rulebook

```java
package com.metro.ticketing.config;

import org.springframework.context.annotation.Bean;             // Marks bean methods.
import org.springframework.context.annotation.Configuration;    // Marks a config class.
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;           // withDefaults() helper.
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration                                                 // This class defines beans.
@EnableWebSecurity                                             // Turn on web security.
@EnableMethodSecurity                                          // Turn on @PreAuthorize.
public class SecurityConfig {

    private final DbUserDetailsService userDetailsService;     // Our database-backed lookup.

    public SecurityConfig(DbUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;          // Constructor injection.
    }

    @Bean                                                      // Spring uses this to secure requests.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)             // No CSRF: stateless API, no cookies.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/passenger/register",            // Public registration.
                    "/v3/api-docs/**",                         // OpenAPI JSON.
                    "/swagger-ui/**",                          // Swagger assets.
                    "/swagger-ui.html",                        // Swagger redirect page.
                    "/error"                                   // Spring's error page.
                ).permitAll()                                  // No login for the list above.
                .requestMatchers("/api/auth/employee/hire").hasRole("ADMIN")
                                                               // Hiring is admin-only.
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                                                               // User CRUD is admin-only.
                .anyRequest().authenticated()                  // Everything else needs a login.
            )
            .httpBasic(Customizer.withDefaults())              // Enable Basic auth.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                                               // Never create an HTTP session.
            .authenticationProvider(authenticationProvider()); // Use our provider below.

        return http.build();                                   // Finish the configuration.
    }

    private AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(userDetailsService); // Load users from the database.
        authProvider.setPasswordEncoder(passwordEncoder());        // Compare with BCrypt.
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();                    // Default strength 10.
    }
}
```

Why both URL rules and `@PreAuthorize`?

- The URL rule stops the request **before** the controller. It is a cheap, central gate.
- `@PreAuthorize` protects the method even if it is called from somewhere else later, and it documents the rule right above the code.
- Duplication is intentional defence in depth. If someone edits one rule, the other still holds.

Why disable CSRF? CSRF attacks rely on the browser automatically attaching cookies. This API never sets a session cookie, so there is nothing to hijack. Disabling CSRF also makes `curl` and Swagger testing simpler.

### 8.3 `DbUserDetailsService` — the bridge between our table and Spring

```java
package com.metro.ticketing.config;

import com.metro.ticketing.entity.User;                        // Our entity.
import com.metro.ticketing.repository.UserRepository;          // Our repository.
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service                                                       // A Spring service bean.
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;               // Database access.

    public DbUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;                  // Constructor injection.
    }

    @Override                                                  // Implements the interface method.
    public UserDetails loadUserByUsername(String contact) throws UsernameNotFoundException {
        User user = userRepository.findByContact(contact)      // Look up by the login name.
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + contact));        // Empty -> login fails.

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getContact())               // Keep the contact as the name.
                .password(user.getPassword())                  // Hand over the stored hash.
                .roles(user.getRole().name())                  // Adds the ROLE_ prefix.
                .disabled(!Boolean.TRUE.equals(user.getIsActive()))
                                                               // null or false -> disabled.
                .build();                                      // Build the UserDetails object.
    }
}
```

Spring never sees our `User` class directly. It sees a standard `UserDetails` with four facts: username, password hash, authorities, and enabled/disabled.

Why not let `User` implement `UserDetails`? It would work, but it would mix persistence and security concerns. This adapter class keeps the entity clean.

### 8.4 `OpenApiConfig` — making Swagger understand Basic Auth

```java
package com.metro.ticketing.config;

import io.swagger.v3.oas.models.Components;                    // Holds reusable OpenAPI parts.
import io.swagger.v3.oas.models.OpenAPI;                       // Root documentation object.
import io.swagger.v3.oas.models.Operation;                     // One API operation.
import io.swagger.v3.oas.models.info.Contact;                  // Contact info block.
import io.swagger.v3.oas.models.info.Info;                     // Title/version block.
import io.swagger.v3.oas.models.info.License;                  // License block.
import io.swagger.v3.oas.models.security.SecurityRequirement;  // "This endpoint needs auth."
import io.swagger.v3.oas.models.security.SecurityScheme;       // "Here is how auth works."
import org.springdoc.core.customizers.OperationCustomizer;     // Hook to tweak operations.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;           // The controller method.

import java.util.Collections;                                  // Empty list helper.

@Configuration                                                 // Config class.
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "basicAuth"; // Reusable scheme name.

    @Bean
    public OpenAPI metroTicketingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Metro Ticketing System API")   // Shown at the top of Swagger.
                        .description("REST API documentation for Metro Ticketing System, "
                                + "covering Stations, Trains, Routes, Schedules, Fares, "
                                + "Tickets, Metro Cards, Payments, and Maintenance.")
                        .version("1.0.0")                      // API version.
                        .contact(new Contact().name("Metro Transit Authority Support"))
                        .license(new License().name("Apache 2.0")
                                .url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                                                               // Apply security globally.
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP) // HTTP authentication.
                                .scheme("basic")               // The Basic scheme.
                                .description("Enter your database username (contact) and password.")));
    }

    @Bean
    public OperationCustomizer publicEndpointCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            if ("registerPassenger".equals(handlerMethod.getMethod().getName())) {
                operation.setSecurity(Collections.emptyList()); // Remove the padlock for registration.
            }
            return operation;                                  // Leave every other operation unchanged.
        };
    }
}
```

This class changes **documentation only**. It does not secure anything; `SecurityConfig` does. The customizer is a second way of marking registration public, alongside `@SecurityRequirements` on the controller method.

### 8.5 `FlywayConfig` — create the schema before JPA checks it

```java
package com.metro.ticketing.config;

import org.flywaydb.core.Flyway;                               // Migration engine.
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;                                  // The database connection pool.

@Configuration                                                 // Config class.
public class FlywayConfig {

    @Bean
    Flyway flyway(DataSource dataSource) {                     // Spring injects the datasource.
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)                        // Use the same database as JPA.
                .baselineOnMigrate(true)                       // Allow an existing database.
                .baselineVersion("1")                          // Treat it as version 1.
                .load();
        flyway.migrate();                                      // Run V1..V6 now, at startup.
        return flyway;                                         // Expose the bean.
    }

    @Bean
    static EntityManagerFactoryDependsOnPostProcessor entityManagerFactoryDependsOnFlyway() {
        return new EntityManagerFactoryDependsOnPostProcessor(Flyway.class);
                                                               // JPA must wait for Flyway.
    }
}
```

Startup order:

1. Read `application.properties`.
2. Create the datasource.
3. Run Flyway migrations.
4. Start Hibernate and run `ddl-auto=validate`.
5. Run `DataSeeder`.
6. Start Tomcat (port 8080) — actually Tomcat starts earlier, but requests are not served until the context is ready.

Without the `EntityManagerFactoryDependsOnPostProcessor`, Hibernate might validate the schema before Flyway creates it, and startup would fail on an empty database.

Why `baselineOnMigrate`? If the database already has tables from an older run, Flyway would normally refuse to migrate a non-empty schema. Baseline mode says "assume it is already at version 1" and applies `V2` onward.

### 8.6 `DataSeeder` — the bootstrap admin and demo accounts

Key parts of the file:

```java
@Component                                                     // A Spring bean.
public class DataSeeder implements ApplicationRunner {        // Runs once after startup.

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEMO_PASSWORD = "Passw0rd!";   // Shared demo password.

    @Value("${app.bootstrap.admin.contact:admin}")             // Property with default "admin".
    private String adminContact;

    @Value("${app.bootstrap.admin.password:admin123}")         // Default "admin123".
    private String adminPassword;

    @Value("${app.bootstrap.admin.first-name:System}")
    private String adminFirstName;

    @Value("${app.bootstrap.admin.last-name:Administrator}")
    private String adminLastName;

    @Override
    public void run(ApplicationArguments args) {
        ensureAdminExists();                                   // Always ensure an admin.
        if (stationRepository.count() > 0) {                   // Demo data already there?
            log.info("DataSeeder: sample data already present, skipping.");
            return;                                            // Stop; do not duplicate.
        }
        seedUsers();                                           // Demo staff and passengers.
        seedStations();
        seedTrains();
        seedRoutes();
        seedSchedules();
        seedFares();
        seedCards(...);
        seedTickets(...);
        seedPayments();
        seedMaintenance(...);
    }

    private void ensureAdminExists() {
        if (userRepository.findByContact(adminContact).isPresent()) {
            return;                                            // Admin already exists.
        }
        User admin = new User();
        admin.setUserId("ADMIN-" + UUID.randomUUID());         // Admin ID prefix.
        admin.setFirstName(adminFirstName);
        admin.setLastName(adminLastName);
        admin.setContact(adminContact);
        admin.setPassword(passwordEncoder.encode(adminPassword)); // Hash the bootstrap password.
        admin.setRole(Role.ADMIN);                             // Full rights.
        admin.setRegistrationDate(LocalDate.now());
        admin.setIsActive(true);
        userRepository.save(admin);                            // INSERT.
    }
}
```

Seeded users and their contacts:

| ID | Name | Role | Contact | Password |
|---|---|---|---|---|
| `ADMIN-<uuid>` | System Administrator | `ADMIN` | `admin` | `admin123` |
| `STAFF-01` | Rahul Mehta | `TICKET_AGENT` | `agent1` | `Passw0rd!` |
| `STAFF-02` | Suresh Kumar | `MAINTENANCE_STAFF` | `maint1` | `Passw0rd!` |
| `PASS-SEED1` | Priya Sharma | `PASSENGER` | `priya` | `Passw0rd!` |
| `PASS-SEED2` | Arjun Rao | `PASSENGER` | `arjun` | `Passw0rd!` |
| `PASS-SEED3` | Meera Singh | `PASSENGER` | `meera` | `Passw0rd!` |

Notice that seeded IDs such as `STAFF-01` and `PASS-SEED1` do **not** follow the prefix convention exactly. That proves the prefix is a convention chosen in code, not a database rule.

`ensureAdminExists` runs **before** the "stations already exist" check, so the admin is recreated on every restart where it is missing, even if the rest of the demo data is present.

### 8.7 `application.properties` — the settings that make auth work

```properties
spring.application.name=ticketing

# Database (Supabase by default)
spring.datasource.url=${DB_URL:jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require}
spring.datasource.username=${DB_USERNAME:postgres.mtpdfkyjrntiowofekwd}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=true

# Bootstrap admin
app.bootstrap.admin.contact=${ADMIN_CONTACT:admin}
app.bootstrap.admin.password=${ADMIN_PASSWORD:admin123}
app.bootstrap.admin.first-name=${ADMIN_FIRST_NAME:System}
app.bootstrap.admin.last-name=${ADMIN_LAST_NAME:Administrator}

# Use exact quoted names from @Table / @Column
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
springdoc.swagger-ui.persist-authorization=true
```

| Property | Meaning |
|---|---|
| `spring.datasource.url` | Where PostgreSQL lives. `DB_URL` overrides it. |
| `spring.datasource.username` | Database user. |
| `spring.datasource.password` | **No default.** `DB_PASSWORD` must be set or startup fails. |
| `ddl-auto=validate` | Hibernate checks tables but never creates them. Flyway owns the schema. |
| `show-sql=true` | Prints every SQL statement to the console. Great for demos. |
| `format_sql=true` | Pretty-prints that SQL over several lines. |
| `open-in-view=true` | Keeps the session open while JSON is written, so lazy `station` works. |
| `app.bootstrap.admin.*` | Values used by `DataSeeder` for the first admin. |
| `physical-strategy=...StandardImpl` | Uses `"FirstName"` exactly instead of converting to `first_name`. |
| `persist-authorization=true` | Swagger remembers the **Authorize** credentials after a page refresh. |

Without the naming strategy, Hibernate would look for `first_name` while the table has `"FirstName"`, and validation would fail.

### 8.8 `TicketingApplication` — how it all starts

```java
@SpringBootApplication                    // @Configuration + @EnableAutoConfiguration + @ComponentScan.
public class TicketingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketingApplication.class, args); // Boot everything.
    }
}
```

The package is `com.metro.ticketing`, so the scan finds `config`, `controller`, `repository`, `entity`, and `exception` automatically. That is why `SecurityConfig`, `DbUserDetailsService`, and `DataSeeder` need no manual registration.

---

## 9. One request from start to finish

Example: Priya opens her profile.

**Request**

```http
GET http://localhost:8080/api/auth/me
Authorization: Basic cHJpeWE6UGFzc3cwcmQh
```

`cHJpeWE6UGFzc3cwcmQh` is Base64 for `priya:Passw0rd!`.

**Story**

1. Tomcat receives the request on port 8080.
2. The Spring Security filter chain runs before `DispatcherServlet`.
3. `BasicAuthenticationFilter` sees the `Authorization` header.
4. It Base64-decodes the value into `priya` and `Passw0rd!`.
5. It creates a `UsernamePasswordAuthenticationToken` and sends it to the `AuthenticationManager`.
6. The manager uses `DaoAuthenticationProvider`.
7. The provider calls `DbUserDetailsService.loadUserByUsername("priya")`.
8. `userRepository.findByContact("priya")` runs:
   ```sql
   SELECT ... FROM "User" WHERE "Contact" = 'priya';
   ```
9. The row is found: ID `PASS-SEED1`, hash `$2a$10$...`, role `PASSENGER`, `isActive = true`.
10. The provider checks `isActive`. It is true, so the account is enabled.
11. `BCryptPasswordEncoder.matches("Passw0rd!", storedHash)` returns true.
12. The provider returns an authenticated token with authority `ROLE_PASSENGER`.
13. `SecurityContextHolder` stores the token for this request only.
14. The URL rule for `/api/auth/me` is `anyRequest().authenticated()`. Priya is authenticated, so it passes.
15. No `@PreAuthorize` on `/me`, so there is no role restriction.
16. `DispatcherServlet` routes to `AuthController.getCurrentUser`.
17. Spring injects the `Authentication` object; `getName()` returns `priya`.
18. `findByContact("priya")` loads the row again inside a read-only transaction.
19. Jackson serializes the user. `password` and `stationId` are hidden; `station` is null.
20. The response is `200 OK`:
    ```json
    {
      "userId": "PASS-SEED1",
      "firstName": "Priya",
      "lastName": "Sharma",
      "role": "PASSENGER",
      "contact": "priya",
      "registrationDate": "2026-04-29",
      "isActive": true,
      "hireDate": null,
      "station": null
    }
    ```
21. The request ends and the `SecurityContext` is cleared. Nothing is remembered.

If the password were wrong, step 11 would fail with `401`. If Priya called `/api/users`, step 14's rule would fail with `403`.

---

## 10. JSON examples

### 10.1 Register a passenger (public)

```http
POST /api/auth/passenger/register
Content-Type: application/json
```

```json
{
  "firstName": "Asha",
  "lastName": "Nair",
  "contact": "asha",
  "password": "secret123"
}
```

`200 OK`:

```json
{
  "userId": "PASS-6f1c2a90-6d6e-4a1e-9b6f-2f0f0f0f0f0f",
  "firstName": "Asha",
  "lastName": "Nair",
  "role": "PASSENGER",
  "contact": "asha",
  "registrationDate": "2026-10-29",
  "isActive": true,
  "hireDate": null,
  "station": null
}
```

Field notes:

| Field | Meaning |
|---|---|
| `firstName`, `lastName` | Required. |
| `contact` | Becomes the login username. Must be unique. |
| `password` | Hashed before saving. Never returned. |
| `role` | Ignored in the request; always `PASSENGER` in the response. |
| `userId` | Generated with the `PASS-` prefix. |
| `isActive` | Set to `true` by the controller. |

### 10.2 Hire an employee (admin only)

```http
POST /api/auth/employee/hire
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json
```

```json
{
  "firstName": "Rahul",
  "lastName": "Mehta",
  "contact": "agent9",
  "password": "Passw0rd!",
  "role": "TICKET_AGENT",
  "stationId": "STN-AND"
}
```

`200 OK`:

```json
{
  "userId": "EMP-0b7d1f2e-3c4d-4e5f-8a9b-1c2d3e4f5a6b",
  "firstName": "Rahul",
  "lastName": "Mehta",
  "role": "TICKET_AGENT",
  "contact": "agent9",
  "registrationDate": "2026-10-29",
  "isActive": true,
  "hireDate": "2026-10-29",
  "station": {
    "stationId": "STN-AND",
    "stationCode": "AND",
    "latitude": 19.11,
    "longitude": 72.8637,
    "address": "Andheri - Western Express Highway, Andheri East",
    "openedDate": "2012-11-29",
    "lineColor": "Blue",
    "isActive": true
  }
}
```

`stationId` is read from the request, converted into a `Station`, and stored. The response shows the object, not the string.

### 10.3 Bad login (401)

No body is sent by the client, or the password is wrong:

```http
GET /api/auth/me
Authorization: Basic cHJpeWE6d3Jvbmc=
```

Response:

```http
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Basic realm="Realm"
Content-Type: application/json
```

```json
{
  "timestamp": "2026-10-29T10:15:30.123+00:00",
  "status": 401,
  "error": "Unauthorized",
  "path": "/api/auth/me"
}
```

This body comes from Spring Boot's `/error` handling, not from `GlobalExceptionHandler`.

### 10.4 Wrong role (403)

Priya is logged in but calls an admin URL:

```http
GET /api/users
Authorization: Basic cHJpeWE6UGFzc3cwcmQh
```

Response:

```json
{
  "error": "You do not have permission to access this resource"
}
```

If the URL-level rule rejects the request, the body may instead be the standard Spring error JSON with `"status": 403`. Either way, the status is 403.

### 10.5 Duplicate registration (400)

```json
{
  "error": "User with this contact already exists."
}
```

### 10.6 Missing field (400)

```json
{
  "password": "Password is required"
}
```

### 10.7 Hire without a station (400)

```json
{
  "error": "Station is required."
}
```

### 10.8 Hire with an unknown station (400)

```json
{
  "error": "Station not found."
}
```

---

## 11. Error table

| Situation | Status | Message / header |
|---|---|---|
| No `Authorization` header | 401 | `WWW-Authenticate: Basic realm="Realm"` + Spring error JSON |
| Malformed Basic header | 401 | Same as above |
| Unknown contact | 401 | Same as above (Spring hides "user not found") |
| Wrong password | 401 | Same as above |
| `isActive = false` | 401 | Same as above (account disabled) |
| Logged in but not admin | 403 | `{"error":"You do not have permission to access this resource"}` or Spring error JSON |
| Register with a used contact | 400 | `{"error":"User with this contact already exists."}` |
| Register without a name/password/contact | 400 | `{"firstName":"First name is required"}` etc. |
| Hire without role | 400 | `{"error":"Role is required."}` |
| Hire without station | 400 | `{"error":"Station is required."}` |
| Hire with a station that does not exist | 400 | `{"error":"Station not found."}` |
| `/me` for a deleted user | 401 (or 400 in a rare race) | Login fails first; if the row disappears after login, `{"error":"Current user not found"}` |
| Missing `DB_PASSWORD` | App fails to start | `Could not resolve placeholder 'DB_PASSWORD'` |
| Invalid role in JSON | 500 | Jackson fails before the controller, and the catch-all handler reports a generic error |
| Any unexpected exception | 500 | `{"error":"An unexpected error occurred: ..."}` |

### 11.1 Why 401 and 403 are different

- **401** = the server could not authenticate you. The fix is to log in with correct credentials.
- **403** = the server authenticated you but you lack permission. The fix is to use an account with the right role.

A common beginner mistake is to return 403 for a bad password. That tells the client "you are logged in but not allowed", which is false.

Why does malformed JSON usually become 500 instead of 400? `GlobalExceptionHandler` has a catch-all `@ExceptionHandler(Exception.class)`. Jackson's parse error is an exception, so the catch-all often handles it before Spring's default "bad request" handling. It is a small design trade-off: one handler for everything is simple, but it can turn client mistakes into 500s. Adding `@ExceptionHandler(HttpMessageNotReadableException.class)` would make the behaviour explicit.

### 11.2 Why a disabled account gets 401, not 403

Spring Security treats a disabled account as a failed authentication. `DaoAuthenticationProvider` throws `DisabledException`, and the filter reports `401`. This is intentional: a disabled user should not learn that their password was correct.

---

## 12. Relationships to other groups

```text
                    +----------------+
                    | SecurityConfig |  (the guard)
                    +-------+--------+
                            | uses
                    +-------v--------+        reads        +-------------+
                    | DbUserDetails  |------------------->| User table  |
                    |    Service     |                    | (01)        |
                    +-------+--------+                    +------+------+
                            |                                    |
                    +-------v--------+                    +------v------+
                    |  AuthController |------------------->| Role enum   |
                    | /api/auth/*     |                    +-------------+
                    +----------------+
```

| Group | Relationship |
|---|---|
| 00 Overall | Auth is the first layer every request passes through, before any controller. |
| 01 User | Login reads `Contact`, `Password`, `Role`, `isActive`; the admin CRUD changes them. |
| 03 Station | Hiring requires a station ID, so station rows must exist first. |
| 04–08 Train/Route/RouteStation/Schedule/Fare | Their admin write endpoints rely on `ROLE_ADMIN`. |
| 09 Ticket | Ticket endpoints use the logged-in contact to find the passenger and check ownership. |
| 10 Payment | Payments check ownership through the ticket's passenger. |
| 11 MetroCard | Cards check ownership through the logged-in passenger. |
| 12 MaintenanceIssue | Agents and maintenance staff use their roles to report and resolve issues. |

The practical rule: **authentication happens once per request, and every controller can then ask "who is calling?"** through the `Authentication` parameter.

---

## 13. How to test in Swagger

1. Set `DB_PASSWORD` in the terminal.
2. Start the app with `.\mvnw.cmd spring-boot:run`.
3. Wait for `Tomcat started on port 8080`.
4. Open `http://localhost:8080/swagger-ui/index.html`.
5. Click **Authorize**.
6. Enter `admin` / `admin123` and click **Authorize**.
7. **Close** the dialog. The padlock icons now look locked.
8. Test `GET /api/auth/me`. Expect the System Administrator profile with `"role":"ADMIN"`.
9. Test `POST /api/auth/employee/hire` with:
   ```json
   {
     "firstName": "Rahul",
     "lastName": "Mehta",
     "contact": "agent9",
     "password": "Passw0rd!",
     "role": "TICKET_AGENT",
     "stationId": "STN-AND"
   }
   ```
10. Expect `200 OK` and an `EMP-` ID.
11. Call the same endpoint again. Expect `400` and "contact already exists".
12. Click **Authorize** → **Logout**.
13. Authorize as `priya` / `Passw0rd!`.
14. Call `GET /api/auth/me`. Expect `PASSENGER` and `PASS-SEED1`.
15. Call `POST /api/auth/employee/hire`. Expect `403`.
16. Call `GET /api/users`. Expect `403`.
17. **Logout**, then call `POST /api/auth/passenger/register` with no login:
    ```json
    {
      "firstName": "Asha",
      "lastName": "Nair",
      "contact": "asha",
      "password": "secret123"
    }
    ```
18. Expect `200` and a `PASS-` ID. Note there is no padlock on this operation.
19. Authorize as `asha` / `secret123`. Call `/api/auth/me`. It works immediately.
20. To see a `401`, click **Logout** and call `/api/auth/me` without authorizing.

`curl` versions:

```bash
# Who am I?
curl -u priya:Passw0rd! http://localhost:8080/api/auth/me

# Register (no credentials)
curl -X POST http://localhost:8080/api/auth/passenger/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Asha","lastName":"Nair","contact":"asha","password":"secret123"}'

# Hire (admin only)
curl -u admin:admin123 -X POST http://localhost:8080/api/auth/employee/hire \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Rahul","lastName":"Mehta","contact":"agent9","password":"Passw0rd!","role":"TICKET_AGENT","stationId":"STN-AND"}'

# Bad password -> 401
curl -i -u priya:wrong http://localhost:8080/api/auth/me

# Passenger tries an admin URL -> 403
curl -i -u priya:Passw0rd! http://localhost:8080/api/users
```

Tip: browsers cache Basic credentials. Use an incognito window or close the browser when switching users.

---

## 14. How to explain this in 2 minutes

Say this:

> Authentication in this project uses HTTP Basic. That means every request carries an `Authorization` header containing the username and password encoded in Base64. Base64 is not encryption; it is only encoding. In production this must run over HTTPS.
>
> The username is the `Contact` column of our `User` table, and the password is stored as a BCrypt hash. BCrypt is one-way and salted, so the database never contains the real password. When a request arrives, Spring Security decodes the header, asks `DbUserDetailsService` to load the user by contact, and then BCrypt compares the typed password with the stored hash.
>
> `DbUserDetailsService` converts our `User` entity into Spring's `UserDetails`. It sets the username, the password hash, the authorities using `.roles(role.name())`, and the disabled flag from `isActive`. The `.roles()` method adds the `ROLE_` prefix, so the authority is `ROLE_ADMIN`, `ROLE_PASSENGER`, and so on.
>
> `SecurityConfig` is the rulebook. It disables CSRF because the API is stateless, permits the registration endpoint, the Swagger files, and `/error`, requires `ADMIN` for `/api/users/**` and `/api/auth/employee/hire`, and requires any login for everything else. It enables HTTP Basic, sets session creation to `STATELESS`, and plugs in a `DaoAuthenticationProvider` that uses our database service and a `BCryptPasswordEncoder`.
>
> Stateless means no session and no cookie. Credentials are checked on every request, and the `SecurityContext` is cleared afterwards.
>
> There are four roles. Admins manage everything, ticket agents sell tickets and top up cards, maintenance staff handle faults, and passengers book tickets and own cards. Authorization is enforced twice: once by URL rules in `SecurityConfig`, and again by `@PreAuthorize` on controller methods. Duplication is deliberate.
>
> `AuthController` exposes three endpoints. Registration is public and always creates a `PASSENGER`, ignoring any role in the request. Hiring is admin-only, requires a role and a station, and creates an `EMP-` ID. `/api/auth/me` returns the logged-in user's profile by looking up `Authentication.getName()`, which is the contact.
>
> Finally, `DataSeeder` creates the first admin from `application.properties` on startup, using `admin` / `admin123` by default, plus demo accounts for each role. Flyway creates the schema before Hibernate validates it, which is why the app can start on an empty database.

---

## 15. Beginner questions and answers

**Q1. What is the difference between authentication and authorization?**
Authentication proves who you are. Authorization decides what you may do. Login is authentication; `@PreAuthorize` is authorization.

**Q2. Why does every request send the password again?**
Because the API is stateless. There is no session or token to reuse. This is simple but requires HTTPS.

**Q3. Is Base64 secure?**
No. Anyone can decode it. It only hides credentials from casual reading. TLS/HTTPS is what actually protects them.

**Q4. Why is the username `Contact` and not the email or the user ID?**
`Contact` is the value the user registers with and the column with a unique index. `UserId` is an internal database key, which is a poor login name.

**Q5. What does BCrypt do?**
It salts and hashes the password many times, producing a 60-character string such as `$2a$10$...`. You cannot reverse it; you can only compare.

**Q6. Where is the salt stored?**
Inside the hash string itself. BCrypt reads it back when checking a password, so no separate column is needed.

**Q7. What is the `10` in `$2a$10$...`?**
The cost factor (2¹⁰ rounds). Higher is slower and harder to brute-force.

**Q8. Why does the app sometimes return 401 with a different JSON shape?**
401 and 403 are produced by Spring Security's filters, before `GlobalExceptionHandler` runs. They use Spring Boot's standard error page. Application errors use `{"error": "..."}`.

**Q9. Why is `@PreAuthorize` needed if `SecurityConfig` already checks the URL?**
Defence in depth. The URL rule is central; the annotation is local and survives refactoring. Both must pass.

**Q10. Why does `hasRole('ADMIN')` work when the role is stored as `ADMIN`?**
Spring adds the `ROLE_` prefix on both sides. `hasRole('ADMIN')` checks for `ROLE_ADMIN`, and `.roles("ADMIN")` creates `ROLE_ADMIN`.

**Q11. What happens if I use `hasAuthority('ADMIN')`?**
It fails, because the actual authority is `ROLE_ADMIN`. Use `hasAuthority('ROLE_ADMIN')` or `hasRole('ADMIN')`.

**Q12. Why is CSRF disabled?**
CSRF attacks rely on cookies being sent automatically. This API has no session cookie, so the attack does not apply.

**Q13. What does `STATELESS` change?**
Spring never creates or reads an `HttpSession`. Every request is authenticated from scratch.

**Q14. How do I log out?**
There is nothing to log out on the server. Stop sending the header, or clear it in Swagger/browser. That is a consequence of being stateless.

**Q15. Why does a disabled account get 401 and not 403?**
Spring treats a disabled account as a failed authentication. The account never becomes "logged in", so there is no identity to forbid.

**Q16. Can registration create an admin?**
No. `registerPassenger` forces `Role.PASSENGER`. Even if the JSON contains `"role":"ADMIN"`, it is overwritten.

**Q17. Can hiring create a passenger?**
Technically yes. `hireEmployee` copies whatever role is sent. It only requires that a role is present. Use `POST /api/users` for precise admin control.

**Q18. Why is the hire endpoint's ID always `EMP-` even for an admin?**
The prefix is hard-coded in that method. It is a naming convention, not a security control.

**Q19. What is the bootstrap admin, and when is it created?**
It is the first `ADMIN` account, created by `DataSeeder.ensureAdminExists()` on startup if no user with that contact exists. Defaults are `admin` / `admin123`.

**Q20. What if I change `ADMIN_PASSWORD` after the admin already exists?**
Nothing happens. The seeder only creates the admin when it is missing. To change the password, update the row through the API or delete the admin and restart.

**Q21. Why is `ddl-auto=validate` instead of `update`?**
So the schema is controlled only by Flyway. `update` can silently change tables and cause drift between environments.

**Q22. Why does the app fail to start without `DB_PASSWORD`?**
`spring.datasource.password=${DB_PASSWORD}` has no default value. Spring cannot resolve the placeholder, so startup stops.

**Q23. What is the `basicAuth` scheme in Swagger?**
It tells Swagger UI to show an **Authorize** button that sends the Basic header. It is documentation plus a testing convenience, not a second security layer.

**Q24. Why is the registration endpoint unlocked in Swagger in two different ways?**
`@SecurityRequirements` on the method and the `OperationCustomizer` bean both remove the padlock. They are redundant but harmless.

---

## 16. Glossary for this file

| Term | Meaning |
|---|---|
| `401 Unauthorized` | Authentication failed or missing. |
| `403 Forbidden` | Authenticated but not permitted. |
| `@Bean` | Method whose return value Spring manages. |
| `@Configuration` | Class that defines beans. |
| `@EnableMethodSecurity` | Enables `@PreAuthorize`. |
| `@EnableWebSecurity` | Enables Spring Security's web filter chain. |
| `@PreAuthorize` | Method-level role check. |
| `@SecurityRequirement` | OpenAPI metadata adding a padlock. |
| `@Value` | Injects a property or environment variable. |
| `ApplicationRunner` | Runs code once after startup. |
| Authentication | Proving identity. |
| `AuthenticationProvider` | Verifies credentials. |
| Authority | Technical permission string such as `ROLE_ADMIN`. |
| Authorization | Checking permission. |
| Base64 | Reversible text encoding used by Basic Auth. |
| Baseline | Flyway's starting version for an existing database. |
| BCrypt | Slow, salted, one-way password hash. |
| Bootstrap admin | The first admin created automatically. |
| `DaoAuthenticationProvider` | Provider that loads users from a database. |
| `DbUserDetailsService` | Our implementation of `UserDetailsService`. |
| Dependency injection | Spring supplies constructor arguments. |
| Filter chain | Ordered security checks before the controller. |
| Flyway | Runs numbered SQL migrations. |
| Hash | One-way fingerprint of data. |
| HTTP Basic | Credentials in an `Authorization` header on every request. |
| Migration | Versioned SQL change. |
| `PasswordEncoder` | Interface for hashing and matching passwords. |
| `permitAll` | URL open to everyone. |
| Principal | The logged-in identity. |
| Role | Business label such as `ADMIN`. |
| Salt | Random data mixed into a hash. |
| `SecurityContext` | Per-request storage of the principal. |
| `SecurityFilterChain` | Bean that configures web security. |
| Session | Server-side login memory; not used here. |
| Stateless | No session; credentials checked every time. |
| Swagger UI | Generated page for testing the API. |
| `UserDetails` | Spring's standard view of a user. |
| `UserDetailsService` | Interface with `loadUserByUsername`. |
| UUID | Long random identifier. |
| `WWW-Authenticate` | Header asking the client for Basic credentials. |

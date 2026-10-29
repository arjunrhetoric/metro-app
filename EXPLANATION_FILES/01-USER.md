# 01 — The User Group

This file explains the **User** part of the project: the `User` entity, the `UserRepository`, and the new `UserController` with full CRUD at `/api/users`.

Related files: `02-AUTH-AND-ROLE.md` explains how a user logs in. This file explains what a user **is** and how an admin manages users.

---

## 1. A real-life analogy: the college admission office

Imagine your college admission office.

- Every student and every staff member has **one file** in the office.
- The file has a fixed form: full name, roll number, department, phone number, date of joining, and whether the person is currently active.
- The **roll number** is unique. No two files can share it.
- The **phone number** is also unique, because it is used to identify you at the gate.
- A clerk at the window can **create** a file, **show** one file, **list** all files, **update** a file, or **delete** a file.
- Only the **head of the office** (the admin) is allowed to do this. Students cannot walk behind the counter and edit files.

In this project:

| College office | Project |
|---|---|
| One admission file | One row in the `User` table |
| The printed form | `User.java` (the entity) |
| The filing cabinet | PostgreSQL database |
| The clerk who fetches files | `UserRepository` |
| The window where you ask | `UserController` (`/api/users`) |
| The head of the office | A user with the `ADMIN` role |
| The unique roll number | `UserId` (primary key) |
| The unique phone number | `Contact` (unique index) |

A "user" here is **any person who can log in**: a passenger, a ticket agent, a maintenance worker, or an admin.

---

## 2. Words you need

| Word | Simple meaning |
|---|---|
| Entity | A Java class that represents one database table. |
| Table | A grid of rows and columns in the database. |
| Row | One record, for example one user. |
| Column | One field of a record, for example `FirstName`. |
| Primary key | The column that uniquely identifies a row; here `UserId`. |
| Foreign key | A column that points to a row in another table; here `StationID`. |
| Unique index | A database rule that stops duplicate values; here on `Contact`. |
| `@Entity` | Annotation that says "this class is a table". |
| `@Table` | Annotation that gives the exact table name. |
| `@Column` | Annotation that gives the exact column name, length, and nullability. |
| `@Id` | Annotation that marks the primary key. |
| `@ManyToOne` | Many users can point to one station. |
| `@JoinColumn` | Names the foreign key column for a relationship. |
| `@Transient` | "Do not save this field in the database." |
| `@JsonProperty` | Controls the JSON name and whether a field is readable/writable. |
| `READ_ONLY` | Show in responses, ignore in requests. |
| `WRITE_ONLY` | Accept in requests, hide from responses. |
| `@NotBlank` | Validation: the text must not be null, empty, or only spaces. |
| `@Valid` | "Run the validation annotations on this object before the method body." |
| `@PrePersist` | A callback that runs just before the first `INSERT`. |
| `@JsonIgnoreProperties` | Tells Jackson to skip named internal fields. |
| `LocalDate` | A date without time, for example `2026-10-29`. |
| `UUID` | A long random ID, for example `6f1c2a90-6d6e-...`. |
| Repository | An interface that reads/writes the database. |
| `JpaRepository` | A Spring interface with ready-made CRUD methods. |
| Derived query | A repository method whose SQL is generated from its name. |
| `Optional<T>` | A box that may contain a value or be empty. |
| Controller | The class that handles HTTP requests. |
| `@RestController` | Marks a controller that returns JSON. |
| `@RequestMapping` | Sets the URL prefix for all methods in a controller. |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` | Shortcuts for GET/POST/PUT/DELETE routes. |
| `@RequestBody` | "Build this parameter from the JSON body." |
| `@PathVariable` | "Take this value from the URL path." |
| `@PreAuthorize` | "Only allow this role to call this method." |
| `@Transactional` | Run the method in one database transaction. |
| `ResponseEntity` | A response wrapper that lets you choose the status code and body. |
| HTTP status | A number such as 200 OK, 201 Created, 204 No Content, 400 Bad Request, 404 Not Found. |
| JSON | Text format for requests and responses. |
| Serialization | Java object → JSON. |
| Deserialization | JSON → Java object. |
| Dependency injection | Spring passes the repository into the controller constructor. |
| BCrypt | One-way password hashing. |
| DTO | A class only for data transfer; this project does not use them. |
| `@Service` | Marks a class as a Spring service bean. |
| `@Override` | Tells the compiler that a method overrides an interface or parent method. |
| `IllegalArgumentException` | Java's "bad argument" exception; this project maps it to 400. |
| `HttpMessageNotReadableException` | Jackson's "cannot read this JSON" exception. |
| Logger | An SLF4J object used to write log lines. |
| CRUD | Create, Read, Update, Delete. |
| Partial update | Updating only the fields that were sent. |

---

## 3. Files in this group

Project root: `C:\Users\arjun\Desktop\metro-ticketing-app`

| Role | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/User.java` |
| Role enum (used by `User`) | `src/main/java/com/metro/ticketing/entity/Role.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/UserRepository.java` |
| Controller | `src/main/java/com/metro/ticketing/controller/UserController.java` |
| Auth controller (creates users too) | `src/main/java/com/metro/ticketing/controller/AuthController.java` |
| Related entity | `src/main/java/com/metro/ticketing/entity/Station.java` |
| Related repository | `src/main/java/com/metro/ticketing/repository/StationRepository.java` |
| Error classes | `src/main/java/com/metro/ticketing/exception/ResourceNotFoundException.java`, `.../GlobalExceptionHandler.java` |
| Table SQL | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Seeder (demo users) | `src/main/java/com/metro/ticketing/config/DataSeeder.java` |

---

## 4. The database table

### 4.1 SQL

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
    "StationID" VARCHAR(50),
    CONSTRAINT "FK_User_Station" FOREIGN KEY ("StationID")
        REFERENCES "Stations" ("StationID")
);

CREATE UNIQUE INDEX IF NOT EXISTS "UX_User_Contact" ON "User" ("Contact");
```

### 4.2 Column by column

| Column | Type | Null? | What it means |
|---|---|---|---|
| `UserId` | `VARCHAR(50)` | No | Primary key. Examples: `PASS-6f1c...`, `EMP-0b7d...`, `ADMIN-...`, `PASS-SEED1`. |
| `FirstName` | `VARCHAR(100)` | No | Given name. |
| `LastName` | `VARCHAR(100)` | No | Family name. |
| `Password` | `VARCHAR(255)` | No | BCrypt hash, about 60 characters. Never plain text. |
| `Role` | `VARCHAR(50)` | No | `ADMIN`, `TICKET_AGENT`, `MAINTENANCE_STAFF`, or `PASSENGER`, stored as text. |
| `Contact` | `VARCHAR(50)` | Yes | The login username. Unique because of `UX_User_Contact`. |
| `RegisterationDate` | `DATE` | Yes | Account creation date. The spelling mistake exists in the schema and in the entity, so they match. |
| `isActive` | `BOOLEAN` | Yes (default `TRUE`) | `false` means the account cannot log in. |
| `HireDate` | `DATE` | Yes | Joining date for staff; `NULL` for passengers. |
| `StationID` | `VARCHAR(50)` | Yes | Foreign key to `Stations`. Staff may be posted to one station. |

### 4.3 Why the table name is quoted

`USER` is a reserved keyword in PostgreSQL. Writing `CREATE TABLE User` would confuse the parser. Double quotes force the exact name: `"User"`. Java needs the quotes escaped inside a string:

```java
@Table(name = "\"User\"")
```

The outer `"` starts the Java string, `\"` is a literal double quote, and the final `"` ends the string. The result is `"User"` with quotes.

### 4.4 Why `Contact` is unique

Two people with the same contact would break login, because login looks users up by contact:

```java
Optional<User> findByContact(String contact);
```

If two rows matched, the login code would not know which one to use. The unique index makes that impossible at the database level. The controllers also check first, so the user gets a friendly `400` instead of a database crash.

### 4.5 Why `Password` is 255 characters

A BCrypt hash is 60 characters, including the `$2a$10$` prefix and the salt. `VARCHAR(255)` leaves room for future algorithms. The plain password is never stored.

---

## 5. The entity, line by line

Full file: `src/main/java/com/metro/ticketing/entity/User.java`

```java
package com.metro.ticketing.entity;                              // This class belongs to the entity package.

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;     // Lets us skip internal Hibernate fields in JSON.
import com.fasterxml.jackson.annotation.JsonProperty;             // Lets us mark fields read-only or write-only.
import io.swagger.v3.oas.annotations.media.Schema;                // Extra information for Swagger UI.
import jakarta.persistence.Column;                                // Maps a field to a column.
import jakarta.persistence.Entity;                                // Marks the class as a table.
import jakarta.persistence.EnumType;                              // The two ways to store an enum.
import jakarta.persistence.Enumerated;                            // Chooses how the enum is stored.
import jakarta.persistence.FetchType;                             // Controls when related data is loaded.
import jakarta.persistence.Id;                                    // Marks the primary key.
import jakarta.persistence.JoinColumn;                            // Names the foreign key column.
import jakarta.persistence.ManyToOne;                             // Many users -> one station.
import jakarta.persistence.PrePersist;                            // Runs a method before the first INSERT.
import jakarta.persistence.Table;                                 // Names the table.
import jakarta.persistence.Transient;                             // "Not a column."
import jakarta.validation.constraints.NotBlank;                   // "Must not be blank" validation.

import java.time.LocalDate;                                       // Date without time.
import java.util.UUID;                                            // Random unique identifier.

@Entity                                                           // "This class is a database table."
@Table(name = "\"User\"")                                         // Table name is exactly "User" (quoted keyword).
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})    // Hide Hibernate's internal proxy fields.
public class User {                                               // One User object = one row.

    @Id                                                           // This is the primary key.
    @Column(name = "\"UserId\"", length = 50)                     // Column "UserId", max 50 characters.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)         // Show in responses, ignore in requests.
    @Schema(accessMode = Schema.AccessMode.READ_ONLY,
            description = "Auto-generated user ID")               // Swagger shows it greyed out.
    private String userId;                                        // Example: PASS-6f1c2a90-....

    @NotBlank(message = "First name is required")                 // Reject null, "", and "   ".
    @Column(name = "\"FirstName\"", length = 100, nullable = false) // Required column, 100 chars.
    private String firstName;                                     // Given name.

    @NotBlank(message = "Last name is required")                  // Same rule for the family name.
    @Column(name = "\"LastName\"", length = 100, nullable = false)
    private String lastName;                                      // Family name.

    @NotBlank(message = "Password is required")                   // Password must be supplied on create.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)        // Accept it, never return it.
    @Column(name = "\"Password\"", length = 255, nullable = false)
    private String password;                                      // BCrypt hash in the database.

    @Enumerated(EnumType.STRING)                                  // Store "ADMIN", not 0 or 1.
    @Column(name = "\"Role\"", length = 50, nullable = false)
    private Role role;                                            // ADMIN / TICKET_AGENT / MAINTENANCE_STAFF / PASSENGER.

    @NotBlank(message = "Contact is required")                    // Contact (login name) is mandatory.
    @Column(name = "\"Contact\"", length = 50)
    private String contact;                                       // Example: "priya", "agent1", "admin".

    @Column(name = "\"RegisterationDate\"")                       // Note the schema's typo.
    private LocalDate registrationDate = LocalDate.now();         // New object starts with today's date.

    @Column(name = "\"isActive\"")
    @JsonProperty("isActive")                                     // Force JSON name to "isActive".
    private Boolean isActive = true;                              // New users are active.

    @Column(name = "\"HireDate\"")
    private LocalDate hireDate;                                   // Null for passengers.

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)         // Client cannot set station by nesting an object.
    @ManyToOne(fetch = FetchType.LAZY)                            // Load the station only when read.
    @JoinColumn(name = "\"StationID\"", referencedColumnName = "\"StationID\"")
    private Station station;                                      // The station this staff member works at.

    @Transient                                                    // Not stored in the table.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)        // Accept "stationId" in requests, hide it in responses.
    private String stationId;                                     // Temporary holder used only while reading a request.

    public User() {                                               // No-argument constructor.
    }                                                             // Required by JPA and Jackson.

    public User(String userId, String firstName, String lastName, String password, Role role,
                String contact, LocalDate registrationDate, Boolean isActive,
                LocalDate hireDate, Station station) {            // All-arguments constructor.
        this.userId = userId;                                     // Copy each parameter into the field.
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.role = role;
        this.contact = contact;
        this.registrationDate = registrationDate;
        this.isActive = isActive;
        this.hireDate = hireDate;
        this.station = station;
    }                                                             // The controllers actually use the no-arg constructor.

    @PrePersist                                                   // Called by JPA just before INSERT.
    public void onCreate() {                                      // Safety net for values that must exist.
        if (this.userId == null && this.role != null) {           // Only if the caller did not set an ID.
            String prefix = switch (this.role) {                  // Java 21 switch expression.
                case PASSENGER -> "PASS";                         // Passenger IDs start with PASS-.
                case ADMIN -> "ADMIN";                            // Admin IDs start with ADMIN-.
                case TICKET_AGENT, MAINTENANCE_STAFF -> "EMP";    // Staff IDs start with EMP-.
            };                                                    // Every enum value is covered, so no default is needed.
            this.userId = prefix + "-" + UUID.randomUUID().toString(); // Example: EMP-0b7d1f2e-....
        }
        if (this.registrationDate == null) {                      // If somehow still null...
            this.registrationDate = LocalDate.now();              // ...use today.
        }
        if (this.isActive == null) {                              // If somehow still null...
            this.isActive = true;                                 // ...activate the account.
        }
    }

    public String getUserId() {                                   // Getter for userId.
        return userId;                                            // Jackson uses this when writing JSON.
    }

    public void setUserId(String userId) {                        // Setter for userId.
        this.userId = userId;                                     // Jackson would use this, but READ_ONLY blocks it.
    }

    public String getFirstName() {                                // Getter for firstName.
        return firstName;                                         // Returns the field value.
    }

    public void setFirstName(String firstName) {                  // Setter for firstName.
        this.firstName = firstName;                               // Stores the new value.
    }

    public String getLastName() {                                 // Getter for lastName.
        return lastName;                                          // Returns the family name.
    }

    public void setLastName(String lastName) {                    // Setter for lastName.
        this.lastName = lastName;                                 // Stores the family name.
    }

    public String getPassword() {                                 // Getter for password.
        return password;                                          // Spring Security reads this hash at login.
    }

    public void setPassword(String password) {                    // Setter for password.
        this.password = password;                                 // Controllers pass an already-hashed value.
    }

    public Role getRole() {                                       // Getter for role.
        return role;                                              // Used for JSON and for login authorities.
    }

    public void setRole(Role role) {                              // Setter for role.
        this.role = role;                                         // Stores the enum value.
    }

    public String getContact() {                                  // Getter for contact.
        return contact;                                           // Used as the login username.
    }

    public void setContact(String contact) {                      // Setter for contact.
        this.contact = contact;                                   // Stores the login name.
    }

    public LocalDate getRegistrationDate() {                      // Getter for registrationDate.
        return registrationDate;                                  // Jackson prints it as "2026-10-29".
    }

    public void setRegistrationDate(LocalDate registrationDate) { // Setter for registrationDate.
        this.registrationDate = registrationDate;                 // Stores the date.
    }

    public Boolean getIsActive() {                                // Getter for isActive.
        return isActive;                                          // Used by the login "disabled" check.
    }

    public void setIsActive(Boolean isActive) {                   // Setter for isActive.
        this.isActive = isActive;                                 // false blocks login.
    }

    public LocalDate getHireDate() {                              // Getter for hireDate.
        return hireDate;                                          // Null for passengers.
    }

    public void setHireDate(LocalDate hireDate) {                 // Setter for hireDate.
        this.hireDate = hireDate;                                 // Stores the joining date.
    }

    public Station getStation() {                                 // Getter for station.
        return station;                                           // Jackson prints the whole station object.
    }

    public void setStation(Station station) {                     // Setter for station.
        this.station = station;                                   // Stores the relationship.
    }

    public String getStationId() {                                // Getter for the transient helper.
        if (stationId != null) {                                  // If a value came in the request...
            return stationId;                                     // ...return it first.
        }
        if (station != null) {                                    // Otherwise, if a station is attached...
            return station.getStationId();                        // ...return its ID.
        }
        return null;                                              // Otherwise, no station.
    }

    public void setStationId(String stationId) {                  // Setter for the transient helper.
        this.stationId = stationId;                               // Only lives in memory during the request.
    }
}
```

### 5.1 Annotation summary

| Annotation | Where | Why it matters |
|---|---|---|
| `@Entity` | class | Hibernate treats the class as a table mapping. |
| `@Table` | class | Exact table name `"User"`. |
| `@JsonIgnoreProperties` | class | Stops Hibernate proxy fields from appearing in JSON. |
| `@Id` | `userId` | Primary key. |
| `@Column` | most fields | Exact column name, length, and `NOT NULL`. |
| `@JsonProperty(READ_ONLY)` | `userId`, `station` | Client cannot set them. |
| `@JsonProperty(WRITE_ONLY)` | `password`, `stationId` | Client can send them, responses never show them. |
| `@Schema` | `userId` | Swagger documentation only; no runtime effect. |
| `@NotBlank` | firstName, lastName, password, contact | Input validation. |
| `@Enumerated(STRING)` | `role` | Stores the enum name as text. |
| `@ManyToOne(LAZY)` | `station` | Many users → one station; load on demand. |
| `@JoinColumn` | `station` | Foreign key column `StationID`. |
| `@Transient` | `stationId` | Not a column. |
| `@PrePersist` | `onCreate` | Fill defaults before insert. |

### 5.2 The two "extra" fields: `station` and `stationId`

This is the trickiest part of the class, so read it twice.

- The **database** stores only `StationID`.
- `station` is the real JPA relationship. It holds a full `Station` object. Jackson prints it in responses, but `READ_ONLY` stops clients from setting it directly.
- `stationId` is a `@Transient` string. It exists only in memory. It lets the JSON request say `"stationId":"STN-AND"` in a simple way.
- The controller reads `request.getStationId()`, looks up the `Station` with `stationRepository.findById(...)`, and calls `user.setStation(station)`.
- The response shows `"station": {...}` and never shows `"stationId"`, because `stationId` is `WRITE_ONLY`.

Think of `stationId` as the **token number** you hand to the clerk, and `station` as the **actual file** the clerk fetches from the cabinet.

### 5.3 Why `Boolean` instead of `boolean`

`Boolean` can be `null`. That lets the code tell the difference between "the client did not send a value" and "the client sent `false`". The update method uses that difference: `null` means "leave it unchanged".

### 5.4 Why there are no `equals`, `hashCode`, or `toString`

The project does not compare users in sets or maps, and Jackson handles output. Adding them is good practice but not required here.

### 5.5 Why field access

`@Id` is on the field, not on a getter. Hibernate therefore reads and writes fields directly. The getters and setters exist for Jackson, for the controllers, and for readability.

---

## 6. The repository, line by line

Full file: `src/main/java/com/metro/ticketing/repository/UserRepository.java`

```java
package com.metro.ticketing.repository;                       // Repository package.

import com.metro.ticketing.entity.User;                       // The entity type.
import org.springframework.data.jpa.repository.JpaRepository; // Parent with ready-made CRUD methods.
import org.springframework.stereotype.Repository;             // Marks it as a Spring bean (optional but clear).

import java.util.Optional;                                    // May contain a User or be empty.

@Repository                                                   // Spring creates an implementation at startup.
public interface UserRepository extends JpaRepository<User, String> {
                                                              // <User, String>: entity is User, PK type is String.
    Optional<User> findByContact(String contact);             // SELECT ... WHERE "Contact" = ?
    boolean existsByContact(String contact);                  // Fast "does this contact exist?" check.
}
```

There is **no class** and **no SQL**. At startup Spring Data JPA:

1. Reads the interface name `UserRepository`.
2. Sees `extends JpaRepository<User, String>`.
3. Creates a hidden implementation class.
4. Registers it as a bean named `userRepository`.
5. Reads `findByContact` and builds a query from the method name.

### 6.1 The two declared methods

| Method | Generated SQL (simplified) | Returns | Used by |
|---|---|---|---|
| `findByContact(String contact)` | `SELECT * FROM "User" WHERE "Contact" = ?` | `Optional<User>` | register, hire, login, `/me`, ownership checks |
| `existsByContact(String contact)` | `SELECT count(*) > 0 FROM "User" WHERE "Contact" = ?` | `boolean` | declared but not called today |

Why `Optional`? Because the contact might not exist. `Optional` forces the caller to decide what to do instead of risking a `NullPointerException`.

Two common ways to unwrap it:

```java
// 1. Throw a custom exception when empty.
User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

// 2. Just check presence.
if (userRepository.findByContact(contact).isPresent()) { ... }
```

### 6.2 Methods inherited from `JpaRepository`

| Method | SQL | Notes |
|---|---|---|
| `save(User)` | `INSERT` or `UPDATE` | Spring treats a non-null ID as "maybe existing" and calls `merge`, which may `SELECT` before the `INSERT`. |
| `saveAll(List)` | Several INSERT/UPDATE | Not used here. |
| `findById(String)` | `SELECT ... WHERE "UserId" = ?` | Returns `Optional<User>`. |
| `findAll()` | `SELECT ... FROM "User"` | No guaranteed order unless you pass a `Sort`. |
| `count()` | `SELECT count(*)` | Used by the seeder. |
| `delete(User)` | `DELETE ... WHERE "UserId" = ?` | Used by `UserController`. |
| `deleteById(String)` | `DELETE ... WHERE "UserId" = ?` | Not used here. |
| `existsById(String)` | `SELECT count(*) > 0 ...` | Other controllers use it. |
| `flush()` | Forces pending SQL now | Not used directly. |

`delete(user)` first loads the entity, then deletes it. `UserController` does exactly that because it needs to return 404 when the row is missing.

### 6.3 What `save` returns

`save` returns the managed entity. After an INSERT, the returned object contains whatever the database generated. Here the ID is generated in Java, so the returned object is essentially the same object, now managed by the persistence context.

### 6.4 A note on `existsByContact`

It is unused but harmless. If you wanted to simplify the duplicate check, you could write:

```java
if (userRepository.existsByContact(request.getContact())) { ... }
```

That avoids loading the whole row when you only need a yes/no answer.

---

## 7. Controller endpoints

Base URL: `http://localhost:8080/api/users`

| Method | URL | Role | Purpose | Success |
|---|---|---|---|---|
| POST | `/api/users` | ADMIN | Create a user of any role | `201 Created` + user JSON |
| GET | `/api/users` | ADMIN | List every user | `200 OK` + array |
| GET | `/api/users/{id}` | ADMIN | Read one user | `200 OK` + user JSON |
| PUT | `/api/users/{id}` | ADMIN | Partly update one user | `200 OK` + updated user |
| DELETE | `/api/users/{id}` | ADMIN | Delete one user | `204 No Content` (empty body) |

Notes:

- The class also has `@SecurityRequirement(name = "basicAuth")`, which only affects Swagger's padlock icon.
- `SecurityConfig` already blocks `/api/users/**` for non-admins at the URL level. `@PreAuthorize` checks again inside each method. Two locks on the same door.
- The older `AuthController` creates users through `/api/auth/passenger/register` and `/api/auth/employee/hire`. Those return `200 OK`, while `POST /api/users` returns `201 Created`.

---

## 8. The controller, line by line

Full file: `src/main/java/com/metro/ticketing/controller/UserController.java`

### 8.1 Class declaration and dependencies

```java
package com.metro.ticketing.controller;                        // Controller package.

import com.metro.ticketing.entity.Role;                        // Needed for the ID prefix switch.
import com.metro.ticketing.entity.Station;                     // Needed to attach a station.
import com.metro.ticketing.entity.User;                        // Request and response type.
import com.metro.ticketing.exception.ResourceNotFoundException; // 404 exception.
import com.metro.ticketing.repository.StationRepository;       // Looks up stations.
import com.metro.ticketing.repository.UserRepository;          // Reads/writes users.
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // Swagger padlock.
import io.swagger.v3.oas.annotations.tags.Tag;                 // Swagger group name.
import jakarta.validation.Valid;                               // Enables @NotBlank checks.
import org.springframework.http.HttpStatus;                    // Constants such as CREATED.
import org.springframework.http.ResponseEntity;                // Response with status + body.
import org.springframework.security.access.prepost.PreAuthorize; // Role check per method.
import org.springframework.security.crypto.password.PasswordEncoder; // BCrypt encoder.
import org.springframework.transaction.annotation.Transactional; // Transaction boundary.
import org.springframework.web.bind.annotation.DeleteMapping;   // DELETE route.
import org.springframework.web.bind.annotation.GetMapping;      // GET route.
import org.springframework.web.bind.annotation.PathVariable;    // Value from the URL.
import org.springframework.web.bind.annotation.PostMapping;     // POST route.
import org.springframework.web.bind.annotation.PutMapping;      // PUT route.
import org.springframework.web.bind.annotation.RequestBody;     // JSON body.
import org.springframework.web.bind.annotation.RequestMapping;  // Common prefix.
import org.springframework.web.bind.annotation.RestController;  // JSON controller.

import java.time.LocalDate;                                    // Today's date.
import java.util.List;                                         // List of users.
import java.util.UUID;                                         // Random ID.

@RestController                                                // Every method returns JSON.
@RequestMapping("/api/users")                                  // All routes start with /api/users.
@Tag(name = "User Management", description = "APIs for User CRUD operations")
                                                               // Swagger section title and description.
@SecurityRequirement(name = "basicAuth")                       // Show a padlock on every method.
public class UserController {                                  // The class itself.

    private final UserRepository userRepository;               // Database access for users.
    private final StationRepository stationRepository;         // Database access for stations.
    private final PasswordEncoder passwordEncoder;             // Hashes passwords.

    public UserController(UserRepository userRepository,
                          StationRepository stationRepository,
                          PasswordEncoder passwordEncoder) {    // Constructor injection.
        this.userRepository = userRepository;                  // Spring passes the bean it created.
        this.stationRepository = stationRepository;
        this.passwordEncoder = passwordEncoder;
    }
```

`private final` means the references can never be reassigned after construction. That is a common Spring style: dependencies are fixed for the life of the controller.

### 8.2 `createUser` — POST `/api/users`

```java
@PostMapping                                                   // POST /api/users
@PreAuthorize("hasRole('ADMIN')")                              // Only ROLE_ADMIN may enter.
@Transactional                                                 // One transaction for the whole method.
public ResponseEntity<User> createUser(@Valid @RequestBody User request) {
                                                               // @RequestBody: JSON -> User.
                                                               // @Valid: run @NotBlank before the body.
    if (request.getContact() != null && userRepository.findByContact(request.getContact()).isPresent()) {
        throw new IllegalArgumentException("User with this contact already exists.");
    }                                                          // 400 from GlobalExceptionHandler.
    if (request.getRole() == null) {                           // Role has no @NotBlank, so check manually.
        throw new IllegalArgumentException("Role is required.");
    }
    if (request.getPassword() == null || request.getPassword().isBlank()) {
        throw new IllegalArgumentException("Password is required.");
    }                                                          // Belt-and-braces; @NotBlank usually fires first.

    User user = new User();                                    // Fresh entity, not the request object.
    String prefix = switch (request.getRole()) {               // Choose a readable prefix.
        case PASSENGER -> "PASS";
        case ADMIN -> "ADMIN";
        case TICKET_AGENT, MAINTENANCE_STAFF -> "EMP";
    };
    user.setUserId(prefix + "-" + UUID.randomUUID());          // Example: EMP-0b7d1f2e-....
    user.setFirstName(request.getFirstName());                 // Copy the first name.
    user.setLastName(request.getLastName());                   // Copy the last name.
    user.setContact(request.getContact());                     // Copy the login name.
    user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash, never store plain text.
    user.setRole(request.getRole());                           // Copy the enum.
    user.setRegistrationDate(request.getRegistrationDate() != null
            ? request.getRegistrationDate() : LocalDate.now()); // Use supplied date or today.
    if (request.getIsActive() != null) {                       // If the client sent a value...
        user.setIsActive(request.getIsActive());               // ...use it.
    } else {                                                   // Otherwise...
        user.setIsActive(true);                                // ...default to active.
    }
    user.setHireDate(request.getHireDate());                   // May be null; no rule forces it.

    if (request.getStationId() != null && !request.getStationId().isBlank()) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Station not found with id: " + request.getStationId()));
        user.setStation(station);                              // Store the relationship, not the string.
    }

    User saved = userRepository.save(user);                    // Persist and return the managed entity.
    return ResponseEntity.status(HttpStatus.CREATED).body(saved); // 201 Created + JSON.
}
```

Key points:

1. Validation runs **before** the method body because of `@Valid`.
2. The duplicate check runs **before** any insert.
3. The client cannot choose the ID; it is always generated.
4. The role can be anything the admin sends, including `ADMIN`.
5. The station is optional here. If sent, it must exist.
6. `hireDate` is copied even for a passenger, because this admin endpoint is deliberately permissive.

### 8.3 `getAllUsers` — GET `/api/users`

```java
@GetMapping                                                    // GET /api/users
@PreAuthorize("hasRole('ADMIN')")                              // Admin only.
@Transactional(readOnly = true)                                // Read-only transaction.
public ResponseEntity<List<User>> getAllUsers() {              // A list of User objects.
    return ResponseEntity.ok(userRepository.findAll());        // 200 OK + every row as JSON.
}
```

`findAll()` has no `ORDER BY`, so the database may return rows in any order. If you need a stable order, add a `Sort` or a repository method such as `findAllByOrderByFirstNameAsc()`.

### 8.4 `getUserById` — GET `/api/users/{id}`

```java
@GetMapping("/{id}")                                           // GET /api/users/{id}
@PreAuthorize("hasRole('ADMIN')")                              // Admin only.
@Transactional(readOnly = true)                                // Read-only transaction.
public ResponseEntity<User> getUserById(@PathVariable String id) {
                                                               // @PathVariable takes {id} from the URL.
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with id: " + id));         // Missing row -> 404.
    return ResponseEntity.ok(user);                            // 200 OK + JSON.
}
```

Note the difference: `ResourceNotFoundException` becomes **404**, while `IllegalArgumentException` becomes **400**. Choosing the right exception is how you choose the status code.

### 8.5 `updateUser` — PUT `/api/users/{id}`

```java
@PutMapping("/{id}")                                           // PUT /api/users/{id}
@PreAuthorize("hasRole('ADMIN')")                              // Admin only.
@Transactional                                                 // Writes are possible, so a normal transaction.
public ResponseEntity<User> updateUser(@PathVariable String id,
                                       @RequestBody User request) {
                                                               // No @Valid: this is a partial update.
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with id: " + id));         // 404 when missing.

    if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
        user.setFirstName(request.getFirstName());
    }                                                          // Blank/absent = leave unchanged.
    if (request.getLastName() != null && !request.getLastName().isBlank()) {
        user.setLastName(request.getLastName());
    }                                                          // Cannot clear a name with "".
    if (request.getContact() != null && !request.getContact().isBlank()) {
        if (!request.getContact().equals(user.getContact())
                && userRepository.findByContact(request.getContact()).isPresent()) {
            throw new IllegalArgumentException("User with this contact already exists.");
        }                                                      // Uniqueness check, ignoring the same user.
        user.setContact(request.getContact());                 // Changing this changes the login name.
    }
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
        user.setPassword(passwordEncoder.encode(request.getPassword()));
    }                                                          // Absent password = keep the old hash.
    if (request.getRole() != null) {
        user.setRole(request.getRole());
    }                                                          // The ID prefix is NOT changed.
    if (request.getIsActive() != null) {
        user.setIsActive(request.getIsActive());
    }                                                          // false immediately blocks login.
    if (request.getHireDate() != null) {
        user.setHireDate(request.getHireDate());
    }                                                          // Cannot clear hireDate with null.
    if (request.getStationId() != null) {
        if (request.getStationId().isBlank()) {
            user.setStation(null);                             // "" explicitly removes the station.
        } else {
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Station not found with id: " + request.getStationId()));
            user.setStation(station);                          // Attach the new station.
        }
    }

    User updated = userRepository.save(user);                  // UPDATE the row.
    return ResponseEntity.ok(updated);                         // 200 OK + updated JSON.
}
```

This is a **partial update** even though the HTTP method is `PUT`. A true `PUT` should replace the whole resource. The project chose the friendlier behaviour: only non-empty fields are changed.

Important consequences:

- Sending `{"firstName":""}` does **not** clear the name; empty strings are ignored.
- Sending `{"stationId":""}` **does** clear the station; the empty string is treated as a command.
- Changing `role` does **not** change the `userId` prefix. A `PASS-...` user promoted to `ADMIN` keeps the `PASS-` ID.
- Changing `contact` changes the username used for Basic login.
- Setting `isActive:false` blocks login on the very next request.
- There is no `@Valid`, so `@NotBlank` does not run here.

### 8.6 `deleteUser` — DELETE `/api/users/{id}`

```java
@DeleteMapping("/{id}")                                        // DELETE /api/users/{id}
@PreAuthorize("hasRole('ADMIN')")                              // Admin only.
@Transactional                                                 // Delete happens inside a transaction.
public ResponseEntity<Void> deleteUser(@PathVariable String id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with id: " + id));         // 404 when missing.
    userRepository.delete(user);                               // DELETE the row.
    return ResponseEntity.noContent().build();                 // 204, empty body.
}
}                                                              // End of UserController class.
```

`ResponseEntity<Void>` means "no body". `noContent()` sets status 204.

If the user has tickets, metro cards, or reported maintenance issues, PostgreSQL rejects the delete because of foreign keys. That exception is not one of the handled types, so `GlobalExceptionHandler` returns **500** with a generic message. A safer design would deactivate the user instead.

---

## 9. One request from start to finish

Example: the admin creates a ticket agent.

**Request**

```http
POST http://localhost:8080/api/users
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

**Story**

1. Swagger sends the request to port 8080.
2. `Authorization` decodes to `admin` / `admin123`.
3. Spring Security loads `admin` through `DbUserDetailsService`.
4. `findByContact("admin")` runs `SELECT ... WHERE "Contact" = 'admin'`.
5. BCrypt compares `admin123` with the stored hash. Match.
6. The principal gets authority `ROLE_ADMIN`.
7. The URL rule `/api/users/**` requires `ADMIN`. Pass.
8. `DispatcherServlet` routes `POST /api/users` to `UserController.createUser`.
9. `@PreAuthorize("hasRole('ADMIN')")` passes.
10. Jackson converts the JSON into a `User` object. `userId` is ignored (`READ_ONLY`); `stationId` is stored in the transient field.
11. `@Valid` checks `firstName`, `lastName`, `contact`, `password`. All present.
12. The duplicate check calls `findByContact("agent9")` and finds nothing.
13. The role is not null and the password is not blank.
14. The prefix switch turns `TICKET_AGENT` into `EMP`.
15. `userId` becomes `EMP-` plus a random UUID.
16. `passwordEncoder.encode("Passw0rd!")` produces a hash such as `$2a$10$Q3...`.
17. `stationRepository.findById("STN-AND")` loads the Andheri station.
18. `user.setStation(andheri)` attaches the object.
19. `userRepository.save(user)` stores the row. Because the ID was assigned in Java, Spring may first run a quick `SELECT` to check whether that ID already exists, and then it runs the `INSERT`.
20. Hibernate fills `registrationDate` and `isActive` because the controller set them.
21. `ResponseEntity.status(CREATED)` sets `201`.
22. Jackson serializes the saved user. `password` and `stationId` are skipped; `station` is included.
23. The transaction commits.
24. Swagger displays `201 Created` and the new user JSON.

If step 12 found a row, the story stops with `400` and "User with this contact already exists."

---

## 10. JSON examples

### 10.1 Create a passenger

```json
{
  "firstName": "Asha",
  "lastName": "Nair",
  "contact": "asha",
  "password": "secret123",
  "role": "PASSENGER"
}
```

`201 Created`:

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

### 10.2 Create staff with a station

```json
{
  "firstName": "Rahul",
  "lastName": "Mehta",
  "contact": "agent9",
  "password": "Passw0rd!",
  "role": "TICKET_AGENT",
  "stationId": "STN-AND",
  "hireDate": "2026-10-29"
}
```

`201 Created`:

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

### 10.3 Field explanations

| JSON field | Direction | Meaning |
|---|---|---|
| `userId` | response only | Generated ID with a role-based prefix. |
| `firstName` | both | Required on create; optional on update. |
| `lastName` | both | Required on create; optional on update. |
| `password` | request only | Plain text going in; hashed before storage; never returned. |
| `role` | both | Required on create; any of the four enum values. |
| `contact` | both | Login username; must be unique. |
| `registrationDate` | both | Defaults to today. |
| `isActive` | both | Defaults to `true`; `false` blocks login. |
| `hireDate` | both | Staff joining date; may be null. |
| `station` | response only | Full station object; cannot be set directly. |
| `stationId` | request only | Convenience field; converted into `station`. |

### 10.4 Update examples

Change the phone number and deactivate:

```json
{
  "contact": "agent9-new",
  "isActive": false
}
```

Remove the station:

```json
{
  "stationId": ""
}
```

Promote to admin (ID stays `EMP-...`):

```json
{
  "role": "ADMIN"
}
```

Reset the password:

```json
{
  "password": "NewPassw0rd!"
}
```

### 10.5 Error examples

Missing first name:

```json
{
  "firstName": "First name is required"
}
```

Duplicate contact:

```json
{
  "error": "User with this contact already exists."
}
```

Unknown ID:

```json
{
  "error": "User not found with id: PASS-123"
}
```

Unknown station:

```json
{
  "error": "Station not found with id: STN-XXX"
}
```

---

## 11. Error table

| Situation | Status | Message |
|---|---|---|
| `firstName` missing or blank | 400 | `{"firstName":"First name is required"}` |
| `lastName` missing or blank | 400 | `{"lastName":"Last name is required"}` |
| `password` missing or blank | 400 | `{"password":"Password is required"}` |
| `contact` missing or blank | 400 | `{"contact":"Contact is required"}` |
| Contact already used | 400 | `{"error":"User with this contact already exists."}` |
| `role` missing | 400 | `{"error":"Role is required."}` |
| `stationId` does not exist | 400 | `{"error":"Station not found with id: ..."}` |
| Bad JSON syntax | 500 | `{"error":"An unexpected error occurred: ..."}` — the catch-all handler wins over Spring's usual 400 |
| `role` value not in the enum | 500 | Same generic error, because Jackson fails before the controller |
| `GET`/`PUT`/`DELETE` with unknown id | 404 | `{"error":"User not found with id: ..."}` |
| No login | 401 | Spring Boot error JSON + `WWW-Authenticate` |
| Not an admin | 403 | `{"error":"You do not have permission to access this resource"}` or Spring's standard 403 error JSON |
| Delete a user referenced by tickets/cards | 500 | `{"error":"An unexpected error occurred: ..."}` |
| Database down | 500 | Generic unexpected error |

Why 400 and not 409 for a duplicate? `409 Conflict` would be more precise, but the handler maps `IllegalArgumentException` to 400. Consistency with the rest of the project matters more here.

---

## 12. Relationships to other groups

```text
Station 1 <----- * User 1 <----- * Ticket
                     ^
                     | 1
                     |
                     * MetroCard

User 1 <----- * MaintenanceIssue (reporter)
```

| Group | Link | What it means |
|---|---|---|
| 02 Auth and Role | `Contact`, `Password`, `Role`, `isActive` | These four columns drive login. |
| 03 Station | `User.StationID → Stations.StationID` | A staff member can be posted to one station. |
| 04 Train | none | Trains do not reference users directly. |
| 05 Route | none | Routes do not reference users. |
| 06 RouteStation | none | Stops do not reference users. |
| 07 Schedule | none | Schedules do not reference users. |
| 08 Fare | none | Fares do not reference users. |
| 09 Ticket | `Tickets.PassengerID → User.UserId` | Every ticket belongs to one passenger. |
| 10 Payment | indirect, through `Ticket` | A payment belongs to a ticket, and that ticket belongs to a user. |
| 11 MetroCard | `MetroCards.PassengerID → User.UserId` | Every card belongs to one passenger. |
| 12 MaintenanceIssue | `MaintenanceIssue.ReportedId → User.UserId` | Every issue records its reporter. |

Rules that follow from these links:

1. Create users before tickets, cards, or maintenance issues.
2. You cannot delete a user who is referenced. Deactivate instead.
3. A station cannot be deleted while staff are posted there.
4. `ADMIN` is the only role that can call `/api/users`.
5. Passengers can still read their own data through other controllers, which look them up by `Authentication.getName()`.

---

## 13. How to test in Swagger

1. Start the app: `.\mvnw.cmd spring-boot:run`.
2. Open `http://localhost:8080/swagger-ui/index.html`.
3. Click **Authorize**.
4. Enter `admin` / `admin123`, click **Authorize**, then **Close**.
5. Open the **User Management** section.
6. `GET /api/users` → **Try it out** → **Execute**. You should see the seeded users: `admin`, `agent1`, `maint1`, `priya`, `arjun`, `meera`, plus any you created. Passwords are absent.
7. `POST /api/users` → **Try it out** → paste:
   ```json
   {
     "firstName": "Test",
     "lastName": "Agent",
     "contact": "testagent",
     "password": "Passw0rd!",
     "role": "TICKET_AGENT",
     "stationId": "STN-AND"
   }
   ```
8. **Execute**. Expect `201` and a `userId` starting with `EMP-`.
9. Copy the `userId`.
10. `GET /api/users/{id}` → paste the ID → **Execute**. Expect `200`.
11. `PUT /api/users/{id}` → paste the ID → body `{"isActive": false}` → **Execute**. Expect `200` with `"isActive": false`.
12. Log out and try to authorize as `testagent` with `Passw0rd!`. Login fails with `401` because the account is inactive.
13. Re-authorize as admin and set `isActive` back to `true`.
14. `DELETE /api/users/{id}` → **Execute**. Expect `204` with an empty body.
15. Call `GET /api/users/{id}` again. Expect `404`.
16. Click **Authorize** → **Logout**, then authorize as `priya` / `Passw0rd!`.
17. Try `GET /api/users`. Expect `403`.

`curl` versions:

```bash
# List users
curl -u admin:admin123 http://localhost:8080/api/users

# Create a user
curl -u admin:admin123 -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"Agent","contact":"testagent","password":"Passw0rd!","role":"TICKET_AGENT","stationId":"STN-AND"}'

# Update
curl -u admin:admin123 -X PUT http://localhost:8080/api/users/EMP-xxxx \
  -H "Content-Type: application/json" \
  -d '{"isActive":false}'

# Delete
curl -u admin:admin123 -X DELETE http://localhost:8080/api/users/EMP-xxxx -i
```

---

## 14. How to explain this in 2 minutes

Say this:

> The User group manages every person who can log in to the metro system: passengers, ticket agents, maintenance staff, and admins.
>
> `User.java` is the entity. It maps to the `"User"` table. The table name is quoted because `USER` is a reserved word in PostgreSQL. The primary key is `UserId`, a 50-character string. The ID has a readable prefix: `PASS-` for passengers, `EMP-` for staff, and `ADMIN-` for admins. The prefix is chosen in the controller and also in a `@PrePersist` method that acts as a safety net.
>
> The table stores first name, last name, a BCrypt password hash, a role, a unique contact, a registration date, an active flag, a hire date, and an optional station ID. `Contact` is the login username, and it has a unique index so two users cannot share it. `Role` is stored as text using `@Enumerated(EnumType.STRING)`.
>
> Two fields are special. `password` is marked `WRITE_ONLY`, so it can come in but never goes out. `stationId` is marked `@Transient` and `WRITE_ONLY`; it is not a column. It is only a convenience field. The controller reads it, looks up the real `Station`, and stores that object in the `ManyToOne` relationship. The response therefore contains a full `station` object and no `stationId`.
>
> `UserRepository` extends `JpaRepository<User, String>`, so it already has `save`, `findById`, `findAll`, and `delete`. It adds `findByContact`, which Spring turns into `WHERE "Contact" = ?`. That method is used for duplicate checks, login, and the "who am I" endpoint.
>
> `UserController` exposes five admin-only endpoints under `/api/users`: POST creates, GET lists, GET by id reads, PUT partly updates, and DELETE removes. Every method has `@PreAuthorize("hasRole('ADMIN')")`, and `SecurityConfig` also blocks the URL for non-admins. Creation validates the required fields with `@Valid`, checks for a duplicate contact, hashes the password, generates an ID, attaches an optional station, and returns `201 Created`. Updates are partial: empty fields are ignored, but an empty `stationId` deliberately clears the station. Deletes return `204`, and a missing ID returns `404`.
>
> Errors are handled centrally: validation gives 400, duplicates give 400, missing rows give 404, wrong roles give 403, and unexpected database errors such as a foreign key violation give 500.

---

## 15. Beginner questions and answers

**Q1. Is `User` a table or a class?**
Both, in a sense. The class is `User.java`; the table is `"User"`. `@Entity` and `@Table` connect them.

**Q2. Why is `User` written with backslashes in Java?**
The table name needs literal double quotes. In a Java string, `\"` means a double quote, so `"\"User\""` becomes `"User"`.

**Q3. Where is the `CREATE TABLE` in the Java code?**
Nowhere. Flyway runs the SQL file. Hibernate only validates that the entity matches the table (`ddl-auto=validate`).

**Q4. Why does the class have two constructors?**
JPA needs the empty constructor to create objects when loading rows. The all-arguments constructor is convenient for tests or manual creation. The controllers use the empty one and call setters.

**Q5. Why are getters and setters written by hand?**
The project does not use Lombok. Writing them makes the behaviour visible and avoids an extra dependency.

**Q6. What does `@PrePersist` do?**
It runs `onCreate()` immediately before the first `INSERT`. It generates an ID if missing and fills default dates.

**Q7. If `@PrePersist` generates IDs, why does the controller also generate them?**
So the controller knows the ID before saving and can choose the prefix based on the requested role. `@PrePersist` is a fallback for any other code path.

**Q8. Why does `userId` have `READ_ONLY`?**
So a client cannot choose its own ID. Jackson ignores `userId` in the request body. It is still included in responses.

**Q9. Why does `password` have `WRITE_ONLY`?**
So it can be accepted in a request but never leaked in a response. Without it, every `GET /api/users` would expose password hashes.

**Q10. Is the password encrypted?**
No, it is **hashed** with BCrypt. Hashing is one-way. You cannot decrypt it; you can only compare a candidate password with the hash.

**Q11. Why is `stationId` not in the database?**
Because the database already has `StationID` as a foreign key managed by the `station` relationship. `stationId` is only a simple way for JSON to carry that value.

**Q12. What happens if I send `"station": {...}` in the request?**
It is ignored because `station` is `READ_ONLY`. Use `"stationId":"STN-AND"` instead.

**Q13. Can a passenger be assigned to a station?**
The database allows it, and `POST /api/users` does not stop it, but it has no meaning. The hire endpoint requires a station for every employee.

**Q14. Why does `PUT` not require all fields?**
The method is written as a partial update. It checks each field for `null` and only copies the ones present. Strict REST would call this `PATCH`, but many projects use `PUT` this way.

**Q15. How do I clear a name or a hire date?**
You cannot through this endpoint. Empty strings are ignored and `null` means "unchanged". You would need a different design or direct database access.

**Q16. How do I clear a station?**
Send `"stationId": ""`. The code checks `isBlank()` and calls `setStation(null)`.

**Q17. What happens to tickets if I change a user's role?**
Nothing. The tickets still point to the same `UserId`. Only permissions change, because permissions come from the current `Role`.

**Q18. Why is the ID prefix not updated when the role changes?**
Because changing a primary key would break every foreign key that points to it. The prefix is a convention, not a rule.

**Q19. Why does deleting a user sometimes return 500?**
The user is referenced by a ticket, card, or maintenance issue. PostgreSQL refuses the delete to protect those rows. The generic handler turns that into 500.

**Q20. Why does `GET /api/users` work for admin but not for `priya`?**
`priya` is authenticated, so the server knows her. But she lacks `ROLE_ADMIN`, so the answer is 403 Forbidden, not 401 Unauthorized.

**Q21. Why is `findAll()` not sorted?**
SQL does not promise an order without `ORDER BY`. Add a `Sort` parameter or a derived method if order matters.

**Q22. Why is `Contact` nullable in SQL if `@NotBlank` requires it?**
The SQL was written before the validation rule, and the column is also used by older data. The Java validation is the effective rule for new requests.

---

## 16. Glossary for this file

| Term | Meaning |
|---|---|
| `@Column` | Maps a field to a column and can set name, length, and nullability. |
| `@DeleteMapping` | Shortcut for a DELETE route. |
| `@Entity` | Marks a class as a table. |
| `@GetMapping` | Shortcut for a GET route. |
| `@Id` | Marks the primary key. |
| `@JoinColumn` | Names the foreign key column. |
| `@ManyToOne` | Many rows point to one row in another table. |
| `@NotBlank` | Rejects null, empty, and whitespace-only strings. |
| `@PathVariable` | Reads a value from the URL path. |
| `@PostMapping` | Shortcut for a POST route. |
| `@PreAuthorize` | Role check before the method runs. |
| `@PrePersist` | Callback before the first INSERT. |
| `@PutMapping` | Shortcut for a PUT route. |
| `@RequestBody` | Builds a parameter from the JSON body. |
| `@RequestMapping` | Common URL prefix for a controller. |
| `@Repository` | Marks a database-access bean. |
| `@RestController` | Controller that returns JSON. |
| `@Table` | Sets the exact table name. |
| `@Transactional` | Wraps the method in a database transaction. |
| `@Transient` | Field is not stored in the database. |
| `@Valid` | Runs validation annotations on a parameter. |
| BCrypt | One-way password hashing algorithm. |
| CRUD | Create, Read, Update, Delete. |
| Derived query | Repository method whose SQL comes from its name. |
| Deserialization | JSON → Java object. |
| Entity | Java class mapped to a table. |
| Foreign key | Column that must match a primary key elsewhere. |
| `JpaRepository` | Spring interface with built-in CRUD methods. |
| JSON | Text data format used by the API. |
| `LocalDate` | Date without time. |
| `Optional` | Container that may be empty. |
| Partial update | Changing only the fields that were supplied. |
| Primary key | Column that uniquely identifies a row. |
| `ResponseEntity` | Wrapper for status code plus body. |
| Serialization | Java object → JSON. |
| Unique index | Database rule preventing duplicates. |
| UUID | Long random identifier. |
| Validation | Checking input before using it. |

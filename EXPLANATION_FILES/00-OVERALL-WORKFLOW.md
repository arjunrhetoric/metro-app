# 00 — The Whole Project: How the Metro Ticketing API Works

**Read this file first.** It explains the project from zero. You do not need to know Spring Boot, HTTP, JSON, or databases before reading it. Every difficult word is defined the first time it appears.

Project root:

```text
C:\Users\arjun\Desktop\metro-ticketing-app
```

After this file, read the file for your group (`01-USER.md`, `02-AUTH-AND-ROLE.md`, and so on).

---

## 1. A real-life analogy: the metro station and its back office

Think of a metro station.

- A passenger walks to the **ticket counter** and asks for something: "One ticket to Ghatkopar."
- The **clerk** at the counter listens, checks the rulebook, and either gives the ticket or says "you need an ID proof."
- The clerk does **not** memorize every passenger. The clerk writes in a **ledger** or checks the **record room** at the back.
- The **record room** has rows and rows of registers. Each register has a fixed set of columns.
- A **security guard** stands at the entrance. He checks your ID badge and decides which floors you may enter.

The software works the same way:

| Metro world | Software world | In this project |
|---|---|---|
| Passenger asking for something | HTTP request | `GET /api/users` |
| The exact words the passenger says | JSON body | `{"firstName":"Asha", ...}` |
| The clerk at the counter | Controller | `UserController`, `AuthController` |
| The clerk's rulebook | Business rules and validation | `if` checks, `@NotBlank`, `@PreAuthorize` |
| The ledger clerk | Repository | `UserRepository` |
| The record room | Database | PostgreSQL on Supabase |
| One row in a register | Row / record | one user, one station, one ticket |
| A blank form with printed columns | Entity class | `User.java`, `Station.java` |
| The security guard | Spring Security | `SecurityConfig` |
| The guard's list of allowed floors | Roles | `ADMIN`, `TICKET_AGENT`, ... |

The most important sentence in this project:

> A request comes in as JSON, the controller applies the rules, the repository talks to the database, and the answer goes back as JSON.

There is **no frontend**. You test the API with Swagger UI (a web page that lists all endpoints), `curl`, or Postman.

---

## 2. Words you need

Read this table once. Come back when a word confuses you.

| Word | One-sentence meaning |
|---|---|
| API | A set of URLs that a program can call to do something. |
| REST API | An API that uses HTTP methods and URLs to work with resources such as users and tickets. |
| Endpoint | One specific URL plus method, for example `GET /api/users`. |
| HTTP | The language browsers and servers use to exchange requests and responses. |
| HTTP request | A message from client to server: method + URL + headers + optional body. |
| HTTP response | A message from server to client: status code + headers + optional body. |
| HTTP method | The verb of a request: `GET` reads, `POST` creates, `PUT` replaces/updates, `PATCH` partly updates, `DELETE` removes. |
| URL | The address of the endpoint, for example `http://localhost:8080/api/users`. |
| Query parameter | A value after `?` in a URL, for example `?amount=100`. |
| Path variable | A value inside the URL path, for example the `{id}` in `/api/users/{id}`. |
| Header | Extra information attached to a request, such as `Authorization` or `Content-Type`. |
| Body | The main content of a request or response, usually JSON. |
| JSON | A text format for data: `{"name":"Asha","age":20}`. |
| Status code | A three-digit number that says what happened: `200` OK, `201` created, `400` bad input, `401` not logged in, `403` not allowed, `404` not found, `500` server crash. |
| Spring Boot | A Java framework that starts a web server and wires the application together automatically. |
| Annotation | A word starting with `@` that adds behaviour to code, for example `@Entity`. |
| Bean | An object that Spring creates and manages for you. |
| Dependency injection (DI) | Spring gives an object the other objects it needs through the constructor instead of you writing `new`. |
| Component scan | Spring searches the package for classes marked `@Component`, `@Service`, `@RestController`, and so on. |
| Auto-configuration | Spring Boot guesses sensible setup from the libraries on the classpath. |
| `@SpringBootApplication` | One annotation that combines configuration, auto-configuration, and component scanning. |
| `@Configuration` | Marks a class whose `@Bean` methods define objects for Spring. |
| `@Transactional` | Runs a method inside one database transaction. |
| `@PreAuthorize` | Checks the caller's role before a method runs. |
| `@RequestBody` | Tells Spring to build a parameter from the JSON request body. |
| `@Valid` | Runs the validation annotations on a parameter before the method body. |
| `ResponseEntity` | A response wrapper that lets a method choose the status code and the body. |
| `Optional` | A container that may hold a value or may be empty. |
| `IllegalArgumentException` | Java's standard "bad argument" exception; this project maps it to 400. |
| `ResourceNotFoundException` | A custom exception that this project maps to 404. |
| Bean Validation | The standard set of input checks such as `@NotBlank` and `@NotNull`. |
| Springdoc | The library that generates the OpenAPI description and Swagger UI. |
| Controller | The class that receives HTTP requests and returns HTTP responses. |
| Repository | The interface that reads and writes the database. |
| Entity | A Java class that maps to one database table. |
| JPA | Java Persistence API: the standard rules for mapping Java objects to tables. |
| Hibernate | The library that implements JPA and produces the real SQL. |
| ORM | Object-Relational Mapping: turning objects into rows and rows into objects. |
| Table | A grid of data in the database with named columns. |
| Row / record | One entry in a table, for example one user. |
| Column / field | One attribute of a row, for example `FirstName`. |
| Primary key (PK) | The column that uniquely identifies a row, here `UserId`. |
| Foreign key (FK) | A column that points to a primary key in another table, here `StationID`. |
| Unique index | A database rule that forbids two rows having the same value, here two users with the same `Contact`. |
| CRUD | Create, Read, Update, Delete — the four basic operations. |
| Migration | A SQL file that changes the database schema in a numbered, repeatable order. |
| Flyway | The tool that runs migration files `V1`, `V2`, ... at startup. |
| Schema | The structure of the database: tables, columns, keys, indexes. |
| Transaction | A group of database changes that either all succeed or all fail. |
| BCrypt | A one-way password hashing algorithm; you can check a password but cannot reverse the hash. |
| Hash | A scrambled fingerprint of a value; the same input always gives the same hash. |
| Salt | Random data added before hashing so two equal passwords give different hashes. |
| Authentication | Proving who you are (username + password). |
| Authorization | Deciding what you are allowed to do (role checks). |
| HTTP Basic Auth | A simple login style: send `username:password` encoded in Base64 in the `Authorization` header on every request. |
| Base64 | A text encoding, **not encryption**; anyone can decode it. |
| Principal | The currently logged-in user as Spring Security sees it. |
| SecurityContext | The place where Spring Security stores the current principal during a request. |
| Stateless | The server keeps no login session; every request must carry credentials again. |
| CSRF | Cross-Site Request Forgery, an attack that matters for cookie-based sessions; disabled here because the API is stateless. |
| Filter | A small component that runs before the controller, used by Spring Security to inspect requests. |
| Role | A job label that controls permissions: `ADMIN`, `TICKET_AGENT`, `MAINTENANCE_STAFF`, `PASSENGER`. |
| Authority | The technical string Spring Security checks, for example `ROLE_ADMIN`. |
| DispatcherServlet | The Spring MVC component that sends each URL to the right controller method. |
| Tomcat | The web server embedded inside Spring Boot; it listens on port 8080. |
| Jackson | The library that converts Java objects to JSON and JSON to Java objects. |
| Serialization | Java object → JSON (response). |
| Deserialization | JSON → Java object (request). |
| Validation | Checking input before using it, for example "first name must not be blank". |
| Constraint | A rule on a field, such as `@NotBlank` or a database `NOT NULL`. |
| Swagger / OpenAPI | A generated web page and JSON description of every endpoint, used for testing. |
| Environment variable | An operating-system value read by the app at startup, for example `DB_PASSWORD`. |
| Placeholder | A value in a config file written as `${NAME:default}` that Spring replaces at startup. |
| JDBC | The low-level Java API for talking to a SQL database. |
| DTO | Data Transfer Object: a separate class just for request/response. This project does **not** use DTOs. |
| Service layer | A separate class between controller and repository. This project does **not** use one. |
| Lombok | A library that generates getters/setters. This project does **not** use it. |
| Idempotent | Safe to repeat with the same result; `GET`, `PUT`, and `DELETE` should be idempotent. |

---

## 3. The files in this project

All paths are relative to the project root.

### 3.1 Application and configuration

| Path | What it does |
|---|---|
| `src/main/java/com/metro/ticketing/TicketingApplication.java` | The `main` method that starts everything. |
| `src/main/java/com/metro/ticketing/config/SecurityConfig.java` | HTTP Basic login, role rules, BCrypt encoder, stateless sessions. |
| `src/main/java/com/metro/ticketing/config/DbUserDetailsService.java` | Loads a user from our database for Spring Security. |
| `src/main/java/com/metro/ticketing/config/OpenApiConfig.java` | Swagger title, version, and the `basicAuth` button. |
| `src/main/java/com/metro/ticketing/config/FlywayConfig.java` | Runs SQL migrations before JPA starts. |
| `src/main/java/com/metro/ticketing/config/DataSeeder.java` | Creates the first admin and demo data on first boot. |
| `src/main/resources/application.properties` | Database URL, JPA settings, admin bootstrap values. |
| `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` | Creates all tables. |
| `src/main/resources/db/migration/V2__prevent_duplicate_fare_station_pairs.sql` | Adds a unique rule on fare station pairs. |
| `src/main/resources/db/migration/V3__add_auth_lifecycle_tables.sql` | Added old token tables (later removed). |
| `src/main/resources/db/migration/V4__widen_auth_token_columns.sql` | Widened old token columns (later removed). |
| `src/main/resources/db/migration/V5__drop_unused_auth_token_tables.sql` | Drops the unused token tables after moving to Basic Auth. |
| `src/main/resources/db/migration/V6__replace_route_station_composite_key.sql` | Gives route stops their own ID. |

### 3.2 The twelve explanation groups

| Group | Entity | Controller | Repository |
|---|---|---|---|
| 01 User | `entity/User.java` | `controller/UserController.java` | `repository/UserRepository.java` |
| 02 Auth and Role | `entity/Role.java`, `entity/User.java` | `controller/AuthController.java` | `repository/UserRepository.java` |
| 03 Station | `entity/Station.java` | `controller/StationController.java` | `repository/StationRepository.java` |
| 04 Train | `entity/Train.java` | `controller/TrainController.java` | `repository/TrainRepository.java` |
| 05 Route | `entity/Route.java` | `controller/RouteController.java` | `repository/RouteRepository.java` |
| 06 RouteStation | `entity/RouteStation.java` | `controller/RouteController.java` (nested `stationIds`) | `repository/RouteStationRepository.java` |
| 07 Schedule | `entity/Schedule.java` | `controller/ScheduleController.java` | `repository/ScheduleRepository.java` |
| 08 Fare | `entity/Fare.java` | `controller/FareController.java` | `repository/FareRepository.java` |
| 09 Ticket | `entity/Ticket.java` | `controller/TicketController.java` | `repository/TicketRepository.java` |
| 10 Payment | `entity/Payment.java` | `controller/PaymentController.java` | `repository/PaymentRepository.java` |
| 11 MetroCard | `entity/MetroCard.java` | `controller/MetroCardController.java` | `repository/MetroCardRepository.java` |
| 12 Maintenance | `entity/MaintenanceIssue.java` | `controller/MaintenanceIssueController.java` | `repository/MaintenanceIssueRepository.java` |

Each entity lives under `src/main/java/com/metro/ticketing/entity/`, each controller under `src/main/java/com/metro/ticketing/controller/`, and each repository under `src/main/java/com/metro/ticketing/repository/`.

### 3.3 Error handling

| Path | What it does |
|---|---|
| `src/main/java/com/metro/ticketing/exception/ResourceNotFoundException.java` | A custom exception that means "no row with this ID". |
| `src/main/java/com/metro/ticketing/exception/GlobalExceptionHandler.java` | Turns exceptions into JSON error responses with the right status code. |

### 3.4 Technology

| Piece | Version / library | Job |
|---|---|---|
| Java | 21 | Language and runtime |
| Spring Boot | 4.1.0 | Framework |
| Spring Web MVC | starter | Controllers and routing |
| Spring Data JPA | starter | Repositories |
| Hibernate | included with JPA | SQL generation |
| Spring Security | starter | Login and roles |
| Validation | starter | `@NotBlank`, `@Valid` |
| PostgreSQL driver | runtime | Talks to PostgreSQL |
| Flyway | core + postgresql | Migrations |
| SpringDoc OpenAPI | 2.8.5 | Swagger UI |
| Maven Wrapper | `mvnw.cmd` | Build and run |

---

## 4. The database

### 4.1 The `User` table, with the exact SQL

This is the SQL from `V1__create_metro_ticketing_schema.sql`:

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
    CONSTRAINT "FK_User_Station" FOREIGN KEY ("StationID") REFERENCES "Stations" ("StationID")
);

CREATE UNIQUE INDEX IF NOT EXISTS "UX_User_Contact" ON "User" ("Contact");
```

### 4.2 What each column means

| Column | Type | Meaning |
|---|---|---|
| `UserId` | `VARCHAR(50)` | Primary key. Text such as `PASS-<uuid>`, `EMP-<uuid>`, or `PASS-SEED1`. |
| `FirstName` | `VARCHAR(100)` | Given name. Required. |
| `LastName` | `VARCHAR(100)` | Family name. Required. |
| `Password` | `VARCHAR(255)` | BCrypt hash (about 60 characters), never the plain password. Required. |
| `Role` | `VARCHAR(50)` | One of `ADMIN`, `TICKET_AGENT`, `MAINTENANCE_STAFF`, `PASSENGER`. Required. |
| `Contact` | `VARCHAR(50)` | The login username. Also used as phone/email in real life. Unique. |
| `RegisterationDate` | `DATE` | The date the account was created. The column name is misspelled in the schema, and the Java code matches the misspelling. |
| `isActive` | `BOOLEAN` | `false` blocks login. Defaults to `TRUE`. |
| `HireDate` | `DATE` | The date an employee joined. `NULL` for passengers. |
| `StationID` | `VARCHAR(50)` | Foreign key to `Stations`. Only staff are assigned to a station. |

Why the quotes? `User` is a reserved word in PostgreSQL, so the table name must be written as `"User"` with double quotes. In Java the same name is written `@Table(name = "\"User\"")` because the backslash escapes the quote inside a Java string.

### 4.3 All tables at a glance

| Table | Holds | Primary key | Main foreign keys |
|---|---|---|---|
| `Stations` | Metro stations | `StationID` | none |
| `Trains` | Train coaches | `TrainID` | none |
| `User` | Passengers and staff | `UserId` | `StationID → Stations` |
| `Route` | Metro lines | `RouteID` | start/end station → `Stations` |
| `RouteStations` | Stops on a route, in order | `RouteStationID` (after V6) | `RouteID`, `StationID` |
| `Schedule` | Timetables | `ScheduleID` | `TrainID`, `RouteID` |
| `Fares` | Price between two stations | `FareID` | source/destination → `Stations` |
| `Tickets` | Issued tickets | `TicketID` | passenger → `User`, fare → `Fares`, two stations |
| `MetroCards` | Smart cards | `CardID` | passenger → `User` |
| `Payment` | Money paid for a ticket | `PaymentID` | `TicketID → Tickets` |
| `MaintenanceIssue` | Fault reports | `IssueID` | train, station, reporter → `User` |

There are 11 tables after the V6 migration (the old `RefreshToken` and `PasswordResetToken` tables were dropped by V5).

### 4.4 The dependency order

Rows must exist before other rows can point at them:

```text
Stations  ->  User (staff station)
Stations  ->  Route  ->  RouteStations
Trains + Route  ->  Schedule
Stations  ->  Fares
User + Fares + Stations  ->  Tickets  ->  Payment
User  ->  MetroCards
Train / Station / User  ->  MaintenanceIssue
```

This is why `DataSeeder` creates users and stations first, then trains, routes, schedules, fares, cards, tickets, payments, and maintenance issues.

---

## 5. Entity line by line

### 5.1 `TicketingApplication.java` — the entry point

```java
package com.metro.ticketing;                                       // 1. The package (folder) this class belongs to.

import org.springframework.boot.SpringApplication;                  // 2. The class that boots a Spring Boot app.
import org.springframework.boot.autoconfigure.SpringBootApplication; // 3. The annotation used below.

@SpringBootApplication                                              // 4. One annotation that enables three things:
                                                                    //    @Configuration (this class can define beans),
                                                                    //    @EnableAutoConfiguration (guess setup),
                                                                    //    @ComponentScan (find classes in this package and below).
public class TicketingApplication {                                 // 5. The main class.

	public static void main(String[] args) {                        // 6. Normal Java entry point, called by the JVM.
		SpringApplication.run(TicketingApplication.class, args);    // 7. Starts Tomcat, Spring, security, JPA, Flyway, the seeder.
	}                                                               // 8. End of main.

}                                                                   // 9. End of class.
```

`@SpringBootApplication` is placed in `com.metro.ticketing`, so the component scan covers `com.metro.ticketing.config`, `.controller`, `.entity`, `.repository`, and `.exception`.

### 5.2 `Role.java` — the four roles

```java
package com.metro.ticketing.entity;   // Role lives in the entity package.

public enum Role {                    // enum = a fixed list of allowed values.
    ADMIN,                            // Runs the system: manages users, stations, trains, routes, fares.
    TICKET_AGENT,                     // Sells tickets and tops up cards at the counter.
    MAINTENANCE_STAFF,                // Reports and resolves faults.
    PASSENGER                         // Travels: books tickets, owns cards and payments.
}
```

An `enum` means Java rejects any other spelling. `"ADMIN"` is valid; `"admin"` or `"MANAGER"` is not.

### 5.3 Anatomy of an entity, using `User.java`

```java
@Entity                                                             // This class maps to a database table.
@Table(name = "\"User\"")                                           // Use the exact table name "User", quotes included.
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})      // Do not put Hibernate's internal proxy fields into JSON.
public class User {                                                 // One object = one row in the "User" table.

    @Id                                                             // This field is the primary key.
    @Column(name = "\"UserId\"", length = 50)                       // Map to column "UserId" with room for 50 characters.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)           // Show it in responses, ignore it in requests.
    @Schema(accessMode = Schema.AccessMode.READ_ONLY,               // Tell Swagger UI it is read-only...
            description = "Auto-generated user ID")                 // ...and show this description.
    private String userId;                                          // The value itself.

    @NotBlank(message = "First name is required")                   // Validation: must not be null or only spaces.
    @Column(name = "\"FirstName\"", length = 100, nullable = false) // Map to required column "FirstName".
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "\"LastName\"", length = 100, nullable = false)
    private String lastName;

    @NotBlank(message = "Password is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)          // Accept it in requests, never print it in responses.
    @Column(name = "\"Password\"", length = 255, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)                                    // Store the role as text ("ADMIN"), not as a number.
    @Column(name = "\"Role\"", length = 50, nullable = false)
    private Role role;

    @NotBlank(message = "Contact is required")
    @Column(name = "\"Contact\"", length = 50)
    private String contact;

    @Column(name = "\"RegisterationDate\"")                         // Note the schema's spelling: RegisterationDate.
    private LocalDate registrationDate = LocalDate.now();           // Default: today.

    @Column(name = "\"isActive\"")
    @JsonProperty("isActive")                                       // Force the JSON name to be exactly "isActive".
    private Boolean isActive = true;                                // New users start active.

    @Column(name = "\"HireDate\"")
    private LocalDate hireDate;                                     // Null for passengers.

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)           // Send the station object in responses...
    @ManyToOne(fetch = FetchType.LAZY)                              // ...many users can share one station...
    @JoinColumn(name = "\"StationID\"", referencedColumnName = "\"StationID\"") // ...joined by this column.
    private Station station;

    @Transient                                                      // Not a database column at all.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)          // Accept stationId in JSON, hide it in responses.
    private String stationId;

    @PrePersist                                                     // Run this method just before the first INSERT.
    public void onCreate() {
        if (this.userId == null && this.role != null) {             // Only generate an ID when none was given.
            String prefix = switch (this.role) {                    // Pick a prefix from the role.
                case PASSENGER -> "PASS";
                case ADMIN -> "ADMIN";
                case TICKET_AGENT, MAINTENANCE_STAFF -> "EMP";
            };
            this.userId = prefix + "-" + UUID.randomUUID().toString(); // Example: PASS-3f2a....
        }
        if (this.registrationDate == null) {                       // If the date is still missing...
            this.registrationDate = LocalDate.now();                // ...use today.
        }
        if (this.isActive == null) {                               // If the flag is still missing...
            this.isActive = true;                                   // ...activate the account.
        }
    }
}
```

The full walkthrough of every getter and setter is in `01-USER.md`.

---

## 6. Repository line by line

```java
package com.metro.ticketing.repository;                    // Interfaces that talk to the database.

import com.metro.ticketing.entity.User;                    // The entity this repository manages.
import org.springframework.data.jpa.repository.JpaRepository; // The parent interface with ready-made methods.
import org.springframework.stereotype.Repository;          // Marks this as a Spring bean.

import java.util.Optional;                                 // A box that may hold a User or may be empty.

@Repository                                                // Optional here, but documents the intent.
public interface UserRepository extends JpaRepository<User, String> {
                                                           // User = entity type, String = type of the primary key.
    Optional<User> findByContact(String contact);          // Spring writes: WHERE "Contact" = ?
    boolean existsByContact(String contact);               // Spring writes a fast "does a row exist?" query.
}
```

You write **no method body**. Spring Data JPA reads the method name, builds the query, and creates an object that implements the interface at startup.

### 6.1 Methods inherited from `JpaRepository`

| Method | SQL Spring generates | Used in this project |
|---|---|---|
| `save(user)` | `INSERT` for a truly new entity, `UPDATE` for an existing one | register, hire, create, update |

Because the controllers assign the ID **before** calling `save`, Spring Data cannot tell that the row is new. It calls Hibernate's `merge`, which may run a quick `SELECT` and then an `INSERT`. The row is still saved correctly; the extra `SELECT` is a harmless detail you may see in the SQL log.
| `findById(id)` | `SELECT * FROM "User" WHERE "UserId" = ?` | get by id, update, delete |
| `findAll()` | `SELECT * FROM "User"` | list all users |
| `delete(user)` | `DELETE FROM "User" WHERE "UserId" = ?` | delete user |
| `count()` | `SELECT count(*) FROM "User"` | seeder checks |
| `existsById(id)` | `SELECT count(*) > 0 ...` | other groups |
| `flush()` | Sends pending SQL immediately | not used directly |

`findByContact` is used in four important places:

1. Register: check whether the contact is already taken.
2. Hire: same check for employees.
3. Login: `DbUserDetailsService` loads the user by contact.
4. `/api/auth/me`: finds the profile of the logged-in user.

`existsByContact` is declared but no controller calls it today. It is a shorter alternative to `findByContact(...).isPresent()`.

---

## 7. Every endpoint in the project

Role column meaning: **Public** = no login, **Any** = any logged-in user, otherwise the exact role required.

### 7.1 Auth and users

| Method | URL | Role | Purpose |
|---|---|---|---|
| POST | `/api/auth/passenger/register` | Public | Create a passenger account. |
| POST | `/api/auth/employee/hire` | ADMIN | Create an employee account. |
| GET | `/api/auth/me` | Any | Return the logged-in user's profile. |
| POST | `/api/users` | ADMIN | Create any user with any role. |
| GET | `/api/users` | ADMIN | List all users. |
| GET | `/api/users/{id}` | ADMIN | Read one user. |
| PUT | `/api/users/{id}` | ADMIN | Partly update one user. |
| DELETE | `/api/users/{id}` | ADMIN | Delete one user. |

### 7.2 Network and fleet

| Method | URL | Role | Purpose |
|---|---|---|---|
| POST | `/api/stations` | ADMIN | Create a station. |
| GET | `/api/stations` | Any | List stations. |
| GET | `/api/stations/{id}` | Any | Read one station. |
| PUT | `/api/stations/{id}` | ADMIN | Update a station. |
| DELETE | `/api/stations/{id}` | ADMIN | Delete a station. |
| POST | `/api/trains` | ADMIN | Create a train. |
| GET | `/api/trains` | Any | List trains. |
| GET | `/api/trains/{id}` | Any | Read one train. |
| PUT | `/api/trains/{id}` | ADMIN | Update a train. |
| DELETE | `/api/trains/{id}` | ADMIN | Delete a train. |
| POST | `/api/routes` | ADMIN | Create a route with ordered `stationIds`. |
| GET | `/api/routes` | Any | List routes. |
| GET | `/api/routes/{id}` | Any | Read one route and its stops. |
| PUT | `/api/routes/{id}` | ADMIN | Update a route and replace its stops. |
| DELETE | `/api/routes/{id}` | ADMIN | Delete a route. |

### 7.3 Timetables, fares, tickets

| Method | URL | Role | Purpose |
|---|---|---|---|
| POST | `/api/schedules` | ADMIN | Create a schedule. |
| GET | `/api/schedules` | Any | List schedules. |
| GET | `/api/schedules/{id}` | Any | Read one schedule. |
| PUT | `/api/schedules/{id}` | ADMIN | Update a schedule. |
| DELETE | `/api/schedules/{id}` | ADMIN | Delete a schedule. |
| POST | `/api/fares` | ADMIN | Create a fare. |
| GET | `/api/fares` | Any | List fares. |
| GET | `/api/fares/lookup?sourceStationId=&destStationId=` | Any | Find the price between two stations. |
| GET | `/api/fares/{id}` | Any | Read one fare. |
| PUT | `/api/fares/{id}` | ADMIN | Update a fare. |
| DELETE | `/api/fares/{id}` | ADMIN | Delete a fare. |
| POST | `/api/tickets` | PASSENGER, TICKET_AGENT, ADMIN | Buy or issue a ticket. |
| GET | `/api/tickets` | ADMIN, TICKET_AGENT | List all tickets. |
| GET | `/api/tickets/{id}` | Any (owner checked) | Read one ticket. |
| GET | `/api/tickets/passenger/{passengerId}` | Any (owner checked) | List one passenger's tickets. |
| PATCH | `/api/tickets/{id}/use` | PASSENGER, TICKET_AGENT, ADMIN | Mark a ticket as used. |
| DELETE | `/api/tickets/{id}` | ADMIN | Delete a ticket. |

### 7.4 Money, cards, maintenance

| Method | URL | Role | Purpose |
|---|---|---|---|
| POST | `/api/payments` | PASSENGER, TICKET_AGENT | Pay for a ticket. |
| GET | `/api/payments` | ADMIN, TICKET_AGENT | List payments. |
| GET | `/api/payments/{id}` | Any (owner checked) | Read one payment. |
| GET | `/api/payments/ticket/{ticketId}` | ADMIN, TICKET_AGENT | Payments of one ticket. |
| DELETE | `/api/payments/{id}` | ADMIN | Delete a payment. |
| POST | `/api/cards` | PASSENGER, TICKET_AGENT, ADMIN | Issue a metro card. |
| POST | `/api/cards/{id}/topup?amount=100` | PASSENGER, TICKET_AGENT, ADMIN | Add balance. |
| GET | `/api/cards` | ADMIN, TICKET_AGENT | List cards. |
| GET | `/api/cards/{id}` | Any (owner checked) | Read one card. |
| GET | `/api/cards/passenger/{passengerId}` | Any (owner checked) | List a passenger's cards. |
| PATCH | `/api/cards/{id}/deactivate` | ADMIN, TICKET_AGENT | Block a card. |
| DELETE | `/api/cards/{id}` | ADMIN | Delete a card. |
| POST | `/api/maintenance` | ADMIN, MAINTENANCE_STAFF, TICKET_AGENT | Report a fault. |
| GET | `/api/maintenance` | Any | List issues. |
| GET | `/api/maintenance/{id}` | Any | Read one issue. |
| GET | `/api/maintenance/train/{trainId}` | Any | Issues for a train. |
| GET | `/api/maintenance/station/{stationId}` | Any | Issues for a station. |
| GET | `/api/maintenance/status/{status}` | Any | Filter by status. |
| PUT | `/api/maintenance/{id}` | ADMIN, MAINTENANCE_STAFF | Update an issue. |
| PATCH | `/api/maintenance/{id}/resolve` | ADMIN, MAINTENANCE_STAFF | Close an issue. |
| DELETE | `/api/maintenance/{id}` | ADMIN | Delete an issue. |

---

## 8. Controller line by line

Every controller follows the same five-part shape:

```text
@RestController  +  @RequestMapping("/api/...")   -> routing
private final Repository fields                   -> dependencies
constructor                                       -> dependency injection
@MethodMapping + @PreAuthorize + @Transactional   -> HTTP + security + transaction
method body                                       -> business rules, then repository call
```

### 8.1 `AuthController.getCurrentUser` — "who am I?"

```java
@GetMapping("/me")                                                  // GET /api/auth/me
@Transactional(readOnly = true)                                     // Open a read-only transaction; no writes allowed.
public ResponseEntity<User> getCurrentUser(Authentication authentication) {
                                                                    // Spring injects the current login information.
    User user = userRepository.findByContact(authentication.getName())
                                                                    // getName() returns the username = the Contact value.
            .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
                                                                    // If the row is missing, throw; handler turns it into 400.
    return ResponseEntity.ok(user);                                 // 200 OK with the user as JSON.
}
```

### 8.2 `UserController.getAllUsers` — list every user

```java
@GetMapping                                                        // GET /api/users
@PreAuthorize("hasRole('ADMIN')")                                  // Second check: caller must be an admin.
@Transactional(readOnly = true)                                    // One read-only database transaction.
public ResponseEntity<List<User>> getAllUsers() {                  // Returns a list of User objects.
    return ResponseEntity.ok(userRepository.findAll());            // 200 OK with every row, converted to JSON.
}
```

### 8.3 `UserController.createUser` — the full pattern

```java
@PostMapping                                                       // POST /api/users
@PreAuthorize("hasRole('ADMIN')")                                  // Only admins may create users.
@Transactional                                                     // All database work succeeds or rolls back together.
public ResponseEntity<User> createUser(@Valid @RequestBody User request) {
                                                                    // @RequestBody = parse JSON into a User;
                                                                    // @Valid = run @NotBlank checks first.
    if (request.getContact() != null && userRepository.findByContact(request.getContact()).isPresent()) {
        throw new IllegalArgumentException("User with this contact already exists."); // 400 via handler.
    }
    if (request.getRole() == null) {                               // Role is mandatory.
        throw new IllegalArgumentException("Role is required.");
    }
    if (request.getPassword() == null || request.getPassword().isBlank()) {
        throw new IllegalArgumentException("Password is required.");
    }

    User user = new User();                                        // Build a fresh entity, do not save the request object.
    String prefix = switch (request.getRole()) {                   // Choose an ID prefix from the role.
        case PASSENGER -> "PASS";
        case ADMIN -> "ADMIN";
        case TICKET_AGENT, MAINTENANCE_STAFF -> "EMP";
    };
    user.setUserId(prefix + "-" + UUID.randomUUID());              // Globally unique ID.
    user.setFirstName(request.getFirstName());                     // Copy allowed fields.
    user.setLastName(request.getLastName());
    user.setContact(request.getContact());
    user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash before saving.
    user.setRole(request.getRole());
    user.setRegistrationDate(request.getRegistrationDate() != null
            ? request.getRegistrationDate() : LocalDate.now());    // Use request value or today.
    if (request.getIsActive() != null) {                           // If the client sent a value...
        user.setIsActive(request.getIsActive());                   // ...use it.
    } else {                                                       // Otherwise...
        user.setIsActive(true);                                    // ...default to active.
    }
    user.setHireDate(request.getHireDate());

    if (request.getStationId() != null && !request.getStationId().isBlank()) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Station not found with id: " + request.getStationId()));
        user.setStation(station);                                  // Attach the real Station object.
    }

    User saved = userRepository.save(user);                        // INSERT and return the saved entity.
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);  // 201 Created with JSON.
}
```

### 8.4 The security guard's rulebook, in one block

```java
http
    .csrf(AbstractHttpConfigurer::disable)                         // No CSRF tokens: this is a stateless API.
    .authorizeHttpRequests(auth -> auth
        .requestMatchers(
            "/api/auth/passenger/register",                        // Anyone may register.
            "/v3/api-docs/**", "/swagger-ui/**",                   // Swagger files must be readable.
            "/swagger-ui.html", "/error"                           // Error page must be reachable.
        ).permitAll()
        .requestMatchers("/api/auth/employee/hire").hasRole("ADMIN") // Hiring is admin-only.
        .requestMatchers("/api/users/**").hasRole("ADMIN")           // All user CRUD is admin-only.
        .anyRequest().authenticated()                                // Everything else needs a login.
    )
    .httpBasic(Customizer.withDefaults())                          // Read the Authorization: Basic header.
    .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))    // Never create an HTTP session.
    .authenticationProvider(authenticationProvider());             // Use our database users + BCrypt.
```

The full explanation of this class is in `02-AUTH-AND-ROLE.md`.

---

## 9. One request from start to finish

Example: an admin opens Swagger and calls **GET /api/users**.

Actual values used:

- Server: `http://localhost:8080`
- Username: `admin`
- Password: `admin123`
- Header value: `Basic YWRtaW46YWRtaW4xMjM=`

Step by step:

1. Swagger builds the request: `GET http://localhost:8080/api/users`.
2. Swagger adds `Authorization: Basic YWRtaW46YWRtaW4xMjM=` and `Accept: application/json`.
3. `YWRtaW46YWRtaW4xMjM=` is Base64 of `admin:admin123`.
4. Tomcat, the embedded web server, receives the request on port 8080.
5. Spring Security's filter chain runs **before** any controller.
6. The Basic authentication filter reads the header, decodes it to `admin` and `admin123`.
7. Spring asks `DaoAuthenticationProvider` to check these credentials.
8. The provider calls `DbUserDetailsService.loadUserByUsername("admin")`.
9. That service calls `userRepository.findByContact("admin")`.
10. Hibernate runs: `select ... from "User" where "Contact"='admin'`.
11. The row is found: ID `ADMIN-...`, hash `$2a$10$...`, role `ADMIN`, active `true`.
12. BCrypt hashes the typed password `admin123` and compares it with the stored hash. It matches.
13. Spring builds a principal with authority `ROLE_ADMIN` and stores it in the `SecurityContext`.
14. The URL rule `.requestMatchers("/api/users/**").hasRole("ADMIN")` passes.
15. `DispatcherServlet` maps `GET /api/users` to `UserController.getAllUsers()`.
16. `@PreAuthorize("hasRole('ADMIN')")` checks the role a second time.
17. `@Transactional(readOnly = true)` opens a read-only transaction.
18. `userRepository.findAll()` causes Hibernate to run `select ... from "User"`.
19. Hibernate turns each row into a `User` object. The password is loaded but never printed.
20. Because `open-in-view=true`, the lazy `station` link can still be read while Jackson converts the object.
21. Jackson skips `password` and `stationId` (WRITE_ONLY) and writes `userId`, names, role, contact, dates, `isActive`, and `station`.
22. The transaction closes and the response goes back: `200 OK` with a JSON array.

If the password were wrong, the story would stop at step 12 with `401 Unauthorized`. If the user were `priya` (a passenger), it would stop at step 14 with `403 Forbidden`.

---

## 10. JSON examples

### 10.1 Register a passenger

Request `POST /api/auth/passenger/register` (no login needed):

```json
{
  "firstName": "Asha",
  "lastName": "Nair",
  "contact": "asha",
  "password": "secret123"
}
```

Response `200 OK`:

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

| Field | Meaning |
|---|---|
| `userId` | Generated ID. `PASS-` means passenger. Read-only, so any value you send is ignored. |
| `firstName`, `lastName` | Copied from the request. |
| `role` | Forced to `PASSENGER` by the register endpoint. |
| `contact` | The login username for future requests. |
| `registrationDate` | Today's date, set by the controller. |
| `isActive` | `true`, so login works immediately. |
| `hireDate` | `null`; passengers are not hired. |
| `station` | `null`; passengers are not posted to a station. |
| password | **Never appears** because of `WRITE_ONLY`. |

### 10.2 Create an employee as admin

Request `POST /api/users` with `admin` / `admin123`:

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

Response `201 Created`:

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

`stationId` is accepted in the request but is **not** a database column. The controller reads it, looks up the `Station`, and stores the full object in `station`. That is why the response contains `station` and not `stationId`.

### 10.3 Error shapes

Validation error `400 Bad Request` — one key per failed field:

```json
{
  "firstName": "First name is required",
  "contact": "Contact is required"
}
```

Business rule error `400 Bad Request`:

```json
{
  "error": "User with this contact already exists."
}
```

Not found `404 Not Found`:

```json
{
  "error": "User not found with id: PASS-123"
}
```

Forbidden `403 Forbidden` (when the method-level check fires):

```json
{
  "error": "You do not have permission to access this resource"
}
```

Unauthenticated `401 Unauthorized` — produced by Spring Security's error page, not by `GlobalExceptionHandler`, so the shape is different:

```json
{
  "timestamp": "2026-10-29T10:15:30.123+00:00",
  "status": 401,
  "error": "Unauthorized",
  "path": "/api/users"
}
```

The response also has the header `WWW-Authenticate: Basic realm="Realm"`.

---

## 11. Error table

| Situation | Status | Message / body |
|---|---|---|
| Missing `firstName`, `lastName`, `password`, or `contact` | 400 | Field-to-message JSON, e.g. `{"firstName":"First name is required"}` |
| Contact already registered | 400 | `{"error":"User with this contact already exists."}` |
| Role missing on create/hire | 400 | `{"error":"Role is required."}` |
| Password missing on `/api/users` create | 400 | `{"password":"Password is required"}` (`@Valid` fires before the manual check) |
| Password missing on register/hire | 400 | `{"password":"Password is required"}` |
| Station ID does not exist | 400 | `{"error":"Station not found with id: STN-XXX"}` |
| Station missing on hire | 400 | `{"error":"Station is required."}` |
| No `Authorization` header | 401 | Spring Boot error JSON, `WWW-Authenticate: Basic` |
| Wrong password | 401 | Same as above |
| Account with `isActive=false` | 401 | Same as above (account is "disabled") |
| Logged in but wrong role | 403 | `{"error":"You do not have permission..."}` or the Spring error JSON if the URL rule rejected it |
| `/api/users/{id}` with unknown id | 404 | `{"error":"User not found with id: ..."}` |
| `/api/auth/me` for a deleted user | 401 (or 400 in a rare race) | Login fails first; if the row vanishes after login, `{"error":"Current user not found"}` |
| Deleting a user who owns tickets or cards | 500 | `{"error":"An unexpected error occurred: ...foreign key..."}` |
| Any unexpected exception | 500 | `{"error":"An unexpected error occurred: <details>"}` |

`GlobalExceptionHandler` is the single place that maps exceptions to these responses:

- `MethodArgumentNotValidException` (failed `@Valid`) → 400 + field map.
- `ResourceNotFoundException` → 404 + `{"error": ...}`.
- `IllegalArgumentException` → 400 + `{"error": ...}`.
- `AccessDeniedException` → 403 + `{"error": ...}`.
- `Exception` (everything else) → 500 + `{"error": ...}` and logs the stack trace.

---

## 12. Relationships to the other groups

`User` is a central table: three other tables point at it, and staff also point at `Stations`. Everything below is a foreign key relationship involving `User.UserId`.

```text
                    +-----------+
                    |  Station  |
                    +-----+-----+
                          ^
                          | StationID (staff only)
                          |
+---------+         +-----+-----+         +-----------+
|  Role   |-------->|   User    |<--------| MetroCard |
+---------+  enum   +-----+-----+  PASS-  +-----------+
                          ^
                          | passenger
                    +-----+-----+
                    |  Ticket   |-----> Payment
                    +-----------+
                          ^
                          | reporter (ReportedId)
                  +-------+---------+
                  | MaintenanceIssue|
                  +-----------------+
```

| Group | Relationship in simple words |
|---|---|
| 01 User | The base table: one row per person who can log in. |
| 02 Auth and Role | Login reads `Contact`, `Password`, `Role`, `isActive`. It does not need the other columns. |
| 03 Station | A staff user may be posted to one station. Deleting a station that still has staff fails. |
| 04 Train | No direct link to users. |
| 05 Route | No direct link to users. |
| 06 RouteStation | No direct link to users. |
| 07 Schedule | No direct link to users. |
| 08 Fare | No direct link to users. |
| 09 Ticket | Each ticket belongs to one passenger (`PassengerID`). |
| 10 Payment | Payment links to a ticket, and the ticket links to the passenger. |
| 11 MetroCard | Each card belongs to one passenger (`PassengerID`). |
| 12 MaintenanceIssue | Each issue records who reported it (`ReportedId`). |

Practical consequences:

1. Create users and stations before anything that references them.
2. You cannot delete a user who has tickets, cards, or reported issues; the database blocks it with a foreign key error.
3. Deactivating (`isActive=false`) is safer than deleting because history stays intact.

---

## 13. How to test in Swagger

1. Set the database password in the terminal:
   - PowerShell: `$env:DB_PASSWORD = "your-password"`
   - Git Bash: `export DB_PASSWORD="your-password"`
2. Start the app: `.\mvnw.cmd spring-boot:run` (Windows) or `./mvnw spring-boot:run`.
3. Wait for the log line `Tomcat started on port 8080`.
4. Open `http://localhost:8080/swagger-ui/index.html`.
5. Click **Authorize** (top right).
6. Enter username `admin` and password `admin123`. Click **Authorize**, then **Close**.
7. Expand **User Management** → `GET /api/users` → **Try it out** → **Execute**.
8. Check the response: `200` and a JSON array of users. No passwords appear.
9. Expand `POST /api/users` and use this body:
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
10. Execute. Expect `201 Created` and a `userId` starting with `EMP-`.
11. Execute the same request again. Expect `400` with "contact already exists".
12. Click **Authorize** again and switch to `priya` / `Passw0rd!`.
13. Try `GET /api/users`. Expect `403 Forbidden`.
14. Try `GET /api/auth/me`. Expect `200` and Priya's profile with `"role":"PASSENGER"`.
15. To test registration without login, first click **Authorize** → **Logout**, then call `POST /api/auth/passenger/register`. It is public.
16. Use an incognito window when switching users, because browsers cache Basic credentials.

Useful `curl` equivalents:

```bash
curl -u admin:admin123 http://localhost:8080/api/users

curl -u priya:Passw0rd! http://localhost:8080/api/auth/me

curl -X POST http://localhost:8080/api/auth/passenger/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Asha","lastName":"Nair","contact":"asha","password":"secret123"}'
```

---

## 14. How to explain this in 2 minutes

Say this out loud:

> My project is a Metro Ticketing REST API built with Spring Boot and PostgreSQL. It has no frontend; the API is tested through Swagger UI.
>
> The system has four roles: passengers buy tickets and hold metro cards, ticket agents sell tickets and top up cards, maintenance staff report and resolve faults, and admins manage the whole network including users, stations, trains, routes, schedules, and fares.
>
> The code has four layers. Entities are Java classes that map to database tables. Repositories are interfaces that read and write those tables; Spring Data JPA writes the SQL from the method name, so there is almost no SQL in Java. Controllers receive HTTP requests, apply the business rules and validation, and return JSON. Spring Security checks the username and password before any controller runs.
>
> Login uses HTTP Basic authentication. The username is the `Contact` column, and the password is stored as a BCrypt hash, never as plain text. Every request carries the credentials in the `Authorization` header. The server keeps no session; it is stateless. `DbUserDetailsService` loads the user from our own `User` table, and `SecurityConfig` decides which URLs are public and which need a role. Admin-only URLs are `/api/users/**` and `/api/auth/employee/hire`. On top of that, `@PreAuthorize` checks the role again inside the controller.
>
> The database schema is created by Flyway migrations, not by Hibernate. Hibernate is set to `validate`, so it only checks that the tables match the entities. On first start, `DataSeeder` creates a bootstrap admin from configuration and loads demo data: stations, trains, routes, fares, cards, and tickets.
>
> A typical request is `GET /api/users`. Spring Security decodes the Basic header, loads the admin by contact, compares the BCrypt hash, checks the admin role, and passes the request to `UserController`. The controller calls `UserRepository.findAll()`, Hibernate runs a `SELECT`, Jackson converts the rows to JSON, and the password field is hidden because it is marked write-only.
>
> Errors are centralised in `GlobalExceptionHandler`: validation problems become 400, missing rows become 404, failed logins become 401, wrong roles become 403, and unexpected crashes become 500.

That is about two minutes at a normal speaking pace.

---

## 15. Beginner questions and answers

**Q1. Is this a website?**
No. It is a backend API. It returns JSON, not HTML pages. Swagger UI is just a testing page for the API.

**Q2. What is the difference between a controller, a repository, and an entity?**
An entity is the shape of the data. A repository fetches and saves that data. A controller receives the HTTP request and decides what to do.

**Q3. Where is the service layer?**
There is none. The business logic lives directly in the controllers. That is a deliberate simplification for this project.

**Q4. Why are entities used as request and response bodies?**
Because the project has no DTO layer. It saves code, but it means the same class is used for input and output. Annotations such as `WRITE_ONLY` and `READ_ONLY` control which fields appear where.

**Q5. Why is there no `new UserRepository()` anywhere?**
Spring creates the repository object at startup and passes it into the controller constructor. That is dependency injection.

**Q6. How does Spring know `findByContact` means `WHERE Contact = ?`?**
Spring Data reads the method name. `findBy` means SELECT, `Contact` is the field name, and the parameter is the value. This is called a derived query.

**Q7. Does Hibernate create the tables?**
No. Flyway creates them. `spring.jpa.hibernate.ddl-auto=validate` only checks that the mapping and the schema agree. If they disagree, startup fails.

**Q8. Why does the app need `DB_PASSWORD` before it starts?**
`application.properties` contains `spring.datasource.password=${DB_PASSWORD}` with no default. If the variable is missing, Spring cannot connect and startup fails.

**Q9. What is HTTP Basic authentication?**
The client sends `Authorization: Basic <Base64 of username:password>` on every request. Base64 is only encoding, so HTTPS is required in production.

**Q10. Why is the password hashed?**
If the database leaks, attackers see `$2a$10$...` instead of the real password. BCrypt is intentionally slow, which makes guessing expensive.

**Q11. Why is the username called `Contact`?**
It is the contact value the user registers with, such as a phone number or username. It is used as the login name and has a unique index.

**Q12. Why does `GET /api/users` return 403 for a passenger instead of 401?**
401 means "I do not know who you are". 403 means "I know who you are, but you are not allowed". Priya is logged in, so the answer is 403.

**Q13. Why do user IDs start with `PASS-`, `EMP-`, or `ADMIN-`?**
It is a readable convention so you can tell the role from the ID. The prefix is chosen by the controller and by `@PrePersist`, but the database does not enforce it.

**Q14. What happens if I send a `userId` in the request body?**
It is ignored, because the field is `READ_ONLY` for JSON. The server generates the ID.

**Q15. Why is `stationId` in the request but not in the response?**
It is a `@Transient` helper field, marked `WRITE_ONLY`. The controller converts it into a real `Station` object, which is what gets stored and returned.

**Q16. Why is `station` marked `LAZY`?**
The database row only stores `StationID`. Hibernate loads the full station only when someone reads it. `open-in-view=true` keeps the database session open during JSON conversion so this works.

**Q17. What does `@Transactional` actually do?**
It wraps the method in a database transaction. If any exception escapes, all changes in that method are rolled back. `readOnly = true` is a hint that no writes are expected.

**Q18. Can I delete the admin user?**
Technically yes, if no other row references it. Practically no: you would lock yourself out until the seeder recreates `admin` on the next restart.

**Q19. Why are some JSON fields missing from the response?**
Jackson only writes getters, and fields marked `WRITE_ONLY` are skipped. `password` and `stationId` are therefore absent.

**Q20. Why does `POST /api/users` return 201 but `POST /api/auth/passenger/register` return 200?**
Different authors wrote them at different times. 201 is the more correct answer for a create, but both are accepted by clients.

---

## 16. Glossary

| Term | Meaning in this project |
|---|---|
| `@Bean` | A method in a config class whose return value Spring manages. |
| `@Column` | Maps a Java field to a database column and sets its name and length. |
| `@Entity` | Marks a class as a database table mapping. |
| `@Id` | Marks the primary key field. |
| `@JoinColumn` | Names the foreign key column used by a relationship. |
| `@ManyToOne` | Many rows point to one row in another table (many users → one station). |
| `@PreAuthorize` | Checks a role before a controller method runs. |
| `@RequestBody` | Tells Spring to build the parameter from the JSON body. |
| `@RestController` | Marks a class whose methods return JSON instead of HTML pages. |
| `@RequestMapping` | Sets the common URL prefix for a controller. |
| `@Transactional` | Runs the method inside one database transaction. |
| `@Valid` | Runs Bean Validation annotations on the parameter. |
| `AccessDeniedException` | The exception used when a logged-in user lacks permission. |
| `Authentication` | The object holding the current user's identity and authorities. |
| `BCryptPasswordEncoder` | The class that hashes and verifies passwords. |
| `Bean Validation` | The `@NotBlank`, `@NotNull` family of input checks. |
| `ControllerAdvice` | A class that applies exception handlers to all controllers. |
| `Derived query` | A repository method whose SQL is generated from its name. |
| `DispatcherServlet` | Spring MVC's front controller that routes URLs to methods. |
| `DTO` | A class used only to carry data between layers; not used here. |
| `Embedded server` | Tomcat packaged inside the application, started by `main`. |
| `Flyway` | Runs `V1`–`V6` SQL files in order at startup. |
| `Foreign key` | A column that must match a primary key in another table. |
| `Hibernate` | The ORM that turns entity operations into SQL. |
| `HTTP Basic` | Username/password sent in a header on every request. |
| `Jackson` | Converts between Java objects and JSON. |
| `JPA` | The mapping standard used by Hibernate. |
| `Lazy loading` | Loading a related row only when it is actually read. |
| `Migration` | A numbered SQL change applied once by Flyway. |
| `OpenAPI` | The machine-readable description behind Swagger UI. |
| `Optional` | A container that may hold a value or be empty, forcing you to handle "not found". |
| `Principal` | The logged-in identity inside the SecurityContext. |
| `Repository` | An interface for database operations, implemented by Spring. |
| `REST` | A style of API based on URLs and HTTP methods. |
| `Role` | A permission label checked by `hasRole`. |
| `Schema` | The structure of tables and columns. |
| `Serialization` | Converting a Java object into JSON. |
| `Stateless` | No server-side login session; credentials are sent each time. |
| `Swagger UI` | The generated test page at `/swagger-ui/index.html`. |
| `Transaction` | All-or-nothing unit of database work. |
| `Unique index` | A database rule preventing duplicate values. |
| `UUID` | A long random identifier, used in generated user IDs. |
| `Validation` | Rejecting bad input before it reaches the database. |

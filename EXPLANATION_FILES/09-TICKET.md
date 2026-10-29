# 09 — Ticket Group

> **Analogy.** A metro ticket is a cinema ticket. It is printed for **one person**, for
> **one journey** (source → destination), at **one price**, and it is valid for a
> limited time. At the gate the staff marks it **used**. A used ticket cannot be used
> again. An expired ticket cannot be used at all. And you are not allowed to pick up
> somebody else's ticket from the counter. Every rule in this file is one of those
> ideas turned into code.

---

## 1. What a Ticket represents

A `Ticket` is **one issued journey pass**.

It stores:

- its own ID (`TKT-...`)
- **who** owns it (`PassengerID` → `User`)
- **which fare** was applied (`FareID` → `Fares`)
- **where** the journey starts and ends (`SourceStationID`, `DestStationID`)
- a free-text type (`SINGLE`, `ROUND_TRIP`, `DAILY`, `WEEKLY`, ...)
- when it expires (`ValidUntil`)
- whether it has been used (`isUsed`)
- when it was issued (`IssueTime`) and created (`CreatedAt`)

It does **not** store the price directly. The price lives in the linked `Fare` row.
It does **not** store the schedule or the train. A ticket is not tied to a departure
time in this model.
It does **not** store a payment. Payments are a separate group and are not created
automatically by ticket booking.

A real seeded row:

| TicketID | PassengerID | FareID | From | To | Type | isUsed |
|---|---|---|---|---|---|---|
| `TKT-SEED1` | `PASS-SEED1` | `FARE-STN-VSV-STN-WEH` | `STN-VSV` | `STN-WEH` | `SINGLE` | `true` |

---

## 2. Words you need

| Word | Simple meaning |
|---|---|
| **Spring Boot** | Framework that starts the server and wires classes. |
| **Bean** | Object created and managed by Spring. |
| **Dependency injection** | Giving a class the objects it needs. |
| **Constructor injection** | Injection through the constructor. |
| **Annotation** | An `@`-prefixed instruction in Java. |
| **JPA** | Java Persistence API: maps objects to tables. |
| **Hibernate** | The library that implements JPA and writes SQL. |
| **Entity** | Java class mapped to a table. |
| **Repository** | Interface for database access; Spring writes the implementation. |
| **Controller** | Class that handles HTTP requests. |
| **REST API** | Web API with resource URLs and HTTP methods. |
| **HTTP** | Request/response protocol of the web. |
| **Endpoint** | One URL plus one HTTP method. |
| **JSON** | Text format `{ "key": value }`. |
| **Serialization** | Java object → JSON. |
| **Deserialization** | JSON → Java object. |
| **`@Entity`** | Marks a class as a table. |
| **`@Table`** | Sets the exact table name. |
| **`@Id`** | Marks the primary key. |
| **`@Column`** | Maps a field to a column. |
| **Primary key** | Column that uniquely identifies a row. |
| **Foreign key** | Column pointing to another table's primary key. |
| **`@ManyToOne`** | Many rows here point to one row there. |
| **`@JoinColumn`** | Names the foreign-key column. |
| **`referencedColumnName`** | Which column in the other table is referenced. |
| **`FetchType.LAZY`** | Load the relation only when used. |
| **`@Transient`** | "Not a database column." |
| **`@JsonProperty`** | Controls JSON reading/writing. |
| **`READ_ONLY`** | In responses, ignored in requests. |
| **`WRITE_ONLY`** | Accepted in requests, hidden in responses. |
| **`@JsonIgnoreProperties`** | Skip named internal properties. |
| **`hibernateLazyInitializer`** | Hibernate proxy property that we hide. |
| **`@NotBlank`** | Validation: not null and not only whitespace. |
| **`@Valid`** | Run validation on this parameter. |
| **`@RequestBody`** | Build parameter from JSON body. |
| **`@PathVariable`** | Take parameter from the URL path. |
| **`@RestController`** | Controller whose return value is the response body. |
| **`@RequestMapping`** | Base URL for the class. |
| **`@PostMapping`** | Handles `POST` (create). |
| **`@GetMapping`** | Handles `GET` (read). |
| **`@PatchMapping`** | Handles `PATCH` (partial update). |
| **`@DeleteMapping`** | Handles `DELETE`. |
| **`@PreAuthorize`** | Role check before the method runs. |
| **`hasRole`** | Exactly one role is required. |
| **`hasAnyRole`** | Any one of several roles is enough. |
| **`@Transactional`** | All database work commits or rolls back together. |
| **`readOnly = true`** | Hint that data will not change. |
| **`ResponseEntity`** | Wrapper for status + body. |
| **`HttpStatus.CREATED`** | Status `201`, resource created. |
| **`Optional`** | Box that may or may not hold a value. |
| **`orElseThrow`** | Return the value or throw when empty. |
| **`JpaRepository`** | Spring Data interface with ready-made CRUD. |
| **Derived query** | Query generated from a method name. |
| **Underscore in a method name** | "Go into the relation", e.g. `passenger_UserId`. |
| **`Authentication`** | Spring object describing the logged-in user. |
| **`authentication.getName()`** | The username used at login; here it is the contact. |
| **`getAuthorities()`** | The roles/permissions of the logged-in user. |
| **`ROLE_PASSENGER`** | The authority string for the passenger role. |
| **Ownership check** | Verify the logged-in user owns the record. |
| **`AccessDeniedException`** | Exception meaning "you are not allowed"; becomes `403`. |
| **`Boolean.TRUE.equals(x)`** | Null-safe way to test whether `x` is `true`. |
| **UUID** | Long random identifier. |
| **`LocalDateTime`** | Date plus time, e.g. `2026-10-29T09:00`. |
| **`isBefore`** | Strictly earlier comparison. |
| **`isBlank`** | Null-safe check for an empty or whitespace-only string. |
| **`IllegalArgumentException`** | Exception used here for bad input or missing records. |
| **Idempotent** | Repeating the call has the same end state. |

---

## 3. Files in this group

| Role | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/Ticket.java` |
| Controller | `src/main/java/com/metro/ticketing/controller/TicketController.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/TicketRepository.java` |
| Table definition | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Seed data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` (`seedTickets`, `ticket`) |
| Related entities | `User.java`, `Fare.java`, `Station.java` |
| Related repository | `FareRepository.java` (fare lookup during booking) |

No service class. All booking rules are inside `TicketController`.

---

## 4. Database table

### SQL from V1

```sql
CREATE TABLE IF NOT EXISTS "Tickets" (
    "TicketID" VARCHAR(50) PRIMARY KEY,
    "PassengerID" VARCHAR(50) NOT NULL,
    "FareID" VARCHAR(50) NOT NULL,
    "SourceStationID" VARCHAR(50) NOT NULL,
    "DestStationID" VARCHAR(50) NOT NULL,
    "TicketType" VARCHAR(50),
    "ValidUntil" TIMESTAMP,
    "isUsed" BOOLEAN DEFAULT FALSE,
    "IssueTime" TIMESTAMP,
    "CreatedAt" TIMESTAMP,
    CONSTRAINT "FK_Ticket_Passenger" FOREIGN KEY ("PassengerID") REFERENCES "User" ("UserId"),
    CONSTRAINT "FK_Ticket_Fare" FOREIGN KEY ("FareID") REFERENCES "Fares" ("FareID"),
    CONSTRAINT "FK_Ticket_SourceStation" FOREIGN KEY ("SourceStationID") REFERENCES "Stations" ("StationID"),
    CONSTRAINT "FK_Ticket_DestStation" FOREIGN KEY ("DestStationID") REFERENCES "Stations" ("StationID")
);
```

### Column-by-column meaning

| Column | Type | Meaning |
|---|---|---|
| `TicketID` | `VARCHAR(50)` | Primary key. New: `TKT-<uuid>`. Seed: `TKT-SEED1` … `TKT-SEED5`. |
| `PassengerID` | `VARCHAR(50)` | Foreign key to `User`. The ticket owner. Required. |
| `FareID` | `VARCHAR(50)` | Foreign key to `Fares`. The price row used. Required. |
| `SourceStationID` | `VARCHAR(50)` | Foreign key to `Stations`. Journey start. Required. |
| `DestStationID` | `VARCHAR(50)` | Foreign key to `Stations`. Journey end. Required. |
| `TicketType` | `VARCHAR(50)` | Free text: `SINGLE`, `ROUND_TRIP`, `DAILY`, `WEEKLY`. Nullable at DB level. |
| `ValidUntil` | `TIMESTAMP` | Expiry date and time. Nullable. |
| `isUsed` | `BOOLEAN` | `true` once the ticket has been validated at a gate. Default `FALSE`. |
| `IssueTime` | `TIMESTAMP` | When the ticket was issued. |
| `CreatedAt` | `TIMESTAMP` | When the row was created. |

### Things the table does not store

- No price/amount column. The amount is reached through `FareID`.
- No train or schedule.
- No QR code or barcode.
- No payment status.
- No passenger name snapshot.

### No unique constraints beyond the primary key

A passenger may hold many tickets. There is no rule like "one ticket per journey".
The same passenger can book the same journey twice and get two separate IDs.

---

## 5. Entity line by line

File: `src/main/java/com/metro/ticketing/entity/Ticket.java`

### Package and imports

```java
package com.metro.ticketing.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;         // hide Hibernate internals
import com.fasterxml.jackson.annotation.JsonProperty;                 // READ_ONLY / WRITE_ONLY
import jakarta.persistence.*;                                         // JPA annotations
import jakarta.validation.constraints.NotBlank;                       // non-blank text check

import java.time.LocalDateTime;                                       // issue/expiry timestamps
```

### Class declaration

```java
@Entity                                                               // this class is a table
@Table(name = "\"Tickets\"")                                          // exact table name: Tickets
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})         // hide proxy internals
public class Ticket {
```

The escaped quotes keep the capital `T`. Without them PostgreSQL would look for
`tickets`, which is not the created table name.

### Primary key

```java
@Id                                                 // primary key
@Column(name = "\"TicketID\"", length = 50)         // column TicketID, max 50 characters
private String ticketId;
```

IDs are strings because seeded tickets use `TKT-SEED1` and API tickets use
`TKT-<uuid>`.

### Passenger relation

```java
@JsonProperty(access = JsonProperty.Access.READ_ONLY)   // response shows passenger, request cannot set it
@ManyToOne(fetch = FetchType.LAZY)                      // many tickets can belong to one user
@JoinColumn(
    name = "\"PassengerID\"",                           // FK column in Tickets
    referencedColumnName = "\"UserId\"",                // PK column in User (note the spelling)
    nullable = false)                                   // required
private User passenger;
```

`@ManyToOne` is right because a user can own many tickets, but a ticket has one owner.

The referenced column is `UserId`, not `UserID`, because that is the exact name used
in the `User` table. Java field names and database column names are independent.

### Fare relation

```java
@JsonProperty(access = JsonProperty.Access.READ_ONLY)   // response only
@ManyToOne(fetch = FetchType.LAZY)                      // many tickets can use one fare
@JoinColumn(
    name = "\"FareID\"",                                // FK column in Tickets
    referencedColumnName = "\"FareID\"",                // PK column in Fares
    nullable = false)
private Fare fare;
```

This is the only link between a ticket and its price. There is no `Amount` field on
`Ticket`.

### Source station relation

```java
@JsonProperty(access = JsonProperty.Access.READ_ONLY)   // response only
@ManyToOne(fetch = FetchType.LAZY)                      // many tickets can start at one station
@JoinColumn(
    name = "\"SourceStationID\"",                       // FK column in Tickets
    referencedColumnName = "\"StationID\"",             // PK column in Stations
    nullable = false)
private Station sourceStation;
```

### Destination station relation

```java
@JsonProperty(access = JsonProperty.Access.READ_ONLY)   // response only
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(
    name = "\"DestStationID\"",                         // FK column in Tickets
    referencedColumnName = "\"StationID\"",             // PK column in Stations
    nullable = false)
private Station destStation;
```

Three relations point at two tables: `passenger` → `User`, `fare` → `Fares`, and two
station fields → `Stations`.

### Transient passenger ID

```java
@Transient                                              // not a column
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // request only
private String passengerId;
```

Why it exists: an agent booking for a passenger sends
`"passengerId": "PASS-SEED1"`. The controller reads it, finds the `User`, and sets the
`passenger` relation.

A passenger booking for themselves does not need to send it, because the controller
overwrites it with their own user ID.

### Transient source station ID

```java
@Transient                                              // not a column
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // request only
private String sourceStationId;
```

Request value such as `"STN-VSV"`.

### Transient destination station ID

```java
@Transient                                              // not a column
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // request only
private String destStationId;
```

Request value such as `"STN-GHP"`.

**Why not send nested objects?** Because a client should not have to know the full
shape of `User`, `Fare` and `Station` just to buy a ticket. Sending IDs is simpler and
safer. The `READ_ONLY` annotations block any attempt to inject nested objects.

### Ticket type

```java
@NotBlank(message = "Ticket type is required")   // must exist and contain a non-space character
@Column(name = "\"TicketType\"", length = 50)    // column TicketType, max 50 characters
private String ticketType;
```

`@NotBlank` is stricter than `@NotNull`:

| Value | `@NotNull` | `@NotBlank` |
|---|---|---|
| `null` | fail | fail |
| `""` | pass | fail |
| `"   "` | pass | fail |
| `"SINGLE"` | pass | pass |

There is no enum and no allowed-value list. `"ANYTHING"` is accepted as long as it is
non-blank and fits in 50 characters.

### Expiry time

```java
@Column(name = "\"ValidUntil\"")   // maps to ValidUntil
private LocalDateTime validUntil;
```

Nullable. If the request does not provide it, the controller sets `now + 24 hours`.
The entity itself has no default.

### Used flag

```java
@Column(name = "\"isUsed\"")        // maps to isUsed
private Boolean isUsed = false;     // new tickets start unused
```

A `Boolean` wrapper, not a primitive, because the database column can be null for old
rows. That is why the controller tests it with
`Boolean.TRUE.equals(ticket.getIsUsed())` instead of `ticket.getIsUsed()` — unboxing a
null would throw a `NullPointerException`.

### Issue time

```java
@Column(name = "\"IssueTime\"")                          // maps to IssueTime
private LocalDateTime issueTime = LocalDateTime.now();   // default when the Java object is built
```

The controller also sets it explicitly, so the value is always "now" at booking.

### Created timestamp

```java
@Column(name = "\"CreatedAt\"")                          // maps to CreatedAt
private LocalDateTime createdAt = LocalDateTime.now();   // default when the object is built
```

### Accessors

```java
public String getTicketId() { return ticketId; }                       // read ID
public void setTicketId(String ticketId) { this.ticketId = ticketId; }

public User getPassenger() { return passenger; }                       // read lazy owner
public void setPassenger(User passenger) { this.passenger = passenger; }

public Fare getFare() { return fare; }                                 // read lazy fare
public void setFare(Fare fare) { this.fare = fare; }

public Station getSourceStation() { return sourceStation; }            // read lazy source
public void setSourceStation(Station sourceStation) { this.sourceStation = sourceStation; }

public Station getDestStation() { return destStation; }                // read lazy destination
public void setDestStation(Station destStation) { this.destStation = destStation; }

public String getTicketType() { return ticketType; }                   // read type text
public void setTicketType(String ticketType) { this.ticketType = ticketType; }

public LocalDateTime getValidUntil() { return validUntil; }            // read expiry
public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }

public Boolean getIsUsed() { return isUsed; }                          // read used flag
public void setIsUsed(Boolean isUsed) { this.isUsed = isUsed; }

public LocalDateTime getIssueTime() { return issueTime; }              // read issue time
public void setIssueTime(LocalDateTime issueTime) { this.issueTime = issueTime; }

public LocalDateTime getCreatedAt() { return createdAt; }              // read creation time
public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

public String getPassengerId() { return passengerId; }                 // transient request value
public void setPassengerId(String passengerId) { this.passengerId = passengerId; }

public String getSourceStationId() { return sourceStationId; }         // transient request value
public void setSourceStationId(String sourceStationId) { this.sourceStationId = sourceStationId; }

public String getDestStationId() { return destStationId; }             // transient request value
public void setDestStationId(String destStationId) { this.destStationId = destStationId; }
```

The JSON property for `isUsed` is `"isUsed"`, because the getter is `getIsUsed()`.

---

## 6. Repository line by line

File: `src/main/java/com/metro/ticketing/repository/TicketRepository.java`

```java
package com.metro.ticketing.repository;

import com.metro.ticketing.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;   // CRUD
import org.springframework.stereotype.Repository;              // bean marker

import java.util.List;                                         // list result

@Repository                                                    // Spring creates the implementation
public interface TicketRepository extends JpaRepository<Ticket, String> {
```

`JpaRepository<Ticket, String>`: entity `Ticket`, primary key type `String`.

### Inherited methods used by the controller

| Method | SQL (conceptual) |
|---|---|
| `findById(id)` | `SELECT ... FROM "Tickets" WHERE "TicketID" = ?` |
| `findAll()` | `SELECT ... FROM "Tickets"` |
| `save(ticket)` | `INSERT ...` on create, `UPDATE ...` on update |
| `deleteById(id)` | `DELETE FROM "Tickets" WHERE "TicketID" = ?` |
| `existsById(id)` | `SELECT COUNT(*) > 0 ...` |
| `count()` | `SELECT COUNT(*) FROM "Tickets"` |

### The one derived query

```java
List<Ticket> findByPassenger_UserId(String passengerId);
```

How Spring Data reads the name:

| Piece | Meaning |
|---|---|
| `find` | This method returns entities. |
| `By` | Start of the conditions. |
| `Passenger` | The `passenger` field on `Ticket`. |
| `_UserId` | Go **inside** that `User` and use its `userId` field. |
| `UserId` parameter | The value to match. |

Conceptual JPQL:

```sql
SELECT t
FROM "Tickets" t
WHERE t."PassengerID" = ?;
```

Because `User.userId` is the primary key of `User`, Hibernate can compare the foreign
key column `PassengerID` directly. Conceptually the query means "all tickets whose
passenger's ID equals the argument".

The return type is `List<Ticket>`, not `Optional`, because a passenger can own zero,
one or many tickets. An empty list is a valid answer, not an error.

There is no `ORDER BY`. The database may return rows in any order. If sorted output
were required, the method would be
`findByPassenger_UserIdOrderByIssueTimeDesc`.

---

## 7. Controller endpoints

Base path: `/api/tickets`

| # | Method | Path | Who can call | Purpose | Success |
|---|---|---|---|---|---|
| 1 | `POST` | `/api/tickets` | `PASSENGER`, `TICKET_AGENT`, `ADMIN` | Issue a ticket | `201` |
| 2 | `GET` | `/api/tickets/{id}` | Any logged-in user (passengers: own only) | Read one ticket | `200` |
| 3 | `GET` | `/api/tickets` | `ADMIN`, `TICKET_AGENT` | List all tickets | `200` |
| 4 | `GET` | `/api/tickets/passenger/{passengerId}` | Any logged-in user (passengers: forced to self) | List one passenger's tickets | `200` |
| 5 | `PATCH` | `/api/tickets/{id}/use` | `PASSENGER`, `TICKET_AGENT`, `ADMIN` (passengers: own only) | Mark as used | `200` |
| 6 | `DELETE` | `/api/tickets/{id}` | `ADMIN` | Delete a ticket | `204` |

Note the difference between the role check and the ownership check:

- `@PreAuthorize` answers "does this role use this endpoint at all?"
- `checkOwnership` answers "is this particular row mine?"

`GET /{id}` has no `@PreAuthorize`, so any authenticated role reaches the method, but a
passenger is then checked against the row owner.

---

## 8. Controller line by line

File: `src/main/java/com/metro/ticketing/controller/TicketController.java`

### Imports and class

```java
package com.metro.ticketing.controller;

import com.metro.ticketing.entity.Fare;                     // fare entity
import com.metro.ticketing.entity.Station;                  // station entity
import com.metro.ticketing.entity.Ticket;                   // request + response entity
import com.metro.ticketing.entity.User;                     // ticket owner
import com.metro.ticketing.repository.FareRepository;       // fare lookup
import com.metro.ticketing.repository.StationRepository;    // station lookup
import com.metro.ticketing.repository.TicketRepository;     // ticket database access
import com.metro.ticketing.repository.UserRepository;       // user lookup
import jakarta.validation.Valid;                            // bean validation
import org.springframework.http.HttpStatus;                 // CREATED
import org.springframework.http.ResponseEntity;             // response wrapper
import org.springframework.security.access.AccessDeniedException; // 403 signal
import org.springframework.security.access.prepost.PreAuthorize;  // role checks
import org.springframework.security.core.Authentication;          // logged-in user info
import org.springframework.transaction.annotation.Transactional;  // transactions
import org.springframework.web.bind.annotation.*;                 // mappings

import java.time.LocalDateTime;                             // now / expiry
import java.util.List;                                      // list responses
import java.util.UUID;                                      // random ticket ID

@RestController                                             // JSON responses
@RequestMapping("/api/tickets")                             // base URL
public class TicketController {
```

### Fields and constructor

```java
private final TicketRepository ticketRepository;    // ticket table
private final UserRepository userRepository;        // user table
private final StationRepository stationRepository;  // station table
private final FareRepository fareRepository;        // fare table

public TicketController(TicketRepository ticketRepository,
                        UserRepository userRepository,
                        StationRepository stationRepository,
                        FareRepository fareRepository) {
    this.ticketRepository = ticketRepository;       // injected by Spring
    this.userRepository = userRepository;           // injected by Spring
    this.stationRepository = stationRepository;     // injected by Spring
    this.fareRepository = fareRepository;           // injected by Spring
}
```

Four dependencies, all injected through the only constructor. The controller never
creates them. This is why the class can be tested by passing fake repositories.

### Create ticket

```java
@PostMapping                                                              // POST /api/tickets
@PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT', 'ADMIN')")         // three roles allowed
@Transactional                                                            // one transaction
public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket request,
                                           Authentication authentication) {
    if (request.getSourceStationId() == null || request.getDestStationId() == null) {
        throw new IllegalArgumentException("Source and destination stations are required");
    }
    if (request.getSourceStationId().equals(request.getDestStationId())) {
        throw new IllegalArgumentException("Source and destination stations must be different");
    }

    String passengerId = request.getPassengerId();                        // may be null
    if (isPassenger(authentication)) {                                    // passenger booking
        passengerId = currentUser(authentication).getUserId();            // force self
    }
    if (passengerId == null || passengerId.isBlank()) {                   // agent forgot the ID
        throw new IllegalArgumentException("Passenger ID is required");
    }

    User passenger = userRepository.findById(passengerId)                 // owner must exist
            .orElseThrow(() -> new IllegalArgumentException("Passenger not found"));
    Station source = stationRepository.findById(request.getSourceStationId())
            .orElseThrow(() -> new IllegalArgumentException("Source station not found"));
    Station destination = stationRepository.findById(request.getDestStationId())
            .orElseThrow(() -> new IllegalArgumentException("Destination station not found"));

    Fare fare = fareRepository                                            // find the price row
            .findBySourceStation_StationIdAndDestStation_StationId(
                    request.getSourceStationId(), request.getDestStationId())
            .orElseThrow(() -> new IllegalArgumentException(
                    "Fare not found for this station pair"));

    Ticket ticket = new Ticket();                                         // new entity
    ticket.setTicketId("TKT-" + UUID.randomUUID());                       // server-generated ID
    ticket.setPassenger(passenger);                                       // real User relation
    ticket.setFare(fare);                                                 // real Fare relation
    ticket.setSourceStation(source);                                      // real Station relation
    ticket.setDestStation(destination);                                   // real Station relation
    ticket.setTicketType(request.getTicketType());                        // free text
    ticket.setIssueTime(LocalDateTime.now());                             // issued now
    ticket.setCreatedAt(LocalDateTime.now());                             // created now
    ticket.setIsUsed(false);                                              // new ticket unused
    ticket.setValidUntil(request.getValidUntil() != null                 // client value?
            ? request.getValidUntil()                                     // yes: use it
            : LocalDateTime.now().plusHours(24));                         // no: 24 hours

    return ResponseEntity.status(HttpStatus.CREATED)                      // 201
            .body(ticketRepository.save(ticket));                         // INSERT
}
```

The important rules in order:

1. **Station checks.** Both IDs are required, and they must differ. Null checks come
   first so `.equals` is never called on `null`.
2. **Who is the passenger?**
   - If the caller has `ROLE_PASSENGER`, the server ignores any `passengerId` in the
     body and uses the logged-in user's ID. A passenger cannot buy for someone else.
   - If the caller is `TICKET_AGENT` or `ADMIN`, the body value is used. It must be
     present and non-blank.
3. **Existence checks.** Passenger, source station and destination station must all
   exist. Each gets its own message.
4. **Fare lookup.** The exact directional fare must exist. This is the same query used
   by `FareController.getFareByStations`.
5. **ID generation.** `TKT-<uuid>`, always server-side.
6. **Defaults.** `isUsed = false`, `issueTime` and `createdAt` = now, and `validUntil`
   = request value or now + 24 hours.
7. **No payment.** Booking a ticket does not create a `Payment` row and does not check
   a metro card balance. That is a separate API.

Why build a new `Ticket` instead of saving `request`? The request contains transient
IDs, not managed relations. Saving it directly would leave the required foreign keys
empty and would also let the client choose the primary key. Creating a new object
gives full control.

### Read one ticket

```java
@GetMapping("/{id}")                                            // GET /api/tickets/{id}
@Transactional(readOnly = true)
public ResponseEntity<Ticket> getTicketById(@PathVariable String id,
                                            Authentication authentication) {
    Ticket ticket = ticketRepository.findById(id)                // primary-key lookup
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    checkOwnership(ticket, authentication);                      // passenger must own it
    return ResponseEntity.ok(ticket);                            // 200
}
```

There is no `@PreAuthorize`, so `SecurityConfig` only enforces "must be logged in".
Any authenticated user reaches this method. `checkOwnership` then limits passengers to
their own rows. Agents and admins pass without restriction.

The ownership check runs **before** the response is built, so a passenger never sees
another passenger's data.

### Read all tickets

```java
@GetMapping                                                     // GET /api/tickets
@PreAuthorize("hasAnyRole('ADMIN', 'TICKET_AGENT')")            // staff only
@Transactional(readOnly = true)
public ResponseEntity<List<Ticket>> getAllTickets() {
    return ResponseEntity.ok(ticketRepository.findAll());       // 200 with array
}
```

A passenger cannot list every ticket. They must use the per-passenger endpoint
instead. This prevents one passenger from enumerating other people's tickets.

### Read tickets for one passenger

```java
@GetMapping("/passenger/{passengerId}")                          // GET /api/tickets/passenger/{id}
@Transactional(readOnly = true)
public ResponseEntity<List<Ticket>> getTicketsByPassenger(
        @PathVariable String passengerId,
        Authentication authentication) {
    if (isPassenger(authentication)) {
        passengerId = currentUser(authentication).getUserId();    // ignore the path value
    }
    return ResponseEntity.ok(ticketRepository.findByPassenger_UserId(passengerId));
}
```

For a passenger, the path parameter is **overwritten** with their own ID. So:

- `priya` calls `/api/tickets/passenger/PASS-SEED2` → she still receives only her own
  tickets (`PASS-SEED1`).
- An agent or admin can pass any passenger ID.

There is no check that the passenger ID exists. An unknown ID simply returns an empty
array `[]`.

### Mark a ticket as used

```java
@PatchMapping("/{id}/use")                                         // PATCH /api/tickets/{id}/use
@PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT', 'ADMIN')")  // three roles
@Transactional
public ResponseEntity<Ticket> markTicketAsUsed(@PathVariable String id,
                                               Authentication authentication) {
    Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    checkOwnership(ticket, authentication);                        // passenger must own it

    if (Boolean.TRUE.equals(ticket.getIsUsed())) {                 // already used?
        throw new IllegalArgumentException("Ticket already used");
    }
    if (ticket.getValidUntil() != null
            && ticket.getValidUntil().isBefore(LocalDateTime.now())) { // expired?
        throw new IllegalArgumentException("Ticket has expired");
    }

    ticket.setIsUsed(true);                                        // flip the flag
    return ResponseEntity.ok(ticketRepository.save(ticket));       // UPDATE, 200
}
```

Why `PATCH` and not `PUT`? Because only one field changes. `PUT` suggests replacing the
whole ticket; `PATCH` suggests a partial change. Both would work technically.

The order of checks matters:

1. The ticket must exist.
2. A passenger must own it.
3. It must not already be used.
4. It must not be expired.

A used ticket that is also expired returns `"Ticket already used"` first, because that
check comes first.

`Boolean.TRUE.equals(...)` is null-safe. If `isUsed` is `null`, the expression is
`false`, so an old row with a null flag is treated as unused.

Expiry uses `isBefore(now)`. A ticket whose `validUntil` is exactly `now` is **not**
expired, because `now.isBefore(now)` is false.

### Delete ticket

```java
@DeleteMapping("/{id}")                                            // DELETE /api/tickets/{id}
@PreAuthorize("hasRole('ADMIN')")                                  // admin only
@Transactional
public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
    if (!ticketRepository.existsById(id)) {                        // clear error message
        throw new IllegalArgumentException("Ticket not found");
    }
    ticketRepository.deleteById(id);                               // DELETE row
    return ResponseEntity.noContent().build();                     // 204
}
```

If a `Payment` row points at this ticket, `FK_Payment_Ticket` blocks the delete and the
generic handler returns `500`. Seeded tickets `TKT-SEED1`, `TKT-SEED2` and `TKT-SEED3`
have payments, so they cannot be deleted.

There is no cascade. The schema does not define `ON DELETE CASCADE`.

### Helper: checkOwnership

```java
private void checkOwnership(Ticket ticket, Authentication authentication) {
    if (isPassenger(authentication)
            && !ticket.getPassenger().getUserId()
                    .equals(currentUser(authentication).getUserId())) {
        throw new AccessDeniedException("You can only access your own tickets");
    }
}
```

Read it in three parts:

1. `isPassenger(authentication)` — only passengers are restricted.
2. `ticket.getPassenger().getUserId()` — the owner of the row. This is a lazy
   `@ManyToOne`, but the method runs inside a transaction, so the load works.
3. `currentUser(authentication).getUserId()` — the logged-in user's ID.

If the two IDs differ, it throws `AccessDeniedException`.

`GlobalExceptionHandler` maps that to `403` but **replaces the message** with
`"You do not have permission to access this resource"`. The custom
`"You can only access your own tickets"` message is therefore not what the client sees.

### Helper: isPassenger

```java
private boolean isPassenger(Authentication authentication) {
    return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_PASSENGER"));
}
```

- `getAuthorities()` returns the user's granted authorities.
- `.stream()` turns the collection into a stream so it can be processed.
- `.anyMatch(...)` returns `true` if at least one element matches.
- The authority string is `"ROLE_PASSENGER"`, with the `ROLE_` prefix added by
  `DbUserDetailsService` when it calls `.roles(user.getRole().name())`.

Note: this returns `true` only for the passenger role, and roles are single-valued in
this project (`User.role` is one enum value). A user cannot be both a passenger and an
admin.

### Helper: currentUser

```java
private User currentUser(Authentication authentication) {
    return userRepository.findByContact(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
}
```

`authentication.getName()` returns the username that was used at login. In
`DbUserDetailsService`, the username is set to the user's `Contact` field:

```java
.withUsername(user.getContact())
```

So the lookup is by contact. If the contact no longer exists, the method throws
`"Current user not found"`, which becomes `400`.

This is an extra database query on every ownership check. It is simple and correct, but
it is not the fastest design; caching the user ID in the authentication principal would
avoid it.

---

## 9. One request from start to finish

Story: Priya books a single ticket from Versova (`STN-VSV`) to Ghatkopar (`STN-GHP`).

1. Priya authorizes in Swagger as `priya` / `Passw0rd!`.
2. `DbUserDetailsService` loads her by `Contact = 'priya'` and creates an
   `Authentication` with authority `ROLE_PASSENGER`.
3. Swagger sends `POST /api/tickets` with:

   ```json
   {
     "sourceStationId": "STN-VSV",
     "destStationId": "STN-GHP",
     "ticketType": "SINGLE"
   }
   ```

4. `@PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT', 'ADMIN')")` passes.
5. Jackson builds a `Ticket`. It fills the three transient IDs and `ticketType`.
   `validUntil` is absent, so it stays `null`.
6. `@Valid` checks `ticketType` with `@NotBlank`. `"SINGLE"` passes.
7. Station checks pass: both IDs present and different.
8. `isPassenger(authentication)` is `true`. The controller ignores any `passengerId` in
   the body and sets `passengerId = currentUser(...).getUserId()`, which is
   `PASS-SEED1`.
9. `userRepository.findById("PASS-SEED1")` finds Priya.
10. `stationRepository.findById("STN-VSV")` finds Versova.
11. `stationRepository.findById("STN-GHP")` finds Ghatkopar.
12. `fareRepository.findBySourceStation_StationIdAndDestStation_StationId("STN-VSV",
    "STN-GHP")` finds the seeded fare `FARE-STN-VSV-STN-GHP` with `baseFare = 40.00`.
    If this row did not exist, the request would fail with
    `"Fare not found for this station pair"`.
13. A new `Ticket` is created with ID
    `TKT-7d3a1f90-2c4b-4e88-a6d5-91b0c2f4e777`.
14. Relations are set: passenger Priya, fare 40.00, source Versova, destination
    Ghatkopar.
15. `ticketType = "SINGLE"`, `issueTime = now`, `createdAt = now`, `isUsed = false`.
16. `validUntil` is absent, so it becomes `now + 24 hours`.
17. `save` inserts the row with the four foreign keys.
18. Serialization includes `passenger`, `fare`, `sourceStation` and `destStation` as
    nested objects (lazy loads happen inside the transaction / open-in-view). The
    transient IDs are hidden. Priya's `password` is hidden too, because `User.password`
    is `WRITE_ONLY`.
19. Response: `201 Created` with the ticket JSON.

Then Priya shows the ticket at the gate:

20. She calls `PATCH /api/tickets/TKT-7d3a.../use`.
21. The ticket is found. `checkOwnership` compares owner `PASS-SEED1` with her ID; they
    match.
22. `isUsed` is `false`, so the "already used" check passes.
23. `validUntil` is in the future, so the expiry check passes.
24. `isUsed` becomes `true`, `save` runs an `UPDATE`, and the response is `200`.
25. If she calls the endpoint again, step 22 throws `"Ticket already used"` → `400`.

---

## 10. JSON request and response examples

### Passenger booking request

```http
POST /api/tickets
Authorization: Basic cHJpeWE6UGFzc3cwcmQh
Content-Type: application/json
```

```json
{
  "sourceStationId": "STN-VSV",
  "destStationId": "STN-GHP",
  "ticketType": "SINGLE"
}
```

Any `passengerId` sent here is ignored for a passenger. The server uses the logged-in
user.

### Agent booking request

```http
POST /api/tickets
Authorization: Basic YWdlbnQxOlBhc3N3MHJkIQ==
Content-Type: application/json
```

```json
{
  "passengerId": "PASS-SEED2",
  "sourceStationId": "STN-AND",
  "destStationId": "STN-GHP",
  "ticketType": "ROUND_TRIP",
  "validUntil": "2026-11-05T18:00:00"
}
```

The agent must send `passengerId`. Without it, the request fails with
`"Passenger ID is required"`.

### Request field table

| Field | Required | Meaning |
|---|---|---|
| `passengerId` | Yes for agent/admin; ignored for passenger | Owner's `UserId`. |
| `sourceStationId` | Yes | Must exist in `Stations` and differ from destination. |
| `destStationId` | Yes | Must exist in `Stations` and differ from source. |
| `ticketType` | Yes | Non-blank text, up to 50 characters. |
| `validUntil` | No | ISO-8601 local date-time. Defaults to now + 24 hours. |
| `ticketId` | — | Ignored; the server generates `TKT-<uuid>`. |
| `fare` | — | Read-only; the server looks it up. |
| `passenger` | — | Read-only; the server looks it up. |
| `sourceStation` / `destStation` | — | Read-only; the server looks them up. |
| `isUsed` | — | Ignored; new tickets are always unused. |
| `issueTime` / `createdAt` | — | Ignored; the server sets them to now. |

### Create response (`201`)

```json
{
  "ticketId": "TKT-7d3a1f90-2c4b-4e88-a6d5-91b0c2f4e777",
  "passenger": {
    "userId": "PASS-SEED1",
    "firstName": "Priya",
    "lastName": "Sharma",
    "role": "PASSENGER",
    "contact": "priya",
    "registrationDate": "2026-04-29",
    "isActive": true,
    "hireDate": null,
    "station": null
  },
  "fare": {
    "fareId": "FARE-STN-VSV-STN-GHP",
    "sourceStation": { "stationId": "STN-VSV", "stationCode": "VSV" },
    "destStation": { "stationId": "STN-GHP", "stationCode": "GHP" },
    "baseFare": 40.00
  },
  "sourceStation": {
    "stationId": "STN-VSV",
    "stationCode": "VSV",
    "latitude": 19.1354,
    "longitude": 72.8209,
    "address": "Versova - JP Road, Andheri West",
    "openedDate": "2013-03-01",
    "lineColor": "Blue",
    "isActive": true
  },
  "destStation": {
    "stationId": "STN-GHP",
    "stationCode": "GHP",
    "latitude": 19.0880,
    "longitude": 72.9096,
    "address": "Ghatkopar - Ghatkopar West, Station Road Junction",
    "openedDate": "2014-01-20",
    "lineColor": "Blue",
    "isActive": true
  },
  "ticketType": "SINGLE",
  "validUntil": "2026-10-30T09:15:00",
  "isUsed": false,
  "issueTime": "2026-10-29T09:15:00",
  "createdAt": "2026-10-29T09:15:00"
}
```

| Response field | Meaning |
|---|---|
| `ticketId` | Generated primary key. |
| `passenger` | Full owner object. Password is absent (`WRITE_ONLY`). |
| `fare` | Full fare object, including `baseFare`. |
| `sourceStation` / `destStation` | Full station objects. |
| `ticketType` | Text from the request. |
| `validUntil` | Request value or now + 24 hours. |
| `isUsed` | `false` for a new ticket. |
| `issueTime` / `createdAt` | Server time at booking. |
| `passengerId`, `sourceStationId`, `destStationId` | Absent; they are `WRITE_ONLY`. |

### Use response (`200`)

```json
{
  "ticketId": "TKT-7d3a1f90-2c4b-4e88-a6d5-91b0c2f4e777",
  "ticketType": "SINGLE",
  "validUntil": "2026-10-30T09:15:00",
  "isUsed": true,
  "issueTime": "2026-10-29T09:15:00",
  "createdAt": "2026-10-29T09:15:00",
  "passenger": { "userId": "PASS-SEED1", "firstName": "Priya" },
  "fare": { "fareId": "FARE-STN-VSV-STN-GHP", "baseFare": 40.00 },
  "sourceStation": { "stationId": "STN-VSV" },
  "destStation": { "stationId": "STN-GHP" }
}
```

Only `isUsed` changes.

### List my tickets

```http
GET /api/tickets/passenger/PASS-SEED1
Authorization: Basic cHJpeWE6UGFzc3cwcmQh
```

```json
[
  {
    "ticketId": "TKT-SEED1",
    "ticketType": "SINGLE",
    "validUntil": "2026-10-29T11:15:00",
    "isUsed": true
  },
  {
    "ticketId": "TKT-SEED3",
    "ticketType": "SINGLE",
    "validUntil": "2026-10-30T05:15:00",
    "isUsed": false
  }
]
```

Rows are shortened here; the real objects contain the relations.

### Error examples

```json
{ "error": "Ticket already used" }
```

```json
{ "error": "Ticket has expired" }
```

```json
{ "error": "You do not have permission to access this resource" }
```

```json
{ "ticketType": "Ticket type is required" }
```

---

## 11. Error table

| Situation | Status | Body |
|---|---|---|
| Source or destination station missing | `400` | `{"error":"Source and destination stations are required"}` |
| Same source and destination | `400` | `{"error":"Source and destination stations must be different"}` |
| Agent/admin did not send `passengerId` | `400` | `{"error":"Passenger ID is required"}` |
| Passenger ID does not exist | `400` | `{"error":"Passenger not found"}` |
| Source station does not exist | `400` | `{"error":"Source station not found"}` |
| Destination station does not exist | `400` | `{"error":"Destination station not found"}` |
| No fare for that direction | `400` | `{"error":"Fare not found for this station pair"}` |
| `ticketType` missing or blank | `400` | `{"ticketType":"Ticket type is required"}` |
| Ticket ID does not exist | `400` | `{"error":"Ticket not found"}` |
| Passenger reads another passenger's ticket | `403` | `{"error":"You do not have permission to access this resource"}` |
| Passenger calls `GET /api/tickets` | `403` | `{"error":"You do not have permission to access this resource"}` |
| `PATCH .../use` on an already used ticket | `400` | `{"error":"Ticket already used"}` |
| `PATCH .../use` after `validUntil` | `400` | `{"error":"Ticket has expired"}` |
| No auth / bad password | `401` | Spring Security default body |
| Non-admin calls `DELETE` | `403` | `{"error":"You do not have permission to access this resource"}` |
| Delete a ticket that has a payment | `500` | `{"error":"An unexpected error occurred: ..."}` |
| Malformed JSON body | `500` | `{"error":"An unexpected error occurred: ..."}` |

Notes:

1. **Not found is `400`.** The controllers throw `IllegalArgumentException`, and
   `GlobalExceptionHandler` maps it to `400`. Only `ResourceNotFoundException` maps to
   `404`, and this group never throws it.
2. **Malformed JSON becomes `500`.** Spring's `HttpMessageNotReadableException` is
   caught by the catch-all `Exception` handler. A cleaner design would map it to `400`.
3. **`403` messages are generic.** `GlobalExceptionHandler` ignores the original
   `AccessDeniedException` message and always writes
   `"You do not have permission to access this resource"`.

---

## 12. Relationships to other groups

```
User (1) ───< (many) Tickets >─── (1) Fares
                        │
                        ├─── (1) Stations   (source)
                        └─── (1) Stations   (destination)

Tickets (1) ───< (many) Payment
```

| Group | Relationship |
|---|---|
| **User** (`01-USER.md`) | `Ticket.passenger` is `@ManyToOne`. One user owns many tickets. |
| **Auth** (`02-AUTH-AND-ROLE.md`) | Provides `Authentication`, the role check, and the current user lookup. |
| **Station** (`03-STATION.md`) | Two `@ManyToOne` relations: source and destination. |
| **Fare** (`08-FARE.md`) | `Ticket.fare` is `@ManyToOne`. The ticket price is read through this link. |
| **Payment** (`10-PAYMENT.md`) | `Payment.ticket` points here. No cascade, so a ticket with payments cannot be deleted. |
| **MetroCard** (`11-METROCARD.md`) | No direct link. Card balance is not checked during booking. |
| **Schedule** (`07-SCHEDULE.md`) | No link. Tickets are not tied to a specific departure. |
| **Maintenance** (`12-MAINTENANCE-ISSUE.md`) | No link. |

Three consequences worth remembering:

1. **No price snapshot.** `Ticket` stores `FareID`, not the amount. If an admin edits
   the fare, existing tickets show the new `baseFare`.
2. **No payment automation.** Booking returns a ticket but does not create a payment.
   `PaymentController` must be called separately, and that endpoint is a different file.
3. **Deleting a ticket is restricted.** The payment foreign key blocks deletion of any
   ticket that was paid for.

---

## 13. Swagger test steps

1. Open `http://localhost:8080/swagger-ui/index.html`.
2. Click **Authorize** and enter `priya` / `Passw0rd!`.
3. Open **ticket-controller**.
4. Call `GET /api/tickets/passenger/PASS-SEED1`. You should see Priya's seeded tickets.
5. Call `GET /api/tickets/passenger/PASS-SEED2` while still logged in as Priya. You
   should still see **only Priya's** tickets, because the controller overwrites the ID.
6. Call `POST /api/tickets` with:

   ```json
   {
     "sourceStationId": "STN-VSV",
     "destStationId": "STN-GHP",
     "ticketType": "SINGLE"
   }
   ```

7. Expect `201`. Copy the `ticketId` and check `isUsed` is `false` and `validUntil` is
   about 24 hours ahead.
8. Call `GET /api/tickets/{ticketId}`. Expect `200`.
9. Call `GET /api/tickets` as Priya. Expect `403`.
10. Call `PATCH /api/tickets/{ticketId}/use`. Expect `200` and `"isUsed": true`.
11. Call `PATCH` again. Expect `400` with `"Ticket already used"`.
12. Create another ticket with `"validUntil": "2020-01-01T00:00:00"`. Expect `201`
    (creation does not check expiry).
13. Call `PATCH .../use` on it. Expect `400` with `"Ticket has expired"`.
14. Log out. Authorize as `arjun` / `Passw0rd!`.
15. Call `GET /api/tickets/{priyaTicketId}`. Expect `403`, because Arjun does not own
    it.
16. Log out. Authorize as `agent1` / `Passw0rd!`.
17. Call `POST /api/tickets` with a `passengerId` and a different journey. Expect `201`.
18. Call `GET /api/tickets`. Expect `200` with every ticket.
19. Log out. Authorize as `admin` / `admin123`.
20. Call `DELETE /api/tickets/{newTicketId}`. Expect `204`.
21. Try `DELETE /api/tickets/TKT-SEED1`. Expect `500`, because `TKT-SEED1` has a seeded
    payment.

Use a private window when switching users; browsers cache Basic credentials.

---

## 14. How to explain this in 2 minutes

> "The Ticket group is the booking step. A ticket connects four things: a passenger, a
> fare, a source station and a destination station. The `Tickets` table has foreign keys
> to all four. There is no price column on the ticket; the price lives in the linked
> `Fares` row.
>
> In the entity, those four relations are `@ManyToOne` with `LAZY` loading and
> `READ_ONLY` JSON access. That means responses show the full passenger, fare and
> station objects, but a client cannot send nested objects to replace them. The client
> sends simple IDs instead. Those IDs are `@Transient` fields, so they are not columns,
> and they are `WRITE_ONLY`, so they do not appear in responses.
>
> `ticketType` is a plain non-blank string. There is no enum of allowed types. `isUsed`
> is `false` by default, `issueTime` and `createdAt` are set to now, and `validUntil`
> is either the request value or now plus 24 hours.
>
> The repository is a `JpaRepository` plus one derived query,
> `findByPassenger_UserId`. The underscore means 'go into the passenger relation and
> compare its userId'. It returns a `List`, because a passenger can have zero, one or
> many tickets.
>
> The controller has six endpoints. Booking allows passengers, ticket agents and
> admins. There is an important ownership rule: if the caller is a passenger, the
> server ignores any `passengerId` in the body and uses the logged-in user's ID. So a
> passenger cannot book for someone else. An agent or admin must send `passengerId`.
>
> Booking also looks up the fare for the exact direction from source to destination.
> If there is no fare row, the request fails with 'Fare not found for this station
> pair'. That is the contract with the Fare group.
>
> Reading a single ticket and marking it used both call `checkOwnership`. Passengers
> can only touch their own rows; agents and admins are not restricted. The ownership
> check compares the ticket's passenger ID with the logged-in user's ID. If they
> differ, it throws `AccessDeniedException`, which the exception handler turns into
> `403`.
>
> `PATCH /api/tickets/{id}/use` is the gate check. It rejects a ticket that is already
> used, and it rejects an expired ticket. It then sets `isUsed` to true. Marking used
> is not idempotent: the second call returns `400` 'Ticket already used'.
>
> Two honest limitations: booking does not create a payment or check card balance, and
> deleting a ticket that has a payment fails with a `500` because there is no cascade.
> Also, 'not found' comes back as `400`, not `404`, because the controllers throw
> `IllegalArgumentException`."

---

## 15. Beginner Q&A

**Q1. What is the difference between a `Fare` and a `Ticket`?**
`Fare` is the price rule for a station pair. `Ticket` is an issued journey for one
passenger, pointing at that fare. One fare can be used by many tickets.

**Q2. Why does `Ticket` not have an `amount` column?**
The price is reached through `FareID`. The design avoids duplicating data. The
trade-off is that historical prices are not preserved: editing a fare changes what old
tickets display.

**Q3. Why is `ticketId` a `String`?**
Seed tickets use `TKT-SEED1`; new tickets use `TKT-<uuid>`. Strings hold both.

**Q4. Who generates `ticketId`?**
The server, with `"TKT-" + UUID.randomUUID()`. A `ticketId` in the request is ignored.

**Q5. What does `@Transient` do here?**
It keeps `passengerId`, `sourceStationId` and `destStationId` out of the database. They
are only used to carry request values into the controller.

**Q6. Why are those fields also `WRITE_ONLY`?**
So they do not appear in responses, where the full `passenger`, `sourceStation` and
`destStation` objects are already present.

**Q7. Why are the relations `READ_ONLY`?**
To stop a client from replacing the owner or fare with a nested object. The controller
must resolve the IDs itself, which also proves the rows exist.

**Q8. Can a passenger book a ticket for a friend?**
No. For `ROLE_PASSENGER`, the controller overwrites `passengerId` with the logged-in
user's ID. The body value is ignored.

**Q9. How does an agent book for a passenger?**
Send `passengerId` in the body. Agents and admins keep the body value. If it is missing
or blank, the request fails with `"Passenger ID is required"`.

**Q10. What happens if there is no fare for the chosen direction?**
The request fails with `400` and `"Fare not found for this station pair"`. Fares are
directional, so A → B and B → A are different rows.

**Q11. What if `validUntil` is not sent?**
The ticket is valid for 24 hours from creation:
`LocalDateTime.now().plusHours(24)`.

**Q12. Can a ticket be created already expired?**
Yes. Booking does not check the date. The expiry check happens only when the ticket is
marked used.

**Q13. Why does marking a used ticket return `400` instead of doing nothing?**
Because the method explicitly throws `IllegalArgumentException("Ticket already
used")`. The operation is not idempotent. A design choice could make it return `200`
instead, but this project does not.

**Q14. Why is `Boolean.TRUE.equals(...)` used instead of `ticket.getIsUsed()`?**
`getIsUsed()` returns a `Boolean` that could be `null` for old rows. Unboxing `null`
throws `NullPointerException`. `Boolean.TRUE.equals(x)` safely returns `false` for
`null`.

**Q15. Is a ticket valid at exactly `validUntil`?**
Yes. The check is `validUntil.isBefore(now)`, which is false when both are equal.

**Q16. Why can Priya call `/api/tickets/passenger/PASS-SEED2` and still see her own
tickets?**
Because the method replaces `passengerId` with her own ID whenever the caller is a
passenger. The path value is ignored for passengers.

**Q17. Why can't a passenger call `GET /api/tickets`?**
`@PreAuthorize("hasAnyRole('ADMIN', 'TICKET_AGENT')")` blocks it. This prevents
enumeration of every ticket in the system.

**Q18. How does `checkOwnership` know who is logged in?**
It receives Spring's `Authentication`. `authentication.getName()` is the login
username, which is the user's `Contact`. `currentUser` looks that contact up in the
database and returns the `User`.

**Q19. What status does a passenger get when reading someone else's ticket?**
`403`, from `AccessDeniedException`. The message is the generic
`"You do not have permission to access this resource"`.

**Q20. Does booking create a payment?**
No. The ticket endpoint only writes a `Tickets` row. `Payment` has its own controller.
This means a ticket can exist without any payment record.

**Q21. Why can't I delete `TKT-SEED1`?**
`Payment` rows point at it, and `FK_Payment_Ticket` has no cascade. The database
rejects the delete, and the generic handler returns `500`.

**Q22. Why is a missing ticket a `400` and not a `404`?**
The controller throws `IllegalArgumentException`, and `GlobalExceptionHandler` maps
that to `400`. `ResourceNotFoundException` would map to `404`, but this controller does
not use it.

**Q23. What does `@Transactional` add here?**
It wraps the method's database work in one transaction. If an exception is thrown, all
changes roll back. It also keeps lazy relations loadable while the method runs.

**Q24. Can the same passenger book the same journey twice?**
Yes. There is no uniqueness rule. Each booking creates a separate `TKT-<uuid>`.

---

## 16. Glossary

| Term | Meaning |
|---|---|
| **`AccessDeniedException`** | Spring exception meaning the user is not allowed; handled as `403`. |
| **`Authentication`** | Object holding the logged-in user's name and authorities. |
| **Authority** | A permission string such as `ROLE_PASSENGER`. |
| **Bean** | Object managed by Spring. |
| **`Boolean` wrapper** | Object form of `boolean` that can also be `null`. |
| **Controller** | Class that maps HTTP requests to Java methods. |
| **CRUD** | Create, Read, Update, Delete. |
| **Derived query** | Query generated from a repository method name. |
| **Entity** | Java class mapped to a table. |
| **Foreign key** | Column referencing another table's primary key. |
| **`hasAnyRole`** | Access rule allowing any one of several roles. |
| **`hasRole`** | Access rule requiring one specific role. |
| **Idempotent** | Repeating the call leaves the same end state. |
| **JPA** | Java Persistence API. |
| **JSON** | Text format for structured data. |
| **Lazy loading** | Loading a relation only when first used. |
| **Open-in-view** | Keeps the Hibernate session open during JSON serialization. |
| **Ownership check** | Verifying the row belongs to the logged-in user. |
| **`PATCH`** | HTTP method for a partial update. |
| **Primary key** | Column that uniquely identifies a row. |
| **Principal** | The identity behind an `Authentication`. |
| **`ROLE_` prefix** | Prefix Spring expects for role-based authority strings. |
| **Serialization** | Object → JSON. |
| **Spring Boot** | Framework that starts and wires the application. |
| **Status code** | Numeric HTTP result such as `201`, `400`, `403`. |
| **Transaction** | Unit of database work that commits or rolls back as a whole. |
| **UUID** | Random 128-bit identifier written with dashes. |
| **Validation** | Checking input rules before using data. |

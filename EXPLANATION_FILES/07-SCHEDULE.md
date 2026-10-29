# 07 — Schedule Group

> **Analogy.** Think of the timetable board hanging on a metro platform. Each row says:
> "Train M1 leaves Versova at 06:00 and reaches Ghatkopar at 06:46."
> One row is a **Schedule**. The train is a real train, the route is a real line, and the
> row only records *when* that train runs on that line. A train cannot be in two places
> at the same time, so the system also checks that two rows for the same train do not
> overlap in time. That is exactly what this group does.

---

## 1. What a Schedule represents

A `Schedule` is **one planned trip**:

- which train (`TrainID`)
- on which route (`RouteID`)
- on which day label (`DayOfWeek`, for example `DAILY` or `MONDAY`)
- departure date and time
- arrival date and time
- the date range in which this timetable row is valid (`ValidFrom`, `ValidTo`)
- whether the row is currently active

It does **not** store stations. The stations come from the route.
It does **not** store ticket prices. Prices live in the `Fares` group.

A real seeded row looks like this:

| ScheduleID | TrainID | RouteID | DayOfWeek | Departure | Arrival |
|---|---|---|---|---|---|
| `SCH-001` | `TRN-101` | `RT-L1` | `DAILY` | today 06:00 | today 06:46 |

---

## 2. Words you need

Read this table once. Every term below appears later in this file.

| Word | Simple meaning |
|---|---|
| **Spring Boot** | A Java framework that starts a web server and connects classes together for you. |
| **Bean** | An object that Spring creates and manages. |
| **Dependency injection** | Giving a class the objects it needs instead of letting it create them with `new`. |
| **Constructor injection** | Dependency injection done through the constructor. |
| **Annotation** | A word starting with `@` that adds instructions to Java code. |
| **JPA** | Java Persistence API: the standard for mapping Java objects to database tables. |
| **Hibernate** | The library that actually implements JPA and writes the SQL. |
| **Entity** | A Java class mapped to one database table. |
| **Repository** | An interface that reads and writes entities; Spring writes the implementation. |
| **Controller** | A class that receives HTTP requests and returns HTTP responses. |
| **REST API** | A web API where URLs represent resources and HTTP methods represent actions. |
| **HTTP** | The request/response protocol used by browsers and APIs. |
| **Endpoint** | One URL plus one HTTP method, for example `POST /api/schedules`. |
| **JSON** | Text format for data, using `{ "key": value }`. |
| **Serialization** | Turning a Java object into JSON. |
| **Deserialization** | Turning JSON into a Java object. |
| **`@Entity`** | Marks a class as a JPA entity (a table). |
| **`@Table`** | Gives the exact database table name. |
| **`@Id`** | Marks the primary-key field. |
| **`@Column`** | Maps a field to a column and sets its name and length. |
| **Primary key** | The column that uniquely identifies a row. |
| **Foreign key** | A column that points to the primary key of another table. |
| **`@ManyToOne`** | Many rows of this table can point to one row of another table. |
| **`@JoinColumn`** | Names the foreign-key column used by a relation. |
| **`FetchType.LAZY`** | Do not load the related row until somebody actually reads it. |
| **`@Transient`** | "Not a database column." JPA ignores this field. |
| **`@JsonProperty`** | Controls how Jackson (the JSON library) reads or writes a field. |
| **`READ_ONLY`** | The field appears in responses but is ignored in requests. |
| **`WRITE_ONLY`** | The field is accepted in requests but hidden from responses. |
| **`@JsonIgnoreProperties`** | Tells Jackson to skip named internal properties. |
| **`hibernateLazyInitializer`** | A fake property Hibernate adds to lazy proxies; we hide it. |
| **`@NotNull`** | Validation: the value must not be `null`. |
| **`@Valid`** | "Run the validation annotations on this object." |
| **`@RequestBody`** | "Build this parameter from the JSON request body." |
| **`@PathVariable`** | "Take this parameter from the URL path." |
| **`@RequestParam`** | "Take this parameter from the query string." |
| **`@RestController`** | A controller whose return values become the response body. |
| **`@RequestMapping`** | Sets the base URL for every method in the class. |
| **`@PostMapping`** | Handles HTTP `POST` (create). |
| **`@PutMapping`** | Handles HTTP `PUT` (full update). |
| **`@GetMapping`** | Handles HTTP `GET` (read). |
| **`@DeleteMapping`** | Handles HTTP `DELETE` (remove). |
| **`@PreAuthorize`** | Checks the logged-in user's role before the method runs. |
| **`hasRole('ADMIN')`** | Only users with role `ADMIN` may call the method. |
| **`@Transactional`** | All database work in the method succeeds or fails as one unit. |
| **`readOnly = true`** | A hint that the transaction will not modify data. |
| **`ResponseEntity`** | A response wrapper that lets you set status, headers and body. |
| **`HttpStatus.CREATED`** | HTTP status `201`, meaning "a new resource was created". |
| **`Optional`** | A box that either contains a value or is empty. |
| **`orElseThrow`** | Open the `Optional`; throw an exception if it is empty. |
| **`JpaRepository`** | Spring Data interface with ready-made CRUD methods. |
| **CRUD** | Create, Read, Update, Delete. |
| **`@Query`** | Supplies a JPQL query written by hand. |
| **`@Param`** | Gives a name to a method parameter used inside a query. |
| **JPQL** | Java Persistence Query Language: SQL-like, but it uses class and field names. |
| **Derived query** | A repository method whose SQL is generated from the method name. |
| **`UUID`** | A long random identifier that is practically unique. |
| **`LocalDate`** | A date without a time, for example `2026-10-29`. |
| **`LocalDateTime`** | A date plus a time, for example `2026-10-29T09:00`. |
| **`IllegalArgumentException`** | Exception used here for bad input or missing records. |
| **Overlap** | Two time ranges that share at least one moment. |

---

## 3. Files in this group

| Role | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/Schedule.java` |
| Controller | `src/main/java/com/metro/ticketing/controller/ScheduleController.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/ScheduleRepository.java` |
| Table definition | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Seed data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` (`seedSchedules`, `schedule`) |
| Related entities | `Train.java`, `Route.java`, `Station.java` |

There is **no service class** in this project. The controller contains the business logic.
This is a deliberate design choice: fewer layers, easier to explain.

---

## 4. Database table

### SQL from V1

```sql
CREATE TABLE IF NOT EXISTS "Schedule" (
    "ScheduleID" VARCHAR(50) PRIMARY KEY,
    "TrainID" VARCHAR(50) NOT NULL,
    "RouteID" VARCHAR(50) NOT NULL,
    "DayOfWeek" VARCHAR(20),
    "ScheduledDeparture" TIMESTAMP,
    "ScheduledArrival" TIMESTAMP,
    "ValidFrom" DATE,
    "ValidTo" DATE,
    "isActive" BOOLEAN DEFAULT TRUE,
    "CreatedAt" TIMESTAMP,
    CONSTRAINT "FK_Schedule_Train" FOREIGN KEY ("TrainID") REFERENCES "Trains" ("TrainID"),
    CONSTRAINT "FK_Schedule_Route" FOREIGN KEY ("RouteID") REFERENCES "Route" ("RouteID")
);
```

### Column-by-column meaning

| Column | Type | Meaning |
|---|---|---|
| `ScheduleID` | `VARCHAR(50)` | Primary key. New ones look like `SCH-<uuid>`; seed rows are `SCH-001`…`SCH-008`. |
| `TrainID` | `VARCHAR(50)` | Foreign key to `Trains`. Which train performs the trip. Required. |
| `RouteID` | `VARCHAR(50)` | Foreign key to `Route`. Which line the trip follows. Required. |
| `DayOfWeek` | `VARCHAR(20)` | Free text label: `DAILY`, `MONDAY`, `SATURDAY`, and so on. Nullable. |
| `ScheduledDeparture` | `TIMESTAMP` | Planned departure date and time. |
| `ScheduledArrival` | `TIMESTAMP` | Planned arrival date and time. |
| `ValidFrom` | `DATE` | First date on which this timetable row applies. |
| `ValidTo` | `DATE` | Last date on which this timetable row applies. |
| `isActive` | `BOOLEAN` | Soft on/off switch. Default `TRUE`. |
| `CreatedAt` | `TIMESTAMP` | When the row was created. |

### Important database facts

1. The table has **no unique constraint** on `(TrainID, ScheduledDeparture)`.
2. The database therefore **does not** prevent two overlapping trips for the same train.
3. The overlap rule is enforced in **Java**, inside `ScheduleController`, with a query.
4. `DayOfWeek` is a plain `String`, not an enum. The API accepts any text.
5. `ValidFrom` and `ValidTo` exist in the table, but the controller never writes them.
   Only `DataSeeder` fills them. This is current behaviour, not a bug in the mapping.

---

## 5. Entity line by line

File: `src/main/java/com/metro/ticketing/entity/Schedule.java`

### Package and imports

```java
package com.metro.ticketing.entity;                    // this class lives in the entity package

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // hide Hibernate internals in JSON
import com.fasterxml.jackson.annotation.JsonProperty;         // control read/write JSON access
import jakarta.persistence.*;                                 // @Entity, @Id, @Column, @ManyToOne, ...
import jakarta.validation.constraints.NotNull;                 // @NotNull validation

import java.time.LocalDate;        // date only
import java.time.LocalDateTime;    // date + time
```

`jakarta.persistence.*` is a wildcard import: it imports every annotation from the
Jakarta Persistence package (`@Entity`, `@Table`, `@Id`, `@Column`, `@ManyToOne`,
`@JoinColumn`, `FetchType`, `@Transient`).

### Class declaration

```java
@Entity                                                              // this class is a JPA entity -> one table
@Table(name = "\"Schedule\"")                                        // exact table name is Schedule (capital S)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})       // do not output Hibernate proxy fields
public class Schedule {
```

Why `"\"Schedule\""`? Java needs the inner quotes escaped. The database receives
`"Schedule"` with double quotes. PostgreSQL folds unquoted names to lowercase, so the
quotes are required to keep the capital `S`.

Why `@JsonIgnoreProperties`? When a lazy relation is loaded, Hibernate may return a
subclass (a proxy) that has extra properties named `hibernateLazyInitializer` and
`handler`. Without this annotation those internals could appear in the JSON.

### Primary key

```java
@Id                                                          // this field is the primary key
@Column(name = "\"ScheduleID\"", length = 50)                // maps to column ScheduleID, max 50 chars
private String scheduleId;
```

The type is `String`, not a number, because IDs are readable text such as `SCH-001`
or `SCH-<uuid>`.

### Train relation

```java
@JsonProperty(access = JsonProperty.Access.READ_ONLY)        // show train in responses, ignore it in requests
@ManyToOne(fetch = FetchType.LAZY)                           // many schedules can use one train
@JoinColumn(                                                 // foreign-key column in this table
    name = "\"TrainID\"",                                    // column name in Schedule
    referencedColumnName = "\"TrainID\"",                    // column in Trains that it points to
    nullable = false)                                        // database column is NOT NULL
private Train train;
```

`@ManyToOne` means: many schedule rows may point to the same train, but each schedule
points to exactly one train.

`FetchType.LAZY` means Hibernate does not run a second `SELECT` for the train until
some code calls `getTrain()` or Jackson serializes it.

### Route relation

```java
@JsonProperty(access = JsonProperty.Access.READ_ONLY)        // visible in response, ignored in request
@ManyToOne(fetch = FetchType.LAZY)                           // many schedules can use one route
@JoinColumn(
    name = "\"RouteID\"",                                    // FK column in Schedule
    referencedColumnName = "\"RouteID\"",                    // PK column in Route
    nullable = false)
private Route route;
```

### Day label

```java
@Column(name = "\"DayOfWeek\"", length = 20)   // maps to DayOfWeek, max 20 characters
private String dayOfWeek;
```

There is no validation on this field. `"MONDAY"`, `"DAILY"` and even `"Funday"` are
accepted. The value is descriptive only; the overlap check uses real timestamps.

### Transient request field for train

```java
@Transient                                                   // NOT a database column
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)       // accepted in requests, hidden in responses
private String trainId;
```

This field exists so a client can send `"trainId": "TRN-101"` instead of building a
whole nested `Train` object. The controller reads this value, looks up the real
`Train`, and sets the `train` relation. It is never saved to the database.

### Transient request field for route

```java
@Transient                                                   // NOT a database column
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)       // request only
private String routeId;
```

Same idea: the client sends `"routeId": "RT-L1"`.

**Why both a relation and an ID field?**

| Need | Field used |
|---|---|
| Store the foreign key in the database | `train`, `route` |
| Let the client send a simple ID | `trainId`, `routeId` |
| Show the full train/route in the response | `train`, `route` |
| Keep the response clean | `trainId`, `routeId` are `WRITE_ONLY` |

### Departure time

```java
@NotNull(message = "Scheduled departure is required")   // validation message if null
@Column(name = "\"ScheduledDeparture\"")                // maps to ScheduledDeparture
private LocalDateTime scheduledDeparture;
```

`@NotNull` is checked by Spring **before** the controller body runs, because the
parameter is marked `@Valid`. If it fails, the request never reaches your code.

### Arrival time

```java
@NotNull(message = "Scheduled arrival is required")     // arrival must be present
@Column(name = "\"ScheduledArrival\"")                  // maps to ScheduledArrival
private LocalDateTime scheduledArrival;
```

`@NotNull` only checks "not null". It does **not** check that arrival is after
departure. The controller's `validateTimes` method does that.

### Validity dates

```java
@Column(name = "\"ValidFrom\"")   // first valid date
private LocalDate validFrom;

@Column(name = "\"ValidTo\"")     // last valid date
private LocalDate validTo;
```

These are `LocalDate`, because a timetable is valid for whole days, not for a time of
day. The controller does not set them; seeded data does.

### Active flag

```java
@Column(name = "\"isActive\"")     // maps to isActive
private Boolean isActive = true;   // new Java objects start active
```

`Boolean` (wrapper) is used instead of `boolean` (primitive) so the value can be
`null`. That matters on update: the controller only changes it when the request
contains a value.

The `= true` is a Java default. The database has its own `DEFAULT TRUE`.

### Created timestamp

```java
@Column(name = "\"CreatedAt\"")                          // maps to CreatedAt
private LocalDateTime createdAt = LocalDateTime.now();  // set when the object is created in Java
```

### Accessors

The file ends with normal getters and setters. They are the only way the controller
and Jackson read or write the private fields.

```java
public String getScheduleId() { return scheduleId; }                 // read the ID
public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; } // write the ID

public Train getTrain() { return train; }                           // read the lazy train
public void setTrain(Train train) { this.train = train; }           // attach a real Train entity

public Route getRoute() { return route; }                           // read the lazy route
public void setRoute(Route route) { this.route = route; }           // attach a real Route entity

public String getDayOfWeek() { return dayOfWeek; }                  // read the day label
public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

public LocalDateTime getScheduledDeparture() { return scheduledDeparture; }
public void setScheduledDeparture(LocalDateTime scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }

public LocalDateTime getScheduledArrival() { return scheduledArrival; }
public void setScheduledArrival(LocalDateTime scheduledArrival) { this.scheduledArrival = scheduledArrival; }

public LocalDate getValidFrom() { return validFrom; }
public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

public LocalDate getValidTo() { return validTo; }
public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

public Boolean getIsActive() { return isActive; }                   // note: getIsActive, not isActive()
public void setIsActive(Boolean isActive) { this.isActive = isActive; }

public LocalDateTime getCreatedAt() { return createdAt; }
public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

public String getTrainId() { return trainId; }                      // transient request value
public void setTrainId(String trainId) { this.trainId = trainId; }

public String getRouteId() { return routeId; }                      // transient request value
public void setRouteId(String routeId) { this.routeId = routeId; }
```

The getter is named `getIsActive()`, not `isActive()`, because the field is a
`Boolean` wrapper. Jackson therefore produces the JSON property `"isActive"`.

---

## 6. Repository line by line

File: `src/main/java/com/metro/ticketing/repository/ScheduleRepository.java`

```java
package com.metro.ticketing.repository;

import com.metro.ticketing.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;   // ready-made CRUD
import org.springframework.data.jpa.repository.Query;         // hand-written JPQL
import org.springframework.data.repository.query.Param;       // named query parameters
import org.springframework.stereotype.Repository;              // marks it as a Spring bean

import java.time.LocalDateTime;

@Repository                                                    // Spring creates an implementation at startup
public interface ScheduleRepository extends JpaRepository<Schedule, String> {
```

`JpaRepository<Schedule, String>` has two type parameters:

- `Schedule` — the entity type
- `String` — the type of the primary key

Spring Data generates a class that implements this interface. You never write
`new ScheduleRepository()`.

### Inherited methods used by the controller

| Method | What it does | SQL produced (conceptually) |
|---|---|---|
| `findById(id)` | Find one row by primary key | `SELECT ... FROM "Schedule" WHERE "ScheduleID" = ?` |
| `findAll()` | Read every row | `SELECT ... FROM "Schedule"` |
| `save(entity)` | Insert or update | `INSERT ...` or `UPDATE ...` |
| `deleteById(id)` | Delete by primary key | `DELETE FROM "Schedule" WHERE "ScheduleID" = ?` |
| `existsById(id)` | Does the row exist? | `SELECT COUNT(*) > 0 ...` |
| `count()` | Number of rows | `SELECT COUNT(*) FROM "Schedule"` |

`save` decides between insert and update by checking whether the ID already exists
in the persistence context / database.

### The overlap query

```java
@Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.train.trainId = :trainId AND " +
       "s.scheduledDeparture < :arrivalTime AND s.scheduledArrival > :departureTime AND " +
       "(:scheduleId IS NULL OR s.scheduleId != :scheduleId)")
boolean hasOverlappingSchedule(
        @Param("trainId") String trainId,                    // which train
        @Param("departureTime") LocalDateTime departureTime, // new trip start
        @Param("arrivalTime") LocalDateTime arrivalTime,     // new trip end
        @Param("scheduleId") String scheduleId               // ID to ignore during update
);
```

This is **JPQL**, not SQL. It uses the class name `Schedule` and the Java field names
(`train`, `scheduledDeparture`, `scheduleId`), not table and column names.

Line-by-line meaning:

| Fragment | Meaning |
|---|---|
| `SELECT COUNT(s) > 0` | "Is the number of matching rows greater than zero?" Returns a `boolean`. |
| `FROM Schedule s` | Look at the `Schedule` entity; `s` is an alias. |
| `s.train.trainId` | Follow the `train` relation and read its `trainId`. In SQL this becomes the `TrainID` foreign key. |
| `s.scheduledDeparture < :arrivalTime` | The existing trip starts before the new trip ends. |
| `s.scheduledArrival > :departureTime` | The existing trip ends after the new trip starts. |
| `(:scheduleId IS NULL OR s.scheduleId != :scheduleId)` | On update, ignore the row being edited. On create, pass `null` and this part is always true. |
| `@Param("...")` | Binds a Java parameter to the `:name` placeholder. |

**Why the two comparisons prove overlap.** Two ranges overlap when each one starts
before the other one ends. Back-to-back trips are allowed: a trip ending at 09:00 and
a new trip starting at 09:00 do not overlap, because `arrival > departure` is false.

Conceptual SQL generated by Hibernate:

```sql
SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
FROM "Schedule" s
WHERE s."TrainID" = ?
  AND s."ScheduledDeparture" < ?
  AND s."ScheduledArrival" > ?
  AND (? IS NULL OR s."ScheduleID" <> ?);
```

The exact SQL may differ slightly (Hibernate can rewrite `COUNT > 0` as an `EXISTS`
subquery), but the logic is the same.

**Return type.** A hand-written `@Query` that returns `boolean` works because the
select expression itself is a boolean. Spring Data reads the single value.

---

## 7. Controller endpoints

Base path: `/api/schedules`

| # | Method | Path | Who can call | Purpose | Success status |
|---|---|---|---|---|---|
| 1 | `POST` | `/api/schedules` | `ADMIN` | Create a schedule | `201 Created` |
| 2 | `PUT` | `/api/schedules/{id}` | `ADMIN` | Update a schedule | `200 OK` |
| 3 | `GET` | `/api/schedules/{id}` | Any logged-in user | Read one schedule | `200 OK` |
| 4 | `GET` | `/api/schedules` | Any logged-in user | List all schedules | `200 OK` |
| 5 | `DELETE` | `/api/schedules/{id}` | `ADMIN` | Delete a schedule | `204 No Content` |

"Any logged-in user" is enforced globally by `SecurityConfig` (`anyRequest().authenticated()`).
The read endpoints have no `@PreAuthorize`, so every authenticated role can read them.

---

## 8. Controller line by line

File: `src/main/java/com/metro/ticketing/controller/ScheduleController.java`

### Imports and class

```java
package com.metro.ticketing.controller;

import com.metro.ticketing.entity.Route;                    // used to look up the route
import com.metro.ticketing.entity.Schedule;                 // entity used as request + response
import com.metro.ticketing.entity.Train;                    // used to look up the train
import com.metro.ticketing.repository.RouteRepository;      // route database access
import com.metro.ticketing.repository.ScheduleRepository;   // schedule database access
import com.metro.ticketing.repository.TrainRepository;      // train database access
import jakarta.validation.Valid;                            // enables @NotNull checks
import org.springframework.http.HttpStatus;                 // status constants like CREATED
import org.springframework.http.ResponseEntity;            // full control of the HTTP response
import org.springframework.security.access.prepost.PreAuthorize; // role checks
import org.springframework.transaction.annotation.Transactional; // transactions
import org.springframework.web.bind.annotation.*;           // @RestController, @GetMapping, ...

import java.util.List;                                      // list of schedules
import java.util.UUID;                                      // random ID generator

@RestController                                             // response bodies are JSON, not HTML pages
@RequestMapping("/api/schedules")                           // every endpoint starts with /api/schedules
public class ScheduleController {
```

### Fields and constructor (dependency injection)

```java
private final ScheduleRepository scheduleRepository;  // final: set once in the constructor
private final RouteRepository routeRepository;        // final: set once
private final TrainRepository trainRepository;        // final: set once

public ScheduleController(ScheduleRepository scheduleRepository,
                          RouteRepository routeRepository,
                          TrainRepository trainRepository) {
    this.scheduleRepository = scheduleRepository;      // store the injected repository
    this.routeRepository = routeRepository;            // store the injected repository
    this.trainRepository = trainRepository;            // store the injected repository
}
```

Spring sees that the class has exactly one constructor. At startup it finds a
`ScheduleRepository` bean, a `RouteRepository` bean and a `TrainRepository` bean, and
passes them in. This is **constructor injection**. The controller never calls `new
ScheduleRepository()`, because a repository is an interface with no implementation in
your code.

`final` means the reference cannot be reassigned after construction. That protects
against accidental replacement and makes the dependency obvious.

### Create schedule

```java
@PostMapping                                              // handles POST /api/schedules
@PreAuthorize("hasRole('ADMIN')")                         // only ADMIN
@Transactional                                            // one database transaction
public ResponseEntity<Schedule> createSchedule(@Valid @RequestBody Schedule request) {
    validateTimes(request);                               // arrival must not be before departure
    if (scheduleRepository.hasOverlappingSchedule(        // ask the database about clashes
            request.getTrainId(),                         // train from the transient request field
            request.getScheduledDeparture(),              // new start
            request.getScheduledArrival(),                // new end
            null)) {                                      // null = do not ignore any existing row
        throw new IllegalArgumentException("Train is already scheduled during this time period");
    }

    Schedule schedule = new Schedule();                   // brand-new entity, not from JSON
    schedule.setScheduleId("SCH-" + UUID.randomUUID());   // server generates the ID
    copyScheduleFields(request, schedule);                // resolve train/route and copy values
    return ResponseEntity.status(HttpStatus.CREATED)      // 201
            .body(scheduleRepository.save(schedule));     // INSERT and return the saved row
}
```

Notes:

1. The client cannot choose `scheduleId`. Whatever it sends is ignored, because the
   controller creates a fresh object and sets a new ID.
2. `@Valid` runs `@NotNull` on departure and arrival before this body starts.
3. The overlap check runs **before** `copyScheduleFields`. If `trainId` is `null`, the
   query compares `trainId = NULL`, which matches nothing, so the query returns false.
   Then `copyScheduleFields` throws `"Train and route are required"`.
4. Leaving out `trainId` does not create a half-valid row, because the copy method
   throws before `save`.

### Update schedule

```java
@PutMapping("/{id}")                                      // handles PUT /api/schedules/{id}
@PreAuthorize("hasRole('ADMIN')")                         // only ADMIN
@Transactional                                            // one transaction
public ResponseEntity<Schedule> updateSchedule(@PathVariable String id,
                                               @Valid @RequestBody Schedule request) {
    Schedule schedule = scheduleRepository.findById(id)   // find the existing row
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

    validateTimes(request);                               // check the new times
    if (scheduleRepository.hasOverlappingSchedule(        // same check as create
            request.getTrainId(),
            request.getScheduledDeparture(),
            request.getScheduledArrival(),
            id)) {                                        // ignore this schedule's own row
        throw new IllegalArgumentException("Train is already scheduled during this time period");
    }

    copyScheduleFields(request, schedule);                // overwrite fields on the managed entity
    return ResponseEntity.ok(scheduleRepository.save(schedule)); // 200 with updated row
}
```

`@PathVariable String id` takes the `{id}` part of the URL. For
`PUT /api/schedules/SCH-001`, `id` is `"SCH-001"`.

The `id` argument to the overlap query is essential. Without it, a schedule would
always overlap with itself and every update would fail.

`save` on an entity that already has an ID performs an `UPDATE`.

### Read one schedule

```java
@GetMapping("/{id}")                                      // handles GET /api/schedules/{id}
@Transactional(readOnly = true)                           // read-only transaction
public ResponseEntity<Schedule> getScheduleById(@PathVariable String id) {
    Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
    return ResponseEntity.ok(schedule);                   // 200 with the entity as JSON
}
```

`readOnly = true` tells Hibernate that no writes are expected. It can skip dirty
checking. It is also good documentation.

`orElseThrow` turns an empty `Optional` into an exception. `GlobalExceptionHandler`
converts `IllegalArgumentException` into HTTP `400` with a JSON error message.

### Read all schedules

```java
@GetMapping                                               // handles GET /api/schedules
@Transactional(readOnly = true)
public ResponseEntity<List<Schedule>> getAllSchedules() {
    return ResponseEntity.ok(scheduleRepository.findAll()); // 200 with a JSON array
}
```

There is no paging and no sorting. The method returns every row in the table.

### Delete schedule

```java
@DeleteMapping("/{id}")                                   // handles DELETE /api/schedules/{id}
@PreAuthorize("hasRole('ADMIN')")                         // only ADMIN
@Transactional
public ResponseEntity<Void> deleteSchedule(@PathVariable String id) {
    if (!scheduleRepository.existsById(id)) {             // check first for a clear error
        throw new IllegalArgumentException("Schedule not found");
    }
    scheduleRepository.deleteById(id);                    // DELETE the row
    return ResponseEntity.noContent().build();            // 204, no body
}
```

`ResponseEntity<Void>` means "there is no response body". `noContent()` sets status
`204`.

The `existsById` check is not strictly required; `deleteById` on a missing row would
also fail. It exists to produce the message `"Schedule not found"`.

### Helper: validateTimes

```java
private void validateTimes(Schedule request) {
    if (request.getScheduledArrival().isBefore(request.getScheduledDeparture())) {
        throw new IllegalArgumentException("Arrival time cannot be before departure time");
    }
}
```

- `private` — only this class can call it.
- `void` — returns nothing; it either passes or throws.
- `isBefore` returns `true` only for a strictly earlier time.
- Arrival **equal** to departure is allowed. The trip has zero length, but it is not
  rejected.
- `@Valid` already guarantees both values are non-null when this runs.

### Helper: copyScheduleFields

```java
private void copyScheduleFields(Schedule request, Schedule schedule) {
    if (request.getRouteId() == null || request.getTrainId() == null) {
        throw new IllegalArgumentException("Train and route are required");
    }

    Route route = routeRepository.findById(request.getRouteId())     // look up the real route
            .orElseThrow(() -> new IllegalArgumentException("Route not found"));
    Train train = trainRepository.findById(request.getTrainId())     // look up the real train
            .orElseThrow(() -> new IllegalArgumentException("Train not found"));

    schedule.setRoute(route);                                   // attach the managed Route entity
    schedule.setTrain(train);                                   // attach the managed Train entity
    schedule.setScheduledDeparture(request.getScheduledDeparture());
    schedule.setScheduledArrival(request.getScheduledArrival());
    schedule.setDayOfWeek(request.getDayOfWeek());              // may be null

    if (request.getIsActive() != null) {                        // only change when provided
        schedule.setIsActive(request.getIsActive());
    }
}
```

Why resolve the IDs into entities? JPA needs a real `Train` / `Route` reference to write
the foreign key. You could build a stub with only the ID, but looking it up also proves
that the row exists and gives a clear 400 error when it does not.

Why the `isActive` null check? On update, a client that does not send `isActive` should
not accidentally switch the schedule off. On create, the entity already defaults to
`true`.

**What this method does not copy:** `scheduleId`, `validFrom`, `validTo`, `createdAt`.
So:

- the ID is always server-generated on create and never changed on update;
- `validFrom` / `validTo` stay `null` for schedules created through the API;
- `createdAt` stays at the value set when the Java object was constructed.

---

## 9. One request from start to finish

Story: an admin adds a Monday morning trip for train `TRN-101` on the Blue Line.

1. The admin opens Swagger and authorizes as `admin` / `admin123`.
2. Swagger sends:

   ```http
   POST /api/schedules
   Authorization: Basic YWRtaW46YWRtaW4xMjM=
   Content-Type: application/json
   ```

   with the JSON body shown in section 10.
3. Spring Security reads the `Authorization` header, loads the user whose `Contact`
   is `admin`, and builds an `Authentication` with authority `ROLE_ADMIN`.
4. Spring MVC matches `POST /api/schedules` to `createSchedule`.
5. `@PreAuthorize("hasRole('ADMIN')")` sees `ROLE_ADMIN` and allows the call.
6. Jackson converts the JSON into a new `Schedule` object. Only writable fields are
   filled: `trainId`, `routeId`, `dayOfWeek`, `scheduledDeparture`,
   `scheduledArrival`, `isActive`.
7. `@Valid` checks `scheduledDeparture` and `scheduledArrival`. Both are present.
8. `validateTimes` compares them: `09:40` is not before `09:00`, so it passes.
9. `hasOverlappingSchedule("TRN-101", 09:00, 09:40, null)` runs. Assume the seeder ran
   today, so the seeded trip `SCH-001` for `TRN-101` is today `06:00`–`06:46`.
   `06:00 < 09:40` is true, but `06:46 > 09:00` is false, so it is not an overlap.
   The query returns `false`.
10. The controller creates a new `Schedule` and sets
    `scheduleId = "SCH-" + UUID`, for example
    `SCH-3f2b9c1e-7a44-4d21-9b0e-5c1f0a8d2e11`.
11. `copyScheduleFields` finds route `RT-L1` and train `TRN-101`, attaches them, and
    copies the times and day label.
12. `isActive` is `true` in the request, so it is set to `true`.
13. `scheduleRepository.save` issues an `INSERT`. Hibernate writes the foreign keys
    `TrainID = 'TRN-101'` and `RouteID = 'RT-L1'`.
14. The saved entity is serialized to JSON. `train` and `route` are lazy, so Hibernate
    loads them during serialization (open-in-view is enabled). The transient
    `trainId` and `routeId` are skipped because they are `WRITE_ONLY`.
15. The response is `201 Created` with the new schedule and a `Location`-less body.

If the admin instead sends `09:30`–`10:10`, step 9 finds the trip just created
(`09:00 < 10:10` and `09:40 > 09:30`) and returns `true`. The controller throws
`IllegalArgumentException`, and the client receives `400` with
`{"error": "Train is already scheduled during this time period"}`.

---

## 10. JSON request and response examples

### Create request (admin)

```json
{
  "trainId": "TRN-101",
  "routeId": "RT-L1",
  "dayOfWeek": "MONDAY",
  "scheduledDeparture": "2026-10-29T09:00:00",
  "scheduledArrival": "2026-10-29T09:40:00",
  "isActive": true
}
```

| Field | Required | Meaning |
|---|---|---|
| `trainId` | yes | Must exist in `Trains`. Used to find the real `Train`. |
| `routeId` | yes | Must exist in `Route`. Used to find the real `Route`. |
| `dayOfWeek` | no | Free text label. Stored as-is. |
| `scheduledDeparture` | yes | ISO-8601 local date-time. No timezone. |
| `scheduledArrival` | yes | ISO-8601 local date-time. Must not be before departure. |
| `isActive` | no | Omitted or `null` means "keep the default / current value". |
| `scheduleId` | — | Ignored on create. The server generates it. |
| `train` | — | Read-only. Ignored in requests. |
| `route` | — | Read-only. Ignored in requests. |
| `validFrom` / `validTo` | — | Accepted by Jackson but not copied by the controller. |

### Create response (`201`)

```json
{
  "scheduleId": "SCH-3f2b9c1e-7a44-4d21-9b0e-5c1f0a8d2e11",
  "train": {
    "trainId": "TRN-101",
    "trainNumber": "M1",
    "capacity": 1800,
    "totalCoaches": 8,
    "manufactureYear": 2014,
    "createdAt": "2026-09-29T10:15:00",
    "isActive": true
  },
  "route": {
    "routeId": "RT-L1",
    "routeName": "Mumbai Metro Line 1 - Blue (Versova to Ghatkopar)",
    "totalDistance": 11.10,
    "estimatedTime": 21,
    "lineColor": "Blue",
    "createdAt": "2026-10-15T10:15:00",
    "updatedAt": "2026-10-15T10:15:00",
    "startStation": { "stationId": "STN-VSV", "stationCode": "VSV" },
    "endStation": { "stationId": "STN-GHP", "stationCode": "GHP" },
    "stationIds": null
  },
  "dayOfWeek": "MONDAY",
  "scheduledDeparture": "2026-10-29T09:00:00",
  "scheduledArrival": "2026-10-29T09:40:00",
  "validFrom": null,
  "validTo": null,
  "isActive": true,
  "createdAt": "2026-10-29T08:55:12.345"
}
```

Notice:

- `trainId` and `routeId` do **not** appear, because they are `WRITE_ONLY`.
- `train` and `route` appear as full objects, because those relations are `READ_ONLY`.
- `validFrom` and `validTo` are `null` for API-created rows.
- `scheduleId` contains a UUID.
- The nested `route` also contains `stationIds`, a transient field filled only by
  `RouteController`. Here it is `null`, because `ScheduleController` never fills it.
  That is not a database error; the field is simply not loaded in this response.

### Update request

```http
PUT /api/schedules/SCH-3f2b9c1e-7a44-4d21-9b0e-5c1f0a8d2e11
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json
```

```json
{
  "trainId": "TRN-101",
  "routeId": "RT-L1",
  "dayOfWeek": "MONDAY",
  "scheduledDeparture": "2026-10-29T09:05:00",
  "scheduledArrival": "2026-10-29T09:45:00"
}
```

The overlap query is called with the schedule's own ID, so the row does not clash with
itself. `isActive` is missing, so the old value is kept.

### Read all

```http
GET /api/schedules
Authorization: Basic priya:Passw0rd!
```

```json
[
  {
    "scheduleId": "SCH-001",
    "train": { "trainId": "TRN-101", "trainNumber": "M1" },
    "route": { "routeId": "RT-L1", "routeName": "Mumbai Metro Line 1 - Blue (Versova to Ghatkopar)" },
    "dayOfWeek": "DAILY",
    "scheduledDeparture": "2026-10-29T06:00:00",
    "scheduledArrival": "2026-10-29T06:46:00",
    "validFrom": "2026-10-28",
    "validTo": "2027-10-29",
    "isActive": true,
    "createdAt": "2026-10-22T10:15:00"
  }
]
```

The real nested objects contain all their own fields; they are shortened here for
readability.

---

## 11. Error table

All error responses are produced by `GlobalExceptionHandler`.

| Situation | Status | Body |
|---|---|---|
| `scheduledDeparture` missing | `400` | `{"scheduledDeparture":"Scheduled departure is required"}` |
| `scheduledArrival` missing | `400` | `{"scheduledArrival":"Scheduled arrival is required"}` |
| Arrival before departure | `400` | `{"error":"Arrival time cannot be before departure time"}` |
| `trainId` or `routeId` missing | `400` | `{"error":"Train and route are required"}` |
| Train ID does not exist | `400` | `{"error":"Train not found"}` |
| Route ID does not exist | `400` | `{"error":"Route not found"}` |
| Train already busy in that time range | `400` | `{"error":"Train is already scheduled during this time period"}` |
| Schedule ID does not exist (GET/PUT/DELETE) | `400` | `{"error":"Schedule not found"}` |
| No `Authorization` header / wrong password | `401` | Spring Security default body (not the JSON handler) |
| Logged in but not `ADMIN` on write endpoints | `403` | `{"error":"You do not have permission to access this resource"}` |
| Unexpected server or database error | `500` | `{"error":"An unexpected error occurred: ..."}` |

Important detail: **"not found" is `400`, not `404`.** These controllers throw
`IllegalArgumentException`, and the handler maps that to `400`. A `404` is only produced
by `ResourceNotFoundException`, which this group does not use.

`401` vs `403`:

- `401 Unauthorized` = "I do not know who you are."
- `403 Forbidden` = "I know who you are, but you may not do this."

---

## 12. Relationships to other groups

```
Trains (1) ───< (many) Schedule (many) >─── (1) Route
                                                 │
                                                 └──< RouteStations >── Stations
```

| Group | Relationship |
|---|---|
| **Train** (`04-TRAIN.md`) | `Schedule.train` is `@ManyToOne`. One train has many scheduled trips. |
| **Route** (`05-ROUTE.md`) | `Schedule.route` is `@ManyToOne`. One route has many scheduled trips. |
| **RouteStation** (`06-ROUTE-STATION.md`) | A schedule follows a route; the route's stop order lives in `RouteStations`. |
| **Station** (`03-STATION.md`) | Reached indirectly through the route. |
| **Fare** (`08-FARE.md`) | No direct link. Schedules say *when*, fares say *how much*. |
| **Ticket** (`09-TICKET.md`) | No direct link. A ticket is not tied to a scheduled trip in this model. |
| **Auth** (`02-AUTH-AND-ROLE.md`) | Write endpoints need `ROLE_ADMIN`. |

Deleting a train or route that still has schedules fails at the database level because
of `FK_Schedule_Train` and `FK_Schedule_Route`. The resulting exception is not caught
as an `IllegalArgumentException`, so the client sees a `500`.

Deleting a schedule is safe: no other table references `Schedule`.

---

## 13. Swagger test steps

1. Start the application and open `http://localhost:8080/swagger-ui/index.html`.
2. Click **Authorize**.
3. Enter username `admin` and password `admin123`. Click **Authorize**, then **Close**.
4. Open the **schedule-controller** section.
5. Call `GET /api/schedules` first. You should see the eight seeded rows
   `SCH-001` … `SCH-008`.
6. Call `GET /api/schedules/SCH-001`. Check the nested `train` and `route`.
7. Call `POST /api/schedules` with:

   ```json
   {
     "trainId": "TRN-101",
     "routeId": "RT-L1",
     "dayOfWeek": "MONDAY",
     "scheduledDeparture": "2026-10-29T09:00:00",
     "scheduledArrival": "2026-10-29T09:40:00",
     "isActive": true
   }
   ```

8. Expect `201`. Copy the generated `scheduleId`.
9. Call `POST` again with `09:30`–`10:10` for the same train. Expect `400` and the
   overlap message.
10. Call `PUT /api/schedules/{id}` with `09:05`–`09:45`. Expect `200`.
11. Log out (or use a private window) and authorize as `priya` / `Passw0rd!`.
12. Call `GET /api/schedules`. Expect `200`.
13. Try `POST /api/schedules` as `priya`. Expect `403`.
14. Authorize again as `admin` and call `DELETE /api/schedules/{id}`. Expect `204`.
15. Call `GET /api/schedules/{id}`. Expect `400` with `"Schedule not found"`.

Swagger caches Basic credentials. Use a private window or the **Logout** button before
switching users.

---

## 14. How to explain this in 2 minutes

> "The Schedule group answers one question: **when does a train run on a route?**
>
> The entity has four important parts. First, an ID like `SCH-<uuid>`. Second, two
> foreign keys: `TrainID` and `RouteID`. Third, the times:
> `ScheduledDeparture` and `ScheduledArrival`, stored as `LocalDateTime`. Fourth,
> bookkeeping fields: `dayOfWeek`, `validFrom`, `validTo`, `isActive` and `createdAt`.
>
> The JSON is designed so the client can send simple strings. `trainId` and `routeId`
> are `@Transient`, so they are not columns, and they are `WRITE_ONLY`, so they never
> appear in responses. The real `train` and `route` fields are `@ManyToOne` with
> `LAZY` loading and `READ_ONLY` JSON access, so responses show full objects but
> requests cannot overwrite them with nested data.
>
> The repository extends `JpaRepository`, which gives `save`, `findById`, `findAll`
> and `deleteById` for free. It adds one hand-written JPQL query,
> `hasOverlappingSchedule`. That query asks: for this train, is there any existing row
> where `existingDeparture < newArrival` and `existingArrival > newDeparture`? It also
> ignores the row being edited by accepting an optional `scheduleId`.
>
> The controller is the business layer. `POST`, `PUT` and `DELETE` require
> `ROLE_ADMIN`. `GET` only requires a login. Create generates the ID, checks the times,
> checks overlap, resolves the train and route, then saves. Update does the same but
> passes its own ID to the overlap query. Delete checks existence and returns `204`.
>
> Validation uses `@Valid` plus `@NotNull`. Errors are turned into JSON by
> `GlobalExceptionHandler`. Missing records come back as `400`, not `404`, because the
> controller throws `IllegalArgumentException`.
>
> The one limitation to mention honestly: the database has no unique constraint for
> overlap, so the rule lives in Java. Two simultaneous requests could both pass the
> check. For a college project that is acceptable, and I can explain how a database
> exclusion constraint would fix it."

---

## 15. Beginner Q&A

**Q1. What is the difference between `Schedule` and `Train`?**
`Train` is the physical vehicle: number, capacity, coaches. `Schedule` is a planned
trip: this train, this route, these times. One train can have many schedules.

**Q2. What is the difference between `Schedule` and `Route`?**
`Route` is the line and its stops. `Schedule` is one departure on that line.

**Q3. Why is `scheduleId` a `String` and not a number?**
Because IDs are readable text: seeded rows use `SCH-001`, and new rows use
`SCH-<uuid>`. A `String` column accepts both.

**Q4. Who generates the ID?**
The controller, with `"SCH-" + UUID.randomUUID()`. Any `scheduleId` sent by the client
is ignored on create.

**Q5. What does `@Transient` do?**
It tells JPA "this field is not a column". Hibernate will not read or write it. It
exists only in the Java object.

**Q6. Why are `trainId` and `routeId` both `@Transient` and `WRITE_ONLY`?**
They are a convenience for requests. `@Transient` keeps them out of the table.
`WRITE_ONLY` keeps them out of the JSON response, where the full `train` and `route`
objects already appear.

**Q7. What does `@JsonProperty(access = READ_ONLY)` do on `train`?**
It lets Jackson write the field into responses but ignore it when reading requests.
So a client cannot replace the train by sending a nested object; it must send
`trainId`.

**Q8. Why is `fetch = FetchType.LAZY` used?**
To avoid loading train and route for every schedule unless they are needed. The
project has `spring.jpa.open-in-view=true`, so the lazy load still works during JSON
serialization.

**Q9. What happens if I send `"isActive": null` on update?**
`copyScheduleFields` checks `!= null`, so the old value is kept.

**Q10. What happens if arrival equals departure?**
It is accepted. `validateTimes` only rejects arrival **before** departure, using
`isBefore`.

**Q11. Why does the overlap query use `<` and `>` instead of `<=` and `>=`?**
So that back-to-back trips are allowed. A trip ending at 09:00 and the next starting
at 09:00 do not share any time.

**Q12. Why does create pass `null` as the last query argument?**
The last part of the query is `(:scheduleId IS NULL OR s.scheduleId != :scheduleId)`.
With `null`, the first half is true, so no row is excluded. On update, the real ID is
passed so the row does not clash with itself.

**Q13. What SQL does `hasOverlappingSchedule` run?**
Conceptually:
`SELECT COUNT(*) > 0 FROM "Schedule" WHERE "TrainID" = ? AND "ScheduledDeparture" < ? AND "ScheduledArrival" > ? AND (? IS NULL OR "ScheduleID" <> ?)`.
Hibernate may rewrite the count as an `EXISTS`.

**Q14. Does the database prevent overlapping schedules?**
No. There is no unique or exclusion constraint. The check is in Java. This is a
known limitation under concurrent requests.

**Q15. Why does `GET` have no `@PreAuthorize`?**
Because all logged-in roles may view timetables. `SecurityConfig` still requires
authentication for every request that is not explicitly public.

**Q16. Why is "not found" a `400` and not a `404`?**
The controller throws `IllegalArgumentException`, and `GlobalExceptionHandler` maps
that to `400`. Only `ResourceNotFoundException` maps to `404`.

**Q17. Why does the controller have no service class?**
The project deliberately keeps three layers: controller, repository, entity. The
controller holds the business rules. Fewer layers means less code to explain.

**Q18. What are `validFrom` and `validTo` used for?**
They describe the date range in which a timetable row applies. Seed data fills them.
The controller currently does not copy them from requests, so API-created rows have
`null` there.

**Q19. What happens if I delete a train that has schedules?**
The database foreign key `FK_Schedule_Train` rejects the delete. The exception is not
an `IllegalArgumentException`, so the handler returns `500`.

**Q20. How does Spring know to call `createSchedule` for `POST /api/schedules`?**
`@RestController` registers the class, `@RequestMapping` adds the base path, and
`@PostMapping` (with no extra path) maps the empty sub-path to `POST`.

---

## 16. Glossary

| Term | Meaning |
|---|---|
| **API** | A defined way for programs to talk to each other. |
| **Authentication** | Proving who you are (username + password). |
| **Authorization** | Deciding what an authenticated user may do. |
| **Basic auth** | Username and password sent in an `Authorization` header, base64-encoded. |
| **Bean** | An object created and managed by Spring. |
| **Body** | The main content of an HTTP request or response. |
| **Controller** | Class that maps URLs to Java methods. |
| **CRUD** | Create, Read, Update, Delete. |
| **Dependency injection** | Supplying a class's dependencies from outside. |
| **Derived query** | Repository method whose query is generated from its name. |
| **Entity** | Java class mapped to a table. |
| **FK (foreign key)** | Column that references another table's primary key. |
| **Flyway** | Tool that runs versioned SQL migration files. |
| **Hibernate** | JPA implementation that generates SQL. |
| **HTTP** | Protocol used for web requests. |
| **ISO-8601** | Standard date/time text format, e.g. `2026-10-29T09:00:00`. |
| **JPA** | Java Persistence API, the mapping standard. |
| **JPQL** | Query language that uses entity names and fields instead of tables and columns. |
| **JSON** | Text data format of objects, arrays, strings, numbers, booleans and null. |
| **Lazy loading** | Loading related data only when it is first accessed. |
| **Open-in-view** | Keeps the Hibernate session open while the response is serialized. |
| **Optional** | Container that may hold a value or be empty. |
| **Overlap** | Two time ranges sharing at least one instant. |
| **PK (primary key)** | Column that uniquely identifies a row. |
| **Proxy** | Stand-in object Hibernate uses for lazy relations. |
| **Repository** | Interface that performs database operations. |
| **REST** | Style where URLs name resources and HTTP methods name actions. |
| **Serialization** | Java object → JSON. |
| **Spring Boot** | Framework that wires and starts the application. |
| **SQL** | Language for relational databases. |
| **Status code** | Three-digit result number, e.g. `200`, `201`, `400`, `403`, `500`. |
| **Transaction** | Group of database operations that all succeed or all roll back. |
| **UUID** | 128-bit random identifier, written as 36 characters with dashes. |
| **Validation** | Checking input before using it. |

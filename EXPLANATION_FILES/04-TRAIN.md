# 04 — Train Group

**In one sentence:** a Train is one physical rake of coaches; it has a number, a capacity, an age, and an active flag, and it is connected to a line only through a Schedule.

**Files in this group**

| File | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/Train.java` |
| Controller | `src/main/java/com/metro/ticketing/controller/TrainController.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/TrainRepository.java` |
| Database table | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Unique index | same file, `UX_Train_Number` |
| Seed data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` |

---

## 1. Real-life analogy

A metro train is like a **bus in a depot**.

- The bus has a fleet number painted on the back, for example `M1`. That is `TrainNumber`. Two buses in the same fleet cannot share a fleet number.
- The bus has a seating-plus-standing capacity, for example 1800 people. That is `Capacity`.
- The bus is made of a certain number of coaches, for example 8. That is `TotalCoaches`.
- The bus was built in a certain year, for example 2014. That is `ManufactureYear`.
- The bus has an internal depot record number that never changes. That is `TrainID`.
- The bus can be taken out of service for maintenance without being scrapped. That is `isActive = false`.

Now the important bit: **the bus itself does not belong to a route.** A bus is assigned to route 7 on Monday morning and route 12 in the evening. The assignment is a separate paper in the depot office. In this project that paper is the **Schedule** row, which points at one train and one route.

So `Train` has no `routeId` field. If you look for one, you will not find it. That is correct design, not a missing feature.

---

## 2. Words you need

| Word | One-sentence meaning |
|---|---|
| Spring Boot | Framework that starts the web server and creates objects for you. |
| Annotation | An `@Something` label that gives the framework instructions. |
| Bean | An object whose life is managed by Spring. |
| Dependency injection | Spring passes needed objects into a constructor instead of the class creating them. |
| Controller | Class that receives HTTP requests and returns HTTP responses. |
| Endpoint | One URL plus one HTTP method. |
| HTTP method | GET (read), POST (create), PUT (update), DELETE (remove). |
| JSON | Text format for data using `{}` objects and `[]` arrays. |
| Jackson | Library that converts between Java objects and JSON. |
| Deserialization | JSON text -> Java object. |
| Serialization | Java object -> JSON text. |
| JPA | Java standard for mapping classes to tables. |
| Hibernate | The library that implements JPA and writes SQL. |
| Entity | Class marked `@Entity` that maps to a table. |
| ORM | Treating a table as a class and a row as an object. |
| Table | Grid of rows and columns in the database. |
| Column | One named field of a table. |
| Primary key | Column that uniquely identifies a row. |
| Foreign key | Column that must match a primary key in another table. |
| Unique index | Rule that no two rows may share a value. |
| Repository | Interface giving database methods without SQL. |
| `JpaRepository` | Spring Data interface with `save`, `findById`, `findAll`, `deleteById`, and more. |
| Transaction | All-or-nothing group of database changes. |
| `@Transactional` | Wraps a method in one transaction. |
| Bean Validation | Annotation-based input checks such as `@NotBlank`. |
| `@Valid` | Tells Spring to run those checks on the JSON body. |
| `ResponseEntity` | Object that carries both status code and body. |
| HTTP status | Number describing the result: 200, 201, 204, 400, 401, 403, 500. |
| `Optional` | Container that may hold a value or nothing. |
| UUID | Long random string used to build unique IDs. |
| `LocalDateTime` | Date plus time, e.g. `2026-10-29T09:30:00`. |
| Basic authentication | `Authorization: Basic base64(user:password)` on every request. |
| Role | Permission label, here `ADMIN`, `TICKET_AGENT`, `MAINTENANCE_STAFF`, `PASSENGER`. |
| `@PreAuthorize` | Role check that runs before the method. |
| Flyway | Tool that applies numbered SQL files to build the schema. |
| Migration | One numbered SQL file. |
| Seeder | Startup class that inserts demo rows. |
| Soft delete | Deactivating a row instead of deleting it. |
| `nullable = false` | JPA instruction to add `NOT NULL` in the database. |
| Swagger UI | Web page for listing and testing endpoints. |
| Rake | Railway word for one complete train set of coaches. |

---

## 3. Files in this group

```
src/main/java/com/metro/ticketing/
├── entity/Train.java                 <- fields of one train
├── repository/TrainRepository.java   <- database access, one line of body
└── controller/TrainController.java   <- 5 endpoints
src/main/resources/db/migration/
└── V1__create_metro_ticketing_schema.sql   <- "Trains" table + unique index
src/main/java/com/metro/ticketing/config/
└── DataSeeder.java                   <- inserts 8 demo trains
```

The three layers:

1. **Entity** — what a train *is*.
2. **Repository** — how to store and fetch trains.
3. **Controller** — how the outside world talks about trains over HTTP.

There is no `TrainService`. The controller talks to the repository directly. That keeps the demo readable; a bigger project would insert a service layer between them.

---

## 4. Database table

Exact SQL from `V1__create_metro_ticketing_schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS "Trains" (
    "TrainID" VARCHAR(50) PRIMARY KEY,
    "TrainNumber" VARCHAR(50) NOT NULL,
    "Capacity" INTEGER,
    "TotalCoaches" INTEGER,
    "ManufactureYear" INTEGER,
    "CreatedAt" TIMESTAMP,
    "isActive" BOOLEAN DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS "UX_Train_Number" ON "Trains" ("TrainNumber");
```

### Column by column

| Column | SQL type | Meaning | Empty allowed? |
|---|---|---|---|
| `TrainID` | `VARCHAR(50)` | Internal identifier and primary key, generated as `TRN-<uuid>`. | No |
| `TrainNumber` | `VARCHAR(50)` | Public fleet number such as `M1`, `A2`, `R1`. | No |
| `Capacity` | `INTEGER` | Total passengers the train can carry, e.g. `1800`. | Yes |
| `TotalCoaches` | `INTEGER` | Number of coaches attached, e.g. `8`. | Yes |
| `ManufactureYear` | `INTEGER` | Year the rake was built, e.g. `2014`. | Yes |
| `CreatedAt` | `TIMESTAMP` | When this database row was created. | Yes |
| `isActive` | `BOOLEAN` | `true` = in service, `false` = withdrawn. Default `TRUE`. | Yes (defaults) |

### Why the names are quoted

PostgreSQL lowercases any unquoted name. `Trains` written without quotes would become `trains`. The project wants the exact capitalised name, so every layer quotes it:

- SQL: `"Trains"`
- Entity: `@Table(name = "\"Trains\"")` — in Java, `\"` is one double-quote character.
- `spring.jpa.hibernate.naming.physical-strategy=...PhysicalNamingStrategyStandardImpl` stops Hibernate from changing names on its own.

`spring.jpa.hibernate.ddl-auto=validate` then compares the entity to the real table at startup. If one column name is wrong, the app will not start.

### Why `TrainNumber` is unique

Two trains with the same fleet number would make schedules ambiguous. The unique index `UX_Train_Number` makes the database itself reject duplicates. The Java code does **not** pre-check; it lets the database enforce the rule.

### Why `Capacity` and `TotalCoaches` are separate

`Capacity` is people. `TotalCoaches` is physical cars. A train with 8 coaches might carry 1800 people, while a 6-coach version carries fewer. Keeping both lets you show "1800 passengers / 8 coaches" in the UI without computing anything.

### Integers, not decimals

`INTEGER` maps to Java `Integer`. There is no need for `NUMERIC` here, because counts and years are whole numbers. Money-like values such as fares use `NUMERIC(10,2)` instead.

---

## 5. Entity line by line

Full file with a comment on every annotation and field:

```java
package com.metro.ticketing.entity;                    // package for all entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Jackson import
import jakarta.persistence.Column;                    // JPA column mapping
import jakarta.persistence.Entity;                    // JPA entity marker
import jakarta.persistence.Id;                        // JPA primary key marker
import jakarta.persistence.Table;                     // JPA table name
import jakarta.validation.constraints.NotBlank;        // validation annotation

import java.time.LocalDateTime;                        // date + time class

@Entity                                                // 1. this class is a table
@Table(name = "\"Trains\"")                            // 2. exact table name, quoted
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
                                                       // 3. hide Hibernate proxy internals from JSON
public class Train {                                   // 4. ordinary Java class

    @Id                                                // 5. primary key
    @Column(name = "\"TrainID\"", length = 50)         // 6. column "TrainID", VARCHAR(50)
    private String trainId;                            // 7. internal ID, e.g. "TRN-101"

    @NotBlank(message = "Train number is required")    // 8. null / "" / "   " -> 400
    @Column(name = "\"TrainNumber\"", length = 50, nullable = false)
                                                       // 9. column "TrainNumber", NOT NULL
    private String trainNumber;                        // 10. fleet number, e.g. "M1"

    @Column(name = "\"Capacity\"")                     // 11. column "Capacity", INTEGER
    private Integer capacity;                          // 12. passengers, e.g. 1800

    @Column(name = "\"TotalCoaches\"")                 // 13. column "TotalCoaches"
    private Integer totalCoaches;                      // 14. coaches, e.g. 8

    @Column(name = "\"ManufactureYear\"")              // 15. column "ManufactureYear"
    private Integer manufactureYear;                   // 16. year built, e.g. 2014

    @Column(name = "\"CreatedAt\"")                    // 17. column "CreatedAt", TIMESTAMP
    private LocalDateTime createdAt = LocalDateTime.now();
                                                       // 18. set at object creation time

    @Column(name = "\"isActive\"")                     // 19. column "isActive"
    private Boolean isActive = true;                   // 20. new trains are active by default

    // ----- getters and setters: plain Java -----

    public String getTrainId() {                       // JSON name: "trainId"
        return trainId;
    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    public String getTrainNumber() {                    // JSON name: "trainNumber"
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public Integer getCapacity() {                      // JSON name: "capacity"
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getTotalCoaches() {                  // JSON name: "totalCoaches"
        return totalCoaches;
    }

    public void setTotalCoaches(Integer totalCoaches) {
        this.totalCoaches = totalCoaches;
    }

    public Integer getManufactureYear() {               // JSON name: "manufactureYear"
        return manufactureYear;
    }

    public void setManufactureYear(Integer manufactureYear) {
        this.manufactureYear = manufactureYear;
    }

    public LocalDateTime getCreatedAt() {               // JSON name: "createdAt"
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsActive() {                      // JSON name: "isActive", not "active"
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
```

### Annotation-by-annotation

| Annotation | Meaning in plain words |
|---|---|
| `@Entity` | "Hibernate, this class is a table." |
| `@Table(name = "\"Trains\"")` | "The table is named exactly `Trains`." |
| `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` | "When writing JSON, drop these two Hibernate-internal properties." They exist on proxy classes and would otherwise appear as noise. |
| `@Id` | "This field is the primary key." |
| `@Column(name = ...)` | "The database column has this exact name." |
| `@Column(length = 50)` | "Use `VARCHAR(50)`." |
| `@Column(nullable = false)` | "Add `NOT NULL`." |
| `@NotBlank` | "Reject blank strings before saving." |

### Why every numeric field is `Integer`, not `int`

`Integer` can be `null`; `int` cannot. All three numeric columns are nullable in SQL, so the Java fields must be able to represent "not filled in". If you used `int`, a missing `capacity` would silently become `0`, which looks like a real value.

### Why `createdAt` is initialised inline

```java
private LocalDateTime createdAt = LocalDateTime.now();
```

Every time `new Train()` runs, this line executes and stores the current date and time. So a train object always has a creation time.

The controller does not copy `createdAt` from the request (`copyTrainFields` skips it). Therefore the client cannot backdate a train: a `createdAt` in the JSON is parsed into the *request* object and then dropped, and the saved entity keeps the timestamp from `new Train()`.

### Why there is no `lineColor` or `routeId` on Train

A train is not permanently tied to a line. It can serve different lines on different days. The link is a **Schedule** row:

```
Schedule.train  -> Train
Schedule.route  -> Route   (and Route has lineColor)
```

So the line of a train is discovered through its schedules, not stored on the train. Adding `lineColor` to `Train` would duplicate data and could contradict the schedule.

---

## 6. Repository line by line

```java
package com.metro.ticketing.repository;                // repository package

import com.metro.ticketing.entity.Train;               // the entity to manage
import org.springframework.data.jpa.repository.JpaRepository; // base CRUD interface
import org.springframework.stereotype.Repository;      // bean marker

@Repository                                            // 1. create a Spring bean for this interface
public interface TrainRepository extends JpaRepository<Train, String> {
                                                       // 2. Train = entity, String = ID type
}
```

No methods are declared. Spring Data JPA reads `JpaRepository<Train, String>` and generates the implementation at startup. There is no `TrainRepositoryImpl.java` anywhere in the project.

### The inherited methods, with the SQL they generate

| Java call | SQL Hibernate runs |
|---|---|
| `trainRepository.save(newTrain)` | `INSERT INTO "Trains" ("TrainID","TrainNumber","Capacity","TotalCoaches","ManufactureYear","CreatedAt","isActive") VALUES (?,?,?,?,?,?,?)` |
| `trainRepository.save(existingTrain)` | `UPDATE "Trains" SET "TrainNumber"=?, "Capacity"=?, ... WHERE "TrainID"=?` |
| `trainRepository.findById("TRN-101")` | `SELECT "TrainID","TrainNumber",... FROM "Trains" WHERE "TrainID"=?` |
| `trainRepository.findAll()` | `SELECT ... FROM "Trains"` |
| `trainRepository.existsById("TRN-101")` | `SELECT 1 FROM "Trains" WHERE "TrainID"=? LIMIT 1` (Hibernate may use `count(*)`) |
| `trainRepository.deleteById("TRN-101")` | `DELETE FROM "Trains" WHERE "TrainID"=?` |
| `trainRepository.count()` | `SELECT count(*) FROM "Trains"` |

One technical detail worth knowing for a viva: the primary key is **assigned by our code** (`TRN-...`), not generated by a database sequence. Because of that, Spring Data cannot tell "new" from "existing" by looking for a `null` ID. It calls `EntityManager.merge`, which may run a `SELECT` before deciding to `INSERT`. The result is still a single new row, but the SQL log may show an extra read. A `@GeneratedValue` key would avoid that. This project accepts the trade-off for readable IDs.

### Who injects `TrainRepository`

- `TrainController` — the five endpoints here.
- `ScheduleController` — to attach a train to a timetable entry.
- `MaintenanceIssueController` — to attach a fault to a train.
- `DataSeeder` — to insert 8 demo trains and to build schedules and issues.

Both `Schedule` and `MaintenanceIssue` hold a `@ManyToOne` to `Train`, so the database has foreign keys pointing at `"Trains"`. That is what prevents deleting a train that is still in use.

---

## 7. Controller endpoints

Base path: `/api/trains`, set by `@RequestMapping("/api/trains")`.

| # | Method | Full path | Who can call | Purpose | Success |
|---|---|---|---|---|---|
| 1 | POST | `/api/trains` | `ADMIN` | Create a train | `201 Created` |
| 2 | PUT | `/api/trains/{id}` | `ADMIN` | Update a train | `200 OK` |
| 3 | GET | `/api/trains/{id}` | any authenticated user | Read one train | `200 OK` |
| 4 | GET | `/api/trains` | any authenticated user | List all trains | `200 OK` |
| 5 | DELETE | `/api/trains/{id}` | `ADMIN` | Delete a train | `204 No Content` |

Every logged-in role can read. Only `ADMIN` can create, update, or delete. A passenger does not need the train list to buy a ticket, but the API does not hide it either.

There is no endpoint like `/api/trains/{id}/route`. A train's route lives in the Schedule group, so you call `GET /api/schedules` and filter on the nested `train.trainId`.

---

## 8. Controller line by line

Full file with a comment on every important line:

```java
package com.metro.ticketing.controller;

import com.metro.ticketing.entity.Train;                 // entity used as body
import com.metro.ticketing.repository.TrainRepository;  // injected dependency
import jakarta.validation.Valid;                         // enables @NotBlank
import org.springframework.http.HttpStatus;              // CREATED constant
import org.springframework.http.ResponseEntity;          // status + body wrapper
import org.springframework.security.access.prepost.PreAuthorize; // role check
import org.springframework.transaction.annotation.Transactional; // transaction
import org.springframework.web.bind.annotation.*;        // web annotations

import java.util.List;                                   // List<Train>
import java.util.UUID;                                   // ID generation

@RestController                                          // 1. return values become JSON
@RequestMapping("/api/trains")                           // 2. base path
public class TrainController {

    private final TrainRepository trainRepository;        // 3. final dependency field

    public TrainController(TrainRepository trainRepository) {
                                                          // 4. Spring injects the repository bean
        this.trainRepository = trainRepository;
    }

    @PostMapping                                          // 5. POST /api/trains
    @PreAuthorize("hasRole('ADMIN')")                     // 6. admin only
    @Transactional                                        // 7. one transaction
    public ResponseEntity<Train> createTrain(@Valid @RequestBody Train request) {
                                                          // 8. JSON -> Train, then validate
        Train train = new Train();                        // 9. fresh entity; createdAt = now
        train.setTrainId("TRN-" + UUID.randomUUID());     // 10. server-side ID
        copyTrainFields(request, train);                  // 11. copy editable fields
        return ResponseEntity.status(HttpStatus.CREATED).body(trainRepository.save(train));
                                                          // 12. INSERT, respond 201 + body
    }

    @PutMapping("/{id}")                                  // 13. PUT /api/trains/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Train> updateTrain(@PathVariable String id, @Valid @RequestBody Train request) {
                                                          // 14. {id} -> method parameter
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Train not found"));
                                                          // 15. load or throw
        copyTrainFields(request, train);                  // 16. overwrite editable fields
        return ResponseEntity.ok(trainRepository.save(train));
                                                          // 17. UPDATE, respond 200 + body
    }

    @GetMapping("/{id}")                                  // 18. GET /api/trains/{id}
    @Transactional(readOnly = true)                       // 19. read-only transaction
    public ResponseEntity<Train> getTrainById(@PathVariable String id) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Train not found"));
        return ResponseEntity.ok(train);                  // 20. JSON + 200
    }

    @GetMapping                                           // 21. GET /api/trains
    @Transactional(readOnly = true)
    public ResponseEntity<List<Train>> getAllTrains() {
        return ResponseEntity.ok(trainRepository.findAll()); // 22. JSON array + 200
    }

    @DeleteMapping("/{id}")                               // 23. DELETE /api/trains/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteTrain(@PathVariable String id) {
        if (!trainRepository.existsById(id)) {            // 24. friendly not-found path
            throw new IllegalArgumentException("Train not found");
        }
        trainRepository.deleteById(id);                   // 25. real DELETE
        return ResponseEntity.noContent().build();        // 26. 204, empty body
    }

    private void copyTrainFields(Train request, Train train) {
                                                          // 27. helper, no HTTP mapping
        train.setTrainNumber(request.getTrainNumber());   // 28. required field
        train.setCapacity(request.getCapacity());
        train.setTotalCoaches(request.getTotalCoaches());
        train.setManufactureYear(request.getManufactureYear());
        if (request.getIsActive() != null) {              // 29. absent -> keep old value
            train.setIsActive(request.getIsActive());
        }
    }
}
```

### What each method does, step by step

**`createTrain`**

1. Role check: `ROLE_ADMIN` or 403.
2. Jackson builds a `Train` from the body.
3. `@Valid` checks `trainNumber`; blank -> 400 and the method never runs.
4. `new Train()` runs the field initialisers: `createdAt = now`, `isActive = true`.
5. `"TRN-" + UUID.randomUUID()` sets the primary key.
6. `copyTrainFields` copies number, capacity, coaches, year, and (if present) active flag.
7. `save` inserts the row.
8. Response is `201` with the saved entity, including the generated ID and `createdAt`.

Fields the client sends but the code ignores on create: `trainId` (overwritten) and `createdAt` (never copied).

**`updateTrain`**

1. `findById` returns `Optional<Train>`.
2. `orElseThrow` supplies the error for a missing row.
3. The four editable fields are copied onto the loaded entity.
4. `isActive` is copied only if not `null`.
5. `save` runs an UPDATE; the ID cannot change.
6. `createdAt` stays whatever it was originally, because it is not copied.

`trainNumber` is required on update too, because `@Valid` runs on every method that declares it.

**`getTrainById`**

Reads one row. `readOnly = true` is a performance hint for Hibernate.

**`getAllTrains`**

Returns every train, active or not, with no ordering. For 8 demo rows this is fine.

**`deleteTrain`**

Checks existence, then deletes. If a `Schedule` or a `MaintenanceIssue` still points at this train, PostgreSQL rejects the DELETE and the response is 500. See section 11.

---

## 9. One request from start to finish

Real values for `POST /api/trains`:

```http
POST /api/trains HTTP/1.1
Host: localhost:8080
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "trainNumber": "M9",
  "capacity": 1800,
  "totalCoaches": 8,
  "manufactureYear": 2025,
  "isActive": true
}
```

1. **TCP connection accepted** by the embedded Tomcat on port 8080.
2. **Security filters run.** The `Authorization` header is Base64-decoded to `admin:admin123`.
3. `DbUserDetailsService` runs `SELECT ... FROM "User" WHERE "Contact" = 'admin'`.
4. BCrypt compares `admin123` with the stored hash. Match, so the request carries `ROLE_ADMIN`.
5. **Spring MVC matches the URL.** `POST /api/trains` maps to `createTrain` through `@RequestMapping` + `@PostMapping`.
6. **`@PreAuthorize` passes**, because the user has `ROLE_ADMIN`.
7. **Jackson deserializes** the body into a `Train` request object: `trainNumber="M9"`, `capacity=1800`, `totalCoaches=8`, `manufactureYear=2025`, `isActive=true`.
8. **`@Valid` runs.** `trainNumber` is non-blank, so validation passes.
9. **`new Train()` executes.** At this instant `createdAt` becomes something like `2026-10-29T10:42:17.318` and `isActive` becomes `true`.
10. `setTrainId("TRN-" + UUID.randomUUID())` produces e.g. `TRN-9c1d4a6e-2b77-4f0a-8e51-73f0c9a2d114`.
11. **`copyTrainFields`** writes `M9`, `1800`, `8`, `2025`, and `true` onto the new entity.
12. **`trainRepository.save(train)`** is called.
13. Hibernate sees a `Train` whose ID is not yet in the session. It runs a SELECT to be safe, finds nothing, and schedules an INSERT.
14. **At commit time** (end of the `@Transactional` method) it flushes:
    `INSERT INTO "Trains" ("TrainID","TrainNumber","Capacity","TotalCoaches","ManufactureYear","CreatedAt","isActive") VALUES ('TRN-9c1d...','M9',1800,8,2025,'2026-10-29 10:42:17.318',true)`.
15. **PostgreSQL checks the unique index** `UX_Train_Number`. `M9` is new, so the insert succeeds.
16. **Jackson serializes** the saved entity field by field using the getters.
17. `ResponseEntity.status(HttpStatus.CREATED)` writes status `201` and the JSON body.
18. Swagger shows the new `trainId`, which you can now use in `GET`, `PUT`, `DELETE`, or in a Schedule.

If `M9` already existed, step 15 would fail and the response would be 500, not 400, because the controller never pre-checks duplicates.

---

## 10. JSON request and response examples

### Create — request

```json
{
  "trainNumber": "M9",
  "capacity": 1800,
  "totalCoaches": 8,
  "manufactureYear": 2025,
  "isActive": true
}
```

| Field | Type | Required | Meaning |
|---|---|---|---|
| `trainNumber` | string, max 50 | Yes | Fleet number, unique in the table. |
| `capacity` | integer | No | Passenger capacity. |
| `totalCoaches` | integer | No | Number of coaches. |
| `manufactureYear` | integer | No | Year built. |
| `isActive` | boolean | No | Omit -> stays `true`. |
| `trainId` | string | Ignored on create | Server generates it. |
| `createdAt` | string | Ignored on create | Server sets it. |

### Create — response `201 Created`

```json
{
  "trainId": "TRN-9c1d4a6e-2b77-4f0a-8e51-73f0c9a2d114",
  "trainNumber": "M9",
  "capacity": 1800,
  "totalCoaches": 8,
  "manufactureYear": 2025,
  "createdAt": "2026-10-29T10:42:17.318",
  "isActive": true
}
```

| Field | Origin |
|---|---|
| `trainId` | Generated in the controller. |
| `trainNumber` | From the request. |
| `capacity` | From the request. |
| `totalCoaches` | From the request. |
| `manufactureYear` | From the request. |
| `createdAt` | From `new Train()`, not from the client. Format is ISO-8601 local date-time. |
| `isActive` | From the request, or `true`. |

### Update — request

```http
PUT /api/trains/TRN-9c1d4a6e-2b77-4f0a-8e51-73f0c9a2d114
```

```json
{
  "trainNumber": "M9",
  "capacity": 1600,
  "totalCoaches": 6,
  "manufactureYear": 2025,
  "isActive": false
}
```

Response is `200 OK` with the updated body. `createdAt` is unchanged. `trainId` is unchanged. Sending a different `trainId` in the body does nothing, because it is never copied.

### List — response

```http
GET /api/trains
Authorization: Basic cHJpeWE6UGFzc3cwcmQh
```

```json
[
  {
    "trainId": "TRN-101",
    "trainNumber": "M1",
    "capacity": 1800,
    "totalCoaches": 8,
    "manufactureYear": 2014,
    "createdAt": "2026-09-29T10:00:00",
    "isActive": true
  },
  {
    "trainId": "TRN-201",
    "trainNumber": "A1",
    "capacity": 2400,
    "totalCoaches": 8,
    "manufactureYear": 2022,
    "createdAt": "2026-09-29T10:00:00",
    "isActive": true
  }
]
```

The `[...]` is a JSON array because the method returns `List<Train>`.

In the seeder, `createdAt` is deliberately set to `LocalDateTime.now().minusMonths(1)`, so all eight demo trains share almost the same timestamp.

### Delete — response

`204 No Content`, empty body.

---

## 11. Error table

| Situation | Status | Body |
|---|---|---|
| No `Authorization` header | 401 | empty + `WWW-Authenticate: Basic` |
| Bad username or password | 401 | empty + `WWW-Authenticate: Basic` |
| Passenger or agent calls POST/PUT/DELETE | 403 | `{"error":"You do not have permission to access this resource"}` |
| `trainNumber` missing / empty / spaces | 400 | `{"trainNumber":"Train number is required"}` |
| Unknown `{id}` on GET, PUT, or DELETE | 400 | `{"error":"Train not found"}` |
| Duplicate `trainNumber` | 500 | `{"error":"An unexpected error occurred: ...UX_Train_Number..."}` |
| Delete a train used by a schedule | 500 | `{"error":"An unexpected error occurred: ...FK_Schedule_Train..."}` |
| Delete a train used by a maintenance issue | 500 | `{"error":"An unexpected error occurred: ...FK_Maintenance_Train..."}` |
| Malformed JSON | 500 | `{"error":"An unexpected error occurred: ..."}` |

### Where these come from

- **401** is produced by Spring Security before the controller is reached.
- **403** is produced by method security or by `GlobalExceptionHandler` catching `AccessDeniedException`.
- **400 for validation** comes from `GlobalExceptionHandler.handleValidationExceptions`, which builds a map of field name -> message.
- **400 for missing rows** comes from `handleIllegalArgumentException`, because the controller throws `IllegalArgumentException`.
- **500 for duplicates and foreign keys** comes from the catch-all `handleGenericException`, because the controller never checks these conditions in advance.
- **500 for malformed JSON** also comes from the catch-all: unreadable JSON throws `HttpMessageNotReadableException`, which has no dedicated handler here.

### Why 400 instead of 404 for a missing train

It is the project convention, not a rule of HTTP. Every controller throws `IllegalArgumentException("... not found")`, and the global handler maps that class to `400 Bad Request`. A stricter design would throw `ResourceNotFoundException`, which the same handler maps to `404`. Mention this honestly if an evaluator asks.

### Why deleting `TRN-101` fails

The demo data has:

```java
schedule("SCH-001", "TRN-101", "RT-L1", "DAILY", ...);
issue("ISS-SEED1", "TRN-101", null, maint, "TRAIN", ...);
```

Both rows hold a foreign key to `"Trains"`. PostgreSQL refuses the DELETE to protect them. There is no `ON DELETE CASCADE`, so nothing is silently lost. You must delete or repoint the schedule and the issue first.

---

## 12. Relationships to other groups

| Other group | Field | Foreign key | Meaning |
|---|---|---|---|
| Schedule | `train` -> `"TrainID"` | `FK_Schedule_Train` | Which train runs this timetable entry. |
| MaintenanceIssue | `train` -> `"TrainID"` | `FK_Maintenance_Train` | Which train has the fault. |
| Route | no direct field | none | A train reaches a route only through `Schedule.route`. |
| Station | no direct field | none | A train reaches stations through its route's stops. |

### How the chain works

```
Train  --(Schedule)-->  Route  --(RouteStation)-->  Station
```

1. `Schedule` has `trainId` and `routeId` foreign keys.
2. `Route` has `startStation`, `endStation`, and a list of `RouteStation` rows.
3. `RouteStation` points at a `Station`.

So "where is train M1 right now?" is not answered by the `Train` table alone. You read the train's schedule, then the route, then the ordered stops.

### JSON nesting

When a schedule is returned, the train object is embedded:

```json
{
  "scheduleId": "SCH-001",
  "dayOfWeek": "DAILY",
  "train": {
    "trainId": "TRN-101",
    "trainNumber": "M1",
    "capacity": 1800,
    "totalCoaches": 8,
    "manufactureYear": 2014,
    "createdAt": "2026-09-29T10:00:00",
    "isActive": true
  },
  "route": { "routeId": "RT-L1", "routeName": "Mumbai Metro Line 1 ..." }
}
```

Maintenance issues nest a train the same way when the issue is about a train.

### Why `Train` has no list of schedules

The mapping is unidirectional: `Schedule` knows its `Train`; `Train` does not know its schedules. That avoids circular JSON (`train -> schedules -> train -> ...`) and keeps the entity small. The cost is that you cannot navigate from a train to its schedules in Java without a repository query.

---

## 13. Seed data

`DataSeeder.seedTrains()` inserts 8 trains:

```java
private void seedTrains() {
    train("TRN-101", "M1", "Blue Line", 1800, 8, 2014);
    train("TRN-102", "M2", "Blue Line", 1800, 8, 2016);
    train("TRN-103", "M3", "Blue Line", 1800, 8, 2019);
    train("TRN-201", "A1", "Aqua Line", 2400, 8, 2022);
    train("TRN-202", "A2", "Aqua Line", 2400, 8, 2023);
    train("TRN-203", "A3", "Aqua Line", 2400, 8, 2024);
    train("TRN-301", "R1", "Red Line", 1800, 8, 2020);
    train("TRN-302", "R2", "Red Line", 1800, 8, 2021);
}
```

| TrainID | Number | Line in the call | Capacity | Coaches | Year |
|---|---|---|---|---|---|
| `TRN-101` | `M1` | Blue | 1800 | 8 | 2014 |
| `TRN-102` | `M2` | Blue | 1800 | 8 | 2016 |
| `TRN-103` | `M3` | Blue | 1800 | 8 | 2019 |
| `TRN-201` | `A1` | Aqua | 2400 | 8 | 2022 |
| `TRN-202` | `A2` | Aqua | 2400 | 8 | 2023 |
| `TRN-203` | `A3` | Aqua | 2400 | 8 | 2024 |
| `TRN-301` | `R1` | Red | 1800 | 8 | 2020 |
| `TRN-302` | `R2` | Red | 1800 | 8 | 2021 |

### The trap in the seeder helper

```java
private void train(String id, String number, String line, int capacity, int coaches, int year) {
    Train train = new Train();
    train.setTrainId(id);
    train.setTrainNumber(number);
    train.setCapacity(capacity);
    train.setTotalCoaches(coaches);
    train.setManufactureYear(year);
    train.setCreatedAt(LocalDateTime.now().minusMonths(1));
    train.setIsActive(true);
    trainRepository.save(train);
}
```

The parameter `line` is **never used**. There is no `setLineColor` on `Train`, because the column does not exist. The word "Blue Line" in the call is documentation for the reader only. This surprises almost everyone reading the seeder for the first time, so know it before a viva.

The actual line association is created later by `seedSchedules()`, which pairs each train ID with a route ID, and the route carries `lineColor`.

Seed trains also get `createdAt` one month in the past, so the data looks older than the run.

The seeder is guarded: `run()` returns early if `stationRepository.count() > 0`, so it will not duplicate these eight rows on a second start.

---

## 14. Swagger test steps

Open `http://localhost:8080/swagger-ui.html`.

1. Click **Authorize**. Username `admin`, password `admin123`.
2. Open **train-controller**.
3. `POST /api/trains` -> **Try it out** -> paste:

```json
{
  "trainNumber": "M9",
  "capacity": 1800,
  "totalCoaches": 8,
  "manufactureYear": 2025,
  "isActive": true
}
```

4. **Execute.** Expect `201` and a `trainId` starting with `TRN-`.
5. Copy the `trainId`.
6. `GET /api/trains/{id}` -> paste it -> **Execute** -> expect `200` and the same fields plus `createdAt`.
7. `GET /api/trains` -> **Execute** -> expect an array that now has 9 entries.
8. `PUT /api/trains/{id}` -> paste the ID and a body with `"capacity": 1600, "totalCoaches": 6` -> **Execute** -> expect `200` with the new values.
9. Try `PUT` with `"trainNumber": ""` -> expect `400` and `{"trainNumber":"Train number is required"}`.
10. `DELETE /api/trains/{id}` -> **Execute** -> expect `204`, because nothing references the new train.
11. `DELETE /api/trains/TRN-101` -> **Execute** -> expect `500`, because `SCH-001` and `ISS-SEED1` reference it.
12. Authorize as `priya` / `Passw0rd!`, then `POST /api/trains` -> expect `403`.
13. Authorize as `agent1` / `Passw0rd!`, then `GET /api/trains` -> expect `200`, because reads are open to every logged-in role.

`curl` for step 3:

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/trains \
  -H "Content-Type: application/json" \
  -d '{"trainNumber":"M9","capacity":1800,"totalCoaches":8,"manufactureYear":2025,"isActive":true}'
```

---

## 15. How to explain this in 2 minutes

> "Train is a simple master table. One row is one physical rake.
>
> The entity is `entity/Train.java`. `@Entity` maps it to a table and `@Table(name = "\"Trains\"")` gives the exact quoted name. The primary key is a string `trainId`, generated by the controller as `TRN-` plus a UUID. `trainNumber` is the public fleet number like `M1`; it has `@NotBlank` for a friendly 400 and a unique index `UX_Train_Number` so two trains cannot share a number. Capacity, total coaches, and manufacture year are `Integer` because the columns are nullable and `int` cannot hold null. `createdAt` is initialised to `LocalDateTime.now()` when the object is created, and the controller never copies it from the request, so clients cannot backdate a train. `isActive` defaults to true and lets us withdraw a train without deleting its history.
>
> Deliberately, Train has no route or line column. A rake can serve different lines, so the assignment is a separate Schedule row that points at one train and one route. That is why the seeder helper takes a `line` argument but never uses it — the line is stored on Route, not Train.
>
> The repository is a one-line interface extending `JpaRepository<Train, String>`. Spring Data generates the implementation, so we get `save`, `findById`, `findAll`, `existsById`, `deleteById`, and `count` with no SQL written by hand. Because we assign the ID ourselves, Spring Data internally uses `merge`, so the SQL log may show a SELECT before the INSERT; the end result is still one new row.
>
> The controller exposes five endpoints under `/api/trains`. POST, PUT, and DELETE are `@PreAuthorize("hasRole('ADMIN')")`; GET is open to any authenticated user. Each method is `@Transactional`. `@Valid @RequestBody` parses and validates the JSON before the body runs. Create makes a new object, sets the ID, copies the editable fields through a helper, and saves. Update loads the row, throws `IllegalArgumentException` if missing, copies fields including optional `isActive`, and saves. Delete checks existence then deletes.
>
> The database protects us: `Schedule` and `MaintenanceIssue` have foreign keys to `Trains`, so deleting `TRN-101` fails with 500 because the demo schedule `SCH-001` and issue `ISS-SEED1` still use it. There is no cascade, so history is safe. A duplicate train number also gives 500, because we rely on the unique index instead of pre-checking — that is the honest limitation of this controller."

---

## 16. Beginner Q&A

**Q1. What is the difference between `trainId` and `trainNumber`?**
`trainId` is the internal database key, e.g. `TRN-101`. `trainNumber` is the public fleet number, e.g. `M1`. The ID never changes; the number is what humans read.

**Q2. Why does the controller generate the ID instead of the database?**
It uses `"TRN-" + UUID.randomUUID()`, which gives readable prefixed keys and needs no sequence. The trade-off is that Spring Data cannot treat a non-null ID as "new", so `save` may run a SELECT before the INSERT.

**Q3. What does `@RequestBody` do?**
It tells Spring to read the HTTP body and let Jackson convert the JSON into a `Train` object.

**Q4. What does `@PathVariable` do?**
It takes a piece of the URL and passes it as a method argument. In `/api/trains/TRN-101`, `id` becomes `"TRN-101"`.

**Q5. Why is `@Valid` needed if the field already has `@NotBlank`?**
`@NotBlank` is only a rule. `@Valid` is the switch that tells Spring to enforce rules on this argument. Without `@Valid`, the check would not run.

**Q6. What happens if I send `"capacity": -5`?**
It is accepted and stored. There is no `@Min(0)` on `capacity`. Validation only covers `trainNumber`. This is a fair criticism to acknowledge.

**Q7. Why is `capacity` an `Integer` and not an `int`?**
`Integer` can be `null`. The column is nullable, and a missing value should stay missing, not become `0`.

**Q8. Why is `createdAt` not copied from the request?**
So the server owns the timestamp. `new Train()` sets it, `copyTrainFields` skips it, and a client-supplied `createdAt` is discarded.

**Q9. Why does the JSON show `isActive` and not `active`?**
Jackson derives the property name from `getIsActive()`, which gives `isActive`. It matches the column name, which keeps the API predictable.

**Q10. Is a train tied to one route?**
No. The link is per schedule. One train can appear in several `Schedule` rows with different routes and times. That is why `Train` has no `routeId`.

**Q11. Then why does the seeder pass `"Blue Line"` to the train helper?**
It is leftover documentation. The parameter is unused and there is no column to store it. The real line comes from the route in the schedule.

**Q12. Which role can create a train?**
Only `ADMIN`. `@PreAuthorize("hasRole('ADMIN')")` runs before the method. Any other role gets 403.

**Q13. Who can read trains?**
Any authenticated user, because `SecurityConfig` only requires `.anyRequest().authenticated()` and this controller adds no `@PreAuthorize` on GET.

**Q14. Why does deleting `TRN-101` fail?**
`Schedule` and `MaintenanceIssue` rows reference it through foreign keys. PostgreSQL blocks the delete. The API returns 500 through the catch-all handler.

**Q15. How would I delete it properly?**
Delete or reassign its schedules and maintenance issues first, then delete the train. Alternatively, set `isActive = false` and keep the row.

**Q16. Why do two trains with the same number give 500 instead of 400?**
The controller does not check for duplicates. The unique index rejects the INSERT and PostgreSQL raises a constraint violation, which the catch-all handler reports as 500. Adding `existsByTrainNumber` before saving would give a cleaner 400.

**Q17. What is the `@Transactional` on a read method for?**
It defines the transaction boundary so the Hibernate session stays open for the read. `readOnly = true` additionally tells Hibernate not to bother dirty-checking.

**Q18. Why does the project return the saved entity instead of just the ID?**
It saves the client a follow-up GET. The response includes the generated `trainId` and `createdAt`, which the client could not know in advance.

**Q19. Can I send `"trainId": "TRN-999"` on create?**
Yes, but it is ignored. `createTrain` overwrites it with a generated value. The field is not protected by `@JsonProperty(access = READ_ONLY)`, so it quietly accepts and discards the value.

**Q20. Is this entity using Lombok?**
No. All getters and setters are written by hand. Lombok would remove about 40 lines but adds a build-time dependency; this project keeps plain Java so every line is visible.

---

## 17. Glossary

| Term | Meaning here |
|---|---|
| **Bean** | Object created and managed by Spring. |
| **BCrypt** | One-way password hashing used for login. |
| **Bean Validation** | The `@NotBlank` / `@NotNull` checking system. |
| **Capacity** | Number of passengers a train can carry. |
| **Coach** | One car of a train. |
| **Column** | One field of a table. |
| **Controller** | Class that maps URLs to methods. |
| **CRUD** | Create, Read, Update, Delete. |
| **`ddl-auto=validate`** | Hibernate setting that checks entities against the real schema and refuses to start on a mismatch. |
| **Deserialization** | JSON -> Java object. |
| **Entity** | Class mapped to a table. |
| **FK** | Foreign key. |
| **Flyway** | Applies the numbered SQL migrations. |
| **Foreign key** | Column that must exist as a primary key elsewhere. |
| **Hibernate** | JPA implementation that writes the SQL. |
| **HTTP status** | Numeric result, e.g. 201. |
| **Idempotent** | Repeating the operation gives the same end state. PUT and DELETE should be idempotent. |
| **Jackson** | Java <-> JSON converter. |
| **JPA** | Java standard for object-table mapping. |
| **JSON** | Text format with `{}` objects and `[]` arrays. |
| **Lazy loading** | Fetching a related row only when it is read. |
| **merge** | JPA operation that copies state onto a managed entity; may SELECT first. |
| **Migration** | One versioned SQL file. |
| **ORM** | Object-relational mapping. |
| **Path variable** | Value taken from the URL path. |
| **Primary key** | Unique row identifier. |
| **Rake** | A complete train set of coaches. |
| **Repository** | Interface providing database methods. |
| **Role** | Permission label such as `ADMIN`. |
| **Seeder** | Class that inserts demo data at startup. |
| **Serialization** | Java object -> JSON. |
| **Soft delete** | Setting `isActive = false` instead of deleting. |
| **Swagger UI** | Generated page for trying endpoints. |
| **Transaction** | All-or-nothing group of database changes. |
| **Unique index** | Rule that values may not repeat. |
| **UUID** | 128-bit random identifier written as 36 characters. |

---

## 18. One-line summary

`Train` stores the fleet — ID, number, capacity, coaches, year, creation time, active flag — with a unique fleet number, a one-line `JpaRepository`, and five admin-guarded REST endpoints, while its route and line live in `Schedule` and its faults live in `MaintenanceIssue`, so foreign keys stop you from deleting a train that is still in use.

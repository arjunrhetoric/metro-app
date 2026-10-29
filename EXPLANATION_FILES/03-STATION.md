# 03 — Station Group

**In one sentence:** a Station is one physical metro stop, and almost every other table in this project points at it.

**Files in this group**

| File | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/Station.java` |
| Controller | `src/main/java/com/metro/ticketing/controller/StationController.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/StationRepository.java` |
| Database table | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Seed data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` |

---

## 1. Real-life analogy

Think of a metro station as a **building with a nameplate**.

- The building has a permanent internal number written in the government register. Nobody outside sees it, but every legal paper uses it. That is `StationID`.
- The building also has a short public code painted on the wall, like `VSV`. Passengers and staff use it. That is `StationCode`. Two buildings cannot share the same painted code.
- The building sits at one spot on the map, so it has latitude and longitude.
- The building has an address, an opening date, and belongs to one colour line.
- The building can be closed for repairs without being demolished. That is `isActive = false`.

Now think about everything else in a metro system: routes start and end at buildings, fares are prices "from building A to building B", tickets record which two buildings you travelled between, staff are posted to a building, and broken escalators are reported against a building. All of those are **pointers to the building register**. This is why Station is created first and deleted last.

In database words: Station is the **parent table**, and the others hold **foreign keys** that point to it.

---

## 2. Words you need

Read this table once. Every later section uses only these words.

| Word | One-sentence meaning |
|---|---|
| Spring Boot | A Java framework that starts a web server and wires your classes together for you. |
| Annotation | A word starting with `@` placed above a class, field, or method that gives the framework extra instructions. |
| Bean | An object that Spring creates and manages instead of you calling `new`. |
| Dependency injection | Spring hands an object its dependencies through the constructor instead of the object building them itself. |
| Controller | The class that receives HTTP requests and sends HTTP responses. |
| Endpoint | One URL plus one HTTP method, for example `POST /api/stations`. |
| HTTP method | The verb of a request: GET means read, POST means create, PUT means replace/update, DELETE means remove. |
| JSON | A text format for data, built from `{ }` objects, `[ ]` arrays, and `"key": value` pairs. |
| Serialization | Turning a Java object into JSON so it can travel over the network. |
| Deserialization | Turning incoming JSON text into a Java object. |
| Jackson | The library Spring uses to do serialization and deserialization. |
| JPA | Java Persistence API: the standard rules for mapping Java classes to database tables. |
| Hibernate | The actual library that implements JPA and writes the SQL. |
| ORM | Object-Relational Mapping: pretending a table is a class and a row is an object. |
| Entity | A Java class marked `@Entity` that JPA maps to one database table. |
| Table | A grid of rows and columns inside the database. |
| Column | One named field of a table, for example `StationCode`. |
| Row / record | One entry in a table; here, one station. |
| Primary key | The column that uniquely identifies each row. |
| Foreign key | A column whose value must exist as a primary key in another table; the database rejects anything else. |
| Unique index | A database rule that says no two rows may share this value. |
| Repository | An interface that gives you database methods without you writing SQL. |
| `JpaRepository` | A Spring Data interface with ready-made `save`, `findById`, `findAll`, `deleteById`, and more. |
| Query method | A repository method whose name Spring reads and turns into SQL, like `findByRoute_RouteId`. |
| Transaction | A group of database changes that either all succeed or all roll back. |
| `@Transactional` | Tells Spring to wrap the method in one transaction. |
| Bean Validation | The standard `@NotBlank`, `@NotNull`, `@Size` checks on fields. |
| `@Valid` | Tells Spring to run those validation checks on the incoming JSON body. |
| HTTP status code | A number that says what happened: 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Server Error. |
| `ResponseEntity` | A Spring class that lets you control both the status code and the response body. |
| `Optional` | A box that may or may not contain a value; forces you to handle "not found". |
| UUID | A long random string that is practically never duplicated. |
| `LocalDate` | A date without time, for example `2024-01-01`. |
| `LocalDateTime` | A date plus time, for example `2026-10-29T10:15:30`. |
| Basic authentication | Sending `Authorization: Basic base64(username:password)` on every request. |
| Role | A label on a user, here `ADMIN`, `TICKET_AGENT`, `MAINTENANCE_STAFF`, or `PASSENGER`. |
| `@PreAuthorize` | A check that runs before a method and blocks callers without the required role. |
| Flyway | A tool that runs numbered SQL files (`V1`, `V2`, ...) to build the database schema. |
| Migration | One of those numbered SQL files. |
| Seeder | A class that inserts demo data at startup if the database is empty. |
| Soft delete | Marking a row inactive instead of actually deleting it. |
| `nullable = false` | A JPA instruction that the database column must not be empty. |
| Swagger UI | A web page that lists the API and lets you send test requests. |
| DTO | Data Transfer Object: a separate class just for request/response shapes. This project mostly does **not** use DTOs; it reuses entities. |

---

## 3. Files in this group

```
src/main/java/com/metro/ticketing/
├── entity/Station.java              <- the shape of one station
├── repository/StationRepository.java <- database access
└── controller/StationController.java <- the 5 web endpoints
src/main/resources/db/migration/
└── V1__create_metro_ticketing_schema.sql  <- creates the "Stations" table
src/main/java/com/metro/ticketing/config/
└── DataSeeder.java                  <- inserts 18 demo stations
```

Three layers, one job each:

1. **Controller** talks HTTP. It never writes SQL.
2. **Repository** talks database. It never knows about HTTP.
3. **Entity** is the shared shape used by both.

---

## 4. Database table

This is the exact SQL that creates the table (from `V1__create_metro_ticketing_schema.sql`):

```sql
CREATE TABLE IF NOT EXISTS "Stations" (
    "StationID" VARCHAR(50) PRIMARY KEY,
    "StationCode" VARCHAR(20) NOT NULL,
    "Latitude" DOUBLE PRECISION,
    "Longitude" DOUBLE PRECISION,
    "Address" VARCHAR(255),
    "OpenedDate" DATE,
    "LineColor" VARCHAR(50),
    "isActive" BOOLEAN DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS "UX_Station_Code" ON "Stations" ("StationCode");
```

### Column by column

| Column | SQL type | Meaning | Can it be empty? |
|---|---|---|---|
| `StationID` | `VARCHAR(50)` | Internal identifier, primary key. Generated by the controller as `STN-<uuid>`. | No |
| `StationCode` | `VARCHAR(20)` | Short public code such as `VSV`, `AND`, `GHP`. | No |
| `Latitude` | `DOUBLE PRECISION` | GPS latitude, e.g. `19.1354`. | Yes |
| `Longitude` | `DOUBLE PRECISION` | GPS longitude, e.g. `72.8209`. | Yes |
| `Address` | `VARCHAR(255)` | Street address. Demo data stores `"<Name> - <address>"` here. | Yes |
| `OpenedDate` | `DATE` | Day the station opened. | Yes |
| `LineColor` | `VARCHAR(50)` | Line label: `Blue`, `Aqua`, `Red`, or `Interchange`. | Yes |
| `isActive` | `BOOLEAN` | `true` = open, `false` = closed. Default `TRUE`. | Yes (defaults) |

### Why the quotes

`"Stations"` is in double quotes because the name starts with a capital letter and has mixed case.

In PostgreSQL, an **unquoted** name is folded to lowercase. So `Stations` would become `stations`. The project wants the exact mixed-case name, so every layer quotes it:

- SQL: `"Stations"`
- JPA: `@Table(name = "\"Stations\"")` — the `\"` is just a Java string escape for one `"`.
- Properties file: `spring.jpa.hibernate.naming.physical-strategy=...PhysicalNamingStrategyStandardImpl` stops Hibernate from renaming things.

If any one of those three disagreed, startup would fail, because `spring.jpa.hibernate.ddl-auto=validate` makes Hibernate check the real schema before the app runs.

### Why a unique index instead of `UNIQUE` on the column

`CREATE UNIQUE INDEX` and a `UNIQUE` column constraint do the same checking job. An index is used here because it is named (`UX_Station_Code`), so a database error message can point at a meaningful name.

---

## 5. Entity line by line

Full file, with a comment on every annotation and field:

```java
package com.metro.ticketing.entity;                    // this class lives in the entity package

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Jackson import
import jakarta.persistence.Column;                    // JPA: rename/limit a column
import jakarta.persistence.Entity;                    // JPA: "this class is a table"
import jakarta.persistence.Id;                        // JPA: "this field is the primary key"
import jakarta.persistence.Table;                     // JPA: "this is the table name"
import jakarta.validation.constraints.NotBlank;       // validation: string must not be blank

import java.time.LocalDate;                           // date without time

@Entity                                               // 1. register this class as a JPA entity
@Table(name = "\"Stations\"")                         // 2. map it to the table named "Stations" (quoted)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
                                                      // 3. when writing JSON, skip two Hibernate-internal
                                                      //    properties that exist on proxy objects
public class Station {                                // 4. plain Java class, no superclass needed

    @Id                                               // 5. this field is the primary key
    @Column(name = "\"StationID\"", length = 50)      // 6. column "StationID", max 50 characters
    private String stationId;                         // 7. String because IDs are "STN-<uuid>"

    @NotBlank(message = "Station code is required")   // 8. if blank or missing -> 400 with this message
    @Column(name = "\"StationCode\"", length = 20, nullable = false)
                                                      // 9. column "StationCode", max 20, NOT NULL in DB
    private String stationCode;                       // 10. short code, e.g. "VSV"

    @Column(name = "\"Latitude\"")                    // 11. column "Latitude"
    private Double latitude;                          // 12. Double (object) so null is possible

    @Column(name = "\"Longitude\"")                   // 13. column "Longitude"
    private Double longitude;                         // 14. Double, nullable

    @Column(name = "\"Address\"")                     // 15. column "Address", default VARCHAR(255)
    private String address;                           // 16. free text

    @Column(name = "\"OpenedDate\"")                  // 17. column "OpenedDate", SQL DATE
    private LocalDate openedDate;                     // 18. Java date, no time part

    @Column(name = "\"LineColor\"", length = 50)       // 19. column "LineColor", max 50
    private String lineColor;                         // 20. "Blue" / "Aqua" / "Red" / "Interchange"

    @Column(name = "\"isActive\"")                    // 21. column "isActive"
    private Boolean isActive = true;                  // 22. starts true for every brand-new object

    // ----- getters and setters: plain Java, no annotations -----

    public String getStationId() {                    // Jackson calls this to write "stationId"
        return stationId;
    }

    public void setStationId(String stationId) {      // Jackson calls this when reading JSON
        this.stationId = stationId;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(LocalDate openedDate) {
        this.openedDate = openedDate;
    }

    public String getLineColor() {
        return lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public Boolean getIsActive() {                    // property name in JSON becomes "isActive"
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
```

### The annotations, one by one

| Annotation | Plain English |
|---|---|
| `@Entity` | "Hibernate, please treat this class as a table." Without it, the class is just a normal Java class. |
| `@Table(name = "\"Stations\"")` | "The table name is exactly `Stations`, capital S, with quotes." |
| `@JsonIgnoreProperties(...)` | "When converting to JSON, ignore `hibernateLazyInitializer` and `handler`." Hibernate sometimes wraps objects in a proxy that has these extra fields; without this line the JSON would contain junk. |
| `@Id` | "This column is the primary key." Exactly one per entity. |
| `@Column(name = ...)` | "The database column has this name." The Java field name and the column name do not have to match. |
| `@Column(length = 50)` | "Use `VARCHAR(50)`." |
| `@Column(nullable = false)` | "Add `NOT NULL`." |
| `@NotBlank` | "Reject `null`, `""`, and `"   "` before the code even reaches the database." |
| `@Column(precision, scale)` | Not used here; used in `Route` to declare an exact decimal `NUMERIC(10,2)`. |

### Why `Double` and not `double`

`double` is a primitive and cannot be `null`. The database column is allowed to be empty, so the Java field must be able to hold "no value". `Double` (capital D) is the object version and can be `null`.

The same reasoning applies to `Boolean isActive`. Because it is a `Boolean`, the controller can tell the difference between:

- `"isActive": false` -> the client really wants it off
- field not sent at all -> `null` -> keep the existing value

With a primitive `boolean`, a missing field would silently arrive as `false` and switch the station off. That is a real bug this design avoids.

### Why `LocalDate` and not `java.util.Date`

`LocalDate` is the modern Java 8 date class. It means "a calendar day" with no time and no timezone. Hibernate maps it to SQL `DATE` automatically. `java.util.Date` is the old, confusing class and is avoided.

### Why IDs are `String`

The project uses readable prefixed IDs: `STN-...`, `TRN-...`, `RT-...`, `RS-...`, `TKT-...`. A `BIGSERIAL` auto-increment number cannot express those prefixes, and a string ID lets the seeder use fixed values like `STN-VSV` that other seed rows can reference by name.

---

## 6. Repository line by line

```java
package com.metro.ticketing.repository;               // repository package

import com.metro.ticketing.entity.Station;            // the entity this repository manages
import org.springframework.data.jpa.repository.JpaRepository; // ready-made CRUD interface
import org.springframework.stereotype.Repository;     // marks this as a Spring bean

@Repository                                           // 1. Spring will create a bean for this interface
public interface StationRepository extends JpaRepository<Station, String> {
                                                      // 2. Station = entity, String = type of the ID
}
```

That is the whole file. There is **no** `class StationRepositoryImpl`. Spring Data JPA generates an implementation at startup, using a dynamic proxy.

### The two type parameters

`JpaRepository<Station, String>`:

- `Station` — which entity. This tells Spring the table is `"Stations"` and the columns come from `Station`.
- `String` — the type of the primary key. `findById("STN-VSV")` therefore takes a `String`.

### Every inherited method the controller uses, and the SQL behind it

| Java call | SQL that Hibernate runs |
|---|---|
| `stationRepository.save(newStation)` when the ID is new | `INSERT INTO "Stations" ("StationID","StationCode","Latitude","Longitude","Address","OpenedDate","LineColor","isActive") VALUES (?,?,?,?,?,?,?,?)` |
| `stationRepository.save(existingStation)` when the ID already exists | `UPDATE "Stations" SET "StationCode"=?, "Latitude"=?, ... WHERE "StationID"=?` |
| `stationRepository.findById("STN-VSV")` | `SELECT "StationID","StationCode",... FROM "Stations" WHERE "StationID"=?` |
| `stationRepository.findAll()` | `SELECT ... FROM "Stations"` |
| `stationRepository.existsById("STN-VSV")` | `SELECT 1 FROM "Stations" WHERE "StationID"=? LIMIT 1` (Hibernate may write it as a `count(*)`) |
| `stationRepository.deleteById("STN-VSV")` | `DELETE FROM "Stations" WHERE "StationID"=?` |
| `stationRepository.count()` | `SELECT count(*) FROM "Stations"` — used by the seeder |

`save` decides between INSERT and UPDATE by looking at the ID: if the ID is `null`, it inserts. That is why the controller always sets an ID before calling `save` on a new object.

Because this project assigns the ID itself instead of using a database sequence, Spring Data cannot tell "new" from "existing" by a `null` ID. It therefore calls `EntityManager.merge`, which may run a SELECT before deciding to INSERT. The end result is still one new row; the SQL log just shows an extra read. This is a deliberate trade-off for readable IDs such as `STN-VSV`.

### Who else injects this repository

`StationRepository` is a very popular dependency. It is constructor-injected into:

- `StationController` — the endpoints in this file
- `RouteController` — to look up start and end stations, and every stop
- `FareController` — to check that both stations exist
- `TicketController` — to build a ticket from two station IDs
- `AuthController` — to assign an employee to a station
- `MaintenanceIssueController` — to attach an issue to a station
- `DataSeeder` — to insert the 18 demo stations
- `MetroCardController`? No. Cards belong to users, not stations.

Because it is a bean, the same single instance is shared. That is safe: it holds no data itself, only database access.

---

## 7. Controller endpoints

Base path: `/api/stations` — declared once with `@RequestMapping("/api/stations")`.

| # | Method | Full path | Who can call | What it does | Success status |
|---|---|---|---|---|---|
| 1 | POST | `/api/stations` | `ADMIN` only | Create a station | `201 Created` |
| 2 | PUT | `/api/stations/{id}` | `ADMIN` only | Update a station | `200 OK` |
| 3 | GET | `/api/stations/{id}` | any logged-in user | Read one station | `200 OK` |
| 4 | GET | `/api/stations` | any logged-in user | List all stations | `200 OK` |
| 5 | DELETE | `/api/stations/{id}` | `ADMIN` only | Delete a station | `204 No Content` |

`{id}` is a path variable: in `/api/stations/STN-VSV`, the value `STN-VSV` is captured into the method parameter `id`.

Why can everyone read but only admins write? Passengers and agents need the station list to book tickets. Only the network administrator should add or remove stations.

---

## 8. Controller line by line

Full file with a comment on each important line:

```java
package com.metro.ticketing.controller;

import com.metro.ticketing.entity.Station;              // the entity used as request and response body
import com.metro.ticketing.repository.StationRepository;
import jakarta.validation.Valid;                        // turns on @NotBlank checking
import org.springframework.http.HttpStatus;             // constants such as CREATED
import org.springframework.http.ResponseEntity;         // wrapper for status + body
import org.springframework.security.access.prepost.PreAuthorize; // role check
import org.springframework.transaction.annotation.Transactional; // database transaction
import org.springframework.web.bind.annotation.*;       // @RestController, @GetMapping, ...

import java.util.List;                                  // for List<Station>
import java.util.UUID;                                  // for generating unique IDs

@RestController                                         // 1. web controller; return values become JSON
@RequestMapping("/api/stations")                        // 2. common prefix for all 5 paths
public class StationController {

    private final StationRepository stationRepository;   // 3. final: assigned once, never reassigned

    public StationController(StationRepository stationRepository) {
                                                         // 4. Spring sees this constructor, finds the
                                                         //    StationRepository bean, and passes it in.
                                                         //    This is dependency injection. No "new" here.
        this.stationRepository = stationRepository;
    }

    @PostMapping                                        // 5. POST /api/stations
    @PreAuthorize("hasRole('ADMIN')")                   // 6. only ROLE_ADMIN may enter
    @Transactional                                      // 7. one transaction: insert + commit
    public ResponseEntity<Station> createStation(@Valid @RequestBody Station request) {
                                                         // 8. @RequestBody: parse JSON into Station
                                                         // 9. @Valid: run @NotBlank checks first
        Station station = new Station();                // 10. empty object, all fields null/default
        station.setStationId("STN-" + UUID.randomUUID());
                                                         // 11. server generates the ID; client cannot choose it
        copyStationFields(request, station);            // 12. copy only the editable fields
        return ResponseEntity.status(HttpStatus.CREATED).body(stationRepository.save(station));
                                                         // 13. INSERT, then respond 201 with the saved row
    }

    @PutMapping("/{id}")                                // 14. PUT /api/stations/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Station> updateStation(@PathVariable String id, @Valid @RequestBody Station request) {
                                                         // 15. @PathVariable binds {id} to the parameter
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));
                                                         // 16. load row; if absent, throw -> 400
        copyStationFields(request, station);            // 17. change the object in memory
        return ResponseEntity.ok(stationRepository.save(station));
                                                         // 18. UPDATE and return 200 with the new body
    }

    @GetMapping("/{id}")                                // 19. GET /api/stations/{id}
    @Transactional(readOnly = true)                     // 20. read-only transaction: no dirty write
    public ResponseEntity<Station> getStationById(@PathVariable String id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));
        return ResponseEntity.ok(station);              // 21. serialize the object to JSON, status 200
    }

    @GetMapping                                         // 22. GET /api/stations
    @Transactional(readOnly = true)
    public ResponseEntity<List<Station>> getAllStations() {
        return ResponseEntity.ok(stationRepository.findAll());
                                                         // 23. List<Station> becomes a JSON array
    }

    @DeleteMapping("/{id}")                             // 24. DELETE /api/stations/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteStation(@PathVariable String id) {
        if (!stationRepository.existsById(id)) {        // 25. check first, so we can give a clear error
            throw new IllegalArgumentException("Station not found");
        }
        stationRepository.deleteById(id);               // 26. DELETE statement
        return ResponseEntity.noContent().build();      // 27. 204 with empty body
    }

    private void copyStationFields(Station request, Station station) {
                                                         // 28. private helper: not an endpoint
        station.setStationCode(request.getStationCode());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setAddress(request.getAddress());
        station.setOpenedDate(request.getOpenedDate());
        station.setLineColor(request.getLineColor());
        if (request.getIsActive() != null) {            // 29. missing field -> keep old value
            station.setIsActive(request.getIsActive());
        }
    }
}
```

### Method-by-method summary

**1. `createStation`**

1. Spring checks `ROLE_ADMIN`.
2. Jackson builds a `Station` from the JSON body.
3. `@Valid` runs `@NotBlank` on `stationCode`. If it fails, the method never starts.
4. A blank `Station` object is created.
5. `STN-` + random UUID is written into it.
6. `copyStationFields` copies code, coordinates, address, date, colour, active flag.
7. `save` runs an INSERT.
8. The saved object is returned with status 201.

Note that `stationId` in the request body, if sent, is **ignored**: the controller overwrites it. That is deliberate — clients must not choose primary keys.

**2. `updateStation`**

1. `findById(id)` returns `Optional<Station>`.
2. `orElseThrow` either returns the station or throws `IllegalArgumentException("Station not found")`.
3. Fields are copied onto the **loaded** object, so the primary key stays the same.
4. `save` sees an existing ID and runs an UPDATE.

If the client leaves out `isActive`, the old value survives because of the `!= null` check.

If the client leaves out `openedDate`, it is set to `null`. There is no null check there. So PUT behaves like "replace the editable fields", not "patch only what I sent". That is a normal PUT semantic.

**3. `getStationById`**

Straight read. `readOnly = true` is a hint that lets Hibernate skip dirty-checking, which is slightly faster.

**4. `getAllStations`**

`findAll()` with no filter, no sort. Fine for 18 demo rows. With 100,000 stations you would add `Pageable`.

**5. `deleteStation`**

A hard delete. `existsById` is checked first only to produce a friendly "Station not found" instead of a silent success. Then `deleteById` runs the DELETE.

If another table still references the station, PostgreSQL refuses with a foreign-key error and the response is 500. There is **no cascade**. See section 11.

---

## 9. One request from start to finish

Let us follow a real `POST /api/stations` from a Swagger page to a database row.

Assume the request is:

```http
POST /api/stations HTTP/1.1
Host: localhost:8080
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "stationCode": "TST",
  "latitude": 19.07,
  "longitude": 72.87,
  "address": "Test Station, Mumbai",
  "openedDate": "2024-01-01",
  "lineColor": "Blue",
  "isActive": true
}
```

1. **Tomcat accepts the TCP connection.** Spring Boot's embedded server is listening on port 8080.
2. **The security filter chain runs first.** It sees the `Authorization` header, decodes Base64 into `admin:admin123`, and asks `DbUserDetailsService` to load the user whose contact is `admin`.
3. `DbUserDetailsService` runs `SELECT ... FROM "User" WHERE "Contact" = 'admin'`.
4. BCrypt compares `admin123` with the stored hash. They match, so the request is marked authenticated with authority `ROLE_ADMIN`.
5. **Spring MVC reads the URL.** `POST /api/stations` matches `createStation` because of `@PostMapping` plus the class-level `@RequestMapping`.
6. **`@PreAuthorize("hasRole('ADMIN')")` runs.** The user has `ROLE_ADMIN`, so the method is allowed to start. Otherwise the answer is 403.
7. **Jackson parses the JSON body** into a new `Station` object and sets the seven fields through the setters.
8. **`@Valid` runs.** `stationCode` is `"TST"`, not blank, so no error.
9. **A new empty `Station` is created** for the database copy.
10. `station.setStationId("STN-" + UUID.randomUUID())` produces something like `STN-3f2b9c1e-7a44-4d31-9b0e-5f1c2a8d6e70`.
11. **`copyStationFields`** copies the seven editable values. `isActive` is `true`, not null, so it is set to `true`.
12. **`stationRepository.save(station)`** is called.
13. Hibernate sees a `Station` with an ID that is not in the database, so it schedules an INSERT.
14. At the end of the `@Transactional` method, Hibernate flushes and commits:
    `INSERT INTO "Stations" ("StationID","StationCode","Latitude","Longitude","Address","OpenedDate","LineColor","isActive") VALUES ('STN-3f2b...','TST',19.07,72.87,'Test Station, Mumbai','2024-01-01','Blue',true)`.
15. PostgreSQL checks `StationCode` against the unique index `UX_Station_Code`. `TST` is new, so the insert succeeds.
16. **The saved object is converted to JSON** by Jackson, field by field, using the getters.
17. `ResponseEntity.status(HttpStatus.CREATED).body(...)` sets status `201` and writes the JSON body.
18. The browser in Swagger shows `201` and the response body with the generated `stationId`.

That whole path is: filter -> dispatcher -> controller -> repository -> Hibernate -> PostgreSQL -> back out as JSON.

---

## 10. JSON request and response examples

### Create — request

```json
{
  "stationCode": "TST",
  "latitude": 19.07,
  "longitude": 72.87,
  "address": "Test Station, Mumbai",
  "openedDate": "2024-01-01",
  "lineColor": "Blue",
  "isActive": true
}
```

| Field | Type | Required | Meaning |
|---|---|---|---|
| `stationCode` | string, max 20 | Yes | Short public code. Must be unique in the table. |
| `latitude` | number | No | GPS latitude. |
| `longitude` | number | No | GPS longitude. |
| `address` | string, max 255 | No | Free-text address. |
| `openedDate` | string `YYYY-MM-DD` | No | Opening day. Jackson parses it into `LocalDate`. |
| `lineColor` | string, max 50 | No | `Blue`, `Aqua`, `Red`, `Interchange`. |
| `isActive` | boolean | No | Omit it and the value stays `true`. |
| `stationId` | string | Not accepted | Ignored on create; the server generates it. |

### Create — response `201 Created`

```json
{
  "stationId": "STN-3f2b9c1e-7a44-4d31-9b0e-5f1c2a8d6e70",
  "stationCode": "TST",
  "latitude": 19.07,
  "longitude": 72.87,
  "address": "Test Station, Mumbai",
  "openedDate": "2024-01-01",
  "lineColor": "Blue",
  "isActive": true
}
```

| Field | Where it came from |
|---|---|
| `stationId` | Generated in step 10 of the story above. Copy this value for later requests. |
| `stationCode` | Echo of the request. |
| `latitude`, `longitude` | Echo of the request. |
| `address` | Echo of the request. |
| `openedDate` | Echo, formatted back as `YYYY-MM-DD`. |
| `lineColor` | Echo of the request. |
| `isActive` | Echo, or `true` if the request omitted it. |

### Update — request

```http
PUT /api/stations/STN-3f2b9c1e-7a44-4d31-9b0e-5f1c2a8d6e70
```

```json
{
  "stationCode": "TST2",
  "latitude": 19.08,
  "longitude": 72.88,
  "address": "Updated address",
  "openedDate": "2024-02-02",
  "lineColor": "Red",
  "isActive": false
}
```

The response is `200 OK` with the updated object. The `stationId` is unchanged even though the body did not include it.

If you send `PUT` without `stationCode`, validation fails, because the same `@Valid` rules apply to update.

### List — response

```http
GET /api/stations
Authorization: Basic cHJpeWE6UGFzc3cwcmQh
```

```json
[
  {
    "stationId": "STN-VSV",
    "stationCode": "VSV",
    "latitude": 19.1354,
    "longitude": 72.8209,
    "address": "Versova - JP Road, Andheri West",
    "openedDate": "2015-04-12",
    "lineColor": "Blue",
    "isActive": true
  },
  {
    "stationId": "STN-AND",
    "stationCode": "AND",
    "latitude": 19.11,
    "longitude": 72.8637,
    "address": "Andheri - Western Express Highway, Andheri East",
    "openedDate": "2014-09-03",
    "lineColor": "Blue",
    "isActive": true
  }
]
```

`[...]` is a JSON **array** because the controller returns `List<Station>`. Each `{...}` inside is one row.

Values such as `19.11` are printed without trailing zeros; `19.1100` in the seeder is the same number.

The `openedDate` in the seeder is computed as `2010-01-01` plus a number of days based on `id.hashCode()`, so your exact dates will differ from the examples. What matters is the format.

### Delete — response

`204 No Content` with an **empty** body. That is why the method returns `ResponseEntity<Void>`.

---

## 11. Error table

| Situation | HTTP status | Response body |
|---|---|---|
| No `Authorization` header | 401 | empty body + `WWW-Authenticate: Basic` header |
| Wrong password | 401 | empty body + `WWW-Authenticate` header |
| Logged in as `priya` (PASSENGER) and calling POST/PUT/DELETE | 403 | `{"error":"You do not have permission to access this resource"}` |
| `stationCode` missing, `""`, or spaces | 400 | `{"stationCode":"Station code is required"}` |
| Unknown `{id}` on GET/PUT | 400 | `{"error":"Station not found"}` |
| Unknown `{id}` on DELETE | 400 | `{"error":"Station not found"}` |
| Duplicate `stationCode` | 500 | `{"error":"An unexpected error occurred: ...unique index \"UX_Station_Code\"..."}` |
| Delete a station used by a route, fare, ticket, user, or issue | 500 | `{"error":"An unexpected error occurred: ...violates foreign key constraint..."}` |
| Malformed JSON (for example a trailing comma) | 500 | `{"error":"An unexpected error occurred: ..."}` |

### Why unknown IDs give 400 and not 404

The controller throws `IllegalArgumentException`. `GlobalExceptionHandler` has:

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

So every `IllegalArgumentException` becomes 400. `ResourceNotFoundException` exists in the project and would become 404, but this controller does not use it. In a stricter API you would throw `ResourceNotFoundException` here. This is a design choice worth mentioning honestly in a viva.

### Why duplicate codes give 500

The controller never queries "does this code exist?". It relies on the unique index. PostgreSQL raises a constraint violation, Hibernate wraps it in `DataIntegrityViolationException`, and the catch-all handler turns it into 500. The project could check `existsByStationCode` first to return a friendly 400, but it does not.

### Why deleting a referenced station gives 500

Every child table declares a foreign key to `"Stations"`, for example:

```sql
CONSTRAINT "FK_Route_StartStation" FOREIGN KEY ("StartStationID") REFERENCES "Stations" ("StationID")
```

Deleting `STN-VSV` while a route starts there is refused by the database itself. The transaction rolls back. Nothing is half-deleted. This protects historical tickets and fares.

### Why malformed JSON gives 500 and not 400

When Jackson cannot parse the body, Spring throws `HttpMessageNotReadableException`. `GlobalExceptionHandler` has no handler for that specific class, so the catch-all `@ExceptionHandler(Exception.class)` catches it and returns 500. Spring's default behaviour would normally be 400, but a catch-all handler overrides it. This is a small weakness in the error design, and an easy one to fix by adding a dedicated handler.

---

## 12. Relationships to other groups

Station is the most-referenced table in the project.

| Other group | Field in that table | Foreign key name | What it means |
|---|---|---|---|
| Route | `startStation` -> `"StartStationID"` | `FK_Route_StartStation` | First stop of the line. |
| Route | `endStation` -> `"EndStationID"` | `FK_Route_EndStation` | Last stop of the line. |
| RouteStation | `station` -> `"StationID"` | `FK_RouteStations_Station` | One ordered stop on a route. |
| Fare | `sourceStation` -> `"SourceStationID"` | `FK_Fare_SourceStation` | "From" side of a price pair. |
| Fare | `destStation` -> `"DestStationID"` | `FK_Fare_DestStation` | "To" side of a price pair. |
| Ticket | `sourceStation` -> `"SourceStationID"` | `FK_Ticket_SourceStation` | Where the journey started. |
| Ticket | `destStation` -> `"DestStationID"` | `FK_Ticket_DestStation` | Where the journey ended. |
| User | `station` -> `"StationID"` | `FK_User_Station` | Posting of an employee. Passengers have `null`. |
| MaintenanceIssue | `station` -> `"StationID"` | `FK_Maintenance_Station` | Station a fault belongs to. |

### What this means in practice

1. **Create order:** stations must exist before routes, fares, tickets, staff postings, or issues.
2. **Delete order:** you must delete the children before the station, or the database blocks you.
3. **JSON shape:** because a `Station` object is embedded in `Route`, `Ticket`, and `MaintenanceIssue` responses, you will see a nested object:

```json
{
  "routeId": "RT-L1",
  "routeName": "Mumbai Metro Line 1 - Blue (Versova to Ghatkopar)",
  "startStation": { "stationId": "STN-VSV", "stationCode": "VSV", "...": "..." },
  "endStation":   { "stationId": "STN-GHP", "stationCode": "GHP", "...": "..." }
}
```

4. **`isActive` is only a flag.** Nothing in the booking code checks it. Marking a station inactive hides nothing automatically. It is there so the data can express "temporarily closed" without losing history.

### Where Station sits in the demo order

`DataSeeder` runs `seedStations()` before `seedRoutes()`, `seedSchedules()`, `seedFares()`, `seedCards()`, `seedTickets()`, `seedPayments()`, and `seedMaintenance()`, because all of those look stations up by ID.

---

## 13. Seed data

`DataSeeder.seedStations()` inserts 18 real Mumbai stations across three lines. Each call is:

```java
station("STN-VSV", "VSV", "Versova", 19.1354, 72.8209, "JP Road, Andheri West", "Blue");
```

and the helper is:

```java
private void station(String id, String code, String name, double lat, double lng,
                     String address, String color) {
    Station station = new Station();
    station.setStationId(id);
    station.setStationCode(code);
    station.setAddress(name + " - " + address);   // name is folded into address
    station.setLatitude(lat);
    station.setLongitude(lng);
    station.setOpenedDate(LocalDate.of(2010, 1, 1).plusDays(id.hashCode() % 2000));
    station.setLineColor(color);
    station.setIsActive(true);
    stationRepository.save(station);
}
```

| Line | Stations |
|---|---|
| Blue | Versova (`VSV`), Andheri (`AND`), Western Express Highway (`WEH`), Airport Road (`APR`), Marol Naka (`MRL`, labelled `Interchange`), Saki Naka (`SKN`), Ghatkopar (`GHP`) |
| Aqua | Aarey (`ARY`), SEEPZ (`SEP`), Bandra Kurla Complex (`BKC`), Dadar (`DDR`), Mumbai Central (`MCU`), CSMT (`CSM`) |
| Red | Dahisar East (`DHE`), Devipada (`DVP`), Poisar (`POI`), Gundavali (`GDU`), Andheri East (`AEE`) |

Two details that often confuse people reading the code:

- There is **no `stationName` column**. The name is glued to the front of `address`, separated by `" - "`. The entity has no name field at all. When you want the name in a UI you split on `" - "`.
- `openedDate` is not a real opening date. It is a deterministic pseudo-random day derived from the station ID's `hashCode()`, used so the demo table is not full of identical dates.

The seeder also refuses to run twice: `run()` returns early if `stationRepository.count() > 0`.

---

## 14. Swagger test steps

Open `http://localhost:8080/swagger-ui.html`.

1. Click **Authorize** (top right).
2. Enter username `admin` and password `admin123`. Swagger sends Basic auth from now on.
3. Open **station-controller**.
4. `POST /api/stations` -> **Try it out** -> paste the create body from section 10 -> **Execute**.
5. Expect `201`. Copy the `stationId` from the response.
6. `GET /api/stations/{id}` -> paste that ID -> **Execute** -> expect `200` and the same station.
7. `GET /api/stations` -> **Execute** -> expect a JSON array that now has 19 entries.
8. `PUT /api/stations/{id}` -> paste the ID and the update body -> **Execute** -> expect `200` with `stationCode: "TST2"` and `isActive: false`.
9. `DELETE /api/stations/{id}` -> **Execute**. Because nothing references `TST2`, expect `204` and an empty body.
10. `GET /api/stations/{id}` again -> expect `400` with `{"error":"Station not found"}`.
11. Authorize again as `priya` / `Passw0rd!`, then `POST /api/stations` -> expect `403`.
12. Try `DELETE /api/stations/STN-VSV` as admin -> expect `500`, because routes, fares, and tickets reference Versova.

A `curl` equivalent for step 4:

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/stations \
  -H "Content-Type: application/json" \
  -d '{"stationCode":"TST","latitude":19.07,"longitude":72.87,"address":"Test Station, Mumbai","openedDate":"2024-01-01","lineColor":"Blue","isActive":true}'
```

---

## 15. How to explain this in 2 minutes

> "Station is the reference table of the whole project. One row is one physical metro stop.
>
> The class is in `entity/Station.java`. `@Entity` says it maps to a table, and `@Table(name = "\"Stations\"")` gives the exact quoted name. The primary key is a `String` called `stationId`, and the controller generates it as `STN-` plus a UUID, so clients cannot pick IDs. `stationCode` is the short public code like `VSV`; it has `@NotBlank` so the request fails with 400 if it is missing, and there is a unique index `UX_Station_Code` so two stations cannot share a code. Latitude and longitude are `Double` because the columns can be empty. `openedDate` is a `LocalDate`. `isActive` is a `Boolean` with a default of `true`, which lets us close a station without deleting its history.
>
> The repository is one line: `interface StationRepository extends JpaRepository<Station, String>`. Spring Data writes the implementation at startup, so we get `save`, `findById`, `findAll`, `existsById`, and `deleteById` for free, with no SQL in our code.
>
> The controller has five endpoints under `/api/stations`. POST, PUT, and DELETE require `ROLE_ADMIN` through `@PreAuthorize`; GET is open to any authenticated user, because passengers need the station list to buy tickets. Each method is `@Transactional`, so the database change either fully commits or fully rolls back. `@Valid @RequestBody` means the JSON is parsed into a `Station` and validated before the method body runs. Create sets the ID and copies the fields through a private helper. Update loads the row with `findById`, throws `IllegalArgumentException` if it is absent, and copies fields onto the loaded object so the ID never changes. Delete checks existence and then really deletes.
>
> Every other table points here: Route has start and end station foreign keys, RouteStation has one station per stop, Fare has source and destination, Ticket has source and destination, User has a posting station, and MaintenanceIssue can belong to a station. So stations are seeded first, and the database blocks deleting a station that anything still references — we rely on foreign keys instead of cascade deletes, so history is never silently destroyed.
>
> One thing to admit: a missing ID returns 400, not 404, because we throw `IllegalArgumentException` and the global handler maps that to Bad Request. That is the project's convention."

---

## 16. Beginner Q&A

**Q1. What is an entity, in one line?**
A Java class that JPA maps to a database table. Each object is a row, each field is a column.

**Q2. Where does the table actually get created?**
Not by the entity. Flyway runs `V1__create_metro_ticketing_schema.sql` at startup. The entity only *describes* the table, and `ddl-auto=validate` checks that the description matches reality.

**Q3. What happens if the entity and the SQL disagree?**
The app refuses to start, because validation fails. That is a feature: it catches typos early.

**Q4. Why is the table name written as `"\"Stations\""`?**
Because the SQL name is mixed-case and quoted. In a Java string, `\"` means one double-quote character. So the value Hibernate receives is `"Stations"` with quotes included.

**Q5. Why is there no `StationService` class?**
The project is deliberately simple: controller -> repository. A service layer would be the next refactoring step if business rules grew.

**Q6. Who creates the `StationRepository` object?**
Spring. It scans interfaces that extend `JpaRepository`, creates a proxy bean, and injects it through the controller's constructor. You never write `new StationRepository()`.

**Q7. What does `@Transactional` actually do?**
It opens a database transaction before the method and commits it after a normal return, or rolls it back if an exception escapes. Without it, each statement could auto-commit separately.

**Q8. Why is `readOnly = true` used on GET methods?**
It tells Hibernate the data will not change, so it can skip dirty checking and use a faster flush mode. It is an optimisation, not a rule.

**Q9. Why `Boolean` instead of `boolean` for `isActive`?**
So the code can detect "the client did not send this field" as `null`, and then keep the old value instead of overwriting it with `false`.

**Q10. Why is `stationCode` unique but not the primary key?**
The primary key must never change, because other tables store it. A code is human-facing and might be corrected later. Keeping them separate means a code fix does not break any foreign key.

**Q11. What exactly does `@Valid` do?**
It tells Spring to run Bean Validation on the argument. `@NotBlank` on `stationCode` fails for `null`, empty, or whitespace-only strings, and the request is rejected with 400 before the method body runs.

**Q12. Where does the error JSON shape come from?**
`GlobalExceptionHandler` uses `@ControllerAdvice`. It catches `MethodArgumentNotValidException`, builds a `Map<String,String>` of field -> message, and returns 400. That is why the body is `{"stationCode": "..."}` rather than a list.

**Q13. Why does creating a station with `"stationId": "STN-1"` not use my ID?**
`createStation` ignores the incoming `stationId`; it always calls `setStationId("STN-" + UUID.randomUUID())`. Server-owned keys prevent collisions and guessing.

**Q14. Why can't I delete `STN-VSV`?**
Because foreign keys from `Route`, `Fare`, `Ticket`, `User`, and `MaintenanceIssue` point at it. PostgreSQL rejects the DELETE to keep the data consistent. The API reports 500 because that is the catch-all handler.

**Q15. Is `isActive = false` the same as deleting?**
No. The row stays. Foreign keys keep working and history stays intact. Nothing in the code currently filters on the flag, so it is informational for now.

**Q16. Why does `GET /api/stations` need a login?**
`SecurityConfig` ends with `.anyRequest().authenticated()`. Only registration, Swagger, and `/error` are public. So even reads require Basic auth.

**Q17. How do I get only the Blue line stations?**
Today: fetch all and filter in the client. The repository has no `findByLineColor`. Adding `List<Station> findByLineColor(String lineColor)` would make Spring generate `WHERE "LineColor" = ?` automatically.

**Q18. Why is the station name not a column?**
The seed data packs it into `address` as `"<Name> - <address>"`. It is a demo shortcut, not good schema design. A real system would have a `StationName` column.

**Q19. What is the `{id}` in `@GetMapping("/{id}")`?**
A path variable. `@PathVariable String id` binds it by name. A request to `/api/stations/STN-VSV` sets `id = "STN-VSV"`.

**Q20. What does `ResponseEntity.ok(...)` return?**
Status 200 plus the object as the body. `ResponseEntity.status(HttpStatus.CREATED)` returns 201. `ResponseEntity.noContent()` returns 204 with no body.

---

## 17. Glossary

| Term | Meaning in this file |
|---|---|
| **API** | The set of URLs and rules other programs use to talk to this app. |
| **Bean** | An object Spring creates and manages. |
| **BCrypt** | A one-way password hashing algorithm. The database stores the hash, never the password. |
| **Bean Validation** | The standard annotation-based checking system (`@NotBlank`, `@NotNull`). |
| **Column** | One field of a table. |
| **Controller** | Class that maps URLs to Java methods and returns JSON. |
| **CRUD** | Create, Read, Update, Delete. |
| **DTO** | A class made only to carry data between layers. Not used in this controller; entities are reused. |
| **Entity** | Class mapped to a table. |
| **Endpoint** | One URL + HTTP method pair. |
| **Flyway** | Runs the numbered SQL migrations. |
| **Foreign key** | A column that must match a primary key in another table. |
| **Hibernate** | The JPA implementation that generates SQL. |
| **HTTP status** | Numeric result of a request, e.g. 201. |
| **Idempotent** | Doing it twice has the same effect as doing it once. PUT and DELETE should be idempotent. |
| **Index** | A database structure that speeds up lookups and can also enforce uniqueness. |
| **Jackson** | Converts Java objects to JSON and back. |
| **JPA** | The Java standard for object-to-table mapping. |
| **JSON** | Text data format using objects `{}` and arrays `[]`. |
| **Lazy loading** | Loading a related row only when the field is actually read. |
| **Migration** | One versioned SQL file applied by Flyway. |
| **ORM** | Mapping objects to relational tables. |
| **Path variable** | A value taken from the URL path, e.g. the ID in `/api/stations/{id}`. |
| **Persistence context** | Hibernate's in-memory set of loaded entities during a transaction. |
| **Primary key** | Column that uniquely identifies a row. |
| **Proxy** | A stand-in object Hibernate uses for lazy relationships. |
| **Repository** | Interface that provides database methods. |
| **Role** | Permission label such as `ADMIN`. |
| **Seeder** | Startup class that inserts demo data. |
| **Serialization** | Java object -> JSON. |
| **Soft delete** | Marking a row inactive rather than removing it. |
| **Swagger UI** | Generated web page for trying the API. |
| **Transaction** | All-or-nothing group of database operations. |
| **Unique index** | Rule that no two rows may share a value. |
| **UUID** | A 128-bit random identifier, written as 36 characters. |
| **Validation** | Checking input before using it. |

---

## 18. One-line summary

`Station` is the parent table of the metro network; a one-line `JpaRepository` gives the controller free database access, five REST endpoints expose it with admin-only writes, and foreign keys from routes, fares, tickets, staff, and maintenance issues make it the first thing seeded and the last thing deletable.

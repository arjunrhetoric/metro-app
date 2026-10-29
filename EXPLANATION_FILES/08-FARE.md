# 08 — Fare Group

> **Analogy.** Think of the fare chart stuck on the wall of a taxi stand:
> "Andheri → Ghatkopar = ₹25." The chart does not calculate anything while you stand
> there. It is a **price list**. A `Fare` row in this project is exactly one line of
> that chart: a source station, a destination station, and one price. The ticket group
> later walks up to this chart and reads the price.

---

## 1. What a Fare represents

A `Fare` is **one price for one direction between two stations**.

Example:

| FareID | SourceStationID | DestStationID | BaseFare |
|---|---|---|---|
| `FARE-STN-AND-STN-GHP` | `STN-AND` (Andheri) | `STN-GHP` (Ghatkopar) | `25.00` |

Important properties of this design:

1. The price is **stored**, not calculated from distance.
2. The pair is **directional**. `AND → GHP` and `GHP → AND` are two separate rows,
   even if `DataSeeder` gives them the same amount.
3. Exactly **one** row is allowed per `(SourceStationID, DestStationID)` pair. This is
   enforced by a unique index added in migration `V2`.
4. There is no ticket-type pricing here. A `SINGLE` ticket and a `WEEKLY` ticket both
   use the same base fare in this model.
5. There is no date or time on a fare. It is a plain price list.

---

## 2. Words you need

| Word | Simple meaning |
|---|---|
| **Spring Boot** | Framework that starts the web server and wires classes together. |
| **Bean** | An object that Spring creates and manages. |
| **Dependency injection** | Giving a class its dependencies instead of it creating them. |
| **Constructor injection** | Dependency injection through the constructor. |
| **Annotation** | An `@`-prefixed instruction added to Java code. |
| **JPA** | Java Persistence API: maps Java objects to tables. |
| **Hibernate** | The library that implements JPA and writes SQL. |
| **Entity** | A Java class mapped to a database table. |
| **Repository** | Interface that reads/writes entities; Spring implements it. |
| **Controller** | Class that handles HTTP requests. |
| **REST API** | Web API where URLs are resources and HTTP methods are actions. |
| **HTTP** | Request/response protocol of the web. |
| **Endpoint** | One URL plus one HTTP method. |
| **JSON** | Text format: `{ "key": value }`. |
| **Serialization** | Java object → JSON. |
| **Deserialization** | JSON → Java object. |
| **`@Entity`** | Marks the class as a table mapping. |
| **`@Table`** | Sets the exact table name. |
| **`@Id`** | Marks the primary key. |
| **`@Column`** | Maps a field to a column. |
| **`precision`** | Total number of digits in a decimal column. |
| **`scale`** | Number of digits after the decimal point. |
| **Primary key** | Column that uniquely identifies a row. |
| **Foreign key** | Column that points to another table's primary key. |
| **`@ManyToOne`** | Many rows here point to one row there. |
| **`@JoinColumn`** | Names the foreign-key column. |
| **`FetchType.LAZY`** | Load the related row only when it is used. |
| **`@Transient`** | "Not a column"; JPA ignores the field. |
| **`@JsonProperty`** | Controls JSON reading/writing of a field. |
| **`READ_ONLY`** | In responses, ignored in requests. |
| **`WRITE_ONLY`** | Accepted in requests, hidden in responses. |
| **`@JsonIgnoreProperties`** | Skip listed internal properties during JSON conversion. |
| **`hibernateLazyInitializer`** | Internal Hibernate proxy property that we hide. |
| **`@NotNull`** | Validation: value must not be null. |
| **`@DecimalMin`** | Validation: number must be at least a given value. |
| **`BigDecimal`** | Exact decimal number class, used for money. |
| **`double`** | Binary floating-point number; can have rounding errors. |
| **`@Valid`** | Run validation annotations on this parameter. |
| **`@RequestBody`** | Build the parameter from the JSON body. |
| **`@PathVariable`** | Take the parameter from the URL path. |
| **`@RequestParam`** | Take the parameter from the query string. |
| **Query string** | The `?a=1&b=2` part of a URL. |
| **`@RestController`** | Controller whose return value is the response body. |
| **`@RequestMapping`** | Base URL for the class. |
| **`@PostMapping`** | Handles `POST` (create). |
| **`@PutMapping`** | Handles `PUT` (update). |
| **`@GetMapping`** | Handles `GET` (read). |
| **`@DeleteMapping`** | Handles `DELETE`. |
| **`@PreAuthorize`** | Role check before the method runs. |
| **`hasRole('ADMIN')`** | Only the `ADMIN` role may call. |
| **`@Transactional`** | All database work succeeds or rolls back together. |
| **`readOnly = true`** | Hints that no data will be modified. |
| **`ResponseEntity`** | Wrapper giving control over status and body. |
| **`HttpStatus.CREATED`** | Status `201`, new resource created. |
| **`Optional`** | Box that may or may not contain a value. |
| **`orElseThrow`** | Return the value or throw if empty. |
| **`JpaRepository`** | Spring Data interface with ready-made CRUD. |
| **Derived query** | Query generated from a method name. |
| **`existsBy...`** | Derived query returning `true`/`false`. |
| **`findBy...`** | Derived query returning entities. |
| **Underscore in method name** | Means "go into the relation", e.g. `sourceStation_StationId`. |
| **Unique index** | Database rule that no two rows share these values. |
| **`UUID`** | Long random identifier. |
| **`IllegalArgumentException`** | Exception used here for bad input or missing records. |
| **Idempotent** | Calling it twice has the same effect as calling once. |

---

## 3. Files in this group

| Role | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/Fare.java` |
| Controller | `src/main/java/com/metro/ticketing/controller/FareController.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/FareRepository.java` |
| Table definition | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Unique index | `src/main/resources/db/migration/V2__prevent_duplicate_fare_station_pairs.sql` |
| Seed data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` (`seedFares`, `fare`) |
| Used by | `src/main/java/com/metro/ticketing/controller/TicketController.java` |

No service class. The controller holds the rules.

---

## 4. Database table

### SQL from V1

```sql
CREATE TABLE IF NOT EXISTS "Fares" (
    "FareID" VARCHAR(50) PRIMARY KEY,
    "SourceStationID" VARCHAR(50) NOT NULL,
    "DestStationID" VARCHAR(50) NOT NULL,
    "BaseFare" NUMERIC(10,2) NOT NULL,
    CONSTRAINT "FK_Fare_SourceStation" FOREIGN KEY ("SourceStationID") REFERENCES "Stations" ("StationID"),
    CONSTRAINT "FK_Fare_DestStation" FOREIGN KEY ("DestStationID") REFERENCES "Stations" ("StationID")
);
```

### SQL from V2

```sql
CREATE UNIQUE INDEX IF NOT EXISTS "UX_Fare_Station_Pair"
    ON "Fares" ("SourceStationID", "DestStationID");
```

### Column-by-column meaning

| Column | Type | Meaning |
|---|---|---|
| `FareID` | `VARCHAR(50)` | Primary key. Seed rows: `FARE-STN-AND-STN-GHP`. API rows: `FARE-<uuid>`. |
| `SourceStationID` | `VARCHAR(50)` | Foreign key to `Stations`. Where the journey starts. Required. |
| `DestStationID` | `VARCHAR(50)` | Foreign key to `Stations`. Where the journey ends. Required. |
| `BaseFare` | `NUMERIC(10,2)` | Price. Up to 10 digits total, exactly 2 after the decimal point. Required. |

`NUMERIC(10,2)` means the database can store values like `99999999.99`. In Java the
matching type is `BigDecimal`.

### Why `V2` exists

V1 had no rule against duplicate station pairs. You could insert:

```text
FARE-A  STN-AND -> STN-GHP  25.00
FARE-B  STN-AND -> STN-GHP  30.00
```

Then a lookup would find two prices and would have to guess. V2 adds a unique index on
`(SourceStationID, DestStationID)`, so the database itself refuses the second row.

The unique index is **case-sensitive and directional**:

- `(AND, GHP)` and `(GHP, AND)` are different pairs → both allowed.
- `(AND, GHP)` and `(AND, GHP)` are the same pair → second one rejected.

### What the table does not contain

- No `CreatedAt` or `UpdatedAt`.
- No distance.
- No ticket type.
- No discount.
- No active flag.

If an evaluator asks "where is the date?", the honest answer is: this table does not
track dates. Only `Fares` rows matter for price lookup.

---

## 5. Entity line by line

File: `src/main/java/com/metro/ticketing/entity/Fare.java`

### Package and imports

```java
package com.metro.ticketing.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;      // hide Hibernate proxy internals
import com.fasterxml.jackson.annotation.JsonProperty;              // READ_ONLY / WRITE_ONLY
import jakarta.persistence.*;                                      // @Entity, @Id, @Column, @ManyToOne, ...
import jakarta.validation.constraints.DecimalMin;                  // minimum decimal value
import jakarta.validation.constraints.NotNull;                     // must not be null

import java.math.BigDecimal;                                       // exact money type
```

### Class declaration

```java
@Entity                                                            // this class maps to a table
@Table(name = "\"Fares\"")                                         // exact table name: Fares
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})      // hide Hibernate proxy fields in JSON
public class Fare {
```

The escaped quotes `\"Fares\"` become `"Fares"` in SQL. PostgreSQL would otherwise
lowercase the name to `fares`, which would not match the created table.

### Primary key

```java
@Id                                                     // primary key
@Column(name = "\"FareID\"", length = 50)               // column FareID, max 50 characters
private String fareId;
```

A `String` is used because IDs are readable: seeded rows use
`FARE-<source>-<dest>`, and API rows use `FARE-<uuid>`.

### Source station relation

```java
@JsonProperty(access = JsonProperty.Access.READ_ONLY)   // appears in responses, ignored in requests
@ManyToOne(fetch = FetchType.LAZY)                      // many fares can start at one station
@JoinColumn(                                            // foreign-key column in this table
    name = "\"SourceStationID\"",                       // column in Fares
    referencedColumnName = "\"StationID\"",             // column in Stations
    nullable = false)                                   // NOT NULL in the database
private Station sourceStation;
```

`@ManyToOne` is correct because one station can be the source of many fares, but each
fare has exactly one source.

`LAZY` means the station row is fetched only when `getSourceStation()` is called, or
when Jackson serializes the field.

### Destination station relation

```java
@JsonProperty(access = JsonProperty.Access.READ_ONLY)   // response only
@ManyToOne(fetch = FetchType.LAZY)                      // many fares can end at one station
@JoinColumn(
    name = "\"DestStationID\"",                         // FK column in Fares
    referencedColumnName = "\"StationID\"",             // PK column in Stations
    nullable = false)
private Station destStation;
```

`sourceStation` and `destStation` both point to the **same** `Stations` table. This is
called a self-referencing-style double relation: two different foreign keys to one
table. There is no special annotation needed; two `@ManyToOne` fields are enough.

### Transient source ID

```java
@Transient                                              // not a database column
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // accepted in requests, hidden in responses
private String sourceStationId;
```

Lets the client send `"sourceStationId": "STN-AND"` instead of a nested station object.

### Transient destination ID

```java
@Transient                                              // not a database column
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // request only
private String destStationId;
```

Lets the client send `"destStationId": "STN-GHP"`.

### Base fare

```java
@NotNull(message = "Base fare is required")                                // null is rejected
@DecimalMin(value = "0.01", message = "Base fare must be greater than zero") // must be >= 0.01
@Column(name = "\"BaseFare\"", precision = 10, scale = 2, nullable = false)  // NUMERIC(10,2) NOT NULL
private BigDecimal baseFare;
```

Three layers of protection:

1. `@NotNull` rejects a missing value with a clear message.
2. `@DecimalMin("0.01")` rejects `0`, `0.00` and negative values. The minimum is
   **inclusive**, so exactly `0.01` is allowed.
3. `nullable = false` makes the database refuse null even if Java validation is
   bypassed.

`@DecimalMin` works on `BigDecimal`, `String` representations of numbers, and numeric
wrapper types. Here it compares the `BigDecimal` value with `0.01`.

### Accessors

```java
public String getFareId() { return fareId; }                         // read the ID
public void setFareId(String fareId) { this.fareId = fareId; }       // write the ID

public Station getSourceStation() { return sourceStation; }          // read the lazy source station
public void setSourceStation(Station sourceStation) { this.sourceStation = sourceStation; }

public Station getDestStation() { return destStation; }              // read the lazy destination station
public void setDestStation(Station destStation) { this.destStation = destStation; }

public BigDecimal getBaseFare() { return baseFare; }                 // read the price
public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }

public String getSourceStationId() { return sourceStationId; }       // transient request value
public void setSourceStationId(String sourceStationId) { this.sourceStationId = sourceStationId; }

public String getDestStationId() { return destStationId; }           // transient request value
public void setDestStationId(String destStationId) { this.destStationId = destStationId; }
```

Note the naming difference between the entity and the database:

| Entity field | Column |
|---|---|
| `destStation` | `DestStationID` |
| `destStationId` (transient) | none |

Jackson therefore produces `"destStation"` and `"destStationId"` in JSON, while SQL
uses `"DestStationID"`.

---

## 6. Repository line by line

File: `src/main/java/com/metro/ticketing/repository/FareRepository.java`

```java
package com.metro.ticketing.repository;

import com.metro.ticketing.entity.Fare;
import org.springframework.data.jpa.repository.JpaRepository;   // CRUD methods
import org.springframework.stereotype.Repository;              // Spring bean marker

import java.util.Optional;                                     // may-or-may-not-have-a-value box

@Repository                                                    // Spring creates the implementation
public interface FareRepository extends JpaRepository<Fare, String> {
```

`JpaRepository<Fare, String>` means: entity `Fare`, primary key type `String`.

### Inherited methods

| Method | Generated SQL (conceptual) |
|---|---|
| `save(fare)` | `INSERT INTO "Fares" ...` or `UPDATE "Fares" ...` |
| `findById(id)` | `SELECT ... FROM "Fares" WHERE "FareID" = ?` |
| `findAll()` | `SELECT ... FROM "Fares"` |
| `existsById(id)` | `SELECT COUNT(*) > 0 ... WHERE "FareID" = ?` |
| `deleteById(id)` | `DELETE FROM "Fares" WHERE "FareID" = ?` |
| `count()` | `SELECT COUNT(*) FROM "Fares"` |

### Method 1: `existsBySourceStation_StationIdAndDestStation_StationId`

```java
boolean existsBySourceStation_StationIdAndDestStation_StationId(
        String sourceStationId, String destStationId);
```

How Spring Data reads the name:

| Piece | Meaning |
|---|---|
| `exists` | Return `true`/`false`, do not load the entity. |
| `By` | Start of the conditions. |
| `SourceStation` | The `sourceStation` field on `Fare`. |
| `_StationId` | Go **into** that object and use its `stationId` field. |
| `And` | Both conditions must match. |
| `DestStation` | The `destStation` field. |
| `_StationId` | Its `stationId`. |

Conceptual SQL:

```sql
SELECT COUNT(*) > 0
FROM "Fares"
WHERE "SourceStationID" = ?
  AND "DestStationID" = ?;
```

Hibernate can use the foreign-key columns directly because `Station.stationId` is the
primary key of `Stations`. Conceptually it means "source station's ID equals ... and
destination station's ID equals ...".

Used in `createFare` to stop duplicate pairs before insert.

### Method 2: `existsBySourceStation_StationIdAndDestStation_StationIdAndFareIdNot`

```java
boolean existsBySourceStation_StationIdAndDestStation_StationIdAndFareIdNot(
        String sourceStationId, String destStationId, String fareId);
```

Same as method 1, plus:

| Piece | Meaning |
|---|---|
| `AndFareIdNot` | `FareID <> ?` — ignore this row. |

Conceptual SQL:

```sql
SELECT COUNT(*) > 0
FROM "Fares"
WHERE "SourceStationID" = ?
  AND "DestStationID" = ?
  AND "FareID" <> ?;
```

Used by `updateFare` so the fare being edited does not count as a duplicate of itself.

### Method 3: `findBySourceStation_StationIdAndDestStation_StationId`

```java
Optional<Fare> findBySourceStation_StationIdAndDestStation_StationId(
        String sourceStationId, String destStationId);
```

Conceptual SQL:

```sql
SELECT *
FROM "Fares"
WHERE "SourceStationID" = ?
  AND "DestStationID" = ?;
```

Spring Data asks the database for a single result (`getSingleResult`), not a list. The
unique index guarantees that at most one row can match, so no `LIMIT` is needed.

The return type is `Optional<Fare>` because the pair might not exist.

`Optional` forces the caller to think about absence:

```java
fareRepository.findBy...(...)
    .orElseThrow(() -> new IllegalArgumentException("Fare not found for this station pair"));
```

Without `Optional`, the method could return `null` and the caller might forget to check,
causing a `NullPointerException`.

The query expects at most one row. The unique index `UX_Fare_Station_Pair` guarantees
that. If the database somehow contained two rows, Spring Data would throw an
`IncorrectResultSizeDataAccessException`.

### Why not `findFirstBy...`?

Because the unique index already guarantees uniqueness. `findBy...` returning
`Optional` expresses the intent clearly: "zero or one".

---

## 7. Controller endpoints

Base path: `/api/fares`

| # | Method | Path | Who can call | Purpose | Success |
|---|---|---|---|---|---|
| 1 | `POST` | `/api/fares` | `ADMIN` | Create a fare for a station pair | `201` |
| 2 | `GET` | `/api/fares/lookup?sourceStationId=...&destStationId=...` | Any logged-in user | Price lookup by station IDs | `200` |
| 3 | `GET` | `/api/fares/{id}` | Any logged-in user | Read one fare by ID | `200` |
| 4 | `GET` | `/api/fares` | Any logged-in user | List all fares | `200` |
| 5 | `PUT` | `/api/fares/{id}` | `ADMIN` | Update a fare | `200` |
| 6 | `DELETE` | `/api/fares/{id}` | `ADMIN` | Delete a fare | `204` |

Endpoint order matters for understanding, not for Spring. Spring MVC prefers a literal
path (`/lookup`) over a template (`/{id}`) when both match, so
`GET /api/fares/lookup` always reaches `getFareByStations`, never `getFareById("lookup")`.

---

## 8. Controller line by line

File: `src/main/java/com/metro/ticketing/controller/FareController.java`

### Imports and class

```java
package com.metro.ticketing.controller;

import com.metro.ticketing.entity.Fare;                        // entity used as request + response
import com.metro.ticketing.entity.Station;                     // resolved from station IDs
import com.metro.ticketing.repository.FareRepository;          // fare database access
import com.metro.ticketing.repository.StationRepository;       // station database access
import jakarta.validation.Valid;                               // enables bean validation
import org.springframework.http.HttpStatus;                    // CREATED constant
import org.springframework.http.ResponseEntity;                // status + body wrapper
import org.springframework.security.access.prepost.PreAuthorize; // role checks
import org.springframework.transaction.annotation.Transactional;  // transaction boundary
import org.springframework.web.bind.annotation.*;              // mapping annotations

import java.util.List;                                         // list responses
import java.util.UUID;                                         // random ID generation

@RestController                                                // JSON responses
@RequestMapping("/api/fares")                                  // base URL
public class FareController {
```

### Fields and constructor (dependency injection)

```java
private final FareRepository fareRepository;          // final dependency
private final StationRepository stationRepository;    // final dependency

public FareController(FareRepository fareRepository, StationRepository stationRepository) {
    this.fareRepository = fareRepository;             // injected by Spring
    this.stationRepository = stationRepository;       // injected by Spring
}
```

Because there is exactly one constructor, Spring does not need `@Autowired`. It matches
the parameters by type against beans it has already created.

`StationRepository` is needed because the request contains station **IDs**, but the
database foreign key needs real `Station` entities.

### Create fare

```java
@PostMapping                                          // POST /api/fares
@PreAuthorize("hasRole('ADMIN')")                     // only ADMIN
@Transactional                                        // one transaction
public ResponseEntity<Fare> createFare(@Valid @RequestBody Fare request) {
    validateStationPair(request.getSourceStationId(), request.getDestStationId());

    if (fareRepository.existsBySourceStation_StationIdAndDestStation_StationId(
            request.getSourceStationId(), request.getDestStationId())) {
        throw new IllegalArgumentException("A fare for this station pair already exists");
    }

    Fare fare = new Fare();                           // new entity, not the request object
    fare.setFareId("FARE-" + UUID.randomUUID());      // server-generated ID
    copyFareFields(request, fare);                    // resolve stations, copy price
    return ResponseEntity.status(HttpStatus.CREATED) // 201
            .body(fareRepository.save(fare));         // INSERT and return saved entity
}
```

Step by step:

1. `validateStationPair` rejects nulls and identical stations.
2. The duplicate check runs a database query.
3. A brand-new `Fare` is created, so any `fareId` sent by the client is ignored.
4. `copyFareFields` looks up both stations and copies `baseFare`.
5. `save` inserts the row.
6. The response is `201` with the saved entity.

Why create a new object instead of saving `request` directly? The request object has no
managed station relations, only transient IDs. Saving it directly would fail on the
non-null foreign keys. It would also let a client choose the primary key.

### Lookup by station pair

```java
@GetMapping("/lookup")                                          // GET /api/fares/lookup
@Transactional(readOnly = true)                                 // read-only transaction
public ResponseEntity<Fare> getFareByStations(
        @RequestParam String sourceStationId,                   // ?sourceStationId=STN-AND
        @RequestParam String destStationId) {                   // &destStationId=STN-GHP
    validateStationPair(sourceStationId, destStationId);        // null / same-station checks
    Fare fare = fareRepository
            .findBySourceStation_StationIdAndDestStation_StationId(
                    sourceStationId, destStationId)             // exact directional lookup
            .orElseThrow(() -> new IllegalArgumentException(
                    "Fare not found for this station pair"));
    return ResponseEntity.ok(fare);                             // 200 with the fare
}
```

`@RequestParam` reads query-string values. By default the parameter is **required**.
If it is absent, Spring raises a binding exception before the method body runs.

This method does **not** check that the stations exist. If they do not, the lookup
simply finds no fare and returns `"Fare not found for this station pair"`.

The lookup is **directional**:

- `sourceStationId=STN-AND&destStationId=STN-GHP` → row `FARE-STN-AND-STN-GHP`.
- `sourceStationId=STN-GHP&destStationId=STN-AND` → row `FARE-STN-GHP-STN-AND`.

If only one direction was seeded, the other direction returns `400`.

### Read one fare by ID

```java
@GetMapping("/{id}")                                            // GET /api/fares/{id}
@Transactional(readOnly = true)
public ResponseEntity<Fare> getFareById(@PathVariable String id) {
    Fare fare = fareRepository.findById(id)                     // primary-key lookup
            .orElseThrow(() -> new IllegalArgumentException("Fare not found"));
    return ResponseEntity.ok(fare);                             // 200
}
```

`@PathVariable` takes the `{id}` segment. For `GET /api/fares/FARE-STN-AND-STN-GHP`,
`id` is `"FARE-STN-AND-STN-GHP"`.

### Read all fares

```java
@GetMapping                                                     // GET /api/fares
@Transactional(readOnly = true)
public ResponseEntity<List<Fare>> getAllFares() {
    return ResponseEntity.ok(fareRepository.findAll());         // 200 with JSON array
}
```

No paging, no filter, no sorting. Every fare row is returned.

### Update fare

```java
@PutMapping("/{id}")                                            // PUT /api/fares/{id}
@PreAuthorize("hasRole('ADMIN')")                               // ADMIN only
@Transactional
public ResponseEntity<Fare> updateFare(@PathVariable String id,
                                       @Valid @RequestBody Fare request) {
    Fare fare = fareRepository.findById(id)                     // load existing row
            .orElseThrow(() -> new IllegalArgumentException("Fare not found"));

    validateStationPair(request.getSourceStationId(), request.getDestStationId());

    if (fareRepository.existsBySourceStation_StationIdAndDestStation_StationIdAndFareIdNot(
            request.getSourceStationId(), request.getDestStationId(), id)) {
        throw new IllegalArgumentException("A fare for this station pair already exists");
    }

    copyFareFields(request, fare);                              // overwrite fields
    return ResponseEntity.ok(fareRepository.save(fare));        // UPDATE, return 200
}
```

The `FareIdNot` variant is essential. Without it, the row would always find itself and
every update would be rejected as a duplicate.

`save` on a loaded entity performs an `UPDATE`, because the ID already exists.

### Delete fare

```java
@DeleteMapping("/{id}")                                         // DELETE /api/fares/{id}
@PreAuthorize("hasRole('ADMIN')")                               // ADMIN only
@Transactional
public ResponseEntity<Void> deleteFare(@PathVariable String id) {
    if (!fareRepository.existsById(id)) {                       // clear not-found message
        throw new IllegalArgumentException("Fare not found");
    }
    fareRepository.deleteById(id);                              // DELETE row
    return ResponseEntity.noContent().build();                  // 204, no body
}
```

If a ticket references this fare, the database foreign key `FK_Ticket_Fare` blocks the
delete. The exception is not an `IllegalArgumentException`, so the generic handler
returns `500`.

### Helper: validateStationPair

```java
private void validateStationPair(String sourceStationId, String destStationId) {
    if (sourceStationId == null || destStationId == null) {
        throw new IllegalArgumentException("Source and destination stations are required");
    }
    if (sourceStationId.equals(destStationId)) {
        throw new IllegalArgumentException("Source and destination stations must be different");
    }
}
```

- It is `private` because only this controller uses it.
- It does not check that the stations exist. That happens in `copyFareFields`, and only
  for create/update.
- It does not check for empty strings. An empty `""` is not null and would pass this
  method, then fail later as "station not found" or "fare not found".
- `equals` is safe here because both values are non-null at that point.

### Helper: copyFareFields

```java
private void copyFareFields(Fare request, Fare fare) {
    Station source = stationRepository.findById(request.getSourceStationId())
            .orElseThrow(() -> new IllegalArgumentException("Source station not found"));
    Station destination = stationRepository.findById(request.getDestStationId())
            .orElseThrow(() -> new IllegalArgumentException("Destination station not found"));

    fare.setSourceStation(source);          // attach managed Station entity
    fare.setDestStation(destination);        // attach managed Station entity
    fare.setBaseFare(request.getBaseFare()); // copy the price
}
```

It does **not** copy `fareId`. The ID is set once on create and never changes.

It also does not validate that source and destination are on the same line. A fare can
technically be created between any two stations, including stations on different lines.

---

## 9. One request from start to finish

Story: an admin adds a ₹25 fare from Andheri (`STN-AND`) to Ghatkopar (`STN-GHP`),
then a passenger looks it up.

1. The admin authorizes in Swagger as `admin` / `admin123`.
2. Swagger sends `POST /api/fares` with the JSON body in section 10.
3. Spring Security authenticates the user and attaches `ROLE_ADMIN`.
4. `@PreAuthorize("hasRole('ADMIN')")` passes.
5. Jackson builds a `Fare` object. It fills `sourceStationId`, `destStationId` and
   `baseFare`. The transient ID fields are writable; `sourceStation` and
   `destStation` are read-only and cannot be set from JSON.
6. `@Valid` checks `baseFare`: not null and at least `0.01`.
7. `validateStationPair` sees two different, non-null IDs.
8. `existsBySourceStation_StationIdAndDestStation_StationId("STN-AND", "STN-GHP")`
   returns `false`. No duplicate exists.
9. A new `Fare` is created with ID `FARE-<uuid>`.
10. `copyFareFields` loads `STN-AND` and `STN-GHP` from `Stations` and attaches them.
    `baseFare` is copied as `25.00`.
11. `save` runs an `INSERT` with `SourceStationID = 'STN-AND'`,
    `DestStationID = 'STN-GHP'`, `BaseFare = 25.00`.
12. The unique index confirms the pair is unique.
13. The entity is serialized. Because `sourceStation` and `destStation` are `LAZY`,
    Hibernate loads them during serialization (open-in-view is on). The transient IDs
    are hidden because they are `WRITE_ONLY`.
14. The client receives `201 Created`.

Then the passenger's lookup:

1. Priya authorizes as `priya` / `Passw0rd!`.
2. She calls
   `GET /api/fares/lookup?sourceStationId=STN-AND&destStationId=STN-GHP`.
3. `@RequestParam` reads both values.
4. `validateStationPair` passes.
5. `findBySourceStation_StationIdAndDestStation_StationId` runs one `SELECT`.
6. The row is found, so `Optional` is non-empty.
7. `ResponseEntity.ok(fare)` returns `200` with the fare JSON, including both station
   objects.
8. If she reversed the IDs, the query would look for `STN-GHP → STN-AND`. If that row
   exists (the seeder creates both directions for most pairs), it is returned.
   Otherwise she gets `400` with `"Fare not found for this station pair"`.

---

## 10. JSON request and response examples

### Create request (admin)

```http
POST /api/fares
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json
```

```json
{
  "sourceStationId": "STN-AND",
  "destStationId": "STN-GHP",
  "baseFare": 25.00
}
```

| Field | Required | Meaning |
|---|---|---|
| `sourceStationId` | yes | Must exist in `Stations`. Not null, not equal to destination. |
| `destStationId` | yes | Must exist in `Stations`. Not null, not equal to source. |
| `baseFare` | yes | Must be `>= 0.01`. Sent as a JSON number. |
| `fareId` | — | Ignored on create; the server generates it. |
| `sourceStation` | — | Read-only; ignored in requests. |
| `destStation` | — | Read-only; ignored in requests. |

### Create response (`201`)

```json
{
  "fareId": "FARE-8c1d4e2a-6b7f-4a90-9d31-2f5e7c8b1a44",
  "sourceStation": {
    "stationId": "STN-AND",
    "stationCode": "AND",
    "latitude": 19.1100,
    "longitude": 72.8637,
    "address": "Andheri - Western Express Highway, Andheri East",
    "openedDate": "2013-06-10",
    "lineColor": "Blue",
    "isActive": true
  },
  "destStation": {
    "stationId": "STN-GHP",
    "stationCode": "GHP",
    "latitude": 19.0880,
    "longitude": 72.9096,
    "address": "Ghatkopar - Ghatkopar West, Station Road Junction",
    "openedDate": "2014-02-02",
    "lineColor": "Blue",
    "isActive": true
  },
  "baseFare": 25.00
}
```

`sourceStationId` and `destStationId` are **absent** from the response because they are
`WRITE_ONLY`. The full nested objects are present because the relations are
`READ_ONLY`.

### Lookup request

```http
GET /api/fares/lookup?sourceStationId=STN-AND&destStationId=STN-GHP
Authorization: Basic cHJpeWE6UGFzc3cwcmQh
```

### Lookup response (`200`)

```json
{
  "fareId": "FARE-STN-AND-STN-GHP",
  "sourceStation": { "stationId": "STN-AND", "stationCode": "AND" },
  "destStation": { "stationId": "STN-GHP", "stationCode": "GHP" },
  "baseFare": 25.00
}
```

The nested stations contain all their fields in reality; they are shortened here.

### Update request

```http
PUT /api/fares/FARE-STN-AND-STN-GHP
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json
```

```json
{
  "sourceStationId": "STN-AND",
  "destStationId": "STN-GHP",
  "baseFare": 28.00
}
```

Response is `200` with `"baseFare": 28.00`. The `fareId` stays the same.

### Duplicate create response (`400`)

```json
{
  "error": "A fare for this station pair already exists"
}
```

### Validation failure response (`400`)

```json
{
  "baseFare": "Base fare must be greater than zero"
}
```

This shape comes from `MethodArgumentNotValidException` handling: one entry per invalid
field, keyed by field name.

### Delete response

`204 No Content`, empty body.

---

## 11. Error table

| Situation | Status | Body |
|---|---|---|
| `baseFare` missing | `400` | `{"baseFare":"Base fare is required"}` |
| `baseFare` is `0` or negative | `400` | `{"baseFare":"Base fare must be greater than zero"}` |
| `sourceStationId` or `destStationId` missing in body | `400` | `{"error":"Source and destination stations are required"}` |
| Same source and destination | `400` | `{"error":"Source and destination stations must be different"}` |
| Source station does not exist | `400` | `{"error":"Source station not found"}` |
| Destination station does not exist | `400` | `{"error":"Destination station not found"}` |
| Duplicate pair on create/update | `400` | `{"error":"A fare for this station pair already exists"}` |
| Fare ID not found | `400` | `{"error":"Fare not found"}` |
| No fare for the pair in lookup | `400` | `{"error":"Fare not found for this station pair"}` |
| Missing `sourceStationId` query parameter | `500` | `{"error":"An unexpected error occurred: Required request parameter ..."}` |
| No auth / bad password | `401` | Spring Security default body |
| Logged in but not `ADMIN` on write | `403` | `{"error":"You do not have permission to access this resource"}` |
| Delete a fare still referenced by a ticket | `500` | `{"error":"An unexpected error occurred: ..."}` |
| Two simultaneous creates of the same pair | `500` | Unique-index violation caught by the generic handler |

Two quirks worth knowing for a viva:

1. **Missing query parameter returns `500`.** Spring raises
   `MissingServletRequestParameterException`, and this project's
   `GlobalExceptionHandler` has a catch-all `Exception` handler that maps it to `500`.
   A cleaner design would handle that exception explicitly as `400`. The code is shown
   as it is; do not claim it returns `400`.
2. **Fare-not-found is `400`.** The controller throws `IllegalArgumentException`, not
   `ResourceNotFoundException`.

---

## 12. Relationships to other groups

```
Stations (1) ───< (many) Fare (many) >─── (1) Stations
                              │
                              └──< (many) Tickets
```

| Group | Relationship |
|---|---|
| **Station** (`03-STATION.md`) | Two `@ManyToOne` relations: source and destination. Both point to `Stations`. |
| **Ticket** (`09-TICKET.md`) | `Ticket.fare` is `@ManyToOne` to `Fare`. A ticket stores which fare was used. |
| **Payment** (`10-PAYMENT.md`) | Payment links to the ticket, not directly to the fare. The amount is copied into `Payment.Amount`. |
| **Schedule** (`07-SCHEDULE.md`) | No direct link. Schedules describe time; fares describe money. |
| **Auth** (`02-AUTH-AND-ROLE.md`) | Create/update/delete need `ROLE_ADMIN`. |
| **DataSeeder** | Creates 78 seeded fares, one per direction for many station pairs. |

Important consequence: a ticket stores a **link** to the fare, not a copy of the price.
If an admin later changes `baseFare`, old tickets that include the fare object will
show the new price. There is no historical price snapshot in the `Tickets` table.

Deleting a fare that any ticket references fails because of `FK_Ticket_Fare`. In the
seed data, most fares used by the five seeded tickets cannot be deleted.

---

## 13. Swagger test steps

1. Open `http://localhost:8080/swagger-ui/index.html`.
2. Click **Authorize** and enter `admin` / `admin123`.
3. Open **fare-controller**.
4. Call `GET /api/fares`. You should see the seeded fares, for example
   `FARE-STN-VSV-STN-GHP` with `baseFare` `40.00`.
5. Call `GET /api/fares/lookup` with:
   - `sourceStationId` = `STN-VSV`
   - `destStationId` = `STN-GHP`

   Expect `200` and `baseFare` `40.00`.
6. Swap the values: source `STN-GHP`, dest `STN-VSV`. This is a different pair; the
   seeder also created it, so expect `200` with `40.00`.
7. Call `POST /api/fares` with a new pair that does not exist yet, for example
   `STN-VSV` → `STN-CSM`, `baseFare` `70.00`. Expect `201`. Copy the `fareId`.
8. Call the same `POST` again. Expect `400` with the duplicate message.
9. Call `POST` with `sourceStationId` equal to `destStationId`. Expect `400` with
   `"Source and destination stations must be different"`.
10. Call `POST` with `baseFare` `0`. Expect `400` with the decimal-min message.
11. Call `PUT /api/fares/{fareId}` with `baseFare` `75.00`. Expect `200`.
12. Call `GET /api/fares/{fareId}`. Expect `200` with `75.00`.
13. Log out, authorize as `priya` / `Passw0rd!`, and repeat step 5. Expect `200`.
14. Try `POST` as `priya`. Expect `403`.
15. Authorize as `admin` again and call `DELETE /api/fares/{fareId}`. Expect `204`.
16. Try to delete `FARE-STN-VSV-STN-GHP`. It is used by seeded tickets, so expect
    `500` because of the ticket foreign key.

Basic credentials are cached by the browser; use a private window when switching users.

---

## 14. How to explain this in 2 minutes

> "The Fare group is the price list. One row means: from station A to station B, the
> price is X.
>
> The `Fares` table has exactly four columns: `FareID`, `SourceStationID`,
> `DestStationID` and `BaseFare`. Both station columns are foreign keys to `Stations`.
> `BaseFare` is `NUMERIC(10,2)`, matched by `BigDecimal` in Java, because money must not
> use `double` — `double` can produce values like `24.999999`.
>
> V1 created the table. V2 added a unique index on `(SourceStationID, DestStationID)`.
> That is the real guarantee that a pair has only one price. The Java duplicate check
> only gives a friendly error message; the index protects the data even if two requests
> arrive at the same time.
>
> In the entity, `sourceStation` and `destStation` are `@ManyToOne` with `LAZY`
> loading. They are `READ_ONLY` in JSON, so responses show the full station objects but
> requests cannot replace them. The request uses `sourceStationId` and `destStationId`
> instead. Those fields are `@Transient`, so they are not columns, and `WRITE_ONLY`, so
> they never appear in responses.
>
> `baseFare` has `@NotNull` and `@DecimalMin("0.01")`. `@Valid` makes Spring run those
> before the controller body. The database also has `NOT NULL`.
>
> The repository is a `JpaRepository` plus three derived queries. The method names are
> read by Spring Data: `existsBySourceStation_StationIdAndDestStation_StationId` means
> 'does a row exist with this source station ID and this destination station ID?' The
> underscore means 'go inside the relation'. The third method adds `FareIdNot`, which
> ignores the row being updated.
>
> The controller has six endpoints. `POST`, `PUT` and `DELETE` need `ADMIN`; `GET` only
> needs a login. `GET /api/fares/lookup` is the important one: it takes two query
> parameters and returns the exact directional fare. Fares are not symmetric — A to B
> and B to A are separate rows.
>
> `TicketController` reuses `findBySourceStation_StationIdAndDestStation_StationId` when
> a ticket is booked. If no fare exists for that direction, ticket creation fails with
> 'Fare not found for this station pair'. That is the main link between the Fare group
> and the Ticket group.
>
> Two honest limitations: deleting a fare used by a ticket gives a `500` from the
> foreign key, and a missing query parameter also gives `500` because of the catch-all
> exception handler."

---

## 15. Beginner Q&A

**Q1. Why use `BigDecimal` instead of `double` for money?**
`double` stores numbers in binary and cannot represent `0.10` exactly. Repeated
calculations produce tiny errors. `BigDecimal` stores exact decimal digits, which is
what money needs.

**Q2. What does `precision = 10, scale = 2` mean?**
At most 10 significant digits total, with 2 digits after the decimal point. The largest
value is `99999999.99`.

**Q3. Why is `fareId` a `String`?**
Seeded fares use readable IDs like `FARE-STN-AND-STN-GHP`; new ones use
`FARE-<uuid>`. Both fit in a string.

**Q4. Are fares symmetric?**
No. `(AND, GHP)` and `(GHP, AND)` are two rows. The seeder usually creates both, but the
API does not do it automatically.

**Q5. If I create only A → B, can a passenger book B → A?**
No. Ticket creation looks up the exact direction and fails with
`"Fare not found for this station pair"`.

**Q6. Why does `sourceStationId` not appear in the response?**
Because it is `@JsonProperty(access = WRITE_ONLY)`. The response already contains the
full `sourceStation` object.

**Q7. Why not just store the station IDs as strings and skip `@ManyToOne`?**
You could, but then the database would not enforce that the station exists, and JSON
responses would not include station details. The relation gives both.

**Q8. Why `FetchType.LAZY`?**
So listing all fares does not immediately join every station row. The stations load
when serialization needs them. `spring.jpa.open-in-view=true` keeps the session open
for that.

**Q9. What does `@Transient` do here?**
It tells JPA that `sourceStationId` and `destStationId` are not columns in `Fares`.
They exist only to carry request values into the controller.

**Q10. Why is there both a Java duplicate check and a database unique index?**
The Java check gives a clear `400` message in normal use. The index is the final
guarantee and also protects against two simultaneous requests. Java-only checking has a
race condition.

**Q11. What happens if two admins create the same pair at the same time?**
Both Java checks may pass. One insert succeeds; the other violates
`UX_Fare_Station_Pair` and returns `500` through the generic handler.

**Q12. Why does `updateFare` use `FareIdNot`?**
Without it, the existing row would match the duplicate check and the update would
always fail. `FareIdNot` excludes the row being edited.

**Q13. Why is `GET /lookup` not confused with `GET /{id}`?**
Spring MVC prefers a literal path segment over a path variable. `/lookup` matches the
literal mapping exactly, so `getFareByStations` wins.

**Q14. Can a fare join two stations on different lines?**
Yes. The controller only checks that both stations exist and are different. It does not
check line colour or route membership.

**Q15. Does the fare depend on distance?**
No. `BaseFare` is stored manually. `DataSeeder` happens to increase prices with more
stops, but that is just the data, not a calculation in code.

**Q16. Does the fare depend on ticket type?**
No. A `SINGLE`, `DAILY` or `WEEKLY` ticket all point to the same fare row. Ticket type
is stored on the ticket, not priced separately.

**Q17. Why is there no `CreatedAt` on `Fares`?**
The table was designed as a simple price list. No audit column was added. Not every
table has timestamps in this schema.

**Q18. What happens if I delete a fare used by a ticket?**
The database foreign key `FK_Ticket_Fare` rejects it. The response is `500`, because
the controller does not catch the database exception.

**Q19. Why does a missing query parameter return `500` instead of `400`?**
Spring throws `MissingServletRequestParameterException` before entering the method.
`GlobalExceptionHandler` has a catch-all `Exception` handler but no specific handler
for that exception, so it becomes `500`. This is a known rough edge.

**Q20. What is the difference between `existsBy...` and `findBy...`?**
`existsBy...` returns a `boolean` and lets the database answer with a count/exists
query. `findBy...` loads the actual entity, wrapped in `Optional` because it may not
exist.

**Q21. Why is `@Valid` placed before `@RequestBody`?**
The order of annotations does not matter to Spring; `@Valid` tells it to validate the
argument after Jackson builds it. Writing `@Valid @RequestBody` is the common style.

**Q22. Can an admin change the `fareId` with `PUT`?**
No. `copyFareFields` never touches `fareId`. The ID in the URL identifies the row.

---

## 16. Glossary

| Term | Meaning |
|---|---|
| **`BigDecimal`** | Java class for exact decimal arithmetic, used for money. |
| **Bean** | Object managed by Spring. |
| **Controller** | Class that turns HTTP requests into method calls. |
| **CRUD** | Create, Read, Update, Delete. |
| **Dependency injection** | Supplying objects a class needs from outside. |
| **Derived query** | Query generated from a repository method name. |
| **Directional** | A → B is different from B → A. |
| **Entity** | Java class mapped to a table. |
| **Foreign key** | Column referencing another table's primary key. |
| **Idempotent** | Repeating the call has the same end state. |
| **Index** | Database structure that speeds up lookups and can enforce uniqueness. |
| **JPA** | Java Persistence API. |
| **JSON** | Text format for structured data. |
| **Lazy loading** | Loading a relation only when accessed. |
| **`NUMERIC(p,s)`** | SQL decimal type with precision `p` and scale `s`. |
| **Open-in-view** | Keeps the Hibernate session open during JSON serialization. |
| **`Optional`** | Box that may contain a value or be empty. |
| **Precision** | Total digits in a decimal number. |
| **Primary key** | Column that uniquely identifies a row. |
| **Query string** | `?key=value&key2=value2` part of a URL. |
| **Scale** | Digits after the decimal point. |
| **Serialization** | Object → JSON. |
| **Spring Boot** | Framework that starts and wires the app. |
| **Status code** | Numeric HTTP result, e.g. `201`, `400`, `403`. |
| **Transaction** | Unit of database work that fully commits or fully rolls back. |
| **Unique index** | Index that forbids duplicate value combinations. |
| **UUID** | Random 128-bit identifier. |
| **Validation** | Checking input rules before using data. |

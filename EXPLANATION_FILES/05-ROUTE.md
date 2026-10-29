# 05 — Route Group

**In one sentence:** a Route is a named metro line between two end stations, with a distance, a travel time, a colour, and an ordered list of stops stored in a separate table.

**Files in this group**

| File | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/Route.java` |
| Controller | `src/main/java/com/metro/ticketing/controller/RouteController.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/RouteRepository.java` |
| Stop repository (used here) | `src/main/java/com/metro/ticketing/repository/RouteStationRepository.java` |
| Station repository (used here) | `src/main/java/com/metro/ticketing/repository/StationRepository.java` |
| Database table | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Seed data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` |

---

## 1. Real-life analogy

Think of a **bus route printed on a board at the bus stop**.

The board says:

- Route name: "Line 1 — Versova to Ghatkopar"
- Total distance: 11.10 km
- Estimated time: 21 minutes
- Colour: Blue
- First stop: Versova
- Last stop: Ghatkopar

That is the `Route` row. It has a name, a length, a duration, a colour, and two special stops (first and last).

But the board also has a **list of stops in order**:

```
1. Versova
2. Andheri
3. Western Express Highway
4. Airport Road
5. Marol Naka
6. Saki Naka
7. Ghatkopar
```

That list is **not** stored inside the route row. In a database you cannot put a variable-length list into one column without breaking normalisation. So every stop becomes its own row in a second table, `"RouteStations"`, and each row says "route X, station Y, position N".

Two consequences follow, and they explain most of the code:

1. **Creating a route is two writes**, not one: insert the route, then insert its stops.
2. **The route JSON needs the list glued back on**, because the entity does not own it. That is what the `@Transient List<String> stationIds` field is for.

The same relationship exists in real transit systems: a "line" is a concept, and "line stops" is a separate timetable table.

---

## 2. Words you need

| Word | One-sentence meaning |
|---|---|
| Spring Boot | Framework that starts the server and wires classes together. |
| Annotation | An `@Something` label that instructs the framework. |
| Bean | An object created and managed by Spring. |
| Dependency injection | Spring passes required objects into a constructor. |
| Controller | Class that handles HTTP requests. |
| Endpoint | One URL plus one HTTP method. |
| HTTP method | GET read, POST create, PUT update, DELETE remove. |
| JSON | Text data format with `{}` objects and `[]` arrays. |
| Jackson | Library converting Java objects to and from JSON. |
| Serialization | Java -> JSON. |
| Deserialization | JSON -> Java. |
| JPA | Java standard for mapping classes to tables. |
| Hibernate | The library that implements JPA and writes SQL. |
| Entity | Class mapped to a table. |
| ORM | Treating tables as classes and rows as objects. |
| Table | Grid of rows and columns. |
| Column | One field of a table. |
| Row | One record in a table. |
| Primary key | Column that uniquely identifies a row. |
| Foreign key | Column whose value must exist as a primary key elsewhere. |
| Unique constraint | Rule that a combination of columns may not repeat. |
| `@ManyToOne` | JPA mapping: many rows here point to one row there. |
| `@JoinColumn` | Tells JPA which column holds the foreign key. |
| `FetchType.LAZY` | Do not load the related row until someone reads it. |
| Proxy | Stand-in object Hibernate uses for a not-yet-loaded relation. |
| `@Transient` | "JPA, ignore this field; do not make a column for it." |
| `@JsonProperty(access = ...)` | Jackson instruction to make a field read-only or write-only in JSON. |
| Repository | Interface providing database methods. |
| `JpaRepository` | Spring Data interface with ready-made CRUD methods. |
| Derived query method | Repository method whose name Spring turns into SQL. |
| Transaction | All-or-nothing group of database changes. |
| `@Transactional` | Wraps a method in one transaction. |
| Bean Validation | `@NotBlank` / `@NotNull` style field checks. |
| `@Valid` | Makes Spring enforce those checks on the request body. |
| `ResponseEntity` | Carries status code plus body. |
| HTTP status | Number telling what happened: 200, 201, 204, 400, 401, 403, 500. |
| `Optional` | Box that may hold a value or nothing. |
| UUID | Long random string used to build unique IDs. |
| `BigDecimal` | Exact decimal number class, used for distances and money. |
| `LocalDateTime` | Date plus time, e.g. `2026-10-29T10:15:30`. |
| `ArrayList` | Growable Java list. |
| Lambda | Short inline function, here used in `orElseThrow(() -> ...)`. |
| Basic authentication | `Authorization: Basic base64(user:password)` on every request. |
| Role | Permission label such as `ADMIN`. |
| `@PreAuthorize` | Role check that runs before a method. |
| Flyway | Tool that applies numbered SQL migrations. |
| Migration | One numbered SQL file. |
| Seeder | Class that inserts demo data at startup. |
| N+1 query problem | One query for a list plus one extra query per item. |
| Swagger UI | Web page for testing endpoints. |

---

## 3. Files in this group

```
src/main/java/com/metro/ticketing/
├── entity/Route.java                 <- the line itself
├── controller/RouteController.java   <- 5 endpoints + 4 private helpers
└── repository/RouteRepository.java   <- one-line CRUD interface

src/main/java/com/metro/ticketing/repository/
├── RouteStationRepository.java       <- used to read/write the stop list
└── StationRepository.java            <- used to look up each station

src/main/resources/db/migration/
└── V1__create_metro_ticketing_schema.sql   <- "Route" table + FKs

src/main/java/com/metro/ticketing/config/
└── DataSeeder.java                   <- inserts 3 demo routes
```

`RouteController` is the only controller in the project that depends on **three** repositories. That is the main structural difference from Station or Train.

The companion file `06-ROUTE-STATION.md` covers the stop list in full detail. This file covers the `Route` row and how the controller manages both together.

---

## 4. Database table

Exact SQL:

```sql
CREATE TABLE IF NOT EXISTS "Route" (
    "RouteID" VARCHAR(50) PRIMARY KEY,
    "RouteName" VARCHAR(150) NOT NULL,
    "TotalDistance" NUMERIC(10,2),
    "EstimatedTime" INTEGER,
    "LineColor" VARCHAR(50),
    "CreatedAt" TIMESTAMP,
    "UpdatedAt" TIMESTAMP,
    "StartStationID" VARCHAR(50),
    "EndStationID" VARCHAR(50),
    CONSTRAINT "FK_Route_StartStation" FOREIGN KEY ("StartStationID") REFERENCES "Stations" ("StationID"),
    CONSTRAINT "FK_Route_EndStation" FOREIGN KEY ("EndStationID") REFERENCES "Stations" ("StationID")
);
```

### Column by column

| Column | SQL type | Meaning | Empty allowed? |
|---|---|---|---|
| `RouteID` | `VARCHAR(50)` | Primary key, generated as `RT-<uuid>`. | No |
| `RouteName` | `VARCHAR(150)` | Human name, e.g. `Mumbai Metro Line 1 - Blue (Versova to Ghatkopar)`. | No |
| `TotalDistance` | `NUMERIC(10,2)` | Length in kilometres, two decimal places, max 8 digits before the point. | Yes |
| `EstimatedTime` | `INTEGER` | Journey time in minutes. | Yes |
| `LineColor` | `VARCHAR(50)` | `Blue`, `Aqua`, `Red`. | Yes |
| `CreatedAt` | `TIMESTAMP` | Row creation time. | Yes |
| `UpdatedAt` | `TIMESTAMP` | Last update time. | Yes |
| `StartStationID` | `VARCHAR(50)` | Foreign key to `"Stations"`. First stop. | Yes |
| `EndStationID` | `VARCHAR(50)` | Foreign key to `"Stations"`. Last stop. | Yes |

### Why the table is called `Route`, singular

Every other table is plural (`Stations`, `Trains`, `Tickets`). `Route` is the odd one out. It is just a naming inconsistency in the original schema. The entity matches it exactly with `@Table(name = "\"Route\"")`, so nothing breaks. Point it out before an evaluator does.

### Why `NUMERIC(10,2)` and not `DOUBLE`

`NUMERIC(10,2)` means "exact decimal with 10 total digits, 2 after the point". It is the safe type for values people read as decimals. Binary floating point (`double`) can store `0.1` as `0.1000000000000000055`, which is unacceptable for money and awkward for distances. Java sees this column as `BigDecimal`.

### Why only start and end are columns

The middle stops are variable in number. A route can have 2 stops or 40. SQL columns are fixed at table-creation time, so a list cannot be a column. The list lives in `"RouteStations"`:

```sql
CREATE TABLE IF NOT EXISTS "RouteStations" (
    "RouteID" VARCHAR(50) NOT NULL,
    "StationID" VARCHAR(50) NOT NULL,
    "SequenceNumber" INTEGER NOT NULL,
    PRIMARY KEY ("RouteID", "StationID"),
    ...
);
```

Later migration `V6__replace_route_station_composite_key.sql` replaces that primary key with a single `RouteStationID` column, but keeps `UNIQUE ("RouteID","StationID")`. Full details are in `06-ROUTE-STATION.md`.

### Why both `StartStationID` and a stop list

They serve different purposes:

- `StartStationID` / `EndStationID` answer "where does this line begin and end?" with a single join. Fast and simple.
- The stop list answers "what is the full ordered sequence?" and includes the ends.

This is duplication: the ends appear in both places. A perfectly normalised design would derive the ends from sequence 1 and the maximum sequence. This project stores them explicitly because it makes queries and validation simpler, and because the seeder fills them in.

### Foreign keys

```
FK_Route_StartStation: "StartStationID" -> "Stations"."StationID"
FK_Route_EndStation:   "EndStationID"   -> "Stations"."StationID"
```

Both columns are nullable at the SQL level, but the controller refuses to create a route without both IDs. So in practice every route made through the API has them.

---

## 5. Entity line by line

Full file with a comment on every annotation and field:

```java
package com.metro.ticketing.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // JSON: hide Hibernate internals
import com.fasterxml.jackson.annotation.JsonProperty;        // JSON: read-only / write-only
import jakarta.persistence.*;                                // all JPA annotations
import jakarta.validation.constraints.NotBlank;              // required-text check

import java.math.BigDecimal;                                 // exact decimal
import java.time.LocalDateTime;                              // date + time
import java.util.List;                                       // stop id list

@Entity                                                      // 1. map class to a table
@Table(name = "\"Route\"")                                   // 2. exact singular table name
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
                                                             // 3. drop proxy noise from JSON
public class Route {

    @Id                                                      // 4. primary key
    @Column(name = "\"RouteID\"", length = 50)               // 5. column "RouteID"
    private String routeId;                                  // 6. "RT-<uuid>"

    @NotBlank(message = "Route name is required")            // 7. blank name -> 400
    @Column(name = "\"RouteName\"", length = 150, nullable = false)
                                                             // 8. NOT NULL, max 150
    private String routeName;                                // 9. display name

    @Column(name = "\"TotalDistance\"", precision = 10, scale = 2)
                                                             // 10. NUMERIC(10,2)
    private BigDecimal totalDistance;                        // 11. kilometres, e.g. 11.10

    @Column(name = "\"EstimatedTime\"")                      // 12. column "EstimatedTime"
    private Integer estimatedTime;                           // 13. minutes, e.g. 21

    @Column(name = "\"LineColor\"", length = 50)             // 14. column "LineColor"
    private String lineColor;                                // 15. "Blue" / "Aqua" / "Red"

    @Column(name = "\"CreatedAt\"")                          // 16. creation timestamp
    private LocalDateTime createdAt = LocalDateTime.now();   // 17. set when object is built

    @Column(name = "\"UpdatedAt\"")                          // 18. last-update timestamp
    private LocalDateTime updatedAt = LocalDateTime.now();   // 19. also set at build time

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)    // 20. serialize out, never read in
    @ManyToOne(fetch = FetchType.LAZY)                       // 21. many routes -> one station
    @JoinColumn(name = "\"StartStationID\"", referencedColumnName = "\"StationID\"")
                                                             // 22. FK column and target column
    private Station startStation;                            // 23. Station object, loaded on demand

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)    // 24. same rules for the end
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"EndStationID\"", referencedColumnName = "\"StationID\"")
    private Station endStation;

    @Transient                                               // 25. JPA: not a column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)   // 26. read from JSON, never write out
    private String startStationId;                           // 27. request-only carrier

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String endStationId;                             // 28. request-only carrier

    @Transient                                               // 29. not a column, but still serialized
    private List<String> stationIds;                         // 30. response carrier + request input

    // ... getters and setters for all ten fields ...
}
```

### Annotation-by-annotation

| Annotation | Meaning |
|---|---|
| `@Entity` | "This class is a table." |
| `@Table(name = "\"Route\"")` | "The table is named exactly `Route`." |
| `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` | "Ignore these Hibernate proxy fields when writing JSON." |
| `@Id` | "Primary key." |
| `@Column` | "Column name and limits." |
| `@Column(precision = 10, scale = 2)` | "`NUMERIC(10,2)`." |
| `@NotBlank` | "Reject null, empty, or whitespace-only strings." |
| `@ManyToOne` | "Many route rows may point at the same station row." |
| `@JoinColumn(name = ..., referencedColumnName = ...)` | "The foreign key column is `StartStationID` and it points at `Stations.StationID`." |
| `FetchType.LAZY` | "Do not fetch the station until someone calls `getStartStation()`." |
| `@JsonProperty(access = READ_ONLY)` | "Include this in responses, ignore it in requests." |
| `@JsonProperty(access = WRITE_ONLY)` | "Accept this in requests, never include it in responses." |
| `@Transient` | "JPA, do not persist this field." |

### Why `startStation` is READ_ONLY

The client should send `"startStationId": "STN-VSV"`, a simple string. It should not send a whole nested station object, because that would let the client invent station details.

But the response should contain the full station, so the UI can show the code and name without a second request. READ_ONLY gives exactly that: output yes, input no.

If a client sends `"startStation": {...}`, Jackson ignores it silently. The route then fails the `startStationId == null` check, which is a slightly confusing error for that client. Worth knowing.

### Why `startStationId` is `@Transient` and WRITE_ONLY

The database column is `StartStationID`, and the entity represents it as a `Station` object, not a string. So there is no field for a plain string ID. The `@Transient` string exists purely as a JSON landing pad:

- `@Transient` -> JPA will not try to create a second column called `startStationId`.
- `@JsonProperty(WRITE_ONLY)` -> it appears in requests but not in responses.

If you forgot `@Transient`, startup validation would fail because Hibernate would look for a `startStationId` column that does not exist.

### Why `stationIds` is `@Transient` but not WRITE_ONLY

This is the one field that goes **both ways**:

- In a request, the client sends the ordered stop list: `"stationIds": ["STN-VSV", "STN-AND", ...]`.
- In a response, the controller fills it with the stored order so the client can see the route.

Because it has no `@JsonProperty` restriction, Jackson reads and writes it normally. The only annotation is `@Transient`, which stops JPA from mapping it.

A subtle effect: any `Route` serialized without the controller filling it in has `stationIds: null`. For example, a route nested inside a Schedule response shows `"stationIds": null`. That is not a bug in Jackson; it is simply an unset field.

### Why `createdAt` and `updatedAt` have initialisers *and* are set in the controller

The initialisers make sure a bare `new Route()` already has timestamps. The controller also sets them explicitly on create:

```java
route.setCreatedAt(LocalDateTime.now());
route.setUpdatedAt(LocalDateTime.now());
```

That is redundant but harmless. On update it sets only `updatedAt`, so the original creation time is preserved. The client cannot set either, because `copyRouteFields` never copies them.

---

## 6. Repository line by line

```java
package com.metro.ticketing.repository;

import com.metro.ticketing.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository                                          // 1. Spring bean
public interface RouteRepository extends JpaRepository<Route, String> {
                                                     // 2. Route entity, String id
}
```

No custom methods. The controller needs only standard CRUD.

### Inherited methods and their SQL

| Java call | SQL |
|---|---|
| `routeRepository.save(newRoute)` | `INSERT INTO "Route" ("RouteID","RouteName","TotalDistance","EstimatedTime","LineColor","CreatedAt","UpdatedAt","StartStationID","EndStationID") VALUES (?,?,?,?,?,?,?,?,?)` |
| `routeRepository.save(existingRoute)` | `UPDATE "Route" SET "RouteName"=?, ... WHERE "RouteID"=?` |
| `routeRepository.findById("RT-L1")` | `SELECT ... FROM "Route" WHERE "RouteID"=?` |
| `routeRepository.findAll()` | `SELECT ... FROM "Route"` |
| `routeRepository.existsById("RT-L1")` | `SELECT 1 FROM "Route" WHERE "RouteID"=? LIMIT 1` |
| `routeRepository.deleteById("RT-L1")` | `DELETE FROM "Route" WHERE "RouteID"=?` |

Notice that `StartStationID` and `EndStationID` are written as plain strings, taken from the `Station` objects' IDs. Hibernate does not insert the whole station row. The `Station` objects must already exist, and their IDs must come from the database, which is why the controller looks them up before saving.

### Two more repositories used by this controller

`RouteStationRepository`:

```java
@Repository
public interface RouteStationRepository extends JpaRepository<RouteStation, String> {
    List<RouteStation> findByRoute_RouteIdOrderBySequenceNumberAsc(String routeId);
    void deleteByRoute_RouteId(String routeId);
}
```

| Method | Generated SQL | Purpose |
|---|---|---|
| `findByRoute_RouteIdOrderBySequenceNumberAsc(id)` | `SELECT ... FROM "RouteStations" WHERE "RouteID"=? ORDER BY "SequenceNumber" ASC` | Read the stops in order. |
| `deleteByRoute_RouteId(id)` | `SELECT ...` then `DELETE FROM "RouteStations" WHERE "RouteStationID"=?` for each row | Clear the stop list. |

**How to read `findByRoute_RouteId`:** the underscore means "go inside the `route` property, then take its `routeId`". It is not a column called `route_routeId`. Spring Data translates `Route_RouteId` into the foreign key `RouteID`.

**About the delete:** derived delete methods load matching entities and remove them one by one, so the log shows a SELECT plus N DELETEs. For a handful of stops this is fine. A bulk `@Modifying @Query("delete from RouteStation rs where rs.route.routeId = :id")` would be one statement, but the project keeps the derived form for readability.

`StationRepository` is the same one-line CRUD interface described in `03-STATION.md`. The controller uses only `findById`.

### Why the controller needs all three

- `RouteRepository` — the route row.
- `RouteStationRepository` — the stop rows, which are a separate table.
- `StationRepository` — turn a client-supplied station ID into a real `Station`, or fail.

No single repository can do the whole job, because the data is spread over three tables.

---

## 7. Controller endpoints

Base path: `/api/routes`.

| # | Method | Full path | Who | Purpose | Success |
|---|---|---|---|---|---|
| 1 | POST | `/api/routes` | `ADMIN` | Create route + stops | `201 Created` |
| 2 | PUT | `/api/routes/{id}` | `ADMIN` | Update route and replace stops | `200 OK` |
| 3 | GET | `/api/routes/{id}` | authenticated | Route plus ordered station IDs | `200 OK` |
| 4 | GET | `/api/routes` | authenticated | All routes, each with station IDs | `200 OK` |
| 5 | DELETE | `/api/routes/{id}` | `ADMIN` | Delete stops, then the route | `204 No Content` |

Three of them also change `"RouteStations"`, even though that table has no controller of its own:

| Endpoint | Effect on `"RouteStations"` |
|---|---|
| POST | Inserts one row per stop, sequence 1..N. |
| PUT | Deletes all old rows, then inserts the new list. |
| DELETE | Deletes all rows for the route, then deletes the route. |
| GET | Reads rows to build the `stationIds` array. |

This "nested resource" style is common: the child rows are managed through the parent URL.

---

## 8. Controller line by line

Full file with comments:

```java
package com.metro.ticketing.controller;

import com.metro.ticketing.entity.Route;
import com.metro.ticketing.entity.RouteStation;
import com.metro.ticketing.entity.Station;
import com.metro.ticketing.repository.RouteRepository;
import com.metro.ticketing.repository.RouteStationRepository;
import com.metro.ticketing.repository.StationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController                                          // 1. JSON in, JSON out
@RequestMapping("/api/routes")                           // 2. base path
public class RouteController {

    private final RouteRepository routeRepository;       // 3. route row access
    private final RouteStationRepository routeStationRepository; // 4. stop list access
    private final StationRepository stationRepository;   // 5. station lookup

    public RouteController(RouteRepository routeRepository,
                           RouteStationRepository routeStationRepository,
                           StationRepository stationRepository) {
                                                         // 6. Spring injects three beans
        this.routeRepository = routeRepository;
        this.routeStationRepository = routeStationRepository;
        this.stationRepository = stationRepository;
    }

    @PostMapping                                         // 7. POST /api/routes
    @PreAuthorize("hasRole('ADMIN')")                    // 8. admin only
    @Transactional                                       // 9. route + stops in one transaction
    public ResponseEntity<Route> createRoute(@Valid @RequestBody Route request) {
        Route route = new Route();                       // 10. empty entity
        route.setRouteId("RT-" + UUID.randomUUID());     // 11. server-generated ID
        copyRouteFields(request, route);                  // 12. name, distance, time, colour, stations
        route.setCreatedAt(LocalDateTime.now());         // 13. explicit creation time
        route.setUpdatedAt(LocalDateTime.now());         // 14. same value for update time
        route = routeRepository.save(route);             // 15. INSERT the route

        saveRouteStations(route, request.getStationIds()); // 16. INSERT one row per stop
        return ResponseEntity.status(HttpStatus.CREATED).body(loadRoute(route.getRouteId()));
                                                         // 17. re-read with stationIds filled in
    }

    @PutMapping("/{id}")                                 // 18. PUT /api/routes/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Route> updateRoute(@PathVariable String id, @Valid @RequestBody Route request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));
                                                         // 19. load or 400
        copyRouteFields(request, route);                 // 20. overwrite editable fields
        route.setUpdatedAt(LocalDateTime.now());         // 21. bump update time only
        route = routeRepository.save(route);             // 22. UPDATE route row

        routeStationRepository.deleteByRoute_RouteId(id);  // 23. remove old stop rows
        saveRouteStations(route, request.getStationIds()); // 24. insert the new stop rows

        return ResponseEntity.ok(loadRoute(id));         // 25. return rebuilt response
    }

    @GetMapping("/{id}")                                 // 26. GET /api/routes/{id}
    @Transactional(readOnly = true)
    public ResponseEntity<Route> getRouteById(@PathVariable String id) {
        return ResponseEntity.ok(loadRoute(id));         // 27. loadRoute throws if absent
    }

    @GetMapping                                          // 28. GET /api/routes
    @Transactional(readOnly = true)
    public ResponseEntity<List<Route>> getAllRoutes() {
        List<Route> routes = routeRepository.findAll();   // 29. one query
        routes.forEach(route -> route.setStationIds(loadStationIds(route.getRouteId())));
                                                         // 30. one extra query per route
        return ResponseEntity.ok(routes);                 // 31. array response
    }

    @DeleteMapping("/{id}")                               // 32. DELETE /api/routes/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteRoute(@PathVariable String id) {
        if (!routeRepository.existsById(id)) {            // 33. friendly not-found
            throw new IllegalArgumentException("Route not found");
        }
        routeStationRepository.deleteByRoute_RouteId(id); // 34. children first (FK)
        routeRepository.deleteById(id);                   // 35. then the parent
        return ResponseEntity.noContent().build();        // 36. 204
    }

    private Route loadRoute(String routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));
                                                         // 37. same load-or-throw
        route.setStationIds(loadStationIds(routeId));     // 38. attach ordered ids
        return route;                                     // 39. return enriched object
    }

    private List<String> loadStationIds(String routeId) {
        List<String> stationIds = new ArrayList<>();      // 40. mutable result list
        for (RouteStation routeStation :
                routeStationRepository.findByRoute_RouteIdOrderBySequenceNumberAsc(routeId)) {
                                                         // 41. stops sorted by sequence
            stationIds.add(routeStation.getStation().getStationId());
                                                         // 42. lazy-load station, take its id
        }
        return stationIds;                                // 43. e.g. [STN-VSV, STN-AND, ...]
    }

    private void copyRouteFields(Route request, Route route) {
        route.setRouteName(request.getRouteName());       // 44. required name
        route.setTotalDistance(request.getTotalDistance());
        route.setEstimatedTime(request.getEstimatedTime());
        route.setLineColor(request.getLineColor());

        if (request.getStartStationId() == null || request.getEndStationId() == null) {
            throw new IllegalArgumentException("Start and end stations are required");
        }                                                 // 45. both IDs mandatory

        Station start = stationRepository.findById(request.getStartStationId())
                .orElseThrow(() -> new IllegalArgumentException("Start station not found"));
                                                         // 46. must exist
        Station end = stationRepository.findById(request.getEndStationId())
                .orElseThrow(() -> new IllegalArgumentException("End station not found"));
                                                         // 47. must exist

        route.setStartStation(start);                     // 48. set the real entity
        route.setEndStation(end);                         // 49. set the real entity
    }

    private void saveRouteStations(Route route, List<String> stationIds) {
        List<String> orderedStations = stationIds;        // 50. request list
        if (orderedStations == null || orderedStations.isEmpty()) {
            orderedStations = List.of(
                    route.getStartStation().getStationId(),
                    route.getEndStation().getStationId());
        }                                                 // 51. fallback: just the two ends

        int sequenceNumber = 1;                           // 52. first stop is number 1
        for (String stationId : orderedStations) {
            Station station = stationRepository.findById(stationId)
                    .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));
                                                         // 53. validate every stop

            RouteStation routeStation = new RouteStation(); // 54. one row
            routeStation.setRouteStationId("RS-" + UUID.randomUUID()); // 55. its own key
            routeStation.setRoute(route);                 // 56. FK to the route
            routeStation.setStation(station);             // 57. FK to the station
            routeStation.setSequenceNumber(sequenceNumber++); // 58. 1, 2, 3, ... then increment
            routeStationRepository.save(routeStation);    // 59. INSERT the stop
        }
    }
}
```

### `createRoute` in words

1. Admin check.
2. JSON -> `Route` request object. Note that `startStation` and `endStation` are READ_ONLY, so only `startStationId`, `endStationId`, and `stationIds` arrive.
3. `@Valid` checks `routeName`.
4. New entity, server ID `RT-...`.
5. `copyRouteFields` copies scalar fields, demands both station IDs, and loads both `Station` rows.
6. Timestamps are set.
7. INSERT the route.
8. `saveRouteStations` inserts the stops, or just the two ends if no list was given.
9. `loadRoute` re-reads the row and attaches `stationIds`, so the response shows what was actually saved.
10. `201` with the full object.

Why re-read instead of using the object in memory? Because `request.getStationIds()` may have been `null`, in which case the real stored list is different from the request. Re-reading guarantees the response reflects the database.

### `updateRoute` in words

1. Load the route or 400.
2. Copy fields onto it; this re-validates and re-loads both stations.
3. Set `updatedAt` to now; keep `createdAt`.
4. UPDATE the route row.
5. **Delete every existing stop row** for this route.
6. Insert the new list.
7. Return the reloaded route.

This is "replace the whole list" semantics, not "merge". If the client sends no `stationIds`, step 6 falls back to the two ends, so a 7-stop route silently becomes a 2-stop route. That is the most surprising behaviour in this controller and worth mentioning in a viva.

Deleting and re-inserting is simpler than diffing the old and new lists. It changes the generated `RouteStationID` values every time, which is harmless because nothing else references them.

### `loadStationIds` and the N+1 problem

`findByRoute_RouteIdOrderBySequenceNumberAsc` returns `RouteStation` objects. Each has a `station` field marked `FetchType.LAZY`, so Hibernate does not join the station table automatically. The loop calls `getStation()` for each row, which triggers one SELECT per stop.

For 7 stops that is 1 + 7 = 8 queries. For the three seeded routes it is 17 statements: 1 to read all routes, 3 to read their stop lists, and 13 lazy station reads. This is the classic **N+1 query problem**.

It works because `spring.jpa.open-in-view=true` keeps the Hibernate session open while Jackson serializes the response. Without that setting, lazy loading outside a transaction would throw `LazyInitializationException`.

A fix would be a JPQL join fetch:

```java
@Query("select rs from RouteStation rs join fetch rs.station where rs.route.routeId = :id order by rs.sequenceNumber")
```

The project does not do that. For demo sizes it is acceptable, and being able to explain the trade-off is better than hiding it.

### `getAllRoutes`

One query for routes, then `loadStationIds` per route. Same N+1 pattern, one level up.

### `deleteRoute`

Children first, parent second. This order is required by the foreign key `FK_RouteStations_Route`. There is no `ON DELETE CASCADE` in the SQL, so the app must do it manually.

But deleting the route still fails if a `Schedule` points at it (`FK_Schedule_Route`). The controller does not delete schedules. That is deliberate: a timetable is historical data. The database returns a foreign-key error and the API returns 500.

---

## 9. One request from start to finish

Let us create a small route with three stops.

```http
POST /api/routes HTTP/1.1
Host: localhost:8080
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "routeName": "Test Line",
  "totalDistance": 5.50,
  "estimatedTime": 12,
  "lineColor": "Blue",
  "startStationId": "STN-VSV",
  "endStationId": "STN-GHP",
  "stationIds": ["STN-VSV", "STN-AND", "STN-GHP"]
}
```

1. **Tomcat accepts** the connection on port 8080.
2. **Security filters** decode the Basic header to `admin:admin123`.
3. `DbUserDetailsService` loads the `admin` user with `SELECT ... FROM "User" WHERE "Contact" = 'admin'`.
4. BCrypt matches the password; the request gets `ROLE_ADMIN`.
5. **Spring MVC** matches `POST /api/routes` to `createRoute`.
6. **`@PreAuthorize`** passes.
7. **Jackson** builds the request `Route`: name, distance `5.50`, time `12`, colour `Blue`, `startStationId`, `endStationId`, `stationIds`.
8. Jackson ignores `startStation` / `endStation` because they are READ_ONLY.
9. **`@Valid`** checks `routeName`; it is fine.
10. **`new Route()`** creates the entity; `createdAt` and `updatedAt` are already `now`.
11. `setRouteId("RT-" + UUID.randomUUID())` -> `RT-7a1c9f0e-5b62-4c8d-9a3f-2d1e6b7c8f90`.
12. **`copyRouteFields`** copies the four scalar fields. It sees both station IDs are present.
13. `stationRepository.findById("STN-VSV")` -> `SELECT ... WHERE "StationID"='STN-VSV'` -> the Versova entity.
14. `stationRepository.findById("STN-GHP")` -> Ghatkopar.
15. `route.setStartStation(start)` and `setEndStation(end)`.
16. The controller overwrites both timestamps with `LocalDateTime.now()`.
17. **`routeRepository.save(route)`** runs
    `INSERT INTO "Route" ("RouteID","RouteName","TotalDistance","EstimatedTime","LineColor","CreatedAt","UpdatedAt","StartStationID","EndStationID") VALUES ('RT-7a1c...','Test Line',5.50,12,'Blue', now, now, 'STN-VSV', 'STN-GHP')`.
18. PostgreSQL checks both foreign keys. Both stations exist, so the row is accepted.
19. **`saveRouteStations`** receives the three IDs. The list is not empty, so no fallback is used.
20. Stop 1: `findById("STN-VSV")` -> entity. `sequenceNumber = 1`. INSERT:
    `INSERT INTO "RouteStations" ("RouteStationID","RouteID","StationID","SequenceNumber") VALUES ('RS-1...','RT-7a1c...','STN-VSV',1)`.
21. Stop 2: `findById("STN-AND")` -> INSERT with `RS-2...` and sequence `2`.
22. Stop 3: `findById("STN-GHP")` -> INSERT with `RS-3...` and sequence `3`.
23. The unique constraint `uq_route_station` on `(RouteID, StationID)` is checked on every insert. All three stations are different, so all pass.
24. **The method returns; the transaction commits.** Had any insert failed, all four inserts would roll back together.
25. **`loadRoute("RT-7a1c...")`** re-reads the route with `SELECT ... WHERE "RouteID"=?`.
26. `loadStationIds` runs `SELECT ... FROM "RouteStations" WHERE "RouteID"=? ORDER BY "SequenceNumber" ASC`.
27. It gets three rows. For each, `getStation()` lazily runs `SELECT ... FROM "Stations" WHERE "StationID"=?`, producing `[STN-VSV, STN-AND, STN-GHP]`.
28. The list is stored in the transient `stationIds` field.
29. **Jackson serializes** the route: scalars, the nested `startStation`, the nested `endStation`, and `stationIds`.
30. `201 Created` is written. Swagger shows the new `routeId`.

Total database work: 1 user read, 2 station reads for start/end, 1 route insert, 3 station reads + 3 stop inserts, 1 route re-read, 1 stop list read, 3 lazy station reads. About 15 statements for one API call. That is the cost of a simple design.

---

## 10. JSON request and response examples

### Create — request

```json
{
  "routeName": "Test Line",
  "totalDistance": 5.50,
  "estimatedTime": 12,
  "lineColor": "Blue",
  "startStationId": "STN-VSV",
  "endStationId": "STN-GHP",
  "stationIds": ["STN-VSV", "STN-AND", "STN-GHP"]
}
```

| Field | Type | Required | Meaning |
|---|---|---|---|
| `routeName` | string, max 150 | Yes | Display name. Blank -> 400. |
| `totalDistance` | decimal | No | Kilometres. Stored as `NUMERIC(10,2)`. |
| `estimatedTime` | integer | No | Minutes end to end. |
| `lineColor` | string | No | `Blue`, `Aqua`, or `Red`. |
| `startStationId` | string | Yes | Existing `StationID`. |
| `endStationId` | string | Yes | Existing `StationID`. |
| `stationIds` | array of strings | No | Ordered stops. If missing or empty, the server uses start + end. |

Do **not** send `startStation` or `endStation` objects. They are `READ_ONLY`, so they are ignored on input.

### Create — response `201 Created`

```json
{
  "routeId": "RT-7a1c9f0e-5b62-4c8d-9a3f-2d1e6b7c8f90",
  "routeName": "Test Line",
  "totalDistance": 5.50,
  "estimatedTime": 12,
  "lineColor": "Blue",
  "createdAt": "2026-10-29T10:55:02.114",
  "updatedAt": "2026-10-29T10:55:02.114",
  "startStation": {
    "stationId": "STN-VSV",
    "stationCode": "VSV",
    "latitude": 19.1354,
    "longitude": 72.8209,
    "address": "Versova - JP Road, Andheri West",
    "openedDate": "2015-04-12",
    "lineColor": "Blue",
    "isActive": true
  },
  "endStation": {
    "stationId": "STN-GHP",
    "stationCode": "GHP",
    "latitude": 19.088,
    "longitude": 72.9096,
    "address": "Ghatkopar - Ghatkopar West, Station Road Junction",
    "openedDate": "2014-09-03",
    "lineColor": "Blue",
    "isActive": true
  },
  "stationIds": ["STN-VSV", "STN-AND", "STN-GHP"]
}
```

| Field | Where it comes from |
|---|---|
| `routeId` | Generated by the controller. |
| `routeName` | Request. |
| `totalDistance` | Request, serialized as a decimal. |
| `estimatedTime` | Request. |
| `lineColor` | Request. |
| `createdAt` | Set by the controller on insert. |
| `updatedAt` | Equal to `createdAt` right after insert. |
| `startStation` | Full nested Station object, loaded via the foreign key. |
| `endStation` | Full nested Station object. |
| `stationIds` | Rebuilt from `"RouteStations"` sorted by sequence. |

### Update — request

```http
PUT /api/routes/RT-7a1c9f0e-5b62-4c8d-9a3f-2d1e6b7c8f90
```

```json
{
  "routeName": "Test Line Extended",
  "totalDistance": 6.20,
  "estimatedTime": 15,
  "lineColor": "Aqua",
  "startStationId": "STN-VSV",
  "endStationId": "STN-GHP",
  "stationIds": ["STN-VSV", "STN-WEH", "STN-GHP"]
}
```

Result: route scalars changed, `createdAt` unchanged, `updatedAt` bumped, old three stop rows deleted, three new rows inserted with new `RS-...` IDs, and `stationIds` becomes `["STN-VSV","STN-WEH","STN-GHP"]`.

### List — response

```http
GET /api/routes
Authorization: Basic cHJpeWE6UGFzc3cwcmQh
```

```json
[
  {
    "routeId": "RT-L1",
    "routeName": "Mumbai Metro Line 1 - Blue (Versova to Ghatkopar)",
    "totalDistance": 11.10,
    "estimatedTime": 21,
    "lineColor": "Blue",
    "createdAt": "2026-10-15T10:00:00",
    "updatedAt": "2026-10-15T10:00:00",
    "startStation": { "stationId": "STN-VSV", "stationCode": "VSV", "...": "..." },
    "endStation": { "stationId": "STN-GHP", "stationCode": "GHP", "...": "..." },
    "stationIds": ["STN-AND", "STN-WEH", "STN-APR", "STN-MRL", "STN-SKN"]
  }
]
```

Look carefully at `stationIds` for the **seeded** route. It has only the five middle stops. Versova and Ghatkopar are missing.

That is not a response bug. `DataSeeder.route()` writes stop rows only for the intermediate stations, starting the sequence at 2:

```java
int seq = 2;
for (String stationId : intermediateIds) {
    RouteStation rs = new RouteStation();
    rs.setRouteStationId("RS-" + UUID.randomUUID());
    rs.setRoute(route);
    rs.setStation(stationRepository.findById(stationId).orElseThrow());
    rs.setSequenceNumber(seq++);
    routeStationRepository.save(rs);
}
```

So the seed data assumes "start is position 1 and end is the last position" without actually inserting them. The result is that a seeded route has 5 stop rows, with sequences 2..6, and no rows for positions 1 and 7.

A route created through the API is different: `saveRouteStations` starts at 1 and inserts every ID you give it, including the ends. Know this difference before a viva, because an evaluator who calls `GET /api/routes/RT-L1` will see it.

### Delete — response

`204 No Content`, empty body, after the stop rows are removed.

---

## 11. Error table

| Situation | Status | Body |
|---|---|---|
| No or bad Basic auth | 401 | empty + `WWW-Authenticate: Basic` |
| Non-admin calls POST/PUT/DELETE | 403 | `{"error":"You do not have permission to access this resource"}` |
| `routeName` blank | 400 | `{"routeName":"Route name is required"}` |
| `startStationId` or `endStationId` missing | 400 | `{"error":"Start and end stations are required"}` |
| `startStationId` does not exist | 400 | `{"error":"Start station not found"}` |
| `endStationId` does not exist | 400 | `{"error":"End station not found"}` |
| A `stationIds` entry does not exist | 400 | `{"error":"Station not found: STN-XXX"}` |
| Same station twice in `stationIds` | 500 | unexpected error, unique constraint `uq_route_station` |
| Unknown route ID on GET/PUT/DELETE | 400 | `{"error":"Route not found"}` |
| Delete a route used by a schedule | 500 | unexpected error, `FK_Schedule_Route` |
| `totalDistance` is not a number | 500 | unexpected error from Jackson |
| `stationIds` is not an array | 500 | unexpected error from Jackson |

### Why duplicates give 500

`saveRouteStations` inserts each stop without checking whether it already appeared. The database has:

```sql
ALTER TABLE "RouteStations" ADD CONSTRAINT "uq_route_station" UNIQUE ("RouteID", "StationID");
```

The second insert with the same station violates it. Hibernate notices at flush time, the transaction rolls back — including the route insert — and the catch-all handler returns 500. A friendly design would check for duplicates in the list first and return 400.

### Why missing stations give 400

`copyRouteFields` and `saveRouteStations` both throw `IllegalArgumentException`. `GlobalExceptionHandler` maps that class to 400 with `{"error": message}`.

### Why a missing route gives 400 and not 404

Same project convention as Station and Train: the controllers throw `IllegalArgumentException`, not `ResourceNotFoundException`. The handler maps the first to 400 and the second to 404. Only `IllegalArgumentException` is used here.

### Why deleting a route can give 500

`RouteController.deleteRoute` removes the stop rows, so `FK_RouteStations_Route` is satisfied. But a `Schedule` row may still point at the route:

```sql
CONSTRAINT "FK_Schedule_Route" FOREIGN KEY ("RouteID") REFERENCES "Route" ("RouteID")
```

Every seeded route has schedules (`RT-L1` has `SCH-001`, `SCH-002`, `SCH-003`), so deleting `RT-L1` fails. This is intentional: timetables are records, not disposable children.

---

## 12. Relationships to other groups

| Group | Relation | Foreign key | Notes |
|---|---|---|---|
| Station | `startStation` -> `"StartStationID"` | `FK_Route_StartStation` | Required by the controller. |
| Station | `endStation` -> `"EndStationID"` | `FK_Route_EndStation` | Required by the controller. |
| RouteStation | one route has many stops | `FK_RouteStations_Route` | Managed by this controller. |
| Schedule | many schedules use one route | `FK_Schedule_Route` | Stops deletion; managed by ScheduleController. |
| Fare / Ticket | none | none | They reference stations directly, not routes. |

### The shape of the network

```
Route 1 --- N RouteStation N --- 1 Station
  ^
  |
  N
Schedule N --- 1 Train
```

- A route has many stops.
- Each stop points at one station.
- A station can appear on many routes.
- A schedule pairs one train with one route.

Fares and tickets deliberately skip routes. They are priced and sold between two **stations**, because a passenger cares about "Versova to Ghatkopar", not about internal route IDs. That keeps pricing stable even if routes are reorganised.

### Why the response nests full stations

`startStation` and `endStation` are `@ManyToOne` objects. Jackson sees the getters and writes the whole station object. That makes the route response self-contained: a UI can render "Versova -> Ghatkopar" without extra calls.

The cost is a larger payload and possible deep nesting when other entities embed a route. For example, a `Schedule` response contains a `route`, which contains two stations, each with all fields.

### No circular reference here

`Station` has no back-reference to `Route`, and `Route` does not hold a list of `RouteStation` objects. So JSON cannot loop infinitely.

If someone later adds `List<RouteStation> stops` to `Route` and `Route route` is already on `RouteStation`, you would get infinite recursion. The usual fix is `@JsonIgnore` on one side or a DTO. The current design avoids the problem by keeping the collection out of the entity.

---

## 13. Seed data

`DataSeeder.seedRoutes()` creates three routes:

```java
route("RT-L1", "Mumbai Metro Line 1 - Blue (Versova to Ghatkopar)",
        new BigDecimal("11.10"), 21, "Blue", "STN-VSV", "STN-GHP",
        "STN-AND", "STN-WEH", "STN-APR", "STN-MRL", "STN-SKN");

route("RT-L2", "Mumbai Metro Line 3 - Aqua (Aarey to CSMT)",
        new BigDecimal("18.50"), 33, "Aqua", "STN-ARY", "STN-CSM",
        "STN-SEP", "STN-MRL", "STN-BKC", "STN-DDR", "STN-MCU");

route("RT-L3", "Mumbai Metro Line 7 - Red (Dahisar East to Andheri East)",
        new BigDecimal("12.30"), 22, "Red", "STN-DHE", "STN-AEE",
        "STN-DVP", "STN-POI", "STN-GDU");
```

| RouteID | Name | Distance | Minutes | Colour | Start | End | Seeded stop rows |
|---|---|---|---|---|---|---|---|
| `RT-L1` | Line 1 — Blue | 11.10 | 21 | Blue | `STN-VSV` | `STN-GHP` | 5 (seq 2-6) |
| `RT-L2` | Line 3 — Aqua | 18.50 | 33 | Aqua | `STN-ARY` | `STN-CSM` | 5 (seq 2-6) |
| `RT-L3` | Line 7 — Red | 12.30 | 22 | Red | `STN-DHE` | `STN-AEE` | 3 (seq 2-4) |

The helper:

```java
private void route(String id, String name, BigDecimal distance, int minutes, String color,
                   String startId, String endId, String... intermediateIds) {
    Route route = new Route();
    route.setRouteId(id);
    route.setRouteName(name);
    route.setTotalDistance(distance);
    route.setEstimatedTime(minutes);
    route.setLineColor(color);
    route.setCreatedAt(LocalDateTime.now().minusWeeks(2));
    route.setUpdatedAt(LocalDateTime.now().minusWeeks(2));
    route.setStartStation(stationRepository.findById(startId).orElseThrow());
    route.setEndStation(stationRepository.findById(endId).orElseThrow());
    routeRepository.save(route);

    int seq = 2;
    for (String stationId : intermediateIds) {
        RouteStation rs = new RouteStation();
        rs.setRouteStationId("RS-" + UUID.randomUUID());
        rs.setRoute(route);
        rs.setStation(stationRepository.findById(stationId).orElseThrow());
        rs.setSequenceNumber(seq++);
        routeStationRepository.save(rs);
    }
}
```

Two things to remember:

1. **Only intermediates are inserted.** Start and end are columns on the route but not rows in `"RouteStations"`, and the sequence starts at 2.
2. **`Marol Naka` (`STN-MRL`) appears on both `RT-L1` and `RT-L2`.** That is allowed: the unique rule is per route, on the pair `(RouteID, StationID)`.

The seeder is skipped when `stationRepository.count() > 0`, so restarting the app does not duplicate routes.

---

## 14. Swagger test steps

Open `http://localhost:8080/swagger-ui.html`.

1. **Authorize** as `admin` / `admin123`.
2. Open **route-controller**.
3. `POST /api/routes` -> **Try it out** -> paste:

```json
{
  "routeName": "Test Line",
  "totalDistance": 5.50,
  "estimatedTime": 12,
  "lineColor": "Blue",
  "startStationId": "STN-VSV",
  "endStationId": "STN-GHP",
  "stationIds": ["STN-VSV", "STN-AND", "STN-GHP"]
}
```

4. **Execute** -> expect `201`, a `routeId` beginning `RT-`, nested `startStation` / `endStation`, and `stationIds` with three entries.
5. Copy the `routeId`.
6. `GET /api/routes/{id}` -> **Execute** -> expect `200` and the same data.
7. `GET /api/routes` -> **Execute** -> expect 4 routes: `RT-L1`, `RT-L2`, `RT-L3`, plus yours.
8. `GET /api/routes/RT-L1` -> look at `stationIds`. It contains only the 5 intermediate stations. This is the seeded-data quirk from section 13.
9. `PUT /api/routes/{id}` -> send a new list of two IDs -> **Execute** -> expect `200` and exactly two `stationIds`.
10. `DELETE /api/routes/{id}` -> **Execute** -> expect `204`.
11. `GET /api/routes/{id}` again -> expect `400` `{"error":"Route not found"}`.
12. `POST /api/routes` with `"routeName": ""` -> expect `400` `{"routeName":"Route name is required"}`.
13. `POST /api/routes` without `startStationId` -> expect `400` `{"error":"Start and end stations are required"}`.
14. `POST /api/routes` with `"startStationId": "STN-NOPE"` -> expect `400` `{"error":"Start station not found"}`.
15. `POST /api/routes` with `"stationIds": ["STN-VSV","STN-VSV"]` -> expect `500`, because of the unique constraint.
16. `DELETE /api/routes/RT-L1` -> expect `500`, because schedules reference it.
17. Re-authorize as `priya` / `Passw0rd!` -> `GET /api/routes` works, `POST /api/routes` gives `403`.

`curl` for step 3:

```bash
curl -u admin:admin123 -X POST http://localhost:8080/api/routes \
  -H "Content-Type: application/json" \
  -d '{"routeName":"Test Line","totalDistance":5.50,"estimatedTime":12,"lineColor":"Blue","startStationId":"STN-VSV","endStationId":"STN-GHP","stationIds":["STN-VSV","STN-AND","STN-GHP"]}'
```

---

## 15. How to explain this in 2 minutes

> "A Route is a named metro line. It has an ID generated as `RT-` plus a UUID, a name, a distance stored as `BigDecimal` and `NUMERIC(10,2)`, an estimated time in minutes, a colour, and two foreign keys to Station for the first and last stop.
>
> The route does not store the full stop list in a column, because a list has no fixed length. The ordered stops live in a separate `RouteStations` table, one row per stop with a sequence number. So creating a route is two writes: insert the route, then insert the stops. That is why `RouteController` injects three repositories — Route, RouteStation, and Station — instead of one.
>
> There is a clever trick in the entity. The JSON needs to accept `startStationId` as a plain string, but the JPA mapping needs a `Station` object. So the entity has both: a `@ManyToOne` `Station startStation` marked `@JsonProperty(READ_ONLY)` so it only appears in responses, and a `@Transient` `String startStationId` marked `WRITE_ONLY` so it only appears in requests. `@Transient` is essential; without it Hibernate would look for a `startStationId` column and startup validation would fail.
>
> The third carrier field is `stationIds`, also `@Transient`. It is the ordered list of station IDs. It is the only one that goes both ways: the client sends it, and the controller fills it when building responses by reading `RouteStations` ordered by `SequenceNumber`.
>
> POST checks `@Valid` on the name, copies the scalar fields, demands both station IDs, loads both stations, sets timestamps, saves the route, then loops over `stationIds` and inserts one `RouteStation` per ID with sequence 1, 2, 3. If the list is missing, it falls back to just start and end. Then it re-reads the route so the response shows exactly what was stored.
>
> PUT loads the route, copies fields, bumps `updatedAt`, saves, deletes all old stop rows, and re-inserts the new list. It is replace-the-whole-list, not merge. If you omit `stationIds`, the route shrinks to two stops, which is the one behaviour to watch out for.
>
> GET reads the route and rebuilds `stationIds`. DELETE removes the stop rows first, because of the foreign key, then the route. Postgres blocks the delete if a Schedule still points at the route, so `RT-L1` cannot be deleted in the demo.
>
> Two honest limitations: a duplicate station in the list gives 500 from the unique constraint instead of a friendly 400, and reading a route causes an N+1 query pattern because `station` is lazy and `getStation()` runs once per stop. The seed data also inserts only intermediate stops, starting at sequence 2, so `GET /api/routes/RT-L1` shows five IDs, not seven."

---

## 16. Beginner Q&A

**Q1. Why is there a separate `RouteStations` table?**
Because a route has a variable number of stops. SQL columns are fixed, so you cannot store a list in one column. One row per stop is the standard relational solution.

**Q2. Then why store start and end on the route too?**
It makes "where does this line start?" a single join instead of an aggregate query. It is deliberate duplication for convenience. A fully normalised model would derive them from sequence 1 and the maximum sequence.

**Q3. What does `@ManyToOne` mean here?**
Many route rows can point to the same station row. The foreign key lives on the route side, in `StartStationID` and `EndStationID`.

**Q4. Why `FetchType.LAZY`?**
So loading a route does not automatically load two full station rows. Hibernate puts a proxy in the field and queries only when `getStartStation()` is called. It saves work when you only need the route name.

**Q5. What is `@JoinColumn` doing?**
It names the foreign key column and the target column. `name = "\"StartStationID\""` is the column in `"Route"`, and `referencedColumnName = "\"StationID\""` is the column in `"Stations"`.

**Q6. What is `@Transient`?**
"JPA, ignore this field." It means no column, no mapping, no schema check. It is used for fields that carry data between JSON and Java but are not stored.

**Q7. Why is `startStation` marked READ_ONLY?**
So clients send a simple `startStationId` string, not a full nested object. Responses still include the full station, which is convenient for the UI.

**Q8. Why is `startStationId` marked WRITE_ONLY?**
So it appears in requests but not responses. Responses already have the full `startStation` object; repeating a bare ID would be redundant.

**Q9. Why is `stationIds` not WRITE_ONLY?**
Because responses need it too. The controller fills it from the stop table so clients can see the order without calling another endpoint.

**Q10. Why does `stationIds` show as `null` inside a Schedule response?**
Because `stationIds` is only filled by `RouteController`. When a `Route` is serialized as part of another entity, nobody set the field, so Jackson writes `null`.

**Q11. Why does `@Transient` not stop Jackson from serializing `stationIds`?**
`@Transient` is a JPA annotation; Jackson does not read it. Jackson decides based on getters and `@JsonProperty`. So a transient field with a public getter is still serialized.

**Q12. Why is the ID generated in the controller?**
Readable prefix plus UUID: `RT-...`. No database sequence is needed. The trade-off is that `save` may use `merge`, which can run a SELECT before the INSERT.

**Q13. Why does create re-read the route instead of returning the object it just saved?**
Because the request may not contain `stationIds`, in which case the stored list differs from the request. `loadRoute` guarantees the response matches the database.

**Q14. Why does update delete all stops and re-insert them?**
It is much simpler than comparing old and new lists. The cost is new `RouteStationID` values on every update, which is harmless because nothing references them.

**Q15. What happens if I send no `stationIds` on update?**
`saveRouteStations` falls back to `[startStationId, endStationId]`, so all previous middle stops disappear. This is replace semantics, not a partial patch.

**Q16. Why does `GET /api/routes/RT-L1` show only five station IDs?**
The seeder inserts rows only for intermediate stations and starts numbering at 2. Start and end exist only as columns on the route. Routes created through the API are unaffected because `saveRouteStations` inserts every ID from sequence 1.

**Q17. Can the same station appear twice on one route?**
Not in the database. `uq_route_station` is `UNIQUE ("RouteID","StationID")`. Sending a duplicate in `stationIds` causes a 500 at flush time.

**Q18. Can the same station appear on two different routes?**
Yes. The unique rule is the pair, not the station alone. `STN-MRL` is on both `RT-L1` and `RT-L2` in the seed data.

**Q19. Why can't I delete `RT-L1`?**
`Schedule` rows `SCH-001`, `SCH-002`, and `SCH-003` reference it through `FK_Schedule_Route`. The controller deletes stops but not schedules, so PostgreSQL blocks the delete and the API returns 500.

**Q20. What is the N+1 problem in `loadStationIds`?**
One query fetches all stop rows, then `getStation()` triggers one more query per stop because the relation is lazy. Seven stops mean eight queries. A `join fetch` query would fix it; the project accepts the cost for demo scale.

**Q21. Why does lazy loading not crash outside the transaction?**
Because `spring.jpa.open-in-view=true` keeps the Hibernate session open during response serialization. Without it, calling `getStation()` after the transaction ended would throw `LazyInitializationException`.

**Q22. Why `BigDecimal` for distance?**
Because `NUMERIC(10,2)` is an exact decimal and `BigDecimal` represents it exactly. `double` can produce rounding artefacts, which are unacceptable for values people read as measurements or money.

**Q23. Why is `estimatedTime` an `Integer` and not a `Duration`?**
The column is a plain `INTEGER` holding minutes. `Duration` would need a different mapping. Simple integer minutes is enough for this schema.

**Q24. Who can create a route?**
Only `ADMIN`, via `@PreAuthorize("hasRole('ADMIN')")`. Reads are open to any authenticated user.

**Q25. Is there a `RouteStationController`?**
No. Stop rows are a child of Route and are managed only through `/api/routes`. That keeps the API surface small and keeps the list and its parent consistent in one transaction.

---

## 17. Glossary

| Term | Meaning in this file |
|---|---|
| **Bean** | Object created and managed by Spring. |
| **BCrypt** | One-way password hashing used at login. |
| **`BigDecimal`** | Java class for exact decimal arithmetic, mapped to `NUMERIC`. |
| **Cascade** | Automatically applying an operation (such as delete) to child rows. Not used here. |
| **Column** | One field of a table. |
| **Controller** | Class mapping URLs to Java methods. |
| **Derived query** | Repository method whose name Spring turns into SQL. |
| **DTO** | Class used only to carry data. This project reuses entities instead. |
| **Entity** | Class mapped to a table. |
| **FetchType.LAZY** | Load the relation only when read. |
| **FK** | Foreign key. |
| **Flyway** | Applies numbered SQL migrations. |
| **Foreign key** | Column that must exist as a primary key elsewhere. |
| **Hibernate** | JPA implementation that writes SQL. |
| **HTTP status** | Numeric result of a request. |
| **Jackson** | Java <-> JSON converter. |
| **JPA** | Java standard for object-table mapping. |
| **JSON** | Text data format with objects and arrays. |
| **`@JoinColumn`** | Declares which column is the foreign key. |
| **`@ManyToOne`** | Many rows here point to one row there. |
| **merge** | JPA operation that copies state onto a managed entity; may SELECT first. |
| **Migration** | One versioned SQL file. |
| **N+1 problem** | One list query plus one query per item. |
| **Normalisation** | Organising tables to avoid duplicated or list-like columns. |
| **Open Session in View** | Keeping the Hibernate session open during response rendering. |
| **`Optional`** | Container that may hold a value or nothing. |
| **ORM** | Object-relational mapping. |
| **Path variable** | Value taken from the URL path. |
| **Primary key** | Unique row identifier. |
| **Proxy** | Placeholder object for a not-yet-loaded relation. |
| **Repository** | Interface providing database methods. |
| **`ResponseEntity`** | Object carrying status and body. |
| **Seeder** | Class that inserts demo data at startup. |
| **Serialization** | Java object -> JSON. |
| **Swagger UI** | Generated page for testing endpoints. |
| **Transaction** | All-or-nothing group of database operations. |
| **`@Transient`** | "JPA, do not persist this field." |
| **Unique constraint** | Rule that a combination of values may not repeat. |
| **UUID** | 128-bit random identifier. |

---

## 18. One-line summary

`Route` is the line row — name, distance, minutes, colour, and two station foreign keys — while its ordered stops live in `RouteStations`; the controller injects three repositories, writes both tables in one transaction, rebuilds `stationIds` on every read, and relies on foreign keys and a unique constraint instead of cascade deletes.

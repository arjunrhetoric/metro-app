# 06 — RouteStation Group

**In one sentence:** a RouteStation is one stop on one route, with a position number, and it is the join table that turns a route into an ordered sequence of stations.

**Files in this group**

| File | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/RouteStation.java` |
| Composite-key class | **does not exist** — there is no `RouteStationId.java` in this project |
| Controller | **no dedicated controller** — managed by `src/main/java/com/metro/ticketing/controller/RouteController.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/RouteStationRepository.java` |
| Table creation | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Table key change | `src/main/resources/db/migration/V6__replace_route_station_composite_key.sql` |
| Seed data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` |

---

## 1. Real-life analogy

Imagine a **train timetable poster** on a platform.

The poster has two parts:

```
LINE: Blue Line, Versova -> Ghatkopar
+-------+----------------------------+
| 1     | Versova                    |
| 2     | Andheri                    |
| 3     | Western Express Highway    |
| 4     | Airport Road               |
| 5     | Marol Naka                 |
| 6     | Saki Naka                  |
| 7     | Ghatkopar                  |
+-------+----------------------------+
```

The top line is the `Route` row. Each numbered line in the table is a **RouteStation** row:

- which route it belongs to,
- which station it is,
- what position it has.

Why not just write the list in the route row? Because the list can be 2 entries long or 40 entries long, and database columns are fixed. A list must become rows.

Why does each row need its own ID? Because a database table wants one column that uniquely identifies each row. The project originally used the pair `(RouteID, StationID)` for that. Later, migration `V6` replaced it with a generated single-column ID, `RouteStationID`. Both designs are legal; the project now uses the single-column style.

The important business rule is the **unique pair**: one route cannot visit the same station twice. Two different routes may both visit Andheri, and that is fine.

---

## 2. Words you need

| Word | One-sentence meaning |
|---|---|
| Spring Boot | Framework that starts the server and wires objects together. |
| Annotation | An `@Something` label giving the framework instructions. |
| Bean | An object managed by Spring. |
| Dependency injection | Spring passes required objects into a constructor. |
| Controller | Class that receives HTTP requests. |
| Endpoint | One URL plus one HTTP method. |
| HTTP method | GET read, POST create, PUT update, DELETE remove. |
| JSON | Text data format with `{}` objects and `[]` arrays. |
| Jackson | Library converting Java <-> JSON. |
| Serialization | Java object -> JSON. |
| Deserialization | JSON -> Java object. |
| JPA | Java standard for mapping classes to tables. |
| Hibernate | Library implementing JPA and writing SQL. |
| Entity | Class mapped to a table. |
| ORM | Treating tables as classes and rows as objects. |
| Table | Grid of rows and columns. |
| Row / record | One entry in a table. |
| Column | One named field of a table. |
| Primary key | Column(s) that uniquely identify a row. |
| Composite key | A primary key made of two or more columns together. |
| Surrogate key | A generated column whose only job is to be the primary key. |
| Foreign key | Column that must match a primary key in another table. |
| Unique constraint | Rule that a combination of column values may not repeat. |
| Join table | Table that links two other tables and adds information about the link. |
| `@ManyToOne` | Many rows here point to one row there. |
| `@JoinColumn` | Declares which column holds the foreign key. |
| `FetchType.LAZY` | Do not load the related row until it is read. |
| Proxy | Placeholder for a relation that is not loaded yet. |
| `@Transient` | "JPA, ignore this field." |
| `@JsonProperty(access = ...)` | Jackson rule for input-only or output-only fields. |
| Repository | Interface giving database methods without SQL. |
| `JpaRepository` | Spring Data interface with ready-made CRUD methods. |
| Derived query method | Method whose name Spring translates into SQL. |
| Property path | The `route_RouteId` part of a method name, meaning "field `route`, then its `routeId`". |
| Transaction | All-or-nothing group of database changes. |
| `@Transactional` | Wraps a method in one transaction. |
| Bean Validation | `@NotBlank`-style input checks. |
| `@Valid` | Makes Spring enforce those checks on the body. |
| `ResponseEntity` | Object carrying status code and body. |
| HTTP status | Number describing the result: 200, 201, 204, 400, 401, 403, 500. |
| `Optional` | Container that may hold a value or nothing. |
| UUID | Long random string used in generated IDs. |
| `ArrayList` | Growable Java list. |
| N+1 query problem | One list query plus one extra query per item. |
| Flyway | Tool that applies numbered SQL migrations. |
| Migration | One numbered SQL file. |
| Seeder | Class that inserts demo data at startup. |
| Swagger UI | Web page for testing endpoints. |
| Backfill | Filling existing rows with a value after adding a new column. |

---

## 3. Files in this group

```
src/main/java/com/metro/ticketing/
├── entity/RouteStation.java                  <- one stop row
├── repository/RouteStationRepository.java    <- 2 custom methods
└── controller/RouteController.java           <- the only way in or out

src/main/resources/db/migration/
├── V1__create_metro_ticketing_schema.sql    <- creates the table with a composite PK
└── V6__replace_route_station_composite_key.sql <- swaps to a single PK

src/main/java/com/metro/ticketing/config/
└── DataSeeder.java                          <- inserts 13 demo stop rows
```

### About `RouteStationId.java`

You may expect a file called `RouteStationId.java`. **It does not exist.** That name is only used when a project maps a composite key with `@EmbeddedId`:

```java
// This is what a composite-key project WOULD look like. NOT used here.
@Embeddable
public class RouteStationId implements Serializable {
    private String route;
    private String station;
}
```

This project does **not** do that. Migration `V6` removed the composite key and gave each stop row its own `RouteStationID` string. So the entity has a plain `@Id String routeStationId`, and there is no key class to write.

If an evaluator asks "where is the composite key class?", the correct answer is: "We used to have a composite key in `V1`, but `V6` replaced it with a surrogate `RouteStationID`; the uniqueness of `(RouteID, StationID)` is now enforced by a separate unique constraint, so no `@EmbeddedId` class is needed."

---

## 4. Database table

### Step 1 — the original table in `V1`

```sql
CREATE TABLE IF NOT EXISTS "RouteStations" (
    "RouteID" VARCHAR(50) NOT NULL,
    "StationID" VARCHAR(50) NOT NULL,
    "SequenceNumber" INTEGER NOT NULL,
    PRIMARY KEY ("RouteID", "StationID"),
    CONSTRAINT "FK_RouteStations_Route" FOREIGN KEY ("RouteID") REFERENCES "Route" ("RouteID"),
    CONSTRAINT "FK_RouteStations_Station" FOREIGN KEY ("StationID") REFERENCES "Stations" ("StationID")
);
```

Here the primary key is the **pair**. One station cannot appear twice on the same route, because the pair would repeat and a primary key must be unique. But the same station can appear on many routes, because the pair differs by `RouteID`.

### Step 2 — the change in `V6`

```sql
-- Replace the composite (RouteID, StationID) primary key with a single generated ID.
-- The unique constraint keeps the original rule: one station per route.
ALTER TABLE "RouteStations" ADD COLUMN IF NOT EXISTS "RouteStationID" VARCHAR(50);

UPDATE "RouteStations"
SET "RouteStationID" = 'RS-' || "RouteID" || '-' || "StationID"
WHERE "RouteStationID" IS NULL;

ALTER TABLE "RouteStations" ALTER COLUMN "RouteStationID" SET NOT NULL;

ALTER TABLE "RouteStations" DROP CONSTRAINT IF EXISTS "RouteStations_pkey";
ALTER TABLE "RouteStations" ADD CONSTRAINT "RouteStations_pkey" PRIMARY KEY ("RouteStationID");

ALTER TABLE "RouteStations" DROP CONSTRAINT IF EXISTS "uq_route_station";
ALTER TABLE "RouteStations" ADD CONSTRAINT "uq_route_station" UNIQUE ("RouteID", "StationID");
```

Line by line:

1. `ADD COLUMN IF NOT EXISTS` — add the new column, but do not crash if it is already there.
2. `UPDATE ... SET "RouteStationID" = 'RS-' || "RouteID" || '-' || "StationID"` — **backfill** every existing row with a readable value, e.g. `RS-RT-L1-STN-AND`. `||` is SQL string concatenation.
3. `SET NOT NULL` — after every row has a value, make the column mandatory.
4. `DROP CONSTRAINT "RouteStations_pkey"` — remove the old composite primary key.
5. `ADD CONSTRAINT "RouteStations_pkey" PRIMARY KEY ("RouteStationID")` — make the single column the new primary key.
6. `ADD CONSTRAINT "uq_route_station" UNIQUE ("RouteID", "StationID")` — keep the old business rule, now as a unique constraint instead of a primary key.

### Final shape

| Column | SQL type | Key | Meaning | Empty allowed? |
|---|---|---|---|---|
| `RouteStationID` | `VARCHAR(50)` | Primary key | Unique ID of this stop row, generated as `RS-<uuid>`. | No |
| `RouteID` | `VARCHAR(50)` | Foreign key -> `"Route"` | Which route this stop belongs to. | No |
| `StationID` | `VARCHAR(50)` | Foreign key -> `"Stations"` | Which station this stop is. | No |
| `SequenceNumber` | `INTEGER` | — | Position on the route: 1, 2, 3, ... | No |

Plus:

```sql
CONSTRAINT "RouteStations_pkey" PRIMARY KEY ("RouteStationID")
CONSTRAINT "uq_route_station" UNIQUE ("RouteID", "StationID")
CONSTRAINT "FK_RouteStations_Route" FOREIGN KEY ("RouteID") REFERENCES "Route" ("RouteID")
CONSTRAINT "FK_RouteStations_Station" FOREIGN KEY ("StationID") REFERENCES "Stations" ("StationID")
```

### Why replace a perfectly good composite key?

| Composite key `(RouteID, StationID)` | Surrogate key `RouteStationID` |
|---|---|
| Natural: the pair really is unique. | Simple: one column, always a string. |
| Foreign keys pointing at it would need both columns. | A future table can reference one stop with one column. |
| Editing "which station is this stop" means deleting and re-inserting. | You could update `StationID` in place. |
| Slightly less storage. | Slightly more storage plus one index. |

Nothing in the current code references a stop row by ID from another table, so the change is mostly forward-looking. The important part is that the business rule was **preserved** by `uq_route_station`.

### There is no unique constraint on `SequenceNumber`

This is surprising but true. The database allows two stops on the same route to both have sequence `5`. The controller never creates such a state, because `saveRouteStations` assigns `1, 2, 3, ...` from a single counter. But the schema does not forbid it.

If you wanted to enforce it, you would add:

```sql
ALTER TABLE "RouteStations" ADD CONSTRAINT "uq_route_sequence" UNIQUE ("RouteID", "SequenceNumber");
```

The project does not, so an evaluator who likes strict schemas may raise this. Answer honestly: uniqueness of order is maintained by the application, not the database.

### Why `SequenceNumber` can have gaps

Nothing says the numbers must be contiguous. The demo seeder starts at `2` and never inserts a `1`, so the seeded rows have sequences `2,3,4,5,6`. Gap-free numbering is a controller convention, not a database rule.

Sorting by `SequenceNumber` still gives the right order. Gaps only matter if code does arithmetic like "position equals index", which this project does not.

---

## 5. Entity line by line

Full file with a comment on every annotation and field:

```java
package com.metro.ticketing.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // hide Hibernate internals in JSON
import com.fasterxml.jackson.annotation.JsonProperty;        // input-only / output-only
import jakarta.persistence.*;                                // all JPA annotations

/**
 * One stop on a route, with its order number.
 *
 * A generated RouteStationID is the primary key, while the unique constraint
 * on (RouteID, StationID) ensures the same station cannot appear twice on a route.
 */
@Entity                                                      // 1. this class is a table
@Table(name = "\"RouteStations\"")                           // 2. exact table name
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
                                                             // 3. drop proxy noise from JSON
public class RouteStation {

    @Id                                                      // 4. primary key
    @Column(name = "\"RouteStationID\"", length = 50)        // 5. column "RouteStationID"
    private String routeStationId;                           // 6. "RS-<uuid>"

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)    // 7. output only in JSON
    @ManyToOne(fetch = FetchType.LAZY)                       // 8. many stops -> one route
    @JoinColumn(name = "\"RouteID\"")                        // 9. FK column is "RouteID"
    private Route route;                                     // 10. the parent route object

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)    // 11. output only in JSON
    @ManyToOne(fetch = FetchType.LAZY)                       // 12. many stops -> one station
    @JoinColumn(name = "\"StationID\"")                      // 13. FK column is "StationID"
    private Station station;                                 // 14. the station object

    @Column(name = "\"SequenceNumber\"", nullable = false)   // 15. NOT NULL column
    private Integer sequenceNumber;                          // 16. position: 1, 2, 3, ...

    public String getRouteStationId() {                      // JSON name: "routeStationId"
        return routeStationId;
    }

    public void setRouteStationId(String routeStationId) {
        this.routeStationId = routeStationId;
    }

    public Route getRoute() {                                // JSON name: "route"
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Station getStation() {                            // JSON name: "station"
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Integer getSequenceNumber() {                     // JSON name: "sequenceNumber"
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
```

### Annotation-by-annotation

| Annotation | Meaning |
|---|---|
| `@Entity` | "Hibernate, this class is a table." |
| `@Table(name = "\"RouteStations\"")` | "The table is named exactly `RouteStations`." |
| `@JsonIgnoreProperties(...)` | "Ignore Hibernate's internal proxy fields when writing JSON." |
| `@Id` | "This field is the primary key." |
| `@Column(name = ...)` | "The database column has this exact name." |
| `@Column(nullable = false)` | "Add `NOT NULL`." |
| `@ManyToOne` | "Many stop rows can point at the same route" and, separately, "many stop rows can point at the same station." |
| `@JoinColumn(name = "\"RouteID\"")` | "The foreign key column is `RouteID`." Without `referencedColumnName`, JPA assumes the target's primary key, which is correct here. |
| `FetchType.LAZY` | "Load the route/station only when someone calls the getter." |
| `@JsonProperty(access = READ_ONLY)` | "Write it in responses, ignore it in requests." |

### Why both relations are READ_ONLY

If these were writable, a client could POST a whole `RouteStation` with a nested `route` object and a nested `station` object, and Jackson would try to build those from JSON. That invites inconsistent data and confusing validation errors.

Instead, the controller builds the row:

```java
routeStation.setRoute(route);       // the Route already loaded/saved
routeStation.setStation(station);   // the Station loaded by ID
```

There is no endpoint that accepts a `RouteStation` body anyway. The READ_ONLY marker documents the intent: these fields are output, not input.

### Why `@JoinColumn` has no `referencedColumnName`

For `Route`, the target primary key is `"RouteID"`, and the column name is also `"RouteID"`. For `Station`, the target primary key is `"StationID"`, and the column name is also `"StationID"`. So JPA's default ("point at the target's `@Id` column") is already correct. `Route` needed `referencedColumnName` explicitly because it has two joins to the same table and the names are clearer when spelled out.

### A JSON trap worth knowing

`RouteStation` has no back-reference list on `Route`, and `Station` has no list of route stations. So there is no cycle:

```
RouteStation -> Route -> startStation -> Station
RouteStation -> station -> Station
```

If `Route` also had `List<RouteStation> stops`, and each `RouteStation` has a `Route`, Jackson would recurse forever. The project avoids the cycle by not mapping that collection. If someone adds it later, one side must get `@JsonIgnore` or the API must return a DTO.

### Why the entity has a Javadoc comment

The comment is the only place that records the key migration decision. It says: a generated ID is the primary key, and the unique constraint keeps the "one station per route" rule. If an evaluator reads only the entity, they still get the design rationale.

### Why `sequenceNumber` is `Integer`, not `int`

The column is `NOT NULL`, so `int` would work here. `Integer` is used for consistency with the other entities and to keep the option of a null "not assigned yet" state in memory before the controller sets the number. Since the column forbids null, saving an entity with a null sequence fails in the database.

---

## 6. Repository line by line

Full file:

```java
package com.metro.ticketing.repository;

import com.metro.ticketing.entity.RouteStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository                                                    // 1. Spring bean
public interface RouteStationRepository extends JpaRepository<RouteStation, String> {
                                                               // 2. entity, then ID type

    List<RouteStation> findByRoute_RouteIdOrderBySequenceNumberAsc(String routeId);
                                                               // 3. read ordered stops

    void deleteByRoute_RouteId(String routeId);                 // 4. delete all stops of a route
}
```

No class implements this interface. Spring Data JPA reads the method names and generates the implementation.

### Method 1 — `findByRoute_RouteIdOrderBySequenceNumberAsc`

Break the name into parts:

| Part | Meaning |
|---|---|
| `findBy` | It is a SELECT. |
| `Route_RouteId` | Navigate the entity's `route` property, then its `routeId` property. The underscore separates the two. |
| `OrderBy` | Add sorting. |
| `SequenceNumber` | Sort by the `sequenceNumber` field. |
| `Asc` | Ascending order: 1, 2, 3, ... |

The parameter `String routeId` is bound to the `route.routeId` condition.

Generated SQL (conceptually):

```sql
SELECT rs."RouteStationID", rs."RouteID", rs."StationID", rs."SequenceNumber"
FROM "RouteStations" rs
WHERE rs."RouteID" = ?
ORDER BY rs."SequenceNumber" ASC;
```

Hibernate may also join to `"Route"` to build the `route` object, but the filter uses the `RouteID` column directly.

Why the underscore? Without it, Spring would look for a property literally called `routeRouteId`, which does not exist. Alternatives include:

```java
findByRouteRouteIdOrderBySequenceNumberAsc(String routeId);  // also valid
```

The underscore is the explicit, readable form.

Important: this returns **all** rows for the route, without a `LIMIT`. It is not paginated. For a metro line with a few dozen stops that is correct.

### Method 2 — `deleteByRoute_RouteId`

| Part | Meaning |
|---|---|
| `deleteBy` | It is a DELETE. |
| `Route_RouteId` | Match rows whose `route.routeId` equals the argument. |

There are two ways Spring Data implements a derived delete:

1. Select the matching entities, then call `EntityManager.remove` on each. This is the default for derived deletes and produces one SELECT plus N DELETEs.
2. A bulk `DELETE` statement, which requires `@Modifying @Query`.

This project uses form 1. The SQL log therefore shows:

```sql
SELECT ... FROM "RouteStations" WHERE "RouteID" = ?;
DELETE FROM "RouteStations" WHERE "RouteStationID" = ?;   -- repeated per row
```

That is fine for 3 to 7 stops. For a 10,000-row child table you would write a bulk delete.

The method returns `void`, so callers cannot tell how many rows were removed. That is acceptable here because the controller only wants a clean slate.

A derived delete **must run inside a transaction**. Both call sites (`updateRoute` and `deleteRoute`) are annotated `@Transactional`, so the requirement is satisfied. Calling it from an unannotated method would throw `TransactionRequiredException`.

### Inherited methods used elsewhere

| Method | Where used | SQL |
|---|---|---|
| `save(routeStation)` | `saveRouteStations` | `INSERT INTO "RouteStations" (...) VALUES (?,?,?,?)` |
| `findByRoute_RouteIdOrderBySequenceNumberAsc(id)` | `loadStationIds` | `SELECT ... WHERE "RouteID"=? ORDER BY "SequenceNumber" ASC` |
| `deleteByRoute_RouteId(id)` | `updateRoute`, `deleteRoute` | `SELECT` then `DELETE` per row |
| `findById`, `findAll`, `count`, `deleteById` | Not used by this project | Standard CRUD if needed |

### Why there are only two custom methods

Everything else is either "insert a row", which `JpaRepository.save` already does, or "delete by ID", which is not needed. The two custom methods capture the only two special queries the feature requires: ordered read and clear-by-parent.

---

## 7. Controller endpoints

There is **no** `RouteStationController`. That is a deliberate design choice: a stop row has no meaning on its own. It only makes sense as part of a route. Exposing `/api/route-stations` would let a client create a stop with no route, or reorder stops without touching the route.

So all access goes through `/api/routes`:

| # | Method | Full path | Who | Effect on `"RouteStations"` |
|---|---|---|---|---|
| 1 | POST | `/api/routes` | `ADMIN` | Insert one row per ID in `stationIds`, sequences 1..N. |
| 2 | PUT | `/api/routes/{id}` | `ADMIN` | Delete all old rows, insert the new list. |
| 3 | GET | `/api/routes/{id}` | authenticated | Read rows ordered by sequence, build `stationIds`. |
| 4 | GET | `/api/routes` | authenticated | Same read, repeated for every route. |
| 5 | DELETE | `/api/routes/{id}` | `ADMIN` | Delete all stop rows, then delete the route. |

Think of `RouteStation` as a **private child** of `Route`. The parent's URL is the only door.

This is a common REST pattern called a nested resource. You could also expose `/api/routes/{id}/stations`, but this project avoids an extra endpoint by embedding the list in the route JSON.

---

## 8. Controller line by line

The controller is `RouteController`. Only the route-relevant parts are reproduced here; the full file is explained in `05-ROUTE.md`.

### Fields and injection

```java
private final RouteRepository routeRepository;                       // route rows
private final RouteStationRepository routeStationRepository;         // stop rows
private final StationRepository stationRepository;                   // station lookup

public RouteController(RouteRepository routeRepository,
                       RouteStationRepository routeStationRepository,
                       StationRepository stationRepository) {
    this.routeRepository = routeRepository;                          // Spring passes the bean
    this.routeStationRepository = routeStationRepository;            // Spring passes the bean
    this.stationRepository = stationRepository;                      // Spring passes the bean
}
```

All three are interfaces. Spring supplies the generated implementations. The controller never calls `new` on a repository.

### Create — how stop rows are born

```java
@PostMapping                                                          // POST /api/routes
@PreAuthorize("hasRole('ADMIN')")                                     // admin only
@Transactional                                                        // one transaction
public ResponseEntity<Route> createRoute(@Valid @RequestBody Route request) {
    Route route = new Route();                                        // empty route
    route.setRouteId("RT-" + UUID.randomUUID());                      // "RT-..."
    copyRouteFields(request, route);                                  // scalars + two stations
    route.setCreatedAt(LocalDateTime.now());                          // creation time
    route.setUpdatedAt(LocalDateTime.now());                          // update time
    route = routeRepository.save(route);                              // INSERT route first

    saveRouteStations(route, request.getStationIds());                // INSERT stop rows
    return ResponseEntity.status(HttpStatus.CREATED).body(loadRoute(route.getRouteId()));
}                                                                     // commit both writes together
```

Why must the route be saved before the stops? Because `RouteStation.RouteID` is a foreign key to `"Route"`. Inserting a stop for a route that does not exist yet would violate `FK_RouteStations_Route`.

### The stop-writing helper

```java
private void saveRouteStations(Route route, List<String> stationIds) {
    List<String> orderedStations = stationIds;                        // the client's order
    if (orderedStations == null || orderedStations.isEmpty()) {       // no list given?
        orderedStations = List.of(                                    // build a 2-entry list
                route.getStartStation().getStationId(),               // first stop
                route.getEndStation().getStationId());                // last stop
    }

    int sequenceNumber = 1;                                           // positions start at 1
    for (String stationId : orderedStations) {                        // keep client order
        Station station = stationRepository.findById(stationId)       // must exist
                .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));

        RouteStation routeStation = new RouteStation();               // one row object
        routeStation.setRouteStationId("RS-" + UUID.randomUUID());    // "RS-..."
        routeStation.setRoute(route);                                 // FK to route
        routeStation.setStation(station);                             // FK to station
        routeStation.setSequenceNumber(sequenceNumber++);             // assign, then increment
        routeStationRepository.save(routeStation);                    // INSERT row
    }
}
```

Line-by-line notes:

1. `orderedStations = stationIds` does **not** copy the list; it points at the same list. That is fine because nothing mutates it.
2. The fallback produces `List.of(start, end)`, an immutable two-element list. So a route always has at least two stops when created through the API.
3. `sequenceNumber++` is a post-increment: the current value is used, then increased. So the first row gets `1`, the next `2`, and so on.
4. The order of `stationIds` in the request **is** the order in the database. The client controls it.
5. Each station is looked up separately, so a list of 7 stops runs 7 station SELECTs. A `findAllById` batch would be one query, but the code keeps the loop simple.
6. An unknown ID aborts the whole transaction. The route insert is rolled back too, so you never get a half-built route.

### Read — how stop rows become `stationIds`

```java
private List<String> loadStationIds(String routeId) {
    List<String> stationIds = new ArrayList<>();                      // result list
    for (RouteStation routeStation :                                  // ordered stop rows
            routeStationRepository.findByRoute_RouteIdOrderBySequenceNumberAsc(routeId)) {
        stationIds.add(routeStation.getStation().getStationId());     // lazy-load, take ID
    }
    return stationIds;                                                // e.g. [STN-VSV, ...]
}
```

- The repository query already sorts by `SequenceNumber`, so the loop preserves the correct order.
- `routeStation.getStation()` triggers a separate SELECT per row because the relation is `LAZY`. That is the N+1 pattern. It works because `spring.jpa.open-in-view=true` keeps the session open during JSON serialization.
- Only the ID string is kept. The full `Station` objects are thrown away after the loop, which is a little wasteful but keeps the JSON shape simple.

### Update — replace the list

```java
@PutMapping("/{id}")                                                  // PUT /api/routes/{id}
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public ResponseEntity<Route> updateRoute(@PathVariable String id, @Valid @RequestBody Route request) {
    Route route = routeRepository.findById(id)                        // load parent
            .orElseThrow(() -> new IllegalArgumentException("Route not found"));

    copyRouteFields(request, route);                                  // update scalars
    route.setUpdatedAt(LocalDateTime.now());                          // bump timestamp
    route = routeRepository.save(route);                              // UPDATE route

    routeStationRepository.deleteByRoute_RouteId(id);                 // remove old stops
    saveRouteStations(route, request.getStationIds());                // add new stops

    return ResponseEntity.ok(loadRoute(id));                          // reload and return
}
```

This is "replace the whole stop list". If the request omits `stationIds`, the helper falls back to the two end stations, so a 7-stop route becomes a 2-stop route. Nothing merges.

Because delete and insert happen in one transaction, a failure during the insert rolls the delete back too. You never lose the old list without getting a new one.

### Delete — children before parent

```java
@DeleteMapping("/{id}")                                               // DELETE /api/routes/{id}
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public ResponseEntity<Void> deleteRoute(@PathVariable String id) {
    if (!routeRepository.existsById(id)) {                            // friendly 400
        throw new IllegalArgumentException("Route not found");
    }
    routeStationRepository.deleteByRoute_RouteId(id);                 // remove child rows first
    routeRepository.deleteById(id);                                   // then the parent
    return ResponseEntity.noContent().build();                        // 204
}
```

If the order were reversed, PostgreSQL would refuse the parent delete because child rows still point at it. There is no `ON DELETE CASCADE`, so the app must clean up manually.

Even with this cleanup, a `Schedule` row can still block the route delete via `FK_Schedule_Route`. Stop rows are not the only children.

### GET one and GET all

```java
@GetMapping("/{id}")                                                  // GET /api/routes/{id}
@Transactional(readOnly = true)
public ResponseEntity<Route> getRouteById(@PathVariable String id) {
    return ResponseEntity.ok(loadRoute(id));                          // loadRoute fills stationIds
}

@GetMapping                                                           // GET /api/routes
@Transactional(readOnly = true)
public ResponseEntity<List<Route>> getAllRoutes() {
    List<Route> routes = routeRepository.findAll();                   // all route rows
    routes.forEach(route -> route.setStationIds(                     // attach stops per route
            loadStationIds(route.getRouteId())));
    return ResponseEntity.ok(routes);                                 // JSON array
}
```

`loadRoute` is the private helper that both GET-by-id and create/update use:

```java
private Route loadRoute(String routeId) {
    Route route = routeRepository.findById(routeId)                   // load or throw
            .orElseThrow(() -> new IllegalArgumentException("Route not found"));
    route.setStationIds(loadStationIds(routeId));                     // fill transient field
    return route;
}
```

The transient `stationIds` field is the bridge between the stop table and the JSON response. It is filled here, never persisted.

---

## 9. One request from start to finish

Let us create a 3-stop route and watch the `"RouteStations"` rows appear.

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

1. **Tomcat accepts** the connection.
2. **Security filters** decode `admin:admin123` from the Basic header.
3. `DbUserDetailsService` runs `SELECT ... FROM "User" WHERE "Contact" = 'admin'`.
4. BCrypt matches, so the request carries `ROLE_ADMIN`.
5. **Spring MVC** maps `POST /api/routes` to `createRoute`.
6. **`@PreAuthorize`** passes.
7. **Jackson** builds the request `Route`. `stationIds` becomes a `List<String>` with three entries in the given order. `startStation` and `endStation` are ignored because they are READ_ONLY.
8. **`@Valid`** checks `routeName`.
9. **`new Route()`** runs, timestamps initialised.
10. `setRouteId("RT-...")` generates e.g. `RT-7a1c9f0e-...`.
11. **`copyRouteFields`** loads `STN-VSV` and `STN-GHP` from `"Stations"` and sets them.
12. **`routeRepository.save(route)`** inserts the route row.
13. PostgreSQL checks `FK_Route_StartStation` and `FK_Route_EndStation`; both pass.
14. **`saveRouteStations(route, ["STN-VSV","STN-AND","STN-GHP"])`** starts.
15. The list is non-null and non-empty, so no fallback is used.
16. `sequenceNumber = 1`.
17. Iteration 1: `findById("STN-VSV")` -> entity. New `RouteStation` with ID `RS-a1...`, route = the saved route, station = Versova, sequence `1`. `save` schedules:
    `INSERT INTO "RouteStations" ("RouteStationID","RouteID","StationID","SequenceNumber") VALUES ('RS-a1...','RT-7a1c...','STN-VSV',1)`.
18. `sequenceNumber` becomes `2`.
19. Iteration 2: `findById("STN-AND")` -> INSERT with `RS-b2...`, `STN-AND`, sequence `2`.
20. `sequenceNumber` becomes `3`.
21. Iteration 3: `findById("STN-GHP")` -> INSERT with `RS-c3...`, `STN-GHP`, sequence `3`.
22. Each insert is checked against `uq_route_station` on `(RouteID, StationID)`. All three stations are different, so all pass.
23. The method returns. **The transaction commits**, flushing all four inserts. If step 21 had failed, the route and the first two stops would roll back.
24. **`loadRoute("RT-7a1c...")`** re-reads the route.
25. `loadStationIds` runs:
    ```sql
    SELECT ... FROM "RouteStations" WHERE "RouteID" = 'RT-7a1c...' ORDER BY "SequenceNumber" ASC;
    ```
26. Three rows come back in order: sequence 1, 2, 3.
27. For each, `getStation()` lazily runs `SELECT ... FROM "Stations" WHERE "StationID" = ?`, producing `STN-VSV`, `STN-AND`, `STN-GHP`.
28. The list is stored in `route.stationIds`.
29. **Jackson** writes the route JSON with the nested start and end stations and `"stationIds": ["STN-VSV","STN-AND","STN-GHP"]`.
30. **`201 Created`** is returned.

Now a read request:

```http
GET /api/routes/RT-7a1c9f0e-5b62-4c8d-9a3f-2d1e6b7c8f90
```

1. Security and mapping as before.
2. `loadRoute` runs `SELECT ... FROM "Route" WHERE "RouteID" = ?`.
3. `loadStationIds` runs the ordered stop query.
4. Three lazy station queries run.
5. The `stationIds` array is filled in sequence order.
6. Jackson writes the response with status 200.

There is no endpoint where you can ask for a single `RouteStation`. The row is only visible as an entry in `stationIds`.

### What if the client sends a duplicate?

```json
"stationIds": ["STN-VSV", "STN-AND", "STN-VSV"]
```

1. Insert route: succeeds.
2. Insert stop 1 (`STN-VSV`, seq 1): succeeds.
3. Insert stop 2 (`STN-AND`, seq 2): succeeds.
4. Insert stop 3 (`STN-VSV`, seq 3): fails `uq_route_station`.
5. The transaction rolls back. Route and stops 1 and 2 disappear.
6. `GlobalExceptionHandler` catches the generic exception and returns 500.

Nothing is left half-written. That atomicity is the main benefit of `@Transactional`.

---

## 10. JSON request and response examples

`RouteStation` is never sent or received on its own. It appears as the `stationIds` array inside a Route.

### Request — the stop list is an array of station IDs

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

| Field | Type | Meaning |
|---|---|---|
| `stationIds` | array of strings | Ordered list of existing `StationID` values. Position 1 is the first element. |
| each entry | string | Must exist in `"Stations"`, or the request fails with 400. |
| duplicates | — | Not allowed; causes a 500 from `uq_route_station`. |
| empty or missing | — | Falls back to `[startStationId, endStationId]`. |

The array order is the route order. JSON arrays are ordered, so this is safe.

### Response — the same list comes back

```json
{
  "routeId": "RT-7a1c9f0e-5b62-4c8d-9a3f-2d1e6b7c8f90",
  "routeName": "Test Line",
  "totalDistance": 5.50,
  "estimatedTime": 12,
  "lineColor": "Blue",
  "createdAt": "2026-10-29T10:55:02.114",
  "updatedAt": "2026-10-29T10:55:02.114",
  "startStation": { "stationId": "STN-VSV", "stationCode": "VSV", "...": "..." },
  "endStation": { "stationId": "STN-GHP", "stationCode": "GHP", "...": "..." },
  "stationIds": ["STN-VSV", "STN-AND", "STN-GHP"]
}
```

| Field | Meaning |
|---|---|
| `stationIds` | Rebuilt from `"RouteStations"` sorted by `SequenceNumber`, not echoed from the request. |
| order | Always the database order, which is the order you sent. |

### What the table actually contains

After the request above, `"RouteStations"` holds:

| RouteStationID | RouteID | StationID | SequenceNumber |
|---|---|---|---|
| `RS-a1...` | `RT-7a1c...` | `STN-VSV` | 1 |
| `RS-b2...` | `RT-7a1c...` | `STN-AND` | 2 |
| `RS-c3...` | `RT-7a1c...` | `STN-GHP` | 3 |

The `stationIds` array is just the third column of this table, in the order of the fourth column.

### If a RouteStation were serialized directly

There is no endpoint, but the entity would produce:

```json
{
  "routeStationId": "RS-a1b2c3d4-...",
  "route": {
    "routeId": "RT-7a1c...",
    "routeName": "Test Line",
    "startStation": { "...": "..." },
    "endStation": { "...": "..." },
    "stationIds": null
  },
  "station": {
    "stationId": "STN-VSV",
    "stationCode": "VSV",
    "latitude": 19.1354,
    "longitude": 72.8209,
    "address": "Versova - JP Road, Andheri West",
    "openedDate": "2015-04-12",
    "lineColor": "Blue",
    "isActive": true
  },
  "sequenceNumber": 1
}
```

Note `"stationIds": null` inside the nested route. It is a transient convenience field, and nobody filled it in this hypothetical path. This is why the project does not expose `RouteStation` directly: the payload is large and the nested route is only half-populated.

### Seeded route response

```json
"stationIds": ["STN-AND", "STN-WEH", "STN-APR", "STN-MRL", "STN-SKN"]
```

This is `RT-L1`. Only five entries, because the seeder inserted only the intermediates and started at sequence 2. See section 13.

---

## 11. Error table

All errors surface through the route endpoints, because there is no stop endpoint of its own.

| Situation | Status | Body |
|---|---|---|
| No or wrong Basic auth | 401 | empty + `WWW-Authenticate: Basic` |
| Non-admin POST/PUT/DELETE | 403 | `{"error":"You do not have permission to access this resource"}` |
| `stationIds` contains an unknown ID | 400 | `{"error":"Station not found: STN-XXX"}` |
| `stationIds` is null and start/end are missing | 400 | `{"error":"Start and end stations are required"}` |
| `stationIds` contains a duplicate | 500 | unexpected error, `uq_route_station` |
| `sequenceNumber` would be null | cannot happen through the API | the controller always sets a number, and the column is `NOT NULL` |
| Delete a route with stops | succeeds | stops are deleted first |
| Delete a route with schedules | 500 | unexpected error, `FK_Schedule_Route` |
| A stop references a deleted station | impossible | `FK_RouteStations_Station` blocks the station delete |

### Why an unknown station gives 400 and not 500

`saveRouteStations` explicitly looks it up and throws:

```java
Station station = stationRepository.findById(stationId)
        .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));
```

`GlobalExceptionHandler` maps `IllegalArgumentException` to 400 and puts the message in `{"error": ...}`.

### Why a duplicate gives 500

There is no pre-check for duplicates in the list. The database unique constraint catches it at flush time. The catch-all handler reports 500. To improve it, you could add:

```java
if (new HashSet<>(orderedStations).size() != orderedStations.size()) {
    throw new IllegalArgumentException("A station cannot appear twice on a route");
}
```

That would turn a database surprise into a friendly 400. The project does not include it.

### Why station deletion is blocked

`FK_RouteStations_Station` means any station used as a stop cannot be deleted while the stop row exists. In the demo, almost every station is used, so deleting stations is mostly impossible. That is intentional protection of the network definition.

---

## 12. Relationships to other groups

| Group | Relation | Foreign key | Meaning |
|---|---|---|---|
| Route | many stops -> one route | `FK_RouteStations_Route` | Parent. Deleting it requires deleting stops first. |
| Station | many stops -> one station | `FK_RouteStations_Station` | The physical stop being referenced. |
| Schedule | indirect | — | A schedule points at a route, and the route's stops are these rows. |
| Fare / Ticket | indirect | — | They price and sell between stations, ignoring routes and stops. |

### Position in the network graph

```
Stations 1 --- N RouteStations N --- 1 Route 1 --- N Schedule N --- 1 Train
```

- A station appears in many route-stop rows, once per route it serves.
- A route has many stop rows.
- A stop row belongs to exactly one route and exactly one station.
- Schedules attach trains to routes, not to individual stops.

### Why the join table is not just a pair

A pure many-to-many join table would have only `(RouteID, StationID)`. This one adds `SequenceNumber`, which is **information about the relationship**. That makes it an entity in its own right, with its own ID, not just a hidden join table.

This is the standard answer to "when does a join table become an entity?" — when it carries data of its own.

### Interaction with Route JSON

`Route` does not hold a collection of `RouteStation` objects. Instead, the controller queries this table and copies the ordered IDs into the transient `stationIds` field. That keeps the entity free of a bidirectional relationship, which would risk infinite JSON recursion.

### Interaction with the database key change

`V6` changed this table's primary key but no other table references `RouteStationID`. So no other entity or migration needed to change. That is why the migration is short and safe.

---

## 13. Seed data

The seeder creates stops inside `seedRoutes()`, using the private helper:

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

    int seq = 2;                                              // <-- starts at 2, not 1
    for (String stationId : intermediateIds) {                // <-- only intermediates
        RouteStation rs = new RouteStation();
        rs.setRouteStationId("RS-" + UUID.randomUUID());
        rs.setRoute(route);
        rs.setStation(stationRepository.findById(stationId).orElseThrow());
        rs.setSequenceNumber(seq++);                          // 2, 3, 4, ...
        routeStationRepository.save(rs);
    }
}
```

### What gets inserted

| Route | Start (column only) | Seeded stop rows | End (column only) |
|---|---|---|---|
| `RT-L1` | `STN-VSV` | `STN-AND` (2), `STN-WEH` (3), `STN-APR` (4), `STN-MRL` (5), `STN-SKN` (6) | `STN-GHP` |
| `RT-L2` | `STN-ARY` | `STN-SEP` (2), `STN-MRL` (3), `STN-BKC` (4), `STN-DDR` (5), `STN-MCU` (6) | `STN-CSM` |
| `RT-L3` | `STN-DHE` | `STN-DVP` (2), `STN-POI` (3), `STN-GDU` (4) | `STN-AEE` |

Total: 5 + 5 + 3 = 13 stop rows.

### The two quirks

1. **Start and end are not stop rows.** They exist as `StartStationID` and `EndStationID` columns on `"Route"`, which is enough for "where does the line begin and end?". But a consumer expecting a complete ordered list from `stationIds` gets only the middle stops.
2. **Sequence starts at 2.** This is consistent with "position 1 is the start station", even though position 1 is not stored. The numbers are contiguous among stored rows, so sorting still works.

A route created through the API behaves differently: `saveRouteStations` starts at 1 and inserts every ID, including the ends. So the seed data and the API disagree about what `stationIds` means.

If you want to fix the seed data, change `int seq = 2;` to `1` and prepend the start ID, then append the end ID. That is a data change, not a schema change. The project keeps it as is, and the honest answer in a viva is: "seeded routes store only intermediate stops; API-created routes store the full list."

### `STN-MRL` appears twice overall

Marol Naka is on `RT-L1` at sequence 5 and on `RT-L2` at sequence 3. That is allowed. The unique constraint is on `(RouteID, StationID)`, so the same station can appear once per route.

This is also a realistic interchange: the station serves two lines.

---

## 14. Swagger test steps

There is no `route-station-controller` in Swagger. You test this table through **route-controller**.

1. Open `http://localhost:8080/swagger-ui.html`.
2. **Authorize** as `admin` / `admin123`.
3. Open **route-controller**.
4. `POST /api/routes` -> **Try it out** -> paste:

```json
{
  "routeName": "Stop Order Test",
  "totalDistance": 5.50,
  "estimatedTime": 12,
  "lineColor": "Blue",
  "startStationId": "STN-VSV",
  "endStationId": "STN-GHP",
  "stationIds": ["STN-VSV", "STN-AND", "STN-GHP"]
}
```

5. **Execute** -> expect `201` and `"stationIds": ["STN-VSV","STN-AND","STN-GHP"]` in exactly that order.
6. Copy the `routeId`.
7. `GET /api/routes/{id}` -> **Execute** -> the order must be the same. This proves the rows were stored with sequence 1, 2, 3 and sorted correctly.
8. `PUT /api/routes/{id}` with the list reversed:

```json
{
  "routeName": "Stop Order Test",
  "totalDistance": 5.50,
  "estimatedTime": 12,
  "lineColor": "Blue",
  "startStationId": "STN-VSV",
  "endStationId": "STN-GHP",
  "stationIds": ["STN-GHP", "STN-AND", "STN-VSV"]
}
```

9. **Execute** -> expect `200` and the reversed list. The old rows were deleted and new ones inserted.
10. `PUT` the same route with **no** `stationIds` -> expect only `["STN-VSV","STN-GHP"]`. This demonstrates the fallback and the replace semantics.
11. `POST` a route with `"stationIds": ["STN-VSV","STN-AND","STN-VSV"]` -> expect `500` from the unique constraint.
12. `POST` a route with `"stationIds": ["STN-NOPE"]` -> expect `400` `{"error":"Station not found: STN-NOPE"}`.
13. `GET /api/routes/RT-L1` -> expect `["STN-AND","STN-WEH","STN-APR","STN-MRL","STN-SKN"]`, the seed quirk with no start or end.
14. `DELETE /api/routes/{id}` from step 5 -> expect `204`. The stop rows are removed first.
15. `GET /api/routes/{id}` -> expect `400` `{"error":"Route not found"}`.
16. `DELETE /api/routes/RT-L1` -> expect `500`, because schedules reference the route (not because of stop rows).

To see the actual rows, run this in the Supabase SQL editor:

```sql
SELECT "RouteStationID", "RouteID", "StationID", "SequenceNumber"
FROM "RouteStations"
WHERE "RouteID" = 'RT-L1'
ORDER BY "SequenceNumber";
```

Expected: five rows, sequences 2 through 6.

---

## 15. How to explain this in 2 minutes

> "RouteStation is the join between a Route and a Station, plus an order number. One row means 'this station is stop number N on this route'.
>
> The table was created in `V1` with a composite primary key on `(RouteID, StationID)`. That naturally enforced the rule that a route cannot visit the same station twice. Migration `V6` replaced that with a single surrogate key, `RouteStationID`. The migration adds the column, backfills existing rows with `'RS-' || RouteID || '-' || StationID`, sets it `NOT NULL`, drops the old primary key, and makes the new column the primary key. Crucially, it adds `UNIQUE (RouteID, StationID)`, so the original business rule is preserved. That is why there is no `RouteStationId.java` and no `@EmbeddedId` in this project — the composite key class is not needed anymore.
>
> The entity has four fields. `routeStationId` is the `@Id`. `route` is a `@ManyToOne` with `@JoinColumn(name = "RouteID")`, lazily loaded and READ_ONLY in JSON. `station` is the same with `StationID`. `sequenceNumber` is a non-null `Integer`.
>
> The repository has just two custom methods. `findByRoute_RouteIdOrderBySequenceNumberAsc` means 'filter by route.routeId and sort by sequenceNumber ascending'; the underscore is the property-path separator. `deleteByRoute_RouteId` clears all stops of a route; Spring Data implements it as a select plus one delete per row, and it must run in a transaction.
>
> There is no RouteStationController. Stop rows are a private child of Route, so all access is through `/api/routes`. POST saves the route first, then loops over the `stationIds` array, loads each station, creates a row with `RS-` plus a UUID, sequence starting at 1, and saves it. PUT deletes all old rows and re-inserts. GET reads rows ordered by sequence and copies the station IDs into the transient `stationIds` field on Route. DELETE removes the child rows before the parent. Everything is inside one `@Transactional`, so a duplicate station or unknown ID rolls the whole thing back.
>
> Two honest limitations. First, duplicates or unknown IDs are not pre-checked; a duplicate hits the unique constraint and returns 500, while an unknown ID is checked in the loop and returns 400. Second, the seeder inserts only intermediate stops and starts numbering at 2, so `GET /api/routes/RT-L1` returns five IDs, not seven. API-created routes store the full list from 1.
>
> Also note there is no unique constraint on `SequenceNumber`. Order is guaranteed by the controller's counter, not by the database."

---

## 16. Beginner Q&A

**Q1. What is a RouteStation in one sentence?**
One stop on one route, with a position number.

**Q2. Why is it an entity and not just a hidden join table?**
Because it carries `SequenceNumber`, which is data about the relationship. A pure join table has only the two foreign keys.

**Q3. Where is `RouteStationId.java`?**
It does not exist. That class would only be needed for a composite key mapped with `@EmbeddedId`. `V6` replaced the composite key with a single `RouteStationID`, so there is no key class.

**Q4. Was the composite key wrong?**
No. It was valid and it enforced uniqueness naturally. The single-column key was chosen for simplicity and future foreign keys. The uniqueness rule was kept as a separate constraint.

**Q5. What exactly does `V6` do?**
Adds `RouteStationID`, backfills it, makes it `NOT NULL`, drops the composite primary key, makes the new column the primary key, and adds `UNIQUE (RouteID, StationID)`.

**Q6. What is a surrogate key?**
A generated column whose only purpose is to identify the row. It has no business meaning. `RouteStationID` is a surrogate; `(RouteID, StationID)` is a natural key.

**Q7. Why does `UQ_ROUTE_STATION` exist if the primary key is unique?**
Because the primary key is now only `RouteStationID`, which says nothing about stations. The unique constraint re-imposes "one station per route".

**Q8. Can the same station appear on two routes?**
Yes. `STN-MRL` is on `RT-L1` and `RT-L2`. The unique rule is the pair, so different `RouteID` values make different pairs.

**Q9. Can a route have the same station twice?**
Not through the database. The insert fails `uq_route_station`. The API does not pre-check, so you get 500.

**Q10. What does `findByRoute_RouteIdOrderBySequenceNumberAsc` mean?**
Find rows where `route.routeId` equals the argument, sorted by `sequenceNumber` ascending. The underscore separates `route` from `routeId`.

**Q11. Why is the method name so long?**
Because Spring Data derives the query from the name. Long names replace SQL. The alternative is `@Query` with explicit JPQL.

**Q12. What SQL does it generate?**
`SELECT ... FROM "RouteStations" WHERE "RouteID" = ? ORDER BY "SequenceNumber" ASC`.

**Q13. What SQL does `deleteByRoute_RouteId` generate?**
By default, a SELECT of the matching entities followed by a DELETE per entity. A bulk `@Modifying @Query` would produce a single DELETE.

**Q14. Why must `deleteByRoute_RouteId` be inside a transaction?**
Delete operations need an active transaction. Both call sites are `@Transactional`. Calling it from a plain method would throw `TransactionRequiredException`.

**Q15. Is there a unique constraint on `SequenceNumber`?**
No. The database allows duplicate positions. The controller assigns `1, 2, 3, ...` so it does not happen in practice. Adding `UNIQUE (RouteID, SequenceNumber)` would enforce it at the schema level.

**Q16. Why can sequence numbers have gaps in seeded data?**
The seeder starts at `2` and never inserts a `1`, reserving position 1 for the start station. Sorting still works. Gaps are harmless because the code never treats the number as an array index.

**Q17. Where does `stationIds` come from in the route JSON?**
The controller reads stop rows ordered by sequence and copies their station IDs into the transient field on `Route`. It is not a column and not stored.

**Q18. Why is `stationIds` transient on Route but not here?**
`RouteStation` stores the real relationship. `Route.stationIds` is only a carrier for JSON. The stop table is the source of truth.

**Q19. Why is there no `/api/route-stations` endpoint?**
A stop cannot exist without a route, and creating one directly would bypass the route's own validation. Keeping it nested keeps the parent and children consistent in one transaction.

**Q20. What happens to stop rows when a route is updated?**
All old rows for that route are deleted and a fresh list is inserted. It is replace semantics, not merge. `RouteStationID` values change on every update, which is harmless.

**Q21. What happens to stop rows when a route is deleted?**
They are deleted first, then the route. Otherwise `FK_RouteStations_Route` would block the parent delete.

**Q22. What happens if I delete a station used as a stop?**
`FK_RouteStations_Station` blocks it. The API returns 500. You must remove the stop from every route first, or deactivate the station.

**Q23. Why are `route` and `station` READ_ONLY in JSON?**
So clients send IDs through the route endpoint rather than nested objects. The controller builds the relations from the database.

**Q24. What is the N+1 issue on reads?**
The ordered query returns stop rows, then `getStation()` runs one extra query per stop because the relation is lazy. Seven stops mean eight queries. `open-in-view=true` keeps the session available during JSON writing.

**Q25. How would you fetch stops and stations in one query?**
```java
@Query("select rs from RouteStation rs join fetch rs.station " +
       "where rs.route.routeId = :routeId order by rs.sequenceNumber")
List<RouteStation> findWithStations(String routeId);
```
That replaces 1 + N queries with one join query.

**Q26. Is the order in `stationIds` guaranteed?**
Yes, as long as you read through `loadStationIds`, which sorts by `SequenceNumber`. The array order in the request is preserved on insert because the loop iterates in list order.

**Q27. Why does the response for `RT-L1` have only five IDs?**
Because the seeder inserts only intermediate stops and starts numbering at 2. Start and end exist only as route columns. Routes created through the API insert the full list from 1.

**Q28. Could two rows have the same sequence and still sort deterministically?**
Not reliably. SQL does not guarantee tie order without a second sort column. Since the controller never produces ties, this is not a practical issue.

---

## 17. Glossary

| Term | Meaning in this file |
|---|---|
| **Backfill** | Filling existing rows with a value after adding a column. |
| **Bean** | Object created and managed by Spring. |
| **Composite key** | Primary key made of two or more columns. |
| **Constraint** | A database rule such as primary key, foreign key, or unique. |
| **Controller** | Class mapping URLs to Java methods. |
| **Derived query** | Repository method whose name Spring turns into SQL. |
| **DTO** | Class used only to carry data. Not used for this table. |
| **Entity** | Class mapped to a table. |
| **FetchType.LAZY** | Load the relation only when read. |
| **FK** | Foreign key. |
| **Flyway** | Applies numbered SQL migrations. |
| **Foreign key** | Column that must exist as a primary key elsewhere. |
| **Hibernate** | JPA implementation that writes SQL. |
| **HTTP status** | Numeric result of a request. |
| **Jackson** | Java <-> JSON converter. |
| **JPA** | Java standard for object-table mapping. |
| **Join table** | Table linking two others, optionally with extra columns. |
| **`@JoinColumn`** | Declares the foreign key column. |
| **`@ManyToOne`** | Many rows here point to one row there. |
| **merge** | JPA operation copying state onto a managed entity; may SELECT first. |
| **Migration** | One versioned SQL file. |
| **N+1 problem** | One list query plus one query per item. |
| **Natural key** | Key made of real business values, e.g. `(RouteID, StationID)`. |
| **Normalisation** | Structuring tables to avoid lists inside columns. |
| **Open Session in View** | Keeping the Hibernate session open during JSON rendering. |
| **`Optional`** | Container that may hold a value or nothing. |
| **ORM** | Object-relational mapping. |
| **Primary key** | Column(s) uniquely identifying a row. |
| **Property path** | The `route_RouteId` part of a query method name. |
| **Proxy** | Placeholder for a not-yet-loaded relation. |
| **Repository** | Interface providing database methods. |
| **Sequence** | Position number of a stop on a route. |
| **Surrogate key** | Generated column used only as a primary key. |
| **Swagger UI** | Generated page for testing endpoints. |
| **Transaction** | All-or-nothing group of database operations. |
| **Unique constraint** | Rule that a combination of values may not repeat. |
| **UUID** | 128-bit random identifier. |

---

## 18. One-line summary

`RouteStation` is the ordered join row between `Route` and `Station`; `V6` gave it a surrogate `RouteStationID` while keeping `(RouteID, StationID)` unique, a two-method repository reads and clears it, and `RouteController` is the only door — writing stops on create, replacing them on update, sorting them on read, and deleting them before the route.

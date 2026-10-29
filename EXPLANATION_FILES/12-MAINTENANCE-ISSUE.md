# 12 — Maintenance Issue Group (Reporting and fixing problems)

---

## 1. Real-life analogy

Think of the **complaint book at a railway station**.

A train door is stuck. A lift is noisy. A platform light is out. Someone writes it in the book:

- what kind of problem it is
- a description
- how urgent it is
- which train or which station is affected
- who reported it
- when it was reported
- whether it is still open or fixed
- when it was fixed

The book is the `MaintenanceIssue` table. Each written entry is one row.

The **status** is the lifecycle of the complaint:

```text
Open  →  In progress  →  Resolved  →  Closed
```

In this project the controller has a `resolve` action that jumps straight to `"Closed"` and stamps `ResolvedAt` with the current time.

There is an honesty point here. The demo data uses uppercase words: `OPEN`, `IN_PROGRESS`, `RESOLVED`. The create endpoint writes `"Open"`. The resolve endpoint writes `"Closed"`. So the same column holds slightly different spellings. That is a real inconsistency in the code, and you should mention it rather than pretend the statuses are perfectly uniform.

Another honesty point: the reporter is **not** taken from the logged-in user. The client sends `reportedById`. If it is omitted, `reportedBy` stays null. A stricter design would force the reporter to be the authenticated user.

---

## 2. Words you need

| Word | One simple sentence |
|---|---|
| HTTP | The protocol clients use to talk to the server. |
| Endpoint | One URL plus one method, e.g. `PATCH /api/maintenance/{id}/resolve`. |
| JSON | Text format for data, like `{"status": "OPEN"}`. |
| Status code | Number describing the result: 200, 201, 204, 400, 401, 403, 500. |
| Spring Boot | Framework that starts the server and wires the parts. |
| Annotation | A `@word` that gives Spring, JPA or Jackson extra instructions. |
| Entity | Java class mapped to a database table. |
| Repository | Interface that provides database operations without hand-written SQL. |
| Controller | Class whose methods handle HTTP requests. |
| JPA | Java standard for mapping objects to tables. |
| Hibernate | Library that implements JPA and generates SQL. |
| Dependency injection | Spring creates objects and passes them into a constructor. |
| Bean | Object managed by Spring. |
| `@RestController` | "This class handles HTTP and returns JSON." |
| `@RequestMapping` | URL prefix shared by all methods in the class. |
| `@PostMapping` | Handles POST (create). |
| `@PutMapping` | Handles PUT (full update). |
| `@GetMapping` | Handles GET (read). |
| `@PatchMapping` | Handles PATCH (partial change or action). |
| `@DeleteMapping` | Handles DELETE (remove). |
| `@PathVariable` | Reads a value from the URL path. |
| `@RequestBody` | Converts the JSON body into a Java object. |
| `@Valid` | Runs validation annotations before the method body. |
| `@PreAuthorize` | Checks roles before the method body. |
| `@Transactional` | Makes the method one all-or-nothing database unit. |
| `Authentication` | Spring's object holding the logged-in user, used in other groups. |
| `ResponseEntity` | Wrapper for status code plus body. |
| `@Entity` | Marks a class as a table. |
| `@Table` | Sets the exact table name. |
| `@Id` | Marks the primary key. |
| `@Column` | Sets exact column name, length or type. |
| `@ManyToOne` | Many issues can point to one train, station or user. |
| `@JoinColumn` | Names the foreign-key column. |
| `@Transient` | "Do not save this field as a column." |
| `@JsonProperty` | Controls JSON input and output for a field. |
| `READ_ONLY` | Shown in responses, ignored in requests. |
| `WRITE_ONLY` | Accepted in requests, hidden from responses. |
| `FetchType.LAZY` | Load the related row only when read. |
| `@NotBlank` | "Must not be null, empty, or only spaces." |
| `LocalDateTime` | Date plus time, no time zone. |
| `UUID` | Long random string, very unlikely to repeat. |
| Primary key | Column that uniquely identifies a row. |
| Foreign key | Column that points to another table's primary key. |
| Nullable | Whether the column may store `NULL`. |
| Derived query | Repository method whose name Spring turns into SQL. |
| Transaction | Group of changes that all succeed or all roll back. |
| Status | Current lifecycle stage of an issue. |
| Priority | How urgent the issue is. |
| Resolve | Mark the issue fixed and stamp the fix time. |
| `TEXT` | PostgreSQL column type for long text. |
| Role | User label: `ADMIN`, `PASSENGER`, `TICKET_AGENT`, `MAINTENANCE_STAFF`. |
| HTTP Basic | Login style that sends credentials with every request. |
| Swagger UI | Web page for testing the API. |

---

## 3. Files in this group

| Role | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/MaintenanceIssue.java` |
| Controller | `src/main/java/com/metro/ticketing/controller/MaintenanceIssueController.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/MaintenanceIssueRepository.java` |
| Table definition | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Error handling | `src/main/java/com/metro/ticketing/exception/GlobalExceptionHandler.java` |
| Demo data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` |

Supporting files:

- `src/main/java/com/metro/ticketing/entity/Train.java`
- `src/main/java/com/metro/ticketing/entity/Station.java`
- `src/main/java/com/metro/ticketing/entity/User.java`
- `src/main/java/com/metro/ticketing/repository/TrainRepository.java`
- `src/main/java/com/metro/ticketing/repository/StationRepository.java`
- `src/main/java/com/metro/ticketing/repository/UserRepository.java`
- `src/main/java/com/metro/ticketing/config/SecurityConfig.java`

---

## 4. Database table

From `V1__create_metro_ticketing_schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS "MaintenanceIssue" (
    "IssueID" VARCHAR(50) PRIMARY KEY,
    "TrainID" VARCHAR(50),
    "StationID" VARCHAR(50),
    "ReportedId" VARCHAR(50),
    "IssueType" VARCHAR(100),
    "Description" TEXT,
    "Status" VARCHAR(50) DEFAULT 'Open',
    "Priority" VARCHAR(20),
    "CreatedAt" TIMESTAMP,
    "ResolvedAt" TIMESTAMP,
    CONSTRAINT "FK_Maintenance_Train"
        FOREIGN KEY ("TrainID") REFERENCES "Trains" ("TrainID"),
    CONSTRAINT "FK_Maintenance_Station"
        FOREIGN KEY ("StationID") REFERENCES "Stations" ("StationID"),
    CONSTRAINT "FK_Maintenance_Reporter"
        FOREIGN KEY ("ReportedId") REFERENCES "User" ("UserId")
);
```

Column by column:

| Column | Type | Null? | Meaning |
|---|---|---|---|
| `IssueID` | `VARCHAR(50)` primary key | no | Unique issue id. App builds `ISS-` + UUID. |
| `TrainID` | `VARCHAR(50)` foreign key | yes | The affected train, if the problem is on a train. |
| `StationID` | `VARCHAR(50)` foreign key | yes | The affected station, if the problem is at a station. |
| `ReportedId` | `VARCHAR(50)` foreign key | yes | The user who reported it. Note the spelling: `ReportedId`, not `ReportedBy`. |
| `IssueType` | `VARCHAR(100)` | yes | Free-text category, e.g. `TRAIN`, `SIGNAL`. |
| `Description` | `TEXT` | yes | Long free-text explanation. |
| `Status` | `VARCHAR(50)` default `'Open'` | yes | Lifecycle stage. |
| `Priority` | `VARCHAR(20)` | yes | Urgency, e.g. `LOW`, `MEDIUM`, `HIGH`. |
| `CreatedAt` | `TIMESTAMP` | yes | When the issue was recorded. |
| `ResolvedAt` | `TIMESTAMP` | yes | When it was marked closed. Null while unresolved. |

Why `TrainID` and `StationID` are both nullable:

- A door fault belongs to a train.
- A broken escalator belongs to a station.
- An issue might belong to both, or to neither.
- Making both `NOT NULL` would force fake values.
- The controller does not require either one.

Why the reporter column is named `ReportedId`:

- That is the literal name in the SQL file.
- It is unusual English. `ReportedBy` would be clearer.
- The Java field is `reportedBy`, but the column is `"ReportedId"`, so `@JoinColumn(name = "\"ReportedId\"")` is required.

Why `IssueType` and `Priority` are `VARCHAR` and not enums:

- The database accepts any string.
- There is no `CHECK` constraint.
- The seeder uses `TRAIN`, `LIFT_ESCALATOR`, `SIGNAL`, `FACILITY`.
- A typo such as `TRANE` would be stored happily.
- This is flexible but weak. Mention it honestly.

Why `Status` has a default:

- If an insert omits status, PostgreSQL uses `'Open'`.
- The controller also sets `"Open"` explicitly, so the default is a safety net.

Why `Description` is `TEXT`:

- `VARCHAR(255)` would be too short for a real fault report.
- `TEXT` has no practical length limit in PostgreSQL.
- In Java it is still a `String`; only the column definition differs.

---

## 5. Entity line by line

File: `src/main/java/com/metro/ticketing/entity/MaintenanceIssue.java`

Comments after `//` are added for teaching. The real file has none.

```java
package com.metro.ticketing.entity;                                      // entity package

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;            // hide Hibernate internals from JSON
import com.fasterxml.jackson.annotation.JsonProperty;                    // per-field JSON control
import jakarta.persistence.*;                                            // JPA annotations
import jakarta.validation.constraints.NotBlank;                          // "not null / not empty / not spaces"

import java.time.LocalDateTime;                                          // date + time

@Entity                                                                  // this class is a table
@Table(name = "\"MaintenanceIssue\"")                                    // exact table name
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})           // hide lazy-proxy fields
public class MaintenanceIssue {

    @Id                                                                  // primary key
    @Column(name = "\"IssueID\"", length = 50)                           // column IssueID, max 50
    private String issueId;                                              // e.g. "ISS-1a2b..."

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)                // output only
    @ManyToOne(fetch = FetchType.LAZY)                                   // many issues -> one train
    @JoinColumn(name = "\"TrainID\"",                                    // FK column on this table
                referencedColumnName = "\"TrainID\"")                    // points to Trains.TrainID
    private Train train;                                                 // may be null

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)                // output only
    @ManyToOne(fetch = FetchType.LAZY)                                   // many issues -> one station
    @JoinColumn(name = "\"StationID\"",                                  // FK column
                referencedColumnName = "\"StationID\"")                  // points to Stations.StationID
    private Station station;                                             // may be null

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)                // output only
    @ManyToOne(fetch = FetchType.LAZY)                                   // many issues -> one reporter
    @JoinColumn(name = "\"ReportedId\"",                                 // FK column, odd spelling
                referencedColumnName = "\"UserId\"")                     // points to User.UserId
    private User reportedBy;                                             // may be null

    @Transient                                                           // no column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)               // input only
    private String trainId;                                              // helper for train id

    @Transient                                                           // no column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)               // input only
    private String stationId;                                            // helper for station id

    @Transient                                                           // no column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)               // input only
    private String reportedById;                                         // helper for reporter id

    @NotBlank(message = "Issue type is required")                        // reject null/""/"   "
    @Column(name = "\"IssueType\"", length = 100)                        // column IssueType, max 100
    private String issueType;                                            // category text

    @NotBlank(message = "Description is required")                       // reject null/""/"   "
    @Column(name = "\"Description\"", columnDefinition = "TEXT")         // force PostgreSQL TEXT
    private String description;                                          // long explanation

    @Column(name = "\"Status\"", length = 50)                            // column Status
    private String status = "Open";                                      // default for new objects

    @Column(name = "\"Priority\"", length = 20)                          // column Priority
    private String priority;                                             // no default; controller uses MEDIUM

    @Column(name = "\"CreatedAt\"")                                      // column CreatedAt
    private LocalDateTime createdAt = LocalDateTime.now();               // default = now

    @Column(name = "\"ResolvedAt\"")                                     // column ResolvedAt
    private LocalDateTime resolvedAt;                                    // null until resolved

    // ... getters and setters ...
}
```

### The getters and setters

```java
public String getIssueId() { return issueId; }
public void setIssueId(String issueId) { this.issueId = issueId; }

public Train getTrain() { return train; }
public void setTrain(Train train) { this.train = train; }

public Station getStation() { return station; }
public void setStation(Station station) { this.station = station; }

public User getReportedBy() { return reportedBy; }
public void setReportedBy(User reportedBy) { this.reportedBy = reportedBy; }

public String getIssueType() { return issueType; }
public void setIssueType(String issueType) { this.issueType = issueType; }

public String getDescription() { return description; }
public void setDescription(String description) { this.description = description; }

public String getStatus() { return status; }
public void setStatus(String status) { this.status = status; }

public String getPriority() { return priority; }
public void setPriority(String priority) { this.priority = priority; }

public LocalDateTime getCreatedAt() { return createdAt; }
public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

public LocalDateTime getResolvedAt() { return resolvedAt; }
public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

public String getTrainId() { return trainId; }
public void setTrainId(String trainId) { this.trainId = trainId; }

public String getStationId() { return stationId; }
public void setStationId(String stationId) { this.stationId = stationId; }

public String getReportedById() { return reportedById; }
public void setReportedById(String reportedById) { this.reportedById = reportedById; }
```

### Three relations, three transient ids

The pattern repeats three times:

| Real relation | Column | Transient input field |
|---|---|---|
| `train` | `TrainID` | `trainId` |
| `station` | `StationID` | `stationId` |
| `reportedBy` | `ReportedId` | `reportedById` |

Why this pattern:

- The database needs a foreign key, so the entity holds a real object.
- The client should not send whole nested objects, so the entity also holds simple id strings.
- Those strings are `@Transient`, so they never become columns.
- They are `WRITE_ONLY`, so responses show the objects but not duplicate ids.
- The controller reads an id, looks up the object, and calls the matching setter.

Why the relations are `READ_ONLY`:

- A client cannot fabricate a `Train` object with fake data.
- It must supply a real id that exists in the database.
- `applyRelations` performs that lookup and throws a clear error if the id is unknown.

### Why `@ManyToOne` is used

- Many issues can point to the same train.
- A train has many issues over its life.
- The same is true for stations and reporters.
- `@ManyToOne` means "this side holds the foreign key".

### Why `FetchType.LAZY`

- Listing 100 issues should not automatically load 100 trains, 100 stations and 100 users.
- Lazy loading defers each related object until it is read.
- On this project `spring.jpa.open-in-view=true`, so the session stays open while JSON is written. That is why lazy fields can still be serialized without an error.

A subtle risk: if `open-in-view` were `false`, Jackson might try to read a lazy field after the transaction closed and throw `LazyInitializationException`. On this project it is `true`, so it works.

### Validation on `issueType` and `description`

```java
@NotBlank(message = "Issue type is required")
private String issueType;

@NotBlank(message = "Description is required")
private String description;
```

`@NotBlank` is stricter than `@NotNull`:

- `null` fails
- `""` fails
- `"   "` (only spaces) fails

The message is used by the global handler when `@Valid` fails.

`train`, `station`, `reportedBy`, `priority`, `status`, `resolvedAt` have **no** validation. In particular:

- an issue can be created with neither a train nor a station
- priority can be any string
- status can be any string on update

### Why `columnDefinition = "TEXT"`

Without it, Hibernate would guess `VARCHAR(255)` for a `String`. The SQL migration uses `TEXT`, and `spring.jpa.hibernate.ddl-auto=validate` compares the mappings to the real schema. `columnDefinition` makes the mapping match exactly.

### Why `status = "Open"` but not `priority`

- The entity default for status matches the database default.
- Priority has no sensible universal default at the entity level.
- The controller chooses `"MEDIUM"` when the client sends none.
- Keeping that decision in the controller makes the business rule visible.

### Why `resolvedAt` starts null

- An issue that has never been fixed has no fix time.
- `null` is meaningful: "not resolved yet".
- The resolve endpoint fills it.

---

## 6. Repository line by line

File: `src/main/java/com/metro/ticketing/repository/MaintenanceIssueRepository.java`

```java
package com.metro.ticketing.repository;

import com.metro.ticketing.entity.MaintenanceIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository                                                                          // Spring bean marker
public interface MaintenanceIssueRepository
        extends JpaRepository<MaintenanceIssue, String> {                            // entity, id type

    List<MaintenanceIssue> findByTrain_TrainId(String trainId);                      // issues for one train
    List<MaintenanceIssue> findByStation_StationId(String stationId);                // issues for one station
    List<MaintenanceIssue> findByStatus(String status);                              // issues in one status
}
```

### Free methods from `JpaRepository`

| Method | What it does | SQL Spring runs |
|---|---|---|
| `save(issue)` | Insert new or update existing | `INSERT INTO "MaintenanceIssue" (...)` or `UPDATE ... WHERE "IssueID" = ?` |
| `findById(id)` | One row by primary key | `SELECT * FROM "MaintenanceIssue" WHERE "IssueID" = ?` |
| `findAll()` | Every row | `SELECT * FROM "MaintenanceIssue"` |
| `existsById(id)` | Existence check | `SELECT COUNT(*) > 0 FROM "MaintenanceIssue" WHERE "IssueID" = ?` |
| `deleteById(id)` | Delete one row | `DELETE FROM "MaintenanceIssue" WHERE "IssueID" = ?` |
| `count()` | Row count | `SELECT COUNT(*) FROM "MaintenanceIssue"` |

### Derived query 1: by train

```java
List<MaintenanceIssue> findByTrain_TrainId(String trainId);
```

| Piece | Meaning |
|---|---|
| `findBy` | SELECT |
| `Train` | the `train` field |
| `_` | go inside the related `Train` object |
| `TrainId` | its `trainId` field |

```sql
SELECT *
FROM "MaintenanceIssue"
WHERE "TrainID" = ?;
```

### Derived query 2: by station

```java
List<MaintenanceIssue> findByStation_StationId(String stationId);
```

```sql
SELECT *
FROM "MaintenanceIssue"
WHERE "StationID" = ?;
```

### Derived query 3: by status

```java
List<MaintenanceIssue> findByStatus(String status);
```

No underscore here, because `status` is a direct field on `MaintenanceIssue`.

```sql
SELECT *
FROM "MaintenanceIssue"
WHERE "Status" = ?;
```

This is an **exact, case-sensitive** match in PostgreSQL. `findByStatus("Open")` will not find rows stored as `OPEN`. Keep that in mind during the demo.

### What is missing

There is no query for priority, no query for reporter, and no query for unresolved issues. You would add methods like `findByPriority` or `findByResolvedAtIsNull` if needed. They are not required by the current controller.

---

## 7. Controller endpoints

Base path: `/api/maintenance`.

| Method | Full path | Who is allowed | Purpose | Success |
|---|---|---|---|---|
| POST | `/api/maintenance` | `ADMIN`, `MAINTENANCE_STAFF`, `TICKET_AGENT` | Report a new issue | 201 + issue JSON |
| PUT | `/api/maintenance/{id}` | `ADMIN`, `MAINTENANCE_STAFF` | Update an issue | 200 + issue JSON |
| GET | `/api/maintenance/{id}` | any logged-in user | Read one issue | 200 + issue JSON |
| GET | `/api/maintenance` | `ADMIN`, `MAINTENANCE_STAFF` | List all issues | 200 + array |
| GET | `/api/maintenance/train/{trainId}` | any logged-in user | Issues for one train | 200 + array |
| GET | `/api/maintenance/station/{stationId}` | any logged-in user | Issues for one station | 200 + array |
| GET | `/api/maintenance/status/{status}` | `ADMIN`, `MAINTENANCE_STAFF` | Issues with one status | 200 + array |
| PATCH | `/api/maintenance/{id}/resolve` | `ADMIN`, `MAINTENANCE_STAFF` | Close the issue | 200 + issue JSON |
| DELETE | `/api/maintenance/{id}` | `ADMIN` | Delete the issue | 204, empty body |

Note who is **not** allowed:

- A `PASSENGER` cannot report an issue.
- Only staff can create. That is a product decision, and it is worth stating.

---

## 8. Controller line by line

File: `src/main/java/com/metro/ticketing/controller/MaintenanceIssueController.java`

### 8.1 Class header and constructor

```java
@RestController                                                         // JSON responses
@RequestMapping("/api/maintenance")                                     // URL prefix
public class MaintenanceIssueController {

    private final MaintenanceIssueRepository maintenanceIssueRepository; // issue table
    private final TrainRepository trainRepository;                       // train lookup
    private final StationRepository stationRepository;                   // station lookup
    private final UserRepository userRepository;                         // reporter lookup

    public MaintenanceIssueController(
            MaintenanceIssueRepository maintenanceIssueRepository,
            TrainRepository trainRepository,
            StationRepository stationRepository,
            UserRepository userRepository) {
        this.maintenanceIssueRepository = maintenanceIssueRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
    }
```

Four dependencies, because the entity has three relations plus its own table. Each relation needs its own repository for the lookup in `applyRelations`.

### 8.2 Report a new issue

```java
@PostMapping                                                             // POST /api/maintenance
@PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_STAFF', 'TICKET_AGENT')")
@Transactional
public ResponseEntity<MaintenanceIssue> createMaintenanceIssue(
        @Valid @RequestBody MaintenanceIssue request) {                  // JSON -> entity, then validation

    MaintenanceIssue issue = new MaintenanceIssue();                     // new row
    issue.setIssueId("ISS-" + UUID.randomUUID());                        // server id
    issue.setIssueType(request.getIssueType());                          // copy category
    issue.setDescription(request.getDescription());                      // copy description
    issue.setPriority(request.getPriority() != null                     // priority:
            ? request.getPriority() : "MEDIUM");                         // sent value or MEDIUM
    issue.setStatus("Open");                                             // always starts Open
    issue.setCreatedAt(LocalDateTime.now());                             // server timestamp
    applyRelations(request, issue);                                      // resolve train/station/reporter ids

    return ResponseEntity.status(HttpStatus.CREATED)                     // 201
            .body(maintenanceIssueRepository.save(issue));               // INSERT + JSON
}
```

Why a new object is built instead of saving `request` directly:

- The client must not choose `issueId`.
- The client must not choose `createdAt`.
- The status must always start at `"Open"`, even if the client sends `"Closed"`.
- Copying field by field makes those rules explicit.

Why priority defaults to `"MEDIUM"`:

- The entity leaves `priority` null.
- A null priority is unhelpful for sorting or display.
- The controller substitutes a sensible middle value.

Why `createdAt` is set on the server:

- Client clocks can be wrong or manipulated.
- The server time is the single source of truth.

Why there is no `Authentication` parameter:

- The code does not use the logged-in user.
- The reporter comes from `reportedById` in the body.
- So an agent could report on behalf of someone else.
- This is a design gap. A stricter version would ignore `reportedById` for non-admins and use `currentUser(authentication)`.

### 8.3 Update an issue

```java
@PutMapping("/{id}")                                                     // PUT /api/maintenance/ISS-X
@PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_STAFF')")
@Transactional
public ResponseEntity<MaintenanceIssue> updateMaintenanceIssue(
        @PathVariable String id,
        @Valid @RequestBody MaintenanceIssue request) {

    MaintenanceIssue issue = maintenanceIssueRepository.findById(id)      // load existing row
            .orElseThrow(() -> new IllegalArgumentException("Maintenance issue not found"));

    issue.setIssueType(request.getIssueType());                           // required by @NotBlank
    issue.setDescription(request.getDescription());                       // required by @NotBlank

    if (request.getStatus() != null) {                                    // optional status
        issue.setStatus(request.getStatus());
    }
    if (request.getPriority() != null) {                                  // optional priority
        issue.setPriority(request.getPriority());
    }

    applyRelations(request, issue);                                       // update any relations supplied

    return ResponseEntity.ok(maintenanceIssueRepository.save(issue));     // UPDATE, 200 + JSON
}
```

Why PUT is a full update, not a patch:

- `issueType` and `description` are always overwritten.
- Both are `@NotBlank`, so the request must include them.
- Sending only `{"status": "RESOLVED"}` fails validation with two field errors.
- That is correct for PUT semantics, but it surprises people who expect a partial update.

Why status and priority are guarded with `if != null`:

- They are optional fields in the request.
- A missing value means "leave the old value alone".
- An empty string is **not** null, so `"status": ""` would be stored. There is no `@NotBlank` on status.

Why `applyRelations` cannot clear a relation:

- It only sets a relation when the id is non-null.
- Sending `{"trainId": null}` leaves the old train in place.
- There is no endpoint to detach a train or station.
- Say this if asked about partial updates.

### 8.4 Read one issue

```java
@GetMapping("/{id}")                                                     // GET /api/maintenance/ISS-SEED1
@Transactional(readOnly = true)
public ResponseEntity<MaintenanceIssue> getMaintenanceIssueById(@PathVariable String id) {

    MaintenanceIssue issue = maintenanceIssueRepository.findById(id)      // look up
            .orElseThrow(() -> new IllegalArgumentException("Maintenance issue not found"));

    return ResponseEntity.ok(issue);                                     // 200 + JSON
}
```

No `@PreAuthorize`:

- Any authenticated user may read a single issue.
- Passengers are authenticated too, so they can read issues by id if they know the id.
- Listing all issues is restricted, but reading one is not.
- That is an inconsistency worth noting.

### 8.5 List all issues

```java
@GetMapping                                                              // GET /api/maintenance
@PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_STAFF')")
@Transactional(readOnly = true)
public ResponseEntity<List<MaintenanceIssue>> getAllMaintenanceIssues() {
    return ResponseEntity.ok(maintenanceIssueRepository.findAll());       // 200 + array
}
```

Ticket agents can create issues but cannot list all of them. Only admin and maintenance staff can.

### 8.6 Issues by train

```java
@GetMapping("/train/{trainId}")                                          // GET /api/maintenance/train/TRN-101
@Transactional(readOnly = true)
public ResponseEntity<List<MaintenanceIssue>> getIssuesByTrain(@PathVariable String trainId) {
    return ResponseEntity.ok(maintenanceIssueRepository.findByTrain_TrainId(trainId)); // 200 + array
}
```

No existence check:

- An unknown train id returns `[]`, not 404.
- That is acceptable for a filter endpoint.

### 8.7 Issues by station

```java
@GetMapping("/station/{stationId}")                                      // GET /api/maintenance/station/STN-AND
@Transactional(readOnly = true)
public ResponseEntity<List<MaintenanceIssue>> getIssuesByStation(@PathVariable String stationId) {
    return ResponseEntity.ok(maintenanceIssueRepository.findByStation_StationId(stationId)); // 200 + array
}
```

Same pattern: no existence check, empty array for unknown ids.

### 8.8 Issues by status

```java
@GetMapping("/status/{status}")                                          // GET /api/maintenance/status/OPEN
@PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_STAFF')")
@Transactional(readOnly = true)
public ResponseEntity<List<MaintenanceIssue>> getIssuesByStatus(@PathVariable String status) {
    return ResponseEntity.ok(maintenanceIssueRepository.findByStatus(status)); // exact match
}
```

Because the match is exact and case-sensitive:

- `/status/OPEN` finds the seeded uppercase rows.
- `/status/Open` finds rows created by the create endpoint.
- `/status/open` finds nothing.
- This is a real usability problem caused by mixed casing in the data.

### 8.9 Resolve an issue

```java
@PatchMapping("/{id}/resolve")                                           // PATCH /api/maintenance/ISS-SEED1/resolve
@PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_STAFF')")
@Transactional
public ResponseEntity<MaintenanceIssue> resolveMaintenanceIssue(@PathVariable String id) {

    MaintenanceIssue issue = maintenanceIssueRepository.findById(id)      // load row
            .orElseThrow(() -> new IllegalArgumentException("Maintenance issue not found"));

    if ("Closed".equalsIgnoreCase(issue.getStatus())) {                   // already closed?
        throw new IllegalArgumentException("Maintenance issue is already closed");
    }

    issue.setStatus("Closed");                                           // mark closed
    issue.setResolvedAt(LocalDateTime.now());                            // stamp fix time
    return ResponseEntity.ok(maintenanceIssueRepository.save(issue));     // UPDATE, 200 + JSON
}
```

Why `"Closed".equalsIgnoreCase(...)` and not `issue.getStatus().equals("Closed")`:

- `issue.getStatus()` could be null, and calling `.equals` on null would crash.
- Putting the constant first is a null-safe idiom.
- `equalsIgnoreCase` accepts `closed`, `CLOSED` and `Closed`.

Why the action is PATCH:

- It changes part of the resource, not the whole thing.
- The URL names the action: `/resolve`.
- No body is needed.

Why `resolvedAt` is set here and nowhere else:

- It records the moment of resolution.
- The update endpoint can set `status` to `"RESOLVED"` but does **not** set `resolvedAt`. That produces a row with a resolved status and a null timestamp.
- This is an inconsistency. The resolve endpoint is the reliable path.

What happens to a seeded `RESOLVED` row:

- Its status is `"RESOLVED"`, not `"Closed"`.
- The guard only blocks `"Closed"`.
- So calling resolve on it succeeds and changes it to `"Closed"`.
- That is another consequence of mixed status words.

### 8.10 Delete an issue

```java
@DeleteMapping("/{id}")                                                   // DELETE /api/maintenance/ISS-SEED4
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public ResponseEntity<Void> deleteMaintenanceIssue(@PathVariable String id) {

    if (!maintenanceIssueRepository.existsById(id)) {                     // avoid silent delete
        throw new IllegalArgumentException("Maintenance issue not found");
    }

    maintenanceIssueRepository.deleteById(id);                            // DELETE FROM ...
    return ResponseEntity.noContent().build();                            // 204, no body
}
```

Only an admin can delete. Maintenance staff can resolve but not erase history.

### 8.11 Helper: apply relations

```java
private void applyRelations(MaintenanceIssue request, MaintenanceIssue issue) {

    if (request.getTrainId() != null) {                                   // did the client send a train id?
        Train train = trainRepository.findById(request.getTrainId())      // find it
                .orElseThrow(() -> new IllegalArgumentException("Train not found"));
        issue.setTrain(train);                                            // attach the real object
    }

    if (request.getStationId() != null) {                                 // did the client send a station id?
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));
        issue.setStation(station);
    }

    if (request.getReportedById() != null) {                              // did the client send a reporter id?
        User reporter = userRepository.findById(request.getReportedById())
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));
        issue.setReportedBy(reporter);
    }
}
```

Why this helper exists:

- Both create and update need the same three lookups.
- Duplicating the code would invite mistakes.
- One helper keeps the rules identical.

Why it is private:

- It is not an HTTP endpoint.
- It is an internal detail of the controller.

Why it checks `!= null`:

- All three relations are optional.
- A station-only issue should not need a train id.
- Omitting an id means "no change" on update.

Why it throws instead of ignoring a bad id:

- Silently dropping a bad reference would hide a client bug.
- A 400 with "Train not found" tells the caller exactly what is wrong.

---

## 9. One request from start to finish

Real seeded data:

- Issue: `ISS-SEED1`
- Status: `OPEN`
- Priority: `HIGH`
- Train: `TRN-101` (Blue Line)
- Reporter: Suresh Kumar, `UserId = STAFF-02`, contact `maint1`, password `Passw0rd!`
- Description: door sensor on coach 3 failing to close during peak hours

### The story: resolving the issue

1. Suresh opens Swagger at `http://localhost:8080/swagger-ui/index.html`.
2. He clicks **Authorize** and enters `maint1` / `Passw0rd!`.
3. He opens `PATCH /api/maintenance/{id}/resolve`.
4. He enters id `ISS-SEED1` and clicks **Execute**.
5. Swagger sends:
   ```http
   PATCH /api/maintenance/ISS-SEED1/resolve
   Authorization: Basic bWFpbnQxOlBhc3N3MHJkIQ==
   ```
6. Spring Security validates the credentials through `DbUserDetailsService`.
7. The `User` row with `Contact = 'maint1'` is loaded.
8. Its role is `MAINTENANCE_STAFF`, so authorities are `[ROLE_MAINTENANCE_STAFF]`.
9. Spring MVC matches `/{id}/resolve` to `resolveMaintenanceIssue`.
10. `@PreAuthorize` allows `ROLE_MAINTENANCE_STAFF`. It passes.
11. `findById("ISS-SEED1")` runs:
    ```sql
    SELECT * FROM "MaintenanceIssue" WHERE "IssueID" = 'ISS-SEED1';
    ```
12. The row loads with `Status = 'OPEN'`, `ResolvedAt = null`.
13. `"Closed".equalsIgnoreCase("OPEN")` returns false, so the guard does not fire.
14. The controller sets `status = "Closed"`.
15. It sets `resolvedAt = 2026-10-29T11:40:12`.
16. `save(issue)` sees an existing id and runs:
    ```sql
    UPDATE "MaintenanceIssue"
    SET "Status" = 'Closed', "ResolvedAt" = '2026-10-29 11:40:12', ...
    WHERE "IssueID" = 'ISS-SEED1';
    ```
17. `@Transactional` commits.
18. The response is `200 OK` with:
    ```json
    {
      "issueId": "ISS-SEED1",
      "status": "Closed",
      "resolvedAt": "2026-10-29T11:40:12.345678",
      "priority": "HIGH",
      "train": { "trainId": "TRN-101" }
    }
    ```
19. Swagger shows the updated row.

If Suresh calls resolve again:

- Step 13 now compares `"Closed"` with `"Closed"`.
- `equalsIgnoreCase` returns true.
- The controller throws `IllegalArgumentException("Maintenance issue is already closed")`.
- The global handler returns 400.
- No update happens.

If a passenger tried the same call:

- `@PreAuthorize` would fail before the method body.
- The result would be 403.

---

## 10. JSON request and response examples

### 10.1 Create — request

```http
POST /api/maintenance
Authorization: Basic YWdlbnQxOlBhc3N3MHJkIQ==
Content-Type: application/json
```

```json
{
  "trainId": "TRN-102",
  "stationId": "STN-AND",
  "reportedById": "STAFF-01",
  "issueType": "DOOR",
  "description": "Coach 5 door does not close fully at Andheri platform.",
  "priority": "HIGH"
}
```

| Field | Type | Required | Meaning |
|---|---|---|---|
| `trainId` | string | no | Affected train. Must exist in `Trains`. |
| `stationId` | string | no | Affected station. Must exist in `Stations`. |
| `reportedById` | string | no | Reporter's `UserId`. Must exist in `User`. |
| `issueType` | string | yes | Category. Cannot be blank. |
| `description` | string | yes | Explanation. Cannot be blank. |
| `priority` | string | no | Defaults to `"MEDIUM"`. |

Do not send `issueId`, `status`, `createdAt` or `resolvedAt`. The server controls all four.

Do not send nested `train`, `station` or `reportedBy` objects. Those fields are `READ_ONLY`, so Jackson ignores them on input.

### 10.2 Create — response 201

```json
{
  "issueId": "ISS-7c3d9a21-4b8e-4f10-9d22-6a5b4c3d2e1f",
  "train": {
    "trainId": "TRN-102",
    "trainNumber": "M2",
    "capacity": 1800,
    "totalCoaches": 8,
    "manufactureYear": 2016,
    "isActive": true
  },
  "station": {
    "stationId": "STN-AND",
    "stationCode": "AND",
    "address": "Andheri - Western Express Highway, Andheri East",
    "lineColor": "Blue",
    "isActive": true
  },
  "reportedBy": {
    "userId": "STAFF-01",
    "firstName": "Rahul",
    "lastName": "Mehta",
    "role": "TICKET_AGENT",
    "contact": "agent1"
  },
  "issueType": "DOOR",
  "description": "Coach 5 door does not close fully at Andheri platform.",
  "status": "Open",
  "priority": "HIGH",
  "createdAt": "2026-10-29T11:05:44.123456",
  "resolvedAt": null
}
```

The example is shortened: real nested objects contain every field. `password` never appears because `User.password` is `WRITE_ONLY`.

Notice:

- `status` is `"Open"` even though the request did not send it.
- `priority` is the sent value.
- `resolvedAt` is `null`.
- `trainId`, `stationId` and `reportedById` do **not** appear at the top level, because those transient fields are `WRITE_ONLY`.

### 10.3 Update — request

```http
PUT /api/maintenance/ISS-SEED2
```

```json
{
  "issueType": "LIFT_ESCALATOR",
  "description": "Escalator 2 at Andheri repaired and tested; noise gone.",
  "status": "RESOLVED",
  "priority": "MEDIUM",
  "stationId": "STN-AND"
}
```

`issueType` and `description` are mandatory because of `@NotBlank`. If either is missing, the request fails with a field map.

`status` and `priority` are optional. Omit them to keep the old values.

### 10.4 Update — response 200

```json
{
  "issueId": "ISS-SEED2",
  "issueType": "LIFT_ESCALATOR",
  "description": "Escalator 2 at Andheri repaired and tested; noise gone.",
  "status": "RESOLVED",
  "priority": "MEDIUM",
  "createdAt": "2026-10-29T06:15:30",
  "resolvedAt": null
}
```

This shows the inconsistency: the status says `RESOLVED`, but `resolvedAt` is still null because PUT does not set it.

### 10.5 Resolve — request

```http
PATCH /api/maintenance/ISS-SEED4/resolve
```

No body.

### 10.6 Resolve — response 200

```json
{
  "issueId": "ISS-SEED4",
  "issueType": "FACILITY",
  "description": "Passenger waiting shelter light is out on platform 1.",
  "status": "Closed",
  "priority": "LOW",
  "createdAt": "2026-10-28T11:05:44",
  "resolvedAt": "2026-10-29T11:40:12.345678"
}
```

### 10.7 List by train — 200

```http
GET /api/maintenance/train/TRN-101
```

```json
[
  {
    "issueId": "ISS-SEED1",
    "issueType": "TRAIN",
    "description": "Door sensor on coach 3 intermittently failing to close during peak hours.",
    "status": "OPEN",
    "priority": "HIGH",
    "createdAt": "2026-10-27T11:05:44",
    "resolvedAt": null
  }
]
```

### 10.8 List by status — 200

```http
GET /api/maintenance/status/OPEN
```

Returns an array of the seeded rows whose status is exactly `OPEN`.

`/status/Open` would return only rows created through `POST /api/maintenance`, because that path writes the mixed-case word.

### 10.9 Delete — 204

```http
DELETE /api/maintenance/ISS-SEED4
```

Status `204 No Content`, empty body.

---

## 11. Error table

| When | Status | Body / message |
|---|---|---|
| No `Authorization` header | 401 | Spring Security default |
| Wrong password | 401 | Spring Security default |
| Passenger calls create, update, resolve, list, or status | 403 | Spring Security access denied |
| Non-admin calls delete | 403 | Spring Security access denied |
| `issueType` missing or blank | 400 | `{"issueType": "Issue type is required"}` |
| `description` missing or blank | 400 | `{"description": "Description is required"}` |
| Both missing on PUT | 400 | Both fields appear in the map |
| Unknown issue id on GET/PUT/resolve/delete | 400 | `{"error": "Maintenance issue not found"}` |
| Unknown `trainId` | 400 | `{"error": "Train not found"}` |
| Unknown `stationId` | 400 | `{"error": "Station not found"}` |
| Unknown `reportedById` | 400 | `{"error": "Reporter not found"}` |
| Resolve an already `Closed` issue | 400 | `{"error": "Maintenance issue is already closed"}` |
| Status path with wrong case | 200 | `[]` — exact match found nothing |
| Priority with any spelling | 201 | Accepted; there is no allowed-value check |
| Malformed JSON body | 500 | `{"error": "An unexpected error occurred: ..."}` |
| Unexpected crash | 500 | Same generic 500 shape |

Why "not found" is 400 rather than 404:

- The controller throws `IllegalArgumentException`.
- `GlobalExceptionHandler` maps that to 400.
- Only `ResourceNotFoundException` maps to 404, and this controller never throws it.

Why malformed JSON is 500:

- `HttpMessageNotReadableException` is caught by the catch-all `@ExceptionHandler(Exception.class)`.
- The generic handler returns 500.
- A more precise handler would return 400.

Why the resolve guard does not use a status enum:

- `status` is a `String`.
- There is no enum and no `CHECK` constraint.
- Any spelling is accepted by the database.
- The guard only knows the word `"Closed"`.

---

## 12. Relationships to other groups

```text
Train (04) 1 ──── many MaintenanceIssue (12)
Station (03) 1 ── many MaintenanceIssue (12)
User (01) 1 ───── many MaintenanceIssue (12)   [reporter]
```

| Related group | How it connects |
|---|---|
| Train (file 04) | `MaintenanceIssue.train` is `@ManyToOne` to `Train.TrainID`. Optional. |
| Station (file 03) | `MaintenanceIssue.station` is `@ManyToOne` to `Station.StationID`. Optional. |
| User (file 01) | `MaintenanceIssue.reportedBy` is `@ManyToOne` to `User.UserId`. Optional. |
| Auth and roles (file 02) | `@PreAuthorize` decides who may create, update, list, resolve and delete. |
| Payment / Ticket / MetroCard | No direct link. |
| Schedule / Route / Fare | No direct link, though a train issue indirectly affects schedules. |

What the links allow:

- "Show every issue on train `TRN-101`" → `findByTrain_TrainId`.
- "Show every issue at station `STN-AND`" → `findByStation_StationId`.
- "Show every issue reported by Suresh" → not implemented; would need `findByReportedBy_UserId`.

What happens on delete:

- Deleting a train or station that still has issues is blocked by the foreign key.
- Deleting the reporter user is also blocked.
- Issues must be deleted or reassigned first.

Why the relations are optional:

- A facility problem at a station has no train.
- A rolling-stock problem has no station.
- Forcing both would create meaningless placeholder rows.

---

## 13. Swagger test steps

1. Start the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   Windows:
   ```bat
   mvnw.cmd spring-boot:run
   ```
2. Open `http://localhost:8080/swagger-ui/index.html`.
3. Click **Authorize**.
4. Use `maint1` / `Passw0rd!` for maintenance staff tests.
5. Expand **maintenance-issue-controller**.
6. Test create:
   - `POST /api/maintenance`
   - body:
     ```json
     {
       "trainId": "TRN-102",
       "stationId": "STN-AND",
       "reportedById": "STAFF-01",
       "issueType": "DOOR",
       "description": "Coach 5 door does not close fully.",
       "priority": "HIGH"
     }
     ```
   - expect `201`, status `"Open"`, a generated `ISS-...` id
7. Test validation:
   - same call with `"issueType": ""` and no description
   - expect `400` with a field map for both
8. Test a bad relation:
   - `"trainId": "TRN-999"`
   - expect `400` "Train not found"
9. Test the default priority:
   - omit `priority`
   - expect `"priority": "MEDIUM"`
10. Test update:
    - `PUT /api/maintenance/ISS-SEED2`
    - body with required `issueType` and `description`, plus `"status": "RESOLVED"`
    - expect `200`
11. Test the PUT rule:
    - send only `{"status": "RESOLVED"}`
    - expect `400` with `issueType` and `description` errors
12. Test read:
    - `GET /api/maintenance/ISS-SEED1`
    - expect `200`
13. Test staff list:
    - `GET /api/maintenance`
    - expect `200` with four seeded issues
14. Test filters:
    - `GET /api/maintenance/train/TRN-101` → array with `ISS-SEED1`
    - `GET /api/maintenance/station/STN-AND` → array with `ISS-SEED2`
    - `GET /api/maintenance/status/OPEN` → seeded open issues
    - `GET /api/maintenance/status/Open` → only issues created by POST
15. Test resolve:
    - `PATCH /api/maintenance/ISS-SEED1/resolve`
    - expect `200`, status `"Closed"`, non-null `resolvedAt`
16. Test the repeat guard:
    - call resolve again on `ISS-SEED1`
    - expect `400` "Maintenance issue is already closed"
17. Test role blocking:
    - authorize as `priya` / `Passw0rd!`
    - `GET /api/maintenance` → `403`
    - `PATCH /api/maintenance/ISS-SEED1/resolve` → `403`
    - `GET /api/maintenance/ISS-SEED1` → `200` (single read is open to any logged-in user)
18. Test admin delete:
    - authorize as `admin` / `admin123`
    - `DELETE /api/maintenance/ISS-SEED4` → `204`
    - repeat → `400` "Maintenance issue not found"

---

## 14. How to explain this in 2 minutes

> "The Maintenance group tracks faults. It has one entity, one repository and one controller.
>
> The table is `MaintenanceIssue`. `IssueID` is the primary key, generated as `ISS-` plus a UUID. There are three optional foreign keys: `TrainID`, `StationID` and `ReportedId`. All three are lazy `@ManyToOne` relations. The other fields are `IssueType`, `Description`, `Status`, `Priority`, `CreatedAt` and `ResolvedAt`.
>
> Each relation also has a transient id string: `trainId`, `stationId`, `reportedById`. They are not columns. They let the client send simple ids, and they are `WRITE_ONLY`, so they never appear in responses. The real relation objects are `READ_ONLY`, so input cannot fake a train or a user.
>
> `issueType` and `description` are `@NotBlank`, so they are required on create and on update. `Description` is mapped as `TEXT`. `status` defaults to `"Open"` at the entity level, and `resolvedAt` starts null.
>
> The repository extends `JpaRepository`, which gives `save`, `findById`, `findAll`, `existsById` and `deleteById`. It adds three derived queries: by train, by station and by status. The first two use an underscore to go inside the relation, for example `findByTrain_TrainId` becomes `WHERE "TrainID" = ?`. The status query is an exact match.
>
> The controller has nine endpoints. Create is for admin, maintenance staff and ticket agents. It ignores any client id and timestamp, always sets status to `"Open"`, defaults priority to `"MEDIUM"`, and resolves the three relations through `applyRelations`. Update is a PUT, so `issueType` and `description` must be resent. Read one is open to any logged-in user. List all and list by status are staff-only. List by train and station are open to any logged-in user. Resolve is a PATCH that checks for the word `"Closed"`, then sets status `"Closed"` and `resolvedAt` to now. Delete is admin-only and returns 204.
>
> The honest issues: status words are inconsistent — the seeder uses uppercase `OPEN`, `IN_PROGRESS`, `RESOLVED`, create writes `"Open"`, and resolve writes `"Closed"`. So the status filter is case-sensitive and can look confusing. Also, PUT can set status to `"RESOLVED"` without setting `resolvedAt`. And the reporter is taken from the body, not from the logged-in user."

---

## 15. Beginner Q&A

**Q1. What is an entity?**
A Java class mapped to a database table. `MaintenanceIssue` maps to `"MaintenanceIssue"`.

**Q2. What does `@Entity` do?**
It tells JPA to manage the class as a table.

**Q3. What does `@Table(name = "\"MaintenanceIssue\"")` do?**
It sets the exact table name. The escaped quotes preserve capital letters in PostgreSQL.

**Q4. What is the primary key?**
`issueId`, stored in `IssueID`.

**Q5. What are the foreign keys?**
`TrainID`, `StationID` and `ReportedId`.

**Q6. Why is `ReportedId` spelled that way?**
That is the literal column name in the SQL migration. The Java field is `reportedBy`, so `@JoinColumn` maps one to the other.

**Q7. Why are the relations optional?**
A station problem has no train, and a train problem may have no station. Both columns allow null.

**Q8. What does `@ManyToOne` mean?**
Many issues can point to one train, one station or one reporter.

**Q9. What does `FetchType.LAZY` do?**
It loads the related row only when it is read. This avoids loading three extra rows for every issue in a list.

**Q10. What is `@Transient`?**
"Not a database column."

**Q11. Why are there three transient id fields?**
So the client can send `trainId`, `stationId` and `reportedById` instead of nested objects.

**Q12. What does `WRITE_ONLY` do?**
It accepts the field in JSON input and hides it from JSON output.

**Q13. What does `READ_ONLY` do on `train`?**
It shows the train in responses but ignores any nested train object sent in a request.

**Q14. How does the server turn `trainId` into a real `Train`?**
`applyRelations` calls `trainRepository.findById(trainId)` and then `issue.setTrain(train)`.

**Q15. What happens if the train id does not exist?**
`orElseThrow` raises `IllegalArgumentException("Train not found")`, which becomes 400.

**Q16. What does `@NotBlank` check?**
That the value is not null, not empty, and not only whitespace.

**Q17. Which fields are required?**
`issueType` and `description`, on both create and update.

**Q18. Why is `description` mapped with `columnDefinition = "TEXT"`?**
To match the PostgreSQL `TEXT` column. Without it, Hibernate would expect `VARCHAR(255)` and schema validation could fail.

**Q19. What is the default status?**
`"Open"` in the entity and in the create method.

**Q20. What is the default priority?**
The entity has none. The create method uses `"MEDIUM"`.

**Q21. Why does the create method build a new object instead of saving `request`?**
To control the id, status and timestamps, and to stop the client from setting them.

**Q22. Can a passenger report a maintenance issue?**
No. Create allows only `ADMIN`, `MAINTENANCE_STAFF` and `TICKET_AGENT`.

**Q23. Can a ticket agent list all issues?**
No. List all allows only `ADMIN` and `MAINTENANCE_STAFF`. Agents can create but not list everything.

**Q24. Can a passenger read one issue by id?**
Yes, if they are logged in. `getMaintenanceIssueById` has no `@PreAuthorize`. This is an inconsistency worth mentioning.

**Q25. What does `@PutMapping` mean here?**
A full update. The client must send `issueType` and `description` again.

**Q26. What happens if I PUT only a status?**
`@Valid` fails on the two missing required fields and returns 400 with a field map.

**Q27. Why are status and priority wrapped in `if != null`?**
So an omitted value keeps the old value. PUT still requires the other two fields.

**Q28. Can I remove a train from an issue?**
No. `applyRelations` only sets a relation when the id is present. Sending null leaves the old relation.

**Q29. What does `findByTrain_TrainId` mean?**
"Find issues whose train's `trainId` equals the argument." The underscore goes inside the related object.

**Q30. What SQL does `findByStatus` generate?**
`SELECT * FROM "MaintenanceIssue" WHERE "Status" = ?`.

**Q31. Is the status match case-sensitive?**
Yes, in PostgreSQL. `OPEN` and `Open` are different strings.

**Q32. Why is that a problem?**
The seeder uses uppercase statuses and the create endpoint uses `"Open"`, so one filter value cannot find both.

**Q33. What does resolve do?**
It checks the status, sets it to `"Closed"`, stamps `resolvedAt` with the current time, and saves.

**Q34. Why `"Closed".equalsIgnoreCase(...)` instead of `status.equals(...)`?**
Because `status` could be null. Putting the constant first is null-safe. It also accepts any letter case.

**Q35. What happens if I resolve twice?**
The second call returns 400 "Maintenance issue is already closed".

**Q36. Does resolve work on a `RESOLVED` issue?**
Yes. The guard only blocks `"Closed"`, so `"RESOLVED"` is changed to `"Closed"`.

**Q37. Does PUT set `resolvedAt`?**
No. PUT can set the status text but never touches `resolvedAt`. Only resolve sets it.

**Q38. Why is there a separate resolve endpoint instead of just PUT?**
It enforces the rule and sets the timestamp in one place. It also avoids requiring the whole body.

**Q39. Why is resolve a PATCH?**
It changes part of the resource and needs no body. The action is named in the URL.

**Q40. Who can delete an issue?**
Only `ADMIN`.

**Q41. What is the difference between resolve and delete?**
Resolve keeps the row with status `"Closed"`. Delete removes it permanently.

**Q42. What happens if I delete a train with issues?**
The foreign key blocks it. The issues must be deleted or reassigned first.

**Q43. What happens if I filter by an unknown train id?**
You get `200` with `[]`, not 404. Filter endpoints do not check existence.

**Q44. Why is a missing issue 400 and not 404?**
The controller throws `IllegalArgumentException`, and the global handler maps that to 400.

**Q45. Why is malformed JSON a 500?**
The catch-all `@ExceptionHandler(Exception.class)` handles `HttpMessageNotReadableException` and returns 500.

**Q46. Does the reporter come from the logged-in user?**
No. It comes from `reportedById` in the body. If omitted, `reportedBy` stays null.

**Q47. Why is that a weakness?**
A user could claim someone else reported an issue. A stricter design would use `Authentication` for non-admins.

**Q48. Are `issueType` and `priority` enums?**
No. They are plain strings with no allowed-value check. A typo is stored as-is.

**Q49. Why 201 on create and 204 on delete?**
201 means a new row was created. 204 means success with no body to return.

**Q50. What does `ResponseEntity.ok(...)` do?**
It builds a response with status 200 and the given object as the JSON body.

---

## 16. Glossary

| Term | Meaning |
|---|---|
| **Annotation** | A `@word` that adds behaviour or metadata. |
| **Bean** | An object created and managed by Spring. |
| **Controller** | Class that handles HTTP requests. |
| **Derived query** | Method name that Spring converts into SQL. |
| **Dependency injection** | Spring passes dependencies into a constructor. |
| **Entity** | Class mapped to a database table. |
| **Fetch type** | When related data is loaded: now or later. |
| **Foreign key** | Column pointing to another table's primary key. |
| **Hibernate** | JPA implementation that generates SQL. |
| **HTTP Basic** | Login by sending credentials with each request. |
| **Idempotent** | Repeating an operation leaves the same end state. |
| **Jackson** | Library that converts Java and JSON. |
| **JPA** | Java standard for object-to-table mapping. |
| **JSON** | Text format for structured data. |
| **`LocalDateTime`** | Date with time, no time zone. |
| **Nullable** | Whether a column may store `NULL`. |
| **`Optional`** | Box that may contain zero or one value. |
| **PATCH** | HTTP method for a partial change or action. |
| **Primary key** | Column that uniquely identifies a row. |
| **Priority** | Urgency label on an issue. |
| **PUT** | HTTP method for a full replacement update. |
| **`READ_ONLY`** | JSON output only. |
| **Reporter** | User who recorded the issue. |
| **Repository** | Interface providing database operations. |
| **Resolve** | Mark fixed and record the time. |
| **Role** | Label used for permissions. |
| **Rollback** | Undo all changes in a failed transaction. |
| **Status** | Lifecycle stage of an issue. |
| **Swagger UI** | Web page for testing the API. |
| **`TEXT`** | PostgreSQL type for long strings. |
| **Transaction** | All-or-nothing database unit. |
| **`@Transient`** | Not a database column. |
| **UUID** | Long random unique string. |
| **`WRITE_ONLY`** | JSON input only. |

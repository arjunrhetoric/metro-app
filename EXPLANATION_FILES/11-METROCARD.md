# 11 — Metro Card Group (Stored-value travel card)

---

## 1. Real-life analogy

Think of a metro card as a **gift card for travel**.

You buy the plastic card once. It has:

- a card number printed on it
- an owner (you)
- a money balance inside
- an issue date
- an expiry date
- an on/off switch (active or blocked)

When the balance is low, you **top up**: you add money at a machine or counter. The balance goes up. The card number does not change. The owner does not change.

When you lose the card, the staff **deactivate** it. The card still exists in the system, but it cannot be used or topped up. That is different from **deleting** it, which removes the row entirely.

One person should not hold two active cards at the same time. That is the project's rule, and the code enforces it when a card is created.

Important: in this project a metro card is **not** used to pay for tickets. Ticket payment is the separate Payment group. The card is a stored balance that the app tracks. Be honest about this if an evaluator asks.

---

## 2. Words you need

| Word | One simple sentence |
|---|---|
| HTTP | The protocol a client uses to talk to the server. |
| Endpoint | One URL plus one method, e.g. `POST /api/cards/{id}/topup`. |
| JSON | Text format for data, like `{"balance": 250.00}`. |
| Status code | A number describing the result: 200, 201, 204, 400, 401, 403, 500. |
| Spring Boot | The framework that starts the server and connects the parts. |
| Annotation | A `@word` that tells Spring, JPA or Jackson how to treat something. |
| Entity | A Java class that maps to a database table. |
| Repository | An interface that provides database operations without hand-written SQL. |
| Controller | A class whose methods handle HTTP requests. |
| JPA | The Java standard for mapping objects to tables. |
| Hibernate | The library that implements JPA and generates SQL. |
| Dependency injection | Spring creates objects and passes them into a constructor. |
| Bean | An object managed by Spring. |
| `@RestController` | "This class returns JSON for HTTP requests." |
| `@RequestMapping` | Sets the URL prefix for all methods in the class. |
| `@PostMapping` | Handles POST (create or action). |
| `@GetMapping` | Handles GET (read). |
| `@PatchMapping` | Handles PATCH (partial change). |
| `@DeleteMapping` | Handles DELETE (remove). |
| `@PathVariable` | Reads a value from the URL path. |
| `@RequestParam` | Reads a value from the query string, e.g. `?amount=100`. |
| `@RequestBody` | Converts the JSON body into a Java object. |
| `@Valid` | Runs bean validation before the method body. |
| `@PreAuthorize` | Checks roles before the method body. |
| `@Transactional` | Makes the method one all-or-nothing database unit. |
| `Authentication` | Spring's object holding the logged-in user's name and roles. |
| `ResponseEntity` | Wrapper for status code plus body. |
| `@Entity` | Marks a class as a table. |
| `@Table` | Sets the exact table name. |
| `@Id` | Marks the primary key. |
| `@Column` | Sets the exact column name, precision or length. |
| `@ManyToOne` | Many cards can point to one user. |
| `@JoinColumn` | Names the foreign-key column. |
| `@Transient` | "Do not save this field as a column." |
| `@JsonProperty` | Controls JSON input and output for a field. |
| `READ_ONLY` | Shown in responses, ignored in requests. |
| `WRITE_ONLY` | Accepted in requests, hidden from responses. |
| `FetchType.LAZY` | Load the related user only when it is read. |
| `BigDecimal` | Exact decimal class used for money. |
| `LocalDate` | A date without a time, e.g. `2027-10-29`. |
| `LocalDateTime` | A date with a time, e.g. `2026-10-29T10:15:30`. |
| `UUID` | A long random string, very unlikely to repeat. |
| Primary key | The column that uniquely identifies a row. |
| Foreign key | A column that points to another table's primary key. |
| Derived query | A repository method whose name Spring turns into SQL. |
| Transaction | A group of changes that all succeed or all roll back. |
| Top-up | Adding money to the card balance. |
| Deactivate | Mark the card unusable without deleting it. |
| Ownership check | Code that ensures a passenger touches only their own card. |
| Role | A user label: `ADMIN`, `PASSENGER`, `TICKET_AGENT`, `MAINTENANCE_STAFF`. |
| HTTP Basic | Login style that sends username and password with each request. |
| Swagger UI | A web page for trying the API by clicking buttons. |

---

## 3. Files in this group

| Role | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/MetroCard.java` |
| Controller | `src/main/java/com/metro/ticketing/controller/MetroCardController.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/MetroCardRepository.java` |
| Table definition | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Error handling | `src/main/java/com/metro/ticketing/exception/GlobalExceptionHandler.java` |
| Demo data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` |

Supporting files:

- `src/main/java/com/metro/ticketing/entity/User.java`
- `src/main/java/com/metro/ticketing/repository/UserRepository.java`
- `src/main/java/com/metro/ticketing/config/SecurityConfig.java`
- `src/main/java/com/metro/ticketing/config/DbUserDetailsService.java`

---

## 4. Database table

From `V1__create_metro_ticketing_schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS "MetroCards" (
    "CardID" VARCHAR(50) PRIMARY KEY,
    "PassengerID" VARCHAR(50) NOT NULL,
    "Balance" NUMERIC(10,2) DEFAULT 0,
    "IssueDate" DATE,
    "ExpiryDate" DATE,
    "CreatedAt" TIMESTAMP,
    "isActive" BOOLEAN DEFAULT TRUE,
    CONSTRAINT "FK_MetroCard_Passenger"
        FOREIGN KEY ("PassengerID") REFERENCES "User" ("UserId")
);
```

Column by column:

| Column | Type | Meaning |
|---|---|---|
| `CardID` | `VARCHAR(50)`, primary key | Unique card id. The app builds it as `CARD-` + a UUID. |
| `PassengerID` | `VARCHAR(50)`, not null, foreign key | The owner. Points to `User.UserId`. |
| `Balance` | `NUMERIC(10,2)`, default 0 | Money stored on the card. |
| `IssueDate` | `DATE` | The day the card was issued. |
| `ExpiryDate` | `DATE` | The last day the card should work. |
| `CreatedAt` | `TIMESTAMP` | When the row was inserted. |
| `isActive` | `BOOLEAN`, default true | `true` = usable, `false` = blocked. |

Things to notice:

- `PassengerID` is `NOT NULL`, so every card has an owner.
- `Balance` has a database default of `0`, but it is nullable in the DDL.
- `isActive` is the only column whose name starts with lowercase `i`.
- The Java field is `isActive`, and the column name is written as `"isActive"` to match exactly.
- There is no uniqueness constraint on `PassengerID`. The "one active card" rule is enforced only by the controller, not by the database.

That last point is important for an evaluator. Two requests arriving at the same moment could both pass the check. A database unique index would be stronger. The project uses the controller check only.

---

## 5. Entity line by line

File: `src/main/java/com/metro/ticketing/entity/MetroCard.java`

Comments after `//` are added for teaching. The real file has none.

```java
package com.metro.ticketing.entity;                                      // this class is in the entity package

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;            // Jackson = Java <-> JSON library
import com.fasterxml.jackson.annotation.JsonProperty;                    // per-field JSON control
import jakarta.persistence.*;                                            // JPA annotations

import java.math.BigDecimal;                                             // exact decimal for money
import java.time.LocalDate;                                              // date only, no time
import java.time.LocalDateTime;                                          // date + time

@Entity                                                                  // "map this class to a table"
@Table(name = "\"MetroCards\"")                                          // exact table name is MetroCards
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})           // hide Hibernate proxy internals from JSON
public class MetroCard {

    @Id                                                                  // primary key
    @Column(name = "\"CardID\"", length = 50)                            // column CardID, max 50 chars
    private String cardId;                                               // e.g. "CARD-8a7b..."

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)                // output only; input cannot set it
    @ManyToOne(fetch = FetchType.LAZY)                                   // many cards -> one user
    @JoinColumn(name = "\"PassengerID\"",                                // foreign-key column on this table
                referencedColumnName = "\"UserId\"",                     // points to User.UserId
                nullable = false)                                        // every card needs an owner
    private User passenger;                                              // the owner object

    @Transient                                                           // not a database column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)               // read "passengerId" from JSON, hide in output
    private String passengerId;                                          // helper field for input only

    @Column(name = "\"Balance\"", precision = 10, scale = 2)             // column Balance; 10 digits, 2 decimals
    private BigDecimal balance = BigDecimal.ZERO;                        // starts at 0.00, never null for new objects

    @Column(name = "\"IssueDate\"")                                      // column IssueDate
    private LocalDate issueDate = LocalDate.now();                       // default = today

    @Column(name = "\"ExpiryDate\"")                                     // column ExpiryDate
    private LocalDate expiryDate;                                        // no default here; controller sets it

    @Column(name = "\"CreatedAt\"")                                      // column CreatedAt
    private LocalDateTime createdAt = LocalDateTime.now();               // default = now

    @Column(name = "\"isActive\"")                                       // column isActive (lowercase i)
    private Boolean isActive = true;                                     // new cards start active

    // ... getters and setters ...
}
```

### The getters and setters

```java
public String getCardId() { return cardId; }
public void setCardId(String cardId) { this.cardId = cardId; }

public User getPassenger() { return passenger; }
public void setPassenger(User passenger) { this.passenger = passenger; }

public BigDecimal getBalance() { return balance; }
public void setBalance(BigDecimal balance) { this.balance = balance; }

public LocalDate getIssueDate() { return issueDate; }
public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

public LocalDate getExpiryDate() { return expiryDate; }
public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

public LocalDateTime getCreatedAt() { return createdAt; }
public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

public Boolean getIsActive() { return isActive; }
public void setIsActive(Boolean isActive) { this.isActive = isActive; }

public String getPassengerId() { return passengerId; }
public void setPassengerId(String passengerId) { this.passengerId = passengerId; }
```

Why the getter is named `getIsActive` and not `isActive`:

- The field type is `Boolean` (the wrapper class), not `boolean`.
- The usual JavaBean naming rule for a `boolean` field named `active` is `isActive()`.
- But the field here is literally named `isActive`.
- Spring and Jackson look for `getIsActive()` to match the property `isActive`.
- Renaming it to `isActive()` would confuse the property name and can break JSON output.

Why there is no `@NotNull` on `balance`:

- The project does not validate the starting balance.
- `@Valid` in the controller therefore does nothing for this entity.
- A negative starting balance would be accepted.
- This is a real gap. A stronger design would add `@DecimalMin("0.00")` and a `@NotNull`.
- Say this honestly if the evaluator asks about validation.

### Why `passenger` is `READ_ONLY`

The client must send a passenger **id**, not a full user object. If the nested user object were writable, a client could try to control user fields.

On output, the full owner object is still shown, so the response is readable.

### Why `passengerId` is `@Transient` plus `WRITE_ONLY`

Two separate jobs:

| Annotation | Job |
|---|---|
| `@Transient` | Do not create a `passengerId` column. |
| `WRITE_ONLY` | Accept `passengerId` in JSON, but do not print it back. |

Without `@Transient`, JPA would try to map a column that already exists as `PassengerID` through `passenger`, causing a duplicate mapping.

Without `WRITE_ONLY`, the response would contain both `passenger` and a redundant `passengerId`.

### Why `BigDecimal.ZERO`

```java
private BigDecimal balance = BigDecimal.ZERO;
```

- It gives every new object a safe starting value.
- It avoids a `NullPointerException` when the controller does `balance.add(amount)`.
- `BigDecimal.ZERO` is a constant, not `new BigDecimal("0")`.

Note: `BigDecimal` is immutable. `balance.add(amount)` does not change `balance`; it returns a **new** object. The controller must call `setBalance(...)` with the result.

### Why `LocalDate` for issue and expiry, but `LocalDateTime` for creation

- Issue and expiry are calendar days. A card expires at the end of a day, not at a precise second.
- `CreatedAt` is an audit timestamp, so the time of day matters.
- This matches the database types: `DATE` for the first two, `TIMESTAMP` for the last.

### Why `Boolean` instead of `boolean`

- `Boolean` can be `null`.
- A database column can be null, so the wrapper matches the database more honestly.
- The controller therefore uses a null-safe check: `Boolean.TRUE.equals(card.getIsActive())`.

If the field were a primitive `boolean`, it could never be null, but a null database value would fail to load.

---

## 6. Repository line by line

File: `src/main/java/com/metro/ticketing/repository/MetroCardRepository.java`

```java
package com.metro.ticketing.repository;

import com.metro.ticketing.entity.MetroCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository                                                                        // Spring bean marker
public interface MetroCardRepository extends JpaRepository<MetroCard, String> {    // entity MetroCard, id String
    List<MetroCard> findByPassenger_UserId(String passengerId);                    // all cards of one user
    Optional<MetroCard> findByPassenger_UserIdAndIsActiveTrue(String passengerId); // the single active card
}
```

### Free methods from `JpaRepository`

| Method | What it does | SQL Spring runs |
|---|---|---|
| `save(card)` | Insert a new card or update an existing one | `INSERT INTO "MetroCards" (...)` or `UPDATE "MetroCards" SET ... WHERE "CardID" = ?` |
| `findById(id)` | Find one card by primary key | `SELECT * FROM "MetroCards" WHERE "CardID" = ?` |
| `findAll()` | All cards | `SELECT * FROM "MetroCards"` |
| `existsById(id)` | Does the card exist? | `SELECT COUNT(*) > 0 FROM "MetroCards" WHERE "CardID" = ?` |
| `deleteById(id)` | Remove one card | `DELETE FROM "MetroCards" WHERE "CardID" = ?` |
| `count()` | Number of cards | `SELECT COUNT(*) FROM "MetroCards"` |

`save` decides insert vs. update by checking whether the id already exists in the persistence context or the table. In this project the id is always set by hand before the first save, so a new card is an insert.

### Derived query 1: all cards of a passenger

```java
List<MetroCard> findByPassenger_UserId(String passengerId);
```

Spring reads it as:

| Piece | Meaning |
|---|---|
| `findBy` | SELECT and return a list |
| `Passenger` | the `passenger` field of `MetroCard` |
| `_` | "go inside that related object" |
| `UserId` | the `userId` field of `User` |

Generated SQL:

```sql
SELECT *
FROM "MetroCards"
WHERE "PassengerID" = ?;
```

The underscore is essential. `findByPassengerId` would look for a field named `passengerId` on `MetroCard`. That field is `@Transient`, so it has no column, and Spring would fail at startup.

### Derived query 2: the active card of a passenger

```java
Optional<MetroCard> findByPassenger_UserIdAndIsActiveTrue(String passengerId);
```

Pieces:

| Piece | Meaning |
|---|---|
| `findBy` | SELECT |
| `Passenger_UserId` | the owner's `userId` |
| `And` | add another condition |
| `IsActiveTrue` | the `isActive` property must be `true` |

Generated SQL:

```sql
SELECT *
FROM "MetroCards"
WHERE "PassengerID" = ?
  AND "isActive" = TRUE;
```

Why `Optional` and not `List`:

- The business rule says at most one active card per passenger.
- The controller only needs to know "is there one?".
- `Optional` expresses "zero or one" clearly.
- `.isPresent()` is then a readable check.

Why the name uses `IsActiveTrue`:

- `True` is a Spring Data keyword that appends `= true`.
- The property is `isActive`, so the method segment is `IsActive` + `True`.
- You could also write `findByPassenger_UserIdAndIsActive(String id, Boolean active)`, but the fixed `True` version is shorter at the call site.

---

## 7. Controller endpoints

Base path: `/api/cards`.

| Method | Full path | Who is allowed | Purpose | Success |
|---|---|---|---|---|
| POST | `/api/cards` | `PASSENGER`, `TICKET_AGENT`, `ADMIN` | Issue a new card | 201 + card JSON |
| POST | `/api/cards/{id}/topup?amount=100.00` | `PASSENGER`, `TICKET_AGENT`, `ADMIN` | Add money | 200 + updated card |
| GET | `/api/cards/{id}` | any logged-in user, ownership enforced for passengers | Read one card | 200 + card JSON |
| GET | `/api/cards` | `ADMIN`, `TICKET_AGENT` | List all cards | 200 + array |
| GET | `/api/cards/passenger/{passengerId}` | any logged-in user; passengers are forced to their own id | List a passenger's cards | 200 + array |
| PATCH | `/api/cards/{id}/deactivate` | `ADMIN`, `TICKET_AGENT` | Set `isActive = false` | 200 + card JSON |
| DELETE | `/api/cards/{id}` | `ADMIN` | Delete the row | 204, empty body |

Note the two different "create" ideas:

- **Issue a card** = `POST /api/cards`.
- **Add money** = `POST /api/cards/{id}/topup`.

Both use POST because both change data. POST is not only for creation in HTTP.

---

## 8. Controller line by line

File: `src/main/java/com/metro/ticketing/controller/MetroCardController.java`

### 8.1 Class header and constructor

```java
@RestController                                                       // return JSON, not HTML
@RequestMapping("/api/cards")                                          // URL prefix for every method
public class MetroCardController {

    private final MetroCardRepository metroCardRepository;             // card table access
    private final UserRepository userRepository;                       // owner lookup

    public MetroCardController(MetroCardRepository metroCardRepository,
                               UserRepository userRepository) {        // Spring injects both
        this.metroCardRepository = metroCardRepository;
        this.userRepository = userRepository;
    }
```

Why only two repositories:

- The controller never needs tickets, fares or payments.
- It only needs cards and users.
- Keeping dependencies small makes the class easier to test and explain.

### 8.2 Issue a new card

```java
@PostMapping                                                           // POST /api/cards
@PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT', 'ADMIN')")      // all three roles may issue
@Transactional                                                         // one transaction
public ResponseEntity<MetroCard> createMetroCard(
        @Valid @RequestBody MetroCard request,                         // JSON -> MetroCard; @Valid runs, but this entity has no rules
        Authentication authentication) {

    String passengerId = request.getPassengerId();                     // start with what the client sent

    if (isPassenger(authentication)) {                                 // if the caller is a passenger
        passengerId = currentUser(authentication).getUserId();        // ignore the client value; force own id
    }

    if (passengerId == null || passengerId.isBlank()) {                // agents/admins must supply one
        throw new IllegalArgumentException("Passenger ID is required");
    }

    User passenger = userRepository.findById(passengerId)              // the owner must exist
            .orElseThrow(() -> new IllegalArgumentException("Passenger not found"));

    if (metroCardRepository                                           // is there already an active card?
            .findByPassenger_UserIdAndIsActiveTrue(passengerId)
            .isPresent()) {
        throw new IllegalArgumentException("Passenger already has an active metro card");
    }

    MetroCard card = new MetroCard();                                  // build a new row
    card.setCardId("CARD-" + UUID.randomUUID());                       // server-generated id
    card.setPassenger(passenger);                                      // real owner object
    card.setBalance(request.getBalance() != null                       // opening balance:
            ? request.getBalance() : BigDecimal.ZERO);                 // use sent value, else 0.00
    card.setIssueDate(LocalDate.now());                                // today
    card.setCreatedAt(LocalDateTime.now());                            // now
    card.setIsActive(true);                                            // new cards are usable
    card.setExpiryDate(request.getExpiryDate() != null                 // expiry:
            ? request.getExpiryDate()                                  // use sent date,
            : LocalDate.now().plusYears(5));                           // else five years from today

    return ResponseEntity.status(HttpStatus.CREATED)                   // 201
            .body(metroCardRepository.save(card));                    // INSERT and return JSON
}
```

The `passengerId` override is the key security idea:

```java
if (isPassenger(authentication)) {
    passengerId = currentUser(authentication).getUserId();
}
```

- A passenger cannot create a card for someone else, even if they send another id.
- Their own id silently wins.
- Agents and admins keep the id they sent.
- This is different from throwing 403. It quietly corrects the input. If asked, say: "The code prefers the authenticated identity over the request value."

Why `@Valid` is present but does nothing useful:

- `@Valid` asks Spring to check constraints on `MetroCard`.
- The entity has no `@NotNull`, `@NotBlank` or `@DecimalMin`.
- So no constraint fires, and `balance` can even be negative.
- This is a design gap, not a bug in the wiring.

Why a duplicate active card is rejected:

- The rule "one active card per passenger" is enforced with a query.
- It only counts cards where `isActive = true`.
- A passenger with an old inactive card can get a new active one.
- The seeder shows exactly this: Priya has `CARD-SEED1` active and `CARD-SEED2` inactive.

Why the opening balance is allowed:

- An agent may issue a card that already has money on it.
- If the client sends no balance, it defaults to `0.00`.
- There is no minimum or maximum check.

Why expiry defaults to five years:

```java
LocalDate.now().plusYears(5)
```

- `LocalDate` is immutable, so this returns a new date.
- The client may send a different date.
- No check rejects a past date. A card could be created already expired.
- The top-up endpoint would then reject it with "Metro card has expired".

### 8.3 Top up a card

```java
@PostMapping("/{id}/topup")                                            // POST /api/cards/CARD-X/topup?amount=100
@PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT', 'ADMIN')")
@Transactional
public ResponseEntity<MetroCard> topUpCard(
        @PathVariable String id,                                       // card id from the path
        @RequestParam BigDecimal amount,                               // money from the query string
        Authentication authentication) {

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {    // must be positive
        throw new IllegalArgumentException("Top-up amount must be greater than zero");
    }

    MetroCard card = metroCardRepository.findById(id)                  // find the card
            .orElseThrow(() -> new IllegalArgumentException("Metro card not found"));

    checkOwnership(card, authentication);                              // passengers only top up their own

    if (!Boolean.TRUE.equals(card.getIsActive())) {                    // null-safe "is it false or null?"
        throw new IllegalArgumentException("Metro card is inactive");
    }

    if (card.getExpiryDate() != null                                 // if there is an expiry date
            && card.getExpiryDate().isBefore(LocalDate.now())) {       // and it is before today
        throw new IllegalArgumentException("Metro card has expired");
    }

    card.setBalance(card.getBalance().add(amount));                    // new balance = old + amount
    return ResponseEntity.ok(metroCardRepository.save(card));          // UPDATE, 200 + card JSON
}
```

Why `amount` comes from `@RequestParam` and not `@RequestBody`:

- A top-up is a single number, so a full JSON object is unnecessary.
- The URL becomes self-describing: `?amount=100.00`.
- Swagger shows a simple input box.
- The downside: money appears in the URL, which would be logged. For real payments, a body would be better.

Why `compareTo(BigDecimal.ZERO) <= 0`:

- `compareTo` returns `0` for equal, negative for smaller, positive for larger.
- `<= 0` rejects zero and negatives.
- `BigDecimal` must not be compared with `==` or `equals` for value checks.

Why `Boolean.TRUE.equals(...)`:

- `card.getIsActive()` may be `null`.
- Calling `.equals(...)` on a null would throw `NullPointerException`.
- `Boolean.TRUE.equals(x)` is null-safe and returns `false` for null.

Why the expiry check uses `isBefore(LocalDate.now())`:

- If expiry is yesterday, it is before today → expired.
- If expiry is today, it is **not** before today → still valid.
- So the card works through the whole expiry day.

Why the balance is updated by replacement:

```java
card.setBalance(card.getBalance().add(amount));
```

- `add` returns a new `BigDecimal`.
- Without `setBalance`, the result would be thrown away.
- Hibernate detects the changed field and writes it on commit.

Why top-up is not linked to Payment:

- `Payment` points to a `Ticket`, not a card.
- There is no card-payment table.
- So a top-up leaves no separate audit row. The new balance is the only record.
- If asked "where is the top-up history?", the honest answer is: "There is none in this version."

### 8.4 Read one card

```java
@GetMapping("/{id}")                                                   // GET /api/cards/CARD-SEED1
@Transactional(readOnly = true)                                        // no writes expected
public ResponseEntity<MetroCard> getMetroCardById(
        @PathVariable String id,
        Authentication authentication) {

    MetroCard card = metroCardRepository.findById(id)                  // look up
            .orElseThrow(() -> new IllegalArgumentException("Metro card not found"));

    checkOwnership(card, authentication);                              // passengers only see their own
    return ResponseEntity.ok(card);                                    // 200 + JSON
}
```

### 8.5 List every card

```java
@GetMapping                                                            // GET /api/cards
@PreAuthorize("hasAnyRole('ADMIN', 'TICKET_AGENT')")                   // staff only
@Transactional(readOnly = true)
public ResponseEntity<List<MetroCard>> getAllMetroCards() {
    return ResponseEntity.ok(metroCardRepository.findAll());           // 200 + array
}
```

Passengers are blocked because the full list would expose other people's balances and ids.

### 8.6 List cards of one passenger

```java
@GetMapping("/passenger/{passengerId}")                                // GET /api/cards/passenger/PASS-SEED1
@Transactional(readOnly = true)
public ResponseEntity<List<MetroCard>> getMetroCardsByPassenger(
        @PathVariable String passengerId,
        Authentication authentication) {

    if (isPassenger(authentication)) {                                 // if caller is a passenger
        passengerId = currentUser(authentication).getUserId();         // replace the path value with own id
    }

    return ResponseEntity.ok(metroCardRepository.findByPassenger_UserId(passengerId)); // 200 + array
}
```

Subtle behaviour:

- A passenger who asks for another passenger's id does **not** get 403.
- The id is silently replaced with their own.
- They get `200` with their own cards.
- That is safe, but it can confuse a tester. Document it.
- Staff keep the requested id, so they can inspect anyone.

There is no "passenger not found" check here. An unknown id simply returns `[]`.

### 8.7 Deactivate a card

```java
@PatchMapping("/{id}/deactivate")                                      // PATCH /api/cards/CARD-SEED3/deactivate
@PreAuthorize("hasAnyRole('ADMIN', 'TICKET_AGENT')")                   // staff only
@Transactional
public ResponseEntity<MetroCard> deactivateMetroCard(@PathVariable String id) {

    MetroCard card = metroCardRepository.findById(id)                  // find
            .orElseThrow(() -> new IllegalArgumentException("Metro card not found"));

    card.setIsActive(false);                                           // flip the flag
    return ResponseEntity.ok(metroCardRepository.save(card));          // UPDATE, 200 + JSON
}
```

Why PATCH and not PUT:

- PATCH means "change part of this resource".
- Only one field changes: `isActive`.
- The balance, owner and dates are untouched.

Why there is no "already inactive" error:

- Calling it twice just sets `false` again.
- The operation is idempotent: repeating it gives the same end state.
- It still returns `200` both times.

Why deactivate instead of delete:

- Deleting loses history and breaks any foreign key that might point at the card later.
- Deactivating keeps the row and blocks use.
- The controller checks `isActive` on top-up, so a deactivated card cannot receive money.
- Reading a deactivated card still works, which is useful for support staff.

### 8.8 Delete a card

```java
@DeleteMapping("/{id}")                                                // DELETE /api/cards/CARD-SEED2
@PreAuthorize("hasRole('ADMIN')")                                      // admin only
@Transactional
public ResponseEntity<Void> deleteMetroCard(@PathVariable String id) {

    if (!metroCardRepository.existsById(id)) {                         // avoid silent delete
        throw new IllegalArgumentException("Metro card not found");
    }

    metroCardRepository.deleteById(id);                                // DELETE FROM "MetroCards" ...
    return ResponseEntity.noContent().build();                         // 204, no body
}
```

`ResponseEntity<Void>` plus `noContent()` means "success, and there is nothing to return".

### 8.9 Ownership helper

```java
private void checkOwnership(MetroCard card, Authentication authentication) {
    if (isPassenger(authentication)                                    // only passengers are restricted
            && !card.getPassenger().getUserId()                        // card owner
                    .equals(currentUser(authentication).getUserId())) { // logged-in user
        throw new AccessDeniedException("You can only access your own metro cards");
    }
}
```

- Agents and admins skip the check.
- Passengers must match the owner id exactly.
- The message is replaced by the global handler with a generic 403 text.

### 8.10 Role helpers

```java
private boolean isPassenger(Authentication authentication) {
    return authentication.getAuthorities().stream()                    // authorities list
            .anyMatch(authority -> authority.getAuthority()             // each authority string
                    .equals("ROLE_PASSENGER"));                          // Spring's prefix
}

private User currentUser(Authentication authentication) {
    return userRepository.findByContact(authentication.getName())       // login name = contact
            .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
}
```

`authentication.getName()` is the contact because `DbUserDetailsService` builds the Spring user with `.withUsername(user.getContact())`.

---

## 9. One request from start to finish

Real seeded data:

- Card: `CARD-SEED3`
- Owner: Arjun Rao, `UserId = PASS-SEED2`, contact `arjun`, password `Passw0rd!`
- Balance before: `120.00`
- Top-up: `100.00`
- Expected balance after: `220.00`

### The story

1. Arjun opens Swagger at `http://localhost:8080/swagger-ui/index.html`.
2. He clicks **Authorize** and enters `arjun` / `Passw0rd!`.
3. He opens `POST /api/cards/{id}/topup`.
4. He enters id `CARD-SEED3` and amount `100.00`.
5. Swagger builds:
   ```http
   POST /api/cards/CARD-SEED3/topup?amount=100.00
   Authorization: Basic YXJqdW46UGFzc3cwcmQh
   ```
6. Spring Security validates the Basic header through `DbUserDetailsService`.
7. The user is loaded from the `"User"` table where `Contact = 'arjun'`.
8. Authorities become `[ROLE_PASSENGER]`.
9. Spring MVC matches `/{id}/topup` and calls `topUpCard`.
10. `@PreAuthorize` allows `ROLE_PASSENGER`.
11. Spring converts the text `100.00` into a `BigDecimal`.
12. `amount.compareTo(BigDecimal.ZERO)` returns `1`, which is greater than `0`. The check passes.
13. `findById("CARD-SEED3")` runs:
    ```sql
    SELECT * FROM "MetroCards" WHERE "CardID" = 'CARD-SEED3';
    ```
    It returns balance `120.00`, owner `PASS-SEED2`, active `true`, expiry in the future.
14. `checkOwnership` sees the caller is a passenger.
15. `currentUser` runs:
    ```sql
    SELECT * FROM "User" WHERE "Contact" = 'arjun';
    ```
    It returns `UserId = PASS-SEED2`.
16. `PASS-SEED2` equals the card's `PassengerID`. The check passes.
17. `Boolean.TRUE.equals(true)` is true, so the card is active.
18. `expiryDate.isBefore(today)` is false, so it is not expired.
19. `card.getBalance().add(amount)` produces a new `BigDecimal` equal to `220.00`.
20. `setBalance` stores the new object in the entity.
21. `save(card)` sees an existing id and runs:
    ```sql
    UPDATE "MetroCards"
    SET "Balance" = 220.00, ...
    WHERE "CardID" = 'CARD-SEED3';
    ```
22. `@Transactional` commits.
23. The response is `200 OK` with the full card JSON, including `"balance": 220.00`.
24. Swagger displays the new balance.

If Arjun had tried `CARD-SEED1` (Priya's card), step 16 would compare `PASS-SEED2` with `PASS-SEED1`, fail, and return 403. Nothing would be updated.

---

## 10. JSON request and response examples

### 10.1 Create a card — request

```http
POST /api/cards
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json
```

```json
{
  "passengerId": "PASS-SEED3",
  "balance": 50.00,
  "expiryDate": "2031-10-29"
}
```

| Field | Type | Required | Meaning |
|---|---|---|---|
| `passengerId` | string | yes for staff, ignored for passengers | Owner's `UserId`. |
| `balance` | number | no | Opening balance. Defaults to `0.00`. |
| `expiryDate` | date string | no | Last valid day. Defaults to today + 5 years. |

Do not send `cardId`, `issueDate`, `createdAt` or `isActive`. The server sets all four.

If a passenger sends `passengerId`, it is ignored and replaced with their own id.

### 10.2 Create — response 201

```json
{
  "cardId": "CARD-4b9e2c11-7f3a-4d21-8c55-1a2b3c4d5e6f",
  "passenger": {
    "userId": "PASS-SEED3",
    "firstName": "Meera",
    "lastName": "Singh",
    "role": "PASSENGER",
    "contact": "meera",
    "registrationDate": "2026-04-29",
    "isActive": true,
    "hireDate": null
  },
  "balance": 50.00,
  "issueDate": "2026-10-29",
  "expiryDate": "2031-10-29",
  "createdAt": "2026-10-29T10:15:30.123456",
  "isActive": true
}
```

This example assumes Meera's existing `CARD-SEED4` has been deleted first. Otherwise the duplicate-active-card rule returns 400. Create the card for a freshly registered passenger if you want a clean run.

Note: `isActive` appears exactly as written because the getter is `getIsActive()`.

`passengerId` does not appear, because the transient field is `WRITE_ONLY`. The id is visible inside `passenger.userId`.

`password` never appears, because `User.password` is `WRITE_ONLY`.

### 10.3 Top-up — request

```http
POST /api/cards/CARD-SEED3/topup?amount=100.00
```

No body is needed. The amount is in the query string.

### 10.4 Top-up — response 200

```json
{
  "cardId": "CARD-SEED3",
  "passenger": {
    "userId": "PASS-SEED2",
    "firstName": "Arjun",
    "lastName": "Rao"
  },
  "balance": 220.00,
  "issueDate": "2026-02-28",
  "expiryDate": "2028-10-29",
  "createdAt": "2026-02-28T10:15:30",
  "isActive": true
}
```

The example is shortened. The real nested passenger contains every user field except `password`.

### 10.5 Read one — 200

```http
GET /api/cards/CARD-SEED1
```

```json
{
  "cardId": "CARD-SEED1",
  "passenger": { "userId": "PASS-SEED1", "firstName": "Priya" },
  "balance": 250.00,
  "issueDate": "2026-02-28",
  "expiryDate": "2029-10-29",
  "createdAt": "2026-02-28T10:15:30",
  "isActive": true
}
```

### 10.6 List by passenger — 200

```http
GET /api/cards/passenger/PASS-SEED1
```

```json
[
  {
    "cardId": "CARD-SEED1",
    "balance": 250.00,
    "expiryDate": "2029-10-29",
    "isActive": true
  },
  {
    "cardId": "CARD-SEED2",
    "balance": 0.00,
    "expiryDate": "2026-12-29",
    "isActive": false
  }
]
```

Priya has two rows but only one active card. This is the seeded example of the business rule.

### 10.7 Deactivate — 200

```http
PATCH /api/cards/CARD-SEED3/deactivate
```

```json
{
  "cardId": "CARD-SEED3",
  "balance": 220.00,
  "isActive": false
}
```

### 10.8 Delete — 204

```http
DELETE /api/cards/CARD-SEED2
```

Status `204 No Content`, empty body.

---

## 11. Error table

| When | Status | Body / message |
|---|---|---|
| No `Authorization` header | 401 | Spring Security default |
| Wrong password | 401 | Spring Security default |
| Role not allowed by `@PreAuthorize` | 403 | Spring Security access denied |
| Passenger reads another's card | 403 | `{"error": "You do not have permission to access this resource"}` |
| Passenger tops up another's card | 403 | same fixed message |
| Passenger sends no `passengerId` | — | Not an error; their own id is used |
| Staff sends no `passengerId` | 400 | `{"error": "Passenger ID is required"}` |
| Staff sends blank `passengerId` | 400 | same message |
| `passengerId` does not exist | 400 | `{"error": "Passenger not found"}` |
| Passenger already has an active card | 400 | `{"error": "Passenger already has an active metro card"}` |
| Duplicate `cardId` | 500 | Database primary-key violation, caught by the generic handler |
| Unknown card id | 400 | `{"error": "Metro card not found"}` |
| Top-up amount is `0` or negative | 400 | `{"error": "Top-up amount must be greater than zero"}` |
| Top-up amount is missing from the URL | 500 | `{"error": "An unexpected error occurred: ..."}` |
| Top-up amount is not a number | 500 | Same generic 500, because the catch-all handler sees the parse error |
| Card is inactive | 400 | `{"error": "Metro card is inactive"}` |
| Card expiry is before today | 400 | `{"error": "Metro card has expired"}` |
| Logged-in contact no longer exists | 400 | `{"error": "Current user not found"}` |
| Delete an unknown card | 400 | `{"error": "Metro card not found"}` |
| Negative starting balance at create | 201 | Accepted — no validation exists |

Two honest notes for the evaluator:

1. **Missing query parameter becomes 500, not 400.** `GlobalExceptionHandler` has a catch-all `@ExceptionHandler(Exception.class)` that turns `MissingServletRequestParameterException` into 500. Many tutorials expect 400 here.
2. **Negative balances are accepted at creation.** Only top-up checks positivity. A stronger entity would validate `balance >= 0`.

Why "not found" is 400 and not 404:

- The controller throws `IllegalArgumentException`.
- The global handler maps that to 400.
- Only `ResourceNotFoundException` maps to 404, and this controller does not use it.

Why the 403 text is generic:

- `GlobalExceptionHandler.handleAccessDeniedException` ignores the original message.
- It always writes "You do not have permission to access this resource".

---

## 12. Relationships to other groups

```text
User (01) 1 ──── many MetroCard (11)
```

| Related group | How it connects |
|---|---|
| User (file 01) | `MetroCard.passenger` is `@ManyToOne` to `User.UserId`. Required. |
| Auth and roles (file 02) | Decides who may issue, top up, list, deactivate or delete. `Authentication` supplies identity. |
| Ticket (file 09) | No direct foreign key. A card is not used to buy a ticket in this code. |
| Payment (file 10) | No direct foreign key. Top-ups do not create `Payment` rows. |
| Station / Route / Fare | No connection. A card is not tied to a line or a station. |

What this means in practice:

- Deleting a user who still has cards is blocked by the foreign key.
- Deleting a card does not affect the user.
- A passenger's cards are found with `findByPassenger_UserId`.
- Because there is no link to `Ticket`, you cannot answer "which trips were paid with this card?" from this schema. Say that clearly rather than inventing a link.

Why the card is a standalone group:

- It stores value, not journeys.
- The journey record is the ticket.
- The money record for a ticket is the payment.
- Mixing them would make the schema harder to reason about.

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
4. Use `admin` / `admin123` for the staff tests.
5. Expand **metro-card-controller**.
6. Create for a passenger who has no card:
   - Register a new passenger first with `POST /api/auth/passenger/register`, or use Meera after deleting her card.
   - `POST /api/cards`
   - body: `{"passengerId": "PASS-SEED3", "balance": 50.00}`
   - expect `201`
   - if Meera already has `CARD-SEED4`, expect `400` "already has an active metro card" — that is the rule working.
7. Test the duplicate rule:
   - repeat the same body for `PASS-SEED1` (Priya)
   - expect `400` "Passenger already has an active metro card"
8. Test top-up:
   - `POST /api/cards/CARD-SEED3/topup`
   - amount `100.00`
   - expect `200` and balance `220.00`
9. Test top-up validation:
   - amount `0`
   - expect `400` "Top-up amount must be greater than zero"
10. Test inactive card:
    - `PATCH /api/cards/CARD-SEED2/deactivate` (already inactive)
    - then `POST /api/cards/CARD-SEED2/topup?amount=10`
    - expect `400` "Metro card is inactive"
11. Test expired card:
    - create a card with `"expiryDate": "2020-01-01"` for a passenger without an active card
    - top it up
    - expect `400` "Metro card has expired"
12. Test ownership:
    - authorize as `arjun` / `Passw0rd!`
    - `GET /api/cards/CARD-SEED1` (Priya's)
    - expect `403`
13. Test the forced id:
    - still as `arjun`
    - `GET /api/cards/passenger/PASS-SEED1`
    - expect `200` but the array contains Arjun's cards, not Priya's
14. Test staff-only list:
    - as `arjun`, `GET /api/cards`
    - expect `403`
15. Test deactivate and delete:
    - as `admin`, `PATCH /api/cards/CARD-SEED4/deactivate` → `200`
    - `DELETE /api/cards/CARD-SEED4` → `204`
    - repeat delete → `400` "Metro card not found"

---

## 14. How to explain this in 2 minutes

> "The Metro Card group is one entity, one repository and one controller. It models a stored-value travel card.
>
> The entity maps to the `MetroCards` table. `CardID` is the primary key, generated as `CARD-` plus a UUID. `PassengerID` is a foreign key to `User`, mapped as a lazy `@ManyToOne`. `Balance` is `BigDecimal` with two decimal places. `IssueDate` and `ExpiryDate` are `LocalDate`. `CreatedAt` is a `LocalDateTime`. `isActive` is a `Boolean` that starts true.
>
> There is also a transient `passengerId` string. It is not a column. It exists so the client can send a simple id instead of a nested user object. It is `WRITE_ONLY`, so it never appears in responses. The real `passenger` field is `READ_ONLY`, so input cannot fake an owner.
>
> The repository extends `JpaRepository`, which gives `save`, `findById`, `findAll`, `existsById` and `deleteById`. We add two derived queries. `findByPassenger_UserId` returns all cards of one user. `findByPassenger_UserIdAndIsActiveTrue` returns the one active card, as an `Optional`. The underscore means 'go inside the passenger object'. The generated SQL filters on `PassengerID` and `isActive = true`.
>
> The controller has seven endpoints. Create issues a card. If the caller is a passenger, the code ignores any `passengerId` in the body and forces their own id. Staff may choose the owner. Creation also rejects a passenger who already has an active card. The id, issue date, active flag and five-year default expiry are all set by the server.
>
> Top-up is `POST /{id}/topup?amount=...`. It rejects zero or negative amounts, checks ownership, rejects inactive cards, rejects expired cards, then adds the amount and saves. Because `BigDecimal` is immutable, the code must call `setBalance` with the result of `add`.
>
> Read one checks ownership. List all is staff-only. List by passenger silently replaces a passenger's requested id with their own, so they can never read someone else's list. Deactivate is a PATCH that sets the flag false without deleting. Delete is admin-only and returns 204.
>
> The main things to be honest about: top-ups are not recorded as payments, there is no top-up history table, balance is not validated at creation, and the one-active-card rule is enforced in Java, not by a database constraint."

---

## 15. Beginner Q&A

**Q1. What is an entity?**
A Java class mapped to a database table. `MetroCard` maps to `"MetroCards"`.

**Q2. What does `@Entity` do?**
It tells JPA to treat the class as a table and to manage its rows.

**Q3. What does `@Table(name = "\"MetroCards\"")` do?**
It sets the exact table name. The escaped quotes keep the capital letters in PostgreSQL.

**Q4. Why is `@Column(name = "\"isActive\"")` lowercase?**
Because that is the literal column name in the SQL migration. JPA must match it exactly.

**Q5. What is a primary key here?**
`cardId`, stored in the `CardID` column.

**Q6. What is a foreign key here?**
`PassengerID`, pointing to `User.UserId`.

**Q7. Why is `passenger` `@ManyToOne`?**
Many cards can belong to one user, but each card belongs to exactly one user.

**Q8. What is `FetchType.LAZY`?**
The user row is not loaded until `getPassenger()` is used. Then Hibernate runs another SELECT.

**Q9. What is `@Transient`?**
It means "do not create a database column for this field".

**Q10. Why does `passengerId` need `@Transient`?**
Because the link is already stored through the `passenger` field. A second column would duplicate it.

**Q11. What does `WRITE_ONLY` do?**
It allows the field in JSON input but hides it from JSON output.

**Q12. What does `READ_ONLY` do on `passenger`?**
It lets the field appear in responses but ignores it in requests, so the client must send an id.

**Q13. How does the client set the owner then?**
By sending `"passengerId": "PASS-SEED3"` in the body.

**Q14. Why is `balance` a `BigDecimal`?**
Because money must be exact. `double` can produce rounding errors.

**Q15. Why is `BigDecimal` described as immutable?**
`add` does not modify the original object; it returns a new one. You must assign the result.

**Q16. What does `balance = BigDecimal.ZERO` do?**
It gives every new card a starting balance of `0.00` and prevents null-pointer errors.

**Q17. What happens if the client sends no balance?**
The controller uses `BigDecimal.ZERO`.

**Q18. Does `@Valid` validate the balance?**
No. The entity has no constraints, so `@Valid` has nothing to check. A negative balance is accepted.

**Q19. What does `@PreAuthorize` do?**
It checks the logged-in user's roles before the method runs.

**Q20. Why is `@PreAuthorize` not on `getMetroCardById`?**
Because any logged-in user may call it. Ownership is checked inside instead.

**Q21. How does the code know the caller's id?**
`authentication.getName()` returns the contact, then `findByContact` loads the user row and `getUserId()` gives the primary key.

**Q22. Why not compare contacts directly?**
The card stores `PassengerID`. Comparing primary keys is exact and survives a contact change.

**Q23. What happens if a passenger sends another person's passengerId at creation?**
The value is silently replaced with their own id. They cannot create a card for someone else.

**Q24. Can an admin create a card for any passenger?**
Yes. The override only applies when the caller has `ROLE_PASSENGER`.

**Q25. Why is a second active card rejected?**
Because the code checks `findByPassenger_UserIdAndIsActiveTrue` before saving. A passenger may still have old inactive cards.

**Q26. Is that rule enforced by the database?**
No. There is no unique index. Two simultaneous requests could both pass. A database constraint would be stronger.

**Q27. What does `findByPassenger_UserIdAndIsActiveTrue` mean?**
"Find cards whose passenger's `userId` equals the argument and whose `isActive` is true."

**Q28. Why does it return `Optional`?**
Because the rule says there should be zero or one result, and `Optional` models that clearly.

**Q29. What does `isPresent()` do?**
It returns true if the `Optional` contains a value.

**Q30. Why is top-up a POST and not a PUT?**
It is an action that changes state, and it is not a full replacement of the card. POST fits. Some APIs use PATCH for this.

**Q31. Why is the amount a query parameter?**
Because it is a single value, and `?amount=100` is easy to type in Swagger. A body would be better for sensitive data.

**Q32. Why reject zero and negative top-ups?**
Adding zero does nothing, and a negative amount would secretly remove money. Both are blocked.

**Q33. Why check inactive before expiry?**
Order does not change correctness, but it gives a clearer message: a blocked card is reported as blocked, not as expired.

**Q34. How is expiry checked?**
`expiryDate.isBefore(LocalDate.now())`. A card expiring today is still valid.

**Q35. What happens when a card expires?**
It can still be read, but top-up is blocked with "Metro card has expired". It is not automatically deactivated.

**Q36. Why deactivate instead of delete?**
Deactivation keeps the row for audit and support. Delete is a hard removal and is admin-only.

**Q37. Is deactivate idempotent?**
Yes. Calling it twice leaves `isActive = false` and returns 200 both times.

**Q38. What does the list-by-passenger endpoint do for a passenger?**
It replaces the requested id with the caller's own id. So they always get their own cards, even if they asked for someone else's.

**Q39. Why does that return 200 instead of 403?**
The code chooses to correct the input silently. It is safe, but it can confuse testers. Say so plainly.

**Q40. Does a top-up create a payment record?**
No. `Payment` points to a ticket, and top-up has no payment table. The new balance is the only record.

**Q41. Can a metro card be used to pay for a ticket?**
Not in this code. There is no link between `MetroCard` and `Ticket`.

**Q42. What happens if I delete a user who has cards?**
The foreign key blocks it, because `PassengerID` would point to a missing user.

**Q43. Why 201 on create?**
201 means a new resource was created. The response also includes the generated id.

**Q44. Why 204 on delete?**
204 means success with no body. There is nothing useful to return after a delete.

**Q45. Where do 400 errors come from?**
From `IllegalArgumentException`, which `GlobalExceptionHandler` maps to 400 with `{"error": "..."}`.

---

## 16. Glossary

| Term | Meaning |
|---|---|
| **Annotation** | A `@word` that adds behaviour or metadata. |
| **Bean** | An object created and managed by Spring. |
| **BigDecimal** | Exact decimal class used for money. |
| **Controller** | Class that handles HTTP requests. |
| **Deactivate** | Mark a card unusable without deleting it. |
| **Derived query** | Method name that Spring converts into SQL. |
| **Dependency injection** | Spring passes dependencies into a constructor. |
| **Entity** | Class mapped to a database table. |
| **Fetch type** | Whether related data loads now (`EAGER`) or later (`LAZY`). |
| **Foreign key** | Column pointing to another table's primary key. |
| **Hibernate** | JPA implementation that generates SQL. |
| **HTTP Basic** | Login by sending credentials with every request. |
| **Idempotent** | Repeating the operation gives the same end state. |
| **Jackson** | Library that converts Java and JSON. |
| **JPA** | Java standard for object-to-table mapping. |
| **JSON** | Text format for structured data. |
| **`LocalDate`** | Date without time. |
| **`LocalDateTime`** | Date with time. |
| **`Optional`** | Box that may contain zero or one value. |
| **Ownership check** | Comparing the caller's id with the row owner's id. |
| **PATCH** | HTTP method for a partial change. |
| **Primary key** | Column that uniquely identifies a row. |
| **`READ_ONLY`** | JSON output only. |
| **Repository** | Interface providing database operations. |
| **Role** | Label used for permissions, e.g. `PASSENGER`. |
| **Rollback** | Undo all changes in a failed transaction. |
| **Scale** | Digits after the decimal point. |
| **Status code** | Numeric HTTP result. |
| **Swagger UI** | Web page for testing the API. |
| **Top-up** | Adding money to a balance. |
| **Transaction** | All-or-nothing database unit. |
| **`@Transient`** | Not a database column. |
| **UUID** | Long random unique string. |
| **`WRITE_ONLY`** | JSON input only. |

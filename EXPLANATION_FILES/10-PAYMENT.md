# 10 — Payment Group (Money paid for a ticket)

---

## 1. Real-life analogy

Imagine a coffee shop.

You order a coffee. The cashier prints a bill (the **ticket**). You then pay at the counter (the **payment**). The payment slip is a separate piece of paper. It says:

- which bill it is paying
- how much money was handed over
- when it was paid

The payment slip does not contain the coffee order itself. It only points at the bill.

In this project:

- a **Ticket** is the bill — "Priya travels Versova → Western Express Highway, fare ₹20"
- a **Payment** is the proof that ₹20 was paid for that ticket

This is why the `Payment` table has a `TicketID` column and no `PassengerID` column. The passenger is found by following the ticket.

One more rule in this shop: you must pay the exact bill amount. Paying ₹25 for a ₹20 bill is rejected. Paying ₹10 is also rejected.

---

## 2. Words you need

| Word | One simple sentence |
|---|---|
| HTTP | The language a browser or Swagger uses to talk to the server. |
| Endpoint | One URL plus one HTTP method that the server listens to, e.g. `POST /api/payments`. |
| JSON | A text format for data, like `{"amount": 20.00}`. |
| Request body | The JSON sent by the client inside the HTTP request. |
| Response body | The JSON sent back by the server. |
| Status code | A number saying what happened: 200 OK, 201 created, 400 bad input, 401 not logged in, 403 forbidden, 404 not found, 204 deleted. |
| Spring Boot | The Java framework that starts the server and connects all the pieces. |
| Annotation | A word starting with `@` that tells Spring extra information about a class, field or method. |
| Entity | A Java class that represents one database table. |
| Repository | A Java interface that reads and writes entities, without you writing SQL. |
| Controller | A Java class whose methods handle HTTP requests. |
| JPA | The Java standard for mapping classes to database tables. |
| Hibernate | The library that actually implements JPA and produces SQL. |
| Dependency injection | Spring creates objects and passes them into your constructor, so you never call `new` on a repository. |
| Bean | An object that Spring creates and manages. |
| `@RestController` | Tells Spring: this class handles HTTP and returns JSON. |
| `@RequestMapping` | Sets the common URL prefix for every method in the class. |
| `@PostMapping` | Handles HTTP POST (create). |
| `@GetMapping` | Handles HTTP GET (read). |
| `@DeleteMapping` | Handles HTTP DELETE (remove). |
| `@PathVariable` | Reads a value out of the URL, e.g. `{id}`. |
| `@RequestBody` | Reads the JSON body and turns it into a Java object. |
| `@Valid` | Runs the validation annotations (`@NotNull`, `@DecimalMin`) before the method runs. |
| `@PreAuthorize` | Checks the logged-in user's role before the method runs. |
| `@Transactional` | Wraps the method in one database transaction: all or nothing. |
| `Authentication` | The object Spring fills with the logged-in user's name and roles. |
| `ResponseEntity` | A wrapper that lets you choose the status code and body. |
| `@Entity` | Marks a class as a database table. |
| `@Table` | Gives the exact table name. |
| `@Id` | Marks the primary key field. |
| `@Column` | Gives the exact column name, length or precision. |
| `@ManyToOne` | Many rows of this table point to one row of another table. |
| `@JoinColumn` | Names the foreign-key column used by `@ManyToOne`. |
| `@Transient` | "Do not save this field in the database." |
| `@JsonProperty` | Controls whether a field appears in JSON input, output, or both. |
| `READ_ONLY` | Can be shown in responses, but ignored in requests. |
| `WRITE_ONLY` | Can be sent in requests, but hidden from responses. |
| `FetchType.LAZY` | Load the related row only when someone actually reads it. |
| `BigDecimal` | A Java class for exact decimal money, better than `double`. |
| `LocalDateTime` | A date and time without a time zone, e.g. `2026-10-29T10:15:30`. |
| `UUID` | A long random string that is very unlikely to repeat. |
| Foreign key | A column that points to the primary key of another table. |
| Primary key | The column that uniquely identifies a row. |
| Derived query | A repository method whose name Spring turns into SQL automatically. |
| Transaction | A group of database changes that either all succeed or all roll back. |
| Role | A label on a user: `ADMIN`, `PASSENGER`, `TICKET_AGENT`, `MAINTENANCE_STAFF`. |
| HTTP Basic | A simple login style: username and password are sent with every request. |
| Swagger UI | A web page that lists the API and lets you click "Try it out". |
| Ownership check | Code that makes sure a passenger can only touch their own rows. |

---

## 3. Files in this group

| Role | Exact path |
|---|---|
| Entity | `src/main/java/com/metro/ticketing/entity/Payment.java` |
| Controller | `src/main/java/com/metro/ticketing/controller/PaymentController.java` |
| Repository | `src/main/java/com/metro/ticketing/repository/PaymentRepository.java` |
| Table definition | `src/main/resources/db/migration/V1__create_metro_ticketing_schema.sql` |
| Error handling | `src/main/java/com/metro/ticketing/exception/GlobalExceptionHandler.java` |
| Demo data | `src/main/java/com/metro/ticketing/config/DataSeeder.java` |

Supporting files used by the controller:

- `src/main/java/com/metro/ticketing/entity/Ticket.java`
- `src/main/java/com/metro/ticketing/entity/Fare.java`
- `src/main/java/com/metro/ticketing/entity/User.java`
- `src/main/java/com/metro/ticketing/repository/TicketRepository.java`
- `src/main/java/com/metro/ticketing/repository/UserRepository.java`
- `src/main/java/com/metro/ticketing/config/SecurityConfig.java`

---

## 4. Database table

From `V1__create_metro_ticketing_schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS "Payment" (
    "PaymentID" VARCHAR(50) PRIMARY KEY,
    "TicketID" VARCHAR(50) NOT NULL,
    "Amount" NUMERIC(10,2) NOT NULL,
    "CreatedAt" TIMESTAMP,
    CONSTRAINT "FK_Payment_Ticket" FOREIGN KEY ("TicketID") REFERENCES "Tickets" ("TicketID")
);
```

Column by column:

| Column | Type | Meaning |
|---|---|---|
| `PaymentID` | `VARCHAR(50)`, primary key | Unique payment id. The app builds it as `PAY-` + a random UUID. |
| `TicketID` | `VARCHAR(50)`, not null, foreign key | Which ticket was paid. Points to `Tickets.TicketID`. |
| `Amount` | `NUMERIC(10,2)`, not null | Money paid. Two decimal places, up to 10 digits total. |
| `CreatedAt` | `TIMESTAMP` | When the payment row was created. |

Why the table name is in double quotes:

- PostgreSQL folds unquoted names to lowercase.
- The project wants exact camel-case names like `Payment` and `PaymentID`.
- So every table and column name is written in double quotes.
- In Java, the quotes are escaped: `@Table(name = "\"Payment\"")`.

Why there is no `PassengerID`:

- The passenger is already stored on the ticket.
- Copying it here would create two sources of truth.
- Following `Payment → Ticket → PassengerID` gives the passenger.
- This avoids the same fact being stored twice and getting out of sync.

Why there is no `FareID`:

- The expected price is already on the ticket, through `Ticket.FareID`.
- The controller compares `payment.amount` with `ticket.fare.baseFare`.

---

## 5. Entity line by line

File: `src/main/java/com/metro/ticketing/entity/Payment.java`

The real file has no comments. Everything after `//` below is added for teaching.

```java
package com.metro.ticketing.entity;                                    // this class lives in the entity package

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;          // Jackson = the library that converts Java <-> JSON
import com.fasterxml.jackson.annotation.JsonProperty;                  // lets us control JSON input/output per field
import jakarta.persistence.*;                                          // JPA annotations: @Entity, @Id, @Column, ...
import jakarta.validation.constraints.DecimalMin;                      // "number must be at least this value"
import jakarta.validation.constraints.NotNull;                         // "value must not be null"

import java.math.BigDecimal;                                           // exact decimal type for money
import java.time.LocalDateTime;                                        // date + time, no time zone

@Entity                                                                // "this class is a database table"
@Table(name = "\"Payment\"")                                           // exact table name is Payment (quoted in SQL)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})         // hide Hibernate's internal proxy fields from JSON
public class Payment {

    @Id                                                                // this field is the primary key
    @Column(name = "\"PaymentID\"", length = 50)                       // column PaymentID, max 50 characters
    private String paymentId;                                          // e.g. "PAY-3f2c8a..."

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)              // show in responses, ignore if client sends it
    @ManyToOne(fetch = FetchType.LAZY)                                 // many payments -> one ticket; load ticket only when read
    @JoinColumn(name = "\"TicketID\"",                                 // foreign-key column in the Payment table
                referencedColumnName = "\"TicketID\"",                 // it points to Tickets.TicketID
                nullable = false)                                      // a payment must belong to a ticket
    private Ticket ticket;                                             // the Java object for that ticket row

    @Transient                                                         // do NOT create a column for this field
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)             // accept "ticketId" in JSON, never print it back
    private String ticketId;                                           // just the ticket's id, used only on input

    @NotNull(message = "Amount is required")                           // reject null amount with this message
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero") // reject 0 or negative
    @Column(name = "\"Amount\"", precision = 10, scale = 2,            // column Amount, 10 total digits, 2 after the dot
            nullable = false)                                          // database also forbids null
    private BigDecimal amount;                                         // money paid, e.g. 20.00

    @Column(name = "\"CreatedAt\"")                                    // column CreatedAt
    private LocalDateTime createdAt = LocalDateTime.now();             // default value = the moment the object is created

    // ... getters and setters follow ...
}
```

### The getters and setters

```java
public String getPaymentId() { return paymentId; }
public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

public Ticket getTicket() { return ticket; }
public void setTicket(Ticket ticket) { this.ticket = ticket; }

public BigDecimal getAmount() { return amount; }
public void setAmount(BigDecimal amount) { this.amount = amount; }

public LocalDateTime getCreatedAt() { return createdAt; }
public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

public String getTicketId() { return ticketId; }
public void setTicketId(String ticketId) { this.ticketId = ticketId; }
```

Why getters and setters matter here:

- Jackson uses them to read JSON into the object and to write the object back as JSON.
- Hibernate uses them (or the fields) to load and store rows.
- Spring Security and the controller call `getTicket()` and `getAmount()`.
- Without `getTicketId()` / `setTicketId()`, the JSON field `ticketId` would not work.

### Why `ticketId` is `@Transient`

`@Transient` means "this is not a database column".

The database already stores the link in the `TicketID` foreign key, through the `ticket` field. If `ticketId` were also a column, there would be two columns for one fact.

But the client does not want to send a whole nested ticket object. The client wants to send:

```json
{"ticketId": "TKT-SEED1", "amount": 20.00}
```

So `ticketId` exists only in memory:

1. Jackson fills `ticketId` from the JSON body.
2. The controller reads it with `request.getTicketId()`.
3. The controller loads the real `Ticket` and calls `payment.setTicket(ticket)`.
4. Hibernate saves the foreign key from `ticket`.

### Why `ticket` is `READ_ONLY`

- On input, the client must use `ticketId`, not a nested ticket object.
- This stops a client from inventing a ticket object with a fake fare.
- On output, the full ticket is still shown, which is useful for the evaluator.

### Why `WRITE_ONLY` and not just `@Transient`

- `@Transient` only affects the database.
- Jackson would still print the field in responses without `@JsonProperty`.
- `WRITE_ONLY` hides it from responses, so the output has `ticket` but not a duplicate `ticketId`.

### Why `BigDecimal`

`double` stores binary fractions and cannot represent `0.10` exactly. Money needs exact values. `BigDecimal` stores digits and a scale, so `20.00` stays `20.00`.

`precision = 10, scale = 2` means:

- `scale = 2` → two digits after the decimal point
- `precision = 10` → at most 10 significant digits in total
- so the largest value is `99999999.99`

### Why `createdAt` has a default

```java
private LocalDateTime createdAt = LocalDateTime.now();
```

- Every new `Payment` object starts with the current time.
- The controller overwrites it with `payment.setCreatedAt(LocalDateTime.now())`.
- The setter still exists, so tests and the seeder can set historical times.

---

## 6. Repository line by line

File: `src/main/java/com/metro/ticketing/repository/PaymentRepository.java`

```java
package com.metro.ticketing.repository;

import com.metro.ticketing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository                                                                        // marks this as a Spring bean
public interface PaymentRepository extends JpaRepository<Payment, String> {        // entity = Payment, id type = String
    List<Payment> findByTicket_TicketId(String ticketId);                          // derived query, explained below
}
```

It is an `interface`, not a class. There is no method body. Spring Data JPA generates an implementation at startup.

### Methods you get for free from `JpaRepository`

| Method | What it does | SQL Spring runs |
|---|---|---|
| `save(payment)` | Insert if the id is new, update if it already exists | `INSERT INTO "Payment" (...) VALUES (...)` or `UPDATE "Payment" SET ... WHERE "PaymentID" = ?` |
| `findById(id)` | Look up one row by primary key | `SELECT * FROM "Payment" WHERE "PaymentID" = ?` |
| `findAll()` | Return every row | `SELECT * FROM "Payment"` |
| `existsById(id)` | Is there a row with this id? | `SELECT COUNT(*) > 0 FROM "Payment" WHERE "PaymentID" = ?` |
| `deleteById(id)` | Delete one row | `DELETE FROM "Payment" WHERE "PaymentID" = ?` |
| `count()` | How many rows? | `SELECT COUNT(*) FROM "Payment"` |

`findById` returns `Optional<Payment>`. An `Optional` is a box that either contains a value or is empty. That is why the controller writes `.orElseThrow(...)`.

### The one derived query

```java
List<Payment> findByTicket_TicketId(String ticketId);
```

Spring reads the method name in pieces:

| Piece | Meaning |
|---|---|
| `findBy` | run a SELECT and return the results |
| `Ticket` | go to the `ticket` field of `Payment` |
| `_` | "now look inside that related object" |
| `TicketId` | use the `ticketId` field of `Ticket` |

Generated SQL:

```sql
SELECT *
FROM "Payment"
WHERE "TicketID" = ?;
```

Why the underscore is needed:

- `findByTicketId` would mean "a field named `ticketId` on `Payment`".
- `Payment` does have a field named `ticketId`, but it is `@Transient`, so it has no column. That would fail.
- `findByTicket_TicketId` clearly means "the `ticketId` inside the related `ticket` object".

The return type is `List<Payment>` because one ticket could in theory have more than one payment row. Today the controller creates one payment per request, but the database does not forbid duplicates.

---

## 7. Controller endpoints

Base path: `/api/payments` (from `@RequestMapping("/api/payments")`).

| Method | Full path | Who is allowed | Purpose | Success |
|---|---|---|---|---|
| POST | `/api/payments` | `PASSENGER`, `TICKET_AGENT` | Record a payment for a ticket | 201 + payment JSON |
| GET | `/api/payments/{id}` | any logged-in user, ownership enforced for passengers | Read one payment | 200 + payment JSON |
| GET | `/api/payments` | `ADMIN`, `TICKET_AGENT` | List every payment | 200 + JSON array |
| GET | `/api/payments/ticket/{ticketId}` | any logged-in user, ownership enforced for passengers | List payments of one ticket | 200 + JSON array |
| DELETE | `/api/payments/{id}` | `ADMIN` | Delete a payment | 204, empty body |

Important: `ADMIN` is **not** in the `POST` allow-list. Admins cannot create payments through this endpoint. That is a deliberate choice in the code, and a common evaluator question.

---

## 8. Controller line by line

File: `src/main/java/com/metro/ticketing/controller/PaymentController.java`

### 8.1 Class header and fields

```java
@RestController                                                     // every method returns JSON, not an HTML page
@RequestMapping("/api/payments")                                    // all paths below start with /api/payments
public class PaymentController {

    private final PaymentRepository paymentRepository;              // talks to the Payment table
    private final TicketRepository ticketRepository;                // needed to look up the ticket
    private final UserRepository userRepository;                    // needed to find the logged-in user row

    public PaymentController(PaymentRepository paymentRepository,     // constructor injection:
                             TicketRepository ticketRepository,       // Spring sees these parameters,
                             UserRepository userRepository) {         // creates the repositories,
        this.paymentRepository = paymentRepository;                   // and passes them in.
        this.ticketRepository = ticketRepository;                     // we never write "new PaymentRepository()".
        this.userRepository = userRepository;
    }
```

Why constructor injection:

- The fields are `final`, so they cannot be swapped by accident.
- The class cannot be created without its dependencies.
- Spring creates one controller object and reuses it for every request.
- This is the standard Spring style and is easy to explain: "Spring hands me the tools I need."

### 8.2 Create payment

```java
@PostMapping                                                        // HTTP POST /api/payments
@PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT')")            // only these two roles may call it
@Transactional                                                      // one transaction: if anything throws, nothing is saved
public ResponseEntity<Payment> createPayment(
        @Valid @RequestBody Payment request,                        // parse JSON into Payment, then run @NotNull/@DecimalMin
        Authentication authentication) {                            // Spring injects the logged-in user info

    if (request.getTicketId() == null) {                            // the transient ticketId must be present
        throw new IllegalArgumentException("Ticket ID is required");
    }

    Ticket ticket = ticketRepository.findById(request.getTicketId()) // find the real ticket row
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

    if (isPassenger(authentication)                                 // if the caller is a passenger
            && !ticket.getPassenger().getUserId()                   // and the ticket's owner id
                    .equals(currentUser(authentication).getUserId())) { // is not the caller's id
        throw new AccessDeniedException("You can only make payments for your own tickets");
    }

    if (ticket.getFare() != null                                  // if the ticket has a fare
            && request.getAmount()                                // and the sent amount
                    .compareTo(ticket.getFare().getBaseFare()) != 0) { // is not exactly equal to the fare
        throw new IllegalArgumentException("Payment amount does not match the ticket fare");
    }

    Payment payment = new Payment();                              // build a brand-new entity
    payment.setPaymentId("PAY-" + UUID.randomUUID());             // generate a unique id
    payment.setTicket(ticket);                                    // link it to the real ticket row
    payment.setAmount(request.getAmount());                       // copy the money amount
    payment.setCreatedAt(LocalDateTime.now());                    // stamp the current time

    return ResponseEntity.status(HttpStatus.CREATED)              // 201 Created
            .body(paymentRepository.save(payment));               // INSERT and return the saved row as JSON
}
```

Step-by-step reasoning:

1. `@Valid` runs first. If `amount` is missing or `0`, the method body never runs. Spring throws `MethodArgumentNotValidException`, and the global handler returns 400 with a field map.
2. The controller checks `ticketId` itself, because `ticketId` is a plain `String` with no `@NotBlank`.
3. `findById` returns `Optional<Ticket>`. `orElseThrow` turns "empty" into a 400 error.
4. The ownership check runs only for passengers. Agents and admins are trusted to pay for anyone.
5. The amount check uses `compareTo`, not `equals`. See the box below.
6. The controller never trusts a client-supplied payment id or timestamp. It generates both.
7. `save` returns the managed entity, so the response contains the generated values.

#### Why `compareTo(...) != 0` and not `equals`

```java
new BigDecimal("20.0").equals(new BigDecimal("20.00"))   // false  (different scale)
new BigDecimal("20.0").compareTo(new BigDecimal("20.00")) // 0     (same value)
```

`compareTo` returns:

- a negative number if the left side is smaller
- `0` if the values are numerically equal
- a positive number if the left side is larger

Money JSON like `20.0` and `20.00` must both be accepted, so `compareTo` is the correct tool.

#### Amount rules in plain English

- must not be null (`@NotNull`)
- must be at least `0.01` (`@DecimalMin`)
- must equal the ticket's fare exactly, when the ticket has a fare
- overpaying is rejected
- underpaying is rejected
- if `ticket.getFare()` is null, the equality check is skipped — this is a safety guard for old or incomplete data

### 8.3 Read one payment

```java
@GetMapping("/{id}")                                                // GET /api/payments/PAY-123
@Transactional(readOnly = true)                                     // read-only transaction: no accidental writes
public ResponseEntity<Payment> getPaymentById(
        @PathVariable String id,                                    // take {id} from the URL
        Authentication authentication) {

    Payment payment = paymentRepository.findById(id)                 // look up the row
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

    checkOwnership(payment, authentication);                         // passengers may only read their own
    return ResponseEntity.ok(payment);                               // 200 OK + JSON
}
```

Why `readOnly = true`:

- It tells Hibernate "we will not change anything".
- Hibernate can skip dirty checking, which is a small performance win.
- It documents the intent to the next reader.

### 8.4 List all payments

```java
@GetMapping                                                         // GET /api/payments
@PreAuthorize("hasAnyRole('ADMIN', 'TICKET_AGENT')")                // passengers are blocked here
@Transactional(readOnly = true)
public ResponseEntity<List<Payment>> getAllPayments() {
    return ResponseEntity.ok(paymentRepository.findAll());           // 200 + JSON array of every row
}
```

Passengers are blocked because the full list would leak other people's payments. They use `/ticket/{ticketId}` instead, which checks ownership.

### 8.5 List payments for one ticket

```java
@GetMapping("/ticket/{ticketId}")                                   // GET /api/payments/ticket/TKT-SEED1
@Transactional(readOnly = true)
public ResponseEntity<List<Payment>> getPaymentsByTicket(
        @PathVariable String ticketId,
        Authentication authentication) {

    Ticket ticket = ticketRepository.findById(ticketId)              // load the ticket first
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

    if (isPassenger(authentication)                                  // same ownership rule as create
            && !ticket.getPassenger().getUserId()
                    .equals(currentUser(authentication).getUserId())) {
        throw new AccessDeniedException("You can only view payments for your own tickets");
    }

    return ResponseEntity.ok(paymentRepository.findByTicket_TicketId(ticketId)); // 200 + array
}
```

Why load the ticket first:

- The ownership information lives on the ticket, not on the payment.
- Without loading the ticket, the code cannot know who owns these payments.
- Loading it also produces a clear "Ticket not found" error instead of an empty list.

### 8.6 Delete payment

```java
@DeleteMapping("/{id}")                                             // DELETE /api/payments/PAY-123
@PreAuthorize("hasRole('ADMIN')")                                   // admins only
@Transactional
public ResponseEntity<Void> deletePayment(@PathVariable String id) {

    if (!paymentRepository.existsById(id)) {                         // avoid a silent no-op delete
        throw new IllegalArgumentException("Payment not found");
    }

    paymentRepository.deleteById(id);                                // DELETE FROM "Payment" WHERE ...
    return ResponseEntity.noContent().build();                       // 204, no body
}
```

Why check `existsById` first:

- `deleteById` on a missing id may throw a different low-level exception or do nothing, depending on the version.
- The explicit check gives a consistent 400 with the message "Payment not found".

`ResponseEntity<Void>` means "there is no response body". `noContent()` sets status 204.

### 8.7 Private helper: ownership

```java
private void checkOwnership(Payment payment, Authentication authentication) {
    if (isPassenger(authentication)                                  // passengers are restricted
            && !payment.getTicket().getPassenger().getUserId()       // ticket owner
                    .equals(currentUser(authentication).getUserId())) { // vs. caller
        throw new AccessDeniedException("You can only access payments for your own tickets");
    }
}
```

This follows two links: `payment → ticket → passenger → userId`.

If either link were null, this would throw a `NullPointerException`. In this schema a payment must have a ticket, and a ticket must have a passenger, so the links are guaranteed by the database.

### 8.8 Private helper: is the caller a passenger?

```java
private boolean isPassenger(Authentication authentication) {
    return authentication.getAuthorities().stream()                  // list of roles/permissions
            .anyMatch(authority -> authority.getAuthority()           // each one is a string
                    .equals("ROLE_PASSENGER"));                       // Spring adds the ROLE_ prefix
}
```

- `getAuthorities()` returns something like `[ROLE_PASSENGER]`.
- The string has the `ROLE_` prefix even though the enum is just `PASSENGER`.
- `anyMatch` returns true if at least one role matches.
- A user with several roles is handled correctly.

### 8.9 Private helper: current user row

```java
private User currentUser(Authentication authentication) {
    return userRepository.findByContact(authentication.getName())     // login name = contact
            .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
}
```

Why `getName()` is a contact:

- `DbUserDetailsService` builds the Spring user with `.withUsername(user.getContact())`.
- So the "username" in Spring Security is the contact column, e.g. `priya`.
- The code then loads the full `User` row to get `userId`, which is the primary key.

Why not compare contacts directly:

- The ticket stores a `PassengerID` foreign key.
- Comparing primary keys is the correct check.
- Contacts can change; ids should not.

---

## 9. One request from start to finish

Real seeded data used below:

- Passenger: Priya Sharma, `UserId = PASS-SEED1`, contact `priya`, password `Passw0rd!`
- Ticket: `TKT-SEED1`, owner Priya, route Versova → Western Express Highway, fare `20.00`
- Payment to create: `20.00`

### The story

1. Priya opens Swagger UI at `http://localhost:8080/swagger-ui/index.html`.
2. She clicks **Authorize** and enters `priya` / `Passw0rd!`. The browser now sends an `Authorization: Basic ...` header on every call.
3. She opens `POST /api/payments` and clicks **Try it out**.
4. She types the body:
   ```json
   {"ticketId": "TKT-SEED1", "amount": 20.00}
   ```
5. Tomcat (the embedded web server) accepts the TCP connection and hands the bytes to Spring.
6. Spring Security reads the Basic header, calls `DbUserDetailsService.loadUserByUsername("priya")`.
7. That service loads the `User` row and builds a Spring user with authority `ROLE_PASSENGER`.
8. Spring MVC sees `POST /api/payments` and picks `createPayment`.
9. `@PreAuthorize` sees `ROLE_PASSENGER` in the allow-list. It passes.
10. Jackson reads the JSON. It sets `request.ticketId = "TKT-SEED1"` and `request.amount = 20.00`.
11. `@Valid` runs. `amount` is not null and is at least `0.01`. It passes.
12. The controller finds the ticket: `SELECT * FROM "Tickets" WHERE "TicketID" = 'TKT-SEED1'`.
13. `isPassenger` returns true.
14. `currentUser` runs `SELECT * FROM "User" WHERE "Contact" = 'priya'` and gets `PASS-SEED1`.
15. It compares `PASS-SEED1` with the ticket's `PassengerID`, which is also `PASS-SEED1`. The ownership check passes.
16. It compares `20.00` with the ticket's fare `20.00`. `compareTo` returns `0`, so the check passes.
17. It builds a new `Payment`:
    - `paymentId = "PAY-" + UUID`, e.g. `PAY-9c1f...`
    - `ticket = TKT-SEED1`
    - `amount = 20.00`
    - `createdAt = 2026-10-29T10:15:30`
18. Hibernate runs:
    ```sql
    INSERT INTO "Payment" ("PaymentID", "TicketID", "Amount", "CreatedAt")
    VALUES ('PAY-9c1f...', 'TKT-SEED1', 20.00, '2026-10-29 10:15:30');
    ```
19. `@Transactional` commits the transaction.
20. The controller returns status `201 Created` with the saved payment as JSON.
21. Swagger shows the response body and the code `201`.

If step 14 found a different id, the request would stop with 403. If step 15 found `15.00` instead of `20.00`, the request would stop with 400. Nothing would be inserted, because the exception rolls the transaction back.

---

## 10. JSON request and response examples

### 10.1 Create request

```http
POST /api/payments
Authorization: Basic cHJpeWE6UGFzc3cwcmQh
Content-Type: application/json
```

```json
{
  "ticketId": "TKT-SEED1",
  "amount": 20.00
}
```

| Field | Type | Required | Meaning |
|---|---|---|---|
| `ticketId` | string | yes | The ticket being paid. Maps to the transient field. |
| `amount` | number | yes | Money paid. Must be `>= 0.01` and equal the ticket fare. |

Do **not** send `paymentId`. It is ignored or overwritten, because the server generates it.

Do **not** send a nested `ticket` object. The field is `READ_ONLY`, so Jackson ignores it on input.

### 10.2 Create response — 201

```json
{
  "paymentId": "PAY-9c1f4d2e-6b7a-4c3d-9e10-2f8a7b6c5d4e",
  "ticket": {
    "ticketId": "TKT-SEED1",
    "passenger": {
      "userId": "PASS-SEED1",
      "firstName": "Priya",
      "lastName": "Sharma",
      "role": "PASSENGER",
      "contact": "priya",
      "registrationDate": "2026-04-29",
      "isActive": true,
      "hireDate": null
    },
    "fare": {
      "fareId": "FARE-STN-VSV-STN-WEH",
      "sourceStation": { "stationId": "STN-VSV", "stationCode": "VSV" },
      "destStation": { "stationId": "STN-WEH", "stationCode": "WEH" },
      "baseFare": 20.00
    },
    "ticketType": "SINGLE",
    "validUntil": "2026-10-29T12:15:30",
    "isUsed": true,
    "issueTime": "2026-10-28T10:15:30",
    "createdAt": "2026-10-28T10:15:30"
  },
  "amount": 20.00,
  "createdAt": "2026-10-29T10:15:30.123456"
}
```

The example is shortened: real nested objects contain every field of `Ticket`, `Fare`, `Station` and `User`. `password` never appears because it is `WRITE_ONLY` on `User`.

| Field | Meaning |
|---|---|
| `paymentId` | Server-generated primary key. |
| `ticket` | The full ticket row, loaded through the foreign key. |
| `ticket.passenger` | The owner. Notice there is no `password`. |
| `ticket.fare.baseFare` | The amount the payment had to match. |
| `amount` | What was actually paid. |
| `createdAt` | Server timestamp in ISO-8601 format. |

Notice that `ticketId` does **not** appear at the top level of the response. That is the effect of `WRITE_ONLY`. The id is still visible inside `ticket.ticketId`.

### 10.3 Read one — 200

```http
GET /api/payments/PAY-SEED1
```

```json
{
  "paymentId": "PAY-SEED1",
  "ticket": {
    "ticketId": "TKT-SEED1",
    "passenger": { "userId": "PASS-SEED1" },
    "fare": { "baseFare": 20.00 }
  },
  "amount": 20.00,
  "createdAt": "2026-10-28T10:15:30"
}
```

### 10.4 List by ticket — 200

```http
GET /api/payments/ticket/TKT-SEED1
```

```json
[
  {
    "paymentId": "PAY-SEED1",
    "ticket": { "ticketId": "TKT-SEED1" },
    "amount": 20.00,
    "createdAt": "2026-10-28T10:15:30"
  }
]
```

The result is an array, even when there is only one row.

### 10.5 Delete — 204

```http
DELETE /api/payments/PAY-SEED1
```

Response: status `204 No Content`, empty body.

---

## 11. Error table

All `IllegalArgumentException` errors become **400** with body `{"error": "..."}`. All `AccessDeniedException` errors become **403**, but the global handler replaces the specific message with a fixed one.

| When | Status | Body / message |
|---|---|---|
| No `Authorization` header | 401 | Spring Security default unauthorized response |
| Wrong username or password | 401 | Spring Security default unauthorized response |
| Role not allowed by `@PreAuthorize` | 403 | Spring Security access-denied response |
| Passenger reads someone else's payment | 403 | `{"error": "You do not have permission to access this resource"}` |
| Passenger pays for someone else's ticket | 403 | same fixed message |
| `ticketId` missing | 400 | `{"error": "Ticket ID is required"}` |
| Ticket id does not exist | 400 | `{"error": "Ticket not found"}` |
| `amount` missing | 400 | `{"amount": "Amount is required"}` |
| `amount` is `0` or negative | 400 | `{"amount": "Amount must be greater than zero"}` |
| Amount differs from fare | 400 | `{"error": "Payment amount does not match the ticket fare"}` |
| Read/delete an unknown payment id | 400 | `{"error": "Payment not found"}` |
| Logged-in contact no longer exists | 400 | `{"error": "Current user not found"}` |
| Unexpected crash | 500 | `{"error": "An unexpected error occurred: ..."}` |

Why "not found" is 400 and not 404:

- The controller throws `IllegalArgumentException`, and the global handler maps that to 400.
- It does **not** throw `ResourceNotFoundException`, which would map to 404.
- This is a real inconsistency in the code. If asked, say: "The project's convention maps these lookups to 400; only `ResourceNotFoundException` gives 404."

Why the 403 message is generic:

- `GlobalExceptionHandler.handleAccessDeniedException` ignores `ex.getMessage()`.
- It always returns "You do not have permission to access this resource".
- So the more specific controller text is for logs and developers, not for the client.

---

## 12. Relationships to other groups

```text
User (01) 1 ──── many Ticket (09)
                     │
                     │ 1
                     │
                   many Payment (10)
                     │
                     │ many-to-one
                     ▼
                   Fare (08) ── Station (03)
```

| Related group | How it connects |
|---|---|
| Ticket (file 09) | `Payment.ticket` is `@ManyToOne`. `Payment` cannot exist without a ticket. |
| Fare (file 08) | Reached through `ticket.fare`. `baseFare` is the price the payment must match. |
| User (file 01) | Reached through `ticket.passenger`. Used for the ownership check. |
| Station (file 03) | Reached through the ticket's source and destination. Not used by payment logic. |
| Auth and roles (file 02) | `@PreAuthorize` and `Authentication` decide who may create, list and delete. |
| MetroCard (file 11) | No direct link. A card top-up does **not** create a `Payment` row in this project. |

Direction of the foreign key:

- `Payment` holds `TicketID`.
- `Ticket` does not hold a payment id.
- So you can always go payment → ticket, but not ticket → payment without a query.
- The query is `findByTicket_TicketId`.

Deletion order matters:

- If you delete a ticket that still has payments, the foreign key blocks it.
- Delete the payments first, or the database throws a constraint error.

---

## 13. Swagger test steps

1. Start the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   On Windows:
   ```bat
   mvnw.cmd spring-boot:run
   ```
2. Open `http://localhost:8080/swagger-ui/index.html`.
3. Click **Authorize**.
4. Enter a seeded user:
   - passenger: `priya` / `Passw0rd!`
   - agent: `agent1` / `Passw0rd!`
   - admin: `admin` / `admin123`
5. Expand **payment-controller**.
6. Test create:
   - `POST /api/payments` → **Try it out**
   - body: `{"ticketId": "TKT-SEED1", "amount": 20.00}`
   - expect `201`
7. Test the amount rule:
   - same call with `"amount": 25.00`
   - expect `400` and "Payment amount does not match the ticket fare"
8. Test validation:
   - same call with `"amount": 0`
   - expect `400` and the field map `{"amount": "Amount must be greater than zero"}`
9. Test ownership:
   - log out, authorize as `arjun` / `Passw0rd!`
   - `GET /api/payments/PAY-SEED1` (Priya's payment)
   - expect `403`
10. Test the owner path:
    - authorize as `priya`, repeat the GET
    - expect `200`
11. Test the ticket list:
    - `GET /api/payments/ticket/TKT-SEED1`
    - expect an array with `PAY-SEED1`
12. Test role blocking:
    - authorize as `priya`
    - `GET /api/payments`
    - expect `403`
13. Test admin delete:
    - authorize as `admin`
    - `DELETE /api/payments/PAY-SEED1`
    - expect `204`
    - repeat the same delete and expect `400` "Payment not found"

---

## 14. How to explain this in 2 minutes

> "Payment is a small group: one entity, one repository, one controller.
>
> The entity has four database fields. `PaymentID` is the primary key. `TicketID` is a foreign key to the ticket. `Amount` is `BigDecimal`, so money is exact. `CreatedAt` is a timestamp.
>
> There is also a `ticketId` string marked `@Transient`. That means it is not a column. It exists only so the client can send `{"ticketId": "...", "amount": 20}` without sending a whole nested ticket. It is `WRITE_ONLY`, so it is accepted on input but hidden on output. The real `ticket` field is `READ_ONLY`, so input cannot fake a ticket.
>
> The repository is an interface extending `JpaRepository`. That gives us `save`, `findById`, `findAll`, `existsById` and `deleteById` for free. We add one derived query, `findByTicket_TicketId`. The underscore means 'go inside the ticket object and use its ticketId'. Spring generates `SELECT * FROM "Payment" WHERE "TicketID" = ?`.
>
> The controller has five endpoints. `POST /api/payments` creates a payment. It is limited to passengers and ticket agents. It requires a ticket id, loads the ticket, and then does two checks. First, if the caller is a passenger, the ticket must belong to them. Second, the amount must equal the ticket's fare exactly, compared with `compareTo`. Then it generates `PAY-` plus a UUID and saves. It returns 201.
>
> `GET /{id}` reads one payment and runs an ownership check. `GET` returns all payments but only for admin and agent. `GET /ticket/{ticketId}` lists payments for one ticket after checking that the ticket belongs to the passenger. `DELETE /{id}` is admin only and returns 204.
>
> Errors are centralized. `IllegalArgumentException` becomes 400 with an `error` field. `AccessDeniedException` becomes 403. Validation failures become 400 with a field-to-message map.
>
> The key design point is that payment follows the ticket. There is no passenger column here. Ownership is checked by walking payment → ticket → passenger."

---

## 15. Beginner Q&A

**Q1. What is an entity?**
A Java class that maps to one database table. Here `Payment` maps to the `"Payment"` table.

**Q2. What does `@Entity` do?**
It tells JPA to manage this class as a table. Hibernate then reads its fields and produces SQL.

**Q3. What does `@Table(name = "\"Payment\"")` mean?**
It sets the exact table name. The inner `\"` is an escaped double quote, because PostgreSQL needs quotes to keep the capital `P`.

**Q4. What is a primary key?**
The column that uniquely identifies a row. `PaymentID` is the primary key.

**Q5. What is a foreign key?**
A column that points to a primary key in another table. `TicketID` points to `Tickets.TicketID`.

**Q6. Why is the ticket field `@ManyToOne`?**
Many payment rows can point to the same ticket; each payment points to one ticket.

**Q7. What is `FetchType.LAZY`?**
The ticket is not loaded immediately. Hibernate waits until `getTicket()` is actually used, then runs a second SELECT. It saves work when the ticket is not needed.

**Q8. What is `@Transient`?**
"Do not store this field in the database." `ticketId` is only a helper for input.

**Q9. What is the difference between `@Transient` and `@JsonProperty(access = WRITE_ONLY)`?**
`@Transient` is about the database. `WRITE_ONLY` is about JSON. `ticketId` uses both: no column, and hidden from responses.

**Q10. Why can't I send a nested ticket object?**
Because the `ticket` field is `READ_ONLY`. Jackson ignores it on input. You must send `ticketId`.

**Q11. Why use `BigDecimal` for money?**
It stores exact decimals. `double` can produce values like `19.999999999` after arithmetic.

**Q12. What does `precision = 10, scale = 2` mean?**
At most 10 total digits, with 2 after the decimal point, so up to `99999999.99`.

**Q13. What does `@Valid` do?**
It runs the validation annotations before the method body. Without it, `@NotNull` on a request body would not be checked.

**Q14. Where does the "Amount is required" message come from?**
From `@NotNull(message = "Amount is required")` on the entity field.

**Q15. Why is `compareTo` used instead of `equals`?**
`equals` also compares scale, so `20.0` and `20.00` are not equal. `compareTo` compares numeric value only.

**Q16. What does `orElseThrow` do?**
It unwraps an `Optional`. If the box is empty, it throws the supplied exception.

**Q17. What is `@Transactional` doing here?**
It groups the database work into one unit. If an exception escapes, the insert is rolled back.

**Q18. Why `readOnly = true` on GET methods?**
It tells Hibernate no changes are expected, so it can skip dirty checking. It also documents intent.

**Q19. What is `ResponseEntity`?**
A wrapper for the HTTP response. It lets you set the status (`CREATED`, `ok`, `noContent`) and the body.

**Q20. Why 201 instead of 200 after create?**
201 means "a new resource was created". 200 means "here is the result of a successful read".

**Q21. What does `@PreAuthorize("hasAnyRole('PASSENGER', 'TICKET_AGENT')")` check?**
It checks the logged-in user's authorities. Spring adds `ROLE_` automatically, so it looks for `ROLE_PASSENGER` or `ROLE_TICKET_AGENT`.

**Q22. Why can't an admin create a payment?**
Because `ADMIN` is not in the `hasAnyRole` list on the create method. The code deliberately limits creation to passengers and ticket agents.

**Q23. How does the controller know who is logged in?**
Spring injects the `Authentication` object. `getName()` returns the contact, because `DbUserDetailsService` sets the username to the contact.

**Q24. Why load the `User` row instead of comparing contacts?**
The ticket stores `PassengerID`. Comparing primary keys is exact and does not break if a contact changes.

**Q25. Why does `currentUser` use `findByContact`?**
Because the login name is the contact. The method returns the full row so the code can read `userId`.

**Q26. What happens if a passenger tries to pay for another passenger's ticket?**
The ownership check throws `AccessDeniedException`, which becomes 403.

**Q27. What happens if the amount does not match?**
`IllegalArgumentException` is thrown, the transaction rolls back, and the client gets 400.

**Q28. Can one ticket have two payments?**
The database allows it, and `findByTicket_TicketId` returns a list. The controller does not block duplicates. In a real system you would add a uniqueness rule.

**Q29. Why does the response not show `ticketId` at the top level?**
Because the field is `WRITE_ONLY`. You can still see `ticket.ticketId` inside the nested ticket.

**Q30. Why is the 403 message not the specific controller message?**
The global handler always writes "You do not have permission to access this resource", ignoring the exception text.

**Q31. Why is a missing payment 400 and not 404?**
The code throws `IllegalArgumentException`, which the global handler maps to 400. Only `ResourceNotFoundException` maps to 404.

**Q32. Does deleting a payment affect the ticket?**
No. The foreign key points from payment to ticket. Deleting the payment only removes the payment row.

**Q33. What happens if I delete a ticket that has payments?**
The database rejects it, because the payment's foreign key would point to nothing.

**Q34. Why does the controller set `createdAt` when the field already has a default?**
The field default runs when the object is constructed. Setting it again is explicit and makes the intent visible. The seeder also uses the setter to create historical timestamps.

**Q35. What is the difference between `@PostMapping` and `@RequestMapping`?**
`@RequestMapping` sets a prefix for the whole class. `@PostMapping` maps one method to POST requests under that prefix.

---

## 16. Glossary

| Term | Meaning |
|---|---|
| **Annotation** | A `@word` that adds instructions for Spring, JPA or Jackson. |
| **Bean** | An object created and managed by Spring. |
| **BigDecimal** | Exact decimal class, used for money. |
| **Controller** | Class that receives HTTP requests and returns responses. |
| **Derived query** | Repository method whose name Spring converts into SQL. |
| **Dependency injection** | Spring passes required objects into a constructor instead of the class creating them. |
| **DTO** | Data transfer object. This project mostly uses entities directly instead. |
| **Entity** | Java class mapped to a database table. |
| **Fetch type** | When related data is loaded: eagerly now, lazily on first use. |
| **Foreign key** | Column pointing to another table's primary key. |
| **Hibernate** | The JPA implementation that generates and runs SQL. |
| **HTTP Basic** | Login by sending username and password in a header on every request. |
| **Jackson** | Library that converts Java objects to JSON and back. |
| **JPA** | Java standard for object-to-table mapping. |
| **JSON** | Text format for structured data. |
| **LAZY** | Load related data only when it is read. |
| **Optional** | A box that may or may not contain a value. |
| **Ownership check** | Code that compares the caller's id with the row owner's id. |
| **Primary key** | Column that uniquely identifies a row. |
| **`READ_ONLY`** | JSON access mode: output only, ignored on input. |
| **Repository** | Interface that provides database operations. |
| **ResponseEntity** | Object that carries HTTP status, headers and body. |
| **Role** | User label used for permission checks. |
| **Rollback** | Undo all changes in a failed transaction. |
| **Scale** | Number of digits after the decimal point. |
| **Spring Boot** | Framework that starts the app and wires components. |
| **Status code** | Three-digit result code of an HTTP request. |
| **Swagger UI** | Web page for exploring and testing the API. |
| **Transaction** | All-or-nothing group of database operations. |
| **`@Transient`** | "Not a database column." |
| **UUID** | Long random identifier, e.g. `9c1f4d2e-...`. |
| **Validation** | Checking input rules such as "not null" or "positive". |
| **`WRITE_ONLY`** | JSON access mode: input only, hidden from output. |

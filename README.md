<div align="center">

# 🚇 Metro Ticketing System

**A role-based REST API for metro transit management and digital ticketing.**

Manage stations, trains, routes, timetables, fares, smart cards, tickets and maintenance incidents, secured with HTTP Basic authentication and documented with live Swagger UI.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-migrations-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3-85EA2D?logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui/index.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

</div>

---

## 📌 Overview

The Metro Ticketing System is a **backend REST API** (no frontend) that covers day-to-day metro operations:

- **Passengers** book tickets, hold smart cards and check timetables.
- **Ticket agents** issue tickets and top up cards at the counter.
- **Maintenance staff** report and resolve faults on trains and stations.
- **Admins** manage the whole network, fares and staff.

It uses a deliberately simple layered design:

```
HTTP Basic Auth -> Controller (business logic) -> Repository -> PostgreSQL
```

- Controllers contain the business logic and transactions.
- Repositories handle all database access through Spring Data JPA.
- Entities are used directly as request and response bodies.
- Flyway creates the schema; a seeder loads demo data on first boot.

---

## ✨ Features

| Area | What you get |
| --- | --- |
| 🔐 **Authentication** | HTTP Basic authentication, BCrypt password hashing, database-backed users |
| 👥 **Role-Based Access Control** | `@PreAuthorize` rules for `ADMIN`, `TICKET_AGENT`, `MAINTENANCE_STAFF`, `PASSENGER` |
| 🚉 **Network & Fleet** | CRUD for stations (coordinates, line colours), trains (capacity, coaches), routes and ordered stops |
| 🕒 **Timetables** | Schedules linked to routes and trains, with overlap detection |
| 🎫 **Fares & Tickets** | Origin–destination fare lookup, digital tickets, use/validate lifecycle |
| 💳 **Smart Cards** | Card issuance, balance tracking and top-ups |
| 🛠️ **Maintenance** | Incident reporting for stations and trains with priority and resolution tracking |
| 📖 **API Docs** | Swagger UI with Basic Auth support |
| 🗄️ **Database** | Flyway migrations (`V1`–`V6`) and demo data seeding |

---

## 🧰 Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 (LTS) |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation) |
| Database | PostgreSQL 15+ |
| Migrations | Flyway |
| Security | Spring Security HTTP Basic, BCrypt |
| API Docs | SpringDoc OpenAPI |
| Build | Maven 3.9+ (Maven Wrapper included) |

---

## 🚀 Quick Start

### Prerequisites

- **JDK 21** — check with `java -version`
- **PostgreSQL 15+** running on port `5432`
- **Maven is optional**; use the included wrapper

### 1. Clone the repository

```bash
git clone https://github.com/arjunrhetoric/metro-app.git
cd metro-app
```

### 2. Database

The project is configured for a hosted Supabase PostgreSQL database by default. Flyway creates all tables on first startup.

Set the database password before running the app (ask the project owner for it):

```powershell
# PowerShell
$env:DB_PASSWORD = "your-supabase-database-password"
```

```bash
# Git Bash / Linux / macOS
export DB_PASSWORD="your-supabase-database-password"
```

Or create a `.env` file in the project root:

```properties
DB_URL=jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.mtpdfkyjrntiowofekwd
DB_PASSWORD=your-supabase-database-password
```

The application loads `.env` automatically if it exists. The file is gitignored, so each developer keeps their own copy.

Optional overrides:

- `DB_URL` — full JDBC URL
- `DB_USERNAME` — database user

To use a local PostgreSQL instead, create an empty database and set:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/metro_db"
export DB_USERNAME="postgres"
export DB_PASSWORD="your-local-password"
```

Flyway will create the schema in whichever empty database you point it at.

### 4. Start the application

```bash
./mvnw spring-boot:run          # Git Bash / Linux / macOS
.\mvnw.cmd spring-boot:run      # PowerShell / CMD
```

API: **http://localhost:8080**
Swagger UI: **http://localhost:8080/swagger-ui/index.html**

---

## 🔐 Authentication

The API uses **HTTP Basic authentication**. Send the database username (the `Contact` field) and password with every request.

### Demo accounts

Seeded automatically on first startup:

| Role | Username | Password | Can do |
| --- | --- | --- | --- |
| `ADMIN` | `admin` | `admin123` | Everything |
| `TICKET_AGENT` | `agent1` | `Passw0rd!` | Issue tickets, top up cards |
| `MAINTENANCE_STAFF` | `maint1` | `Passw0rd!` | Report/resolve faults |
| `PASSENGER` | `priya` | `Passw0rd!` | Book tickets, own cards/payments |
| `PASSENGER` | `arjun` | `Passw0rd!` | Book tickets, own cards/payments |

Register a new passenger with `POST /api/auth/passenger/register`, then use the same contact and password as Basic credentials.

### Swagger UI

1. Open http://localhost:8080/swagger-ui/index.html
2. Click **Authorize**.
3. Enter username and password (for example `admin` / `admin123`).
4. Call any endpoint.

### cURL

```bash
# Admin
curl -u admin:admin123 http://localhost:8080/api/stations

# Passenger
curl -u priya:Passw0rd! http://localhost:8080/api/tickets/passenger/PASS-SEED1
```

Use an incognito window or restart the browser to switch users, because browsers cache Basic credentials.

---

## 📖 API Documentation

| Resource | URL |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

### Main endpoints

| Area | Base path |
| --- | --- |
| Auth (register/hire/me) | `/api/auth` |
| Stations | `/api/stations` |
| Trains | `/api/trains` |
| Routes | `/api/routes` |
| Schedules | `/api/schedules` |
| Fares | `/api/fares` |
| Tickets | `/api/tickets` |
| Metro cards | `/api/cards` |
| Payments | `/api/payments` |
| Maintenance | `/api/maintenance` |

Example ticket request (passenger is taken from the authenticated user):

```bash
curl -u priya:Passw0rd! -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"sourceStationId":"STN-VSV","destStationId":"STN-GHP","ticketType":"SINGLE"}'
```

Top-up uses a query parameter:

```bash
curl -u priya:Passw0rd! -X POST \
  "http://localhost:8080/api/cards/CARD-SEED1/topup?amount=100"
```

---

## 🏛️ Project Structure

```text
metro-ticketing-app/
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/metro/ticketing/
    │   │   ├── TicketingApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java          HTTP Basic, roles, BCrypt
    │   │   │   ├── DbUserDetailsService.java    loads users from the database
    │   │   │   ├── DataSeeder.java              admin + demo data
    │   │   │   ├── FlywayConfig.java            runs migrations before JPA
    │   │   │   └── OpenApiConfig.java           Swagger Basic Auth scheme
    │   │   ├── controller/                      10 controllers (business logic)
    │   │   ├── entity/                          11 entities + Role enum
    │   │   ├── repository/                      11 Spring Data repositories
    │   │   └── exception/
    │   │       └── GlobalExceptionHandler.java  JSON error responses
    │   └── resources/
    │       ├── application.properties
    │       └── db/migration/                    Flyway V1–V6
```

### Request flow

1. Spring Security reads the `Authorization: Basic ...` header.
2. `DbUserDetailsService` loads the user by `Contact` and BCrypt checks the password.
3. The controller method runs; `@PreAuthorize` checks the role.
4. The controller executes business logic inside `@Transactional`.
5. Repositories read/write PostgreSQL through Hibernate.
6. `GlobalExceptionHandler` converts errors to JSON.

---

## ⚙️ Configuration

| Property | Environment variable | Default |
| --- | --- | --- |
| `spring.datasource.url` | `DB_URL` | `jdbc:postgresql://localhost:5432/metro_db` |
| `spring.datasource.username` | `DB_USERNAME` | `postgres` |
| `spring.datasource.password` | `DB_PASSWORD` | `@Arjun1221` |
| `app.bootstrap.admin.contact` | `ADMIN_CONTACT` | `admin` |
| `app.bootstrap.admin.password` | `ADMIN_PASSWORD` | `admin123` |
| `app.bootstrap.admin.first-name` | `ADMIN_FIRST_NAME` | `System` |
| `app.bootstrap.admin.last-name` | `ADMIN_LAST_NAME` | `Administrator` |

JPA is set to `ddl-auto=validate`; Flyway owns the schema. `open-in-view=true` allows entities with lazy relations to be serialized safely.

---

## 🔒 Security Notes

Before deploying publicly:

- [ ] Change the demo passwords and the bootstrap admin password.
- [ ] Use a dedicated database user with limited privileges.
- [ ] Enable HTTPS; Basic credentials are only safe over TLS.
- [ ] Remove or disable the demo data seeder.
- [ ] Set `spring.jpa.show-sql=false`.

---

## 🩺 Troubleshooting

| Problem | Fix |
| --- | --- |
| `Connection refused` | PostgreSQL is not running or not on port `5432`. Check `DB_URL`. |
| `password authentication failed` | `DB_PASSWORD` does not match your PostgreSQL user. |
| `database "metro_db" does not exist` | Run `CREATE DATABASE metro_db;`. |
| `Unsupported class file major version` | Use JDK 21. Check `java -version` and `JAVA_HOME`. |
| `401 Unauthorized` | Wrong username/password, or none supplied. Use `-u user:pass`. |
| `403 Forbidden` | The authenticated role is not allowed to call that endpoint. |
| Port `8080` already in use | Run with `--server.port=9090`. |
| Browser keeps old credentials | Use an incognito window or clear the site's auth cache. |

---

## 📄 License

Distributed under the Apache License 2.0. See [`LICENSE`](LICENSE).

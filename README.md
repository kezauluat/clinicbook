# ClinicBook: Clinic Appointment Booking System

Web Technology project · AUCA · 2026–2027 · Instructor: Jeremie U. Tuyisenge

Patients in Rwandan private clinics usually book by walking in or calling, which causes long queues, double-booked doctors and missed visits. **ClinicBook** lets patients book a free doctor slot online, sends email/SMS confirmations and reminders, and gives clinic staff one dashboard to manage schedules.

## Features

| Role | What they can do |
| --- | --- |
| Patient | Register / log in (email or Google), search doctors, book a free 30-min slot, reschedule, cancel (up to 2 h before), read visit notes |
| Doctor | Set weekly working hours, see the daily schedule, write visit notes and prescriptions |
| Receptionist | See the day's appointments, check patients in, cancel, book for walk-in patients |
| Admin | Create clinics, departments, doctors and receptionists; enable/disable accounts |

## Tech stack

| Layer | Technology |
| --- | --- |
| Frontend | React 19, Vite, Tailwind CSS 4, React Router, Axios |
| Backend | Spring Boot 4 (Java 17), layered architecture (controller → service → repository) |
| Relational DB | PostgreSQL 17 (users, roles, clinics, doctors, availability, appointments) |
| NoSQL DB | MongoDB 7 (visit notes, notification logs, audit logs with 180-day TTL) |
| Cache | Redis (doctor search, clinics, departments; 10-min TTL) |
| Messaging | RabbitMQ topic exchange `clinic.events` → email + SMS queues, retry ×3, dead-letter queue |
| Security | Spring Security, JWT (15-min access + rotating refresh tokens), Google OAuth2, BCrypt, RBAC |
| DevOps | Docker multi-stage images, Docker Compose, GitHub Actions CI → GHCR |
| Testing | JUnit 5, Mockito, AssertJ |

## Architecture

```
React SPA --HTTPS + JWT--> Spring Boot API --> PostgreSQL / MongoDB / Redis
                                 |
                                 +-- events --> RabbitMQ --> Notification consumer --> Email (SMTP) + SMS
```

Booking logic is synchronous; notifications are event-driven and only published **after** the database transaction commits, so a slow mail server never blocks a booking.

**Double-booking protection (3 layers):** a service check, a PostgreSQL partial unique index `(doctor_id, start_at) WHERE status <> 'CANCELLED'`, and optimistic locking (`@Version`).

## Run it

Requirements: Docker Desktop.

```bash
# whole system (DBs, RabbitMQ, Mailpit, backend, frontend)
docker compose --profile app up -d --build
```

| Service | URL |
| --- | --- |
| Web app | http://localhost:3000 |
| API | http://localhost:8080/api/v1 |
| Mailpit (test inbox) | http://localhost:8025 |
| RabbitMQ dashboard | http://localhost:15672 (clinic / clinic123) |

### Development mode

```bash
docker compose up -d                         # databases + RabbitMQ + Mailpit only
cd backend && ./mvnw spring-boot:run         # API on :8080
cd frontend && npm install && npm run dev    # UI on :5173
```

### Demo accounts (seeded on first start)

| Role | Email | Password |
| --- | --- | --- |
| Admin | admin@clinicbook.rw | Admin@12345 |
| Doctor | doctor@clinicbook.rw | Doctor@12345 |
| Receptionist | reception@clinicbook.rw | Reception@12345 |
| Patient | register at /register | (your own) |

### Google login (optional)

Create an OAuth client in Google Cloud Console with redirect URI `http://localhost:8080/login/oauth2/code/google`, then set `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` before starting the backend.

## Main API endpoints

| Method | Path | Access |
| --- | --- | --- |
| POST | /api/v1/auth/register, /login, /refresh, /logout | Public |
| GET | /oauth2/authorization/google | Public |
| GET | /api/v1/doctors, /doctors/{id}/slots?date= | Public |
| POST | /api/v1/appointments | PATIENT, RECEPTIONIST |
| GET | /api/v1/appointments/my | PATIENT |
| PATCH | /api/v1/appointments/{id}/cancel, /reschedule | Owner, RECEPTIONIST, ADMIN |
| PATCH | /api/v1/appointments/{id}/check-in | RECEPTIONIST |
| GET/POST | /api/v1/doctor/availability, /doctor/appointments, /doctor/appointments/{id}/notes | DOCTOR |
| GET | /api/v1/patients?q= | RECEPTIONIST, ADMIN |
| ALL | /api/v1/admin/** | ADMIN |

## Tests and CI

```bash
cd backend && ./mvnw test
```

GitHub Actions (`.github/workflows/ci.yml`) runs on every push and pull request: backend tests and packaging, frontend lint and build, and on `main` it builds and pushes Docker images to GitHub Container Registry.

## Git workflow

`main` (releases) ← `develop` (integration) ← `feature/*` branches, merged through pull requests with conventional commit messages.

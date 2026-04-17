# Event Registration System Team Implementation Plan

## Team Ownership

### Person 1: Auth Service Owner
- Service: `auth-service`
- Owns:
  - user registration
  - login
  - JWT generation and validation
  - role-based authorization rules
  - user and role database tables
  - auth tests

### Person 2: Event Service Owner
- Service: `event-service`
- Owns:
  - event CRUD
  - seat limits
  - event cancellation
  - event rescheduling
  - organizer permissions
  - event database tables
  - event tests

### Person 3: Registration Service Owner
- Service: `registration-service`
- Owns:
  - attendee registration
  - registration cancellation
  - participant tracking
  - overbooking prevention
  - registration database tables
  - registration tests

### Person 4: Notification Service Owner
- Service: `notification-service`
- Owns:
  - notification creation
  - notification retrieval
  - read status updates
  - event/registration notification triggers
  - notification database tables
  - notification tests

### Person 5: Platform and Integration Owner
- Services:
  - `api-gateway`
  - `config-server`
  - `eureka-server`
- Also owns:
  - `docker-compose.yml`
  - shared environment configuration
  - root `pom.xml`
  - integration testing
  - GitHub merge hygiene

## Main Rules

- Each person should work only inside their own service folder.
- Do not let multiple people edit the same service unless necessary.
- Do not let multiple services share the same database tables.
- Do not read another service database directly.
- All cross-service communication must happen through APIs.
- Merge through pull requests only.
- Keep PRs small and focused.

## Correct Build Order

### Phase 1: Platform Baseline
- Person 5 builds:
  - `config-server`
  - `eureka-server`
  - `api-gateway`
  - Docker setup
  - PostgreSQL setup

### Phase 2: Authentication
- Person 1 builds:
  - user registration
  - login
  - JWT
  - roles
  - authorization basics

### Phase 3: Event Management
- Person 2 builds:
  - event creation
  - event update
  - event cancellation
  - event rescheduling
  - event listing
  - seat limits

### Phase 4: Registration Logic
- Person 3 builds:
  - register to event
  - cancel registration
  - participant tracking
  - registration count

### Phase 5: Notifications
- Person 4 builds:
  - registration notifications
  - cancellation notifications
  - reschedule notifications

### Phase 6: Final Integration
- Person 5 integrates all services through:
  - gateway routes
  - Docker Compose
  - env vars
  - end-to-end checks

## Dependency Order

- `config-server` has no dependency on business services.
- `eureka-server` depends on `config-server`.
- `api-gateway` depends on `config-server` and `eureka-server`.
- `auth-service` depends on `config-server` and `eureka-server`.
- `event-service` depends on `config-server`, `eureka-server`, and auth rules.
- `registration-service` depends on:
  - `auth-service`
  - `event-service`
- `notification-service` depends on:
  - `event-service`
  - `registration-service`
  - optionally `auth-service`

## Shared Contracts That Must Be Fixed Early

### Roles
- `ADMIN`
- `ORGANIZER`
- `PARTICIPANT`

### JWT Claims
- `sub`
- `userId`
- `roles`

### Event Status
- `SCHEDULED`
- `CANCELLED`
- `RESCHEDULED`

### Registration Status
- `REGISTERED`
- `CANCELLED`

### Common Error Response
```json
{
  "timestamp": "2026-04-16T19:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Event is full",
  "path": "/api/registrations"
}
```

## Database Ownership

### Auth Database: `auth_db`
- Owner: Person 1
- Tables:
  - `users`
  - `roles`
  - `user_roles`
  - optional `refresh_tokens`

### Event Database: `event_db`
- Owner: Person 2
- Tables:
  - `events`

### Registration Database: `registration_db`
- Owner: Person 3
- Tables:
  - `registrations`

### Notification Database: `notification_db`
- Owner: Person 4
- Tables:
  - `notifications`

## Database Rules

- Every service owns its own database.
- No joins across services.
- No shared business tables.
- Every schema change should be added through migration files.
- Do not edit the database manually and then try to remember the change.

## Suggested Core Table Fields

### `users`
- `id`
- `full_name`
- `email`
- `password_hash`
- `status`
- `created_at`

### `roles`
- `id`
- `name`

### `user_roles`
- `user_id`
- `role_id`

### `events`
- `id`
- `title`
- `description`
- `location`
- `start_time`
- `end_time`
- `max_seats`
- `status`
- `organizer_id`
- `created_at`
- `updated_at`

### `registrations`
- `id`
- `event_id`
- `participant_id`
- `status`
- `registered_at`
- `cancelled_at`

### `notifications`
- `id`
- `user_id`
- `type`
- `title`
- `message`
- `read`
- `created_at`

## Exact Minimum Endpoints

## Auth Service Endpoints

### Public
- `POST /api/auth/register`
- `POST /api/auth/login`

### Protected
- `GET /api/auth/me`
- `GET /api/auth/users/{userId}`
- `GET /api/auth/users/{userId}/roles`
- `GET /api/auth/validate`

### Register Request
```json
{
  "fullName": "Ali Hassan",
  "email": "ali@example.com",
  "password": "Secret123!",
  "role": "PARTICIPANT"
}
```

### Register Response
```json
{
  "id": 1,
  "fullName": "Ali Hassan",
  "email": "ali@example.com",
  "role": "PARTICIPANT"
}
```

### Login Request
```json
{
  "email": "ali@example.com",
  "password": "Secret123!"
}
```

### Login Response
```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "ali@example.com",
  "roles": ["PARTICIPANT"]
}
```

## Event Service Endpoints

- `POST /api/events`
- `GET /api/events`
- `GET /api/events/{eventId}`
- `PUT /api/events/{eventId}`
- `PATCH /api/events/{eventId}/cancel`
- `PATCH /api/events/{eventId}/reschedule`
- `GET /api/events/{eventId}/availability`

### Create Event Request
```json
{
  "title": "Spring Boot Workshop",
  "description": "Hands-on workshop",
  "location": "Cairo Hall A",
  "startTime": "2026-05-01T10:00:00",
  "endTime": "2026-05-01T13:00:00",
  "maxSeats": 100,
  "organizerId": 2
}
```

### Create Event Response
```json
{
  "id": 10,
  "title": "Spring Boot Workshop",
  "status": "SCHEDULED",
  "availableSeats": 100
}
```

### Event Details Response
```json
{
  "id": 10,
  "title": "Spring Boot Workshop",
  "description": "Hands-on workshop",
  "location": "Cairo Hall A",
  "startTime": "2026-05-01T10:00:00",
  "endTime": "2026-05-01T13:00:00",
  "maxSeats": 100,
  "registeredCount": 0,
  "availableSeats": 100,
  "status": "SCHEDULED",
  "organizerId": 2
}
```

### Availability Response
```json
{
  "eventId": 10,
  "status": "SCHEDULED",
  "maxSeats": 100,
  "registeredCount": 35,
  "availableSeats": 65,
  "registrationOpen": true
}
```

## Registration Service Endpoints

- `POST /api/registrations`
- `GET /api/registrations/{registrationId}`
- `GET /api/registrations/me`
- `GET /api/registrations/events/{eventId}`
- `DELETE /api/registrations/{registrationId}`
- `GET /api/registrations/events/{eventId}/count`

### Registration Request
```json
{
  "eventId": 10,
  "participantId": 1
}
```

### Registration Response
```json
{
  "id": 100,
  "eventId": 10,
  "participantId": 1,
  "status": "REGISTERED",
  "registeredAt": "2026-04-16T19:10:00"
}
```

### Event Registrations Response
```json
[
  {
    "id": 100,
    "eventId": 10,
    "participantId": 1,
    "status": "REGISTERED",
    "registeredAt": "2026-04-16T19:10:00"
  }
]
```

### Registration Count Response
```json
{
  "eventId": 10,
  "registeredCount": 35
}
```

## Notification Service Endpoints

- `POST /api/notifications`
- `GET /api/notifications/users/{userId}`
- `PATCH /api/notifications/{notificationId}/read`

### Internal Integration Endpoints
- `POST /api/notifications/internal/registration-created`
- `POST /api/notifications/internal/registration-cancelled`
- `POST /api/notifications/internal/event-cancelled`
- `POST /api/notifications/internal/event-rescheduled`

### Notification Request
```json
{
  "userId": 1,
  "type": "REGISTRATION_CONFIRMED",
  "title": "Registration Confirmed",
  "message": "You are registered for Spring Boot Workshop"
}
```

### Notification Response
```json
[
  {
    "id": 501,
    "userId": 1,
    "type": "REGISTRATION_CONFIRMED",
    "title": "Registration Confirmed",
    "message": "You are registered for Spring Boot Workshop",
    "read": false,
    "createdAt": "2026-04-16T19:20:00"
  }
]
```

## API Gateway Routes

- `/api/auth/**` -> `auth-service`
- `/api/events/**` -> `event-service`
- `/api/registrations/**` -> `registration-service`
- `/api/notifications/**` -> `notification-service`

## Gateway Access Rules

### Public Routes
- `POST /api/auth/register`
- `POST /api/auth/login`

### Protected Routes
- all other routes

### Role Permissions
- `ADMIN`
  - full access
- `ORGANIZER`
  - create, update, cancel, reschedule events
  - view participants
- `PARTICIPANT`
  - browse events
  - register for events
  - cancel own registration
  - read own notifications

## What Each Person Needs From Others

### Person 1 Needs
- nothing to start

### Person 1 Delivers
- JWT format
- user identity contract
- role names
- auth endpoints

### Person 2 Needs From Person 1
- JWT claims format
- organizer/admin role rules
- current user identity usage

### Person 2 Delivers
- event details
- event availability
- event state changes

### Person 3 Needs From Person 1
- authenticated user identity
- participant role rules

### Person 3 Needs From Person 2
- `GET /api/events/{eventId}`
- `GET /api/events/{eventId}/availability`

### Person 3 Delivers
- registration created/cancelled info
- participant counts

### Person 4 Needs From Person 2
- event cancelled payload
- event rescheduled payload

### Person 4 Needs From Person 3
- registration created payload
- registration cancelled payload

### Person 4 Needs From Person 1
- optional user lookup contract

### Person 5 Needs From Everyone
- final endpoint paths
- service names
- ports
- env vars

## GitHub Workflow

- branch per owner:
  - `feature/auth-service`
  - `feature/event-service`
  - `feature/registration-service`
  - `feature/notification-service`
  - `feature/platform`
- no direct push to `main`
- merge through PRs only
- one reviewer minimum
- squash merge preferred
- sync with `main` daily

## PR Checklist

- builds locally
- no unrelated file changes
- config updated if needed
- endpoint contract is respected
- test added or testing gap stated

## Week 1 Suggestion

### Person 5
- finish platform configuration
- verify Docker boot order
- verify gateway routes

### Person 1
- user entity
- role model
- register/login endpoints
- JWT utility

### Person 2
- event entity
- event DTOs
- create/list/get endpoints

### Person 3
- registration entity
- registration DTOs
- service skeleton

### Person 4
- notification entity
- notification DTOs
- notification service skeleton

## Week 2 Suggestion

### Person 1
- finalize authorization rules
- `/me` and validate endpoints

### Person 2
- update/cancel/reschedule logic
- organizer permissions

### Person 3
- integrate registration with event checks
- add cancellation and count endpoints

### Person 4
- integrate notification triggers
- read/update endpoints

### Person 5
- full integration
- route protection
- Docker full-run verification

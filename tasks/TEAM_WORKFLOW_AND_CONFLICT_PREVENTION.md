# Team Workflow and Conflict Prevention Guide

## Goal

This document explains how the 5 team members should work on this project on GitHub with the lowest possible chance of:
- merge conflicts
- broken integration
- duplicated work
- inconsistent APIs
- inconsistent databases

This does not guarantee zero problems, but if the team follows it strictly, most avoidable problems should not happen.

## Core Rule

Split work by **microservice ownership**, not by technical layer.

Correct:
- one person owns `auth-service`
- one person owns `event-service`
- one person owns `registration-service`
- one person owns `notification-service`
- one person owns platform and integration

Wrong:
- one person does all controllers
- one person does all entities
- one person does all repositories

The wrong split causes constant merge conflicts because many people edit the same files and same folders.

## Ownership Rules

### Person 1
- Owns `auth-service`
- Must avoid editing other business services unless agreed

### Person 2
- Owns `event-service`
- Must avoid editing other business services unless agreed

### Person 3
- Owns `registration-service`
- Must avoid editing other business services unless agreed

### Person 4
- Owns `notification-service`
- Must avoid editing other business services unless agreed

### Person 5
- Owns:
  - `api-gateway`
  - `config-server`
  - `eureka-server`
  - `docker-compose.yml`
  - root `pom.xml`
  - integration work

## File Boundaries

Each person should normally change only:

### Service owner files
- `src/main/java/com/event/<their-service>/**`
- `src/main/resources/**`
- `src/test/**`
- their service `pom.xml`

### Platform owner files
- `api-gateway/**`
- `config-server/**`
- `eureka-server/**`
- root `pom.xml`
- `docker-compose.yml`
- Docker and integration files

If someone needs to edit another person’s service:
- ask first
- state why
- keep the PR very small

## GitHub Branch Strategy

Use one permanent feature branch per owner:

- `feature/auth-service`
- `feature/event-service`
- `feature/registration-service`
- `feature/notification-service`
- `feature/platform`

Optional short-lived branches for a small task are also fine:
- `feature/auth-jwt`
- `feature/event-cancel-endpoint`

## Main Git Rules

- never push directly to `main`
- merge only through pull requests
- every PR should be reviewed
- squash merge is preferred
- sync from `main` daily
- do not keep huge long-running branches without syncing

## Daily Workflow

Each team member should follow this sequence:

1. pull latest `main`
2. update their branch from `main`
3. make changes only in owned files
4. test locally
5. open a small PR
6. get review
7. merge
8. pull latest `main` again before starting the next task

## Pull Request Rules

Each PR should contain one focused change only.

Good PR examples:
- add login endpoint
- add event creation entity and DTO
- add registration table migration
- add notification read endpoint

Bad PR examples:
- login + event CRUD + Docker edits + config changes
- “full project update”

## PR Checklist

Before opening a PR, check:

- project builds locally
- no unrelated files changed
- no `target/` files included
- no IDE files included
- endpoint path matches team contract
- database changes have a migration file
- env/config changes are documented
- response format matches agreed structure

## Contract-First Rule

Before implementation starts, the team must agree on these shared contracts:

### Roles
- `ADMIN`
- `ORGANIZER`
- `PARTICIPANT`

### JWT claims
- `sub`
- `userId`
- `roles`

### Event status
- `SCHEDULED`
- `CANCELLED`
- `RESCHEDULED`

### Registration status
- `REGISTERED`
- `CANCELLED`

### Common error response
```json
{
  "timestamp": "2026-04-16T19:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Event is full",
  "path": "/api/registrations"
}
```

If these are not fixed early, the services may compile but still fail to integrate.

## API Coordination Rules

Whenever one service depends on another, both owners must agree on:
- endpoint path
- request JSON
- response JSON
- status codes
- error response

### Required dependency agreements

#### Auth -> Everyone
Person 1 must publish:
- login response shape
- JWT claim structure
- `/me` behavior
- role naming

#### Event -> Registration
Person 2 and Person 3 must agree on:
- `GET /api/events/{eventId}`
- `GET /api/events/{eventId}/availability`

#### Registration -> Notification
Person 3 and Person 4 must agree on:
- registration-created payload
- registration-cancelled payload

#### Event -> Notification
Person 2 and Person 4 must agree on:
- event-cancelled payload
- event-rescheduled payload

## Database Conflict Prevention

This project already uses one database per service:

- `auth_db`
- `event_db`
- `registration_db`
- `notification_db`

This must not change.

### Strict database rules

- each service owner edits only their own database schema
- no service reads another service database directly
- no shared business tables between services
- no manual schema changes in pgAdmin
- every schema change must be a Flyway migration

### Migration rules

Migration files must be added only in the owner’s service:
- `auth-service/src/main/resources/db/migration`
- `event-service/src/main/resources/db/migration`
- `registration-service/src/main/resources/db/migration`
- `notification-service/src/main/resources/db/migration`

Use timestamp-style names:
- `V20260416_203500__init_auth_schema.sql`
- `V20260417_101000__add_user_status.sql`

This reduces version-number collisions.

## Config Conflict Prevention

To avoid config conflicts:

- Person 5 owns shared platform config
- business service owners should not casually edit:
  - root `pom.xml`
  - `docker-compose.yml`
  - gateway routes
  - config server shared files

If a service needs new config:
- the service owner asks Person 5
- or opens a very small PR only for that config addition

## Integration Order

The team should not integrate randomly.

Correct order:

1. platform baseline
2. auth-service
3. event-service
4. registration-service
5. notification-service
6. final gateway and end-to-end verification

## Team Sync Meetings

Have a short sync every working day or every other day.

Each person answers:
- what I finished
- what I am changing now
- what files I will touch next
- what contract I need from someone else
- what is blocking me

This prevents silent divergence.

## What To Do When Blocked

If a person depends on another unfinished service:

### Use temporary stubs
- define DTOs
- define interfaces
- define expected payloads
- do not invent different contracts

### Example
If `registration-service` needs event availability but `event-service` is not ready:
- Person 3 creates a client interface or placeholder service
- uses the already agreed response format
- waits for Person 2 to match that contract

## What Not To Do

- do not rename package structures without telling the team
- do not change endpoint paths casually
- do not change role names after others started coding
- do not change JSON field names without agreement
- do not edit another service “just quickly” and merge it silently
- do not commit `target/`, `.idea/`, or local-only files
- do not manually create database tables outside migration files

## Merge Conflict Handling

If a merge conflict happens:

1. stop and identify whether it is:
   - real logic conflict
   - formatting-only conflict
   - file ownership conflict
2. if it is another owner’s area, ask that owner before resolving
3. prefer rebasing from latest `main`
4. do not blindly accept both changes without understanding them

If two people constantly conflict in one area, the task split is wrong and must be corrected.

## Recommended Team Discipline

- one owner per service
- one owner per DB schema
- one owner for platform
- contract first
- migrations only
- PRs small
- sync often
- integrate in order

## Best Practical Summary

If the team wants the safest workflow, follow these five rules:

1. each person edits only their own microservice
2. no direct push to `main`
3. no manual database changes outside Flyway migrations
4. no API contract changes without team agreement
5. merge small PRs frequently instead of giant PRs late

If these are followed, future conflicts should stay limited and manageable.

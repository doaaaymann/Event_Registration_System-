# Auth Service Postman Testing Guide

## How To Know The Base URL

The base URL comes from the port mapping in `docker-compose.yml`.

For `auth-service`, Docker maps:
- container port `8081`
- to your machine port `8081`

So the direct auth-service base URL is:

```text
http://localhost:8081
```

If you want to test through the API Gateway instead, use:

```text
http://localhost:8080
```

For now, test `auth-service` directly first:

```text
http://localhost:8081
```

## Postman Variables

Create a Postman environment with:

- `baseUrl = http://localhost:8081`
- `token =`
- `participantToken =`
- `participantId =`

## Request 1: Login Admin

- Method: `POST`
- URL: `{{baseUrl}}/api/auth/login`

Headers:
- `Content-Type: application/json`

Body:
```json
{
  "email": "admin@event.local",
  "password": "Admin12345"
}
```

Expected:
- `200 OK`

Example response:
```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "admin@event.local",
  "roles": ["ADMIN"]
}
```

In Postman `Tests` tab:
```javascript
const json = pm.response.json();
pm.environment.set("token", json.accessToken);
```

## Request 2: Register Participant

- Method: `POST`
- URL: `{{baseUrl}}/api/auth/register`

Headers:
- `Content-Type: application/json`

Body:
```json
{
  "fullName": "Ali Hassan",
  "email": "ali@example.com",
  "password": "Secret123",
  "role": "PARTICIPANT"
}
```

Expected:
- `201 Created`

Example response:
```json
{
  "id": 2,
  "fullName": "Ali Hassan",
  "email": "ali@example.com",
  "status": "ACTIVE",
  "roles": ["PARTICIPANT"]
}
```

In Postman `Tests` tab:
```javascript
const json = pm.response.json();
pm.environment.set("participantId", json.id);
```

## Request 3: Reject Public Organizer Registration

- Method: `POST`
- URL: `{{baseUrl}}/api/auth/register`

Body:
```json
{
  "fullName": "Omar Organizer",
  "email": "omar@example.com",
  "password": "Secret123",
  "role": "ORGANIZER"
}
```

Expected:
- `400 Bad Request`

## Request 4: Login Participant

- Method: `POST`
- URL: `{{baseUrl}}/api/auth/login`

Headers:
- `Content-Type: application/json`

Body:
```json
{
  "email": "ali@example.com",
  "password": "Secret123"
}
```

Expected:
- `200 OK`

In Postman `Tests` tab:
```javascript
const json = pm.response.json();
pm.environment.set("participantToken", json.accessToken);
```

## Request 5: Get Current User

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/me`

Headers:
- `Authorization: Bearer {{participantToken}}`

Expected:
- `200 OK`

## Request 6: Validate Participant Token

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/validate`

Headers:
- `Authorization: Bearer {{participantToken}}`

Expected:
- `200 OK`

Expected response shape:
```json
{
  "valid": true,
  "userId": 2,
  "email": "ali@example.com",
  "roles": ["PARTICIPANT"]
}
```

## Request 7: Get Participant By ID With Participant Token

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/users/{{participantId}}`

Headers:
- `Authorization: Bearer {{participantToken}}`

Expected:
- `200 OK`

## Request 8: Get Participant Roles With Participant Token

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/users/{{participantId}}/roles`

Headers:
- `Authorization: Bearer {{participantToken}}`

Expected:
- `200 OK`

## Request 9: Get Another User As Participant

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/users/1`

Headers:
- `Authorization: Bearer {{participantToken}}`

Expected:
- `403 Forbidden`

## Request 10: Get Participant As Admin

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/users/{{participantId}}`

Headers:
- `Authorization: Bearer {{token}}`

Expected:
- `200 OK`

## Request 11: Get Roles As Admin

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/users/{{participantId}}/roles`

Headers:
- `Authorization: Bearer {{token}}`

Expected:
- `200 OK`

## Request 12: Unauthorized Without Token

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/me`

No Authorization header.

Expected:
- `401 Unauthorized`

## Request 13: Unauthorized With Invalid Token

- Method: `GET`
- URL: `{{baseUrl}}/api/auth/me`

Headers:
- `Authorization: Bearer abc.def.ghi`

Expected:
- `401 Unauthorized`

## Recommended Testing Order

1. Login admin
2. Register participant
3. Reject organizer public registration
4. Login participant
5. Get current user
6. Validate token
7. Get participant by own id
8. Get participant roles by own id
9. Try forbidden access
10. Use admin to access participant data
11. Test missing token
12. Test invalid token

## Fast Base URL Check

If you forget the base URL:

- direct auth service:
  - `http://localhost:8081`
- through gateway:
  - `http://localhost:8080`

You can also confirm from `docker-compose.yml`:

```yaml
auth-service:
  ports:
    - "8081:8081"
```

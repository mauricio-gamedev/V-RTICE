# VÉRTICE API — Identity v1

Base path: `/v1`

All request and response bodies use JSON. Authenticated routes require:

`Authorization: Bearer <accessToken>`

## Register

`POST /v1/auth/register`

Request:

```json
{
  "email": "person@example.com",
  "username": "person_01",
  "displayName": "Person",
  "invitationCode": "<beta-invite-code>",
  "password": "at-least-10-characters"
}
```

Rules:
- Email is trimmed and normalized to lowercase.
- Username is lowercase, 3–24 characters, letters/numbers/underscore only.
- Display name is 2–40 characters.
- Password is 10–128 characters.
- Email and username are unique.
- A valid environment invitation code is required for beta registration.

Success: `201 Created` with the auth response below. Duplicate email or username: `409 Conflict`. Missing or invalid invitation code: `403 Forbidden`.

## Login

`POST /v1/auth/login`

Request:

```json
{
  "identifier": "person_01",
  "password": "at-least-10-characters"
}
```

The identifier may be the email address or username. Invalid credentials return a generic `401 Unauthorized`.

## Refresh

`POST /v1/auth/refresh`

Request:

```json
{
  "refreshToken": "<refresh-token>"
}
```

Success returns a new access token and a new refresh token. The previous refresh token is invalidated by an atomic database update. Reuse returns `401 Unauthorized`.

## Current profile

`GET /v1/me`

Requires an access token. Returns the authenticated user's public profile fields: id, email, username, displayName, and createdAt.

## Logout

`POST /v1/auth/logout`

Requires an access token. Revokes the current server-side session. Subsequent requests with access tokens from that session are rejected, even if the JWT has not reached its exp time.

## Auth response

```json
{
  "accessToken": "<short-lived-jwt>",
  "refreshToken": "<rotating-opaque-token>",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "person@example.com",
    "username": "person_01",
    "displayName": "Person",
    "createdAt": "2026-11-18T00:00:00Z"
  }
}
```

Access tokens expire after 15 minutes. Refresh sessions expire after 30 days and receive a new expiry on successful rotation.

## Error envelope

```json
{
  "code": "invalid_credentials",
  "message": "E-mail/nome de usuário ou senha inválidos."
}
```

## Operational notes

- PostgreSQL is the source of truth. Flyway applies schema migrations at API startup.
- Passwords are hashed using Argon2id. Raw passwords are never stored.
- Only SHA-256 hashes of opaque refresh tokens are stored.
- JWTs are signed with an environment-provided secret. The secret is never shipped to Android.
- Session revocation is checked against PostgreSQL during authenticated requests.
- Beta registration requires the server-side `REGISTRATION_INVITE_CODE`; the code is compared in constant time and is never stored in the database or committed to the repository.
- Email verification, password reset, login rate limiting, account recovery, and abuse controls are not yet implemented. This invite gate is only a temporary closed-testing control, not a replacement for production abuse protection.

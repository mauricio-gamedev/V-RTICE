# Security policy

VÉRTICE is under active development and is not yet suitable for production use.

## Reporting a vulnerability

Do not publish credentials, tokens, personal data, or a reproducible exploit in a public issue. Contact the repository maintainer privately through GitHub's security advisory/reporting facilities.

## Security expectations

- TLS for all remote environments.
- Password hashing with a purpose-built password hashing algorithm (Argon2id is the current proposal).
- Short-lived access tokens and rotating, revocable refresh tokens.
- Authorization enforced by the backend, not by client visibility.
- Media room tokens issued by the backend only after checking room membership and permissions.
- Secrets managed through environment variables or a secret manager.
- File uploads validated by actual content type, size, extension policy, and storage permissions.
- Microphone, camera, and screen capture activated only after explicit user action and platform consent.
- Account deletion, retention, and age policies must be defined before public beta.

Do not assume end-to-end encryption exists. It has not been implemented or independently reviewed.

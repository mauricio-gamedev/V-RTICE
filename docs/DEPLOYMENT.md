# VÉRTICE — staging deployment

This procedure creates a closed staging environment for testing account creation and authentication from the Android app. It is not a production deployment.

## Architecture

- API: Render free Docker web service.
- Database: Neon PostgreSQL.
- Region pairing: Render Virginia with Neon AWS US East 1 (N. Virginia).
- TLS: Render-managed HTTPS on the generated `.onrender.com` hostname.

The Render free web service sleeps after 15 minutes without inbound traffic and may take about a minute to wake on the next request. Render positions free instances for testing and hobby projects, not production. Neon Free also has compute and storage quotas; monitor usage in its dashboard.

Official provider documentation:
- [Render Blueprint specification](https://render.com/docs/blueprint-spec)
- [Render free instance limitations](https://render.com/docs/free)
- [Neon connection strings](https://neon.com/docs/connect/connect-from-any-app)
- [Neon Free Plan](https://neon.com/pricing)

## 1. Create the PostgreSQL database in Neon

1. Sign in to Neon and create a PostgreSQL project named `vertice-staging`.
2. Choose AWS US East 1 (N. Virginia) to match the Render region.
3. Copy the **direct** connection string, not the pooled/PgBouncer string. Flyway runs migrations during API startup.
4. Keep the database username and password private.

The Neon connection string usually has this shape:

```text
postgresql://USER:PASSWORD@HOST/DBNAME?sslmode=require
```

Render needs three variables. Convert the URL prefix to JDBC and remove the user/password section from the URL:

```text
DATABASE_URL=jdbc:postgresql://HOST/DBNAME?sslmode=require
DATABASE_USER=USER
DATABASE_PASSWORD=PASSWORD
```

Retain `sslmode=require`. Never put real credentials in GitHub, source code, the APK, or chat messages.

## 2. Create the Render API service

1. Sign in to Render and connect the GitHub account with access to `mauricio-gamedev/V-RTICE`.
2. Create a new **Blueprint** from this repository and select the branch that contains `render.yaml`.
3. Review the service named `vertice-api-staging`. It builds from the repository root using `backend/Dockerfile` and checks `/health/live`.
4. Enter the secret values requested by the Blueprint:
   - `DATABASE_URL`
   - `DATABASE_USER`
   - `DATABASE_PASSWORD`
   - `REGISTRATION_INVITE_CODE`: generate a private random code of 12–128 characters. Do not commit it.
5. Render generates `JWT_SECRET` using its secret generator. Keep the generated value private.
6. Create the service and wait for its first deployment to complete.

The Blueprint sets `DATABASE_POOL_SIZE=2` for the small staging service. Automatic deploys are set to wait for the linked checks to pass.

## 3. Verify the deployed API

Copy the exact HTTPS hostname shown in the Render dashboard. Do not guess the hostname.

Open this endpoint in a browser, replacing the placeholder with the actual service hostname:

```text
https://<exact-render-service-host>/health/live
```

Expected response:

```json
{"status":"ok","service":"vertice-api"}
```

Then open VÉRTICE on Android. In **Endereço do servidor**, paste only the base URL shown by Render, for example the exact `https://...` origin without `/health/live`, and tap **Verificar**.

When creating a beta account, enter the same private `REGISTRATION_INVITE_CODE` value configured in Render. The Android app sends it to the API; it is not embedded in the app.

## 4. Staging restrictions

- Keep the service URL and invitation code private while this is a closed test.
- The invite code gates registration, but it does not replace login rate limiting, email verification, password recovery, abuse controls, backups, or operational monitoring.
- Do not store sensitive personal information or rely on this staging database for important data.
- Render free instances sleep after inactivity; first requests after sleep can be slow.
- Neon free quotas can change. Monitor compute, storage, and data transfer in the Neon dashboard.
- This configuration is for development and device validation, not a public launch.

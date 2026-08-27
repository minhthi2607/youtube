# YouTube Clone (Spring Boot)

A minimal full-stack YouTube clone: upload, store, browse, search, stream and comment on videos.

- **Backend**: Java 21, Spring Boot 4.1.1, Spring Security (JWT), Spring Data JPA, MySQL.
- **Frontend**: plain HTML/CSS/vanilla JS served from `src/main/resources/static` (no build step, no extra dependency — see *Frontend choice* below).
- **Video storage**: local filesystem (`uploads/` by default) behind a small `FileStorageService` abstraction, so a cloud backend (S3, GCS, ...) can be swapped in later without touching controllers/services.
- **Migrations**: plain SQL in `src/main/resources/schema.sql`, run automatically by Spring Boot on startup (`spring.sql.init.mode=always`). Hibernate is set to `ddl-auto=validate` — it never mutates the schema, `schema.sql` is the single source of truth.

## Features

- Register / log in, receive a JWT and use it for authenticated requests.
- Upload a video (mp4/webm, up to 500MB) with a title and description.
- Browse and search videos by title, paginated.
- Play videos in the browser with seeking (HTTP `Range` requests are supported end-to-end).
- Comment on videos (create/read/update/delete), ordered newest-first.
- Like / dislike videos (toggle, one reaction per user per video).
- Subscribe / unsubscribe to a channel, see a channel's videos and subscriber count.

## Project layout

```
src/main/java/com/project/youtube/
  config/        Spring Security configuration
  controller/    REST controllers
  dto/           Request/response DTOs
  entity/        JPA entities (User, Video, Comment, VideoLike, Subscription)
  exception/     Custom exceptions + a global @RestControllerAdvice
  repository/    Spring Data JPA repositories
  security/      JWT issuing/validation, UserDetailsService, auth filter
  service/       Business logic
  storage/       FileStorageService abstraction (local filesystem impl)
src/main/resources/
  application.properties
  schema.sql               MySQL schema (migration)
  static/                  Minimal frontend (HTML/CSS/JS)
uploads/                   Local video storage (gitignored, dev only)
```

## Prerequisites

- JDK 21+
- MySQL 8.x running locally (or reachable), with a user that can create/own a database.
  `spring.datasource.url` uses `createDatabaseIfNotExist=true`, so the schema itself doesn't need to be pre-created — just an accessible MySQL server and credentials.

## Configuration (environment variables)

All of these have dev-friendly defaults in `application.properties`; override as needed:

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `youtube_clone` | Database name |
| `DB_USERNAME` | `root` | MySQL user |
| `DB_PASSWORD` | `root` | MySQL password |
| `STORAGE_LOCATION` | `uploads` | Directory where uploaded video files are written |
| `JWT_SECRET` | *(dev placeholder, 32+ bytes)* | HMAC-SHA256 signing key for JWTs — **set a real secret outside of local dev** |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | JWT lifetime in milliseconds |

Example local setup:

```bash
mysql -u root -p -e "CREATE USER IF NOT EXISTS 'ytclone'@'localhost' IDENTIFIED BY 'ytclone'; \
  GRANT ALL PRIVILEGES ON youtube_clone.* TO 'ytclone'@'localhost'; FLUSH PRIVILEGES;"

export DB_USERNAME=ytclone
export DB_PASSWORD=ytclone
export JWT_SECRET="a-random-secret-that-is-at-least-32-bytes-long"
```

## Build & run

```bash
./gradlew bootRun
```

Or build a jar and run it:

```bash
./gradlew build
java -jar build/libs/youtube-0.0.1-SNAPSHOT.jar
```

The app serves both the REST API and the frontend on the same port (default `8080`):

- Frontend: http://localhost:8080/index.html
- API base: http://localhost:8080/api

On first run, `schema.sql` creates the `users`, `videos`, `comments`, `likes` and `subscriptions` tables automatically.

## Running tests

```bash
./gradlew test
```

The included `YoutubeApplicationTests` boots the full Spring context, which requires a reachable MySQL instance (per the *Prerequisites* above) — there is no in-memory test database, by design, to keep the dependency set unchanged from what Spring Initializr generated.

## REST API overview

All endpoints are under `/api`. Authenticated endpoints expect `Authorization: Bearer <token>`.

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | – | Register, returns a JWT |
| POST | `/api/auth/login` | – | Log in, returns a JWT |
| GET | `/api/users/me` | required | Current user |
| GET | `/api/users/{id}` | – | Public channel profile |
| GET | `/api/users/{id}/videos` | – | A channel's videos |
| POST/DELETE | `/api/users/{id}/subscribe` | required | Subscribe / unsubscribe |
| GET | `/api/users/{id}/subscribe` | required | Subscription status |
| GET | `/api/videos` | – | List/search videos (`?q=`, `?page=`, `?size=`) |
| POST | `/api/videos` | required | Upload a video (`multipart/form-data`: `title`, `description`, `file`) |
| GET | `/api/videos/{id}` | – | Video metadata (also increments the view count) |
| GET | `/api/videos/{id}/stream` | – | Video bytes, supports `Range` requests for seeking |
| GET/POST | `/api/videos/{id}/comments` | GET: – / POST: required | List / add comments |
| PUT/DELETE | `/api/comments/{id}` | required (author only) | Edit / delete a comment |
| POST | `/api/videos/{id}/like` | required | Toggle like |
| POST | `/api/videos/{id}/dislike` | required | Toggle dislike |
| GET | `/api/videos/{id}/likes` | – | Like/dislike counts (+ current user's reaction if authenticated) |

## Design notes / deviations from a "textbook" setup

These choices were made to stay strictly within the dependencies Spring Initializr already generated (Lombok, DevTools, Web, Security, Data JPA, MySQL Driver, Validation) — nothing was added without asking:

- **JWT is hand-rolled** (`security/JwtService.java`): a small HMAC-SHA256 sign/verify implementation using `javax.crypto` (JDK-provided) and Jackson (already on the classpath via `spring-boot-starter-webmvc`) for claim encoding, instead of pulling in a dedicated JWT library (e.g. `jjwt`).
- **Migrations use `schema.sql`**, not Flyway — Flyway isn't among the pre-selected dependencies.
- **Frontend is plain static HTML/CSS/JS**, not Thymeleaf or a separate React app — both would require a new dependency (Thymeleaf) or separate Node tooling (React). Static assets under `src/main/resources/static` are served for free by `spring-boot-starter-webmvc`, and call the JSON API with `fetch`.

If any of these trade-offs should instead pull in a proper library (jjwt, Flyway, Thymeleaf/React), that's a one-line ask away — this was intentionally kept dependency-neutral by default.

## Known limitations (out of scope for this iteration)

- No thumbnail generation or video transcoding.
- No admin/moderation role — every registered user has the same permissions.
- No refresh tokens — JWTs simply expire after `JWT_EXPIRATION_MS`.
- No pagination UI beyond the API's `page`/`size` params (the frontend loads the first page only).

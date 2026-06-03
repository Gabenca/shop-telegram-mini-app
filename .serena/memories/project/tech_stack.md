# Tech Stack: shop-telegram-mini

## Backend (`backend/`)

| Layer | Technology | Version | Notes |
|---|---|---|---|
| Language | Java | 21 | Records, pattern matching, virtual threads available |
| Framework | Spring Boot | 3.3.5 | `spring-boot-starter-web`, `data-jpa`, `security`, `validation` |
| Build | Gradle | 8.14.2 | Kotlin DSL (`build.gradle.kts`), wrapper present |
| ORM | Spring Data JPA + Hibernate | (via Spring Boot) | Repositories per aggregate root |
| DB (dev) | H2 | (via Spring Boot) | In-memory, default profile |
| DB (prod) | PostgreSQL | 15 | Via `docker-compose.yml` |
| Auth | Spring Security + custom filter | — | `TelegramInitDataFilter` validates HMAC-SHA256 of init data |
| Migration | (none yet) | — | Schema via `ddl-auto=update`; consider Flyway when prod-real |
| Testing | JUnit 5 + Spring Boot Test + Mockito | (via Spring Boot) | `@WebMvcTest` for controllers, plain unit tests for services |
| Lombok | Yes | latest | `@RequiredArgsConstructor`, `@Data` etc. used throughout |

### Backend Dependencies (declared in `build.gradle.kts`)
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-devtools`
- `postgresql` (runtime)
- `com.h2database:h2` (runtime)
- `org.projectlombok:lombok`

## Frontend (`frontend/`)

| Layer | Technology | Version | Notes |
|---|---|---|---|
| Framework | Angular | 18.2 | Standalone components, no NgModules |
| Language | TypeScript | 5.5 | Strict mode |
| Styling | SCSS | — | Per-component `.scss` files |
| Telegram | `@twa-dev/sdk` | 8.0.2 | `TelegramWebapp` injected in core services |
| Routing | Angular Router | (via Angular) | Routes in `app.routes.ts` |
| HTTP | `HttpClient` | (via Angular) | `telegram-init-data.interceptor.ts` adds `X-Telegram-Init-Data` header |
| State | RxJS BehaviorSubjects in services | — | No NgRx |
| Testing | Jasmine + Karma (default) | (via Angular CLI) | `app.spec.ts` present |

### Frontend Structure (high-level)
- `core/` — singletons: guards, interceptors, services that wrap `@twa-dev/sdk`
- `features/` — routed components grouped by domain (`home`, `recipes`, `meal-plan`, `shopping-list`, `couple`)
- `shared/` — reusable presentational components, models, pipes, utility services
- `services/` (top-level under `app/`) — feature-spanning services if needed
- `environments/` — `environment.ts` (dev) and `environment.prod.ts`

## DevOps

- **Docker Compose** — only the PostgreSQL service. No backend/frontend images.
- **Ngrok** — required for HTTPS tunneling during local Telegram Mini App testing (Telegram requires HTTPS).
- **Scripts** — `scripts/start-with-https.ps1` automates backend + ngrok; `scripts/start-frontend-https.ps1` does the same for frontend.

## Tooling Versions in This Repo

- **JDK:** `C:\Users\max99\.jdks\ms-21.0.11` (set `JAVA_HOME` before running gradle)
- **Node:** implied by Angular 18 requirements (Node 18.19+ or 20.11+)
- **Gradle wrapper:** `backend/gradlew.bat` (and `gradlew` for bash)
- **Ngrok:** installed via `winget install ngrok.ngrok`

## OpenCode / Superpowers / Serena

- **Opencode** (the CLI) loads plugins from `opencode.json` at repo root.
- **Superpowers** v5.1.0 — loaded as a plugin from `github:obra/superpowers`. Provides 14 process skills (TDD, debugging, planning, etc.). Read-only, do not edit in the cache.
- **Serena MCP** — provides semantic code tools (`serena_find_symbol`, `serena_replace_symbol_body`, etc.) backed by language servers. Configured in `opencode.json` pointing at `C:\Users\max99\.local\bin\serena.exe`. Project config: `.serena/project.yml`.
- **Custom project skill:** `.opencode/skills/serena-superpowers/SKILL.md` — bridges the two.

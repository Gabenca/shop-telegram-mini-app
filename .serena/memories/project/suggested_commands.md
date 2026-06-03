# Suggested Commands: shop-telegram-mini

> PowerShell syntax. All commands assume the repo root as the current directory unless noted.

## First-Time Setup

```powershell
# Backend dependencies are resolved by Gradle on first run (no separate install)
cd backend
$env:JAVA_HOME = "C:\Users\max99\.jdks\ms-21.0.11"
.\gradlew.bat build              # Download deps, compile, run all tests

# Frontend dependencies
cd ..\frontend
npm install
```

## Development (Local)

### Backend only
```powershell
cd backend
$env:JAVA_HOME = "C:\Users\max99\.jdks\ms-21.0.11"
.\gradlew.bat bootRun
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# OpenAPI JSON: http://localhost:8080/v3/api-docs
# H2 console: http://localhost:8080/h2-console
# Telegram webhook (when enabled): POST http://localhost:8080/api/v1/telegram/webhook
```

### Frontend only (assumes backend already running)
```powershell
cd frontend
npm start
# Angular dev server: http://localhost:4200
# /api/* proxied to http://localhost:8080 (see proxy.conf.json)
```

### PostgreSQL via Docker
```powershell
docker compose up -d postgres
# (Only the postgres service is defined; no backend image.)
```

### Full HTTPS stack for Telegram Mini App
```powershell
# One-shot: backend + ngrok tunnel
.\scripts\start-with-https.ps1

# Frontend with HTTPS (if you want to dev frontend over HTTPS)
.\scripts\start-frontend-https.ps1 -BackendUrl "https://<your-ngrok-subdomain>.ngrok-free.app"
```

## Testing

### Backend
```powershell
cd backend
$env:JAVA_HOME = "C:\Users\max99\.jdks\ms-21.0.11"

# All tests
.\gradlew.bat test

# Single test class
.\gradlew.bat test --tests "com.example.backend.service.RecipeServiceTest"

# Single test method
.\gradlew.bat test --tests "com.example.backend.service.RecipeServiceTest.createRecipe_validInput_persistsRecipe"

# With output
.\gradlew.bat test --info
```

### Frontend
```powershell
cd frontend
npm test
# Or watch mode: npm run test:watch
```

## Building

### Backend
```powershell
cd backend
$env:JAVA_HOME = "C:\Users\max99\.jdks\ms-21.0.11"
.\gradlew.bat clean build
# Produces: build/libs/backend-0.0.1-SNAPSHOT.jar
```

### Frontend
```powershell
cd frontend
npm run build
# Produces: dist/frontend/browser/ including ngsw-worker.js, ngsw.json, manifest.webmanifest
# (When building the multi-stage Dockerfile the frontend dist is bundled into the jar)
```

### Docker image (multi-stage)
```powershell
# From the repo root
docker build -t shop-telegram-mini:latest .
docker run --rm -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/recipe_db `
  -e SPRING_DATASOURCE_USERNAME=postgres `
  -e SPRING_DATASOURCE_PASSWORD=password `
  -e TELEGRAM_BOT_TOKEN=<token> `
  shop-telegram-mini:latest
# Optional webhook registration:
#   -e TELEGRAM_WEBHOOK_ENABLED=true `
#   -e TELEGRAM_WEBHOOK_URL=https://your-host.example/api/v1/telegram/webhook `
```

## Linting / Type-Check

### Backend
```powershell
cd backend
$env:JAVA_HOME = "C:\Users\max99\.jdks\ms-21.0.11"
.\gradlew.bat check       # Includes compile + test + any checkstyle/spotbugs if configured
```

### Frontend
```powershell
cd frontend
npm run lint              # If configured in package.json
npx tsc --noEmit          # Type-check only
```

## Database Inspection (H2 dev)

H2 console URL (default): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb` (check `application.properties` for actual)
- User: `sa`, password: (empty) — verify in config

## Serena MCP

The serena binary is already at `C:\Users\max99\.local\bin\serena.exe`.
The MCP is launched by opencode via the `mcp.serena` block in `opencode.json` — no manual start needed.

```powershell
# Manual invocation for debugging (rarely needed)
C:\Users\max99\.local\bin\serena.exe start-mcp-server --context opencode --project-from-cwd
```

## Git

```powershell
git status
git add -p                                       # Stage interactively
git commit -m "feat: add shopping list export"
git push origin <branch>
```

## Useful Opencode One-Liners

```powershell
# Run a single skill from a fresh session
opencode --skill serena-superpowers

# (If available) list installed MCP servers
opencode mcp list
```

## Troubleshooting

| Symptom | Fix |
|---|---|
| `gradle.bat` complains about JAVA_HOME | Set `$env:JAVA_HOME = "C:\Users\max99\.jdks\ms-21.0.11"` in the same shell |
| Frontend can't reach backend | Check `frontend/proxy.conf.json` target; backend must be on `:8080` |
| Telegram WebApp API missing fields | App must be opened from Telegram (not browser); check `TelegramService` initialisation |
| `SpaController` 404 on refresh | File may be untracked in git; copy from working tree or recreate |
| ngrok URL changes every restart | Free plan behaviour; configure static domain (paid) or use Cloudflare tunnel |
| DB port conflict | Check `docker compose ps`; only PostgreSQL is containerised |

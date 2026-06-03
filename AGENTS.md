# AGENTS.md — Project Directives for AI Agents

This file is read by AI agents (opencode, Claude Code, etc.) at the start of a session. Keep it short, directive, and stable.

## TL;DR

- This is **shop-telegram-mini** — Telegram Mini App, Java 21 + Spring Boot 3.3.5 backend, Angular 18.2 + TypeScript frontend. Russian UI, English code.
- **Superpowers** plugin is active — it governs *how* to work (TDD, debugging, planning, review, subagent dispatch, verification).
- **Serena MCP** is active — it provides semantic code tools (`serena_find_symbol`, `serena_replace_symbol_body`, `serena_find_referencing_symbols`, `serena_get_diagnostics_for_file`, …). **Prefer serena tools over `grep`/`glob`/`read`/`edit` for any code navigation or modification.** See `.opencode/skills/serena-superpowers/SKILL.md` for the full mapping.
- The serena-superpowers bridge skill is mandatory context — load it before applying any superpowers skill.

## Active Plugins & MCP (per `opencode.json`)

- `plugin`: `superpowers@git+https://github.com/obra/superpowers.git` (v5.1.0, read-only — do not edit in cache)
- `mcp.serena`: local, `C:\Users\max99\.local\bin\serena.exe start-mcp-server --context opencode --project-from-cwd`
- `mcp.context7`: remote, for up-to-date library docs
- `mcp.sequential-thinking`: local, for structured reasoning
- `mcp.memory`: local, knowledge graph

## Serena Project State

- Config: `.serena/project.yml` (languages: `java`, `typescript`; ignored_paths set; `initial_prompt` configured)
- Symbol cache: `.serena/cache/{java,typescript}/*.pkl` (pre-built)
- Memories: `.serena/memories/project/`
  - `project_overview.md` — what the project is, target users, non-goals
  - `tech_stack.md` — versions, key dependencies, opencode/serena/superpowers setup
  - `code_conventions.md` — package layout, naming, style, anti-patterns
  - `project_structure.md` — file tree, "where to find X" index
  - `suggested_commands.md` — build/test/run/lint commands
  - `task_checklist.md` — reusable verification list

When a task touches a domain, **read the corresponding memory first**. When you learn a new convention, write it back.

## Tech at a Glance

- **Backend:** Java 21, Spring Boot 3.3.5, Spring Data JPA, Spring Security, H2 (dev) / PostgreSQL 15 (prod via `docker-compose.yml`), Gradle 8.14.2 (Kotlin DSL), Lombok.
- **Frontend:** Angular 18.2 standalone components, TypeScript 5.5 (strict), SCSS, `@twa-dev/sdk` 8.0.2, RxJS in services.
- **Auth:** Custom `TelegramInitDataFilter` validates HMAC of `X-Telegram-Init-Data` against the bot token. No passwords. Users are paired into `Couple`s via invite code.
- **Package roots:** `com.example.backend.*` (Java) and `frontend/src/app/{core,features,shared,services}` (TS).

## Common Commands

```powershell
# Backend
cd backend; $env:JAVA_HOME="C:\Users\max99\.jdks\ms-21.0.11"; .\gradlew.bat bootRun
cd backend; $env:JAVA_HOME="C:\Users\max99\.jdks\ms-21.0.11"; .\gradlew.bat test

# Frontend
cd frontend; npm install; npm start
cd frontend; npm test; npx tsc --noEmit

# Database
docker compose up -d postgres

# Full HTTPS stack (for Telegram Mini App)
.\scripts\start-with-https.ps1
```

See `.serena/memories/project/suggested_commands.md` for the full list including troubleshooting.

## Hard Rules

1. **Never edit the superpowers plugin in place.** It lives in `~/.cache/opencode/packages/superpowers@...` and is regenerated on update. Project customisations go in `.opencode/skills/`, `AGENTS.md`, and `.serena/`.
2. **Never return JPA entities from controllers** — always DTOs. See `code_conventions.md`.
3. **Every endpoint (except `/actuator/health`) requires valid Telegram init data.** `SecurityConfig` enforces this; don't weaken it.
4. **All data is scoped to a `Couple`.** Any new query must filter by the authenticated user's `coupleId`.
5. **Build artifacts are ignored** (`.gitignore` covers `build/`, `dist/`, `target/`, `node_modules/`, `.angular/`, `.gradle/`, `.idea/`). Don't commit them.
6. **Russian UI strings, English identifiers.** Commit messages in English, conventional-commits style.
7. **`ddl-auto=update` is acceptable for dev; never for prod.** When introducing prod-real persistence, switch to Flyway/Liquibase.

## Workflow Expectations

- Apply superpowers skills to every non-trivial task: `brainstorming` → `writing-plans` (or `subagent-driven-development`) → `test-driven-development` → `verification-before-completion` → `requesting-code-review` → `finishing-a-development-branch`.
- At each code-touching step, use serena tools. See `.opencode/skills/serena-superpowers/SKILL.md` for the mapping.
- For multi-file refactors or "find every caller of X" work, dispatch a subagent with a `find_referencing_symbols` query rather than exploring manually.
- Always run the task_checklist verification items before claiming completion. Use `serena_get_diagnostics_for_file` as a fast pre-check.

## When You Discover Something New

- A new convention, gotcha, or stable command → add it to the relevant `.serena/memories/project/*.md` (use `serena_write_memory` if available, otherwise edit the file).
- A repeated task pattern → consider promoting it to a new skill in `.opencode/skills/`.
- A change in stack versions → update `tech_stack.md` and `AGENTS.md`.

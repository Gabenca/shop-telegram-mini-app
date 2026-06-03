# Task Checklist: shop-telegram-mini

A reusable verification list. Trim or extend per task; do not paste "✓" for items not actually done.

## Backend — Before Any Change

- [ ] Identified the affected aggregate(s) (`Recipe`, `MealPlanEntry`, `ShoppingListItem`, `Couple`, `User`)
- [ ] Confirmed whether the change crosses a couple boundary (if yes, add an access check)
- [ ] Wrote a failing test that reproduces the current behaviour or the desired new behaviour
- [ ] Located the corresponding test file under `backend/src/test/...`

## Backend — After Implementation

- [ ] `gradlew.bat test` is green
- [ ] No new warnings in compile output
- [ ] DTOs (not entities) returned from controller methods
- [ ] `GlobalExceptionHandler` covers any new exception type
- [ ] Validation annotations on new request DTOs
- [ ] `SecurityConfig` still permits `/actuator/health` only
- [ ] No `System.out.println`; use `@Slf4j` or explicit logger
- [ ] No new `ddl-auto`-dependent changes without migration plan

## Frontend — Before Any Change

- [ ] Identified the feature folder under `frontend/src/app/features/...`
- [ ] Checked if shared types need to be updated in `shared/models/index.ts`
- [ ] Wrote a failing `*.spec.ts` (or noted why unit test isn't appropriate)

## Frontend — After Implementation

- [ ] `npm test` is green
- [ ] `npx tsc --noEmit` passes with no errors
- [ ] No `any` in new TypeScript
- [ ] `async` pipe used in templates (not `.subscribe()` in components)
- [ ] HTTP errors caught in services, not silently swallowed
- [ ] Telegram init data added to outbound requests (handled by interceptor — verify if new endpoint)
- [ ] No direct calls to `@twa-dev/sdk` outside `TelegramService`
- [ ] Component file follows `<name>.component.{ts,html,scss}` pattern
- [ ] Standalone component (no NgModule declarations)

## Cross-Cutting

- [ ] No hardcoded URLs (use `environment.ts`)
- [ ] Russian user-facing strings; English identifiers
- [ ] No secrets in committed files
- [ ] Conventional commit message drafted (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`)
- [ ] If behaviour changed: update `AGENTS.md` or relevant memory in `.serena/memories/project/`

## Code Review Self-Check

- [ ] PR diff is small and focused (no "while I'm here" changes)
- [ ] Tests cover happy path AND at least one error/edge case
- [ ] Any new public method has either: a unit test, an integration test, or a `@WebMvcTest`
- [ ] No commented-out code
- [ ] No TODOs without an associated issue
- [ ] Build artifacts (`build/`, `dist/`, `target/`, `node_modules/`, `.angular/`) NOT committed

## Debugging Session Exit

- [ ] Root cause identified and documented (per `systematic-debugging` skill)
- [ ] Failing test exists that reproduces the bug
- [ ] Fix is minimal and addresses the root cause
- [ ] All previously-passing tests still pass
- [ ] If a new memory was discovered (convention, command, gotcha): added to the appropriate `.serena/memories/project/*.md` file

## Release / Deploy (when applicable)

- [ ] `gradlew.bat clean build` succeeds
- [ ] `npm run build` succeeds
- [ ] Environment variables for prod profile documented
- [ ] `application-prod.properties` or equivalent exists
- [ ] No `ddl-auto=update` in prod profile
- [ ] Telegram bot token rotated if needed
- [ ] Ngrok / HTTPS endpoint updated in BotFather

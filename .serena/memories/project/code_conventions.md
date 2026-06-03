# Code Conventions: shop-telegram-mini

## Backend (Java 21 / Spring Boot)

### Package Layout
- Root package: `com.example.backend`
- Sub-packages by responsibility, NOT by technical layer:
  - `domain` — JPA entities (`@Entity` classes: `User`, `Couple`, `Recipe`, `Ingredient`, `MealPlanEntry`, `MealPlanEntryDish`, `ShoppingListItem`, `Unit`, `MealType`)
  - `repository` — Spring Data JPA interfaces (one per aggregate root)
  - `service` — business logic (`*Service` classes, `@Service` + `@RequiredArgsConstructor`)
  - `controller` — REST endpoints (`@RestController`, `@RequestMapping("/api/...")`)
  - `dto` — request/response DTOs (records preferred; no JPA annotations)
  - `config` — `@Configuration` classes (`SecurityConfig`, `CorsConfig`, `WebConfig`, `UserArgumentResolver`, `OpenApiConfig`)
  - `telegram` — Telegram-specific (`TelegramInitDataFilter`, `TelegramBotService`, `TelegramWebhookController`, `WebhookRegistrar`)
  - `security` — custom guards and AOP (`@CoupleScoped` annotation + `CoupleScopeAspect`)
  - `event` — Spring `ApplicationEvent` subclasses (`DomainEvent`, `MealPlanChangedEvent`, `ShoppingListRegenerateEvent`) + `@EventListener` handlers (`PartnerNotificationListener`)
  - `exception` — custom exceptions + `GlobalExceptionHandler` (`@RestControllerAdvice`)

### Naming
- **Entities:** singular noun (`Recipe`, not `RecipeEntity`); PascalCase; JPA `@Table(name = "recipes")` if plural needed at DB level
- **DTOs:** suffixed with purpose (`CreateRecipeRequest`, `RecipeDto`, `MealPlanEntryDto`)
- **Services:** `<Entity>Service` (`RecipeService`, `CoupleService`)
- **Controllers:** `<Entity>Controller` (`RecipeController`)
- **Repositories:** `<Entity>Repository` (`RecipeRepository`)
- **Custom exceptions:** `<Reason>Exception` (`ResourceNotFoundException`, `AccessDeniedException`)

### Style
- Lombok is in use — prefer `@RequiredArgsConstructor` for DI, `@Data` for DTOs, `@Slf4j` (or manual logger) in services
- Use `record` for DTOs and value objects where possible (Java 21)
- Prefer `Optional<T>` over `null` returns from services
- Service methods throw custom exceptions, not return error codes
- `@Transactional` on service methods that mutate state; `readOnly = true` on read methods

### REST Conventions
- Base path: `/api/v1/<resource>` (e.g. `/api/v1/recipes`, `/api/v1/meal-plan`) — versioned from day 1
- Use plural nouns
- HTTP verbs: GET (read), POST (create), PUT/PATCH (update), DELETE
- Validation: `@Valid` on request bodies + `jakarta.validation` annotations on DTO fields
- Authenticated user is injected via custom `@AuthenticationPrincipal`-like argument (see `UserArgumentResolver`)
- Response bodies are DTOs, never entities (avoid lazy-loading serialization traps)
- One exception: `POST /api/v1/telegram/webhook` is `permitAll` in `SecurityConfig` (Telegram's own caller doesn't carry our init-data header)
- OpenAPI/Swagger UI: `/swagger-ui.html`, JSON at `/v3/api-docs` (springdoc, no `@Operation` annotations — relies on auto-generation)

### Error Handling
- `GlobalExceptionHandler` maps custom exceptions to `ErrorResponse` DTO with appropriate HTTP status
- Standard statuses: 400 (validation), 401 (no init data), 403 (access denied), 404 (not found), 409 (conflict, `UserNotInCoupleException`), 500 (unexpected)

### Domain Events
- `DomainEvent` is abstract; carries `coupleId` + `actorUserId`
- `MealPlanChangedEvent` and `ShoppingListRegenerateEvent` extend it
- `PartnerNotificationListener` is `@Async @Transactional(readOnly=true) @EventListener`; iterates `userRepository.findByCoupleId(coupleId)`, calls `TelegramBotService.sendMessage(telegramId, text)` for everyone except the actor
- Service mutation methods that publish events take an extra `userId` parameter; controllers pass `user.getId()`
- `@EnableAsync` lives on `BackendApplication`

### AOP Guards
- `@CoupleScoped` is a method-level marker; `CoupleScopeAspect` `@Around` checks the args for a `User` whose `couple` is null and throws `UserNotInCoupleException` (HTTP 409)
- The annotation is opt-in; the old `if (user.getCouple() == null) { … }` checks still exist in most controllers — a future sweep should migrate them to `@CoupleScoped`

### Tests
- Service unit tests: plain JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- Controller tests: `@WebMvcTest` with security filters disabled (`@AutoConfigureMockMvc(addFilters = false)`)
- Integration tests: `@SpringBootTest` — currently only `CoupleIntegrationTest`
- Test naming: `<MethodName>_<Scenario>_<Expected>` (e.g. `addRecipe_withDuplicateName_throwsConflict`)

## Frontend (Angular 18 / TypeScript)

### Structure
- **Standalone components only** (no NgModules for features)
- One folder per component containing `.ts` / `.html` / `.scss` (no `.spec.ts` checked in consistently yet)
- Component selector prefix: `app-`
- File name matches class name + suffix (`meal-plan.component.ts` → `MealPlanComponent`)
- Reusable presentational components live in `shared/components/` and are signal-input based (`input<string>(...)` etc.)

### Naming
- Components: PascalCase class, kebab-case file (`recipe-list.component.ts`)
- Services: `*.service.ts` → `*Service` class
- Models/interfaces: in `shared/models/index.ts`, exported via barrel
- Pipes: `*.pipe.ts` → `*Pipe` class
- Routes: kebab-case paths, `data: { title: '...' }` for breadcrumbs/page titles

### Style
- Strict TypeScript — no `any`, prefer explicit types
- Use RxJS `BehaviorSubject<T>` for service-level state
- `async` pipe in templates; avoid `.subscribe()` in components
- HTTP errors handled in services, surfaced to components via `catchError` + state subjects
- Interceptors add `X-Telegram-Init-Data` header to every outgoing request
- Telegram WebApp API wrapped in `TelegramService` (don't call `@twa-dev/sdk` directly in components)

### State Management
- Per-feature state lives in feature services (e.g. `ShoppingListService` keeps a `BehaviorSubject<ShoppingListItem[]>`)
- Cross-feature state lifted to `core/services/`
- No NgRx, no Signals-only architecture (mix is fine but prefer observables for consistency)
- **Optimistic updates:** when the user action should feel instant, flip the local cache first, then PATCH, then revert on error. See `ShoppingListService.toggleItemChecked` for the canonical pattern.

### Tests
- Karma + Jasmine (default Angular setup)
- `app.spec.ts` present as a smoke test
- Test files co-located with source

### Telegram UX Conventions
- All public methods on `TelegramService` no-op when `isTelegramWebApp === false` so dev in a plain browser is unaffected
- `hapticFeedback('light' | 'medium' | 'heavy')` on user actions: light for navigation/picker, medium for async success/failure
- `telegramGuard` is applied alongside `coupleGuard` on every protected route; non-Telegram browsers land on `/no-telegram` (a friendly `TelegramRequiredComponent`)

### Theming
- Theme variables live in `src/styles.scss` under `:root` (`--warm-*`, `--tg-theme-*`)
- Dark mode: `@media (prefers-color-scheme: dark)` block remaps the same variables; components consume them via `var(--tg-theme-…)`
- Skeleton shimmer: `SkeletonComponent` (shimmer animation, theme-aware) — used in `recipe-list` for loading state

## Cross-Cutting

- **Language:** All user-facing text and code comments in Russian. Code identifiers, commit messages, and API contracts in English.
- **Commit messages:** Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`). Lowercase, no period.
- **Git:** Suboptimal — `frontend/` has a nested `.git` directory (not a submodule). Don't add a new one inside `frontend/`.
- **Secrets:** Telegram bot token read from env var or `application.properties`; never committed.
- **No CI yet:** Build, test, and deploy are manual.

## Anti-Patterns to Avoid

- ❌ Returning JPA entities from controllers (always DTOs)
- ❌ `any` in TypeScript
- ❌ Direct `subscribe()` in components (use `async` pipe)
- ❌ `System.out.println` — use `@Slf4j` logger
- ❌ Hardcoded URLs — use `environment.ts`
- ❌ Cross-couple data access without explicit `coupleId` check in service
- ❌ `ddl-auto=update` in production (Flyway is enabled in prod profile)
- ❌ `EAGER` fetch on JPA collections — prefer `LAZY` + `JOIN FETCH` in the repository query (see `MealPlanEntry.dishes`)
- ❌ Manual `if (user.getCouple() == null)` checks when `@CoupleScoped` would do it (aspect pending migration sweep)
- ❌ Importing `@twa-dev/sdk` directly in feature components — go through `TelegramService`

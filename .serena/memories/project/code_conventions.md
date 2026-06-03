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
  - `config` — `@Configuration` classes (`SecurityConfig`, `CorsConfig`, `WebConfig`, `UserArgumentResolver`)
  - `telegram` — Telegram-specific (`TelegramInitDataFilter`, `TelegramBotService`)
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
- Base path: `/api/<resource>` (e.g. `/api/recipes`, `/api/meal-plan`)
- Use plural nouns
- HTTP verbs: GET (read), POST (create), PUT/PATCH (update), DELETE
- Validation: `@Valid` on request bodies + `jakarta.validation` annotations on DTO fields
- Authenticated user is injected via custom `@AuthenticationPrincipal`-like argument (see `UserArgumentResolver`)
- Response bodies are DTOs, never entities (avoid lazy-loading serialization traps)

### Error Handling
- `GlobalExceptionHandler` maps custom exceptions to `ErrorResponse` DTO with appropriate HTTP status
- Standard statuses: 400 (validation), 401 (no init data), 403 (access denied), 404 (not found), 409 (conflict), 500 (unexpected)

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
- Per-feature state lives in feature services
- Cross-feature state lifted to `core/services/`
- No NgRx, no Signals-only architecture (mix is fine but prefer observables for consistency)

### Tests
- Karma + Jasmine (default Angular setup)
- `app.spec.ts` present as a smoke test
- Test files co-located with source

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
- ❌ `ddl-auto=update` in production (use Flyway/Liquibase when needed)

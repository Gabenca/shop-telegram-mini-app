# Project Structure: shop-telegram-mini

## Top-Level Layout

```
shop-telegram-mini/
├── backend/                # Spring Boot API
├── frontend/               # Angular SPA (contains nested .git — leave as-is for now)
├── scripts/                # PowerShell launchers
├── docs/                   # ngrok-setup.md and other docs
├── docker-compose.yml      # PostgreSQL only
├── Dockerfile              # (top-level) — purpose unclear, may be unused
├── opencode.json           # Opencode CLI config (plugins, MCP servers)
├── AGENTS.md               # Project directives for AI agents
├── .opencode/
│   ├── skills/             # Local opencode skills
│   │   ├── frontend-design/SKILL.md
│   │   └── serena-superpowers/SKILL.md
│   └── oh-my-openagent.jsonc
├── .serena/                # Serena MCP state
│   ├── project.yml         # Project config (languages, ignored paths, initial_prompt)
│   ├── project.local.yml   # Local overrides (currently empty)
│   ├── cache/              # LSP symbol cache (do not edit)
│   └── memories/project/   # Project memories
└── README.md               # Russian-language usage docs
```

## Backend Layout (`backend/`)

```
backend/
├── build.gradle.kts        # Kotlin DSL Gradle
├── settings.gradle.kts
├── gradle/wrapper/         # Gradle wrapper
├── gradlew, gradlew.bat    # Wrapper scripts
└── src/
    ├── main/
    │   ├── java/com/example/backend/
    │   │   ├── BackendApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java          # Telegram filter + permitAll actuator/health
    │   │   │   ├── CorsConfig.java
    │   │   │   ├── WebConfig.java
    │   │   │   └── UserArgumentResolver.java    # Resolves current user from request attrs
    │   │   ├── controller/
    │   │   │   ├── CoupleController.java
    │   │   │   ├── MealPlanController.java
    │   │   │   ├── PhotoController.java         # Multipart upload
    │   │   │   ├── RecipeController.java
    │   │   │   ├── ShoppingListController.java
    │   │   │   └── SpaController.java           # Untracked file — serves frontend index.html
    │   │   ├── domain/                          # JPA entities
    │   │   │   ├── Couple.java
    │   │   │   ├── Ingredient.java
    │   │   │   ├── MealPlanEntry.java
    │   │   │   ├── MealPlanEntryDish.java
    │   │   │   ├── MealType.java                # Enum: BREAKFAST, LUNCH, SNACK, DINNER
    │   │   │   ├── Recipe.java
    │   │   │   ├── ShoppingListItem.java
    │   │   │   ├── Unit.java                    # Enum: GRAM, MILLILITER, PIECE
    │   │   │   └── User.java
    │   │   ├── dto/                             # Request/response DTOs
    │   │   │   ├── CoupleDto.java
    │   │   │   ├── CreateRecipeRequest.java
    │   │   │   ├── CreateMealPlanEntryRequest.java
    │   │   │   ├── CreateDishRequest.java
    │   │   │   ├── CreateManualItemRequest.java
    │   │   │   ├── IngredientRequest.java / IngredientDto.java
    │   │   │   ├── JoinCoupleRequest.java
    │   │   │   ├── MealPlanEntryDto.java / MealPlanEntryDishDto.java
    │   │   │   ├── PhotoUploadResponse.java
    │   │   │   ├── RecipeDto.java
    │   │   │   ├── ShoppingListItemDto.java
    │   │   │   └── UserDto.java
    │   │   ├── exception/
    │   │   │   ├── AccessDeniedException.java
    │   │   │   ├── CoupleNotFoundException.java
    │   │   │   ├── ErrorResponse.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── InvalidInviteCodeException.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── repository/
    │   │   │   ├── CoupleRepository.java
    │   │   │   ├── IngredientRepository.java
    │   │   │   ├── MealPlanEntryRepository.java
    │   │   │   ├── RecipeRepository.java
    │   │   │   ├── ShoppingListItemRepository.java
    │   │   │   └── UserRepository.java
    │   │   ├── service/
    │   │   │   ├── CoupleService.java
    │   │   │   ├── MealPlanService.java
    │   │   │   ├── RecipeService.java
    │   │   │   └── ShoppingListService.java
    │   │   └── telegram/
    │   │       ├── TelegramBotService.java
    │   │       └── TelegramInitDataFilter.java
    │   └── resources/
    │       ├── application.properties           # DB, JPA, Telegram token
    │       └── ...
    └── test/
        └── java/com/example/backend/
            ├── BackendApplicationTests.java
            ├── controller/
            │   ├── MealPlanControllerTest.java
            │   ├── PhotoControllerTest.java
            │   ├── RecipeControllerTest.java
            │   └── ShoppingListControllerTest.java
            ├── integration/
            │   └── CoupleIntegrationTest.java
            ├── repository/
            │   ├── CoupleRepositoryTest.java
            │   └── UserRepositoryTest.java
            └── service/
                ├── CoupleServiceTest.java
                ├── MealPlanServiceTest.java
                ├── RecipeServiceTest.java
                └── ShoppingListServiceTest.java
```

## Frontend Layout (`frontend/`)

```
frontend/
├── package.json
├── angular.json
├── proxy.conf.json          # Dev: /api → http://localhost:8080
└── src/
    ├── index.html
    ├── main.ts              # Bootstrap with appConfig
    ├── styles.scss
    ├── environments/
    │   ├── environment.ts           # Dev
    │   └── environment.prod.ts
    └── app/
        ├── app.ts                   # Root standalone component
        ├── app.config.ts            # Providers (router, http, interceptors)
        ├── app.routes.ts            # Top-level routes
        ├── app.html / app.scss / app.spec.ts
        ├── app.animations.ts
        ├── core/
        │   ├── guards/
        │   │   └── couple.guard.ts              # Requires user to be in a couple
        │   ├── interceptors/
        │   │   └── telegram-init-data.interceptor.ts  # Adds X-Telegram-Init-Data
        │   └── services/
        │       └── telegram.service.ts           # Wraps @twa-dev/sdk
        ├── features/                # Routed feature components
        │   ├── home/
        │   │   └── home.component.{ts,html,scss}
        │   ├── recipes/
        │   │   ├── recipe-list/
        │   │   ├── recipe-detail/
        │   │   └── recipe-form/
        │   ├── meal-plan/
        │   │   └── meal-plan.component.{ts,html,scss}
        │   ├── shopping-list/
        │   │   └── shopping-list.component.{ts,html,scss}
        │   └── couple/
        │       ├── couple-join/
        │       └── couple-profile/
        ├── services/                # Feature-spanning services (rare)
        └── shared/
            ├── components/
            │   ├── day-card/        # Single day in meal plan
            │   ├── home-button/     # Navigation button on home
            │   ├── modal/           # Generic modal
            │   └── recipe-card/     # Recipe preview card
            ├── models/index.ts      # TypeScript interfaces (Recipe, MealPlanEntry, etc.)
            ├── pipes/unit.pipe.ts  # Unit formatting (g, ml, pcs)
            ├── services/            # API service + per-domain services
            │   ├── api.service.ts
            │   ├── couple.service.ts
            │   ├── meal-plan.service.ts
            │   ├── recipe.service.ts
            │   └── shopping-list.service.ts
            └── utils/date.utils.ts
```

## Where to Find Things (Quick Index)

| What | Where |
|---|---|
| Recipe CRUD endpoints | `backend/.../controller/RecipeController.java` |
| Recipe domain logic | `backend/.../service/RecipeService.java` |
| Recipe JPA entity | `backend/.../domain/Recipe.java` |
| Recipe list page | `frontend/src/app/features/recipes/recipe-list/` |
| Recipe API client | `frontend/src/app/shared/services/recipe.service.ts` |
| Auth filter | `backend/.../telegram/TelegramInitDataFilter.java` |
| Telegram WebApp wrapper | `frontend/src/app/core/services/telegram.service.ts` |
| Frontend dev proxy | `frontend/proxy.conf.json` |
| Build config (backend) | `backend/build.gradle.kts` |
| Build config (frontend) | `frontend/angular.json` |
| DB schema | `backend/src/main/resources/application.properties` (no migrations yet) |
| HTTPS tunneling | `scripts/start-with-https.ps1`, `docs/ngrok-setup.md` |
| Serena config | `.serena/project.yml` |
| Serena memories | `.serena/memories/project/*.md` |
| Opencode config | `opencode.json` (root) |
| Local skills | `.opencode/skills/` |
| Project directives | `AGENTS.md` (root) |

# План проверки проекта shop-telegram-mini

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) или superpowers:executing-plans для реализации исправлений.

**Goal:** Провести комплексный аудит проекта Telegram Mini App (планировщик питания для пар), оценить качество кода, архитектуру, безопасность и инфраструктуру, а также предоставить конкретный план исправлений с приоритетами.

**Architecture:** Spring Boot (REST API) + Angular (SPA). Аутентификация через Telegram Web App init-data (HMAC-SHA256). Данные изолируются на уровне "пары" (couple).

**Tech Stack:** Java 21, Spring Boot 3.3.5, Angular 18.2, PostgreSQL 15 / H2, Gradle, Docker Compose.

---

## Общая оценка

| Аспект | Оценка (1–10) | Критичность |
|--------|---------------|-------------|
| Backend — Архитектура | 6 | Средняя |
| Backend — Безопасность | 4 | **Критическая** |
| Backend — JPA / Домен | 7 | Средняя |
| Backend — REST API | 6 | Средняя |
| Backend — Тесты | 4 | Высокая |
| Frontend — Архитектура | 7 | Средняя |
| Frontend — TypeScript | 6 | Средняя |
| Frontend — Сборка / Конфиг | 5 | Средняя |
| Инфраструктура / DevOps | 3 | **Критическая** |
| Git / Репозиторий | 2 | **Критическая** |

---

## Раздел 1: Git / Репозиторий

### Проблема 1.1 — Вложенный git-репозиторий в `frontend/`

**Описание:** Директория `frontend/` содержит собственный `.git`, но не настроена как Git submodule. `git status` показывает `modified: frontend (modified content, untracked content)`. Это ломает версионирование фронтенда в основном репозитории.

**Влияние:** Свежий `git clone` не получит историю frontend. Сложно отслеживать изменения. Риск потери кода.

**Исправление:**

```bash
# Удалить вложенный репозиторий, сохранив файлы
cd frontend
rm -rf .git
cd ..
git add frontend
git commit -m "fix: remove nested git repo in frontend/"
```

### Проблема 1.2 — `SpaController.java` не отслеживается в git

**Описание:** `backend/src/main/java/com/example/backend/controller/SpaController.java` есть в файловой системе, но `git status` показывает его как `Untracked`.

**Влияние:** После `git clone` на новой машине deep-linking в Angular не работает (404 при прямом переходе по `/recipes/123`).

**Исправление:**

```bash
git add backend/src/main/java/com/example/backend/controller/SpaController.java
git commit -m "fix: track SpaController for Angular deep-linking"
```

### Проблема 1.3 — `backend/src/main/resources/static/` не должен коммититься

**Описание:** В директории `static/` лежат собранные артефакты Angular. Они не должны быть в git, но `.gitignore` не исключает `backend/src/main/resources/static/`.

**Исправление:** Добавить в корневой `.gitignore`:

```gitignore
# Compiled frontend artifacts inside backend
backend/src/main/resources/static/
```

---

## Раздел 2: Backend — Безопасность

### Проблема 2.1 — `SecurityConfig` разрешает все запросы

**Описание:** `SecurityConfig.java` использует `.anyRequest().permitAll()`. Spring Security полностью отключён, кроме CSRF/formLogin/basic. `TelegramInitDataFilter` работает как отдельный Servlet Filter, но его можно обойти, просто не передавая заголовок, если бы он был зарегистрирован через Security filter chain. Однако с `permitAll` Spring Security вообще не блокирует ничего.

**Влияние:** API полностью открыт. Любой может вызывать `/api/recipes`, `/api/couple/create` без аутентификации.

**Исправление:**

```java
package com.example.backend.config;

import com.example.backend.telegram.TelegramInitDataFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TelegramInitDataFilter telegramInitDataFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(telegramInitDataFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

**Примечание:** Для этого `TelegramInitDataFilter` должен реализовывать `org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter` или, проще, фильтр должен выставлять `Authentication` в `SecurityContextHolder`. Более простой путь — оставить Filter, но в `SecurityConfig` требовать аутентификацию, а фильтр в случае успеха выставлять анонимную аутентификацию с кастомными authorities. Альтернативно, добавить `OncePerRequestFilter` и в случае успеха вызывать `SecurityContextHolder.getContext().setAuthentication(...)`.

### Проблема 2.2 — `TelegramInitDataFilter` не защищает от пустого `botToken`

**Описание:** Если `telegram.bot-token` не задан, `botToken` пустая строка. HMAC вычисляется, но с пустым ключом. Злоумышленник может подделать init-data, зная только алгоритм.

**Исправление:** Добавить строгую проверку в `doFilter`:

```java
if (botToken == null || botToken.isBlank()) {
    LOGGER.severe("Telegram bot token is not configured");
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Auth configuration error");
    return;
}
```

### Проблема 2.3 — `RuntimeException` в `GlobalExceptionHandler` возвращает детали

**Описание:** `@ExceptionHandler(RuntimeException.class)` возвращает `ex.getMessage()` клиенту с HTTP 500. Это может раскрывать внутренние детали (SQL ошибки, stack traces).

**Исправление:**

```java
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
    LOGGER.severe("Unexpected error: " + ex.getMessage());
    ErrorResponse error = new ErrorResponse("Внутренняя ошибка сервера");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
}
```

### Проблема 2.4 — `PhotoController` не проверяет `userId` / couple

**Описание:** `uploadPhoto` берёт `telegramId` из request, но не проверяет `userId` и не валидирует права. `getPhoto` вообще не требует аутентификации и отдаёт любое фото по `fileId`.

**Исправление:** Добавить проверку аутентификации (через фильтр) и, при необходимости, проверку couple для `getPhoto`.

---

## Раздел 3: Backend — REST API / Контроллеры

### Проблема 3.1 — Дублирование `getUserFromRequest`

**Описание:** Один и тот же метод копируется в `RecipeController`, `MealPlanController`, `ShoppingListController`.

**Исправление:** Вынести в `@ModelAttribute` метод в базовый класс или `@ControllerAdvice`:

```java
@RestControllerAdvice
public class UserArgumentResolver {

    @ModelAttribute("currentUser")
    public User resolveUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
```

Или использовать `HandlerMethodArgumentResolver`.

### Проблема 3.2 — Неконсистентные HTTP-ответы

**Описание:** Некоторые endpoints возвращают сырые DTO без `ResponseEntity`, другие — с `ResponseEntity`. Например, `RecipeController.getRecipeById()` возвращает `RecipeDto`, а `createRecipe` — `ResponseEntity<RecipeDto>`.

**Исправление:** Привести все endpoints к единому стилю с `ResponseEntity`.

### Проблема 3.3 — Отсутствует валидация `id` в path variables

**Описание:** `@PathVariable Long id` не валидируется как `@Positive`. Можно передать `-1` или `0`.

**Исправление:** Использовать `@Validated` на классе + `@Positive` на параметрах, или проверять в сервисах.

---

## Раздел 4: Backend — Сервисы / Бизнес-логика

### Проблема 4.1 — `MealPlanService.addMealPlanEntry` не проверяет владельца рецепта

**Описание:** Можно добавить в план рецепт из другой пары, указав чужой `recipeId`.

**Исправление:**

```java
public MealPlanEntryDto addMealPlanEntry(CreateMealPlanEntryRequest request, Long coupleId) {
    Recipe recipe = recipeRepository.findById(request.getRecipeId())
            .orElseThrow(() -> new ResourceNotFoundException("Recipe not found: " + request.getRecipeId()));

    if (!recipe.getCouple().getId().equals(coupleId)) {
        throw new AccessDeniedException("Recipe does not belong to your couple");
    }

    // ... остальная логика
}
```

### Проблема 4.2 — `CoupleService.leaveCouple` — баг с orphan deletion

**Описание:** После `user.setCouple(null)` и `save(user)` в кеше 1-го уровня Hibernate объект `couple.getUsers()` всё ещё содержит пользователя. `remainingUsers` фильтрует `u.getCouple() != null`, но в LAZY-коллекции может быть stale data. Пара может удалиться, хотя второй пользователь ещё в ней.

**Исправление:** Перед удалением делать `fetch` или проверять через `userRepository.countByCoupleId(couple.getId())`.

```java
@Transactional
public void leaveCouple(Long telegramId) {
    User user = userRepository.findByTelegramId(telegramId)
        .orElseThrow(() -> new CoupleNotFoundException("Пользователь не найден"));

    Couple couple = user.getCouple();
    if (couple == null) {
        return;
    }

    user.setCouple(null);
    userRepository.save(user);

    long remainingCount = userRepository.countByCoupleId(couple.getId());
    if (remainingCount == 0) {
        coupleRepository.delete(couple);
    }
}
```

И добавить в `UserRepository`:

```java
long countByCoupleId(Long coupleId);
```

### Проблема 4.3 — `RecipeService` — `Unit.valueOf(i.getUnit())` без обработки ошибок

**Описание:** Если клиент отправит невалидное значение unit, будет `IllegalArgumentException` → 500.

**Исправление:** Добавить `@Pattern` или кастомный валидатор на DTO, либо ловить исключение в `GlobalExceptionHandler`.

### Проблема 4.4 — `ShoppingListService.regenerateShoppingList` — N+1

**Описание:** В цикле вызывается `shoppingListItemRepository.save(item)` для каждого aggregated item. Лучше использовать `saveAll`.

**Исправление:**

```java
List<ShoppingListItem> toSave = aggregated.values().stream()
    .peek(item -> item.setCouple(couple))
    .collect(Collectors.toList());
List<ShoppingListItem> savedItems = shoppingListItemRepository.saveAll(toSave);
```

---

## Раздел 5: Backend — JPA / Сущности

### Проблема 5.1 — `Ingredient.weightInGrams` — вводящее в заблуждение название

**Описание:** Поле называется `weightInGrams`, но `Unit` может быть `MILLILITER` или `PIECE`.

**Исправление:** Переименовать в `quantity` или `amount`. Это требует миграции БД (dev — H2 auto-update, prod — ручная миграция или Flyway/Liquibase).

```java
private Double quantity; // или amount
```

### Проблема 5.2 — Отсутствуют ограничения `@Column(nullable = false)` на важных полях

**Описание:** `Recipe.name`, `Ingredient.name`, `MealPlanEntry.date` не помечены как `nullable = false`.

**Исправление:** Добавить `nullable = false` и соответствующие валидации в DTO.

### Проблема 5.3 — `Recipe.instructions` (4000) и `Recipe.description` (2000) — риск truncation

**Описание:** Длинные текстовые поля ограничены. Для рецептов это может быть мало.

**Исправление:** Использовать `@Column(columnDefinition = "TEXT")` или `@Lob` для длинных текстов.

---

## Раздел 6: Backend — Тесты

### Проблема 6.1 — `@WebMvcTest` с `addFilters = false`

**Описание:** Все `@WebMvcTest` отключают фильтры. Это значит, что тесты не проверяют аутентификацию вообще.

**Исправление:** Убрать `addFilters = false`. Мокать `TelegramInitDataFilter` или выставлять `requestAttr` в `MockMvc`.

### Проблема 6.2 — Отсутствуют интеграционные тесты

**Описание:** Нет `@SpringBootTest` с Testcontainers или H2. Не тестируются репозитории, транзакции, каскады.

**Исправление:** Добавить `@DataJpaTest` для репозиториев и `@SpringBootTest(webEnvironment = RANDOM_PORT)` для интеграционных сценариев.

### Проблема 6.3 — Тесты не покрывают `AccessDeniedException`

**Описание:** Нет тестов на попытку доступа к чужим рецептам/планам.

**Исправление:** Добавить параметризованные тесты на cross-couple access.

---

## Раздел 7: Frontend — Архитектура и TypeScript

### Проблема 7.1 — `ApiService.baseUrl = '/api'` — не конфигурируется

**Описание:** URL захардкожен. Для production (когда frontend и backend на одном домене) это ок, но для dev с ngrok нужна гибкость.

**Исправление:** Использовать `environment.ts`:

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: '/api'
};
```

```typescript
// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: '/api'
};
```

В `ApiService`:

```typescript
protected baseUrl = environment.apiUrl;
```

### Проблема 7.2 — `proxy.conf.json` содержит placeholder

**Описание:** `target` = `https://a1b2c3d4.ngrok-free.app`. Это не работает для локальной разработки без ngrok.

**Исправление:** Заменить на `http://localhost:8080` по умолчанию. Скрипт `start-frontend-https.ps1` перезаписывает proxy при запуске, но default должен быть рабочим.

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

### Проблема 7.3 — `CoupleService extends ApiService` — антипаттерн наследования сервисов

**Описание:** Сервисы Angular не должны наследовать друг от друга. Лучше композиция.

**Исправление:**

```typescript
@Injectable({ providedIn: 'root' })
export class CoupleService {
  private couple$ = new BehaviorSubject<Couple | null>(null);

  constructor(private api: ApiService) {}

  getCouple(): Observable<Couple> {
    return this.api.http.get<Couple>(`${this.api.baseUrl}/couple`).pipe(
      tap(couple => this.couple$.next(couple))
    );
  }
  // ...
}
```

Или, ещё лучше, каждый сервис инжектит `HttpClient` напрямую.

### Проблема 7.4 — `telegramInitDataInterceptor` — типизация

**Описание:** `(window as any).Telegram?.WebApp` — плохая типизация. Уже есть `TelegramService` с корректными типами.

**Исправление:** Использовать `TelegramService` или корректные типы из `@twa-dev/types`.

### Проблема 7.5 — Отсутствует обработка ошибок HTTP в сервисах

**Описание:** Сервисы не оборачивают HTTP ошибки в user-friendly сообщения. Нет retry logic.

**Исправление:** Добавить глобальный ErrorHandler в Angular и retry в сервисах:

```typescript
import { ErrorHandler, Injectable } from '@angular/core';
import { TelegramService } from './telegram.service';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  constructor(private telegram: TelegramService) {}

  handleError(error: any): void {
    console.error(error);
    this.telegram.showPopup('Ошибка', 'Что-то пошло не так. Попробуйте позже.');
  }
}
```

И зарегистрировать в `app.config.ts`:

```typescript
{ provide: ErrorHandler, useClass: GlobalErrorHandler }
```

### Проблема 7.6 — `coupleGuard` — race condition

**Описание:** При каждом переходе `coupleGuard` делает HTTP-запрос. Если пользователь быстро кликает, может быть race condition.

**Исправление:** Кэшировать результат `getCouple()` с `shareReplay(1)` в `CoupleService`.

---

## Раздел 8: Инфраструктура / DevOps

### Проблема 8.1 — Отсутствует `Dockerfile` для backend и frontend

**Исправление:** Создать `Dockerfile` для Spring Boot и `nginx.conf` для Angular. Пример backend:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY backend/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Проблема 8.2 — `docker-compose.yml` только для PostgreSQL

**Исправление:** Добавить сервисы `backend` и `frontend` (или хотя бы `backend`) в `docker-compose.yml` для полноценного local prod-like окружения.

### Проблема 8.3 — `application.yml` prod — жёстко заданы credentials

**Описание:** `username: postgres`, `password: password` в `application.yml`. Это небезопасно для production.

**Исправление:** Использовать переменные окружения:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/recipe_db}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: validate
```

И добавить `telegram.bot-token` тоже через env:

```yaml
telegram:
  bot-token: ${TELEGRAM_BOT_TOKEN:}
```

### Проблема 8.4 — Отсутствует CI/CD pipeline

**Исправление:** Добавить `.github/workflows/ci.yml` для Gradle build + Angular build + tests.

---

## Раздел 9: Документация

### Проблема 9.1 — Нет `AGENTS.md`

**Исправление:** Создать короткий `AGENTS.md` с инструкциями по сборке, тестам и ключевым конвенциям.

### Проблема 9.2 — `frontend/README.md` некорректен

**Описание:** Упоминает Vitest, но используется Karma.

**Исправление:** Удалить или переписать `frontend/README.md`.

---

## Итоговый план исправлений (по приоритету)

### 🔴 Критический (блокирует production / git)

| # | Задача | Файлы |
|---|--------|-------|
| 1 | Удалить вложенный `.git` в `frontend/` и закоммитить | `frontend/.git` |
| 2 | Закоммитить `SpaController.java` | `backend/.../SpaController.java` |
| 3 | Исправить `SecurityConfig` — требовать аутентификацию | `SecurityConfig.java`, `TelegramInitDataFilter.java` |
| 4 | Добавить проверку `botToken` в `TelegramInitDataFilter` | `TelegramInitDataFilter.java` |
| 5 | Убрать `backend/src/main/resources/static/` из git, добавить в `.gitignore` | `.gitignore` |

### 🟠 Высокий (баги бизнес-логики, безопасность данных)

| # | Задача | Файлы |
|---|--------|-------|
| 6 | Исправить `leaveCouple` — stale collection bug | `CoupleService.java`, `UserRepository.java` |
| 7 | Проверить владельца рецепта в `MealPlanService.addMealPlanEntry` | `MealPlanService.java` |
| 8 | Скрыть internal error details в `GlobalExceptionHandler` | `GlobalExceptionHandler.java` |
| 9 | Добавить `@Validated` / `@Positive` на path variables | Контроллеры |
| 10 | Убрать дублирование `getUserFromRequest` | Новый `UserArgumentResolver.java` |

### 🟡 Средний (архитектура, тесты, конфигурация)

| # | Задача | Файлы |
|---|--------|-------|
| 11 | Переименовать `weightInGrams` → `quantity` | Сущности, DTO, frontend models |
| 12 | Исправить `proxy.conf.json` — localhost по умолчанию | `frontend/proxy.conf.json` |
| 13 | Использовать `environment.ts` для API URL | `frontend/environments/*`, `ApiService.ts` |
| 14 | Рефактор `CoupleService extends ApiService` → композиция | Frontend services |
| 15 | Добавить `@DataJpaTest` и интеграционные тесты | `backend/src/test/java/...` |
| 16 | Убрать `addFilters = false` в `@WebMvcTest` | Тесты контроллеров |
| 17 | Добавить `Dockerfile` и расширить `docker-compose.yml` | `Dockerfile`, `docker-compose.yml` |
| 18 | Перевести DB credentials и bot-token на env vars | `application.yml` |

### 🟢 Низкий (документация, DX, оптимизация)

| # | Задача | Файлы |
|---|--------|-------|
| 19 | Добавить `AGENTS.md` | `AGENTS.md` |
| 20 | Исправить `frontend/README.md` | `frontend/README.md` |
| 21 | Использовать `saveAll` в `ShoppingListService` | `ShoppingListService.java` |
| 22 | Добавить `@Lob` / `TEXT` для длинных текстов | `Recipe.java` |
| 23 | Добавить глобальный ErrorHandler в Angular | `app.config.ts` |
| 24 | Добавить `shareReplay` в `CoupleService` | `CoupleService.ts` |
| 25 | Добавить CI/CD pipeline (GitHub Actions) | `.github/workflows/ci.yml` |

---

## Проверка после исправлений

```bash
# 1. Backend tests
cd backend
./gradlew test

# 2. Backend build
./gradlew bootJar

# 3. Frontend tests
cd ../frontend
npm test -- --watch=false --browsers=ChromeHeadless

# 4. Frontend build
npm run build

# 5. Docker Compose
cd ..
docker-compose up -d postgres
# verify backend starts against PostgreSQL
```

**Plan saved to:** `docs/superpowers/plans/2026-06-02-project-audit-plan.md`

---

*Plan generated by OpenCode using writing-plans skill.*

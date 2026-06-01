# Дизайн: Telegram Mini App для пар — Планировщик питания

**Дата:** 2026-06-01  
**Статус:** Черновик

---

## Обзор

Telegram Mini App для пар, позволяющее совместно управлять рецептами, планировать питание на неделю и формировать список покупок. Два пользователя объединяются в "пару" через код-приглашение и видят общие данные.

---

## Целевая аудитория

Пары/семьи, которые хотят:
- Сохранять любимые рецепты с фото
- Планировать меню на неделю
- Автоматически генерировать список покупок
- Совместно редактировать данные в реальном времени

---

## Технологический стек

### Backend (уже реализован частично)
- **Java 21** + **Spring Boot 3.3.5**
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL 15** (prod) / **H2** (dev)
- **Gradle** (Kotlin DSL)
- **Lombok** для boilerplate

### Frontend (с нуля)
- **Angular 18.2** (standalone components)
- **TypeScript 5.5**
- **SCSS** (без UI-библиотек)
- **@twa-dev/sdk** — Telegram Web App SDK
- **RxJS BehaviorSubject** для state management

### Хранение фото
- **Telegram Bot API** — фото сохраняются в Telegram, хранится `file_id`
- Бесплатно, без дополнительной инфраструктуры

---

## Модель данных

### Новые сущности

#### `User`
| Поле | Тип | Описание |
|------|-----|----------|
| `id` | Long (PK) | Автоинкремент |
| `telegramId` | Long (unique) | Telegram user ID |
| `username` | String | Имя пользователя |
| `couple` | ManyToOne → Couple | Ссылка на пару |

#### `Couple`
| Поле | Тип | Описание |
|------|-----|----------|
| `id` | Long (PK) | Автоинкремент |
| `inviteCode` | String (unique) | 6-символьный код приглашения |
| `createdAt` | LocalDateTime | Дата создания |

### Существующие сущности (добавить FK)

Все существующие сущности получают FK `couple_id`:
- `Recipe` → `couple_id`
- `MealPlanEntry` → `couple_id`
- `ShoppingListItem` → `couple_id`

### Обновление enum `MealType`

```java
public enum MealType {
    BREAKFAST,        // Завтрак
    LUNCH,            // Обед
    AFTERNOON_SNACK,  // Полдник
    DINNER            // Ужин
}
```

---

## API Endpoints

### Couple Management (новые)

| Метод | Путь | Описание |
|-------|------|----------|
| `POST` | `/api/couple/create` | Создать пару, вернуть inviteCode |
| `POST` | `/api/couple/join?code=ABC123` | Присоединиться к паре по коду |
| `GET` | `/api/couple` | Получить информацию о своей паре |
| `DELETE` | `/api/couple/leave` | Покинуть пару (если остаётся 1 человек — пара удаляется вместе с данными) |

### Photo Upload (новый)

| Метод | Путь | Описание |
|-------|------|----------|
| `POST` | `/api/recipes/upload-photo` | Загрузить фото (multipart), вернуть file_id |

### Существующие endpoints (обновить)

Все существующие endpoints фильтруют данные по `couple_id` текущего пользователя:
- `/api/recipes` — только рецепты пары
- `/api/meal-plan` — только план пары
- `/api/shopping-list` — только список покупок пары

---

## Telegram интеграция

### Backend: `TelegramInitDataFilter`
- Извлекает `user.id` из Telegram init data
- Находит/создаёт `User` по `telegramId`
- Добавляет `userId` в request attributes
- В dev-профиле отключён

### Frontend: `TelegramService`
- Инициализация Web App SDK
- Применение темы Telegram (цвета, шрифты)
- **Main Button** — основные действия ("Сохранить", "Добавить")
- **Back Button** — навигация назад
- **openCamera/openGallery** — загрузка фото рецептов
- **share** — поделиться invite-кодом

---

## Frontend архитектура

### Структура модулей

```
app/
├── core/
│   ├── interceptors/
│   │   └── TelegramInitDataInterceptor  — добавляет X-Telegram-Init-Data header
│   ├── guards/
│   │   └── CoupleGuard                  — редирект на /join если нет пары
│   └── services/
│       └── TelegramService              — обёртка над @twa-dev/sdk
├── features/
│   ├── home/                            — Главный экран с карточками
│   ├── recipes/                         — Список рецептов
│   │   ├── recipe-detail/               — Детальный вид (табы)
│   │   └── recipe-form/                 — Создание/редактирование
│   ├── meal-plan/                       — План на неделю
│   ├── shopping-list/                   — Список покупок
│   └── couple/                          — Создание/присоединение к паре
├── shared/
│   ├── models/                          — TypeScript интерфейсы
│   ├── services/                        — API сервисы
│   └── components/                      — Переиспользуемые компоненты
```

### Роутинг

| Путь | Компонент | Guard |
|------|-----------|-------|
| `/join` | CoupleJoinComponent | — |
| `/` | HomeComponent | CoupleGuard |
| `/recipes` | RecipeListComponent | CoupleGuard |
| `/recipes/new` | RecipeFormComponent | CoupleGuard |
| `/recipes/:id` | RecipeDetailComponent | CoupleGuard |
| `/recipes/:id/edit` | RecipeFormComponent | CoupleGuard |
| `/plan` | MealPlanComponent | CoupleGuard |
| `/shopping` | ShoppingListComponent | CoupleGuard |

### State Management

Каждый сервис хранит свой стейт в `BehaviorSubject`:

```typescript
@Injectable()
export class RecipeService {
  private recipes$ = new BehaviorSubject<Recipe[]>([]);
  
  getRecipes(): Observable<Recipe[]> {
    return this.recipes$.asObservable();
  }
  
  loadRecipes(): void {
    this.http.get<Recipe[]>('/api/recipes')
      .subscribe(recipes => this.recipes$.next(recipes));
  }
}
```

Компоненты подписываются через `async pipe`:

```typescript
@Component({
  template: `
    @for (recipe of recipes$ | async; track recipe.id) {
      <app-recipe-card [recipe]="recipe" />
    }
  `
})
export class RecipeListComponent {
  recipes$ = this.recipeService.getRecipes();
}
```

---

## Экраны и пользовательские сценарии

### Экран "Присоединение к паре" (`/join`)

**UI:**
- Заголовок: "Планировщик питания"
- Две кнопки:
  - "Создать пару" → генерирует код, показывает код + кнопку "Поделиться"
  - "Ввести код" → поле ввода 6 символов + кнопка "Присоединиться"

**Логика:**
1. Тап "Создать пару" → `POST /api/couple/create` → получить `{ inviteCode: "ABC123" }`
2. Показать код крупно + кнопка "Поделиться" (Telegram share)
3. Тап "Ввести код" → показать поле ввода
4. Ввод кода → `POST /api/couple/join?code=ABC123` → редирект на `/`

---

### Экран "Главная" (`/`)

**UI:**
- Заголовок: "Планировщик питания"
- 4 крупные карточки-раздела (grid 2x2):
  - 📖 Рецепты
  - 📅 План на неделю
  - 🛒 Список покупок
  - 👤 Профиль пары

**Логика:**
- Тап на карточку → навигация к соответствующему разделу

---

### Экран "Рецепты" (`/recipes`)

**UI:**
- Заголовок: "Рецепты"
- Сетка карточек 2 колонки (фото + название)
- FAB (Floating Action Button) "+" в правом нижнем углу

**Логика:**
- Тап на карточку → `/recipes/:id`
- Тап на "+" → `/recipes/new`

---

### Экран "Детальный рецепт" (`/recipes/:id`)

**UI:**
- Фото вверху (на всю ширину, aspect-ratio 16:9)
- Название (h1) + описание (subtitle)
- Табы: "Ингредиенты" | "Приготовление"
- Кнопка "Добавить в план" (открывает модалку)
- Иконка карандаша (редактировать) в правом верхнем углу
- Иконка корзины (удалить) рядом

**Табы:**
- **Ингредиенты:** список с граммовками (например, "Свёкла — 300г")
- **Приготовление:** пошаговая инструкция

**Модалка "Добавить в план":**
- Мультиселект дней (чекбоксы: Пн, Вт, Ср, Чт, Пт, Сб, Вс) — можно выбрать несколько дней
- Выбор типа приёма пищи (dropdown: Завтрак/Обед/Полдник/Ужин)
- Кнопка "Добавить" (создаёт по записи `MealPlanEntry` для каждого выбранного дня)

**Логика:**
- Тап на карандаш → `/recipes/:id/edit`
- Тап на корзину → confirm → `DELETE /api/recipes/:id` → назад к списку
- Тап "Добавить в план" → модалка → `POST /api/meal-plan` → toast "Добавлено"

---

### Экран "Создание/редактирование рецепта" (`/recipes/new` или `/recipes/:id/edit`)

**UI:**
- Поле "Название" (input)
- Поле "Описание" (textarea, max 2000 символов)
- Кнопка "Добавить фото" → Telegram openCamera/openGallery
- Превью фото (если загружено)
- Секция "Ингредиенты" с кнопкой "+ Ингредиент":
  - Для каждого ингредиента:
    - Поле "Название" (input)
    - Поле "Количество" (number input)
    - Dropdown "Единица" (г/мл/шт)
  - Кнопка "Удалить" (иконка корзины) рядом с каждым
- Поле "Инструкция приготовления" (textarea, max 4000 символов)
- Main Button (Telegram): "Сохранить"

**Логика:**
1. Тап "Добавить фото" → `telegram.openCamera()` или `telegram.openGallery()`
2. Получить фото → `POST /api/recipes/upload-photo` (multipart) → получить `file_id`
3. Заполнить форму
4. Тап "Сохранить" (Main Button):
   - Если `/recipes/new` → `POST /api/recipes` с body:
     ```json
     {
       "name": "Борщ",
       "description": "Классический борщ",
       "photoUrl": "file_id_from_upload",
       "instructions": "1. Сварить бульон...",
       "ingredients": [
         { "name": "Свёкла", "weightInGrams": 300, "unit": "GRAM" }
       ]
     }
     ```
   - Если `/recipes/:id/edit` → `PUT /api/recipes/:id`
5. Редирект на `/recipes/:id` (или `/recipes` если новый)

---

### Экран "План на неделю" (`/plan`)

**UI:**
- Заголовок: "План на неделю"
- Подзаголовок: диапазон дат (например, "26 мая — 1 июня")
- Кнопки "← Пред. неделя" / "След. неделя →"
- 7 карточек-дней (Пн–Вс), каждая содержит:
  - Заголовок дня: "Пн, 26 мая"
  - 4 слота:
    - 🌅 Завтрак: название рецепта или "—"
    - ☀️ Обед: название рецепта или "—"
    - 🍪 Полдник: название рецепта или "—"
    - 🌙 Ужин: название рецепта или "—"
  - Тап на слот → действие (см. логику)
- Кнопка "Сгенерировать список покупок" внизу (фиксированная)

**Логика:**
- Тап на пустой слот ("—") → модалка выбора рецепта из списка → `POST /api/meal-plan`
- Тап на занятый слот (название рецепта) → две опции:
  - "Перейти к рецепту" → `/recipes/:id`
  - "Удалить из плана" → `DELETE /api/meal-plan/:entryId`
- Тап "← Пред. неделя" → загрузить предыдущую неделю → `GET /api/meal-plan?weekStart=YYYY-MM-DD`
- Тап "След. неделя →" → загрузить следующую неделю
- Тап "Сгенерировать список покупок" → `POST /api/shopping-list/regenerate?weekStart=YYYY-MM-DD` → редирект на `/shopping`

---

### Экран "Список покупок" (`/shopping`)

**UI:**
- Заголовок: "Список покупок"
- Подзаголовок: диапазон дат (например, "26 мая — 1 июня")
- Плоский список продуктов с чекбоксами:
  - Каждый элемент: `[☐] Название — количество`
  - Отмеченные: `[☑] Название — количество` (зачёркнуто, opacity 0.5)
  - Ручные товары: помечены "добавлено вручную" (мелкий текст, иконка)
- FAB "+" для добавления ручного товара
- Кнопка "Перегенерировать" (иконка обновления) в правом верхнем углу

**Модалка "Добавить продукт":**
- Поле "Название" (input)
- Поле "Количество" (number input)
- Dropdown "Единица" (г/мл/шт)
- Кнопка "Добавить"

**Логика:**
- Тап на чекбокс → `PATCH /api/shopping-list/items/:id` (toggle `checked`)
- Тап на "+" → модалка → `POST /api/shopping-list/items` с body:
  ```json
  {
    "ingredientName": "Кофе",
    "totalQuantity": 200,
    "unit": "GRAM",
    "manual": true
  }
  ```
- Тап "Перегенерировать" → `POST /api/shopping-list/regenerate?weekStart=YYYY-MM-DD` (сохраняет ручные товары)

---

### Экран "Профиль пары" (доступен с главной)

**UI:**
- Заголовок: "Профиль пары"
- Секция "Участники":
  - Имя 1 (текущий пользователь)
  - Имя 2 (партнёр)
- Секция "Код приглашения":
  - Код крупно (например, "ABC123")
  - Кнопка "Поделиться" (Telegram share)
- Кнопка "Покинуть пару" (красная, внизу)

**Логика:**
- Тап "Поделиться" → `telegram.share({ url: "https://t.me/bot?start=ABC123" })`
- Тап "Покинуть пару" → confirm → `DELETE /api/couple/leave` → редирект на `/join`

---

## Обработка ошибок

### Backend

Добавить `@ControllerAdvice` для глобальной обработки ошибок:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CoupleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCoupleNotFound() {
        return ResponseEntity.status(404)
            .body(new ErrorResponse("Пара не найдена"));
    }
    
    @ExceptionHandler(InvalidInviteCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCode() {
        return ResponseEntity.status(400)
            .body(new ErrorResponse("Неверный код приглашения"));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation() {
        return ResponseEntity.status(400)
            .body(new ErrorResponse("Ошибка валидации"));
    }
}
```

### Frontend

- HTTP Interceptor для обработки 401/403 (редирект на `/join`)
- Toast-уведомления для ошибок (например, "Не удалось загрузить рецепт")
- Loading spinners для async операций

---

## Тестирование

### Backend (уже есть частично)
- Unit-тесты сервисов (JUnit 5 + Mockito)
- Integration-тесты контроллеров (MockMvc)
- Покрыть новые endpoints для Couple

### Frontend (добавить)
- Unit-тесты сервисов (Jasmine)
- Component-тесты для ключевых компонентов (Angular TestBed)

---

## Deployment

### Backend
- Docker Compose для PostgreSQL (уже есть)
- Добавить Dockerfile для Spring Boot app
- Переменные окружения: `TELEGRAM_BOT_TOKEN`, `DATABASE_URL`

### Frontend
- `ng build --configuration production`
- Статические файлы в `dist/frontend/browser/`
- Хостинг: любой статический хостинг (Netlify, Vercel, GitHub Pages)
- Настроить CORS в backend для домена frontend

---

## Этапы реализации

1. **Backend: Модель данных для пар**
   - Добавить сущности `User`, `Couple`
   - Обновить существующие сущности (добавить `couple_id`)
   - Реализовать endpoints для Couple
   - Обновить `TelegramInitDataFilter` для извлечения `userId`

2. **Backend: Загрузка фото**
   - Реализовать `POST /api/recipes/upload-photo`
   - Интеграция с Telegram Bot API (`sendPhoto`, `getFile`)

3. **Frontend: Базовая структура**
   - Настроить роутинг
   - Реализовать `TelegramService`, `CoupleGuard`, `TelegramInitDataInterceptor`
   - Экран `/join` (создание/присоединение к паре)

4. **Frontend: Рецепты**
   - Экран `/recipes` (список карточек)
   - Экран `/recipes/:id` (детальный вид с табами)
   - Экран `/recipes/new` и `/recipes/:id/edit` (форма с загрузкой фото)

5. **Frontend: План на неделю**
   - Экран `/plan` (вертикальный список дней)
   - Модалка выбора рецепта
   - Навигация по неделям

6. **Frontend: Список покупок**
   - Экран `/shopping` (плоский список с чекбоксами)
   - Добавление ручных товаров
   - Кнопка "Перегенерировать"

7. **Frontend: Главная и профиль**
   - Экран `/` (карточки-разделы)
   - Экран профиля пары

8. **Тестирование и полировка**
   - Написать тесты
   - Обработка ошибок
   - UI/UX полировка

---

## Открытые вопросы

- **Реальное время:** Нужно ли синхронизировать изменения между пользователями в реальном времени (WebSocket)? Или достаточно polling каждые 30 секунд?
- **Уведомления:** Нужно ли отправлять уведомления в Telegram (например, "Партнёр добавил рецепт")?
- **Экспорт:** Нужна ли возможность экспорта списка покупок в текстовый файл или Telegram-сообщение?
- **История:** Нужно ли хранить историю планов на прошлые недели?

---

## Приложения

### A. TypeScript интерфейсы

```typescript
interface User {
  id: number;
  telegramId: number;
  username: string;
}

interface Couple {
  id: number;
  inviteCode: string;
  users: User[];
}

interface Recipe {
  id: number;
  name: string;
  description: string;
  photoUrl: string;
  instructions: string;
  ingredients: Ingredient[];
  createdAt: string;
  updatedAt: string;
}

interface Ingredient {
  id: number;
  name: string;
  weightInGrams: number;
  unit: 'GRAM' | 'MILLILITER' | 'PIECE';
}

interface MealPlanEntry {
  id: number;
  date: string;
  recipe: Recipe;
  mealType: 'BREAKFAST' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER';
}

interface ShoppingListItem {
  id: number;
  weekStartDate: string;
  ingredientName: string;
  totalQuantity: number;
  unit: 'GRAM' | 'MILLILITER' | 'PIECE';
  checked: boolean;
  manual: boolean;
}
```

### B. Примеры API запросов/ответов

#### Создание пары
```http
POST /api/couple/create
Authorization: Telegram initData

Response 200:
{
  "id": 1,
  "inviteCode": "ABC123"
}
```

#### Присоединение к паре
```http
POST /api/couple/join?code=ABC123
Authorization: Telegram initData

Response 200:
{
  "id": 1,
  "inviteCode": "ABC123",
  "users": [
    { "id": 1, "telegramId": 123456, "username": "alice" },
    { "id": 2, "telegramId": 789012, "username": "bob" }
  ]
}
```

#### Загрузка фото
```http
POST /api/recipes/upload-photo
Content-Type: multipart/form-data

file: <binary data>

Response 200:
{
  "fileId": "AgACAgIAAxkBAAIBZ2..."
}
```

#### Создание рецепта
```http
POST /api/recipes
Content-Type: application/json

{
  "name": "Борщ",
  "description": "Классический украинский борщ",
  "photoUrl": "AgACAgIAAxkBAAIBZ2...",
  "instructions": "1. Сварить бульон\n2. Нашинковать капусту...",
  "ingredients": [
    { "name": "Свёкла", "weightInGrams": 300, "unit": "GRAM" },
    { "name": "Капуста", "weightInGrams": 200, "unit": "GRAM" }
  ]
}

Response 201:
{
  "id": 1,
  "name": "Борщ",
  ...
}
```

---

**Конец документа**

# Telegram Mini App — Планировщик питания для пар

Приложение для пар, позволяющее совместно управлять рецептами, планировать питание на неделю и формировать список покупок.

## Возможности

- **Рецепты**: Сохраняйте рецепты с фото, ингредиентами и инструкциями
- **План на неделю**: Планируйте завтраки, обеды, полдники и ужины на 7 дней
- **Список покупок**: Автоматическая генерация из плана + ручное добавление
- **Совместное использование**: Два пользователя делят общие данные через код-приглашение

## Технологический стек

### Backend
- Java 21
- Spring Boot 3.3.5
- PostgreSQL 15 / H2 (dev)
- Gradle 8.14.2

### Frontend
- Angular 18.2
- TypeScript 5.5
- SCSS
- @twa-dev/sdk (Telegram Web App SDK)

## Запуск

### Backend

```bash
cd backend
set JAVA_HOME=C:\Users\max99\.jdks\ms-21.0.11
gradlew.bat bootRun
```

Backend запустится на `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm start
```

Frontend запустится на `http://localhost:4200` с прокси на backend.

### HTTPS через Ngrok (для Telegram Mini App)

Telegram Mini App требует HTTPS. Для тестирования используйте Ngrok:

#### 1. Установка Ngrok

```powershell
winget install ngrok.ngrok
```

#### 2. Настройка Authtoken

Зарегистрируйтесь на [ngrok.com](https://ngrok.com) и выполните:

```powershell
ngrok config add-authtoken <YOUR_AUTHTOKEN>
```

#### 3. Автоматический запуск Backend + HTTPS

```powershell
# Запускает backend и создаёт HTTPS туннель
.\scripts\start-with-https.ps1
```

Или вручную:

```powershell
# Терминал 1: Backend
cd backend
$env:JAVA_HOME="C:\Users\max99\.jdks\ms-21.0.11"
.\gradlew.bat bootRun

# Терминал 2: Ngrok
cd shop-telegram-mini
ngrok http 8080
```

#### 4. Полученный HTTPS URL

Ngrok выведет что-то вроде:

```
Forwarding  https://a1b2c3d4.ngrok-free.app -> http://localhost:8080
```

**Используйте этот URL в настройках Telegram Bot.**

#### 5. Настройка Telegram Bot

1. Откройте [@BotFather](https://t.me/botfather)
2. Выберите вашего бота → `Bot Settings` → `Menu Button` → `Configure menu button`
3. Введите URL: `https://a1b2c3d4.ngrok-free.app`
4. Название кнопки: `Планировщик`

> ⚠️ **Важно**: URL на бесплатном плане ngrok меняется при каждом запуске. Для production настройте статический домен.

#### 6. Отдельный запуск Frontend с HTTPS

Если нужно разработать frontend с HTTPS:

```powershell
.\scripts\start-frontend-https.ps1 -BackendUrl "https://a1b2c3d4.ngrok-free.app"
```

## Использование

1. Откройте приложение в Telegram (настройте бота через BotFather)
2. Создайте пару или введите код-приглашение от партнёра
3. Добавляйте рецепты с фото
4. Планируйте питание на неделю
5. Генерируйте список покупок из плана

## Структура проекта

```
shop-telegram-mini/
├── backend/          # Spring Boot API
├── frontend/         # Angular приложение
├── scripts/          # Скрипты для запуска
├── docs/              # Документация
│   ├── ngrok-setup.md # Настройка HTTPS
│   └── ...
```

## HTTPS для Production

Для production рекомендуется:
1. **Let's Encrypt + Nginx** — бесплатные сертификаты + reverse proxy
2. **Cloudflare** — CDN + SSL
3. **Платный план Ngrok** — статический домен

См. `docs/ngrok-setup.md` для подробной настройки.

## Лицензия

MIT

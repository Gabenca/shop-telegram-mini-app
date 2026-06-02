# Настройка HTTPS через Ngrok

## Что такое Ngrok?

Ngrok — инструмент для создания защищённых туннелей к localhost. Автоматически предоставляет HTTPS URL, который можно использовать в Telegram Mini App.

## Установка Ngrok

### Вариант 1: Скачать с официального сайта

1. Перейдите на [ngrok.com/download](https://ngrok.com/download)
2. Скачайте версию для Windows
3. Распакуйте архив
4. Добавьте путь к `ngrok.exe` в переменную среды `PATH`

### Вариант 2: Через Chocolatey

```powershell
choco install ngrok
```

### Вариант 3: Через Winget

```powershell
winget install ngrok.ngrok
```

## Регистрация (обязательно для HTTPS)

1. Зарегистрируйтесь на [ngrok.com](https://ngrok.com)
2. Получите Authtoken в личном кабинете
3. Выполните команду:

```powershell
ngrok config add-authtoken <YOUR_AUTHTOKEN>
```

## Быстрый запуск

### Ручной запуск

```powershell
# 1. Запустите backend
cd backend
$env:JAVA_HOME="C:\Users\max99\.jdks\ms-21.0.11"
.\gradlew.bat bootRun

# 2. В новом терминале запустите ngrok
cd shop-telegram-mini
ngrok http 8080
```

### Автоматический запуск через скрипт

```powershell
# Запускает backend и ngrok в одном окне
.\scripts\start-with-https.ps1
```

## Полученный URL

Ngrok выведет что-то вроде:

```
Forwarding  https://a1b2c3d4.ngrok-free.app -> http://localhost:8080
```

**Этот HTTPS URL используйте в Telegram Bot для Web App.**

## Настройка Telegram Bot

1. Откройте @BotFather
2. Выберите вашего бота
3. Выберите `Bot Settings` → `Menu Button` → `Configure menu button`
4. Введите URL: `https://a1b2c3d4.ngrok-free.app`
5. Название кнопки: `Планировщик`

## Важные ограничения Ngrok

- **Бесплатный план:** URL меняется при каждом запуске
- **Ограничение:** ~40 запросов в минуту
- **Сессия:** ~2 часа (после этого нужно перезапустить)

Для production используйте **Let's Encrypt + Nginx** или обновитесь до платного плана Ngrok (статический домен).

# Production Setup Guide

## Prerequisites

- Docker + Docker Compose
- Telegram Bot Token (from @BotFather)
- Domain with HTTPS (or use Ngrok for testing)

## Environment Variables

Create a `.env` file in the project root:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/recipe_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_secure_password

# Telegram
TELEGRAM_BOT_TOKEN=your_bot_token_from_botfather
```

## Docker Compose Deployment

```bash
# Build and start all services
docker-compose up --build -d

# View logs
docker-compose logs -f backend

# Stop everything
docker-compose down
```

## Telegram Bot Configuration

1. Open [@BotFather](https://t.me/botfather) in Telegram
2. Select your bot → `Bot Settings` → `Menu Button` → `Configure menu button`
3. Enter your production URL (e.g., `https://your-domain.com`)
4. Button text: `Планировщик`

## Health Checks

The backend exposes `/actuator/health` for monitoring:

```bash
curl https://your-domain.com/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

## Database Migrations

Flyway runs automatically on startup in production profile. Migration files are located in:
`backend/src/main/resources/db/migration/`

## HTTPS Setup

For production, use one of:
1. **Let's Encrypt + Nginx** (recommended)
2. **Cloudflare** (CDN + SSL)
3. **Paid Ngrok** (static domain)

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `Failed to validate Telegram init data` | Check `TELEGRAM_BOT_TOKEN` is set correctly |
| `Connection refused` to PostgreSQL | Ensure `postgres` service is healthy before backend starts |
| 404 on Angular routes | Verify `SpaController.java` is present and compiled |

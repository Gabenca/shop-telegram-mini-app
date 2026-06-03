# Project Overview: shop-telegram-mini

## Type
Telegram Mini App — meal planner for couples (Russian language UI).

## Project Positioning
A lightweight, single-purpose tool for two-person households to:
- Maintain a shared recipe book with photos, ingredients, and instructions
- Plan weekly meals (breakfast, lunch, snack, dinner × 7 days)
- Auto-generate a shopping list from the plan + manually add ad-hoc items
- Share all of the above via a couple-pairing invite code

Built as a Telegram Mini App (frontend runs inside Telegram's WebView, opened from a bot menu button). No public marketing site, no third-party login.

## Target Users
- Couples who want to coordinate meals
- Russian-speaking audience (UI labels, comments, README all in Russian)
- Users comfortable with Telegram as their primary app

## Core Features
1. **Recipes** — CRUD with photo upload, ingredient list, free-form instructions
2. **Meal Plan** — week grid (4 meal types × 7 days), drag/select recipes into slots
3. **Shopping List** — auto-derived from meal plan ingredients (aggregated by unit) + manual items
4. **Couples** — invite-code based pairing; both members see the same recipes, plan, and list

## Technical Highlights
- **Backend:** Spring Boot 3.3.5, Java 21, JPA, H2 (dev) / PostgreSQL 15 (prod), Spring Security with custom `TelegramInitDataFilter` (validates `X-Telegram-Init-Data` header against bot token HMAC)
- **Frontend:** Angular 18.2 standalone components, feature-based structure, `proxy.conf.json` for dev API forwarding, `@twa-dev/sdk` for Telegram WebApp integration
- **Auth model:** No passwords. User identity comes from Telegram init data (HMAC of `user` + `auth_date` against bot token). A `UserArgumentResolver` extracts the resolved user from request attributes and binds it to controller methods.
- **Multi-tenancy:** All entities (`Recipe`, `MealPlanEntry`, `ShoppingListItem`, `Couple`) are scoped by `couple_id`. Users can only see their own couple's data.

## Development Status
**Active development / MVP.** The audit memory that previously lived under `project-audit/` has been removed as part of adopting OpenSem-style project memories; known issues should be re-discovered on demand or re-recorded in `task_checklist.md`.

## Non-Goals (for now)
- No CI/CD pipeline
- No Docker image for the backend (only PostgreSQL is containerised)
- No integration tests beyond the existing `CoupleIntegrationTest`
- No PWA, no offline mode
- No multi-language UI (Russian only)

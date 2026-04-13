# Hireme

A backend-heavy job hunting automation engine built to solve a real problem — the noise-to-signal ratio in job boards is terrible. Hireme cuts through it by running every job listing through a Gemini AI scoring pipeline and surfacing only the ones worth applying to.

## Architecture

The system is split into two independently deployable runtime profiles from a single Spring Boot codebase:

```
[SQS Queue]
     │
     ▼
[Worker Profile]          ← Consumes SQS events, runs AI scoring, persists to DB
     │
     ▼
[PostgreSQL / Neon]
     │
     ▼
[API Profile]             ← Serves the dashboard, handles re-analysis, manages config
     │
     ▼
[React Dashboard]         ← Jobs, executions, settings, AI config
```

This profile-based split means the worker can run as an AWS Lambda (event-driven, scales to zero) while the API runs as a persistent service — both sharing the same codebase and database schema.

## Engineering Highlights

**Configuration-driven AI scoring**
The system prompt, user prompt template, candidate profile, and search criteria are all stored in PostgreSQL and editable via the dashboard at runtime. No redeployment needed to tune the AI behavior.

**Idempotent job ingestion**
Every incoming job is checked against existing title + company combinations before processing. Duplicates are skipped and counted against the execution totals — preventing wasted Gemini quota on jobs already seen.

**Quota management with time-windowing**
Daily Gemini API limits are tracked per-day in the database with pessimistic locking to handle concurrent worker instances. The quota is split across morning and afternoon windows to avoid exhausting the daily budget in a single run.

**Execution lifecycle tracking**
Every scraping run is modeled as a `JobExecution` entity that tracks jobs received, accepted, rejected, failed, skipped, and dropped — giving full visibility into pipeline health from the dashboard.

**Transactional analysis result handling**
Each AI analysis result is persisted in a `REQUIRES_NEW` transaction — isolating failures per-job so a single bad response doesn't roll back the entire batch.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 17, Spring Boot 3 |
| AI | Google Gemini (configurable model) |
| Queue | AWS SQS |
| Database | PostgreSQL (Neon) |
| Security | Spring Security, JWT |
| Notifications | Slack Webhooks |
| Deployment | AWS Lambda (worker), EC2 (api) |

## Key Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/login` | JWT login |
| GET | `/api/v1/jobs` | Paginated job list with status filter |
| GET | `/api/v1/jobs/{id}` | Job detail with full AI analysis breakdown |
| PATCH | `/api/v1/jobs/{id}/status` | Update recruitment pipeline status |
| POST | `/api/v1/jobs/{id}/analyse` | Dispatch re-analysis to SQS |
| GET | `/api/v1/observation/executions` | Recent execution history |
| GET | `/api/v1/stats/dashboard` | Live pipeline metrics |
| GET/PUT | `/api/v1/settings/ai-config` | Update AI prompt at runtime |
| GET/PUT | `/api/v1/settings/profile` | Manage candidate profile used in prompts |

## Running Locally

```bash
# Fill in environment variables
cp .env.example .env

# Start API server
export $(cat .env | xargs) && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local,api
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| `NEON_DB_URL` | PostgreSQL connection string |
| `JWT_SECRET` | JWT signing key |
| `GEMINI_API_KEY` | Google Gemini API key |
| `GEMINI_MODEL_VERSION` | Model to use (default: gemini-2.5-flash) |
| `ADMIN_USERNAME` | Dashboard admin username |
| `ADMIN_PASSWORD` | Dashboard admin password |
| `SLACK_WEBHOOK_URL` | Slack incoming webhook URL |

---

Built by Sameer

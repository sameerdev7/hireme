# JobHunter – Analysis & API Service (Java / Spring Boot)

## What this is
This service powers job scoring and retrieval. It analyzes incoming jobs using an AI-based scoring pipeline, persists results to PostgreSQL, and exposes REST APIs for the ingestor and dashboard.

It is deployed as two AWS Lambda functions from a single codebase using Spring Profiles:
- **worker**: SQS-driven ingestion + analysis  
- **api**: REST endpoints for the dashboard + re-analysis  

## Responsibilities
- Consume jobs from SQS (worker)
- Score jobs using an LLM with configurable prompts
- Persist jobs and scoring metadata to PostgreSQL
- Send Slack notifications for high scoring jobs
- Expose REST APIs for querying jobs (api)
- Trigger re-analysis of existing jobs when configuration changes (api)

## Key design choices
- **Configuration-driven scoring**: search rules, AI prompts, and user profile context are managed via the UI and stored in the database (not hard-coded).
- **Dual execution paths**:
  - SQS for bulk ingestion analysis  
  - REST API for on-demand re-analysis  
- **Profile-based composition**:
  - Worker secured via AWS IAM (no HTTP exposure)
  - API secured via Spring Security (JWT)

## How it fits in the system
SQS → Worker Lambda (score + persist) → PostgreSQL  
Dashboard → API Lambda (query + config + re-analysis) → PostgreSQL  

## Tech
Java • Spring Boot • AWS Lambda • Amazon SQS • PostgreSQL • Spring Security

## Related Repositories
- Architecture: https://github.com/iamvusumzi/jobhunter-system  
- Ingestion Service: https://github.com/iamvusumzi/jobhunter-ingestor  
- Dashboard: https://github.com/iamvusumzi/jobhunter-dashboard  

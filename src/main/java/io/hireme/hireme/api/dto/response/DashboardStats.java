package io.hireme.hireme.api.dto.response;

public record DashboardStats(

        // 1. Action Items
        int newMatchesCount,
        int totalActiveJobs,

        // 2. Gemini Health (Daily)
        int geminiRequestsToday,
        int geminiDailyLimit,
        int geminiTokenCountToday,

        // 3. Pipeline Metrics
        ExecutionStats pipelineStats,

        // 4. Quality Metrics
        double avgCompatibilityScore,
        long avgLatencyMs
) {}


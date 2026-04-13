package io.hireme.hireme.observation.analysis;

import java.util.Collections;
import java.util.List;

public record JobAnalysisResult(
        Integer relevanceScore,
        String matchRationale,
        List<String> extractedSkills,
        Integer tokenUsage,
        Long latencyMs,
        String modelVersion,
        boolean isSuccess
) {
    public static JobAnalysisResult failure(String errorMessage, long latency) {
        return new JobAnalysisResult(
                30,
                "Analysis Failed: " + errorMessage,
                Collections.emptyList(),
                0,
                latency,
                "unknown",
                false
        );
    }
}

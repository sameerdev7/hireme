package io.hireme.hireme.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AnalysisDetailDto(
        int compatibilityScore,
        String rationale,
        List<String> skills,
        int tokenUsage,
        Long latencyMs,
        String modelVersion,
        String promptSnapshot, // Useful for debugging
        LocalDateTime analysedAt
) {}

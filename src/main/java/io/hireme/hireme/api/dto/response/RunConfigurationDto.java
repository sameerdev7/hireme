package io.hireme.hireme.api.dto.response;

import java.util.List;

public record RunConfigurationDto(
        String executionId,
        String query,
        List<String> excludedKeywords,
        List<String> excludedSkills,
        String location,
        int dailyLimit,
        boolean active
) {
}

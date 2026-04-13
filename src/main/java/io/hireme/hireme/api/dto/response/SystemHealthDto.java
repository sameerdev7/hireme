package io.hireme.hireme.api.dto.response;

import java.time.LocalDateTime;

public record SystemHealthDto(
        int totalJobsAnalysed,
        LocalDateTime lastRunTime,
        String systemStatus,
        String version,
        double matchRate
) {
}

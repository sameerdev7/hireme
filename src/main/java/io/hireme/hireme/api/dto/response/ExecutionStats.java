package io.hireme.hireme.api.dto.response;

public record ExecutionStats(
        int totalFound,
        int totalFiltered,
        int totalAnalysed,
        int totalAccepted,
        int totalRejected
        ) {
}

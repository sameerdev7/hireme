package io.hireme.hireme.job;

public record IngestionEvent(
        String executionId,
        String source,
        String title,
        String description,
        String company,
        String location,
        String url
) {
}

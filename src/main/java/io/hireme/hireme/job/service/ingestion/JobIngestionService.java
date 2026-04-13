package io.hireme.hireme.job.service.ingestion;

import io.hireme.hireme.job.IngestionEvent;

public interface JobIngestionService {
    void handleJobEvent(IngestionEvent event);

}

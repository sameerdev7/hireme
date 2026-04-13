package io.hireme.hireme.observation.execution;

import io.hireme.hireme.api.dto.request.CompleteIngestionRequest;
import io.hireme.hireme.api.dto.response.RunConfigurationDto;
import io.hireme.hireme.job.RecruitmentStatus;


public interface ExecutionService {
    RunConfigurationDto generateRunConfiguration();
    void handleIngestionCompletion(String executionId, CompleteIngestionRequest request);
    void recordAnalysisOutcome(String executionId, RecruitmentStatus jobStatus, int tokensUsed, boolean isAnalysisSuccessful);
    void incrementSkippedCount(String executionId);
    void incrementDroppedCount(String executionId);
}

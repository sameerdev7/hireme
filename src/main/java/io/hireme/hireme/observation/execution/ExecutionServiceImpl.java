package io.hireme.hireme.observation.execution;

import io.hireme.hireme.api.dto.request.CompleteIngestionRequest;
import io.hireme.hireme.api.dto.response.RunConfigurationDto;
import io.hireme.hireme.job.RecruitmentStatus;
import io.hireme.hireme.job.config.SearchCriteria;
import io.hireme.hireme.job.config.SearchCriteriaRepository;
import io.hireme.hireme.observation.quota.QuotaService;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;


@Service
@AllArgsConstructor
@Log4j2
public class ExecutionServiceImpl implements ExecutionService {

    private final JobExecutionRepository repository;
    private final SearchCriteriaRepository searchCriteriaRepository;
    private final QuotaService quotaService;

    @Override
    @Transactional
    public RunConfigurationDto generateRunConfiguration() {
        SearchCriteria searchCriteria = searchCriteriaRepository.findFirstByIsActiveTrue();

        int remainingQuota = quotaService.calculateBatchLimit();

        if (remainingQuota <= 0 || searchCriteria == null) {
            return new RunConfigurationDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    remainingQuota,
                    false
            );
        }


        JobExecution execution = JobExecution.builder()
                .searchCriteria(searchCriteria)
                .status(JobExecution.ExecutionStatus.RUNNING)
                .build();
        JobExecution savedExecution = repository.save(execution);

        return new RunConfigurationDto(
                savedExecution.getId(),
                searchCriteria.getQuery(),
                searchCriteria.getExcludedKeywords(),
                searchCriteria.getExcludedSkills(),
                searchCriteria.getLocation(),
                remainingQuota,
                true
        );
    }

    @Override
    @Transactional
    public void handleIngestionCompletion(String executionId, CompleteIngestionRequest request) {
        JobExecution execution = repository.findByIdWithLock(executionId)
                .orElseThrow(()-> new IllegalArgumentException("Unknown execution id " + executionId));

        execution.completeIngestion(request.getTotalJobsSent(), request.getSourceResults());

        repository.save(execution);

        if (execution.isCompleted()) {
            logCompletion(execution);
        } else {
            log.info("Ingestion Phase Complete. Waiting for analysis of {} jobs.", execution.getExpectedTotal());
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAnalysisOutcome(String executionId, RecruitmentStatus jobStatus, int tokensUsed, boolean isAnalysisSuccessful) {
        repository.findByIdWithLock(executionId).ifPresent(execution -> {

            execution.recordAnalysisOutcome(jobStatus, tokensUsed, isAnalysisSuccessful);
            repository.save(execution);

            if (execution.isCompleted()) {
                logCompletion(execution);
            }
        });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementSkippedCount(String executionId) {
        repository.findByIdWithLock(executionId).ifPresent(exec -> {

            exec.recordSkip();
            repository.save(exec);

            if (exec.isCompleted()) {
                logCompletion(exec);
            }
        });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementDroppedCount(String executionId) {
        repository.findByIdWithLock(executionId).ifPresent(exec -> {
            log.warn("💀 Job dropped in execution {}", executionId);
            exec.recordDrop();
            repository.save(exec);

            if (exec.isCompleted()) {
                logCompletion(exec);
            }
        });
    }

    private void logCompletion(JobExecution exec) {
        log.info("🏁 Execution {} Closed. [Skipped: {} | Accepted: {} | Rejected: {} | Failed: {} | Dropped: {} ]",
                exec.getId(),exec.getJobsSkipped(), exec.getJobsAccepted(), exec.getJobsRejected(), exec.getJobsFailed(), exec.getJobsDropped());
    }

}

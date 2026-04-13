package io.hireme.hireme.job.service.ingestion;

import io.hireme.hireme.job.JobListing;
import io.hireme.hireme.job.JobRepository;
import io.hireme.hireme.job.RecruitmentStatus;
import io.hireme.hireme.job.service.analysis.JobAnalysisProcessor;
import io.hireme.hireme.job.IngestionEvent;
import io.hireme.hireme.observation.execution.ExecutionService;
import io.hireme.hireme.observation.execution.JobExecution;
import io.hireme.hireme.observation.execution.JobExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobIngestionServiceImpl implements JobIngestionService {

    private final JobRepository jobRepository;
    private final JobExecutionRepository executionRepository;
    private final ExecutionService executionService;
    private final JobAnalysisProcessor analysisProcessor;

    @Override
    public void handleJobEvent(IngestionEvent event){
        try {
            JobListing job = createJobListing(event);
            analysisProcessor.analyzeAndEnrich(job);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Constraint violation for job '{}': {}. Dropping.", event.title(), ex.getMessage());
            executionService.incrementDroppedCount(event.executionId());
        } catch (Exception ex) {
            log.error("Unexpected error while processing job '{}': {}", event.title(), ex.getMessage(), ex);
            executionService.incrementDroppedCount(event.executionId());
        }
    }

    private JobListing createJobListing(IngestionEvent event){
        log.info("Creating JobListing for job: {}", event.title());
        JobExecution execution = executionRepository.findById(event.executionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Execution Id: "+ event.executionId())); // TODO custom exception

        String companyName = (event.company() != null && !event.company().isBlank())
                ? event.company()
                : "Unknown Company";

        JobListing job = JobListing.builder()
                .title(event.title())
                .company(companyName)
                .url(event.url())
                .location(event.location())
                .source(event.source())
                .description(event.description())
                .foundInExecution(execution)
                .recruitmentStatus(RecruitmentStatus.INCOMING)
                .build();
        JobListing savedJob = jobRepository.saveAndFlush(job);
        log.info("saved job {}", savedJob.getTitle());
        return savedJob;
    }

}

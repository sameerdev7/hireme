package io.hireme.hireme;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hireme.hireme.common.exception.ResourceNotFoundException;
import io.hireme.hireme.job.ReanalysisRequest;
import io.hireme.hireme.job.service.ingestion.IdempotencyService;
import io.hireme.hireme.job.service.ingestion.JobIngestionService;
import io.hireme.hireme.job.IngestionEvent;
import io.hireme.hireme.job.service.workflow.JobWorkflowService;
import io.hireme.hireme.observation.execution.ExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.function.Consumer;

@Slf4j
@Configuration
@Profile("worker")
@RequiredArgsConstructor
public class WorkerConfig {

    private final IdempotencyService idempotencyService;
    private final JobIngestionService jobIngestionService;
    private final JobWorkflowService workflowService;
    private final ExecutionService executionService;
    private final ObjectMapper objectMapper;

    @Bean
    public Consumer<SQSEvent> ingestionConsumer() {
        return sqsEvent -> {
            for (SQSEvent.SQSMessage message : sqsEvent.getRecords()){
                try {
                    SQSEvent.MessageAttribute typeAttr = message.getMessageAttributes().get("type");
                    String jobType = (typeAttr != null) ? typeAttr.getStringValue() : null;

                    String jsonBody = message.getBody();

                    if ("RE_ANALYSIS".equals(jobType)) {
                        ReanalysisRequest request = objectMapper.readValue(jsonBody, ReanalysisRequest.class);
                        log.info("Worker: Starting re-analysis for Job ID: {}", request.jobId());
                        try {
                            workflowService.reAnalyseJob(request.jobId());
                        } catch (ResourceNotFoundException e) {
                            log.warn("Worker: Job ID {} not found", request.jobId());
                        }
                        
                    } else {
                        IngestionEvent event = objectMapper.readValue(jsonBody, IngestionEvent.class);
                        log.info("Worker received ingestion event: {}", event.title());

                        if (idempotencyService.isJobDuplicate(event.title(), event.company())) {
                            log.info("Skipping duplicate job {} for company {}", event.title(), event.company());
                            executionService.incrementSkippedCount(event.executionId());
                            continue;
                        }
                        jobIngestionService.handleJobEvent(event);
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to process message ID: {}", message.getMessageId(),e);
                    throw new RuntimeException(e);
                }
            }

        };
    }
}

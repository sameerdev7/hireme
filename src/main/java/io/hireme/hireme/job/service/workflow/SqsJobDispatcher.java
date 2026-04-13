package io.hireme.hireme.job.service.workflow;

import io.hireme.hireme.job.ReanalysisRequest;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("api")
@RequiredArgsConstructor
@Slf4j
public class SqsJobDispatcher {

    private final SqsTemplate sqsTemplate;

    @Value("${app.sqs.queue-name}")
    private String queueName;

    public void dispatchReanalysis(Long jobId) {
        ReanalysisRequest payload = new ReanalysisRequest(jobId);

        log.info("Dispatching re-analysis for Job ID: {} to SQS", jobId);

        sqsTemplate.send(to -> to
                .queue(queueName)
                .payload(payload)
                .header("type", "RE_ANALYSIS")
        );
    }
}

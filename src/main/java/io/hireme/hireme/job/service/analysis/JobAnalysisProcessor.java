package io.hireme.hireme.job.service.analysis;

import io.hireme.hireme.job.AnalysisStatus;
import io.hireme.hireme.job.JobListing;
import io.hireme.hireme.observation.analysis.JobAnalysisResult;
import io.hireme.hireme.observation.quota.QuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobAnalysisProcessor {

    private final DeepAnalysisService deepAnalysisService;
    private final JobAnalysisResultHandler jobAnalysisResultHandler;
    private final QuotaService  quotaService;

    @Value("${analysis.gemini.min-interval-ms}")
    private long minIntervalMs;

    public void analyzeAndEnrich(JobListing job) {
        AnalysisStatus initialStatus = job.getAnalysisStatus();
        boolean isFromWorker = (initialStatus == AnalysisStatus.PENDING);

        job.setAnalysisStatus(AnalysisStatus.IN_PROGRESS);

        if(!quotaService.tryConsumeQuota()){
            log.warn("⛔ Daily Quota Empty. Job {} remains PENDING for tomorrow.", job.getId());
            job.setAnalysisStatus(initialStatus);
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            log.info("Starting Deep Analysis for Job: {} (Source: {})",
                    job.getTitle(), isFromWorker ? "Worker" : "Manual/Re-analysis");

            JobAnalysisResult result = deepAnalysisService.runDeepAnalysis(
                    job.getTitle(),
                    job.getDescription()
                );

            jobAnalysisResultHandler.handleAnalysisResult(job, result, initialStatus);

        } catch (Exception e) {
            log.error("Analysis Failed", e);
            JobAnalysisResult result = JobAnalysisResult.failure(e.getMessage(), 0);
            jobAnalysisResultHandler.handleAnalysisResult(job, result, initialStatus);

        } finally {
            if(isFromWorker){
                applyThrottling(startTime);
            } else {
                log.info("Skipping throttling for re-analysis job: {}", job.getTitle());
            }
        }
    }

    private void applyThrottling(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        long requiredSleep = minIntervalMs - duration;

        if (requiredSleep > 0) {
            try {
                log.info("Throttling SQS Worker: Analysis took {} ms. Sleeping for {} ms.", duration, requiredSleep);
                Thread.sleep(requiredSleep);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}

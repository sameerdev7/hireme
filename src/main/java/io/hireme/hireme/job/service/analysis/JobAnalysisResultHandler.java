package io.hireme.hireme.job.service.analysis;


import io.hireme.hireme.job.AnalysisStatus;
import io.hireme.hireme.job.JobListing;
import io.hireme.hireme.job.JobRepository;
import io.hireme.hireme.job.RecruitmentStatus;
import io.hireme.hireme.job.notification.NotificationService;
import io.hireme.hireme.observation.analysis.AnalysisLog;
import io.hireme.hireme.observation.analysis.JobAnalysisResult;
import io.hireme.hireme.observation.execution.ExecutionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class JobAnalysisResultHandler {

    private final JobRepository jobRepository;
    private final ExecutionService executionService;
    private final NotificationService notificationService;

    private static final int MATCH_THRESHOLD = 65;
    private static final int NOTIFICATION_THRESHOLD = 70;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAnalysisResult(JobListing job, JobAnalysisResult result, AnalysisStatus initialStatus) {

        boolean isNewJob = (initialStatus == AnalysisStatus.PENDING);

        if (result.isSuccess()) {
            job.setAnalysisStatus(AnalysisStatus.COMPLETED);
        } else {
            job.setAnalysisStatus(AnalysisStatus.FAILED);
        }
        applyAnalysisData(job, result);

        JobListing savedJob = jobRepository.save(job);

        handleSideEffects(savedJob, result, isNewJob);

        log.info("Async Cycle Complete: {} [Status: {}]", savedJob.getTitle(), savedJob.getRecruitmentStatus());
    }

    private void applyAnalysisData(JobListing job, JobAnalysisResult result) {
        AnalysisLog logEntry = AnalysisLog.builder()
                .compatibilityScore(result.relevanceScore())
                .matchRationale(result.matchRationale())
                .extractedSkills(result.extractedSkills())
                .tokenCount(result.tokenUsage())
                .latencyMs(result.latencyMs())
                .modelVersion(result.modelVersion())
                .build();

        job.setAnalysis(logEntry);
        job.setPriorityScore(result.relevanceScore());

        RecruitmentStatus status = result.relevanceScore() >= MATCH_THRESHOLD
                ? RecruitmentStatus.NEW_MATCH
                : RecruitmentStatus.AUTO_REJECTED;

        job.setRecruitmentStatus(status);
    }

    private void handleSideEffects(JobListing savedJob, JobAnalysisResult result, boolean isNewJob) {
        if (isNewJob && savedJob.getFoundInExecution() != null) {
            executionService.recordAnalysisOutcome(
                    savedJob.getFoundInExecution().getId(),
                    savedJob.getRecruitmentStatus(),
                    result.tokenUsage(),
                    result.isSuccess()
            );
        }

        if (isNewJob && shouldNotify(savedJob)) {
            log.info("🔔 High Match Found ({}). Sending Notification...", savedJob.getPriorityScore());
            notificationService.sendNotification(
                    savedJob.getTitle(),
                    savedJob.getUrl(),
                    savedJob.getPriorityScore(),
                    savedJob.getSource()
            );
        }
    }

    private boolean shouldNotify(JobListing job) {
        return job.getRecruitmentStatus() == RecruitmentStatus.NEW_MATCH
                && job.getPriorityScore() >= NOTIFICATION_THRESHOLD;
    }
}

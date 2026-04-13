package io.hireme.hireme.observation;

import io.hireme.hireme.api.dto.response.*;
import io.hireme.hireme.common.AppMapper;
import io.hireme.hireme.job.JobRepository;
import io.hireme.hireme.job.RecruitmentStatus;
import io.hireme.hireme.observation.analysis.AnalysisLog;
import io.hireme.hireme.observation.analysis.AnalysisLogRepository;
import io.hireme.hireme.observation.execution.JobExecution;
import io.hireme.hireme.observation.execution.JobExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ObservationService {

    private final JobExecutionRepository executionRepo;
    private final JobRepository jobRepo;
    private final AnalysisLogRepository analysisRepo;
    private final AppMapper mapper;

    public List<ExecutionSummaryDto> getRecentExecutions(int limit) {
        Pageable limitRequest = PageRequest.of(0, limit, Sort.by("startedAt").descending());
        Page<JobExecution> jobExecutionPage = executionRepo.findAll(limitRequest);

        return jobExecutionPage.getContent().stream()
                .map(mapper::mapToSummary)
                .toList();
    }

    public ExecutionDetailDto getExecutionDetails(String id) {
        JobExecution exec = executionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found"));

        List<SourceLogDto> logs = exec.getSourceLogs().stream().map(mapper:: mapSourceLog).toList();
        return new ExecutionDetailDto(
                mapper.mapToSummary(exec),
                exec.getJobsSkipped(),
                exec.getJobsAnalysed(),
                exec.getJobsRejected(),
                exec.getJobsFailed(),
                exec.getTotalTokenUsed(),
                exec.getSearchCriteria().getSearchName(),
                logs,
                null
        );
    }

    public DashboardStats getDashboardStats() {
        int newMatches = Math.toIntExact(jobRepo.countByRecruitmentStatus(RecruitmentStatus.NEW_MATCH));
        int active = Math.toIntExact(jobRepo.count());

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        List<AnalysisLog> todayLogs = analysisRepo.findByAnalysedAtAfter(startOfDay);

        int requests = todayLogs.size();
        int tokens = todayLogs.stream().mapToInt(AnalysisLog::getTokenCount).sum();
        double avgScore = todayLogs.stream().mapToInt(AnalysisLog::getCompatibilityScore).average().orElse(0.0);
        long avgLatency = (long) todayLogs.stream().mapToLong(AnalysisLog::getLatencyMs).average().orElse(0.0);

        List<JobExecution> recentExecs = executionRepo.findTop5ByOrderByStartedAtDesc();
        int totalFound = recentExecs.stream()
                .mapToInt(JobExecution::totalJobsFound)
                .sum();

        int totalFiltered = recentExecs.stream()
                .mapToInt(JobExecution::totalJobsFiltered)
                .sum();

        int totalAnalysed = recentExecs.stream()
                .mapToInt(JobExecution::getJobsAnalysed)
                .sum();

        int totalAccepted = recentExecs.stream()
                .mapToInt(JobExecution::getJobsAccepted)
                .sum();

        int totalRejected = recentExecs.stream()
                .mapToInt(JobExecution::getJobsRejected)
                .sum();

        ExecutionStats stats = new ExecutionStats(
                totalFound,
                totalFiltered,
                totalAnalysed,
                totalAccepted,
                totalRejected
        );

        return new DashboardStats(
            newMatches,
                active,
                requests,
                18,
                tokens,
                stats,
                avgScore,
                avgLatency
        );

    }
}
package io.hireme.hireme.common;

import io.hireme.hireme.api.dto.response.*;
import io.hireme.hireme.job.JobListing;
import io.hireme.hireme.observation.analysis.AnalysisLog;
import io.hireme.hireme.observation.execution.JobExecution;
import io.hireme.hireme.observation.execution.SourceRunLog;
import io.hireme.hireme.user.CandidateProfile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AppMapper {

    public JobListingDto  toJobListingResponse(JobListing job) {
        if  (job == null) return null;

        return new JobListingDto (
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getUrl(),
                job.getLocation(),
                job.getPriorityScore(),
                job.getRecruitmentStatus().toString(),
                job.getAnalysisStatus().toString(),
                job.getSource(),
                job.getFoundInExecution().getId(),
                job.getCreatedAt()
        );
    }

    public JobDetailDto toJobDetailResponse(JobListing job) {
        if  (job == null) return null;
        AnalysisLog log = job.getCurrentAnalysis();
        AnalysisDetailDto analysisDetail = mapToAnalysisDto(log);
        JobListingDto jobListingDto = toJobListingResponse(job);

        return new JobDetailDto (
                jobListingDto,
                job.getDescription(),
                analysisDetail
        );
    }

    public ExecutionSummaryDto mapToSummary(JobExecution execution) {
        if (execution == null) return null;

        int expectedTotal = Objects.requireNonNullElse(execution.getExpectedTotal(), 0);

        return new ExecutionSummaryDto(
                execution.getId(),
                execution.getStatus(),
                execution.getStartedAt(),
                execution.getFinishedAt(),
                expectedTotal,
                execution.getJobsAccepted()
        );
    }

    public SourceLogDto mapSourceLog(SourceRunLog s) {
        if (s == null) return null;

        SourceLogDto dto = new SourceLogDto();
        dto.setSourceName(s.getSourceName());
        dto.setJobsFound(s.getJobsFound());
        dto.setJobsFiltered(s.getJobsFiltered());
        dto.setSuccess(s.isSuccess());
        dto.setError(s.getError());
        return dto;
    }


    private AnalysisDetailDto mapToAnalysisDto(AnalysisLog a) {
    if (a == null) return null;
    return new AnalysisDetailDto(
            a.getCompatibilityScore(),
            a.getMatchRationale(),
            a.getExtractedSkills(),
            a.getTokenCount(),
            a.getLatencyMs(),
            a.getModelVersion(),
            a.getPromptUsed(),
            a.getAnalysedAt()
    );
}

    public CandidateProfileDto mapToCandidateProfileDto(CandidateProfile c) {
        return new CandidateProfileDto(
                c.getId(),
                c.getBio(),
                c.getRawResume(),
                c.getTopSkills(),
                c.getExperienceSummary()
        );
    }

}

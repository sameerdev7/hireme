package io.hireme.hireme.job.service.workflow;

import java.util.Optional;

import io.hireme.hireme.api.dto.response.JobDetailDto;
import io.hireme.hireme.api.dto.response.JobListingDto;

import io.hireme.hireme.job.RecruitmentStatus;
import lombok.NonNull;
import org.springframework.data.domain.Page;

public interface JobWorkflowService {
    Page<JobListingDto> getJobs(RecruitmentStatus status, int page, int size);
    JobDetailDto getJob(Long id);
    Optional<JobListingDto> updateStatus(@NonNull Long id, RecruitmentStatus status);
    void reAnalyseJob(Long id);
    void cleanupOldRejectedJobs();
}

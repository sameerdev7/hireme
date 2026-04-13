package io.hireme.hireme.job.service.workflow;

import java.time.LocalDateTime;
import java.util.Optional;

import io.hireme.hireme.api.dto.response.JobDetailDto;
import io.hireme.hireme.api.dto.response.JobListingDto;
import io.hireme.hireme.common.AppMapper;
import io.hireme.hireme.common.exception.ResourceNotFoundException;
import io.hireme.hireme.job.AnalysisStatus;
import io.hireme.hireme.job.service.analysis.JobAnalysisProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.hireme.hireme.job.JobListing;
import io.hireme.hireme.job.RecruitmentStatus;
import io.hireme.hireme.job.JobRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Log4j2
public class JobWorkflowServiceImpl implements JobWorkflowService {

    private final JobRepository jobRepository;
    private final JobAnalysisProcessor analysisProcessor;
    private final AppMapper mapper;

    @Override
    public JobDetailDto getJob(Long id) {
        JobListing job = jobRepository.findById(id).orElseThrow();
        return mapper.toJobDetailResponse(job);
    }

    @Override
    public Page<JobListingDto> getJobs(RecruitmentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<JobListing> jobPage;

        if (status != null) {
            jobPage = jobRepository.findByRecruitmentStatus(status, pageable);
        } else {
            jobPage = jobRepository.findAll(pageable);
        }
        return jobPage.map(mapper::toJobListingResponse);
    }

    @Override
    public Optional<JobListingDto> updateStatus(@NonNull Long id, RecruitmentStatus status) {
        Optional<JobListing> jobOptional = jobRepository.findById(id);
        if (jobOptional.isEmpty()) {
            return Optional.empty();
        }

        JobListing job = jobOptional.get();
        try {

            job.setRecruitmentStatus(status);
            job.setUpdatedAt(LocalDateTime.now());
            
            JobListing updatedJob = jobRepository.save(job);
            return Optional.of(mapper.toJobListingResponse(updatedJob));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public void reAnalyseJob(Long id){

        JobListing job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        job.setAnalysisStatus(AnalysisStatus.RE_ANALYSIS_PENDING);

        analysisProcessor.analyzeAndEnrich(job);
    }
    /**
     * Scheduled Task to clean up the database.
     * This method runs automatically at 3 AM every Sunday.
     * It deletes jobs that were rejected more than 30 days ago.
     */
    @Override
    @Scheduled(cron = "0 0 3 * * SUN")
    public void cleanupOldRejectedJobs() {
        log.info("Cleaning up old rejected jobs");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        jobRepository.deleteByRecruitmentStatusAndCreatedAtBefore(RecruitmentStatus.AUTO_REJECTED, cutoff);
        log.info("Clean up complete");
    }

}
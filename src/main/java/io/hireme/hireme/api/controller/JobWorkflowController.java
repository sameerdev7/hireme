package io.hireme.hireme.api.controller;


import io.hireme.hireme.api.dto.response.JobDetailDto;
import io.hireme.hireme.api.dto.response.JobListingDto;
import io.hireme.hireme.job.RecruitmentStatus;
import io.hireme.hireme.job.service.workflow.SqsJobDispatcher;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.hireme.hireme.job.service.workflow.JobWorkflowService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Profile("api")
public class JobWorkflowController {

    private final JobWorkflowService service;
    private final SqsJobDispatcher  sqsJobDispatcher;

    @GetMapping("/{id}")
    public ResponseEntity<JobDetailDto> getJob(@PathVariable Long id){
        return ResponseEntity.ok(service.getJob(id));
    }

    // 2. Endpoint for Frontend/You to see all jobs
    @GetMapping
    public PagedModel<JobListingDto> getAllJobs(
            @RequestParam(required = false)RecruitmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        Page<JobListingDto> jobs = service.getJobs(status, page, size);
        return new PagedModel<>(jobs);
    }

    // 3. Endpoint for Frontend/You to update job status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobListingDto> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> update) {
        RecruitmentStatus status = RecruitmentStatus.valueOf(update.get("status"));

        return service.updateStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/analyse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reProcessJob(@PathVariable Long id) {
        sqsJobDispatcher.dispatchReanalysis(id);
        return ResponseEntity.accepted().build();
    }
}
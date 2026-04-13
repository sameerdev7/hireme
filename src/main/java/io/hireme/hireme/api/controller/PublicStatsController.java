package io.hireme.hireme.api.controller;


import io.hireme.hireme.api.dto.response.SystemHealthDto;
import io.hireme.hireme.observation.execution.JobExecution;
import io.hireme.hireme.observation.execution.JobExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/public")
@Profile("api")
@RequiredArgsConstructor
@Log4j2
public class PublicStatsController {

    private final JobExecutionRepository jobExecutionRepository;

    @GetMapping("/stats")
    public ResponseEntity<SystemHealthDto> getSystemHealth()
    {
        List<JobExecution> jobs = jobExecutionRepository.findAll();

        int totalJobsAnalysed = jobs.stream()
                .mapToInt(JobExecution::getJobsAnalysed)
                .sum();

        int totalAcceptedJobs = jobs.stream()
                .mapToInt(JobExecution::getJobsAccepted)
                .sum();

        double matchRate = (totalJobsAnalysed == 0) ? 0.0 : (double) totalAcceptedJobs / totalJobsAnalysed;
        double matchRatePercentage = matchRate * 100;
        String status = "OPERATIONAL";

        LocalDateTime lastRun = jobExecutionRepository.findTopByOrderByStartedAtDesc()
                .map(JobExecution::getStartedAt)
                .orElse(LocalDateTime.now().minusDays(1));

        return ResponseEntity.ok(new SystemHealthDto(
                totalJobsAnalysed,
                lastRun,
                status,
                "v1.0.0",
                matchRatePercentage
        ));
    }
}

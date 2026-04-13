package io.hireme.hireme.api.controller;

import io.hireme.hireme.job.IngestionEvent;
import io.hireme.hireme.job.service.ingestion.JobIngestionService;
import io.hireme.hireme.job.service.workflow.JobWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dev")
@Profile("local")
@RequiredArgsConstructor
public class DevIngestionController {

    private final JobIngestionService jobIngestionService;
    private final JobWorkflowService jobWorkflowService;

    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@RequestBody IngestionEvent event) {
        jobIngestionService.handleJobEvent(event);
        return ResponseEntity.ok("Job ingested: " + event.title());
    }

    @PostMapping("/jobs/{id}/reanalyse")
    public ResponseEntity<String> reanalyse(@PathVariable Long id) {
        jobWorkflowService.reAnalyseJob(id);
        return ResponseEntity.ok("Re-analysis triggered for job: " + id);
    }
}

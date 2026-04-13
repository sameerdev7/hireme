package io.hireme.hireme.api.controller;

import io.hireme.hireme.api.dto.response.ExecutionDetailDto;
import io.hireme.hireme.api.dto.response.ExecutionSummaryDto;
import io.hireme.hireme.observation.ObservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/observation")
@Profile("api")
@RequiredArgsConstructor
public class ObservationController {

    private final ObservationService observationService;

    @GetMapping("/executions")
    public ResponseEntity<List<ExecutionSummaryDto>> getRecentHistory(
            @RequestParam(defaultValue = "10") int limit ) {

        return ResponseEntity.ok(observationService.getRecentExecutions(limit));
    }

    // 2. Get deep dive for one execution
    @GetMapping("/executions/{id}")
    public ResponseEntity<ExecutionDetailDto> getExecutionDetails(@PathVariable String id) {
        try {
            return ResponseEntity.ok(observationService.getExecutionDetails(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

    }

}

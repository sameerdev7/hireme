package io.hireme.hireme.api.controller;

import io.hireme.hireme.api.dto.request.CompleteIngestionRequest;
import io.hireme.hireme.api.dto.response.RunConfigurationDto;
import io.hireme.hireme.observation.execution.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/executions")
@Profile("api")
@RequiredArgsConstructor
public class ExecutionLifecycleController {

    private final ExecutionService executionService;

    @GetMapping("/initiate")
    public ResponseEntity<RunConfigurationDto> initiateRun(){
        return ResponseEntity.ok(executionService.generateRunConfiguration());
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> completeJobIngestion(@PathVariable String id, @RequestBody CompleteIngestionRequest request){
        executionService.handleIngestionCompletion(id, request);
        return ResponseEntity.ok().build();
    }
}


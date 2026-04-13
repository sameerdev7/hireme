package io.hireme.hireme.api.dto.response;

import io.hireme.hireme.observation.execution.JobExecution.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ExecutionSummaryDto {
    private String id;
    private ExecutionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private int totalJobsReceived;
    private int totalJobsAccepted;
}

package io.hireme.hireme.api.dto.request;

import io.hireme.hireme.observation.execution.SourceRunLog;
import lombok.Data;

import java.util.List;

@Data
public class CompleteIngestionRequest {
        private Integer totalJobsSent;
        private List<SourceRunLog> sourceResults;
}


package io.hireme.hireme.api.dto.response;


import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
public class ExecutionDetailDto {
    @JsonUnwrapped
    private ExecutionSummaryDto summary;

    private int jobsSkipped;
    private int jobsAnalysed;
    private int jobsRejected;
    private int jobsFailed;

    private int totalTokensUsed;

    private String searchTerm;
    private List<SourceLogDto> sourceLogs;
    private String failureReason;
}
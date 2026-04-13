package io.hireme.hireme.api.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class JobDetailDto {

    @JsonUnwrapped
    private JobListingDto jobListing;

    private String description;
    private AnalysisDetailDto analysisLog;

}

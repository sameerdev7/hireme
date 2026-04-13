package io.hireme.hireme.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class JobListingDto {
    Long Id;
    String title;
    String company;
    String url;
    String location;
    int priorityScore;
    String recruitmentStatus;
    String analysisStatus;
    String source;
    String executionId;
    LocalDateTime createdAt;
}

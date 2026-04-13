package io.hireme.hireme.api.dto.response;

import lombok.Data;

@Data
public class SourceLogDto {
    private String sourceName; // "Adzuna", "Google"
    private int jobsFound;     // 50
    private int jobsFiltered;
    private boolean success;   // true/false
    private String error;
}

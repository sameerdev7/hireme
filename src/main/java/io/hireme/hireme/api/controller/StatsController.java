package io.hireme.hireme.api.controller;

import io.hireme.hireme.api.dto.response.DashboardStats;
import io.hireme.hireme.observation.ObservationService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@Profile("api")
@AllArgsConstructor
public class StatsController {

    private final ObservationService observationService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(observationService.getDashboardStats());
    }
}

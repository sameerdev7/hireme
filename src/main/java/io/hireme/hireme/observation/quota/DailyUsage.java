package io.hireme.hireme.observation.quota;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "daily_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyUsage {

    @Id
    private LocalDate date;

    private int requestCount;

    @Version
    private Long version;

    public void increment() {
        requestCount++;
    }
}

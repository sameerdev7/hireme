package io.hireme.hireme.observation.quota;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuotaService {

    private final DailyUsageRepository usageRepo;

    public static final int MAX_DAILY_LIMIT = 18;

    @Transactional
    public boolean tryConsumeQuota() {
        LocalDate today = LocalDate.now();
        Optional<DailyUsage> usageOpt = usageRepo.findByIdWithLock(today);

        DailyUsage usage;

        if (usageOpt.isPresent()) {
            // Happy Path: It exists, and we hold the lock.
            usage = usageOpt.get();
        } else {
            // Edge Case: First request of the day.
            // We cannot lock a non-existent row, so we must try to create it.
            try {
                usage = usageRepo.saveAndFlush(new DailyUsage(today, 0, null));
            } catch (DataIntegrityViolationException e) {
                // RACE CONDITION HANDLED:
                // Another worker created it milliseconds before us.
                // Now we can lock the one they created.
                usage = usageRepo.findByIdWithLock(today)
                        .orElseThrow(() -> new IllegalStateException("DailyUsage disappeared!"));
            }
        }

        if (usage.getRequestCount() >= MAX_DAILY_LIMIT) return false;

        usage.increment();
        usageRepo.save(usage);
        return true;
    }

    public int getRemainingQuota() {
        LocalDate today = LocalDate.now();
        return usageRepo.findById(today)
                .map(u -> Math.max(0, MAX_DAILY_LIMIT - u.getRequestCount()))
                .orElse(MAX_DAILY_LIMIT);
    }

    public int calculateBatchLimit() {
        int actualRemaining = getRemainingQuota();

        LocalTime splitTime = LocalTime.of(13, 0);

         if (LocalTime.now().isBefore(splitTime)) {
            int morningBudget = MAX_DAILY_LIMIT / 2;

            int usedSoFar = MAX_DAILY_LIMIT - actualRemaining;

            int remainingInMorningBudget = Math.max(0, morningBudget - usedSoFar);

            return Math.min(actualRemaining, remainingInMorningBudget);
        }

        return actualRemaining;
    }
}
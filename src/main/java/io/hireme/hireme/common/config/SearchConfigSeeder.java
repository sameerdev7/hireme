package io.hireme.hireme.common.config;

import io.hireme.hireme.job.config.SearchCriteria;
import io.hireme.hireme.job.config.SearchCriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchConfigSeeder implements CommandLineRunner {

    private final SearchCriteriaRepository searchCriteriaRepository;

    @Override
    public void run(String... args) throws Exception {
        if(searchCriteriaRepository.count() == 0) {
            System.out.println("🌱 Seeding Dummy Search Configs...");

            // 1. The "Golden" Search
            SearchCriteria javaSearch = new SearchCriteria();
            javaSearch.setSearchName("CPT Java Backend");
            javaSearch.setQuery("Junior Software Engineer");
            javaSearch.setLocation("Cape Town, Western Cape, South Africa");
            javaSearch.setExcludedKeywords(List.of("Intern","Graduate", "senior", "snr", "sr.", "lead", "principal", "manager", "architect", "expert", "data", "governance"));
            javaSearch.setExcludedSkills(List.of(".net", "c#", "php"));
            javaSearch.setMaxDaysOld(7);
            javaSearch.setIsActive(true);

            searchCriteriaRepository.saveAll(java.util.List.of(javaSearch));

            System.out.println("✅ Seeding Complete. 1 Active.");
        }
    }
}

package com.ximad.prism;

import com.ximad.prism.core.model.RecRequest;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppSimulation implements CommandLineRunner {

    private final RecommendationService recommendationService;

    @Override
    public void run(String... args) throws Exception {
        log.info(">>> Initializing Simulation (Advanced Trace Ecosystem)...");

        String yaml = """
                scenarioId: "home_mixed_feed"
                params:
                  total_timeout: 300
                  
                interceptors: ["Performance", "Deduplication", "Exposure"]
                
                timeouts:
                  RECALL: 200
                  RANK: 50
                  FILTER: 30
                  HYDRATION: 100

                experiments:
                  - id: "recall_exp"
                    groups:
                      - name: "full"
                        range: "0-499"
                      - name: "minimal"
                        range: "500-999"
                  - id: "rank_exp"
                    groups:
                      - name: "simple"
                        range: "0-299"
                      - name: "mmoe"
                        range: "300-999"
                    
                experiment:
                  - steps:
                      - name: "ExperimentStrategy"
                        params: {}
                        
                profile:
                  - steps:
                      - name: "MockProfile"
                        params: {}
                        
                filters:
                  - steps:
                      - name: "ExposureFilter"
                        params: {}
                        
                rerank:
                  - steps:
                      - name: "DiversityRerank"
                        params: {}
                      - name: "ExplorationRerank"
                        params: { insert_pos: 2 }
                      - name: "TruncateRerank"
                        params: { limit: 25 }
                        
                hydration:
                  - steps:
                      - name: "MockHydration"
                        params: {}
                    
                recall:
                  - when: { experiment: "recall_exp", variant: "full" }
                    steps:
                      - name: "VectorRecall"
                        params: { timeout: 100 }
                  - when: { experiment: "recall_exp", variant: "minimal" }
                    steps:
                      - name: "CrossDomainRecall"
                        params: { target_biz: "HOUSE" }
                  - steps:
                      - name: "UserInterestRecall"
                        params: {}
                      - name: "HotRecall"
                        params: { count: 30 }

                ranking:
                  - when: { experiment: "rank_exp", variant: "simple" }
                    modelId: "SimpleRank"
                    fusionFormula: "pctr * 100"
                  - when: { experiment: "rank_exp", variant: "mmoe" }
                    modelId: "MMoERank"
                    fusionFormula: "pctr * 100 + pcvr * 50"
                """;

        List<String> testUsers = List.of("alice", "bob", "charlie", "david"); 
        
        for (String uid : testUsers) {
            log.info("\n>>> ================================================");
            log.info(">>> SIMULATION FOR USER: {}", uid);
            log.info(">>> ================================================");
            
            // Set debugMode = true to trigger full trace logging
            RecRequest request = new RecRequest(uid, "1", "home_mixed_feed", new HashMap<>(), 10, true);
            
            List<RecommendItem> results = recommendationService.recommend(request, yaml);
            
            log.info(">>> RESULT SIZE: {}", results.size());
            log.info(">>> ------------------------------------------------");
        }
    }
}

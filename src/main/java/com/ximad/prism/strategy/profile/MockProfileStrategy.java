package com.ximad.prism.strategy.profile;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.UserProfile;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.IProfileStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.StructuredTaskScope;

@Slf4j
@Component("MockProfile")
public class MockProfileStrategy implements IProfileStrategy {
    @Override
    public void load(RecContext ctx, StepConfig config) {
        String userId = ctx.getRequest().userId();

        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.awaitAll(),
                c -> c.withTimeout(Duration.ofMillis(50))
        )) {
            var interestsTask = scope.fork(() -> {
                // Mock RPC
                ctx.getTrace().log(PipelineStage.PROFILE, "get user: {} interests", userId);
                return Collections.singletonList("java");
            });
            var vectorTask = scope.fork(() -> {
                ctx.getTrace().log(PipelineStage.PROFILE, "get user: {} vector", userId);
                return new float[]{0.1f, 0.2f};
            });

            scope.join();

            UserProfile profile = new UserProfile(
                    interestsTask.get(),
                    vectorTask.get(),
                    Collections.emptySet(), // negativeFeedback
                    Set.of("JOB_HIRED"), // implicit intent
                    "Beijing"
            );
            ctx.setUserProfile(profile);

            ctx.getTrace().log(PipelineStage.PROFILE, "get user: {} profile success", userId);
        } catch (Exception e) {
            ctx.setUserProfile(new UserProfile(Collections.emptyList(), new float[0], Collections.emptySet(), Collections.emptySet(), "Unknown"));
            log.error("get user profile failed", e);
            ctx.getTrace().log(PipelineStage.PROFILE, "get user: {} profile failed", userId);
        }
    }
}

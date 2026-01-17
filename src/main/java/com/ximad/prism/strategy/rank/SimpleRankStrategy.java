package com.ximad.prism.strategy.rank;

import com.ximad.prism.config.model.RankConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IRankStrategy;
import org.springframework.stereotype.Component;

@Component("SimpleRank")
public class SimpleRankStrategy implements IRankStrategy {
    @Override
    public void rank(RecContext ctx, RankConfig config) {
        for (RecommendItem item : ctx.getCandidates()) {
            // Simple random score
            double score = Math.random() * 100;
            item.setScore(score);
        }
        ctx.getCandidates().sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
    }
}

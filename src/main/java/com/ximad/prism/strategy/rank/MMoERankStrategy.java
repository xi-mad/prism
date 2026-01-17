package com.ximad.prism.strategy.rank;

import com.ximad.prism.config.model.RankConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IRankStrategy;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component("MMoERank")
public class MMoERankStrategy implements IRankStrategy {
    @Override
    public void rank(RecContext ctx, RankConfig config) {
        for (RecommendItem item : ctx.getCandidates()) {
            // MMoE simulation
            double pctr = Math.random() * 0.1;
            double pcvr = Math.random() * 0.05;
            double score = pctr * 100 + pcvr * 50;
            
            // Calibration
            Double calib = config.calibration().getOrDefault(item.getBizType(), 1.0);
            item.setScore(score * calib);
        }
        ctx.getCandidates().sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
    }
}

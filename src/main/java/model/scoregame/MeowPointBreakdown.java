package model.scoregame;

import java.util.LinkedHashMap;
import java.util.Map;

public record MeowPointBreakdown(int total, Map<String, Integer> patternScores) {
    public MeowPointBreakdown {
        patternScores = Map.copyOf(new LinkedHashMap<>(patternScores));
    }

    public static MeowPointBreakdown empty() {
        return new MeowPointBreakdown(0, Map.of());
    }
}

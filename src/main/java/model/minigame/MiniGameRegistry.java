package model.minigame;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class MiniGameRegistry {

    private static final MiniGameRegistry INSTANCE = new MiniGameRegistry();

    private final Map<MiniGameId, List<MiniGameStageConfig>> stages = new EnumMap<>(MiniGameId.class);

    private MiniGameRegistry() {
        stages.put(MiniGameId.VASE_BREAKER, List.of(
                MiniGameStageConfig.vaseBreaker(1),
                MiniGameStageConfig.vaseBreaker(2),
                MiniGameStageConfig.vaseBreaker(3)));
        stages.put(MiniGameId.WALNUT_BOWLING, List.of(MiniGameStageConfig.placeholder(MiniGameId.WALNUT_BOWLING)));
        stages.put(MiniGameId.I_ZOMBIE, List.of(MiniGameStageConfig.placeholder(MiniGameId.I_ZOMBIE)));
        stages.put(MiniGameId.BEGHOULED, List.of(MiniGameStageConfig.placeholder(MiniGameId.BEGHOULED)));
        stages.put(MiniGameId.ZOMBOTANY, List.of(MiniGameStageConfig.placeholder(MiniGameId.ZOMBOTANY)));
    }

    public static MiniGameRegistry getInstance() {
        return INSTANCE;
    }

    public List<MiniGameStageConfig> getStages(MiniGameId id) {
        return stages.getOrDefault(id, List.of());
    }

    public MiniGameStageConfig getStage(MiniGameId id, int stageIndex) {
        for (MiniGameStageConfig stage : getStages(id)) {
            if (stage.getStageIndex() == stageIndex) {
                return stage;
            }
        }
        return null;
    }

    public List<MiniGameId> getAllMiniGames() {
        return List.of(MiniGameId.values());
    }
}

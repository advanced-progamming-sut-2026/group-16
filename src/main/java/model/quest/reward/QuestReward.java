package model.quest.reward;

public final class QuestReward {

    public enum Type {CURRENCY, UNLOCKABLE, INVENTORY}

    private final Type type;
    private final int coins;
    private final int diamonds;
    private final String unlockTargetId;   // plant id or level id to unlock
    private final String seedPacketPlantId; // plant id for seed packet reward
    private final int seedPacketCount;

    private QuestReward(Builder b) {
        this.type = b.type;
        this.coins = b.coins;
        this.diamonds = b.diamonds;
        this.unlockTargetId = b.unlockTargetId;
        this.seedPacketPlantId = b.seedPacketPlantId;
        this.seedPacketCount = b.seedPacketCount;
    }


    public static QuestReward coins(int amount) {
        return new Builder(Type.CURRENCY).coins(amount).build();
    }

    public static QuestReward diamonds(int amount) {
        return new Builder(Type.CURRENCY).diamonds(amount).build();
    }

    public static QuestReward seedPackets(String plantId, int count) {
        return new Builder(Type.INVENTORY)
                .seedPacketPlantId(plantId)
                .seedPacketCount(count)
                .build();
    }

    public static QuestReward unlockable(String targetId) {
        return new Builder(Type.UNLOCKABLE).unlockTargetId(targetId).build();
    }

    public static QuestReward randomPlantUnlock() {
        // Signals that the reward system should pick a random available plant
        return new Builder(Type.UNLOCKABLE).unlockTargetId("RANDOM_PLANT").build();
    }

    public Type getType() {
        return type;
    }

    public int getCoins() {
        return coins;
    }

    public int getDiamonds() {
        return diamonds;
    }

    public String getUnlockTargetId() {
        return unlockTargetId;
    }

    public String getSeedPacketPlantId() {
        return seedPacketPlantId;
    }

    public int getSeedPacketCount() {
        return seedPacketCount;
    }


    public String describe() {
        return switch (type) {
            case CURRENCY -> {
                if (coins > 0 && diamonds > 0)
                    yield coins + " coins + " + diamonds + " diamonds";
                else if (coins > 0)
                    yield coins + " coins";
                else
                    yield diamonds + " diamonds";
            }
            case INVENTORY -> seedPacketCount + " seed packets for " + seedPacketPlantId;
            case UNLOCKABLE -> "RANDOM_PLANT".equals(unlockTargetId)
                    ? "A random new plant"
                    : "Unlocks: " + unlockTargetId;
        };
    }

    public static final class Builder {
        private final Type type;
        private int coins;
        private int diamonds;
        private String unlockTargetId;
        private String seedPacketPlantId;
        private int seedPacketCount;

        public Builder(Type type) {
            this.type = type;
        }

        public Builder coins(int v) {
            this.coins = v;
            return this;
        }

        public Builder diamonds(int v) {
            this.diamonds = v;
            return this;
        }

        public Builder unlockTargetId(String v) {
            this.unlockTargetId = v;
            return this;
        }

        public Builder seedPacketPlantId(String v) {
            this.seedPacketPlantId = v;
            return this;
        }

        public Builder seedPacketCount(int v) {
            this.seedPacketCount = v;
            return this;
        }

        public QuestReward build() {
            return new QuestReward(this);
        }
    }
}

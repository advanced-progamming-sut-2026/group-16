package io.github.finalwave.model.shop;

import io.github.finalwave.model.collection.PlayerPlantProgress;
import io.github.finalwave.model.greenhouse.GreenhouseLayout;
import io.github.finalwave.model.user.GreenhousePot;
import io.github.finalwave.model.user.User;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ShopManager {
    private final List<ShopItem> permanentItems = List.of(
            new ShopItem("pot", "Pot", GreenhouseLayout.POT_UNLOCK_COST_COINS, "coin(s)", 1, "unlock one greenhouse slot"),
            new ShopItem("plant_food", "Plant Food", 3, "diamond(s)", 1, "max 3 stored"),
            new ShopItem("seed_random", "Random Seed Packets", 1000, "coin(s)", 5, "random unlocked plant"),
            new ShopItem("seed_selective", "Selective Seed Packets", 5, "diamond(s)", 10, "requires -t <plant_type>"),
            new ShopItem("gem_to_coin", "Gem to Coin", 5, "diamond(s)", 500, "convert gems to coins")
    );

    private final Random random;

    public ShopManager() {
        this(new Random());
    }

    public ShopManager(Random random) {
        this.random = random;
    }

    private static final int[] COIN_PACK_COUNTS = {1, 2, 5, 10, 20, 40, 80, 100};
    private static final String[] COIN_PACK_TITLES = {
            "A handful of coins!",
            "Coins, coins, coins!",
            "More coins, more fun!",
            "So many coins!",
            "A bag of coins!",
            "A bucket of coins!",
            "A barrel of coins!",
            "So very many coins!"
    };

    public List<ShopOffer> offers(User user) {
        refreshDailyOfferIfNeeded(user);
        List<ShopOffer> list = new ArrayList<>();
        ShopOffer daily = dailyOffer(user);
        if (daily != null) {
            list.add(daily);
        }
        for (ShopItem item : permanentItems) {
            list.add(toOffer(user, item));
        }
        return list;
    }

    public List<ShopOffer> offers(User user, ShopTab tab) {
        if (tab == ShopTab.COINS) {
            return coinPackOffers();
        }
        return offers(user).stream().filter(offer -> offer.tab() == tab).toList();
    }

    public String formatPermanentItems() {
        StringBuilder builder = new StringBuilder("Shop items:\n");
        for (ShopItem item : permanentItems) {
            builder.append("- ").append(item.formatLine()).append('\n');
        }
        return builder.toString().trim();
    }

    public String formatDailyOffer(User user) {
        refreshDailyOfferIfNeeded(user);
        if (user.getDailyOfferPlant() == null) {
            return "No daily offer available.";
        }
        return "daily - " + user.getDailyOfferPlant()
                + " - 1600 coin(s) - 10 seed packets"
                + (user.isDailyOfferPurchased() ? " - already purchased" : "");
    }

    public PurchaseResult purchase(User user, String itemId, int count, String plantType) {
        if (count <= 0) {
            return PurchaseResult.invalidCount();
        }
        refreshDailyOfferIfNeeded(user);
        return switch (itemId) {
            case "pot" -> buyPot(user, count);
            case "plant_food" -> buyPlantFood(user, count);
            case "seed_random" -> buyRandomSeed(user, count);
            case "seed_selective" -> buySelectiveSeed(user, count, plantType);
            case "gem_to_coin" -> buyGemToCoin(user, count);
            case "daily" -> buyDailyOffer(user, count);
            default -> PurchaseResult.itemNotFound(itemId);
        };
    }

    public void refreshDailyOfferIfNeeded(User user) {
        LocalDate today = LocalDate.now();
        if (today.equals(user.getDailyOfferDate())) {
            return;
        }
        List<String> unlockedPlants = user.getPlantProgress().getUnlockedPlantNames();
        user.setDailyOfferDate(today);
        user.setDailyOfferPurchased(false);
        user.setDailyOfferPlant(unlockedPlants.isEmpty()
                ? null
                : unlockedPlants.get(random.nextInt(unlockedPlants.size())));
    }

    private PurchaseResult buyPot(User user, int count) {
        int remainingLocked = GreenhouseLayout.SLOT_COUNT - user.countUnlockedPots();
        if (remainingLocked <= 0) {
            return PurchaseResult.maxCapacity("Pot");
        }
        if (count > remainingLocked) {
            return PurchaseResult.maxCapacity("Pot");
        }
        if (!user.spendCoins(count * GreenhouseLayout.POT_UNLOCK_COST_COINS)) {
            return PurchaseResult.insufficientCoins();
        }
        for (int i = 0; i < count; i++) {
            GreenhousePot pot = user.findNextLockedPot();
            if (pot != null) {
                pot.setLocked(false);
            }
        }
        return PurchaseResult.success("Pot", count, "unlocked " + count + " greenhouse slot(s)");
    }

    private PurchaseResult buyPlantFood(User user, int count) {
        if (user.getPlantFood() + count > 3) {
            return PurchaseResult.maxCapacity("Plant Food");
        }
        if (!user.spendDiamonds(count * 3)) {
            return PurchaseResult.insufficientDiamonds();
        }
        user.setPlantFood(user.getPlantFood() + count);
        return PurchaseResult.success("Plant Food", count, "stored: " + user.getPlantFood());
    }

    private PurchaseResult buyRandomSeed(User user, int count) {
        List<String> unlockedPlants = user.getPlantProgress().getUnlockedPlantNames();
        if (unlockedPlants.isEmpty()) {
            return PurchaseResult.itemNotFound("seed_random");
        }
        if (!user.spendCoins(count * 1000)) {
            return PurchaseResult.insufficientCoins();
        }
        List<String> rewards = new ArrayList<>();
        PlayerPlantProgress progress = user.getPlantProgress();
        for (int i = 0; i < count; i++) {
            String plantName = unlockedPlants.get(random.nextInt(unlockedPlants.size()));
            progress.addSeedPackets(plantName, 5);
            rewards.add(plantName + " +5");
        }
        return PurchaseResult.success("Random Seed Packets", count, String.join(", ", rewards));
    }

    private PurchaseResult buySelectiveSeed(User user, int count, String plantType) {
        if (plantType == null || plantType.isBlank()) {
            return PurchaseResult.plantTypeRequired();
        }
        if (!user.getPlantProgress().isOwned(plantType)) {
            return PurchaseResult.plantNotUnlocked(plantType);
        }
        if (!user.spendDiamonds(count * 5)) {
            return PurchaseResult.insufficientDiamonds();
        }
        user.getPlantProgress().addSeedPackets(plantType, count * 10);
        return PurchaseResult.success("Selective Seed Packets", count, plantType + " +" + (count * 10));
    }

    private PurchaseResult buyGemToCoin(User user, int count) {
        if (!user.spendDiamonds(count * 5)) {
            return PurchaseResult.insufficientDiamonds();
        }
        user.addCoins(count * 500);
        return PurchaseResult.success("Gem to Coin", count, "+" + (count * 500) + " coin(s)");
    }

    private PurchaseResult buyDailyOffer(User user, int count) {
        if (count != 1) {
            return PurchaseResult.invalidCount();
        }
        if (user.getDailyOfferPlant() == null) {
            return PurchaseResult.itemNotFound("daily");
        }
        if (user.isDailyOfferPurchased()) {
            return PurchaseResult.dailyAlreadyPurchased();
        }
        if (!user.spendCoins(1600)) {
            return PurchaseResult.insufficientCoins();
        }
        user.getPlantProgress().addSeedPackets(user.getDailyOfferPlant(), 10);
        user.setDailyOfferPurchased(true);
        return PurchaseResult.success("Daily Offer", 1, user.getDailyOfferPlant() + " +10");
    }

    private ShopOffer dailyOffer(User user) {
        if (user.getDailyOfferPlant() == null) {
            return null;
        }
        return new ShopOffer(
                "daily",
                "Daily Offer",
                "10 seed packets for " + user.getDailyOfferPlant(),
                1600,
                "coin(s)",
                true,
                user.isDailyOfferPurchased(),
                remainingUntilMidnight(),
                user.getDailyOfferPlant(),
                packetImageId(user.getDailyOfferPlant()),
                false,
                1,
                quantityLabel(10),
                ShopTab.SEEDS);
    }

    private ShopOffer toOffer(User user, ShopItem item) {
        boolean soldOut = switch (item.getId()) {
            case "pot" -> GreenhouseLayout.SLOT_COUNT - user.countUnlockedPots() <= 0;
            case "plant_food" -> user.getPlantFood() >= 3;
            default -> false;
        };
        String previewImage = switch (item.getId()) {
            case "pot" -> "IMAGE_UI_SPROUTS_STACK_1";
            case "plant_food" -> "IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON";
            case "gem_to_coin" -> "IMAGE_UI_COINS_STACK_2";
            case "seed_random" -> "IMAGE_UI_STOREMULTI_SEEDPACKETICON";
            case "seed_selective" -> packetImageId("Sunflower");
            default -> "IMAGE_UI_SPROUTS_STACK_1";
        };
        ShopTab tab = switch (item.getId()) {
            case "seed_random", "seed_selective" -> ShopTab.SEEDS;
            case "gem_to_coin" -> ShopTab.COINS;
            default -> ShopTab.GARDEN;
        };
        String quantity = quantityLabel(item.getPacketAmount());
        return new ShopOffer(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCurrency(),
                false,
                soldOut,
                null,
                null,
                previewImage,
                "seed_selective".equals(item.getId()),
                1,
                quantity,
                tab);
    }

    private List<ShopOffer> coinPackOffers() {
        ShopItem item = permanentItems.stream()
                .filter(candidate -> "gem_to_coin".equals(candidate.getId()))
                .findFirst()
                .orElseThrow();
        List<ShopOffer> packs = new ArrayList<>();
        for (int index = 0; index < COIN_PACK_COUNTS.length; index++) {
            int count = COIN_PACK_COUNTS[index];
            int coins = item.getPacketAmount() * count;
            packs.add(new ShopOffer(
                    item.getId(),
                    COIN_PACK_TITLES[index],
                    "+" + coins + " coins",
                    item.getPrice() * count,
                    item.getCurrency(),
                    false,
                    false,
                    null,
                    null,
                    "IMAGE_UI_COINS_STACK_" + index,
                    false,
                    count,
                    quantityLabel(coins),
                    ShopTab.COINS));
        }
        return packs;
    }

    private static String quantityLabel(int amount) {
        return "x" + String.format(Locale.US, "%,d", amount);
    }

    private static String packetImageId(String plantName) {
        if (plantName == null || plantName.isBlank()) {
            return "IMAGE_UI_STOREMULTI_SEEDPACKETICON";
        }
        return "IMAGE_UI_PACKETS_" + plantName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private static String remainingUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        Duration remaining = Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay());
        long hours = Math.max(0L, remaining.toHours());
        long minutes = Math.max(0L, remaining.toMinutes() % 60);
        return hours + "h " + minutes + "m remaining";
    }

    public record PurchaseResult(String status, String itemName, int count, String extraInfo, String errorArg) {
        public static PurchaseResult success(String itemName, int count, String extraInfo) {
            return new PurchaseResult("success", itemName, count, extraInfo, null);
        }

        public static PurchaseResult invalidCount() {
            return new PurchaseResult("invalid_count", null, 0, null, null);
        }

        public static PurchaseResult itemNotFound(String itemId) {
            return new PurchaseResult("item_not_found", null, 0, null, itemId);
        }

        public static PurchaseResult insufficientCoins() {
            return new PurchaseResult("insufficient_coins", null, 0, null, null);
        }

        public static PurchaseResult insufficientDiamonds() {
            return new PurchaseResult("insufficient_diamonds", null, 0, null, null);
        }

        public static PurchaseResult dailyAlreadyPurchased() {
            return new PurchaseResult("daily_purchased", null, 0, null, null);
        }

        public static PurchaseResult maxCapacity(String itemName) {
            return new PurchaseResult("max_capacity", itemName, 0, null, null);
        }

        public static PurchaseResult plantTypeRequired() {
            return new PurchaseResult("plant_type_required", null, 0, null, null);
        }

        public static PurchaseResult plantNotUnlocked(String plantType) {
            return new PurchaseResult("plant_not_unlocked", null, 0, null, plantType);
        }
    }
}
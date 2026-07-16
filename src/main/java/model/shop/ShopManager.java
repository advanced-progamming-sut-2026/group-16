package model.shop;

import model.collection.PlayerPlantProgress;
import model.user.GreenhousePot;
import model.user.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShopManager {
    private final List<ShopItem> permanentItems = List.of(
            new ShopItem("pot", "Pot", 2000, "coin(s)", 1, "unlock one greenhouse slot"),
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
        int remainingLocked = 20 - user.countUnlockedPots();
        if (remainingLocked <= 0) {
            return PurchaseResult.maxCapacity("Pot");
        }
        if (count > remainingLocked) {
            return PurchaseResult.maxCapacity("Pot");
        }
        if (!user.spendCoins(count * 2000)) {
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
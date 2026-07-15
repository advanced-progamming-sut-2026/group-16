package model;

import model.definition.PlantRegistry;
import model.user.User;

import java.io.IOException;
import java.io.InputStream;

public final class App {

    private static App app;

    private User currentUser;
    private PlantRegistry plantRegistry;

    private App() {
        plantRegistry = new PlantRegistry();
        loadPlantRegistry();
    }

    public static App getInstance() {
        if (app == null) {
            app = new App();
        }
        return app;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public PlantRegistry getPlantRegistry() {
        return plantRegistry;
    }

    private void loadPlantRegistry() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("plants.json")) {
            if (input == null) {
                throw new IllegalStateException("plants.json is missing from application resources");
            }
            plantRegistry.loadFromJson(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load plants.json", e);
        }
    }
}

package io.github.finalwave.controller;

import io.github.finalwave.view.api.View;

public abstract class ViewController {
    protected View view;
    protected Navigator navigator;

    public final View getView() {
        return view;
    }

    public final void setView(View view) {
        this.view = view;
    }

    public final void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public void handleCommand(String command) {
    }

    public void displayMenu() {
    }

    public void onEnter() {
        displayMenu();
    }

    public void onResume() {
        displayMenu();
    }

    public void onPause() {
    }

    public void onExit() {
    }
}

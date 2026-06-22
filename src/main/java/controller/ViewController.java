package controller;

import view.api.View;

public abstract class ViewController {
    protected View view;
    protected CommandParser parser;

    public final View getView() {
        return view;
    }

    public final void setView(View view) {
        this.view = view;
    }

    public final void setParser(CommandParser parser) {
        this.parser = parser;
    }

    public void handleCommand(String command) {
    }

    public void displayMenu() {
    }
}

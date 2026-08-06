package io.github.finalwave.view.cli;

import io.github.finalwave.view.api.DeadLineView;

public class DeadLineLevelViewCli extends SpecialLevelViewCli implements DeadLineView {

    @Override
    public void showDeadLineRule(int column) {
        displayMessage("Dead line active at column " + column
                + ". If any zombie crosses it, you lose immediately.");
    }

    @Override
    public void showDeadLineBreached(int column, String zombieType) {
        displayError("A zombie crossed the dead line (column " + column + "). You lose!");
    }
}

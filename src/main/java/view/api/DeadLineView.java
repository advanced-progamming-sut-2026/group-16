package view.api;

public interface DeadLineView extends SpecialLevelView {

    void showDeadLineRule(int column);

    void showDeadLineBreached(int column, String zombieType);
}

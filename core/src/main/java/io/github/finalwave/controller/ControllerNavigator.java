package io.github.finalwave.controller;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class ControllerNavigator implements Navigator {
    private final Deque<ViewController> stack = new ArrayDeque<>();
    private final NavigationBinder binder;

    public ControllerNavigator(NavigationBinder binder) {
        this.binder = Objects.requireNonNull(binder, "binder");
    }

    @Override
    public void push(ViewController controller) {
        Objects.requireNonNull(controller, "controller");
        ViewController previous = stack.peek();
        if (previous != null) {
            previous.onPause();
        }
        attach(controller);
        stack.push(controller);
        activate(controller);
        controller.onEnter();
    }

    @Override
    public void pop() {
        if (stack.size() <= 1) {
            return;
        }
        ViewController leaving = stack.pop();
        leaving.onExit();
        ViewController resumed = stack.peek();
        if (resumed != null) {
            activate(resumed);
            resumed.onResume();
        }
    }

    @Override
    public void replace(ViewController controller) {
        Objects.requireNonNull(controller, "controller");
        if (stack.isEmpty()) {
            reset(controller);
            return;
        }
        ViewController leaving = stack.pop();
        leaving.onExit();
        attach(controller);
        stack.push(controller);
        activate(controller);
        controller.onEnter();
    }

    @Override
    public void reset(ViewController root) {
        Objects.requireNonNull(root, "root");
        while (!stack.isEmpty()) {
            stack.pop().onExit();
        }
        attach(root);
        stack.push(root);
        activate(root);
        root.onEnter();
    }

    @Override
    public ViewController current() {
        return stack.peek();
    }

    @Override
    public int size() {
        return stack.size();
    }

    private void attach(ViewController controller) {
        controller.setNavigator(this);
    }

    private void activate(ViewController controller) {
        binder.bind(controller);
    }
}

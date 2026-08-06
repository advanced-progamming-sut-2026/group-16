package io.github.finalwave.controller;

public interface Navigator {
    void push(ViewController controller);

    void pop();

    void replace(ViewController controller);

    void reset(ViewController root);

    ViewController current();

    int size();
}

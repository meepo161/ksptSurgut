package ru.avem.ksptamur.states.main;

public interface State {
    void toIdleState();

    void toWaitState();

    void toResultState();
}

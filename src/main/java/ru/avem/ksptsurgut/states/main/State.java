package ru.avem.ksptsurgut.states.main;

public interface State {
    void toIdleState();

    void toWaitState();

    void toResultState();
}

package ru.avem.ksptsurgut.states.main;


public interface Statable {
    void toIdleState();

    void toWaitState();

    void toResultState();
}

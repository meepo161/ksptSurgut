package ru.avem.ksptamur.states.main;


public interface Statable {
    void toIdleState();

    void toWaitState();

    void toResultState();
}

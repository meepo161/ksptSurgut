package ru.avem.ksptsurgut.states.experiment;

public interface Statable {
    void toInitState();

    void toRunState();

    void toStoppingState();

    void toErrorState();

    void toFinishedState();
}

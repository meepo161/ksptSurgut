package ru.avem.ksptsurgut.states.experiment;

public interface State {
    void toInitState();

    void toRunState();

    void toStoppingState();

    void toErrorState();

    void toFinishedState();
}

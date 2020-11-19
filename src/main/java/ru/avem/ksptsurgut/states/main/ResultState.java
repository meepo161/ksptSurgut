package ru.avem.ksptsurgut.states.main;

public class ResultState implements State {
    private Statable statable;

    public ResultState(Statable statable) {
        this.statable = statable;
    }

    @Override
    public void toIdleState() {
        statable.toIdleState();
    }

    @Override
    public void toWaitState() {
        statable.toWaitState();
    }

    @Override
    public void toResultState() {
    }
}

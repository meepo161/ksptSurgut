package ru.avem.ksptamur.states.main;

public class IdleState implements State {
    private Statable statable;

    public IdleState(Statable statable) {
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

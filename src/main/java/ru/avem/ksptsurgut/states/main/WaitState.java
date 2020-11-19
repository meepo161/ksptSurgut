package ru.avem.ksptsurgut.states.main;

import ru.avem.ksptsurgut.utils.View;

public class WaitState implements State {
    private Statable statable;

    public WaitState(Statable statable) {
        this.statable = statable;
    }

    @Override
    public void toIdleState() {
        View.showConfirmDialog("Подтвердите отмену", "Внимание! Все несохранённые результаты будут утеряны", () -> {
            statable.toIdleState();
        }, () -> {

        });
    }

    @Override
    public void toWaitState() {
        statable.toWaitState();
    }

    @Override
    public void toResultState() {
        statable.toResultState();
    }
}

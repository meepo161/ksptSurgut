package ru.avem.ksptamur.communication.devices.cs02021;

import java.util.Observable;
import java.util.Observer;

public class CS020201Model extends Observable {
    public static final int RESPONDING_PARAM = 0;
    private int deviceID;
    private boolean readResponding = true;
    private boolean writeResponding = true;

    CS020201Model(Observer observer, int deviceID) {
        addObserver(observer);
        this.deviceID = deviceID;
    }

    void resetResponding() {
        readResponding = true;
        writeResponding = true;
    }

    void setReadResponding(boolean readResponding) {
        this.readResponding = readResponding;
        setResponding();
    }

    void setWriteResponding(boolean writeResponding) {
        this.writeResponding = writeResponding;
        setResponding();
    }

    private void setResponding() {
        notice(RESPONDING_PARAM, readResponding && writeResponding);
    }

    void setResponding(boolean responding) {
        notice(RESPONDING_PARAM, responding);
    }

    private void notice(int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }
}

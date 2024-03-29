package ru.avem.ksptsurgut.communication.devices.avem_voltmeter;

import java.util.Observable;
import java.util.Observer;

public class AvemVoltmeterModel extends Observable {
    public static final int RESPONDING_PARAM = 0;
    public static final int U_PARAM = 1;
    private int deviceID;
    private boolean readResponding;
    private boolean writeResponding;

    AvemVoltmeterModel(Observer observer, int deviceID) {
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

    public void setU(float u) {
        notice(U_PARAM, u);
    }

    private void notice(int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }
}
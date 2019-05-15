package ru.avem.ksptamur.communication.devices.pr200;

import java.util.Observable;
import java.util.Observer;

public class OwenPRModel extends Observable {
    public static final int RESPONDING_PARAM = 0;
    public static final int DI1_CURRENT_1 = 1;
    public static final int DI2_CURRENT_DELTA = 2;
    public static final int DI3_DOOR_BLOCK = 3;
    public static final int DI4_INSULATION = 4;
    public static final int DI5_START_BTN = 5;
    public static final int DI6_STOP_BTN = 6;
    public static final int DI7_DOOR_ZONE = 7;

    public static final int DI1_CURRENT_1_FIXED = 8;
    public static final int DI2_CURRENT_DELTA_FIXED = 9;
    public static final int DI3_DOOR_BLOCK_FIXED = 10;
    public static final int DI4_INSULATION_FIXED = 11;
    public static final int DI5_START_BTN_FIXED = 12;
    public static final int DI6_STOP_BTN_FIXED = 13;
    public static final int DI7_DOOR_ZONE_FIXED = 14;

    private int deviceID;
    private boolean readResponding;
    private boolean writeResponding;

    OwenPRModel(Observer observer, int deviceID) {
        addObserver(observer);
        this.deviceID = deviceID;
    }

    void setReadResponding(boolean readResponding) {
        this.readResponding = readResponding;
        setResponding();
    }

    void setWriteResponding(boolean writeResponding) {
        this.writeResponding = writeResponding;
        setResponding();
    }

    void setResponding() {
        notice(RESPONDING_PARAM, readResponding && writeResponding);
    }

    void setInstantInputStatus(short instantInputStatusInputStatus) {
        notice(DI1_CURRENT_1, (instantInputStatusInputStatus & 0b1) > 0);
        notice(DI2_CURRENT_DELTA, (instantInputStatusInputStatus & 0b10) > 0);
        notice(DI3_DOOR_BLOCK, (instantInputStatusInputStatus & 0b100) > 0);
        notice(DI4_INSULATION, (instantInputStatusInputStatus & 0b1000) > 0);
        notice(DI5_START_BTN, (instantInputStatusInputStatus & 0b10000) > 0);
        notice(DI6_STOP_BTN, (instantInputStatusInputStatus & 0b100000) > 0);
        notice(DI7_DOOR_ZONE, (instantInputStatusInputStatus & 0b1000000) > 0);
    }

    void setFixedInputStatus(short fixedInputStatus) {
        notice(DI1_CURRENT_1_FIXED, (fixedInputStatus & 0b1) > 0);
        notice(DI2_CURRENT_DELTA_FIXED, (fixedInputStatus & 0b10) > 0);
        notice(DI3_DOOR_BLOCK_FIXED, (fixedInputStatus & 0b100) > 0);
        notice(DI4_INSULATION_FIXED, (fixedInputStatus & 0b1000) > 0);
        notice(DI5_START_BTN_FIXED, (fixedInputStatus & 0b10000) > 0);
        notice(DI6_STOP_BTN_FIXED, (fixedInputStatus & 0b100000) > 0);
        notice(DI7_DOOR_ZONE_FIXED, (fixedInputStatus & 0b1000000) > 0);
    }

    private void notice(int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }
}
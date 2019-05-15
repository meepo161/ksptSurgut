package ru.avem.ksptamur.communication.devices.pr200;

import java.util.Observable;
import java.util.Observer;

public class OwenPRModel extends Observable {
    public static final int RESPONDING_PARAM = 0;
    public static final int PRDI1 = 1;
    public static final int PRDI2 = 2;
    public static final int PRDI3 = 3;
    public static final int PRDI4 = 4;
    public static final int PRDI5 = 5;
    public static final int PRDI6 = 6;
    public static final int PRDI7 = 7;
    public static final int PRDI8 = 8;

    public static final int PRMDI1 = 9;
    public static final int PRMDI2 = 10;
    public static final int PRMDI3 = 11;
    public static final int PRMDI4 = 12;
    public static final int PRMDI5 = 13;
    public static final int PRMDI6 = 14;
    public static final int PRMDI7 = 15;
    public static final int PRMDI8 = 16;

    public static final int PRDI1_FIXED = 17;
    public static final int PRDI2_FIXED = 18;
    public static final int PRDI3_FIXED = 19;
    public static final int PRDI4_FIXED = 20;
    public static final int PRDI5_FIXED = 21;
    public static final int PRDI6_FIXED = 22;
    public static final int PRDI7_FIXED = 23;

    public static final int PRMDI1_FIXED = 24;
    public static final int PRMDI2_FIXED = 25;
    public static final int PRMDI3_FIXED = 26;
    public static final int PRMDI4_FIXED = 27;
    public static final int PRMDI5_FIXED = 28;
    public static final int PRMDI6_FIXED = 29;
    public static final int PRMDI7_FIXED = 30;

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
        notice(PRDI1, (instantInputStatusInputStatus & 0b1) > 0);
        notice(PRDI2, (instantInputStatusInputStatus & 0b10) > 0);
        notice(PRDI3, (instantInputStatusInputStatus & 0b100) > 0);
        notice(PRDI4, (instantInputStatusInputStatus & 0b1000) > 0);
        notice(PRDI5, (instantInputStatusInputStatus & 0b10000) > 0);
        notice(PRDI6, (instantInputStatusInputStatus & 0b100000) > 0);
        notice(PRDI7, (instantInputStatusInputStatus & 0b1000000) > 0);
    }

    void setFixedInputStatus(short fixedInputStatus) {
        notice(PRDI1_FIXED, (fixedInputStatus & 0b1) > 0);
        notice(PRDI2_FIXED, (fixedInputStatus & 0b10) > 0);
        notice(PRDI3_FIXED, (fixedInputStatus & 0b100) > 0);
        notice(PRDI4_FIXED, (fixedInputStatus & 0b1000) > 0);
        notice(PRDI5_FIXED, (fixedInputStatus & 0b10000) > 0);
        notice(PRDI6_FIXED, (fixedInputStatus & 0b100000) > 0);
        notice(PRDI7_FIXED, (fixedInputStatus & 0b1000000) > 0);
    }

    private void notice(int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }
}
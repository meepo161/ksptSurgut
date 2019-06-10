//package ru.avem.ksptamur.communication.devices.fr_a800;
//
//import java.util.Observable;
//import java.util.Observer;
//
//public class FRA800Model extends Observable {
//    public static final int RESPONDING_PARAM = 0;
//    public static final int READY_PARAM = 1;
//
//    private int deviceID;
//    private boolean readResponding;
//    private boolean writeResponding;
//
//    FRA800Model(Observer observer, int deviceID) {
//        addObserver(observer);
//        this.deviceID = deviceID;
//    }
//
//
//    void setReadResponding(boolean readResponding) {
//        this.readResponding = readResponding;
//        setResponding();
//    }
//
//    void setWriteResponding(boolean writeResponding) {
//        this.writeResponding = writeResponding;
//        setResponding();
//    }
//
//    private void setResponding() {
//        notice(RESPONDING_PARAM, readResponding && writeResponding);
//    }
//
//    void setResponding(boolean responding) {
//        notice(RESPONDING_PARAM, responding);
//    }
//
//    void setControlState(short controlState) {
////        notice(KM1_PARAM, (sta & 0b1) == 0);
////        notice(DOOR_S_PARAM, (sta & 0b10) == 0);
////        notice(PARAM, (sta & 0b100) > 0);
//        notice(READY_PARAM, (controlState & 0b1000) > 0);
////        notice(OBJ_1_PARAM, (sta & 0b10000) == 0);
////        notice(OBJ_2_PARAM, (sta & 0b100000) == 0);
////        notice(OBJ_3_PARAM, (sta & 0b1000000) == 0);
////        notice(OBJ_4_PARAM, (sta & 0b10000000) == 0);
//    }
//
//    private void notice(int param, Object value) {
//        setChanged();
//        notifyObservers(new Object[]{deviceID, param, value});
//    }
//}
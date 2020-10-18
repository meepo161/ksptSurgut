package ru.avem.ksptsurgut.communication.devices;

public interface DeviceController {
    int INPUT_BUFFER_SIZE = 256;
    byte NUMBER_OF_READ_ATTEMPTS = 10;
    byte NUMBER_OF_WRITE_ATTEMPTS = 10;

    byte NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS = 10;
    byte NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS = 10;

    int PM130_ID = 1;
    int PM130_2_ID = 2;
    int PHASEMETER_ID = 4;
    int IKAS_ID = 5;
    int PR200_ID = 6;
    int TRM_ID = 7;
    int MEGACS_ID = 8;
    int DELTACP2000_ID = 11;
    int PARMA400_ID = 12;
    int AVEM_A_ID = 13;
    int AVEM_B_ID = 14;
    int AVEM_C_ID = 15;
//    int FR_A800_OBJECT_ID = 11;

    void read(Object... args);

    boolean thereAreReadAttempts();

    void write(Object... args);

    boolean thereAreWriteAttempts();

    boolean isNeedToRead();

    void setNeedToRead(boolean isNeedToRead);

    void resetAllAttempts();

    void resetAllDeviceStateOnAttempts();
}
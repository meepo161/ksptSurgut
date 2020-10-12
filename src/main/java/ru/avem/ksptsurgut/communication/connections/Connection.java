package ru.avem.ksptsurgut.communication.connections;

public interface Connection {

    boolean initConnection();

    void closeConnection();

    boolean isInitiatedConnection();

    int write(byte[] outputArray);

    int read(byte[] inputArray);

    void setPortParameters(int baudRate, int dataBits, int stopBits, int parity);

    void setConnectionBaudrate(int baudrate);
}

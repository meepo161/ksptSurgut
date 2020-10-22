package ru.avem.ksptsurgut.communication.connections;

public interface Connection {
    String getName();

    boolean isInitiated();

    void setBaudrate(int baudrate);

    void setParameters(int baudRate, int dataBits, int stopBits, int parity);

    int write(byte[] src);

    int read(byte[] dst);

    void close();
}

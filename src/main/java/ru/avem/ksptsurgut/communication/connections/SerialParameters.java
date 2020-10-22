package ru.avem.ksptsurgut.communication.connections;

public class SerialParameters {
    public final int baudRate;
    public final int dataBits;
    public final int stopBits;
    public final int parity;
    public final int writeTimeout;
    public final int readTimeout;

    public SerialParameters(int baudRate, int dataBits, int stopBits, int parity, int writeTimeout, int readTimeout) {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        this.writeTimeout = writeTimeout;
        this.readTimeout = readTimeout;
    }
}

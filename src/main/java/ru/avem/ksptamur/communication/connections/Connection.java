package ru.avem.ksptamur.communication.connections;

public interface Connection {
    boolean initConnection();

    void closeConnection();

    boolean isInitiatedConnection();

    int write(byte[] outputArray);

    int read(byte[] inputArray);
}

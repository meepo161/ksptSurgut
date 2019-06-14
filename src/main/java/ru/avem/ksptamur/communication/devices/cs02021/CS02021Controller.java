package ru.avem.ksptamur.communication.devices.cs02021;

import ru.avem.ksptamur.communication.connections.Connection;
import ru.avem.ksptamur.communication.devices.DeviceController;
import ru.avem.ksptamur.communication.modbus.utils.CRC16;
import ru.avem.ksptamur.utils.Logger;

import java.nio.ByteBuffer;
import java.util.Observer;

public class CS02021Controller implements DeviceController {
    private byte address;
    private final Connection mConnection;
    private boolean isExperimentRun;

    private CS020201Model model;
    private boolean needToReed;

    public CS02021Controller(int address, Observer observer, Connection connection, int megacsId) {
        this.address = (byte) address;
        mConnection = connection;
        model = new CS020201Model(observer, megacsId);
    }

    public synchronized boolean setVoltage(int u) {
        byte byteU = (byte) (u / 10);
        ByteBuffer outputBuffer = ByteBuffer.allocate(5)
                .put((byte) 0x08)
                .put((byte) 0x01)
                .put(byteU);
        CRC16.signReversWithSlice(outputBuffer);
        mConnection.write(outputBuffer.array());
        byte[] inputArray = new byte[40];
        ByteBuffer inputBuffer = ByteBuffer.allocate(40);
        int attempt = 0;
        int frameSize = 0;
        do {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frameSize = mConnection.read(inputArray);
            inputBuffer.put(inputArray, 0, frameSize);
        } while (inputBuffer.position() < 5 && (++attempt < 10));
        System.out.println("666666   = " + frameSize);
        return frameSize > 0;
    }

    public synchronized float[] readData() {
        float[] data = new float[4];
        ByteBuffer outputBuffer = ByteBuffer.allocate(5)
                .put((byte) 0x08)
                .put((byte) 0x07)
                .put((byte) 0x71)
                .put((byte) 0x64)
                .put((byte) 0x7F);

        ByteBuffer inputBuffer = ByteBuffer.allocate(40);
        ByteBuffer finalBuffer = ByteBuffer.allocate(40);

        while (isExperimentRun) {
            Logger.withTag("StatusActivity").log("isExperimentRun=" + isExperimentRun);
            inputBuffer.clear();
            mConnection.write(outputBuffer.array());
            byte[] inputArray = new byte[40];
            int attempt = 0;
            do {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int frameSize = mConnection.read(inputArray);
                inputBuffer.put(inputArray, 0, frameSize);
            } while (inputBuffer.position() < 16 && (++attempt < 15));
            if (attempt < 15) {
                break;
            }
        }

        if (inputBuffer.position() == 16) {
            inputBuffer.flip().position(2);
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put((byte) 0);
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put((byte) 0);
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put((byte) 0);
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put(inputBuffer.get());
            finalBuffer.put((byte) 0);
            finalBuffer.flip();
            data[0] = finalBuffer.getFloat();
            data[1] = finalBuffer.getFloat();
            data[2] = finalBuffer.getFloat();
            data[3] = finalBuffer.getFloat();
        }
        return data;
    }

    public synchronized boolean isResponding() {
        ByteBuffer outputBuffer = ByteBuffer.allocate(5)
                .put((byte) 0x08)
                .put((byte) 0x07)
                .put((byte) 0x71)
                .put((byte) 0x64)
                .put((byte) 0x7F);

        ByteBuffer inputBuffer = ByteBuffer.allocate(40);

        inputBuffer.clear();
        int writtenBytes = mConnection.write(outputBuffer.array());
        Logger.withTag("TAG").log("writtenBytes=" + writtenBytes);
        byte[] inputArray = new byte[40];
        int attempt = 0;
        do {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int frameSize = mConnection.read(inputArray);
            inputBuffer.put(inputArray, 0, frameSize);
        } while (inputBuffer.position() < 16 && (++attempt < 15));
        return inputBuffer.position() >= 16;
    }

    public void setExperimentRun(boolean experimentRun) {
        isExperimentRun = experimentRun;
        Logger.withTag("isExperimentRun").log(experimentRun);
    }

    @Override
    public void read(Object... args) {
        model.setResponding(isResponding());
    }

    @Override
    public void write(Object... args) {

    }

    @Override
    public boolean needToRead() {
        return needToReed;
    }

    @Override
    public void setNeedToRead(boolean needToRead) {
        needToReed = needToRead;
    }


    // TODO: 14.05.2019
    @Override
    public boolean thereAreReadAttempts() {
        return false;
    }

    @Override
    public boolean thereAreWriteAttempts() {
        return false;
    }

    @Override
    public void resetAllAttempts() {

    }

    @Override
    public void resetAllDeviceStateOnAttempts() {

    }
}

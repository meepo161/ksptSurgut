package ru.avem.ksptsurgut.communication.devices.cs02021;


import ru.avem.ksptsurgut.Constants;
import ru.avem.ksptsurgut.communication.connections.Connection;
import ru.avem.ksptsurgut.communication.devices.DeviceController;
import ru.avem.ksptsurgut.communication.modbus.utils.CRC16;
import ru.avem.ksptsurgut.utils.Logger;

import java.nio.ByteBuffer;
import java.util.Observer;

import static ru.avem.ksptsurgut.communication.CommunicationModel.LOCK;
import static ru.avem.ksptsurgut.utils.Utils.sleep;


public class CS02021Controller implements DeviceController {
    private byte address;
    private final Connection mConnection;
    private boolean isExperimentRun;

    private CS020201Model model;
    private boolean isNeedToRead;

    public CS02021Controller(int deviceID, Observer observer, Connection connection) {
        address = (byte) deviceID;
        mConnection = connection;
        model = new CS020201Model(observer, deviceID);
    }

    public boolean setVoltage(int u) {
        synchronized (LOCK) {
            mConnection.setConnectionBaudrate(Constants.Communication.BAUDRATE_MEGACS);
            byte byteU = (byte) (u / 10);
            ByteBuffer outputBuffer = ByteBuffer.allocate(5)
                    .put(address)
                    .put((byte) 0x01)
                    .put(byteU);
            CRC16.signReversWithSlice(outputBuffer);
            mConnection.write(outputBuffer.array());
            byte[] inputArray = new byte[40];
            ByteBuffer inputBuffer = ByteBuffer.allocate(40);
            int attempt = 0;
            int frameSize = 0;
            do {
                sleep(2);
                frameSize = mConnection.read(inputArray);
                inputBuffer.put(inputArray, 0, frameSize);
            } while (inputBuffer.position() < 5 && (++attempt < 10));
            mConnection.setConnectionBaudrate(Constants.Communication.BAUDRATE_MAIN);
            return frameSize > 0;
        }
    }

    public float[] readData() {
        synchronized (LOCK) {
            mConnection.setConnectionBaudrate(Constants.Communication.BAUDRATE_MEGACS);
            float[] data = new float[4];
            ByteBuffer outputBuffer = ByteBuffer.allocate(5)
                    .put(address)
                    .put((byte) 0x07)
                    .put((byte) 0x71)
                    .put((byte) 0x64)
                    .put((byte) 0x7F);

            ByteBuffer inputBuffer = ByteBuffer.allocate(40);
            ByteBuffer finalBuffer = ByteBuffer.allocate(40);

            while (isExperimentRun) {
                inputBuffer.clear();
                mConnection.write(outputBuffer.array());
                byte[] inputArray = new byte[40];
                int attempt = 0;
                do {
                    sleep(2);
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
            mConnection.setConnectionBaudrate(Constants.Communication.BAUDRATE_MAIN);
            return data;
        }
    }

    private boolean isResponding() {
        synchronized (LOCK) {
            mConnection.setConnectionBaudrate(Constants.Communication.BAUDRATE_MEGACS);
            ByteBuffer outputBuffer = ByteBuffer.allocate(5)
                    .put(address)
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
                sleep(2);
                int frameSize = mConnection.read(inputArray);
                inputBuffer.put(inputArray, 0, frameSize);
            } while (inputBuffer.position() < 16 && (++attempt < 15));
            mConnection.setConnectionBaudrate(Constants.Communication.BAUDRATE_MAIN);
            return inputBuffer.position() >= 16;
        }
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
    public boolean isNeedToRead() {
        return isNeedToRead;
    }

    @Override
    public void setNeedToRead(boolean isNeedToRead) {
        this.isNeedToRead = isNeedToRead;
    }

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

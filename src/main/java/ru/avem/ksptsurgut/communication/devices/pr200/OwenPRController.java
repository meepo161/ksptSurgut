package ru.avem.ksptsurgut.communication.devices.pr200;

import ru.avem.ksptsurgut.communication.devices.DeviceController;
import ru.avem.ksptsurgut.communication.modbus.ModbusController;

import java.nio.ByteBuffer;
import java.util.Observer;

public class OwenPRController implements DeviceController {
    public static final short INSTANT_STATES_REGISTER = 512;
    public static final short INSTANT_STATES_REGISTER2 = 519;
    public static final short INSTANT_STATES_REGISTER3 = 520;
    public static final short FIXED_STATES_REGISTER = 513;
    public static final short FIXED_STATES_REGISTER2 = 521;
    public static final short FIXED_STATES_REGISTER3 = 522;
    public static final short RES_REGISTER = 514;
    public static final short KMS1_REGISTER = 515;
    public static final short KMS2_REGISTER = 516;
    public static final short KMS3_REGISTER = 523;
    public static final short RESET_DOG = 517;
    public static final short RESET_TIMER = 518;

    private static final int NUM_OF_WORDS_IN_REGISTER = 1;
    private static final short NUM_OF_REGISTERS = 2 * NUM_OF_WORDS_IN_REGISTER;

    private OwenPRModel model;
    private byte address;
    private ModbusController modbusController;
    public byte readAttempt = NUMBER_OF_READ_ATTEMPTS;
    public byte readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    public byte writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    public byte writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    private boolean isNeedToRead;

    public OwenPRController(int address, Observer observer, ModbusController controller, int id) {
        this.address = (byte) address;
        model = new OwenPRModel(observer, id);
        modbusController = controller;
    }


    @Override
    public void resetAllAttempts() {
        resetReadAttempts();
        resetReadAttemptsOfAttempts();
        resetWriteAttempts();
        resetWriteAttemptsOfAttempts();
    }

    public void resetReadAttempts() {
        readAttempt = NUMBER_OF_READ_ATTEMPTS;
    }

    private void resetReadAttemptsOfAttempts() {
        readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    }

    public void resetWriteAttempts() {
        writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    }

    private void resetWriteAttemptsOfAttempts() {
        writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    }

    @Override
    public void read(Object... args) {
        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
        if (thereAreReadAttempts()) {
            readAttempt--;
            ModbusController.RequestStatus status = modbusController.readInputRegisters(
                    address, INSTANT_STATES_REGISTER, NUM_OF_REGISTERS, inputBuffer);
            if (status.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
                model.setReadResponding(true);
                resetReadAttempts();
                model.setInstantInputStatus(inputBuffer.getShort());
                model.setFixedInputStatus(inputBuffer.getShort());
                resetReadAttemptsOfAttempts();
            } else {
                read(args);
            }
        } else {
           readAttemptOfAttempt--;
            if (readAttemptOfAttempt <= 0) {
                model.setReadResponding(false);
            } else {
                resetReadAttempts();
            }
        }
    }

    @Override
    public void write(Object... args) {
        short register = (short) args[0];
        int numOfRegisters = (int) args[1];
        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
        ByteBuffer dataBuffer = ByteBuffer.allocate(2 * numOfRegisters);
        for (int i = 2; i < numOfRegisters + 2; i++) {
            dataBuffer.putShort((short) ((int) args[i]));
        }
        dataBuffer.flip();

        if (thereAreWriteAttempts()) {
            writeAttempt--;
            ModbusController.RequestStatus status = modbusController.writeMultipleHoldingRegisters(
                    address, register, (short) numOfRegisters, dataBuffer, inputBuffer);
            if (status.equals(ModbusController.RequestStatus.PORT_NOT_INITIALIZED)) {
                return;
            }
            if (status.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
                model.setWriteResponding(true);
                resetWriteAttempts();
                resetWriteAttemptsOfAttempts();
            } else {
                write(args);
            }
        } else {
            writeAttemptOfAttempt--;
            if (writeAttemptOfAttempt <= 0) {
                model.setWriteResponding(false);
            } else {
                resetWriteAttempts();
            }
        }
    }

    @Override
    public boolean thereAreReadAttempts() {
        return readAttempt > 0;
    }

    @Override
    public boolean thereAreWriteAttempts() {
        return writeAttempt > 0;
    }

    @Override
    public boolean isNeedToRead() {
        return isNeedToRead;
    }

    @Override
    public void setNeedToRead(boolean isNeedToRead) {
        if (isNeedToRead) {
            model.resetResponding();
        }
        this.isNeedToRead = isNeedToRead;
    }

    @Override
    public void resetAllDeviceStateOnAttempts() {
        readAttempt = 1;
        readAttemptOfAttempt = 0;
        writeAttempt = 1;
        writeAttemptOfAttempt = 0;
    }
}
//package ru.avem.ksptamur.communication.devices.fr_a800;
//
//import ru.avem.ksptamur.communication.devices.DeviceController;
//import ru.avem.ksptamur.communication.modbus.ModbusController;
//
//import java.nio.ByteBuffer;
//import java.util.Observer;
//
//public class FRA800Controller implements DeviceController {
//    public static final short CONTROL_REGISTER = 8;
//    public static final short CURRENT_FREQUENCY_REGISTER = 14;
//    public static final short MAX_FREQUENCY_REGISTER = 1002;
//    public static final short MAX_VOLTAGE_REGISTER = 1018;
//
//    private static final int NUM_OF_WORDS_IN_REGISTER = 1;
//    private static final short NUM_OF_REGISTERS = 1 * NUM_OF_WORDS_IN_REGISTER;
//
//    private FRA800Model model;
//    private byte address;
//    private ModbusController modbusController;
//    public byte readAttempt = NUMBER_OF_READ_ATTEMPTS;
//    public byte readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
//    public byte writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
//    public byte writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
//    private boolean needToReed;
//
//    public FRA800Controller(int modbusAddress, Observer observer, ModbusController controller, int id) {
//        address = (byte) modbusAddress;
//        model = new FRA800Model(observer, id);
//        modbusController = controller;
//    }
//
//    @Override
//    public void read(Object... args) {
//        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
//        if (thereAreReadAttempts()) {
//            readAttempt--;
//            ModbusController.RequestStatus status = modbusController.readMultipleHoldingRegisters(
//                    address, CONTROL_REGISTER, NUM_OF_REGISTERS, inputBuffer);
//            if (status.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
//                model.setResponding(true);
//                model.setControlState(inputBuffer.getShort());
//                resetReadAttempts();
//                resetReadAttemptsOfAttempts();
//            } else {
//                read(args);
//            }
//        } else {
//            readAttemptOfAttempt--;
//            if (readAttemptOfAttempt <= 0) {
//                model.setReadResponding(false);
//            }
//        }
//    }
//
//    @Override
//    public void write(Object... args) {
//        short register = (short) args[0];
//        int numOfRegisters = (int) args[1];
//        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
//        ByteBuffer dataBuffer = ByteBuffer.allocate(2 * numOfRegisters);
//        for (int i = 2; i < numOfRegisters + 2; i++) {
//            dataBuffer.putShort((short) ((int) args[i]));
//        }
//        dataBuffer.flip();
//
//        if (thereAreWriteAttempts()) {
//            writeAttempt--;
//            ModbusController.RequestStatus status = modbusController.writeMultipleHoldingRegisters(
//                    address, register, (short) numOfRegisters, dataBuffer, inputBuffer);
//            if (status.equals(ModbusController.RequestStatus.PORT_NOT_INITIALIZED)) {
//                return;
//            }
//            if (status.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
//                model.setWriteResponding(true);
//                resetWriteAttempts();
//                resetWriteAttemptsOfAttempts();
//            } else {
//                write(args);
//            }
//        } else {
//            writeAttemptOfAttempt--;
//            if (writeAttemptOfAttempt <= 0) {
//                model.setWriteResponding(false);
//            }
//        }
//    }
//
//    @Override
//    public void resetAllAttempts() {
//        resetReadAttempts();
//        resetReadAttemptsOfAttempts();
//        resetWriteAttempts();
//        resetWriteAttemptsOfAttempts();
//    }
//
//
//    public void resetReadAttempts() {
//        readAttempt = NUMBER_OF_READ_ATTEMPTS;
//    }
//
//    private void resetReadAttemptsOfAttempts() {
//        readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
//    }
//
//    public void resetWriteAttempts() {
//        writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
//    }
//
//    private void resetWriteAttemptsOfAttempts() {
//        writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
//    }
//
//    @Override
//    public boolean thereAreReadAttempts() {
//        return readAttempt > 0;
//    }
//
//
//    @Override
//    public boolean thereAreWriteAttempts() {
//        return writeAttempt > 0;
//    }
//
//    @Override
//    public boolean needToRead() {
//        return needToReed;
//    }
//
//    @Override
//    public void setNeedToRead(boolean needToRead) {
//        if (needToRead) {
//            model.setReadResponding(true);
//            model.setWriteResponding(true);
//        }
//        needToReed = needToRead;
//    }
//
//    @Override
//    public void resetAllDeviceStateOnAttempts() {
//        resetReadAttempts();
//        readAttemptOfAttempt = 0;
//        resetWriteAttempts();
//        writeAttemptOfAttempt = 0;
//    }
//}
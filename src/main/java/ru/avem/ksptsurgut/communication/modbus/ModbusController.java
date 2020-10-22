package ru.avem.ksptsurgut.communication.modbus;


import java.nio.ByteBuffer;

public interface ModbusController {
    int DELAY = 20;
    int READ_DELAY = 20;

    RequestStatus readInputRegisters(byte deviceAddress, short registerAddress, short numberOfRegisters,
                                     ByteBuffer dstBuffer);

    RequestStatus writeSingleHoldingRegister(byte deviceAddress, short registerAddress, byte[] data,
                                             ByteBuffer dstBuffer);

    RequestStatus readMultipleHoldingRegisters(byte deviceAddress, short registerAddress, short numberOfRegisters,
                                               ByteBuffer dstBuffer);

    RequestStatus writeMultipleHoldingRegisters(byte deviceAddress, short registerAddress, short numberOfRegisters,
                                                ByteBuffer srcBuffer, ByteBuffer dstBuffer);

    enum RequestStatus {
        FRAME_RECEIVED, FRAME_TIME_OUT, BAD_CRC, BAD_FUNCTION, BAD_DATA_ADDS, BAD_DATA_VALUE,
        DEVICE_FAILURE, UNKNOWN, PORT_NOT_INITIALIZED
    }

    enum Command {
        READ_MULTIPLE_HOLDING_REGISTERS((byte) 0x03),
        READ_INPUT_REGISTERS((byte) 0x04),
        WRITE_SINGLE_HOLDING_REGISTER((byte) 0x06),
        WRITE_MULTIPLE_HOLDING_REGISTER((byte) 0x10),
        READ_EXCEPTION_STATUS((byte) 0x07),
        REPORT_SLAVE_ID((byte) 0x11);

        private final byte value;

        Command(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }
}

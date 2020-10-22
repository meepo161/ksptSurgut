package ru.avem.ksptsurgut.communication.modbus;

import ru.avem.ksptsurgut.communication.CommunicationModel;
import ru.avem.ksptsurgut.communication.connections.Connection;
import ru.avem.ksptsurgut.communication.modbus.utils.CRC16;
import ru.avem.ksptsurgut.communication.utils.LogAnalyzer;

import java.nio.ByteBuffer;

import static ru.avem.ksptsurgut.utils.Utils.sleep;

public class RTUController implements ModbusController {
    private final Connection connection;

    private final LogAnalyzer logAnalyzer;

    public RTUController(Connection connection) {
        this.connection = connection;
        logAnalyzer = new LogAnalyzer(connection.getName());
    }

    public RequestStatus readInputRegisters(byte deviceAddress, short registerAddress,
                                            short numberOfRegisters, ByteBuffer dstBuffer) {
        ByteBuffer srcBuffer = ByteBuffer.allocate(8)
                .put(deviceAddress)
                .put(Command.READ_INPUT_REGISTERS.getValue());
        if (numberOfRegisters != 0) {
            srcBuffer.putShort(registerAddress)
                    .putShort(numberOfRegisters);
        }
        CRC16.sign(srcBuffer);
        return sendCommand(deviceAddress, Command.READ_INPUT_REGISTERS.getValue(), srcBuffer, dstBuffer);
    }

    public RequestStatus writeSingleHoldingRegister(byte deviceAddress, short registerAddress,
                                                    byte[] data, ByteBuffer dstBuffer) {
        ByteBuffer srcBuffer = ByteBuffer.allocate(10)
                .put(deviceAddress)
                .put(Command.WRITE_SINGLE_HOLDING_REGISTER.getValue())
                .putShort(registerAddress)
                .put(data);
        CRC16.sign(srcBuffer);
        return sendCommand(deviceAddress, Command.WRITE_SINGLE_HOLDING_REGISTER.getValue(), srcBuffer, dstBuffer);
    }

    public RequestStatus readMultipleHoldingRegisters(byte deviceAddress, short registerAddress,
                                                      short numberOfRegisters,
                                                      ByteBuffer dstBuffer) {
        ByteBuffer srcBuffer = ByteBuffer.allocate(8)
                .put(deviceAddress)
                .put(Command.READ_MULTIPLE_HOLDING_REGISTERS.getValue());
        if (numberOfRegisters != 0) {
            srcBuffer.putShort(registerAddress)
                    .putShort(numberOfRegisters);
        }
        CRC16.sign(srcBuffer);
        return sendCommand(deviceAddress, Command.READ_MULTIPLE_HOLDING_REGISTERS.getValue(), srcBuffer, dstBuffer);
    }

    @Override
    public RequestStatus writeMultipleHoldingRegisters(byte deviceAddress, short registerAddress, short numberOfRegisters, ByteBuffer dataBuffer, ByteBuffer dstBuffer) {
        ByteBuffer srcBuffer = ByteBuffer.allocate(256)
                .put(deviceAddress)
                .put(Command.WRITE_MULTIPLE_HOLDING_REGISTER.getValue());
        if (numberOfRegisters != 0) {
            srcBuffer.putShort(registerAddress)
                    .putShort(numberOfRegisters)
                    .put((byte) (numberOfRegisters * 2))
                    .put(dataBuffer);
        }
        CRC16.sign(srcBuffer);
        return sendCommand(deviceAddress, Command.WRITE_MULTIPLE_HOLDING_REGISTER.getValue(), sliceBuffer(srcBuffer), dstBuffer);
    }

    private ByteBuffer sliceBuffer(ByteBuffer srcBuffer) {
        ByteBuffer slicedBuffer = ByteBuffer.allocate(srcBuffer.position());
        return slicedBuffer.put(srcBuffer.array(), 0, srcBuffer.position());
    }

    private RequestStatus sendCommand(byte deviceAddress, short command, ByteBuffer srcBuffer, ByteBuffer dstBuffer) {
        synchronized (CommunicationModel.instance) {
            RequestStatus status;

            logAnalyzer.addWrite();
            int writeFrameSize = connection.write(srcBuffer.array());
            if (writeFrameSize < 0) {
                return RequestStatus.PORT_NOT_INITIALIZED;
            }

            int attempt = 1;
            byte[] dst;
            int readFrameSize;
            do {
                sleep(ModbusController.READ_DELAY);
                dst = new byte[256];
                readFrameSize = connection.read(dst);
                if (readFrameSize < 0) {
                    return RequestStatus.PORT_NOT_INITIALIZED;
                }
            } while (attempt-- > 0 && readFrameSize <= 4);

            if (readFrameSize > 4 &&
                    dst[0] == deviceAddress &&
                    (dst[1] == command || dst[1] == (command & 0x80))) {
                if (CRC16.check(dst, readFrameSize)) {
                    if ((dst[1] & 0x80) == 0) {
                        logAnalyzer.addSuccess();
                        status = RequestStatus.FRAME_RECEIVED;
                        dstBuffer.clear();
                        dstBuffer.put(dst, 0, readFrameSize).flip().position(3);
                    } else {
                        switch (dst[2]) {
                            case 0x01:
                                status = RequestStatus.BAD_FUNCTION;
                                break;
                            case 0x02:
                                status = RequestStatus.BAD_DATA_ADDS;
                                break;
                            case 0x03:
                                status = RequestStatus.BAD_DATA_VALUE;
                                break;
                            case 0x04:
                                status = RequestStatus.DEVICE_FAILURE;
                                break;
                            default:
                                status = RequestStatus.UNKNOWN;
                                break;
                        }
                    }
                } else {
                    status = RequestStatus.BAD_CRC;
                }
            } else {
                status = RequestStatus.UNKNOWN;
            }
            sleep(ModbusController.DELAY);
            return status;
        }
    }
}

package ru.avem.ksptsurgut.communication.connections.usbserial.driver;


import ru.avem.ksptsurgut.utils.Log;

import javax.usb.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Cp21xxSerialDriver implements UsbSerialDriver {
    private static final Class<Cp21xxSerialDriver> TAG = Cp21xxSerialDriver.class;

    private final UsbDevice device;
    private final UsbSerialPort port;

    public Cp21xxSerialDriver(UsbDevice device) {
        this.device = device;
        port = new Cp21xxSerialPort(device);
    }

    @Override
    public UsbDevice getDevice() {
        return device;
    }

    public static Map<Integer, int[]> getSupportedDevices() {
        final Map<Integer, int[]> supportedDevices = new LinkedHashMap<>();
        supportedDevices.put(SiliconLabsId.VENDOR_SILABS,
                new int[]{
                        SiliconLabsId.SILABS_CP2102,
                        SiliconLabsId.SILABS_CP2105,
                        SiliconLabsId.SILABS_CP2108,
                        SiliconLabsId.SILABS_CP2110
                });
        return supportedDevices;
    }

    @Override
    public UsbSerialPort getPort() {
        return port;
    }

    public static class Cp21xxSerialPort extends CommonUsbSerialPort {
        public static final int DEFAULT_BAUD_RATE = 9600;

        private static final int REQTYPE_HOST_TO_DEVICE = 0x41;

        private static final int SILABSER_IFC_ENABLE_REQUEST_CODE = 0x00;
        private static final int SILABSER_SET_BAUDDIV_REQUEST_CODE = 0x01;
        private static final int SILABSER_SET_LINE_CTL_REQUEST_CODE = 0x03;
        private static final int SILABSER_SET_MHS_REQUEST_CODE = 0x07;
        private static final int SILABSER_SET_BAUDRATE = 0x1E;
        private static final int SILABSER_FLUSH_REQUEST_CODE = 0x12;

        private static final int FLUSH_READ_CODE = 0x0a;
        private static final int FLUSH_WRITE_CODE = 0x05;

        private static final int UART_ENABLE = 0x0001;
        private static final int UART_DISABLE = 0x0000;

        private static final int BAUD_RATE_GEN_FREQ = 0x384000;

        private static final int MCR_DTR = 0x0001;
        private static final int MCR_RTS = 0x0002;
        private static final int MCR_ALL = 0x0003;

        private static final int CONTROL_WRITE_DTR = 0x0100;
        private static final int CONTROL_WRITE_RTS = 0x0200;

        public Cp21xxSerialPort(UsbDevice device) {
            super(device);
        }

        @Override
        public void open() throws IOException {
            if (inputPipe != null || outputPipe != null) {
                throw new IOException("Already opened.");
            }

            boolean opened = false;
            List<UsbInterface> usbInterfaces = device.getActiveUsbConfiguration().getUsbInterfaces();
            try {
                for (UsbInterface usbInterface : usbInterfaces) {
                    try {
                        usbInterface.claim();
                    } catch (UsbException ignored) {
                    }
                }

                UsbInterface dataInterface = usbInterfaces.get(usbInterfaces.size() - 1);
                for (UsbEndpoint ep : (List<UsbEndpoint>) dataInterface.getUsbEndpoints()) {
                    if (ep.getType() == UsbConst.ENDPOINT_TYPE_BULK) {
                        if (ep.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN) {
                            inputPipe = ep.getUsbPipe();
                        } else {
                            outputPipe = ep.getUsbPipe();
                        }
                    }
                }

                setConfigSingle(SILABSER_IFC_ENABLE_REQUEST_CODE, UART_ENABLE);
                setConfigSingle(SILABSER_SET_MHS_REQUEST_CODE, MCR_ALL | CONTROL_WRITE_DTR | CONTROL_WRITE_RTS);
                setConfigSingle(SILABSER_SET_BAUDDIV_REQUEST_CODE, BAUD_RATE_GEN_FREQ / DEFAULT_BAUD_RATE);
                opened = true;
            } catch (UsbException | UsbDisconnectedException e) {
                e.printStackTrace();
            } finally {
                if (!opened) {
                    try {
                        close();
                    } catch (IOException | UsbDisconnectedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void setConfigSingle(int request, int value) throws UsbException {
            device.syncSubmit(device.createUsbControlIrp(
                    (byte) REQTYPE_HOST_TO_DEVICE,
                    (byte) request,
                    (short) value,
                    (short) 0
            ));
        }

        @Override
        public void close() throws IOException {
            if (inputPipe == null || outputPipe == null) {
                throw new IOException("Already closed");
            }

            try {
                setConfigSingle(SILABSER_IFC_ENABLE_REQUEST_CODE, UART_DISABLE);
                inputPipe.close();
                outputPipe.close();
                for (UsbInterface usbInterface : (List<UsbInterface>) device.getActiveUsbConfiguration().getUsbInterfaces()) {
                    usbInterface.release();
                }
            } catch (UsbException | UsbDisconnectedException ignored) {

            } finally {
                inputPipe = null;
                outputPipe = null;
            }
        }

        @Override
        public int read(byte[] dst, int timeoutMillis) throws UsbException {
            int numBytesRead = 0;
            synchronized (readBufferMutex) {
                try {
                    if (!inputPipe.isOpen()) {
                        inputPipe.open();
                    } else {
                        return numBytesRead;
                    }
                    UsbIrp usbIrp = inputPipe.asyncSubmit(readBuffer);
                    usbIrp.waitUntilComplete(timeoutMillis);
                    numBytesRead = usbIrp.getActualLength();
                    System.arraycopy(readBuffer, 0, dst, 0, numBytesRead);
                } finally {
                    try {
                        inputPipe.close();
                    } catch (UsbException | UsbDisconnectedException e) {
                        try {
                            inputPipe.abortAllSubmissions();
                        } catch (UsbDisconnectedException ignored) {
                        }
                    }
                }
            }
            return numBytesRead;
        }

        @Override
        public int write(byte[] src, int timeoutMillis) {
            int offset = 0;
            try {
                outputPipe.open();
                while (offset < src.length) {
                    final int writeLength;
                    final int amtWritten;

                    synchronized (writeBufferMutex) {
                        final byte[] writeBuffer;

                        writeLength = Math.min(src.length - offset, this.writeBuffer.length);
                        if (offset == 0) {
                            writeBuffer = src;
                        } else {
                            System.arraycopy(src, offset, this.writeBuffer, 0, writeLength);
                            writeBuffer = this.writeBuffer;
                        }

                        UsbIrp usbIrp = outputPipe.asyncSubmit(writeBuffer);
                        usbIrp.waitUntilComplete(timeoutMillis);
                        amtWritten = usbIrp.getActualLength();
                    }

                    offset += amtWritten;
                }
            } catch (UsbException | UsbDisconnectedException e) {
                try {
                    outputPipe.abortAllSubmissions();
                } catch (UsbDisconnectedException ignored) {
                }
            } finally {
                try {
                    outputPipe.close();
                } catch (UsbException | UsbDisconnectedException e) {
                    try {
                        outputPipe.abortAllSubmissions();
                    } catch (UsbDisconnectedException ignored) {
                    }
                }
            }

            return offset;
        }

        private void setBaudRate(int baudRate) {
            byte[] data = new byte[]{
                    (byte) (baudRate & 0xff),
                    (byte) ((baudRate >> 8) & 0xff),
                    (byte) ((baudRate >> 16) & 0xff),
                    (byte) ((baudRate >> 24) & 0xff)
            };

            UsbControlIrp irp = device.createUsbControlIrp(
                    (byte) REQTYPE_HOST_TO_DEVICE,
                    (byte) SILABSER_SET_BAUDRATE,
                    (short) 0,
                    (short) 0
            );
            irp.setData(data);
            try {
                device.syncSubmit(irp);
            } catch (UsbException | UsbDisconnectedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws UsbException {
            setBaudRate(baudRate);

            int configDataBits = 0;
            switch (dataBits) {
                case DATABITS_5:
                    configDataBits |= 0x0500;
                    break;
                case DATABITS_6:
                    configDataBits |= 0x0600;
                    break;
                case DATABITS_7:
                    configDataBits |= 0x0700;
                    break;
                case DATABITS_8:
                    configDataBits |= 0x0800;
                    break;
                default:
                    configDataBits |= 0x0800;
                    break;
            }

            switch (parity) {
                case PARITY_NONE:
                    configDataBits |= 0x0000;
                    break;
                case PARITY_ODD:
                    configDataBits |= 0x0010;
                    break;
                case PARITY_EVEN:
                    configDataBits |= 0x0020;
                    break;
            }

            switch (stopBits) {
                case STOPBITS_1:
                    configDataBits |= 0;
                    break;
                case STOPBITS_2:
                    configDataBits |= 2;
                    break;
            }
            setConfigSingle(SILABSER_SET_LINE_CTL_REQUEST_CODE, configDataBits);
        }

        public void purgeHwBuffers(boolean purgeReadBuffers, boolean purgeWriteBuffers) throws UsbException {
            int value = (purgeReadBuffers ? FLUSH_READ_CODE : 0) | (purgeWriteBuffers ? FLUSH_WRITE_CODE : 0);

            if (value != 0) {
                setConfigSingle(SILABSER_FLUSH_REQUEST_CODE, value);
            }
        }
    }

    private static final class SiliconLabsId {
        public static final int VENDOR_SILABS = 0x10c4;

        public static final int SILABS_CP2102 = 0xea60;
        public static final int SILABS_CP2105 = 0xea70;
        public static final int SILABS_CP2108 = 0xea71;
        public static final int SILABS_CP2110 = 0xea80;

        private SiliconLabsId() {
            throw new IllegalAccessError("Non-instantiable class.");
        }
    }
}

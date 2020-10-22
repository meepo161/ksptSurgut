package ru.avem.ksptsurgut.communication.connections.usbserial.driver;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbPipe;
import java.io.IOException;

abstract class CommonUsbSerialPort implements UsbSerialPort {
    public static final int DEFAULT_BUFFER_SIZE = 16 * 1024;

    protected final UsbDevice device;
    protected final Object readBufferMutex = new Object();
    protected final Object writeBufferMutex = new Object();
    protected UsbPipe inputPipe;
    protected UsbPipe outputPipe;
    protected byte[] readBuffer;
    protected byte[] writeBuffer;

    public CommonUsbSerialPort(UsbDevice device) {
        this.device = device;

        readBuffer = new byte[DEFAULT_BUFFER_SIZE];
        writeBuffer = new byte[DEFAULT_BUFFER_SIZE];
    }

    @Override
    public abstract void open() throws IOException, UsbException;

    @Override
    public abstract void close() throws IOException, UsbException;

    @Override
    public abstract int read(final byte[] dst, final int timeoutMillis) throws IOException, UsbException;

    @Override
    public abstract int write(final byte[] src, final int timeoutMillis) throws IOException, UsbException;

    @Override
    public abstract void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException, UsbException;

    @Override
    public String toString() {
        return String.format("<%s>", getClass().getSimpleName());
    }
}

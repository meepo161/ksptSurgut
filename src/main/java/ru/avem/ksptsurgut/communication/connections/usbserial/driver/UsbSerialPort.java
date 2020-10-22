package ru.avem.ksptsurgut.communication.connections.usbserial.driver;

import javax.usb.UsbException;
import java.io.IOException;

public interface UsbSerialPort {
    /**
     * 5 data bits.
     */
    int DATABITS_5 = 5;

    /**
     * 6 data bits.
     */
    int DATABITS_6 = 6;

    /**
     * 7 data bits.
     */
    int DATABITS_7 = 7;

    /**
     * 8 data bits.
     */
    int DATABITS_8 = 8;

    /**
     * No parity.
     */
    int PARITY_NONE = 0;

    /**
     * Odd parity.
     */
    int PARITY_ODD = 1;

    /**
     * Even parity.
     */
    int PARITY_EVEN = 2;

    /**
     * Mark parity.
     */
    int PARITY_MARK = 3;

    /**
     * Space parity.
     */
    int PARITY_SPACE = 4;

    /**
     * 1 stop bit.
     */
    int STOPBITS_1 = 1;

    /**
     * 1.5 stop bits.
     */
    int STOPBITS_1_5 = 3;

    /**
     * 2 stop bits.
     */
    int STOPBITS_2 = 2;

    void open() throws IOException, UsbException;

    void close() throws IOException, UsbException;

    int read(final byte[] dst, final int timeoutMillis) throws IOException, UsbException;

    int write(final byte[] src, final int timeoutMillis) throws IOException, UsbException;

    void setParameters(int baudRate, int dataBits, int stopBits, int parity) throws IOException, UsbException;
}

package ru.avem.ksptsurgut.communication.connections.usbserial.driver;

import javax.usb.UsbDevice;

public interface UsbSerialDriver {
    UsbDevice getDevice();

    UsbSerialPort getPort();
}

package ru.avem.ksptsurgut.communication.connections.usbserial.driver;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class UsbSerialProber {
    private static final ProbeTable probeTable = new ProbeTable();

    public static List<UsbSerialDriver> findAllUsbSerialDrivers() throws UsbException {
        List<UsbSerialDriver> allDrivers = new ArrayList<>();
        fillAllDrivers(allDrivers, UsbHostManager.getUsbServices().getRootUsbHub());
        return allDrivers;
    }

    private static void fillAllDrivers(List<UsbSerialDriver> result, UsbHub rootUsbHub) {
        List<UsbDevice> attachedUsbDevices = rootUsbHub.getAttachedUsbDevices();
        for (UsbDevice usbDevice : attachedUsbDevices) {
            if (usbDevice.isUsbHub()) {
                fillAllDrivers(result, (UsbHub) usbDevice);
            } else {
                UsbSerialDriver driver = probeDevice(usbDevice);
                if (driver != null) {
                    result.add(driver);
                }
            }
        }
    }

    private static UsbSerialDriver probeDevice(final UsbDevice usbDevice) {
        final int vendorId = usbDevice.getUsbDeviceDescriptor().idVendor() & 0xffff;
        final int productId = usbDevice.getUsbDeviceDescriptor().idProduct() & 0xffff;

        final Class<? extends UsbSerialDriver> driverClass = probeTable.findDriver(vendorId, productId);
        if (driverClass != null) {
            final UsbSerialDriver driver;
            try {
                final Constructor<? extends UsbSerialDriver> constructor = driverClass.getConstructor(UsbDevice.class);
                driver = constructor.newInstance(usbDevice);
            } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return driver;
        }
        return null;
    }
}

package ru.avem.ksptsurgut.communication.connections.usbserial;


import ru.avem.ksptsurgut.communication.connections.Connection;
import ru.avem.ksptsurgut.communication.connections.SerialParameters;
import ru.avem.ksptsurgut.communication.connections.usbserial.driver.UsbSerialDriver;
import ru.avem.ksptsurgut.communication.connections.usbserial.driver.UsbSerialPort;
import ru.avem.ksptsurgut.communication.connections.usbserial.driver.UsbSerialProber;
import ru.avem.ksptsurgut.utils.Log;

import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import java.io.IOException;
import java.util.List;

import static ru.avem.ksptsurgut.communication.modbus.utils.Utils.toHexString;

public class UsbSerialConnection implements Connection {
    private static final Class<UsbSerialConnection> TAG = UsbSerialConnection.class;

    public final String productName;
    private final SerialParameters serialParameters;
    private UsbSerialPort port;

    public UsbSerialConnection(String productName, SerialParameters serialParameters) {
        this.productName = productName;
        this.serialParameters = serialParameters;
    }

    @Override
    public String getName() {
        return productName;
    }

    @Override
    public void setBaudrate(int baudrate) {
        setParameters(
                baudrate,
                serialParameters.dataBits,
                serialParameters.stopBits,
                serialParameters.parity
        );
    }

    @Override
    public void setParameters(int baudRate, int dataBits, int stopBits, int parity) {
        if (isInitiated()) {
            try {
                port.setParameters(baudRate, dataBits, stopBits, parity);
            } catch (IOException | UsbException | UsbDisconnectedException e) {
                close();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        try {
            if (port != null) {
                port.close();
            }
        } catch (UsbDisconnectedException | UsbException | IOException e) {
            e.printStackTrace();
        } finally {
            port = null;
        }
    }

    @Override
    public boolean isInitiated() {
        return port != null;
    }

    @Override
    public synchronized int write(byte[] src) {
        initConnection();
        int numBytesWrite = -1;
        if (port != null) {
            try {
                numBytesWrite = port.write(src, serialParameters.writeTimeout);
                Log.i(TAG, "Write " + numBytesWrite + " bytes.");
                Log.i(TAG, "Write " + toHexString(src));
            } catch (IOException | UsbException | UsbDisconnectedException e) {
                close();
                e.printStackTrace();
            }
        }
        return numBytesWrite;
    }

    private void initConnection() {
        if (!isInitiated()) {
            UsbSerialDriver usbSerialDriver = getSerialDriver();
            if (usbSerialDriver != null) {
                UsbSerialPort port = usbSerialDriver.getPort();
                try {
                    port.open();
                    this.port = port;
                    setParameters(serialParameters.baudRate, serialParameters.dataBits, serialParameters.stopBits, serialParameters.parity);
                } catch (UsbException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private UsbSerialDriver getSerialDriver() {
        try {
            List<UsbSerialDriver> drivers = UsbSerialProber.findAllUsbSerialDrivers();

            if (!drivers.isEmpty()) {
                for (UsbSerialDriver driver : drivers) {
                    try {
                        if (driver.getDevice().getProductString().equals(productName)) {
                            return driver;
                        }
                    } catch (Exception ignored) {

                    }
                }
            }
        } catch (UsbException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public synchronized int read(byte[] dst) {
        initConnection();
        int numBytesRead = -1;
        if (port != null) {
            try {
                numBytesRead = port.read(dst, serialParameters.readTimeout);
                Log.i(TAG, "Read " + numBytesRead + " bytes.");
                Log.i(TAG, "Read: " + toHexString(dst, numBytesRead));
            } catch (IOException | UsbException | UsbDisconnectedException e) {
                close();
                e.printStackTrace();
            }
        }
        return numBytesRead;
    }
}

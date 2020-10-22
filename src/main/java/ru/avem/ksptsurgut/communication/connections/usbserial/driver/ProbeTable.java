package ru.avem.ksptsurgut.communication.connections.usbserial.driver;

import ru.avem.ksptsurgut.utils.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProbeTable {
    private final Map<Pair<Integer, Integer>, Class<? extends UsbSerialDriver>> probeTable = new LinkedHashMap<>();

    public ProbeTable() {
        addCp21xxDriver();
    }

    private void addCp21xxDriver() {
        final Method method;

        try {
            method = Cp21xxSerialDriver.class.getMethod("getSupportedDevices");
        } catch (SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        final Map<Integer, int[]> devices;
        try {
            devices = (Map<Integer, int[]>) method.invoke(null);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<Integer, int[]> entry : devices.entrySet()) {
            final int vendorId = entry.getKey().intValue();
            for (int productId : entry.getValue()) {
                probeTable.put(Pair.create(vendorId, productId), Cp21xxSerialDriver.class);
            }
        }
    }

    public Class<? extends UsbSerialDriver> findDriver(int vendorId, int productId) {
        return probeTable.get(Pair.create(vendorId, productId));
    }
}

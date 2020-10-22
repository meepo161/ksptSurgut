package ru.avem.ksptsurgut.communication.utils;

import java.nio.ByteBuffer;

public class Utils {
    private Utils() {
        throw new IllegalAccessError("Non-instantiable class.");
    }

    public static byte[] intToByteArray(int i) {
        ByteBuffer convertBuffer = ByteBuffer.allocate(4);
        convertBuffer.clear();
        return convertBuffer.putInt(i).array();
    }

    public static byte[] floatToByteArray(float f) {
        ByteBuffer convertBuffer = ByteBuffer.allocate(4);
        convertBuffer.clear();
        return convertBuffer.putFloat(f).array();
    }
}

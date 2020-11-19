package ru.avem.ksptsurgut.utils;

import java.awt.*;
import java.io.*;
import java.util.Locale;

public class Utils {

    private Utils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static void sleep(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileFromFile(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source);
             OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    public static void copyFileFromStream(InputStream is, File dest) throws IOException {
        try (OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
        }
    }

    public static void printStackTrace(String message) {
        System.out.println("[" + message + "]");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < elements.length; i++) {
            StackTraceElement s = elements[i];
            System.out.println("\tat " + s.getClassName() + "." + s.getMethodName()
                    + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
        }
        System.out.println();
    }

    public static String formatRMrg(float measuringR) {
        String units;
        if (measuringR > 1_000_000_000_000f) {
            measuringR = measuringR / 1_000_000_000_000f;
            units = "*10¹²";
        } else if (measuringR > 1_000_000_000f) {
            measuringR = measuringR / 1_000_000_000f;
            units = "*10⁹";
        } else if (measuringR > 1_000_000f) {
            measuringR = measuringR / 1_000_000f;
            units = "*10⁶";
        }  else if (measuringR > 1_000f) {
            measuringR = measuringR / 1_000f;
            units = "*10³";
        } else {
            units = "";
        }
        return String.format("%.2f %s", measuringR, units);
    }

    public static String formatRealNumber(double num) {
        num = Math.abs(num);
        String format = "%.0f";
        if (num == 0) {
            format = "%.0f";
        } else if (num < 0.1f) {
            format = "%.5f";
        } else if (num < 1f) {
            format = "%.4f";
        } else if (num < 10f) {
            format = "%.3f";
        } else if (num < 100f) {
            format = "%.2f";
        } else if (num < 1000f) {
            format = "%.1f";
        } else if (num < 10000f) {
            format = "%.0f";
        }
        return String.format(Locale.US, format, num);
    }
}

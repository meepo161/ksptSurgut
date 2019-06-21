package ru.avem.ksptamur;

import ru.avem.ksptamur.communication.serial.driver.UsbSerialPort;
import ru.avem.ksptamur.utils.BuildConfig;


public final class Constants {
    public static final class Display {
        public static final int WIDTH = BuildConfig.DEBUG ? 1920 : 1366;
        public static final int HEIGHT = BuildConfig.DEBUG ? 1080 : 768;
    }

    public static final class Communication {
        public static final String RS485_DEVICE_NAME = "CP2103 USB to RS-485";
        public static final int BAUDRATE_MAIN = 38400;
        public static final int BAUDRATE_MEGACS = 9600;
        public static final int DATABITS = UsbSerialPort.DATABITS_8;
        public static final int STOPBITS = UsbSerialPort.STOPBITS_1;
        public static final int PARITY = UsbSerialPort.PARITY_NONE;
        public static final int WRITE_TIMEOUT = 100;
        public static final int READ_TIMEOUT = 100;
    }

    public static final class Experiments {
        public static final String EXPERIMENT0_NAME = "0. Измерение сопротивления изоляции";
        public static final String EXPERIMENT1_NAME = "1. Измерение сопротивления обмоток постоянному току";
        public static final String EXPERIMENT2_NAME = "2. Определение коэффициента трансформации";
        public static final String EXPERIMENT3_NAME = "3. Проверка группы соединений обмоток";
        public static final String EXPERIMENT4_NAME = "4. Реализация опыта короткого замыкания";
        public static final String EXPERIMENT5_NAME = "5. Реализация опыта холостого хода";
        public static final String EXPERIMENT6_NAME = "6. Испытание прочности межвитковой изоляции";
        public static final String EXPERIMENT7_NAME = "7. Испытание электрической прочности изоляции";
    }

    public static final class Measuring {
        public static final int VOLT = 10;
        public static final int HZ = 100;
    }
}
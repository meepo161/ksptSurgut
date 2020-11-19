package ru.avem.ksptsurgut;

import ru.avem.ksptsurgut.communication.serial.driver.UsbSerialPort;
import ru.avem.ksptsurgut.utils.BuildConfig;

import java.text.SimpleDateFormat;


public final class Constants {
    public static final class Display {
        public static final int WIDTH = BuildConfig.DEBUG ? 1366 : 1366;
        public static final int HEIGHT = BuildConfig.DEBUG ? 768 : 768;
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
        public static final String EXPERIMENT1_NAME = "1. Измерение сопротивления изоляции";
        public static final String EXPERIMENT2_NAME = "2. Измерение сопротивления обмоток постоянному току";
        public static final String EXPERIMENT3_NAME = "3. Определение коэффициента трансформации и группы соединений обмоток";
        public static final String EXPERIMENT4_NAME = "4. Реализация опыта короткого замыкания";
        public static final String EXPERIMENT5_NAME = "5. Реализация опыта холостого хода";
        public static final String EXPERIMENT6_NAME = "6. Испытание прочности межвитковой изоляции";
        public static final String EXPERIMENT7_NAME = "7. Испытание электрической прочности изоляции";
    }

    public static final class Measuring {
        public static final int VOLT = 10;
        public static final int HZ = 100;
    }

    public static final class Info {
        public static final String TITLE = "КСПТ (ООО \"РН-Комсомольский НПЗ\")";
        public static final String VERSION = "Версия: 1.0.0\n" +
                "ООО НПП АВЭМ, Новочеркасск\n" +
                "Разработчик: Сулейманов М.У.\n" +
                "+79381110516";
        public static final String DATE = "Дата: 05.07.2019";
    }

    public static final class Formatting {
        public static final SimpleDateFormat EXPERIMENT_FORMAT = new SimpleDateFormat("HH:mm:ss-SSS");
        public static final SimpleDateFormat PROTOCOL_FORMAT = new SimpleDateFormat("Время проведения испытания: HH:mm:ss");
        public static final SimpleDateFormat LOGGING_FILE_FORMAT = new SimpleDateFormat("dd_MM(HH-mm-ss)");
        public static final SimpleDateFormat PROTOCOL_DATE_FORMAT = new SimpleDateFormat("dd-MM-yy");
    }
}
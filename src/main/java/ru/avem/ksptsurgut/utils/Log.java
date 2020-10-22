package ru.avem.ksptsurgut.utils;

import com.j256.ormlite.logger.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Log {
    private static final DateFormat df = new SimpleDateFormat("HH:mm:ss-SSS");

    public static void d(String tag, String s) {
        System.out.printf("%s D/%s: %s\n", df.format(System.currentTimeMillis()), tag, s);
    }

    public static void i(String tag, String s) {
        System.out.printf("%s I/%s: %s\n", df.format(System.currentTimeMillis()), tag, s);
    }

    public static void w(String tag, String s, Exception e) {
        System.out.printf("%s W/%s: %s\n", df.format(System.currentTimeMillis()), tag, s);
    }

    public static void w(String tag, Exception e) {
        System.out.printf("%s W/%s: %s\n", df.format(System.currentTimeMillis()), tag, e.getMessage());
    }

    public static void e(String tag, String s, IOException e) {
        System.out.printf("%s E/%s: %s %s\n", df.format(System.currentTimeMillis()), s, tag, e.getMessage());
    }

    public static void d(Class<?> clazz, String s) {
        LoggerFactory.getLogger(clazz).debug(s);
    }

    public static void i(Class<?> clazz, String s) {
        LoggerFactory.getLogger(clazz).info(s);
    }

    public static void w(String tag, String s) {
        LoggerFactory.getLogger(tag).warn(s);
    }

    public static void w(Class<?> clazz, String s) {
        LoggerFactory.getLogger(clazz).warn(s);
    }

    public static void w(String tag, String s, Throwable e) {
        LoggerFactory.getLogger(tag).warn(s, e);
    }

    public static void w(Class<?> clazz, String s, Throwable e) {
        LoggerFactory.getLogger(clazz).warn(s, e);
    }

    public static void e(String tag, String s) {
        LoggerFactory.getLogger(tag).error(s);
    }

    public static void e(Class<?> clazz, String s) {
        LoggerFactory.getLogger(clazz).error(s);
    }

    public static void e(String tag, String s, Throwable e) {
        LoggerFactory.getLogger(tag).error(s, e);
    }

    public static void e(Class<?> clazz, String s, Throwable e) {
        LoggerFactory.getLogger(clazz).error(s, e);
    }
}

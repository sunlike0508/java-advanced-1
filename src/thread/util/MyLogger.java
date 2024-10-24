package thread.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public abstract class MyLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static void log(Object object) {

        String time = LocalTime.now().format(formatter);

        System.out.printf("%S [%9s] %s\n", time, Thread.currentThread().getName(), object);
    }
}

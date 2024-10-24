package thread.start;

import static thread.util.MyLogger.log;

public class InnerRunnableMainV4 {

    public static void main(String[] args) {

        log("main() start");

        Thread thread = new Thread(() -> log("run() start"));

        thread.start();

        log("main() end");
    }
}

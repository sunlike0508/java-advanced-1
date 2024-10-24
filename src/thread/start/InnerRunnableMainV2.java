package thread.start;

import static thread.util.MyLogger.log;

public class InnerRunnableMainV2 {

    public static void main(String[] args) {

        log("main() start");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                log("run() start");
            }
        };

        new Thread(runnable).start();

        log("main() end");
    }
}

package thread.start;

import static thread.util.MyLogger.log;

public class CounterThreadMain {
    public static void main(String[] args) {
        new Thread(() -> {
            for(int i = 1; i <= 5; i++) {
                try {
                    log("value: " + i);
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    static class CounterThread extends Thread {

        @Override
        public void run() {
            for(int i = 1; i <= 5; i++) {
                try {
                    log("value: " + i);
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

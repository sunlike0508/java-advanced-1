package thread.start;

import static util.MyLogger.log;

public class CounterRunnableMain {

    public static void main(String[] args) {
        Runnable r = new CounterRunnable();

        Thread thread = new Thread(r, "counter");
        thread.start();
    }

    static class CounterRunnable implements Runnable {

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

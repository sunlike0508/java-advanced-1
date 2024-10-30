package thread.start;

import static util.MyLogger.log;

public class MultiThreadMain {

    public static void main(String[] args) {

        new Thread(() -> run(1000), "ThreadA").start();

        new Thread(() -> run(500), "ThreadB").start();
    }

    static void run(int time) {
        int count = 0;

        while(true) {
            try {
                log("value : " + count++);
                Thread.sleep(time);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}

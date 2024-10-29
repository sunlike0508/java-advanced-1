package thread.control.yield;

import thread.start.HelloRunnable;
import static thread.util.ThreadUtils.sleep;

public class YieldMain {

    static final int THREAD_COUNT = 1000;

    public static void main (String[] args) {
        for(int i = 0 ; i < THREAD_COUNT ; i++) {
            new Thread(new MyRunnable()).start();
        }
    }

    static class MyRunnable implements Runnable {

        @Override
        public void run() {
            for(int i = 0 ; i < 10 ; i++) {

                System.out.println(Thread.currentThread().getName() + " - " + i);
                // 1. 암것도 안하기
                //sleep(1); // 2. sleep(1)
                Thread.yield(); // yield
            }
        }
    }
}

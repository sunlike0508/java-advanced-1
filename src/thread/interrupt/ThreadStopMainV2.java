package thread.interrupt;

import static thread.util.MyLogger.log;
import static thread.util.ThreadUtils.sleep;

public class ThreadStopMainV2 {

    public static void main(String[] args) {
        MyTask myTask = new MyTask();
        Thread thread = new Thread(myTask, "work");
        thread.start();

        sleep(4000);
        log("작업 중단 지시");
        thread.interrupt();
        log("work 쓰레드 인터럽트 상태1 = " + thread.isInterrupted());
    }

    static class MyTask implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    log("작업중");
                    Thread.sleep(3000);
                }
            } catch(InterruptedException e) {
                log("work 쓰레드 인터럽트 상태2 = " + Thread.currentThread().isInterrupted());
                log("message = " + e.getMessage());
                log("state = " + Thread.currentThread().getState());
            }

            log("자원정리");
            log("작업종료");
        }
    }
}

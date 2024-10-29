package thread.interrupt;

import static thread.util.MyLogger.log;
import static thread.util.ThreadUtils.sleep;

public class ThreadStopMainV3 {

    public static void main(String[] args) {
        MyTask myTask = new MyTask();
        Thread thread = new Thread(myTask, "work");
        thread.start();

        sleep(100);
        log("작업 중단 지시");
        thread.interrupt();
        log("work 쓰레드 인터럽트 상태1 = " + thread.isInterrupted());
    }

    static class MyTask implements Runnable {

        @Override
        public void run() {

            while(!Thread.currentThread().isInterrupted()) {
                log("작업중");
            }

            log("work 쓰레드 인터럽트 상태2 = " + Thread.currentThread().isInterrupted());

            try {
                log("자원정리");
                Thread.sleep(1000);
                log("작업종료");
            } catch(InterruptedException e) {
                log("자원 정리 실패 - 인터럽트 발생");
                log("work 쓰레드 인터럽트 상태3 = " + Thread.currentThread().isInterrupted());
            }
        }
    }
}

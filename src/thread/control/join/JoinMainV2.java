package thread.control.join;

import static util.MyLogger.log;
import static util.ThreadUtils.sleep;

public class JoinMainV2 {

    public static void main(String[] args) {
        log("start");
        SumTask task1 = new SumTask(1, 50);
        SumTask task2 = new SumTask(51, 100);

        Thread t1 = new Thread(task1, "thread-1");
        Thread t2 = new Thread(task2, "thread-2");

        t1.start();
        t2.start();
        log("main 쓰레드 sleep");
        sleep(3000);
        log("main 쓰레드 wakeup");

        log("task1.result = " + task1.result);
        log("task2.result = " + task2.result);

        int sumAll = task1.result + task2.result;

        log("sumAll = " + sumAll);
        log("end");

    }

    static class SumTask implements Runnable {

        int startValue;
        int endValue;
        int result;

        public SumTask(int startValue, int endValue) {
            this.startValue = startValue;
            this.endValue = endValue;
        }

        @Override
        public void run() {
            log("작업시작");
            sleep(2000);
            int sum = 0;
            for(int i = startValue; i <= endValue; i++) {
                sum += i;
            }
            result = sum;
            log("작업완료 result : " + result);
        }
    }
}

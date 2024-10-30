package thread.volatile1;

import static util.MyLogger.log;
import static util.ThreadUtils.sleep;

public class VolatileFlagMain {

    public static void main(String[] args) {
        MyTask myTask = new MyTask();
        Thread t1 = new Thread(myTask, "work");
        log("runFlag = " + myTask.runFlag);
        t1.start();

        sleep(1000);
        log("runFlag를 false");
        myTask.runFlag = false;
        log("runFlag = " + myTask.runFlag);
        log("main 종료");
    }

    static class MyTask implements Runnable {

        boolean runFlag = true;
        //volatile boolean runFlag = false;

        @Override
        public void run() {
            log("task 시작");
            while(runFlag) {

            }
            log("task 종료");
        }
    }
}

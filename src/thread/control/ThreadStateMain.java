package thread.control;

import static util.MyLogger.log;

public class ThreadStateMain {

    public static void main(String[] args) throws InterruptedException {

        Thread thread = new Thread(new MyRunnable(), "myThread");
        log("myThread.state1 = " + thread.getState());
        log("myThread.start()");
        thread.start();
        Thread.sleep(1000);
        log("myThread.state3 = " + thread.getState());
        Thread.sleep(4000);
        log("myThread.state5 = " + thread.getState());
        log("myThread.end()");
    }

    static class MyRunnable implements Runnable {

        @Override
        public void run() {
            try {

                log("start");
                log("myThread.state2 = " + Thread.currentThread().getState());
                log("sleep() start");
                Thread.sleep(3000);
                //log("myThread.state3 = " + Thread.currentThread().getState());
                log("sleep() end");
                log("myThread.state4 = " + Thread.currentThread().getState());
                log("end");
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

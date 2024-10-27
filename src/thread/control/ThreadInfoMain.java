package thread.control;

import thread.start.HelloThread;
import static thread.util.MyLogger.log;

public class ThreadInfoMain {

    public static void main(String[] args) {
        Thread mainThread = Thread.currentThread();

        log("mainThread = " + mainThread);
        log("mainThread = " + mainThread.threadId());
        log("mainThread = " + mainThread.getName());
        log("mainThread = " + mainThread.getPriority());
        log("mainThread = " + mainThread.getThreadGroup());
        log("mainThread = " + mainThread.getState());


        Thread myThread = new Thread(new HelloThread(), "myThread");

        log("myThread = " + myThread);
        log("myThread = " + myThread.threadId());
        log("myThread = " + myThread.getName());
        log("myThread = " + myThread.getPriority());
        log("myThread = " + myThread.getThreadGroup());
        log("myThread = " + myThread.getState());
    }
}

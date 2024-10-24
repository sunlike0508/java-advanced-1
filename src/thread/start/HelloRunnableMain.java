package thread.start;

public class HelloRunnableMain {

    public static void main(String[] args) {

        System.out.println(Thread.currentThread().getName() + ": main() start");
        HelloRunnable runnable = new HelloRunnable();
        Thread t1 = new Thread(runnable);
        t1.start();
        System.out.println(Thread.currentThread().getName() + ": main() end");
    }
}

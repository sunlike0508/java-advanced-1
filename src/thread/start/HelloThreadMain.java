package thread.start;

public class HelloThreadMain {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + "메인 시작");

        System.out.println(Thread.currentThread().getName() + "start 시작");
        HelloThread helloThread = new HelloThread();
        System.out.println(Thread.currentThread().getName() + "start 끝");

        helloThread.start();

        System.out.println(Thread.currentThread().getName() + "메인 끝");
    }
}

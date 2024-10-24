package thread.start;

public class HelloThreadMain {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + "메인 시작");

        System.out.println(Thread.currentThread().getName() + "run 호출 시작");
        HelloThread helloThread = new HelloThread();
        //helloThread.start();
        helloThread.run();
        System.out.println(Thread.currentThread().getName() + "run 호출 끝");
        System.out.println(Thread.currentThread().getName() + "메인 끝");
    }
}

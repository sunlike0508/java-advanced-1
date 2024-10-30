package thread.control.printer;

import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import static util.MyLogger.log;

public class MyPrinterV3 {

    public static void main(String[] args) {
        Printer printer = new Printer();
        Thread printThread = new Thread(printer, "printer");
        printThread.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            log("프린터할 문서를 입력해: 종료(q): ");

            String input = scanner.nextLine();

            if(input.equals("q")) {
               printThread.interrupt();
               break;
            }

            printer.addJob(input);
        }
    }

    static class Printer implements Runnable {
        Queue<String> jobQueue = new ConcurrentLinkedQueue<>();

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                if(jobQueue.isEmpty()) {
                    continue;
                }

                try {
                    String job = jobQueue.poll();
                    log("출력 시작" + job + ", 대기 문서: " + jobQueue);
                    Thread.sleep(3000);
                    log("출력 완료");
                } catch (InterruptedException e) {
                    log("인터럽트");
                    break;
                }
            }

            log("프린터 종료");
        }

        public void addJob(String input) {
            jobQueue.add(input);
        }
    }
}

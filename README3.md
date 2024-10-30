# 메모리 가시성

## volatile

```java
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

```



`main` 스레드, `work` 스레드 모두 `MyTask` 인스턴스( `x001` )에 있는 `runFlag` 를 사용한다. 

이 값을 `false` 로 변경하면 `work` 스레드의 작업을 종료할 수 있다.

프로그램은 아주 단순하다.

`main` 스레드는 새로운 스레드인 `work` 스레드를 생성하고 작업을 시킨다.

`work` 스레드는 `run()` 메서드를 실행하면서 `while(runFlag)` 가 `true` 인 동안 계속 작업을 한다. 

만약 `runFlag` 가 `false` 로 변경되면 반복문을 빠져나오면서 `"task 종료"` 를 출력하고 작업을 종료한다.

`main` 스레드는 `sleep()` 을 통해 1초간 쉰 다음에 `runFlag` 를 `false` 로 설정한다.

`work` 스레드는 `run()` 메서드를 실행하면서 `while(runFlag)` 를 체크하는데, 이제 `runFlag` 가 `false` 가 되었으므로 `"task 종료"` 를 출력하고 작업을 종료해야 한다.

```shell
23:08:08.671 [     main] runFlag = true
23:08:08.673 [     work] task 시작
23:08:09.678 [     main] runFlag를 false
23:08:09.678 [     main] runFlag = false
23:08:09.679 [     main] main 종료
```

원래는 task 종료 로그를 찍고 프로그램이 종료되어야 하나 그렇지 않다.

실제 실행 결과를 보면 `task 종료` 가 출력되지 않는다! 그리고 자바 프로그램도 멈추지 않고 계속 실행된다. 

정확히는 `work` 스레드가 while문에서 빠져나오지 못하고 있는 것이다.

`work` 스레드는 while문을 빠 져나오고 task 종료를 출력해야 한다! 도대체 어떻게 된 일일까?

## volatile, 메모리 가시성2 메모리 가시성 문제

멀티스레드는 이미 여러 스레드가 작동해서 안 그래도 이해하기 어려운데, 거기에 한술 더하는 문제가 있으니, 바로 메모리 가시성(memory visibility)문제이다. 

먼저 우리가 일반적으로 생각하는 메모리 접근 방식에 대해서 설명하겠다. 

**일반적으로 생각하는 메모리 접근 방식**




`main` 스레드와 `work` 스레드는 각각의 CPU 코어에 할당되어서 실행된다. 

물론 CPU 코어가 1개라면 빠르게 번갈아 가면서 실행될 수 있다.

















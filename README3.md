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

<img width="683" alt="Screenshot 2024-10-30 at 23 09 21" src="https://github.com/user-attachments/assets/d4fbf105-7c7d-444b-99d3-b16e3064eefa">

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

## volatile, 메모리 가시성2 

### 메모리 가시성 문제

멀티스레드는 이미 여러 스레드가 작동해서 안 그래도 이해하기 어려운데, 거기에 한술 더하는 문제가 있으니, 바로 메모리 가시성(memory visibility)문제이다. 

먼저 우리가 일반적으로 생각하는 메모리 접근 방식에 대해서 설명하겠다. 

### **일반적으로 생각하는 메모리 접근 방식**

<img width="476" alt="Screenshot 2024-10-30 at 23 12 07" src="https://github.com/user-attachments/assets/3e7433ae-fc25-4574-960b-076eec97663d">

`main` 스레드와 `work` 스레드는 각각의 CPU 코어에 할당되어서 실행된다. 

물론 CPU 코어가 1개라면 빠르게 번갈아 가면서 실행될 수 있다.

<img width="596" alt="Screenshot 2024-10-30 at 23 27 48" src="https://github.com/user-attachments/assets/b075f95d-5902-4b96-8d5b-077e584999e8">

점선 위쪽은 스레드의 실행 흐름을 나타내고, 점선 아래쪽은 하드웨어를 나타낸다.

자바 프로그램을 실행하고 `main` 스레드와 `work` 스레드는 모두 메인 메모리의 `runFlag` 의 값을 읽는다. 

프로그램의 시작 시점에는 `runFlag` 를 변경하지 않기 때문에 모든 스레드에서 `true` 의 값을 읽는다.

참고로 `runFlag` 의 초기값은 `true` 이다.

`work` 스레드의 경우 `while(runFlag[true])` 가 만족하기 때문에 while문을 계속 반복해서 수행한다.

<img width="668" alt="Screenshot 2024-10-30 at 23 29 54" src="https://github.com/user-attachments/assets/8a8a4130-a6ed-4141-89c2-09bf7bafcd09">

`main` 스레드는 `runFlag` 값을 `false` 로 설정한다.

이때 메인 메모리의 `runFlag` 값이 `false` 로 설정된다.

`work` 스레드는 `while(runFlag)` 를 실행할 때 `runFlag` 의 데이터를 메인 메모리에서 확인한다. 

`runFlag` 의 값이 `false` 이므로 while문을 탈출하고, `"task 종료"` 를 출력한다.

아마도 이런 시나리오를 생각했을 것이다. 그런데 실제로는 이런 방식으로 작동하지 않는다.

### 실제 메모리의 접근 방식

CPU는 처리 성능을 개선하기 위해 중간에 캐시 메모리라는 것을 사용한다.

<img width="472" alt="Screenshot 2024-10-30 at 23 30 53" src="https://github.com/user-attachments/assets/b8c5c8bc-8aaf-4db7-941c-796fe2762a57">

메인 메모리는 CPU 입장에서 보면 거리도 멀고, 속도도 상대적으로 느리다. 

대신에 상대적으로 가격이 저렴해서 큰 용량을 쉽게 구성할 수 있다.

CPU 연산은 매우 빠르기 때문에 CPU 연산의 빠른 성능을 따라가려면, CPU 가까이에 매우 빠른 메모리가 필요한데, 이것이 바로 캐시 메모리이다. 

캐시 메모리는 CPU와 가까이 붙어있고, 속도도 매우 빠른 메모리이다. 

하지만 상대적으로 가격이 비싸기 때문에 큰 용량을 구성하기는 어렵다.

현대의 CPU 대부분은 코어 단위로 캐시 메모리를 각각 보유하고 있다. 

참고로 여러 코어가 공유하는 캐시 메모리도 있다.

<img width="463" alt="Screenshot 2024-10-30 at 23 31 37" src="https://github.com/user-attachments/assets/fb5f158a-33f5-4f93-8658-682a9e282306">

각 스레드가 `runFlag` 의 값을 사용하면 CPU는 이 값을 효율적으로 처리하기 위해 먼저 `runFlag` 를 캐시 메 모리에 불러온다.

그리고 이후에는 캐시 메모리에 있는 `runFlag` 를 사용하게 된다.

<img width="496" alt="Screenshot 2024-10-30 at 23 45 30" src="https://github.com/user-attachments/assets/135bc65f-29ad-42cb-ae95-d6a95d37be7d">

점선 위쪽은 스레드의 실행 흐름을 나타내고, 점선 아래쪽은 하드웨어를 나타낸다.

자바 프로그램을 실행하고 `main` 스레드와 `work` 스레드는 모두 `runFlag` 의 값을 읽는다. 

CPU는 이 값을 효율적으로 처리하기 위해 먼저 캐시 메모리에 불러온다.

`main` 스레드와 `work` 스레드가 사용하는 `runFlag` 가 각각의 캐시 메모리에 보관된다.

프로그램의 시작 시점에는 `runFlag` 를 변경하지 않기 때문에 모든 스레드에서 `true` 의 값을 읽는다. 

참고로 `runFlag` 의 초기값은 `true` 이다.

`work` 스레드의 경우 `while(runFlag[true])` 가 만족하기 때문에 while문을 계속 반복해서 수행한다.

<img width="576" alt="Screenshot 2024-10-30 at 23 51 24" src="https://github.com/user-attachments/assets/161963a3-5bc4-40d1-b7f6-2ea39828b3cd">

`main` 스레드는 `runFlag` 를 `false` 로 설정한다.

이때 캐시 메모리의 `runFlag` 가 `false` 로 설정된다.

**여기서 핵심은 캐시 메모리의 runFlag 값만 변한다는 것이다! 메인 메모리에 이 값이 즉시 반영되지 않는다.**

`main` 스레드가 `runFlag` 의 값을 변경해도 CPU 코어1이 사용하는 캐시 메모리의 `runFlag` 값만 `false` 로 변경된다.

`work` 스레드가 사용하는 CPU 코어2의 캐시 메모리의 `runFlag` 값은 여전히 `true` 이다.

`work` 스레드의 경우 `while(runFlag[true])` 가 만족하기 때문에 while문을 계속 반복해서 수행한다.

<img width="642" alt="Screenshot 2024-10-30 at 23 38 55" src="https://github.com/user-attachments/assets/d0acd3e9-5898-44e3-84e6-2e22a9248017">

캐시 메모리에 있는 `runFlag` 의 값이 언제 메인 메모리에 반영될까?

이 부분에 대한 정답은 "알 수 없다"이다. CPU 설계 방식과 종류의 따라 다르다. 극단적으로 보면 평생 반영되지 않을 수도 있다!

메인 메모리에 반영을 한다고 해도, 문제는 여기서 끝이 아니다.

메인 메모리에 반영된 `runFlag` 값을 `work` 스레드가 사용하는 캐시 메모리에 다시 불러와야 한다.

<img width="544" alt="Screenshot 2024-10-30 at 23 39 27" src="https://github.com/user-attachments/assets/fb7a24cc-13d2-45ab-8535-dac62f2a22f6">

메인 메모리에 변경된 `runFlag` 값이 언제 CPU 코어2의 캐시 메모리에 반영될까?

이 부분에 대한 정답도 "알 수 없다"이다. CPU 설계 방식과 종류의 따라 다르다. 극단적으로 보면 평생 반영되지 않을 수도 있다!

언젠가 CPU 코어2의 캐시 메모리에 `runFlag` 값을 불러오게 되면 `work` 스레드가 확인하는 `runFlag` 의 값이 `false` 가 되므로 while문을 탈출하고, `"task 종료"` 를 출력한다.

캐시 메모리를 메인 메모리에 반영하거나, 메인 메모리의 변경 내역을 캐시 메모리에 다시 불러오는 것은 언제 발생할까?

이 부분은 CPU 설계 방식과 실행 환경에 따라 다를 수 있다. 

즉시 반영될 수도 있고, 몇 밀리초 후에 될 수도 있고, 몇 초 후에 될 수도 있고, 평생 반영되지 않을 수도 있다.

주로 컨텍스트 스위칭이 될 때, 캐시 메모리도 함께 갱신되는데, 이 부분도 환경에 따라 달라질 수 있다.

예를 들어 `Thread.sleep()` 이나 콘솔에 내용을 출력할 때 스레드가 잠시 쉬는데, 이럴 때 컨텍스트 스위칭이 되면서 주로 갱신된다. 

하지만 이것이 갱신을 보장하는 것은 아니다.

**메모리 가시성(memory visibility)**

이처럼 멀티스레드 환경에서 한 스레드가 변경한 값이 다른 스레드에서 언제 보이는지에 대한 문제를 메모리 가시성 (memory visibility)이라 한다. 

이름 그대로 메모리에 변경한 값이 보이는가, 보이지 않는가의 문제이다.

그렇다면 한 스레드에서 변경한 값이 다른 스레드에서 즉시 보이게 하려면 어떻게 해야할까?



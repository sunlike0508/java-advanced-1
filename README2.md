# 스레드 제어와 생명 주기1

```java
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
```

```shell
15:15:48.368 [     main] mainThread = Thread[#1,main,5,main]
15:15:48.373 [     main] mainThread = 1
15:15:48.373 [     main] mainThread = main
15:15:48.375 [     main] mainThread = 5
15:15:48.375 [     main] mainThread = java.lang.ThreadGroup[name=main,maxpri=10]
15:15:48.376 [     main] mainThread = RUNNABLE
15:15:48.376 [     main] myThread = Thread[#23,myThread,5,main]
15:15:48.376 [     main] myThread = 23
15:15:48.376 [     main] myThread = myThread
15:15:48.377 [     main] myThread = 5
15:15:48.377 [     main] myThread = java.lang.ThreadGroup[name=main,maxpri=10]
15:15:48.377 [     main] myThread = NEW
```

1. 스레드 생성
   스레드를 생성할 때는 실행할 `Runnable` 인터페이스의 구현체와, 스레드의 이름을 전달할 수 있다.
```java
Thread myThread = new Thread(new HelloRunnable(), "myThread"); 
```

**Runnable 인터페이스**: 실행할 작업을 포함하는 인터페이스이다. `HelloRunnable` 클래스는 `Runnable` 인 터페이스를 구현한 클래스이다.

**스레드 이름**: `"myThread"` 라는 이름으로 스레드를 생성한다. 이 이름은 디버깅이나 로깅 목적으로 유용하다. 참고로 이름을 생략하면 `Thread-0` , `Thread-1` 과 같은 임의의 이름이 생성된다.

2. 스레드 객체 정보

```
log("myThread = " + myThread); 
```

`myThread` 객체를 문자열로 변환하여 출력한다. `Thread` 클래스의 `toString()` 메서드는 스레드 ID, 스레 드 이름, 우선순위, 스레드 그룹을 포함하는 문자열을 반환한다.


3. 스레드 ID 

```java
log("myThread.threadId() = " + myThread.threadId()); 
```

**threadId()**: 스레드의 고유 식별자를 반환하는 메서드이다. 이 ID는 JVM 내에서 각 스레드에 대해 유일하다. ID는 스레드가 생성될 때 할당되며, 직접 지정할 수 없다.

4. 스레드 이름 

```java
log("myThread.getName() = " + myThread.getName());
```

**getName()**: 스레드의 이름을 반환하는 메서드이다. 

생성자에서 `"myThread"` 라는 이름을 지정했기 때문에, 이 값이 반환된다. 참고로 스레드 ID는 중복되지 않지만, 스레드 이름은 중복될 수 있다.

5. 스레드 우선순위 

```java
log("myThread.getPriority() = " + myThread.getPriority()); 
```

**getPriority()**: 스레드의 우선순위를 반환하는 메서드이다. 우선순위는 1 (가장 낮음)에서 10 (가장 높음)까지의 값으로 설정할 수 있으며, 기본값은 5이다. `setPriority()` 메서드를 사용해서 우선순위를 변경할 수 있다. 

우선순위는 스레드 스케줄러가 어떤 스레드를 우선 실행할지 결정하는 데 사용된다. 하지만 실제 실행 순서는 JVM 구현과 운영체제에 따라 달라질 수 있다.

6. 스레드 그룹

```java
log("myThread.getThreadGroup() = " + myThread.getThreadGroup());
```

**getThreadGroup()**: 스레드가 속한 스레드 그룹을 반환하는 메서드이다. 스레드 그룹은 스레드를 그룹화하여 관리할 수 있는 기능을 제공한다. 

기본적으로 모든 스레드는 부모 스레드와 동일한 스레드 그룹에 속하게 된다. 

스레드 그룹은 여러 스레드를 하나의 그룹으로 묶어서 특정 작업(예: 일괄 종료, 우선순위 설정 등)을 수행할 수 있다.

**부모 스레드(Parent Thread)**: 

새로운 스레드를 생성하는 스레드를 의미한다. 

스레드는 기본적으로 다른 스레드 에 의해 생성된다. 이러한 생성 관계에서 새로 생성된 스레드는 생성한 스레드를 **부모**로 간주한다. 

예를 들어 `myThread` 는 `main` 스레드에 의해 생성되었으므로 `main` 스레드가 부모 스레드이다.

`main` 스레드는 기본으로 제공되는 `main` 스레드 그룹에 소속되어 있다. 따라서 `myThread` 도 부모 스레드인 `main` 스레드의 그룹인 `main` 스레드 그룹에 소속된다.

**참고**: 스레드 그룹 기능은 직접적으로 잘 사용하지는 않기 때문에, 이런 것이 있구나 정도만 알고 넘어가자

7. 스레드 상태 

```java
log("myThread.getState() = " + myThread.getState()); 
```

**getState()**: 스레드의 현재 상태를 반환하는 메서드이다. 반환되는 값은 `Thread.State` 열거형에 정의된 상 수 중 하나이다. 주요 상태는 다음과 같다.

* **NEW**: 스레드가 아직 시작되지 않은 상태이다.

* **RUNNABLE**: 스레드가 실행 중이거나 실행될 준비가 된 상태이다. 

* **BLOCKED**: 스레드가 동기화 락을 기다리는 상태이다.

* **WAITING**: 스레드가 다른 스레드의 특정 작업이 완료되기를 기다리는 상태이다. 

* **TIMED_WAITING**: 일정 시간 동안 기다리는 상태이다.

* **TERMINATED**: 스레드가 실행을 마친 상태이다.

출력 결과를 보면 `main` 스레드는 실행 중이기 때문에 `RUNNABLE` 상태이다. `myThread` 는 생성하고 아직 시작하지 않았기 때문에, `NEW` 상태이다.

## 스레드의 생명 주기 - 설명

<img width="710" alt="Screenshot 2024-10-27 at 15 24 26" src="https://github.com/user-attachments/assets/5452afe3-8dbd-4f89-a96e-555e4e0fef5e">

**스레드의 상태**

* **New (새로운 상태)**: 스레드가 생성되었으나 아직 시작되지 않은 상태.
* **Runnable (실행 가능 상태)**: 스레드가 실행 중이거나 실행될 준비가 된 상태.
* **일시 중지 상태들 (Suspended States)**
  * **Blocked (차단 상태)**: 스레드가 동기화 락을 기다리는 상태.
  * **Waiting (대기 상태)**: 스레드가 무기한으로 다른 스레드의 작업을 기다리는 상태.
  * **Timed Waiting (시간 제한 대기 상태)**: 스레드가 일정 시간 동안 다른 스레드의 작업을 기다리는 상태.
* **Terminated (종료 상태)**: 스레드의 실행이 완료된 상태.

**참고**: 자바에서 스레드의 **일시 중지 상태들(Suspended States)** 이라는 상태는 없다. 스레드가 기다리는 상태들
을 묶어서 쉽게 설명하기 위해 사용한 용어이다.

자바 스레드(Thread)의 생명 주기는 여러 상태(state)로 나뉘어지며, 각 상태는 스레드가 실행되고 종료되기까지의 과정을 나타낸다.

자바 스레드의 생명 주기를 자세히 알아보자.

1. **New (새로운 상태)**

스레드가 생성되고 아직 시작되지 않은 상태이다. 
 
이 상태에서는 `Thread` 객체가 생성되지만, `start()` 메서드가 호출되지 않은 상태이다. 

예: `Thread thread = new Thread(runnable);`

2. **Runnable (실행 가능 상태)**

스레드가 실행될 준비가 된 상태이다. 이 상태에서 스레드는 실제로 CPU에서 실행될 수 있다.

`start()` 메서드가 호출되면 스레드는 이 상태로 들어간다.

예: `thread.start();`

이 상태는 스레드가 실행될 준비가 되어 있음을 나타내며, 실제로 CPU에서 실행될 수 있는 상태이다. 

그러나 Runnable 상태에 있는 모든 스레드가 동시에 실행되는 것은 아니다. 

운영체제의 스케줄러가 각 스레드에 CPU 시간을 할당하여 실행하기 때문에, Runnable 상태에 있는 스레드는 스케줄러의 실행 대기열에 포함되어 있다가 차례로 CPU에서 실행된다.

참고로 운영체제 스케줄러의 실행 대기열에 있든, CPU에서 실제 실행되고 있든 모두 `RUNNABLE` 상태이다. 

자바에서 둘을 구분해서 확인할 수는 없다.

보통 실행 상태라고 부른다.

3. **Blocked (차단 상태)**
 
스레드가 다른 스레드에 의해 동기화 락을 얻기 위해 기다리는 상태이다.

예를 들어, `synchronized` 블록에 진입하기 위해 락을 얻어야 하는 경우 이 상태에 들어간다.

예: `synchronized (lock) { ... }` 코드 블록에 진입하려고 할 때, 다른 스레드가 이미 `lock` 의 락을 가지고 있는 경우.

지금은 이런 상태가 있다 정도만 알아두자. 이 부분은 뒤에서 자세히 다룬다.

4. **Waiting (대기 상태)**

스레드가 다른 스레드의 특정 작업이 완료되기를 무기한 기다리는 상태이다.

`wait()` , `join()` 메서드가 호출될 때 이 상태가 된다.

스레드는 다른 스레드가 `notify()` 또는 `notifyAll()` 메서드를 호출하거나, `join()` 이 완료될 때까지 기다린다.

예: `object.wait();`

지금은 이런 상태가 있다 정도만 알아두자. 이 부분은 뒤에서 자세히 다룬다.

5. **Timed Waiting (시간 제한 대기 상태)**

스레드가 특정 시간 동안 다른 스레드의 작업이 완료되기를 기다리는 상태이다.

`sleep(long millis)` , `wait(long timeout)` , `join(long millis)` 메서드가 호출될 때 이 상태가 된다.

주어진 시간이 경과하거나 다른 스레드가 해당 스레드를 깨우면 이 상태에서 벗어난다.

예: `Thread.sleep(1000);`

지금은 이런 상태가 있다 정도만 알아두자. 이 부분은 뒤에서 자세히 다룬다.

6. **Terminated (종료 상태)**

스레드의 실행이 완료된 상태이다.

스레드가 정상적으로 종료되거나, 예외가 발생하여 종료된 경우 이 상태로 들어간다. 스레드는 한 번 종료되면 다시 시작할 수 없다.

**자바 스레드의 상태 전이 과정**

1. **New → Runnable**: `start()` 메서드를 호출하면 스레드가 `Runnable` 상태로 전이된다.
2. **Runnable → Blocked/Waiting/Timed Waiting**: 스레드가 락을 얻지 못하거나, `wait()` 또는 `sleep()` 메서드를 호출할 때 해당 상태로 전이된다.
3. **Blocked/Waiting/Timed Waiting → Runnable**: 스레드가 락을 얻거나, 기다림이 완료되면 다시 `Runnable` 상태로 돌아간다.
4. **Runnable → Terminated**: 스레드의 `run()` 메서드가 완료되면 스레드는 `Terminated` 상태가 된다.

```java
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
```

```shell
15:47:21.534 [     main] myThread.state1 = NEW
15:47:21.536 [     main] myThread.start()
15:47:21.537 [ myThread] start
15:47:21.537 [ myThread] myThread.state2 = RUNNABLE
15:47:21.537 [ myThread] sleep() start
15:47:22.542 [     main] myThread.state3 = TIMED_WAITING
15:47:24.542 [ myThread] sleep() end
15:47:24.544 [ myThread] myThread.state4 = RUNNABLE
15:47:24.544 [ myThread] end
15:47:26.545 [     main] myThread.state5 = TERMINATED
15:47:26.546 [     main] myThread.end()
```

<img width="699" alt="Screenshot 2024-10-27 at 15 48 41" src="https://github.com/user-attachments/assets/26961c86-85e7-4a16-801b-a4f0f2f8fb38">

<img width="684" alt="Screenshot 2024-10-27 at 15 48 52" src="https://github.com/user-attachments/assets/08788dc2-3917-44fd-9f8d-4fc7ca16a7e6">

## 체크 예외 재정의

`Runnable` 인터페이스의 `run()` 메서드를 구현할 때 `InterruptedException` 체크 예외를 밖으로 던질 수 없는 이유를 알아보자.

`Runnable` 인터페이스는 다음과 같이 정의되어 있다. 

```java
public interface Runnable {
    void run();
}
```

자바에서 메서드를 재정의 할 때, 재정의 메서드가 지켜야할 예외와 관련된 규칙이 있다. 

**체크 예외**

부모 메서드가 체크 예외를 던지지 않는 경우, 재정의된 자식 메서드도 체크 예외를 던질 수 없다.

자식 메서드는 부모 메서드가 던질 수 있는 체크 예외의 하위 타입만 던질 수 있다.

**언체크(런타임) 예외**

예외 처리를 강제하지 않으므로 상관없이 던질 수 있다.

`Runnable` 인터페이스의 `run()` 메서드는 아무런 체크 예외를 던지지 않는다. 

따라서 `Runnable` 인터페이스의 `run()` 메서드를 재정의 하는 곳에서는 체크 예외를 밖으로 던질 수 없다. 

다음 코드를 실행하면 컴파일 오류가 발생한다.

```java
public class CheckedException {

    public static void main(String[] args) throws Exception {
        throw new Exception();
    }

    static class CheckedRunnable implements Runnable {


        @Override
        public void run() throw Exception{ // 오류 발생
            throw new Exception(); // 오류 발생
        }
    }
}
```

자바는 왜 이런 제약을 두는 것일까?

부모 클래스의 메서드를 호출하는 클라이언트 코드는 부모 메서드가 던지는 특정 예외만을 처리하도록 작성된다. 

자식 클래스가 더 넓은 범위의 예외를 던지면 해당 코드는 모든 예외를 제대로 처리하지 못할 수 있다. 

이는 예외 처리의 일관성을 해치고, 예상하지 못한 런타임 오류를 초래할 수 있다.

**체크 예외 재정의 규칙**

자식 클래스에 재정의된 메서드는 부모 메서드가 던질 수 있는 체크 예외의 하위 타입만을 던질 수 있다. 

원래 메서드가 체크 예외를 던지지 않는 경우, 재정의된 메서드도 체크 예외를 던질 수 없다.

**안전한 예외 처리**

체크 예외를 `run()` 메서드에서 던질 수 없도록 강제함으로써, 개발자는 반드시 체크 예외를 try-catch 블록 내에서 처리하게 된다. 

이는 예외 발생 시 예외가 적절히 처리되지 않아서 프로그램이 비정상 종료되는 상황을 방지할 수 있다. 

특히 멀티스레딩 환경에서는 예외 처리를 강제함으로써 스레드의 안정성과 일관성을 유지할 수 있다.

하지만 이전에 자바 예외 처리 강의에서 설명했듯이, 체크 예외를 강제하는 이런 부분들은 자바 초창기 기조이고, 최근에는 체크 예외보다는 언체크(런타임) 예외를 선호한다.


## join

**Waiting (대기 상태)**

스레드가 다른 스레드의 특정 작업이 완료되기를 무기한 기다리는 상태이다.

```java
public class JoinMainV0 {

    public static void main(String[] args) {
        log("start");
        Thread thread1 = new Thread(new Job(), "thread-1");
        Thread thread2 = new Thread(new Job(), "thread-2");
        thread1.start();
        thread2.start();
        log("end");
    }

    static class Job implements Runnable {
        @Override
        public void run() {
            log("작업시작");
            ThreadUtils.sleep(2000);
            log("작업완료");
        }
    }
}
```

```shell
16:16:21.412 [     main] start
16:16:21.415 [     main] end
16:16:21.415 [ thread-1] 작업시작
16:16:21.415 [ thread-2] 작업시작
16:16:23.417 [ thread-2] 작업완료
16:16:23.421 [ thread-1] 작업완료
```

실행 결과를 보면 `main` 스레드가 먼저 종료되고, 그 다음에 `thread-1` , `thread-2` 가 종료된다.

`main` 스레드는 `thread-1` , `thread-2` 를 실행하고 바로 자신의 다음 코드를 실행한다. 

여기서 핵심은 `main` 스레 드가 `thread-1` , `thread-2` 가 끝날때까지 기다리지 않는다는 점이다. 

`main` 스레드는 단지 `start()` 를 호출해서 다른 스레드를 실행만 하고 바로 자신의 다음 코드를 실행한다.

그런데 만약 `thread-1` , `thread-2` 가 종료된 다음에 `main` 스레드를 가장 마지막에 종료하려면 어떻게 해야할까? 

예를 들어서 `main` 스레드가 `thread-1` , `thread-2` 에 각각 어떤 작업을 지시하고, 그 결과를 받아서 처리하고 싶다면 어떻게 해야할까?


## join이 필요한 상황

```java
public class JoinMainV1 {

    public static void main(String[] args) {
        log("start");
        SumTask task1 = new SumTask(1, 50);
        SumTask task2 = new SumTask(51, 100);

        Thread t1 = new Thread(task1, "thread-1");
        Thread t2 = new Thread(task2, "thread-2");

        t1.start();
        t2.start();

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
```

```shell
16:57:21.344 [     main] start
16:57:21.345 [ thread-1] 작업시작
16:57:21.345 [ thread-2] 작업시작
16:57:21.349 [     main] task1.result = 0
16:57:21.349 [     main] task2.result = 0
16:57:21.349 [     main] sumAll = 0
16:57:21.349 [     main] end
16:57:23.348 [ thread-2] 작업완료 result : 3775
16:57:23.351 [ thread-1] 작업완료 result : 1275
```

<img width="686" alt="Screenshot 2024-10-27 at 16 58 36" src="https://github.com/user-attachments/assets/92068138-151d-4eac-965c-37e615db428a">

`main` 스레드는 `thread-1` , `thread2` 에 작업을 지시하고, `thread-1` , `thread2` 가 계산을 완료하기도 전에 먼저 계산 결과를 조회했다. 

참고로 `thread-1` , `thread-2` 가 계산을 완료하는데는 2초 정도의 시간이 걸린다. 따라서 결과가 `task1 + task2 = 0` 으로 출력된다.

<img width="679" alt="Screenshot 2024-10-27 at 16 58 41" src="https://github.com/user-attachments/assets/4bfbf008-4aef-4f24-a720-3bd103d07fd9">


프로그램이 처음 시작되면 `main` 스레드는 `thread-1` , `thread-2` 를 생성하고 `start()` 로 실행한다. 

`thread-1` , `thread-2` 는 각각 자신에게 전달된 `SumTask` 인스턴스의 `run()` 메서드를 스택에 올리고 실행한다.

`thread-1` 은 `x001` 인스턴스의 `run()` 메서드를 실행한다. `thread-2` 는 `x002` 인스턴스의 `run()` 메서드를 실행한다.

<img width="686" alt="Screenshot 2024-10-27 at 16 59 07" src="https://github.com/user-attachments/assets/8eb18f27-6177-44fb-8420-4c041d93bfb2">

`main` 스레드는 두 스레드를 시작한 다음에 바로 `task1.result` , `task2.result` 를 통해 인스턴스에 있는 결과 값을 조회한다. 

참고로 `main` 스레드가 실행한 `start()` 메서드는 스레드의 실행이 끝날 때 까지 기다리지 않는다! 

다른 스레드를 실행만 해두고, 자신의 다음 코드를 실행할 뿐이다!

`thread-1` , `thread-2` 가 계산을 완료해서, `result` 에 연산 결과를 담을 때 까지는 약 2초 정도의 시간이 걸린다. 

`main` 스레드는 계산이 끝나기 전에 `result` 의 결과를 조회한 것이다. 따라서 `0` 값이 출력된다.

<img width="690" alt="Screenshot 2024-10-27 at 16 59 18" src="https://github.com/user-attachments/assets/0a45b3d6-f3a8-4dab-9203-7319b4b16f48">

2초가 지난 이후에 `thread-1` , `thread-2` 는 계산을 완료한다.

이때 `main` 스레드는 이미 자신의 코드를 모두 실행하고 종료된 상태이다.

`task1` 인스턴스의 `result` 에는 `1275` 가 담겨있고, `task2` 인스턴스의 `result` 에는 `3775` 가 담겨있다. 

여기서 문제의 핵심은 `main` 스레드가 `thread-1` , `thread-2` 의 계산이 끝날 때 까지 기다려야 한다는 점이다. 

그럼 어떻게 해야 `main` 스레드가 기다릴 수 있을까?

### 참고 - this의 비밀

어떤 메서드를 호출하는 것은, 정확히는 특정 스레드가 어떤 메서드를 호출하는 것이다.

스레드는 메서드의 호출을 관리하기 위해 메서드 단위로 스택 프레임을 만들고 해당 스택 프레임을 스택위에 쌓아 올린다.

이때 인스턴스의 메서드를 호출하면, 어떤 인스턴스의 메서드를 호출했는지 기억하기 위해, 해당 인스턴스의 참조값을 스택 프레임 내부에 저장해둔다. 

이것이 바로 우리가 자주 사용하던 `this` 이다.

특정 메서드 안에서 `this` 를 호출하면 바로 스택프레임 안에 있는 `this` 값을 불러서 사용하게 된다.

그림을 보면 스택 프레임 안에 있는 `this` 를 확인할 수 있다. 

이렇게 `this` 가 있기 때문에 `thread-1` , `thread-2` 는 자신의 인스턴스를 구분해서 사용할 수 있다. 

예를 들어서 필드에 접근할 때 `this` 를 생략하면 자동으로 `this` 를 참고해서 필드에 접근한다.

정리하면 `this` 는 호출된 인스턴스 메서드가 소속된 객체를 가리키는 참조이며, 이것이 스택 프레임 내부에 저장되어 있다.

## join - sleep 사용


```java
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
```

하지만 이렇게 `sleep()` 을 사용해서 무작정? 기다리는 방법은 대기 시간에 손해도 보고, 또 `thread-1` , `thread-2` 의 수행시간이 달라지는 경우에는 정확한 타이밍을 맞추기 어렵다.

더 나은 방법은 `thread-1` , `thread-2` 가 계산을 끝내고 종료될 때 까지 `main` 스레드가 기다리는 방법이다.

예를 들어서 `main` 스레드가 반복문을 사용해서 `thread-1` , `thread-2` 의 상태가 `TERMINATED` 가 될 때 까지 계속 확인하는 방법이 있다.



```java
while(thread.getState() != TERMINATED) { //스레드의 상태가 종료될 때 까지 계속 반복
}
//계산 결과 출력 
```

<img width="694" alt="Screenshot 2024-10-27 at 17 16 50" src="https://github.com/user-attachments/assets/02de6a5a-ae12-439f-a66e-d9d89e9e46a9">

이런 방법은 번거롭고 또 계속되는 반복문은 CPU 연산을 사용한다. 

이때 `join()` 메서드를 사용하면 깔끔하게 문제를 해결할 수 있다.

## join - join 사용

```java
public static void main(String[] args) throws InterruptedException {
    log("start");
    SumTask task1 = new SumTask(1, 50);
    SumTask task2 = new SumTask(51, 100);
  
    Thread t1 = new Thread(task1, "thread-1");
    Thread t2 = new Thread(task2, "thread-2");
  
    t1.start();
    t2.start();
  
    t1.join();
    t2.join();
  
    log("join() - main 쓰레드가 thread 1,2 종료까지 대기");
  
    log("task1.result = " + task1.result);
    log("task2.result = " + task2.result);
  
    int sumAll = task1.result + task2.result;
  
    log("sumAll = " + sumAll);
    log("end");
}
```

<img width="694" alt="Screenshot 2024-10-27 at 17 19 27" src="https://github.com/user-attachments/assets/375252a2-bb02-461a-82a1-01b937cbe5a1">

`main` 스레드에서 다음 코드를 실행하게 되면 `main` 스레드는 `thread-1` , `thread-2` 가 종료될 때 까지 기다린다. 

이때 `main` 스레드는 `WAITING` 상태가 된다.

```java
 thread1.join();
 thread2.join();
```

예를 들어서 `thread-1` 이 아직 종료되지 않았다면 `main` 스레드는 `thread1.join()` 코드 안에서 더는 진행하지 않고 멈추어 기다린다. 

이후에 `thread-1` 이 종료되면 `main` 스레드는 `RUNNABLE` 상태가 되고 다음 코드로 이동한다.

이때 `thread-2` 이 아직 종료되지 않았다면 `main` 스레드는 `thread2.join()` 코드 안에서 진행하지 않고 멈추어 기다린다. 

이후에 `thread-2` 이 종료되면 `main` 스레드는 `RUNNABLE` 상태가 되고 다음 코드로 이동한다.

이 경우 `thread-1` 이 종료되는 시점에 `thread-2` 도 거의 같이 종료되기 때문에 `thread2.join()` 은 대기하지 않고 바로 빠져나온다.

**Waiting (대기 상태)**

스레드가 다른 스레드의 특정 작업이 완료되기를 무기한 기다리는 상태이다.

`join()` 을 호출하는 스레드는 대상 스레드가 `TERMINATED` 상태가 될 때 까지 대기한다. 

대상 스레드가 `TERMINATED` 상태가 되면 호출 스레드는 다시 `RUNNABLE` 상태가 되면서 다음 코드를 수행한다.

이렇듯 특정 스레드가 완료될 때 까지 기다려야 하는 상황이라면 `join()` 을 사용하면 된다.

하지만 `join()` 의 단점은 다른 스레드가 완료될 때 까지 무기한 기다리는 단점이있다. 

만약 다른 스레드의 작업을 일정 시간 동안만 기다리고 싶다면 어떻게 해야할까?

## join - 특정시간 만큼만 대기

`join()` 은 두 가지 메서드가 있다.

`join()` : 호출 스레드는 대상 스레드가 완료될 때 까지 무한정 대기한다.

`join(ms)` : 호출 스레드는 특정 시간 만큼만 대기한다. 호출 스레드는 지정한 시간이 지나면 다시 `RUNNABLE` 상태가 되면서 다음 코드를 수행한다.

```java
public static void main(String[] args) throws InterruptedException {
    log("start");
    SumTask task1 = new SumTask(1, 50);

    Thread t1 = new Thread(task1, "thread-1");
    t1.start();

    log("join(1000) - main 쓰레드가 thread 1종료까지 대기");
    t1.join(1000);
    log("task1.result = " + task1.result);
    log("end");
}
```

```shell
17:29:05.033 [     main] start
17:29:05.035 [     main] join(1000) - main 쓰레드가 thread 1종료까지 대기
17:29:05.035 [ thread-1] 작업시작
17:29:06.044 [     main] task1.result = 0
17:29:06.045 [     main] end
17:29:07.042 [ thread-1] 작업완료 result : 1275
```

<img width="701" alt="Screenshot 2024-10-27 at 17 22 52" src="https://github.com/user-attachments/assets/5fc07dd1-4bbf-44ff-a1af-bc6cd27979ab">

* `main` 스레드는 `join(1000)` 을 사용해서 `thread-1` 을 1초간 기다린다. 
  * 이때 `main` 스레드의 상태는 `WAITING` 이 아니라 `TIMED_WAITING` 이 된다. 
  * 보통 무기한 대기하면 `WAITING` 상태가 되고, 특정 시간 만큼만 대기하는 경우 `TIMED_WAITING` 상태가 된다.
* `thread-1` 의 작업에는 2초가 걸린다.
* 1초가 지나도 `thread-1` 의 작업이 완료되지 않으므로, `main` 스레드는 대기를 중단한다. 그리고 `main` 스레 드는 다시 `RUNNABLE` 상태로 바뀌면서 다음 코드를 수행한다. 
  * 이때 `thread-1` 의 작업이 아직 완료되지 않았기 때문에 `task1.result = 0` 이 출력된다.
* `main` 스레드가 종료된 이후에 `thread-1` 이 계산을 끝낸다. 따라서 `작업 완료 result = 1275` 이 출력된다.

**정리**

다른 스레드가 끝날 때 까지 무한정 기다려야 한다면 `join()` 을 사용하고, 다른 스레드의 작업을 무한정 기다릴 수 없다면 `join(ms)` 를 사용하면 된다. 

물론 기다리다 중간에 나오는 상황인데, 결과가 없다면 추가적인 오류 처리가 필요 할 수 있다.

# 쓰레드 제어와 생명주기2

## 인터럽트 - 시작1

특정 스레드의 작업을 중간에 중단하려면 어떻게 해야할까?

```java
public class ThreadStopMainV1 {

    public static void main(String[] args) {
        MyTask myTask = new MyTask();
        Thread thread = new Thread(myTask, "work");
        thread.start();

        sleep(4000);
        log("작업 중단 지시");
        myTask.runFlag = false;
    }

    static class MyTask implements Runnable {

        volatile boolean runFlag = true;

        @Override
        public void run() {
            while (runFlag) {
                log("작업중");
                sleep(3000);
            }

            log("자원정리");
            log("작업종료");
        }
    }
}
```
<img width="687" alt="Screenshot 2024-10-28 at 22 12 35" src="https://github.com/user-attachments/assets/85539536-b465-40f3-9056-de0e31deaaf0">

`work` 스레드는 `runFlag` 가 `true` 인 동안 계속 실행된다.

<img width="691" alt="Screenshot 2024-10-28 at 22 12 56" src="https://github.com/user-attachments/assets/026d4381-d36d-42df-9621-72bfb2d50f76">

프로그램 시작 후 4초 뒤에 `main` 스레드는 `runFlag` 를 `false` 로 변경한다.

`work` 스레드는 `while(runFlag)` 에서 `runFlag` 의 조건이 `false` 로 변한 것을 확인하고, while문을 빠져 나가면서 작업을 종료한다.

**문제점**

실행을 해보면 알겠지만 `main` 스레드가 `runFlag=false` 를 통해 작업 중단을 지시해도, `work` 스레드가 즉각 반응하지 않는다. 

로그를 보면 작업 중단 지시 2초 정도 이후에 자원을 정리하고 작업을 종료한다. 

```shell
22:12:16.605 [     work] 작업중
22:12:19.612 [     work] 작업중
22:12:20.596 [     main] 작업 중단 지시
22:12:22.619 [     work] 자원정리 //2초 정도 경과후 실행 work]
22:12:22.620 [     work] 작업종료
```

이 방식의 가장 큰 문제는 다음 코드의 `sleep()` 에 있다. 

```java
while (runFlag) { 
    log("작업 중");
    sleep(3000);
}
```

`main` 스레드가 `runFlag` 를 `false` 로 변경해도, `work` 스레드는 `sleep(3000)` 을 통해 3초간 잠들어 있다. 

3초간의 잠이 깬 다음에 `while(runFlag)` 코드를 실행해야, `runFlag` 를 확인하고 작업을 중단할 수 있다.

참고로 `runFlag` 를 변경한 후 2초라는 시간이 지난 이후에 작업이 종료되는 이유는 `work` 스레드가 3초에 한 번씩 깨어나서 `runFlag` 를 확인하는데, `main` 스레드가 4초에 `runFlag` 를 변경했기 때문이다.

`work` 스레드 입장에서 보면 두 번째 `sleep()` 에 들어가고 1초 후 `main` 스레드가 `runFlag` 를 변경한다. 

3초간 `sleep()` 이므로 아직 2초가 더 있어야 깨어난다.

어떻게 하면 `sleep()` 처럼 스레드가 대기하는 상태에서 스레드를 깨우고, 작업도 빨리 종료할 수 있을까?

## 인터럽트 - 시작2

예를 들어서, 특정 스레드가 `Thread.sleep()` 을 통해 쉬고 있는데, 처리해야 하는 작업이 들어와서 해당 스레드를 급하게 깨워야 할 수 있다. 

또는 `sleep()` 으로 쉬고 있는 스레드에게 더는 일이 없으니, 작업 종료를 지시 할 수도 있다. 

인터럽트를 사용하면, `WAITING` , `TIMED_WAITING` 같은 대기 상태의 스레드를 직접 깨워서, 작동하는 `RUNNABLE`상태로 만들 수 있다.

앞서 작성한 예제의 작업 중단 지시를 인터럽트를 통해 처리해보자.

```java
public class ThreadStopMainV2 {

  public static void main(String[] args) {
    MyTask myTask = new MyTask();
    Thread thread = new Thread(myTask, "work");
    thread.start();

    sleep(4000);
    log("작업 중단 지시");
    thread.interrupt();
    log("work 쓰레드 인터럽트 상태1 = " + thread.isInterrupted());
  }

  static class MyTask implements Runnable {

    @Override
    public void run() {
      try {
        while (true) {
          log("작업중");
          Thread.sleep(3000);
        }
      } catch(InterruptedException e) {
        log("work 쓰레드 인터럽트 상태2 = " + Thread.currentThread().isInterrupted());
        log("message =" + e.getMessage());
        log("state = " + Thread.currentThread().getState());
      }

      log("자원정리");
      log("작업종료");
    }
  }
}
```

```shell
22:29:57.691 [     work] 작업중
22:30:00.698 [     work] 작업중
22:30:01.680 [     main] 작업 중단 지시
22:30:01.691 [     main] work 쓰레드 인터럽트 상태1 = true
22:30:01.691 [     work] work 쓰레드 인터럽트 상태2 = false
22:30:01.692 [     work] message = sleep interrupted
22:30:01.693 [     work] state = RUNNABLE
22:30:01.693 [     work] 자원정리
22:30:01.693 [     work] 작업종료
```

특정 스레드의 인스턴스에 `interrupt()` 메서드를 호출하면, 해당 스레드에 인터럽트가 발생한다. 

인터럽트가 발생하면 해당 스레드에 `InterruptedException` 이 발생한다.

이때 인터럽트를 받은 스레드는 대기 상태에서 깨어나 `RUNNABLE` 상태가 되고, 코드를 정상 수행한다.

이때 `InterruptedException` 을 `catch` 로 잡아서 정상 흐름으로 변경하면 된다.

***참고로 `interrupt()` 를 호출했다고 해서 즉각 `InterruptedException` 이 발생하는 것은 아니다.***

***오직 `sleep()` 처럼 `InterruptedException` 을 던지는 메서드를 호출 하거나 또는 호출 중일 때 예외가 발생한다.***

(이건 몰랐던 사실이네)

예를 들어서 위 코드에서 `while(true)` , `log("작업 중")` 에서는 `InterruptedException` 이 발생하지 않는다.

`Thread.sleep()` 처럼 `InterruptedException` 을 던지는 메서드를 호출하거나 또는 호출하며 대기중일 때 예외가 발생한다.

<img width="704" alt="Screenshot 2024-10-28 at 22 32 17" src="https://github.com/user-attachments/assets/aadb0fa4-8054-41ff-ac13-8c41108e4950">

`main` 스레드가 4초 뒤에 `work` 스레드에 `interrupt()` 를 건다.

`work` 스레드는 인터럽트 상태(`true` )가 된다.

스레드가 인터럽트 상태일 때는, `sleep()` 처럼 `InterruptedException` 이 발생하는 메서드를 호출하거나 또는 이미 호출하고 대기 중이라면  `InterruptedException` 이 발생한다.

이때 2가지 일이 발생한다.

`work` 스레드는 `TIMED_WAITING` 상태에서 `RUNNABLE` 상태로 변경되고, `InterruptedException` 예외를 처리하면서 반복문을 탈출한다.

`work` 스레드는 인터럽트 상태가 되었고, 인터럽트 상태이기 때문에 인터럽트 예외가 발생한다.

인터럽트 상태에서 인터럽트 예외가 발생하면 `work` 스레드는 다시 작동하는 상태가 된다. 

따라서 `work` 스레드의 인터럽트 상태는 종료된다.

`work` 스레드의 인터럽트 상태는 `false` 로 변경된다.

## 인터럽트 - 시작3

그런데 앞선 코드에서 한가지 아쉬운 부분이 있다.

```java
while (true) { //인터럽트 체크 안함 log("작업 중");
    Thread.sleep(3000); //여기서만 인터럽트 발생 
}
```
여기서 `while(true)` 부분은 체크를 하지 않는다는 점이다. 

인터럽트가 발생해도 이 부분은 항상 `true` 이기 때문에 다음 코드로 넘어간다. 

그리고 `sleep()` 을 호출하고 나서야 인터럽트가 발생하는 것이다.

다음과 같이 인터럽트의 상태를 확인하면, 더 빨리 반응할 수 있을 것이다. 

```java
while (인터럽트_상태_확인) { //여기서도 인터럽트 상태 체크 log("작업 중");
Thread.sleep(3000); //인터럽트 발생
}
```

이 코드와 같이 인터럽트의 상태를 확인하면 while문을 체크하는 부분에서 더 빠르게 while문을 빠져나갈 수 있다. 

물론 이 예제의 경우 코드가 단순해서 실질적인 차이는 매우 작다.

추가로 인터럽트의 상태를 직접 확인하면, 다음과 같이 인터럽트를 발생 시키는 `sleep()` 과 같은 코드가 없어도 인터럽트 상태를 직접 확인하기 때문에 while문을 빠져나갈 수 있다.

```java
while (인터럽트_상태_확인) { //여기서도 체크 log("작업 중");
}
```
while문에서 인터럽트의 상태를 직접 확인하도록 코드를 변경해보자.

추가로 예제를 단순화하고 더 직접적인 이해를 돕기 위해 `run()` 의 반복문에서 `sleep()` 코드도 함께 제거하자.

```java
public class ThreadStopMainV3 {

    public static void main(String[] args) {
        MyTask myTask = new MyTask();
        Thread thread = new Thread(myTask, "work");
        thread.start();

        sleep(100);
        log("작업 중단 지시");
        thread.interrupt();
        log("work 쓰레드 인터럽트 상태1 = " + thread.isInterrupted());
    }

    static class MyTask implements Runnable {

        @Override
        public void run() {

            while(!Thread.currentThread().isInterrupted()) {
                log("작업중");
            }

            log("work 쓰레드 인터럽트 상태2 = " + Thread.currentThread().isInterrupted());

            try {
                log("자원정리");
                Thread.sleep(1000);
                log("작업종료");
            } catch(InterruptedException e) {
                log("자원 정리 실패 - 인터럽트 발생");
                log("work 쓰레드 인터럽트 상태3 = " + Thread.currentThread().isInterrupted());
            }
        }
    }
}
```

```shell
22:49:36.267 [     work] 작업중
22:49:36.267 [     main] 작업 중단 지시
22:49:36.267 [     work] 작업중
22:49:36.270 [     main] work 쓰레드 인터럽트 상태1 = true
22:49:36.270 [     work] work 쓰레드 인터럽트 상태2 = true
22:49:36.271 [     work] 자원정리
22:49:36.271 [     work] 자원 정리 실패 - 인터럽트 발생
22:49:36.271 [     work] work 쓰레드 인터럽트 상태3 = false
```

**주요 실행 순서**

`main` 스레드는 `interrupt()` 메서드를 사용해서, `work` 스레드에 인터럽트를 건다. 

`work` 스레드는 인터럽트 상태이다. `isInterrupted()=true` 가 된다.

이때 다음과 같이 while 조건이 `false` 가 되면서 while문을 탈출한다.

여기까지 보면 아무런 문제가 없어 보인다. 하지만 이 코드에는 심각한 문제가 있다.

바로 `work` 스레드의 인터럽트 상태가 `true` 로 계속 유지된다는 점이다.

앞서 인터럽트 예외가 터진 경우 스레드의 인터럽트 상태는 `false` 가 된다.

반면에 `isInterrupted()` 메서드는 인터럽트의 상태를 변경하지 않는다. 단순히 인터럽트의 상태를 확인만 한다.

`work` 스레드는 이후에 자원을 정리하는 코드를 실행하는데, 이때도 인터럽트의 상태는 계속 `true` 로 유지된다. 

이때 만약 인터럽트가 발생하는 `sleep()` 과 같은 코드를 수행한다면, 해당 코드에서 인터럽트 예외가 발생하게 된다.

(내가 정리 : 위의 코드를 보면 작업을 하다가 중단되고 작업하던 자원을 정리해야하는데 인터럽트가 계속해서 살아있기 때문에 자원 정리에도 실패한다.)

이것은 우리가 기대한 결과가 아니다! 

우리가 기대하는 것은 `while()` 문을 탈출하기 위해 딱 한 번만 인터럽트를 사용 하는 것이지, 다른 곳에서도 계속해서 인터럽트가 발생하는 것이 아니다.

**자바에서 인터럽트 예외가 한 번 발생하면, 스레드의 인터럽트 상태를 다시 정상( `false` )으로 돌리는 것은 이런 이유 때문이다.**

**스레드의 인터럽트 상태를 정상으로 돌리지 않으면 이후에도 계속 인터럽트가 발생하게 된다.**

**인터럽트의 목적을 달성하면 인터럽트 상태를 다시 정상으로 돌려두어야 한다.**

참고로 이 예제에서 자원 정리에 실패할 때 인터럽트 예외가 발생하면서 인터럽트의 상태가 정상( `false` )으로 돌아온다.

```
work 스레드 인터럽트 상태3 = false 
```

그럼 우리는 어떻게 해야할까?

`while(인터럽트_상태_확인)` 같은 곳에서 인터럽트의 상태를 확인한 다음에, 만약 인터럽트 상태( `true` )라면 인터럽트 상태를 다시 정상( `false` )으로 돌려두면 된다.

## 인터럽트 - 시작4

**Thread.interrupted()**

스레드의 인터럽트 상태를 단순히 확인만 하는 용도라면 `isInterrupted()` 를 사용하면 된다. 

하지만 직접 체크해서 사용할 때는 `Thread.interrupted()` 를 사용해야 한다.

이 메서드는 다음과 같이 작동한다.

스레드가 인터럽트 상태라면 `true` 를 반환하고, 해당 스레드의 인터럽트 상태를 `false` 로 변경한다. 

스레드가 인터럽트 상태가 아니라면 `false` 를 반환하고, 해당 스레드의 인터럽트 상태를 변경하지 않는다.

```java
public class ThreadStopMainV4 {

    public static void main(String[] args) {
        MyTask myTask = new MyTask();
        Thread thread = new Thread(myTask, "work");
        thread.start();

        sleep(100);
        log("작업 중단 지시");
        thread.interrupt();
        log("work 쓰레드 인터럽트 상태1 = " + thread.isInterrupted());
    }

    static class MyTask implements Runnable {

        @Override
        public void run() {

            while(!Thread.interrupted()) { // V3에서 여기만 변경
                log("작업중");
            }

            log("work 쓰레드 인터럽트 상태2 = " + Thread.currentThread().isInterrupted());

            try {
                log("자원정리");
                Thread.sleep(1000);
                log("작업종료");
            } catch(InterruptedException e) {
                log("자원 정리 실패 - 인터럽트 발생");
                log("work 쓰레드 인터럽트 상태3 = " + Thread.currentThread().isInterrupted());
            }
        }
    }
}
```

```shell
22:59:20.614 [     work] 작업중
22:59:20.614 [     work] 작업중
22:59:20.614 [     main] 작업 중단 지시
22:59:20.614 [     work] 작업중
22:59:20.617 [     main] work 쓰레드 인터럽트 상태1 = true
22:59:20.618 [     work] work 쓰레드 인터럽트 상태2 = false
22:59:20.618 [     work] 자원정리
22:59:21.623 [     work] 작업종료
```

`work` 스레드는 이후에 자원을 정리하는 코드를 실행하는데, 이때 인터럽트의 상태는 `false` 이므로 인터럽트가 발생하는 `sleep()` 과 같은 코드를 수행해도 인터럽트가 발생하지 않는다. 이후에 자원을 정상적으로 잘 정리하는 것을 확인할 수 있다.

**자바는 인터럽트 예외가 한 번 발생하면, 스레드의 인터럽트 상태를 다시 정상( `false` )으로 돌린다.** 

**스레드의 인터럽트 상태를 정상으로 돌리지 않으면 이후에도 계속 인터럽트가 발생하게 된다.** 

**인터럽트의 목적을 달성하면 인터럽트 상태를 다시 정상으로 돌려두어야 한다.**

인터럽트의 상태를 직접 체크해서 사용하는 경우 `Thread.interrupted()` 를 사용하면 이런 부분이 해결된다. 

참고로 `isInterrupted()` 는 특정 스레드의 상태를 변경하지 않고 확인할 때 사용한다.

물론 꼭 이것만이 정답은 아니다. 

예를 들어 너무 긴급한 상황이어서 자원 정리도 하지 않고, 최대한 빨리 스레드를 종료 해야 한다면 해당 스레드를 다시 인터럽트 상태로 변경하는 것도 방법이다.


## 프린터 예제 1


```java
public class MyPrinterV1 {

    public static void main(String[] args) {
        Printer printer = new Printer();
        Thread printThread = new Thread(printer, "printer");
        printThread.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            log("프린터할 문서를 입력해: 종료(q): ");

            String input = scanner.nextLine();

            if(input.equals("q")) {
               printer.work = false;
               break;
            }

            printer.addJob(input);
        }
    }

    static class Printer implements Runnable {
        volatile boolean work = true;
        Queue<String> jobQueue = new ConcurrentLinkedQueue<>();

        @Override
        public void run() {
            while (work) {
                if(jobQueue.isEmpty()) {
                    continue;
                }

                String job = jobQueue.poll();
                log("출력 시작" + job + ", 대기 문서: " + jobQueue);
                sleep(3000);
                log("출력 완료");
            }

            log("프린터 종료");
        }

        public void addJob(String input) {
            jobQueue.add(input);
        }
    }
}
```

`volatile` : 여러 스레드가 동시에 접근하는 변수에는 `volatile` 키워드를 붙어주어야 안전하다. 

여기서는 `main` 스레드, `printer` 스레드 둘다 `work` 변수에 동시에 접근할 수 있다. `volatile` 에 대한 자세한 내용은 뒤에서 설명한다.

`ConcurrentLinkedQueue` : 여러 스레드가 동시에 접근하는 경우, 컬렉션 프레임워크가 제공하는 일반적인 자료구조를 사용하면 안전하지 않다. 

여러 스레드가 동시에 접근하는 경우 동시성을 지원하는 동시성 컬렉션을 사용해야 한다. 

`Queue` 의 경우 `ConcurrentLinkedQueue` 를 사용하면 된다. 동시성 컬렉션의 자세한 내용은 뒤에서 설명한다. 여기서는 일반 큐라고 생각하면 된다.

<img width="686" alt="Screenshot 2024-10-29 at 22 48 44" src="https://github.com/user-attachments/assets/01b41c35-98c3-4584-b7ea-66ad48d99c64">

main` 스레드: 사용자의 입력을 받아서 `Printer` 인스턴스의 `jobQueue` 에 담는다. 

`printer` 스레드: `jobQueue` 가 있는지 확인한다.

`jobQueue` 에 내용이 있으면 `poll()` 을 이용해서 꺼낸 다음에 출력한다.

출력하는데는 약 3초의 시간이 걸린다. 여기서는 `sleep(3000)` 를 사용해서 출력 시간을 가상으로 구현했다.

출력을 완료하면 while문을 다시 반복한다.

만약 `jobQueue` 가 비었다면 `continue` 를 사용해서 다시 while문을 반복한다. 이렇게 해서 `jobQueue` 에 출력할 내용이 들어올 때 까지 계속 확인한다.

<img width="683" alt="Screenshot 2024-10-29 at 22 49 18" src="https://github.com/user-attachments/assets/6dc09fa9-9936-47a3-a4ee-a10753dfaeb0">

`main` 스레드: 사용자가 `q` 를 입력한다. `printer.work` 의 값을 `false` 로 변경한다.

`main` 스레드는 while문을 빠져나가고 `main` 스레드가 종료된다.

`printer` 스레드: while문에서 `work` 의 값이 `false` 인 것을 확인한다.

`printer` 스레드는 while문을 빠져나가고, "프린터 종료"를 출력하고, `printer` 스레드는 종료된다.

앞서 살펴보았듯이 이 방식의 문제는 종료( `q` )를 입력했을 때 바로 반응하지 않는다는 점이다. 

왜냐하면 `printer` 스 레드가 반복문을 빠져나오려면 while문을 체크해야 하는데, `printer` 스레드가 `sleep(3000)` 을 통해 대기 상태에 빠져서 작동하지 않기 때문이다. 

따라서 최악의 경우 `q` 를 입력하고 3초 이후에 프린터가 종료된다.

이제 인터럽트를 사용해서 반응성이 느린 문제를 해결해 보자.

## 예제 2

```java
public class MyPrinterV2 {

    public static void main(String[] args) {
        Printer printer = new Printer();
        Thread printThread = new Thread(printer, "printer");
        printThread.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            log("프린터할 문서를 입력해: 종료(q): ");

            String input = scanner.nextLine();

            if(input.equals("q")) {
               printer.work = false;
               printThread.interrupt();
               break;
            }

            printer.addJob(input);
        }
    }

    static class Printer implements Runnable {
        volatile boolean work = true;
        Queue<String> jobQueue = new ConcurrentLinkedQueue<>();

        @Override
        public void run() {
            while (work) {
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
```

## 예제 3

```java
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
```

## yield 양보하기

어떤 스레드를 얼마나 실행할지는 운영체제가 스케줄링을 통해 결정한다. 

그런데 특정 스레드가 크게 바쁘지 않은 상황 이어서 다른 스레드에 CPU 실행 기회를 양보하고 싶을 수 있다. 

이렇게 양보하면 스케줄링 큐에 대기 중인 다른 스레드 가 CPU 실행 기회를 더 빨리 얻을 수 있다.

```java
public class YieldMain {

  static final int THREAD_COUNT = 1000;

  public static void main (String[] args) {
    for(int i = 0 ; i < THREAD_COUNT ; i++) {
      new Thread(new MyRunnable()).start();
    }
  }

  static class MyRunnable implements Runnable {

    @Override
    public void run() {
      for(int i = 0 ; i < 10 ; i++) {

        System.out.println(Thread.currentThread().getName() + " - " + i);
        // 1. 암것도 안하기
        //sleep(1); // 2. sleep(1)
        //Thread.yield(); // yield
      }
    }
  }
}
```

1000개의 스레드를 실행한다.

각 스레드가 실행하는 로직은 아주 단순하다. 스레드당 0~9까지 출력하면 끝난다.

`run()` 에 있는 1, 2, 3 주석을 변경하면서 실행해보자.

여기서는 3가지 방식을 사용한다.

1. `Empty` : `sleep(1)` , `yield()` 없이 호출한다. 운영체제의 스레드 스케줄링을 따른다.

```shell
Thread-3 - 0
Thread-2 - 0
Thread-2 - 1
Thread-2 - 2
Thread-2 - 3
```

특정 스레드가 쭉~ 수행된 다음에 다른 스레드가 수행되는 것을 확인할 수 있다.

참고로 실행 환경에 따라 결과는 달라질 수 있다. 다른 예시보다 상대적으로 하나의 스레드가 쭉~ 연달아 실행되 다가 다른 스레드로 넘어간다.

이 부분은 운영체제의 스케줄링 정책과 환경에 따라 다르지만 대략 0.01초(10ms)정도 하나의 스레드가 실행되고, 다른 스레드로 넘어간다.

2. `sleep(1)` : 특정 스레드를 잠시 쉬게 한다.

```shell
Thread-750 - 9
Thread-388 - 9
Thread-547 - 9
Thread-769 - 8
Thread-805 - 5
```

`sleep(1)` 을사용해서스레드의상태를1밀리초동안아주잠깐 `RUNNABLE` `TIMED_WAITING` 으로변경 한다. 

이렇게 되면 스레드는 CPU 자원을 사용하지 않고, 실행 스케줄링에서 잠시 제외된다. 

1 밀리초의 대기 시 간이후다시 `TIMED_WAITING` `RUNNABLE` 상태가 되면서 실행 스케줄링에 포함된다.

결과적으로 `TIMED_WAITING` 상태가 되면서 다른 스레드에 실행을 양보하게 된다. 

그리고 스캐줄링 큐에 대기 중인 다른 스레드가 CPU의 실행 기회를 빨리 얻을 수 있다.

하지만이방식은 `RUNNABLE` `TIMED_WAITING` `RUNNABLE` 로 변경되는 복잡한 과정을 거치고, 또 특정시간 만큼 스레드가 실행되지 않는 단점이 있다.

예를 들어서 양보할 스레드가 없다면, 차라리 나의 스레드를 더 실행하는 것이 나은 선택일 수 있다. 

이 방법은 나머지 스레드가 모두 대기 상태로 쉬고 있어도 내 스레드까지 잠깐 실행되지 않는 것이다. 

쉽게 이야기해서 양보할 사람이 없는데 혼자서 양보한 이상한 상황이 될 수 있다.

3. `yield()` : `yield()` 를 사용해서 다른 스레드에 실행을 양보한다.

```shell
Thread-868 - 8
Thread-882 - 9
Thread-940 - 9
Thread-943 - 8
Thread-868 - 9
Thread-943 - 9
```

자바의 스레드가 `RUNNABLE` 상태일 때, 운영체제의 스케줄링은 다음과 같은 상태들을 가질 수 있다.

**실행 상태(Running):** 스레드가 CPU에서 실제로 실행 중이다.

**실행 대기 상태(Ready):** 스레드가 실행될 준비가 되었지만, CPU가 바빠서 스케줄링 큐에서 대기 중이다.

운영체제는 실행 상태의 스레드들을 잠깐만 실행하고 실행 대기 상태로 만든다. 

그리고 실행 대기 상태의 스레드들을 잠깐만 실행 상태로 변경해서 실행한다. 

이 과정을 계속 반복한다. 참고로 자바에서는 두 상태를 구분할 수는 없다.

그래서 이 두 개의 상태랄 합쳐 runnable 이라고 한다.

**yield()의 작동**

`Thread.yield()` 메서드는 현재 실행 중인 스레드가 자발적으로 CPU를 양보하여 다른 스레드가 실행될 수 있도록 한다.

`yield()` 메서드를 호출한 스레드는 `RUNNABLE` 상태를 유지하면서 CPU를 양보한다. 

즉, 이 스레드는 다시 스케줄링 큐에 들어가면서 다른 스레드에게 CPU 사용 기회를 넘긴다.

자바에서 `Thread.yield()` 메서드를 호출하면 현재 실행 중인 스레드가 CPU를 양보하도록 힌트를 준다. 

이는 스레 드가 자신에게 할당된 실행 시간을 포기하고 다른 스레드에게 실행 기회를 주도록 한다. 

참고로 `yield()` 는 운영체제 의 스케줄러에게 단지 힌트를 제공할 뿐, 강제적인 실행 순서를 지정하지 않는다. 

그리고 반드시 다른 스레드가 실행되는 것도 아니다.

`yield()` 는 `RUNNABLE` 상태를 유지하기 때문에, 쉽게 이야기해서 양보할 사람이 없다면 본인 스레드가 계속 실행될 수 있다.

참고로 최근에는 10코어 이상의 CPU도 많기 때문에 스레드 10개 정도만 만들어서 실행하면, 양보가 크게 의미가 없다. 

양보해도 CPU 코어가 남기 때문에 양보하지 않고 계속 수행될 수 있다. 

CPU 코어 수 이상의 스레드를 만들어야 양보하는 상황을 확인할 수 있다. 

그래서 이번 예제에서 1000개의 스레드를 실행한 것이다.

**참고**: `log()` 가 사용하는 기능은 현재 시간도 획득해야 하고, 날짜 포멧도 지정해야 하는 등 복잡하다. 

이 사이에 스레드의 컨텍스트 스위칭이 발생하기 쉽다. 

이런 이유로 스레드의 실행 순서를 일정하게 출력하기 어렵다. 

그래서 여기서는 단순한 `System.out.println()` 을 사용했다.

## 예제 4

```java
public class MyPrinterV4 {

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
          Thread.yield();
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
```

현재 작동하는 스레드가 아주 많다고 가정해보자.

인터럽트도 걸리지 않고, `jobQueue` 도 비어있는데, 이런 체크 로직에 CPU 자원을 많이 사용하게 되면, 정작 필요한 스레드들의 효율이 상대적으로 떨어질 수 있다.

차라리 그 시간에 다른 스레드들을 더 많이 실행해서 `jobQueue` 에 필요한 작업을 빠르게 만들어 넣어주는게 더 효율적일 것이다.

그래서 다음과 같이 `jobQueue` 에 작업이 비어있으면 `yield()` 를 호출해서, 다른 스레드에 작업을 양보하는게 전체 관점에서 보면 더 효율적이다.
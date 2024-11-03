# 고급 동기화 - concurrent.Lock

## LockSupport1

`synchronized` 는 자바 1.0부터 제공되는 매우 편리한 기능이지만, 다음과 같은 한계가 있다.

### **synchronized 단점**

**무한 대기**: `BLOCKED` 상태의 스레드는 락이 풀릴 때 까지 무한 대기한다.

특정 시간까지만 대기하는 타임아웃X

중간에 인터럽트X

**공정성**: 락이 돌아왔을 때 `BLOCKED` 상태의 여러 스레드 중에 어떤 스레드가 락을 획득할 지 알 수 없다. 

최악의 경우 특정 스레드가 너무 오랜기간 락을 획득하지 못할 수 있다.

결국 더 유연하고, 더 세밀한 제어가 가능한 방법들이 필요하게 되었다.

이런 문제를 해결하기 위해 자바 1.5부터 `java.util.concurrent` 라는 동시성 문제 해결을 위한 라이브러리 패키지가 추가된다.

이 라이브러리에는 수 많은 클래스가 있지만, 가장 기본이 되는 `LockSupport` 에 대해서 먼저 알아보자. 

`LockSupport` 를 사용하면 `synchronized` 의 가장 큰 단점인 무한 대기 문제를 해결할 수 있다.

### LockSupport 기능

`LockSupport` 는 스레드를 `WAITING` 상태로 변경한다.

`WAITING` 상태는 누가 깨워주기 전까지는 계속 대기한다. 그리고 CPU 실행 스케줄링에 들어가지 않는다.

`LockSupport` 의 대표적인 기능은 가능과 같다. 

* `park()` : 스레드를 `WAITING` 상태로 변경한다. 
  * 스레드를 대기 상태로 둔다. 참고로 `park` 의 뜻이 "주차하다", "두다"라는 뜻이다. 
* `parkNanos(nanos)` : 스레드를 나노초 동안만 `TIMED_WAITING` 상태로 변경한다.
  * 지정한 나노초가 지나면 `TIMED_WAITING` 상태에서 빠져나오고 `RUNNABLE` 상태로 변경된다. 
* `unpark(thread)` : `WAITING` 상태의 대상 스레드를 `RUNNABLE` 상태로 변경한다.


```java
public class LockSupportMainV1 {

    public static void main(String[] args) {
        Thread thread1 = new Thread(new ParkTest(), "Thread-1");
        thread1.start();

        sleep(100);
        log("Thread-1 state: " + thread1.getState());

        log("main -> unpark(Thread-1)");
        LockSupport.unpark(thread1);
    }

    static class ParkTest implements Runnable {

        @Override
        public void run() {
            log("park 시작");

            LockSupport.park();
            log("park 종료 state: " + Thread.currentThread().getState());
            log("인터럽트 상태: " + Thread.currentThread().isInterrupted());
        }
    }
}
```
```shell
23:28:35.974 [ Thread-1] park 시작
23:28:36.056 [     main] Thread-1 state: WAITING
23:28:36.056 [     main] main -> unpark(Thread-1)
23:28:36.057 [ Thread-1] park 종료 state: RUNNABLE
23:28:36.059 [ Thread-1] 인터럽트 상태: false
```

<img width="523" alt="Screenshot 2024-11-03 at 23 29 38" src="https://github.com/user-attachments/assets/983475b7-1076-4772-8ca3-fbf2beed1dde">

`main` 스레드가 `Thread-1` 을 `start()` 하면 `Thread-1` 은 `RUNNABLE` 상태가 된다.

`Thread-1` 은 `Thread.park()` 를호출한다. `Thread-1` 은 `RUNNABLE` `WAITING` 상태가 되면서 대기한다.

`main` 스레드가 `Thread-1` 을 `unpark()` 로 깨운다. `Thread-1` 은 대기 상태에서 실행 가능 상태로 변한다.

`WAITING` `RUNNABLE` 상태로 변한다.

이처럼 `LockSupport` 는 특정 스레드를 `WAITING` 상태로, 또 `RUNNABLE` 상태로 변경할 수 

그런데 대기 상태로 바꾸는 `LockSupport.park()` 는 매개변수가 없는데, 실행 가능 상태로 바꾸는 `LockSupport.unpark(thread1)` 는 왜 특정 스레드를 지정하는 매개변수가 있을까?

왜냐하면 실행 중인 스레드는 `LockSupport.park()` 를 호출해서 스스로 대기 상태에 빠질 수 있지만, 대기 상태의 스레드는 자신의 코드를 실행할 수 없기 때문이다. 

따라서 외부 스레드의 도움을 받아야 깨어날 수 있다.












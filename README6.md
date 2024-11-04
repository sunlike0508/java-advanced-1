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
        //thread1.interrupt();
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

`main` 스레드가 `Thread-1` 을 `start()` 하면 `Thread-1` 은 `RUNNABLE` 상태가 된다.

`Thread-1` 은 `Thread.park()` 를호출한다. `Thread-1` 은 `RUNNABLE` `WAITING` 상태가 되면서 대기한다.

`main` 스레드가 `Thread-1` 을 `unpark()` 로 깨운다. `Thread-1` 은 대기 상태에서 실행 가능 상태로 변한다.

`WAITING` `RUNNABLE` 상태로 변한다.

이처럼 `LockSupport` 는 특정 스레드를 `WAITING` 상태로, 또 `RUNNABLE` 상태로 변경할 수 

그런데 대기 상태로 바꾸는 `LockSupport.park()` 는 매개변수가 없는데, 실행 가능 상태로 바꾸는 `LockSupport.unpark(thread1)` 는 왜 특정 스레드를 지정하는 매개변수가 있을까?

왜냐하면 실행 중인 스레드는 `LockSupport.park()` 를 호출해서 스스로 대기 상태에 빠질 수 있지만, 대기 상태의 스레드는 자신의 코드를 실행할 수 없기 때문이다. 

따라서 외부 스레드의 도움을 받아야 깨어날 수 있다.

### 인터럽트 사용

`WAITING` 상태의 스레드에 인터럽트가 발생하면 `WAITING` 상태에서 `RUNNABLE` 상태로 변하면서 깨어난다.

```shell
# interrupt
23:39:50.865 [ Thread-1] park 시작
23:39:50.952 [     main] Thread-1 state: WAITING
23:39:50.952 [     main] main -> unpark(Thread-1)
23:39:50.952 [ Thread-1] park 종료 state: RUNNABLE
23:39:50.955 [ Thread-1] 인터럽트 상태: true
```

실행 결과를 보면 스레드가 `RUNNABLE` 상태로 깨어난 것을 확인할 수 있다. 

그리고 해당 스레드의 인터럽트의 상태도 `true` 인 것을 확인할 수 있다.

이처럼 `WAITING` 상태의 스레드는 인터럽트를 걸어서 중간에 깨울 수 있다.

## LockSupport2

시간 대기

이번에는 스레드를 특정 시간 동안만 대기하는 `parkNanos(nanos)` 를 호출해보자.

`parkNanos(nanos)` : 스레드를 나노초 동안만 `TIMED_WAITING` 상태로 변경한다. 

지정한 나노초가 지나면 `TIMED_WAITING` 상태에서 빠져나와서 `RUNNABLE` 상태로 변경된다.

참고로 밀리초 동안만 대기하는 메서드는 없다. 

`parkUntil(밀리초)` 라는 메서드가 있는데, 이 메서드는 특정 에포크(Epoch) 시간에 맞추어 깨어나는 메서드이다. 정확한 미래의 에포크 시점을 지정해야 한다.

```java
public class LockSupportMainV2 {

    public static void main(String[] args) {
        Thread thread1 = new Thread(new ParkTest(), "Thread-1");
        thread1.start();

        sleep(100);
        log("Thread-1 state: " + thread1.getState());
    }

    static class ParkTest implements Runnable {

        @Override
        public void run() {
            log("park 시작");

            LockSupport.parkNanos(2000_000000);
            log("park 종료 state: " + Thread.currentThread().getState());
            log("인터럽트 상태: " + Thread.currentThread().isInterrupted());
        }
    }
}
```

```shell
23:44:46.012 [ Thread-1] park 시작
23:44:46.095 [     main] Thread-1 state: TIMED_WAITING
23:44:48.019 [ Thread-1] park 종료 state: RUNNABLE
23:44:48.020 [ Thread-1] 인터럽트 상태: false
```


`Thread-1` 은 `parkNanos(2초)` 를 사용해서 2초간 `TIMED_WAITING` 상태에 빠진다. 

`Thread-1` 은 2초 이후에 시간 대기 상태( `TIMED_WAITING` )를 빠져나온다.


### BLOCKED vs WAITING

`WAITING` 상태에 특정 시간까지만 대기하는 기능이 포함된 것이 `TIMED_WAITING` 이다. 여기서는 둘을 묶어서 `WAITING` 상태라 표현하겠다.

**인터럽트**
* `BLOCKED` 상태는 인터럽트가 걸려도 대기 상태를 빠져나오지 못한다. 여전히 `BLOCKED` 상태이다.
* `WAITING` , `TIMED_WAITING` 상태는 인터럽트가 걸리면 대기 상태를 빠져나온다. 그래서 `RUNNABLE` 상태로 변한다. 

**용도**
* `BLOCKED` 상태는 자바의 `synchronized` 에서 락을 획득하기 위해 대기할 때 사용된다.
* `WAITING` , `TIMED_WAITING` 상태는 스레드가 특정 조건이나 시간 동안 대기할 때 발생하는 상태이다. 
* `WAITING` 상태는 다양한 상황에서 사용된다. 예를 들어, `Thread.join()` , `LockSupport.park()` ,`Object.wait()` 와 같은 메서드 호출 시 `WAITING` 상태가 된다.
* `TIMED_WAITING` 상태는 `Thread.sleep(ms),` `Object.wait(long timeout)` , `Thread.join(long millis)` , `LockSupport.parkNanos(ns)` 등과 같은 시간 제한이 있는 대기 메서드를 호출할 때 발생한다.

**대기( `WAITING` ) 상태와 시간 대기 상태( `TIMED_WAITING` )는 서로 짝이 있다.** 
* `Thread.join()` , `Thread.join(long millis)` 
* `Thread.park()` , `Thread.parkNanos(long millis)` 
* `Object.wait()` , `Object.wait(long timeout)`

**참고**: `Object.wait()` 는 뒤에서 다룬다.

`BLOCKED` , `WAITING` , `TIMED_WAITING` 상태 모두 스레드가 대기하며, 실행 스케줄링에 들어가지 않기 때문에, CPU 입장에서 보면 실행하지 않는 비슷한 상태이다.
* `BLOCKED` 상태는 `synchronized` 에서만 사용하는 특별한 대기 상태라고 이해하면 된다.
* `WAITING` , `TIMED_WAITING` 상태는 범용적으로 활용할 수 있는 대기 상태라고 이해하면 된다.


### LockSupport 정리

`LockSupport` 를 사용하면 스레드를 `WAITING` , `TIMED_WAITING` 상태로 변경할 수 있고, 또 인터럽트를 받아서 스레드를 깨울 수도 있다. 

이런 기능들을 잘 활용하면 `synchronized` 의 단점인 무한 대기 문제를 해결할 수 있을 것 같다.

**synchronized 단점**

* **무한 대기**: `BLOCKED` 상태의 스레드는 락이 풀릴 때 까지 무한 대기한다.
  * 특정 시간까지만 대기하는 타임아웃X `parkNanos()` 를 사용하면 특정 시간까지만 대기할 수 있음 
  * 중간에 인터럽트X `park()` , `parkNanos()` 는 인터럽트를 걸 수 있음

이처럼 `LockSupport` 를 활용하면, 무한 대기하지 않는 락 기능을 만들 수 있다. 물론 그냥 되는 것은 아니고 `LockSupport` 를 활용해서 안전한 임계 영역을 만드는 어떤 기능을 개발해야 한다. 예를 들면 다음과 같을 것이다.


```java
if (!lock.tryLock(10초)) { // 내부에서 parkNanos() 사용
  log("[진입 실패] 너무 오래 대기했습니다.");
  return false;
}
//임계 영역 시작
...
//임계 영역 종료
lock.unlock() // 내부에서 unpark() 사용 
```

락( `lock` )이라는 클래스를 만들고, 특정 스레드가 먼저 락을 얻으면 `RUNNABLE` 로 실행하고, 락을 얻지 못하면 `park()` 를 사용해서 대기 상태로 만드는 것이다. 

그리고 스레드가 임계 영역의 실행을 마치고 나면 락을 반납하고, `unpark()` 를 사용해서 대기 중인 다른 스레드를 깨우는 것이다. 

물론 `parkNanos()` 를 사용해서 너무 오래 대기하면 스레드가 스스로 중간에 깨어나게 할 수도 있다.

하지만 이런 기능을 직접 구현하기는 매우 어렵다. 

예를 들어 스레드 10개를 동시에 실행했는데, 그중에 딱 1개의 스레드만 락을 가질 수 있도록 락 기능을 만들어야 한다. 

그리고 나머지 9개의 스레드가 대기해야 하는데, 어떤 스레드가 대 기하고 있는지 알 수 있는 자료구조가 필요하다. 

그래야 이후에 대기 중인 스레드를 찾아서 깨울 수 있다. 

여기서 끝이 아니다. 대기 중인 스레드 중에 어떤 스레드를 깨울지에 대한 우선순위 결정도 필요하다.

한마디로 `LockSupport` 는 너무 저수준이다. `synchronized` 처럼 더 고수준의 기능이 필요하다.

하지만 걱정하지 말자! 자바는 `Lock` 인터페이스와 `ReentrantLock` 이라는 구현체로 이런 기능들을 이미 다 구현해 두었다. 

`ReentrantLock` 은 `LockSupport` 를 활용해서 `synchronized` 의 단점을 극복하면서도 매우 편리하게 임계 영역을 다룰 수 있는 다양한 기능을 제공한다.

## ReentrantLock - 이론

자바는 1.0부터 존재한 `synchronized` 와 `BLOCKED` 상태를 통한 통한 임계 영역 관리의 한계를 극복하기 위해 자바 1.5부터 `Lock` 인터페이스와 `ReentrantLock` 구현체를 제공한다.

**synchronized 단점**

* **무한 대기**: `BLOCKED` 상태의 스레드는 락이 풀릴 때 까지 무한 대기한다.
  * 특정 시간까지만 대기하는 타임아웃X
  * 중간에 인터럽트X
* **공정성**: 락이 돌아왔을 때 `BLOCKED` 상태의 여러 스레드 중에 어떤 스레드가 락을 획득할 지 알 수 없다. 최악의 경우 특정 스레드가 너무 오랜기간 락을 획득하지 못할 수 있다.

### Lock 인터페이스

```java
package java.util.concurrent.locks;

public interface Lock {
    void lock();
    void lockInterruptibly() throws InterruptedException;
    boolean tryLock();
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
    void unlock();
    Condition newCondition();
}
```
`Lock` 인터페이스는 동시성 프로그래밍에서 쓰이는 안전한 임계 영역을 위한 락을 구현하는데 사용된다.

`Lock` 인터페이스는 다음과 같은 메서드를 제공한다. 대표적인 구현체로 `ReentrantLock` 이 있다.

`void lock()`
* 락을 획득한다. 만약 다른 스레드가 이미 락을 획득했다면, 락이 풀릴 때까지 현재 스레드는 대기( `WAITING` )한다. 이 메서드는 인터럽트에 응답하지 않는다.
* 예) 맛집에 한번 줄을 서면 끝까지 기다린다. 친구가 다른 맛집을 찾았다고 중간에 연락해도 포기하지 않고 기다린다.

**주의!**

여기서 사용하는 락은 객체 내부에 있는 모니터 락이 아니다! 

`Lock` 인터페이스와 `ReentrantLock` 이 제공하는 기능이다!

모니터 락과 `BLOCKED` 상태는 `synchronized` 에서만 사용된다.

**`void lockInterruptibly()`**
* 락 획득을 시도하되, 다른 스레드가 인터럽트할 수 있도록 한다. 만약 다른 스레드가 이미 락을 획득했다면, 현재 스레드는 락을 획득할 때까지 대기한다. 
* 대기 중에 인터럽트가 발생하면 `InterruptedException` 이 발생하며 락 획득을 포기한다. 
* 예) 맛집에 한번 줄을 서서 기다린다. 다만 친구가 다른 맛집을 찾았다고 중간에 연락하면 포기한다.

**`boolean tryLock()`**
* 락 획득을 시도하고, 즉시 성공 여부를 반환한다. 만약 다른 스레드가 이미 락을 획득했다면 `false` 를 반환하고, 그렇지 않으면 락을 획득하고 `true` 를 반환한다.
* 예) 맛집에 대기 줄이 없으면 바로 들어가고, 대기 줄이 있으면 즉시 포기한다.

**`boolean tryLock(long time, TimeUnit unit)`**
* 주어진 시간 동안 락 획득을 시도한다. 주어진 시간 안에 락을 획득하면 `true` 를 반환한다. 
* 주어진 시간이 지나도 락을 획득하지 못한 경우 `false` 를 반환한다. 이 메서드는 대기 중 인터럽트가 발생하면 `InterruptedException` 이 발생하며 락 획득을 포기한다. 
* 예) 맛집에 줄을 서지만 특정 시간 만큼만 기다린다. 특정 시간이 지나도 계속 줄을 서야 한다면 포기한다. 친구가 다른 맛집을 찾았다고 중간에 연락해도 포기한다.
  
**`void unlock()`**
* 락을 해제한다. 락을 해제하면 락 획득을 대기 중인 스레드 중 하나가 락을 획득할 수 있게 된다. 
* 락을 획득한 스레드가 호출해야 하며, 그렇지 않으면 `IllegalMonitorStateException` 이 발생할 수 있다. 
* 예) 식당안에 있는 손님이 밥을 먹고 나간다. 식당에 자리가 하나 난다. 기다리는 손님께 이런 사실을 알려주어야 한다. 기다리던 손님중 한 명이 식당에 들어간다.
  
**`Condition newCondition()`**
* `Condition` 객체를 생성하여 반환한다. `Condition` 객체는 락과 결합되어 사용되며, 스레드가 특정 조건을 기다리거나 신호를 받을 수 있도록 한다. 
* 이는 `Object` 클래스의 `wait` , `notify` , `notifyAll` 메서드와 유사한 역할을 한다. 
* 참고로 이 부분은 뒤에서 자세히 다룬다.

이 메서드들을 사용하면 고수준의 동기화 기법을 구현할 수 있다. 

`Lock` 인터페이스는 `synchronized` 블록보다 더 많은 유연성을 제공하며, 특히 락을 특정 시간 만큼만 시도하거나, 인터럽트 가능한 락을 사용할 때 유용하다.

이 메서드들을 보면 알겠지만 다양한 메서드를 통해 `synchronized` 의 단점인 무한 대기 문제도 깔끔하게 해결할 수 있다.

참고**: `lock()` 메서드는 인터럽트에 응하지 않는다고 되어있다. 

이 메서드의 의도는 인터럽트가 발생해도 무시하고 락을 기다리는 것이다.

앞서 대기( `WAITING` ) 상태의 스레드에 인터럽트가 발생하면 대기 상태를 빠져나온다고 배웠다. 

그런데 `lock()` 메서 드의 설명을 보면 대기( `WAITING` ) 상태인데 인터럽트에 응하지 않는다고 되어있다. 

어떻게 된 것일까?

`lock()` 을 호출해서 락을 얻기 위해 대기중인 스레드에 인터럽트가 발생하면 순간 대기 상태를 빠져나오는 것은 맞다. 

그래서 아주 짧지만 `WAITING` `RUNNABLE` 이된다. 그런데 `lock()` 메서드 안에서 해당 스레드를 다시 `WAITING` 상태로 강제로 변경해버린다. 

이런 원리로 인터럽트를 무시하는 것이다. 

참고로 인터럽트가 필요하면 `lockInterruptibly()` 를 사용하면 된다. 

새로운 `Lock` 은 개발자에게 다양한 선택권을 제공한다.

### 공정성

`Lock` 인터페이스가 제공하는 다양한 기능 덕분에 `synchronized` 의 단점인 무한 대기 문제가 해결되었다. 그런데 공정성에 대한 문제가 남아있다.

**synchronized 단점**

* **공정성**: 락이 돌아왔을 때 `BLOCKED` 상태의 여러 스레드 중에 어떤 스레드가 락을 획득할 지 알 수 없다. 
  * 최악의 경우 특정 스레드가 너무 오랜기간 락을 획득하지 못할 수 있다.

`Lock` 인터페이스의 대표적인 구현체로 `ReentrantLock` 이 있는데, 이 클래스는 스레드가 공정하게 락을 얻을 수 있는 모드를 제공한다.

```java
public class ReentrantLockEx {

    private final Lock nonFairLock = new ReentrantLock();
    private final Lock fairLock = new ReentrantLock(true);

    public void nonFairLockTest() {
        nonFairLock.lock();
        try {
            // 임계 영역

        } finally {

            nonFairLock.unlock();
        }
    }
    public void fairLockTest() {
        fairLock.lock();
        try {
            // 임계 영역
        } finally {
            fairLock.unlock();
        }
    }
}
```

`ReentrantLock` 락은 공정성(fairness) 모드와 비공정(non-fair) 모드로 설정할 수 있으며, 이 두 모드는 락을 획득 하는 방식에서 차이가 있다.

### 비공정 모드 (Non-fair mode)

비공정 모드는 `ReentrantLock` 의 기본 모드이다. 이 모드에서는 락을 먼저 요청한 스레드가 락을 먼저 획득한다는 보장이 없다. 

락을 풀었을 때, 대기 중인 스레드 중 아무나 락을 획득할 수 있다. 이는 락을 빨리 획득할 수 있지만, 특정 스레드가 장기간 락을 획득하지 못할 가능성도 있다.

**비공정 모드 특징**
* **성능 우선**: 락을 획득하는 속도가 빠르다.
* **선점 가능**: 새로운 스레드가 기존 대기 스레드보다 먼저 락을 획득할 수 있다. **기아 현상 가능성**: 특정 스레드가 계속해서 락을 획득하지 못할 수 있다.


### 공정 모드 (Fair mode)

생성자에서 `true` 를 전달하면 된다. 예) `new ReentrantLock(true)`

공정 모드는 락을 요청한 순서대로 스레드가 락을 획득할 수 있게 한다. 

이는 먼저 대기한 스레드가 먼저 락을 획득하게 되어 스레드 간의 공정성을 보장한다. 그러나 이로 인해 성능이 저하될 수 있다.

**공정 모드 특징**

* **공정성 보장**: 대기 큐에서 먼저 대기한 스레드가 락을 먼저 획득한다.
* **기아 현상 방지**: 모든 스레드가 언젠가 락을 획득할 수 있게 보장된다.
* **성능 저하**: 락을 획득하는 속도가 느려질 수 있다.

**비공정, 공정 모드 정리**
* **비공정 모드**는 성능을 중시하고, 스레드가 락을 빨리 획득할 수 있지만, 특정 스레드가 계속해서 락을 획득하지 못 할 수 있다.
* **공정 모드**는 스레드가 락을 획득하는 순서를 보장하여 공정성을 중시하지만, 성능이 저하될 수 있다.

**정리**

`Lock` 인터페이스와 `ReentrantLock` 구현체를 사용하면 `synchronized` 단점인 무한 대기와 공정성 문제를 모두 해결할 수 있다.






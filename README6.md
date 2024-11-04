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

## ReentrantLock - 활용

```java
public class BankAccountV4 implements BankAccount {

  private int balance;

  private final Lock lock = new ReentrantLock();

  public BankAccountV4(int initialBalance) {
    this.balance = initialBalance;
  }

  @Override
  public boolean withdraw(int amount) {
    log("거래 시작 : " + getClass().getSimpleName());

    lock.lock();

    try {
      log("[검증 시작] 출금액: " + amount + ", 잔액: " + balance);

      if(balance < amount) {
        log("[검증 실패]");
        return false;
      }

      log("[검증 완료] 출금액: " + amount + ", 잔액: " + balance);

      sleep(1000);

      balance -= amount;

      log("[출금 완료] 출금액: " + amount + ", 잔액: " + balance);
    } finally {
      lock.unlock();
    }

    log("거래 종료");

    return false;
  }


  @Override
  public int getBalance() {
    lock.lock();
    try {
      return balance;
    } finally {
      lock.unlock();
    }
  }
}
```

```shell
22:21:26.312 [       t1] 거래 시작 : BankAccountV4
22:21:26.312 [       t2] 거래 시작 : BankAccountV4
22:21:26.317 [       t1] [검증 시작] 출금액: 800, 잔액: 1000
22:21:26.317 [       t1] [검증 완료] 출금액: 800, 잔액: 1000
22:21:26.803 [     main] t1 state: TIMED_WAITING
22:21:26.803 [     main] t2 state: WAITING
22:21:27.323 [       t1] [출금 완료] 출금액: 800, 잔액: 200
22:21:27.324 [       t1] 거래 종료
22:21:27.324 [       t2] [검증 시작] 출금액: 800, 잔액: 200
22:21:27.325 [       t2] [검증 실패]
22:21:27.332 [     main] 최종잔액 : 200
```

`private final Lock lock = new ReentrantLock()` 을 사용하도록 선언한다. 

`synchronized(this)` 대신에 `lock.lock()` 을 사용해서 락을 건다.

`lock()` `unlock()` 까지는 안전한 임계 영역이 된다.

임계 영역이 끝나면 반드시! 락을 반납해야 한다. 

그렇지 않으면 대기하는 스레드가 락을 얻지 못한다.

따라서 `lock.unlock()` 은 반드시 `finally` 블럭에 작성해야한다. 

이렇게 하면 검증에 실패해서 중간 에 `return` 을 호출해도 또는 중간에 예상치 못한 예외가 발생해도 `lock.unlock()` 이 반드시 호출된다.

**주의!**

여기서 사용하는 락은 객체 내부에 있는 모니터 락이 아니다! 

`Lock` 인터페이스와 `ReentrantLock` 이 제공하는 기능이다!

모니터 락과 `BLOCKED` 상태는 `synchronized` 에서만 사용된다.

<img width="526" alt="Screenshot 2024-11-04 at 22 24 29" src="https://github.com/user-attachments/assets/2fc8267c-90e5-426b-9a95-c4c3138beafe">

`t1` , `t2` 가 출금을 시작한다. 여기서는 `t1` 이 약간 먼저 실행된다고 가정하겠다.

`ReenterantLock` 내부에는 락과 락을 얻지 못해 대기하는 스레드를 관리하는 대기 큐가 존재한다. 

여기서 이야기하는 락은 객체 내부에 있는 모니터 락이 아니다. `ReentrantLock` 이 제공하는 기능이다.

<img width="526" alt="Screenshot 2024-11-04 at 22 25 24" src="https://github.com/user-attachments/assets/18ef6afa-d483-4f89-aafb-81858be5ee88">

`t1` : `ReenterantLock` 에 있는 락을 획득한다.

락을 획득하는 경우 `RUNNABLE` 상태가 유지되고, 임계 영역의 코드를 실행할 수 있다.

<img width="529" alt="Screenshot 2024-11-04 at 22 25 27" src="https://github.com/user-attachments/assets/4f6e31d0-b5de-4b98-ae3c-440377320954">

`t1` : 임계 영역의 코드를 실행한다.

<img width="530" alt="Screenshot 2024-11-04 at 22 25 31" src="https://github.com/user-attachments/assets/c3f49090-c28d-4d5c-9b85-075396be9127">

`t2` : `ReenterantLock` 에 있는 락의 획득을 시도한다. 하지만 락이 없다.

<img width="535" alt="Screenshot 2024-11-04 at 22 25 36" src="https://github.com/user-attachments/assets/6cf807d2-aaa2-494e-8c11-374513444cf2">

`t2` : 락을 획득하지 못하면 `WAITING` 상태가 되고, 대기 큐에서 관리된다.

`LockSupoort.park()` 가 내부에서 호출된다.

참고로 `tryLock(long time, TimeUnit unit)` 와 같은 시간 대기 기능을 사용하면 `TIMED_WAITING` 이 되고, 대기 큐에서 관리된다.

<img width="531" alt="Screenshot 2024-11-04 at 22 25 46" src="https://github.com/user-attachments/assets/61aad37c-7e46-4285-ac6e-407d0e3bf499">

`t1` : 임계 영역의 수행을 완료했다. 이때 잔액은 `balance=200` 이 된다.

<img width="517" alt="Screenshot 2024-11-04 at 22 25 59" src="https://github.com/user-attachments/assets/dc978f78-4234-4821-b3e8-9f2c7876dcbd">

`t1` : 임계 영역을 수행하고 나면 `lock.unlock()` 을 호출한다.

* **1. t1**: 락을 반납한다.
*  **2. t1**: 대기 큐의 스레드를 하나 깨운다. `LockSupoort.unpark(thread)` 가 내부에서 호출된다.
*  **3. t2**: `RUNNABLE` 상태가 되면서 깨어난 스레드는 락 획득을 시도한다.
 * 이때 락을 획득하면 `lock.lock()` 을 빠져나오면서 대기 큐에서도 제거된다.
 * 이때 락을 획득하지 못하면 다시 대기 상태가 되면서 대기 큐에 유지된다.
  * 참고로 락 획득을 시도하는 잠깐 사이에 새로운 스레드가 락을 먼저 가져갈 수 있다.
  * 공정 모드의 경우 대기 큐에 먼저 대기한 스레드가 먼저 락을 가져간다.


<img width="526" alt="Screenshot 2024-11-04 at 22 26 14" src="https://github.com/user-attachments/assets/cc222a72-c905-494c-b6cb-c692dbccb7d5">


`t2` : 락을 획득한 `t2` 스레드는 `RUNNABLE` 상태로 임계 영역을 수행한다.

<img width="530" alt="Screenshot 2024-11-04 at 22 26 21" src="https://github.com/user-attachments/assets/937d2479-450e-4ec6-a137-17a578518098">


`t2` : 잔액[200]이 출금액[800]보다 적으므로 검증 로직을 통과하지 못한다. 따라서 검증 실패이다. `return false` 가 호출된다.
이때 `finally` 구문이 있으므로 `finally` 구문으로 이동한다.

<img width="541" alt="Screenshot 2024-11-04 at 22 26 32" src="https://github.com/user-attachments/assets/08281181-f505-4a9b-a618-9077ba3cbe40">


`t2` : `lock.unlock()` 을 호출해서 락을 반납하고, 대기 큐의 스레드를 하나 깨우려고 시도한다. 대기 큐에 스레 드가 없으므로 이때는 깨우지 않는다.

<img width="526" alt="Screenshot 2024-11-04 at 22 26 43" src="https://github.com/user-attachments/assets/9097af11-d3dc-43b6-9edc-d22fa316c2ef">

**참고**: `volatile` 를 사용하지 않아도 `Lock` 을 사용할 때 접근하는 변수의 메모리 가시성 문제는 해결된다. (이전에 학
습한 자바 메모리 모델 참고)

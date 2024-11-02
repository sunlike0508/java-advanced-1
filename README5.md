# 문제

## 지역 변수의 공유

다음 코드에서 `MyTask` 의 `run()` 메서드는 두 스레드에서 동시에 실행한다.

다음 코드의 실행 결과를 예측해보자.

그리고 `localValue` 지역 변수에 동시성 문제가 발생하는지 하지 않는지 생각해보자.


```java
public class SyncTest2Main {

    public static void main(String[] args) throws InterruptedException {
        MyCounter myCounter = new MyCounter();

        Runnable task = myCounter::count;

        Thread thread1 = new Thread(task, "Thread-1");
        Thread thread2 = new Thread(task, "Thread-2");

        thread1.start();
        thread2.start();
    }


    static class MyCounter {

        public void count() {
            int localValue = 0;

            for(int i = 0; i < 1000; i++) {
                localValue = localValue + 1;
            }

            log("결과: " + localValue);
        }
    }
}
```
`localValue` 는 지역 변수이다.

스택 영역은 각각의 스레드가 가지는 별도의 메모리 공간이다. 

이 메모리 공간은 다른 스레드와 공유하지 않는다. 

지역 변수는 스레드의 개별 저장 공간인 스택 영역에 생성된다.

따라서 **지역 변수는 절대로! 다른 스레드와 공유되지 않는다!**

이런 이유로 지역 변수는 동기화에 대한 걱정을 하지 않아도 된다.

여기에 `synchronized` 를 사용하면 아무 이득도 얻을 수 없다. 

성능만 느려진다! 지역 변수를 제외한, 인스턴스의 멤버 변수(필드), 클래스 변수 등은 공유될 수 있다.

##문제3 - final 필드

다음에서 `value` 필드(멤버 변수)는 공유되는 값이다. 멀티스레드 상황에서 문제가 될 수 있을까?

```java
class Immutable {
    private final int value;
    
    public Immutable(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    } 
}
```
**정답**

여러 스레드가 공유 자원에 접근하는 것 자체는 사실 문제가 되지 않는다. 

진짜 문제는 공유 자원을 사용하는 중간에 다 른 스레드가 공유 자원의 값을 변경해버리기 때문에 발생한다. 

결국 변경이 문제가 되는 것이다.

여러 스레드가 접근 가능한 공유 자원이라도 그 값을 아무도 변경할 수 없다면 문제 되지 않는다. 

이 경우 모든 스레드가 항상 같은 값을 읽기 때문이다.

필드에 `final` 이 붙으면 어떤 스레드도 값을 변경할 수 없다. 

따라서 멀티스레드 상황에 문제 없는 안전한 공유 자원이 된다.

## 정리

자바는 처음부터 멀티스레드를 고려하고 나온 언어이다. 

그래서 자바 1.0 부터 `synchronized` 같은 동기화 방법을 프로그래밍 언어의 문법에 포함해서 제공한다.

### **synchronized 장점**

프로그래밍 언어에 문법으로 제공

아주 편리한 사용

**자동 잠금 해제**: `synchronized` 메서드나 블록이 완료되면 자동으로 락을 대기중인 다른 스레드의 잠금이 해제 된다. 

개발자가 직접 특정 스레드를 깨우도록 관리해야 한다면, 

매우 어렵고 번거로울 것이다.

`synchronized` 는 매우 편리하지만, 제공하는 기능이 너무 단순하다는 단점이 있다. 

시간이 점점 지나면서 멀티스레드가 더 중요해지고 점점 더 복잡한 동시성 개발 방법들이 필요해졌다.

### **synchronized 단점**

**무한 대기**: `BLOCKED` 상태의 스레드는 락이 풀릴 때 까지 무한 대기한다.

특정 시간까지만 대기하는 타임아웃X

중간에 인터럽트X

**공정성**: 락이 돌아왔을 때 `BLOCKED` 상태의 여러 스레드 중에 어떤 스레드가 락을 획득할 지 알 수 없다. 

최악의 경우 특정 스레드가 너무 오랜기간 락을 획득하지 못할 수 있다.

`synchronized` 의 가장 치명적인 단점은 락을 얻기 위해 `BLOCKED` 상태가 되면 락을 얻을 때까지 무한 대기한다는 점이다. 

비유를 하자면 맛집에 한 번 줄을 서면 10시간이든 100시간이든 밥을 먹을 때까지 강제적으로 계속 기다려야 한다는 점이다.

예를 들어 웹 애플리케이션의 경우 고객이 어떤 요청을 했는데, 화면에 계속 요청 중만 뜨고, 응답을 못 받는 것이다. 

차라리 너무 오랜 시간이 지나면, 시스템에 사용자가 너무 많아서 다음에 다시 시도해달라고 하는 식의 응답을 주는 것이 더 나은 선택일 것이다.

결국 더 유연하고, 더 세밀한 제어가 가능한 방법들이 필요하게 되었다. 

이런 문제를 해결하기 위해 자바 1.5부터 `java.util.concurrent` 라는 동시성 문제 해결을 위한 패키지가 추가된다.

참고로 단순하고 편리하게 사용하기에는 `synchronized` 가 좋으므로, 목적에 부합한다면 `synchronized` 를 사용 하면 된다.























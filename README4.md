# 동기화


## 출금 예제

```java
public class BankMain {
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccountV1(1000);

        Thread thread1 = new Thread(new WithdrawTask(account, 800), "t1");
        Thread thread2 = new Thread(new WithdrawTask(account, 800), "t2");
        thread1.start();
        thread2.start();

        sleep(500);
        log("t1 state: " + thread1.getState());
        log("t2 state: " + thread2.getState());

        thread1.join();
        thread2.join();

        log("최종잔액 : " + account.getBalance());
    }
}

public class WithdrawTask implements Runnable {


    private BankAccount account;
    private int amount;

    public WithdrawTask(BankAccount account, int amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void run() {

        account.withdraw(amount);
    }
}

public class BankAccountV1 implements BankAccount {

    private int balance;
    //volatile private int balance;

    public BankAccountV1(int initialBalance) {
        this.balance = initialBalance;
    }

    @Override
    public boolean withdraw(int amount) {
        log("거래 시작 : " + getClass().getSimpleName());

        log("[검증 시작] 출금액: " + amount + ", 잔액: " + balance);

        if(balance < amount) {
            log("[검증 실패]");
            return false;
        }

        log("[검증 완료] 출금액: " + amount + ", 잔액: " + balance);

        sleep(1000);

        balance -= amount;

        log("[출금 완료] 출금액: " + amount + ", 잔액: " + balance);

        log("거래 종료");

        return false;
    }


    @Override
    public int getBalance() {
        return balance;
    }
}
```


<img width="699" alt="Screenshot 2024-11-02 at 15 54 29" src="https://github.com/user-attachments/assets/219d248e-06cb-49ca-adf0-e762fa974ef2">

각각의 스레드의 스택에서 `run()` 이 실행된다.

`t1` 스레드는 `WithdrawTask(x002)` 인스턴스의 `run()` 을 호출한다.

`t2` 스레드는 `WithdrawTask(x003)` 인스턴스의 `run()` 을 호출한다.

스택 프레임의 `this` 에는 호출한 메서드의 인스턴스 참조가 들어있다. 

두 스레드는 같은 계좌( `x001` )에 대해서 출금을 시도한다.

**참고**: 그림에서는 편의상 `BankAccountV1` 대신에 `BankAccount` 라고 표현하겠다.

<img width="687" alt="Screenshot 2024-11-02 at 15 55 00" src="https://github.com/user-attachments/assets/2fc60808-f39b-4b12-a81b-3537f0d53a48">

`t1` 스레드의 `run()` 에서 `withdraw()` 를 실행한다.

거의 동시에 `t2` 스레드의 `run()` 에서 `withdraw()` 를 실행한다.

`t1` 스레드와 `t2` 스레드는 같은 `BankAccount(x001)` 인스턴스의 `withdraw()` 메서드를 호출한다. 

따라서 두 스레드는 같은 `BankAccount(x001)` 인스턴스에 접근하고 또 `x001` 인스턴스에 있는 잔액 ( `balance` ) 필드도 함께 사용한다.


```shell
15:53:24.449 [       t1] 거래 시작 : BankAccountV1
15:53:24.449 [       t2] 거래 시작 : BankAccountV1
15:53:24.460 [       t1] [검증 시작] 출금액: 800, 잔액: 1000
15:53:24.461 [       t1] [검증 완료] 출금액: 800, 잔액: 1000
15:53:24.461 [       t2] [검증 시작] 출금액: 800, 잔액: 1000
15:53:24.461 [       t2] [검증 완료] 출금액: 800, 잔액: 1000
15:53:24.936 [     main] t1 state: TIMED_WAITING
15:53:24.937 [     main] t2 state: TIMED_WAITING
15:53:25.466 [       t2] [출금 완료] 출금액: 800, 잔액: 200
15:53:25.466 [       t1] [출금 완료] 출금액: 800, 잔액: 200
15:53:25.467 [       t2] 거래 종료
15:53:25.468 [       t1] 거래 종료
15:53:25.469 [     main] 최종잔액 : 200
```

(영한님은 -600이 나옴)

**참고**: 실행환경에 따라서 t1, t2가 동시에 실행될 수도 있다. 이 경우 출금액은 같고, 잔액은 200원이 된다. 이 부분은 바로 뒤에서 설명한다. (내가 그럼)


### **동시성 문제**

이 시나리오는 악의적인 사용자가 2대의 PC에서 동시에 같은 계좌의 돈을 출금한다고 가정한다.

`t1` , `t2` , 스레드는 거의 동시에 실행되지만, 아주 약간의 차이로 `t1` 스레드가 먼저 실행되고, `t2` 스레드가 그 다음에 실행된다고 가정하겠다.

처음 계좌의 잔액은 1000원이다. `t1` 스레드가 800원을 출금하면 잔액는 200원이 남는다.

이제 계좌의 잔액은 200원이다. `t2` 스레드가 800원을 출금하면 잔액보다 더 많은 돈을 출금하게 되므로 출금에 실패해야 한다.

그런데 실행 결과를 보면 기대와는 다르게 `t1` , `t2` 는 각각 800원씩 총 1600원 출금에 성공한다.

계좌의 잔액는 `-600` 원이 되어있고, 계좌는 예상치 못하게 마이너스 금액이 되어버렸다.

악의적인 사용자는 2대의 PC를 통해 자신의 계좌에 있는 1000원 보다 더 많은 금액인 1600원 출금에 성공한다. 

분명히 계좌를 출금할 때 잔고를 체크하는 로직이 있는데도 불구하고, 왜 이런 문제가 발생했을까?

**계좌 출금시 잔고 체크 로직** 

```java
if (balance < amount) {
    log("[검증 실패] 출금액: " + amount + ", 잔액: " + balance);
    return false;
}
```

**참고**: `balance` 값에 `volatile` 을 도입하면 문제가 해결되지 않을까? 그렇지 않다. 

`volatile` 은 한 스레드가 값을 변경했을 때 다른 스레드에서 변경된 값을 즉시 볼 수 있게 하는 메모리 가시성의 문제를 해결할 뿐이다. 

예를 들어 `t1` 스레드가 `balance` 의 값을 변경했을 때, `t2` 스레드에서 `balance` 의 변경된 값을 즉시 확인해도 여전히 같은 문제가 발생한다. 

이 문제는 메모리 가시성 문제를 해결해도 여전히 발생한다.


## 동시성 문제

### t1, t2 순서로 실행 가정

<img width="693" alt="Screenshot 2024-11-02 at 16 05 47" src="https://github.com/user-attachments/assets/e06d1e98-fb72-44c3-8a03-02fad153f60f">

`t1` 이 약간 먼저 실행되면서, 출금을 시도한다.

`t1` 이 출금 코드에 있는 검증 로직을 실행한다. 

이때 잔액이 출금 액수보다 많은지 확인한다. 

잔액[1000]이 출금액[800] 보다 많으므로 검증 로직을 통과한다.

<img width="674" alt="Screenshot 2024-11-02 at 16 05 59" src="https://github.com/user-attachments/assets/0119c9c8-5787-4ec0-a07f-ff1bb8a22b23">

`t1` : 출금 검증 로직을 통과해서 출금을 위해 잠시 대기중이다. 

출금에 걸리는 시간으로 생각하자.

`t2` : 검증 로직을 실행한다. 잔액이 출금 금액보다 많은지 확인한다. 

잔액[1000]이 출금액[800] 보다 많으므로 통과한다.

**바로 이 부분이 문제다! t1이 아직 잔액(balance)를 줄이지 못했기 때문에 t2는 검증 로직에서 현재 잔액을 1000원으 로 확인한다.**

`t1` 이 검증 로직을 통과하고 바로 잔액을 줄였다면 이런 문제가 발생하지 않겠지만, `t1` 이 검증 로직을 통과하고 잔액을 줄이기도 전에 먼저 `t2` 가검증 로직을 확인한 것이다.

그렇다면 `sleep(1000)` 코드를 빼면 되지 않을까? 

이렇게하면 `t1` 이 검증 로직을 통과하고 바로 잔액을 줄일 수 있을 것 같다. 

하지만 `t1` 이 검증 로직을 통과하고 `balance = balance - amount` 를 계산하기 직전에 `t2` 가 실행 되면서 검증 로직을 통과할 수도 있다. 

`sleep(1000)` 은 단지 이런 문제를 쉽게 확인하기 위해 넣었을 뿐이다.

<img width="678" alt="Screenshot 2024-11-02 at 16 06 05" src="https://github.com/user-attachments/assets/77f849e7-ac03-4938-9b7a-93eb80b4a7a2">

결과적으로 `t1` , `t2` 모두 검증 로직을 통과하고, 출금을 위해 잠시 대기중이다. 

출금에 걸리는 시간으로 생각하자.

<img width="680" alt="Screenshot 2024-11-02 at 16 06 11" src="https://github.com/user-attachments/assets/44ee0a08-9d44-47a3-9ea3-b4d0c1a24c4f">

`t1` 은 800원을 출금하면서, 잔액을 1000원에서 출금 액수인 800원 만큼 차감한다. 

**이제 계좌의 잔액은 200원 이 된다.**

<img width="673" alt="Screenshot 2024-11-02 at 16 06 15" src="https://github.com/user-attachments/assets/b93bc73d-4a11-4678-baf6-1e1530ee1f1e">

`t2` 는 800원을 출금하면서, 잔액을 200원에서 출금 액수인 800원 만큼 차감한다. 

**이제 잔액은 -600원이 된다.**

(나는 동시에 실행이 되어서 200원이 됌)

<img width="675" alt="Screenshot 2024-11-02 at 16 06 19" src="https://github.com/user-attachments/assets/6fa5f972-3164-4f9e-91e6-66c5c1d1c564">

**결과**
`t1` : 800원 출금 완료
`t2` : 800원 출금 완료
처음 원금은 1000원이었는데, 최종 잔액는 -600원이 된다. 

은행 입장에서 마이너스 잔액이 있으면 안된다!

나의 경우 200원이 나와서 정상인것처럼 보이지만 사실 t2가 실행이 될때 검증 실패가 떠야한다.

### t1, t2 동시에 실행 가정

t1, t2가 완전히 동시에 실행되는 상황을 알아보자.

<img width="675" alt="Screenshot 2024-11-02 at 16 06 27" src="https://github.com/user-attachments/assets/93a82289-f6ee-4031-a139-1e627a7c73aa">

`t1` , `t2` 는 동시에 검증 로직을 실행한다. 잔액이 출금 금액보다 많은지 확인한다. 

잔액[1000]이 출금액[800] 보다 많으므로 둘다 통과한다.

<img width="678" alt="Screenshot 2024-11-02 at 16 06 31" src="https://github.com/user-attachments/assets/82b262be-4d15-4ec8-bb08-210a6618be2a">

결과적으로 `t1` , `t2` 모두 검증 로직을 통과하고, 출금을 위해 잠시 대기중이다. 

출금에 걸리는 시간으로 생각하자.

<img width="683" alt="Screenshot 2024-11-02 at 16 06 37" src="https://github.com/user-attachments/assets/f61dafea-13c2-4c18-99e8-2d6a5416f0ed">

`t1` 은 800원을 출금하면서, 잔액을 1000원에서 출금 액수인 800원 만큼 차감한다. 

**이제 잔액은 200원이 된다.**

`t2` 은 800원을 출금하면서, 잔액을 1000원에서 출금 액수인 800원 만큼 차감한다. 

**이제 잔액은 200원이 된다.**

`t1` , `t2` 가 동시에 실행되기 때문에 둘다 잔액( `balance` )을 확인하는 시점에 잔액은 1000원이다!

`t1` , `t2` 둘다 동시에 계산된 결과를 잔액에 반영하는데, 둘다 계산 결과인 200원을 반영하므로 **최종 잔액은 200 원이 된다.**

```
balance = balance - amount; 
```

이 코드는 다음의 단계로 이루어진다.

1. 계산을 위해 오른쪽에 있는 `balance` 값과 `amount` 값을 조회한다.
2. 두 값을 계산한다.
3. 계산 결과를 왼쪽의 `balance` 변수에 저장한다.

여기서 1번 단계의 `balance` 값을 조회할 때 `t1` , `t2` 두 스레드가 동시에 `x001.balance` 의 필드 값을 읽는다. 

이때 값은 `1000` 이다. 따라서 두 스레드는 모두 잔액을 1000원으로 인식한다.

2번 단계에서 두 스레드 모두 `1000 - 800` 을 계산해서 `200` 이라는 결과를 만든다.

3번 단계에서 두 스레드 모두 `balance = 200` 을 대입한다.

<img width="678" alt="Screenshot 2024-11-02 at 16 06 45" src="https://github.com/user-attachments/assets/899261a5-7e09-45fd-9bcc-1d24dd5906a5">

**결과**

`t1` : 800원 출금완료

`t2` : 800원 출금완료

원래 원금이 1000원이었는데, 최종 잔액는 200원이 된다.

은행 입장에서 보면 총 1600원이 빠져나갔는데, 잔액는 800원만 줄어들었다.

800원이 감쪽같이 어디론가 사라 진 것이다!

이 문제가 왜 발생했고, 또 이런 문제를 어떻게 해결할 수 있을까?

## 임계영역

이런 문제가 발생한 근본 원인은 여러 스레드가 함께 사용하는 공유 자원을 여러 단계로 나누어 사용하기 때문이다. 

**1. 검증 단계**: 잔액( `balance` )이 출금액( `amount` ) 보다 많은지 확인한다.

**2. 출금 단계**: 잔액( `balance` )을 출금액( `amount` ) 만큼 줄인다.

```java
출금() {
1. 검증 단계: 잔액(balance) 확인 2. 출금 단계: 잔액(balance) 감소
}
```

**이 로직에는 하나의 큰 가정이 있다.**

스레드 하나의 관점에서 `출금()` 을 보면 1. 검증 단계에서 확인한 잔액( `balance` ) 1000원은 2. 출금 단계에서 계산 을 끝마칠 때 까지 같은 1000원으로 유지되어야 한다. 

그래야 검증 단계에서 확인한 금액으로, 출금 단계에서 정확한 잔액을 계산할 수 있다.

그래야 검증 단계에서 확인한 1000원에 800원을 차감해서 200원이라는 잔액을 정확하게 계산할 수 있다.

결국 여기서는 내가 사용하는 값이 중간에 변경되지 않을 것이라는 가정이 있다.

그런데 만약 중간에 다른 스레드가 잔액의 값을 변경한다면, 큰 혼란이 발생한다. 

1000원이라 생각한 잔액이 다른 값으 로 변경되면 잔액이 전혀 다른 값으로 계산될 수 있다.

### **공유 자원**
잔액( `balance` )은 여러 스레드가 함께 사용하는 공유 자원이다. 

따라서 출금 로직을 수행하는 중간에 다른 스레드에서 이 값을 얼마든지 변경할 수 있다. 

참고로 여기서는 `출금()` 메서드를 호출할 때만 잔액( `balance` )의 값이 변경된다. 

따라서 다른 스레드가 출금 메서드를 호출하면서, 사용중인 출금 값을 중간에 변경해 버릴 수 있다.

### **한 번에 하나의 스레드만 실행**

만약 `출금()` 이라는 메서드를 한 번에 하나의 스레드만 실행할 수 있게 제한한다면 어떻게 될까?

예를 들어 `t1` , `t2` 스레드가 함께 `출금()` 을 호출하면 `t1` 스레드가 먼저 처음부터 끝까지 `출금()` 메서드를 완료하고, 그 다음에 `t2` 스레드가 처음부터 끝까지 `출금()` 메서드를 완료하는 것이다.

이렇게하면 공유자원인 `balance` 를 한번에 하나의 스레드만 변경할 수 있다. 

따라서 계산 중간에 다른 스레드가 `balance` 의 값을 변경하는 부분을 걱정하지 않아도 된다. (참고로 여기서는 `출금()` 메서드를 호출할 때만 잔액( `balance` )의 값이 변경된다.)

더 자세히는 출금을 진행할 때 잔액( `balance` )을 검증하는 단계부터 잔액의 계산을 완료할 때 까지 잔액의 값은 중간에 변하면 안된다.

이 검증과 계산 이 두 단계는 한 번에 하나의 스레드만 실행해야 한다. 그래야 잔액(`balance` )이 중간에 변하지 않고, 안전하게 계산을 수행할 수 있다.

### **임계 영역(critical section)** 

영어로 크리티컬 섹션이라 한다.

여러 스레드가 동시에 접근하면 데이터 불일치나 예상치 못한 동작이 발생할 수 있는 위험하고 또 중요한 코드 부분을 뜻한다.

여러 스레드가 동시에 접근해서는 안 되는 공유 자원을 접근하거나 수정하는 부분을 의미한다.

예) 공유 변수나 공유 객체를 수정

앞서 우리가 살펴본 `출금()` 로직이 바로 임계 영역이다.

더 자세히는 출금을 진행할 때 잔액( `balance` )을 검증하는 단계부터 잔액의 계산을 완료할 때 까지가 임계 영역이다. 

여기서 `balance` 는 여러 스레드가 동시에 접근해서는 안되는 공유 자원이다.

이런 임계 영역은 한 번에 하나의 스레드만 접근할 수 있도록 안전하게 보호해야 한다.

그럼 어떻게 한 번에 하나의 스레드만 접근할 수 있도록 임계 영역을 안전하게 보호할 수 있을까?

여러가지 방법이 있지만 자바는 `synchronized` 키워드를 통해 아주 간단하게 임계 영역을 보호할 수 있다.

## synchronized 메서드

자바의 `synchronized` 키워드를 사용하면 한 번에 하나의 스레드만 실행할 수 있는 코드 구간을 만들 수 있다.


```java
public class BankAccountV2 implements BankAccount {

    volatile private int balance;

    public BankAccountV2(int initialBalance) {
        this.balance = initialBalance;
    }

    @Override
    public synchronized boolean withdraw(int amount) {
        log("거래 시작 : " + getClass().getSimpleName());

        log("[검증 시작] 출금액: " + amount + ", 잔액: " + balance);

        if(balance < amount) {
            log("[검증 실패]");
            return false;
        }

        log("[검증 완료] 출금액: " + amount + ", 잔액: " + balance);

        sleep(1000);

        balance -= amount;

        log("[출금 완료] 출금액: " + amount + ", 잔액: " + balance);

        log("거래 종료");

        return false;
    }


    @Override
    public int getBalance() {
        return balance;
    }
}
```

```shell
16:50:48.257 [       t1] 거래 시작 : BankAccountV2
16:50:48.263 [       t1] [검증 시작] 출금액: 800, 잔액: 1000
16:50:48.263 [       t1] [검증 완료] 출금액: 800, 잔액: 1000
16:50:48.741 [     main] t1 state: TIMED_WAITING
16:50:48.741 [     main] t2 state: BLOCKED
16:50:49.269 [       t1] [출금 완료] 출금액: 800, 잔액: 200
16:50:49.269 [       t1] 거래 종료
16:50:49.270 [       t2] 거래 시작 : BankAccountV2
16:50:49.270 [       t2] [검증 시작] 출금액: 800, 잔액: 200
16:50:49.270 [       t2] [검증 실패]
16:50:49.274 [     main] 최종잔액 : 200
```

환경에 따라 t2가 먼저 시작될 수 있다.

### synchronized 분석

지금부터 자바의 `synchronized` 가 어떻게 작동하는지 그림으로 분석해보자.

참고로 실행 결과를 보면 `t2` 가 `BLOCKED` 상태인데, 이 상태도 확인해보자.

<img width="699" alt="Screenshot 2024-11-02 at 16 51 55" src="https://github.com/user-attachments/assets/ffd7d061-b712-4767-850b-5b78ecab24f2">


**모든 객체(인스턴스)는 내부에 자신만의 락( `lock` )을 가지고 있다.** 모니터 락(monitor lock)이라도고 부른다.

객체 내부에 있고 우리가 확인하기는 어렵다.

스레드가 `synchronized` 키워드가 있는 메서드에 진입하려면 반드시 해당 인스턴스의 락이 있어야 한다! 

여기서는 `BankAccount(x001)` 인스턴스의 `synchronized withdraw()` 메서드를 호출하므로 이 인스턴스의 락이 필요하다.

스레드 `t1` , `t2` 는 `withdraw()` 를 실행하기 직전이다.

<img width="705" alt="Screenshot 2024-11-02 at 16 52 12" src="https://github.com/user-attachments/assets/e5a30646-b1f4-4c76-9c59-b97b48d03c2d">



<img width="699" alt="Screenshot 2024-11-02 at 16 52 16" src="https://github.com/user-attachments/assets/077ea4ae-5955-4f03-953e-5a7eda2d7fe1">

<img width="699" alt="Screenshot 2024-11-02 at 16 52 24" src="https://github.com/user-attachments/assets/d442cb4e-c3aa-4878-80e6-7735cf5fa2ab">


<img width="706" alt="Screenshot 2024-11-02 at 16 52 30" src="https://github.com/user-attachments/assets/5111bed1-2bbd-47d5-b213-5586af7ed534">


<img width="697" alt="Screenshot 2024-11-02 at 16 52 35" src="https://github.com/user-attachments/assets/2fcbfeab-073f-4a17-b460-55d453aefda4">

<img width="695" alt="Screenshot 2024-11-02 at 16 52 40" src="https://github.com/user-attachments/assets/c87e614c-5ffc-4dab-9173-43c9a9d2153c">


<img width="698" alt="Screenshot 2024-11-02 at 16 52 44" src="https://github.com/user-attachments/assets/0a762498-658c-4470-9063-034dcf3d40d1">

<img width="693" alt="Screenshot 2024-11-02 at 16 52 48" src="https://github.com/user-attachments/assets/bc756667-3de9-4046-842c-1c8d4e977cfb">



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


<img width="693" alt="Screenshot 2024-11-02 at 16 05 47" src="https://github.com/user-attachments/assets/e06d1e98-fb72-44c3-8a03-02fad153f60f">

<img width="674" alt="Screenshot 2024-11-02 at 16 05 59" src="https://github.com/user-attachments/assets/0119c9c8-5787-4ec0-a07f-ff1bb8a22b23">

<img width="678" alt="Screenshot 2024-11-02 at 16 06 05" src="https://github.com/user-attachments/assets/77f849e7-ac03-4938-9b7a-93eb80b4a7a2">

<img width="680" alt="Screenshot 2024-11-02 at 16 06 11" src="https://github.com/user-attachments/assets/44ee0a08-9d44-47a3-9ea3-b4d0c1a24c4f">

<img width="673" alt="Screenshot 2024-11-02 at 16 06 15" src="https://github.com/user-attachments/assets/b93bc73d-4a11-4678-baf6-1e1530ee1f1e">

<img width="675" alt="Screenshot 2024-11-02 at 16 06 19" src="https://github.com/user-attachments/assets/6fa5f972-3164-4f9e-91e6-66c5c1d1c564">

<img width="675" alt="Screenshot 2024-11-02 at 16 06 27" src="https://github.com/user-attachments/assets/93a82289-f6ee-4031-a139-1e627a7c73aa">

<img width="678" alt="Screenshot 2024-11-02 at 16 06 31" src="https://github.com/user-attachments/assets/82b262be-4d15-4ec8-bb08-210a6618be2a">

<img width="683" alt="Screenshot 2024-11-02 at 16 06 37" src="https://github.com/user-attachments/assets/f61dafea-13c2-4c18-99e8-2d6a5416f0ed">

<img width="678" alt="Screenshot 2024-11-02 at 16 06 45" src="https://github.com/user-attachments/assets/899261a5-7e09-45fd-9bcc-1d24dd5906a5">











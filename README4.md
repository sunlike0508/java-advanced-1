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





















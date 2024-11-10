# 생산자 소비자 문제 1

생산자 소비자 문제는 멀티스레드 프로그래밍에서 자주 등장하는 동시성 문제 중 하나로, 여러 스레드가 동시에 데이터를 생산하고 소비하는 상황을 다룬다.

**멀티스레드의 핵심을 제대로 이해하려면 반드시 생산자 소비자 문제를 이해하고, 올바른 해결 방안도 함께 알아두어야 한다.**

**생산자 소비자 문제를 제대로 이해하면 멀티스레드를 제대로 이해했다고 볼 수 있다. 그 만큼 중요한 내용이다.**

**기본 개념**

* **생산자(Producer)**: 데이터를 생성하는 역할을 한다. 예를 들어, 파일에서 데이터를 읽어오거나 네트워크에서 데이터를 받아오는 스레드가 생산자 역할을 할 수 있다
  * 앞서 프린터 예제에서 사용자의 입력을 프린터 큐에 전달하는 스레드가 생산자의 역할이다. 
* **소비자(Consumer)**: 생성된 데이터를 사용하는 역할을 한다. 예를 들어, 데이터를 처리하거나 저장하는 스레드가 소비자 역할을 할 수 있다.
  * 앞서 프린터 예제에서 프린터 큐에 전달된 데이터를 받아서 출력하는 스레드가 소비자 역할이다.
* **버퍼(Buffer)**: 생산자가 생성한 데이터를 일시적으로 저장하는 공간이다. 이 버퍼는 한정된 크기를 가지며, 생산자와 소비자가 이 버퍼를 통해 데이터를 주고받는다.
  * 앞서 프린터 예제에서 프린터 큐가 버퍼 역할이다.

**문제 상황**

**생산자가 너무 빠를 때**: 

* 버퍼가 가득 차서 더 이상 데이터를 넣을 수 없을 때까지 생산자가 데이터를 생성한다.
* 버퍼가 가득 찬 경우 생산자는 버퍼에 빈 공간이 생길 때까지 기다려야 한다.

**소비자가 너무 빠를 때**: 

* 버퍼가 비어서 더 이상 소비할 데이터가 없을 때까지 소비자가 데이터를 처리한다.
* 버퍼가 비어있을 때 소비자는 버퍼에 새로운 데이터가 들어올 때까지 기다려야 한다. 
 
이 문제는 다음 두 용어로 불린다. 참고로 둘다 같은 뜻이다.

**생산자 소비자 문제**(producer-consumer problem): 

* 생산자 소비자 문제는, 생산자 스레드와 소비자 스레드가 특정 자원을 함께 생산하고, 소비하면서 발생하는 문제이다.

**한정된 버퍼 문제**(bounded-buffer problem): 

* 이 문제는 결국 중간에 있는 버퍼의 크기가 한정되어 있기 때문 에 발생한다. 따라서 한정된 버퍼 문제라고도 한다.
* 예제를 통해서 생산자 소비자 문제가 왜 발생하는지, 그리고 어떤 해결 방안들이 있는지 예제 코드를 통해서 알아보자.
* 쉬운 문제가 아니므로 최대한 점진적으로 천천히 단계적으로 알아보겠다.

## 생산자 소비자 문제 - 예제1 코드

```java
public class BoundedQueueV1 implements BoundedQueue {

    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;


    public BoundedQueueV1(int max) {this.max = max;}


    @Override
    public synchronized void put(String data) {
        if(queue.size() == max) {
            log("[put] 큐가 가득 참, 버림 : " + data);
            return;
        }

        queue.offer(data);
    }


    @Override
    public synchronized String take() {
        if(queue.isEmpty()) {
            return null;
        }
        return queue.poll();
    }


    @Override
    public String toString() {
        return queue.toString();
    }
}
```

**주의!**: 원칙적으로 `toString()` 에도 `synchronized` 를 적용해야 한다. 

그래야 `toString()` 을 통한 조회 시점에도 정확한 데이터를 조회할 수 있다. 

하지만 이 부분이 이번 설명의 핵심이 아니고, 또 예제 코드를 단순하게 유지하기 위해 여기서는 `toString()` 에 `synchronized` 를 사용하지 않겠다.

**임계 영역**

여기서 핵심 공유 자원은 바로 `queue(ArrayDeque)` 이다. 

여러 스레드가 접근할 예정이므로 `synchronized` 를 사용해서 한 번에 하나의 스레드만 `put()` 또는 `take()` 를 실행할 수 있도록 안전한 임계 영역을 만든다.

예) `put(data)` 을 호출할 때 `queue.size()` 가 `max` 가 아니어서, `queue.offer()` 를 호출하려고 한다. 

그런데 호출하기 직전에 다른 스레드에서 `queue` 에 데이터를 저장해서 `queue.size()` 가 `max` 로 변할 수 있다!


```java
public class ProducerTask implements Runnable {

  private BoundedQueue queue;
  private String request;

  public ProducerTask(BoundedQueue queue, String request) {
    this.queue = queue;
    this.request = request;
  }

  @Override
  public void run() {
    log("[생산 시도] " + request + " -> " + queue);
    queue.put(request);
    log("[생산 완료] " + request + " -> " + queue);
  }
}

public class ConsumerTask implements Runnable {
    public BoundedQueue queue;
    
    public ConsumerTask(BoundedQueue queue) {
        this.queue = queue;
    }
    @Override
    public void run() {

        log("[소비 시도]     ? <- " +  queue);
        String data = queue.take();
        log("[소비 완료] " +  data + " <- " + queue);
    }
}


public class BoundedMain {

  public static void main(String[] args) {
    BoundedQueue queue = new BoundedQueueV1(2);

    // 둘 중에 하나만 실행해야 함
    //producerFirst(queue); // 생산자 먼저 실행
    consumerFirst(queue); // 소비자 먼저 실행
  }


  private static void consumerFirst(BoundedQueue queue) {
    log("== [소비자 먼저 실행] 시작, " + queue.getClass().getSimpleName() + " ==");
    List<Thread> threads = new ArrayList<>();
    startConsumer(queue, threads);
    printAllState(queue, threads);
    startProducer(queue, threads);
    printAllState(queue, threads);
    log("== [소비자 먼저 실행] 종료, " + queue.getClass().getSimpleName() + " ==");
  }


  private static void producerFirst(BoundedQueue queue) {
    log("== [생산자 먼저 실행] 시작, " + queue.getClass().getSimpleName() + " ==");
    List<Thread> threads = new ArrayList<>();
    startProducer(queue, threads);
    printAllState(queue, threads);
    startConsumer(queue, threads);
    printAllState(queue, threads);
    log("== [생산자 먼저 실행] 종료, " + queue.getClass().getSimpleName() + " ==");
  }


  private static void startConsumer(BoundedQueue queue, List<Thread> threads) {
    System.out.println();
    log("소비자 시작");

    for(int i = 1; i <= 3; i++) {
      Thread consumer = new Thread(new ConsumerTask(queue), "consumer" + i);
      threads.add(consumer);
      consumer.start();
      sleep(100);
    }
  }


  private static void printAllState(BoundedQueue queue, List<Thread> threads) {
    System.out.println();
    log("현재 상태 출력, 큐 데이터: " + queue);
    for(Thread thread : threads) {
      log(thread.getName() + ": " + thread.getState());
    }
  }


  private static void startProducer(BoundedQueue queue, List<Thread> threads) {
    System.out.println();
    log("생산자 시작");

    for(int i = 1; i <= 3; i++) {
      Thread producer = new Thread(new ProducerTask(queue, "data" + i), "producer" + i);
      threads.add(producer);
      producer.start();
      sleep(100);
    }
  }
}
```

여기서 핵심은 스레드를 0.1초 단위로 쉬면서 순서대로 실행한다는 점이다. 

생산자 먼저인 `producerFirst` 를 호출하면 `producer1` `producer2` `producer3` `consumer1` `consumer2` `consumer3` 순서로 실행된다.

소비자 먼저인 `consumerFirst` 를 호출하면 `consumer1` `consumer2` `consumer3` `producer1` `producer2` `producer3` 순서로 실행된다.

참고로 여기서는 이해를 돕기 위해 이렇게 순서대로 실행했다. 실제로는 동시에 실행될 것이다.

```shell
23:05:54.410 [     main] == [생산자 먼저 실행] 시작, BoundedQueueV1 ==

23:05:54.412 [     main] 생산자 시작
23:05:54.419 [producer1] [생산 시도] data1 -> []
23:05:54.419 [producer1] [생산 완료] data1 -> [data1]
23:05:54.520 [producer2] [생산 시도] data2 -> [data1]
23:05:54.520 [producer2] [생산 완료] data2 -> [data1, data2]
23:05:54.626 [producer3] [생산 시도] data3 -> [data1, data2]
23:05:54.626 [producer3] [put] 큐가 가득 참, 버림 : data3
23:05:54.627 [producer3] [생산 완료] data3 -> [data1, data2]

23:05:54.727 [     main] 현재 상태 출력, 큐 데이터: [data1, data2]
23:05:54.727 [     main] producer1: TERMINATED
23:05:54.727 [     main] producer2: TERMINATED
23:05:54.727 [     main] producer3: TERMINATED

23:05:54.728 [     main] 소비자 시작
23:05:54.728 [consumer1] [소비 시도]     ? <- [data1, data2]
23:05:54.729 [consumer1] [소비 완료] data1 <- [data2]
23:05:54.833 [consumer2] [소비 시도]     ? <- [data2]
23:05:54.833 [consumer2] [소비 완료] data2 <- []
23:05:54.938 [consumer3] [소비 시도]     ? <- []
23:05:54.938 [consumer3] [소비 완료] null <- []

23:05:55.043 [     main] 현재 상태 출력, 큐 데이터: []
23:05:55.043 [     main] producer1: TERMINATED
23:05:55.043 [     main] producer2: TERMINATED
23:05:55.043 [     main] producer3: TERMINATED
23:05:55.043 [     main] consumer1: TERMINATED
23:05:55.043 [     main] consumer2: TERMINATED
23:05:55.043 [     main] consumer3: TERMINATED
23:05:55.044 [     main] == [생산자 먼저 실행] 종료, BoundedQueueV1 ==

22:53:16.011 [     main] == [소비자 먼저 실행] 시작, BoundedQueueV1 ==

22:53:16.013 [     main] 소비자 시작
22:53:16.016 [consumer1] [소비 시도]     ? <- []
22:53:16.018 [consumer1] [소비 완료] null <- []
22:53:16.121 [consumer2] [소비 시도]     ? <- []
22:53:16.121 [consumer2] [소비 완료] null <- []
22:53:16.225 [consumer3] [소비 시도]     ? <- []
22:53:16.225 [consumer3] [소비 완료] null <- []

22:53:16.330 [     main] 현재 상태 출력, 큐 데이터: []
22:53:16.330 [     main] consumer1: TERMINATED
22:53:16.330 [     main] consumer2: TERMINATED
22:53:16.330 [     main] consumer3: TERMINATED

22:53:16.330 [     main] 생산자 시작
22:53:16.331 [producer1] [생산 시도] data1 -> []
22:53:16.332 [producer1] [생산 완료] data1 -> [data1]
22:53:16.436 [producer2] [생산 시도] data2 -> [data1]
22:53:16.436 [producer2] [생산 완료] data2 -> [data1, data2]
22:53:16.541 [producer3] [생산 시도] data3 -> [data1, data2]
22:53:16.541 [producer3] [put] 큐가 가득 참, 버림 : data3
22:53:16.542 [producer3] [생산 완료] data3 -> [data1, data2]

22:53:16.641 [     main] 현재 상태 출력, 큐 데이터: [data1, data2]
22:53:16.642 [     main] consumer1: TERMINATED
22:53:16.642 [     main] consumer2: TERMINATED
22:53:16.642 [     main] consumer3: TERMINATED
22:53:16.642 [     main] producer1: TERMINATED
22:53:16.643 [     main] producer2: TERMINATED
22:53:16.643 [     main] producer3: TERMINATED
22:53:16.643 [     main] == [소비자 먼저 실행] 종료, BoundedQueueV1 ==
```

위에는 소비자 먼저로 실행했을 때 결과

## 생산자 소비자 문제 - 예제1 분석 - 생산자 우선

<img width="694" alt="Screenshot 2024-11-05 at 23 00 55" src="https://github.com/user-attachments/assets/40a97be9-6c31-48fb-8bc4-d9a1a43b1227">

`p1` : `producer1` 생산자 스레드를 뜻한다.

`c1` : `consumer1` 소비자 스레드를 뜻한다.

임계 영역은 `synchronized` 를 영역을 뜻한다. 

스레드가 이 영역에 들어가려면 모니터 락( `lock` )이 필요하다. 설명을 단순화 하기 위해`BoundedQueue` 의 버전 정보는 생략한다.

스레드가 처음부터 모두 생성되어 있는 것은 아니지만, 모두 그려두고 시작하겠다.

### **생산자 스레드 실행 시작**

<img width="698" alt="Screenshot 2024-11-05 at 23 01 02" src="https://github.com/user-attachments/assets/72ba38c5-3dac-48aa-94a7-f13ccd4dc5ec">

```shell
23:05:54.410 [     main] == [생산자 먼저 실행] 시작, BoundedQueueV1 ==

23:05:54.412 [     main] 생산자 시작
23:05:54.419 [producer1] [생산 시도] data1 -> []
```

<img width="689" alt="Screenshot 2024-11-05 at 23 01 34" src="https://github.com/user-attachments/assets/594a2c4b-49b7-4362-9708-fb8676eb01ff">

```shell
23:05:54.419 [producer1] [생산 완료] data1 -> [data1]
```

<img width="684" alt="Screenshot 2024-11-05 at 23 01 37" src="https://github.com/user-attachments/assets/766c2fe5-1e44-46a7-bb74-fdd08f3a7c84">

```shell
23:05:54.520 [producer2] [생산 시도] data2 -> [data1]
```

<img width="695" alt="Screenshot 2024-11-05 at 23 01 42" src="https://github.com/user-attachments/assets/42d2c569-2186-4f08-bd23-c823d34d8d6d">

```shell
23:05:54.520 [producer2] [생산 완료] data2 -> [data1, data2]
```

<img width="699" alt="Screenshot 2024-11-05 at 23 01 45" src="https://github.com/user-attachments/assets/2f2ecfff-b84e-4424-8945-222e13cedacf">

```shell
23:05:54.626 [producer3] [생산 시도] data3 -> [data1, data2]
23:05:54.626 [producer3] [put] 큐가 가득 참, 버림 : data3
```

`p3` 는 `data3` 을 큐에 저장하려고 시도한다.

하지만 큐가 가득 차 있기 때문에 더는 큐에 데이터를 추가할 수 없다. 따라서 `put()` 내부에서 `data3` 은 버린다.

**데이터를 버리지 않는 대안**

`data3` 을 버리지 않는 대안은, 큐에 빈 공간이 생길 때 까지 `p3` 스레드가 기다리는 것이다. 

언젠가는 소비자 스레드가 실행되어서 큐의 데이터를 가져갈 것이고, 큐에 빈 공간이 생기게 된다. 이때 큐에 데이터를 보관하는 것이다.

그럼 어떻게 기다릴 수 있을까?

단순하게 생각하면 생산자 스레드가 반복문을 사용해서 큐에 빈 공간이 생기는지 주기적으로 체크한 다음에, 만약 빈 공 간이 없다면 `sleep()` 을 짧게 사용해서 잠시 대기하고, 깨어난 다음에 다시 반복문에서 큐의 빈 공간을 체크하는 식으 로 구현하면 될 것 같다.

이후에 `BoundedQueueV2` 에서 이 방식으로 개선해보자.

<img width="680" alt="Screenshot 2024-11-05 at 23 01 50" src="https://github.com/user-attachments/assets/63314dc7-6476-4dd8-99fa-cff87cb9dfc2">

```shell
23:05:54.627 [producer3] [생산 완료] data3 -> [data1, data2]
```

### **생산자 스레드 실행 완료**

<img width="689" alt="Screenshot 2024-11-05 at 23 01 55" src="https://github.com/user-attachments/assets/a4c146da-a9b7-4f22-bb0c-9e0db8b88c4f">

```shell
22:53:16.330 [     main] 현재 상태 출력, 큐 데이터: []
22:53:16.330 [     main] consumer1: TERMINATED
22:53:16.330 [     main] consumer2: TERMINATED
22:53:16.330 [     main] consumer3: TERMINATED
```

### **소비자 스레드 실행 시작**

<img width="684" alt="Screenshot 2024-11-05 at 23 02 01" src="https://github.com/user-attachments/assets/b7a71e0f-86d7-4332-83ca-d15b2af25d7a">

```shell
23:05:54.728 [     main] 소비자 시작
23:05:54.728 [consumer1] [소비 시도]     ? <- [data1, data2]
```

<img width="674" alt="Screenshot 2024-11-05 at 23 02 10" src="https://github.com/user-attachments/assets/6bd79a62-5a03-4dc5-817a-8e89530e3daf">

```shell
23:05:54.729 [consumer1] [소비 완료] data1 <- [data2]
```

<img width="677" alt="Screenshot 2024-11-05 at 23 03 05" src="https://github.com/user-attachments/assets/58932973-720f-495b-8243-c2ce8d82c69e">

```shell
23:05:54.833 [consumer2] [소비 시도]     ? <- [data2]
```

<img width="686" alt="Screenshot 2024-11-05 at 23 03 11" src="https://github.com/user-attachments/assets/c8308ae4-fe75-4a1c-9d3d-9a68abaade81">

```shell
23:05:54.833 [consumer2] [소비 완료] data2 <- []
```

<img width="683" alt="Screenshot 2024-11-05 at 23 03 17" src="https://github.com/user-attachments/assets/aaa27de0-1f43-47fc-a00b-bd27aae3741c">

```shell
23:05:54.938 [consumer3] [소비 시도]     ? <- []
```

`c3` 는 큐에서 데이터를 획득하려고 한다.

하지만 큐에 데이터가 없기 때문에 데이터를 획득할 수 없다. 

따라서 대신에 `null` 을 반환한다.

**큐에 데이터가 없다면 기다리자**

소비자 입장에서 큐에 데이터가 없다면 기다리는 것도 대안이다.

`null` 을 받지 않는 대안은, 큐에 데이터가 추가될 때 까지 `c3` 스레드가 기다리는 것이다. 

언젠가는 생산자 스레드가 실행되어서 큐에 데이터를 추가할 것이다. 

물론 생산자 스레드가 계속해서 데이터를 생산한다는 가정이 필요하다. 그럼 어떻게 기다릴 수 있을까?

단순하게 생각하면 소비자 스레드가 반복문을 사용해서 큐에 데이터가 있는지 주기적으로 체크한 다음에, 만약 데이터 가 없다면 `sleep()` 을 짧게 사용해서 잠시 대기하고, 깨어난 다음에 다시 반복문에서 큐에 데이터가 있는지 체크하는 식으로 구현하면 될 것 같다.

`BoundedQueueV2` 에서 이 방식으로 개선해보자.

생각해보면 큐에 데이터가 없는 상황은 앞서 큐의 데이터가 가득찬 상황과 비슷하다. 

한정된 버퍼(Bounded buffer) 문제는 이렇듯 버퍼에 데이터가 가득 찬 상황에 데이터를 생산해서 추가할 때도 문제가 발생하고, 큐에 데이터가 없는데 데이터를 소비할 때도 문제가 발생한다.

<img width="684" alt="Screenshot 2024-11-05 at 23 03 27" src="https://github.com/user-attachments/assets/80d50983-fe50-433b-b126-70b2eb5eeb9f">

```shell
23:05:54.938 [consumer3] [소비 완료] null <- []
```

### **소비자 스레드 실행 완료**

<img width="697" alt="Screenshot 2024-11-05 at 23 03 38" src="https://github.com/user-attachments/assets/6f66ea67-bb91-4abb-a4db-4e33e110297a">

```shell
23:05:55.043 [     main] 현재 상태 출력, 큐 데이터: []
23:05:55.043 [     main] producer1: TERMINATED
23:05:55.043 [     main] producer2: TERMINATED
23:05:55.043 [     main] producer3: TERMINATED
23:05:55.043 [     main] consumer1: TERMINATED
23:05:55.043 [     main] consumer2: TERMINATED
23:05:55.043 [     main] consumer3: TERMINATED
23:05:55.044 [     main] == [생산자 먼저 실행] 종료, BoundedQueueV1 ==
```

결과적으로 버퍼가 가득차서 `p3` 가 생산한 `data3` 은 버려졌다. 

그리고 `c3` 가 데이터를 조회하는 시점에 버퍼는 비어 있어서 데이터를 받지 못하고 `null` 값을 받았다. 

스레드가 대기하며 기다릴 수 있다면 `p3` 가 생산한 `data3` 을 `c3` 가 받을 수도 있었을 것이다.

## 소비자 우선

### **실행 전**

<img width="474" alt="Screenshot 2024-11-06 at 22 09 37" src="https://github.com/user-attachments/assets/256db181-bb06-4d3e-b2b8-9c7d8c742525">

### **소비자 스레드 실행 시작**

<img width="458" alt="Screenshot 2024-11-06 at 22 11 38" src="https://github.com/user-attachments/assets/09f60e74-3bee-40e2-8660-2163af491153">

```shell
22:53:16.013 [     main] 소비자 시작
22:53:16.016 [consumer1] [소비 시도]     ? <- []
```

<img width="464" alt="Screenshot 2024-11-06 at 22 11 51" src="https://github.com/user-attachments/assets/301f68d5-52a4-413d-97ef-0ae5a726a5c2">

```shell
22:53:16.018 [consumer1] [소비 완료] null <- []
```

### **소비자 스레드 실행 완료**

<img width="465" alt="Screenshot 2024-11-06 at 22 11 55" src="https://github.com/user-attachments/assets/e1131b7c-88f6-4952-9317-a18db5fc164a">

```shell
22:53:16.121 [consumer2] [소비 시도]     ? <- []
22:53:16.121 [consumer2] [소비 완료] null <- []
22:53:16.225 [consumer3] [소비 시도]     ? <- []
22:53:16.225 [consumer3] [소비 완료] null <- []
22:53:16.330 [     main] 현재 상태 출력, 큐 데이터: []
22:53:16.330 [     main] consumer1: TERMINATED
22:53:16.330 [     main] consumer2: TERMINATED
22:53:16.330 [     main] consumer3: TERMINATED
```

큐에 데이터가 없으므로 `null` 을 반환한다. 

결과적으로 `c1` , `c2` , `c3` 모두 데이터를 받지 못하고 종료된다.

언젠가 생산자가 데이터를 넣어준다고 가정해보면 `c1` , `c2` , `c3` 스레드는 큐에 데이터가 추가될 때 까지 기다리는 것도 방법이다. (이 부분은 뒤에서 구현한다.)


### **생산자 스레드 실행 시작**

<img width="463" alt="Screenshot 2024-11-06 at 22 12 00" src="https://github.com/user-attachments/assets/c8eb453e-faa6-4aed-9b42-c9ec788cb383">

```shell
22:53:16.330 [     main] 생산자 시작
22:53:16.331 [producer1] [생산 시도] data1 -> []
```

<img width="467" alt="Screenshot 2024-11-06 at 22 12 07" src="https://github.com/user-attachments/assets/2a4a702e-d228-4438-98b8-d7c5d2ff9b9c">

```shell
22:53:16.332 [producer1] [생산 완료] data1 -> [data1]
```

<img width="467" alt="Screenshot 2024-11-06 at 22 12 23" src="https://github.com/user-attachments/assets/e260ef5d-5e3e-4773-a521-6b4c3748f0d3">

```shell
22:53:16.436 [producer2] [생산 시도] data2 -> [data1]
```

<img width="458" alt="Screenshot 2024-11-06 at 22 12 29" src="https://github.com/user-attachments/assets/29c9ae1f-222b-4040-a8a6-9d6005ad2587">

```shell
22:53:16.436 [producer2] [생산 완료] data2 -> [data1, data2]
```

<img width="475" alt="Screenshot 2024-11-06 at 22 12 32" src="https://github.com/user-attachments/assets/2f68ff94-aaea-406d-94e9-0d676a2efb48">

```shell
22:53:16.541 [producer3] [생산 시도] data3 -> [data1, data2]
22:53:16.541 [producer3] [put] 큐가 가득 참, 버림 : data3
```

`p3` 의 경우 큐에 데이터가 가득 차서 `data3` 을 포기하고 버린다.

소비자가 계속해서 큐의 데이터를 가져간다고 가정하면, `p3` 스레드는 기다리는 것도 하나의 방법이다.

<img width="456" alt="Screenshot 2024-11-06 at 22 12 37" src="https://github.com/user-attachments/assets/9621dfca-ea2e-43fd-a86c-9f80b77f77b2">

```shell
22:53:16.542 [producer3] [생산 완료] data3 -> [data1, data2]
```

### **생산자 스레드 실행 완료**

<img width="459" alt="Screenshot 2024-11-06 at 22 12 50" src="https://github.com/user-attachments/assets/ee794268-376d-49da-8c3b-b10fad5aeea1">

```shell
22:53:16.641 [     main] 현재 상태 출력, 큐 데이터: [data1, data2]
22:53:16.642 [     main] consumer1: TERMINATED
22:53:16.642 [     main] consumer2: TERMINATED
22:53:16.642 [     main] consumer3: TERMINATED
22:53:16.642 [     main] producer1: TERMINATED
22:53:16.643 [     main] producer2: TERMINATED
22:53:16.643 [     main] producer3: TERMINATED
22:53:16.643 [     main] == [소비자 먼저 실행] 종료, BoundedQueueV1 ==
```


### 문제점

**생산자 스레드 먼저 실행**의 경우 `p3` 가 보관하는 `data3` 은 버려지고, `c3` 는 데이터를 받지 못한다. ( `null` 을 받는다.)

**소비자 스레드 먼저 실행**의 경우 `c1` , `c2` , `c3` 는 데이터를 받지 못한다.( `null` 을 받는다.) 그리고 `p3` 가 보관하 는 `data3` 은 버려진다.

예제는 단순하게 설명하기 위해 생산자 스레드 3개, 소비자 스레드 3개를 한 번만 실행했지만, 실제로 이런 생산자 소비 자 구조는 보통 계속해서 실행된다. 

레스토랑에 손님은 계속 찾아오고, 음료 공장은 계속해서 음료를 만들어낸다.

쇼핑 몰이라면 고객은 계속해서 주문을 한다.

**버퍼가 가득 찬 경우**: 생산자 입장에서 버퍼에 여유가 생길 때 까지 조금만 기다리면 되는데, 기다리지 못하고, 데이터를 버리는 것은 아쉽다.

**버퍼가 빈 경우**: 소비자 입장에서 버퍼에 데이터가 채워질 때 까지 조금만 기다리면 되는데, 기다리지 못하고, `null` 데이터를 얻는 것은 아쉽다.

문제의 해결 방안은 단순하다. 

앞서 설명한 것처럼 스레드가 기다리면 되는 것이다! 그럼 기다리도록 구현해보자.

## 생산자 소비자 문제 - 예제2 코드

이번에는 각 상황에 맞추어 스레드가 기다리도록 해보자.

```java
public class BoundedQueueV2 implements BoundedQueue {

    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;


    public BoundedQueueV2(int max) {this.max = max;}


    @Override
    public synchronized void put(String data) {
        while(queue.size() == max) {
            log("[put] 큐가 가득 참, 생산자 대기");
            sleep(1000);
        }

        queue.offer(data);
    }


    @Override
    public synchronized String take() {
        while(queue.isEmpty()) {
            log("[take] 큐에 데이터가 없음, 소비자 대기");
            sleep(1000);
        }
        return queue.poll();
    }


    @Override
    public String toString() {
        return queue.toString();
    }
}
```

**put(data) - 데이터를 버리지 않는 대안**

`data3` 을 버리지 않는 대안은, 큐가 가득 찾을 때, 큐에 빈 공간이 생길 때 까지, 생산자 스레드가 기다리면 된다. 

언젠가는 소비자 스레드가 실행되어서 큐의 데이터를 가져갈 것이고, 그러면 큐에 데이터를 넣을 수 있는 공간이 생기게 된다.

그럼 어떻게 기다릴 수 있을까?

여기서는 생산자 스레드가 반복문을 사용해서 큐에 빈 공간이 생기는지 주기적으로 체크한다. 만약 빈 공간이 없다면 `sleep()` 을 사용해서 잠시 대기하고, 깨어난 다음에 다시 반복문에서 큐의 빈 공간을 체크하는 식으로 구현했다.

**take() - 큐에 데이터가 없다면 기다리자**

소비자 입장에서 큐에 데이터가 없다면 기다리는 것도 대안이다.

큐에 데이터가 없을때 `null` 을 받지 않는 대안은,큐에 데이터가 추가될 때까지 소비자 스레드가 기다리는 것이다. 언젠가는 생산자 스레드가 실행되어서 큐의 데이터를 추가할 것이고, 큐에 데이터가 생기게 된다. 

물론 생산자 스레드가 계속해서 데이터를 생산한다는 가정이 필요하다.

그럼 어떻게 기다릴 수 있을까?

여기서는 소비자 스레드가 반복문을 사용해서 큐에 데이터가 있는지 주기적으로 체크한 다음에, 만약 데이터가 없다면 `sleep()` 을 사용해서 잠시 대기하고, 깨어난 다음에 다시 반복문에서 큐에 데이터가 있는지 체크하는 식으로 구현했다.

```shell
22:34:38.795 [     main] == [생산자 먼저 실행] 시작, BoundedQueueV2 ==

22:34:38.796 [     main] 생산자 시작
22:34:38.803 [producer1] [생산 시도] data1 -> []
22:34:38.803 [producer1] [생산 완료] data1 -> [data1]
22:34:38.904 [producer2] [생산 시도] data2 -> [data1]
22:34:38.904 [producer2] [생산 완료] data2 -> [data1, data2]
22:34:39.009 [producer3] [생산 시도] data3 -> [data1, data2]
22:34:39.010 [producer3] [put] 큐가 가득 참, 생산자 대기

22:34:39.114 [     main] 현재 상태 출력, 큐 데이터: [data1, data2]
22:34:39.115 [     main] producer1: TERMINATED
22:34:39.115 [     main] producer2: TERMINATED
22:34:39.115 [     main] producer3: TIMED_WAITING

22:34:39.115 [     main] 소비자 시작
22:34:39.116 [consumer1] [소비 시도]     ? <- [data1, data2]
22:34:39.218 [consumer2] [소비 시도]     ? <- [data1, data2]
22:34:39.323 [consumer3] [소비 시도]     ? <- [data1, data2]

22:34:39.429 [     main] 현재 상태 출력, 큐 데이터: [data1, data2]
22:34:39.429 [     main] producer1: TERMINATED
22:34:39.429 [     main] producer2: TERMINATED
22:34:39.429 [     main] producer3: TIMED_WAITING
22:34:39.429 [     main] consumer1: BLOCKED
22:34:39.429 [     main] consumer2: BLOCKED
22:34:39.430 [     main] consumer3: BLOCKED
22:34:39.430 [     main] == [생산자 먼저 실행] 종료, BoundedQueueV2 ==
22:34:40.015 [producer3] [put] 큐가 가득 참, 생산자 대기
22:34:41.021 [producer3] [put] 큐가 가득 참, 생산자 대기
22:34:42.022 [producer3] [put] 큐가 가득 참, 생산자 대기
22:34:43.027 [producer3] [put] 큐가 가득 참, 생산자 대기
```
**문제 - 생산자 먼저 실행의 경우**

`producer3` 이 종료되지 않고 계속 수행되고, `consumer1` , `consumer2` , `consumer3` 은 `BLOCKED` 상태가 된다.

**참고**: 만약 실행 결과가 지금 내용과 다르고 특히 "현재 상태 출력"과 그 이후 부분이 나오지 않는다면 `toString()` 에 있는 `synchronized` 를 제거해야 한다. 

원칙적으로 `toString()` 에도 `synchronized` 를 적용해야 한다. 

그래야 `toString()` 을 통한 조회 시점에도 모니터 락이 걸리며 정확한 데이터를 조회할 수 있다. 

하지만 이 부분이 이번 설명의 핵심이 아니고, 또 예제 코드를 단순하게 유지하기 위해 여기서는 `toString()` 에 `synchronized` 를 사용하지 않겠다. 

왜 결과에 차이가 나는지는 이후에 설명하는 내용을 들어보면 자연스럽게 이해가 될 것이다.

```shell
22:39:11.126 [     main] == [소비자 먼저 실행] 시작, BoundedQueueV2 ==

22:39:11.129 [     main] 소비자 시작
22:39:11.133 [consumer1] [소비 시도]     ? <- []
22:39:11.134 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
22:39:11.239 [consumer2] [소비 시도]     ? <- []
22:39:11.345 [consumer3] [소비 시도]     ? <- []

22:39:11.450 [     main] 현재 상태 출력, 큐 데이터: []
22:39:11.450 [     main] consumer1: TIMED_WAITING
22:39:11.451 [     main] consumer2: BLOCKED
22:39:11.451 [     main] consumer3: BLOCKED

22:39:11.451 [     main] 생산자 시작
22:39:11.452 [producer1] [생산 시도] data1 -> []
22:39:11.557 [producer2] [생산 시도] data2 -> []
22:39:11.662 [producer3] [생산 시도] data3 -> []

22:39:11.767 [     main] 현재 상태 출력, 큐 데이터: []
22:39:11.767 [     main] consumer1: TIMED_WAITING
22:39:11.768 [     main] consumer2: BLOCKED
22:39:11.768 [     main] consumer3: BLOCKED
22:39:11.768 [     main] producer1: BLOCKED
22:39:11.768 [     main] producer2: BLOCKED
22:39:11.768 [     main] producer3: BLOCKED
22:39:11.769 [     main] == [소비자 먼저 실행] 종료, BoundedQueueV2 ==
22:39:12.139 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
22:39:13.145 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
22:39:14.150 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
22:39:15.155 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
```

**문제 - 소비자 먼저 실행의 경우**

소비자 먼저 실행의 경우 `consumer1` 이 종료되지 않고 계속 수행된다. 

그리고 나머지 모든 스레드가 `BLOCKED` 상태가 된다.

세상이 뭔가 멈춘 것 같다! 왜 이런 문제가 발생했을까? 먼저 잠깐의 시간동안 스스로 원인을 생각해보자.

## 생산자 소비자 문제 - 예제2 분석

### BoundedQueueV2 - 생산자 먼저 실행 분석

**실행 결과 - BoundedQueueV2, 생산자 먼저 실행**

<img width="465" alt="Screenshot 2024-11-06 at 22 58 31" src="https://github.com/user-attachments/assets/9126f1a4-15a7-4478-b6ae-cc5e60e5f9f7">

**생산자 스레드 실행 시작**

<img width="467" alt="Screenshot 2024-11-06 at 23 03 15" src="https://github.com/user-attachments/assets/56f2074c-789f-40fc-a733-893cf6331390">

```shell
22:34:38.795 [     main] == [생산자 먼저 실행] 시작, BoundedQueueV2 ==

22:34:38.796 [     main] 생산자 시작
22:34:38.803 [producer1] [생산 시도] data1 -> []
```
<img width="468" alt="Screenshot 2024-11-06 at 23 03 19" src="https://github.com/user-attachments/assets/19981801-36b2-4ca0-b495-86e6fc62c865">


```shell
22:34:38.803 [producer1] [생산 완료] data1 -> [data1]
```
<img width="465" alt="Screenshot 2024-11-06 at 23 03 24" src="https://github.com/user-attachments/assets/d1bc2900-ee1c-4c3a-a462-978cd58d933c">


```shell
22:34:38.904 [producer2] [생산 시도] data2 -> [data1]
```

<img width="466" alt="Screenshot 2024-11-06 at 23 03 27" src="https://github.com/user-attachments/assets/19dce6ac-a5df-45b6-8ec3-99b09595ab26">

```shell
22:34:38.904 [producer2] [생산 완료] data2 -> [data1, data2]
```
<img width="464" alt="Screenshot 2024-11-06 at 23 03 32" src="https://github.com/user-attachments/assets/b71614c7-0ea3-4109-bdc9-18cd68dd0e1e">

```shell
22:34:39.009 [producer3] [생산 시도] data3 -> [data1, data2]
22:34:39.010 [producer3] [put] 큐가 가득 참, 생산자 대기
```

생산자 스레드인 `p3` 는 임계 영역에 들어가기 위해 먼저 락을 획득한다. 

큐에 `data3` 을 저장하려고 시도한다.

그런데 큐가 가득 차있다.

`p3` 는 `sleep(1000)` 을사용해서잠시대기한다.이때 `RUNNABLE` `TIMED_WAITING` 상태가된다.

이때 반복문을 사용해서 1초마다 큐에 빈 자리가 있는지 반복해서 확인한다.

빈 자리가 있다면 큐에 데이터를 입력하고 완료된다.

빈 자리가 없다면 `sleep()` 으로 잠시 대기한 다음 반복문을 계속해서 수행한다.

1초마다 한 번씩 체크하 기 때문에 "큐가 가득 참, 생산자 대기"라는 메시지가 계속 출력될 것이다.

**여기서 핵심은 `p3` 스레드가 락을 가지고 있는 상태에서, 큐에 빈 자리가 나올 때 까지 대기한다는 점이다.**

```shell
22:34:39.114 [     main] 현재 상태 출력, 큐 데이터: [data1, data2]
22:34:39.115 [     main] producer1: TERMINATED
22:34:39.115 [     main] producer2: TERMINATED
22:34:39.115 [     main] producer3: TIMED_WAITING
```

**소비자 스레드 실행 시작**

<img width="467" alt="Screenshot 2024-11-06 at 23 03 37" src="https://github.com/user-attachments/assets/c856ba29-8d19-4008-a992-d845ef62da2a">

```shell
22:34:39.115 [     main] 소비자 시작
22:34:39.116 [consumer1] [소비 시도]     ? <- [data1, data2]
```

<img width="464" alt="Screenshot 2024-11-06 at 23 03 42" src="https://github.com/user-attachments/assets/b17aa19a-2789-4682-a385-612b4605dade">

**무한 대기 문제**
`c1` 이 임계 영역에 들어가기 위해 락을 획득하려 한다.

그런데 락이 없다! 왜냐하면 `p3` 가락을 가지고 임계영역에 이미 들어가 있기 때문이다. 

`p3` 가락을 반납하기전 까지는 `c1` 은 절대로 임계 영역(여기서는 `synchronized` )에 들어갈 수 없다!

여기서 심각한 무한 대기 문제가 발생한다.

`p3` 가 락을 반납하려면 소비자 스레드인 `c1` 이 먼저 작동해서 큐의 데이터를 가져가야 한다.

소비자 스레드인 `c1` 이 락을 획득하려면 생산자 스레드인 `p3` 가 먼저 락을 반납해야한다. 

`p3` 는 락을 반납하지 않고, `c1` 은 큐의 데이터를 가져갈 수 없다.

지금 상태면 `p3` 는 절대로 락을 반납할 수 없다.

왜냐하면 락을 반납하려면 `c1` 이 먼저 큐의 데이터를 소비해야 한다.

그래야 `p3` 가큐에 `data3` 을저장하고임계영역을빠져나가며락을반납할수있다.

그런데 `p3` 가락을 가지고임계영역안에있기때문에,임계영역밖의 `c1` 은 락을 획득할 수 없으므로, 큐에 접근하지 못하고 무한 대기한다.

결과적으로 소비자 스레드인 `c1` 은 `p3` 가 락을 반납할 때 까지 `BLOCKED` 상태로 대기한다.

```shell
22:34:39.218 [consumer2] [소비 시도]     ? <- [data1, data2]
```
<img width="469" alt="Screenshot 2024-11-06 at 23 03 46" src="https://github.com/user-attachments/assets/7164f761-a1f9-4d77-a480-51aea75263af">

`c2` 도 마찬가지로 락을 얻을 수 없으므로 `BLOCKED` 상태로 대기한다.

```shell
22:34:39.323 [consumer3] [소비 시도]     ? <- [data1, data2]
```
<img width="474" alt="Screenshot 2024-11-06 at 23 03 50" src="https://github.com/user-attachments/assets/969c1c22-79ca-4d73-9156-c1475751f24e">

`c3` 도 마찬가지로 락을 얻을 수 없으므로 `BLOCKED` 상태로 대기한다.

```shell

22:34:39.429 [     main] 현재 상태 출력, 큐 데이터: [data1, data2]
22:34:39.429 [     main] producer1: TERMINATED
22:34:39.429 [     main] producer2: TERMINATED
22:34:39.429 [     main] producer3: TIMED_WAITING
22:34:39.429 [     main] consumer1: BLOCKED
22:34:39.429 [     main] consumer2: BLOCKED
22:34:39.430 [     main] consumer3: BLOCKED
22:34:39.430 [     main] == [생산자 먼저 실행] 종료, BoundedQueueV2 ==
22:34:40.015 [producer3] [put] 큐가 가득 참, 생산자 대기
22:34:41.021 [producer3] [put] 큐가 가득 참, 생산자 대기
22:34:42.022 [producer3] [put] 큐가 가득 참, 생산자 대기
22:34:43.027 [producer3] [put] 큐가 가득 참, 생산자 대기
```

결과적으로 `c1` , `c2` , `c3` 는 모두 락을 획득하기 위해 `BLOCKED` 상태로 대기한다.

`p3` 는 1초마다 한 번씩 깨어나서 큐의 상태를 확인한다. 

그런데 본인이 락을 가지고 있기 때문에 다른 스레드가 임계 영역 안에 들어오는 것이 불가능하다. 

따라서 다른 스레드는 임계 영역 안에 있는 큐에 접근조차 할 수 없다.

결국 `p3` 는 절대로 비워지지 않는 큐를 계속 확인하게된다.

그리고 `[put] 큐가 가득 참, 생산자 대기` 를 1초마다 계속 출력한다.

결국 이런 상태가 무한하게 지속된다.

### BoundedQueueV2 - 소비자 먼저 실행 분석 

**실행 결과 - BoundedQueueV2, 소비자 먼저 실행**

<img width="477" alt="Screenshot 2024-11-06 at 23 24 01" src="https://github.com/user-attachments/assets/b56a79e9-8678-4922-91bc-68b9c1ef4336">

**소비자 스레드 실행 시작**

<img width="462" alt="Screenshot 2024-11-06 at 23 24 04" src="https://github.com/user-attachments/assets/c191c0bd-ec7f-43e3-a73b-cc564774f7a5">

```shell
22:39:11.126 [     main] == [소비자 먼저 실행] 시작, BoundedQueueV2 ==

22:39:11.129 [     main] 소비자 시작
22:39:11.133 [consumer1] [소비 시도]     ? <- []
22:39:11.134 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
```

소비자 스레드인 `c1` 은 임계영역에 들어가기 위해 락을 획득한다.

`c1` 은 큐의 데이터를 획득하려 하지만, 데이터가 없다.

`c1` 은 `sleep(1000)` 을사용해서잠시대기한다.

이때 `RUNNABLE` `TIMED_WAITING` 상태가된다.

이때 반복문을 사용해서 1초마다 큐에 데이터가 있는지 반복해서 확인한다.

데이터가 있다면 큐의 데이터를 가져오고 완료된다.

데이터가 없다면 반복문을 계속해서 수행한다. 

1초마다 한 번 "큐에 데이터가 없음, 소비자 대기"라는 메시 지가 출력될 것이다.

<img width="484" alt="Screenshot 2024-11-06 at 23 24 43" src="https://github.com/user-attachments/assets/58eb87ab-ba85-4d29-8c25-26762c6af058">

```shell
22:39:11.239 [consumer2] [소비 시도]     ? <- []
22:39:11.345 [consumer3] [소비 시도]     ? <- []

22:39:11.450 [     main] 현재 상태 출력, 큐 데이터: []
22:39:11.450 [     main] consumer1: TIMED_WAITING
22:39:11.451 [     main] consumer2: BLOCKED
22:39:11.451 [     main] consumer3: BLOCKED
```

**무한 대기 문제**

`c2` , `c3` 가 임계 영역에 들어가기 위해 락을 획득하려 한다.

그런데 락이 없다! 

왜냐하면 `c1` 이 락을 가지고 임계영역에 들어가있기 때문이다. 

`c1` 이락을 반납하기 전까지는 `c2` , `c3` 는 절대로 임계 영역(여기서는 `synchronized` )은 들어갈 수 없다!

여기서 심각한 무한 대기 문제가 발생한다.

`c1` 이 락을 반납하지 않기 때문에 `c2` , `c3` 는 `BLOCKED` 상태가 된다.

**생산자 스레드 실행 시작**

<img width="480" alt="Screenshot 2024-11-06 at 23 25 56" src="https://github.com/user-attachments/assets/a28ea39e-6fbc-4a37-9bd8-7d8789cfef00">

```shell
22:39:11.451 [     main] 생산자 시작
22:39:11.452 [producer1] [생산 시도] data1 -> []
22:39:11.557 [producer2] [생산 시도] data2 -> []
22:39:11.662 [producer3] [생산 시도] data3 -> []
```

**무한 대기 문제**
`p1` , `p2` , `p3` 가 임계영역에 들어가기위해 락을 획득하려한다.

그런데 락이없다!왜냐하면 `c1` 이락을가지고임계영역에들어가있기때문이다. 

`c1` 이락을반납하기전까지 는 `p1` , `p2` , `p3` 는 절대로 임계 영역(여기서는 `synchronized` )은 들어갈 수 없다!

여기서 심각한 무한 대기 문제가 발생한다.

`c1` 이 락을 반납하려면 생산자 스레드인 `p1` , `p2` , `p3` 가 먼저 작동해서 큐의 데이터를 추가해야 한다. 

생산자 스레드( `p1` , `p2` , `p3` )가 락을 획득하려면 소비자 스레드인 `c1` 이 먼저 락을 반납해야 한다. 

`c1` 은 락을 반납하지 않고, `p1` 은 큐에 데이터를 추가할 수 없다.(물론 `p2` , `p3` 도 포함이다.)

지금 상태면 `c1` 은 절대로 락을 반납할 수 없다.

왜냐하면 락을 반납하려면 `p1` 이 먼저 큐의 데이터를 추가해야 한다.

그래야 `c1` 이 큐에서 데이터를 획득하고 임계영역을 빠져나가며 락을 반납할 수 있다.

그런데 `c1` 이 락을 가지고 임계영역 안에 있기 때문에, 임계영역 밖의 `p1` 은 락을 획득할 수 없으므로, 큐에 접근하지 못하고 무한 대기한다.

결과적으로 생산자 스레드인 `p1` 은 `c1` 이 락을 반납할 때 까지 `BLOCKED` 상태로 대기한다.

```shell
22:39:11.767 [     main] 현재 상태 출력, 큐 데이터: []
22:39:11.767 [     main] consumer1: TIMED_WAITING
22:39:11.768 [     main] consumer2: BLOCKED
22:39:11.768 [     main] consumer3: BLOCKED
22:39:11.768 [     main] producer1: BLOCKED
22:39:11.768 [     main] producer2: BLOCKED
22:39:11.768 [     main] producer3: BLOCKED
22:39:11.769 [     main] == [소비자 먼저 실행] 종료, BoundedQueueV2 ==
22:39:12.139 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
22:39:13.145 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
22:39:14.150 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
22:39:15.155 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
```

결과적으로 `c1` 을 제외한 모든 스레드가 락을 획득하기 위해 `BLOCKED` 상태로 대기한다.

`c1` 은 1초마다 한 번씩 깨어나서 큐의 상태를 확인한다. 

그런데 본인이 락을 가지고 있기 때문에 다른 스레드는 임계 영 역에 들어오는 것이 불가능하고, 큐에 접근조차 할 수 없다. 

따라서 `[take] 큐에 데이터가 없음, 소비자 대기` 를 1초마다 계속 출력한다.

결국 이런 상태가 무한하게 지속된다.


### **정리**

버퍼가 비었을 때 소비하거나, 버퍼가 가득 찾을 때 생산하는 문제를 해결하기 위해, 단순히 스레드가 잠깐 기다리면 될 것이라 생각했는데, 문제가 더 심각해졌다. 

생각해보면 결국 임계 영역 안에서 락을 가지고 대기하는 것이 문제이다. 

이것은 마치 열쇠를 가진 사람이 안에서 문을 잠궈버린 것과 같다. 

그래서 다른 스레드가 임계 영역안에 접근조차 할 수 없 는 것이다.

여기서 잘 생각해보면, 락을 가지고 임계 영역안에 있는 스레드가 `sleep()` 을 호출해서 잠시 대기할 때는 아무일도 하지 않는다. 

그렇다면 이렇게 아무일도 하지 않고 대기하는 동안 잠시 다른 스레드에게 락을 양보하면 어떨까? 

그러면 다른 스레드가 버퍼에 값을 채우거나 버퍼의 값을 가져갈 수 있을 것이다. 

그러면 락을 가진 스레드도 버퍼에서 값을 획득 하거나 값을 채우고 락을 반납할 수 있을 것이다.

예를 들어 락을 가진 소비자 스레드가 임계 영역 안에서 버퍼의 값을 획득하기를 기다린다고 가정하자. 

버퍼에 값이 없 으면 값이 채워질 때 까지 소비자 스레드는 아무일도 하지 않고 대기해야 한다. 

어차피 아무일도 하지 않으므로, 이때 잠 시 락을 다른 스레드에게 빌려주는 것이다. 

락을 획득한 생산자 스레드는 이때 버퍼에 값을 채우고 락을 반납한다. 

버퍼 에 값이 차면 대기하던 소비자 스레드가 다시 락을 획득한 다음에 버퍼의 값을 가져가고 락을 반납하는 것이다.

이 설명이 잘 이해가 되지 않아도 괜찮다. 

바로 다음에 예제를 통해서 천천히 알아볼 것이다. 

여기서는 다음 딱 한가지만 생각하면 된다.

"락을 가지고 대기하는 스레드가 대기하는 동안 다른 스레드에게 락을 양보할 수 있다면, 이 문제를 쉽게 풀 수 있다."

자바의 `Object.wait()` , `Object.noitfy()` 를 사용하면 락을 가지고 대기하는 스레드가 대기하는 동안 다른 스레드에게 락을 양보할 수 있다.





















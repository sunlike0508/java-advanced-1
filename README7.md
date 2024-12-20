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

## Object - wait, notify - 예제3 코드

바는 처음부터 멀티스레드를 고려하며 탄생한 언어다.

앞서 설명한 `synchronized` 를 사용한 임계 영역 안에서 락을 가지고 무한 대기하는 문제는 흥미롭게도 `Object` 클래스에 해결 방안이 있다. 

`Object` 클래스는 이런 문제를 해결할 수 있는 `wait()` , `notify()` 라는 메서드를 제공한다. 

`Object` 는 모든 자바 객체의 부모이기 때문에, 여기 있는 기능들은 모두 자바 언어의 기본 기능이라 생각하면 된다.

**wait(), notify() 설명** 

* `Object.wait()`
  * 현재 스레드가 가진 락을 반납하고 대기( `WAITING` )한다. 
  * 현재 스레드를 대기( `WAITING` ) 상태로 전환한다. 
  * 이 메서드는 현재 스레드가 `synchronized` 블록이나 메서드에서 락을 소유하고 있을 때만 호출할 수 있다. 
  * 호출한 스레드는 락을 반납하고, 다른 스레드가 해당 락을 획득할 수 있도록 한다. 
  * 이렇게 대기 상태로 전환된 스레드는 다른 스레드가 `notify()` 또는 `notifyAll()` 을 호출할 때까지 대기 상태를 유지한다. 

* `Object.notify()`
  * 대기 중인 스레드 중 하나를 깨운다. 
  * 이 메서드는 `synchronized` 블록이나 메서드에서 호출되어야 한다. 
  * 깨운 스레드는 락을 다시 획득할 기회를 얻게 된다. 
  * 만약 대기 중인 스레드가 여러 개라면, 그 중 하나만이 깨워지게 된다. 
   
* `Object.notifyAll()`
  * 대기 중인 모든 스레드를 깨운다.
  * 이 메서드 역시 `synchronized` 블록이나 메서드에서 호출되어야 하며, 모든 대기 중인 스레드가 락을 획 득할 수 있는 기회를 얻게 된다. 
  * 이 방법은 모든 스레드를 깨워야 할 필요가 있는 경우에 유용하다.

`wait()` , `notify()` 메서드를 적절히 사용하면, 멀티스레드 환경에서 발생할 수 있는 문제를 효율적으로 해결할 수 있다. 

이 기능을 활용해서 스레드가 락을 가지고 임계 영역안에서 무한 대기하는 문제를 해결해보자.

```java
public class BoundedQueueV3 implements BoundedQueue {

    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;


    public BoundedQueueV3(int max) {this.max = max;}


    @Override
    public synchronized void put(String data) {
        while(queue.size() == max) {
            log("[put] 큐가 가득 참, 생산자 대기");
            try {
                wait();
                log("[put] 생산자 깨어남");
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        queue.offer(data);
        log("[put] 생산자 데이터 저장, notify() 호출");
        notify();
    }


    @Override
    public synchronized String take() {
        while(queue.isEmpty()) {
            log("[take] 큐에 데이터가 없음, 소비자 대기");
            try {
                wait();
                log("[take] 소비자 깨어남");
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        String data = queue.poll();
        log("[take] 소비자 데이터 획득, notify() 호출");
        notify();
        return data;
    }


    @Override
    public String toString() {
        return queue.toString();
    }
}
```

**put(data) - wait(), notify()**

* `synchronized` 를 통해 임계 영역을 설정한다. 생산자 스레드는 락 획득을 시도한다.
* 락을 획득한 생산자 스레드는 반복문을 사용해서 큐에 빈 공간이 생기는지 주기적으로 체크한다. 
* 만약 빈 공간이 없다면 `Object.wait()` 을 사용해서 대기한다. 참고로 **대기할 때 락을 반납하고 대기**한다. 
* 그리고 대기 상태에 서 깨어나면, 다시 반복문에서 큐의 빈 공간을 체크한다.
* `wait()` 를호출해서대기하는경우 `RUNNABLE` `WAITING` 상태가된다.
* 생산자가 데이터를 큐에 저장하고 나면 `notify()` 를 통해 저장된 데이터가 있다고 대기하는 스레드에 알려주어야 한다. 
* 예를 들어서 큐에 데이터가 없어서 대기하는 소비자 스레드가 있다고 가정하자. 
* 이때 `notify()` 를 호 출하면 소비자 스레드는 깨어나서 저장된 데이터를 획득할 수 있다.

**take() - wait(), notify()**

* `synchronized` 를 통해 임계 영역을 설정한 다. 소비자 스레드는 락 획득을 시도한다. 
* 락을 획득한 소비자 스레드는 반복문을 사용해서 큐에 데이터가 있는지 주기적으로 체크한다. 
* 만약 데이터가 없다 면 `Object.wait()` 을 사용해서 대기한다. 참고로 **대기할 때 락을 반납하고 대기**한다. 
* 그리고 대기 상태에서 깨 어나면, 다시 반복문에서 큐에 데이터가 있는지 체크한다.
* 대기하는 경우 `RUNNABLE` `WAITING` 상태가 된다.
* 소비자가 데이터를 획득하고 나면 `notify()` 를 통해 큐에 저장할 여유 공간이 생겼다고, 대기하는 스레드에게 알려주어야 한다.
* 예를 들어서 큐에 데이터가 가득 차서 대기하는 생산자 스레드가 있다고 가정하자.
* 이때 `notify()` 를 호출하면 생산자 스레드는 깨어나서 데이터를 큐에 저장할 수 있다.
* `wait()` 로 대기 상태에 빠진 스레드는 `notify()` 를 사용해야 깨울 수 있다.
* 생산자는 생산을 완료하면 `notify()` 로 대기하는 스레드를 깨워서 생산된 데이터를 가져가게 하고, 소비자는 소비를 완료하면 `notify()` 로 대기하는 스레드를 깨워서 데이터를 생산하라고 하면 된다. 
* 여기서 중요한 핵심은 `wait()` 를 호출해서 대기 상태에 빠질 때 락을 반납하고 대기 상태에 빠진다는 것이다.
* 대기 상태에 빠지면 어차피 아무일도 하지 않으므로 락도 필요하지 않다.

```shell
20:53:16.747 [     main] == [생산자 먼저 실행] 시작, BoundedQueueV3 ==

20:53:16.750 [     main] 생산자 시작
20:53:16.755 [producer1] [생산 시도] data1 -> []
20:53:16.755 [producer1] [put] 생산자 데이터 저장, notify() 호출
20:53:16.755 [producer1] [생산 완료] data1 -> [data1]
20:53:16.856 [producer2] [생산 시도] data2 -> [data1]
20:53:16.856 [producer2] [put] 생산자 데이터 저장, notify() 호출
20:53:16.856 [producer2] [생산 완료] data2 -> [data1, data2]
20:53:16.960 [producer3] [생산 시도] data3 -> [data1, data2]
20:53:16.960 [producer3] [put] 큐가 가득 참, 생산자 대기

20:53:17.065 [     main] 현재 상태 출력, 큐 데이터: [data1, data2]
20:53:17.065 [     main] producer1: TERMINATED
20:53:17.065 [     main] producer2: TERMINATED
20:53:17.066 [     main] producer3: WAITING

20:53:17.066 [     main] 소비자 시작
20:53:17.066 [consumer1] [소비 시도]     ? <- [data1, data2]
20:53:17.066 [consumer1] [take] 소비자 데이터 획득, notify() 호출
20:53:17.066 [consumer1] [소비 완료] data1 <- [data2]
20:53:17.066 [producer3] [put] 생산자 깨어남
20:53:17.067 [producer3] [put] 생산자 데이터 저장, notify() 호출
20:53:17.067 [producer3] [생산 완료] data3 -> [data2, data3]
20:53:17.168 [consumer2] [소비 시도]     ? <- [data2, data3]
20:53:17.168 [consumer2] [take] 소비자 데이터 획득, notify() 호출
20:53:17.169 [consumer2] [소비 완료] data2 <- [data3]
20:53:17.271 [consumer3] [소비 시도]     ? <- [data3]
20:53:17.272 [consumer3] [take] 소비자 데이터 획득, notify() 호출
20:53:17.272 [consumer3] [소비 완료] data3 <- []

20:53:17.373 [     main] 현재 상태 출력, 큐 데이터: []
20:53:17.373 [     main] producer1: TERMINATED
20:53:17.373 [     main] producer2: TERMINATED
20:53:17.373 [     main] producer3: TERMINATED
20:53:17.373 [     main] consumer1: TERMINATED
20:53:17.374 [     main] consumer2: TERMINATED
20:53:17.374 [     main] consumer3: TERMINATED
20:53:17.374 [     main] == [생산자 먼저 실행] 종료, BoundedQueueV3 ==
```

```shell
20:55:19.903 [     main] == [소비자 먼저 실행] 시작, BoundedQueueV3 ==

20:55:19.905 [     main] 소비자 시작
20:55:19.908 [consumer1] [소비 시도]     ? <- []
20:55:19.908 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
20:55:20.013 [consumer2] [소비 시도]     ? <- []
20:55:20.013 [consumer2] [take] 큐에 데이터가 없음, 소비자 대기
20:55:20.117 [consumer3] [소비 시도]     ? <- []
20:55:20.117 [consumer3] [take] 큐에 데이터가 없음, 소비자 대기

20:55:20.222 [     main] 현재 상태 출력, 큐 데이터: []
20:55:20.223 [     main] consumer1: WAITING
20:55:20.223 [     main] consumer2: WAITING
20:55:20.223 [     main] consumer3: WAITING

20:55:20.223 [     main] 생산자 시작
20:55:20.224 [producer1] [생산 시도] data1 -> []
20:55:20.224 [producer1] [put] 생산자 데이터 저장, notify() 호출
20:55:20.224 [producer1] [생산 완료] data1 -> [data1]
20:55:20.224 [consumer1] [take] 소비자 깨어남
20:55:20.224 [consumer1] [take] 소비자 데이터 획득, notify() 호출
20:55:20.224 [consumer1] [소비 완료] data1 <- []
20:55:20.224 [consumer2] [take] 소비자 깨어남
20:55:20.224 [consumer2] [take] 큐에 데이터가 없음, 소비자 대기
20:55:20.325 [producer2] [생산 시도] data2 -> []
20:55:20.325 [producer2] [put] 생산자 데이터 저장, notify() 호출
20:55:20.325 [producer2] [생산 완료] data2 -> [data2]
20:55:20.325 [consumer3] [take] 소비자 깨어남
20:55:20.325 [consumer3] [take] 소비자 데이터 획득, notify() 호출
20:55:20.325 [consumer3] [소비 완료] data2 <- []
20:55:20.325 [consumer2] [take] 소비자 깨어남
20:55:20.325 [consumer2] [take] 큐에 데이터가 없음, 소비자 대기
20:55:20.428 [producer3] [생산 시도] data3 -> []
20:55:20.428 [producer3] [put] 생산자 데이터 저장, notify() 호출
20:55:20.428 [producer3] [생산 완료] data3 -> [data3]
20:55:20.428 [consumer2] [take] 소비자 깨어남
20:55:20.429 [consumer2] [take] 소비자 데이터 획득, notify() 호출
20:55:20.429 [consumer2] [소비 완료] data3 <- []

20:55:20.533 [     main] 현재 상태 출력, 큐 데이터: []
20:55:20.533 [     main] consumer1: TERMINATED
20:55:20.534 [     main] consumer2: TERMINATED
20:55:20.534 [     main] consumer3: TERMINATED
20:55:20.534 [     main] producer1: TERMINATED
20:55:20.534 [     main] producer2: TERMINATED
20:55:20.535 [     main] producer3: TERMINATED
20:55:20.535 [     main] == [소비자 먼저 실행] 종료, BoundedQueueV3 ==
```

## Object - wait, notify - 예제3 분석 - 생산자 우선

**생산자 스레드 실행 시작**

<img width="470" alt="Screenshot 2024-11-10 at 21 04 28" src="https://github.com/user-attachments/assets/e1b0b4d6-3df0-4477-9113-288ac281bd65">

```shell
20:53:16.747 [     main] == [생산자 먼저 실행] 시작, BoundedQueueV3 ==

20:53:16.750 [     main] 생산자 시작
```

**스레드 대기 집합(wait set)**

* `synchronized` 임계 영역 안에서 `Object.wait()` 를 호출하면 스레드는 대기( `WAITING` ) 상태에 들어간다. 
* 이렇게 대기 상태에 들어간 스레드를 관리하는 것을 대기 집합(wait set)이라 한다. 
* 참고로 모든 객체는 각자 의 대기 집합을 가지고 있다.
* 모든 객체는 락(모니터 락)과 대기 집합을 가지고 있다. 
* 둘은 한 쌍으로 사용된다. 
* 따라서 락을 획득한 객체의 대기 집합을 사용해야 한다. 
* 여기서는 `BoundedQueue(x001)` 구현 인스턴스의 락과 대기 집합을 사용한다.
  * `synchronized` 를 메서드에 적용하면 해당 인스턴스의 락을 사용한다. 
  * 여기서는 `BoundedQueue(x001)` 의 구현체이다.
  * `wait()` 호출은 앞에 `this` 를 생략할 수 있다. 
  * `this` 는 해당 인스턴스를 뜻한다. 여기서는 
  * `BoundedQueue(x001)` 의 구현체이다.

<img width="473" alt="Screenshot 2024-11-10 at 21 04 33" src="https://github.com/user-attachments/assets/015c52b4-7c12-4fb6-a4f6-eec86620eafd">

```shell
20:53:16.755 [producer1] [생산 시도] data1 -> []
20:53:16.755 [producer1] [put] 생산자 데이터 저장, notify() 호출
```
`p1` 이 락을 획득하고 큐에 데이터를 저장한다.

큐에 데이터가 추가 되었기 때문에 스레드 대기 집합에 이 사실을 알려야 한다.

`notify()` 를 호출하면 스레드 대기 집합에서 대기하는 스레드 중 하나를 깨운다.

현재 대기 집합에 스레드가 없으므로 아무일도 발생하지 않는다. 

만약 소비자 스레드가 대기 집합에 있었다면 깨어나서 큐에 들어있는 데이터를 소비했을 것이다.

<img width="469" alt="Screenshot 2024-11-10 at 21 04 42" src="https://github.com/user-attachments/assets/0a88322e-8491-41e3-b703-30cad3ddb1d0">


```shell
20:53:16.755 [producer1] [생산 완료] data1 -> [data1]
```

<img width="462" alt="Screenshot 2024-11-10 at 21 04 45" src="https://github.com/user-attachments/assets/6449f5b5-5b77-442f-af30-9297d6af73ac">


```shell
20:53:16.856 [producer2] [생산 시도] data2 -> [data1]
20:53:16.856 [producer2] [put] 생산자 데이터 저장, notify() 호출
20:53:16.856 [producer2] [생산 완료] data2 -> [data1, data2]
```

`p2` 도 큐에 데이터를 저장하고 생산을 완료한다.

<img width="464" alt="Screenshot 2024-11-10 at 21 04 49" src="https://github.com/user-attachments/assets/212e71db-253a-4f78-a855-0513e98bd382">

```shell
20:53:16.960 [producer3] [생산 시도] data3 -> [data1, data2]
20:53:16.960 [producer3] [put] 큐가 가득 참, 생산자 대기
```

`p3` 가 데이터를 생산하려고 하는데, 큐가 가득 찼다. `wait()` 를 호출한다.

**생산자 스레드 실행 완료**

<img width="483" alt="Screenshot 2024-11-10 at 21 04 55" src="https://github.com/user-attachments/assets/4d2a42e9-e18e-422d-867f-de1bf14759bd">

```shell
20:53:17.065 [     main] 현재 상태 출력, 큐 데이터: [data1, data2]
20:53:17.065 [     main] producer1: TERMINATED
20:53:17.065 [     main] producer2: TERMINATED
20:53:17.066 [     main] producer3: WAITING
```
* `wait()` 를 호출하면 
  * 락을 반납한다.
  * 스레드의 상태가 `RUNNABLE` `WAITING` 로 변경된다. 
  * 스레드 대기 집합에서 관리된다.

* 스레드 대기 집합에서 관리되는 스레드는 이후에 다른 스레드가 `notify()` 를 통해 스레드 대기 집합에 신호를 주면 깨어날 수 있다.

**소비자 스레드 실행 시작**

<img width="470" alt="Screenshot 2024-11-10 at 21 04 59" src="https://github.com/user-attachments/assets/b888645c-e60e-4e1a-9b8b-c287692eca01">

```shell
20:53:17.066 [     main] 소비자 시작
```

<img width="471" alt="Screenshot 2024-11-10 at 21 05 04" src="https://github.com/user-attachments/assets/b4da7fa0-bc36-4fd7-ac81-15e360ad6551">

```shell
20:53:17.066 [consumer1] [소비 시도]     ? <- [data1, data2]
20:53:17.066 [consumer1] [take] 소비자 데이터 획득, notify() 호출
```
소비자 스레드가 데이터를 획득했기 때문에 큐에 데이터를 보관할 빈자리가 생겼다. 

소비자 스레드는 `notify()` 를 호출해서 스레드 대기 집합에 이 사실을 알려준다.

<img width="476" alt="Screenshot 2024-11-10 at 21 05 07" src="https://github.com/user-attachments/assets/edc827bd-def1-46fd-82f7-107e596055e0">

스레드 대기 집합은 `notify()` 신호를 받으면 대기 집합에 있는 스레드 중 하나를 깨운다.

그런데 대기 집합에 있는 스레드가 깨어난다고 바로 작동하는 것은 아니다. 

깨어난 스레드는 여전히 임계 영역 안 에 있다.

임계 영역에 있는 코드를 실행하려면 먼저 락이 필요하다. 

`p3` 는 대기 집합에서는 나가지만 여전히 임계 영역에 있으므로 락을 획득하기 위해 `BLOCKED` 상태로 대기한다. 

당연한 이야기지만 임계 영역 안에서 2개의 스레드가 실행되면 큰 문제가 발생한다! 

임계 영역 안에서는 락을 가지고 있는 하나의 스레드만 실행 되어야 한다.
  * `p3` : `WAITING` `BLOCKED`

참고로 이때 임계 영역의 코드를 처음으로 돌아가서 실행하는 것은 아니다. 

대기 집합에 들어오게 된 `wait()` 를 호출한 부분 부터 실행된다. 

락을 획득하면 `wait()` 이후의 코드를 실행한다.

<img width="459" alt="Screenshot 2024-11-10 at 21 05 12" src="https://github.com/user-attachments/assets/4a31327f-694e-41b8-a017-abd448e67b30">

```shell
20:53:17.066 [consumer1] [소비 완료] data1 <- [data2]
```
`c1` 은 데이터 소비를 완료하고 락을 반납하고 임계 영역을 빠져나간다.

<img width="477" alt="Screenshot 2024-11-10 at 21 05 16" src="https://github.com/user-attachments/assets/b6e87bf2-02b8-43ed-95da-a027da94292e">

```shell
20:53:17.066 [producer3] [put] 생산자 깨어남
20:53:17.067 [producer3] [put] 생산자 데이터 저장, notify() 호출
```
* `p3` 가 락을 획득한다.
  * `BLOCKED` `RUNNABLE`
  * `wait()` 코드에서 대기했기 때문에 이후의 코드를 실행한다.
  * `data3` 을 큐에 저장한다.
  * `notify()` 를 호출한다. 데이터를 저장했기 때문에 혹시 스레드 대기 집합에 소비자가 대기하고 있다면 소비자를 하나 깨워야 한다.
  * 물론 지금은 대기 집합에 스레드가 없기 때문에 아무 일도 일어나지 않는다.

<img width="459" alt="Screenshot 2024-11-10 at 21 05 22" src="https://github.com/user-attachments/assets/0092a671-edda-4e71-b4b5-df151189d584">

```shell
20:53:17.067 [producer3] [생산 완료] data3 -> [data2, data3]
```

<img width="473" alt="Screenshot 2024-11-10 at 21 05 27" src="https://github.com/user-attachments/assets/341ffe86-8ad6-4ca5-87f7-2dc71cc51b56">

```shell
20:53:17.168 [consumer2] [소비 시도]     ? <- [data2, data3]
20:53:17.168 [consumer2] [take] 소비자 데이터 획득, notify() 호출
20:53:17.169 [consumer2] [소비 완료] data2 <- [data3]
20:53:17.271 [consumer3] [소비 시도]     ? <- [data3]
20:53:17.272 [consumer3] [take] 소비자 데이터 획득, notify() 호출
20:53:17.272 [consumer3] [소비 완료] data3 <- []
```

`c2` , `c3` 를 실행한다. 데이터가 있으므로 둘다 데이터를 소비하고 완료한다.

둘다 `notify()` 를 호출하지만 대기 집합에 스레드가 없으므로 아무일도 발생하지 않는다.

<img width="464" alt="Screenshot 2024-11-10 at 21 05 39" src="https://github.com/user-attachments/assets/2262d2cc-bf24-4ddd-a9fb-610c0e9ce474">

```shell
20:53:17.373 [     main] 현재 상태 출력, 큐 데이터: []
20:53:17.373 [     main] producer1: TERMINATED
20:53:17.373 [     main] producer2: TERMINATED
20:53:17.373 [     main] producer3: TERMINATED
20:53:17.373 [     main] consumer1: TERMINATED
20:53:17.374 [     main] consumer2: TERMINATED
20:53:17.374 [     main] consumer3: TERMINATED
20:53:17.374 [     main] == [생산자 먼저 실행] 종료, BoundedQueueV3 ==
```

**정리**

`wait()` , `notify()` 덕분에 스레드가 락을 놓고 대기하고, 또 대기하는 스레드를 필요한 시점에 깨울 수 있었다.

생산자 스레드가 큐가 가득차서 대기해도, 소비자 스레드가 큐의 데이터를 소비하고 나면 알려주기 때문에, 최적의 타이밍에 깨어나서 데이터를 생산할 수 있었다.

덕분에 최종 결과를 보면 `p1` , `p2` , `p3` 는 모두 데이터를 정상 생산하고, `c1` , `c2` , `c3` 는 모두 데이터를 정상 소비할 수 있었다.

다음에는 반대로 소비자를 먼저 실행해보자.

## Object - wait, notify - 예제3 분석 - 소비자 우선

**소비자 스레드 실행 시작**

<img width="466" alt="Screenshot 2024-11-10 at 21 43 46" src="https://github.com/user-attachments/assets/1379e207-9c73-47c2-b85c-c81e3f74a4aa">

```shell
20:55:19.903 [     main] == [소비자 먼저 실행] 시작, BoundedQueueV3 ==

20:55:19.905 [     main] 소비자 시작
```

<img width="473" alt="Screenshot 2024-11-10 at 21 43 50" src="https://github.com/user-attachments/assets/ec2f947e-e690-44b5-a9ad-93c163be4da5">

```shell
20:55:19.908 [consumer1] [소비 시도]     ? <- []
20:55:19.908 [consumer1] [take] 큐에 데이터가 없음, 소비자 대기
```

<img width="462" alt="Screenshot 2024-11-10 at 21 43 55" src="https://github.com/user-attachments/assets/7fdcbcac-3e56-4861-9a9f-4b9210199cea">
<img width="471" alt="Screenshot 2024-11-10 at 21 43 58" src="https://github.com/user-attachments/assets/25a7cc6a-e432-4f58-b220-b98685bc28a8">

**소비자 스레드 실행 완료**

<img width="468" alt="Screenshot 2024-11-10 at 21 44 03" src="https://github.com/user-attachments/assets/4a49daae-9994-46a0-8d54-efecf4ff1eb7">

```shell
20:55:20.013 [consumer2] [소비 시도]     ? <- []
20:55:20.013 [consumer2] [take] 큐에 데이터가 없음, 소비자 대기
20:55:20.117 [consumer3] [소비 시도]     ? <- []
20:55:20.117 [consumer3] [take] 큐에 데이터가 없음, 소비자 대기
```

큐에 데이터가 없기 때문에 `c1` , `c2` , `c3` 모두 스레드 대기 집합에서 대기한다.

이후에 생산자가 큐에 데이터를 생산하면 `notify()` 를 통해 이 스레드들을 하나씩 깨워서 데이터를 소비할 수 있을 것이다.

```shell
20:55:20.222 [     main] 현재 상태 출력, 큐 데이터: []
20:55:20.223 [     main] consumer1: WAITING
20:55:20.223 [     main] consumer2: WAITING
20:55:20.223 [     main] consumer3: WAITING
```

**생산자 스레드 실행 시작**

<img width="461" alt="Screenshot 2024-11-10 at 21 46 52" src="https://github.com/user-attachments/assets/dd19dd69-4b5c-46d4-87f5-cb755ecb8bab">

```shell
20:55:20.223 [     main] 생산자 시작
20:55:20.224 [producer1] [생산 시도] data1 -> []
20:55:20.224 [producer1] [put] 생산자 데이터 저장, notify() 호출
```

`p1` 은 락을 획득하고, 큐에 데이터를 생산한다. 

큐에 데이터가 있기 때문에 소비자를 하나 깨울 수 있다. 

`notify()` 를 통해 스레드 대기 집합에 이 사실을 알려준다.

<img width="466" alt="Screenshot 2024-11-10 at 21 47 01" src="https://github.com/user-attachments/assets/c4e2e905-94a0-48f8-8c4e-51199cdbed39">

`notify()` 를 받은 스레드 대기 집합은 스레드 중에 하나를 깨운다.

여기서 `c1` , `c2` , `c3` 중에 어떤 스레드가 깨어날까? 정답은 "예측할 수 없다"이다.

어떤 스레드가 깨워질지는 JVM 스펙에 명시되어 있지 않다. 

따라서 JVM 버전 환경등에 따라서 달라진다. 

그런데 대기 집합에 있는 스레드가 깨어난다고 바로 작동하는 것은 아니다. 

깨어난 스레드는 여전히 임계 영역 안에 있다.

임계 영역에 있는 코드를 실행하려면 먼저 락이 필요하다. 

대기 집합에서는 나가지만 여전히 임계 영역에 있으므로 락을 획득하기 위해 `BLOCKED` 상태로 대기한다.
* `c1` : `WAITING` `BLOCKED`

<img width="471" alt="Screenshot 2024-11-10 at 21 48 09" src="https://github.com/user-attachments/assets/2c24f5a0-f99b-4617-ba61-94f419a3996d">

```shell
20:55:20.224 [producer1] [생산 완료] data1 -> [data1]
```

<img width="473" alt="Screenshot 2024-11-10 at 21 48 56" src="https://github.com/user-attachments/assets/53d6da15-bec8-4a76-ba02-5b05ea1502db">

```shell
20:55:20.224 [consumer1] [take] 소비자 깨어남
20:55:20.224 [consumer1] [take] 소비자 데이터 획득, notify() 호출
```
`c1` 은 락을 획득하고, 임계 영역 안에서 실행되며 데이터를 획득한다.

`c1` 이 데이터를 획득했으므로 큐에 데이터를 넣을 공간이 있다는 것을 대기 집합에 알려준다. 

만약 대기 집합에 생산자 스레드가 대기하고 있다면 큐에 데이터를 넣을 수 있을 것이다.

<img width="468" alt="Screenshot 2024-11-10 at 21 49 04" src="https://github.com/user-attachments/assets/35176c2f-afc8-4053-a0da-6f6d24cd4089">

`c1` 이 `notify()` 로 스레드 대기 집합에 알렸지만, **생산자 스레드가 아니라 소비자 스레드만 있다.** 

따라서 의도 와는 다르게 소비자 스레드인 `c2` 가 대기 상태에서 깨어난다. 

(물론 대기 집합에 있는 어떤 스레드가 깨어날지는 알 수 없다. 여기서는 `c2` 가 깨어난다고 가정한다. 심지어 생산자와 소비자 스레드가 함께 대기 집합에 있어도 어떤 스레드가 깨어날지는 알 수 없다.)

<img width="475" alt="Screenshot 2024-11-10 at 21 49 08" src="https://github.com/user-attachments/assets/8f77342c-8c40-4aca-9ee8-402ab34aeadb">

```shell
20:55:20.224 [consumer1] [소비 완료] data1 <- []
```

`c1` 은 작업을 완료한다.

`c1` 이 `c2` 를 깨웠지만, 문제가 하나 있다. 바로 큐에 데이터가 없다는 점이다.

<img width="465" alt="Screenshot 2024-11-10 at 21 49 12" src="https://github.com/user-attachments/assets/eac98680-b8fb-44ef-9065-ec7510bcf0e3">

```shell
20:55:20.224 [consumer2] [take] 소비자 깨어남
20:55:20.224 [consumer2] [take] 큐에 데이터가 없음, 소비자 대기
```
`c2` 는 락을 획득하고, 큐에 데이터를 소비하려고 시도 한다. 

그런데 큐에는 데이터가 없다.

큐에 데이터가 없기 때문에, `c2` 는 결국 `wait()` 를 호출해서 대기 상태로 변하며 다시 대기 집합에 들어간다.

<img width="465" alt="Screenshot 2024-11-10 at 21 49 18" src="https://github.com/user-attachments/assets/5edd9c5e-a4c0-449f-8f16-0950d671a2c3">

이처럼 소비자인 `c1` 이 같은 소비자인 `c2` 를 깨우는 것은 상당히 비효율적이다.

`c1` 입장에서 `c2` 를 깨우게 되면 아무일도 하지 않고 그냥 다시 스레드 대기 집합에 들어갈 수 있다.

결과적으로 CPU만 사용하고, 아무 일도 하지 않은 상태로 다시 대기 상태가 되어버린다.

그렇다고 `c1` 이 스레드 대기 집합에 있는 어떤 스레드를 깨울지 선택할 수는 없다. 

`notify()` 는 스레드 대기 집 합에 있는 스레드 중 임의의 하나를 깨울 뿐이다.

물론 이것이 비효율적이라는 것이지 문제가 되는 것은 아니다. 결과에는 문제가 없다. 

가끔씩 약간 돌아서 갈 뿐이다.

<img width="459" alt="Screenshot 2024-11-10 at 21 49 22" src="https://github.com/user-attachments/assets/caba7749-98ff-4168-8807-a74878605887">

<img width="465" alt="Screenshot 2024-11-10 at 21 49 25" src="https://github.com/user-attachments/assets/671790e8-2b74-4016-b109-17a316adee2d">

```shell
20:55:20.325 [producer2] [생산 시도] data2 -> []
20:55:20.325 [producer2] [put] 생산자 데이터 저장, notify() 호출
```

`p2` 가 락을 획득하고 데이터를 저장한 다음에 `notify()` 를 호출한다. 

데이터가 있으므로 소비자 스레드가 깨어난다면 데이터를 소비할 수 있다.

<img width="467" alt="Screenshot 2024-11-10 at 21 49 30" src="https://github.com/user-attachments/assets/d0747f0b-9fdd-4fca-adec-382860b6437b">

스레드 대기 집합에 있는 `c3` 가 깨어난다. 참고로 어떤 스레드가 깨어날지는 알 수 없다. 

`c3` 는 임계 영역 안에 있으므로 락을 획득하기 위해 대기( `BLOCKED` )한다.

<img width="467" alt="Screenshot 2024-11-10 at 21 49 35" src="https://github.com/user-attachments/assets/4c8bf8b4-ea2b-4335-b107-b097005250cf">

```shell
20:55:20.325 [producer2] [생산 완료] data2 -> [data2]
```

<img width="471" alt="Screenshot 2024-11-10 at 21 49 39" src="https://github.com/user-attachments/assets/ae046afc-f2f8-40ae-9b34-0327f57dd0bc">

```shell
20:55:20.325 [consumer3] [take] 소비자 깨어남
20:55:20.325 [consumer3] [take] 소비자 데이터 획득, notify() 호출
```

`c3` 는 락을 획득하고 `BLOCKED` `RUNNABLE` 상태가 된다.

`c3` 는 데이터를 획득한 다음에 `notify()` 를 통해 스레드 대기 집합에 알린다. 

큐에 여유 공간이 생겼기 때문에 생산자 스레드가 대기 중이라면 데이터를 생산할 수 있다.

<img width="462" alt="Screenshot 2024-11-10 at 21 49 43" src="https://github.com/user-attachments/assets/b646ea9a-cbff-4f03-84e7-e8c23d24f228">

생산자 스레드를 깨울 것으로 기대하고, `notify()` 를 호출했지만 스레드 대기 집합에는 소비자인 `c2` 만 존재한다.

`c2` 가 깨어나지만 임계 영역 안에 있으므로 락을 기다리는 `BLOCKED` 상태가 된다.

<img width="473" alt="Screenshot 2024-11-10 at 21 49 47" src="https://github.com/user-attachments/assets/60e18fef-8f5a-4d8e-b9e7-5cd5d0a87052">

```shell
20:55:20.325 [consumer3] [소비 완료] data2 <- []
```

<img width="476" alt="Screenshot 2024-11-10 at 21 49 51" src="https://github.com/user-attachments/assets/52b3226a-8b3d-437b-b41d-694d30de8a74">

```shell
20:55:20.325 [consumer2] [take] 소비자 깨어남
20:55:20.325 [consumer2] [take] 큐에 데이터가 없음, 소비자 대기
```

`c2` 가 락을 획득하고, 큐에서 데이터를 획득하려 하지만 데이터가 없다.

`c2` 는 다시 `wait()` 를 호출해서 대기( `WAITING` ) 상태에 들어가고, 다시 대기 집합에서 관리된다.

<img width="469" alt="Screenshot 2024-11-10 at 21 49 56" src="https://github.com/user-attachments/assets/0bf8b326-1471-4a84-8f5c-953d9519c220">

물론 `c2` 의 지금 이 사이클은 CPU 자원만 소모하고 다시 대기 집합에 들어갔기 때문에 비효율적이다.

만약 소비자인 `c3` 입장에서 생산자, 소비자 스레드를 선택해서 깨울 수 있다면, 소비자인 `c2` 를 깨우지는 않았을 것이다. 

하지만 `notify()` 는 이런 선택을 할 수 없다.

<img width="484" alt="Screenshot 2024-11-10 at 21 49 59" src="https://github.com/user-attachments/assets/40f3451c-a794-4462-ad53-80404f487b1f">

<img width="460" alt="Screenshot 2024-11-10 at 21 50 02" src="https://github.com/user-attachments/assets/dbdcfd9f-2d0f-47c6-9206-b44709211ecd">

```shell
20:55:20.428 [producer3] [생산 시도] data3 -> []
20:55:20.428 [producer3] [put] 생산자 데이터 저장, notify() 호출
```
`p3` 가 데이터를 저장하고 `notify()` 를 통해 스레드 대기 집합에 알린다.

스레드 대기 집합에는 소비자 `c2` 가 있으므로 생산한 데이터를 잘 소비할 수 있다.

<img width="478" alt="Screenshot 2024-11-10 at 21 50 06" src="https://github.com/user-attachments/assets/ee25ca31-7fb4-45d7-b875-9f94435c2ff8">

<img width="475" alt="Screenshot 2024-11-10 at 21 50 10" src="https://github.com/user-attachments/assets/621a5b96-9d98-48db-a70b-519a78b98e28">

```shell
20:55:20.428 [producer3] [생산 완료] data3 -> [data3]
```

<img width="474" alt="Screenshot 2024-11-10 at 21 50 13" src="https://github.com/user-attachments/assets/7eba7f51-1131-40ce-b270-d21aa80408dd">

```shell
20:55:20.428 [consumer2] [take] 소비자 깨어남
20:55:20.429 [consumer2] [take] 소비자 데이터 획득, notify() 호출
```

<img width="456" alt="Screenshot 2024-11-10 at 21 50 17" src="https://github.com/user-attachments/assets/2de186d3-86fd-4f5b-b283-29723e8224e5">

```shell
20:55:20.429 [consumer2] [소비 완료] data3 <- []
```

**생산자 스레드 실행 완료**

<img width="467" alt="Screenshot 2024-11-10 at 21 50 24" src="https://github.com/user-attachments/assets/b62ee6f5-9e60-48b4-81c9-bc55ccec5672">

```shell
20:55:20.533 [     main] 현재 상태 출력, 큐 데이터: []
20:55:20.533 [     main] consumer1: TERMINATED
20:55:20.534 [     main] consumer2: TERMINATED
20:55:20.534 [     main] consumer3: TERMINATED
20:55:20.534 [     main] producer1: TERMINATED
20:55:20.534 [     main] producer2: TERMINATED
20:55:20.535 [     main] producer3: TERMINATED
20:55:20.535 [     main] == [소비자 먼저 실행] 종료, BoundedQueueV3 ==
```

**정리**

최종 결과를 보면 `p1` `p2` `p3` 모두 데이터를 정상 생산하고, c1, c2, c3는 모두 데이터를 정상 소비할 수 있었다.

하지만 소비자인 `c1` 이 같은 소비자인 `c2` , `c3` 를 깨울 수 있었다.

이 경우 큐에 데이터가 없을 가능성이 있다.

이때는 깨어난 소비자 스레드가 CPU 자원만 소모하고 다시 대기 집합에 들어갔기 때문에 비효율적이다.

만약 소비자인 `c1` 입장에서 생산자, 소비자 스레드를 선택해서 깨울 수 있다면, 소비자인 `c2` 를 깨우지는 않았을 것이다.

예를 들어서 소비자는 생산자만 깨우고, 생산자는 소비자만 깨울 수 있다면 더 효율적으로 작동할 수 있을 것 같다. 

하지 만 `notify()` 는 이런 선택을 할 수 없다.

물론 이것이 비효율적이라는 것이지 결과에는 아무런 문제가 없다. 약간 돌아서 갈 뿐이다.

## Object - wait, notify - 한계

지금까지 살펴본 `Object.wait()` , `Object.notify()` 방식은 스레드 대기 집합 하나에 생산자, 소비자 스레드를 모두 관리한다. 

그리고 `notify()` 를 호출할 때 임의의 스레드가 선택된다. 

따라서 앞서 살펴본 것 처럼 큐에 데이터가 없는 상황에 소비자가 같은 소비자를 깨우는 비효율이 발생할 수 있다. 

또는 큐에 데이터가 가득 차있는데 생산자가 같은 생산자를 깨우는 비효율도 발생할 수 있다.

### 비효율 - 생산자 실행 예시

<img width="483" alt="Screenshot 2024-11-10 at 22 55 34" src="https://github.com/user-attachments/assets/601bb56e-6d8e-41f5-88ea-b7332e82dbec">

다음과 같은 상황을 가정하겠다.

큐에 `dataX` 가 보관되어 있다.

스레드 대기 집합에는 다음 스레드가 대기하고 있다.

소비자: `c1` , `c2` , `c3`

생산자: `p1` , `p2` , `p3`

`p0` 스레드가 `data0` 생산을 시도한다.

<img width="450" alt="Screenshot 2024-11-10 at 22 55 54" src="https://github.com/user-attachments/assets/da1fce36-2302-4dfa-8ce6-12765031731d">

`p0` 스레드가 실행되면서 `data0` 를 큐에 저장한다. 이때 큐에 데이터가 가득 찬다. 

`notify()` 를 통해 대기 집합의 스레드를 하나 깨운다.

<img width="474" alt="Screenshot 2024-11-10 at 22 56 43" src="https://github.com/user-attachments/assets/f69b8de3-adac-47a6-b829-8a1241d7c148">

만약 `notify()` 의 결과로 소비자 스레드가 깨어나게 되면 소비자 스레드는 큐의 데이터를 획득하고, 완료된다.

<img width="477" alt="Screenshot 2024-11-10 at 22 56 56" src="https://github.com/user-attachments/assets/fa84d198-f553-4af9-9229-42f87d660dc5">

만약 `notify()` 의 결과로 생산자 스레드를 깨우게 되면, 이미 큐에 데이터는 가득 차 있다. 

따라서 데이터를 생 산하지 못하고 다시 대기 집합으로 이동하는 비효율이 발생한다.

### 비효율 - 소비자 실행 예시

<img width="470" alt="Screenshot 2024-11-10 at 22 57 36" src="https://github.com/user-attachments/assets/877a14ba-1ec4-4b99-8fab-c00740898143">

이번에는 반대의 경우로 소비자 `c0` 를 실행해보자.

<img width="463" alt="Screenshot 2024-11-10 at 22 57 44" src="https://github.com/user-attachments/assets/73acf7a7-3a9d-4413-ae4c-48c230f09594">

`c0` 스레드가 실행되고 `data0` 를 획득한다. 

이제 큐에 데이터는 비어있게 된다.

`c0` 스레드는 `notify()` 를 호출한다.

<img width="485" alt="Screenshot 2024-11-10 at 22 57 58" src="https://github.com/user-attachments/assets/3c4303a1-a0f6-450e-a91b-f930e6a9caab">

스레드 대기 집합에서 소비자 스레드가 깨어나면 큐에 데이터가 없기 때문에 다시 대기 집합으로 이동하는 비효율 이 발생한다.

<img width="470" alt="Screenshot 2024-11-10 at 22 58 09" src="https://github.com/user-attachments/assets/0cff206d-c2e1-4a43-ab95-f3f21eb47964">

스레드 대기 집합에서 생산자 스레드가 깨어나면 큐에 데이터를 저장하고 완료된다.

**같은 종류의 스레드를 깨울 때 비효율이 발생한다.**

이 내용을 통해서 알 수 있는 사실은 생산자가 같은 생산자를 깨우거나, 소비자가 같은 소비자를 깨울 때 비효율이 발생 할 수 있다는 점이다. 

생산자가 소비자를 깨우고, 반대로 소비자가 생산자를 깨운다면 이런 비효율은 발생하지 않는다.

### 스레드 기아(thread starvation)

`notify()` 의 또 다른 문제점으로는 어떤 스레드가 깨어날 지 알 수 없기 때문에 발생할 수 있는 스레드 기아 문제가
있다.

<img width="475" alt="Screenshot 2024-11-10 at 22 58 37" src="https://github.com/user-attachments/assets/76453e1c-d5c9-4a18-bc15-c9e57a8a1d72">

`notify()` 가 어떤 스레드를 깨울지는 알 수 없다. 

최악의 경우 `c1` ~ `c5` 스레드가 반복해서 깨어날 수 있다. 

`c1` 이 깨어나도 큐에 소비할 데이터가 없다. 따라서 다시 스레드 대기 집합에 들어간다.

`notify()` 로 다시 깨우는데 어떤 스레드를 깨울지 알 수 없다. 따라서 `c1` ~ `c5` 스레드가 반복해서 깨어날 수 있다.

`p1` 은 실행 순서를 얻지 못하다가 아주 나중에 깨어날 수도 있다.

이렇게 대기 상태의 스레드가 실행 순서를 계속 얻지 못해서 실행되지 않는 상황을 스레드 기아(starvation) 상태라 한다.

물론 `p1` 이가장먼저실행될수도있다.

이런 문제를 해결하는 방법 중에 `notify()` 대신에 `notifyAll()` 을 사용하는 방법이 있다.

### notifyAll()

`notifyAll()` 을 사용하면 스레드 대기 집합에 있는 모든 스레드를 한번에 다 깨울 수 있다.

<img width="491" alt="Screenshot 2024-11-10 at 22 59 25" src="https://github.com/user-attachments/assets/bc8d5859-b19c-4632-abe7-f24e19c04316">

데이터를 획득한 `c0` 스레드가 `notifyAll()` 을 호출한다.

<img width="473" alt="Screenshot 2024-11-10 at 22 59 34" src="https://github.com/user-attachments/assets/28199705-d6be-4cea-b8fa-a133d492fa0c">

대기 집합에 있는 모든 스레드가 깨어난다.

모든 스레드는 다 임계 영역 안에 있다. 따라서 락을 먼저 획득해야 한다.

락을 획득하지 못하면 `BLOCKED` 상태가 된다.

만약 `c1` 이 먼저 락을 먼저 획득한다면 큐에 데이터가 없으므로 다시 스레드 대기 집합에 들어간다.

`c2` ~ `c5` 모두 마찬가지이다.

<img width="462" alt="Screenshot 2024-11-10 at 22 59 48" src="https://github.com/user-attachments/assets/53309813-c243-45e5-a9a1-56761c191627">

따라서 `p1` 이 가장 늦게 락 획득을 시도해도, `c1` ~ `c5` 는 모두 스레드 대기 집합에 들어갔으므로 결과적으로 `p1` 만 남게 되고, 결국 락을 획득하게 된다.

<img width="461" alt="Screenshot 2024-11-10 at 23 00 01" src="https://github.com/user-attachments/assets/5c668d60-1c83-403f-8eda-f8d56189ad1d">

`p1` 은 락을 획득하고, 데이터를 생성한 다음에 `notifyAll()` 을 호출하고 실행을 완료할 수 있다. 

참고로 반대의 경우도 같은 스레드 기아 문제가 발생할 수 있기 때문에 `notifyAll()` 을 호출한다.

결과적으로 **notifyAll()** 을 사용해서 스레드 기아 문제는 막을 수 있지만, 비효율을 막지는 못한다.

### 정리

지금까지 만든 `BoundedQueue` 의 구현체들을 간단하게 정리해보자.

**BoundedQueueV1**

단순한 큐 자료 구조이다. 스레드를 제어할 수 없기 때문에, 버퍼가 가득 차거나, 버퍼에 데이터가 없는 한정된 버 퍼 상황에서 문제가 발생한다.

버퍼가 가득 찬 경우: 생산자의 데이터를 버린다.

버퍼에 데이터가 없는 경우: 소비자는 데이터를 획득할 수 없다. ( `null` )


**BoundedQueueV2**

앞서 발생한 문제를 해결하기 위해 반복문을 사용해서 스레드를 대기하는 방법을 적용했다. 

하지만 `synchronized` 임계 영역 안에서 락을 들고 대기하기 때문에, 다른 스레드가 임계 영역에 접근할 수 없는 문제가 발생했다. 

결과적으로 나머지 스레드는 모두 `BLOCKED` 상태가 되고, 자바 스레드 세상이 멈추는 심각한 문제가 발생했다.

**BoundedQueueV3**

`synchronized` 와 함께 사용할 수 있는 `wait()` , `notify()` , `notifyAll()` 을 사용해서 문제를 해결했다. 

`wait()` 를 사용하면 스레드가 대기할 때, 락을 반납하고 대기한다. 

이후에 `notify()` 를 호출하면 스레드가 깨어나면서 락 획득을 시도한다. 이때 락을 획득하면 `RUNNABLE` 상태가 되고, 락을 획득하지 못하면 락 획득을 대기하는 `BLOCKED` 상태가된다.

이렇게 해서 스레드를 제어하는 큐 자료 구조를 만들 수 있었다. 생산자 스레드는 버퍼가 가득차면 버퍼에 여유가 생길 때 까지 대기한다. 

소비자 스레드는 버퍼에 데이터가 없으면 버퍼에 데이터가 들어올 때 까지 대기한다.

이런 구현 덕분에 단순한 자료 구조를 넘어서 스레드까지 제어할 수 있는 자료 구조를 완성했다.

이 방식의 단점은 스레드가 대기하는 대기 집합이 하나이기 때문에, 원하는 스레드를 선택해서 깨울 수 없다는 문 제가 있었다. 

예를 들어서 생산자는 데이터를 생산한 다음 대기하는 소비자를 깨워야 하는데, 대기하는 생산자를 깨울 수 있다. 

따라서 비효율이 발생한다. 물론 이렇게 해도 비효율이 있을 뿐 로직은 모두 정상 작동한다.



지금까지 자바 `synchronized` 와 `Object.wait()` , `Object.notify()` , `Object.notifyAll()` 을 사용해서 생산자 소비자 문제를 해결해보았다.

이 기술을 사용한 덕분에 생산자는 큐에 데이터가 가득차 있어도, 큐에 데이터를 저장할 공간이 생길 때 까지 대기할 수 있었다. 

소비자도 큐에 데이터가 없어도, 큐에 데이터가 들어올 때 까지 대기할 수 있었다. 결과적으로 버리는 데이터 없 이 안전하게 데이터를 큐에 보관하고 또 소비할 수 있었다.

하지만 이 방법은 일부 비효율이 발생했다.

생산자 스레드는 데이터를 생성하고, 대기중인 소비자 스레드에게 알려주어야 한다. 

소비자 스레드는 데이터를 소비자 고, 대기중인 생산자 스레드에게 알려주어야 한다. 

하지만 스레드 대기 집합은 하나이고 이 안에 생산자 스레드와 소비자 스레드가 함께 대기한다. 

그리고 `notify()` 는 원하는 목표를 지정할 수 없었다. 

물론 `notifyAll()` 을 사용할 수 있지만, 원하지 않는 모든 스레드까지 모두 깨어난다. 이런 문제를 해결하려면 어떻게 해야할까?













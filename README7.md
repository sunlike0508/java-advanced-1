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


























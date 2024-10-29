# 프로세스와 쓰레드

## 멀티태스킹과 멀테프로세싱

멀티스레드에 대해서 제대로 이해하려면 먼저 멀티태스킹과 프로세스 같은 운영체제의 기본 개념들에 대해서 알아야한다. 

여기서는 멀티스레드를 이해하기 위한 목적으로 최대한 단순하게 핵심 내용만 알아보겠다.

* 단일 프로그램 실행

만약 프로그램을 2개 이상 동시에 실행한다고 가정해보자. 

예를 들어서 음악 프로그램을 통해 음악을 들으면서, 동시에 워드 프로그램을 통해 문서를 작성하는 것이다. 

여기서는 연산을 처리할 수 있는 CPU 코어가 1개만 있다고 가정하겠다.

**그림1 - 프로그램A의 코드1 실행 시작**

<img width="562" alt="image" src="https://github.com/user-attachments/assets/363a2759-a5e6-4c7c-a05b-48cda8e1276c">

**그림2 - 프로그램A의 코드2 실행 중**

<img width="596" alt="image" src="https://github.com/user-attachments/assets/732d75d2-1ea9-4ee7-8494-6b263888bcd5">

**그림3 - 프로그램A의 실행 완료**

<img width="617" alt="image" src="https://github.com/user-attachments/assets/2bfc1313-7dd2-40f4-933a-2b877151a189">

**그림4 - 프로그램B 실행 시작**

<img width="601" alt="image" src="https://github.com/user-attachments/assets/eb76961a-24ec-4e59-ab20-61e166cb0b7a">

**그림5 - 프로그램A 완료후 프로그램 B완료**

<img width="596" alt="image" src="https://github.com/user-attachments/assets/9e12408e-05ec-4a0a-bd93-740ed927f2b7">

프로그램의 실행이란 프로그램을 구성하는 코드를 순서대로 CPU에서 연산(실행)하는 일이다.

여기서 CPU 코어는 하나로 가정하므로, 한 번에 하나의 프로그램 코드만 실행할 수 있다.

이때, 하나의 프로그램 안에 있는 코드를 모두 실행한 후에야 다른 프로그램의 코드를 실행할 수 있다면? 

예를 들어 음악 프로그램이 끝난 뒤에야 워드 프로그램을 실행할 수 있다면 컴퓨터 사용자는 매우 답답할 것이다.

실제로 초창기의 컴퓨터는 이 처럼 한 번에 하나의 프로그램만 실행했다.

이를 해결하기 위해 하나의 CPU 코어로 여러 프로그램을 동시에 실행하는 '멀티태스킹' 기술이 등장했다.

### 멀티태스킹

순서대로 촬영한 연속된 사진을 빠르게 교차해서 보여줄 경우 사람은 이를 움직이는 영상으로 인지한다. 

애니메이션이바로 이 원리를 이용한다. 예를 들어 우리가 애니메이션을 볼 때 1초에 30 ~ 60장의 사진이 지나간다. 

이 정도 속도면 사람은 사진이 아니라 연속해서 움직이는 영상으로 인지한다. 

현대의 CPU는 초당 수십억 번 이상의 연산을 수행한다.

쉽게 이야기해서 초당 수십억 장의 사진이 빠르게 교차되는 것이다.

만약 CPU가 매우 빠르게 두 프로그램의 코드를 번갈아 수행한다면, 사람이 느낄 때 두 프로그램이 동시에 실행되는 것처럼 느껴질 것이다. (대략 0.01초(10ms) 단위로 돌아가며 실행한다.)

**그림1**

<img width="600" alt="image" src="https://github.com/user-attachments/assets/4b61da09-11bc-41c3-b467-acc1867f5ba1">

**그림1 - 프로그램B의 코드1 수행**

<img width="598" alt="image" src="https://github.com/user-attachments/assets/855980f2-8a7c-479e-a941-382bfee3016b">

**그림3 - 프로그램 수행 완료**

<img width="583" alt="image" src="https://github.com/user-attachments/assets/c7b5a611-f573-45e6-b34a-8d527c9c668d">

이 방식은 CPU 코어가 프로그램A의 코드를 0.01초 정도 수행하다가 잠시 멈추고, 프로그램B의 코드를 0.01초 정도 수행한다. 

그리고 다시 프로그램A의 이전에 실행중인 코드로 돌아가서 0.01초 정도 코드를 수행하는 방식으로 반복 동작한다.

이렇게 각 프로그램의 실행 시간을 분할해서 마치 동시에 실행되는 것 처럼 하는 기법을 시분할(Time Sharing, 시간공유) 기법이라 한다. 

이런 방식을 사용하면 CPU 코어가 하나만 있어도 여러 프로그램이 동시에 실행되는 것 처럼 느낄수 있다.

이렇게 하나의 컴퓨터 시스템이 동시에 여러 작업을 수행하는 능력을 멀티태스킹(Multitasking)이라 한다.

**참고**: CPU에 어떤 프로그램이 얼마만큼 실행될지는 운영체제가 결정하는데 이것을 스케줄링(Scheduling)이라한다. 

이때 단순히 시간으로만 작업을 분할하지는 않고, CPU를 최대한 활용할 수 있는 다양한 우선순위와 최적화 기법을 사용한다. 

우리는 운영체제가 스케줄링을 수행하고, CPU를 최대한 사용하면서 작업이 골고루 수행될 수 있게 최적화한다는 정도로 이해하면 충분하다. 

자세한 내용이 궁금한 분들은 운영체제 이론을 참고하자

멀티프로세싱 CPU 코어가 둘 이상이면 어떻게 될까?

여기서는 프로그램은 A, B, C 3가지이고, CPU 코어는 2개이다.

CPU 안에는 실제 연산을 처리할 수 있는 코어라는 것이 있다. 

과거에는 하나의 CPU 안에 보통 하나의 코어만 들어있었다. 

그래서 CPU와 코어를 따로 분리해서 이야기하지 않았다. 최근에는 하나의 CPU 안에 보통 2개이상의 코어가 들어있다.

**그림1 - 프로그램A, 프로그램B 실행**

<img width="566" alt="image" src="https://github.com/user-attachments/assets/e1c4cba7-a150-4759-b0af-be8fbac4b8b5">


CPU 코어가 2개이므로 물리적으로 동시에 2개의 프로그램을 처리할 수 있다.

여기서는 CPU 코어가 먼저 프로그램A와 프로그램B를 실행한다.

**그림2 - 프로그램C, 프로그램A 실행**

<img width="586" alt="image" src="https://github.com/user-attachments/assets/2ccf6342-5b15-4601-a240-5a53a6c39677">

CPU 코어들이 프로그램A와 프로그램B를 실행하다가 잠시 멈추고, 프로그램C와 프로그램A를 수행한다. 이런식으로 코어가 2개여도 2개보다 더 많은 프로그램을 실행할 수 있다.

**그림3 - 실행 완료**

<img width="591" alt="image" src="https://github.com/user-attachments/assets/92f13435-15bd-4c69-b033-ec072ad09a85">

**멀티프로세싱 vs. 멀티태스킹**

멀티프로세싱은 하드웨어 장비의 관점이고, 멀티태스킹은 운영체제 소프트웨어의 관점이다.

**멀티프로세싱**

여러 CPU(여러 CPU 코어)를 사용하여 동시에 여러 작업을 수행하는 것을 의미한다.

하드웨어 기반으로 성능을 향상시킨다.

예: 다중 코어 프로세서를 사용하는 현대 컴퓨터 시스템

**멀티태스킹**
단일 CPU(단일 CPU 코어)가 여러 작업을 동시에 수행하는 것처럼 보이게 하는 것을 의미한다.

소프트웨어 기반으로 CPU 시간을 분할하여 각 작업에 할당한다.

예: 현대 운영 체제에서 여러 애플리케이션이 동시에 실행되는 환경

참고로 이 예는 여러 CPU 코어를 사용하기 때문에 멀티프로세싱이다. 동시에 각각의 단일 CPU 코어에 여러 작업을 분할해서 수행하기 때문에 멀티태스킹이다.

## 프로세스와 스레드

<img width="586" alt="image" src="https://github.com/user-attachments/assets/0cc4215a-f1cb-4206-baaa-2f97bf0abc73">

### 프로세스

프로그램은 실제 실행하기 전까지는 단순한 파일에 불과하다.

프로그램을 실행하면 프로세스가 만들어지고 프로그램이 실행된다.

이렇게 운영체제 안에서 **실행중인 프로그램을 프로세스**라 한다.

프로세스는 실행 중인 프로그램의 **인스턴스**이다.

자바 언어로 비유를 하자면 클래스는 프로그램이고, 인스턴스는 프로세스이다.

프로세스는 실행 중인 프로그램의 인스턴스이다. 

각 프로세스는 독립적인 메모리 공간을 갖고 있으며, 운영체제에서 별도의 작업 단위로 분리해서 관리된다. 

각 프로세스는 별도의 메모리 공간을 갖고 있기 때문에 서로 간섭하지 않는다. 

그리고 프로세스가 서로의 메모리에 직접 접근할 수 없다.

프로세스는 이렇듯 서로 격리되어 관리되기 때문에, 하나의 프로세스가 충돌해도 다른 프로세스에는 영향을 미치지 않는다. 

쉽게 이야기해서 특정 프로세스(프로그램)에 심각한 문제가 발생하면 해당 프로세스만 종료되고, 다른 프로세스에 영향을 주지 않는다.

**프로세스의 메모리 구성**

* **코드 섹션**: 실행할 프로그램의 코드가 저장되는 부분
* **데이터 섹션**: 전역 변수 및 정적 변수가 저장되는 부분(그림에서 기타에 포함)
* **힙 (Heap)**: 동적으로 할당되는 메모리 영역
* **스택 (Stack)**: 메서드(함수) 호출 시 생성되는 지역 변수와 반환 주소가 저장되는 영역(스레드에 포함)

### 스레드 (Thread)

**프로세스는 하나 이상의 스레드를 반드시 포함한다.**

스레드는 프로세스 내에서 실행되는 작업의 단위이다. 

한 프로세스 내에서 여러 스레드가 존재할 수 있으며, 이들은 프로세스가 제공하는 동일한 메모리 공간을 공유한다. 

스레드는 프로세스보다 단순하므로 생성 및 관리가 단순하고 가볍다.

**메모리 구성**
* **공유 메모리**: 같은 프로세스의 코드 섹션, 데이터 섹션, 힙(메모리)은 프로세스 안의 모든 스레드가 공유한다.
* **개별 스택**: 각 스레드는 자신의 스택을 갖고 있다.

**프로그램이 실행된다는 것은 어떤 의미일까?**

프로그램을 실행하면 운영체제는 먼저 디스크에 있는 파일 덩어리인 프로그램을 메모리로 불러오면서 프로세스를 만든다. 

그럼 만들어진 프로세스를 어떻게 실행할까?

프로그램이 실행된다는 것은 사실 프로세스 안에 있는 코드가 한 줄씩 실행되는 것이다. 

코드는 보통 `main()` 부터 시작해서 하나씩 순서대로 내려가면서 실행된다.

```java
public class Operator {
  public static void main(String[] args) {
    int sum1 = 1;
    int sum2 = sum1 + 1;
    System.out.println("sum1 = " + sum1);
    System.out.println("sum2 = " + sum2);
  }
}
```

생각해보면 어떤 무언가가 코드를 하나씩 순서대로 실행하기 때문에 프로그램이 작동하고 계산도 하고, 출력도 할 수 있다.

이 코드를 하나씩 실행하면서 내려가는 것의 정체가 무엇일까?

비유를 하자면 마치 실(thread) 같은 것이 코드를 위에서 아래로 하나씩 꿰면서 하나씩 내려가는 것 같다.

이렇듯 프로세스의 코드를 실행하는 흐름을 스레드(thread)라 한다. 

스레드는 번역하면 "실", "실을 꿰다"라는 뜻이다.

스레드는 프로세스 내에서 실행되는 작업의 단위이다. 

한 프로세스 내에 하나의 스레드가 존재할 수 있고, 한 프로세스내에 여러 스레드가 존재할 수도 있다. 

그리고 스레드는 프로세스가 제공하는 동일한 메모리 공간을 공유한다.

* **단일 스레드**: 한 프로세스 내에 하나의 스레드만 존재
* **멀티 스레드**: 한 프로세스 내에 여러 스레드가 존재

**하나의 프로세스 안에는 최소 하나의 스레드가 존재**한다. 

그래야 프로그램이 실행될 수 있다.

참고로 우리가 지금까지 작성한 자바 코드들은 모두 한 프로세스 내에서 하나의 스레드만 사용하는 단일 스레드였다.

정리하면 프로세스는 실행 환경과 자원을 제공하는 컨테이너 역할을 하고, 스레드는 CPU를 사용해서 코드를 하나하나 실행한다.

**멀티스레드가 필요한 이유**

하나의 프로그램도 그 안에서 동시에 여러 작업이 필요하다.

워드 프로그램으로 문서를 편집하면서, 문서가 자동으로 저장되고, 맞춤법 검사도 함께 수행된다.

유튜브는 영상을 보는 동안, 댓글도 달 수 있다.

운영제체 관점에서 보면 다음과 같이 구분할 수 있다.

**워드 프로그램 - 프로세스A**

스레드1: 문서 편집

스레드2: 자동 저장

스레드3: 맞춤법 검사

**유튜브 - 프로세스B**

스레드1: 영상 재생

스레드2: 댓글

## 스레드와 스케줄링
앞서 멀티태스킹에서 설명한 운영체제의 스케줄링을 과정을 더 자세히 알아보자.

CPU 코어는 1개이고, 프로세스는 2개이다. 프로세스 A는 스레드1개 프로세스B는 스레드가 2개 있다.

프로세스는 실행 환경과 자원을 제공하는 컨테이너 역할을 하고, 실제 CPU를 사용해서 코드를 하나하나 실행하는 것은 스레드이다.

<img width="593" alt="image" src="https://github.com/user-attachments/assets/e34964ea-aae3-48c7-b5f6-201ce36e50e9">

프로세스A에 있는 스레드A1을 실행한다.

<img width="598" alt="image" src="https://github.com/user-attachments/assets/7822bfd9-943f-4206-8b77-b8aed3180e37">

프로세스A에 있는 스레드A1의 실행을 잠시 멈추고 프로세스B에 있는 스레드 B1을 실행한다.

<img width="604" alt="image" src="https://github.com/user-attachments/assets/9c443847-2233-42d2-b5a8-b45e8d1b3b6b">

프로세스B에 있는 스레드 B1의 실행을 잠시 멈추고 같은 프로세스의 스레드 B2를 실행한다.

이후에 프로세스A에 있는 스레드A1을 실행한다.

이 과정을 반복한다.

### 단일 코어 스케줄링

운영체체가 스레드를 어떻게 스케줄링 하는지, 스케줄링 관점으로 알아보자.

운영체제는 내부에 스케줄링 큐를 가지고 있고, 각각의 스레드는 스케줄링 큐에서 대기한다.

<img width="584" alt="image" src="https://github.com/user-attachments/assets/4bd4fbff-8a03-482d-a4ba-bb2b56044f3e">

스레드A1, 스레드B1, 스레드B2가 스케줄링 큐에 대기한다.

<img width="589" alt="image" src="https://github.com/user-attachments/assets/e22148b6-7535-433b-a971-ea3600d53005">

운영체제는 스레드A1을 큐에서 꺼내고 CPU를 통해 실행한다.
이때 스레드A1이 프로그램의 코드를 수행하고, CPU를 통한 연산도 일어난다.

<img width="575" alt="image" src="https://github.com/user-attachments/assets/5d871a70-0573-467f-bd27-3a831caaea57">

운영체제는 스레드A1을 잠시 멈추고, 스케줄링 큐에 다시 넣는다.

<img width="587" alt="image" src="https://github.com/user-attachments/assets/a77be9ff-4379-4ee5-89ca-2af0c459cbb0">

운영체제는 스레드B1을 큐에서 꺼내고 CPU를 통해 실행한다.

이런 과정을 반복해서 수행한다.

### 멀티 코어 스케줄링

CPU 코어가 2개 이상이면 한 번에 더 많은 스레드를 물리적으로 진짜 동시에 실행할 수 있다.

<img width="596" alt="image" src="https://github.com/user-attachments/assets/978c2af5-f210-4a63-b902-f49add9aa565">

스레드A1, 스레드B1, 스레드B2가 스케줄링 큐에 대기한다.

<img width="588" alt="image" src="https://github.com/user-attachments/assets/30766234-cb0e-4ba2-89b2-5444bb868b69">

스레드A1, 스레드B1을 병렬로 실행한다. 스레드B2는 스케줄링 큐에 대기한다.

<img width="592" alt="image" src="https://github.com/user-attachments/assets/243421b4-aa22-4f15-8a9a-720cbb01bd74">

스레드A1의 수행을 잠시 멈추고, 스레드A1을 스케줄링 큐에 다시 넣는다.

<img width="588" alt="image" src="https://github.com/user-attachments/assets/14185d06-7e68-4c90-871b-be77e10e587b">

스케줄링 큐에 대기 중인 스레드B1을 CPU 코어1에서 실행한다.

물론 조금 있다가 CPU 코어2에서 실행중인 스레드B2도 수행을 멈추고, 스레드 스케줄링 큐에 있는 다른 스레드가 실행 될 것이다.

이런 과정을 반복해서 수행한다.

**참고**: 

CPU에 어떤 프로그램이 얼마만큼 실행될지는 운영체제가 결정하는데 이것을 스케줄링(Scheduling)이라한다. 

이때 단순히 시간으로만 작업을 분할하지는 않고, CPU를 최대한 활용할 수 있는 다양한 우선순위와 최적화기법을 사용한다. 

우리는 운영체제가 스케줄링을 수행하고, CPU를 최대한 사용하면서 작업이 골고루 수행될 수 있게 최적화한다는 정도로 이해하면 충분하다.

### 프로세스, 스레드와 스케줄링 - 정리

**멀티태스킹과 스케줄링**

멀티태스킹이란 동시에 여러 작업을 수행하는 것을 말한다. 

이를 위해 운영체제는 스케줄링이라는 기법을 사용한다. 스케줄링은 CPU 시간을 여러 작업에 나누어 배분하는 방법이다.

**프로세스와 스레드**

**프로세스**는 실행 중인 프로그램의 인스턴스이다. 각 프로세스는 독립적인 메모리 공간을 가지며, 운영체제에서 독립된 실행 단위로 취급된다.

**스레드**는 프로세스 내에서 실행되는 작은 단위이다. 여러 스레드는 하나의 프로세스 내에서 자원을 공유하며, 프로세스의 코드, 데이터, 시스템 자원등을 공유한다. 실제로 CPU에 의해 실행되는 단위는 스레드이다.

**프로세스의 역할**
프로세스는 실행 환경을 제공한다. 여기에는 메모리 공간, 파일 핸들, 시스템 자원(네트워크 연결) 등이 포함된다.

이는 프로세스가 컨테이너 역할을 한다는 의미이다.

프로세스 자체는 운영체제의 스케줄러에 의해 직접 실행되지 않으며, 프로세스 내의 스레드가 실행된다. 

참고로 1개의 프로세스 안에 하나의 스레드만 실행되는 경우도 있고, 1개의 프로세스 안에 여러 스레드가 실행되는 경우도 있다.

##컨텍스트 스위칭

멀티태스킹이 반드시 효율적인 것 만은 아니다.

**사람의 멀티태스킹**

비유를 하자면 내가 프로그램A를 개발하고 있는데, 갑자기 기획자가 프로그램B를 수정해달라고 한다. 

프로그램A의 개발을 멈추고, 프로그램B를 수정한다고 가정해보자. 여기서 프로그램B의 수정을 잘 마치고, 다시 프로그램A를 개발하기 위해 돌아간다.

이때 먼저 프로그램A의 어디를 개발하고 있었는지 해당 코드의 위치를 찾아야 한다. 

그리고 개발할 때 변수들을 많이 선언하는데, 변수들에 어떤 값들이 들어가는지 머리속에 다시 불러와야 한다.

만약 프로그램A의 개발이 다 끝나고 나서, 프로그램B를 수정한다면, 전체 시간으로 보면 더 효율적으로 개발할 수 있을것이다.

**컴퓨터의 멀티태스킹**

운영체제의 멀티태스킹을 생각해보자. CPU 코어는 하나만 있다고 가정하자.

스레드A, 스레드B가 있다.

운영체제는 먼저 스레드A를 실행한다. 

멀티태스킹을 해야 하기 때문에 스레드A를 계속 실행할 수 없다. 

스레드A를 잠시 멈추고, 스레드B를 실행한다. 

이후에 스레드A로 그냥 돌아갈 수 없다. 

CPU에서 스레드를 실행하는데, 스레드A의코드가 어디까지 수행되었는지 위치를 찾아야 한다. 

그리고 계산하던 변수들의 값을 CPU에 다시 불러들여야 한다. 

따라서 **스레드A를 멈추는 시점에 CPU에서 사용하던 이런 값들을 메모리에 저장해두어야 한다. 그리고 이후에 스레드A를 다시 실행할 때 이 값들을 CPU에 다시 불러와야 한다.**

이런 과정을 **컨텍스트 스위칭(context switching)**이라 한다.

컨텍스트는 현재 작업하는 문맥을 뜻한다. 

현재 작업하는 문맥이 변하기 때문에 컨텍스트 스위칭이다.

컨텍스트 스위칭 과정에서 이전에 실행 중인 값을 메모리에 잠깐 저장하고, 이후에 다시 실행하는 시점에 저장한 값을 CPU에 다시 불러와야 한다.

결과적으로 컨텍스트 스위칭 과정에는 약간의 비용이 발생한다.

**멀티스레드는 대부분 효율적이지만, 컨텍스트 스위칭 과정이 필요하므로 항상 효율적인 것은 아니다**

예를 들어서 1 ~ 10000까지 더해야 한다고 가정해보자. 이 문제는 둘로 나눌 수 있다.

스레드1: 1 ~ 5000까지 더함

스레드2: 5001 ~ 10000까지 더함

마지막에 스레드1의 결과와 스레드2의 결과를 더함

**CPU 코어가 2개**

CPU 코어가 2개 있다면 스레드1, 스레드2로 나누어 멀티스레드로 병렬 처리하는게 효율적이다. 모든 CPU를 사용하므로 연산을 2배 빠르게 처리할 수 있다.

**CPU 코어가 1개**

CPU 코어가 1개 있는데, 스레드를 2개로 만들어서 연산하면 중간중간 컨텍스트 스위칭 비용이 발생한다. 

운영체제 스케줄링 방식에 따라서 다르겠지만, 스레드1을 1 ~ 1000 정도까지 연산한 상태에서 잠시 멈추고 스레드2를 5001 ~ 6001까지 연산하는 식으로 반복할 수 있다. 

이때 CPU는 스레드1을 멈추고 다시 실행할 때 어디까지 연산했는지 알아야 하고, 그 값을 CPU에 다시 불러와야 한다. 

결과적으로 이렇게 반복할 때 마다 컨텍스트 스위칭 비용(시간)이든다.

결과적으로 연산 시간 + 컨텍스트 스위칭 시간이 든다.

이런 경우 단일 스레드로 1 ~ 10000까지 더하는 것이 컨텍스트 스위칭 비용 없이, 연산 시간만 사용하기 때문에 더 효율적이다.

예를 이렇게 들었지만 실제로 컨텍스트 스위칭에 걸리는 시간은 아주 짧다. 하지만 스레드가 매우 많다면 이 비용이 커질 수 있다.

물론 최신 CPU는 초당 수 십억 단위를 계산하기 때문에 실제로는 계산에 더 큰 숫자를 사용해야 컨텍스트 스위칭이 발생한다.

### 참고 - 실무 이야기

**CPU - 4개, 스레드 2개**

스레드의 숫자가 너무 적으면 모든 CPU를 100% 다 활용할 수 없지만, 스레드가 몇 개 없으므로 컨텍스트 스위칭 비용이 줄어든다.

**CPU - 4개, 스레드 100개**

스레드의 숫자가 너무 많으면 CPU를 100% 다 활용할 수 있지만 컨텍스트 스위칭 비용이 늘어난다.

**CPU - 4개, 스레드 4개**

스레드의 숫자를 CPU의 숫자에 맞춘다면 CPU를 100% 활용할 수 있고, 컨텍스트 스위칭 비용도 자주 발생하지 않기

때문에 최적의 상태가 된다. 이상적으로는 CPU 코어 수 + 1개 정도로 스레드를 맞추면 특정 스레드가 잠시 대기할 때 남은 스레드를 활용할 수 있다.

**CPU 바운드 작업 vs I/O 바운드 작업**

각각의 스레드가 하는 작업은 크게 2가지로 구분할 수 있다.

**CPU-바운드 작업 (CPU-bound tasks)**

CPU의 연산 능력을 많이 요구하는 작업을 의미한다.

이러한 작업은 주로 계산, 데이터 처리, 알고리즘 실행 등 CPU의 처리 속도가 작업 완료 시간을 결정하는 경우다.

예시: 복잡한 수학 연산, 데이터 분석, 비디오 인코딩, 과학적 시뮬레이션 등

**I/O-바운드 작업 (I/O-bound tasks)**

디스크, 네트워크, 파일 시스템 등과 같은 입출력(I/O) 작업을 많이 요구하는 작업을 의미한다.

이러한 작업은 I/O 작업이 완료될 때까지 대기 시간이 많이 발생하며, CPU는 상대적으로 유휴(대기) 상태에 있는 경우가 많다. 

쉽게 이야기해서 스레드가 CPU를 사용하지 않고 I/O 작업이 완료될 때 까지 대기한다.

예시: 데이터베이스 쿼리 처리, 파일 읽기/쓰기, 네트워크 통신, 사용자 입력 처리 등.

**웹 애플리케이션 서버**

분야마다 다르겠지만, 실무에서는 CPU-바운드 작업 보다는 I/O-바운드 작업이 많다.

예를 들어서 백엔드 개발자의 경우 주로 웹 애플리케이션 서버를 개발하는데, 스레드가 1 ~ 10000까지 더하는 CPU의 연산이 필요한 작업보다는, 대부분 사용자의 입력을 기다리거나, 데이터베이스를 호출하고 그 결과를 기다리는 등, 기다리는 일이 많다. 

쉽게 이야기해서 스레드가 CPU를 많이 사용하지 않는 I/O-바운드 작업이 많다는 뜻이다.

일반적인 자바 웹 애플리케이션 서버의 경우 사용자의 요청 하나를 처리하는데 1개의 스레드가 필요하다.

사용자 4명이 동시에 요청하면 4개의 스레드가 작동하는 것이다. 그래야 4명의 사용자의 요청을 동시에 처리할 수 있다.

사용자의 요청을 하나 처리하는데, 스레드는 CPU를 1% 정도 사용하고, 대부분 데이터베이스 서버에 어떤 결과를 조회하면서 기다린다고 가정하자. 

이때는 스레드는 CPU를 거의 사용하지 않고 대기한다. 바로 I/O-바운드 작업이 많다는 것이다.

이 경우 CPU 코어가 4개 있다고해서 스레드 숫자도 CPU 코어에 맞추어 4개로 설정하면 안된다! 그러면 동시에 4명의 사용자 요청만 처리할 수 있다. 

이때 CPU는 단순하게 계산해서 4% 정도만 사용할 것이다. 결국 사용자는 동시에 4명 밖에 못받지만 CPU는 4%만 사용하며 CPU가 놀고 있는 사태가 벌어질 수 있다.

사용자의 요청 하나를 처리하는데 CPU를 1%만 사용한다면 단순하게 생각해도 100개의 스레드를 만들 수 있다. 

이렇게 하면 동시에 100명의 사용자 요청을 받을 수 있다. 물론 실무에서는 성능 테스트를 통해서 최적의 스레드 숫자를 찾는 것이 이상적이다.

결국 스레드 숫자만 늘리면 되는데, 이런 부분을 잘 이해하지 못해서 서버 장비에 문제가 있다고 생각하고 2배 더 좋은 장비로 구매하는 사태가 발생하기도 한다! 

이렇게 되면 CPU는 4%의 절반인 2%만 사용하고 사용자는 여전히 동시에 4명 밖에 받지 못하는 사태가 벌어진다.

정리하면 스레드의 숫자는 CPU-바운드 작업이 많은가, 아니면 I/O-바운드 작업이 많은가에 따라 다르게 설정해야 한다.

* **CPU-바운드 작업**: CPU 코어 수 + 1개
  * CPU를 거의 100% 사용하는 작업이므로 스레드를 CPU 숫자에 최적화
* **I/O-바운드 작업**: CPU 코어 수 보다 많은 스레드를 생성, CPU를 최대한 사용할 수 있는 숫자까지 스레드 생성
  * CPU를 많이 사용하지 않으므로 성능 테스트를 통해 CPU를 최대한 활용하는 숫자까지 스레드 생성
  * 단 너무 많은 스레드를 생성하면 컨텍스트 스위칭 비용도 함께 증가 - 적절한 성능 테스트 필요

참고로 웹 애플리케이션 서버라도 상황에 따라 CPU 바운드 작업이 많을 수 있다. 이 경우 CPU-바운드 작업에 최적화된 CPU 숫자를 고려하면 된다.

<<<<<<< HEAD
# 쓰레드 생성과 실행

## 쓰레드

### 자바 메모리 구조 복습

**자바 메모리 구조**

<img width="708" alt="Screenshot 2024-10-23 at 22 45 38" src="https://github.com/user-attachments/assets/5c56c55c-29c7-44f8-9dd2-15d6dbfdfeaa">

* **메서드 영역(Method Area)**: 메서드 영역은 프로그램을 실행하는데 필요한 공통 데이터를 관리한다. 이 영역은 프로그램의 모든 영역에서 공유한다.
  * 클래스 정보: 클래스의 실행 코드(바이트 코드), 필드, 메서드와 생성자 코드등 모든 실행 코드가 존재한다. 
  * static 영역: `static` 변수들을 보관한다.
  * 런타임 상수 풀: 프로그램을 실행하는데 필요한 공통 리터럴 상수를 보관한다.

* **스택 영역(Stack Area)**: 자바 실행 시, 하나의 실행 스택이 생성된다. 각 스택 프레임은 지역 변수, 중간 연산 결과, 메서드 호출 정보 등을 포함한다.
  * 스택 프레임: 스택 영역에 쌓이는 네모 박스가 하나의 스택 프레임이다. 메서드를 호출할 때 마다 하나의 스택 프레임이 쌓이고, 메서드가 종료되면 해당 스택 프레임이 제거된다.

**힙 영역(Heap Area)**: 객체(인스턴스)와 배열이 생성되는 영역이다. 가비지 컬렉션(GC)이 이루어지는 주요 영역이며, 더 이상 참조되지 않는 객체는 GC에 의해 제거된다.

**참고**: 스택 영역은 더 정확히는 각 스레드별로 하나의 실행 스택이 생성된다. 

따라서 스레드 수 만큼 스택이 생성 된다. 

지금은 스레드를 1개만 사용하므로 스택도 하나이다. 이후 스레드를 추가할 것인데, 그러면 스택도 스레드 수 만큼 증가한다.

### 스레드 생성

스레드를 직접 만들어보자. 그래서 해당 스레드에서 별도의 로직을 수행해보자.

스레드를 만들 때는 `Thread` 클래스를 상속 받는 방법과 `Runnable` 인터페이스를 구현하는 방법이 있다. 

먼저 `Thread` 클래스를 상속 받아서 스레드를 생성해보자.

#### 스레드 생성 - Thread 상속

자바는 많은 것을 객체로 다룬다. 자바가 예외를 객체로 다루듯이, 스레드도 객체로 다룬다.

스레드가 필요하면, 스레드 객체를 생성해서 사용하면 된다.

```java
public class HelloThread extends Thread {

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + ": run()");
    }
}
public static void main(String[] args) {
  System.out.println(Thread.currentThread().getName());

  HelloThread helloThread = new HelloThread();

  helloThread.start();
}
```

`run()` 메서드가 아니라 반드시 `start()` 메서드를 호출해야 한다. 그래야 별도의 스레드에서 `run()` 코드가 실행된다.

<img width="703" alt="Screenshot 2024-10-23 at 23 18 45" src="https://github.com/user-attachments/assets/d7963c0d-d46a-4c0a-b33c-7951c9be3355">

실행 결과를 보면 `main()` 메서드는 `main` 이라는 이름의 스레드가 실행하는 것을 확인할 수 있다. 

프로세스가 작동하 려면 스레드가 최소한 하나는 있어야 한다. 그래야 코드를 실행할 수 있다. 

자바는 실행 시점에 `main` 이라는 이름의 스 레드를 만들고 프로그램의 시작점인 `main()` 메서드를 실행한다.

<img width="691" alt="Screenshot 2024-10-23 at 23 19 05" src="https://github.com/user-attachments/assets/b9c97047-a0ef-4d7a-8c1f-eb561882e332">

`HelloThread` 스레드 객체를 생성한 다음에 `start()` 메서드를 호출하면 자바는 스레드를 위한 별도의 스택 공간을 할당한다.

스레드 객체를 생성하고, 반드시 `start()` 를 호출해야 스택 공간을 할당 받고 스레드가 작동한다.

스레드에 이름을 주지 않으면 자바는 스레드에 `Thread-0` , `Thread-1` 과 같은 임의의 이름을 부여한다. 

새로운 `Thread-0` 스레드가 사용할 전용 스택 공간이 마련되었다.

`Thread-0` 스레드는 `run()` 메서드의 스택 프레임을 스택에 올리면서 `run()` 메서드를 시작한다.

**메서드를 실행하면 스택 위에 스택 프레임이 쌓인다**

`main` 스레드는 `main()` 메서드의 스택 프레임을 스택에 올리면서 시작한다.

직접 만드는 스레드는 `run()` 메서드의 스택 프레임을 스택에 올리면서 시작한다. 

실행 결과를 보면 `Thread-0` 스레드가 `run()` 메서드를 실행한 것을 확인할 수 있다.

<img width="697" alt="Screenshot 2024-10-23 at 23 19 41" src="https://github.com/user-attachments/assets/b69a8d67-92d8-40fe-a516-e863df364e6e">

`main` 스레드가 `HelloThread` 인스턴스를 생성한다. 이때 스레드에 이름을 부여하지 않으면 자바가 `Thread-0` , `Thread-1` 과 같은 임의의 이름을 부여한다.

`start()` 메서드를 호출하면, `Thread-0` 스레드가 시작되면서 `Thread-0` 스레드가 `run()` 메서드를 호출한다.

여기서 핵심은 `main` 스레드가 `run()` 메서드를 실행하는게 아니라 `Thread-0` 스레드가 `run()` 메서드를 실행한다는 점이다.

`main` 스레드는 단지 `start()` 메서드를 통해 `Thread-0` 스레드에게 실행을 지시할 뿐이다. 

다시 강조하지만 `main` 스레드가 `run()` 을 호출하는 것이 아니다! 

`main` 스레드는 다른 스레드에게 일을 시작하라고 지시만 하고, 바로 `start()` 메서드를 빠져나온다.

이제 `main` 스레드와 `Thread-0` 스레드는 동시에 실행된다.

`main` 스레드 입장에서 보면 그림의 1, 2, 3번 코드를 멈추지 않고 계속 수행한다. 

그리고 `run()` 메서드는 `main` 이 아닌 별도의 스레드에서 실행된다.

**스레드 간 실행 순서는 보장하지 않는다.**

스레드는 동시에 실행되기 때문에 스레드 간에 실행 순서는 얼마든지 달라질 수 있다. 

따라서 다음과 같이 다양한 실행 결과가 나올 수 있다.

스레드 간의 실행 순서는 얼마든지 달라질 수 있다.

CPU 코어가 2개여서 물리적으로 정말 동시에 실행될 수도 있고, 하나의 CPU 코어에 시간을 나누어 실행될 수도 있다. 

그리고 한 스레드가 얼마나 오랜기간 실행되는지도 보장하지 않는다. 

한 스레드가 먼저 다 수행된 다음에 다른 스레드가 수행될 수도 있고, 둘이 완전히 번갈아 가면서 수행되는 경우도 있다.

스레드는 순서와 실행 기간을 모두 보장하지 않는다. 이것이 바로 멀티스레드다!

## 스레드 시작2 

### start() vs run()

스레드의 `start()` 대신에 재정의한 `run()` 메서드를 직접 호출하면 어떻게 될까?

```java
public static void main(String[] args) {
  System.out.println(Thread.currentThread().getName() + "메인 시작");
  
  System.out.println(Thread.currentThread().getName() + "run 호출 시작");
  HelloThread helloThread = new HelloThread();
  helloThread.run();
  System.out.println(Thread.currentThread().getName() + "run 호출 끝");
  System.out.println(Thread.currentThread().getName() + "메인 끝");
}
```

```shell
main메인 시작
mainrun 호출 시작
main: run()
mainrun 호출 끝
main메인 끝
```

<img width="690" alt="Screenshot 2024-10-24 at 22 29 05" src="https://github.com/user-attachments/assets/a1eb2c81-0a04-47ee-bfd6-6d25b91935f7">

실행 결과를 잘 보면 별도의 스레드가 `run()` 을 실행하는 것이 아니라, `main` 스레드가 `run()` 메서드를 호출하는 것을 확인할 수 있다.

자바를 처음 실행하면 `main` 스레드가 `main()` 메서드를 호출하면서 시작한다.

`main` 스레드는 `HelloThread` 인스턴스에 있는 `run()` 이라는 메서드를 호출한다.

`main` 스레드가 `run()` 메서드를 실행했기 때문에 `main` 스레드가 사용하는 스택위에 `run()` 스택 프레임이 올라간다.

<img width="683" alt="Screenshot 2024-10-24 at 22 31 31" src="https://github.com/user-attachments/assets/5ac3e48c-ea05-44b8-aa17-071222a38475">

결과적으로 `main` 스레드에서 모든 것을 처리한 것이 된다.

스레드의 `start()` 메서드는 스레드에 스택 공간을 할당하면서 스레드를 시작하는 아주 특별한 메서드이다. 

그리고 해당 스레드에서 `run()` 메서드를 실행한다. 

따라서 `main` 스레드가 아닌 별도의 스레드에서 재정의한 `run()` 메서 드를 실행하려면, 반드시 `start()` 메서드를 호출해야 한다.

## 데몬 스레드

### 데몬 스레드

스레드는 사용자(user) 스레드와 데몬(daemon) 스레드 2가지 종류로 구분할 수 있다.

**사용자 스레드(non-daemon 스레드)** 

프로그램의 주요 작업을 수행한다.

작업이 완료될 때까지 실행된다.

모든 user 스레드가 종료되면 JVM도 종료된다.

### 데몬 스레드

백그라운드에서 보조적인 작업을 수행한다.

모든 user 스레드가 종료되면 데몬 스레드는 자동으로 종료된다.

JVM은 데몬 스레드의 실행 완료를 기다리지 않고 종료된다. 

데몬 스레드가 아닌 모든 스레드가 종료되면, 자바 프로그램도 종료된다.

**용어 - 데몬** 

그리스 신화에서 데몬은 신과 인간 사이의 중간적 존재로, 보이지 않게 활동하며 일상적인 일들을 도왔다. 

이런 의미로 컴퓨터 과학에서는 사용자에게 직접적으로 보이지 않으면서 시스템의 백그라운드에서 작업을 수행하는 것을 데몬 스레드, 데몬 프로세스라 한다. 

예를 들어서 사용하지 않는 파일이나 메모리를 정리하는 작업 들이 있다.

```java
public class DaemonThreadMain {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + ": main() start");
        DaemonThread dt = new DaemonThread();
        dt.setDaemon(true);
        dt.start();
        System.out.println(Thread.currentThread().getName() + ": main() end");
    }

    static class DaemonThread extends Thread {

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + ": run() start");

            try {
                Thread.sleep(10000);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println(Thread.currentThread().getName() + ": run() end");
        }
    }
}
```

```shell
main: main() start
main: main() end
Thread-0: run() start
```

`setDaemon(true)` 로 설정해보자.

`Thread-0` 는 데몬 스레드로 설정된다.

유일한 user 스레드인 `main` 스레드가 종료되면서 자바 프로그램도 종료된다. 

따라서 `run() end` 가 출력되기 전에 프로그램이 종료된다.

## 스레드 생성 - Runnable

스레드를 만들 때는 `Thread` 클래스를 상속 받는 방법과 `Runnable` 인터페이스를 구현하는 방법이 있다.

이번에는 `Runnable` 인터페이스를 구현하는 방식으로 스레드를 생성해보자

**Runnable 인터페이스** 

자바가 제공하는 스레드 실행용 인터페이스

```java
package java.lang;

public interface Runnable {
  void run();
}
```

```java
public class HelloRunnable implements Runnable {
     @Override
     public void run() {
         System.out.println(Thread.currentThread().getName() + ": run()");
     }
}
```

```java

public static void main(String[] args) {

    System.out.println(Thread.currentThread().getName() + ": main() start");
    HelloRunnable runnable = new HelloRunnable();
    Thread t1 = new Thread(runnable);
    t1.start();
    System.out.println(Thread.currentThread().getName() + ": main() end");
}
```

```shell
main: main() start
main: main() end
Thread-0: run()
```

실행 결과는 기존과 같다. 차이가 있다면, 스레드와 해당 스레드가 실행할 작업이 서로 분리되어 있다는 점이다.

스레드 객체를 생성할 때, 실행할 작업을 생성자로 전달하면 된다.

### Thread 상속 vs Runnable 구현

***스레드 사용할 때는 `Thread` 를 상속 받는 방법보다 `Runnable` 인터페이스를 구현하는 방식을 사용하자.***

두 방식이 서로 장단점이 있지만, 스레드를 생성할 때는 `Thread` 클래스를 상속하는 방식보다 `Runnable` 인터페이스 를 구현하는 방식이 더 나은 선택이다.

**Thread 클래스 상속 방식** 

**장점**

간단한 구현: `Thread` 클래스를 상속받아 `run()` 메서드만 재정의하면 된다.

**단점**

상속의 제한: 자바는 단일 상속만을 허용하므로 이미 다른 클래스를 상속받고 있는 경우 `Thread` 클래스를 상속 받을 수 없다.

유연성 부족: 인터페이스를 사용하는 방법에 비해 유연성이 떨어진다.

**Runnable 인터페이스를 구현 방식** 

**장점**

상속의 자유로움: `Runnable` 인터페이스 방식은 다른 클래스를 상속받아도 문제없이 구현할 수 있다. 

코드의 분리: 스레드와 실행할 작업을 분리하여 코드의 가독성을 높일 수 있다.

여러 스레드가 동일한 `Runnable` 객체를 공유할 수 있어 자원 관리를 효율적으로 할 수 있다.

**단점**

코드가 약간 복잡해질 수 있다. 

`Runnable` 객체를 생성하고 이를 `Thread` 에 전달하는 과정이 추가된다.

정리하자면 `Runnable` 인터페이스를 구현하는 방식을 사용하자. 

스레드와 실행할 작업을 명확히 분리하고, 인터페이스를 사용하므로 `Thread` 클래스를 직접 상속하는 방식보다 더 유연하고 유지보수 하기 쉬운 코드를 만들 수 있다.


## 로거 만들기

현재 어떤 스레드가 코드를 실행하는지 출력하기 위해 다음과 같이 긴 코드를 작성하는 것은 너무 번거롭다. 

```java
System.out.println(Thread.currentThread().getName() + ": run()"); 
```

다음 예시와 같이 실행하면, 현재 시간, 스레드 이름, 출력 내용등이 한번에 나오는 편리한 기능을 만들어보자. 

```java
log("hello thread");
log(123);
```

**실행 결과** 

```
 15:39:02.000 [main] hello thread
 15:39:02.002 [main] 123
```

```java
public abstract class MyLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static void log(Object object) {

        String time = LocalTime.now().format(formatter);

        System.out.printf("%S [%9s] %s\n", time, Thread.currentThread().getName(), object);
    }
}
```

## 여러 스레드 만들기

```java
public static void main(String[] args) {

    log("main() start");

    HelloRunnable runnable = new HelloRunnable();

    Thread thread1 = new Thread(runnable);
    thread1.start();
    Thread thread2 = new Thread(runnable);
    thread2.start();
    Thread thread3 = new Thread(runnable);
    thread3.start();
    
    log("main() end");
}
```

```shell
22:56:47.278 [     main] main() start
22:56:47.281 [     main] main() end
Thread-0: run()
Thread-1: run()
Thread-2: run()
```

실행 결과는 다를 수 있다. 스레드의 실행 순서는 보장되지 않는다.

스레드3개를 생성할 때 모두 같은 `HelloRunnable` 인스턴스( `x001` )를 스레드의 실행 작업으로 전달했다. 

`Thread-0` , `Thread-1` , `Thread-2` 는 모두 `HelloRunnable` 인스턴스에 있는 `run()` 메서드를 실행한다.

<img width="710" alt="Screenshot 2024-10-24 at 22 57 24" src="https://github.com/user-attachments/assets/47ceb4da-7589-44d6-b910-252df9440c77">

## Runnable을 만드는 다양한 방법

### 정적 중첩 클래스 사용

```java
public class InnerRunnableMainV1 {

    public static void main(String[] args) {

        log("main() start");

        MyRunnable runnable = new MyRunnable();
        new Thread(runnable).start();

        log("main() end");
    }

    static class MyRunnable implements Runnable {

        @Override
        public void run() {
            log("run() start");
        }
    }
}
```

### 익명 클래스 사용

```java
public class InnerRunnableMainV2 {

    public static void main(String[] args) {

        log("main() start");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                log("run() start");
            }
        };

        new Thread(runnable).start();

        log("main() end");
    }
}
```

```java
public class InnerRunnableMainV3 {

    public static void main(String[] args) {

        log("main() start");

        Thread thread = new Thread() {
            @Override
            public void run() {
                log("run() start");
            }
        };

        thread.start();

        log("main() end");
    }
}

```

```java
public class InnerRunnableMainV4 {

    public static void main(String[] args) {

        log("main() start");

        Thread thread = new Thread(() -> log("run() start"));

        thread.start();

        log("main() end");
    }
}
```

## 문제와 풀이

### 문제1: Thread 상속

다음 요구사항에 맞게 멀티스레드 프로그램을 작성해라.

1. `Thread` 클래스를 상속받은 `CounterThread` 라는 스레드 클래스를 만들자.
2. 이 스레드는 1부터 5까지의 숫자를 1초 간격으로 출력해야 한다. 
3. 앞서 우리가 만든 `log()` 기능을 사용해서 출력해라.
4. `main()` 메서드에서 `CounterThread` 스레드 클래스를 만들고 실행해라.

실행 결과를 참고하자.

**실행 결과**

```
09:46:23.329 [ Thread-0] value: 1
09:46:24.332 [ Thread-0] value: 2
09:46:25.338 [ Thread-0] value: 3
09:46:26.343 [ Thread-0] value: 4
09:46:27.349 [ Thread-0] value: 5
```

```java
public class CounterThreadMain {
    public static void main(String[] args) {
        Thread thread = new CounterThread();
        thread.start();
    }
  
    static class CounterThread extends Thread {
  
        @Override
        public void run() {
            for(int i = 1; i <= 5; i++) {
                try {
                    log("value: " + i);
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

public static void main(String[] args) {
    new Thread(() -> {
        for(int i = 1; i <= 5; i++) {
            try {
                log("value: " + i);
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }).start();
}
```

### 문제 2 : runnable 로 구현

```java
public class CounterRunnableMain {

    public static void main(String[] args) {
        Runnable r = new CounterRunnable();

        Thread thread = new Thread(r, "counter");
        thread.start();
    }

    static class CounterRunnable implements Runnable {

        @Override
        public void run() {
            for(int i = 1; i <= 5; i++) {
                try {
                    log("value: " + i);
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
```

### 문제 3: 여러 스레드 사용

1. `Thread-A` , `Thread-B` 두 스레드를 만들어라
2. `Thread-A` 는 1초에 한 번씩 "A"를 출력한다.
3. `Thread-B` 는 0.5초에 한 번씩 "B"를 출력한다. 이 프로그램은 강제 종료할 때 까지 계속 실행된다.

```java
/**
 * 나는 그냥 카운터로 해봤음
 */
public class MultiThreadMain {

  public static void main(String[] args) {

    new Thread(() -> run(1000), "ThreadA").start();

    new Thread(() -> run(500), "ThreadB").start();
  }

  static void run(int time) {
    int count = 0;

    while(true) {
      try {
        log("value : " + count++);
        Thread.sleep(time);
      } catch(InterruptedException e) {
        throw new RuntimeException(e);
      }

    }
  }
}
```

[스레드와 생명주기] (https://github.com/sunlike0508/java-advanced-1/blob/main/README2.md)
[메모리 가시성] (https://github.com/sunlike0508/java-advanced-1/blob/main/README3.md)

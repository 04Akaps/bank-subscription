# Kafka 컨슈머 프레임워크

Kotlin으로 구축된 유연하고 구성 가능한 Kafka 컨슈머 애플리케이션으로, 커스터마이징 가능한 핸들러를 통해 Kafka 토픽의 메시지를 처리합니다.

## 개요

이 프레임워크는 높은 구성성을 가진 Kafka 메시지 소비 및 처리를 위한 아키텍처를 제공합니다. 다음과 같은 기능을 제공합니다:

- 전용 핸들러를 통한 다중 Kafka 토픽 처리
- 각 토픽별 폴링 간격, 동시성, 레코드 배치 크기 등의 독립적 구성
- 동기 및 비동기 메시지 처리 지원
- 사용자 정의 비즈니스 로직 구현을 위한 확장 가능한 핸들러 아키텍처
- 데이터 저장을 위한 DB활용

## 아키텍처

```
┌─────────────────────────────┐     ┌───────────────────────────────┐
│                             │     │                               │
│         Kafka Broker        │────▶│      설정 파일                │
│                             │     │    (application.yaml)         │
└─────────────────────────────┘     └───────────────┬───────────────┘
                                                    │
                                                    ▼
┌─────────────────────────────┐     ┌───────────────────────────────┐
│                             │     │                               │
│          메시지 핸들러        │◀────│       컨슈머 프레임워크       │
│                             │     │                               │
└───────────────┬─────────────┘     └───────────────────────────────┘
                │
                ▼
┌─────────────────────────────┐
│                             │
│           DB 저장소           │
│                             │
└─────────────────────────────┘
```

## 설정

애플리케이션은 YAML을 통해 구성이 되며, 실행ㄱ됩니다.

```yaml
kafka:
  info:
    bootstrap-servers: <url>
    consumer:
      group-id: <group>
      auto-offset-reset: earliest  # 원하는 설정
      auto-commit: false # 원하는 설정
  topics:
    transactions:
      pollInterval: 1000 # 원하는 설정
      concurrency: 3 # 원하는 설정
      maxPollRecords: 500 # 원하는 설정
      enabled: true # 사용 여부
      
    # topics에 Key가 추가됨에 따라서 코드상에서도 해당 키에 매칭되는 핸들러를 등록해줘야 합니다.
    
# 원하는 설정 -> 가독성을 위한 Kafka 로그 설정
logging:
  level:
    org.apache.kafka: WARN
    org.springframework.kafka: INFO
    org.apache.kafka.clients.NetworkClient: ERROR
    org.apache.kafka.common.network.Selector: ERROR
    org.apache.kafka.clients.consumer: WARN
    org.apache.kafka.clients.Metadata: WARN
    org.example.consumer.ConsumerFactory: INFO
```

## 토픽 핸들러 추가하기

새로운 토픽의 메시지를 처리하기 위해:

1. `application.yaml`의 `kafka.topics` 아래에 토픽 구성을 추가합니다.
2. 핸들러 구현체를 생성 및 맵핑합니다.:

```kotlin
@Bean
fun kafkaTopicMaps(): KafkaTopicMaps {
    val handlers = mutableMapOf<String, MessageHandler>()
    val factories = mutableMapOf<String, ConcurrentKafkaListenerContainerFactory<String, Any>>()

    topicConfig.topics.forEach { (topicName, properties) ->
        if (properties.enabled) {
            when(topicName) {
                "transactions" -> handlers[topicName] = bankTransactionalHandler
                // 더 많은 핸들러 매핑...
                else ->{
                    throw RuntimeException("Topic [$topicName] not supported yet")
                }
            }

            factories[topicName] = createKafkaListenerContainerFactory(properties)
        }
    }

    return KafkaTopicMaps(handlers, factories)
}
```

```kotlin
@Component
class BankTransactionalHandler(
    private val logger: Logger = LoggerFactory.getLogger(BankTransactionalHandler::class.java)
) : MessageHandler {
    override fun handle(record: ConsumerRecord<String, Any>, acknowledgment: Acknowledgment) {
        try {
            val message = record.value()
            logger.info("Received Order message: {}", message)

            // 메시지 처리 진행

//            acknowledgment.acknowledge() // 일련의 과정 진행 이후에 commit 여부 결정
            logger.info("Order message processed successfully and acknowledged")
        } catch (e: Exception) {
            logger.error("Error processing Order message", e)
        }
    }
}
```

## 동기식 vs 비동기식 처리

이 프레임워크는 두 가지 처리 방식을 지원하고 원하는 방식을 선택하여 활용 가능합니다.
토픽마다 핸들러를 내부적으로 따로 관리가 되어서 처리가 되기 떄문에

핸들러에 원하는 동기식 처리 또는 비동기식 처리
추가로 원하는 로깅을 편하게 구현 할 수 있습니다.

## DB 자율화

이 프레임워크는 데이터 저장을 위해 원하는 DB를 선택하여 활용 가능합니다.

## 스펙

```kotlin
plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"

    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("org.springframework.boot") version "3.2.3"
}
```

## 추가 고려 사항

```
Kafka 컨슈머 프레임워크를 위해 고려해볼 수 있는 추가 사항들입니다:

1. 오류 처리 및 데드 레터 큐

실패한 메시지 처리 방법과 데드 레터 큐로의 라우팅에 대한 섹션을 추가하는 것을 고려
 -> DLQ 또는 간단한 DB 처리를 통해 진행 가능

2. 컨슈머 그룹 관리

컨슈머 그룹이 어떻게 관리되고 재조정되는지에 대한 세부 정보를 추가
 -> 토픽마다 Consumer 그룹을 설정하는게 맞는지

3. 오프셋 관리

consumer는 내부적으로 fetch한 정보를 가지고 있다보니,
Seeking기능을 추가하는게 맞는지, 아니면 앞서 말한 DLQ를 활용하여 처리하는게 좋을지 고민 필요 
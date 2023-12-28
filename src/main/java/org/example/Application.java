package org.example;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.output.StreamCallback;
import io.siddhi.core.util.EventPrinter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

// ------------------------------------------------------------
// 설명
// ------------------------------------------------------------
// subscribe_kafka(topic:event) --> siddhi(event filter) --> publish_kafka(topic:feedback) --> commit_kafka(topic:event)

@SpringBootApplication
public class Application {

  private static String readSiddhiQueryFromFile(String filePath) throws IOException {
    try (InputStream inputStream = Application.class.getClassLoader().getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining("\n"));
    }
  }

  public static void main(String[] args) throws IOException {
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    // ------------------------------------------------------------
    // 카프카 준비
    // ------------------------------------------------------------
    // 입력 스트림 준비
    ConsumerFactory<String, String> consumerFactory = context.getBean(ConsumerFactory.class);
    Consumer<String, String> consumer = consumerFactory.createConsumer();
    consumer.subscribe(Collections.singletonList("event"));
    // 출력 스트림 준비
    KafkaTemplate<String, String> template = context.getBean(KafkaTemplate.class);

    // ------------------------------------------------------------
    // SiddhiApp 준비
    // ------------------------------------------------------------
    SiddhiManager siddhiManager = new SiddhiManager();
    // DSL 로드
    String siddhiApp = readSiddhiQueryFromFile("siddhi/stream_filter.siddhi");
    // DSL 런타임 생성
    SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

    // ------------------------------------------------------------
    // SiddhiApp 출력 스트림 연결 (카프카 "feedback" 토픽)
    // ------------------------------------------------------------
    siddhiAppRuntime.addCallback("OutputStream", new StreamCallback() {
      @Override
      public void receive(Event[] events) {
        EventPrinter.print(events);
        // 처리 결과를 kafka "feedback" topic 에 발행
        for (Event event : events) {
          AppFeedback appFeedback = AppFeedback.toFeedback(event.getData());
          // 피드백 한다
          if(!appFeedback.getIsFiltered()) {
            template.send("feedback", appFeedback.toString());
          }
          // 피드백 완료한 이벤트를 커밋 처리 한다.
          TopicPartition topicPartition = new TopicPartition(appFeedback.getTopic(), appFeedback.getPartition());
          OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(appFeedback.getOffset());
          consumer.commitSync(Collections.singletonMap(topicPartition, offsetAndMetadata));
        }
      }
    });

    // ------------------------------------------------------------
    // SiddhiApp 구동
    // ------------------------------------------------------------
    siddhiAppRuntime.start();

    // ------------------------------------------------------------
    // SiddhiApp 입력 스트림 연결 (카프카 "event" 토픽)
    // ------------------------------------------------------------
    InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
    while (true) {
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
      records.forEach(record -> {
        try {
          AppEvent appEvent = AppEvent.toEvent(
              record.topic(),
              record.partition(),
              record.offset(),
              record.value()
          );
          inputHandler.send(appEvent.toArray());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }
}
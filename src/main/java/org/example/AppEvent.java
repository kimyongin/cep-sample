package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppEvent {

  private String topic;
  private Integer partition;
  private Long offset;
  private String symbol;
  private Float price;
  private Long volume;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static AppEvent toEvent(String topic, Integer partition, Long offset, String jsonString) {
    try {
      AppEvent streamEvent = objectMapper.readValue(jsonString, AppEvent.class);
      streamEvent.setTopic(topic);
      streamEvent.setPartition(partition);
      streamEvent.setOffset(offset);
      return streamEvent;
    } catch (Exception e) {
      throw new RuntimeException("Error deserializing JSON", e);
    }
  }

  public Object[] toArray() {
    return new Object[]{topic, partition, offset, symbol, price, volume};
  }
}

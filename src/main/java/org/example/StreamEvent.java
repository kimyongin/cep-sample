package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StreamEvent {

  private String topic;
  private Integer partition;
  private Long offset;
  private String symbol;
  private Float price;
  private Long volume;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static StreamEvent toEvent(String topic, Integer partition, Long offset, String jsonString) {
    try {
      StreamEvent streamEvent = objectMapper.readValue(jsonString, StreamEvent.class);
      streamEvent.setTopic(topic);
      streamEvent.setPartition(partition);
      streamEvent.setOffset(offset);
      return streamEvent;
    } catch (Exception e) {
      throw new RuntimeException("Error deserializing JSON", e);
    }
  }

  public String toString() {
    try {
      return objectMapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serializing JSON", e);
    }
  }

  public static StreamEvent toEvent(Object[] array) {
    StreamEvent streamEvent = new StreamEvent();
    streamEvent.setTopic((String) array[0]);
    streamEvent.setPartition((Integer) array[1]);
    streamEvent.setOffset((Long) array[2]);
    streamEvent.setSymbol((String) array[3]);
    streamEvent.setPrice((Float) array[4]);
    streamEvent.setVolume((Long) array[5]);
    return streamEvent;
  }

  public Object[] toArray() {
    return new Object[]{topic, partition, offset, symbol, price, volume};
  }
}

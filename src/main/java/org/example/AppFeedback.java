package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.protocol.types.Field.Bool;

@Data
@NoArgsConstructor
public class AppFeedback {

  private String topic;
  private Integer partition;
  private Long offset;
  private String symbol;
  private Float price;
  private Long volume;
  private Boolean isFiltered;

  private static final ObjectMapper objectMapper = new ObjectMapper();


  public String toString() {
    try {
      return objectMapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serializing JSON", e);
    }
  }

  public static AppFeedback toFeedback(Object[] array) {
    AppFeedback appFeedback = new AppFeedback();
    appFeedback.setTopic((String) array[0]);
    appFeedback.setPartition((Integer) array[1]);
    appFeedback.setOffset((Long) array[2]);
    appFeedback.setSymbol((String) array[3]);
    appFeedback.setPrice((Float) array[4]);
    appFeedback.setVolume((Long) array[5]);
    appFeedback.setIsFiltered((Boolean) array[6]);
    return appFeedback;
  }
}

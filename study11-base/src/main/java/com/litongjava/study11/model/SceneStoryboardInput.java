package com.litongjava.study11.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SceneStoryboardInput {
  private Long videoId;
  private String topic;
  private String language;
  private int min, max;
}

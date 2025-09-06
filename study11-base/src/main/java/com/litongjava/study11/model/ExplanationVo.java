package com.litongjava.study11.model;

import java.util.List;

import com.litongjava.consts.ModelPlatformName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
//"voice_provider": "openai",
//"voice_id": "default_voice",
//"language": "english",
public class ExplanationVo {
  private String question;
  // 0 sence by sence 1 all sence at once
  private Long id;
  private int generate_type;
  private String voice_provider = "openai";
  private String voice_id = "default_voice";
  private String language = "english";
  private String provider = ModelPlatformName.ANTHROPIC;;
  private String model;
  private List<Long> imageIds;
  private String user_id;
  private String md5;

  public ExplanationVo(String text) {
    this.question = text;
  }

  public ExplanationVo(String user_id, String text) {
    this.user_id = user_id;
    this.question = text;
  }

  public ExplanationVo(String user_id, String topic, String language) {
    this.user_id = user_id;
    this.question = topic;
    this.language = language;
  }

  public String getPlatform() {
    return provider;
  }

}

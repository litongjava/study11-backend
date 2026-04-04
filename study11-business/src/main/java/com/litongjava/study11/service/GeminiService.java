package com.litongjava.study11.service;

import lombok.extern.slf4j.Slf4j;
import nexus.io.gemini.GeminiChatRequest;
import nexus.io.gemini.GeminiChatResponse;
import nexus.io.gemini.GeminiClient;
import nexus.io.gemini.GoogleModels;
import nexus.io.tio.utils.json.JsonUtils;

@Slf4j
public class GeminiService {

  public GeminiChatResponse generate(GeminiChatRequest geminiChatRequestVo) {
    GeminiChatResponse chatResponse = null;
    try {
      //GEMINI_2_5_PRO_PREVIEW_03_25
      //GEMINI_2_5_PRO_EXP_03_25 //免费
      chatResponse = GeminiClient.generate(GoogleModels.GEMINI_2_5_FLASH, geminiChatRequestVo);
    } catch (Exception e) {
      log.error("Faile to generate code:{}", JsonUtils.toJson(geminiChatRequestVo), e);
      return null;
    }
    return chatResponse;
  }
}

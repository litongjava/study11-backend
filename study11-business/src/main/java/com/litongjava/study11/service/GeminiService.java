package com.litongjava.study11.service;

import com.litongjava.gemini.GeminiChatRequestVo;
import com.litongjava.gemini.GeminiChatResponseVo;
import com.litongjava.gemini.GeminiClient;
import com.litongjava.gemini.GoogleModels;
import com.litongjava.tio.utils.json.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeminiService {

  public GeminiChatResponseVo generate(GeminiChatRequestVo geminiChatRequestVo) {
    GeminiChatResponseVo chatResponse = null;
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

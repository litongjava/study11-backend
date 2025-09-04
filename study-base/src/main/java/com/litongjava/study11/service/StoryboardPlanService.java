package com.litongjava.study11.service;

import java.util.ArrayList;
import java.util.List;

import com.litongjava.chat.UniChatClient;
import com.litongjava.chat.UniChatMessage;
import com.litongjava.chat.UniChatRequest;
import com.litongjava.chat.UniChatResponse;
import com.litongjava.consts.ModelPlatformName;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.openrouter.OpenRouterModels;
import com.litongjava.template.PromptEngine;
import com.litongjava.tio.utils.crypto.Md5Utils;
import com.litongjava.utils.CodeBlockUtils;

public class StoryboardPlanService {

  private SenceStoryboardService senceStoryboardService = Aop.get(SenceStoryboardService.class);

  public String plan(Long videoId, String topic, String language) {
    String md5 = Md5Utils.md5Hex(topic);
    String answer = senceStoryboardService.queryStoryboard(md5, language);
    if (answer == null) {
      String prompt = PromptEngine.renderToString("sence_storyboard_prompt.txt");
      String userPrompt = "topic:" + topic + ". Please respond in " + language;

      List<UniChatMessage> messages = new ArrayList<>();
      messages.add(UniChatMessage.buildUser(userPrompt));

      UniChatRequest request = new UniChatRequest();
      request.setPlatform(ModelPlatformName.OPENROUTER);
      request.setModel(OpenRouterModels.QWEN_QWEN3_CODER);

      request.setTemperature(0f);
      request.setSystemPrompt(prompt);
      request.setMessages(messages);
      request.setCacheSystemPrompt(true);
      request.setMax_tokens(32000);
      request.setResponseMimeType("json");

      UniChatResponse resposne = UniChatClient.generate(request);
      answer = resposne.getMessage().getContent();
      senceStoryboardService.saveStoryboard(videoId, md5, topic, language, answer, null);
    }

    String parsedXml = CodeBlockUtils.parseXml(answer);
    if (parsedXml == null) {
      int start = answer.indexOf("<SCENE_OUTLINE>");
      int end = answer.indexOf("</SCENE_OUTLINE>");
      parsedXml = answer.substring(start + 15, end);
    }
    try {
      parsedXml = parsedXml.replace("<SCENE_OUTLINE>", "").replace("</SCENE_OUTLINE>", "");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return parsedXml;
  }
}

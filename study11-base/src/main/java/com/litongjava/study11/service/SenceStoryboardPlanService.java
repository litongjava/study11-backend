package com.litongjava.study11.service;

import java.util.ArrayList;
import java.util.List;

import com.litongjava.chat.UniChatClient;
import com.litongjava.chat.UniChatMessage;
import com.litongjava.chat.UniChatRequest;
import com.litongjava.chat.UniChatResponse;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.openai.chat.ChatResponseFormatType;
import com.litongjava.template.PromptEngine;
import com.litongjava.tio.utils.crypto.Md5Utils;
import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.utils.CodeBlockUtils;

import dev.langchain4j.model.chat.request.ResponseFormatType;

public class SenceStoryboardPlanService {

  private SenceStoryboardService senceStoryboardService = Aop.get(SenceStoryboardService.class);

  public String plan(Long videoId, String topic, String language, String platform, String model) {
    String md5 = Md5Utils.md5Hex(topic);
    String parsedJson = senceStoryboardService.queryStoryboard(md5, language);

    String prompt = PromptEngine.renderToString("sence_storyboard_prompt_json_format.txt");
    String userPrompt = "topic:" + topic + ". Please respond in " + language;

    List<UniChatMessage> messages = new ArrayList<>();
    messages.add(UniChatMessage.buildUser(userPrompt));

    UniChatRequest request = new UniChatRequest();
    request.setPlatform(platform);
    request.setModel(model);

    request.setTemperature(0f);
    request.setSystemPrompt(prompt);
    request.setMessages(messages);
    request.setCacheSystemPrompt(true);
    request.setMax_tokens(32000);
    request.setResponseFormat(ChatResponseFormatType.json_object);

    while (parsedJson == null) {
      UniChatResponse resposne = UniChatClient.generate(request);
      parsedJson = resposne.getMessage().getContent();

      parsedJson = CodeBlockUtils.parseJson(parsedJson);
      if (parsedJson != null) {
        try {
          FastJson2Utils.parseObject(parsedJson);
        } catch (Exception e) {
          messages.add(UniChatMessage.buildUser("Failed to parse Json:" + e.getMessage()));
          parsedJson = null;
        }

      }
    }

    senceStoryboardService.saveStoryboard(videoId, md5, topic, language, parsedJson, null);

    return parsedJson;
  }
}

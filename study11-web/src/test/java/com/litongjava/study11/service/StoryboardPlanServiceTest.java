package com.litongjava.study11.service;

import org.junit.Test;

import com.litongjava.chat.UniChatRequest;
import com.litongjava.consts.ModelPlatformName;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.openai.ChatProvider;
import com.litongjava.openrouter.OpenRouterModels;
import com.litongjava.study11.config.AdminAppConfig;
import com.litongjava.study11.model.SceneStoryboardInput;
import com.litongjava.tio.boot.testing.TioBootTest;

public class StoryboardPlanServiceTest {

  @Test
  public void test() {
    TioBootTest.runWith(AdminAppConfig.class);

    SceneStoryboardInput input = new SceneStoryboardInput(1L, "三角函数的反函数", "Chinese", 10, 15);
    UniChatRequest uniChatRequest = new UniChatRequest();
    uniChatRequest.setPlatform(ModelPlatformName.OPENROUTER).setModel(OpenRouterModels.QWEN_QWEN3_CODER)
        .setProvider(ChatProvider.cerebras());
    String html = Aop.get(SceneStoryboardPlanService.class).planJson(input, uniChatRequest);
    System.out.println(html);
  }

}

package com.litongjava.study11.service;

import org.junit.Test;

import com.litongjava.consts.ModelPlatformName;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.openrouter.OpenRouterModels;
import com.litongjava.study11.config.AdminAppConfig;
import com.litongjava.study11.model.SceneStoryboardInput;
import com.litongjava.tio.boot.testing.TioBootTest;

public class StoryboardPlanServiceTest {

  @Test
  public void test() {
    TioBootTest.runWith(AdminAppConfig.class);

    SceneStoryboardInput input = new SceneStoryboardInput(1L, "三角函数", "Chinese", 10, 15);
    String html = Aop.get(SceneStoryboardPlanService.class).planJson(input, ModelPlatformName.OPENROUTER,
        OpenRouterModels.QWEN_QWEN3_CODER);
    System.out.println(html);
  }

}

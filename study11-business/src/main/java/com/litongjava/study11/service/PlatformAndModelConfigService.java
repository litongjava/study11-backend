package com.litongjava.study11.service;

import com.litongjava.bailian.BaiLianAiModels;
import com.litongjava.chat.UniChatRequest;
import com.litongjava.consts.ModelPlatformName;
import com.litongjava.openai.ChatProvider;
import com.litongjava.openrouter.OpenRouterModels;
import com.litongjava.study11.consts.Study11DomainConst;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlatformAndModelConfigService {

  public void configPlatformAndModel(UniChatRequest uniChatRequest) {
    String domain = uniChatRequest.getDomain();
    log.info("domain:{}", domain);
    if (Study11DomainConst.PREVIEW_STUDY11_AI.equals(domain)) {
      uniChatRequest.setPlatform(ModelPlatformName.OPENROUTER).setModel(OpenRouterModels.QWEN_QWEN3_CODER)
          //
          .setProvider(ChatProvider.cerebras());
    } else {
      uniChatRequest.setPlatform(ModelPlatformName.BAILIAN).setModel(BaiLianAiModels.QWEN3_CODER_PLUS);
    }
  }
}

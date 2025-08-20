package com.litongjava.study11.service;

import com.litongjava.bailian.BaiLianAiModels;
import com.litongjava.chat.UniChatRequest;
import com.litongjava.consts.ModelPlatformName;

public class PlatformAndModelSetService {

  public void configPlatformAndModel(UniChatRequest uniChatRequest) {
    uniChatRequest.setPlatform(ModelPlatformName.BAILIAN).setModel(BaiLianAiModels.QWEN3_CODER_PLUS);
  }
}

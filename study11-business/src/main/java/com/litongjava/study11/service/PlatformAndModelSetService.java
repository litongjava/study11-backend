package com.litongjava.study11.service;

import com.litongjava.cerebras.CerebrasModels;
import com.litongjava.chat.UniChatRequest;
import com.litongjava.consts.ModelPlatformName;

public class PlatformAndModelSetService {

  public void configPlatformAndModel(UniChatRequest uniChatRequest) {
//    uniChatRequest.setPlatform(ModelPlatformName.BAILIAN).setModel(BaiLianAiModels.QWEN3_CODER_PLUS);
    uniChatRequest.setPlatform(ModelPlatformName.CEREBRAS).setModel(CerebrasModels.QWEN_3_CODER_480B);
  }
}

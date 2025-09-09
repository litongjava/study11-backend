package com.litongjava.study11.service;

import com.litongjava.bailian.BaiLianAiModels;
import com.litongjava.cerebras.CerebrasModels;
import com.litongjava.chat.UniChatRequest;
import com.litongjava.consts.ModelPlatformName;
import com.litongjava.study11.consts.Study11DomainConst;

public class PlatformAndModelSetService {

  public void configPlatformAndModel(UniChatRequest uniChatRequest) {
    if (Study11DomainConst.PREVIEW_STUDY11_AI.equals(uniChatRequest.getDomain())) {
      uniChatRequest.setPlatform(ModelPlatformName.CEREBRAS).setModel(CerebrasModels.QWEN_3_CODER_480B);
    }
    uniChatRequest.setPlatform(ModelPlatformName.BAILIAN).setModel(BaiLianAiModels.QWEN3_CODER_PLUS);
  }
}

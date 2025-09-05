package com.litongjava.study11.controller;

import com.litongjava.annotation.RequestPath;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.study11.service.K12QuestionTestService;
import com.litongjava.tio.utils.thread.TioThreadUtils;

@RequestPath("/k12")
public class K12Controller {

  public RespBodyVo batch() {
    TioThreadUtils.execute(() -> {
      try {
        Aop.get(K12QuestionTestService.class).batchTest();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    return RespBodyVo.ok();
  }
}

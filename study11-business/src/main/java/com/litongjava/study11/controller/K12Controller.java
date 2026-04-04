package com.litongjava.study11.controller;

import com.litongjava.study11.service.K12QuestionTestService;

import nexus.io.annotation.RequestPath;
import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.tio.utils.thread.TioThreadUtils;

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

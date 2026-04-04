package com.litongjava.study11.controller;

import com.jfinal.kit.Kv;
import com.litongjava.study11.model.ExplanationVo;
import com.litongjava.study11.service.HtmlAnimationService;
import com.litongjava.study11.service.QuestionBatchTestService;

import nexus.io.annotation.EnableCORS;
import nexus.io.annotation.RequestPath;
import nexus.io.consts.ModelPlatformName;
import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.utils.thread.TioThreadUtils;

@EnableCORS
@RequestPath("/api/v1/html")
public class HtmlController {

  HtmlAnimationService htmlService = Aop.get(HtmlAnimationService.class);

  @RequestPath("/generate")
  public RespBodyVo generate(String topic, String language, HttpRequest request) {
    if (language == null) {
      language = "Chinese";
    }
    String host = request.getHost();
    ExplanationVo explanationVo = new ExplanationVo("1", topic, language);
    explanationVo.setProvider(ModelPlatformName.BAILIAN);
    explanationVo.setDomain(host);

    Long id = htmlService.generate(explanationVo);

    String url = "//" + host + "/preview/" + id;
    Kv by = Kv.by("url", url);
    return RespBodyVo.ok(by);
  }

  public RespBodyVo batch() {
    TioThreadUtils.execute(() -> {
      try {
        Aop.get(QuestionBatchTestService.class).batchTest();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    return RespBodyVo.ok();
  }

  public RespBodyVo detail(Long id, HttpRequest request) {
    String host = request.getHost();
    Kv result = htmlService.detail(id, host);
    return RespBodyVo.ok(result);
  }

  public RespBodyVo recommends(Integer pageNo, int pageSize, String sort_by, HttpRequest request) {
    String host = request.getHost();
    Kv result = htmlService.recommends(pageNo, pageSize, sort_by, host);
    return RespBodyVo.ok(result);
  }

}

package com.litongjava.study11.controller;

import com.jfinal.kit.Kv;
import com.litongjava.annotation.EnableCORS;
import com.litongjava.annotation.RequestPath;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.study11.service.HtmlAnimationService;
import com.litongjava.study11.service.QuestionBatchTestService;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.utils.thread.TioThreadUtils;

@EnableCORS
@RequestPath("/api/v1/html")
public class HtmlController {

  HtmlAnimationService htmlService = Aop.get(HtmlAnimationService.class);

  @RequestPath("/generate")
  public RespBodyVo generate(String topic, String language, HttpRequest request) {
    if (language == null) {
      language = "Chinese";
    }
    Long id = htmlService.generate(topic, language);
    String host = request.getHost();
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

package com.litongjava.study11.controller;

import com.jfinal.kit.Kv;
import com.litongjava.annotation.EnableCORS;
import com.litongjava.annotation.RequestPath;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.study11.service.HtmlService;
import com.litongjava.tio.http.common.HttpRequest;

@EnableCORS
@RequestPath("/v1/api/html")
public class HtmlController {

  HtmlService htmlService = Aop.get(HtmlService.class);

  @RequestPath("/generate")
  public RespBodyVo generate(String topic, HttpRequest request) {
    Long id = htmlService.generate(topic);
    String host = request.getHost();
    String url = "//" + host + "/preview/" + id;
    Kv by = Kv.by("url", url);
    return RespBodyVo.ok(by);
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

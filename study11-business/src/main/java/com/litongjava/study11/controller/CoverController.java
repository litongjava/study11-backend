package com.litongjava.study11.controller;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.service.HtmlAnimationService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.util.Resps;

import nexus.io.annotation.RequestPath;
import nexus.io.model.body.RespBodyVo;

@RequestPath("/cover")
public class CoverController {

  HtmlAnimationService htmlService = Aop.get(HtmlAnimationService.class);

  @RequestPath("/{id}")
  public HttpResponse preview(Long id) {
    HttpResponse response = TioRequestContext.getResponse();
    String svg = htmlService.getSvgById(id);
    return Resps.svg(response, svg);
  }

  public RespBodyVo parse() {
    return htmlService.parseSvg();
  }
}

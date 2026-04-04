package com.litongjava.study11.controller;

import com.litongjava.study11.service.HtmlAnimationService;

import nexus.io.annotation.RequestPath;
import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.util.Resps;

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

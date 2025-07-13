package com.litongjava.study11.controller;

import com.litongjava.annotation.RequestPath;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.service.HtmlService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.util.Resps;

@RequestPath("/preview")
public class PreviewController {

  HtmlService htmlService = Aop.get(HtmlService.class);

  @RequestPath("/{id}")
  public HttpResponse preview(Long id) {
    HttpResponse response = TioRequestContext.getResponse();
    String html = htmlService.getCodeById(id);
    return Resps.html(response, html);
  }
}

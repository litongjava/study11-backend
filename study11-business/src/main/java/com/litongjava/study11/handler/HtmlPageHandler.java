package com.litongjava.study11.handler;

import com.litongjava.study11.service.IndexService;

import nexus.io.jfinal.aop.Aop;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.util.Resps;

public class HtmlPageHandler {

  public HttpResponse index(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    IndexService indexService = Aop.get(IndexService.class);
    String html = indexService.index();
    Resps.html(response, html);
    return response;
  }
}

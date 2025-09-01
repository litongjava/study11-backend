package com.litongjava.study11.handler;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.service.IndexService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.util.Resps;

public class HtmlPageHandler {

  public HttpResponse index(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    IndexService indexService = Aop.get(IndexService.class);
    String html = indexService.index();
    Resps.html(response, html);
    return response;
  }
}

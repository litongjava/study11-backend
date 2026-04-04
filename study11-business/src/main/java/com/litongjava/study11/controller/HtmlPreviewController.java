package com.litongjava.study11.controller;

import java.net.URL;

import com.litongjava.study11.service.HtmlAnimationService;

import nexus.io.annotation.RequestPath;
import nexus.io.jfinal.aop.Aop;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.common.MimeType;
import nexus.io.tio.http.server.util.Resps;
import nexus.io.tio.utils.hutool.FileUtil;
import nexus.io.tio.utils.hutool.ResourceUtil;

@RequestPath("/preview")
public class HtmlPreviewController {

  HtmlAnimationService htmlService = Aop.get(HtmlAnimationService.class);

  @RequestPath("/{id}")
  public HttpResponse preview(Long id) {
    HttpResponse response = TioRequestContext.getResponse();
    String html = htmlService.getHtmlCodeById(id);
    return Resps.html(response, html);
  }

  @RequestPath("/animation-player.css")
  public HttpResponse css() {
    HttpResponse response = TioRequestContext.getResponse();
    String charset = response.getCharset();
    URL resource = ResourceUtil.getResource("prompts/animation-player.css");
    if (resource != null) {
      String cssContent = FileUtil.readString(resource);
      String contentType = Resps.getMimeTypeStr(MimeType.TEXT_CSS_CSS, charset);
      return Resps.string(response, cssContent, charset, contentType);
    }
    response.setStatus(404);
    return response;
  }

  @RequestPath("/animation-player-utils.js")
  public HttpResponse js() {
    HttpResponse response = TioRequestContext.getResponse();
    String charset = response.getCharset();
    URL resource = ResourceUtil.getResource("prompts/animation-player-utils.js");
    if (resource != null) {
      String cssContent = FileUtil.readString(resource);
      String contentType = Resps.getMimeTypeStr(MimeType.TEXT_JAVASCRIPT_JS, charset);
      return Resps.string(response, cssContent, charset, contentType);
    }
    response.setStatus(404);
    return response;
  }

  @RequestPath("/player-control.js")
  public HttpResponse playerControlJS() {
    HttpResponse response = TioRequestContext.getResponse();
    String charset = response.getCharset();
    URL resource = ResourceUtil.getResource("prompts/player-control.js");
    if (resource != null) {
      String cssContent = FileUtil.readString(resource);
      String contentType = Resps.getMimeTypeStr(MimeType.TEXT_JAVASCRIPT_JS, charset);
      return Resps.string(response, cssContent, charset, contentType);
    }
    response.setStatus(404);
    return response;
  }

  @RequestPath("/template.html")
  public HttpResponse templateHtml() {
    HttpResponse response = TioRequestContext.getResponse();
    String charset = response.getCharset();
    URL resource = ResourceUtil.getResource("prompts/template.html");
    if (resource != null) {
      String cssContent = FileUtil.readString(resource);
      String contentType = Resps.getMimeTypeStr(MimeType.TEXT_HTML_HTML, charset);
      return Resps.string(response, cssContent, charset, contentType);
    }
    response.setStatus(404);
    return response;
  }

}

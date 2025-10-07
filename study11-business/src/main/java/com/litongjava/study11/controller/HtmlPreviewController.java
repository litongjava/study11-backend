package com.litongjava.study11.controller;

import java.net.URL;

import com.litongjava.annotation.RequestPath;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.service.HtmlAnimationService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.MimeType;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.hutool.FileUtil;
import com.litongjava.tio.utils.hutool.ResourceUtil;

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

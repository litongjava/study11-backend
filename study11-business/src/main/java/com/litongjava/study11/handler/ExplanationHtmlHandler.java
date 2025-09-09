package com.litongjava.study11.handler;

import com.jfinal.kit.Kv;
import com.litongjava.consts.ModelPlatformName;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.model.ExplanationVo;
import com.litongjava.study11.service.HtmlAnimationService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.core.Tio;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.sse.SsePacket;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.http.server.util.SseEmitter;
import com.litongjava.tio.utils.json.JsonUtils;
import com.litongjava.tio.utils.thread.TioThreadUtils;

public class ExplanationHtmlHandler {

  HtmlAnimationService htmlService = Aop.get(HtmlAnimationService.class);

  public HttpResponse index(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    CORSUtils.enableCORS(response);
    response.addServerSentEventsHeader();
    Tio.bSend(request.channelContext, response);

    String host = request.getHost();
    Kv kv = Kv.by("msg", "ok");
    Tio.bSend(request.channelContext, new SsePacket("200", JsonUtils.toJson(kv)));
    response.setSend(false);
    request.channelContext.setAttribute("type", "SSE");
    String bodyString = request.getBodyString();
    ExplanationVo explanationVo = JsonUtils.parse(bodyString, ExplanationVo.class);
    explanationVo.setProvider(ModelPlatformName.BAILIAN);
    explanationVo.setDomain(host);

    TioThreadUtils.execute(() -> {
      try {
        Long id = htmlService.generate(explanationVo);
        String url = htmlService.appendPreviewUrl(host, id);

        Kv done = Kv.by("url", url);
        Tio.send(request.channelContext, new SsePacket("main", JsonUtils.toJson(done)));
      } catch (Exception e) {
        if (!request.channelContext.isClosed) {
          Kv done = Kv.by("type", "error");
          Tio.send(request.channelContext, new SsePacket("close", JsonUtils.toJson(done)));
        }
        e.printStackTrace();
        reportError(explanationVo.getUser_id(), request, e);
      } finally {
        SseEmitter.closeSeeConnection(request.channelContext);
      }
    });
    return response;
  }

  @SuppressWarnings("unused")
  private void reportError(String userId, HttpRequest request, Exception e) {
    String appGroupName = "tio-boot";
    String warningName = "ExplanationHandler";
    String content = "Failed to generate video ";
    String level = "LeveL 1";
    // AlarmUtils.send(appGroupName, warningName, level, content, userId, request,
    // e);
  }
}

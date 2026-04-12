package com.litongjava.study11.config;

import com.litongjava.study11.handler.ExplanationHtmlHandler;
import com.litongjava.study11.handler.HtmlPageHandler;

import nexus.io.annotation.AConfiguration;
import nexus.io.annotation.Initialization;
import nexus.io.tio.boot.admin.config.TioAdminDbConfiguration;
import nexus.io.tio.boot.admin.config.TioAdminEnjoyEngineConfig;
import nexus.io.tio.boot.server.TioBootServer;
import nexus.io.tio.http.server.router.HttpRequestRouter;

@AConfiguration
public class AdminAppConfig {

  @Initialization
  public void config() {
    // 配置数据库相关
    new TioAdminDbConfiguration().config();
    new Study11ControllerConfiguration().config();
    new TioAdminEnjoyEngineConfig().config();
    HttpRequestRouter r = TioBootServer.me().getRequestRouter();
    if (r != null) {
      HtmlPageHandler indexHandler = new HtmlPageHandler();
      r.add("/", indexHandler::index);
      ExplanationHtmlHandler explanationHtmlHandler = new ExplanationHtmlHandler();
      r.add("/api/explanation/html", explanationHtmlHandler::index);
    }

  }
}

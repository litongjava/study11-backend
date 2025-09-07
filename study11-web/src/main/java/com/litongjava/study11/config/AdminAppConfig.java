package com.litongjava.study11.config;

import com.litongjava.annotation.AConfiguration;
import com.litongjava.annotation.Initialization;
import com.litongjava.study11.handler.ExplanationHtmlHandler;
import com.litongjava.study11.handler.HtmlPageHandler;
import com.litongjava.tio.boot.admin.config.TioAdminDbConfiguration;
import com.litongjava.tio.boot.admin.config.TioAdminEnjoyEngineConfig;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.http.server.router.HttpRequestRouter;

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

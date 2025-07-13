package com.litongjava.study11.config;

import java.util.ArrayList;
import java.util.List;

import com.litongjava.study11.controller.HtmlController;
import com.litongjava.study11.controller.PreviewController;
import com.litongjava.tio.boot.http.handler.controller.TioBootHttpControllerRouter;
import com.litongjava.tio.boot.server.TioBootServer;

public class Study11ControllerConfiguration {

  public void config() {
    TioBootHttpControllerRouter controllerRouter = TioBootServer.me().getControllerRouter();
    if (controllerRouter == null) {
      return;
    }
    List<Class<?>> scannedClasses = new ArrayList<>();
    scannedClasses.add(HtmlController.class);
    scannedClasses.add(PreviewController.class);
    controllerRouter.addControllers(scannedClasses);
  }
}

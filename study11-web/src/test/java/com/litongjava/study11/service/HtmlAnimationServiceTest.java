package com.litongjava.study11.service;

import org.junit.Test;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.config.AdminAppConfig;
import com.litongjava.tio.boot.testing.TioBootTest;

public class HtmlAnimationServiceTest {

  @Test
  public void test() {
    TioBootTest.runWith(AdminAppConfig.class);
    // String topic = "If two vectors have the same magnitude, do their components
    // have to be the same?";
    //String topic = "为什么天空是蓝色?";
    String topic = "什么是加密货币?";
    Long id = Aop.get(HtmlAnimationService.class).selectIdByTopic(topic);
    if(id==null) {
      System.out.println("is is null");
    }
  }
}

package com.litongjava.study11.service;

import org.junit.Test;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.config.AdminAppConfig;
import com.litongjava.tio.boot.testing.TioBootTest;

public class HtmlAnimationQuesitonTest {

  @Test
  public void test() {
    TioBootTest.runWith(AdminAppConfig.class);
    //String topic = "If two vectors have the same magnitude, do their components have to be the same?";
    String topic = "为什么天空是蓝色?";
    Long id = Aop.get(HtmlAnimationService.class).generate(topic,"Chinese");
    System.out.println(id);
  }
}
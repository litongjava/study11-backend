package com.litongjava.study11.service;

import org.junit.Test;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.config.AdminAppConfig;
import com.litongjava.tio.boot.testing.TioBootTest;

public class HtmlAnimationQuesitonTest {

  @Test
  public void test() {
    TioBootTest.runWith(AdminAppConfig.class);
    Long id = Aop.get(HtmlService.class).generate("在python中,什么是manim");
    System.out.println(id);
  }
}
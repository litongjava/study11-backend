package com.litongjava.study11.service;

import org.junit.Test;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.config.AdminAppConfig;
import com.litongjava.tio.boot.testing.TioBootTest;

public class HtmlServiceTest {

  @Test
  public void test() {
    TioBootTest.runWith(AdminAppConfig.class);
    Long id = Aop.get(HtmlService.class).generate("动量守恒定律");
    System.out.println(id);
  }

}
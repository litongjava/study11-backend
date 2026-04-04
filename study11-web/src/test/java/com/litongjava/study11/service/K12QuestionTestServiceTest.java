package com.litongjava.study11.service;

import org.junit.Test;

import com.litongjava.study11.config.AdminAppConfig;

import nexus.io.jfinal.aop.Aop;
import nexus.io.tio.boot.testing.TioBootTest;

public class K12QuestionTestServiceTest {

  @Test
  public void testInitData() {
    TioBootTest.runWith(AdminAppConfig.class);
    Aop.get(K12QuestionTestService.class).initData();
  }
  
}

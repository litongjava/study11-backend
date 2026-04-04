package com.litongjava.study11.service;

import org.junit.Test;

import com.litongjava.study11.config.AdminAppConfig;

import nexus.io.jfinal.aop.Aop;
import nexus.io.tio.boot.testing.TioBootTest;

public class QuestionBatchTestServiceTest {

  @Test
  public void initData() {
    TioBootTest.runWith(AdminAppConfig.class);
    Aop.get(QuestionBatchTestService.class).initData();
  }

  @Test
  public void test() {
    TioBootTest.runWith(AdminAppConfig.class);
    Aop.get(QuestionBatchTestService.class).batchTest();
  }
}

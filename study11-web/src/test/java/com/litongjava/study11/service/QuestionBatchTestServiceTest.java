package com.litongjava.study11.service;

import org.junit.Test;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.config.AdminAppConfig;
import com.litongjava.tio.boot.testing.TioBootTest;

public class QuestionBatchTestServiceTest {

  @Test
  public void test() {
    TioBootTest.runWith(AdminAppConfig.class);
    Aop.get(QuestionBatchTestService.class).batchTest();
  }
}

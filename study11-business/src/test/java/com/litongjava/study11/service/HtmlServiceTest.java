package com.litongjava.study11.service;

import org.junit.Test;

import nexus.io.jfinal.aop.Aop;

public class HtmlServiceTest {

  @Test
  public void test() {
    String systemPrompt = Aop.get(HtmlAnimationService.class).getSystemPrompt();
    System.out.println(systemPrompt);
  }
}
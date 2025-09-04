package com.litongjava.study11.utils;

import java.net.URL;

import org.junit.Test;

import com.litongjava.tio.utils.hutool.FileUtil;
import com.litongjava.tio.utils.hutool.ResourceUtil;
import com.litongjava.utils.CodeBlockUtils;

public class HtmlCodeUtilsTest {

  @Test
  public void test() {
    URL url = ResourceUtil.getResource("prompts/01.html");
    String html = FileUtil.readString(url);
    String parseHtml = CodeBlockUtils.parseHtml(html);
    System.out.println(parseHtml);
  }

}

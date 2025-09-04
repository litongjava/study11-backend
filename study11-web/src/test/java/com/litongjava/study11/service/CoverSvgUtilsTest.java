package com.litongjava.study11.service;

import java.io.File;

import org.junit.Test;

import com.litongjava.study11.utils.CoverSvgUtils;
import com.litongjava.tio.utils.hutool.FileUtil;

public class CoverSvgUtilsTest {

  @Test
  public void test() {
    String htmlContent = FileUtil.readString(new File("html", "553442173855342592.html"));
    // 基本用法
    String svg = CoverSvgUtils.parseFirstSvg(htmlContent);
    System.out.println(svg);

    // 获取详细信息
    // CoverSvgUtils.SvgInfo info = CoverSvgUtils.getSvgInfo(htmlContent);
  }
}

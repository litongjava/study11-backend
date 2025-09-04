package com.litongjava.study11.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.litongjava.study11.model.SvgInfo;

public class CoverSvgUtils {

  /**
   * 从HTML中提取第一个SVG元素作为封面
   * 
   * @param html HTML内容
   * @return 提取的SVG字符串，如果没有找到则返回null
   */
  public static String parseFirstSvg(String html) {
    if (html == null || html.trim().isEmpty()) {
      return null;
    }

    // 使用正则表达式匹配第一个完整的SVG元素
    // 支持嵌套的SVG标签和自闭合标签
    Pattern svgPattern = Pattern.compile("<svg[^>]*>.*?</svg>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    Matcher matcher = svgPattern.matcher(html);

    if (matcher.find()) {
      String svgContent = matcher.group();

      // 清理和优化SVG内容
      svgContent = cleanSvg(svgContent);

      return svgContent;
    }

    return null;
  }

  /**
   * 清理SVG内容，移除不必要的属性和优化
   * 
   * @param svgContent 原始SVG内容
   * @return 清理后的SVG内容
   */
  private static String cleanSvg(String svgContent) {
    if (svgContent == null) {
      return null;
    }

    // 移除JavaScript相关的事件属性（安全考虑）
    svgContent = svgContent.replaceAll("\\s+on\\w+\\s*=\\s*[\"'][^\"']*[\"']", "");

    // 移除script标签（如果有的话）
    svgContent = svgContent.replaceAll("<script[^>]*>.*?</script>", "");

    // 确保SVG有合适的xmlns属性
    if (!svgContent.contains("xmlns=")) {
      svgContent = svgContent.replaceFirst("<svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"");
    }

    // 移除多余的空白字符
    svgContent = svgContent.replaceAll("\\s+", " ").trim();

    return svgContent;
  }

  /**
   * 提取SVG并添加封面样式优化
   * 
   * @param html   HTML内容
   * @param width  目标宽度（可选）
   * @param height 目标高度（可选）
   * @return 优化后的SVG字符串
   */
  public static String parseFirstSvgWithSize(String html, Integer width, Integer height) {
    String svg = parseFirstSvg(html);

    if (svg == null) {
      return null;
    }

    // 如果指定了尺寸，则更新viewBox和尺寸属性
    if (width != null && height != null) {
      // 更新width和height属性
      svg = svg.replaceAll("\\s+width\\s*=\\s*[\"'][^\"']*[\"']", " width=\"" + width + "\"");
      svg = svg.replaceAll("\\s+height\\s*=\\s*[\"'][^\"']*[\"']", " height=\"" + height + "\"");

      // 如果没有width和height属性，则添加
      if (!svg.contains("width=")) {
        svg = svg.replaceFirst("<svg", "<svg width=\"" + width + "\"");
      }
      if (!svg.contains("height=")) {
        svg = svg.replaceFirst("<svg", "<svg height=\"" + height + "\"");
      }
    }

    return svg;
  }

  /**
   * 获取SVG的基本信息
   * 
   * @param html HTML内容
   * @return SVG信息对象
   */
  public static SvgInfo getSvgInfo(String html) {
    String svg = parseFirstSvg(html);
    if (svg == null) {
      return null;
    }

    SvgInfo info = new SvgInfo();

    // 提取viewBox信息
    Pattern viewBoxPattern = Pattern.compile("viewBox\\s*=\\s*[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);
    Matcher viewBoxMatcher = viewBoxPattern.matcher(svg);
    if (viewBoxMatcher.find()) {
      info.setViewBox(viewBoxMatcher.group(1));
    }

    // 提取width信息
    Pattern widthPattern = Pattern.compile("width\\s*=\\s*[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);
    Matcher widthMatcher = widthPattern.matcher(svg);
    if (widthMatcher.find()) {
      info.setWidth(widthMatcher.group(1));
    }

    // 提取height信息
    Pattern heightPattern = Pattern.compile("height\\s*=\\s*[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);
    Matcher heightMatcher = heightPattern.matcher(svg);
    if (heightMatcher.find()) {
      info.setHeight(heightMatcher.group(1));
    }

    info.setSvgContent(svg);
    return info;
  }

}
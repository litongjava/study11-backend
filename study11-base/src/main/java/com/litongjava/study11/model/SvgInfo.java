package com.litongjava.study11.model;

/**
 * SVG信息类
 */
public class SvgInfo {
  private String svgContent;
  private String viewBox;
  private String width;
  private String height;

  // Getters and Setters
  public String getSvgContent() {
    return svgContent;
  }

  public void setSvgContent(String svgContent) {
    this.svgContent = svgContent;
  }

  public String getViewBox() {
    return viewBox;
  }

  public void setViewBox(String viewBox) {
    this.viewBox = viewBox;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  @Override
  public String toString() {
    return "SvgInfo{" + "viewBox='" + viewBox + '\'' + ", width='" + width + '\'' + ", height='" + height + '\'' + '}';
  }
}
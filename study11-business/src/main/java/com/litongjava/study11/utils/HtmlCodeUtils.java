package com.litongjava.study11.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HtmlCodeUtils {

  /**
   * Extracts HTML content from the generated text. If the text contains ```html
   * fences, returns the content between them. Otherwise, if it starts with
   * '<html', returns trimmed text.
   * 
   * @param generatedText the raw text containing HTML
   * @return the extracted HTML or null if none found
   */
  public static String parseHtml(String generatedText) {
    String code;
    int indexOf = generatedText.indexOf("```html");
    if (indexOf == -1) {
      generatedText = generatedText.trim();
      if (generatedText.toLowerCase().startsWith("<html")) {
        return generatedText;
      } else if (generatedText.toLowerCase().startsWith("<!DOCTYPE html")) {
        return generatedText;
      } else {
        log.error("No HTML data found in the output:{}", generatedText);
        return null;
      }
    } else {
      int lastIndexOf = generatedText.lastIndexOf("```");
      log.info("HTML fence index:{},{}", indexOf, lastIndexOf);
      if (lastIndexOf > indexOf + 8) {
        try {
          code = generatedText.substring(indexOf + 8, lastIndexOf);
        } catch (Exception e) {
          log.error("Error extracting HTML from text:{}", generatedText, e);
          return null;
        }
      } else {
        try {
          code = generatedText.substring(indexOf + 8);
        } catch (Exception e) {
          log.error("Error extracting HTML from text:{}", generatedText, e);
          return null;
        }
      }
      return code;
    }
  }
}

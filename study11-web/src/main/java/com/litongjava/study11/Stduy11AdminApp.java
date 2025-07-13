package com.litongjava.study11;

import com.litongjava.annotation.AComponentScan;
import com.litongjava.tio.boot.TioApplication;

@AComponentScan
public class Stduy11AdminApp {

  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    TioApplication.run(Stduy11AdminApp.class, args);
    long end = System.currentTimeMillis();
    System.out.println((end - start) + "(ms)");
  }
}

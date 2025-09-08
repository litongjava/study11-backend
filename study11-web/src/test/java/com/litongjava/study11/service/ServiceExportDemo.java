package com.litongjava.study11.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.junit.Test;

import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.study11.config.AdminAppConfig;
import com.litongjava.table.utils.EasyExcelUtils;
import com.litongjava.tio.boot.testing.TioBootTest;

public class ServiceExportDemo {

  @Test
  public void testExport_study11_question_test_for_k12() {
    TioBootTest.runWith(AdminAppConfig.class);

    // 查询数据
    String sql = "select * from study11_question_test_for_k12 order by id";
    List<Row> records = Db.find(sql);
    for (Row row : records) {
      Long videoId = row.getLong("video_id");
      row.set("video_url", "https://preview.jieti.cc/preview/" + videoId);
    }

    // 导出数据到 Excel 文件
    try (OutputStream outputStream = new FileOutputStream("study11_question_test_for_k12.xlsx")) {
      EasyExcelUtils.write(outputStream, "test1", records);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }
}

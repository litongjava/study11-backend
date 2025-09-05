package com.litongjava.study11.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.litongjava.consts.ModelPlatformName;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.openrouter.OpenRouterModels;
import com.litongjava.study11.consts.Study11TableName;
import com.litongjava.study11.model.ExplanationVo;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class K12QuestionTestService {

  private HtmlAnimationService htmlAnimationService = Aop.get(HtmlAnimationService.class);

  public void initData() {
    String csvFile = "E:\\code\\java\\project-ppnt\\study11\\study11-backend\\study11-business\\questions\\VideoTutor Math Grades K-12.csv";

    List<Row> rows = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
      // 读取表头
      String headerLine = br.readLine();
      if (headerLine == null) {
        throw new RuntimeException("CSV 文件为空");
      }

      String csvSplitBy = ",";
      String[] headers = headerLine.split(csvSplitBy);

      String line;

      // 逐行读取内容
      while ((line = br.readLine()) != null) {
        // 按逗号分隔
        String[] values = line.split(csvSplitBy, -1);
        Row row = Row.by("id", SnowflakeIdUtils.id()).set("language", "English");
        for (int i = 0; i < headers.length; i++) {
          String key = headers[i].trim();
          if ("Grade".equals(key)) {
            key = "grade";
          } else if ("ID".equals(key)) {
            key = "no";
          } else if ("Problem".equals(key)) {
            key = "question";
          }
          Object value = i < values.length ? values[i].trim() : "";
          row.set(key, value);
        }
        rows.add(row);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    Db.batchSave("study11_question_test_for_k12", rows, rows.size());
  }

  public void batchTest() {
    String sql = "select * from %s where url is null order by id";
    sql = String.format(sql, Study11TableName.study11_question_test_for_k12);
    List<Row> rows = Db.find(sql);
    log.info("size:{}", rows.size());
    for (Row row : rows) {
      Long id = row.getLong("id");
      String question = row.getString("question");
      String language = row.getStr("language");
      ExplanationVo explanationVo = new ExplanationVo("1", question, language);

      explanationVo.setProvider(ModelPlatformName.OPENROUTER);
      explanationVo.setModel(OpenRouterModels.QWEN_QWEN3_CODER);

      Long videoId = htmlAnimationService.generate(explanationVo);

      String video_url = "https://preview.study11.ai/preview/" + videoId;
      try {
        Db.update(Study11TableName.study11_question_test_for_k12, Row.by("id", id).set("url", video_url));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}

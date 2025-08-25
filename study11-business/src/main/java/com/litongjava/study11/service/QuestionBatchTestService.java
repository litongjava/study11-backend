package com.litongjava.study11.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.study11.consts.EfTableName;
import com.litongjava.tio.utils.hutool.FileUtil;
import com.litongjava.tio.utils.hutool.ResourceUtil;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuestionBatchTestService {
  private HtmlAnimationService htmlAnimationService = Aop.get(HtmlAnimationService.class);

  public void initData() {
    // URL resource =
    // ResourceUtil.getResource("questions/basic_test_question_en.txt");
    // List<String> questions = FileUtil.readURLAsLines(resource);
    // int count = 0;
    // for (String q : questions) {
    // long id = SnowflakeIdUtils.id();
    // boolean save = Db.save(EfTableName.ef_question_test, Row.by("id",
    // id).set("question", q).set("language", "English"));
    // if (save) {
    // count++;
    // }
    // }

    URL resource = ResourceUtil.getResource("questions/basic_test_question_cn.txt");
    List<String> questions = FileUtil.readURLAsLines(resource);
    List<Row> rows = new ArrayList<>();
    for (String q : questions) {
      long id = SnowflakeIdUtils.id();
      Row row = Row.by("id", id).set("question", q).set("language", "Chinese");
      rows.add(row);
    }
    Db.batchSave(EfTableName.ef_question_test, rows, 2000);
    log.info("finish insert:{}", rows.size());
  }

  public void batchTest() {
    String sql = "select * from %s where url is null order by id";
    sql = String.format(sql, EfTableName.ef_question_test);
    List<Row> rows = Db.find(sql);
    log.info("size:{}", rows.size());
    for (Row row : rows) {
      Long id = row.getLong("id");
      String question = row.getString("question");
      String language = row.getStr("language");
      Long videoId = htmlAnimationService.generate(question, language);
      String video_url = "https://preview.study11.ai/preview/" + videoId;
      try {
        Db.update(EfTableName.ef_question_test, Row.by("id", id).set("url", video_url));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}

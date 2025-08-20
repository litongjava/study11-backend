package com.litongjava.study11.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.Kv;
import com.litongjava.bailian.BaiLianAiModels;
import com.litongjava.chat.ChatFile;
import com.litongjava.chat.UniChatClient;
import com.litongjava.chat.UniChatMessage;
import com.litongjava.chat.UniChatRequest;
import com.litongjava.chat.UniChatResponse;
import com.litongjava.consts.ModelPlatformName;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.study11.consts.EfTableName;
import com.litongjava.template.PromptEngine;
import com.litongjava.tio.utils.base64.Base64Utils;
import com.litongjava.tio.utils.crypto.Md5Utils;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import com.litongjava.tio.utils.http.HttpDownloadUtils;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlanGeneratedAnswerService {

  public void saveAnswer(Long videoId, String md5, String topic, String language, String answer, String urlString) {
    long id = SnowflakeIdUtils.id();
    try {
      Row row = Row.by("id", id).set("md5", md5).set("question", topic)
          //
          .set("language", language).set("answer", answer).set("urls", urlString).set("video_id", videoId);
      Db.save(EfTableName.ef_generated_answer, row);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String queryAnswer(String md5, String language) {
    String sql = "select answer from %s where md5=? and language=?";
    sql = String.format(sql, EfTableName.ef_generated_answer);
    String answer = Db.queryStr(sql, md5, language);
    return answer;
  }

  public String queryAnserById(Long gruopId) {
    String sql = "select answer from %s where video_id=?";
    sql = String.format(sql, EfTableName.ef_generated_answer);
    String answer = Db.queryStr(sql, gruopId);
    return answer;
  }
}
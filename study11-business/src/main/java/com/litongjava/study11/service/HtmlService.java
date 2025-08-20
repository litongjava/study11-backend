package com.litongjava.study11.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.Kv;
import com.litongjava.chat.UniChatClient;
import com.litongjava.chat.UniChatRequest;
import com.litongjava.chat.UniChatResponse;
import com.litongjava.consts.ModelPlatformName;
import com.litongjava.db.SqlPara;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.model.page.Page;
import com.litongjava.template.PromptEngine;
import com.litongjava.tio.utils.hutool.FileUtil;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;
import com.litongjava.utils.CodeBlockUtils;
import com.litongjava.volcengine.VolcEngineModels;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HtmlService {

  private static final String selectSql = "select id,topic,language,type,elapsed,user_id,is_public,view_count,create_time from study11_html_code";

  public Long generate(String topic) {
    String sql = "select id from study11_html_code where topic=?";
    Long id = Db.queryLong(sql, topic);
    if (id != null) {
      return id;
    }

    Kv kv = Kv.by("topic", topic);
    String prompt = PromptEngine.renderToString("generate_html_code_system_prompt.txt", kv);
    String html = genCode(prompt);
    id = SnowflakeIdUtils.id();
    Row row = Row.by("id", id).set("topic", topic).set("html", html);
    Db.save("study11_html_code", row);
    return id;
  }

  public String getCodeById(Long id) {
    String sql = "select html from study11_html_code where id=?";
    return Db.queryStr(sql, id);
  }

  public String genCode(String userPrompt) {

    UniChatRequest uniChatRequest = new UniChatRequest(userPrompt, 0f);
    uniChatRequest.setPlatform(ModelPlatformName.VOLC_ENGINE).setModel(VolcEngineModels.DEEPSEEK_R1_250528);

    UniChatResponse generate = UniChatClient.generate(uniChatRequest);
    String generatedText = generate.getMessage().getContent();

    String code = CodeBlockUtils.parseHtml(generatedText);

    if (code != null) {
      code = code.replaceAll("^(\\s*\\R)+", "").replaceAll("(\\R\\s*)+$", "");
      code.trim();
      new File("html").mkdirs();
      try {
        String path = "html/" + SnowflakeIdUtils.id() + ".html";
        log.info("code file:{}", path);
        FileUtil.writeString(code, path, "UTF-8");
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    return code;
  }

  public Kv recommends(Integer pageNo, int pageSize, String sort_by, String host) {
    // recent and view
    String sql = null;
    if ("recent".equals(sort_by)) {
      sql = selectSql + " order by create_time desc";
    } else {
      sql = selectSql + " order by view_count desc,create_time desc";
    }

    SqlPara sqlPara = SqlPara.by(sql);
    Page<Row> paginate = Db.paginate(pageNo, pageSize, sqlPara);
    int totalRow = paginate.getTotalRow();
    List<Row> list = paginate.getList();
    List<Kv> kvs = new ArrayList<>();
    for (Row row : list) {
      Kv kv = row.toKv();
      Long id = kv.getLong("id");
      String url = append(host, id);
      kv.set("url", url);
      kvs.add(kv);
    }
    Kv result = Kv.by("total", totalRow).set("videos", kvs);
    return result;
  }

  public Kv detail(Long id, String host) {
    String sql = selectSql + " where id=?";
    Row row = Db.findFirst(sql, id);
    if (row != null) {
      Kv kv = row.toKv();
      String url = append(host, id);
      kv.set("url", url);
      return kv;
    }
    return null;
  }

  private String append(String host, Long id) {
    String url = "//" + host + "/preview/" + id;
    return url;
  }
}

package com.litongjava.study11.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.Kv;
import com.litongjava.chat.UniChatClient;
import com.litongjava.chat.UniChatMessage;
import com.litongjava.chat.UniChatRequest;
import com.litongjava.chat.UniChatResponse;
import com.litongjava.db.SqlPara;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.page.Page;
import com.litongjava.template.PromptEngine;
import com.litongjava.tio.utils.hutool.FileUtil;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;
import com.litongjava.utils.CodeBlockUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HtmlAnimationService {

  private static final String selectSql = "select id,topic,language,type,elapsed,user_id,is_public,view_count,create_time from study11_html_code";

  PlatformAndModelSetService platformAndModelSetService = Aop.get(PlatformAndModelSetService.class);
  PlanSenceService planSenceService = Aop.get(PlanSenceService.class);

  public Long generate(String topic, String language) {

    String sql = "select id from study11_html_code where topic=?";
    Long id = Db.queryLong(sql, topic);
    if (id != null) {
      return id;
    }
    id = SnowflakeIdUtils.id();
    log.info("start with id:{}", id);
    log.info("start plan:{}", topic);
    String plan = planSenceService.plan(id, topic, language);
    log.info("finish plan:{}", topic);

    String prompt = getSystemPrompt();
    log.info("start generate code of plan:{}", topic);
    String html = genCode(prompt, plan, topic, language, id);
    log.info("finish generate code of plan:{}", topic);
    Row row = Row.by("id", id).set("topic", topic).set("html", html);
    Db.save("study11_html_code", row);
    return id;
  }

  public String getSystemPrompt() {
    String prompt = PromptEngine.renderToString("generate_html_code_system_prompt.txt");
    return prompt;
  }

  public String getCodeById(Long id) {
    String sql = "select html from study11_html_code where id=?";
    return Db.queryStr(sql, id);
  }

  public String genCode(String systemPrompt, String plan, String question, String language, long id) {
    List<UniChatMessage> messages = new ArrayList<>();
    question = "The user question is:" + question+" .The generated subtitles and narration must use the "+language;
    plan = "The user scene plan is:" + plan;

    String userMessage = question + ".\r\n" + plan + ".\r\n" + "Please only output the html code.";

    String prompt = systemPrompt + "\r\n" + userMessage;
    File file = new File("prompts");
    if (!file.exists()) {
      file.mkdirs();
    }
    FileUtil.writeString(prompt, "prompts" + File.separator + id + ".txt");
    messages.add(UniChatMessage.buildUser(userMessage));

    UniChatRequest uniChatRequest = new UniChatRequest(systemPrompt, messages, 0f);
    platformAndModelSetService.configPlatformAndModel(uniChatRequest);

    UniChatResponse generate = UniChatClient.generate(uniChatRequest);
    String generatedText = generate.getMessage().getContent();

    String code = CodeBlockUtils.parseHtml(generatedText);
    if (code != null) {
      code = code.replaceAll("^(\\s*\\R)+", "").replaceAll("(\\R\\s*)+$", "");
      code.trim();
      File htmlFolder = new File("html");
      if (!htmlFolder.exists()) {
        htmlFolder.mkdirs();
      }

      try {
        String path = "html/" + id + ".html";
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

package com.litongjava.study11.service;

import java.util.List;

import com.jfinal.kit.Kv;

import nexus.io.db.activerecord.Db;
import nexus.io.db.activerecord.Row;
import nexus.io.template.EnjoyEngine;

public class IndexService {

  public String index() {
    List<Row> data = Db.find("select id,topic,language,view_count,create_time from study11_html_code order by id");
    Kv kv = Kv.by("data", data);
    String html = EnjoyEngine.renderToString("index.html", kv);
    return html;
  }
}

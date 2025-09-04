package com.litongjava.study11.service;

import com.litongjava.db.DbJsonObject;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.study11.consts.StudyBaseTableName;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

public class SenceStoryboardService {

  public void saveStoryboard(Long videoId, String md5, String topic, String language, String storyboard,
      String urlString) {
    long id = SnowflakeIdUtils.id();
    DbJsonObject dbJsonObject = new DbJsonObject(storyboard);
    try {
      Row row = Row.by("id", id).set("md5", md5).set("question", topic)
          //
          .set("language", language).set("storyboard", dbJsonObject).set("urls", urlString).set("video_id", videoId);

      Db.save(StudyBaseTableName.study11_sence_storyboard, row);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String queryStoryboard(String md5, String language) {
    String sql = "select storyboard from %s where md5=? and language=?";
    sql = String.format(sql, StudyBaseTableName.study11_sence_storyboard);
    String storyboard = Db.queryStr(sql, md5, language);
    return storyboard;
  }

  public String queryStoryboardById(Long gruopId) {
    String sql = "select storyboard from %s where video_id=?";
    sql = String.format(sql, StudyBaseTableName.study11_sence_storyboard);
    String storyboard = Db.queryStr(sql, gruopId);
    return storyboard;
  }

}

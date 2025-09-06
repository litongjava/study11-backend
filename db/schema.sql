drop table if exists study11_html_code;
CREATE TABLE study11_html_code (
  id BIGINT PRIMARY KEY,
  topic VARCHAR,
  "language" VARCHAR,
  html VARCHAR,
  "cover_svg" VARCHAR,
  view_count int4 DEFAULT 0 NULL,
  user_id VARCHAR,
  "type" VARCHAR,
  "elapsed" bigint,
  
  is_public bool DEFAULT FALSE,
  "creator" VARCHAR ( 64 ) DEFAULT '',
  "create_time" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updater" VARCHAR ( 64 ) DEFAULT '',
  "update_time" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "deleted" SMALLINT DEFAULT 0,
  "tenant_id" BIGINT NOT NULL DEFAULT 0
);


drop table if exists study11_scene_storyboard;
CREATE TABLE study11_scene_storyboard (
  id BIGINT PRIMARY KEY,
  md5 varchar,
  topic VARCHAR,
  language varchar,
  storyboard JSONB,
  storyboard_xml varchar,
  urls VARCHAR,
  video_id bigint,
  sence_quiz JSONB,
  "creator" VARCHAR ( 64 ) DEFAULT '',
  "create_time" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updater" VARCHAR ( 64 ) DEFAULT '',
  "update_time" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "deleted" SMALLINT DEFAULT 0,
  "tenant_id" BIGINT NOT NULL DEFAULT 0
);

drop table if exists ef_question_test;
CREATE TABLE ef_question_test (
  id BIGINT NOT NULL,
  question VARCHAR NOT NULL,
  language VARCHAR(64) NOT NULL,
  url varchar,
  creator VARCHAR(64) DEFAULT '', -- 创建者
  create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  updater VARCHAR(64) DEFAULT '', -- 更新者
  update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 更新时间
  deleted SMALLINT NOT NULL DEFAULT 0, -- 删除标志
  tenant_id BIGINT NOT NULL DEFAULT 0, -- 租户ID
  PRIMARY KEY (id) -- 主键
);

drop table if exists study11_question_test_for_k12;
CREATE TABLE study11_question_test_for_k12 (
  id BIGINT NOT NULL,
  grade varchar,
  no varchar,
  question VARCHAR NOT NULL,
  language VARCHAR(64) NOT NULL,
  url varchar,  
  video_url varchar,
  creator VARCHAR(64) DEFAULT '', -- 创建者
  create_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
  updater VARCHAR(64) DEFAULT '', -- 更新者
  update_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 更新时间
  deleted SMALLINT NOT NULL DEFAULT 0, -- 删除标志
  tenant_id BIGINT NOT NULL DEFAULT 0, -- 租户ID
  PRIMARY KEY (id) -- 主键
);
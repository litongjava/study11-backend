drop table if exists study11_html_code;
CREATE TABLE study11_html_code (
  id BIGINT PRIMARY KEY,
  topic VARCHAR,
  html VARCHAR,
  "language" VARCHAR,
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


drop table if exists ef_generated_answer;
CREATE TABLE ef_generated_answer (
  id BIGINT PRIMARY KEY,
  md5 varchar,
  question VARCHAR,
  language varchar,
  answer VARCHAR,
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
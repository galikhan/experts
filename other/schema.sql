--CREATE SEQUENCE fx_sequence;

drop table fx_leagues;
create table fx_leagues (
  id_ BIGINT PRIMARY KEY,
  name_ VARCHAR(255),
  creator_ varchar(255),
  desc_ text,
  chat_id_ BIGINT,
  group_chat_ boolean,
  create_date_ timestamp,
  modify_date_ timestamp
)
ALTER TABLE fx_leagues ADD CONSTRAINT uq_name__chat_id_ UNIQUE (name_, chat_id_);

drop table fx_users;
create table fx_users (
  name_ VARCHAR(255) PRIMARY KEY ,
  firstname VARCHAR(255),
  lastname VARCHAR(255)
);


drop table fx_forecasts;
create table fx_forecasts (
  id_ INTEGER PRIMARY KEY,
  user_ VARCHAR(255),
  league_ BIGINT,
  match_id_ INTEGER,
  home_point_ INTEGER,
  guests_point_ INTEGER,
  create_date_ timestamp,
  modify_date_ timestamp
)


drop table fx_matches;
create table fx_matches (
  id_  BIGINT PRIMARY KEY,
  league_ BIGINT,
  match_id_ INTEGER,
  home_ VARCHAR(255),
  guests_ VARCHAR(255),
  home_point_ INTEGER,
  guests_point_ INTEGER,
  finished_ boolean,
  create_date_ timestamp,
  modify_date_ timestamp
)
--alter table fx_matches add constraint fk_matches_leagues foreign key (league_) references fx_leagues(id_);

drop table fx_experts;
create table fx_experts (
  id_  BIGINT PRIMARY KEY,
  league_ BIGINT,
  user_ VARCHAR(255),
  plus4_ INTEGER,
  plus2_ INTEGER,
  plus1_ INTEGER,
  total_ INTEGER,
  create_date_ timestamp,
  modify_date_ timestamp
)


drop table fx_request_log;
create table fx_request_log (
  id_  BIGINT PRIMARY KEY,
  chat_id_  BIGINT,
  user_ VARCHAR(255),
  request_ VARCHAR(255),
  create_date_ timestamp,
  modify_date_ timestamp
)


drop table fx_conversation;
create table fx_conversation (
  id_  BIGINT PRIMARY KEY,
  chat_id_ BIGINT,
  type_ VARCHAR(30),
  user_ VARCHAR(255),
  request_ VARCHAR(255),
  response_ TEXT,
  has_answer_ BOOLEAN,
  removed_ BOOLEAN,
  active_ BOOLEAN,
  league_ BIGINT,
  create_date_ timestamp,
  modify_date_ timestamp
)


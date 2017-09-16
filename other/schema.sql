create table fx_users (
  name_ VARCHAR(255) PRIMARY KEY ,
  firstname VARCHAR(255),
  lastname VARCHAR(255)
)

create table fx_results (
  id_ INTEGER PRIMARY KEY,
  user_ VARCHAR(255),
  league_ VARCHAR(255),
  match_id_ INTEGER,
  home_point_ INTEGER,
  guests_point_ INTEGER,
  create_date_ timestamp,
  modify_date_ timestamp
)

create table fx_leagues (
  name_ VARCHAR(255) PRIMARY KEY ,
  desc_ text,
  creator_ varchar(255),
  create_date_ timestamp,
  modify_date_ timestamp
)


create table fx_matches (
  id_  BIGINT PRIMARY KEY,
  league_ VARCHAR(255),
  match_id_ INTEGER,
  home_ VARCHAR(255),
  guests_ VARCHAR(255),
  home_point_ INTEGER,
  guests_point_ INTEGER,
  create_date_ timestamp,
  modify_date_ timestamp
)


create table fx_experts (
  id_  BIGINT PRIMARY KEY,
  league_ VARCHAR(255),
  user_ VARCHAR(255),
  plus4_ INTEGER,
  plus2_ INTEGER,
  plus1_ INTEGER,
  total_ INTEGER,
  create_date_ timestamp,
  modify_date_ timestamp
)





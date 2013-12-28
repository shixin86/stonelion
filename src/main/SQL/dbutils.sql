drop table if exists `dbutils`;
create table dbutils(
tinyint_v tinyint(10) unsigned not null default 0 primary key,
smallint_v smallint(10) unsigned not null default 0,
mediumint_v mediumint(10) not null default 0,
int_v int(10) not null default 0,
bigint_v bigint(10) not null default 0,
float_v float(10,5) not null,
double_v double(10,5) not null,
char_v char(255),
varchar_v varchar(255),
blob_v blob,
text_v text,
date_v date not null,
time_v time not null,
year_v year not null,
datetime_v datetime not null,
timestamp_v timestamp not null
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
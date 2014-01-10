#!/bin/sh
#export server=mysql.babywar.xinqihd.com
#export server=mysqldb.babywar.xinqihd.com
export server=localhost
export username=root
export password=r00t123

mysql -u$username -p$password -h$server << EOF
create database if not exists babywardb default character set utf8;
use babywardb;
drop table if exists billing;
create table billing  (
  id int primary key auto_increment,
  username varchar(50) not null,
  rolename varchar(50) not null,
  chargeid int,
  channel varchar(20),
  /*
    User's phone type
  */
  device   varchar(20),
  chargedate datetime not null unique,
  /* 
   The amount of money user charge. like 0.99$ or 6 RMB
   */
  amount float,
  /* 
    The amount should be converted to RMB
  */
  rmb_amount float,
  /* 
    The currency. 'US Dollor' or 'RMB'
  */
  currency varchar(20),
  /* 
    The discount
  */
  discount float,
  /* 
    The original yuanbao
  */
  orig_yuanbao int,
  /*
    The yuanbao that user got
  */
  bought_yuanbao int,
  /* 
   The total yuanbao now user have.
  */
  total_yuanbao int,
  /*
   Only for iOS iAP
  */
  receipt_data varchar(255),
  /*
   The success mark
  */
  success bool,
  index username (username)
) 
default character set = utf8
collate = utf8_bin;
EOF

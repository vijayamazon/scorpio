drop table if exists amazon_order;
create table amazon_order
(
  id              bigint unsigned primary key auto_increment,
  amazon_order_id char(19) not null,
  market          varchar(10) not null default '',
  store           varchar(10) not null default '',
  status          varchar(50) not null,
  fulfillment     tinyint(1) not null comment 'AFN:0;MFN:1',
  data            json comment 'https://docs.developer.amazonservices.com/en_US/orders-2013-09-01/Orders_Datatypes.html#Order',
  create_time     timestamp not null default current_timestamp,
  unique index (amazon_order_id),
  index (market),
  index (store),
  index (status(10))
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists amazon_order_item;
create table amazon_order_item
(
  id                   bigint unsigned primary key auto_increment,
  amazon_order_id      char(19) not null,
  amazon_order_item_id varchar(20) not null,
  market               varchar(10) not null default '',
  store                varchar(10) not null default '',
  asin                 varchar(50) not null,
  seller_sku           varchar(50) not null,
  data                 json comment 'https://docs.developer.amazonservices.com/en_US/orders-2013-09-01/Orders_Datatypes.html#OrderItem',
  create_time          timestamp not null default current_timestamp,
  unique index (amazon_order_item_id(14)),
  index (amazon_order_id),
  index (market),
  index (store),
  index (asin(10)),
  index (seller_sku(10))
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists amazon_entry;
create table amazon_entry
(
  id              bigint unsigned primary key auto_increment,
  market          varchar(10) not null default '',
  store           varchar(10) not null default '',
  asin            varchar(50) not null,
  sku             varchar(50) not null,
  url             varchar(500) not null default '',
  status          tinyint(1) unsigned not null default 1 comment 'disable:0;enable:1',
  create_time     timestamp not null default current_timestamp,
  index (market),
  index (store),
  index (asin(10)),
  index (sku(10))
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists amazon_entry_snapshot;
create table amazon_entry_snapshot
(
  id              bigint unsigned primary key auto_increment,
  market          varchar(10) not null default '',
  store           varchar(10) not null default '',
  asin            varchar(50) not null,
  sku             varchar(50) not null,
  variable        int unsigned not null default 0,
  rank_best       int unsigned not null default 0,
  review_count    int unsigned not null default 0,
  star_average    float(3,2) not null default 0,
  star_1          int unsigned not null default 0,
  star_2          int unsigned not null default 0,
  star_3          int unsigned not null default 0,
  star_4          int unsigned not null default 0,
  star_5          int unsigned not null default 0,
  create_time     timestamp not null default current_timestamp,
  index (market),
  index (store),
  index (asin(10)),
  index (sku(10))
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

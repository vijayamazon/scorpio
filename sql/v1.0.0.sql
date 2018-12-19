drop table if exists amazon_order;
create table amazon_order
(
  id              bigint unsigned primary key auto_increment,
  amazon_order_id char(19) not null,
  market          char(4) not null default '',
  status          varchar(50) not null,
  fulfillment     tinyint(1) not null comment 'AFN:0;MFN:1',
  data            json comment 'https://docs.developer.amazonservices.com/en_US/orders-2013-09-01/Orders_Datatypes.html#Order',
  create_time     timestamp not null default current_timestamp,
  unique index (amazon_order_id),
  index (market),
  index (status(10))
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists amazon_order_item;
create table amazon_order_item
(
  id                   bigint unsigned primary key auto_increment,
  amazon_order_id      char(19) not null,
  amazon_order_item_id varchar(20) not null,
  market               varchar(10) not null default '',
  asin                 varchar(50) not null,
  seller_sku           varchar(50) not null,
  data                 json comment 'https://docs.developer.amazonservices.com/en_US/orders-2013-09-01/Orders_Datatypes.html#OrderItem',
  create_time          timestamp not null default current_timestamp,
  unique index (amazon_order_item_id(14)),
  index (amazon_order_id),
  index (market),
  index (asin(10)),
  index (seller_sku(10))
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists amazon_entry;
create table amazon_entry
(
  id              bigint unsigned primary key auto_increment,
  market          char(4) not null default '',
  asin            varchar(50) not null,
  sku             varchar(50) not null,
  url             varchar(500) not null default '',
  status          tinyint(1) unsigned not null default 1 comment 'disable:0;enable:1',
  create_time     timestamp not null default current_timestamp,
  index (market),
  index (asin(10)),
  index (sku(10))
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists amazon_entry_snapshot;
create table amazon_entry_snapshot
(
  id              bigint unsigned primary key auto_increment,
  market          char(4) not null default '',
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
  index (asin(10)),
  index (sku(10))
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists amazon_inventory;
create table amazon_inventory
(
  id                   bigint unsigned primary key auto_increment,
  market               varchar(10) not null default '',
  asin                 varchar(50) not null,
  fn_sku               varchar(50) not null,
  seller_sku           varchar(50) not null,
  in_stock_quantity    int unsigned not null default 0,
  update_time          timestamp not null default current_timestamp,
  index (asin),
  index (market),
  index (seller_sku(10))
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists amazon_fulfillment;
create table amazon_fulfillment
(
  id                   bigint unsigned primary key auto_increment,
  name                 CHAR(3) not null,
  data                 tinyint(1) unsigned not null
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

insert into amazon_fulfillment values (1, 'AFN', 0), (2, 'MFN', 1);

drop view if exists vs_order;
create view vs_order as select i.market, i.amazon_order_id, i.amazon_order_item_id, i.asin, i.seller_sku, from_unixtime(o.data->'$.purchaseDate'/1000) as purchase_time, o.fulfillment, o.status from amazon_order o, amazon_order_item i where o.amazon_order_id = i.amazon_order_id;

drop view if exists vs_entry_order;
create view vs_entry_order as select e.market, e.asin, e.sku, i.seller_sku, o.amazon_order_id, i.amazon_order_item_id, from_unixtime(o.data->'$.purchaseDate'/1000) as purchase_time, o.fulfillment, o.status from amazon_entry e, amazon_order o, amazon_order_item i where e.market = o.market and o.amazon_order_id = i.amazon_order_id and i.seller_sku like concat('%', e.sku, '-%');

drop view if exists vs_entry_inventory;
create view vs_entry_inventory as select e.asin, e.sku, i.seller_sku, i.in_stock_quantity, i.update_time from amazon_entry e, amazon_inventory i where i.seller_sku like concat('%', e.sku, '-%');

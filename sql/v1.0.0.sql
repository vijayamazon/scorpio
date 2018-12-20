drop table if exists amazon_order;
create table amazon_order
(
  id              bigint unsigned primary key auto_increment,
  market          char(4) not null default '',
  amazon_order_id char(19) not null,
  status          varchar(50) not null,
  fulfillment     char(3) not null,
  purchase_date   date comment 'valid when status is: Unshipped,PartiallyShipped,Shipped',
  data            json comment 'https://docs.developer.amazonservices.com/en_US/orders-2013-09-01/Orders_Datatypes.html#Order',
  create_time     timestamp not null default current_timestamp,
  unique index (market, amazon_order_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists amazon_order_item;
create table amazon_order_item
(
  id                   bigint unsigned primary key auto_increment,
  market               varchar(10) not null default '',
  amazon_order_id      char(19) not null,
  amazon_order_item_id varchar(20) not null,
  quantity             tinyint(2) not null default 1,
  status               varchar(50) not null,
  fulfillment          char(3) not null,
  purchase_date        date comment 'valid when status is: Unshipped,PartiallyShipped,Shipped',
  asin                 varchar(50) not null,
  seller_sku           varchar(50) not null,
  data                 json comment 'https://docs.developer.amazonservices.com/en_US/orders-2013-09-01/Orders_Datatypes.html#OrderItem',
  create_time          timestamp not null default current_timestamp,
  index (amazon_order_id),
  index (amazon_order_item_id),
  index (asin),
  index (seller_sku(10)),
  index(purchase_date)
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
  start_date      date comment 'start monitoring date',
  stop_date       date comment 'stop monitoring date',
  unique index (market, asin, sku),
  index (asin),
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
  create_date     date not null,
  index (asin),
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
  total_quantity       int unsigned not null default 0,
  fulfillment          char(3) not null,
  create_date          date not null,
  unique index (market, fn_sku, create_date),
  index (asin),
  index (fn_sku),
  index (seller_sku(10)),
  index (create_date)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop view if exists vs_order_item;
create view vs_order_item as select i.market, e.asin as entry, e.sku, i.asin, i.seller_sku, i.fulfillment, i.purchase_date, i.quantity from amazon_order_item i, amazon_entry e where i.seller_sku like concat('%', e.sku, '-%');

drop view if exists vs_order;
create view vs_order as select i.market, e.asin as entry, i.fulfillment, i.purchase_date, sum(i.quantity) as total_quantity from amazon_order_item i, amazon_entry e where i.seller_sku like concat('%', e.sku, '-%') group by i.market, e.asin, i.fulfillment, i.purchase_date;

drop view if exists vs_inventory_item;
create view vs_inventory_item as select i.market, e.asin as entry, e.sku, i.asin, i.fn_sku, i.seller_sku, i.fulfillment, i.create_date from amazon_inventory i, amazon_entry e where  i.seller_sku like concat('%', e.sku, '-%');

drop view if exists vs_inventory;
create view vs_inventory as select i.market, e.asin as entry, i.fulfillment, i.create_date, sum(i.in_stock_quantity) as quantity from amazon_inventory i, amazon_entry e where  i.seller_sku like concat('%', e.sku, '-%') group by i.market, e.asin, i.fulfillment, i.create_date;

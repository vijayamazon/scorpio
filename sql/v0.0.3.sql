drop table if exists amazon_inventory;
create table amazon_inventory
(
  id                   bigint unsigned primary key auto_increment,
  market               varchar(10) not null default '',
  store                varchar(10) not null default '',
  asin                 varchar(50) not null,
  fn_sku               varchar(50) not null,
  seller_sku           varchar(50) not null,
  in_stock_quantity    int unsigned not null default 0,
  update_time          timestamp not null default current_timestamp,
  unique index (asin),
  index (market),
  index (store),
  index (seller_sku(10))
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

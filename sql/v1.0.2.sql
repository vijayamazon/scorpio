drop table if exists amazon_seller_sku;
create table amazon_seller_sku
(
  id              bigint unsigned primary key auto_increment,
  market          char(4) not null default '',
  sku             varchar(50) not null,
  seller_sku      varchar(50) not null,
  create_time     timestamp not null default current_timestamp,
  unique index (market, seller_sku),
  index (seller_sku)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

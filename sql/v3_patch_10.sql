drop table if exists shopify_order;
create table shopify_order
(
    id               bigint unsigned primary key auto_increment,
    sku              varchar(100) not null,
    size             varchar(100) not null,
    order_id         varchar(100) not null,
    order_number     varchar(100) not null,
    order_name       varchar(100) not null,
    order_time       timestamp    not null,
    contact_email    varchar(100) not null,
    price_usd_cent   int unsigned not null default 0,
    gateway          varchar(100) not null,
    financial_status varchar(100) not null,
    country_code     char(2)      not null,
    data             json,
    create_time      timestamp    not null default current_timestamp,
    index (order_id),
    index (sku, size)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

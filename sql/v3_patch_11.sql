drop table if exists shopify;
create table shopify
(
    id          bigint unsigned primary key auto_increment,
    order_id    varchar(100) not null,
    order_name  varchar(100) not null,
    unique index (order_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

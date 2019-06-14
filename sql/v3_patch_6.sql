drop table if exists amazon_asin;
create table amazon_asin
(
    id          bigint unsigned primary key auto_increment,
    market      char(4)     not null default '',
    asin        varchar(50) not null,
    parent_asin varchar(50) not null,
    color       varchar(50) not null,
    size        varchar(50) not null,
    create_date date        not null,
    update_date date,
    index (asin),
    index (parent_asin),
    unique index (market, asin, color, size)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

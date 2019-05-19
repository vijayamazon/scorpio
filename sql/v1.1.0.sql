drop table if exists amazon_inventory_report;
create table amazon_inventory_report
(
    id              bigint unsigned primary key auto_increment,
    asin            varchar(50) not null,
    seller_sku      varchar(50) not null,
    price           int not null default 0,
    quantity        int not null default 0,
    report_date     date not null,
    unique index (seller_sku),
    index (asin)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists amazon_report_log;
create table amazon_report_log
(
    id              bigint unsigned primary key auto_increment,
    request_id      varchar(50) not null,
    report_id       varchar(50) not null,
    report_type     varchar(50) not null,
    status          int not null default 0 comment '0: init, 1: processed',
    create_time     timestamp not null default current_timestamp,
    unique index (request_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

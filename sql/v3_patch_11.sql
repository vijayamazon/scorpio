drop table if exists amazon_business_report;
create table amazon_business_report
(
    id                        bigint unsigned primary key auto_increment,
    market                    varchar(10) not null,
    date                      date        not null,
    asin                      varchar(20) not null,
    title                     varchar(500),
    session                   int unsigned,
    session_percentage        float unsigned,
    pv                        int unsigned,
    pv_percentage             float unsigned,
    buy_box_percentage        float unsigned,
    units_ordered             int unsigned,
    unit_session_percentage   float unsigned,
    ordered_product_sales     int unsigned,
    ordered_product_sales_raw varchar(100),
    total_order_items         int unsigned,
    index (market),
    index (date),
    index (asin)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

drop table if exists amazon_ads_report;
create table amazon_ads_report
(
    id                             bigint unsigned primary key auto_increment,
    market                         varchar(10) not null,
    date                           date,
    portfolio_name                 varchar(100),
    currency                       varchar(10),
    campaign_name                  varchar(100),
    ad_group_name                  varchar(100),
    advertised_sku                 varchar(100),
    advertised_asin                varchar(100),
    impression                     int unsigned,
    click                          int unsigned,
    ctr                            float unsigned,
    cpc                            int unsigned,
    cpc_raw                        varchar(100),
    spend                          int unsigned,
    spend_raw                      varchar(100),
    7_day_total_sales              int unsigned,
    7_day_total_sales_raw          varchar(100),
    acos                           float unsigned,
    roas                           float unsigned,
    7_day_total_orders             int unsigned,
    7_day_total_units              int unsigned,
    7_day_conversion_rate          float unsigned,
    7_day_advertised_sku_units     int unsigned,
    7_day_other_sku_units          int unsigned,
    7_day_advertised_sku_sales     int unsigned,
    7_day_advertised_sku_sales_raw varchar(100),
    7_day_other_sku_sales          int unsigned,
    7_day_other_sku_sales_raw      varchar(100),
    index (market),
    index (date),
    index (campaign_name),
    index (ad_group_name),
    index (advertised_asin)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

drop table if exists amazon_entry_2;
create table amazon_entry_2
(
    id     bigint unsigned primary key auto_increment,
    market varchar(10) not null,
    asin   varchar(50) not null,
    status tinyint(1)  not null,
    date   date        not null,
    unique (market, asin),
    index (asin),
    index (date)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table amazon_inventory
    add column data text;
alter table amazon_inventory
    add index (market);

drop table if exists amazon_fba_return;
create table amazon_fba_return
(
    id                    bigint unsigned primary key auto_increment,
    market                varchar(10)  not null,
    date                  date,
    time                  timestamp,
    order_id              varchar(20)  not null,
    seller_sku            varchar(100) not null,
    asin                  varchar(20)  not null,
    fn_sku                varchar(20),
    product_name          varchar(1000),
    quantity              tinyint unsigned,
    fulfillment_center_id varchar(20),
    detailed_disposition  varchar(100),
    reason                varchar(500),
    status                varchar(100),
    license_plate_number  varchar(100),
    customer_comments     text,
    sku                   varchar(50) default '',
    size                  varchar(10) default '',
    index (market),
    index (date),
    index (order_id),
    index (asin),
    index (seller_sku),
    index (detailed_disposition),
    index (reason),
    index (sku),
    index (size)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

drop table if exists amazon_product;
create table amazon_product
(
    id         bigint unsigned primary key auto_increment,
    market     varchar(10) not null,
    parent     varchar(20) not null,
    asin       varchar(20) not null,
    title      varchar(500),
    image      varchar(500),
    color      varchar(50),
    seller_sku varchar(100),
    sku        varchar(50),
    size       varchar(10),
    unique (market, asin),
    index (parent),
    index (asin),
    index (seller_sku),
    index (sku),
    index (size),
    index (color)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

drop table if exists amazon_bz_child_report;
create table amazon_bz_child_report
(
    id                        bigint unsigned primary key auto_increment,
    market                    varchar(10) not null,
    date                      date        not null,
    parent                    varchar(20) not null,
    asin                      varchar(20) not null,
    title                     varchar(500),
    session                   int unsigned,
    session_percentage        float unsigned,
    pv                        int unsigned,
    pv_percentage             float unsigned,
    buy_box_percentage        float unsigned,
    units_ordered             int unsigned,
    unit_session_percentage   float unsigned,
    ordered_product_sales     int unsigned,
    ordered_product_sales_raw varchar(100),
    total_order_items         int unsigned,
    seller_sku                varchar(100),
    sku                       varchar(50),
    size                      varchar(10),
    index (market),
    index (parent),
    index (date),
    index (asin)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

drop table if exists amazon_age_report;
create table amazon_age_report
(
    id            bigint unsigned primary key auto_increment,
    market        varchar(10)   not null,
    date          date          not null,
    asin          varchar(20)   not null,
    fn_sku        varchar(20)   not null,
    seller_sku    varchar(100)  not null,
    quantity      int unsigned,
    age_90        int unsigned,
    age_180       int unsigned,
    age_270       int unsigned,
    age_365       int unsigned,
    age_year_plus int unsigned,
    currency      char(10),
    sku           varchar(50),
    size          varchar(10),
    unique (market, asin),
    index (asin),
    index (seller_sku),
    index (sku)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

drop table if exists product_tag;
create table product_tag
(
    id  bigint unsigned primary key auto_increment,
    sku varchar(50),
    tag varchar(50),
    index (sku),
    index (tag)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;
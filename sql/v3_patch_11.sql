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
drop table if exists amazon_ads;
create table amazon_ads
(
    id                         bigint unsigned primary key auto_increment,
    report_log                 text,
    report_date                date,
    portfolio_name             varchar(100),
    currency                   varchar(100),
    campaign_name              varchar(100),
    ad_group_name              varchar(100),
    advertised_sku             varchar(100),
    advertised_asin            varchar(100),
    impressions                int unsigned,
    clicks                     int unsigned,
    ctr                        varchar(100),
    cpc                        varchar(100),
    spend                      varchar(100),
    7_day_total_sales          varchar(100),
    acos                       varchar(100),
    roas                       float,
    7_day_total_orders         int unsigned,
    7_day_total_units          int unsigned,
    7_day_conversion_rate      varchar(100),
    7_day_advertised_sku_units int unsigned,
    7_day_other_sku_units      int unsigned,
    7_day_advertised_sku_sales varchar(100),
    7_day_other_sku_sales      varchar(100),
    index (report_date),
    index (campaign_name),
    index (ad_group_name),
    index (advertised_asin)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table amazon_ads
    add column calc_spend int unsigned,
    add column calc_sales int unsigned;

drop table if exists amazon_keyword;
create table amazon_keyword
(
    id          bigint unsigned primary key auto_increment,
    sku         varchar(100) not null default '',
    keyword     varchar(500) not null default '',
    index (sku),
    index (keyword(20))
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;
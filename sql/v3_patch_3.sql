alter table amazon_seller_sku
    add column size varchar(10) default '';

drop table if exists amazon_inbound;
create table amazon_inbound
(
    id             bigint unsigned primary key auto_increment,
    shipment_id    varchar(50)  not null,
    shipment_name  varchar(100) not null,
    status         varchar(50)  not null,
    dest_center_id varchar(10)  not null,
    data           json comment 'http://docs.developer.amazonservices.com/en_US/fba_inbound/FBAInbound_Datatypes.html#InboundShipmentInfo',
    market         varchar(10)  not null,
    dest           varchar(10)  not null,
    create_time    timestamp    not null default current_timestamp,
    unique index (shipment_id(10))
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

drop table if exists amazon_inbound_item;
create table amazon_inbound_item
(
    id                bigint unsigned primary key auto_increment,
    # copy of shipment info
    shipment_id       varchar(50)  not null,
    shipment_name     varchar(100) not null,
    status            varchar(50)  not null,
    dest_center_id    varchar(10)  not null,
    market            varchar(10)  not null,
    dest              varchar(10)  not null,
    # shipment item
    seller_sku        varchar(50)  not null,
    fn_sku            varchar(50)  not null,
    quantity_shipped  int unsigned not null,
    quantity_received int unsigned not null,
    data              json comment 'http://docs.developer.amazonservices.com/en_US/fba_inbound/FBAInbound_Datatypes.html#InboundShipmentInfo',
    sku               varchar(50)  not null,
    size              varchar(10)  not null,
    create_time       timestamp    not null default current_timestamp,
    index (shipment_id(10)),
    index (sku, size)
);

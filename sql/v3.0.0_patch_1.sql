alter table amazon_order_item
    add column sku varchar(50) default '',
    add column size varchar(10) default '',
    add index (sku);

alter table amazon_inventory
    add column sku varchar(50) default '',
    add column size varchar(10) default '',
    add index (sku);

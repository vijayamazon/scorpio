alter table amazon_entry drop column sku, add unique (market, asin);
alter table amazon_entry_snapshot drop column sku;

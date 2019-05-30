drop view if exists vs_inventory_item;
create view vs_inventory_item as select i.market, e.asin as entry, e.sku, i.asin, i.fn_sku, i.seller_sku, i.fulfillment, i.create_date, i.in_stock_quantity as quantity from amazon_inventory i, amazon_entry e where i.market = e.market and i.seller_sku like concat('%', e.sku, '-%');

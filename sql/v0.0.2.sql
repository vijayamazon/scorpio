drop view if exists vo_store;
create view vo_store as select distinct store from amazon_entry;

drop view if exists vs_order;
create view vs_order as select i.amazon_order_id, i.amazon_order_item_id, i.asin, i.seller_sku, from_unixtime(o.data->'$.purchaseDate'/1000) as purchase_time, o.fulfillment, o.status from amazon_order o, amazon_order_item i where o.amazon_order_id = i.amazon_order_id;

drop view if exists vs_entry_order;
create view vs_entry_order as select e.asin, e.sku, i.seller_sku, o.amazon_order_id, i.amazon_order_item_id, from_unixtime(o.data->'$.purchaseDate'/1000) as purchase_time, o.fulfillment, o.status from amazon_entry e, amazon_order o, amazon_order_item i where o.amazon_order_id = i.amazon_order_id and i.seller_sku like concat('%', e.sku, '-%');

drop view if exists vs_entry_inventory;
create view vs_entry_inventory as select e.asin, e.sku, i.seller_sku, i.in_stock_quantity, i.update_time from amazon_entry e, amazon_inventory i where i.seller_sku like concat('%', e.sku, '-%');

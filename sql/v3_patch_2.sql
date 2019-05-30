alter table amazon_inventory drop column fulfillment;

drop view if exists vs_inventory_item;
create view vs_inventory_item as
select i.market,
       e.asin              as entry,
       e.sku,
       i.asin,
       i.fn_sku,
       i.seller_sku,
       i.create_date,
       i.in_stock_quantity as quantity
from amazon_inventory i,
     amazon_entry e
where i.market = e.market
  and i.seller_sku like concat('%', e.sku, '-%');

drop view if exists vs_inventory;
create view vs_inventory as
select i.market, e.asin as entry, i.create_date, sum(i.in_stock_quantity) as quantity
from amazon_inventory i,
     amazon_entry e
where i.market = e.market
  and i.seller_sku like concat('%', e.sku, '-%')
group by i.market, e.asin, i.create_date;

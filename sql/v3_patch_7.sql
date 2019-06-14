drop view if exists vs_inventory_item;
drop view if exists vs_inventory;
create view vs_inventory as
select a.market, a.parent_asin as entry, i.create_date, sum(i.in_stock_quantity) as quantity
from amazon_asin a,
     amazon_inventory i
where a.market = i.market
  and a.asin = i.asin
group by 1, 2, 3;

drop view if exists vs_order_item;
drop view if exists vs_order;
create view vs_order as
select a.market, a.parent_asin as entry, i.fulfillment, i.purchase_date, sum(i.quantity) as total_quantity
from amazon_asin a,
     amazon_order_item i
where a.market = i.market
  and a.asin = i.asin
group by 1, 2, 3, 4;

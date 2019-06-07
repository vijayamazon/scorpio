alter table amazon_inbound
    drop column dest_center_id,
    drop column dest;
alter table amazon_inbound_item
    drop column dest_center_id,
    drop column dest;

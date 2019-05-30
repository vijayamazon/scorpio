drop table if exists amazon_fulfillment;
create table amazon_fulfillment
(
  id                   bigint unsigned primary key auto_increment,
  name                 CHAR(3) not null,
  data                 tinyint(1) unsigned not null
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

insert into amazon_fulfillment values (1, 'AFN', 0), (2, 'MFN', 1);

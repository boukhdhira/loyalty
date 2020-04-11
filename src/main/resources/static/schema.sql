
create table if not exists t_account (
id serial primary key not null,
number varchar(9) unique not null,
name VARCHAR(50) NOT NULL,
user_id varchar(50) not null,
version numeric (5)
);

create table if not exists t_account_credit_card (
id serial primary key,
account_id  integer references t_account(id) on delete cascade,
number varchar(16) unique not null
 );

create table if not exists t_account_beneficiary (
id serial primary key ,
account_id integer references t_account(id) on delete cascade,
name VARCHAR(50) NOT NULL,
allocation_percentage numeric (5, 2) not null,
savings numeric (10, 2) not null
);

create table if not exists t_store (
id serial primary key ,
merchant_number varchar(10) unique not null,
name VARCHAR(50) NOT NULL,
benefits_percentage numeric (5, 2) not null,
benefits_availability_policy varchar(1) not null
);

create table if not exists t_bonus(
id serial primary key,
confirmation_number varchar(10) unique not null,
bonus_amount numeric (5, 2) not null,
bonus_date date not null,
account_number varchar(10) not null,
product_number varchar (10) not null,
shopping_amount numeric (5, 2) not null,
shopping_date date not null
);

--TODO: create script of init user /roles datatable
--INSERT INTO t_role(	id, name)	VALUES (1, 'USER');
--INSERT INTO t_role(	id, name)	VALUES (2, 'ADMIN');
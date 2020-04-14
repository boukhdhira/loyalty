
insert into t_role(	id, name) values (1, 'USER') ON CONFLICT (id) DO NOTHING;
insert into t_role(	id, name) values (2, 'ADMIN') ON CONFLICT (id) DO NOTHING;

insert into T_STORE (MERCHANT_NUMBER, NAME, BENEFITS_PERCENTAGE, BENEFITS_AVAILABILITY_POLICY)
	values ('0134567890', 'MarcketStore', 0.03, 'A') ON CONFLICT (MERCHANT_NUMBER) DO NOTHING;
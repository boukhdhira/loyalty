
insert into t_role(	id, name) values (1, 'USER') ON CONFLICT (id) DO NOTHING;
insert into t_role(	id, name) values (2, 'ADMIN') ON CONFLICT (id) DO NOTHING;


insert into t_user (username, first_name, last_name, email, account_expired, account_locked, credentials_expired, enabled, password_hash)
values ('admin', 'admin', 'admin','layaltyy.program@gmail.com', false, false, false, true, '$2a$10$s9RM04fih2B.EQ260jiFheq/.QBD7nYnng5PI5iO7FVc8BK4bu07O')
ON CONFLICT (username) DO NOTHING;

insert into user_roles (user_id, role_id) values (1, 2) ;

insert  into t_account(number, name, version, user_id) values ('999999999', 'admin account', 0, 1) ON CONFLICT (number) DO NOTHING;

insert into T_STORE (MERCHANT_NUMBER, NAME, BENEFITS_PERCENTAGE, BENEFITS_AVAILABILITY_POLICY)
	values ('0134567890', 'MarcketStore', 0.03, 'A') ON CONFLICT (MERCHANT_NUMBER) DO NOTHING;

CREATE TABLE PLUGIN_ACCOUNT
(
   id varchar(36) NOT NULL,
   name varchar(255) NOT NULL,
   comp_reg_no varchar(255),
   contact_email varchar(255),
   note varchar(1024),
   locale varchar(50),
   PRIMARY KEY (id)
);

CREATE TABLE PLUGIN_ACCOUNT_MODULE
(
   module_id varchar(36) NOT NULL,
   account_id varchar(36) NOT NULL,
   PRIMARY KEY (module_id, account_id)
);

CREATE TABLE PLUGIN_REBIRTH
(
   id varchar(36) NOT NULL,
   value varchar(3000) NOT NULL,
   password varchar(512),
   PRIMARY KEY (id)
);

ALTER TABLE PLUGIN_ACCOUNT_MODULE 
ADD CONSTRAINT fk_plugin_account_account FOREIGN KEY (account_id) REFERENCES PLUGIN_ACCOUNT (id) ON DELETE CASCADE;

create view view_account_locales as
select count(u.id), ua.value as account_id, ul.value as locale from user_entity u 
left join user_attribute ua on (ua.user_id = u.id and ua.name = 'accountNumber')
left join user_attribute ul on (ul.user_id = u.id and ul.name = 'locale')
where ul.value is not null and ul.value != 'en'
group by ua.value, ul.value;

create view view_account_locales_max as
select ls.account_id, max(ls.locale) as locale 
from view_account_locales ls
group by ls.account_id 
having ls.account_id is not null;

create view view_account_identities_locales as
select count(u.id), ua.value as account_id, ul.value as locale from user_entity u 
left join user_attribute ua on (ua.user_id = u.id and ua.name = 'accountNumber')
left join user_attribute ul on (ul.user_id = u.id and ul.name = 'locale')
where ul.value is not null
group by ua.value, ul.value;


insert into PLUGIN_ACCOUNT 
select --a.id, 
a.value as id, COALESCE(a1.value, g.name) as name, a4.value as comp_reg_no, a2.value as contact_email, a3.value as note, COALESCE(lo.locale, 'en') as locale 
from keycloak_group g 
left join group_attribute a on (a.group_id = g.id and a.name = 'accountNumber')
left join group_attribute a1 on (a1.group_id = g.id and a1.name = 'accountName')
left join group_attribute a2 on (a2.group_id = g.id and a2.name = 'contactEmail')
left join group_attribute a3 on (a3.group_id = g.id and a3.name = 'note')
left join group_attribute a4 on (a4.group_id = g.id and a4.name = 'compRegNo')
left join view_account_locales_max lo on (lo.account_id = a.value) 
where a.value is not null;
--and (a3.value not like '20200218-Migration2PROD-%' or a3.value is null);


create index ix_user_attribute_name_value on user_attribute(name, value);


---

UPDATE 
  PLUGIN_ACCOUNT a 
SET 
  locale = lca.locale 
FROM (select account_id, locale from view_account_locales_max) as lca
WHERE a.id = lca.account_id ;

---

select a.id, a.locale, lo.locale from PLUGIN_ACCOUNT a
left join view_account_locales_max lo on (lo.account_id=a.id)
where a.locale <> lo.locale or a.locale is null

--


delete from user_group_membership where group_id in (
--delete from group_attribute where group_id in (
select g.id from keycloak_group g
left join group_attribute a3 on (a3.group_id = g.id and a3.name = 'note')
where a3.value like '20200218-Migration2PROD-%');

delete from keycloak_group g
where g.name in (
select name || ' ' || '(' || id || ')' 
from plugin_account 
where note like '20200218-Migration2PROD-%');

left join group_attribute a3 on (a3.group_id = g.id and a3.name = 'note')
where a3.value like '20200218-Migration2PROD-%');



select count(u.id) as cnt, a.id from plugin_account a
left join user_attribute u on (u.name = 'accountNumber' and u.value = a.id)
group by a.id
having count(u.id) = 0;


select ue.*, u.value, a.id from user_entity ue
left join user_attribute u on (u.name = 'accountNumber' and u.user_id = ue.id)
left join plugin_account a on (a.id = u.value)
where a is null;




select ue.id, c.value, u.value, n.value, a.id, ue.enabled from user_entity ue
left join user_attribute u on (u.name = 'accountNumber' and u.user_id = ue.id)
left join user_attribute c on (c.name = 'contactNumber' and c.user_id = ue.id)
left join user_attribute n on (n.name = 'contactNumber' and n.user_id = ue.id)
left join plugin_account a on (a.id = u.value)
where u.value = '73699' 

select r.name, u.* from user_role_mapping u right join keycloak_role r on (r.id = u.role_id)
where u.user_id = 'f91ebe0e-4bec-4aae-b9fd-21a9da2f2aeb'
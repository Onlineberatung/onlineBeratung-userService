alter table userservice.session
    add language_code varchar(2) not null default 'de' after agency_id;

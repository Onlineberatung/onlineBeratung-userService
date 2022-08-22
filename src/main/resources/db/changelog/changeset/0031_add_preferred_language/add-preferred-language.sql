alter table userservice.consultant
    add language_code varchar(2) default 'de' not null after language_formal;

alter table userservice.user
    add language_code varchar(2) default 'de' not null after language_formal;

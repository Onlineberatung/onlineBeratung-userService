alter table userservice.consultant
    add encourage_2fa bit default true not null after language_formal;

alter table userservice.user
    add encourage_2fa bit default true not null after language_formal;

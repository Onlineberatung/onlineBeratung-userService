create table userservice.language
(
    language_code varchar(2)  not null,
    consultant_id varchar(36) not null,
    primary key (consultant_id, language_code)
) engine = InnoDB
  charset = utf8
  collate = utf8_unicode_ci;

alter table userservice.language
    add constraint language_id_consultant_constraint
        foreign key (consultant_id) references consultant (consultant_id);

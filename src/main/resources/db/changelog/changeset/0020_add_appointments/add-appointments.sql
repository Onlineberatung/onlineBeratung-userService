create table userservice.appointment
(
    id            char(36)    not null,
    datetime      timestamp   not null,
    description   varchar(300),
    status        varchar(7)  not null,
    consultant_id varchar(36) not null,
    primary key (id)
) engine = InnoDB
  charset = utf8
  collate = utf8_unicode_ci;

alter table userservice.appointment
    add constraint appointment_consultant_constraint
        foreign key (consultant_id) references userservice.consultant (consultant_id)
            on delete cascade on update cascade;

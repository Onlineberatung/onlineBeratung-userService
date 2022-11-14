alter table userservice.session
    add is_consultant_directly_set bit default false not null after is_monitoring;

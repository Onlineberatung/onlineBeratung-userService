alter table userservice.consultant
    add constraint idx_first_name_last_name_email unique (first_name, last_name, email);

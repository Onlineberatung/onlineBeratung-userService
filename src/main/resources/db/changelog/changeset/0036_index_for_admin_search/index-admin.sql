alter table userservice.admin
    add constraint idx_username_first_name_last_name_email unique (username, first_name, last_name, email);

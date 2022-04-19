DROP INDEX IF EXISTS idx_first_name_last_name_email ON userservice.consultant;

alter table userservice.consultant
    add constraint idx_first_name_last_name_email_delete_date
        unique (first_name, last_name, email, delete_date);

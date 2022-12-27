alter table userservice.consultant
    add terms_and_conditions_confirmation datetime after language_formal;
alter table userservice.consultant
    add data_privacy_confirmation datetime after language_formal;

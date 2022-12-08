alter table userservice.user
    add terms_and_conditions_confirmation datetime after language_formal;
alter table userservice.user
    add data_privacy_confirmation datetime after language_formal;

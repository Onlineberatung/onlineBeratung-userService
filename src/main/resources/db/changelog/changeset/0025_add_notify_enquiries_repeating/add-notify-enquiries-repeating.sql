alter table userservice.consultant
    add notify_enquiries_repeating bit default true not null after encourage_2fa;

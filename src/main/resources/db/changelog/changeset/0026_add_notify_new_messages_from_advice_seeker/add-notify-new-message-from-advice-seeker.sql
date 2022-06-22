alter table userservice.consultant
    add notify_new_chat_message_from_advice_seeker bit default true not null after notify_enquiries_repeating;

alter table userservice.consultant
    add notify_new_feedback_message_from_advice_seeker bit default true not null after notify_new_chat_message_from_advice_seeker;

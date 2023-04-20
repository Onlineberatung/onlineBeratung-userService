-- initially all notifications_settings were set to true for consultants having email (see prev migration)
-- so we just have to consider setting some of them to false during this migration
UPDATE `userservice`.`consultant`
SET notifications_settings = REPLACE(notifications_settings, "'initialEnquiryNotificationEnabled': 'false'", "'initialEnquiryNotificationEnabled': 'true'")
where notify_enquiries_repeating = 0;

UPDATE `userservice`.`consultant`
SET notifications_settings = REPLACE(notifications_settings, "'newChatMessageNotificationEnabled': 'false'", "'newChatMessageNotificationEnabled': 'true'")
where notify_new_chat_message_from_advice_seeker = 0 AND notify_new_feedback_message_from_advice_seeker = 0;
ALTER TABLE `userservice`.`consultant`
    ADD COLUMN `notifications_enabled` tinyint(4) unsigned NOT NULL DEFAULT '0';

ALTER TABLE `userservice`.`consultant`
    ADD COLUMN `notifications_settings` VARCHAR(4000) NULL DEFAULT '';

UPDATE `userservice`.`consultant`
SET notifications_settings = "{'initialEnquiryNotificationEnabled': 'true','newChatMessageNotificationEnabled': 'true', 'reassignmentNotificationEnabled': 'true','appointmentNotificationEnabled': 'true'}",
    notifications_enabled = 1
WHERE email is not NULL and email <> '';

ALTER TABLE `userservice`.`user`
    ADD COLUMN `notifications_enabled` tinyint(4) unsigned NOT NULL DEFAULT '0';

ALTER TABLE `userservice`.`user`
    ADD COLUMN `notifications_settings` VARCHAR(4000) NULL DEFAULT '';

UPDATE `userservice`.`user`
SET notifications_settings = "{'newChatMessageNotificationEnabled': 'true', 'reassignmentNotificationEnabled': 'true', 'appointmentNotificationEnabled': 'true'}",
    notifications_enabled = 1
WHERE email is not NULL and email <> '';
ALTER TABLE `userservice`.`chat`
    ADD COLUMN `hint_message` VARCHAR(300) NULL DEFAULT NULL;
ALTER TABLE `userservice`.`chat`
    ADD COLUMN `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP);
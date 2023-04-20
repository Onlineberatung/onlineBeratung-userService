ALTER TABLE `userservice`.`consultant`
    DROP COLUMN `notifications_enabled`;

ALTER TABLE `userservice`.`consultant`
    DROP COLUMN `notifications_settings`;

ALTER TABLE `userservice`.`user`
    DROP COLUMN `notifications_enabled`;

ALTER TABLE `userservice`.`user`
    DROP COLUMN `notifications_settings`;
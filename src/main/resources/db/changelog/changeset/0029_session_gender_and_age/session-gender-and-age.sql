ALTER TABLE `userservice`.`session`
    ADD COLUMN `user_gender` VARCHAR(50) NULL DEFAULT NULL;

ALTER TABLE `userservice`.`session`
    ADD COLUMN `user_age` INTEGER NULL DEFAULT NULL;

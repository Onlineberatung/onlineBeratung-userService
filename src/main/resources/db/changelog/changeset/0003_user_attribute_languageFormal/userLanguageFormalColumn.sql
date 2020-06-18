ALTER TABLE `userservice`.`user`
ADD COLUMN `language_formal` tinyint(4) NOT NULL DEFAULT 0 AFTER `rc_user_id`;
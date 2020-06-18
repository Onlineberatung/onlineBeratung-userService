ALTER TABLE `userservice`.`consultant`
ADD COLUMN `language_formal` tinyint(4) NOT NULL DEFAULT 1 AFTER `rc_user_id`;
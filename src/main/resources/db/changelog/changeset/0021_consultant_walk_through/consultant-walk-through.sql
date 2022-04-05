ALTER TABLE `userservice`.`consultant`
ADD COLUMN `walk_through_enabled` tinyint(4) NOT NULL DEFAULT 1 AFTER `status`;

UPDATE `userservice`.`consultant` SET `walk_through_enabled` = true;
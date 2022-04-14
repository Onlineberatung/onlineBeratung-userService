ALTER TABLE `userservice`.`consultant`
ADD COLUMN `status` varchar(21) NULL DEFAULT NULL AFTER `encourage_2fa`;
UPDATE `userservice`.`consultant` SET `status` = 'CREATED';
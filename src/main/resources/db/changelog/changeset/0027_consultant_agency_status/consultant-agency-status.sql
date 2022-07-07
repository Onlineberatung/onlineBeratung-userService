ALTER TABLE `userservice`.`consultant_agency`
    ADD COLUMN `status` varchar(11) NULL DEFAULT NULL;
UPDATE `userservice`.`consultant_agency`
SET `status` = 'CREATED';

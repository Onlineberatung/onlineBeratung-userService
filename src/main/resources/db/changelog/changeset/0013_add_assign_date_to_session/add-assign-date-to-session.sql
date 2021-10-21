ALTER TABLE `userservice`.`session`
    ADD `assign_date` datetime DEFAULT NULL
    AFTER `message_date`;
UPDATE `userservice`.`session`
    SET `assign_date` = `update_date`
    WHERE status = 2;

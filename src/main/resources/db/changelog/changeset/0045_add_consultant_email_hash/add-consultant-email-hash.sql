ALTER TABLE `userservice`.`consultant`
ADD COLUMN emailHash VARCHAR(32);

UPDATE `userservice`.`consultant`
SET emailHash = MD5(email);
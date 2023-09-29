ALTER TABLE `userservice`.`consultant`
ADD COLUMN email_hash VARCHAR(32);

UPDATE `userservice`.`consultant`
SET email_hash = MD5(email);
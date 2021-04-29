ALTER TABLE `userservice`.`session`
ADD `registration_type` VARCHAR NOT NULL DEFAULT 'REGISTERED' AFTER `consulting_type`;

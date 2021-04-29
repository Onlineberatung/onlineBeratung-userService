ALTER TABLE `userservice`.`session`
ADD `registration_type` varchar(255) COLLATE 'utf8_unicode_ci' NOT NULL DEFAULT 'REGISTERED'
    AFTER `consulting_type`;

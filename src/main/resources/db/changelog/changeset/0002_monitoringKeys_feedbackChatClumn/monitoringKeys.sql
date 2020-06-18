ALTER TABLE `userservice`.`session_monitoring`
ADD INDEX `INDEX` (`session_id`);

ALTER TABLE `userservice`.`session_monitoring`
ADD CONSTRAINT `session_monitoring_fk_session_id` 
FOREIGN KEY (`session_id`) 
REFERENCES `userservice`.`session` (`id`) 
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `userservice`.`session_monitoring_option`
ADD INDEX `INDEX` (`session_id`);

ALTER TABLE `userservice`.`session_monitoring_option`
ADD CONSTRAINT `session_monitoring_option_fk_session_id`
FOREIGN KEY (`session_id`) 
REFERENCES `userservice`.`session` (`id`) 
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `userservice`.`session_monitoring_option`
ADD CONSTRAINT `session_monitoring_option_fk_unique`
FOREIGN KEY (`session_id`, `monitoring_type`, `monitoring_key_name`) 
REFERENCES `userservice`.`session_monitoring` (`session_id`, `type`, `key_name`) 
ON DELETE RESTRICT ON UPDATE CASCADE;
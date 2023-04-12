ALTER TABLE `userservice`.`session_monitoring`
DROP INDEX `INDEX`;

ALTER TABLE `userservice`.`session_monitoring`
DROP FOREIGN KEY `session_monitoring_fk_session_id`;

ALTER TABLE `userservice`.`session_monitoring_option`
DROP INDEX `INDEX`;

ALTER TABLE `userservice`.`session_monitoring_option`
DROP FOREIGN KEY `session_monitoring_option_fk_session_id`;

ALTER TABLE `userservice`.`session_monitoring_option`
DROP FOREIGN KEY `session_monitoring_option_fk_unique`;
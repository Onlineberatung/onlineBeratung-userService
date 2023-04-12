ALTER TABLE `userservice`.`session`
ADD COLUMN `is_monitoring` tinyint(4) NOT NULL DEFAULT 0 AFTER `is_team_session`;
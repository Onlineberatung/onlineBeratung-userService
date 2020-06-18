ALTER TABLE `userservice`.`chat`
ADD COLUMN `duration` smallint NOT NULL AFTER `start_date`;
ALTER TABLE `userservice`.`chat`
ADD COLUMN `rc_group_id` varchar(255) NULL AFTER `consultant_id_owner`;
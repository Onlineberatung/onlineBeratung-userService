ALTER TABLE `userservice`.`consultant`
ADD `delete_date` datetime NULL DEFAULT NULL AFTER `id_old`;

ALTER TABLE `userservice`.`user`
ADD `delete_date` datetime NULL DEFAULT NULL AFTER `id_old`;
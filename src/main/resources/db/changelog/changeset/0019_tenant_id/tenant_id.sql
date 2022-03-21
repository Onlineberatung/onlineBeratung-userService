ALTER TABLE `userservice`.`user`
ADD COLUMN `tenant_id` bigint(21) NULL DEFAULT NULL AFTER `user_id`;

ALTER TABLE `userservice`.`consultant`
ADD COLUMN `tenant_id` bigint(21) NULL DEFAULT NULL AFTER `consultant_id`;

ALTER TABLE `userservice`.`session`
ADD COLUMN `tenant_id` bigint(21) NULL DEFAULT NULL AFTER `Id`;

ALTER TABLE `userservice`.`consultant_agency`
ADD COLUMN `tenant_id` bigint(21) NULL DEFAULT NULL AFTER `id`;
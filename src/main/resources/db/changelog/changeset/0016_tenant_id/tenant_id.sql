ALTER TABLE `userservice`.`user`
ADD COLUMN `tenant_id` bigint(21) NULL DEFAULT NULL AFTER `id`;

ALTER TABLE `userservice`.`consultant`
ADD COLUMN `tenant_id` bigint(21) NULL DEFAULT NULL AFTER `id`;

ALTER TABLE `userservice`.`session`
ADD COLUMN `tenant_id` bigint(21) NULL DEFAULT NULL AFTER `id`;
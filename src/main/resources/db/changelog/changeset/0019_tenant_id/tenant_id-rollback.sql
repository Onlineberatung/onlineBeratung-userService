ALTER TABLE `userservice`.`user`
DROP `tenant_id`;
ALTER TABLE `userservice`.`consultant`
DROP `tenant_id`;
ALTER TABLE `userservice`.`session`
DROP `tenant_id`;
ALTER TABLE `consultant_agency`.`session`
DROP `tenant_id`;


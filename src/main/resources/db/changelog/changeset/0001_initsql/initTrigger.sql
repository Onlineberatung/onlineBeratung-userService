CREATE TRIGGER `userservice`.`user_update` BEFORE UPDATE ON `userservice`.`user` FOR EACH ROW BEGIN
set new.update_date=utc_timestamp();
END //
CREATE TRIGGER `userservice`.`consultant_update` BEFORE UPDATE ON `userservice`.`consultant` FOR EACH ROW BEGIN
set new.update_date=utc_timestamp();
END //
CREATE TRIGGER `userservice`.`consultant_agency_update` BEFORE UPDATE ON `userservice`.`consultant_agency` FOR EACH ROW BEGIN
set new.update_date=utc_timestamp();
END //
CREATE TRIGGER `userservice`.`session_update` BEFORE UPDATE ON `userservice`.`session` FOR EACH ROW BEGIN
set new.update_date=utc_timestamp();
END //
CREATE TRIGGER `userservice`.`session_data_update` BEFORE UPDATE ON `userservice`.`session_data` FOR EACH ROW BEGIN
set new.update_date=utc_timestamp();
END //
CREATE TRIGGER `userservice`.`session_monitoring_update` BEFORE UPDATE ON `userservice`.`session_monitoring` FOR EACH ROW BEGIN
set new.update_date=utc_timestamp();
END //
CREATE TRIGGER `userservice`.`session_monitoring_option_update` BEFORE UPDATE ON `userservice`.`session_monitoring_option` FOR EACH ROW BEGIN
set new.update_date=utc_timestamp();
END //
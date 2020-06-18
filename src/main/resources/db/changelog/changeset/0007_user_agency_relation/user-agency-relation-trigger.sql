CREATE TRIGGER `userservice`.`user_agency_update` BEFORE UPDATE ON `userservice`.`user_agency` FOR EACH ROW BEGIN
set new.update_date=utc_timestamp();
END //
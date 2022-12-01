CREATE TRIGGER `userservice`.`admin_update` BEFORE UPDATE ON `userservice`.`admin` FOR EACH ROW BEGIN
    set new.update_date=utc_timestamp();
END //
CREATE TRIGGER `userservice`.`admin_agency_update` BEFORE UPDATE ON `userservice`.`admin_agency` FOR EACH ROW BEGIN
    set new.update_date=utc_timestamp();
END //

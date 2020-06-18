CREATE TRIGGER `userservice`.`chat_update` BEFORE UPDATE ON `userservice`.`chat` FOR EACH ROW BEGIN
set new.update_date=utc_timestamp();
END //
CREATE TRIGGER `userservice`.`chat_agency_update` BEFORE UPDATE ON `userservice`.`chat_agency` FOR EACH ROW BEGIN
set new.update_date=utc_timestamp();
END //
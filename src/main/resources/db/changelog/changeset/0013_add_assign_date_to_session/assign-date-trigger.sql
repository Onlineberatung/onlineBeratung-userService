CREATE TRIGGER `userservice`.`assign_date_update` BEFORE UPDATE ON `userservice`.`session` FOR EACH ROW BEGIN
    IF OLD.assign_date IS NULL AND OLD.status = 1 AND NEW.status = 2 THEN
        SET NEW.assign_date=utc_timestamp();
    END IF;
END //

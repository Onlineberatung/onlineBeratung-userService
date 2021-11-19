CREATE TRIGGER `userservice`.`user_mobile_token_update`
    BEFORE UPDATE ON `userservice`.`user_mobile_token`
    FOR EACH ROW BEGIN set new.update_date=utc_timestamp();
END //
CREATE TRIGGER `userservice`.`consultant_mobile_token_update`
    BEFORE UPDATE ON `userservice`.`consultant_mobile_token`
    FOR EACH ROW BEGIN set new.update_date=utc_timestamp();
END //

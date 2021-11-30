ALTER TABLE `userservice`.`session`
    ADD `is_peer_chat` tinyint(4) unsigned NOT NULL DEFAULT '0'
    AFTER `rc_feedback_group_id`;
UPDATE `userservice`.`session`
    SET `is_peer_chat` = '1'
    WHERE `rc_feedback_group_id` IS NOT NULL;

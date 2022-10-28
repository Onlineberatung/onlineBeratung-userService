CREATE TABLE `userservice`.`session_topic` (
    `id` bigint(21) NOT NULL,
    `session_id` bigint(21) unsigned NOT NULL,
    `topic_id` bigint(21) unsigned NOT NULL,
    `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
    `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
    PRIMARY KEY (`id`),
    KEY `session_id` (`session_id`),
    CONSTRAINT `session_topic_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `userservice`.`session` (`Id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE SEQUENCE `userservice`.`sequence_session_topic`
INCREMENT BY 1
MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 10;

ALTER TABLE `userservice`.`session`
    ADD COLUMN `counselling_relation` VARCHAR(50) NULL DEFAULT NULL;

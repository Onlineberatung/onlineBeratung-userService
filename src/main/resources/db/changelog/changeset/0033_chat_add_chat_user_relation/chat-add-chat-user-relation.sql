CREATE TABLE `userservice`.`chat_user` (
    `id` bigint(21) NOT NULL,
    `chat_id` bigint(21) unsigned NOT NULL,
    `user_id` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
    `create_date` datetime NOT NULL DEFAULT UTC_TIMESTAMP(),
    `update_date` datetime NOT NULL DEFAULT UTC_TIMESTAMP(),
    PRIMARY KEY (`id`),
    KEY `chat_id` (`chat_id`),
    CONSTRAINT `chat_user_ibfk_1` FOREIGN KEY (`chat_id`) REFERENCES `userservice`.`chat` (`Id`) ON UPDATE CASCADE,
    CONSTRAINT `chat_user_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `userservice`.`user` (`user_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE SEQUENCE `userservice`.`sequence_chat_user`
INCREMENT BY 1
MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 10;

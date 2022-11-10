CREATE TABLE `userservice`.`user_mobile_token`
(
    `id`               bigint(21) unsigned NOT NULL,
    `user_id`          varchar(36) COLLATE utf8_unicode_ci NOT NULL,
    `mobile_app_token` longtext                            NOT NULL UNIQUE,
    `create_date`      datetime                            NOT NULL DEFAULT (UTC_TIMESTAMP),
    `update_date`      datetime                            NOT NULL DEFAULT (UTC_TIMESTAMP),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE SEQUENCE `userservice`.`sequence_user_mobile_token`
    INCREMENT BY 1
    MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 100;

CREATE TABLE `userservice`.`consultant_mobile_token`
(
    `id`               bigint(21) unsigned NOT NULL,
    `consultant_id`    varchar(36) COLLATE utf8_unicode_ci NOT NULL,
    `mobile_app_token` longtext                            NOT NULL UNIQUE,
    `create_date`      datetime                            NOT NULL DEFAULT (UTC_TIMESTAMP),
    `update_date`      datetime                            NOT NULL DEFAULT (UTC_TIMESTAMP),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`consultant_id`) REFERENCES `consultant` (`consultant_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE SEQUENCE `userservice`.`sequence_consultant_mobile_token`
    INCREMENT BY 1
    MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 100;

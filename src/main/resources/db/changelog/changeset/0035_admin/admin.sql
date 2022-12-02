CREATE TABLE IF NOT EXISTS `userservice`.`admin` (
    `admin_id` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
    `tenant_id` bigint(21) NULL DEFAULT NULL,
    `username` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
    `first_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
    `last_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
    `email` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
    `type` varchar(6) NOT NULL,
    `rc_user_id` varchar(255) NULL,
    `id_old` bigint(21) NULL,
    `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
    `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
PRIMARY KEY (`admin_id`),
UNIQUE `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `userservice`.`admin_agency` (
    `id` bigint(21) unsigned NOT NULL,
    `admin_id` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
    `agency_id` bigint(21) unsigned NOT NULL,
    `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
    `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
    PRIMARY KEY (`id`),
    KEY `admin_id` (`admin_id`),
    CONSTRAINT `admin_agency_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE SEQUENCE IF NOT EXISTS `userservice`.`sequence_admin_agency`
    INCREMENT BY 1
    MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 100;


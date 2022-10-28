CREATE TABLE `userservice`.`chat` (
	`id` bigint(21) unsigned NOT NULL,
    `topic` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
    `consulting_type` tinyint(4) unsigned NOT NULL,
	`initial_start_date` datetime NOT NULL,
	`start_date` datetime NOT NULL,
	`is_repetitive` tinyint(1) unsigned NOT NULL DEFAULT '0',
	`chat_interval` varchar(255) COLLATE utf8_unicode_ci NULL,
	`is_active` tinyint(1) unsigned NOT NULL DEFAULT '0',
	`max_participants` tinyint(4) unsigned NULL,
	`consultant_id_owner` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
	`create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  	`update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
	PRIMARY KEY (`id`),
	KEY `consultant_id_owner` (`consultant_id_owner`),
  	CONSTRAINT `chat_consultant_ibfk_1` FOREIGN KEY (`consultant_id_owner`) REFERENCES `consultant` (`consultant_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE SEQUENCE `userservice`.`sequence_chat`
INCREMENT BY 1
MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 100;
CREATE TABLE `userservice`.`chat_agency` (
  `id` bigint(21) unsigned NOT NULL,
  `chat_id` bigint(21) unsigned NOT NULL,
  `agency_id` bigint(21) unsigned NOT NULL,
  `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  PRIMARY KEY (`id`),
  KEY `chat_id` (`chat_id`),
  CONSTRAINT `chat_agency_ibfk_1` FOREIGN KEY (`chat_id`) REFERENCES `chat` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE SEQUENCE `userservice`.`sequence_chat_agency`
INCREMENT BY 1
MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 100;
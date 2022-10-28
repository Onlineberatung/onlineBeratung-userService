CREATE TABLE `userservice`.`user` (
  `user_id` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `id_old` bigint(21) unsigned NULL,
  `username` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `rc_user_id` varchar(255) NULL,
  `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `userservice`.`consultant` (
  `consultant_id` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `username` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `first_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `last_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `is_team_consultant` tinyint(4) unsigned NOT NULL DEFAULT '0',
  `is_absent` tinyint(4) unsigned NOT NULL DEFAULT 0,
  `absence_message` longtext COLLATE utf8_unicode_ci DEFAULT NULL,
  `rc_user_id` varchar(255) NULL,
  `id_old` bigint(21) NULL,
  `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  PRIMARY KEY (`consultant_id`),
  UNIQUE `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `userservice`.`consultant_agency` (
  `id` bigint(21) unsigned NOT NULL,
  `consultant_id` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `agency_id` bigint(21) unsigned NOT NULL,
  `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  PRIMARY KEY (`id`),
  KEY `consultant_id` (`consultant_id`),
  CONSTRAINT `consultant_agency_ibfk_1` FOREIGN KEY (`consultant_id`) REFERENCES `consultant` (`consultant_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE SEQUENCE `userservice`.`sequence_consultant_agency`
INCREMENT BY 1
MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 100;
CREATE TABLE `userservice`.`session` (
  `id` bigint(21) unsigned NOT NULL,
  `user_id` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `consultant_id` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `consulting_type` tinyint(4) NOT NULL,
  `message_date` datetime DEFAULT NULL,
  `postcode` varchar(5) COLLATE utf8_unicode_ci NOT NULL,
  `agency_id` bigint(21) unsigned DEFAULT NULL,
  `rc_group_id` varchar(255) NULL,
  `status` tinyint(4) NOT NULL,
  `is_team_session` tinyint(4) NOT NULL DEFAULT '0',
  `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  PRIMARY KEY (`id`),
  KEY `index_consultant_id_status` (`consultant_id`,`status`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `session_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON UPDATE CASCADE,
  CONSTRAINT `session_ibfk_2` FOREIGN KEY (`consultant_id`) REFERENCES `consultant` (`consultant_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE SEQUENCE `userservice`.`sequence_session`
INCREMENT BY 1
MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 100;
CREATE TABLE `userservice`.`session_data` (
  `id` bigint(21) unsigned NOT NULL,
  `session_id` bigint(21) unsigned NOT NULL,
  `type` tinyint(4) NOT NULL,
  `key_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `value` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_type_key_name` (`session_id`, `type`,`key_name`),
  KEY `session_id` (`session_id`),
  CONSTRAINT `session_data_ibfk_2` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE SEQUENCE `userservice`.`sequence_session_data`
INCREMENT BY 1
MINVALUE = 0
NOMAXVALUE
START WITH 0
CACHE 100;
CREATE TABLE `userservice`.`session_monitoring` (
  `session_id` bigint(21) unsigned NOT NULL,
  `type` tinyint(4) unsigned NOT NULL,
  `key_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `value` tinyint(1) DEFAULT NULL,
  `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  PRIMARY KEY (`session_id`,`type`,`key_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `userservice`.`session_monitoring_option` (
  `session_id` bigint(21) unsigned NOT NULL,
  `monitoring_type` tinyint(4) unsigned NOT NULL,
  `monitoring_key_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `key_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `value` tinyint(1) DEFAULT NULL,
  `create_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  `update_date` datetime NOT NULL DEFAULT (UTC_TIMESTAMP),
  PRIMARY KEY (`session_id`,`monitoring_type`,`monitoring_key_name`,`key_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
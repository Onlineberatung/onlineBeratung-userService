INSERT INTO `chat` (`id`, `topic`, `consulting_type`, `initial_start_date`, `start_date`, `duration`, `is_repetitive`, `chat_interval`, `is_active`, `max_participants`, `consultant_id_owner`, `rc_group_id`) VALUES
    (0,	'TestV2 1',	null, '2022-08-24 10:45:00', '2022-08-24 10:45:00',	60,	0, NULL, 0, NULL, '0b3b1cc6-be98-4787-aa56-212259d811b9', 'x'),
    (1,	'TestV2 2',	null, '2022-08-25 11:00:00', '2022-08-25 11:00:00',	60,	0, NULL, 0, NULL, '0b3b1cc6-be98-4787-aa56-212259d811b9', 'xx'),
    (2,	'TestV2 3',	null, '2022-08-26 10:00:00', '2022-08-26 10:00:00',	60,	0, NULL, 0, NULL, '1293c11a-1b3e-47b8-a16e-ce0a8f055689', 'xxx'),
    (3,	'TestV2 4',	null, '2022-08-26 10:00:00', '2022-08-26 10:00:00',	60,	0, NULL, 0, NULL, '1293c11a-1b3e-47b8-a16e-ce0a8f055689', 'xxxx');

INSERT INTO `chat_agency` (`id`, `chat_id`, `agency_id`) VALUES
    (0, 0, 1731), (1, 0, 1),
    (2, 1, 1731), (3, 1, 1),
    (4, 2, 22);

INSERT INTO `user_agency` (`id`, `user_id`, `agency_id`) VALUES
    (0, '017cac2a-2086-47eb-9f8e-40547dfa2fd5', 22),
    (1, '015d013d-95e7-4e91-85b5-12cdb3d317f3', 1731);

INSERT INTO `user_chat` (`id`, `user_id`, `chat_id`) VALUES
    (0,	'015d013d-95e7-4e91-85b5-12cdb3d317f3',	0),
    (1,	'015d013d-95e7-4e91-85b5-12cdb3d317f3',	1),
    (2,	'017cac2a-2086-47eb-9f8e-40547dfa2fd5',	2);




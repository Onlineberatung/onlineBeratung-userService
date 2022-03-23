INSERT INTO CONSULTANT (`consultant_id`, `tenant_id`, `encourage_2fa`, `username`,`first_name`,`last_name`,`email`, `is_team_consultant`, `is_absent`,`language_formal`)
VALUES ('0b3b1cc6-be98-4787-aa56-212259d811b8', '1', true, 'enc.MVWWSZ3SMF2GS33OFV2GKYRR', 'test1', 'test1', 'test1',1, false, false);
INSERT INTO CONSULTANT (`consultant_id`, `tenant_id`, `encourage_2fa`, `username`,`first_name`,`last_name`,`email`, `is_team_consultant`, `is_absent`, `language_formal`)
VALUES ('0b3b1cc6-be98-4787-aa56-212259d811b7', '1', true, 'enc.MVWWSZ3SMF2GS33OFV2GKYPP', 'test2', 'test2', 'test2',0, false, false);

INSERT INTO CONSULTANT_AGENCY (`id`, `tenant_id`, `consultant_id`, `agency_id`)
VALUES (100, '1', '0b3b1cc6-be98-4787-aa56-212259d811b8', 1);
INSERT INTO CONSULTANT_AGENCY (`id`, `tenant_id`, `consultant_id`, `agency_id`)
VALUES (101, '1', '0b3b1cc6-be98-4787-aa56-212259d811b7', 1);

-- -----------------------------------------------------
-- update procedure updateStats
-- -----------------------------------------------------

DROP procedure IF EXISTS `updateStats`;

DELIMITER $$
CREATE PROCEDURE `updateStats`()
begin

	--  add new entry with current timestamp
	INSERT INTO 
		stat_entry (entrydate) values (NOW());

	--  get current count of entries in table stat_entry
	SET @id = (select max(stat_entry_id) from stat_entry);
	--  the tool specific logic follows, inserting entries in table stat_value using the same id
    --  BEGIN OF TOOL SPECIFIC LOGIC ------------
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'policies',(SELECT count(name) FROM policy));
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'templates',(SELECT count(name) FROM consent_template));
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'modules_without_versions',(SELECT count(name) FROM module));
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'modules_with_versions',(SELECT count(distinct name) FROM module));
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'informed_consents',(SELECT count(*) FROM consent));
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'signed_policies',(SELECT count(*) FROM signed_policy));
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'withdrawals',(SELECT count(*) as num_withdrawals FROM (select CONSENT_VIRTUAL_PERSON_ID, CONSENT_DATE, CT_NAME from signed_policy where status=5 group by CONSENT_VIRTUAL_PERSON_ID, CONSENT_DATE, CT_NAME) as withdrawals));        

	--  END OF TOOL SPECIFIC LOGIC ------------
	--  show and return data sets
	SELECT t1.stat_entry_id as id, t1.entrydate as timestamp, t2.stat_attr as attribut, t2.stat_value as value 
		FROM stat_entry AS t1, stat_value AS t2
		WHERE t1.stat_entry_id = t2.stat_value_id;
end$$

DELIMITER ;




-- update previous stats

SET SQL_SAFE_UPDATES = 0;

UPDATE `stat_value`
SET
`stat_attr` = 'withdrawals'
WHERE `stat_attr` = 'revocations';

SET SQL_SAFE_UPDATES = 1;

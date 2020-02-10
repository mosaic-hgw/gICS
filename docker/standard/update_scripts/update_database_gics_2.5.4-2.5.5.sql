CREATE  TABLE IF NOT EXISTS `stat_entry` (
  `STAT_ENTRY_ID` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `ENTRYDATE` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`STAT_ENTRY_ID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `stat_value`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `stat_value` (
  `stat_value_id` BIGINT(20) NULL DEFAULT NULL ,
  `stat_value` VARCHAR(255) NULL DEFAULT NULL ,
  `stat_attr` VARCHAR(50) NULL DEFAULT NULL ,
  INDEX `FK_stat_value_stat_value_id` (`stat_value_id` ASC),
  CONSTRAINT `FK_stat_value_stat_value_id`
    FOREIGN KEY (`stat_value_id`)
    REFERENCES `stat_entry` (`STAT_ENTRY_ID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- procedure updateStats
-- -----------------------------------------------------

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
	INSERT INTO stat_value (stat_value_id,stat_attr,stat_value) values (@id, 'revocations',(SELECT count(*) as num_revocations FROM (select * from signed_policy where status=5 group by CONSENT_VIRTUAL_PERSON_ID, CONSENT_DATE, CT_NAME) as revocations));        
	--  END OF TOOL SPECIFIC LOGIC ------------

	--  show and return data sets
	SELECT t1.stat_entry_id as id, t1.entrydate as timestamp, t2.stat_attr as attribut, t2.stat_value as value 
		FROM stat_entry AS t1, stat_value AS t2
		WHERE t1.stat_entry_id = t2.stat_value_id;
end$$
DELIMITER ;

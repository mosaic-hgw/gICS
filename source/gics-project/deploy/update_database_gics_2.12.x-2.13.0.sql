-- attention! dependent on your sql-client it may be neccessary to change "modify" with "change column"

delete from `sequence` where `seq_name` = 'statistic_index';
insert into `sequence` values('statistic_index', (select max(`STAT_ENTRY_ID`) from `stat_entry`));
update `sequence`set `seq_count` = 0 where `seq_name` = 'statistic_index' and `seq_count` IS NULL;
alter table `stat_value` modify `stat_value` bigint(20);
alter table `stat_value` modify `stat_attr` varchar(255);
alter table `stat_entry` modify `ENTRYDATE` timestamp(3);

-- replace old variants of empty digital signatures
UPDATE signature
SET SIGNATURESCANBASE64 = NULL
WHERE SIGNATURESCANBASE64 IN ("no signature", "no real signature");

ALTER TABLE `domain` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `consent` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `consent_template` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `module` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `module_consent_template` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `policy` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `free_text_def` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `free_text_val` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `qc` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `qc_hist` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `signature` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `signer_id` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
ALTER TABLE `signer_id_type` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";

update `domain` set `FHIR_ID` = UUID();
update `consent` set `FHIR_ID` = UUID();
update `consent_template` set `FHIR_ID` = UUID();
update `module` set `FHIR_ID` = CONCAT(SUBSTRING(UUID(), 2, 8), SUBSTRING(UUID(), 1, 1), SUBSTRING(UUID(), 10));
update `module_consent_template` set `FHIR_ID` = UUID();
update `policy` set `FHIR_ID` = UUID();
update `free_text_def` set `FHIR_ID` = UUID();
update `free_text_val` set `FHIR_ID` = UUID();
update `qc` set `FHIR_ID` = UUID();
update `qc_hist` set `FHIR_ID` = UUID();
update `signature` set `FHIR_ID` = UUID();
update `signer_id` set `FHIR_ID` = UUID();
update `signer_id_type` set `FHIR_ID` = UUID();

ALTER TABLE `signature` ADD `SIGNATUREPLACE` VARCHAR(255);

-- -----------------------------------
-- consent_template_scan
-- -----------------------------------
-- Create new consent_template_scan table
CREATE TABLE consent_template_scan
(
    ID varchar(255) PRIMARY KEY NOT NULL,
    SCANBASE64 longtext,
    FILETYPE varchar(255)
);

-- Drop constraint (consent_template->scan)
ALTER TABLE consent_template
DROP FOREIGN KEY FK_consent_template_SCAN_BASE64;

-- Copy scan id and filetype from consent_template to consent_template_scan
INSERT INTO consent_template_scan (ID, FILETYPE)
SELECT SCAN_BASE64 as ID, SCANFILETYPE AS FILETYPE FROM consent_template;

-- Copy base64 from scan to consent_template_scan
UPDATE consent_template_scan cts
INNER JOIN scan s ON s.ID = cts.ID
SET cts.SCANBASE64 = s.SCANBASE64;

-- Drop filetype and rename reference to consent_template_scan in consent_template
ALTER TABLE consent_template DROP COLUMN SCANFILETYPE;
ALTER TABLE consent_template CHANGE `SCAN_BASE64` `SCAN` VARCHAR(255);

-- Add constraint
ALTER TABLE consent_template
ADD CONSTRAINT FK_consent_template_SCAN
FOREIGN KEY (SCAN) REFERENCES consent_template_scan(ID);

-- -----------------------------------
-- consent_scan
-- -----------------------------------
-- Remove constraint before remove scans
ALTER TABLE consent
DROP FOREIGN KEY FK_consent_SCAN_BASE64;

-- Remove consent template scans in scan table
DELETE FROM scan WHERE ID IN (SELECT ID FROM consent_template_scan);

-- Remove null scans
DELETE FROM scan WHERE SCANBASE64 IS NULL;

create table consent_scan like scan;
insert into consent_scan (ID) select ID from scan;

ALTER TABLE consent_scan
ADD COLUMN CONSENT_DATE timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
ADD COLUMN VIRTUAL_PERSON_ID bigint NOT NULL,
ADD COLUMN CT_DOMAIN_NAME varchar(50) NOT NULL,
ADD COLUMN CT_NAME varchar(100) NOT NULL,
ADD COLUMN CT_VERSION int NOT NULL,
ADD COLUMN FILETYPE varchar(255) NULL,
ADD COLUMN `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "",
ADD COLUMN `FILENAME` VARCHAR(255) NOT NULL DEFAULT "",
ADD COLUMN `UPLOAD_DATE` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);

UPDATE consent_scan s
INNER JOIN consent c ON s.ID = c.SCAN_BASE64
    SET s.CONSENT_DATE = c.CONSENT_DATE,
    s.VIRTUAL_PERSON_ID = c.VIRTUAL_PERSON_ID,
    s.CT_DOMAIN_NAME = c.CT_DOMAIN_NAME,
    s.CT_NAME = c.CT_NAME,
    s.CT_VERSION = c.CT_VERSION,
    s.FILETYPE = c.SCANFILETYPE;

-- if adding the constraint fails
-- DELETE FROM consent_scan WHERE ID NOT IN (SELECT SCAN_BASE64 FROM consent);

ALTER TABLE consent_scan
ADD CONSTRAINT FK_scan_CONSENT
FOREIGN KEY
(
  CONSENT_DATE,
  VIRTUAL_PERSON_ID,
  CT_DOMAIN_NAME,
  CT_NAME,
  CT_VERSION
)
REFERENCES consent
(
  CONSENT_DATE,
  VIRTUAL_PERSON_ID,
  CT_DOMAIN_NAME,
  CT_NAME,
  CT_VERSION
);

update `consent_scan` set `FHIR_ID` = UUID();
ALTER TABLE consent_scan DROP PRIMARY KEY;
ALTER TABLE `consent_scan` ADD PRIMARY KEY (FHIR_ID);

create table helper
(
   ID varchar(255) PRIMARY KEY NOT NULL,
   FHIR_ID VARCHAR(41) NOT NULL DEFAULT ""
);

insert into helper(ID, FHIR_ID) select ID, FHIR_ID from consent_scan;
CREATE INDEX i_fhir_id ON helper(FHIR_ID);
ALTER TABLE `consent_scan` DROP COLUMN `ID`;

update consent_scan cs set SCANBASE64 = (select SCANBASE64 from scan s where s.ID = (select ID from helper h where h.FHIR_ID = cs.FHIR_ID));

ALTER TABLE consent
	DROP COLUMN SCAN_BASE64,
	DROP COLUMN SCANFILETYPE;
DROP TABLE `scan`;
DROP TABLE `helper`;

-- -----------------------------------
-- consent_scan end
-- -----------------------------------

ALTER TABLE `consent_template` ADD `VERSION_LABEL` VARCHAR(255);
ALTER TABLE `signed_policy` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
update `signed_policy` set `FHIR_ID` = UUID();

ALTER TABLE `domain` CHANGE COLUMN `properties` `PROPERTIES` VARCHAR(4095) DEFAULT NULL;
ALTER TABLE `module` CHANGE COLUMN `extern_properties` `EXTERN_PROPERTIES` VARCHAR(4095) DEFAULT NULL;
ALTER TABLE `module_consent_template` CHANGE COLUMN `extern_properties` `EXTERN_PROPERTIES` VARCHAR(4095) DEFAULT NULL;
ALTER TABLE `consent_template` CHANGE COLUMN `extern_properties` `EXTERN_PROPERTIES` VARCHAR(4095) DEFAULT NULL;
ALTER TABLE `domain` CHANGE COLUMN `extern_properties` `EXTERN_PROPERTIES` VARCHAR(4095) DEFAULT NULL;
ALTER TABLE `consent` CHANGE COLUMN `extern_properties` `EXTERN_PROPERTIES` VARCHAR(4095) DEFAULT NULL;

ALTER TABLE `consent` ADD `EXPIRATION_PROPERTIES` varchar(255);
ALTER TABLE `domain` ADD `EXPIRATION_PROPERTIES` varchar(255);

ALTER TABLE `signer_id_type` ADD `ORDER_NUMBER` int NOT NULL;

update `consent` c1 set `EXPIRATION_PROPERTIES` =
(select y from (select concat('EXPIRATION_DATE=', date_format(`VALID_TO`, '%Y.%m.%d'), ';EXPIRATION_DATE_FORMAT=yyyy.MM.dd;') y,
`CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`, `CONSENT_DATE` from `consent`) c2
where c1.`CT_DOMAIN_NAME` = c2.`CT_DOMAIN_NAME` and c1.`CT_NAME` = c2.`CT_NAME` and c1.`CT_VERSION` = c2.`CT_VERSION` and c1.`CONSENT_DATE` = c2.`CONSENT_DATE`)
where `VALID_TO` is not null;

alter table `consent` drop column `VALID_TO`;

ALTER SCHEMA `gics` DEFAULT CHARACTER SET utf8 collate utf8_bin;
SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE alias CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE consent CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE consent_scan CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE consent_template CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE consent_template_scan CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE domain CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE free_text_def CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE free_text_val CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE healthcheck CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE module CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE module_consent_template CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE module_policy CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE policy CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE qc CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE qc_hist CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE sequence CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE signature CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE signed_policy CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE signer_id CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE signer_id_type CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE stat_entry CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE stat_value CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE text CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE virtual_person CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
ALTER TABLE virtual_person_signer_id CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE `free_text_def`
	ADD `EXTERN_PROPERTIES` varchar(4095),
	ADD `LABEL` varchar(255);

ALTER TABLE `module_policy` ADD `COMMENT` varchar(255);
ALTER TABLE `module_policy` ADD `EXTERN_PROPERTIES` varchar(4095);
ALTER TABLE `module_policy` ADD `EXPIRATION_PROPERTIES` varchar(255);
ALTER TABLE `module_policy` ADD `FHIR_ID` VARCHAR(41) NOT NULL DEFAULT "";
update `module_policy` set `FHIR_ID` = UUID();


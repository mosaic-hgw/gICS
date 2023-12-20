ALTER TABLE `domain`
    CHANGE COLUMN `properties` `properties` VARCHAR(4095) DEFAULT NULL;

ALTER TABLE `qc`
    CHANGE COLUMN `COMMENT` `COMMENT` VARCHAR(4095) NULL DEFAULT NULL COLLATE 'utf8_general_ci';
ALTER TABLE `qc`
    CHANGE COLUMN `DATE` `TIMESTAMP` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE `qc`
    CHANGE COLUMN `EXTERN_PROPERTIES` `EXTERN_PROPERTIES` VARCHAR(4095) NULL DEFAULT NULL COLLATE 'utf8_general_ci';
ALTER TABLE `qc`
    CHANGE COLUMN `INSPECTOR` `INSPECTOR` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8_general_ci';
ALTER TABLE `qc`
    CHANGE COLUMN `TYPE` `TYPE` VARCHAR(100) NOT NULL COLLATE 'utf8_general_ci';
ALTER TABLE `qc`
    CHANGE COLUMN `CONSENT_DATE` `CONSENT_DATE` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE `qc`
    CHANGE COLUMN `TYPE` `TYPE` VARCHAR(100) NOT NULL COLLATE 'utf8_general_ci';
ALTER TABLE `qc`
    DROP COLUMN `QC_PASSED`;

ALTER TABLE `qc_hist`
    CHANGE COLUMN `COMMENT` `COMMENT` VARCHAR(4095) NULL DEFAULT NULL COLLATE 'utf8_general_ci';
ALTER TABLE `qc_hist`
    CHANGE COLUMN `DATE` `TIMESTAMP` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE `qc_hist`
    CHANGE COLUMN `EXTERN_PROPERTIES` `EXTERN_PROPERTIES` VARCHAR(4095) NULL DEFAULT NULL COLLATE 'utf8_general_ci';
ALTER TABLE `qc_hist`
    CHANGE COLUMN `INSPECTOR` `INSPECTOR` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8_general_ci';
ALTER TABLE `qc_hist`
    CHANGE COLUMN `TYPE` `TYPE` VARCHAR(100) NOT NULL COLLATE 'utf8_general_ci';
ALTER TABLE `qc_hist`
    CHANGE COLUMN `CONSENT_DATE` `CONSENT_DATE` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE `qc_hist`
    CHANGE COLUMN `TYPE` `TYPE` VARCHAR(100) NOT NULL COLLATE 'utf8_general_ci';
ALTER TABLE `qc_hist`
    DROP COLUMN `QC_PASSED`;
ALTER TABLE `qc_hist`
    CHANGE COLUMN `START_DATE` `START_DATE` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE `qc_hist`
	ADD PRIMARY KEY (`CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`, `START_DATE`) USING BTREE;

INSERT INTO qc (CONSENT_DATE, VIRTUAL_PERSON_ID, CT_DOMAIN_NAME, CT_NAME, CT_VERSION, TIMESTAMP, TYPE, COMMENT)
SELECT CONSENT_DATE, VIRTUAL_PERSON_ID, CT_DOMAIN_NAME, CT_NAME, CT_VERSION, NOW(), @default_qs_state, 'initial qs entry gics version 2.12.0'
FROM consent;

INSERT INTO qc_hist (CONSENT_DATE, VIRTUAL_PERSON_ID, CT_DOMAIN_NAME, CT_NAME, CT_VERSION, TIMESTAMP, TYPE, COMMENT, START_DATE)
SELECT CONSENT_DATE, VIRTUAL_PERSON_ID, CT_DOMAIN_NAME, CT_NAME, CT_VERSION, NOW(), @default_qs_state, 'initial qs entry gics version 2.12.0', NOW()
FROM consent;

ALTER TABLE `alias`
    CHANGE COLUMN `CREATE_TIMESTAMP` `CREATE_TIMESTAMP` timestamp(3) NOT NULL;
ALTER TABLE `alias`
    CHANGE COLUMN `DEACTIVATE_TIMESTAMP` `DEACTIVATE_TIMESTAMP` timestamp(3) NULL DEFAULT NULL;

CREATE TABLE healthcheck
(
   NAME VARCHAR(16) PRIMARY KEY NOT NULL
);

INSERT INTO healthcheck VALUES ('healthcheck');

ALTER TABLE module CHANGE COLUMN extern_properties extern_properties VARCHAR(4095) DEFAULT NULL;

ALTER TABLE module_consent_template CHANGE COLUMN extern_properties extern_properties VARCHAR(4095) DEFAULT NULL;

ALTER TABLE consent_template CHANGE COLUMN extern_properties extern_properties VARCHAR(4095) DEFAULT NULL;

ALTER TABLE domain CHANGE COLUMN extern_properties extern_properties VARCHAR(4095) DEFAULT NULL;

ALTER TABLE consent CHANGE COLUMN extern_properties extern_properties VARCHAR(4095) DEFAULT NULL;
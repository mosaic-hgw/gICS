-- BEGIN Fix uppercase table name SEQUENCE in linux systems
-- Make sure both tables exist
CREATE TABLE IF NOT EXISTS `sequence` (
    SEQ_NAME varchar(50) PRIMARY KEY NOT NULL,
    SEQ_COUNT decimal(38,0)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=UTF8_BIN;
CREATE TABLE IF NOT EXISTS `SEQUENCE` (
    SEQ_NAME varchar(50) PRIMARY KEY NOT NULL,
    SEQ_COUNT decimal(38,0)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_bin;

-- Create backup table
CREATE TABLE `sequence_backup` (
    SEQ_NAME varchar(50) NOT NULL,
    SEQ_COUNT decimal(38,0)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=utf8_bin;

-- Combine both sequence tables
INSERT INTO `sequence_backup` SELECT * FROM `sequence`;
INSERT INTO `sequence_backup` SELECT * FROM `SEQUENCE`;

-- Drop old tables
DROP TABLE IF EXISTS `SEQUENCE`;
DROP TABLE IF EXISTS `sequence`;

-- Create new sequence table
CREATE TABLE `sequence` (
    SEQ_NAME varchar(50) PRIMARY KEY NOT NULL,
    SEQ_COUNT decimal(38,0)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE=UTF8_BIN;

-- Insert combined values
INSERT INTO `sequence`
SELECT SEQ_NAME, MAX(SEQ_COUNT) FROM `sequence_backup` GROUP BY SEQ_NAME;

-- Drop backup
DROP TABLE `sequence_backup`;
-- END Fix uppercase table name SEQUENCE in linux systems

-- remove deprected stats procedure
DROP procedure IF EXISTS `updateStats`;
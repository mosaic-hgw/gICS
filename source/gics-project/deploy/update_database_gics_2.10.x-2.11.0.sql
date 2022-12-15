-- Scans
RENAME TABLE `text` TO `scan`;
ALTER TABLE `scan`
	CHANGE COLUMN `TEXT` `SCANBASE64` LONGTEXT NULL DEFAULT NULL AFTER `ID`;

-- Texts
CREATE TABLE text
(
   ID varchar(255) PRIMARY KEY NOT NULL,
   TEXT longtext
);

-- Copy existing texts from scan into text table
INSERT INTO `text` (ID, TEXT)
SELECT ID, SCANBASE64
FROM scan
WHERE ID LIKE '%MODUL' OR ID LIKE '%HEADER' OR ID LIKE '%FOOTER';

-- Delete foreign keys of texts in scan table
ALTER TABLE consent_template
DROP FOREIGN KEY FK_consent_template_HEADER;

ALTER TABLE consent_template
DROP FOREIGN KEY FK_consent_template_FOOTER;

ALTER TABLE `module`
DROP FOREIGN KEY FK_module_TEXT;

-- Create foreign keys for texts in text table
ALTER TABLE consent_template
ADD CONSTRAINT FK_consent_template_HEADER
FOREIGN KEY (HEADER)
REFERENCES text(ID);

ALTER TABLE consent_template
ADD CONSTRAINT FK_consent_template_FOOTER
FOREIGN KEY (FOOTER)
REFERENCES text(ID);

ALTER TABLE `module`
ADD CONSTRAINT FK_module_TEXT
FOREIGN KEY (TEXT)
REFERENCES text(ID);

-- Delete text entries from scan table
DELETE FROM `scan`
WHERE ID LIKE '%MODUL' OR ID LIKE '%HEADER' OR ID LIKE '%FOOTER';

-- Fix faulty scans from older versions
UPDATE scan
SET SCANBASE64 = replace(SCANBASE64, '\n', '')
WHERE SCANBASE64 LIKE '%\n%';

UPDATE scan
SET SCANBASE64 = replace(SCANBASE64, '\r', '')
WHERE SCANBASE64 LIKE '%\r%';
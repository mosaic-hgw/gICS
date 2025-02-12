-- Add column for policy changed check
ALTER TABLE `domain` ADD LAST_CHECK_TIMESTAMP TIMESTAMP NULL;

-- Fix default for stat_entry in gics installations before version 2.13
ALTER TABLE stat_entry MODIFY COLUMN ENTRYDATE timestamp(3) DEFAULT CURRENT_TIMESTAMP(3)  NOT NULL;

-- for expirations of singedPolicies
ALTER TABLE signed_policy ADD EXPIRATION_PROPERTIES varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL;
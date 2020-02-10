SET foreign_key_checks = 0;
ALTER TABLE free_text_val MODIFY consent_date timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE signed_policy MODIFY consent_date timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE consent MODIFY consent_date timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE signature MODIFY consent_date timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE signature MODIFY signaturedate timestamp(3) NULL DEFAULT NULL;
SET foreign_key_checks = 1;

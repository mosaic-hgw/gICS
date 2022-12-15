-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server Version:               8.0.19 - MySQL Community Server - GPL
-- Server Betriebssystem:        Win64
-- HeidiSQL Version:             10.3.0.5771
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Exportiere Datenbank Struktur für notification_service
CREATE DATABASE IF NOT EXISTS `notification_service` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `notification_service`;

-- Exportiere Struktur von Tabelle notification_service.configuration
CREATE TABLE IF NOT EXISTS `configuration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activated` bit(1) DEFAULT NULL,
  `configKey` varchar(255) NOT NULL,
  `description` text,
  `value` longtext,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_configKey` (`configKey`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Exportiere Daten aus Tabelle notification_service.configuration: ~0 rows (ungefähr)
/*!40000 ALTER TABLE `configuration` DISABLE KEYS */;
INSERT INTO `configuration` (`id`, `activated`, `configKey`, `description`, `value`) VALUES
	(1, b'1', 'notification.config', NULL, '<org.emau.icmvc.ttp.notification.service.model.config.NotificationConfig>\r\n  <consumerConfigs>\r\n    <param>\r\n            <key>comdis</key>\r\n            <value class="org.emau.icmvc.ttp.notification.service.model.config.ConsumerConfig">\r\n                <connectionType>EJB</connectionType>\r\n                <messageTypes>\r\n                    <string>EPIX.AssignIdentity</string>\r\n                </messageTypes>\r\n                <excludeClientIdFilter class="set">\r\n                </excludeClientIdFilter>\r\n                <parameter>\r\n                    <param>\r\n                        <key>ejb.jndi.url</key>\r\n                        <value>java:global/dzhk-dispatcher-ear-1.15.3/dispatcher-services-1.13.3/InternalNotificationConsumer</value>\r\n                    </param>\r\n                </parameter>\r\n            </value>\r\n        </param>\r\n  </consumerConfigs>\r\n</org.emau.icmvc.ttp.notification.service.model.config.NotificationConfig>');
/*!40000 ALTER TABLE `configuration` ENABLE KEYS */;

-- Exportiere Struktur von Tabelle notification_service.notifications
CREATE TABLE IF NOT EXISTS `notifications` (
                                               `id`
                                               bigint
                                               NOT
                                               NULL
                                               AUTO_INCREMENT,
                                               `client_id`
                                               varchar
(
                                               255
) NOT NULL,
    `consumer_id` varchar
(
    255
) NOT NULL,
    `creationDate` datetime NOT NULL,
    `data` text NOT NULL,
    `send_date` datetime DEFAULT NULL,
    `status` varchar
(
    255
) DEFAULT NULL,
    `type` varchar
(
    255
) NOT NULL,
    `error_message` text,
    PRIMARY KEY
(
    `id`
)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;

-- Exportiere Struktur von Tabelle notification_service.sequence
CREATE TABLE IF NOT EXISTS `sequence` (
  `SEQ_NAME` varchar(50) NOT NULL,
  `SEQ_COUNT` decimal(38,0) DEFAULT NULL,
  PRIMARY KEY (`SEQ_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Exportiere Daten aus Tabelle notification_service.sequence: ~0 rows (ungefähr)
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` (`SEQ_NAME`, `SEQ_COUNT`) VALUES
	('SEQ_GEN', 0);
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;

CREATE USER 'noti_user'@'%' IDENTIFIED BY 'noti_password';
GRANT ALL ON notification_service.* TO 'noti_user'@'%';

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;

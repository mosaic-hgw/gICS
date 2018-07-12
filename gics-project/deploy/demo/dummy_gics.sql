-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: nb21-cmvc    Database: gics
-- ------------------------------------------------------
-- Server version	5.6.19-0ubuntu0.14.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `SEQUENCE`
--

DROP TABLE IF EXISTS `SEQUENCE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SEQUENCE` (
  `SEQ_NAME` varchar(50) NOT NULL,
  `SEQ_COUNT` decimal(38,0) DEFAULT NULL,
  PRIMARY KEY (`SEQ_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SEQUENCE`
--

LOCK TABLES `SEQUENCE` WRITE;
/*!40000 ALTER TABLE `SEQUENCE` DISABLE KEYS */;
INSERT INTO `SEQUENCE` VALUES ('virtual_person_index',0);
/*!40000 ALTER TABLE `SEQUENCE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consent`
--

DROP TABLE IF EXISTS `consent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `consent` (
  `PATIENTSIGNATURE_IS_FROM_GUARDIAN` bit(1) DEFAULT b'0',
  `PHYSICANID` varchar(255) DEFAULT NULL,
  `SCANFILETYPE` varchar(255) DEFAULT NULL,
  `CONSENT_DATE` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `VIRTUAL_PERSON_ID` bigint(20) NOT NULL,
  `CT_DOMAIN_NAME` varchar(50) NOT NULL,
  `CT_NAME` varchar(100) NOT NULL,
  `CT_VERSION` int(11) NOT NULL,
  `COMMENT` varchar(255) DEFAULT NULL,
  `EXTERN_PROPERTIES` varchar(255) DEFAULT NULL,
  `SCAN_BASE64` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`CONSENT_DATE`,`VIRTUAL_PERSON_ID`,`CT_DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`),
  UNIQUE KEY `I_PRIMARY` (`CONSENT_DATE`,`VIRTUAL_PERSON_ID`,`CT_DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`),
  KEY `I_FK_consent_CT_NAME` (`CT_NAME`,`CT_VERSION`,`CT_DOMAIN_NAME`),
  KEY `I_FK_consent_SCAN_BASE64` (`SCAN_BASE64`),
  KEY `I_FK_consent_VIRTUAL_PERSON_ID` (`VIRTUAL_PERSON_ID`),
  CONSTRAINT `FK_consent_SCAN_BASE64` FOREIGN KEY (`SCAN_BASE64`) REFERENCES `text` (`ID`),
  CONSTRAINT `FK_consent_CT_NAME` FOREIGN KEY (`CT_NAME`, `CT_VERSION`, `CT_DOMAIN_NAME`) REFERENCES `consent_template` (`NAME`, `VERSION`, `DOMAIN_NAME`),
  CONSTRAINT `FK_consent_VIRTUAL_PERSON_ID` FOREIGN KEY (`VIRTUAL_PERSON_ID`) REFERENCES `virtual_person` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consent`
--

LOCK TABLES `consent` WRITE;
/*!40000 ALTER TABLE `consent` DISABLE KEYS */;
/*!40000 ALTER TABLE `consent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consent_template`
--

DROP TABLE IF EXISTS `consent_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `consent_template` (
  `TITLE` varchar(255) DEFAULT NULL,
  `COMMENT` varchar(255) DEFAULT NULL,
  `EXTERN_PROPERTIES` varchar(255) DEFAULT NULL,
  `PROPERTIES` varchar(255) DEFAULT NULL,
  `SCANFILETYPE` varchar(255) DEFAULT NULL,
  `NAME` varchar(100) NOT NULL,
  `VERSION` int(11) NOT NULL,
  `DOMAIN_NAME` varchar(50) NOT NULL,
  `TYPE` varchar(20) NOT NULL,
  `FOOTER` varchar(255) DEFAULT NULL,
  `HEADER` varchar(255) DEFAULT NULL,
  `SCAN_BASE64` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`NAME`,`VERSION`,`DOMAIN_NAME`),
  UNIQUE KEY `I_PRIMARY` (`NAME`,`VERSION`,`DOMAIN_NAME`),
  KEY `I_FK_consent_template_SCAN_BASE64` (`SCAN_BASE64`),
  KEY `I_FK_consent_template_HEADER` (`HEADER`),
  KEY `I_FK_consent_template_FOOTER` (`FOOTER`),
  KEY `I_FK_consent_template_DOMAIN_NAME` (`DOMAIN_NAME`),
  CONSTRAINT `FK_consent_template_HEADER` FOREIGN KEY (`HEADER`) REFERENCES `text` (`ID`),
  CONSTRAINT `FK_consent_template_DOMAIN_NAME` FOREIGN KEY (`DOMAIN_NAME`) REFERENCES `domain` (`NAME`),
  CONSTRAINT `FK_consent_template_FOOTER` FOREIGN KEY (`FOOTER`) REFERENCES `text` (`ID`),
  CONSTRAINT `FK_consent_template_SCAN_BASE64` FOREIGN KEY (`SCAN_BASE64`) REFERENCES `text` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consent_template`
--

LOCK TABLES `consent_template` WRITE;
/*!40000 ALTER TABLE `consent_template` DISABLE KEYS */;
INSERT INTO `consent_template` VALUES ('<h1 style=\"text-align: center;\"><u>Dummy Studie</u></h1>','','orderNr=1;study=dummy;containsHTML=true','','','t_consent',1000,'dummy','CONSENT','dummy_###_t_consent_###_1000_###_FOOTER','dummy_###_t_consent_###_1000_###_HEADER','dummy_###_t_consent_###_1000_###_CONSENTTEMPLATESCAN');
/*!40000 ALTER TABLE `consent_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `domain`
--

DROP TABLE IF EXISTS `domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domain` (
  `NAME` varchar(50) NOT NULL,
  `COMMENT` varchar(255) DEFAULT NULL,
  `CT_VERSION_CONVERTER` varchar(255) DEFAULT NULL,
  `EXTERN_PROPERTIES` varchar(255) DEFAULT NULL,
  `LABEL` varchar(255) DEFAULT NULL,
  `MODULE_VERSION_CONVERTER` varchar(255) DEFAULT NULL,
  `POLICY_VERSION_CONVERTER` varchar(255) DEFAULT NULL,
  `PROPERTIES` varchar(255) DEFAULT NULL,
  `LOGO` longtext,
  PRIMARY KEY (`NAME`),
  UNIQUE KEY `I_PRIMARY` (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `domain`
--

LOCK TABLES `domain` WRITE;
/*!40000 ALTER TABLE `domain` DISABLE KEYS */;
INSERT INTO `domain` VALUES ('dummy','','org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorVersionConverter','','Dummy-Studie','org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorVersionConverter','org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorVersionConverter','domain properties: REVOKE_IS_PERMANENT = false; TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST = false; SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS = false; SCANS_SIZE_LIMIT = 10485760','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');
/*!40000 ALTER TABLE `domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `free_text_def`
--

DROP TABLE IF EXISTS `free_text_def`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `free_text_def` (
  `COMMENT` varchar(255) DEFAULT NULL,
  `CONVERTERSTRING` varchar(255) DEFAULT NULL,
  `POS` int(11) DEFAULT NULL,
  `REQUIRED` bit(1) DEFAULT b'0',
  `TYPE` int(11) DEFAULT NULL,
  `FREETEXT_NAME` varchar(255) NOT NULL,
  `DOMAIN_NAME` varchar(50) NOT NULL,
  `CT_NAME` varchar(100) NOT NULL,
  `CT_VERSION` int(11) NOT NULL,
  PRIMARY KEY (`FREETEXT_NAME`,`DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`),
  UNIQUE KEY `I_PRIMARY` (`FREETEXT_NAME`,`DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`),
  KEY `I_FK_free_text_def_CT_NAME` (`CT_NAME`,`CT_VERSION`,`DOMAIN_NAME`),
  CONSTRAINT `FK_free_text_def_CT_NAME` FOREIGN KEY (`CT_NAME`, `CT_VERSION`, `DOMAIN_NAME`) REFERENCES `consent_template` (`NAME`, `VERSION`, `DOMAIN_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `free_text_def`
--

LOCK TABLES `free_text_def` WRITE;
/*!40000 ALTER TABLE `free_text_def` DISABLE KEYS */;
/*!40000 ALTER TABLE `free_text_def` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `free_text_val`
--

DROP TABLE IF EXISTS `free_text_val`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `free_text_val` (
  `VALUE` longtext,
  `FREETEXTDEV_NAME` varchar(255) NOT NULL,
  `CONSENT_DATE` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `CONSENT_VIRTUAL_PERSON_ID` bigint(20) NOT NULL,
  `CT_DOMAIN_NAME` varchar(50) NOT NULL,
  `CT_NAME` varchar(100) NOT NULL,
  `CT_VERSION` int(11) NOT NULL,
  PRIMARY KEY (`FREETEXTDEV_NAME`,`CONSENT_DATE`,`CONSENT_VIRTUAL_PERSON_ID`,`CT_DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`),
  UNIQUE KEY `I_PRIMARY` (`FREETEXTDEV_NAME`,`CONSENT_DATE`,`CONSENT_VIRTUAL_PERSON_ID`,`CT_DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`),
  KEY `I_FK_free_text_val_CONSENT_DATE` (`CONSENT_DATE`,`CONSENT_VIRTUAL_PERSON_ID`,`CT_DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`),
  CONSTRAINT `FK_free_text_val_CONSENT_DATE` FOREIGN KEY (`CONSENT_DATE`, `CONSENT_VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`) REFERENCES `consent` (`CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `free_text_val`
--

LOCK TABLES `free_text_val` WRITE;
/*!40000 ALTER TABLE `free_text_val` DISABLE KEYS */;
/*!40000 ALTER TABLE `free_text_val` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `module`
--

DROP TABLE IF EXISTS `module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `module` (
  `COMMENT` varchar(255) DEFAULT NULL,
  `EXTERN_PROPERTIES` varchar(255) DEFAULT NULL,
  `TITLE` varchar(255) DEFAULT NULL,
  `NAME` varchar(100) NOT NULL,
  `VERSION` int(11) NOT NULL,
  `DOMAIN_NAME` varchar(50) NOT NULL,
  `TEXT` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`NAME`,`VERSION`,`DOMAIN_NAME`),
  UNIQUE KEY `I_PRIMARY` (`NAME`,`VERSION`,`DOMAIN_NAME`),
  KEY `I_FK_module_TEXT` (`TEXT`),
  KEY `I_FK_module_DOMAIN_NAME` (`DOMAIN_NAME`),
  CONSTRAINT `FK_module_DOMAIN_NAME` FOREIGN KEY (`DOMAIN_NAME`) REFERENCES `domain` (`NAME`),
  CONSTRAINT `FK_module_TEXT` FOREIGN KEY (`TEXT`) REFERENCES `text` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `module`
--

LOCK TABLES `module` WRITE;
/*!40000 ALTER TABLE `module` DISABLE KEYS */;
INSERT INTO `module` VALUES ('','','<h2>Wiederkontaktierung</h2>','m_contact',1000,'dummy','dummy_###_m_contact_###_1000_###_MODUL'),('','','<h2>Nur zu Testzwecken</h2>','m_intro',1000,'dummy','dummy_###_m_intro_###_1000_###_MODUL'),('','','<h2>Speicherung und Auswertung</h2>','m_storage_analysis',1000,'dummy','dummy_###_m_storage_analysis_###_1000_###_MODUL'),('','','<h2>Widerrufsrecht</h2>','m_withdraw',1000,'dummy','dummy_###_m_withdraw_###_1000_###_MODUL');
/*!40000 ALTER TABLE `module` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `module_consent_template`
--

DROP TABLE IF EXISTS `module_consent_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `module_consent_template` (
  `COMMENT` varchar(255) DEFAULT NULL,
  `DEFAULTCONSENTSTATUS` int(11) DEFAULT NULL,
  `DISPLAYCHECKBOXES` bigint(20) DEFAULT NULL,
  `EXTERN_PROPERTIES` varchar(255) DEFAULT NULL,
  `MANDATORY` bit(1) DEFAULT b'0',
  `ORDER_NUMBER` int(11) DEFAULT NULL,
  `CT_DOMAIN` varchar(50) NOT NULL,
  `CT_NAME` varchar(100) NOT NULL,
  `CT_VERSION` int(11) NOT NULL,
  `M_DOMAIN` varchar(50) NOT NULL,
  `M_NAME` varchar(100) NOT NULL,
  `M_VERSION` int(11) NOT NULL,
  `PARENT_M_DOMAIN` varchar(50) DEFAULT NULL,
  `PARENT_M_NAME` varchar(100) DEFAULT NULL,
  `PARENT_M_VERSION` int(11) DEFAULT NULL,
  PRIMARY KEY (`CT_DOMAIN`,`CT_NAME`,`CT_VERSION`,`M_DOMAIN`,`M_NAME`,`M_VERSION`),
  UNIQUE KEY `I_PRIMARY` (`CT_DOMAIN`,`CT_NAME`,`CT_VERSION`,`M_DOMAIN`,`M_NAME`,`M_VERSION`),
  KEY `I_FK_module_consent_template_PARENT_M_NAME` (`PARENT_M_NAME`,`PARENT_M_VERSION`,`PARENT_M_DOMAIN`),
  KEY `I_FK_module_consent_template_CT_NAME` (`CT_NAME`,`CT_VERSION`,`CT_DOMAIN`),
  KEY `I_FK_module_consent_template_M_NAME` (`M_NAME`,`M_VERSION`,`M_DOMAIN`),
  CONSTRAINT `FK_module_consent_template_PARENT_M_NAME` FOREIGN KEY (`PARENT_M_NAME`, `PARENT_M_VERSION`, `PARENT_M_DOMAIN`) REFERENCES `module` (`NAME`, `VERSION`, `DOMAIN_NAME`),
  CONSTRAINT `FK_module_consent_template_CT_NAME` FOREIGN KEY (`CT_NAME`, `CT_VERSION`, `CT_DOMAIN`) REFERENCES `consent_template` (`NAME`, `VERSION`, `DOMAIN_NAME`),
  CONSTRAINT `FK_module_consent_template_M_NAME` FOREIGN KEY (`M_NAME`, `M_VERSION`, `M_DOMAIN`) REFERENCES `module` (`NAME`, `VERSION`, `DOMAIN_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `module_consent_template`
--

LOCK TABLES `module_consent_template` WRITE;
/*!40000 ALTER TABLE `module_consent_template` DISABLE KEYS */;
INSERT INTO `module_consent_template` VALUES (NULL,NULL,3,NULL,'\0',2,'dummy','t_consent',1000,'dummy','m_contact',1000,NULL,NULL,NULL),(NULL,NULL,3,NULL,'',0,'dummy','t_consent',1000,'dummy','m_intro',1000,NULL,NULL,NULL),(NULL,NULL,3,NULL,'',1,'dummy','t_consent',1000,'dummy','m_storage_analysis',1000,NULL,NULL,NULL),(NULL,4,0,NULL,'\0',3,'dummy','t_consent',1000,'dummy','m_withdraw',1000,NULL,NULL,NULL);
/*!40000 ALTER TABLE `module_consent_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `module_policy`
--

DROP TABLE IF EXISTS `module_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `module_policy` (
  `P_NAME` varchar(100) NOT NULL,
  `P_DOMAIN_NAME` varchar(50) NOT NULL,
  `P_VERSION` int(11) NOT NULL,
  `M_NAME` varchar(100) NOT NULL,
  `M_DOMAIN_NAME` varchar(50) NOT NULL,
  `M_VERSION` int(11) NOT NULL,
  PRIMARY KEY (`P_NAME`,`P_DOMAIN_NAME`,`P_VERSION`,`M_NAME`,`M_DOMAIN_NAME`,`M_VERSION`),
  UNIQUE KEY `I_PRIMARY` (`P_NAME`,`P_DOMAIN_NAME`,`P_VERSION`,`M_NAME`,`M_DOMAIN_NAME`,`M_VERSION`),
  KEY `I_FK_MODULE_POLICY_M_NAME` (`M_NAME`,`M_VERSION`,`M_DOMAIN_NAME`),
  KEY `I_FK_MODULE_POLICY_P_NAME` (`P_NAME`,`P_VERSION`,`P_DOMAIN_NAME`),
  CONSTRAINT `FK_MODULE_POLICY_P_NAME` FOREIGN KEY (`P_NAME`, `P_VERSION`, `P_DOMAIN_NAME`) REFERENCES `policy` (`NAME`, `VERSION`, `DOMAIN_NAME`),
  CONSTRAINT `FK_MODULE_POLICY_M_NAME` FOREIGN KEY (`M_NAME`, `M_VERSION`, `M_DOMAIN_NAME`) REFERENCES `module` (`NAME`, `VERSION`, `DOMAIN_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `module_policy`
--

LOCK TABLES `module_policy` WRITE;
/*!40000 ALTER TABLE `module_policy` DISABLE KEYS */;
INSERT INTO `module_policy` VALUES ('p_contact','dummy',1000,'m_contact','dummy',1000),('p_intro','dummy',1000,'m_intro','dummy',1000),('p_collect_idat','dummy',1000,'m_storage_analysis','dummy',1000),('p_collect_mdat','dummy',1000,'m_storage_analysis','dummy',1000),('p_store_and_process_data','dummy',1000,'m_storage_analysis','dummy',1000),('p_use_and_secondary_use_data','dummy',1000,'m_storage_analysis','dummy',1000),('p_withdraw','dummy',1000,'m_withdraw','dummy',1000);
/*!40000 ALTER TABLE `module_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `policy`
--

DROP TABLE IF EXISTS `policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `policy` (
  `COMMENT` varchar(255) DEFAULT NULL,
  `EXTERN_PROPERTIES` varchar(255) DEFAULT NULL,
  `NAME` varchar(100) NOT NULL,
  `VERSION` int(11) NOT NULL,
  `DOMAIN_NAME` varchar(50) NOT NULL,
  PRIMARY KEY (`NAME`,`VERSION`,`DOMAIN_NAME`),
  UNIQUE KEY `I_PRIMARY` (`NAME`,`VERSION`,`DOMAIN_NAME`),
  KEY `I_FK_policy_DOMAIN_NAME` (`DOMAIN_NAME`),
  CONSTRAINT `FK_policy_DOMAIN_NAME` FOREIGN KEY (`DOMAIN_NAME`) REFERENCES `domain` (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `policy`
--

LOCK TABLES `policy` WRITE;
/*!40000 ALTER TABLE `policy` DISABLE KEYS */;
INSERT INTO `policy` VALUES ('','','p_collect_idat',1000,'dummy'),('','','p_collect_mdat',1000,'dummy'),('','','p_contact',1000,'dummy'),('dummies only','','p_intro',1000,'dummy'),('','','p_store_and_process_data',1000,'dummy'),('','','p_use_and_secondary_use_data',1000,'dummy'),('','','p_withdraw',1000,'dummy');
/*!40000 ALTER TABLE `policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `SEQ_NAME` varchar(50) NOT NULL,
  `SEQ_COUNT` decimal(38,0) DEFAULT NULL,
  PRIMARY KEY (`SEQ_NAME`),
  UNIQUE KEY `I_PRIMARY` (`SEQ_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sequence`
--

LOCK TABLES `sequence` WRITE;
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `signature`
--

DROP TABLE IF EXISTS `signature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `signature` (
  `SIGNATUREDATE` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `SIGNATURESCANBASE64` longtext,
  `TYPE` int(11) NOT NULL,
  `CONSENT_DATE` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `CONSENT_VIRTUAL_PERSON_ID` bigint(20) NOT NULL,
  `CT_DOMAIN_NAME` varchar(50) NOT NULL,
  `CT_NAME` varchar(100) NOT NULL,
  `CT_VERSION` int(11) NOT NULL,
  PRIMARY KEY (`TYPE`,`CONSENT_DATE`,`CONSENT_VIRTUAL_PERSON_ID`,`CT_DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`),
  UNIQUE KEY `I_PRIMARY` (`TYPE`,`CONSENT_DATE`,`CONSENT_VIRTUAL_PERSON_ID`,`CT_DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`),
  KEY `I_FK_signature_CONSENT_DATE` (`CONSENT_DATE`,`CONSENT_VIRTUAL_PERSON_ID`,`CT_DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`),
  CONSTRAINT `FK_signature_CONSENT_DATE` FOREIGN KEY (`CONSENT_DATE`, `CONSENT_VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`) REFERENCES `consent` (`CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `signature`
--

LOCK TABLES `signature` WRITE;
/*!40000 ALTER TABLE `signature` DISABLE KEYS */;
/*!40000 ALTER TABLE `signature` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `signed_policy`
--

DROP TABLE IF EXISTS `signed_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `signed_policy` (
  `STATUS` int(11) DEFAULT NULL,
  `CONSENT_DATE` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `CONSENT_VIRTUAL_PERSON_ID` bigint(20) NOT NULL,
  `CT_DOMAIN_NAME` varchar(50) NOT NULL,
  `CT_NAME` varchar(100) NOT NULL,
  `CT_VERSION` int(11) NOT NULL,
  `POLICY_DOMAIN_NAME` varchar(50) NOT NULL,
  `POLICY_NAME` varchar(100) NOT NULL,
  `POLICY_VERSION` int(11) NOT NULL,
  PRIMARY KEY (`CONSENT_DATE`,`CONSENT_VIRTUAL_PERSON_ID`,`CT_DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`,`POLICY_DOMAIN_NAME`,`POLICY_NAME`,`POLICY_VERSION`),
  UNIQUE KEY `I_PRIMARY` (`CONSENT_DATE`,`CONSENT_VIRTUAL_PERSON_ID`,`CT_DOMAIN_NAME`,`CT_NAME`,`CT_VERSION`,`POLICY_DOMAIN_NAME`,`POLICY_NAME`,`POLICY_VERSION`),
  KEY `I_FK_signed_policy_POLICY_NAME` (`POLICY_NAME`,`POLICY_VERSION`,`POLICY_DOMAIN_NAME`),
  CONSTRAINT `FK_signed_policy_CONSENT_DATE` FOREIGN KEY (`CONSENT_DATE`, `CONSENT_VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`) REFERENCES `consent` (`CONSENT_DATE`, `VIRTUAL_PERSON_ID`, `CT_DOMAIN_NAME`, `CT_NAME`, `CT_VERSION`),
  CONSTRAINT `FK_signed_policy_POLICY_NAME` FOREIGN KEY (`POLICY_NAME`, `POLICY_VERSION`, `POLICY_DOMAIN_NAME`) REFERENCES `policy` (`NAME`, `VERSION`, `DOMAIN_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `signed_policy`
--

LOCK TABLES `signed_policy` WRITE;
/*!40000 ALTER TABLE `signed_policy` DISABLE KEYS */;
/*!40000 ALTER TABLE `signed_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `signer_id`
--

DROP TABLE IF EXISTS `signer_id`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `signer_id` (
  `VALUE` varchar(255) NOT NULL,
  `SIT_DOMAIN_NAME` varchar(50) NOT NULL,
  `SIT_NAME` varchar(100) NOT NULL,
  PRIMARY KEY (`VALUE`,`SIT_DOMAIN_NAME`,`SIT_NAME`),
  UNIQUE KEY `I_PRIMARY` (`VALUE`,`SIT_DOMAIN_NAME`,`SIT_NAME`),
  KEY `I_FK_signer_id_SIT_NAME` (`SIT_NAME`,`SIT_DOMAIN_NAME`),
  CONSTRAINT `FK_signer_id_SIT_NAME` FOREIGN KEY (`SIT_NAME`, `SIT_DOMAIN_NAME`) REFERENCES `signer_id_type` (`NAME`, `DOMAIN_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `signer_id`
--

LOCK TABLES `signer_id` WRITE;
/*!40000 ALTER TABLE `signer_id` DISABLE KEYS */;
/*!40000 ALTER TABLE `signer_id` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `signer_id_type`
--

DROP TABLE IF EXISTS `signer_id_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `signer_id_type` (
  `NAME` varchar(100) NOT NULL,
  `DOMAIN_NAME` varchar(50) NOT NULL,
  PRIMARY KEY (`NAME`,`DOMAIN_NAME`),
  UNIQUE KEY `I_PRIMARY` (`NAME`,`DOMAIN_NAME`),
  KEY `I_FK_signer_id_type_DOMAIN_NAME` (`DOMAIN_NAME`),
  CONSTRAINT `FK_signer_id_type_DOMAIN_NAME` FOREIGN KEY (`DOMAIN_NAME`) REFERENCES `domain` (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `signer_id_type`
--

LOCK TABLES `signer_id_type` WRITE;
/*!40000 ALTER TABLE `signer_id_type` DISABLE KEYS */;
INSERT INTO `signer_id_type` VALUES ('studyPSN,sap.pat_id,sap.case_id','dummy');
/*!40000 ALTER TABLE `signer_id_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stat_entry`
--

DROP TABLE IF EXISTS `stat_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stat_entry` (
  `STAT_ENTRY_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ENTRYDATE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`STAT_ENTRY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stat_entry`
--

LOCK TABLES `stat_entry` WRITE;
/*!40000 ALTER TABLE `stat_entry` DISABLE KEYS */;
/*!40000 ALTER TABLE `stat_entry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stat_value`
--

DROP TABLE IF EXISTS `stat_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stat_value` (
  `stat_value_id` bigint(20) DEFAULT NULL,
  `stat_value` varchar(255) DEFAULT NULL,
  `stat_attr` varchar(50) DEFAULT NULL,
  KEY `FK_stat_value_stat_value_id` (`stat_value_id`),
  CONSTRAINT `FK_stat_value_stat_value_id` FOREIGN KEY (`stat_value_id`) REFERENCES `stat_entry` (`STAT_ENTRY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stat_value`
--

LOCK TABLES `stat_value` WRITE;
/*!40000 ALTER TABLE `stat_value` DISABLE KEYS */;
/*!40000 ALTER TABLE `stat_value` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `text`
--

DROP TABLE IF EXISTS `text`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `text` (
  `ID` varchar(255) NOT NULL,
  `TEXT` longtext,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `I_PRIMARY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `text`
--

LOCK TABLES `text` WRITE;
/*!40000 ALTER TABLE `text` DISABLE KEYS */;
INSERT INTO `text` VALUES ('dummy_###_m_contact_###_1000_###_MODUL','<div>Suspendisse dui purus, scelerisque at, vulputate vitae, pretium mattis, nunc. Mauris eget neque at sem venenatis eleifend. Ut nonummy. Fusce aliquet pede non pede. Suspendisse dapibus lorem pellentesque magna. Integer nulla.</div><div>Donec blandit feugiat ligula. Donec hendrerit, felis et imperdiet euismod, purus ipsum pretium metus, in lacinia nulla nisl eget sapien. Donec ut est in lectus consequat consequat. Etiam eget dui. Aliquam erat volutpat. Sed at lorem in nunc porta tristique.</div>'),('dummy_###_m_intro_###_1000_###_MODUL','<div style=\"text-align: justify;\"><span style=\"color: black;\">Hiermit willige ich ein, keinen realen Patienten in diese Studie einzuschließen.</span></div>'),('dummy_###_m_storage_analysis_###_1000_###_MODUL','<div>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Maecenas porttitor congue massa. Fusce posuere, magna sed pulvinar ultricies, purus lectus malesuada libero, sit amet commodo magna eros quis urna. Nunc viverra imperdiet enim. Fusce est. Vivamus a tellus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Proin pharetra nonummy pede.</div><div>Mauris et orci. Aenean nec lorem. In porttitor. Donec laoreet nonummy augue. Suspendisse dui purus, scelerisque at, vulputate vitae, pretium mattis, nunc. Mauris eget neque at sem venenatis eleifend. Ut nonummy. Fusce aliquet pede non pede.</div><div>Suspendisse dapibus lorem pellentesque magna. Integer nulla. Donec blandit feugiat ligula. Donec hendrerit, felis et imperdiet euismod, purus ipsum pretium metus, in lacinia nulla nisl eget sapien. Donec ut est in lectus consequat consequat. Etiam eget dui. Aliquam erat volutpat. Sed at lorem in nunc porta tristique.</div>'),('dummy_###_m_withdraw_###_1000_###_MODUL','<div>Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Proin semper, ante vitae sollicitudin posuere, metus quam iaculis nibh, vitae scelerisque nunc massa eget pede. Sed velit urna, interdum vel, ultricies vel, faucibus at, quam. Donec elit est, consectetuer eget, consequat quis, tempus quis, wisi. In in nunc. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos hymenaeos.</div><div>Donec ullamcorper fringilla eros. Fusce in sapien eu purus dapibus commodo. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Cras faucibus condimentum odio. Sed ac ligula. Aliquam at eros.</div>'),('dummy_###_t_consent_###_1000_###_CONSENTTEMPLATESCAN',''),('dummy_###_t_consent_###_1000_###_FOOTER',''),('dummy_###_t_consent_###_1000_###_HEADER','<div style=\"text-align: justify;\">Diese Studie dient ausschließlich der System- und Funktionsprüfung.</div><div style=\"text-align: justify;\">Es dürfen nur zzz-Patienten durch ausgewähltes Personal eingeschlossen werden.</div>');
/*!40000 ALTER TABLE `text` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `virtual_person`
--

DROP TABLE IF EXISTS `virtual_person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_person` (
  `ID` bigint(20) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `I_PRIMARY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `virtual_person`
--

LOCK TABLES `virtual_person` WRITE;
/*!40000 ALTER TABLE `virtual_person` DISABLE KEYS */;
/*!40000 ALTER TABLE `virtual_person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `virtual_person_signer_id`
--

DROP TABLE IF EXISTS `virtual_person_signer_id`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtual_person_signer_id` (
  `SIT_NAME` varchar(100) NOT NULL,
  `SIT_DOMAIN_NAME` varchar(50) NOT NULL,
  `SI_VALUE` varchar(255) NOT NULL,
  `VP_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`SIT_NAME`,`SIT_DOMAIN_NAME`,`SI_VALUE`,`VP_ID`),
  UNIQUE KEY `I_PRIMARY` (`SIT_NAME`,`SIT_DOMAIN_NAME`,`SI_VALUE`,`VP_ID`),
  KEY `I_FK_VIRTUAL_PERSON_SIGNER_ID_SI_VALUE` (`SI_VALUE`,`SIT_DOMAIN_NAME`,`SIT_NAME`),
  KEY `I_FK_VIRTUAL_PERSON_SIGNER_ID_VP_ID` (`VP_ID`),
  CONSTRAINT `FK_VIRTUAL_PERSON_SIGNER_ID_SI_VALUE` FOREIGN KEY (`SI_VALUE`, `SIT_DOMAIN_NAME`, `SIT_NAME`) REFERENCES `signer_id` (`VALUE`, `SIT_DOMAIN_NAME`, `SIT_NAME`),
  CONSTRAINT `FK_VIRTUAL_PERSON_SIGNER_ID_VP_ID` FOREIGN KEY (`VP_ID`) REFERENCES `virtual_person` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `virtual_person_signer_id`
--

LOCK TABLES `virtual_person_signer_id` WRITE;
/*!40000 ALTER TABLE `virtual_person_signer_id` DISABLE KEYS */;
/*!40000 ALTER TABLE `virtual_person_signer_id` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'gics'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-07-02 17:35:11

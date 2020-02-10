/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


USE gras;

-- project

INSERT INTO `gras`.`project` (`name`) VALUES ('gics');
-- INSERT INTO `gras`.`project` (`name`) VALUES ('gpas');
-- INSERT INTO `gras`.`project` (`name`) VALUES ('epix');

-- group_

INSERT INTO `gras`.`group_` (`id`, `name`, `project_name`, `description`) VALUES ('1', 'gics Nutzer', 'gics', 'gics Nutzer');
INSERT INTO `gras`.`group_` (`id`, `name`, `project_name`, `description`) VALUES ('2', 'gics Admins', 'gics', 'gics Admins');
-- INSERT INTO `gras`.`group_` (`id`, `name`, `project_name`, `description`) VALUES ('3', 'gPAS Nutzer', 'gpas', 'gPAS Nutzer');
-- INSERT INTO `gras`.`group_` (`id`, `name`, `project_name`, `description`) VALUES ('4', 'gPAS Admins', 'gpas', 'gPAS Admins');
-- INSERT INTO `gras`.`group_` (`id`, `name`, `project_name`, `description`) VALUES ('5', 'EPIX Nutzer', 'epix', 'E-PIX Nutzer');
-- INSERT INTO `gras`.`group_` (`id`, `name`, `project_name`, `description`) VALUES ('6', 'EPIX Admins', 'epix', 'E-PIX Admins');

-- role

INSERT INTO `gras`.`role` (`id`, `name`, `project_name`, `description`) VALUES ('1', 'role.gics.user', 'gics', 'gics Nutzerbereich');
INSERT INTO `gras`.`role` (`id`, `name`, `project_name`, `description`) VALUES ('2', 'role.gics.admin', 'gics', 'gics Adminbereich');
-- INSERT INTO `gras`.`role` (`id`, `name`, `project_name`, `description`) VALUES ('3', 'role.gpas.user', 'gpas', 'gPAS Nutzerbereich');
-- INSERT INTO `gras`.`role` (`id`, `name`, `project_name`, `description`) VALUES ('4', 'role.gpas.admin', 'gpas', 'gPAS Adminbereich');
-- INSERT INTO `gras`.`role` (`id`, `name`, `project_name`, `description`) VALUES ('5', 'role.epix.user', 'epix', 'E-PIX Nutzerbereich');
-- INSERT INTO `gras`.`role` (`id`, `name`, `project_name`, `description`) VALUES ('6', 'role.epix.admin', 'epix', 'E-PIX Adminbereich');

-- group_role_mapping

INSERT INTO `gras`.`group_role_mapping` (`group_id`, `role_id`) VALUES ('1', '1');
INSERT INTO `gras`.`group_role_mapping` (`group_id`, `role_id`) VALUES ('2', '1');
INSERT INTO `gras`.`group_role_mapping` (`group_id`, `role_id`) VALUES ('2', '2');
-- INSERT INTO `gras`.`group_role_mapping` (`group_id`, `role_id`) VALUES ('3', '3');
-- INSERT INTO `gras`.`group_role_mapping` (`group_id`, `role_id`) VALUES ('4', '3');
-- INSERT INTO `gras`.`group_role_mapping` (`group_id`, `role_id`) VALUES ('4', '4');
-- INSERT INTO `gras`.`group_role_mapping` (`group_id`, `role_id`) VALUES ('5', '5');
-- INSERT INTO `gras`.`group_role_mapping` (`group_id`, `role_id`) VALUES ('6', '5');
-- INSERT INTO `gras`.`group_role_mapping` (`group_id`, `role_id`) VALUES ('6', '6');


-- default user

-- default user 
call createUser("admin","miracum2020","user for admin privileges");
call createUser("employee","miracum2020","user for standard privileges");

-- grant privileges for gics
call grantAdminRights("gics","admin");
call grantStandardRights("gics","employee");

-- grant privileges for gpas
-- call grantAdminRights("gpas","admin");
-- call grantStandardRights("gpas","employee");

-- grant privileges for epix
-- call grantAdminRights("epix","admin");
-- call grantStandardRights("epix","employee");

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;


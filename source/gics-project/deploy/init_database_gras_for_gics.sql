/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

INSERT INTO `project` (`name`, `description`) VALUES ('gics', '');

INSERT INTO `group_` (`id`, `name`, `project_name`, `description`) VALUES (5, 'gICS Nutzer', 'gics', 'gICS Nutzer');
INSERT INTO `group_` (`id`, `name`, `project_name`, `description`) VALUES (6, 'gICS Admins', 'gics', 'gICS Admins');

INSERT INTO `role` (`id`, `name`, `project_name`, `description`) VALUES (5, 'role.gics.user', 'gics', 'gICS Nutzerbereich');
INSERT INTO `role` (`id`, `name`, `project_name`, `description`) VALUES (6, 'role.gics.admin', 'gics', 'gICS Adminbereich');

INSERT INTO `group_role_mapping` (`group_id`, `role_id`) VALUES (5, 5);
INSERT INTO `group_role_mapping` (`group_id`, `role_id`) VALUES (6, 5);
INSERT INTO `group_role_mapping` (`group_id`, `role_id`) VALUES (6, 6);

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
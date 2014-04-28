SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `vineyard` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `vineyard` ;

-- -----------------------------------------------------
-- Table `vineyard`.`worker`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `vineyard`.`worker` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `username` VARCHAR(16) NOT NULL ,
  `name` VARCHAR(45) NULL DEFAULT NULL ,
  `email` VARCHAR(255) NULL DEFAULT NULL ,
  `password` VARCHAR(32) NOT NULL ,
  `role` SET('operator','admin') NOT NULL DEFAULT 'operator' ,
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) ,
  PRIMARY KEY (`id`) );


-- -----------------------------------------------------
-- Table `vineyard`.`place`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `vineyard`.`place` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NOT NULL ,
  `parent` INT UNSIGNED NULL COMMENT 'ID del posto genitore se questo non è un elemento di primo livello, NULL altrimenti' ,
  `description` TEXT NULL DEFAULT NULL ,
  `location` POINT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_place_1_idx` (`parent` ASC) ,
  CONSTRAINT `fk_place_1`
    FOREIGN KEY (`parent` )
    REFERENCES `vineyard`.`place` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `vineyard`.`place_attribute`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `vineyard`.`place_attribute` (
  `place` INT UNSIGNED NOT NULL ,
  `key` VARCHAR(45) NOT NULL ,
  `value` TEXT NOT NULL ,
  UNIQUE INDEX `unique_couple` (`place` ASC, `key` ASC) ,
  CONSTRAINT `fk_place_attribute_1`
    FOREIGN KEY (`place` )
    REFERENCES `vineyard`.`place` (`id` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB
COMMENT = 'contiene coppie chiave-valore con gli attributi opzionali di' /* comment truncated */;


-- -----------------------------------------------------
-- Table `vineyard`.`group`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `vineyard`.`group` (
  `id` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(45) NULL DEFAULT NULL ,
  `description` TEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'contiene coppie gruppo-lavoratore, permette di definire dei ' /* comment truncated */;


-- -----------------------------------------------------
-- Table `vineyard`.`group_composition`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `vineyard`.`group_composition` (
  `group` INT UNSIGNED NOT NULL ,
  `worker` INT UNSIGNED NOT NULL ,
  INDEX `fk_group_worker_1_idx` (`group` ASC) ,
  INDEX `fk_group_worker_2_idx` (`worker` ASC) ,
  CONSTRAINT `fk_group_worker_1`
    FOREIGN KEY (`group` )
    REFERENCES `vineyard`.`group` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_group_worker_2`
    FOREIGN KEY (`worker` )
    REFERENCES `vineyard`.`worker` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
COMMENT = 'contiene coppie gruppo-lavoratore, permette di descrivere la' /* comment truncated */;


-- -----------------------------------------------------
-- Table `vineyard`.`task`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `vineyard`.`task` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `assignee` INT UNSIGNED NULL ,
  `create_time` TIMESTAMP NULL DEFAULT NULL,
  `assign_time` TIMESTAMP NULL DEFAULT NULL ,
  `due_time`TIMESTAMP  NULL DEFAULT NULL ,
  `status` ENUM('new','assigned','resolved') NOT NULL DEFAULT 'new' ,
  `priority` ENUM('not-set','low','medium','high') NOT NULL DEFAULT 'not-set' ,
  `issuer` INT UNSIGNED NULL DEFAULT NULL COMMENT 'utente che ha riportato il problema' ,
  `place` INT UNSIGNED NOT NULL ,
  `title` VARCHAR(45) NOT NULL ,
  `description` TEXT NULL DEFAULT NULL ,
  `location` POINT NULL DEFAULT NULL ,
  `assigned_worker` INT UNSIGNED NULL DEFAULT NULL ,
  `assigned_group` INT UNSIGNED NULL DEFAULT NULL ,
  `start_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `end_time` TIMESTAMP NULL DEFAULT NULL ,
  PRIMARY KEY (`id`, `start_time`) ,
  INDEX `fk_task_2_idx` (`place` ASC) ,
  INDEX `fk_task_3_idx` (`assigned_group` ASC) ,
  INDEX `fk_task_4_idx` (`assigned_worker` ASC) ,
  INDEX `fk_task_5_idx` (`assignee` ASC) ,
  INDEX `fk_task_1_idx` (`issuer` ASC) ,
  CONSTRAINT `fk_task_2`
    FOREIGN KEY (`place` )
    REFERENCES `vineyard`.`place` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_task_3`
    FOREIGN KEY (`assigned_group` )
    REFERENCES `vineyard`.`group` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_task_4`
    FOREIGN KEY (`assigned_worker` )
    REFERENCES `vineyard`.`worker` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_task_5`
    FOREIGN KEY (`assignee` )
    REFERENCES `vineyard`.`worker` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_task_1`
    FOREIGN KEY (`issuer` )
    REFERENCES `vineyard`.`worker` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
COMMENT = 'contiene i task assegnati ai lavoratori';


-- -----------------------------------------------------
-- Table `vineyard`.`task_photo`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `vineyard`.`task_photo` (
  `task` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `url` VARCHAR(45) NOT NULL ,
  UNIQUE INDEX `unique_couple` (`task` ASC, `url` ASC) ,
  CONSTRAINT `fk_task_photo_1`
    FOREIGN KEY (`task` )
    REFERENCES `vineyard`.`task` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `vineyard`.`worker`
-- -----------------------------------------------------
START TRANSACTION;
USE `vineyard`;
INSERT INTO `vineyard`.`worker` (`id`, `username`, `name`, `email`, `password`, `role`) VALUES (1, 'f.carrara', 'Fabio Carrara', 'fabio.blackdragon@gmail.com', '5f4dcc3b5aa765d61d8327deb882cf99', 'admin');
INSERT INTO `vineyard`.`worker` (`id`, `username`, `name`, `email`, `password`, `role`) VALUES (2, 'd.formichelli', 'Daniele Formichelli', 'daniele.formichelli@gmail.com', '5f4dcc3b5aa765d61d8327deb882cf99', 'admin');
INSERT INTO `vineyard`.`worker` (`id`, `username`, `name`, `email`, `password`, `role`) VALUES (3, 's.chiavo', 'Salvatore Chiavo', 's.chiavo@gmail.com', '5f4dcc3b5aa765d61d8327deb882cf99', 'operator');
INSERT INTO `vineyard`.`worker` (`id`, `username`, `name`, `email`, `password`, `role`) VALUES (4, 'b.racciante', 'Bernardo Racciante', 'b.racciante@gmail.com', '5f4dcc3b5aa765d61d8327deb882cf99', 'operator');
INSERT INTO `vineyard`.`worker` (`id`, `username`, `name`, `email`, `password`, `role`) VALUES (5, 'n.egro', 'Niccolò Egro', 'n.egro@gmail.com', '5f4dcc3b5aa765d61d8327deb882cf99', 'operator');
INSERT INTO `vineyard`.`worker` (`id`, `username`, `name`, `email`, `password`, `role`) VALUES (6, 'o.pressore', 'Oreste Pressore', 'o.pressore@gmail.com', '5f4dcc3b5aa765d61d8327deb882cf99', 'admin,operator');

COMMIT;

-- -----------------------------------------------------
-- Data for table `vineyard`.`place`
-- -----------------------------------------------------
START TRANSACTION;
USE `vineyard`;
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (13,'Vigneto Acino Fresco',NULL,'Tutta la mia bellissima proprietà fabbrica-acini.',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (1,'Vigna A',13,'Prima vigna',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (2,'Vigna B',13,'Seconda vigna',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (3,'Vigna C',13,'Terza vigna',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (4,'Filare A1',1,'Primo filare prima vigna',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (5,'Filare A2',1,'Secondo filare prima vigna',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (6,'Filare A3',1,'Terzo filare seconda vigna',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (7,'Filare B1',2,'Primo filare seconda vigna',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (8,'Filare C1',3,'Primo filare terza vigna',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (9,'Filare C2',3,'Secondo filare terza vigna',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (10,'Filare C3',3,'Terzo filare terza vigna',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (11,'Fila C21',8,'Fila 1 Filare C2',POINT(43.826348, 10.888534));
INSERT INTO `place` (`id`,`name`,`parent`,`description`,`location`) VALUES (12,'Fila C22',8,'Fila 2 Filare C2',POINT(43.826348, 10.888534));


COMMIT;

-- -----------------------------------------------------
-- Data for table `vineyard`.`place_attribute`
-- -----------------------------------------------------
START TRANSACTION;
USE `vineyard`;
INSERT INTO `vineyard`.`place_attribute` (`place`, `key`, `value`) VALUES (1, 'Ettaraggio', '3');
INSERT INTO `vineyard`.`place_attribute` (`place`, `key`, `value`) VALUES (2, 'Ettaraggio', '4');
INSERT INTO `vineyard`.`place_attribute` (`place`, `key`, `value`) VALUES (3, 'Ettaraggio', '1');
INSERT INTO `vineyard`.`place_attribute` (`place`, `key`, `value`) VALUES (2, 'Varietà', 'Varietà 1');
INSERT INTO `vineyard`.`place_attribute` (`place`, `key`, `value`) VALUES (3, 'Varietà', 'Varietà 2');
INSERT INTO `vineyard`.`place_attribute` (`place`, `key`, `value`) VALUES (1, 'Anno di impianto', '2012');
INSERT INTO `vineyard`.`place_attribute` (`place`, `key`, `value`) VALUES (4, 'Agenti Patogeni', 'Rischio bla bla');
INSERT INTO `vineyard`.`place_attribute` (`place`, `key`, `value`) VALUES (5, 'Ultime Produzioni', 'tot tonnellate');

COMMIT;

-- -----------------------------------------------------
-- Data for table `vineyard`.`group`
-- -----------------------------------------------------
START TRANSACTION;
USE `vineyard`;
INSERT INTO `vineyard`.`group` (`id`, `name`, `description`) VALUES (1, 'Team 1', 'Gruppo potatura');
INSERT INTO `vineyard`.`group` (`id`, `name`, `description`) VALUES (2, 'Team 2', 'Gruppo raccolta');

COMMIT;

-- -----------------------------------------------------
-- Data for table `vineyard`.`group_composition`
-- -----------------------------------------------------
START TRANSACTION;
USE `vineyard`;
INSERT INTO `vineyard`.`group_composition` (`group`, `worker`) VALUES (1, 3);
INSERT INTO `vineyard`.`group_composition` (`group`, `worker`) VALUES (1, 4);
INSERT INTO `vineyard`.`group_composition` (`group`, `worker`) VALUES (1, 5);
INSERT INTO `vineyard`.`group_composition` (`group`, `worker`) VALUES (2, 6);
INSERT INTO `vineyard`.`group_composition` (`group`, `worker`) VALUES (2, 3);
INSERT INTO `vineyard`.`group_composition` (`group`, `worker`) VALUES (2, 4);

COMMIT;

-- -----------------------------------------------------
-- Data for table `vineyard`.`task`
-- -----------------------------------------------------
START TRANSACTION;
USE `vineyard`;
INSERT INTO `vineyard`.`task` (`id`, `assignee`, `create_time`, `assign_time`, `due_time`, `status`, `priority`, `issuer`, `place`, `title`, `description`, `location`, `assigned_worker`, `assigned_group`, `start_time`, `end_time`) VALUES (1, NULL, NOW(), NULL, NULL, 'new', 'not-set', 5, 3, 'Problema X', 'C\'è un problema con X', POINT(51,23), NULL, NULL, NOW(), NULL);
INSERT INTO `vineyard`.`task` (`id`, `assignee`, `create_time`, `assign_time`, `due_time`, `status`, `priority`, `issuer`, `place`, `title`, `description`, `location`, `assigned_worker`, `assigned_group`, `start_time`, `end_time`) VALUES (2, NULL, NOW(), NULL, NULL, 'new', 'low', 4, 6, 'Problema Y', 'C\'è un problema con Y, assegnato', POINT(51,23), 4, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY));
INSERT INTO `vineyard`.`task` (`id`, `assignee`, `create_time`, `assign_time`, `due_time`, `status`, `priority`, `issuer`, `place`, `title`, `description`, `location`, `assigned_worker`, `assigned_group`, `start_time`, `end_time`) VALUES (2, 6, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 'assigned', 'medium', 4, 6, 'Problema Y', 'C\'è un problema con Y, assegnato', POINT(51,23), 4, NULL, DATE_ADD(NOW(), INTERVAL 1 DAY), NULL);
INSERT INTO `vineyard`.`task` (`id`, `assignee`, `create_time`, `assign_time`, `due_time`, `status`, `priority`, `issuer`, `place`, `title`, `description`, `location`, `assigned_worker`, `assigned_group`, `start_time`, `end_time`) VALUES (3, 2, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 'assigned', 'high', NULL, 2, 'Task Z', 'C\'è da fare Z', POINT(51,23), NULL, 1, NOW(), NULL);
INSERT INTO `vineyard`.`task` (`id`, `assignee`, `create_time`, `assign_time`, `due_time`, `status`, `priority`, `issuer`, `place`, `title`, `description`, `location`, `assigned_worker`, `assigned_group`, `start_time`, `end_time`) VALUES (4, 1, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 'resolved', 'low', NULL, 11, 'Task ABC', 'ABC è questo e quest\'altro', POINT(51,23), 6, NULL, NOW(), NULL);

COMMIT;

-- -----------------------------------------------------
-- Data for table `vineyard`.`task_photo`
-- -----------------------------------------------------
START TRANSACTION;
USE `vineyard`;
INSERT INTO `vineyard`.`task_photo` (`task`, `url`) VALUES (1, 'issue0.jpg');
INSERT INTO `vineyard`.`task_photo` (`task`, `url`) VALUES (2, 'issue1.jpg');
INSERT INTO `vineyard`.`task_photo` (`task`, `url`) VALUES (2, 'issue1-2.jpg');

COMMIT;

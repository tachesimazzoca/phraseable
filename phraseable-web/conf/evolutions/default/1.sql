# schema

# --- !Ups

CREATE TABLE `account` (
  `id` BIGINT NOT NULL,
  `username` VARCHAR(255) NOT NULL DEFAULT '' UNIQUE,
  `password_salt` VARCHAR(4) NOT NULL DEFAULT '',
  `password_hash` VARCHAR(40) NOT NULL DEFAULT '',
  `status` TINYINT(1) NOT NULL DEFAULT 0,
  `email` VARCHAR(255) NOT NULL DEFAULT '',
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`)
);
CREATE INDEX `account_username` ON `account` (`username`);

CREATE TABLE `id_sequence` (
  `sequence_name` VARCHAR(255) NOT NULL DEFAULT '',
  `sequence_value` BIGINT NOT NULL,
  PRIMARY KEY (`sequence_name`)
);
INSERT INTO `id_sequence` VALUES ('account', 0);

# --- !Downs
DROP TABLE IF EXISTS `account`;

DROP TABLE IF EXISTS `id_sequence`;

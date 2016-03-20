# schema

# --- !Ups

CREATE TABLE `accounts` (
  `id` BIGINT NOT NULL,
  `username` VARCHAR(255) NOT NULL DEFAULT '' UNIQUE,
  `password_salt` VARCHAR(4) NOT NULL DEFAULT '',
  `password_hash` VARCHAR(40) NOT NULL DEFAULT '',
  `status` TINYINT(1) NOT NULL DEFAULT 0,
  `email` VARCHAR(255) NOT NULL DEFAULT '' UNIQUE,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`)
);
CREATE INDEX `accounts_username` ON `accounts` (`username`);

# --- !Downs
DROP TABLE IF EXISTS `accounts`;

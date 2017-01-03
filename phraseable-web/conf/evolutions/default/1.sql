# schema

# --- !Ups
CREATE TABLE `session_storage` (
  `storage_key` VARCHAR(255) NOT NULL,
  `storage_value` TEXT,
  `modified_at` TIMESTAMP,
  PRIMARY KEY (`storage_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `idx_session_storage_01` ON `session_storage` (`modified_at`);

CREATE TABLE `verification_storage` (
  `storage_key` VARCHAR(255) NOT NULL,
  `storage_value` TEXT,
  `modified_at` TIMESTAMP,
  PRIMARY KEY (`storage_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `idx_verification_storage_01` ON `verification_storage` (`modified_at`);

CREATE TABLE `id_sequence` (
  `sequence_name` VARCHAR(255) NOT NULL UNIQUE,
  `sequence_value` BIGINT NOT NULL,
  PRIMARY KEY (`sequence_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `id_sequence` VALUES ('account', 0);
INSERT INTO `id_sequence` VALUES ('phrase', 0);
INSERT INTO `id_sequence` VALUES ('category', 0);

CREATE TABLE `account` (
  `id` BIGINT NOT NULL,
  `email` VARCHAR(255) NOT NULL DEFAULT '' UNIQUE,
  `password_salt` VARCHAR(4) NOT NULL DEFAULT '',
  `password_hash` VARCHAR(40) NOT NULL DEFAULT '',
  `status` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `idx_account_01` ON `account` (`email`);

CREATE TABLE `account_access` (
  `code` VARCHAR(255) NOT NULL DEFAULT '',
  `account_id` BIGINT NOT NULL,
  `user_agent` TEXT,
  `remote_address` VARCHAR(40) NOT NULL DEFAULT '',
  `created_at` TIMESTAMP,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `idx_account_access_01` ON `account_access` (`account_id`);

CREATE TABLE `phrase` (
  `id` BIGINT NOT NULL,
  `lang` CHAR(2) NOT NULL,
  `term` TEXT,
  `translation` TEXT,
  `description` TEXT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `phrase_keyword` (
  `phrase_id` BIGINT NOT NULL,
  `keyword` VARCHAR(255) NOT NULL default ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `idx_phrase_keyword_01` ON `phrase_keyword` (`phrase_id`);
CREATE INDEX `idx_phrase_keyword_02` ON `phrase_keyword` (`keyword`);

CREATE TABLE `category` (
  `id` BIGINT NOT NULL,
  `title` VARCHAR(255) NOT NULL UNIQUE,
  `description` TEXT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `idx_category_01` ON `category` (`title`);

CREATE TABLE `rel_phrase_category` (
  `phrase_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  `priority` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `idx_rel_phrase_category_01` ON `rel_phrase_category` (`phrase_id`);
CREATE INDEX `idx_rel_phrase_category_02` ON `rel_phrase_category` (`category_id`);

# --- !Downs
DROP TABLE `session_storage`;
DROP TABLE `verification_storage`;
DROP TABLE `id_sequence`;
DROP TABLE `account`;
DROP TABLE `account_access`;
DROP TABLE `phrase`;
DROP TABLE `phrase_keyword`;
DROP TABLE `category`;
DROP TABLE `rel_phrase_category`;

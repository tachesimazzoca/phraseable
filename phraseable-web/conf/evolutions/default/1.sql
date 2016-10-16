# schema

# --- !Ups
CREATE TABLE `id_sequence` (
  `sequence_name` VARCHAR(255) NOT NULL UNIQUE,
  `sequence_value` BIGINT NOT NULL,
  PRIMARY KEY (`sequence_name`)
);
INSERT INTO `id_sequence` VALUES ('account', 0);
INSERT INTO `id_sequence` VALUES ('phrase', 0);
INSERT INTO `id_sequence` VALUES ('phrase_translation', 0);
INSERT INTO `id_sequence` VALUES ('category', 0);

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
CREATE INDEX `idx_account_01` ON `account` (`username`);

CREATE TABLE `phrase` (
  `id` BIGINT NOT NULL,
  `content` TEXT,
  `description` TEXT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE `phrase_translation` (
  `id` BIGINT NOT NULL,
  `phrase_id` BIGINT NOT NULL,
  `locale` CHAR(5) NOT NULL,
  `content` TEXT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`)
);
CREATE INDEX `idx_phrase_translation_01` ON `phrase_translation` (`phrase_id`);
CREATE INDEX `idx_phrase_translation_02` ON `phrase_translation` (`locale`);

CREATE TABLE `category` (
  `id` BIGINT NOT NULL,
  `title` TEXT,
  `description` TEXT,
  `created_at` TIMESTAMP,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE `rel_phrase_category` (
  `phrase_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL
);
CREATE INDEX `idx_rel_phrase_category_01` ON `rel_phrase_category` (`phrase_id`);
CREATE INDEX `idx_rel_phrase_category_02` ON `rel_phrase_category` (`category_id`);

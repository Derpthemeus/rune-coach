CREATE TABLE aggregated_stats
(
  champion_id    SMALLINT                               NOT NULL,
  perk_id        SMALLINT(5) UNSIGNED                   NOT NULL,
  patch          VARCHAR(6)                             NOT NULL,
  total_matches  BIGINT UNSIGNED DEFAULT '0'            NOT NULL,
  total_wins     BIGINT UNSIGNED DEFAULT '0'            NOT NULL,
  var1_total     BIGINT UNSIGNED DEFAULT '0'            NOT NULL,
  var2_total     BIGINT UNSIGNED DEFAULT '0'            NOT NULL,
  var3_total     BIGINT UNSIGNED DEFAULT '0'            NOT NULL,
  last_player_id BIGINT UNSIGNED DEFAULT '0'            NOT NULL,
  last_updated   DATETIME DEFAULT '1970-01-01 00:00:01' NOT NULL,
  PRIMARY KEY (patch, champion_id, perk_id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX IX_last_updated
  ON aggregated_stats (last_updated);

CREATE TABLE leagues
(
  uuid         CHAR(36)                               NOT NULL
    PRIMARY KEY,
  last_checked DATETIME DEFAULT '1970-01-01 00:00:01' NOT NULL
)
  ENGINE = InnoDB;

CREATE INDEX last_checked
  ON leagues (last_checked);

CREATE TABLE matches
(
  match_id         BIGINT UNSIGNED                        NOT NULL
    PRIMARY KEY,
  patch            VARCHAR(6)                             NULL,
  downloaded_match TINYINT DEFAULT '0'                    NOT NULL,
  match_timestamp  DATETIME DEFAULT '1970-01-01 00:00:01' NOT NULL
)
  ENGINE = InnoDB;

CREATE INDEX IX_patch
  ON matches (patch);

CREATE INDEX IX_to_be_downloaded
  ON matches (downloaded_match);

CREATE TABLE participant_perks
(
  player_id BIGINT UNSIGNED      NOT NULL,
  perk_id   SMALLINT(5) UNSIGNED NOT NULL,
  var1      SMALLINT(5) UNSIGNED NOT NULL,
  var2      SMALLINT(5) UNSIGNED NOT NULL,
  var3      SMALLINT(5) UNSIGNED NOT NULL,
  PRIMARY KEY (player_id, perk_id),
  CONSTRAINT UX_statAggregator
  UNIQUE (perk_id, player_id)
)
  ENGINE = InnoDB;

CREATE TABLE participants
(
  player_id   BIGINT UNSIGNED AUTO_INCREMENT
    PRIMARY KEY,
  winner      TINYINT              NOT NULL,
  champion_id SMALLINT(5) UNSIGNED NOT NULL,
  match_id    BIGINT UNSIGNED      NOT NULL,
  CONSTRAINT UX_statAggregator
  UNIQUE (champion_id, player_id)
)
  ENGINE = InnoDB;

CREATE INDEX IX_match_id
  ON participants (match_id);

ALTER TABLE participant_perks
  ADD CONSTRAINT FK_player_id
FOREIGN KEY (player_id) REFERENCES participants (player_id);

CREATE TABLE patches
(
  patch      VARCHAR(6) NOT NULL
    PRIMARY KEY,
  start_date DATETIME   NOT NULL
)
  ENGINE = InnoDB;

ALTER TABLE matches
  ADD CONSTRAINT FK_patch
FOREIGN KEY (patch) REFERENCES patches (patch);

CREATE TABLE perk_scores
(
  perk_id      SMALLINT(5) UNSIGNED                        NOT NULL,
  champion_id  SMALLINT                                    NOT NULL
  COMMENT 'The champion or negative tag ID',
  patch        VARCHAR(6)                                  NOT NULL,
  score        DOUBLE                                      NULL
  COMMENT 'The score (currently just the winrate, but can be any value), or NULL if not yet calculated',
  games        INT DEFAULT '0'                             NOT NULL,
  score_type   SET ('RAW', 'RELATIVE', 'SLOT', 'SUBSTYLE') NOT NULL,
  last_updated DATETIME DEFAULT '1970-01-01 00:00:01'      NOT NULL,
  PRIMARY KEY (patch, score_type, perk_id, champion_id),
  CONSTRAINT FK_perk_scores__patch
  FOREIGN KEY (patch) REFERENCES patches (patch)
)
  ENGINE = InnoDB;

CREATE INDEX IX_getChampionInfo
  ON perk_scores (patch, champion_id, score_type);

CREATE INDEX IX_relativeChampion
  ON perk_scores (patch, score_type, champion_id, perk_id);

CREATE INDEX IX_toUpdate
  ON perk_scores (patch, last_updated);

CREATE TABLE summoners
(
  summoner_id          BIGINT UNSIGNED                        NOT NULL
    PRIMARY KEY,
  account_id           BIGINT UNSIGNED                        NULL,
  league               CHAR(36)                               NULL,
  matches_last_updated DATETIME DEFAULT '1970-01-01 00:00:01' NOT NULL,
  league_last_updated  DATETIME DEFAULT '1970-01-01 00:00:01' NOT NULL,
  last_match_timestamp DATETIME DEFAULT '1970-01-01 00:00:01' NOT NULL,
  CONSTRAINT UX_account_id
  UNIQUE (account_id)
)
  ENGINE = InnoDB;

CREATE INDEX IX_matches_last_updated
  ON summoners (matches_last_updated);

CREATE INDEX IX_league_last_updated
  ON summoners (league_last_updated);

CREATE TABLE tag__champion
(
  champion_id SMALLINT(5) UNSIGNED NOT NULL,
  tag_id      SMALLINT(6) UNSIGNED NOT NULL,
  PRIMARY KEY (champion_id, tag_id)
)
  ENGINE = InnoDB;

CREATE INDEX IX_champion_id
  ON tag__champion (champion_id);

CREATE INDEX FX_tag_champion__tag_id
  ON tag__champion (tag_id);

CREATE TABLE tags
(
  tag_name  VARCHAR(20)  NOT NULL,
  noun      VARCHAR(50)  NULL
  COMMENT 'A standalone noun to describe this type of champion (e.g. "tank" or "jungler")',
  adjective VARCHAR(50)  NULL
  COMMENT 'An adjective that can be prepended to " champions" to describe this type of champion (e.g. "squishy[ champions]" or "long-range[ champions]")',
  verb      VARCHAR(50)  NULL
  COMMENT 'A phrase that can be appended to "champions who " to describe this type of champion (e.g. "[champions who ]favor extended fights" or [champions who ]have lots of CC")',
  `_note`   VARCHAR(500) NULL
  COMMENT 'Notes about this tag (this are not used by the program or visible to users)',
  tag_id    SMALLINT(6) UNSIGNED AUTO_INCREMENT
    PRIMARY KEY,
  CONSTRAINT tags_tag_name_uindex
  UNIQUE (tag_name)
)
  ENGINE = InnoDB;

ALTER TABLE tag__champion
  ADD CONSTRAINT FX_tag_champion__tag_id
FOREIGN KEY (tag_id) REFERENCES tags (tag_id);

INSERT INTO patches (patch, start_date) VALUES ('7.22', '2017-11-08 11:04:00');
INSERT INTO patches (patch, start_date) VALUES ('7.23', '2017-11-21 12:17:00');
INSERT INTO patches (patch, start_date) VALUES ('7.24', '2017-12-06 11:58:00');

INSERT INTO summoners (summoner_id) VALUES (41271279);

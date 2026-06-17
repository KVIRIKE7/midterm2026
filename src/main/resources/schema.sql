-- UNO game history schema
-- Compatible with H2 (and standard SQL)

CREATE TABLE IF NOT EXISTS players (
    id      INTEGER PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS games (
    id         INTEGER PRIMARY KEY AUTO_INCREMENT,
    started_at TIMESTAMP NOT NULL,
    ended_at   TIMESTAMP,
    winner     VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS rounds (
    id          INTEGER PRIMARY KEY AUTO_INCREMENT,
    game_id     INTEGER NOT NULL REFERENCES games(id),
    round_num   INTEGER NOT NULL,
    winner      VARCHAR(100) NOT NULL,
    played_at   TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS scores (
    id        INTEGER PRIMARY KEY AUTO_INCREMENT,
    game_id   INTEGER NOT NULL REFERENCES games(id),
    player    VARCHAR(100) NOT NULL,
    score     INTEGER NOT NULL DEFAULT 0
);

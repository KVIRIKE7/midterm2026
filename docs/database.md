# Database Documentation

## Overview

Assignment 5 adds persistence to the UNO CLI game using **MyBatis** as the ORM framework and **H2** as the database engine.

- **Database:** H2 (file-based for runtime, in-memory for tests)
- **ORM:** MyBatis 3.5.16
- **Schema:** auto-created on first run via `schema.sql`

---

## Why H2?

H2 requires zero installation — it runs as a plain `.jar` dependency. The database is stored as a local file (`uno-data.mv.db`) in the working directory. No server setup needed.

---

## Schema

```sql
players   — not used as a separate table (names stored directly in games/scores)
games     — one row per game session (id, started_at, ended_at, winner)
rounds    — one row per round within a game (game_id, round_num, winner, played_at)
scores    — one row per player per game (game_id, player, score)
```

The schema is created automatically on startup by `GameRepository`. You do not need to run any SQL manually.

---

## Configuration

MyBatis is configured in `src/main/resources/mybatis-config.xml`.

Two environments are defined:

| Environment | URL | Used by |
|-------------|-----|---------|
| `production` | `jdbc:h2:file:./uno-data` | Normal game runs |
| `test` | `jdbc:h2:mem:uno_test` | `mvn test` (in-memory, isolated) |

No credentials are stored in source code. The H2 default (`sa` / empty password) is used for local development only.

---

## Schema Setup

The schema is applied automatically when the game starts. If you want to inspect or reset it manually:

```bash
# View the H2 database file directly using H2 console
java -jar ~/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar
# Then connect to: jdbc:h2:file:./uno-data
```

To reset all data, simply delete the database file:

```bash
rm uno-data.mv.db
```

---

## Running Persistence Tests

```bash
mvn test
```

Persistence tests use the in-memory `test` environment — no file is created, no cleanup needed.

---

## Viewing Game History

After playing one or more games, run the report:

```bash
java -jar target/uno-1.0.0.jar --report
```

This prints:
- Recent games (last 10) with timestamps and winners
- Player win counts
- Top 10 highest scores

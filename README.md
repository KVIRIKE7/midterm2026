# UNO CLI — Assignment 5

A command-line UNO game with Maven build tooling, logging, Docker support, and database persistence via MyBatis + H2.

## Requirements

- Java 11+
- Maven 3.8+
- Docker (optional)

---

## Local Build

```bash
mvn compile
```

## Local Test

```bash
mvn test
```

Runs both characterization tests and persistence tests. Persistence tests use an isolated in-memory H2 database — no setup required.

## Package

```bash
mvn package
```

Produces `target/uno-1.0.0.jar`.

## Local Run

```bash
java -jar target/uno-1.0.0.jar
```

### Options

| Flag | Description |
|------|-------------|
| `--bots N` | Number of bot players (default: 3) |
| `--games N` | Number of rounds to play (default: 1) |
| `--human` | Add a human player |
| `--quiet` | Suppress turn-by-turn output |
| `--seed N` | Set random seed |
| `--report` | Show game history and statistics |

### Examples

```bash
# Play 5 quiet rounds
java -jar target/uno-1.0.0.jar --games 5 --quiet

# View game history after playing
java -jar target/uno-1.0.0.jar --report
```

---

## Persistence

Game results are saved automatically to a local H2 database file (`uno-data.mv.db`) on every run.

See [docs/database.md](docs/database.md) for full details on schema, configuration, and how to reset data.

---

## Logging

Game events are written to `uno.log` in the working directory.

---

## Docker Build

```bash
docker build -t uno-game .
```

## Docker Run

```bash
docker run --rm uno-game --games 3 --quiet
```

View report from a persistent volume:

```bash
docker run --rm -v $(pwd)/data:/app/data uno-game --report
```

---

## Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── Main.java
│   │   │   ├── GameState.java
│   │   │   ├── Rules.java
│   │   │   ├── Cards.java
│   │   │   ├── BotStrategy.java
│   │   │   ├── ConsoleView.java
│   │   │   ├── DeckFactory.java
│   │   │   └── persistence/
│   │   │       ├── GameRepository.java
│   │   │       ├── GameMapper.java
│   │   │       ├── GameRecord.java
│   │   │       ├── RoundRecord.java
│   │   │       ├── ScoreRecord.java
│   │   │       └── WinCount.java
│   │   └── resources/
│   │       ├── mybatis-config.xml
│   │       ├── schema.sql
│   │       └── persistence/
│   │           └── GameMapper.xml
│   └── test/java/
│       ├── CharacterizationTests.java
│       └── PersistenceTests.java
├── docs/
│   └── database.md
├── pom.xml
├── Dockerfile
└── README.md
```

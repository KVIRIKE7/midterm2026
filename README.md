# UNO CLI — Final Project

A command-line UNO game built in Java with Maven, logging, Docker, and database persistence.

The game follows standard UNO rules and runs automatically across multiple rounds until a player reaches **500 points**.

---

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

Runs all three test suites:
- `CharacterizationTests` — card utilities, bot logic, deterministic game runs
- `PersistenceTests` — database layer using isolated in-memory H2
- `UnoRulesTest` — every UNO rule: deck, legal play, Skip, Reverse, Draw Two, Wild, Wild Draw Four, scoring, multi-round

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
| `--human` | Add yourself as a human player |
| `--quiet` | Suppress turn-by-turn output |
| `--seed N` | Set random seed for reproducibility |
| `--report` | Show game history and statistics |

### Examples

```bash
# Play against 2 bots
java -jar target/uno-1.0.0.jar --human --bots 2

# Watch 3 bots play quietly until someone hits 500
java -jar target/uno-1.0.0.jar --quiet

# View game history after playing
java -jar target/uno-1.0.0.jar --report
```

---

## How to Play

On your turn you will see your hand with index numbers:

```
Top card: R5
Your hand: 0:R3  1:BS  2:W
Choose card index/code or DRAW:
```

- Type the **index number** to play that card (e.g. `0`)
- Type `DRAW` to draw a card
- For Wild or Wild Draw Four, you will be asked to call a color: `R`, `Y`, `G`, or `B`
- When you reach one card, type `UNO` when prompted — missing it draws 2 penalty cards

The game runs across rounds automatically until someone reaches **500 points**.

---

## Persistence

Game results are saved automatically to a local H2 database file (`uno-data.mv.db`).

See [docs/database.md](docs/database.md) for full details.

To view statistics after playing:

```bash
java -jar target/uno-1.0.0.jar --report
```

---

## Logging

Game events are written to `uno.log` in the working directory including turns, cards played, draws, UNO calls, and round results.

---

## Docker Build

```bash
docker build -t uno-game .
```

## Docker Run

```bash
docker run --rm uno-game --quiet
```

With a persistent database volume:

```bash
docker run --rm -v $(pwd)/data:/app uno-game --quiet
```

View report from saved data:

```bash
docker run --rm -v $(pwd)/data:/app uno-game --report
```

---

## Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── Main.java               # Game loop and CLI input
│   │   │   ├── GameState.java          # Mutable game state
│   │   │   ├── Rules.java              # Card legality logic
│   │   │   ├── Cards.java              # Card string utilities
│   │   │   ├── BotStrategy.java        # Bot AI
│   │   │   ├── ConsoleView.java        # All console output
│   │   │   ├── DeckFactory.java        # Deck builder
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
│       ├── PersistenceTests.java
│       └── UnoRulesTest.java
├── docs/
│   ├── database.md
│   ├── rules-supported.md
│   └── final-report.md
├── pom.xml
├── Dockerfile
└── README.md
```

---

## Documentation

- [docs/database.md](docs/database.md) — database setup, schema, how to reset
- [docs/rules-supported.md](docs/rules-supported.md) — which UNO rules are implemented
- [docs/final-report.md](docs/final-report.md) — architecture, tests, limitations
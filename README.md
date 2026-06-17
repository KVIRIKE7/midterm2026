# UNO CLI — Assignment 4

A command-line UNO game built in Java, now with Maven build tooling, logging, and Docker support.

## Requirements

- Java 11+
- Maven 3.8+ (for local build/run)
- Docker (for containerised run)

---

## Local Build

```bash
mvn compile
```

## Local Test

```bash
mvn test
```

All characterization tests run automatically. Results are printed to the terminal.

## Package (create runnable JAR)

```bash
mvn package
```

This produces `target/uno-1.0.0.jar` — a fat jar with all dependencies included.

## Local Run

```bash
java -jar target/uno-1.0.0.jar
```

### Options

| Flag | Description |
|------|-------------|
| `--bots N` | Number of bot players (default: 3) |
| `--games N` | Number of games to play (default: 1) |
| `--human` | Add a human player |
| `--quiet` | Suppress turn-by-turn output |
| `--seed N` | Set random seed for reproducibility |

Example with a human player:
```bash
java -jar target/uno-1.0.0.jar --bots 2 --human
```

---

## Logging

Game events are written to `uno.log` in the working directory. Logged events include:

- Game session start/end
- Each player's turn
- Cards played and drawn
- Invalid input and illegal card attempts
- Round winner and points scored

---

## Docker Build

```bash
docker build -t uno-game .
```

## Docker Run

Bot-only game (default):
```bash
docker run --rm uno-game
```

With options (e.g. 2 bots, 5 games, quiet mode):
```bash
docker run --rm uno-game --bots 2 --games 5 --quiet
```

> **Note:** Interactive human mode (`--human`) requires a TTY:
> ```bash
> docker run --rm -it uno-game --human
> ```

---

## Project Structure

```
├── src/
│   ├── main/java/
│   │   ├── Main.java           # Entry point and game loop
│   │   ├── GameState.java      # All mutable game state
│   │   ├── Rules.java          # Card legality logic
│   │   ├── Cards.java          # Card string utilities
│   │   ├── BotStrategy.java    # Bot AI
│   │   ├── ConsoleView.java    # All console output
│   │   └── DeckFactory.java    # Deck builder
│   └── test/java/
│       └── CharacterizationTests.java  # JUnit 5 regression tests
├── pom.xml
├── Dockerfile
└── README.md
```

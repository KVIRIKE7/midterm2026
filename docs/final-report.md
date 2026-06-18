# Final Report

## What UNO Rules Are Implemented

All core UNO rules are implemented:

- **Deck:** Standard 108-card deck with 4 colors, numbers 0–9, Skip, Reverse, Draw Two, Wild, Wild Draw Four
- **Legal play:** Cards matched by color, number, or action type; wilds always playable
- **Skip:** Next player loses turn
- **Reverse:** Direction flips; acts as Skip in 2-player games
- **Draw Two:** Next player draws 2 and loses turn
- **Wild:** Player calls the next color
- **Wild Draw Four:** Player calls color, next player draws 4 and loses turn
- **Draw/pass:** Player draws one card if no legal play; may play it immediately if legal
- **UNO call:** Announced automatically; human players must type `UNO` or draw 2 as penalty
- **Round scoring:** Winner earns points from others' remaining hands (number = face value, specials = 20, wilds = 50)
- **Multi-round:** Game runs across rounds until a player reaches 500 points

Not implemented: Wild Draw Four challenge rule, Draw Two stacking.

See `docs/rules-supported.md` for the full rule-by-rule breakdown.

---

## How to Play from the CLI

Build and run:

```bash
mvn package
java -jar target/uno-1.0.0.jar --human --bots 2
```

On your turn you will see:
- The current top card
- Your hand with index numbers (e.g. `0:R5 1:BS 2:W`)

Type the **index number** of the card you want to play, or type `DRAW` to draw a card.

When you play a Wild or Wild Draw Four, type the color you want: `R`, `Y`, `G`, or `B`.

When you reach one card, type `UNO` when prompted. If you miss it, you draw 2 penalty cards.

The game runs automatically until someone hits 500 points.

To see game history after playing:
```bash
java -jar target/uno-1.0.0.jar --report
```

---

## Architecture: How Game Logic Is Separated from the CLI

The project separates concerns across clearly defined classes:

| Class | Responsibility |
|-------|---------------|
| `Rules.java` | Card legality logic — no I/O, fully testable |
| `Cards.java` | Card string utilities (color, rank, points) — no I/O |
| `DeckFactory.java` | Builds the standard 108-card deck — no I/O |
| `BotStrategy.java` | Bot decision logic — no I/O, fully testable |
| `GameState.java` | All mutable game state (hands, deck, scores, turn order) |
| `ConsoleView.java` | All console output — separated from logic |
| `Main.java` | Game loop and CLI input handling |
| `persistence/` | Database layer (MyBatis + H2) — separated from game logic |

Game rules (`Rules`, `Cards`, `BotStrategy`, `DeckFactory`) contain zero console I/O. This means they can be tested directly without mocking or simulating input, as shown in `UnoRulesTest.java`.

The CLI (`Main.java`) handles input and delegates to the logic layer. `ConsoleView.java` handles all output so display logic is not scattered through the game loop.

---

## What Tests Were Added

Three test classes cover the full project:

### `CharacterizationTests.java`
Original tests from the midterm. Cover card utilities, legal play, bot behavior, deck composition, scoring, and deterministic game runs with fixed seeds.

### `PersistenceTests.java`
Added in Assignment 5. Seven tests covering the MyBatis + H2 persistence layer: saving games, rounds, scores, querying recent games, win counts, and top scores. Uses in-memory H2 so no setup is needed.

### `UnoRulesTest.java`
Added in the final project. Covers every rule category:
- Deck composition (108 cards, correct counts per card type)
- Legal play validation (color, number, action, wild, illegal cases)
- Skip behavior and legality
- Reverse (3-player direction change, 2-player skip behavior)
- Draw Two (cards given, turn skipped)
- Wild (always playable, called color affects next play)
- Wild Draw Four (4 cards given, turn skipped)
- Draw/pass (bot draws when no legal card, plays when legal)
- Scoring (face value, specials, wilds, hand totals)
- Multi-round (scores accumulate, target is 500, deterministic seed check)

Run all tests with:
```bash
mvn test
```

---

## Limitations

- **Wild Draw Four challenge:** Not implemented. The next player always draws 4 without being able to challenge.
- **Draw Two stacking:** Not implemented. Players cannot stack Draw Two cards.
- **Bot difficulty:** All bots use the same simple strategy — play the highest-priority legal card. No lookahead or adaptive play.
- **UNO penalty for bots:** Bots automatically call UNO. Only human players can miss the UNO call.
- **Multiplayer:** CLI only supports local single-machine play. No network multiplayer.
- **Persistence in Docker:** The H2 database file is written to the container's working directory. It is lost when the container stops unless a volume is mounted.

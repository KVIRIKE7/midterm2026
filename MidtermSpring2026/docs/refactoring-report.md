# Refactoring Report

## What behavior was characterized before refactoring?

Before touching any production code, 114 characterization tests were written in `CharacterizationTests.java` covering:

- `color()` — all four colors and wild cards returning empty string
- `rank()` — every rank string including the "RR" ambiguity (starts with R for color, ends with R for reverse)
- `number()` — digits 0-9 and -1 for non-number cards
- `points()` — face values, 20 for action cards, 50 for wilds
- `isLegal()` — all five matching rules: wild always legal, called color, color match, action-type match, number match; plus negative cases
- `chooseBotCard()` — priority order (DRAW_TWO > SKIP > NUMBER > WILD), last-resort -1 when nothing legal
- `chooseBotColor()` — majority color wins, tie-break order R>Y>G>B, all-wild hand returns R
- Deck composition — 108 cards, 25 per color, exactly one 0 per color, two of each other non-wild
- Scoring — the exact example from `docs/rules.html` (R5+B9+GS+W = 84)
- `join()` — hand display format "0:R5 1:W 2:G+2"
- End-to-end deterministic games — three seeded runs pin exact final scores

The most important characterization was the **end-to-end seed test**: given the same `--seed`, the same final scores must appear after refactoring. This catches any change to the RNG consumption order or rule evaluation.

## What were the worst design problems found?

In priority order of risk:

1. **Duplicated legality check** — the five-branch isLegal logic appeared verbatim in three places: the main turn loop (bot path inline), `chooseBotCard` (inlined four times), and `isLegal()`. Any rule change had to be made in all copies.
2. **Global mutable state** — every method read from static fields (`upCard`, `calledColor`, `direction`, `currentPlayer`) making it impossible to test a single game rule without side effects on every other.
3. **Console I/O tangled with game logic** — `System.out.println` calls scattered through `playGame()` made it impossible to run the game silently for tests without the `quiet` flag workaround.
4. **Deck building inside playGame()** — the 108-card deck was assembled inline with no way to verify composition without running a full game.
5. **Bot strategy inlined** — the four-pass priority search was duplicated logic embedded in the game loop with no independent testability.

## Which refactorings were performed?

All steps were done incrementally; characterization tests were re-run after each step.

**Step 1: Extract `Cards` class**
Moved `color()`, `rank()`, `number()`, `points()`, and `join()` into a stateless utility class. No logic changed. `Main` delegates via wrapper methods so `selfTest` and `CharacterizationTests` continue to work unchanged.

**Step 2: Extract `Rules` class**
Moved `isLegal()` into a class that takes all inputs as parameters (no global state). The five-branch matching logic now has one canonical home. Both `Main` and `CharacterizationTests` call `Rules.isLegal()` directly.

**Step 3: Extract `GameState` class**
Moved all mutable game fields (`deck`, `discard`, `hands`, `playerNames`, `humanPlayers`, `scores`, `currentPlayer`, `direction`, `upCard`, `calledColor`) into a single object. `Main` keeps legacy static fields synchronized for backward compatibility with `CharacterizationTests`. `draw()` and `advanceTurn()` moved onto `GameState` since they only operate on its own data.

**Step 4: Extract `BotStrategy` class**
Moved `chooseBotCard` and `chooseBotColor` into `BotStrategy.chooseCard()` and `BotStrategy.chooseColor()`. Both take explicit parameters; neither reads global state. The four-copy inline isLegal duplication inside the bot loop was eliminated — every legality check now calls `Rules.isLegal()`.

**Step 5: Extract `ConsoleView` class**
All `System.out.println` calls in `playGame()` were replaced with named method calls on a `ConsoleView` instance. The `quiet` flag moved into `ConsoleView`. `playGame()` now only orchestrates; it does not format output.

**Step 6: Extract `DeckFactory` class**
The inline deck-building block was moved to `DeckFactory.buildStandardDeck()`. The `CharacterizationTests.testDeckComposition()` test now uses this factory directly, verifying deck shape without running a game.

## What behavior was intentionally preserved?

Everything documented in `docs/rules.html` was preserved:

- All hands are visible in the terminal (not hidden)
- Human players may type `draw` even with a legal card available
- Illegal index input causes a penalty card and turn loss (the out-of-bounds check before the legality check)
- Typed card codes that fail the legality check print "That card is not legal." and re-prompt (no penalty)
- Bot players automatically play a drawn card when it is legal
- Reverse in a 2-player game acts as a skip
- Wild cards skip out of the initial draw loop (wilds cannot be the starting upCard)
- Safety limit stops the game at 3000 turns
- `--seed` produces fully deterministic output

## What risks remain?

- **Legacy static fields in `Main`** — `CharacterizationTests` reads `Main.upCard`, `Main.calledColor`, and `Main.scores` directly. The `syncFromState()` method keeps them in sync, but it is a maintenance hazard. The correct fix is to update the tests to read from `state` directly, then delete the legacy fields. That step was not taken to avoid making the test update the scope of one refactoring.
- **`playGame()` is still long** — the core turn loop still handles draw, penalty, play, wild color, UNO call, win check, and effect application in sequence. `applyCardEffect()` was extracted, but the loop itself would benefit from further extraction into a `TurnProcessor` or similar.
- **Human input is still in `Main`** — `askHuman()` and `askColor()` were not moved to `ConsoleView` because they involve both reading input and returning values used by game logic. Moving them requires deciding whether the view owns the scanner. This is the next natural step.
- **Scores array duplication** — `GameState.scores` and `Main.scores` are separate arrays kept in sync by copying. A future step should make `Main.scores` reference `state.scores` directly.

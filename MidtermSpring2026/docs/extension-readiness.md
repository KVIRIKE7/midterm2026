# Extension Readiness

## Which extension would this design support best?

**Replacing or improving the CLI view** — for example, adding color to terminal output, printing a compact board summary, adding a replay log, or swapping in a different output format entirely.

## Where would that change be implemented?

All display output now lives in `ConsoleView`. Adding ANSI color codes, reformatting the hand display, or writing a `ReplayLogView` that records each event to a file would require only changes to `ConsoleView` (or a new class that implements the same interface). The game loop in `Main.playGame()` calls the view methods by name and does not know or care what they write.

Adding a replay log specifically would look like this: replace `ConsoleView` with a `TeeView` that writes to the console *and* appends structured lines to a file. Zero changes to game logic.

## What part of the design still makes change difficult?

**Human input is still coupled to `Main`.**  `askHuman()` and `askColor()` live in `Main` and read from `Main.scanner`. They are not part of `ConsoleView` because they return values that flow back into the game loop. As long as they stay in `Main`, replacing the input channel (for example, a scripted input for automated testing, or a GUI input dialog) requires editing `Main`.

The fix is to extract an interface like `PlayerInput` with `chooseCard(hand)` and `chooseColor()` methods, pass it into `playGame()`, and implement it with `ConsolePlayerInput` for the CLI. Until that is done, testing the human-input path requires redirecting `System.in`, which is fragile.

**The turn loop is still a long method.** `playGame()` still sequences draw, penalty check, play, wild, UNO, win, and effect in one block. Adding a new phase (for example, a "challenge W4" step) means editing a long method rather than inserting a small handler. Extracting a `TurnProcessor` or a chain of small turn-phase methods would make rule variants easier to add.

#!/bin/bash
set -e
mkdir -p out
javac -d out src/Cards.java src/Rules.java src/GameState.java src/BotStrategy.java src/DeckFactory.java src/ConsoleView.java src/Main.java src/CharacterizationTests.java
echo "Compiled successfully."
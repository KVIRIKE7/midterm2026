import persistence.GameRecord;
import persistence.GameRepository;
import persistence.ScoreRecord;
import persistence.WinCount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.io.IOException;

public class Main {

    static final Logger logger = Logger.getLogger("UNO");

    static ArrayList<String> playerNames   = new ArrayList<>();
    static ArrayList<Boolean> humanPlayers = new ArrayList<>();
    static ArrayList<ArrayList<String>> hands = new ArrayList<>();
    static ArrayList<String> deck    = new ArrayList<>();
    static ArrayList<String> discard  = new ArrayList<>();
    static int[] scores = new int[10];
    static int currentPlayer = 0;
    static int direction = 1;
    static String upCard = "";
    static String calledColor = "";
    static boolean quiet = false;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);
    static final int TARGET_SCORE = 500;
    static GameState state;

    // Persistence — null when running tests that don't need DB
    static GameRepository repository;

    static void setupLogging() {
        try {
            FileHandler fh = new FileHandler("uno.log", true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Warning: could not open log file.");
        }
    }

    public static void main(String[] args) {
        setupLogging();

        int bots = 3;
        int games = 1;
        boolean human = false;
        boolean report = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                games = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--report")) {
                report = true;
            } else if (args[i].equals("--self-test")) {
                selfTest();
                return;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: java -jar uno.jar [--bots N] [--games N] [--human] [--quiet] [--seed N] [--report]");
                return;
            }
        }

        // Initialise database
        repository = new GameRepository("production");

        if (report) {
            printReport();
            return;
        }

        random = new Random(seed);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        logger.info("Game session started: bots=" + bots + " games=" + games + " seed=" + seed);

        ConsoleView view = new ConsoleView(quiet);

        GameRecord gameRecord = repository.startGame();
        int roundNum = 0;

        String overallWinner = null;

        while (overallWinner == null) {
            roundNum++;
            if (!quiet) System.out.println("\n=== Round " + roundNum + " ===");
            String roundWinner = playGame(view);
            if (roundWinner != null) {
                repository.saveRound(gameRecord.id, roundNum, roundWinner);
                for (int i = 0; i < playerNames.size(); i++) {
                    if (scores[i] >= TARGET_SCORE) {
                        overallWinner = playerNames.get(i);
                        break;
                    }
                }
            }
        }

        repository.endGame(gameRecord, overallWinner);
        repository.saveScores(gameRecord.id, playerNames, scores);
        System.out.println("\n*** " + overallWinner + " wins with " + TARGET_SCORE + "+ points! ***");

        // Determine overall winner (highest score)
        String sessionWinner = playerNames.get(0);
        for (int i = 1; i < playerNames.size(); i++) {
            if (scores[i] > scores[playerNames.indexOf(sessionWinner)]) {
                sessionWinner = playerNames.get(i);
            }
        }

        repository.endGame(gameRecord, sessionWinner);
        repository.saveScores(gameRecord.id, playerNames, scores);

        view.showFinalScores(playerNames, scores);
        logger.info("Game session ended. Overall winner: " + sessionWinner);
    }

    static void printReport() {
        System.out.println("\n===== UNO GAME HISTORY REPORT =====\n");

        System.out.println("-- Recent Games (last 10) --");
        List<GameRecord> recent = repository.recentGames(10);
        if (recent.isEmpty()) {
            System.out.println("  No games recorded yet.");
        } else {
            for (GameRecord g : recent) {
                System.out.println("  Game #" + g.id
                        + " | Started: " + g.startedAt
                        + " | Winner: " + (g.winner != null ? g.winner : "N/A"));
            }
        }

        System.out.println("\n-- Player Win Counts --");
        List<WinCount> wins = repository.playerWinCounts();
        if (wins.isEmpty()) {
            System.out.println("  No wins recorded yet.");
        } else {
            for (WinCount w : wins) {
                System.out.println("  " + w.player + ": " + w.wins + " win(s)");
            }
        }

        System.out.println("\n-- Highest Scores (top 10) --");
        List<ScoreRecord> top = repository.highestScores(10);
        if (top.isEmpty()) {
            System.out.println("  No scores recorded yet.");
        } else {
            for (ScoreRecord s : top) {
                System.out.println("  " + s.player + ": " + s.score + " pts (game #" + s.gameId + ")");
            }
        }

        System.out.println("\n===================================\n");
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<>());
        }
        state = new GameState(playerNames, humanPlayers, hands, 10);
    }

    static void playGame() {
        playGame(new ConsoleView(quiet));
    }

    // Returns the name of the round winner, or null if safety limit hit
    static String playGame(ConsoleView view) {
        state.deck.clear();
        state.deck.addAll(DeckFactory.buildStandardDeck());
        state.shuffleDeck(random);
        deck = state.deck;
        discard = state.discard;

        state.discard.clear();
        for (ArrayList<String> h : state.hands) h.clear();

        for (int i = 0; i < state.playerCount(); i++) {
            for (int j = 0; j < 7; j++) state.hand(i).add(state.draw(random));
        }

        state.upCard = state.draw(random);
        while (state.upCard.startsWith("W")) {
            state.discard.add(state.upCard);
            state.upCard = state.draw(random);
        }
        state.calledColor  = "";
        state.direction    = 1;
        state.currentPlayer = random.nextInt(state.playerCount());
        syncFromState();

        logger.info("Round started. Up card: " + state.upCard + ". First player: " + state.currentPlayerName());

        int guard = 0;
        while (guard < 3000) {
            guard++;

            String name = state.currentPlayerName();
            ArrayList<String> hand = state.currentHand();

            logger.info("Player turn: " + name + " | Up: " + state.upCard + " | Hand size: " + hand.size());
            view.showTurnHeader(state.upCard, state.calledColor, name, hand);

            int chosen = state.currentPlayerIsHuman()
                    ? askHuman(hand)
                    : chooseBotCard(hand);

            if (chosen == -1) {
                String drawn = state.draw(random);
                hand.add(drawn);
                logger.info(name + " draws: " + drawn);
                view.showDraw(name, drawn);
                if (Rules.isLegal(drawn, state.upCard, state.calledColor)) {
                    if (!state.currentPlayerIsHuman()) {
                        chosen = hand.size() - 1;
                    } else {
                        System.out.print("Play drawn card " + drawn + "? y/n: ");
                        String answer = scanner.nextLine();
                        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    logger.warning(name + " invalid index — penalty.");
                    view.showPenaltyInvalidIndex(name);
                    hand.add(state.draw(random));
                    state.advanceTurn();
                    syncFromState();
                    continue;
                }

                String card = hand.get(chosen);
                if (!Rules.isLegal(card, state.upCard, state.calledColor)) {
                    logger.warning(name + " illegal card " + card + " — penalty.");
                    view.showPenaltyIllegalCard(name, card);
                    hand.add(state.draw(random));
                    state.advanceTurn();
                    syncFromState();
                    continue;
                }

                hand.remove(chosen);
                state.discard.add(state.upCard);
                state.upCard   = card;
                state.calledColor = "";
                logger.info(name + " plays " + card);
                view.showPlay(name, card);

                if (card.equals("W") || card.equals("W4")) {
                    state.calledColor = state.currentPlayerIsHuman()
                            ? askColor()
                            : chooseBotColor(hand);
                    logger.info(name + " calls color: " + state.calledColor);
                    view.showCalledColor(name, state.calledColor);
                }

                if (hand.size() == 1) {
                    view.showUno(name);
                    if (state.currentPlayerIsHuman()) {
                        System.out.print("Say UNO! type 'UNO' or miss it: ");
                        String unoCall = scanner.nextLine().trim().toUpperCase();
                        if (!unoCall.equals("UNO")) {
                            System.out.println("Missed UNO! You draw 2 penalty cards.");
                            logger.info(name + " missed UNO — penalty 2 cards.");
                            hand.add(state.draw(random));
                            hand.add(state.draw(random));
                        }
                    }
                }

                if (hand.isEmpty()) {
                    int points = 0;
                    for (int i = 0; i < state.hands.size(); i++) {
                        if (i != state.currentPlayer) {
                            for (String c : state.hand(i)) points += Cards.points(c);
                        }
                    }
                    state.scores[state.currentPlayer] += points;
                    scores[state.currentPlayer] = state.scores[state.currentPlayer];
                    logger.info("Round ended. Winner: " + name + " | Points: " + points);
                    view.showWin(name, points);
                    syncFromState();
                    return name;
                }

                applyCardEffect(card, view);

            } else {
                state.advanceTurn();
            }
            syncFromState();
        }
        logger.warning("Safety limit reached.");
        view.showSafetyLimit();
        return null;
    }

    static void applyCardEffect(String card, ConsoleView view) {
        String r = Cards.rank(card);
        if (r.equals("SKIP")) {
            state.advanceTurn();
            state.advanceTurn();
        } else if (r.equals("REVERSE")) {
            state.direction *= -1;
            state.advanceTurn();
            if (state.playerCount() == 2) state.advanceTurn();
        } else if (r.equals("DRAW_TWO")) {
            state.advanceTurn();
            state.currentHand().add(state.draw(random));
            state.currentHand().add(state.draw(random));
            view.showDrawsTwo(state.currentPlayerName());
            state.advanceTurn();
        } else if (r.equals("WILD_DRAW_FOUR")) {
            state.advanceTurn();
            for (int i = 0; i < 4; i++) state.currentHand().add(state.draw(random));
            view.showDrawsFour(state.currentPlayerName());
            state.advanceTurn();
        } else {
            state.advanceTurn();
        }
    }

    static void syncFromState() {
        currentPlayer = state.currentPlayer;
        direction     = state.direction;
        upCard        = state.upCard;
        calledColor   = state.calledColor;
        for (int i = 0; i < state.scores.length; i++) scores[i] = state.scores[i];
    }

    static int chooseBotCard(ArrayList<String> hand) {
        return BotStrategy.chooseCard(hand, state.upCard, state.calledColor);
    }

    static String chooseBotColor(ArrayList<String> hand) {
        return BotStrategy.chooseColor(hand);
    }

    static int askHuman(ArrayList<String> hand) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) return -1;
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) return index;
            } catch (Exception ignored) {}
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (Rules.isLegal(hand.get(i), state.upCard, state.calledColor)) return i;
                    System.out.println("That card is not legal.");
                    logger.info("Human invalid card: " + input);
                }
            }
            System.out.println("Card not found.");
            logger.info("Human invalid input: " + input);
        }
    }

    static String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R")) return "R";
            if (input.equals("Y")) return "Y";
            if (input.equals("G")) return "G";
            if (input.equals("B")) return "B";
            System.out.println("Bad color.");
            logger.info("Human invalid color: " + input);
        }
    }

    static String  color(String card)                            { return Cards.color(card); }
    static String  rank(String card)                             { return Cards.rank(card); }
    static int     number(String card)                           { return Cards.number(card); }
    static int     points(String card)                           { return Cards.points(card); }
    static String  join(ArrayList<String> cards)                 { return Cards.join(cards); }
    static boolean isLegal(String card, String up, String call) { return Rules.isLegal(card, up, call); }

    static void next() {
        state.advanceTurn();
        syncFromState();
    }

    static void selfTest() {
        int passed = 0;
        if (color("R5").equals("R")) passed++; else fail("color R5");
        if (rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (points("W4") == 50) passed++; else fail("wild points");
        if (isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");

        ArrayList<String> h = new ArrayList<>();
        h.add("B3"); h.add("R4"); h.add("W");
        state = new GameState(playerNames, humanPlayers, hands, 10);
        state.upCard = "R9"; state.calledColor = "";
        upCard = "R9"; calledColor = "";
        if (chooseBotCard(h) == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<>();
        h2.add("B1"); h2.add("B2"); h2.add("R3");
        if (chooseBotColor(h2).equals("B")) passed++; else fail("bot color");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) { throw new RuntimeException("Failed: " + name); }
}

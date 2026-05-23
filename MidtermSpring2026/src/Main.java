import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class Main {
    // Legacy static fields kept so CharacterizationTests can read/write them
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
    static GameState state;

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
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
            } else if (args[i].equals("--self-test")) {
                selfTest();
                return;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        random = new Random(seed);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        ConsoleView view = new ConsoleView(quiet);

        for (int g = 1; g <= games; g++) {
            view.showGameHeader(g);
            playGame(view);
        }

        view.showFinalScores(playerNames, scores);
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

    static void playGame(ConsoleView view) {
        // Build and shuffle deck
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

        int guard = 0;
        while (guard < 3000) {
            guard++;

            String name = state.currentPlayerName();
            ArrayList<String> hand = state.currentHand();

            view.showTurnHeader(state.upCard, state.calledColor, name, hand);

            int chosen = state.currentPlayerIsHuman()
                    ? askHuman(hand)
                    : chooseBotCard(hand);

            if (chosen == -1) {
                String drawn = state.draw(random);
                hand.add(drawn);
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
                    view.showPenaltyInvalidIndex(name);
                    hand.add(state.draw(random));
                    state.advanceTurn();
                    syncFromState();
                    continue;
                }

                String card = hand.get(chosen);
                if (!Rules.isLegal(card, state.upCard, state.calledColor)) {
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
                view.showPlay(name, card);

                if (card.equals("W") || card.equals("W4")) {
                    state.calledColor = state.currentPlayerIsHuman()
                            ? askColor()
                            : chooseBotColor(hand);
                    view.showCalledColor(name, state.calledColor);
                }

                if (hand.size() == 1) view.showUno(name);

                if (hand.isEmpty()) {
                    int points = 0;
                    for (int i = 0; i < state.hands.size(); i++) {
                        if (i != state.currentPlayer) {
                            for (String c : state.hand(i)) points += Cards.points(c);
                        }
                    }
                    state.scores[state.currentPlayer] += points;
                    scores[state.currentPlayer] = state.scores[state.currentPlayer];
                    view.showWin(name, points);
                    syncFromState();
                    return;
                }

                applyCardEffect(card, view);

            } else {
                state.advanceTurn();
            }
            syncFromState();
        }
        view.showSafetyLimit();
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

    // -----------------------------------------------------------------------
    // Bot strategy (delegates to BotStrategy)
    // -----------------------------------------------------------------------
    static int chooseBotCard(ArrayList<String> hand) {
        return BotStrategy.chooseCard(hand, state.upCard, state.calledColor);
    }

    static String chooseBotColor(ArrayList<String> hand) {
        return BotStrategy.chooseColor(hand);
    }

    // -----------------------------------------------------------------------
    // Console I/O
    // -----------------------------------------------------------------------
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
                }
            }
            System.out.println("Card not found.");
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
        }
    }

    // -----------------------------------------------------------------------
    // Backward-compat delegates
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // selfTest — original checks preserved verbatim
    // -----------------------------------------------------------------------
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
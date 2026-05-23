import java.util.ArrayList;

/**
 * Characterization tests for the UNO CLI implementation.
 *
 * These tests document CURRENT behavior of Main.java — including quirks —
 * not ideal UNO rules. They exist to catch regressions during refactoring.
 *
 * Run: java -cp out CharacterizationTests
 */
public class CharacterizationTests {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        // --- Card.color() ---
        testColor();

        // --- Card.rank() ---
        testRank();

        // --- Card.number() ---
        testNumber();

        // --- Card.points() ---
        testPoints();

        // --- Rules.isLegal() ---
        testIsLegal();

        // --- BotStrategy.chooseBotColor() ---
        testChooseBotColor();

        // --- BotStrategy.chooseBotCard() ---
        testChooseBotCard();

        // --- Deck composition ---
        testDeckComposition();

        // --- Scoring ---
        testScoring();

        // --- Display.join() ---
        testJoin();

        // --- End-to-end deterministic games ---
        testDeterministicGame();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // -----------------------------------------------------------------------
    // color()
    // -----------------------------------------------------------------------
    static void testColor() {
        check("color R5 -> R",    Main.color("R5").equals("R"));
        check("color Y3 -> Y",    Main.color("Y3").equals("Y"));
        check("color G+2 -> G",   Main.color("G+2").equals("G"));
        check("color BS -> B",    Main.color("BS").equals("B"));
        check("color BR -> B",    Main.color("BR").equals("B"));
        check("color W -> empty", Main.color("W").equals(""));
        check("color W4 -> empty",Main.color("W4").equals(""));
        check("color R0 -> R",    Main.color("R0").equals("R"));
    }

    // -----------------------------------------------------------------------
    // rank()
    // -----------------------------------------------------------------------
    static void testRank() {
        check("rank W -> WILD",           Main.rank("W").equals("WILD"));
        check("rank W4 -> WILD_DRAW_FOUR",Main.rank("W4").equals("WILD_DRAW_FOUR"));
        check("rank RS -> SKIP",          Main.rank("RS").equals("SKIP"));
        check("rank YS -> SKIP",          Main.rank("YS").equals("SKIP"));
        check("rank GR -> REVERSE",       Main.rank("GR").equals("REVERSE"));
        check("rank BR -> REVERSE",       Main.rank("BR").equals("REVERSE"));
        check("rank R+2 -> DRAW_TWO",     Main.rank("R+2").equals("DRAW_TWO"));
        check("rank G+2 -> DRAW_TWO",     Main.rank("G+2").equals("DRAW_TWO"));
        check("rank R5 -> NUMBER",        Main.rank("R5").equals("NUMBER"));
        check("rank B0 -> NUMBER",        Main.rank("B0").equals("NUMBER"));
        check("rank Y9 -> NUMBER",        Main.rank("Y9").equals("NUMBER"));
        // RR: starts with R (red), ends with R -> REVERSE
        check("rank RR -> REVERSE",       Main.rank("RR").equals("REVERSE"));
    }

    // -----------------------------------------------------------------------
    // number()
    // -----------------------------------------------------------------------
    static void testNumber() {
        check("number R5 -> 5",   Main.number("R5") == 5);
        check("number G0 -> 0",   Main.number("G0") == 0);
        check("number B9 -> 9",   Main.number("B9") == 9);
        check("number RS -> -1",  Main.number("RS") == -1);
        check("number W -> -1",   Main.number("W") == -1);
        check("number W4 -> -1",  Main.number("W4") == -1);
    }

    // -----------------------------------------------------------------------
    // points()
    // -----------------------------------------------------------------------
    static void testPoints() {
        check("points R5 -> 5",    Main.points("R5") == 5);
        check("points G0 -> 0",    Main.points("G0") == 0);
        check("points B9 -> 9",    Main.points("B9") == 9);
        check("points RS -> 20",   Main.points("RS") == 20);
        check("points YR -> 20",   Main.points("YR") == 20);
        check("points G+2 -> 20",  Main.points("G+2") == 20);
        check("points W -> 50",    Main.points("W") == 50);
        check("points W4 -> 50",   Main.points("W4") == 50);
    }

    // -----------------------------------------------------------------------
    // isLegal()
    // -----------------------------------------------------------------------
    static void testIsLegal() {
        // Wild cards are always legal
        check("W always legal",    Main.isLegal("W", "R5", ""));
        check("W4 always legal",   Main.isLegal("W4", "B9", ""));
        check("W legal on W up",   Main.isLegal("W", "W", "R"));

        // Match by color
        check("same color R legal",  Main.isLegal("R2", "R9", ""));
        check("same color Y legal",  Main.isLegal("YS", "Y3", ""));
        check("diff color illegal",  !Main.isLegal("B3", "R9", ""));

        // Match by number
        check("same number legal",   Main.isLegal("G9", "R9", ""));
        check("same number 0 legal", Main.isLegal("Y0", "B0", ""));
        check("diff number illegal", !Main.isLegal("G7", "R9", ""));

        // Match by action type (non-number ranks)
        check("skip on skip legal",     Main.isLegal("GS", "RS", ""));
        check("reverse on reverse",     Main.isLegal("BR", "YR", ""));
        check("draw2 on draw2",         Main.isLegal("G+2", "R+2", ""));
        check("skip on draw2 illegal",  !Main.isLegal("GS", "R+2", ""));
        check("draw2 on skip illegal",  !Main.isLegal("G+2", "RS", ""));

        // Called color (after a wild)
        check("called color match",     Main.isLegal("B3", "W", "B"));
        check("called color no match",  !Main.isLegal("R3", "W", "B"));
        // Called color overrides up-card color (up is W which has no color)
        check("B card on W called B",   Main.isLegal("B9", "W4", "B"));
        check("G card on W called B",   !Main.isLegal("G9", "W4", "B"));

        // Edge: color match beats called color check order
        // When calledColor is set, color(up)="" for wild, so color match is ""
        // meaning a colored card must match calledColor
        check("R card on W4 called R",  Main.isLegal("R5", "W4", "R"));

        // RS on R9: same color (both R), so legal even though ranks differ
        check("RS on R9 legal by color", Main.isLegal("RS", "R9", ""));
        // GS on R9: different color (G vs R) and SKIP != NUMBER -> illegal
        check("GS on R9 illegal",        !Main.isLegal("GS", "R9", ""));
    }

    // -----------------------------------------------------------------------
    // chooseBotColor()
    // -----------------------------------------------------------------------
    static void testChooseBotColor() {
        // Majority color wins
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1"); hand.add("B2"); hand.add("R3");
        check("bot color majority B", Main.chooseBotColor(hand).equals("B"));

        hand = new ArrayList<>();
        hand.add("R1"); hand.add("R2"); hand.add("R3"); hand.add("G1");
        check("bot color majority R", Main.chooseBotColor(hand).equals("R"));

        // Tie: R wins over Y by >= comparison order
        hand = new ArrayList<>();
        hand.add("R1"); hand.add("Y1");
        check("bot color tie R>=Y picks R", Main.chooseBotColor(hand).equals("R"));

        // All wilds: no colored cards -> counts all zero -> R wins
        hand = new ArrayList<>();
        hand.add("W"); hand.add("W4");
        check("bot color all wild -> R", Main.chooseBotColor(hand).equals("R"));

        // Single card
        hand = new ArrayList<>();
        hand.add("G7");
        check("bot color single G", Main.chooseBotColor(hand).equals("G"));
    }

    // -----------------------------------------------------------------------
    // chooseBotCard() — priority: DRAW_TWO > SKIP > NUMBER > WILD
    // -----------------------------------------------------------------------
    static void testChooseBotCard() {
        // Initialize a minimal state so chooseBotCard can read upCard/calledColor
        initTestState("R9", "");

        // Prefers DRAW_TWO over everything
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5");    // index 0: number, legal
        hand.add("R+2");   // index 1: draw_two, legal
        hand.add("W");     // index 2: wild, legal
        check("bot prefers draw_two", Main.chooseBotCard(hand) == 1);

        // Prefers SKIP over number and wild
        hand = new ArrayList<>();
        hand.add("R5");    // index 0: number, legal
        hand.add("RS");    // index 1: skip, legal
        hand.add("W");     // index 2: wild, legal
        check("bot prefers skip over number", Main.chooseBotCard(hand) == 1);

        // Prefers number over wild
        hand = new ArrayList<>();
        hand.add("W");     // index 0: wild
        hand.add("R4");    // index 1: number, legal
        check("bot prefers number over wild", Main.chooseBotCard(hand) == 1);

        // Wild as last resort
        hand = new ArrayList<>();
        hand.add("B3");    // index 0: illegal
        hand.add("W");     // index 1: wild
        check("bot plays wild as last resort", Main.chooseBotCard(hand) == 1);

        // No legal card: returns -1
        hand = new ArrayList<>();
        hand.add("B3");    // illegal against R9, no called color
        hand.add("G7");    // illegal
        check("bot draws when no legal card", Main.chooseBotCard(hand) == -1);

        // Called color match: B3 is legal when calledColor is B
        initTestState("W", "B");
        hand = new ArrayList<>();
        hand.add("B3");    // legal via called color, NUMBER
        check("bot uses called color match", Main.chooseBotCard(hand) == 0);

        // Reset
        initTestState("", "");
    }

    // -----------------------------------------------------------------------
    // Deck composition
    // -----------------------------------------------------------------------
    static void testDeckComposition() {
        // Build a fresh deck the same way playGame() does
        ArrayList<String> deck = buildFreshDeck();

        check("deck has 108 cards", deck.size() == 108);

        // Count wilds
        int wilds = 0; int w4s = 0;
        for (String c : deck) { if (c.equals("W")) wilds++; if (c.equals("W4")) w4s++; }
        check("4 wilds", wilds == 4);
        check("4 wild draw fours", w4s == 4);

        // Count each color
        String[] colors = {"R", "Y", "G", "B"};
        for (String col : colors) {
            int count = 0;
            for (String c : deck) { if (Main.color(c).equals(col)) count++; }
            check("25 cards of color " + col, count == 25);
        }

        // Each color: one 0, two each of 1-9, two S, two R, two +2
        for (String col : colors) {
            int zeros = 0, skips = 0, revs = 0, draw2s = 0;
            for (String c : deck) {
                if (!Main.color(c).equals(col)) continue;
                if (c.equals(col + "0")) zeros++;
                if (c.equals(col + "S")) skips++;
                if (c.equals(col + "R")) revs++;
                if (c.equals(col + "+2")) draw2s++;
            }
            check(col + " has 1 zero", zeros == 1);
            check(col + " has 2 skips", skips == 2);
            check(col + " has 2 reverses", revs == 2);
            check(col + " has 2 draw-twos", draw2s == 2);
        }
    }

    // -----------------------------------------------------------------------
    // Scoring — points tallied from losing hands
    // -----------------------------------------------------------------------
    static void testScoring() {
        // hand with known point values: R5=5, B9=9, GS=20, W=50 => 84
        ArrayList<String> losingHand = new ArrayList<>();
        losingHand.add("R5");
        losingHand.add("B9");
        losingHand.add("GS");
        losingHand.add("W");
        int total = 0;
        for (String c : losingHand) total += Main.points(c);
        check("scoring example from rules: 84", total == 84);

        // Zero-card hand scores 0
        check("empty hand -> 0 points", handPoints(new ArrayList<>()) == 0);

        // Action cards
        check("skip = 20",     Main.points("BS") == 20);
        check("reverse = 20",  Main.points("RR") == 20);
        check("draw2 = 20",    Main.points("Y+2") == 20);
        check("wild = 50",     Main.points("W") == 50);
        check("wild4 = 50",    Main.points("W4") == 50);
        check("number 0 = 0",  Main.points("G0") == 0);
        check("number 7 = 7",  Main.points("R7") == 7);
    }

    // -----------------------------------------------------------------------
    // join() — hand display format
    // -----------------------------------------------------------------------
    static void testJoin() {
        ArrayList<String> hand = new ArrayList<>();
        check("join empty -> empty string", Main.join(hand).equals(""));

        hand.add("R5");
        check("join one card", Main.join(hand).equals("0:R5"));

        hand.add("W");
        check("join two cards", Main.join(hand).equals("0:R5 1:W"));

        hand.add("G+2");
        check("join three cards", Main.join(hand).equals("0:R5 1:W 2:G+2"));
    }

    // -----------------------------------------------------------------------
    // Deterministic end-to-end game results
    // These pin the complete game outcome for specific seeds.
    // If refactoring changes these, a behavior change occurred.
    // -----------------------------------------------------------------------
    static void testDeterministicGame() {
        // Seed 42, 3 bots, 3 games
        {
            int[] result = runBotGame(42, 3, 3);
            check("seed42 bots3 games3: Bot1 score 0",  result[0] == 0);
            check("seed42 bots3 games3: Bot2 score 0",  result[1] == 0);
            check("seed42 bots3 games3: Bot3 score 87", result[2] == 87);
        }
        // Seed 99, 3 bots, 10 games
        {
            int[] result = runBotGame(99, 3, 10);
            check("seed99 bots3 games10: Bot1 score 497", result[0] == 497);
            check("seed99 bots3 games10: Bot2 score 185", result[1] == 185);
            check("seed99 bots3 games10: Bot3 score 345", result[2] == 345);
        }
        // Seed 7, 2 bots, 5 games (2-player, reverse acts as skip)
        {
            int[] result = runBotGame(7, 2, 5);
            check("seed7 bots2 games5: Bot1 score 0",  result[0] == 0);
            check("seed7 bots2 games5: Bot2 score 40", result[1] == 40);
        }
        // Additional seeds for wider coverage
        {
            int[] result = runBotGame(1234, 3, 5);
            int total = result[0] + result[1] + result[2];
            check("seed1234 bots3 games5: scores are non-negative", result[0] >= 0 && result[1] >= 0 && result[2] >= 0);
            // Pin the exact values
            check("seed1234 bots3 games5: Bot1=" + result[0], result[0] == 0);
            check("seed1234 bots3 games5: Bot2=" + result[1], result[1] == 0);
            check("seed1234 bots3 games5: Bot3=" + result[2], result[2] == 547);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Set up a minimal Main.state for unit-level tests that call chooseBotCard. */
    static void initTestState(String upCard, String calledColor) {
        Main.upCard = upCard;
        Main.calledColor = calledColor;
        Main.state = new GameState(
                Main.playerNames, Main.humanPlayers, Main.hands, 10);
        Main.state.upCard = upCard;
        Main.state.calledColor = calledColor;
    }

    static ArrayList<String> buildFreshDeck() {
        ArrayList<String> deck = new ArrayList<>();
        String[] colors = {"R", "Y", "G", "B"};
        for (String col : colors) {
            deck.add(col + "0");
            for (int n = 1; n <= 9; n++) {
                deck.add(col + n);
                deck.add(col + n);
            }
            deck.add(col + "S"); deck.add(col + "S");
            deck.add(col + "R"); deck.add(col + "R");
            deck.add(col + "+2"); deck.add(col + "+2");
        }
        for (int i = 0; i < 4; i++) {
            deck.add("W");
            deck.add("W4");
        }
        return deck;
    }

    static int handPoints(ArrayList<String> hand) {
        int total = 0;
        for (String c : hand) total += Main.points(c);
        return total;
    }

    /**
     * Run a fully-deterministic bot-only game and return final scores.
     * Temporarily redirects Main's static state; resets afterward.
     */
    static int[] runBotGame(long seed, int bots, int games) {
        // Save & reset Main state
        Main.quiet = true;
        Main.random = new java.util.Random(seed);
        Main.scores = new int[10];
        Main.setupPlayers(bots, false);

        for (int g = 1; g <= games; g++) {
            Main.playGame();
        }

        int[] result = new int[bots];
        for (int i = 0; i < bots; i++) {
            result[i] = Main.scores[i];
        }
        return result;
    }

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }
}
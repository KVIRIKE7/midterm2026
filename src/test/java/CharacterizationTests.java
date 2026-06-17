import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

/**
 * Characterization tests for the UNO CLI implementation.
 *
 * These tests document CURRENT behavior of Main.java — including quirks —
 * not ideal UNO rules. They exist to catch regressions during refactoring.
 *
 * Converted to JUnit Jupiter so they run via: mvn test
 */
public class CharacterizationTests {

    @BeforeEach
    void resetState() {
        Main.playerNames.clear();
        Main.humanPlayers.clear();
        Main.hands.clear();
        Main.scores = new int[10];
        Main.quiet = true;
    }

    // --- color() ---
    @Test void colorR5()   { assertEquals("R", Main.color("R5")); }
    @Test void colorY3()   { assertEquals("Y", Main.color("Y3")); }
    @Test void colorGp2()  { assertEquals("G", Main.color("G+2")); }
    @Test void colorBS()   { assertEquals("B", Main.color("BS")); }
    @Test void colorBR()   { assertEquals("B", Main.color("BR")); }
    @Test void colorW()    { assertEquals("",  Main.color("W")); }
    @Test void colorW4()   { assertEquals("",  Main.color("W4")); }
    @Test void colorR0()   { assertEquals("R", Main.color("R0")); }

    // --- rank() ---
    @Test void rankW()     { assertEquals("WILD",           Main.rank("W")); }
    @Test void rankW4()    { assertEquals("WILD_DRAW_FOUR", Main.rank("W4")); }
    @Test void rankRS()    { assertEquals("SKIP",           Main.rank("RS")); }
    @Test void rankGR()    { assertEquals("REVERSE",        Main.rank("GR")); }
    @Test void rankRp2()   { assertEquals("DRAW_TWO",       Main.rank("R+2")); }
    @Test void rankR5()    { assertEquals("NUMBER",         Main.rank("R5")); }
    @Test void rankRR()    { assertEquals("REVERSE",        Main.rank("RR")); }

    // --- number() ---
    @Test void numberR5()  { assertEquals(5,  Main.number("R5")); }
    @Test void numberG0()  { assertEquals(0,  Main.number("G0")); }
    @Test void numberRS()  { assertEquals(-1, Main.number("RS")); }
    @Test void numberW()   { assertEquals(-1, Main.number("W")); }

    // --- points() ---
    @Test void pointsR5()  { assertEquals(5,  Main.points("R5")); }
    @Test void pointsRS()  { assertEquals(20, Main.points("RS")); }
    @Test void pointsW()   { assertEquals(50, Main.points("W")); }
    @Test void pointsW4()  { assertEquals(50, Main.points("W4")); }

    // --- isLegal() ---
    @Test void wildAlwaysLegal()    { assertTrue(Main.isLegal("W",  "R5", "")); }
    @Test void w4AlwaysLegal()      { assertTrue(Main.isLegal("W4", "B9", "")); }
    @Test void sameColorLegal()     { assertTrue(Main.isLegal("R2", "R9", "")); }
    @Test void diffColorIllegal()   { assertFalse(Main.isLegal("B3", "R9", "")); }
    @Test void sameNumberLegal()    { assertTrue(Main.isLegal("G9", "R9", "")); }
    @Test void calledColorMatch()   { assertTrue(Main.isLegal("B3", "W",  "B")); }
    @Test void calledColorNoMatch() { assertFalse(Main.isLegal("R3", "W", "B")); }
    @Test void skipOnSkipLegal()    { assertTrue(Main.isLegal("GS", "RS", "")); }
    @Test void skipOnDraw2Illegal() { assertFalse(Main.isLegal("GS", "R+2", "")); }

    // --- chooseBotColor() ---
    @Test void botColorMajority() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1"); hand.add("B2"); hand.add("R3");
        assertEquals("B", Main.chooseBotColor(hand));
    }

    @Test void botColorAllWild() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("W"); hand.add("W4");
        assertEquals("R", Main.chooseBotColor(hand));
    }

    // --- chooseBotCard() ---
    @Test void botPrefersDrawTwo() {
        initTestState("R9", "");
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5"); hand.add("R+2"); hand.add("W");
        assertEquals(1, Main.chooseBotCard(hand));
    }

    @Test void botPrefersNumberOverWild() {
        initTestState("R9", "");
        ArrayList<String> hand = new ArrayList<>();
        hand.add("W"); hand.add("R4");
        assertEquals(1, Main.chooseBotCard(hand));
    }

    @Test void botDrawsWhenNoLegal() {
        initTestState("R9", "");
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3"); hand.add("G7");
        assertEquals(-1, Main.chooseBotCard(hand));
    }

    // --- Deck composition ---
    @Test void deckHas108Cards() {
        assertEquals(108, buildFreshDeck().size());
    }

    @Test void deckHas4Wilds() {
        long count = buildFreshDeck().stream().filter(c -> c.equals("W")).count();
        assertEquals(4, count);
    }

    @Test void deckHas4WildDraw4s() {
        long count = buildFreshDeck().stream().filter(c -> c.equals("W4")).count();
        assertEquals(4, count);
    }

    // --- Scoring ---
    @Test void scoringExample() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5"); hand.add("B9"); hand.add("GS"); hand.add("W");
        int total = hand.stream().mapToInt(Main::points).sum();
        assertEquals(84, total);
    }

    // --- join() ---
    @Test void joinEmpty() {
        assertEquals("", Main.join(new ArrayList<>()));
    }

    @Test void joinOneCard() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5");
        assertEquals("0:R5", Main.join(hand));
    }

    @Test void joinTwoCards() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5"); hand.add("W");
        assertEquals("0:R5 1:W", Main.join(hand));
    }

    // --- Deterministic games ---
    @Test void deterministicSeed42() {
        int[] result = runBotGame(42, 3, 3);
        assertEquals(0,  result[0], "Bot1 score");
        assertEquals(0,  result[1], "Bot2 score");
        assertEquals(87, result[2], "Bot3 score");
    }

    @Test void deterministicSeed99() {
        int[] result = runBotGame(99, 3, 10);
        assertEquals(497, result[0], "Bot1 score");
        assertEquals(185, result[1], "Bot2 score");
        assertEquals(345, result[2], "Bot3 score");
    }

    @Test void deterministicSeed7() {
        int[] result = runBotGame(7, 2, 5);
        assertEquals(0,  result[0], "Bot1 score");
        assertEquals(40, result[1], "Bot2 score");
    }

    @Test void deterministicSeed1234() {
        int[] result = runBotGame(1234, 3, 5);
        assertEquals(0,   result[0], "Bot1 score");
        assertEquals(0,   result[1], "Bot2 score");
        assertEquals(547, result[2], "Bot3 score");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
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
            for (int n = 1; n <= 9; n++) { deck.add(col + n); deck.add(col + n); }
            deck.add(col + "S"); deck.add(col + "S");
            deck.add(col + "R"); deck.add(col + "R");
            deck.add(col + "+2"); deck.add(col + "+2");
        }
        for (int i = 0; i < 4; i++) { deck.add("W"); deck.add("W4"); }
        return deck;
    }

    static int[] runBotGame(long seed, int bots, int games) {
        Main.quiet = true;
        Main.random = new java.util.Random(seed);
        Main.scores = new int[10];
        Main.setupPlayers(bots, false);
        for (int g = 1; g <= games; g++) Main.playGame();
        int[] result = new int[bots];
        for (int i = 0; i < bots; i++) result[i] = Main.scores[i];
        return result;
    }
}

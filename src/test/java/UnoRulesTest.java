import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

/**
 * Tests covering all UNO rule features for the final project.
 * No console input required — all tests run against game logic directly.
 */
public class UnoRulesTest {

    @BeforeEach
    void reset() {
        Main.playerNames.clear();
        Main.humanPlayers.clear();
        Main.hands.clear();
        Main.scores = new int[10];
        Main.quiet = true;
    }

    // ── 1. Deck Composition ───────────────────────────────────────────────

    @Test void deckHas108Cards() {
        assertEquals(108, DeckFactory.buildStandardDeck().size());
    }

    @Test void deckHasFourWilds() {
        long count = DeckFactory.buildStandardDeck().stream().filter(c -> c.equals("W")).count();
        assertEquals(4, count);
    }

    @Test void deckHasFourWildDraw4s() {
        long count = DeckFactory.buildStandardDeck().stream().filter(c -> c.equals("W4")).count();
        assertEquals(4, count);
    }

    @Test void deckHas25CardsPerColor() {
        ArrayList<String> deck = DeckFactory.buildStandardDeck();
        for (String col : new String[]{"R","Y","G","B"}) {
            long count = deck.stream().filter(c -> Cards.color(c).equals(col)).count();
            assertEquals(25, count, "Color " + col + " should have 25 cards");
        }
    }

    @Test void deckHasOneZeroPerColor() {
        ArrayList<String> deck = DeckFactory.buildStandardDeck();
        for (String col : new String[]{"R","Y","G","B"}) {
            long count = deck.stream().filter(c -> c.equals(col + "0")).count();
            assertEquals(1, count, col + "0 should appear once");
        }
    }

    @Test void deckHasTwoSkipsPerColor() {
        ArrayList<String> deck = DeckFactory.buildStandardDeck();
        for (String col : new String[]{"R","Y","G","B"}) {
            long count = deck.stream().filter(c -> c.equals(col + "S")).count();
            assertEquals(2, count, col + "S should appear twice");
        }
    }

    @Test void deckHasTwoReversesPerColor() {
        ArrayList<String> deck = DeckFactory.buildStandardDeck();
        for (String col : new String[]{"R","Y","G","B"}) {
            long count = deck.stream().filter(c -> c.equals(col + "R")).count();
            assertEquals(2, count, col + "R should appear twice");
        }
    }

    @Test void deckHasTwoDrawTwosPerColor() {
        ArrayList<String> deck = DeckFactory.buildStandardDeck();
        for (String col : new String[]{"R","Y","G","B"}) {
            long count = deck.stream().filter(c -> c.equals(col + "+2")).count();
            assertEquals(2, count, col + "+2 should appear twice");
        }
    }

    // ── 2. Legal Play Validation ──────────────────────────────────────────

    @Test void matchByColorIsLegal() {
        assertTrue(Rules.isLegal("R5", "R9", ""));
    }

    @Test void matchByNumberIsLegal() {
        assertTrue(Rules.isLegal("G9", "R9", ""));
    }

    @Test void matchByActionTypeSkipIsLegal() {
        assertTrue(Rules.isLegal("GS", "RS", ""));
    }

    @Test void matchByActionTypeReverseIsLegal() {
        assertTrue(Rules.isLegal("BR", "YR", ""));
    }

    @Test void matchByActionTypeDrawTwoIsLegal() {
        assertTrue(Rules.isLegal("G+2", "R+2", ""));
    }

    @Test void wildIsAlwaysLegal() {
        assertTrue(Rules.isLegal("W", "R5", ""));
        assertTrue(Rules.isLegal("W", "B9", ""));
        assertTrue(Rules.isLegal("W", "GS", ""));
    }

    @Test void wildDraw4IsAlwaysLegal() {
        assertTrue(Rules.isLegal("W4", "G3", ""));
        assertTrue(Rules.isLegal("W4", "BS", ""));
    }

    @Test void illegalColorMismatchRejected() {
        assertFalse(Rules.isLegal("B3", "R9", ""));
    }

    @Test void illegalActionMismatchRejected() {
        assertFalse(Rules.isLegal("GS", "R+2", ""));
    }

    @Test void calledColorMatchIsLegal() {
        assertTrue(Rules.isLegal("B3", "W", "B"));
    }

    @Test void calledColorMismatchIsIllegal() {
        assertFalse(Rules.isLegal("R3", "W", "B"));
    }

    // ── 3. Skip ──────────────────────────────────────────────────────────

    @Test void skipAdvancesTwoPlayers() {
        setupBotGame(3);
        int before = Main.state.currentPlayer;
        Main.applyCardEffect("RS", new ConsoleView(true));
        int expected = (before + 2) % Main.state.playerCount();
        assertEquals(expected, Main.state.currentPlayer);
    }

    @Test void skipIsLegalOnSameColorSkip() {
        assertTrue(Rules.isLegal("GS", "RS", ""));
    }

    @Test void skipIsLegalOnSameColor() {
        assertTrue(Rules.isLegal("RS", "R5", ""));
    }

    // ── 4. Reverse ────────────────────────────────────────────────────────

    @Test void reverseChangesDirectionForThreePlayers() {
        setupBotGame(3);
        assertEquals(1, Main.state.direction);
        Main.applyCardEffect("RR", new ConsoleView(true));
        assertEquals(-1, Main.state.direction);
    }

    @Test void reverseTwoPlayerActsLikeSkip() {
        setupBotGame(2);
        int before = Main.state.currentPlayer;
        Main.applyCardEffect("RR", new ConsoleView(true));
        assertEquals(before, Main.state.currentPlayer);
    }

    @Test void reverseIsLegalOnSameColorReverse() {
        assertTrue(Rules.isLegal("BR", "YR", ""));
    }

    // ── 5. Draw Two ───────────────────────────────────────────────────────

    @Test void drawTwoGivesNextPlayerTwoCards() {
        setupBotGame(3);
        int nextPlayer = (Main.state.currentPlayer + 1) % Main.state.playerCount();
        int before = Main.state.hand(nextPlayer).size();
        Main.applyCardEffect("R+2", new ConsoleView(true));
        assertEquals(before + 2, Main.state.hand(nextPlayer).size());
    }

    @Test void drawTwoSkipsNextPlayer() {
        setupBotGame(3);
        int before = Main.state.currentPlayer;
        Main.applyCardEffect("R+2", new ConsoleView(true));
        int expected = (before + 2) % Main.state.playerCount();
        assertEquals(expected, Main.state.currentPlayer);
    }

    @Test void drawTwoIsLegalOnMatchingColor() {
        assertTrue(Rules.isLegal("R+2", "R5", ""));
    }

    @Test void drawTwoIsLegalOnMatchingAction() {
        assertTrue(Rules.isLegal("B+2", "R+2", ""));
    }

    // ── 6. Wild ───────────────────────────────────────────────────────────

    @Test void wildPlayableOnAnyCard() {
        assertTrue(Rules.isLegal("W", "R5", ""));
        assertTrue(Rules.isLegal("W", "G9", ""));
        assertTrue(Rules.isLegal("W", "BS", ""));
    }

    @Test void calledColorAfterWildAffectsLegalPlay() {
        assertTrue(Rules.isLegal("B5", "W", "B"));
        assertFalse(Rules.isLegal("R5", "W", "B"));
    }

    @Test void botChoosesColorBasedOnMajority() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1"); hand.add("B2"); hand.add("R3");
        assertEquals("B", BotStrategy.chooseColor(hand));
    }

    @Test void botDefaultsToRedWithAllWilds() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("W"); hand.add("W4");
        assertEquals("R", BotStrategy.chooseColor(hand));
    }

    // ── 7. Wild Draw Four ─────────────────────────────────────────────────

    @Test void wildDraw4GivesNextPlayerFourCards() {
        setupBotGame(3);
        int nextPlayer = (Main.state.currentPlayer + 1) % Main.state.playerCount();
        int before = Main.state.hand(nextPlayer).size();
        Main.applyCardEffect("W4", new ConsoleView(true));
        assertEquals(before + 4, Main.state.hand(nextPlayer).size());
    }

    @Test void wildDraw4SkipsNextPlayer() {
        setupBotGame(3);
        int before = Main.state.currentPlayer;
        Main.applyCardEffect("W4", new ConsoleView(true));
        int expected = (before + 2) % Main.state.playerCount();
        assertEquals(expected, Main.state.currentPlayer);
    }

    @Test void wildDraw4AlwaysPlayable() {
        assertTrue(Rules.isLegal("W4", "R5", ""));
        assertTrue(Rules.isLegal("W4", "BS", ""));
        assertTrue(Rules.isLegal("W4", "W",  "G"));
    }

    // ── 8. Draw/Pass Behavior ─────────────────────────────────────────────

    @Test void botDrawsWhenNoLegalCard() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3"); hand.add("G7");
        assertEquals(-1, BotStrategy.chooseCard(hand, "R9", ""));
    }

    @Test void botPlaysWhenLegalCardExists() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3"); hand.add("R5");
        assertNotEquals(-1, BotStrategy.chooseCard(hand, "R9", ""));
    }

    @Test void botPrefersNumberOverWild() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("W"); hand.add("R4");
        int chosen = BotStrategy.chooseCard(hand, "R9", "");
        assertEquals(1, chosen);
    }

    // ── 9. Scoring ────────────────────────────────────────────────────────

    @Test void numberCardsScoreFaceValue() {
        assertEquals(5,  Cards.points("R5"));
        assertEquals(0,  Cards.points("G0"));
        assertEquals(9,  Cards.points("B9"));
    }

    @Test void actionCardsScore20() {
        assertEquals(20, Cards.points("RS"));
        assertEquals(20, Cards.points("YR"));
        assertEquals(20, Cards.points("G+2"));
    }

    @Test void wildCardsScore50() {
        assertEquals(50, Cards.points("W"));
        assertEquals(50, Cards.points("W4"));
    }

    @Test void totalHandScoreIsCorrect() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5"); hand.add("B9"); hand.add("GS"); hand.add("W");
        int total = hand.stream().mapToInt(Cards::points).sum();
        assertEquals(84, total);
    }

    // ── 10. Multi-round / Target Score ────────────────────────────────────

    @Test void targetScoreIs500() {
        assertEquals(500, Main.TARGET_SCORE);
    }

    @Test void scoresAccumulateAcrossRounds() {
        Main.random = new java.util.Random(42);
        Main.setupPlayers(3, false);
        for (int i = 0; i < 5; i++) Main.playGame();
        int total = 0;
        for (int s : Main.scores) total += s;
        assertTrue(total > 0, "Scores should accumulate after multiple rounds");
    }

    @Test void deterministicSeed42ThreeRounds() {
        Main.random = new java.util.Random(42);
        Main.setupPlayers(3, false);
        for (int i = 0; i < 3; i++) Main.playGame();
        assertEquals(87, Main.scores[2], "Bot3 score after seed 42, 3 rounds");
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    static void setupBotGame(int bots) {
        Main.random = new java.util.Random(1);
        Main.setupPlayers(bots, false);
        for (int i = 0; i < Main.state.playerCount(); i++) {
            Main.state.hand(i).clear();
            Main.state.hand(i).add("R1");
            Main.state.hand(i).add("R2");
            Main.state.hand(i).add("R3");
        }
        Main.state.deck.addAll(DeckFactory.buildStandardDeck());
        Main.state.upCard = "R5";
        Main.state.calledColor = "";
        Main.state.direction = 1;
        Main.state.currentPlayer = 0;
        Main.syncFromState();
    }
}

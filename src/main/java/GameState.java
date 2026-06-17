import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Holds all mutable game state for one UNO match session.
 *
 * Previously scattered as static fields across Main. Extracting this makes
 * the boundary between state and behavior explicit, and lets tests set up
 * specific game scenarios without wrestling with static globals.
 */
class GameState {

    final ArrayList<String> playerNames;
    final ArrayList<Boolean> humanPlayers;
    final ArrayList<ArrayList<String>> hands;
    final ArrayList<String> deck;
    final ArrayList<String> discard;
    final int[] scores;

    int currentPlayer;
    int direction;
    String upCard;
    String calledColor;

    GameState(ArrayList<String> playerNames,
              ArrayList<Boolean> humanPlayers,
              ArrayList<ArrayList<String>> hands,
              int scoreSlots) {
        this.playerNames   = playerNames;
        this.humanPlayers  = humanPlayers;
        this.hands         = hands;
        this.deck          = new ArrayList<>();
        this.discard       = new ArrayList<>();
        this.scores        = new int[scoreSlots];
        this.currentPlayer = 0;
        this.direction     = 1;
        this.upCard        = "";
        this.calledColor   = "";
    }

    // -----------------------------------------------------------------------
    // Deck operations
    // -----------------------------------------------------------------------

    /** Shuffle the deck with the given RNG. */
    void shuffleDeck(Random random) {
        Collections.shuffle(deck, random);
    }

    /**
     * Draw one card. Recycles the discard pile into the deck when empty.
     * Returns a fallback "W" wild if both piles are exhausted.
     */
    String draw(Random random) {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) return "W";
        return deck.remove(0);
    }

    // -----------------------------------------------------------------------
    // Turn navigation
    // -----------------------------------------------------------------------

    /** Advance currentPlayer by one step in the current direction. */
    void advanceTurn() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) currentPlayer = 0;
        if (currentPlayer < 0)                   currentPlayer = playerNames.size() - 1;
    }

    // -----------------------------------------------------------------------
    // Convenience accessors
    // -----------------------------------------------------------------------

    int playerCount()                         { return playerNames.size(); }
    String playerName(int index)              { return playerNames.get(index); }
    boolean isHuman(int index)                { return humanPlayers.get(index); }
    ArrayList<String> hand(int index)         { return hands.get(index); }
    ArrayList<String> currentHand()          { return hands.get(currentPlayer); }
    String currentPlayerName()               { return playerNames.get(currentPlayer); }
    boolean currentPlayerIsHuman()           { return humanPlayers.get(currentPlayer); }
}

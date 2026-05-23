import java.util.ArrayList;

/**
 * Bot decision logic: card selection and color choice.
 *
 * Extracted from Main so bot behavior can be tested and later replaced
 * without touching the game loop. Takes all inputs as parameters.
 *
 * Priority order (preserved from original):
 *   1. DRAW_TWO (most aggressive)
 *   2. SKIP
 *   3. NUMBER
 *   4. WILD / WILD_DRAW_FOUR (last resort)
 */
class BotStrategy {

    /**
     * Choose the index of the card to play from {@code hand}, or -1 to draw.
     *
     * @param hand        the bot's current hand
     * @param upCard      the current top-of-discard card
     * @param calledColor the color declared after the last wild ("" if none)
     */
    static int chooseCard(ArrayList<String> hand, String upCard, String calledColor) {
        // 1. Prefer DRAW_TWO
        for (int i = 0; i < hand.size(); i++) {
            if (Cards.rank(hand.get(i)).equals("DRAW_TWO")
                    && Rules.isLegal(hand.get(i), upCard, calledColor)) return i;
        }
        // 2. Prefer SKIP
        for (int i = 0; i < hand.size(); i++) {
            if (Cards.rank(hand.get(i)).equals("SKIP")
                    && Rules.isLegal(hand.get(i), upCard, calledColor)) return i;
        }
        // 3. Prefer NUMBER
        for (int i = 0; i < hand.size(); i++) {
            if (Cards.rank(hand.get(i)).equals("NUMBER")
                    && Rules.isLegal(hand.get(i), upCard, calledColor)) return i;
        }
        // 4. Fall back to any WILD
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) return i;
        }
        return -1;
    }

    /**
     * Choose the color to call after playing a wild card.
     * Returns the color most represented in the bot's remaining hand.
     * Ties broken in R > Y > G > B order (preserved from original).
     *
     * @param hand the bot's hand AFTER the wild has been removed
     */
    static String chooseColor(ArrayList<String> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (String card : hand) {
            String c = Cards.color(card);
            if (c.equals("R")) r++;
            else if (c.equals("Y")) y++;
            else if (c.equals("G")) g++;
            else if (c.equals("B")) b++;
        }
        if (r >= y && r >= g && r >= b) return "R";
        if (y >= r && y >= g && y >= b) return "Y";
        if (g >= r && g >= y && g >= b) return "G";
        return "B";
    }
}

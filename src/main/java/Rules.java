/**
 * UNO legality rules.
 *
 * Extracted from Main so rule behavior can be tested without running the CLI.
 * Takes all inputs as parameters; holds no mutable state.
 */
class Rules {

    /**
     * Returns true if {@code card} may be played on top of {@code upCard}
     * given the currently called color after a wild.
     *
     * @param card        the card the player wants to play
     * @param upCard      the current top-of-discard card
     * @param calledColor the color declared after the last wild ("" if none)
     */
    static boolean isLegal(String card, String upCard, String calledColor) {
        // Wilds are always playable
        if (card.startsWith("W")) return true;

        // Match the called color (set after a wild was played)
        if (!calledColor.equals("") && Cards.color(card).equals(calledColor)) return true;

        // Match by color
        if (Cards.color(card).equals(Cards.color(upCard))) return true;

        // Match by action type (skip on skip, reverse on reverse, draw2 on draw2)
        if (Cards.rank(card).equals(Cards.rank(upCard)) && !Cards.rank(card).equals("NUMBER")) return true;

        // Match by number
        if (Cards.rank(card).equals("NUMBER")
                && Cards.rank(upCard).equals("NUMBER")
                && Cards.number(card) == Cards.number(upCard)) return true;

        return false;
    }
}

import java.util.ArrayList;

/**
 * Builds a standard 108-card UNO deck.
 *
 * Extracted from playGame() so deck composition can be verified independently.
 * One 0 per color, two each of 1-9/S/R/+2, four W and four W4.
 */
class DeckFactory {

    static ArrayList<String> buildStandardDeck() {
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
}
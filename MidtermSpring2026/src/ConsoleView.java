import java.util.ArrayList;

/**
 * All console output for the UNO game.
 *
 * Extracted from Main so display logic can be changed (or replaced
 * entirely) without touching game rules. When quiet=true no output
 * is produced, matching the original --quiet flag behavior.
 */
class ConsoleView {

    private final boolean quiet;

    ConsoleView(boolean quiet) {
        this.quiet = quiet;
    }

    void showGameHeader(int gameNumber) {
        if (!quiet) System.out.println("\n=== Game " + gameNumber + " ===");
    }

    void showTurnHeader(String upCard, String calledColor, String playerName, ArrayList<String> hand) {
        if (!quiet) {
            System.out.println("\nUp card: " + upCard
                    + (calledColor.isEmpty() ? "" : " called " + calledColor));
            System.out.println(playerName + " hand: " + Cards.join(hand));
        }
    }

    void showDraw(String playerName, String card) {
        if (!quiet) System.out.println(playerName + " draws " + card);
    }

    void showPlay(String playerName, String card) {
        if (!quiet) System.out.println(playerName + " plays " + card);
    }

    void showCalledColor(String playerName, String color) {
        if (!quiet) System.out.println(playerName + " calls " + color);
    }

    void showUno(String playerName) {
        if (!quiet) System.out.println(playerName + " says UNO!");
    }

    void showWin(String playerName, int points) {
        if (!quiet) System.out.println(playerName + " wins and scores " + points);
    }

    void showDrawsTwo(String playerName) {
        if (!quiet) System.out.println(playerName + " draws two.");
    }

    void showDrawsFour(String playerName) {
        if (!quiet) System.out.println(playerName + " draws four.");
    }

    void showPenaltyInvalidIndex(String playerName) {
        if (!quiet) System.out.println(playerName + " selected an invalid index and draws a penalty card.");
    }

    void showPenaltyIllegalCard(String playerName, String card) {
        if (!quiet) System.out.println(playerName + " tried illegal card " + card + " and draws a penalty card.");
    }

    void showSafetyLimit() {
        if (!quiet) System.out.println("Game stopped at safety limit.");
    }

    void showFinalScores(ArrayList<String> names, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i) + ": " + scores[i]);
        }
    }
}
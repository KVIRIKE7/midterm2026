package persistence;

/** Represents a player's final score in one game, stored in the scores table. */
public class ScoreRecord {
    public int id;
    public int gameId;
    public String player;
    public int score;

    public ScoreRecord() {}

    public ScoreRecord(int gameId, String player, int score) {
        this.gameId = gameId;
        this.player = player;
        this.score  = score;
    }
}

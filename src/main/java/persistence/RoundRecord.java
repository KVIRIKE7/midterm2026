package persistence;

import java.sql.Timestamp;

/** Represents one round within a game stored in the rounds table. */
public class RoundRecord {
    public int id;
    public int gameId;
    public int roundNum;
    public String winner;
    public Timestamp playedAt;

    public RoundRecord() {}

    public RoundRecord(int gameId, int roundNum, String winner, Timestamp playedAt) {
        this.gameId   = gameId;
        this.roundNum = roundNum;
        this.winner   = winner;
        this.playedAt = playedAt;
    }
}

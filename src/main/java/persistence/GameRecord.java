package persistence;

import java.sql.Timestamp;

/** Represents one completed game session stored in the games table. */
public class GameRecord {
    public int id;
    public Timestamp startedAt;
    public Timestamp endedAt;
    public String winner;

    public GameRecord() {}

    public GameRecord(Timestamp startedAt) {
        this.startedAt = startedAt;
    }
}

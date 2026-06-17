package persistence;

import java.util.List;

/** MyBatis mapper interface — SQL is defined in GameMapper.xml. */
public interface GameMapper {

    void insertGame(GameRecord game);
    void updateGame(GameRecord game);
    List<GameRecord> recentGames(int limit);

    void insertRound(RoundRecord round);

    void insertScore(ScoreRecord score);

    List<WinCount>    playerWinCounts();
    List<ScoreRecord> highestScores(int limit);
}
package persistence;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

/**
 * Central access point for all database operations.
 * Wraps MyBatis SqlSessionFactory and exposes simple save/query methods.
 */
public class GameRepository {

    private static final Logger logger = Logger.getLogger("UNO.DB");
    private final SqlSessionFactory factory;

    public GameRepository(String environment) {
        try {
            InputStream config = Resources.getResourceAsStream("mybatis-config.xml");
            factory = new SqlSessionFactoryBuilder().build(config, environment);
            initSchema();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise database: " + e.getMessage(), e);
        }
    }

    /** Runs schema.sql to create tables if they don't exist yet. */
    private void initSchema() throws Exception {
        try (SqlSession session = factory.openSession()) {
            Connection conn = session.getConnection();
            InputStream sql = Resources.getResourceAsStream("schema.sql");
            // Execute each statement separated by semicolon
            String[] statements = new String(sql.readAllBytes()).split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String s : statements) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty()) stmt.execute(trimmed);
                }
            }
            session.commit();
        }
        logger.info("Database schema initialised.");
    }

    // ── write operations ──────────────────────────────────────────────────

    public GameRecord startGame() {
        GameRecord game = new GameRecord(now());
        try (SqlSession session = factory.openSession()) {
            session.getMapper(GameMapper.class).insertGame(game);
            session.commit();
        }
        logger.info("Game started, id=" + game.id);
        return game;
    }

    public void endGame(GameRecord game, String winner) {
        game.endedAt = now();
        game.winner  = winner;
        try (SqlSession session = factory.openSession()) {
            session.getMapper(GameMapper.class).updateGame(game);
            session.commit();
        }
        logger.info("Game ended, id=" + game.id + " winner=" + winner);
    }

    public void saveRound(int gameId, int roundNum, String winner) {
        RoundRecord r = new RoundRecord(gameId, roundNum, winner, now());
        try (SqlSession session = factory.openSession()) {
            session.getMapper(GameMapper.class).insertRound(r);
            session.commit();
        }
    }

    public void saveScores(int gameId, List<String> players, int[] scores) {
        try (SqlSession session = factory.openSession()) {
            GameMapper mapper = session.getMapper(GameMapper.class);
            for (int i = 0; i < players.size(); i++) {
                mapper.insertScore(new ScoreRecord(gameId, players.get(i), scores[i]));
            }
            session.commit();
        }
    }

    // ── query / report operations ─────────────────────────────────────────

    public List<GameRecord> recentGames(int limit) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(GameMapper.class).recentGames(limit);
        }
    }

    public List<WinCount> playerWinCounts() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(GameMapper.class).playerWinCounts();
        }
    }

    public List<ScoreRecord> highestScores(int limit) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(GameMapper.class).highestScores(limit);
        }
    }

    // ── helper ────────────────────────────────────────────────────────────

    private java.sql.Timestamp now() {
        return new java.sql.Timestamp(System.currentTimeMillis());
    }
}

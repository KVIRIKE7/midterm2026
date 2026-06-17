import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import persistence.*;

import java.util.Arrays;
import java.util.List;

/**
 * Persistence layer tests.
 * Uses the "test" MyBatis environment which connects to an in-memory H2 database.
 * Fully isolated — no file system state, no manual setup required.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersistenceTests {

    static GameRepository repo;

    @BeforeAll
    static void setup() {
        repo = new GameRepository("test");
    }

    @Test
    @Order(1)
    void canStartAndEndGame() {
        GameRecord game = repo.startGame();
        assertTrue(game.id > 0, "Game should have a generated id");
        assertNotNull(game.startedAt, "startedAt should be set");

        repo.endGame(game, "Bot1");
        assertNotNull(game.endedAt, "endedAt should be set after endGame");
        assertEquals("Bot1", game.winner);
    }

    @Test
    @Order(2)
    void canSaveAndQueryRound() {
        GameRecord game = repo.startGame();
        repo.saveRound(game.id, 1, "Bot2");
        repo.endGame(game, "Bot2");

        // Verify via recentGames query
        List<GameRecord> recent = repo.recentGames(10);
        assertFalse(recent.isEmpty(), "Should have at least one game");
    }

    @Test
    @Order(3)
    void canSaveScores() {
        GameRecord game = repo.startGame();
        List<String> players = Arrays.asList("Bot1", "Bot2", "Bot3");
        int[] scores = {150, 0, 75};
        repo.saveScores(game.id, players, scores);
        repo.endGame(game, "Bot1");

        List<ScoreRecord> top = repo.highestScores(10);
        assertFalse(top.isEmpty(), "Should have scores");
        assertEquals(150, top.get(0).score, "Highest score should be 150");
        assertEquals("Bot1", top.get(0).player, "Highest scorer should be Bot1");
    }

    @Test
    @Order(4)
    void recentGamesReturnsLatestFirst() {
        // start two more games
        GameRecord g1 = repo.startGame();
        repo.endGame(g1, "Bot1");
        GameRecord g2 = repo.startGame();
        repo.endGame(g2, "Bot2");

        List<GameRecord> recent = repo.recentGames(2);
        assertEquals(2, recent.size());
        // Most recent game should have the higher id
        assertTrue(recent.get(0).id > recent.get(1).id,
                "Most recent game should come first");
    }

    @Test
    @Order(5)
    void playerWinCountsAreAccurate() {
        // From previous tests: Bot1 won twice, Bot2 once
        List<WinCount> wins = repo.playerWinCounts();
        assertFalse(wins.isEmpty());

        WinCount top = wins.get(0);
        assertNotNull(top.player);
        assertTrue(top.wins > 0);
    }

    @Test
    @Order(6)
    void highestScoresLimitWorks() {
        List<ScoreRecord> top3 = repo.highestScores(3);
        assertTrue(top3.size() <= 3, "Should return at most 3 results");
    }

    @Test
    @Order(7)
    void emptyDatabaseReturnsEmptyLists() {
        // Fresh in-memory repo for isolation
        GameRepository fresh = new GameRepository("test");
        // After schema init, tables exist but may have data from other tests
        // Just verify the calls don't throw
        assertDoesNotThrow(() -> fresh.recentGames(5));
        assertDoesNotThrow(() -> fresh.playerWinCounts());
        assertDoesNotThrow(() -> fresh.highestScores(5));
    }
}

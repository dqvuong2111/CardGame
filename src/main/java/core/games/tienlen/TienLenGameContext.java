package core.games.tienlen; // Đảm bảo đúng package

import core.Card;
import core.Player;
// import core.RuleSet; // Không cần trực tiếp nếu TienLenVariantRuleSet đã extends
import java.util.List;

public interface TienLenGameContext {
    // Getters cho trạng thái game
    List<Card> getLastPlayedCards();
    Player getLastPlayer();
    int getPassCount();
    boolean isFirstTurnOfGame();
    Player getPlayerWhoPlayedLastValidCards();
    List<Player> getPlayers();
    Player getCurrentPlayer();
    int getCurrentPlayerIndex();
    TienLenVariantRuleSet getRuleSet(); // Sử dụng TienLenVariantRuleSet
    List<Player> getWinners();
    int getCurrentWinnerRank();
    TienLenGameState getCurrentTienLenState(); // <<--- Sử dụng TienLenGameState top-level

    // Setters hoặc các phương thức hành động để thay đổi trạng thái
    void setLastPlayedCards(List<Card> cards);
    void setLastPlayer(Player player);
    void resetPassCount();
    void incrementPassCount();
    void setFirstTurnOfGame(boolean isFirst);
    void setPlayerWhoPlayedLastValidCards(Player player);
    void setCurrentPlayerByIndex(int index);
    void addWinner(Player winner, int rank);
    void setCurrentTienLenState(TienLenGameState newState); // <<--- Sử dụng TienLenGameState top-level

    // Lấy input từ người chơi
    List<Card> getHumanInputSynchronously();
    void clearHumanInput();

    // Thông báo sự kiện
    void notifyMessage(String message);
    void notifyPlayerTurnStarted(Player player);
    void notifyCardsPlayed(Player player, List<Card> cardsPlayed, List<Card> newLastPlayedCards);
    void notifyPlayerPassed(Player player);
    void notifyRoundStarted(Player startingPlayer);
    void notifyPlayerEliminated(Player player);
    // void notifyGameStateUpdated(); // Phương thức này thường được gọi nội bộ bởi TienLenGame
}
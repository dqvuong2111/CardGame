package core.games.tienlen; // Đảm bảo đúng package

import core.Card;
import core.games.tienlen.tienlenplayer.TienLenPlayer;

// import core.RuleSet; // Không cần trực tiếp nếu TienLenVariantRuleSet đã extends
import java.util.List;

public interface TienLenGameContext {
    // Getters cho trạng thái game
    List<Card> getLastPlayedCards();
    TienLenPlayer getLastPlayer();
    int getPassCount();
    boolean isFirstTurnOfGame();
    TienLenPlayer getPlayerWhoPlayedLastValidCards();
    List<TienLenPlayer> getPlayers();
    TienLenPlayer getCurrentPlayer();
    int getCurrentPlayerIndex();
    TienLenVariantRuleSet getRuleSet(); // Sử dụng TienLenVariantRuleSet
    List<TienLenPlayer> getWinners();
    int getCurrentWinnerRank();
    TienLenGameState getCurrentTienLenState(); // <<--- Sử dụng TienLenGameState top-level

    // Setters hoặc các phương thức hành động để thay đổi trạng thái
    void setLastPlayedCards(List<Card> cards);
    void setLastPlayer(TienLenPlayer player);
    void resetPassCount();
    void incrementPassCount();
    void setFirstTurnOfGame(boolean isFirst);
    void setPlayerWhoPlayedLastValidCards(TienLenPlayer player);
    void setCurrentPlayerByIndex(int index);
    void addWinner(TienLenPlayer winner, int rank);
    void setCurrentTienLenState(TienLenGameState newState); // <<--- Sử dụng TienLenGameState top-level

    // Lấy input từ người chơi
    List<Card> getHumanInputSynchronously();
    void clearHumanInput();

    // Thông báo sự kiện
    void notifyMessage(String message);
    void notifyPlayerTurnStarted(TienLenPlayer player);
    void notifyCardsPlayed(TienLenPlayer player, List<Card> cardsPlayed, List<Card> newLastPlayedCards);
    void notifyPlayerPassed(TienLenPlayer player);
    void notifyRoundStarted(TienLenPlayer startingPlayer);
    void notifyPlayerEliminated(TienLenPlayer player);
    // void notifyGameStateUpdated(); // Phương thức này thường được gọi nội bộ bởi TienLenGame
}
package core.games;

import core.Card;
import core.games.tienlenplayer.TienLenPlayer;

import java.util.List;

public interface GameContext {
    // Getters cho trạng thái game
    List<Card> getLastPlayedCards();
    TienLenPlayer getLastPlayer();
    int getPassCount();
    boolean isFirstTurnOfGame();
    TienLenPlayer getPlayerWhoPlayedLastValidCards();
    List<TienLenPlayer> getPlayers();
    TienLenPlayer getCurrentPlayer();
    int getCurrentPlayerIndex();
    RuleSet getRuleSet();
    List<TienLenPlayer> getWinners();
    int getCurrentWinnerRank();
    GameState getCurrentTienLenState();

    // Setters hoặc các phương thức hành động để thay đổi trạng thái
    void setLastPlayedCards(List<Card> cards);
    void setLastPlayer(TienLenPlayer player);
    void resetPassCount();
    void incrementPassCount();
    void setFirstTurnOfGame(boolean isFirst);
    void setPlayerWhoPlayedLastValidCards(TienLenPlayer player);
    void setCurrentPlayerByIndex(int index);
    void addWinner(TienLenPlayer winner, int rank);
    void setCurrentTienLenState(GameState newState);

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
}
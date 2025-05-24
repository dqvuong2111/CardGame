package core.games.tienlen;

import core.Card;
import core.Player;
import java.util.ArrayList;
import java.util.List;

public class TienLenState {
    private List<Card> lastPlayedCards;
    private Player lastPlayer;
    private int passCount;
    private boolean isFirstTurnOfGame;
    private List<Player> winners;
    private int currentWinnerRank;
    private Player playerWhoPlayedLastValidCards;
    private TienLenGameState currentTienLenGameState; // Sử dụng enum top-level

    public TienLenState() {
        this.lastPlayedCards = new ArrayList<>();
        this.lastPlayer = null;
        this.passCount = 0;
        this.isFirstTurnOfGame = true;
        this.winners = new ArrayList<>();
        this.currentWinnerRank = 1;
        this.playerWhoPlayedLastValidCards = null;
        this.currentTienLenGameState = TienLenGameState.INITIALIZING;
    }

    // Getters
    public List<Card> getLastPlayedCards() { return new ArrayList<>(lastPlayedCards); } // Trả về bản sao
    public Player getLastPlayer() { return lastPlayer; }
    public int getPassCount() { return passCount; }
    public boolean isFirstTurnOfGame() { return isFirstTurnOfGame; }
    public List<Player> getWinners() { return new ArrayList<>(winners); } // Trả về bản sao
    public int getCurrentWinnerRank() { return currentWinnerRank; }
    public Player getPlayerWhoPlayedLastValidCards() { return playerWhoPlayedLastValidCards; }
    public TienLenGameState getCurrentTienLenGameState() { return currentTienLenGameState; }

    // Setters & Modifiers
    public void setLastPlayedCards(List<Card> cards) { this.lastPlayedCards = (cards != null) ? new ArrayList<>(cards) : new ArrayList<>(); }
    public void setLastPlayer(Player player) { this.lastPlayer = player; }
    public void resetPassCount() { this.passCount = 0; }
    public void incrementPassCount() { this.passCount++; }
    public void setFirstTurnOfGame(boolean firstTurnOfGame) { isFirstTurnOfGame = firstTurnOfGame; }
    
    public void addWinner(Player winner, int rank) {
        if (!this.winners.contains(winner)) {
            winner.setWinnerRank(rank);
            this.winners.add(winner);
            // Chỉ tăng currentWinnerRank nếu rank được thêm là rank hiện tại đang chờ
            if (rank == this.currentWinnerRank) {
                 this.currentWinnerRank++;
            }
        }
    }
    public void clearWinnersAndRank() {
        this.winners.clear();
        this.currentWinnerRank = 1;
        // Cần đảm bảo các player cũng được reset rank
    }

    public void setPlayerWhoPlayedLastValidCards(Player player) { this.playerWhoPlayedLastValidCards = player; }
    public void setCurrentTienLenGameState(TienLenGameState currentTienLenGameState) { this.currentTienLenGameState = currentTienLenGameState; }

    public void resetForNewGame() {
        this.lastPlayedCards.clear();
        this.lastPlayer = null;
        this.passCount = 0;
        this.isFirstTurnOfGame = true;
        this.winners.clear();
        this.currentWinnerRank = 1;
        this.playerWhoPlayedLastValidCards = null;
        this.currentTienLenGameState = TienLenGameState.INITIALIZING;
    }

    public void resetForNewRound(Player roundStarter) {
        this.lastPlayedCards.clear();
        this.lastPlayer = null;
        this.passCount = 0;
        this.playerWhoPlayedLastValidCards = roundStarter;
        this.isFirstTurnOfGame = false; // Vòng mới không phải là lượt đầu game nữa
        this.currentTienLenGameState = TienLenGameState.ROUND_IN_PROGRESS;
    }
}
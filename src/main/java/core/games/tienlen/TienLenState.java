package core.games.tienlen;

import core.Card;
import core.games.tienlen.tienlenplayer.TienLenPlayer;

import java.util.ArrayList;
import java.util.List;

public class TienLenState {
    private List<Card> lastPlayedCards;
    private TienLenPlayer lastPlayer;
    private int passCount;
    private boolean isFirstTurnOfGame;
    private List<TienLenPlayer> winners;
    private int currentWinnerRank;
    private TienLenPlayer playerWhoPlayedLastValidCards;
    private TienLenGameState currentTienLenGameState;

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
    public TienLenPlayer getLastPlayer() { return lastPlayer; }
    public int getPassCount() { return passCount; }
    public boolean isFirstTurnOfGame() { return isFirstTurnOfGame; }
    public List<TienLenPlayer> getWinners() { return new ArrayList<>(winners); } // Trả về bản sao
    public int getCurrentWinnerRank() { return currentWinnerRank; }
    public TienLenPlayer getPlayerWhoPlayedLastValidCards() { return playerWhoPlayedLastValidCards; }
    public TienLenGameState getCurrentTienLenGameState() { return currentTienLenGameState; }

    // Setters & Modifiers
    public void setLastPlayedCards(List<Card> cards) { this.lastPlayedCards = (cards != null) ? new ArrayList<>(cards) : new ArrayList<>(); }
    public void setLastPlayer(TienLenPlayer player) { this.lastPlayer = player; }
    public void resetPassCount() { this.passCount = 0; }
    public void incrementPassCount() { this.passCount++; }
    public void setFirstTurnOfGame(boolean firstTurnOfGame) { isFirstTurnOfGame = firstTurnOfGame; }
    
    public void addWinner(TienLenPlayer winner, int rank) {
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

    public void setPlayerWhoPlayedLastValidCards(TienLenPlayer player) { this.playerWhoPlayedLastValidCards = player; }
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

    public void resetForNewRound(TienLenPlayer roundStarter) {
        this.lastPlayedCards.clear();
        this.lastPlayer = null;
        this.passCount = 0;
        this.playerWhoPlayedLastValidCards = roundStarter;
        this.isFirstTurnOfGame = false;
        this.currentTienLenGameState = TienLenGameState.ROUND_IN_PROGRESS;
    }
}
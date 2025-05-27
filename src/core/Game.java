// core/Game.java
package core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import java.util.function.Consumer;

import ui.Swing.CardGameGUI; 

public abstract class Game<R extends RuleSet> {
    protected String name;
    protected List<Player> players;
    protected Deck deck;
    protected int currentPlayerIndex;
    protected boolean isFinished;
    public R ruleSet; 
    
    protected List<GameEventListener> listeners;

    // THÊM: Khai báo biến gameThread ở đây (FIELD DECLARATION)
    public Thread gameThread; 
    
    public abstract String getGameStateDisplay(); 
    
    public enum GeneralGameState {
        INITIALIZING,
        RUNNING,
        PAUSED,
        GAME_OVER
    }
    
    public Runnable onGameEndCallback;
    
    public void setOnGameEndCallback(Runnable callback) { // <-- THÊM DÒNG NÀY
        this.onGameEndCallback = callback;
    }
    
 // THÊM: Phương thức để thêm người chơi sau khi khởi tạo game
    public void addPlayer(Player player) {
        if (this.players == null) {
            this.players = new ArrayList<>();
        }
        this.players.add(player);
    }
    // THÊM: Phương thức công khai để bắt đầu game loop trong một thread riêng
    public void startGameLoop() {
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this::runGameLoop);
            gameThread.setDaemon(true);
            gameThread.start();
        }
    }

    
    
    public String getName() {
		return name;
	}

	public R getRuleSet() {
		return ruleSet;
	}

	public int getCurrentPlayerIndex() {
		return currentPlayerIndex;
	}



	protected GeneralGameState generalState; 
    
    public GeneralGameState getGeneralGameState() {
        return generalState;
    }

    public Game(String name, List<Player> players, Deck deck, R ruleSet) {
        this.name = name;
        this.players = players;
        this.deck = deck;
        this.currentPlayerIndex = 0;
        this.isFinished = false;
        this.ruleSet = ruleSet;
        this.listeners = new ArrayList<>();
        this.generalState = GeneralGameState.INITIALIZING; 
    }

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    protected void notifyGameStateUpdated() {
        for (GameEventListener listener : listeners) {
            SwingUtilities.invokeLater(listener::onGameStateUpdated);
        }
    }

    protected void notifyMessageReceived(String message) {
        for (GameEventListener listener : listeners) {
            SwingUtilities.invokeLater(() -> listener.onMessageReceived(message));
        }
    }
    
    protected void notifyPlayerTurnStarted(Player player) {
        for (GameEventListener listener : listeners) {
            SwingUtilities.invokeLater(() -> listener.onPlayerTurnStarted(player));
        }
    }

    protected void notifyCardsPlayed(Player player, List<Card> cardsPlayed, List<Card> lastPlayedCards) {
        for (GameEventListener listener : listeners) {
            SwingUtilities.invokeLater(() -> listener.onCardsPlayed(player, cardsPlayed, lastPlayedCards));
        }
    }

    protected void notifyPlayerPassed(Player player) {
        for (GameEventListener listener : listeners) {
            SwingUtilities.invokeLater(() -> listener.onPlayerPassed(player));
        }
    }
    
    protected void notifyRoundStarted(Player startingPlayer) {
        for (GameEventListener listener : listeners) {
            SwingUtilities.invokeLater(() -> listener.onRoundStarted(startingPlayer));
        }
    }

    protected void notifyPlayerEliminated(Player player) {
        for (GameEventListener listener : listeners) {
            SwingUtilities.invokeLater(() -> listener.onPlayerEliminated(player));
        }
    }

    protected void notifyGameOver(List<Player> winners) {
        for (GameEventListener listener : listeners) {
        	System.out.println(listener);
            SwingUtilities.invokeLater(() -> listener.onGameOver(winners));
        }
    }

    public void startGame() {
        setGeneralGameState(GeneralGameState.RUNNING); 
    }

    public void resetGame() {
        // Mặc định không làm gì, lớp con sẽ ghi đè
    }

    // Các phương thức abstract để lớp con phải triển khai game loop
    protected abstract void runGameLoop();
    protected abstract void stopGameLoop(); 

    public void nextPlayer() {
        int nextPlayerCandidate = (currentPlayerIndex + 1) % players.size();
        while (players.get(nextPlayerCandidate).hasNoCards() && nextPlayerCandidate != currentPlayerIndex) {
            nextPlayerCandidate = (nextPlayerCandidate + 1) % players.size();
        }

        if (!players.get(nextPlayerCandidate).hasNoCards()) {
             currentPlayerIndex = nextPlayerCandidate;
        } else {
            // Không thay đổi người chơi nếu tất cả người chơi khác đã hết bài
        }
        notifyGameStateUpdated();
    }
    
 // THÊM: Phương thức để xóa người nghe sự kiện
    public void removeGameEventListener(GameEventListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getHumanPlayer() {
        for (Player player : players) {
            if (!player.isAI()) {
                return player;
            }
        }
        return null;
    }

    // Các phương thức abstract để lớp con cài đặt logic cụ thể của game
    protected abstract void dealCards();
    public abstract void playTurn();
    public abstract boolean checkGameOver();
    protected abstract List<Player> determineWinners(); // Thêm phương thức trừu tượng này

    // Các phương thức cụ thể của game (TienLenGame sẽ triển khai)
    public abstract List<Card> getLastPlayedCards();
    public abstract Player getLastPlayer();
    public abstract boolean isValidPlay(List<Card> cards);
    public abstract void setPlayerInput(List<Card> cards); 
    public abstract boolean canPass(Player player); 
    public abstract int getPassCount(); // Thêm getPassCount vào Game

    public interface GameEventListener {
        void onGameStateUpdated();
        void onMessageReceived(String message);
        void onPlayerTurnStarted(Player player);
        void onCardsPlayed(Player player, List<Card> cardsPlayed, List<Card> lastPlayedCards);
        void onPlayerPassed(Player player);
        void onRoundStarted(Player startingPlayer);
        void onPlayerEliminated(Player player);
        void onGameOver(List<Player> winners);
    }

    public void setGeneralGameState(GeneralGameState generalState) {
        this.generalState = generalState;
    }
}






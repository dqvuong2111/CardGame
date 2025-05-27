package core;

import java.util.ArrayList;
import java.util.List;

import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.tienlenplayer.TienLenPlayer;
import javafx.application.Platform;

public abstract class Game<R extends TienLenVariantRuleSet> {

    // Thuộc tính (Fields)
    protected String name;
    protected List<TienLenPlayer> players;
    protected Deck deck;
    protected int currentPlayerIndex;
    protected boolean isFinished;
    public R ruleSet;
    protected List<GameEventListener> listeners;
    public Thread gameThread;
    protected GeneralGameState generalState;
    public Runnable onGameEndCallback;

    // Enum
    public enum GeneralGameState {
        INITIALIZING,
        RUNNING,
        PAUSED,
        GAME_OVER
    }

    // Interface
    public interface GameEventListener {
        void onGameStateUpdated();
        void onMessageReceived(String message);
        void onPlayerTurnStarted(TienLenPlayer player);
        void onCardsPlayed(TienLenPlayer player, List<Card> cardsPlayed, List<Card> lastPlayedCards);
        void onPlayerPassed(TienLenPlayer player);
        void onRoundStarted(TienLenPlayer startingPlayer);
        void onPlayerEliminated(TienLenPlayer player);
        void onGameOver(List<TienLenPlayer> winners);
    }

    // Constructor
    public Game(String name, List<TienLenPlayer> players, Deck deck, R ruleSet) {
        this.name = name;
        this.players = players;
        this.deck = deck;
        this.currentPlayerIndex = 0;
        this.isFinished = false;
        this.ruleSet = ruleSet;
        this.listeners = new ArrayList<>();
        this.generalState = GeneralGameState.INITIALIZING;
    }

    public abstract String getGameStateDisplay();
    protected abstract void runGameLoop();
    protected abstract void stopGameLoop();
    protected abstract void dealCards(int cardsPerPlayer);
    public abstract boolean checkGameOver();
    protected abstract List<TienLenPlayer> determineWinners(); 
    public abstract List<Card> getLastPlayedCards();
    public abstract TienLenPlayer getLastPlayer();
    public abstract boolean isValidPlay(List<Card> cards);
    public abstract void setPlayerInput(List<Card> cards);
    public abstract boolean canPass(TienLenPlayer player);
    public abstract int getPassCount(); 

    public void setOnGameEndCallback(Runnable callback) { 
        this.onGameEndCallback = callback;
    }

    public void addPlayer(TienLenPlayer player) {
        if (this.players == null) {
            this.players = new ArrayList<>();
        }
        this.players.add(player);
    }

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

    public GeneralGameState getGeneralGameState() {
        return generalState;
    }

    public void addGameEventListener(GameEventListener listener) {
        if(!listeners.contains(listener)) {
        	listeners.add(listener);
        }
    }

    protected void notifyGameStateUpdated() {
        for (GameEventListener listener : listeners) {
            Platform.runLater(listener::onGameStateUpdated);
        }
    }

    protected void notifyMessageReceived(String message) {
        for (GameEventListener listener : listeners) {
            Platform.runLater(() -> listener.onMessageReceived(message));
        }
    }

    protected void notifyPlayerTurnStarted(TienLenPlayer player) {
        for (GameEventListener listener : listeners) {
            Platform.runLater(() -> listener.onPlayerTurnStarted(player));
        }
    }

    protected void notifyCardsPlayed(TienLenPlayer player, List<Card> cardsPlayed, List<Card> lastPlayedCards) {
        for (GameEventListener listener : listeners) {
            Platform.runLater(() -> listener.onCardsPlayed(player, cardsPlayed, lastPlayedCards));
        }
    }

    protected void notifyPlayerPassed(TienLenPlayer player) {
        for (GameEventListener listener : listeners) {
            Platform.runLater(() -> listener.onPlayerPassed(player));
        }
    }

    protected void notifyRoundStarted(TienLenPlayer startingPlayer) {
        for (GameEventListener listener : listeners) {
            Platform.runLater(() -> listener.onRoundStarted(startingPlayer));
        }
    }

    protected void notifyPlayerEliminated(TienLenPlayer player) {
        for (GameEventListener listener : listeners) {
            Platform.runLater(() -> listener.onPlayerEliminated(player));
        }
    }

    protected void notifyGameOver(List<TienLenPlayer> winners) {
        for (GameEventListener listener : listeners) {
            System.out.println(listener); 
            Platform.runLater(() -> listener.onGameOver(winners));
        }
    }

    public void startGame() {
        setGeneralGameState(GeneralGameState.RUNNING);
    }

    public void resetGame() {
    }

    public void nextPlayer() {
        if (players == null || players.isEmpty()) { 
            return;
        }
        int nextPlayerCandidate = (currentPlayerIndex + 1) % players.size();
        int originalCandidate = nextPlayerCandidate; 
        while (players.get(nextPlayerCandidate).hasNoCards()) {
            nextPlayerCandidate = (nextPlayerCandidate + 1) % players.size();
            if (nextPlayerCandidate == originalCandidate) { 
                notifyGameStateUpdated(); 
                return;
            }
        }

        currentPlayerIndex = nextPlayerCandidate;
        notifyGameStateUpdated();
    }

    public void removeGameEventListener(GameEventListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public TienLenPlayer getCurrentPlayer() {
        if (players == null || players.isEmpty() || currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) { 
            return null; 
        }
        return players.get(currentPlayerIndex);
    }

    public List<TienLenPlayer> getPlayers() {
        return players;
    }

    public TienLenPlayer getHumanPlayer() {
        if (players == null) return null; 
        for (TienLenPlayer player : players) {
            if (!player.isAI()) {
                return player;
            }
        }
        return null;
    }

    public void setGeneralGameState(GeneralGameState generalState) {
        this.generalState = generalState;
    }
}
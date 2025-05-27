// core/Game.java
package core;

import java.util.ArrayList;
import java.util.List;

import core.games.tienlen.TienLenVariantRuleSet; // Giả sử bạn đã có lớp này
import core.games.tienlen.tienlenplayer.TienLenPlayer; // Giả sử bạn đã có lớp này
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
    public Thread gameThread; // THÊM: Khai báo biến gameThread ở đây (FIELD DECLARATION)
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

    // Phương thức trừu tượng (Abstract Methods)
    public abstract String getGameStateDisplay();
    protected abstract void runGameLoop();
    protected abstract void stopGameLoop();
    protected abstract void dealCards(int cardsPerPlayer);
    public abstract boolean checkGameOver();
    protected abstract List<TienLenPlayer> determineWinners(); // Thêm phương thức trừu tượng này
    public abstract List<Card> getLastPlayedCards();
    public abstract TienLenPlayer getLastPlayer();
    public abstract boolean isValidPlay(List<Card> cards);
    public abstract void setPlayerInput(List<Card> cards);
    public abstract boolean canPass(TienLenPlayer player);
    public abstract int getPassCount(); // Thêm getPassCount vào Game

    // Phương thức (Methods)
    public void setOnGameEndCallback(Runnable callback) { // <-- THÊM DÒNG NÀY
        this.onGameEndCallback = callback;
    }

    // THÊM: Phương thức để thêm người chơi sau khi khởi tạo game
    public void addPlayer(TienLenPlayer player) {
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
            // THAY ĐỔI Ở ĐÂY
            Platform.runLater(listener::onGameStateUpdated);
        }
    }

    protected void notifyMessageReceived(String message) {
        for (GameEventListener listener : listeners) {
            // THAY ĐỔI Ở ĐÂY
            Platform.runLater(() -> listener.onMessageReceived(message));
        }
    }

    protected void notifyPlayerTurnStarted(TienLenPlayer player) {
        for (GameEventListener listener : listeners) {
            // THAY ĐỔI Ở ĐÂY
            Platform.runLater(() -> listener.onPlayerTurnStarted(player));
        }
    }

    protected void notifyCardsPlayed(TienLenPlayer player, List<Card> cardsPlayed, List<Card> lastPlayedCards) {
        for (GameEventListener listener : listeners) {
            // THAY ĐỔI Ở ĐÂY
            Platform.runLater(() -> listener.onCardsPlayed(player, cardsPlayed, lastPlayedCards));
        }
    }

    protected void notifyPlayerPassed(TienLenPlayer player) {
        for (GameEventListener listener : listeners) {
            // THAY ĐỔI Ở ĐÂY
            Platform.runLater(() -> listener.onPlayerPassed(player));
        }
    }

    protected void notifyRoundStarted(TienLenPlayer startingPlayer) {
        for (GameEventListener listener : listeners) {
            // THAY ĐỔI Ở ĐÂY
            Platform.runLater(() -> listener.onRoundStarted(startingPlayer));
        }
    }

    protected void notifyPlayerEliminated(TienLenPlayer player) {
        for (GameEventListener listener : listeners) {
            // THAY ĐỔI Ở ĐÂY
            Platform.runLater(() -> listener.onPlayerEliminated(player));
        }
    }

    protected void notifyGameOver(List<TienLenPlayer> winners) {
        for (GameEventListener listener : listeners) {
            // Dòng System.out.println(listener); có thể giữ lại để debug nếu muốn
            System.out.println(listener); // In ra listener để kiểm tra (nếu đang debug CardGameGUIJavaFX)
            // THAY ĐỔI Ở ĐÂY
            Platform.runLater(() -> listener.onGameOver(winners));
        }
    }

    public void startGame() {
        setGeneralGameState(GeneralGameState.RUNNING);
    }

    public void resetGame() {
        // Mặc định không làm gì, lớp con sẽ ghi đè
    }

    public void nextPlayer() {
        if (players == null || players.isEmpty()) { // Thêm kiểm tra null hoặc rỗng
            return;
        }
        int nextPlayerCandidate = (currentPlayerIndex + 1) % players.size();
        // Vòng lặp để tìm người chơi tiếp theo còn bài
        // Phải đảm bảo không lặp vô hạn nếu tất cả người chơi đã hết bài (isFinished nên xử lý trường hợp này)
        int originalCandidate = nextPlayerCandidate; // Để tránh vòng lặp vô hạn nếu mọi người đều hết bài
        while (players.get(nextPlayerCandidate).hasNoCards()) {
            nextPlayerCandidate = (nextPlayerCandidate + 1) % players.size();
            if (nextPlayerCandidate == originalCandidate) { // Nếu đã quay lại điểm bắt đầu và không tìm thấy ai
                // Tất cả người chơi khác (hoặc tất cả) có thể đã hết bài.
                // Game nên được đánh dấu là isFinished bởi logic checkGameOver()
                // Không nên thay đổi currentPlayerIndex nếu không còn ai để chơi
                notifyGameStateUpdated(); // Cập nhật trạng thái dù không đổi người
                return;
            }
        }

        // Chỉ cập nhật nếu tìm thấy người chơi hợp lệ còn bài
        currentPlayerIndex = nextPlayerCandidate;
        notifyGameStateUpdated();
    }

    // THÊM: Phương thức để xóa người nghe sự kiện
    public void removeGameEventListener(GameEventListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public TienLenPlayer getCurrentPlayer() {
        if (players == null || players.isEmpty() || currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) { // Thêm kiểm tra ràng buộc
            return null; // Hoặc ném một Exception tùy theo thiết kế
        }
        return players.get(currentPlayerIndex);
    }

    public List<TienLenPlayer> getPlayers() {
        return players;
    }

    public TienLenPlayer getHumanPlayer() {
        if (players == null) return null; // Thêm kiểm tra null
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
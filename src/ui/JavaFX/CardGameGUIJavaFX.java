// ui/CardGameGUIJavaFX.java
package ui.JavaFX;

import core.Game;
import core.Player;
import core.Card;
import javafx.application.Platform;
import javafx.scene.Parent; // Root node trong JavaFX
import javafx.scene.*;
import javafx.stage.Stage;   // Cần Stage để quản lý cửa sổ
import java.util.List;
import java.util.ArrayList;

public abstract class CardGameGUIJavaFX<T extends Game<?>> implements Game.GameEventListener {
    protected T game;
    protected Stage primaryStage; // Reference to the primary stage
    protected Parent root; // The root node of the UI scene

    public CardGameGUIJavaFX(T game, Stage primaryStage) {
        this.game = game;
        this.primaryStage = primaryStage;
        this.game.addGameEventListener(this);
        // initGUI sẽ trả về root node của Scene
        this.root = initGUI();
        // Tạo Scene và đặt vào Stage
        Scene scene = new Scene(root, 1200, 800); // Kích thước ban đầu, sẽ được MAXIMIZED
        primaryStage.setScene(scene);
    }

    // Phương thức trừu tượng này sẽ trả về root node của giao diện
    protected abstract Parent initGUI();
    public abstract void displayPlayerHand(Player player);
    public abstract void showMessage(String message);
    public abstract void updateGameState();
    public abstract List<Card> getPlayerCardSelection(Player player);

    @Override
    public void onGameStateUpdated() {
        // Luôn chạy trên JavaFX Application Thread
        Platform.runLater(this::updateGameState);
    }

    @Override
    public void onMessageReceived(String message) {
        Platform.runLater(() -> showMessage(message));
    }

    @Override
    public void onPlayerTurnStarted(Player player) {
        Platform.runLater(() -> {
            showMessage("Lượt của " + player.getName());
            updateGameState();
        });
    }

    @Override
    public void onCardsPlayed(Player player, List<Card> cardsPlayed, List<Card> lastPlayedCards) {
        Platform.runLater(() -> {
            showMessage(player.getName() + " đã đánh: " + cardsPlayed);
            updateGameState();
        });
    }

    @Override
    public void onPlayerPassed(Player player) {
        Platform.runLater(() -> {
            showMessage(player.getName() + " đã BỎ LƯỢT.");
            updateGameState();
        });
    }

    @Override
    public void onRoundStarted(Player startingPlayer) {
        Platform.runLater(() -> {
            showMessage("Vòng mới bắt đầu! Người đi đầu: " + startingPlayer.getName());
            updateGameState();
        });
    }

    @Override
    public void onPlayerEliminated(Player player) {
        Platform.runLater(() -> {
            showMessage(player.getName() + " đã hết bài!");
            updateGameState();
        });
    }

    @Override
    public void onGameOver(List<Player> winners) {
        Platform.runLater(() -> {
            StringBuilder sb = new StringBuilder("GAME KẾT THÚC! Người thắng: ");
            if (winners.isEmpty()) {
                sb.append("Không ai thắng (có thể do lỗi).");
            } else {
                for (int i = 0; i < winners.size(); i++) {
                    sb.append(winners.get(i).getName());
                    if (winners.get(i).getWinnerRank() > 0) {
                        sb.append(" (Hạng: ").append(winners.get(i).getWinnerRank()).append(")");
                    }
                    if (i < winners.size() - 1) {
                        sb.append(", ");
                    }
                }
            }
            showMessage(sb.toString());
            // Enable New Game button here
            // Bạn sẽ cần truy cập newGameButton từ GraphicUIJavaFX
        });
    }
}
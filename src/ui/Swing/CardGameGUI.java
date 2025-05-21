// ui/CardGameGUI.java
package ui.Swing;

import core.Game;
import core.Player;
import core.Card;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList; 

// Lớp CardGameGUI sẽ implement GameEventListener
public abstract class CardGameGUI<T extends Game<?>> extends JFrame implements Game.GameEventListener {
    protected T game; // Sử dụng kiểu chung T cho trường game

    public CardGameGUI(T game) { // Constructor cũng nhận kiểu chung T
        this.game = game;
        this.game.addGameEventListener(this);
        initGUI();
    }

    protected abstract void initGUI();
    public abstract void displayPlayerHand(Player player);
    public abstract void showMessage(String message);
    public abstract void updateGameState();
    public abstract List<Card> getPlayerCardSelection(Player player);

    @Override
    public void onGameStateUpdated() {
        updateGameState();
    }

    @Override
    public void onMessageReceived(String message) {
        showMessage(message);
    }
    
    @Override
    public void onPlayerTurnStarted(Player player) {
        showMessage("Lượt của " + player.getName());
        updateGameState(); 
    }

    @Override
    public void onCardsPlayed(Player player, List<Card> cardsPlayed, List<Card> lastPlayedCards) {
        showMessage(player.getName() + " đã đánh: " + cardsPlayed);
        updateGameState(); 
    }
    
    @Override
    public void onPlayerPassed(Player player) {
        showMessage(player.getName() + " đã BỎ LƯỢT.");
        updateGameState();
    }
    
    @Override
    public void onRoundStarted(Player startingPlayer) {
        showMessage("Vòng mới bắt đầu! Người đi đầu: " + startingPlayer.getName());
        updateGameState();
    }
    
    @Override
    public void onPlayerEliminated(Player player) {
        showMessage(player.getName() + " đã hết bài!");
        updateGameState();
    }

    @Override
    public void onGameOver(List<Player> winners) {
        StringBuilder sb = new StringBuilder("GAME KẾT THÚC! Người thắng: ");
        if (winners.isEmpty()) {
            sb.append("Không ai thắng (có thể do lỗi).");
        } else {
            for (int i = 0; i < winners.size(); i++) {
                sb.append(winners.get(i).getName());
                if (winners.get(i).getWinnerRank() > 0) { // Chỉ hiển thị rank nếu có
                    sb.append(" (Hạng: ").append(winners.get(i).getWinnerRank()).append(")");
                }
                if (i < winners.size() - 1) {
                    sb.append(", ");
                }
            }
        }
        showMessage(sb.toString());
        updateGameState(); // Cập nhật trạng thái cuối cùng của GUI
    }
}
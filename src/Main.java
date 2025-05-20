// Main.java
import core.Game;
import core.Player;
import core.RuleSet;
import core.AIPlayer;
import core.AIPlayer.AIStrategy;
import core.Card;
import core.rules.TienLenGame; // Import TienLenGame
import core.rules.TienLenRule; // Import TienLenRule
import ui.CardGameGUI; // Import lớp trừu tượng CardGameGUI
import ui.GraphicUI; // Import GraphicUI

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.JFrame; // Cần import JFrame
import java.lang.Thread;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // --- CHỌN GAME Ở ĐÂY ---
            // Khởi tạo RuleSet trước
            TienLenRule tienLenRule = new TienLenRule();
            
            // Khởi tạo danh sách người chơi RỖNG trước
            List<Player> players = new ArrayList<>();

            // Khởi tạo game Tiến Lên với danh sách người chơi và RuleSet
            TienLenGame game = new TienLenGame(players, tienLenRule); 

            // Sử dụng kiểu cụ thể cho CardGameGUI để đảm bảo an toàn kiểu
            CardGameGUI<TienLenGame> gui = new GraphicUI(game);

            // Thêm người chơi vào game SAU KHI game đã được khởi tạo
            // RuleSet của game cần được truyền cho AIPlayer
            RuleSet currentRuleSet = game.getRuleSet(); 
            
            Player humanPlayer = new Player("Người chơi 1", false);
            AIPlayer aiPlayer2 = new AIPlayer("Người chơi 2", AIStrategy.GREEDY, currentRuleSet);
            AIPlayer aiPlayer3 = new AIPlayer("Người chơi 3 (AI Tham lam)", AIStrategy.GREEDY, currentRuleSet);
            AIPlayer aiPlayer4 = new AIPlayer("Người chơi 4 (AI Thông minh)", AIStrategy.SMART, currentRuleSet);

            game.addPlayer(humanPlayer);
            game.addPlayer(aiPlayer2);
            game.addPlayer(aiPlayer3);
            game.addPlayer(aiPlayer4);
            
            gui.setTitle(game.getName()); // Sử dụng tên game từ instance
            gui.setSize(1200, 800); 
            gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gui.setVisible(true);
            gui.setLocationRelativeTo(null);

            // Khởi động vòng lặp game trong một thread riêng để không chặn EDT (Event Dispatch Thread)
            // Đặt generalState thành RUNNING trước khi bắt đầu gameThread
            game.dealCards(); // Thêm dòng này để chia bài và tìm người đi đầu
            game.setGeneralGameState(Game.GeneralGameState.RUNNING); // Đặt trạng thái game thành RUNNING
            game.startGameLoop(); // Bắt đầu vòng lặp game
        });
    }
}
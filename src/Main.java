// Main.java (for JavaFX)
import core.*;
import core.AIPlayer.AIStrategy;
import core.rules.TienLenGame;
import core.rules.TienLenRule;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.JavaFX.GraphicUIJavaFX;
import javafx.scene.Scene; // Import Scene

import java.util.ArrayList;
import java.util.List;

// Main lớp sẽ extends Application
public class Main extends Application {

    private TienLenGame game; // Giữ tham chiếu đến game
    private GraphicUIJavaFX gui; // Giữ tham chiếu đến GUI JavaFX

    @Override
    public void start(Stage primaryStage) {
        // --- CHỌN GAME Ở ĐÂY ---
        TienLenRule tienLenRule = new TienLenRule();
        List<Player> players = new ArrayList<>(); // Khởi tạo danh sách người chơi RỖNG

        game = new TienLenGame(players, tienLenRule);
        
        // Thêm người chơi vào game SAU KHI game đã được khởi tạo
        RuleSet currentRuleSet = game.getRuleSet();
        Player humanPlayer = new Player("Người chơi 1", false);
        Player humanPlayer2 = new Player("Người chơi 2", false);
        AIPlayer aiPlayer3 = new AIPlayer("(AI Tham lam)", AIStrategy.GREEDY, currentRuleSet);
        AIPlayer aiPlayer4 = new AIPlayer("(AI Thông minh)", AIStrategy.SMART, currentRuleSet);

        game.addPlayer(humanPlayer);
        game.addPlayer(humanPlayer2);
        game.addPlayer(aiPlayer3);
        game.addPlayer(aiPlayer4);
        
        // Khởi tạo GUI JavaFX
        gui = new GraphicUIJavaFX(game, primaryStage); // Truyền primaryStage vào GUI


        // Thiết lập title cho Stage
        primaryStage.setTitle(game.getName());

        // Hiển thị Stage
        primaryStage.show();

        // Khởi động vòng lặp game trong một thread riêng để không chặn JavaFX Application Thread
        game.dealCards();
        System.out.println("Bai Player 1:" +  humanPlayer.getHand().size());
        System.out.println("Bai Player 2:" +  humanPlayer2.getHand().size());
        System.out.println("Bai AI3:" +  aiPlayer3.getHand().size());
        System.out.println("Bai AI4:" +  aiPlayer3.getHand().size());
        gui.updateGameState();
        game.setGeneralGameState(Game.GeneralGameState.RUNNING);
        game.startGameLoop();
    }

    public static void main(String[] args) {
        launch(args); // Khởi chạy ứng dụng JavaFX
    }
}
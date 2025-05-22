// Main.java
import core.*;
import core.AIPlayer.AIStrategy;
import core.rules.TienLenGame;
import core.rules.TienLenRule;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.JavaFX.GraphicUIJavaFX;
import javafx.scene.Scene; 

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private TienLenGame game; 
    private GraphicUIJavaFX gui; 

    public static volatile boolean isShuttingDown = false; 

    @Override
    public void start(Stage primaryStage) {
        TienLenRule tienLenRule = new TienLenRule();
        List<Player> players = new ArrayList<>(); 

        game = new TienLenGame(players, tienLenRule);
        
        Player humanPlayer1 = new Player("Người chơi 1", false);
        Player humanPlayer2 = new Player("Người chơi 2", false);
        Player aiPlayer3 = new AIPlayer("AI 3", AIPlayer.AIStrategy.SMART, tienLenRule);
        Player aiPlayer4 = new AIPlayer("AI 4", AIPlayer.AIStrategy.SMART, tienLenRule);

        game.addPlayer(humanPlayer1);
        game.addPlayer(humanPlayer2);
        game.addPlayer(aiPlayer3);
        game.addPlayer(aiPlayer4);
        
        gui = new GraphicUIJavaFX(game, primaryStage); // GUI được khởi tạo và tự đăng ký làm listener

        primaryStage.setTitle("Tiến Lên Miền Nam");
        
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Window closing request received. Terminating game thread and application.");
            event.consume(); 
            
            Main.isShuttingDown = true; 
            
            if (game != null) {
                System.out.println("Yêu cầu dừng game loop...");
                // Gỡ bỏ GUI khỏi danh sách người nghe sự kiện của game ngay lập tức
                game.removeGameEventListener(gui); // THAY ĐỔI MỚI TẠI ĐÂY
                game.stopGameLoop(); 
            }

            Platform.exit();
        });
        
        primaryStage.show();

        game.dealCards();
        gui.updateGameState();
        game.setGeneralGameState(Game.GeneralGameState.RUNNING);
        game.startGameLoop();
    }

    @Override
    public void stop() throws Exception {
        if (game != null) {
            System.out.println("Main application stop method called.");
            // Đảm bảo game loop được yêu cầu dừng lại một lần nữa
            game.stopGameLoop(); 
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args); 
    }
}
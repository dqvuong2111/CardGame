// File: ui/JavaFX/SceneManager.java
package ui.JavaFX;

import core.Game;
// ... (các import khác của bạn) ...
import core.ai.tienlenai.TienLenAI;
import core.ai.tienlenai.TienLenAIStrategy;
import core.games.tienlen.AbstractTienLenGame;
import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.samloc.SamLocRule;
import core.games.tienlen.samloc.SamLocGame;
import core.games.tienlen.tienlenmienbac.TienLenMienBacGame;
import core.games.tienlen.tienlenmienbac.TienLenMienBacRule;
import core.games.tienlen.tienlenmiennam.TienLenMienNamGame;
import core.games.tienlen.tienlenmiennam.TienLenMienNamRule;
import core.games.tienlen.tienlenplayer.TienLenPlayer;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label; // Cần cho việc truyền label vào scene con
import javafx.stage.Screen;
import javafx.stage.Stage;
import ui.JavaFX.Scenes.MainMenuSceneView;
import ui.JavaFX.Scenes.PlayerCustomizationSceneView;
import ui.JavaFX.Scenes.SelectAIStrategySceneView;
import ui.JavaFX.Scenes.SelectGameVariantSceneView;
import ui.JavaFX.Scenes.SelectNumberOfHumansSceneView;

// Import các lớp Scene View mới



import java.util.ArrayList;
import java.util.List;

public class SceneManager {
    private Stage primaryStage;
    private GraphicUIJavaFX gameGUI;
    private AbstractTienLenGame<?> currentGame;

    public enum GameVariant {
        SAM_LOC("Sâm Lốc"),
        TIEN_LEN_MIEN_NAM("Tiến Lên Miền Nam"),
        TIEN_LEN_MIEN_BAC("Tiến Lên Miền Bắc");
        private final String displayName;
        GameVariant(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    public static final int FIXED_TOTAL_PLAYERS = 4; // Giữ lại là public static final
    private int numberOfHumanPlayers = 1;
    private int numberOfAIPlayers = FIXED_TOTAL_PLAYERS - numberOfHumanPlayers;
    private GameVariant selectedGameVariant = GameVariant.SAM_LOC;
    private TienLenAI.StrategyType aiStrategy = TienLenAI.StrategyType.SMART;
    private boolean requiredThreeSpades = true;

    // Các label trên màn hình PlayerCustomization mà các scene con cần cập nhật
    private Label currentHumanPlayersDisplayLabel; // Từ PlayerCustomizationSceneView
    private Label currentAIPlayersDisplayLabel;    // Từ PlayerCustomizationSceneView
    private Label currentAIStrategyDisplayLabel;   // Từ PlayerCustomizationSceneView
    private Label currentGameVariantDisplayLabel;


    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Tiến Lên"); // Tiêu đề chung ban đầu
        this.primaryStage.sceneProperty().addListener((obs, oldScene, newScene) -> {
	        if (newScene != null) {
	            System.out.println("Listener: Scene đã thay đổi. Gọi forceMaximize().");
	            Platform.runLater(this::forceMaximize);
	        }
	    });
        forceMaximize();
        showMainMenu(); // Bắt đầu với Main Menu
    }

    public void showMainMenu() {
        MainMenuSceneView mainMenu = new MainMenuSceneView(this);
        Parent root = mainMenu.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tiến Lên - Menu Chính");
        primaryStage.setMaximized(true);
        primaryStage.show(); // Đảm bảo show nếu là lần đầu
    }

    // Phương thức để PlayerCustomizationSceneView đăng ký các label của nó
    public void setPlayerCustomizationLabels(Label humanLabel, Label aiLabel, Label strategyLabel, Label variantLabel) {
        this.currentHumanPlayersDisplayLabel = humanLabel;
        this.currentAIPlayersDisplayLabel = aiLabel;
        this.currentAIStrategyDisplayLabel = strategyLabel;
        this.currentGameVariantDisplayLabel = variantLabel; // LƯU LẠI LABEL LOẠI GAME
        updateDisplayedValuesOnCustomizationScene(); // Cập nhật tất cả các label lần đầu
    }
    
    


    public void showPlayerCustomizationScene() {
        PlayerCustomizationSceneView customizationView = new PlayerCustomizationSceneView(this);
        Parent root = customizationView.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tiến Lên - Tùy Chỉnh Ván Chơi");
        // forceMaximize();
        // updateDisplayedValuesOnCustomizationScene(); // Được gọi bên trong createContent hoặc qua setPlayerCustomizationLabels
    }
    
    // Các scene con sẽ gọi các setter này, sau đó gọi lại updateDisplayedValues...
    public void setNumberOfHumanPlayers(int num) {
        this.numberOfHumanPlayers = num;
        this.numberOfAIPlayers = FIXED_TOTAL_PLAYERS - this.numberOfHumanPlayers;
        if (this.numberOfAIPlayers < 0) this.numberOfAIPlayers = 0;
        updateDisplayedValuesOnCustomizationScene();
    }

    public void setAiStrategy(TienLenAI.StrategyType strategy) {
        this.aiStrategy = strategy;
        updateDisplayedValuesOnCustomizationScene();
    }
    
    public void setSelectedGameVariant(GameVariant variant) {
        this.selectedGameVariant = variant;
        updateDisplayedValuesOnCustomizationScene(); // Đảm bảo label này cũng được cập nhật nếu nó là một phần của các label được quản lý tập trung
    }

    // Getter cho các giá trị cấu hình để SceneView có thể khởi tạo UI đúng
    public int getNumberOfHumanPlayers() { return numberOfHumanPlayers; }
    public int getNumberOfAIPlayers() { return numberOfAIPlayers; } // Tính toán lại nếu cần
    public TienLenAI.StrategyType getAiStrategy() { return aiStrategy; }
    public GameVariant getSelectedGameVariant() { return selectedGameVariant; }


    // Cập nhật các label trên màn hình PlayerCustomization
    // Được gọi sau khi các giá trị thay đổi hoặc khi scene được hiển thị
    private void updateDisplayedValuesOnCustomizationScene() {
        if (currentHumanPlayersDisplayLabel != null) {
            currentHumanPlayersDisplayLabel.setText(String.valueOf(this.numberOfHumanPlayers));
        }
        if (currentAIPlayersDisplayLabel != null) {
            int calculatedAIs = FIXED_TOTAL_PLAYERS - this.numberOfHumanPlayers;
            if (calculatedAIs < 0) calculatedAIs = 0;
            currentAIPlayersDisplayLabel.setText(String.valueOf(calculatedAIs));
        }
        if (currentAIStrategyDisplayLabel != null) {
            currentAIStrategyDisplayLabel.setText(this.aiStrategy.toString());
        }
        // CẬP NHẬT CHO LABEL LOẠI GAME
        if (currentGameVariantDisplayLabel != null) {
            currentGameVariantDisplayLabel.setText(this.selectedGameVariant.toString());
        }
    }

    public void showSelectGameVariantScene(Label gameVariantDisplayLabel) {
        // gameVariantDisplayLabel là Label từ PlayerCustomizationSceneView
        // để SelectGameVariantSceneView có thể cập nhật nó.
        SelectGameVariantSceneView selectVariantView = new SelectGameVariantSceneView(this, gameVariantDisplayLabel);
        Parent root = selectVariantView.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chọn Chế Độ Chơi");
        // Platform.runLater(this::forceMaximize); // Gọi nếu cần và không dùng listener
    }
    
    public void showSelectNumberOfHumansScene(Label humanDisplayLabel, Label aiDisplayLabel) {
        // Truyền các Label từ PlayerCustomizationSceneView vào đây để scene con có thể cập nhật chúng
        SelectNumberOfHumansSceneView selectHumansView = new SelectNumberOfHumansSceneView(this, humanDisplayLabel, aiDisplayLabel);
        Parent root = selectHumansView.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chọn Số Người Chơi Thật");
        // forceMaximize();
    }

    public void showSelectAIStrategyScene(Label strategyDisplayLabel) {
        // Truyền Label từ PlayerCustomizationSceneView
        SelectAIStrategySceneView selectAIStrategyView = new SelectAIStrategySceneView(this, strategyDisplayLabel);
        Parent root = selectAIStrategyView.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chọn Chiến Lược AI");
        // forceMaximize();
    }


    public void startGame() {
        stopCurrentGame(); 

        System.out.println("Bắt đầu startGame():");
        System.out.println("  - Loại game: " + selectedGameVariant);
        System.out.println("  - Người thật: " + numberOfHumanPlayers);
        System.out.println("  - Chiến lược AI: " + aiStrategy);
        
        // Tính toán lại số AI một cách chắc chắn
        this.numberOfAIPlayers = FIXED_TOTAL_PLAYERS - this.numberOfHumanPlayers;
        if (this.numberOfAIPlayers < 0) this.numberOfAIPlayers = 0;
        System.out.println("  - AI Players (đã tính): " + this.numberOfAIPlayers);

        List<TienLenPlayer> players = new ArrayList<>();
        for (int i = 0; i < this.numberOfHumanPlayers; i++) {
            players.add(new TienLenPlayer("Người " + (i + 1), false));
        }

        TienLenVariantRuleSet ruleSetForThisGame;
        if (selectedGameVariant == GameVariant.TIEN_LEN_MIEN_BAC) {
            ruleSetForThisGame = new TienLenMienBacRule();
        } else if (selectedGameVariant == GameVariant.TIEN_LEN_MIEN_NAM) {
            ruleSetForThisGame = new TienLenMienNamRule();
        } else if (selectedGameVariant == GameVariant.SAM_LOC) {
            ruleSetForThisGame = new SamLocRule();
        } else {
            throw new IllegalArgumentException("Loại game không hợp lệ: " + selectedGameVariant);
        }
        System.out.println("  - RuleSet: " + ruleSetForThisGame.getClass().getSimpleName());

        for (int i = 0; i < this.numberOfAIPlayers; i++) {
            // ... (logic tạo AI strategy implementation như cũ) ...
            players.add(new TienLenAI("AI " + (this.numberOfHumanPlayers + i + 1), getAIStrategyImpl(aiStrategy) , ruleSetForThisGame));
        }
        
        if (selectedGameVariant == GameVariant.TIEN_LEN_MIEN_BAC) {
            currentGame = new TienLenMienBacGame(players, (TienLenMienBacRule) ruleSetForThisGame);
        } else if (selectedGameVariant == GameVariant.TIEN_LEN_MIEN_NAM) {
            currentGame = new TienLenMienNamGame(players, (TienLenMienNamRule) ruleSetForThisGame);
        } else if (selectedGameVariant == GameVariant.SAM_LOC) {
            currentGame = new SamLocGame(players, (SamLocRule) ruleSetForThisGame);
        } else {
            throw new IllegalArgumentException("Loại game không hợp lệ: " + selectedGameVariant);
        }
        System.out.println("  - Đã tạo currentGame: " + currentGame.getName());
        
        gameGUI = new GraphicUIJavaFX(currentGame, primaryStage, this);
        // Constructor của GraphicUIJavaFX đã gọi initGUI() và primaryStage.setScene()

        primaryStage.setTitle(currentGame.getName() + " - Đang Chơi");
        // forceMaximize();

        currentGame.dealCards(); 
        // gameGUI.updateGameState(); // Sẽ được gọi qua event listener
        currentGame.setGeneralGameState(Game.GeneralGameState.RUNNING);
        currentGame.startGameLoop();
        
        System.out.println("startGame() hoàn tất, game loop đã bắt đầu.");
    }
    
    // Helper để lấy AI Strategy Implementation (bạn đã có tương tự)
    public TienLenAIStrategy getAIStrategyImpl(TienLenAI.StrategyType type) {
        if (selectedGameVariant == GameVariant.SAM_LOC) {
            requiredThreeSpades = false;
        }
        // ... (return new RandomStrategy(), GreedyStrategy(), SmartStrategy())
        switch (type) {
            case RANDOM: return new core.ai.tienlenai.strategies.RandomStrategy(requiredThreeSpades);
            case GREEDY: return new core.ai.tienlenai.strategies.GreedyStrategy(requiredThreeSpades);
            case SMART:
            default: return new core.ai.tienlenai.strategies.SmartStrategy(requiredThreeSpades);
        }
    }

    public void stopCurrentGame() {
        if (currentGame != null) {
            System.out.println("Yêu cầu dừng game hiện tại...");
            if (gameGUI != null) { // Hủy đăng ký listener trước
                currentGame.removeGameEventListener(gameGUI);
            }
            currentGame.stopGameLoop(); // Dừng luồng game
            // Chờ luồng game kết thúc (có thể không cần thiết nếu setDaemon(true))
            try {
                if (currentGame.gameThread != null && currentGame.gameThread.isAlive()) {
                    currentGame.gameThread.join(500); // Chờ tối đa 0.5 giây
                    if (currentGame.gameThread.isAlive()) {
                        System.err.println("Luồng game không dừng kịp thời.");
                        // currentGame.gameThread.interrupt(); // Cân nhắc nếu join không đủ
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Luồng chính bị ngắt khi chờ luồng game dừng.");
            }
            currentGame = null;
            gameGUI = null;
            System.out.println("Game hiện tại đã được dừng và dọn dẹp.");
        }
    }
    private void forceMaximize() {
	    System.out.println("forceMaximize() được gọi. Trạng thái ban đầu: isMaximized=" + primaryStage.isMaximized() +
	                       ", W=" + primaryStage.getWidth() + ", H=" + primaryStage.getHeight());

	    Screen screen = Screen.getPrimary();
	    Rectangle2D bounds = screen.getVisualBounds();

	    boolean flagIsMaximized = primaryStage.isMaximized();
	    boolean dimensionsAreCorrect = Math.abs(primaryStage.getWidth() - bounds.getWidth()) < 5 && // Cho phép sai số nhỏ
	                                   Math.abs(primaryStage.getHeight() - bounds.getHeight()) < 5;

	    if (flagIsMaximized && dimensionsAreCorrect) {
	        System.out.println("   Đã maximized và kích thước đúng. Không cần hành động.");
	        return;
	    }

	    // Nếu flag isMaximized=true nhưng kích thước sai
	    if (flagIsMaximized && !dimensionsAreCorrect) {
	        System.out.println("   isMaximized=true NHƯNG kích thước SAI. Thử đặt lại W/H thủ công...");
	        // Tạm thời tắt listener của maximizedProperty để tránh nó phản ứng với các thay đổi bên dưới nếu có
	        // (Cần cẩn thận với việc này, có thể không cần thiết)

	        primaryStage.setWidth(bounds.getWidth());
	        primaryStage.setHeight(bounds.getHeight());
	        primaryStage.setX(bounds.getMinX());
	        primaryStage.setY(bounds.getMinY());

	        // Kiểm tra lại ngay sau khi đặt thủ công
	        boolean manualSetWorked = Math.abs(primaryStage.getWidth() - bounds.getWidth()) < 5 &&
	                                  Math.abs(primaryStage.getHeight() - bounds.getHeight()) < 5;

	        System.out.println("   Sau khi đặt W/H thủ công: isMaximized=" + primaryStage.isMaximized() + // Flag có thể bị thay đổi
	                           ", W=" + primaryStage.getWidth() + ", H=" + primaryStage.getHeight() +
	                           ". Manual set worked: " + manualSetWorked);

	        if (manualSetWorked) {
	            // Nếu đặt thủ công đã đúng kích thước, thử đảm bảo flag isMaximized là true
	            // mà không cần toggle mạnh.
	            if (!primaryStage.isMaximized()) { // Nếu flag bị clear do set W/H
	                Platform.runLater(() -> { // Chạy ở pulse tiếp theo
	                    primaryStage.setMaximized(true); // Cố gắng đặt lại flag
	                    System.out.println("   Đặt lại isMaximized=true sau khi manual W/H thành công.");
	                });
	            }
	            System.out.println("   Đặt W/H thủ công có vẻ đã khắc phục.");
	            return; // Kết thúc, hy vọng nó mượt hơn
	        } else {
	            // Nếu đặt thủ công không ăn thua, phải dùng đến toggle
	            System.out.println("   Đặt W/H thủ công không hiệu quả. Buộc phải toggle maximized state...");
	            primaryStage.setMaximized(false);
	            Platform.runLater(() -> {
	                primaryStage.setMaximized(true);
	                System.out.println("   Đã toggle xong: isMaximized=" + primaryStage.isMaximized() +
	                                   ", W=" + primaryStage.getWidth() + ", H=" + primaryStage.getHeight());
	            });
	            return;
	        }
	    }

	    // Nếu flag isMaximized=false
	    if (!flagIsMaximized) {
	        System.out.println("   isMaximized=false. Đang đặt setMaximized(true)...");
	        primaryStage.setMaximized(true);
	        // Kiểm tra lại sau 1 pulse xem lệnh có thực sự hiệu quả về kích thước không
	        Platform.runLater(() -> {
	            boolean newDimsCorrect = primaryStage.isMaximized() &&
	                                     Math.abs(primaryStage.getWidth() - bounds.getWidth()) < 5 &&
	                                     Math.abs(primaryStage.getHeight() - bounds.getHeight()) < 5;
	            System.out.println("   Sau khi setMaximized(true) (từ false): isMaximized=" + primaryStage.isMaximized() +
	                               ", W=" + primaryStage.getWidth() + ", H=" + primaryStage.getHeight() +
	                               ". Dims correct: " + newDimsCorrect);
	            if (primaryStage.isMaximized() && !newDimsCorrect) {
	                 System.err.println("   CẢNH BÁO: setMaximized(true) không làm kích thước đúng! Có thể cần toggle.");
	                 // Lúc này, nếu vẫn sai, có thể phải gọi lại forceMaximize một lần nữa, 
	                 // hoặc chấp nhận rằng có vấn đề sâu hơn.
	                 // primaryStage.setMaximized(false);
	                 // Platform.runLater(() -> primaryStage.setMaximized(true));
	            }
	        });
	    }
	}
}
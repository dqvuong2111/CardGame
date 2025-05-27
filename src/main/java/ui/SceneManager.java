package ui;

import core.Game;
import core.ai.tienlenai.TienLenAI;
import core.ai.tienlenai.AIStrategy;
import core.games.AbstractCardGame;
import core.games.RuleSet;
import core.games.samloc.SamLocGame;
import core.games.samloc.SamLocRule;
import core.games.tienlen.tienlenmienbac.TienLenMienBacGame;
import core.games.tienlen.tienlenmienbac.TienLenMienBacRule;
import core.games.tienlen.tienlenmiennam.TienLenMienNamGame;
import core.games.tienlen.tienlenmiennam.TienLenMienNamRule;
import core.games.tienlenplayer.TienLenPlayer;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ui.Scenes.MainMenuSceneView;
import ui.Scenes.PlayerCustomizationSceneView;
import ui.Scenes.SelectAIStrategySceneView;
import ui.Scenes.SelectGameVariantSceneView;
import ui.Scenes.SelectGraphicScene;
import ui.Scenes.SelectNumberOfHumansSceneView;

import java.util.ArrayList;
import java.util.List;

public class SceneManager {
    private Stage primaryStage;
    private CardGameUI gameGUI;
    private AbstractCardGame<?> currentGame;

    public enum GameVariant {
        SAM_LOC("Sâm Lốc"),
        TIEN_LEN_MIEN_NAM("Tiến Lên Miền Nam"),
        TIEN_LEN_MIEN_BAC("Tiến Lên Miền Bắc");
        private final String displayName;
        GameVariant(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum Graphic {
        BASIC("Basic"),
        GRAPHIC("Graphic");
        private final String displayName;
        Graphic(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }
    
    public static final int FIXED_TOTAL_PLAYERS = 4;
    private int numberOfHumanPlayers = 1;
    private int numberOfAIPlayers = FIXED_TOTAL_PLAYERS - numberOfHumanPlayers;
    private GameVariant selectedGameVariant = GameVariant.TIEN_LEN_MIEN_NAM;
    private TienLenAI.StrategyType aiStrategy = TienLenAI.StrategyType.SMART;
    private Graphic graphic = Graphic.BASIC;

    private Label currentHumanPlayersDisplayLabel; 
    private Label currentAIPlayersDisplayLabel;    
    private Label currentAIStrategyDisplayLabel;   
    private Label currentGameVariantDisplayLabel;
    private Label currentGraphicDisplayLabel;


    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Tiến Lên"); 
        this.primaryStage.sceneProperty().addListener((obs, oldScene, newScene) -> {
	        if (newScene != null) {
	            Platform.runLater(this::forceMaximize);
	        }
	    });
        forceMaximize();
        showMainMenu(); 
    }

    public void showMainMenu() {
        MainMenuSceneView mainMenu = new MainMenuSceneView(this);
        Parent root = mainMenu.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tiến Lên - Menu Chính");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public void setPlayerCustomizationLabels(Label humanLabel, Label aiLabel, Label strategyLabel, Label variantLabel, Label graphicLabel) {
        this.currentHumanPlayersDisplayLabel = humanLabel;
        this.currentAIPlayersDisplayLabel = aiLabel;
        this.currentAIStrategyDisplayLabel = strategyLabel;
        this.currentGameVariantDisplayLabel = variantLabel;
        this.currentGraphicDisplayLabel = graphicLabel;
        updateDisplayedValuesOnCustomizationScene(); 
    }
    
    public void showPlayerCustomizationScene() {
        PlayerCustomizationSceneView customizationView = new PlayerCustomizationSceneView(this);
        Parent root = customizationView.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tiến Lên - Tùy Chỉnh Ván Chơi");
    }
    
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
        updateDisplayedValuesOnCustomizationScene(); 
    }

    public void setSelectedGraphic(Graphic graphic) {
        this.graphic = graphic;
        updateDisplayedValuesOnCustomizationScene(); 
    }

    // Getter cho các giá trị cấu hình để SceneView có thể khởi tạo UI
    public int getNumberOfHumanPlayers() { return numberOfHumanPlayers; }
    public int getNumberOfAIPlayers() { return numberOfAIPlayers; } 
    public TienLenAI.StrategyType getAiStrategy() { return aiStrategy; }
    public GameVariant getSelectedGameVariant() { return selectedGameVariant; }
    public Graphic getGraphic() { return graphic; }


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
        if (currentGameVariantDisplayLabel != null) {
            currentGameVariantDisplayLabel.setText(this.selectedGameVariant.toString());
        }
        if (currentGraphicDisplayLabel != null) {
            currentGraphicDisplayLabel.setText(this.graphic.toString());
        }
    }

    public void showSelectGameVariantScene(Label gameVariantDisplayLabel) {
        SelectGameVariantSceneView selectVariantView = new SelectGameVariantSceneView(this, gameVariantDisplayLabel);
        Parent root = selectVariantView.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chọn Chế Độ Chơi");
    }

    public void showSelectGraphicScene(Label graphicDisplayLabel) {
        SelectGraphicScene selectGraphicView = new SelectGraphicScene(this, graphicDisplayLabel);
        Parent root = selectGraphicView.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chọn Loại Giao diện");
    }
    
    public void showSelectNumberOfHumansScene(Label humanDisplayLabel, Label aiDisplayLabel) {
        SelectNumberOfHumansSceneView selectHumansView = new SelectNumberOfHumansSceneView(this, humanDisplayLabel, aiDisplayLabel);
        Parent root = selectHumansView.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chọn Số Người Chơi Thật");
    }

    public void showSelectAIStrategyScene(Label strategyDisplayLabel) {
        SelectAIStrategySceneView selectAIStrategyView = new SelectAIStrategySceneView(this, strategyDisplayLabel);
        Parent root = selectAIStrategyView.createContent();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chọn Chiến Lược AI");
    }


    public void startGame() {
        stopCurrentGame(); 
        
        // Tính toán lại số AI
        this.numberOfAIPlayers = FIXED_TOTAL_PLAYERS - this.numberOfHumanPlayers;
        if (this.numberOfAIPlayers < 0) this.numberOfAIPlayers = 0;

        List<TienLenPlayer> players = new ArrayList<>();
        for (int i = 0; i < this.numberOfHumanPlayers; i++) {
            players.add(new TienLenPlayer("Người " + (i + 1), false));
        }

        RuleSet ruleSetForThisGame;
        if (selectedGameVariant == GameVariant.TIEN_LEN_MIEN_BAC) {
            ruleSetForThisGame = new TienLenMienBacRule();
        } else if (selectedGameVariant == GameVariant.TIEN_LEN_MIEN_NAM) {
            ruleSetForThisGame = new TienLenMienNamRule();
        } else if (selectedGameVariant == GameVariant.SAM_LOC) {
            ruleSetForThisGame = new SamLocRule();
        } else {
            throw new IllegalArgumentException("Loại game không hợp lệ: " + selectedGameVariant);
        }

        for (int i = 0; i < this.numberOfAIPlayers; i++) {
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
        
        if (graphic == Graphic.BASIC) {
            gameGUI = new BasicUI(currentGame, primaryStage, this);
        } else if (graphic == Graphic.GRAPHIC) {
            gameGUI = new GraphicUI(currentGame, primaryStage, this);
        }

        primaryStage.setTitle(currentGame.getName() + " - Đang Chơi");

        currentGame.dealCards(currentGame.getRuleSet().getCardsPerPlayer()); 
        currentGame.setGeneralGameState(Game.GeneralGameState.RUNNING);
        currentGame.startGameLoop();
    }
    
    public AIStrategy getAIStrategyImpl(TienLenAI.StrategyType type) {
        if (selectedGameVariant == GameVariant.SAM_LOC) {
        }
        switch (type) {
            case RANDOM: return new core.ai.tienlenai.strategies.RandomStrategy();
            case GREEDY: return new core.ai.tienlenai.strategies.GreedyStrategy();
            case SMART:
            default: return new core.ai.tienlenai.strategies.SmartStrategy();
        }
    }

    public void stopCurrentGame() {
        if (currentGame != null) {
            if (gameGUI != null) {
                currentGame.removeGameEventListener(gameGUI);
            }
            currentGame.stopGameLoop();
            try {
                if (currentGame.gameThread != null && currentGame.gameThread.isAlive()) {
                    currentGame.gameThread.join(500);
                    if (currentGame.gameThread.isAlive()) {
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            currentGame = null;
            gameGUI = null;
        }
    }
    private void forceMaximize() {
	    Screen screen = Screen.getPrimary();
	    Rectangle2D bounds = screen.getVisualBounds();

	    boolean flagIsMaximized = primaryStage.isMaximized();
	    boolean dimensionsAreCorrect = Math.abs(primaryStage.getWidth() - bounds.getWidth()) < 5 && // Cho phép sai số nhỏ
	                                   Math.abs(primaryStage.getHeight() - bounds.getHeight()) < 5;

	    if (flagIsMaximized && dimensionsAreCorrect) {
	        return;
	    }

	    if (flagIsMaximized && !dimensionsAreCorrect) {

	        primaryStage.setWidth(bounds.getWidth());
	        primaryStage.setHeight(bounds.getHeight());
	        primaryStage.setX(bounds.getMinX());
	        primaryStage.setY(bounds.getMinY());

	        boolean manualSetWorked = Math.abs(primaryStage.getWidth() - bounds.getWidth()) < 5 &&
	                                  Math.abs(primaryStage.getHeight() - bounds.getHeight()) < 5;

	        if (manualSetWorked) {
	            if (!primaryStage.isMaximized()) {
	                Platform.runLater(() -> {
	                    primaryStage.setMaximized(true); 
	                });
	            }
	            return;
	        } else {
	            primaryStage.setMaximized(false);
	            Platform.runLater(() -> {
	                primaryStage.setMaximized(true);
	            });
	            return;
	        }
	    }

	    if (!flagIsMaximized) {
	        primaryStage.setMaximized(true);
	        Platform.runLater(() -> {
	            boolean newDimsCorrect = primaryStage.isMaximized() &&
	                                     Math.abs(primaryStage.getWidth() - bounds.getWidth()) < 5 &&
	                                     Math.abs(primaryStage.getHeight() - bounds.getHeight()) < 5;
	            
	        });
	    }
	}
}
package ui.JavaFX;

import core.Game;
import core.Player;
import core.ai.AIPlayer;
import core.ai.AIStrategy;
import core.ai.strategies.GreedyStrategy;
import core.ai.strategies.RandomStrategy;
import core.ai.strategies.SmartStrategy;
import core.games.tienlen.tienlenmiennam.TienLenMienNamGame;
import core.games.tienlen.tienlenmiennam.TienLenMienNamRule;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SceneManager {
    private Stage primaryStage;
    private GraphicUIJavaFX gameGUI;
    private TienLenMienNamGame currentGame;

    private int totalPlayers = 2;
    private int humanPlayers = 1;
    private int aiPlayers = 3;
    private AIPlayer.StrategyType aiStrategy = AIPlayer.StrategyType.SMART;

    private Label totalPlayersLabel;
    private Slider totalPlayersSlider;
    private Label humanPlayersLabel;
    private Slider humanPlayersSlider;
    private Label aiPlayersLabel;
    private Slider aiPlayersSlider;
    private ChoiceBox<AIPlayer.StrategyType> aiStrategyChoiceBox;
    
    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Tiến Lên Miền Nam");
        this.primaryStage.setMaximized(true);
        showMainMenu();
    }

    public void showMainMenu() {
        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(50));
        menuLayout.setStyle("-fx-background-color: #f0f8ff;");

        Label titleLabel = new Label("TIẾN LÊN MIỀN NAM");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(javafx.scene.paint.Color.web("#2c3e50"));

        totalPlayersLabel = new Label("Tổng số người chơi: " + totalPlayers);
        totalPlayersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        totalPlayersLabel.setTextFill(javafx.scene.paint.Color.web("#2c3e50"));

        totalPlayersSlider = new Slider(2, 4, totalPlayers);
        totalPlayersSlider.setBlockIncrement(1);
        totalPlayersSlider.setMajorTickUnit(1);
        totalPlayersSlider.setMinorTickCount(0);
        totalPlayersSlider.setShowTickLabels(true);
        totalPlayersSlider.setShowTickMarks(true);
        totalPlayersSlider.setSnapToTicks(true);
        totalPlayersSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            totalPlayers = newVal.intValue();
            totalPlayersLabel.setText("Tổng số người chơi: " + totalPlayers);
            updateSliderRanges();
        });

        humanPlayersLabel = new Label("Số người chơi (bạn và bạn bè): " + humanPlayers);
        humanPlayersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        humanPlayersLabel.setTextFill(javafx.scene.paint.Color.web("#2c3e50"));

        humanPlayersSlider = new Slider(1, totalPlayers, humanPlayers);
        humanPlayersSlider.setBlockIncrement(1);
        humanPlayersSlider.setMajorTickUnit(1);
        humanPlayersSlider.setMinorTickCount(0);
        humanPlayersSlider.setShowTickLabels(true);
        humanPlayersSlider.setShowTickMarks(true);
        humanPlayersSlider.setSnapToTicks(true);
        humanPlayersSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            humanPlayers = newVal.intValue();
            humanPlayersLabel.setText("Số người chơi (bạn và bạn bè): " + humanPlayers);
            updateSliderRanges();
        });

        aiPlayersLabel = new Label("Số lượng AI: " + aiPlayers);
        aiPlayersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        aiPlayersLabel.setTextFill(javafx.scene.paint.Color.web("#2c3e50"));

        aiPlayersSlider = new Slider(0, totalPlayers - humanPlayers, aiPlayers);
        aiPlayersSlider.setBlockIncrement(1);
        aiPlayersSlider.setMajorTickUnit(1);
        aiPlayersSlider.setMinorTickCount(0);
        aiPlayersSlider.setShowTickLabels(true);
        aiPlayersSlider.setShowTickMarks(true);
        aiPlayersSlider.setSnapToTicks(true);
        aiPlayersSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            aiPlayers = newVal.intValue();
            aiPlayersLabel.setText("Số lượng AI: " + aiPlayers);
        });

        Label aiStrategyLabel = new Label("Chiến lược AI:");
        aiStrategyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        aiStrategyLabel.setTextFill(javafx.scene.paint.Color.web("#2c3e50"));

        aiStrategyChoiceBox = new ChoiceBox<>();
        aiStrategyChoiceBox.getItems().addAll(AIPlayer.StrategyType.SMART, AIPlayer.StrategyType.GREEDY, AIPlayer.StrategyType.RANDOM);
        aiStrategyChoiceBox.setValue(aiStrategy);
        aiStrategyChoiceBox.setOnAction(e -> aiStrategy = aiStrategyChoiceBox.getValue());

        Button startGameButton = new Button("Bắt đầu Game");
        startGameButton.setPrefWidth(250);
        startGameButton.setPrefHeight(60);
        startGameButton.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        startGameButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        startGameButton.setOnMouseEntered(e -> startGameButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));
        startGameButton.setOnMouseExited(e -> startGameButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));
        startGameButton.setOnAction(e -> startGame());

        Button exitButton = new Button("Thoát");
        exitButton.setPrefWidth(250);
        exitButton.setPrefHeight(60);
        exitButton.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        exitButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        exitButton.setOnMouseEntered(e -> exitButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));
        exitButton.setOnMouseExited(e -> exitButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));
        exitButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        menuLayout.getChildren().addAll(
                titleLabel,
                totalPlayersLabel,
                totalPlayersSlider,
                humanPlayersLabel,
                humanPlayersSlider,
                aiPlayersLabel,
                aiPlayersSlider,
                aiStrategyLabel,
                aiStrategyChoiceBox,
                startGameButton,
                exitButton
        );

        Scene menuScene = new Scene(menuLayout);
        primaryStage.setScene(menuScene);
        primaryStage.setMaximized(true);
        updateSliderRanges();
    }

    private void updateSliderRanges() {
        humanPlayersSlider.setMax(totalPlayers);
        if (humanPlayers > totalPlayers) {
            humanPlayers = totalPlayers;
            humanPlayersSlider.setValue(humanPlayers);
        }

        aiPlayersSlider.setMax(totalPlayers - humanPlayers);
        if (aiPlayers > (totalPlayers - humanPlayers)) {
            aiPlayers = totalPlayers - humanPlayers;
            aiPlayersSlider.setValue(aiPlayers);
        }
        
        // Đảm bảo tổng số human + AI không vượt quá tổng người chơi
        if (humanPlayers + aiPlayers > totalPlayers) {
            if (aiPlayers > 0) {
                aiPlayers = totalPlayers - humanPlayers;
            } else { // Nếu không có AI, thì human = totalPlayers
                humanPlayers = totalPlayers;
            }
            aiPlayersSlider.setValue(aiPlayers);
            humanPlayersSlider.setValue(humanPlayers);
        }

        humanPlayersLabel.setText("Số người chơi (bạn và bạn bè): " + humanPlayers);
        aiPlayersLabel.setText("Số lượng AI: " + aiPlayers);
    }

    private void startGame() {
        stopCurrentGame();

        TienLenMienNamRule tienLenRule = new TienLenMienNamRule();
        List<Player> players = new ArrayList<>();

        // Add human players
        for (int i = 0; i < humanPlayers; i++) {
            players.add(new Player("Người chơi " + (i + 1), false));
        }

        // Add AI Players
        for (int i = 0; i < aiPlayers; i++) {
            AIStrategy strategyImplementation; // Khai báo biến để giữ instance của strategy

            // Sử dụng giá trị enum this.aiStrategy để quyết định tạo instance nào
            switch (this.aiStrategy) { // this.aiStrategy là enum AIPlayer.AIStrategy (hoặc StrategyType)
                case RANDOM:
                    strategyImplementation = new RandomStrategy(); // Tạo đối tượng RandomStrategy
                    break;
                case GREEDY:
                    strategyImplementation = new GreedyStrategy(); // Tạo đối tượng GreedyStrategy
                    break;
                case SMART:
                default: // Mặc định là SMART nếu có giá trị enum không khớp
                    strategyImplementation = new SmartStrategy(); // Tạo đối tượng SmartStrategy
                    break;
            }
            // Bây giờ truyền strategyImplementation (là một object) vào constructor AIPlayer
            players.add(new AIPlayer("AI " + (humanPlayers + i + 1), strategyImplementation, tienLenRule));
        }

        // If for some reason (e.g. initial setup logic or direct modification)
        // the sum of human and AI players doesn't match totalPlayers, adjust
        // This is a safety net; ideally, updateSliderRanges handles consistency.
        while (players.size() < totalPlayers) {
            players.add(new AIPlayer("AI " + (players.size() + 1), new SmartStrategy(), tienLenRule));
        }
        // Remove excess players if any (shouldn't happen with correct slider logic)
        while (players.size() > totalPlayers) {
            players.remove(players.size() - 1);
        }


        currentGame = new TienLenMienNamGame(players, tienLenRule);
        gameGUI = new GraphicUIJavaFX(currentGame, primaryStage);


        primaryStage.setTitle("Tiến Lên Miền Nam");
        primaryStage.setMaximized(true);
        primaryStage.show();

        currentGame.dealCards();
        gameGUI.updateGameState();
        currentGame.setGeneralGameState(Game.GeneralGameState.RUNNING);
        currentGame.startGameLoop();
    }

    public void stopCurrentGame() {
        if (currentGame != null) {
            System.out.println("Yêu cầu dừng game loop hiện tại...");
            currentGame.removeGameEventListener(gameGUI);
            currentGame.stopGameLoop();
            try {
                if (currentGame.gameThread != null && currentGame.gameThread.isAlive()) {
                    currentGame.gameThread.join(2000);
                    if (currentGame.gameThread.isAlive()) {
                        System.err.println("Game thread did not terminate gracefully. Interrupting.");
                        currentGame.gameThread.interrupt();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Main thread interrupted while waiting for game thread to stop.");
            }
            currentGame = null;
            gameGUI = null;
        }
    }
}
// ui/JavaFX/SceneManager.java
package ui.JavaFX;

import core.AIPlayer;
import core.Game;
import core.Player;
import core.rules.TienLenGame;
import core.rules.TienLenRule;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ChoiceBox; // THÊM: Import ChoiceBox
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // THÊM: Import Optional

public class SceneManager {
    private Stage primaryStage;
    private GraphicUIJavaFX gameGUI;
    private TienLenGame currentGame;

    // Cài đặt game mặc định
    private int numberOfPlayers = 4; // Mặc định 4 người chơi
    private int numberOfAIPlayers = 3; // Mặc định 3 AI, 1 người (tổng 4 người)
    private AIPlayer.AIStrategy aiStrategy = AIPlayer.AIStrategy.SMART; // Mặc định chiến lược SMART

    // Khai báo các biến UI là thành viên của lớp
    private Label numPlayersLabel;
    private Slider numPlayersSlider;
    private Label numAIsLabel;
    private Slider numAIsSlider;
    private ChoiceBox<AIPlayer.AIStrategy> aiStrategyChoiceBox; // Thêm ChoiceBox
    
    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Tiến Lên Miền Nam");
        this.primaryStage.setMaximized(true);
        showMainMenu();
    }

    public void showMainMenu() {
        VBox menuLayout = new VBox(20); // Khoảng cách giữa các phần tử là 20
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(50));
        menuLayout.setStyle("-fx-background-color: #2c3e50;"); // Màu nền tối

        Label titleLabel = new Label("TIẾN LÊN MIỀN NAM");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(javafx.scene.paint.Color.web("#ecf0f1")); // Màu chữ sáng

        // --- Cài đặt số lượng người chơi (bao gồm cả người chơi và AI) ---
        Label numPlayersLabel = new Label("Số lượng người chơi (bao gồm bạn): " + numberOfPlayers);
        numPlayersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        numPlayersLabel.setTextFill(javafx.scene.paint.Color.web("#ecf0f1"));

        Slider numPlayersSlider = new Slider(2, 4, numberOfPlayers); // Min 2, Max 4, Giá trị ban đầu 4
        numPlayersSlider.setBlockIncrement(1);
        numPlayersSlider.setMajorTickUnit(1);
        numPlayersSlider.setMinorTickCount(0);
        numPlayersSlider.setShowTickLabels(true);
        numPlayersSlider.setShowTickMarks(true);
        numPlayersSlider.setSnapToTicks(true);
        numPlayersSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            numberOfPlayers = newVal.intValue();
            // Đảm bảo số lượng AI không vượt quá tổng số người chơi - 1 (vì có 1 người chơi)
            if (numberOfAIPlayers > numberOfPlayers - 1) {
                numberOfAIPlayers = numberOfPlayers - 1;
            }
            numPlayersLabel.setText("Số lượng người chơi (bao gồm bạn): " + numberOfPlayers);
            // Cập nhật nhãn số lượng AI
            numAIsLabel.setText("Số lượng AI: " + numberOfAIPlayers);
            numAIsSlider.setMax(numberOfPlayers - 1); // Cập nhật max của slider AI
        });


        // --- Cài đặt số lượng AI (tối đa bằng số lượng người chơi - 1) ---
        Label numAIsLabel = new Label("Số lượng AI: " + numberOfAIPlayers);
        numAIsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        numAIsLabel.setTextFill(javafx.scene.paint.Color.web("#ecf0f1"));

        Slider numAIsSlider = new Slider(1, numberOfPlayers - 1, numberOfAIPlayers); // Min 1 AI (ít nhất 1 người chơi với AI)
        numAIsSlider.setBlockIncrement(1);
        numAIsSlider.setMajorTickUnit(1);
        numAIsSlider.setMinorTickCount(0);
        numAIsSlider.setShowTickLabels(true);
        numAIsSlider.setShowTickMarks(true);
        numAIsSlider.setSnapToTicks(true);
        numAIsSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            numberOfAIPlayers = newVal.intValue();
            numAIsLabel.setText("Số lượng AI: " + numberOfAIPlayers);
        });

        // Đảm bảo Slider của AI không vượt quá giới hạn khi numberOfPlayers thay đổi
        numPlayersSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            numberOfPlayers = newVal.intValue();
            numPlayersLabel.setText("Số lượng người chơi (bao gồm bạn): " + numberOfPlayers);
            // Đảm bảo số lượng AI không vượt quá tổng số người chơi - 1 (vì có 1 người chơi)
            if (numberOfAIPlayers > numberOfPlayers - 1) {
                numberOfAIPlayers = numberOfPlayers - 1;
            }
            numAIsSlider.setMax(numberOfPlayers - 1);
            numAIsSlider.setValue(numberOfAIPlayers); // Cập nhật giá trị slider AI
            numAIsLabel.setText("Số lượng AI: " + numberOfAIPlayers);
        });


        // --- Chọn chiến lược AI ---
        Label aiStrategyLabel = new Label("Chiến lược AI:");
        aiStrategyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        aiStrategyLabel.setTextFill(javafx.scene.paint.Color.web("#ecf0f1"));

        ChoiceBox<AIPlayer.AIStrategy> aiStrategyChoiceBox = new ChoiceBox<>();
        aiStrategyChoiceBox.getItems().addAll(AIPlayer.AIStrategy.SMART, AIPlayer.AIStrategy.GREEDY, AIPlayer.AIStrategy.RANDOM);
        aiStrategyChoiceBox.setValue(aiStrategy); // Giá trị mặc định
        aiStrategyChoiceBox.setOnAction(e -> aiStrategy = aiStrategyChoiceBox.getValue());


        // --- Nút Bắt đầu game ---
        Button startGameButton = createMenuButton("Bắt đầu Game");
        startGameButton.setOnAction(e -> startGame());

        // --- Nút Thoát ---
        Button exitButton = createMenuButton("Thoát");
        exitButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        menuLayout.getChildren().addAll(
                titleLabel,
                numPlayersLabel,
                numPlayersSlider,
                numAIsLabel,
                numAIsSlider,
                aiStrategyLabel,
                aiStrategyChoiceBox,
                startGameButton,
                exitButton
        );

        Scene menuScene = new Scene(menuLayout);
        primaryStage.setScene(menuScene);
        primaryStage.setMaximized(true);
        
    }

    private void startGame() {
        // Dừng game hiện tại nếu có
        stopCurrentGame();

        TienLenRule tienLenRule = new TienLenRule();
        List<Player> players = new ArrayList<>();

        // Thêm người chơi là con người
        players.add(new Player("Người chơi 1", false));

        // Thêm AI Players dựa trên lựa chọn
        for (int i = 0; i < numberOfAIPlayers; i++) {
            players.add(new AIPlayer("AI " + (i + 2), aiStrategy, tienLenRule)); // AI 2, AI 3, ...
        }

        // Nếu tổng số người chơi ít hơn numberOfPlayers, thêm người chơi AI cho đủ
        while (players.size() < numberOfPlayers) {
            players.add(new AIPlayer("AI " + (players.size() + 1), aiStrategy, tienLenRule));
        }


        currentGame = new TienLenGame(players, tienLenRule);
        gameGUI = new GraphicUIJavaFX(currentGame, primaryStage); // GUI được khởi tạo và tự đăng ký làm listener

        // Thiết lập callback khi game kết thúc để quay lại menu chính
        currentGame.setOnGameEndCallback(() -> {
            Platform.runLater(this::showMainMenu);
        });


        primaryStage.setTitle("Tiến Lên Miền Nam");
        primaryStage.setMaximized(true);
        // GraphicUIJavaFX đã tự tạo và đặt scene vào primaryStage trong constructor của nó
        // Nên chỉ cần đảm bảo primaryStage được hiển thị.
        // primaryStage.setScene(gameGUI.getScene()); // Dòng này không cần thiết nếu GUI tự đặt scene
        primaryStage.show();

        // Khởi động vòng lặp game
        currentGame.dealCards();
        gameGUI.updateGameState(); // Cập nhật trạng thái ban đầu của GUI
        currentGame.setGeneralGameState(Game.GeneralGameState.RUNNING); // Đặt trạng thái game
        currentGame.startGameLoop(); // Bắt đầu game loop trong thread riêng
    }

    // Phương thức để dừng game hiện tại
    public void stopCurrentGame() {
        if (currentGame != null) {
            System.out.println("Yêu cầu dừng game loop hiện tại...");
            currentGame.removeGameEventListener(gameGUI); // Gỡ bỏ listener để tránh lỗi
            currentGame.stopGameLoop();
            currentGame = null;
            gameGUI = null; // Giải phóng GUI
        }
    }

    // --- Helper for Menu Buttons ---
    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(250);
        button.setPrefHeight(60);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));
        return button;
    }
}
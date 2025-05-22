// ui/JavaFX/SceneManager.java
package ui.JavaFX;

import core.AIPlayer;
import core.Player;
import core.rules.TienLenGame;
import core.rules.TienLenRule;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider; // Import Slider
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger; // Để sử dụng biến đếm trong lambda

public class SceneManager {
    private Stage primaryStage;
    private GraphicUIJavaFX gameGUI;
    private TienLenGame currentGame; // Giữ tham chiếu đến game hiện tại

    // Cài đặt game mặc định
    private int numberOfAIPlayers = 3; // Mặc định 3 AI, 1 người (tổng 4 người)

    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Tiến Lên Miền Nam");
        this.primaryStage.setMaximized(true); // Luôn mở toàn màn hình
        showMainMenu(); // Hiển thị Main Menu khi khởi tạo
    }

    public void showMainMenu() {
        VBox menuLayout = new VBox(20); // Khoảng cách giữa các phần tử là 20
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(50));
        menuLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #4CAF50, #8BC34A);"); // Nền xanh lá cây

        Label titleLabel = new Label("TIẾN LÊN MIỀN NAM");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);

        // --- Lựa chọn số người chơi AI ---
        VBox playerSelectionBox = new VBox(10);
        playerSelectionBox.setAlignment(Pos.CENTER);
        Label playerCountLabel = new Label("Chọn số lượng người chơi AI (2-3):");
        playerCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        playerCountLabel.setTextFill(javafx.scene.paint.Color.WHITE);

        Slider aiPlayerSlider = new Slider(1, 3, numberOfAIPlayers); // Min: 1 AI (tổng 2 người), Max: 3 AI (tổng 4 người)
        aiPlayerSlider.setShowTickLabels(true);
        aiPlayerSlider.setShowTickMarks(true);
        aiPlayerSlider.setMajorTickUnit(1);
        aiPlayerSlider.setMinorTickCount(0);
        aiPlayerSlider.setSnapToTicks(true);
        aiPlayerSlider.setPrefWidth(300);
        aiPlayerSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            numberOfAIPlayers = newVal.intValue();
            playerCountLabel.setText("Chọn số lượng người chơi AI (" + (numberOfAIPlayers + 1) + " người chơi):");
        });
        playerSelectionBox.getChildren().addAll(playerCountLabel, aiPlayerSlider);

        Button startButton = createMenuButton("BẮT ĐẦU TRÒ CHƠI");
        startButton.setOnAction(e -> startGame());

        Button exitButton = createMenuButton("THOÁT");
        exitButton.setOnAction(e -> Platform.exit());

        menuLayout.getChildren().addAll(titleLabel, playerSelectionBox, startButton, exitButton);

        Scene menuScene = new Scene(menuLayout, 1200, 800); // Kích thước menu ban đầu
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void startGame() {
        // Dừng game hiện tại nếu có
        if (currentGame != null) {
            System.out.println("Stopping current game before starting a new one...");
            currentGame.stopGameLoop();
            // Đảm bảo game thread đã dừng hẳn trước khi khởi tạo game mới
            try {
                if (currentGame.gameThread != null && currentGame.gameThread.isAlive()) {
                    currentGame.gameThread.join(); // Chờ game thread kết thúc
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Main thread interrupted while waiting for game thread to stop.");
            }
            System.out.println("Current game stopped.");
        }

        // Tạo người chơi
        List<Player> players = new ArrayList<>();
        players.add(new Player("Bạn", false)); // Người chơi người

        for (int i = 0; i < numberOfAIPlayers; i++) {
            players.add(new AIPlayer("AI " + (i + 1), AIPlayer.AIStrategy.SMART, new TienLenRule()));
        }

        // Khởi tạo Game và RuleSet
        TienLenRule ruleSet = new TienLenRule();
        currentGame = new TienLenGame(players, ruleSet);

        // Khởi tạo và thiết lập GUI
        gameGUI = new GraphicUIJavaFX(currentGame, primaryStage); // Truyền primaryStage để GUI quản lý scene
        currentGame.addGameEventListener(gameGUI); // Đăng ký GUI làm listener cho game

        // Đặt callback khi game kết thúc để quay lại menu chính
        currentGame.setOnGameEndCallback(() -> {
            Platform.runLater(this::showMainMenu);
        });


        // Xử lý khi đóng cửa sổ
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Đóng cửa sổ. Dừng game loop...");
            // Kiểm tra và dừng gameThread một cách an toàn
            if (currentGame != null) {
                System.out.println("Yêu cầu dừng game loop...");
                currentGame.removeGameEventListener(gameGUI); // Gỡ bỏ GUI khỏi danh sách người nghe sự kiện của game
                currentGame.stopGameLoop();
                // Đảm bảo game thread đã dừng hẳn trước khi thoát ứng dụng
                try {
                    if (currentGame.gameThread != null && currentGame.gameThread.isAlive()) {
                        currentGame.gameThread.join(); // Chờ game thread kết thúc
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Main thread interrupted while waiting for game thread to stop before exit.");
                }
            }
            Platform.exit();
        });

        // Hiển thị Scene của game
        primaryStage.setScene(gameGUI.getScene()); // GraphicUIJavaFX cần cung cấp một method để lấy Scene
        primaryStage.show();

        // Khởi động vòng lặp game
        currentGame.dealCards();
        gameGUI.updateGameState();
        currentGame.setGeneralGameState(Game.GeneralGameState.RUNNING);
        currentGame.startGameLoop();
    }

    // --- Helper for Menu Buttons ---
    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(250); // Tăng chiều rộng nút
        button.setPrefHeight(60);  // Tăng chiều cao nút
        button.setFont(Font.font("Arial", FontWeight.BOLD, 22)); // Tăng cỡ chữ
        button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;"));
        return button;
    }
}
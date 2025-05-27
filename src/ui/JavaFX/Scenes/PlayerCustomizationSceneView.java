// File: ui/JavaFX/Scenes/PlayerCustomizationSceneView.java
package ui.JavaFX.Scenes;

import core.ai.tienlenai.TienLenAI; // Giả sử bạn có enum này ở đây
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ui.JavaFX.SceneManager; // Import SceneManager

public class PlayerCustomizationSceneView {

    private SceneManager sceneManager;

    // Các label này sẽ được khởi tạo trong createContent và đăng ký với SceneManager
    private Label currentHumanPlayersDisplay;
    private Label currentAIPlayersDisplay;
    private Label currentAIStrategyDisplay;
    private Label currentGameVariantDisplay;

    public PlayerCustomizationSceneView(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent createContent() {
        VBox rootLayout = new VBox(20);
        rootLayout.setAlignment(Pos.CENTER);
        rootLayout.setPadding(new Insets(20));
        String imagePathForCustomization = "/background/mainmenu.jpg"; // Đảm bảo đường dẫn này đúng
        try {
            String imageUrl = getClass().getResource(imagePathForCustomization).toExternalForm();
            rootLayout.setStyle(
                "-fx-background-image: url('" + imageUrl + "'); " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center center; " +
                "-fx-background-size: cover;"
            );
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh nền cho PlayerCustomizationScene: " + imagePathForCustomization + " - " + e.getMessage());
            rootLayout.setStyle("-fx-background-color: #ECEFF1;"); // Màu nền dự phòng
        }

        VBox mainPanel = new VBox(20);
        mainPanel.setAlignment(Pos.CENTER); // Căn giữa mainPanel trong rootLayout
        mainPanel.setPadding(new Insets(30, 40, 30, 40));
        mainPanel.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.5);" + // Tăng độ mờ của nền đen
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.5, 0, 0);" + // Bóng đổ rõ hơn
            "-fx-border-color: rgba(255, 255, 255, 0.2);" + // Viền trắng mờ hơn
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 15;"
        );
        mainPanel.setMaxWidth(650); // Tăng chiều rộng tối đa một chút

        Label sceneTitleLabel = new Label("Tùy Chỉnh Ván Chơi");
        sceneTitleLabel.setFont(Font.font("Arial", FontWeight.BLACK, 32)); // Chữ to hơn
        sceneTitleLabel.setTextFill(Color.WHITE);
        sceneTitleLabel.setMaxWidth(Double.MAX_VALUE);
        sceneTitleLabel.setAlignment(Pos.CENTER);
        DropShadow dsTitle = new DropShadow();
        dsTitle.setOffsetY(2.0); dsTitle.setOffsetX(2.0); dsTitle.setColor(Color.rgb(0,0,0,0.3));
        sceneTitleLabel.setEffect(dsTitle);
        VBox.setMargin(sceneTitleLabel, new Insets(0, 0, 25, 0)); // Tăng margin dưới

        Label fixedTotalDisplay = new Label("Tổng người chơi: " + SceneManager.FIXED_TOTAL_PLAYERS + " (cố định)");
        fixedTotalDisplay.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        fixedTotalDisplay.setTextFill(Color.WHITE);
        fixedTotalDisplay.setMaxWidth(Double.MAX_VALUE);
        fixedTotalDisplay.setAlignment(Pos.CENTER);
        fixedTotalDisplay.setEffect(dsTitle); // Dùng lại dsTitle
        VBox.setMargin(fixedTotalDisplay, new Insets(0, 0, 25, 0)); // Tăng margin dưới

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(20); // Tăng khoảng cách ngang
        settingsGrid.setVgap(18); // Tăng khoảng cách dọc
        
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setPrefWidth(220); // Độ rộng cho cột label
        labelCol.setHalignment(HPos.LEFT);
        ColumnConstraints valueDisplayCol = new ColumnConstraints();
        valueDisplayCol.setPrefWidth(200); // Độ rộng cho cột hiển thị giá trị
        valueDisplayCol.setHalignment(HPos.LEFT);
        ColumnConstraints buttonChangeCol = new ColumnConstraints();
        buttonChangeCol.setPrefWidth(150); // Độ rộng cho cột nút "Thay đổi"
        buttonChangeCol.setHalignment(HPos.RIGHT);
        settingsGrid.getColumnConstraints().addAll(labelCol, valueDisplayCol, buttonChangeCol);

        // 1. Số người chơi thật
        Label humanTitleLabel = new Label("Người chơi thật:");
        styleSettingsLabel(humanTitleLabel);
        currentHumanPlayersDisplay = new Label(); // Khởi tạo rỗng, SceneManager sẽ cập nhật
        styleValueDisplayLabel(currentHumanPlayersDisplay);
        Button changeHumansButton = new Button("Thay đổi");
        styleMiniButton(changeHumansButton);
        changeHumansButton.setOnAction(e -> sceneManager.showSelectNumberOfHumansScene(currentHumanPlayersDisplay, currentAIPlayersDisplay));
        settingsGrid.add(humanTitleLabel, 0, 0);
        settingsGrid.add(currentHumanPlayersDisplay, 1, 0);
        settingsGrid.add(changeHumansButton, 2, 0);

        // 2. Số người chơi AI
        Label aiTitleLabel = new Label("Người chơi AI:");
        styleSettingsLabel(aiTitleLabel);
        currentAIPlayersDisplay = new Label(); // Khởi tạo rỗng
        styleValueDisplayLabel(currentAIPlayersDisplay);
        settingsGrid.add(aiTitleLabel, 0, 1);
        settingsGrid.add(currentAIPlayersDisplay, 1, 1);
        // Không có nút thay đổi cho số AI vì nó được tính tự động

        // 3. Chiến lược AI
        Label strategyTitleLabel = new Label("Chiến lược AI:");
        styleSettingsLabel(strategyTitleLabel);
        currentAIStrategyDisplay = new Label(); // Khởi tạo rỗng
        styleValueDisplayLabel(currentAIStrategyDisplay);
        Button changeStrategyButton = new Button("Thay đổi");
        styleMiniButton(changeStrategyButton);
        changeStrategyButton.setOnAction(e -> sceneManager.showSelectAIStrategyScene(currentAIStrategyDisplay));
        settingsGrid.add(strategyTitleLabel, 0, 2);
        settingsGrid.add(currentAIStrategyDisplay, 1, 2);
        settingsGrid.add(changeStrategyButton, 2, 2);
        
        // 4. Loại game
        Label gameVariantTitleLabel = new Label("Loại game:");
        styleSettingsLabel(gameVariantTitleLabel);
        currentGameVariantDisplay = new Label(); // Khởi tạo rỗng
        styleValueDisplayLabel(currentGameVariantDisplay);
        Button changeGameVariantButton = new Button("Thay đổi");
        styleMiniButton(changeGameVariantButton);
        changeGameVariantButton.setOnAction(e -> sceneManager.showSelectGameVariantScene(currentGameVariantDisplay));
        settingsGrid.add(gameVariantTitleLabel, 0, 3);
        settingsGrid.add(currentGameVariantDisplay, 1, 3);
        settingsGrid.add(changeGameVariantButton, 2, 3);

        HBox bottomButtonBar = new HBox(25); // Tăng khoảng cách nút
        bottomButtonBar.setAlignment(Pos.CENTER);
        VBox.setMargin(bottomButtonBar, new Insets(35, 0, 10, 0)); // Tăng margin trên, giảm margin dưới

        Button backToMainMenuBtn = new Button("Về Menu Chính");
        styleSelectionSceneButton(backToMainMenuBtn, "#7f8c8d", "#606f70", 200); // Nút to hơn
        backToMainMenuBtn.setOnAction(e -> sceneManager.showMainMenu());

        Button startGameCustomBtn = new Button("Bắt Đầu Ván Mới");
        styleSelectionSceneButton(startGameCustomBtn, "#FF8C00", "#FFA500", 200); // Nút to hơn
        startGameCustomBtn.setOnAction(e -> sceneManager.startGame());
        
        bottomButtonBar.getChildren().addAll(backToMainMenuBtn, startGameCustomBtn);

        mainPanel.getChildren().addAll(sceneTitleLabel, fixedTotalDisplay, settingsGrid, bottomButtonBar);
        rootLayout.getChildren().add(mainPanel);

        // Đăng ký các Label với SceneManager để SceneManager có thể cập nhật chúng
        sceneManager.setPlayerCustomizationLabels(
            currentHumanPlayersDisplay,
            currentAIPlayersDisplay,
            currentAIStrategyDisplay,
            currentGameVariantDisplay 
        );
        // SceneManager.updateDisplayedValuesOnCustomizationScene() sẽ được gọi
        // bên trong setPlayerCustomizationLabels để điền giá trị ban đầu.

        return rootLayout;
    }

    // --- Các hàm Style Helper ---
    private void styleSettingsLabel(Label label) {
        label.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // Chữ to hơn chút
        label.setTextFill(Color.WHITE);
        label.setPadding(new Insets(0, 0, 0, 5)); // Chút padding trái
    }

    private void styleValueDisplayLabel(Label label) {
         label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
         label.setTextFill(Color.WHITE); // Đổi màu cho dễ nhìn hơn trên nền tối
         label.setPadding(new Insets(0, 0, 0, 5));
    }

    private void styleMiniButton(Button button) {
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setPrefHeight(35); // Cao hơn chút
        button.setPrefWidth(130); // Rộng hơn chút
        String baseColor = "rgba(255, 255, 255, 0.15)"; 
        String hoverColor = "rgba(255, 255, 255, 0.3)"; 
        String textFill = "white"; 
        String baseStyle = String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-background-radius: 6; " +
            "-fx-border-radius: 6; -fx-border-color: rgba(255,255,255,0.4); -fx-border-width: 1px; -fx-padding: 5 15 5 15;",
            baseColor, textFill);
        String hoverStyle = String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-background-radius: 6; " +
            "-fx-border-radius: 6; -fx-border-color: rgba(255,255,255,0.7); -fx-border-width: 1px; -fx-padding: 5 15 5 15;",
            hoverColor, textFill);
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setCursor(Cursor.HAND);
    }

    private void styleSelectionSceneButton(Button button, String baseColor, String hoverColor, double width) {
		button.setPrefWidth(width);
		button.setPrefHeight(50); // Cao hơn chút
		button.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // Chữ to hơn
		String baseStyle = String.format(
				"-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 7, 0.0, 0, 2);",
				baseColor);
		String hoverStyle = String.format(
				"-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 10, 0.0, 0, 3);",
				hoverColor);
		button.setStyle(baseStyle);
		button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
		button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setCursor(Cursor.HAND);
	}
}
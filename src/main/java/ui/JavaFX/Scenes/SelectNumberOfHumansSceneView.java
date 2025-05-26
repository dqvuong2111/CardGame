package ui.JavaFX.Scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ui.JavaFX.SceneManager;

public class SelectNumberOfHumansSceneView {
    private SceneManager sceneManager;
    private Label humanCountDisplayLabelToUpdate; // Label từ PlayerCustomizationSceneView
    private Label aiCountDisplayLabelToUpdate;   // Label từ PlayerCustomizationSceneView
    private int tempSelectedHumanPlayers;

    public SelectNumberOfHumansSceneView(SceneManager sceneManager, Label humanCountDisplayLabel, Label aiCountDisplayLabel) {
        this.sceneManager = sceneManager;
        this.humanCountDisplayLabelToUpdate = humanCountDisplayLabel;
        this.aiCountDisplayLabelToUpdate = aiCountDisplayLabel;
        this.tempSelectedHumanPlayers = sceneManager.getNumberOfHumanPlayers(); // Khởi tạo giá trị tạm
    }

    public Parent createContent() {
        VBox rootPane = new VBox(30);
        rootPane.setAlignment(Pos.CENTER);
		rootPane.setPadding(new Insets(30));        String imagePath = "/background/mainmenu.jpg";
        try {
            String imageUrl = getClass().getResource(imagePath).toExternalForm();
            rootPane.setStyle("-fx-background-image: url('" + imageUrl + "'); -fx-background-size: cover;");
        } catch (Exception e) { rootPane.setStyle("-fx-background-color: #ECEFF1;"); }


        Label title = new Label("Chọn Số Người Chơi Thật");
        title.setFont(Font.font("Arial", FontWeight.BLACK, 30)); // Font đậm hơn, màu sắc tùy chỉnh cho phù hợp nền
	    title.setTextFill(javafx.scene.paint.Color.WHITE); // Ví dụ chữ trắng nếu nền tối        title.setFont(Font.font("Arial", FontWeight.BLACK, 30));
        title.setTextFill(Color.WHITE);
        DropShadow dsText = new DropShadow(); dsText.setRadius(3); dsText.setOffsetX(1); dsText.setOffsetY(1); dsText.setColor(Color.rgb(0,0,0,0.7));
        title.setEffect(dsText);

        VBox contentPanel = new VBox(20);
        contentPanel.setAlignment(Pos.CENTER);
        contentPanel.setPadding(new Insets(20));
        contentPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3); -fx-background-radius: 10;");

        HBox selectorBox = new HBox(20);
        selectorBox.setAlignment(Pos.CENTER);

        Button decrementButton = new Button("-");
        // ... (style decrementButton) ...
        styleSubSceneNavButton(decrementButton, "#bdc3c7", "#95a5a6", true);


        Label largeHumanCountDisplay = new Label(String.valueOf(this.tempSelectedHumanPlayers));
        // ... (style largeHumanCountDisplay) ...
        largeHumanCountDisplay.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        largeHumanCountDisplay.setTextFill(Color.WHITE);
        largeHumanCountDisplay.setEffect(dsText);
        largeHumanCountDisplay.setMinWidth(100);
        largeHumanCountDisplay.setAlignment(Pos.CENTER);


        Button incrementButton = new Button("+");
        // ... (style incrementButton) ...
        styleSubSceneNavButton(incrementButton, "#bdc3c7", "#95a5a6", true);


        decrementButton.setOnAction(e -> {
            if (tempSelectedHumanPlayers > 1) {
                tempSelectedHumanPlayers--;
                largeHumanCountDisplay.setText(String.valueOf(tempSelectedHumanPlayers));
            }
        });
        incrementButton.setOnAction(e -> {
            if (tempSelectedHumanPlayers < SceneManager.FIXED_TOTAL_PLAYERS) {
                tempSelectedHumanPlayers++;
                largeHumanCountDisplay.setText(String.valueOf(tempSelectedHumanPlayers));
            }
        });

        selectorBox.getChildren().addAll(decrementButton, largeHumanCountDisplay, incrementButton);

        Button confirmButton = new Button("Xác Nhận");
        // ... (style confirmButton) ...
        styleSubSceneNavButton(confirmButton, "#FF8C00", "#FFA500", false);

        confirmButton.setOnAction(e -> {
            sceneManager.setNumberOfHumanPlayers(this.tempSelectedHumanPlayers); // Cập nhật giá trị trong SceneManager
            // Cập nhật trực tiếp các label đã truyền vào (nếu cần, hoặc để SceneManager tự làm)
            if (humanCountDisplayLabelToUpdate != null) {
                humanCountDisplayLabelToUpdate.setText(String.valueOf(sceneManager.getNumberOfHumanPlayers()));
            }
            if (aiCountDisplayLabelToUpdate != null) {
                aiCountDisplayLabelToUpdate.setText(String.valueOf(sceneManager.getNumberOfAIPlayers()));
            }
            sceneManager.showPlayerCustomizationScene(); // Quay lại
        });
        VBox.setMargin(confirmButton, new Insets(20, 0, 0, 0));

        contentPanel.getChildren().addAll(selectorBox, confirmButton);
        rootPane.getChildren().addAll(title, contentPanel);
        return rootPane;
    }
    
    private void styleSubSceneNavButton(Button button, String baseColor, String hoverColor, boolean isSmall) {
		button.setPrefHeight(isSmall ? 60 : 50);
		if (isSmall) { // +/- buttons
			button.setPrefWidth(60);
            button.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		} else { // Confirm button
			button.setPrefWidth(200); 
            button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		}
		String baseStyle = String.format(
				"-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0.0, 0, 1);",
				baseColor);
		String hoverStyle = String.format(
				"-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0.0, 0, 2);",
				hoverColor);
		button.setStyle(baseStyle);
		button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
		button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setCursor(javafx.scene.Cursor.HAND);
	}
}
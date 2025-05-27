package ui.Scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ui.SceneManager;

import java.util.List;

import core.ai.tienlenai.TienLenAI;

public class SelectAIStrategySceneView {

    private SceneManager sceneManager;
    private Label aiStrategyDisplayLabelToUpdate; 
    private TienLenAI.StrategyType tempSelectedAIStrategy;

    public SelectAIStrategySceneView(SceneManager sceneManager, Label aiStrategyDisplayLabel) {
        this.sceneManager = sceneManager;
        this.aiStrategyDisplayLabelToUpdate = aiStrategyDisplayLabel;
        
        this.tempSelectedAIStrategy = sceneManager.getAiStrategy();
    }

    public Parent createContent() {
        VBox rootPane = new VBox(25); 
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setPadding(new Insets(40)); 

        String imagePath = "/background/mainmenu.jpg"; 
        try {
            String imageUrl = getClass().getResource(imagePath).toExternalForm();
            rootPane.setStyle(
                "-fx-background-image: url('" + imageUrl + "'); " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center center; " +
                "-fx-background-size: cover;"
            );
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh nền cho SelectAIStrategyScene: " + imagePath + " - " + e.getMessage());
            rootPane.setStyle("-fx-background-color: #ECEFF1;"); 
        }

        Label title = new Label("Chọn Chiến Lược Cho AI");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);
        DropShadow dsText = new DropShadow();
        dsText.setRadius(3);
        dsText.setOffsetX(1);
        dsText.setOffsetY(1);
        dsText.setColor(Color.rgb(0, 0, 0, 0.7));
        title.setEffect(dsText);
        VBox.setMargin(title, new Insets(0, 0, 30, 0));

        VBox strategyLabelsContainer = new VBox(18); 
        strategyLabelsContainer.setAlignment(Pos.CENTER);
        strategyLabelsContainer.setMaxWidth(350); 
        Label smartLabel = createStrategyLabel("Thông Minh (Smart)", TienLenAI.StrategyType.SMART);
        Label greedyLabel = createStrategyLabel("Tham Lam (Greedy)", TienLenAI.StrategyType.GREEDY);
        Label randomLabel = createStrategyLabel("Ngẫu Nhiên (Random)", TienLenAI.StrategyType.RANDOM);

        List<Label> allStrategyLabels = List.of(smartLabel, greedyLabel, randomLabel);

        Runnable updateLabelStyles = () -> {
            for (Label label : allStrategyLabels) {
                TienLenAI.StrategyType labelStrategy = (TienLenAI.StrategyType) label.getUserData();
                if (labelStrategy == this.tempSelectedAIStrategy) {
                    label.setStyle(getSelectedStrategyLabelStyle());
                } else {
                    label.setStyle(getNormalStrategyLabelStyle());
                }
            }
        };

        for (Label label : allStrategyLabels) {
            label.setOnMouseClicked(event -> {
                this.tempSelectedAIStrategy = (TienLenAI.StrategyType) label.getUserData();
                updateLabelStyles.run(); 
            });
        }
        
        updateLabelStyles.run(); 

        strategyLabelsContainer.getChildren().addAll(smartLabel, greedyLabel, randomLabel);

        Button confirmButton = new Button("Xác Nhận");
        styleActionButton(confirmButton, "#FF8C00", "#FFA500"); 
        VBox.setMargin(confirmButton, new Insets(35, 0, 0, 0));

        confirmButton.setOnAction(e -> {
            sceneManager.setAiStrategy(this.tempSelectedAIStrategy); 
            
            if (aiStrategyDisplayLabelToUpdate != null) {
                aiStrategyDisplayLabelToUpdate.setText(this.tempSelectedAIStrategy.toString());
            }
            sceneManager.showPlayerCustomizationScene(); 
        });

        rootPane.getChildren().addAll(title, strategyLabelsContainer, confirmButton);
        return rootPane;
    }

    private Label createStrategyLabel(String text, TienLenAI.StrategyType strategyType) {
        Label label = new Label(text);
        label.setUserData(strategyType); 
        label.setCursor(Cursor.HAND);
        return label;
    }

    private String getNormalStrategyLabelStyle() {
        return "-fx-font-family: 'Arial'; -fx-font-size: 22px; " +
               "-fx-padding: 12px 25px; -fx-border-color: #FFD700; -fx-border-width: 1px; " +
               "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: rgba(44, 62, 80, 0.7); " + // Nền mờ
               "-fx-text-fill: white; " +
               "-fx-alignment: center; -fx-pref-width: 320px;";
    }

    private String getSelectedStrategyLabelStyle() {
        return "-fx-font-family: 'Arial'; -fx-font-size: 24px; " +
               "-fx-padding: 12px 25px; -fx-border-color: #FFFFFF; -fx-border-width: 2.5px; " +
               "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: rgba(184, 134, 11, 0.85); " + // Nền vàng đồng mờ
               "-fx-text-fill: white; " +
               "-fx-font-weight: bold; -fx-alignment: center; -fx-pref-width: 320px;" +
               "-fx-effect: dropshadow(gaussian, #FFD700, 15, 0.4, 0, 0);";
    }
    
    private void styleActionButton(Button button, String baseColor, String hoverColor) {
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        String baseStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0.0, 0, 1);",
                baseColor);
        String hoverStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0.0, 0, 2);",
                hoverColor);
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setCursor(Cursor.HAND);
    }
}
package ui.Scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ui.SceneManager;
import javafx.application.Platform;

public class MainMenuSceneView {

    private SceneManager sceneManager;

    public MainMenuSceneView(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent createContent() {
        VBox menuLayout = new VBox(30);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(50));

        String imagePath = "/background/mainmenu.jpg";
        try {
            String imageUrl = getClass().getResource(imagePath).toExternalForm();
            menuLayout.setStyle(
                "-fx-background-image: url('" + imageUrl + "'); " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center center; " +
                "-fx-background-size: cover;"
            );
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh nền cho MainMenuScene: " + imagePath + " - " + e.getMessage());
            menuLayout.setStyle("-fx-background-color: #D3D3D3;");
        }

        Label titleLabel = new Label("GAME BÀI");
        titleLabel.setFont(Font.font("Arial", FontWeight.BLACK, 54));
        titleLabel.setTextFill(Color.WHITE);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(2.0);
        ds.setOffsetX(2.0);
        ds.setColor(Color.rgb(50, 50, 50, 0.4));
        titleLabel.setEffect(ds);

        Button newGameButton = new Button("Bắt đầu");
        styleMenuButton(newGameButton, "#FF8C00", "#FFA500");
        newGameButton.setOnAction(e -> sceneManager.showPlayerCustomizationScene());

        Button exitButton = new Button("Thoát Game");
        styleMenuButton(exitButton, "#e74c3c", "#c0392b");
        exitButton.setOnAction(e -> {
            sceneManager.stopCurrentGame();
            Platform.exit();
            System.exit(0);
        });

        menuLayout.getChildren().addAll(titleLabel, newGameButton, exitButton);
        return menuLayout;
    }

    private void styleMenuButton(Button button, String baseColor, String hoverColor) {
        button.setPrefWidth(300);
        button.setPrefHeight(70);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        String baseStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0.0, 0, 3);",
                baseColor);
        String hoverStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0.0, 0, 4);",
                hoverColor);
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setCursor(javafx.scene.Cursor.HAND);
    }
}
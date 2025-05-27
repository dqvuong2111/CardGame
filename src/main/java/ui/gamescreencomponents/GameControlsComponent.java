package ui.gamescreencomponents;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameControlsComponent extends VBox {
    private Button playButton;
    private Button passButton;
    private Button newGameButton;
    private Button backToMainMenuButton;

    public GameControlsComponent(EventHandler<ActionEvent> playAction,
                                 EventHandler<ActionEvent> passAction,
                                 EventHandler<ActionEvent> newGameAction,
                                 EventHandler<ActionEvent> backToMenuAction) {
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(20));
        this.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.3);" +
            "-fx-background-radius: 10;"
        );
        this.setPrefWidth(190);

        playButton = new Button("Đánh bài");
        styleButton(playButton, true);
        playButton.setOnAction(playAction);

        passButton = new Button("Bỏ lượt");
        styleButton(passButton, false);
        passButton.setOnAction(passAction);

        newGameButton = new Button("Ván mới");
        styleButton(newGameButton, false);
        newGameButton.setOnAction(newGameAction);
        newGameButton.setDisable(true); 

        backToMainMenuButton = new Button("Về Menu");
        styleButton(backToMainMenuButton, false);
        backToMainMenuButton.setOnAction(backToMenuAction);
        backToMainMenuButton.setDisable(true);

        this.getChildren().addAll(playButton, passButton, newGameButton, backToMainMenuButton);
    }

    private void styleButton(Button button, boolean isPrimary) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        button.setPrefHeight(40);

        String baseColor = isPrimary ? "#FFFFFF" : "#E8F5E9";
        String hoverColor = isPrimary ? "#F5F5F5" : "#C8E6C9";
        String textColor = "#004D40";

        if (!isPrimary && button.getText().equals("Về Menu")) {
             baseColor = "#f8f9fa";
             hoverColor = "#e2e6ea";
             textColor = "#495057";
        }


        String baseStyle = String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; " +
            "-fx-background-radius: 8; -fx-border-radius: 8; " +
            "-fx-border-color: %s; -fx-border-width: 1.5px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0.0, 0, 2);",
            baseColor, textColor, textColor
        );
        String hoverStyle = String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; " +
            "-fx-background-radius: 8; -fx-border-radius: 8; " +
            "-fx-border-color: %s; -fx-border-width: 1.5px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 7, 0.0, 0, 3);",
            hoverColor, textColor, textColor
        );
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setCursor(javafx.scene.Cursor.HAND);
    }

    public void updateButtonStates(boolean canPlay, boolean canPass, boolean isGameOver) {
        playButton.setDisable(!canPlay || isGameOver);
        passButton.setDisable(!canPass || isGameOver);
        newGameButton.setDisable(!isGameOver);
        backToMainMenuButton.setDisable(!isGameOver);
    }
    
    public void enablePlayButton(boolean enable) {
        if (playButton != null) {
            playButton.setDisable(!enable);
        }
    }
}
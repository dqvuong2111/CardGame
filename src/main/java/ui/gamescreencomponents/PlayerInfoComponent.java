package ui.gamescreencomponents;

import core.games.tienlenplayer.TienLenPlayer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PlayerInfoComponent extends VBox {
    private Label nameLabel;
    private Label cardsCountLabel;
    private TienLenPlayer player;

    public PlayerInfoComponent(TienLenPlayer player) {
        this.player = player;
        this.setPadding(new Insets(8));
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPrefWidth(190);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3); -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1px;");


        nameLabel = new Label(player.getName() + (player.isAI() ? " (AI)" : ""));
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        cardsCountLabel = new Label("Bài: " + player.getHand().size());
        cardsCountLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        
        this.getChildren().addAll(nameLabel, cardsCountLabel);
        updateStyle(false, false);
    }

    public void updateData(TienLenPlayer playerSnapshot, boolean isCurrent, boolean isGameOver) {
        this.player = playerSnapshot;
        cardsCountLabel.setText("Bài: " + player.getHand().size());
        updateStyle(isCurrent, isGameOver);
    }

    public void updateStyle(boolean isCurrent, boolean isGameOver) {
        DropShadow dsText = new DropShadow();
        dsText.setRadius(2); dsText.setOffsetX(1); dsText.setOffsetY(1);
        dsText.setColor(Color.rgb(0,0,0,0.7));

        nameLabel.setEffect(dsText);
        cardsCountLabel.setEffect(dsText);


        if (!isGameOver && isCurrent) {
            this.setStyle(
                "-fx-border-color: #FFFACD; " +
                "-fx-border-width: 3.5; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10;" +
                "-fx-background-color: rgba(0, 100, 0, 0.7);"
            );
            nameLabel.setText(player.getName() + (player.isAI() ? " (AI) - Lượt!" : " (Bạn) - Lượt!"));
            nameLabel.setTextFill(Color.WHITE);
            cardsCountLabel.setTextFill(Color.LIGHTGOLDENRODYELLOW);
        } else {
            if (player.isAI()) {
                this.setStyle(
                    "-fx-border-color: rgba(100, 150, 100, 0.5);" +
                    "-fx-border-width: 1.5; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8;" +
                    "-fx-background-color: rgba(0, 50, 0, 0.55);"
                );
                nameLabel.setText(player.getName() + " (AI)");
                nameLabel.setTextFill(Color.LIGHTGREEN);
                cardsCountLabel.setTextFill(Color.rgb(180, 220, 180));
            } else {
                this.setStyle(
                   "-fx-border-color: rgba(220, 220, 220, 0.3);" +
                   "-fx-border-width: 1; " +
                   "-fx-border-radius: 8; " +
                   "-fx-background-radius: 8;" +
                   "-fx-background-color: rgba(255, 255, 255, 0.25);"
               );
               nameLabel.setText(player.getName());
               nameLabel.setTextFill(Color.rgb(220,220,220));
               cardsCountLabel.setTextFill(Color.rgb(200, 200, 200));
            }
            if (isGameOver && player.getHand().isEmpty()) {
                nameLabel.setTextFill(Color.LIGHTGREEN);
            }
        }
    }
}
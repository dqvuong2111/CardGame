// File: ui/JavaFX/gamescreencomponents/TableCardsComponent.java
package ui.JavaFX.gamescreencomponents;

import core.Card;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


import java.util.List;

public class TableCardsComponent extends VBox {
    private Label playedCardsTitle;
    private HBox playedCardsBox; // Nơi chứa các CardView của bài đã đánh

    public TableCardsComponent() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);
        this.setPadding(new Insets(10));
        this.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.3);" +
            "-fx-background-radius: 10;"
        );

        playedCardsTitle = new Label("Bài trên bàn");
        playedCardsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playedCardsTitle.setTextFill(Color.WHITE);
        DropShadow ds = new DropShadow(); ds.setRadius(2); ds.setOffsetX(1); ds.setOffsetY(1); ds.setColor(Color.rgb(0,0,0,0.7));
        playedCardsTitle.setEffect(ds);

        playedCardsBox = new HBox(5); // Spacing giữa các lá bài
        playedCardsBox.setAlignment(Pos.CENTER);
        playedCardsBox.setPadding(new Insets(10));
        playedCardsBox.setStyle("-fx-background-color: transparent;");

        this.getChildren().addAll(playedCardsTitle, playedCardsBox);
    }

    public void displayCards(List<Card> cards) {
        playedCardsBox.getChildren().clear();
        if (cards != null && !cards.isEmpty()) {
            for (Card card : cards) {
                // Quan trọng: Bạn cần cách để tạo CardView.
                // Nếu CardView là public static inner class của GraphicUIJavaFX:
                // GraphicUIJavaFX.CardView cardView = new GraphicUIJavaFX.CardView(card);
                // Nếu CardView là một lớp public riêng:
                CardView cardView = new CardView(card); // Giả sử có constructor này
                playedCardsBox.getChildren().add(cardView);
            }
        } else {
            Label noCardsLabel = new Label("Không có bài trên bàn");
            noCardsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            noCardsLabel.setTextFill(Color.WHITE);
            DropShadow dsLabel = new DropShadow(); dsLabel.setRadius(2); dsLabel.setOffsetX(1); dsLabel.setOffsetY(1); dsLabel.setColor(Color.rgb(0,0,0,0.7));
            noCardsLabel.setEffect(dsLabel);
            playedCardsBox.getChildren().add(noCardsLabel);
        }
    }
}
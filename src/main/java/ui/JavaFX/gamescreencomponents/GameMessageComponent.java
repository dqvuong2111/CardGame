package ui.JavaFX.gamescreencomponents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameMessageComponent extends VBox { // Kế thừa từ VBox
    private Label messageLabel;

    public GameMessageComponent(String initialMessage) {
        this.messageLabel = new Label(initialMessage);
        styleComponent();
        this.getChildren().add(messageLabel);
    }

    private void styleComponent() {
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(5, 10, 5, 10)); // Padding cho message box
        this.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.3);" + // Nền đen mờ ví dụ
            "-fx-background-radius: 10;"
        );

        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        messageLabel.setTextFill(Color.WHITE);

        DropShadow ds = new DropShadow();
        ds.setRadius(3);
        ds.setOffsetX(1.5);
        ds.setOffsetY(1.5);
        ds.setColor(Color.rgb(0, 0, 0, 0.7));
        messageLabel.setEffect(ds);
    }

    public void setMessage(String text) {
        if (messageLabel != null) {
            messageLabel.setText(text);
        }
    }
}
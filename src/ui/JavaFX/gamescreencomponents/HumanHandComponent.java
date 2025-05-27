// File: ui/JavaFX/gamescreencomponents/HumanHandComponent.java
package ui.JavaFX.gamescreencomponents;

import core.Card;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HumanHandComponent extends VBox {
    private Label humanHandTitle;
    private HBox playerHandBox; // Nơi chứa các CardView của người chơi
    private EventHandler<MouseEvent> onCardClickHandler; // Callback khi card được click

    public HumanHandComponent(EventHandler<MouseEvent> onCardClickHandler) {
        this.onCardClickHandler = onCardClickHandler;
        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);
        this.setPadding(new Insets(10));
        this.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.2);" +
            "-fx-background-radius: 10;"
        );

        humanHandTitle = new Label("Bài của bạn");
        humanHandTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        humanHandTitle.setTextFill(Color.WHITE);
        DropShadow ds = new DropShadow(); ds.setRadius(2); ds.setOffsetX(1); ds.setOffsetY(1); ds.setColor(Color.rgb(0,0,0,0.7));
        humanHandTitle.setEffect(ds);

        playerHandBox = new HBox(5); // Spacing giữa các lá bài
        playerHandBox.setAlignment(Pos.CENTER);
        playerHandBox.setPadding(new Insets(10, 0, 0, 0));

        this.getChildren().addAll(humanHandTitle, playerHandBox);
    }

    public void displayHand(List<Card> handCards, List<Card> selectedCards, Comparator<Card> cardComparator) {
        playerHandBox.getChildren().clear();
        playerHandBox.setAlignment(Pos.CENTER);

        if (handCards == null || handCards.isEmpty()) {
            showFinishedMessage(); // Hoặc một thông báo khác
            return;
        }
        
        List<Card> sortedHand = new ArrayList<>(handCards);
        if (cardComparator != null) {
            Collections.sort(sortedHand, cardComparator);
        } else {
            Collections.sort(sortedHand); // Sắp xếp mặc định nếu không có comparator
        }

        for (Card card : sortedHand) {
            CardView cardView = new CardView(card); // Giả sử CardView có sẵn
            if (selectedCards != null && selectedCards.contains(card)) {
                cardView.setSelected(true);
            } else {
                cardView.setSelected(false);
            }
            // Quan trọng: Gán sự kiện click cho CardView
            // Sự kiện click sẽ gọi callback onCardClickHandler đã được truyền từ GraphicUIJavaFX
            // Callback này sẽ xử lý logic chọn/bỏ chọn trong selectedCards của GraphicUIJavaFX
            // và cập nhật lại trạng thái của CardView (ví dụ: gọi lại cardView.setSelected())
            // và có thể cả trạng thái nút Play.
            // Để CardView tự quản lý selected state và translateY, onCardClickHandler
            // sẽ chỉ cần cập nhật selectedCards và gọi cardView.setSelected()
            if (onCardClickHandler != null) {
                 // Để truyền CardView vào handler, chúng ta cần tạo một handler mới
                 // bao bọc onCardClickHandler hoặc sửa onCardClickHandler để nhận CardView
                 // Tạm thời, gán trực tiếp nếu onCardClickHandler có thể xử lý MouseEvent từ bất kỳ Node nào
                 // và chúng ta tìm CardView từ event.getSource().
                 // Cách tốt hơn là CardView tự xử lý setOnMouseClicked của nó và gọi một callback
                 // mà GraphicUIJavaFX cung cấp, callback đó nhận Card làm tham số.
                 // Trong ví dụ này, chúng ta sẽ để onCardClickHandler được gọi trực tiếp.
                 // GraphicUIJavaFX sẽ cần lấy CardView từ event.getSource().
                 cardView.setOnMouseClicked(onCardClickHandler);
            }
            playerHandBox.getChildren().add(cardView);
        }
    }

    public void showFinishedMessage() {
        playerHandBox.getChildren().clear();
        playerHandBox.setAlignment(Pos.CENTER);
        Label finishedLabel = new Label("Bạn đã hết bài!");
        finishedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        finishedLabel.setTextFill(Color.LIGHTGREEN); // Màu xanh lá
        DropShadow ds = new DropShadow(); ds.setRadius(3); ds.setOffsetX(1); ds.setOffsetY(1); ds.setColor(Color.rgb(0,0,0,0.5));
        finishedLabel.setEffect(ds);
        playerHandBox.getChildren().add(finishedLabel);
    }

    public void showWaitingMessage(String message) {
        playerHandBox.getChildren().clear();
        playerHandBox.setAlignment(Pos.CENTER);
        Label waitingLabel = new Label(message);
        waitingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        waitingLabel.setTextFill(Color.WHITE);
        DropShadow ds = new DropShadow(); ds.setRadius(2); ds.setOffsetX(1); ds.setOffsetY(1); ds.setColor(Color.rgb(0,0,0,0.7));
        waitingLabel.setEffect(ds);
        playerHandBox.getChildren().add(waitingLabel);
    }

    public void clearHand() {
        playerHandBox.getChildren().clear();
    }
    
    public void setHandTitle(String title) {
        if (humanHandTitle != null) {
            humanHandTitle.setText(title);
        }
    }
}
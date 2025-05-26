// File: ui/JavaFX/gamescreencomponents/CardView.java
package ui.JavaFX.gamescreencomponents; // Đặt đúng package

import core.Card; // Import lớp Card từ core
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import java.io.InputStream; // Đã có

// Lớp CardView giờ là một lớp public độc lập
public class CardView extends StackPane {
    private Card card;
    private boolean selected;
    private ImageView cardImageView;
    private static final double SELECTION_OFFSET_Y = -15;

    private static final double CARD_WIDTH = 80;
    private static final double CARD_HEIGHT = 110;

    // Đường dẫn có thể cần điều chỉnh nếu vị trí tương đối thay đổi,
    // nhưng nếu bắt đầu bằng "/" thì nó là tuyệt đối từ gốc resources.
    private static final String CARD_IMAGE_PATH_PREFIX = "/cards/";
    private static final String CARD_IMAGE_EXTENSION = ".png";

    private static Image CARD_BACK_IMAGE = null;

    static {
        try {
            String cardBackPath = CARD_IMAGE_PATH_PREFIX + "BACK" + CARD_IMAGE_EXTENSION;
            // Khi CardView là lớp riêng, getClass() sẽ tham chiếu đến CardView.class
            InputStream stream = CardView.class.getResourceAsStream(cardBackPath);
            if (stream != null) {
                CARD_BACK_IMAGE = new Image(stream);
                if (CARD_BACK_IMAGE.isError()) {
                    System.err.println("Lỗi tải ảnh mặt sau trong CardView: " + cardBackPath + " - " + CARD_BACK_IMAGE.getException());
                    CARD_BACK_IMAGE = null;
                }
            } else {
                System.err.println("LỖI NGHIÊM TRỌNG: Không tìm thấy resource cho ảnh mặt sau tại: " + cardBackPath + " từ CardView.class");
            }
        } catch (Exception e) {
            System.err.println("Ngoại lệ không mong muốn khi tải ảnh mặt sau trong CardView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CardView(Card card) {
        this.card = card;
        this.selected = false;

        cardImageView = new ImageView();
        cardImageView.setFitWidth(CARD_WIDTH);
        cardImageView.setFitHeight(CARD_HEIGHT);
        cardImageView.setSmooth(true);

        loadImage();

        this.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        this.getChildren().add(cardImageView);
        this.setAlignment(Pos.CENTER);

        updateSelectionVisuals();
    }

    private void loadImage() {
        if (this.card == null) {
            if (CARD_BACK_IMAGE != null) {
                cardImageView.setImage(CARD_BACK_IMAGE);
            } else {
                System.err.println("CardView: Card is null và không có CARD_BACK_IMAGE.");
                // Cân nhắc set một màu nền mặc định cho cardImageView nếu không có ảnh
                // Ví dụ: this.setStyle("-fx-background-color: lightgrey; -fx-border-color: black;");
            }
            return;
        }

        String imageFileName = getCardImageFileName(this.card);
        if (imageFileName.equals("error_card_name")) {
            displayErrorOrCardBack();
            return;
        }

        String fullImagePath = CARD_IMAGE_PATH_PREFIX + imageFileName + CARD_IMAGE_EXTENSION;
        // Sử dụng getClass() của instance CardView để lấy resource
        InputStream imageStream = getClass().getResourceAsStream(fullImagePath);

        if (imageStream != null) {
            Image img = new Image(imageStream);
            if (img.isError()) {
                System.err.println("Lỗi khi tạo Image object (ảnh có thể hỏng): " + fullImagePath + " cho " + this.card + " Lỗi: " + img.getException());
                displayErrorOrCardBack();
            } else {
                cardImageView.setImage(img);
            }
        } else {
            System.err.println("Không tìm thấy file ảnh: " + fullImagePath + " cho quân bài " + this.card + " (InputStream is null)");
            displayErrorOrCardBack();
        }
    }

    private void displayErrorOrCardBack() {
        if (CARD_BACK_IMAGE != null) {
            cardImageView.setImage(CARD_BACK_IMAGE);
        } else {
            System.err.println("CardView: Không thể hiển thị ảnh lỗi hoặc mặt sau (CARD_BACK_IMAGE is null).");
        }
    }

    private String getCardImageFileName(Card c) {
        String rankStr;
        int rankValue = c.getRank().getValue();

        switch (rankValue) {
            case 2: rankStr = "2"; break;
            case 14: rankStr = "A"; break;
            case 13: rankStr = "K"; break;
            case 12: rankStr = "Q"; break;
            case 11: rankStr = "J"; break;
            case 10: rankStr = "10"; break;
            case 9: rankStr = "9"; break;
            case 8: rankStr = "8"; break;
            case 7: rankStr = "7"; break;
            case 6: rankStr = "6"; break;
            case 5: rankStr = "5"; break;
            case 4: rankStr = "4"; break;
            case 3: rankStr = "3"; break;
            default:
                System.err.println("Rank không hợp lệ cho tên file ảnh: " + rankValue);
                return "error_card_name";
        }

        String suitStr;
        switch (c.getSuit()) {
            case HEARTS: suitStr = "H"; break;
            case DIAMONDS: suitStr = "D"; break;
            case CLUBS: suitStr = "C"; break;
            case SPADES: suitStr = "S"; break;
            default:
                System.err.println("Suit không hợp lệ cho tên file ảnh: " + c.getSuit());
                return "error_card_name";
        }
        return rankStr + "-" + suitStr; // Giữ nguyên quy ước tên file của bạn
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateSelectionVisuals();
    }

    private void updateSelectionVisuals() {
        if (selected) {
            this.setTranslateY(SELECTION_OFFSET_Y);
            DropShadow glowEffect = new DropShadow();
            glowEffect.setColor(Color.rgb(0, 150, 255, 0.9));
            glowEffect.setWidth(20);
            glowEffect.setHeight(20);
            glowEffect.setRadius(10);
            glowEffect.setSpread(0.6);
            cardImageView.setEffect(glowEffect);
        } else {
            this.setTranslateY(0);
            cardImageView.setEffect(null);
        }
    }

    public Card getCard() {
        return this.card;
    }
}
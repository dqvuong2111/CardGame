package ui; // <-- Đảm bảo dòng này có và đúng

import core.Card;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * CardPanel là một JPanel tùy chỉnh để hiển thị một lá bài.
 * Nó có thể hiển thị trạng thái được chọn (highlight) của lá bài.
 */
public class CardPanel extends JPanel {
    private Card card;
    private boolean selected; // Trạng thái chọn của lá bài

    public CardPanel(Card card, boolean selected) {
        this.card = card;
        this.selected = selected;
        setPreferredSize(new Dimension(80, 110)); // Kích thước mặc định của lá bài
        setOpaque(false); // Đảm bảo nền trong suốt để có thể vẽ bo tròn và đổ bóng
    }

    public Card getCard() {
        return card;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint(); // Yêu cầu vẽ lại khi trạng thái chọn thay đổi
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create(); // Tạo bản sao của Graphics để không ảnh hưởng đến đối tượng gốc

        // Bật Antialiasing cho đường viền mượt mà hơn
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Vẽ nền lá bài (bo tròn)
        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        g2d.setColor(Color.WHITE); // Màu nền lá bài
        g2d.fill(roundedRect);

        // Vẽ viền lá bài (bo tròn)
        g2d.setColor(Color.BLACK); // Màu viền
        g2d.setStroke(new BasicStroke(1)); // Độ dày viền
        g2d.draw(roundedRect);

        // Vẽ overlay khi lá bài được chọn
        if (selected) {
            g2d.setColor(new Color(0, 0, 255, 100)); // Màu xanh trong suốt
            g2d.fill(roundedRect); // Vẽ overlay lên toàn bộ lá bài
            g2d.setColor(Color.BLUE); // Màu viền khi chọn
            g2d.setStroke(new BasicStroke(2)); // Viền dày hơn
            g2d.draw(roundedRect);
            g2d.setStroke(new BasicStroke(1)); // Reset stroke
        }

        // Xác định màu chất bài
        Color suitColor = (card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS) ? Color.RED : Color.BLACK;
        g2d.setColor(suitColor);

        // Vẽ Rank (số/chữ)
        String rankStr = card.getRank().toString();
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int xRank = (getWidth() - fm.stringWidth(rankStr)) / 2; // Canh giữa
        int yRank = fm.getAscent() + 5; // Khoảng cách từ trên xuống
        g2d.drawString(rankStr, xRank, yRank);

        // Vẽ Suit (biểu tượng chất)
        // DÒNG NÀY ĐÒI HỎI Card.Suit CÓ PHƯƠNG THỨC toStringRepresentation()
        String suitStr = card.toString();
        g2d.setFont(new Font("Arial", Font.PLAIN, 18)); // Font nhỏ hơn cho chất
        fm = g2d.getFontMetrics();
        int xSuit = (getWidth() - fm.stringWidth(suitStr)) / 2;
        int ySuit = yRank + fm.getAscent() + 10; // Đặt dưới Rank
        g2d.drawString(suitStr, xSuit, ySuit);

        g2d.dispose(); // Giải phóng tài nguyên đồ họa
    }
}
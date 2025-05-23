package ui;

import core.Card;
import core.Player;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// Import CardPanel (đảm bảo dòng này có)
import ui.CardPanel;

/**
 * PlayerPanel là một JPanel tùy chỉnh để hiển thị thông tin và bài của một người chơi.
 * Nó có thể hiển thị chi tiết bài nếu là người chơi chính (human player)
 * hoặc chỉ hiển thị tên và số lá bài nếu là AI/người chơi khác.
 */
public class PlayerPanel extends JPanel {
    private Player player;
    private JLabel nameLabel;
    private JLabel cardCountLabel;
    private JPanel handDisplayPanel; // Nơi hiển thị các CardPanel cho bài trên tay
    private boolean isHumanPlayer; // True nếu đây là người chơi chính
    private boolean isActivePlayer; // True nếu đây là người chơi đang có lượt

    // Interface để thông báo sự kiện click bài
    public interface PlayerActionListener {
        void onCardClicked(Card card, CardPanel cardPanel);
    }
    private PlayerActionListener actionListener;

    // Constructor
    public PlayerPanel(Player player, boolean isHumanPlayer) {
        this.player = player;
        this.isHumanPlayer = isHumanPlayer;
        // this.selectedCards = new ArrayList<>(); // XÓA DÒNG NÀY
        this.isActivePlayer = false;

        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        nameLabel = new JLabel(player.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        infoPanel.add(nameLabel);

        cardCountLabel = new JLabel("Bài: " + player.getHand().size() + " lá", SwingConstants.CENTER);
        cardCountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        cardCountLabel.setForeground(Color.LIGHT_GRAY);
        infoPanel.add(cardCountLabel);

        if (isHumanPlayer) {
            handDisplayPanel = new JPanel();
            handDisplayPanel.setOpaque(false);
            handDisplayPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

            add(infoPanel, BorderLayout.NORTH);
            add(handDisplayPanel, BorderLayout.CENTER);
        } else {
            add(infoPanel, BorderLayout.CENTER);
        }

        updateBorder();
    }

    public void setPlayerActionListener(PlayerActionListener listener) {
        this.actionListener = listener;
    }

    /**
     * Cập nhật thông tin hiển thị của người chơi (tên, số lá bài, trạng thái thắng).
     */
    public void updatePlayerInfo() {
        String nameText = player.getName();
        if (player.getWinnerRank() > 0) {
            nameText += " (Hạng " + player.getWinnerRank() + ")";
        }
        nameLabel.setText(nameText);
        cardCountLabel.setText("Bài: " + player.getHand().size() + " lá");
        updateBorder();
    }

    /**
     * Cập nhật hiển thị bài trên tay của người chơi chính.
     * Phương thức này cũng nhận vào danh sách các lá bài đã được chọn
     * từ GraphicUI để có thể đánh dấu chúng.
     * @param cards Danh sách các lá bài trên tay.
     * @param currentlySelectedCards Danh sách các lá bài đang được chọn bởi GraphicUI.
     */
    public void setPlayerHand(List<Card> cards, List<Card> currentlySelectedCards) {
        if (isHumanPlayer && handDisplayPanel != null) {
            handDisplayPanel.removeAll();
            if (cards != null) {
                for (Card card : cards) {
                    // Kiểm tra xem lá bài này có trong danh sách selected hiện tại không
                    boolean isSelected = currentlySelectedCards.contains(card);
                    CardPanel cardPanel = new CardPanel(card, isSelected); // Truyền trạng thái selected
                    cardPanel.setPreferredSize(new Dimension(80, 110));
                    cardPanel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (actionListener != null) {
                                actionListener.onCardClicked(card, cardPanel); // Báo cho GraphicUI
                            }
                        }
                    });
                    handDisplayPanel.add(cardPanel);
                }
            }
            handDisplayPanel.revalidate();
            handDisplayPanel.repaint();
        }
    }

    /**
     * Xóa tất cả các lựa chọn bài hiện tại của người chơi.
     * (Phương thức này sẽ không còn dùng selectedCards nội bộ, mà chỉ tắt highlight CardPanel)
     */
    public void clearSelectedCardsVisuals() { // Đổi tên để rõ ràng hơn
        if (handDisplayPanel != null) {
            for (Component comp : handDisplayPanel.getComponents()) {
                if (comp instanceof CardPanel) {
                    ((CardPanel) comp).setSelected(false);
                }
            }
        }
        handDisplayPanel.repaint();
    }

    /**
     * Bật/tắt trạng thái highlight cho người chơi đang có lượt.
     * @param active True nếu người chơi này đang có lượt, false nếu không.
     */
    public void setActive(boolean active) {
        this.isActivePlayer = active;
        updateBorder();
    }

    // XÓA PHƯƠNG THỨC NÀY: getSelectedCards()
    // XÓA PHƯƠNG THỨC NÀY: toggleCardSelection(CardPanel cardPanel)
    // XÓA PHƯƠNG THỨC NÀY: clearSelectedCards()

    private void updateBorder() {
        if (isActivePlayer) {
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLUE, 3),
                player.getName(),
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                Color.BLUE
            ));
        } else {
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                player.getName(),
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.PLAIN, 14),
                Color.GRAY
            ));
        }
    }
}
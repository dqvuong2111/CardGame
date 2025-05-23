package ui.Swing;

import core.*;
import core.rules.TienLenGame;
import core.rules.TienLenRule;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GraphicUI extends CardGameGUI<TienLenGame> {
    private JPanel centerPanel;
    private JPanel playerHandPanel; 
    private JPanel playedCardsPanel; 

    private JButton passButton;
    private JButton playButton;
    private JButton newGameButton;

    private JLabel messageLabel;

    private List<Card> selectedCards = new ArrayList<>();
    
    private final Object playerInputLock = new Object();
    private volatile boolean waitingForInput = false;
    private List<Card> playerSelectedInput = null;

    private Map<Player, JPanel> playerPanels = new HashMap<>();

    public GraphicUI(TienLenGame game) {
        super(game); // Gọi constructor của lớp cha
        // Ép kiểu game để có thể truy cập các phương thức riêng của TienLenGame
        this.game = game; 
    }

    @Override
    protected void initGUI() {
        setTitle("Tiến Lên Miền Nam");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setSize(1200, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));

        // --- TOP PANEL: Messages and Game Info ---
        JPanel topPanel = new JPanel(new BorderLayout());
        messageLabel = new JLabel("Bắt đầu trò chơi!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setForeground(Color.BLUE);
        topPanel.add(messageLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        
        // --- Center Panel (for played cards and AI hands) ---
        centerPanel = new JPanel(new BorderLayout(10, 10)); // Use BorderLayout for playedCardsPanel in center
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        playedCardsPanel = new JPanel();
        playedCardsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        playedCardsPanel.setBorder(new TitledBorder("Bài trên bàn"));
        centerPanel.add(playedCardsPanel, BorderLayout.CENTER); // Center of centerPanel

        JPanel aiPlayersPanel = new JPanel(new GridLayout(1, 3, 10, 10)); // For AI players
        aiPlayersPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add some padding

        // Add AI player panels (assuming 3 AI players for 4 total players)
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.isAI()) {
                JPanel aiPanel = new JPanel();
                aiPanel.setBorder(new TitledBorder(p.getName() + " (AI)"));
                aiPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2)); // Smaller spacing for AI cards
                playerPanels.put(p, aiPanel); // Store reference
                aiPlayersPanel.add(aiPanel);
            }
        }
        centerPanel.add(aiPlayersPanel, BorderLayout.SOUTH); // Place AI hands below played cards

        add(centerPanel, BorderLayout.CENTER);

        // --- Player Hand Panel (South) ---
        playerHandPanel = new JPanel();
        playerHandPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        playerHandPanel.setBorder(new TitledBorder("Bài của bạn"));
        add(playerHandPanel, BorderLayout.SOUTH);

        // --- Control Panel (East) ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        playButton = new JButton("Đánh bài");
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePlayButton();
            }
        });
        controlPanel.add(playButton);
        controlPanel.add(Box.createVerticalStrut(10)); // Spacer

        passButton = new JButton("Bỏ lượt");
        passButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        passButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePassButton();
            }
        });
        controlPanel.add(passButton);
        controlPanel.add(Box.createVerticalStrut(20)); // Spacer

        newGameButton = new JButton("Ván mới");
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNewGameButton();
            }
        });
        controlPanel.add(newGameButton);

        add(controlPanel, BorderLayout.EAST);

        // Pack and set visible
        pack();
        setLocationRelativeTo(null); // Center the window
        setVisible(true);

        // Display initial state
        updateGameState();
    }

    private void handlePlayButton() {
        // Đánh thức luồng game với lựa chọn của người chơi
        if (waitingForInput) {
            TienLenGame tienLenGame = (TienLenGame) game;
            
            // Kiểm tra tính hợp lệ của nước đi trước khi gửi cho game
            if (tienLenGame.isValidPlay(selectedCards)) {
                tienLenGame.setPlayerInput(new ArrayList<>(selectedCards)); // Gửi bản sao của selectedCards
                selectedCards.clear(); // Xóa lựa chọn sau khi gửi
                waitingForInput = false; // Đã nhận được input
            } else {
                showMessage("Bài của bạn không hợp lệ. Vui lòng chọn lại.");
            }
        }
    }

    private void handlePassButton() {
        // Đánh thức luồng game với lựa chọn bỏ lượt
        if (waitingForInput) {
            TienLenGame tienLenGame = (TienLenGame) game;
            // Cho phép pass nếu game rules cho phép
            if (tienLenGame.canPass(tienLenGame.getHumanPlayer())) {
                tienLenGame.setPlayerInput(new ArrayList<>()); // Gửi danh sách rỗng để báo hiệu pass
                selectedCards.clear(); // Xóa lựa chọn
                waitingForInput = false; // Đã nhận được input
            } else {
                showMessage("Bạn không thể bỏ lượt lúc này!");
            }
        }
    }

    private void handleNewGameButton() {
        game.resetGame(); // Gọi phương thức resetGame trong Game
        updateGameState(); // Cập nhật GUI sau khi game được reset
    }


    @Override
    public void displayPlayerHand(Player player) {
        playerHandPanel.removeAll();
        // Sắp xếp bài của người chơi theo quy tắc Tiến Lên để hiển thị
        List<Card> hand = new ArrayList<>(player.getHand());
        hand.sort(((TienLenGame) game).ruleSet.getCardComparator());

        for (Card card : hand) {
            CardPanel cardPanel = new CardPanel(card);
            cardPanel.setPreferredSize(new Dimension(80, 110)); // Kích thước cố định cho CardPanel

            // Đánh dấu thẻ đã chọn
            if (selectedCards.contains(card)) {
                cardPanel.setSelected(true);
            }

            cardPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (((TienLenGame) game).getCurrentState() == TienLenGame.GameState.WAITING_FOR_PLAYER_INPUT) {
                        if (cardPanel.isSelected()) {
                            selectedCards.remove(card);
                        } else {
                            selectedCards.add(card);
                        }
                        cardPanel.setSelected(!cardPanel.isSelected());
                        playerHandPanel.repaint(); // Yêu cầu vẽ lại panel để cập nhật trạng thái chọn
                    }
                }
            });
            playerHandPanel.add(cardPanel);
        }
        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }


    @Override
    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageLabel.setText(message);
        });
    }

    @Override
    public void updateGameState() {
        SwingUtilities.invokeLater(() -> {
            // Cập nhật bài trên bàn
            playedCardsPanel.removeAll();
            List<Card> lastPlayed = game.getLastPlayedCards();
            if (lastPlayed != null && !lastPlayed.isEmpty()) {
                for (Card card : lastPlayed) {
                    CardPanel playedCardPanel = new CardPanel(card);
                    playedCardPanel.setPreferredSize(new Dimension(80, 110));
                    playedCardsPanel.add(playedCardPanel);
                }
            } else {
                playedCardsPanel.add(new JLabel("Không có bài trên bàn."));
            }
            playedCardsPanel.revalidate();
            playedCardsPanel.repaint();

            // Cập nhật bài của người chơi thật
            Player humanPlayer = game.getHumanPlayer();
            if (humanPlayer != null) {
                displayPlayerHand(humanPlayer);
                // Bật/tắt nút dựa trên trạng thái game và lượt người chơi
                boolean isHumanTurn = (game.getCurrentPlayer() == humanPlayer);
                boolean isWaitingForInputState = (((TienLenGame)game).getCurrentState() == TienLenGame.GameState.WAITING_FOR_PLAYER_INPUT);
                
                playButton.setEnabled(isHumanTurn && isWaitingForInputState);
                passButton.setEnabled(isHumanTurn && isWaitingForInputState && game.canPass(humanPlayer));
                newGameButton.setEnabled(game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER);
                
                // Set the flag for player input
                waitingForInput = (isHumanTurn && isWaitingForInputState);
            }

            // Cập nhật bài của các AI và highlight người chơi hiện tại
            for (Player p : game.getPlayers()) {
                if (p.isAI()) {
                    JPanel aiPanel = playerPanels.get(p);
                    if (aiPanel != null) {
                        aiPanel.removeAll();
                        // Chỉ hiển thị số lượng bài của AI, không hiển thị lá bài cụ thể
                        aiPanel.add(new JLabel(p.getName() + ": " + p.getHand().size() + " lá"));
                        aiPanel.revalidate();
                        aiPanel.repaint();
                        
                        // Highlight AI hiện tại
                        if (game.getCurrentPlayer() == p) {
                             aiPanel.setBorder(BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(Color.BLUE, 3), // Blue border for current player
                                p.getName() + " (AI) - Lượt!",
                                TitledBorder.LEFT, TitledBorder.TOP, null, Color.BLUE
                            ));
                        } else {
                            aiPanel.setBorder(BorderFactory.createTitledBorder(p.getName() + " (AI)"));
                        }
                    }
                } else { // Human player panel border
                    if (game.getCurrentPlayer() == p) {
                         playerHandPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(Color.BLUE, 3), // Blue border for current player
                            p.getName() + " (Bạn) - Lượt!",
                            TitledBorder.LEFT, TitledBorder.TOP, null, Color.BLUE
                        ));
                    } else {
                        playerHandPanel.setBorder(BorderFactory.createTitledBorder(p.getName() + " (Bạn)"));
                    }
                }
            }
            repaint();
        });
    }


    @Override
    public List<Card> getPlayerCardSelection(Player player) {
        // Phương thức này không còn được sử dụng trực tiếp để chờ input
        // Thay vào đó, input được gửi qua setPlayerInput
        return null; 
    }

    // Lớp CardPanel để vẽ từng lá bài
    class CardPanel extends JPanel {
        private Card card;
        private boolean selected;

        public CardPanel(Card card) {
            this.card = card;
            this.selected = false;
            setPreferredSize(new Dimension(80, 110));
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            setOpaque(false); // Make it transparent so background can show if needed
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            repaint(); // Redraw to show selection change
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw card background
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            // Draw selection overlay
            if (selected) {
                g2d.setColor(new Color(0, 0, 255, 100)); // Transparent blue overlay
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2d.setColor(Color.BLUE);
                g2d.setStroke(new BasicStroke(2)); // Thicker border
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2d.setStroke(new BasicStroke(1)); // Reset stroke
            }

            // Determine suit color
            Color suitColor = (card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS) ? Color.RED : Color.BLACK;
            g2d.setColor(suitColor);

            // Draw Rank
            String rankStr;
            if(card.getRank().getValue() == 15) rankStr = (card.getRank().getValue() - 13) + card.suitToString(); 
            else
            	rankStr = card.toString(); 
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g2d.getFontMetrics();
            int xRank = (getWidth() - fm.stringWidth(rankStr)) / 2;
            int yRank = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(rankStr, xRank, yRank);

        }
    }
}
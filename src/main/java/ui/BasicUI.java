//package ui;
//
//import core.*;
//import core.rules.TienLenGame;
//import core.rules.TienLenRule;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.List;
//import java.util.ArrayList;
//
///**
// * Lớp BasicUI cung cấp giao diện cơ bản cho game bài sử dụng text
// */
//public class BasicUI extends CardGameGUI {
//    private JPanel centerPanel, northPanel, southPanel, eastPanel, westPanel;
//    private JPanel messagePanel;        // panel riêng cho thông báo
//    private JLabel messageLabel;        // label thông báo
//    private JPanel playedCardsPanel;    // panel hiển thị bài đánh ra giữa màn hình
//    private JButton passButton;         // nút bỏ lượt
//    private JButton playButton;         // nút đánh bài
//    
//    private List<Card> selectedCards = new ArrayList<>(); // Danh sách bài được chọn
//    private List<Card> lastPlayedCards = new ArrayList<>(); // Bài được đánh ra gần nhất
//    
//    private final Object lock = new Object();
//    private boolean waitingForInput = false;
//
//    public BasicUI(Game game) {
//        super(game);
//    }
//
//    @Override
//    protected void initGUI() {
//        setTitle(game.getName() + " - Giao Diện Cơ Bản");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setSize(900, 600);
//        setLayout(new BorderLayout());
//        this.setLocationRelativeTo(null);
//
//        // Tạo các panel cho người chơi
//        northPanel = createPlayerPanel("", new JPanel(new FlowLayout(FlowLayout.CENTER)));
//        southPanel = createPlayerPanel("", new JPanel(new FlowLayout(FlowLayout.CENTER)));
//        eastPanel = createPlayerPanel("", new JPanel(new FlowLayout(FlowLayout.CENTER)));
//        westPanel = createPlayerPanel("", new JPanel(new FlowLayout(FlowLayout.CENTER)));
//
//        // Center panel dùng BorderLayout
//        centerPanel = new JPanel(new BorderLayout());
//        centerPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
//
//        // Message panel đặt ở NORTH của centerPanel
//        messagePanel = new JPanel(new BorderLayout());
//        messageLabel = new JLabel("Hãy bắt đầu ván chơi mới", SwingConstants.CENTER);
//        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
//        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
//        messagePanel.add(messageLabel, BorderLayout.CENTER);
//
//        // Thêm các nút điều khiển
//        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        passButton = new JButton("Bỏ lượt");
//        playButton = new JButton("Đánh bài");
//        JButton newGameButton = new JButton("Ván mới");
//        
//        passButton.addActionListener(e -> {
//            if (waitingForInput) {
//                selectedCards.clear();
//                synchronized (lock) {
//                    waitingForInput = false;
//                    lock.notify();
//                }
//            }
//        });
//        
//        playButton.addActionListener(e -> {
//            if (waitingForInput && !selectedCards.isEmpty()) {
//                // Chỉ cho phép đánh bài nếu hợp lệ
//                if (game instanceof TienLenGame) {
//                    TienLenGame tienLenGame = (TienLenGame) game;
//                    if (tienLenGame.isValidPlay(selectedCards)) {
//                        synchronized (lock) {
//                            waitingForInput = false;
//                            lock.notify();
//                        }
//                    } else {
//                        showMessage("Nước đi không hợp lệ, vui lòng chọn lại!");
//                    }
//                } else {
//                    synchronized (lock) {
//                        waitingForInput = false;
//                        lock.notify();
//                    }
//                }
//            } else {
//                showMessage("Vui lòng chọn bài trước khi đánh!");
//            }
//        });
//        
//        newGameButton.addActionListener(e -> {
//            startNewGame();
//        });
//        
//        controlPanel.add(playButton);
//        controlPanel.add(passButton);
//        controlPanel.add(newGameButton);
//        messagePanel.add(controlPanel, BorderLayout.SOUTH);
//
//        centerPanel.add(messagePanel, BorderLayout.NORTH);
//
//        // Panel hiển thị bài đánh ra
//        playedCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        playedCardsPanel.setBorder(BorderFactory.createTitledBorder("Bài đánh ra"));
//        centerPanel.add(playedCardsPanel, BorderLayout.CENTER);
//
//        add(northPanel, BorderLayout.NORTH);
//        add(southPanel, BorderLayout.SOUTH);
//        add(eastPanel, BorderLayout.EAST);
//        add(westPanel, BorderLayout.WEST);
//        add(centerPanel, BorderLayout.CENTER);
//
//        updatePlayerPanels();
//        setVisible(true);
//    }
//
//    private void startNewGame() {
//        selectedCards.clear();
//        lastPlayedCards.clear();
//        game.startGame();
//        updatePlayerPanels();
//        updateGameState();
//        showMessage("Ván mới bắt đầu! Đến lượt của " + game.getCurrentPlayer().getName());
//    }
//
//    private void updatePlayerPanels() {
//        List<Player> players = game.getPlayers();
//        int numPlayers = players.size();
//        
//        // Cập nhật tiêu đề panel theo số người chơi
//        for (int i = 0; i < numPlayers; i++) {
//            JPanel panel = getPlayerPanel(i);
//            if (panel != null) {
//                ((javax.swing.border.TitledBorder) panel.getBorder()).setTitle(players.get(i).getName());
//            }
//        }
//        
//        // Ẩn các panel không cần thiết
//        westPanel.setVisible(numPlayers > 2);
//        eastPanel.setVisible(numPlayers > 1);
//    }
//
//    private JPanel createPlayerPanel(String title, JPanel panel) {
//        panel.setBorder(BorderFactory.createTitledBorder(title));
//        return panel;
//    }
//
//    @Override
//    public void displayPlayerHand(Player player) {
//        JPanel targetPanel = getPlayerPanel(game.getPlayers().indexOf(player));
//        if (targetPanel != null) {
//            targetPanel.removeAll();
//            List<Card> hand = player.getHand();
//            
//            // Giao diện text dùng các checkbox thay vì button với hình ảnh
//            JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
//            for (Card card : hand) {
//                JCheckBox checkBox = createCardCheckBox(card, player);
//                cardPanel.add(checkBox);
//            }
//            
//            targetPanel.add(cardPanel);
//            targetPanel.revalidate();
//            targetPanel.repaint();
//        }
//    }
//
//    private JCheckBox createCardCheckBox(Card card, Player player) {
//        String text = card.toString();
//        JCheckBox checkBox = new JCheckBox(text);
//        
//        // Đổi màu cho text theo chất bài
//        switch (card.getSuit()) {
//            case HEARTS, DIAMONDS -> checkBox.setForeground(Color.RED);
//            case CLUBS, SPADES -> checkBox.setForeground(Color.BLACK);
//        }
//        
//        checkBox.setFont(new Font("Monospaced", Font.PLAIN, 14));
//        
//        // Chỉ cho phép người chơi hiện tại tương tác
//        boolean isCurrentPlayer = player.equals(game.getCurrentPlayer());
//        boolean isHumanPlayer = !player.isAI();
//        checkBox.setEnabled(isCurrentPlayer && isHumanPlayer && waitingForInput);
//        
//        // Xử lý sự kiện khi chọn bài
//        checkBox.addActionListener(e -> {
//            if (checkBox.isSelected()) {
//                selectedCards.add(card);
//            } else {
//                selectedCards.remove(card);
//            }
//        });
//        
//        return checkBox;
//    }
//
//    private void updatePlayedCards(List<Card> cards) {
//        playedCardsPanel.removeAll();
//        
//        if (cards != null && !cards.isEmpty()) {
//            JPanel cardsDisplay = new JPanel(new FlowLayout(FlowLayout.CENTER));
//            for (Card card : cards) {
//                JLabel cardLabel = new JLabel(card.toString());
//                cardLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
//                
//                // Đổi màu theo chất bài
//                switch (card.getSuit()) {
//                    case HEARTS, DIAMONDS -> cardLabel.setForeground(Color.RED);
//                    case CLUBS, SPADES -> cardLabel.setForeground(Color.BLACK);
//                }
//                
//                cardLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
//                cardLabel.setPreferredSize(new Dimension(40, 25));
//                cardLabel.setHorizontalAlignment(SwingConstants.CENTER);
//                
//                cardsDisplay.add(cardLabel);
//            }
//            playedCardsPanel.add(cardsDisplay);
//        } else {
//            playedCardsPanel.add(new JLabel("Chưa có bài nào được đánh"));
//        }
//        
//        playedCardsPanel.revalidate();
//        playedCardsPanel.repaint();
//    }
//
//    @Override
//    public void showMessage(String message) {
//        messageLabel.setText(message);
//    }
//
//    @Override
//    public int getPlayerCardChoice(Player player) {
//        // Phương thức này không được sử dụng trong triển khai mới
//        // vì chúng ta cho phép chọn nhiều lá bài
//        return -1;
//    }
//    
//    /**
//     * Lấy các lá bài mà người chơi chọn để đánh
//     */
//    public List<Card> getPlayerCardSelection(Player player) {
//        selectedCards.clear();
//        waitingForInput = true;
//        
//        // Cập nhật trạng thái các nút cho người chơi hiện tại
//        updateGameState();
//        
//        showMessage("Đến lượt " + player.getName() + ". Chọn bài và nhấn 'Đánh bài' hoặc 'Bỏ lượt'");
//        
//        synchronized (lock) {
//            try {
//                while (waitingForInput) {
//                    lock.wait();
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        
//        List<Card> result = new ArrayList<>(selectedCards);
//        selectedCards.clear();
//        return result;
//    }
//
//    @Override
//    public void updateGameState() {
//        for (Player player : game.getPlayers()) {
//            displayPlayerHand(player);
//        }
//        
//        // Cập nhật bài đánh ra nếu là game Tiến Lên
//        if (game instanceof TienLenGame) {
//            lastPlayedCards = ((TienLenGame) game).getLastPlayedCards();
//            updatePlayedCards(lastPlayedCards);
//        }
//    }
//
//    private JPanel getPlayerPanel(int playerIndex) {
//        int numPlayers = game.getPlayers().size();
//        
//        if (numPlayers <= 2) {
//            // 2 người chơi: north và south
//            return switch (playerIndex) {
//                case 0 -> northPanel;
//                case 1 -> southPanel;
//                default -> null;
//            };
//        } else if (numPlayers == 3) {
//            // 3 người chơi: north, east, south
//            return switch (playerIndex) {
//                case 0 -> northPanel;
//                case 1 -> eastPanel;
//                case 2 -> southPanel;
//                default -> null;
//            };
//        } else {
//            // 4 người chơi: north, east, south, west
//            return switch (playerIndex) {
//                case 0 -> northPanel;
//                case 1 -> eastPanel;
//                case 2 -> southPanel;
//                case 3 -> westPanel;
//                default -> null;
//            };
//        }
//    }
//}
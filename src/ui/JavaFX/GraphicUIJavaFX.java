package ui.JavaFX;

import core.*;
import core.rules.TienLenGame;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert; // Import Alert
import javafx.scene.control.Alert.AlertType; // Import AlertType
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType; // Import ButtonType
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality; // Import Modality
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional; // Import Optional
import java.util.stream.Collectors;

public class GraphicUIJavaFX extends CardGameGUIJavaFX<TienLenGame> {

    private BorderPane rootLayout;
    private VBox messageBox;
    private Label messageLabel;
    private HBox playedCardsBox;
    private HBox playerHandBox; // HBox chỉ dành cho các CardView của người chơi human
    private VBox controlBox;
    private Button passButton;
    private Button playButton;
    private Button newGameButton;

    private List<Card> selectedCards = new ArrayList<>();
    private volatile boolean waitingForInput = false;

    private Map<Player, VBox> playerPanels; // VBox cho TẤT CẢ players (AI và Human)

    public GraphicUIJavaFX(TienLenGame game, Stage primaryStage) {
        super(game, primaryStage);
        Scene scene = new Scene(initGUI(), 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    @Override
    protected Parent initGUI() {
        System.out.println("initGUI started.");
        playerPanels = new HashMap<>();
        rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(20));

        // --- Message Panel (TOP) ---
        messageBox = new VBox();
        messageLabel = new Label("Chào mừng bạn đến với Tiến Lên Miền Nam!");
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        messageBox.setAlignment(Pos.CENTER);
        messageBox.getChildren().add(messageLabel);
        rootLayout.setTop(messageBox);
        BorderPane.setMargin(messageBox, new Insets(0, 0, 20, 0));

        // --- Players Section (LEFT) ---
        VBox allPlayersInfoBox = new VBox(15);
        allPlayersInfoBox.setPadding(new Insets(10));
        allPlayersInfoBox.setAlignment(Pos.TOP_CENTER);
        allPlayersInfoBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-background-color: #f0f0f0;");
        
        allPlayersInfoBox.setPrefWidth(200);
        allPlayersInfoBox.setMinWidth(180);
        allPlayersInfoBox.setMaxWidth(250);
        System.out.println("allPlayersInfoBox created.");
        
        List<Player> players = game.getPlayers();
        System.out.println("Number of players found: " + players.size());
        
        players.sort((p1, p2) -> {
            if (!p1.isAI() && p2.isAI()) return -1;
            if (p1.isAI() && !p2.isAI()) return 1;
            return p1.getName().compareTo(p2.getName());
        });

        for (Player p : players) {
            VBox playerPanel = new VBox(5);
            playerPanel.setPadding(new Insets(10));
            playerPanel.setAlignment(Pos.CENTER);
            playerPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f8f8f8;");
            
            playerPanel.setPrefWidth(180);
            playerPanel.setMinWidth(150); 
            
            Label nameLabel;
            if (p.isAI()) {
                nameLabel = new Label(p.getName() + " (AI)");
            } else {
                nameLabel = new Label(p.getName());
            }
            nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            Label cardsCountLabel = new Label("Bài: " + p.getHand().size());
            playerPanel.getChildren().addAll(nameLabel, cardsCountLabel);

            playerPanels.put(p, playerPanel);
            allPlayersInfoBox.getChildren().add(playerPanel);
            System.out.println("Added player panel for " + p.getName() + " to allPlayersInfoBox.");
        }
        
        rootLayout.setLeft(allPlayersInfoBox);
        System.out.println("allPlayersInfoBox set to LEFT.");
        BorderPane.setMargin(allPlayersInfoBox, new Insets(0, 20, 0, 0)); 

        // --- Played Cards Section (CENTER) ---
        VBox playedCardsContainer = new VBox(5);
        playedCardsContainer.setAlignment(Pos.CENTER);
        playedCardsContainer.setPadding(new Insets(20));
        playedCardsContainer.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-background-color: #f0f0f0;");
        
        Label playedCardsTitle = new Label("Bài trên bàn");
        playedCardsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        playedCardsBox = new HBox(5);
        playedCardsBox.setAlignment(Pos.CENTER);
        playedCardsBox.setPadding(new Insets(10));
        playedCardsBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #e0e0e0;");
        
        playedCardsContainer.getChildren().addAll(playedCardsTitle, playedCardsBox);
        rootLayout.setCenter(playedCardsContainer);
        BorderPane.setMargin(playedCardsContainer, new Insets(0, 0, 20, 0)); 

        // --- Player Hand Panel (BOTTOM) ---
        VBox humanHandDisplayContainer = new VBox(5); 
        humanHandDisplayContainer.setAlignment(Pos.CENTER);
        humanHandDisplayContainer.setPadding(new Insets(10));
        humanHandDisplayContainer.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f0f0ff;");

        Label humanHandTitle = new Label("Bài của bạn");
        humanHandTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        playerHandBox = new HBox(5);
        playerHandBox.setAlignment(Pos.CENTER);
        playerHandBox.setPadding(new Insets(10, 0, 0, 0));

        humanHandDisplayContainer.getChildren().addAll(humanHandTitle, playerHandBox);
        rootLayout.setBottom(humanHandDisplayContainer);

        // --- Control Panel (RIGHT) ---
        controlBox = new VBox(20);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(20, 10, 20, 10));
        controlBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-background-color: #e8e8e8;");

        controlBox.setPrefWidth(150);
        controlBox.setMaxWidth(180);

        playButton = new Button("Đánh bài");
        playButton.setMaxWidth(Double.MAX_VALUE);
        playButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playButton.setOnAction(event -> handlePlayButton());

        passButton = new Button("Bỏ lượt");
        passButton.setMaxWidth(Double.MAX_VALUE);
        passButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        passButton.setOnAction(event -> handlePassButton());

        newGameButton = new Button("Ván mới");
        newGameButton.setMaxWidth(Double.MAX_VALUE);
        newGameButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        newGameButton.setOnAction(event -> handleNewGameButton());
        newGameButton.setDisable(true); // Vô hiệu hóa nút new game lúc đầu

        controlBox.getChildren().addAll(playButton, passButton, newGameButton);
        rootLayout.setRight(controlBox);
        System.out.println("initGUI finished. RootLayout has " + rootLayout.getChildren().size() + " children.");
        return rootLayout;
    }

    @Override
    public void displayPlayerHand(Player player) {
        if (!player.isAI() && game.getCurrentPlayer() == player) {
            playerHandBox.getChildren().clear();
            List<Card> hand = new ArrayList<>(player.getHand());
            hand.sort(((TienLenGame) game).ruleSet.getCardComparator());

            for (Card card : hand) {
                CardView cardView = new CardView(card);
                if (selectedCards.contains(card)) {
                    cardView.setSelected(true);
                }

                cardView.setOnMouseClicked(event -> {
                    if (((TienLenGame) game).getCurrentState() == TienLenGame.GameState.WAITING_FOR_PLAYER_INPUT && game.getCurrentPlayer() == player) {
                        if (cardView.isSelected()) {
                            selectedCards.remove(card);
                        } else {
                            selectedCards.add(card);
                        }
                        cardView.setSelected(!cardView.isSelected());
                    }
                });
                playerHandBox.getChildren().add(cardView);
            }
        } else if (player.isAI()) {
            // Nothing to do for AI hand display here
        } else {
            // If it's a human player but not their turn, clear hand area
            playerHandBox.getChildren().clear();
            playerHandBox.getChildren().add(new Label("Đây không phải lượt của bạn."));
        }
    }

    @Override
    public void showMessage(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    @Override
    public void updateGameState() {
        Platform.runLater(() -> {
            playedCardsBox.getChildren().clear();
            List<Card> lastPlayed = game.getLastPlayedCards();
            if (lastPlayed != null && !lastPlayed.isEmpty()) {
                for (Card card : lastPlayed) {
                    CardView playedCardView = new CardView(card);
                    playedCardsBox.getChildren().add(playedCardView);
                }
            } else {
                playedCardsBox.getChildren().add(new Label("Không có bài trên bàn."));
            }

            for (Player p : game.getPlayers()) {
                VBox playerPanel = playerPanels.get(p);
                if (playerPanel != null) {
                    Label cardsCountLabel = (Label) playerPanel.getChildren().get(1);
                    cardsCountLabel.setText("Bài: " + p.getHand().size());

                    Label nameLabel = (Label) playerPanel.getChildren().get(0);

                    if (game.getCurrentPlayer() == p) {
                        playerPanel.setStyle("-fx-border-color: blue; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-color: #e0e0ff;");
                        nameLabel.setText(p.getName() + (p.isAI() ? " (AI) - Lượt!" : " (Bạn) - Lượt!"));
                        nameLabel.setTextFill(Color.BLUE);
                    } else {
                        playerPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f8f8f8;");
                        nameLabel.setText(p.getName() + (p.isAI() ? " (AI)" : ""));
                        nameLabel.setTextFill(Color.BLACK);
                    }
                }
            }

            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null && !currentPlayer.isAI()) {
                displayPlayerHand(currentPlayer);
                boolean isHumanTurn = (currentPlayer == game.getCurrentPlayer());
                boolean isWaitingForInputState = (((TienLenGame) game).getCurrentState() == TienLenGame.GameState.WAITING_FOR_PLAYER_INPUT);

                playButton.setDisable(!(isHumanTurn && isWaitingForInputState));
                passButton.setDisable(!(isHumanTurn && isWaitingForInputState && game.canPass(currentPlayer)));
                
                newGameButton.setDisable(!(game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER));

                waitingForInput = (isHumanTurn && isWaitingForInputState);
            } else {
                playButton.setDisable(true);
                passButton.setDisable(true);
                newGameButton.setDisable(!(game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER));
                playerHandBox.getChildren().clear();
                playerHandBox.getChildren().add(new Label("Chờ lượt của bạn..."));
                selectedCards.clear();
                waitingForInput = false;
            }
        });
    }

    private void handlePlayButton() {
        if (waitingForInput) {
            TienLenGame tienLenGame = (TienLenGame) game;
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null && !currentPlayer.isAI() && tienLenGame.isValidPlay(selectedCards)) {
                tienLenGame.setPlayerInput(new ArrayList<>(selectedCards));
                selectedCards.clear();
                waitingForInput = false;
            } else {
                showMessage("Bài của bạn không hợp lệ. Vui lòng chọn lại.");
            }
        }
    }

    private void handlePassButton() {
        if (waitingForInput) {
            TienLenGame tienLenGame = (TienLenGame) game;
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null && !currentPlayer.isAI() && tienLenGame.canPass(currentPlayer)) {
                tienLenGame.setPlayerInput(new ArrayList<>()); // Empty list for pass
                selectedCards.clear();
                waitingForInput = false;
            } else {
                showMessage("Bạn không thể bỏ lượt lúc này!");
            }
        }
    }

    private void handleNewGameButton() {
        // Reset game state
        game.resetGame();
        game.dealCards(); // Chia bài lại
        
        // Reset UI components
        selectedCards.clear();
        playerHandBox.getChildren().clear();
        playedCardsBox.getChildren().clear();
        messageLabel.setText("Chào mừng bạn đến với Tiến Lên Miền Nam!");
        
        // Disable buttons until human player's turn
        playButton.setDisable(true);
        passButton.setDisable(true);
        newGameButton.setDisable(true); // Disable new game button until next game over

        // Update player panels (clear highlights, update card counts)
        updateGameState(); 
        
        // Start game loop again
        game.startGameLoop();
    }

    @Override
    public void onGameOver(List<Player> winners) {
        StringBuilder sb = new StringBuilder("GAME KẾT THÚC!\n");
        if (winners.isEmpty()) {
            sb.append("Không ai thắng (có thể do lỗi).\n");
        } else {
            sb.append("Thứ hạng:\n");
            for (int i = 0; i < winners.size(); i++) {
                Player p = winners.get(i);
                sb.append(p.getWinnerRank()).append(". ").append(p.getName());
                if (p.getWinnerRank() > 0) { // Chỉ hiển thị rank nếu có
                    sb.append(" (Hạng: ").append(p.getWinnerRank()).append(")");
                }
                sb.append("\n");
            }
        }
        
        // Hiển thị dialog chiến thắng
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Game Kết Thúc");
            alert.setHeaderText("Kết quả ván đấu:");
            alert.setContentText(sb.toString());

            // Thêm nút "Chơi lại" và "Thoát"
            ButtonType playAgainButton = new ButtonType("Chơi lại");
            ButtonType exitButton = new ButtonType("Thoát");
            alert.getButtonTypes().setAll(playAgainButton, exitButton);

            // Đặt cửa sổ dialog là cửa sổ con của cửa sổ chính
            alert.initOwner(primaryStage); // primaryStage là Stage của ứng dụng
            alert.initModality(Modality.APPLICATION_MODAL); // Chặn tương tác với cửa sổ chính

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == playAgainButton) {
                handleNewGameButton(); // Gọi lại phương thức xử lý chơi mới
            } else {
                Platform.exit(); // Thoát ứng dụng nếu người dùng chọn Thoát hoặc đóng dialog
            }
        });
        
        // Cần cập nhật trạng thái game và UI sau khi game kết thúc
        updateGameState(); 
    }

    @Override
    public List<Card> getPlayerCardSelection(Player player) {
        return null;
    }

    // CardView class remains the same
    class CardView extends StackPane {
        private Card card;
        private boolean selected;
        private Rectangle backgroundRect;
        private Label cardLabel;

        public CardView(Card card) {
            this.card = card;
            this.selected = false;
            
            backgroundRect = new Rectangle(80, 110);
            backgroundRect.setFill(Color.WHITE);
            backgroundRect.setStroke(Color.BLACK);
            backgroundRect.setArcWidth(10);
            backgroundRect.setArcHeight(10);

            String rankStr;
            if(card.getRank().getValue() == 15) {
                rankStr = "2" + card.suitToString();
            } else if (card.getRank().getValue() == 14) {
                rankStr = "A" + card.suitToString();
            } else if (card.getRank().getValue() == 13) {
                rankStr = "K" + card.suitToString();
            } else if (card.getRank().getValue() == 12) {
                rankStr = "Q" + card.suitToString();
            } else if (card.getRank().getValue() == 11) {
                rankStr = "J" + card.suitToString();
            }
            else {
                rankStr = card.getRank().getValue() + card.suitToString();
            }

            cardLabel = new Label(rankStr);
            Color suitColor = (card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS) ? Color.RED : Color.BLACK;
            cardLabel.setTextFill(suitColor);
            cardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

            getChildren().addAll(backgroundRect, cardLabel);

            updateSelectionBorder();
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            updateSelectionBorder();
        }

        private void updateSelectionBorder() {
            if (selected) {
                backgroundRect.setStroke(Color.BLUE);
                backgroundRect.setStrokeWidth(3);
                backgroundRect.setFill(new Color(0, 0, 1, 0.2));
            } else {
                backgroundRect.setStroke(Color.BLACK);
                backgroundRect.setStrokeWidth(1);
                backgroundRect.setFill(Color.WHITE);
            }
        }
    }
}
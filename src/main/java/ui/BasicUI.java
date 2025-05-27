package ui;

import core.*;
import core.games.tienlen.*;
import core.games.tienlen.tienlenplayer.TienLenPlayer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
 // Required for this.currentScene

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicUI extends CardGameUI<AbstractTienLenGame<? extends TienLenVariantRuleSet>> {
    private SceneManager sceneManager;
    private BorderPane rootLayout;
    private VBox messageBox;
    private Label messageLabel;
    private HBox playedCardsBox;
    private HBox playerHandBox;
    private VBox controlBox;
    private Button passButton;
    private Button playButton;
    private Button newGameButton;
    private Button backToMainMenuButton;

    private List<Card> selectedCards = new ArrayList<>();
    private volatile boolean waitingForInput = false;

    private Map<TienLenPlayer, VBox> playerPanels;

    public BasicUI(AbstractTienLenGame<? extends TienLenVariantRuleSet> game, Stage primaryStage, SceneManager sceneManager) {
        super(game, primaryStage); 

        this.sceneManager = sceneManager;
        this.root = initGUI(); 
        this.currentScene = new Scene(this.root); 

        primaryStage.setScene(this.currentScene); 
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true); 
    }


    @Override
    protected Parent initGUI() {
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
        
        List<TienLenPlayer> players = game.getPlayers();
        
        players.sort((p1, p2) -> {
            if (!p1.isAI() && p2.isAI()) return -1;
            if (p1.isAI() && !p2.isAI()) return 1;
            return p1.getName().compareTo(p2.getName());
        });

        for (TienLenPlayer p : players) {
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
        }
        
        rootLayout.setLeft(allPlayersInfoBox);
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
        newGameButton.setDisable(true);

        backToMainMenuButton = new Button("Về Menu");
        backToMainMenuButton.setMaxWidth(Double.MAX_VALUE);
        backToMainMenuButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        backToMainMenuButton.setOnAction(event -> handleBackToMainMenuButton());
        backToMainMenuButton.setDisable(true);

        controlBox.getChildren().addAll(playButton, passButton, newGameButton, backToMainMenuButton);
        rootLayout.setRight(controlBox);
        return rootLayout;
    }

    @Override
    public void displayPlayerHand(TienLenPlayer player) {
        if (!player.isAI() && game.getCurrentPlayer() == player) {
            playerHandBox.getChildren().clear();
            List<Card> hand = new ArrayList<>(player.getHand());
            hand.sort(game.getRuleSet().getCardComparator());

            for (Card card : hand) {
                CardView cardView = new CardView(card);
                if (selectedCards.contains(card)) {
                    cardView.setSelected(true);
                }

                cardView.setOnMouseClicked(event -> {
                    if (game.getCurrentTienLenState() == TienLenGameState.WAITING_FOR_PLAYER_INPUT && game.getCurrentPlayer() == player) {
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
            
        } else {
            
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

            for (TienLenPlayer p : game.getPlayers()) {
                VBox playerPanel = playerPanels.get(p);
                if (playerPanel != null && playerPanel.getChildren().size() >= 2) { 
                    Label cardsCountLabel = (Label) playerPanel.getChildren().get(1);
                    cardsCountLabel.setText("Bài: " + p.getHand().size());

                    Label nameLabel = (Label) playerPanel.getChildren().get(0);

                    if (game.getGeneralGameState() != Game.GeneralGameState.GAME_OVER && game.getCurrentPlayer() == p) {
                        playerPanel.setStyle("-fx-border-color: blue; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-color: #e0e0ff;");
                        nameLabel.setText(p.getName() + (p.isAI() ? " (AI) - Lượt!" : " (Bạn) - Lượt!"));
                        nameLabel.setTextFill(Color.BLUE);
                    } else if (game.getGeneralGameState() != Game.GeneralGameState.GAME_OVER) { 
                        playerPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f8f8f8;");
                        nameLabel.setText(p.getName() + (p.isAI() ? " (AI)" : ""));
                        nameLabel.setTextFill(Color.BLACK);
                    } else { // Game đã kết thúc, có thể không cần thay đổi style hoặc đặt style mặc định cho tất cả
                         playerPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f8f8f8;");
                         nameLabel.setText(p.getName() + (p.isAI() ? " (AI)" : ""));
                         nameLabel.setTextFill(Color.BLACK);
                    }
                }
            }

            boolean isGameOver = (game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER);
            TienLenPlayer currentPlayer = game.getCurrentPlayer(); 

            if (isGameOver) {
                // GAME ĐÃ KẾT THÚC
                playButton.setDisable(true);
                passButton.setDisable(true);

                playerHandBox.getChildren().clear();
                selectedCards.clear();
                waitingForInput = false;
            } else {
                if (currentPlayer != null && !currentPlayer.isAI()) {
                    // Đến lượt người chơi human
                    displayPlayerHand(currentPlayer);
                    boolean isWaitingForInputState = (game.getCurrentTienLenState() == TienLenGameState.WAITING_FOR_PLAYER_INPUT);

                    playButton.setDisable(!isWaitingForInputState); // Chỉ kích hoạt khi đang chờ input
                    passButton.setDisable(!(isWaitingForInputState && game.canPass(currentPlayer)));
                    waitingForInput = isWaitingForInputState;
                } else {
                    // Lượt của AI hoặc trạng thái khác (ví dụ: game vừa bắt đầu, chưa đến lượt ai)
                    playButton.setDisable(true);
                    passButton.setDisable(true);
                    playerHandBox.getChildren().clear();
                    if (currentPlayer != null && currentPlayer.isAI()) {
                         playerHandBox.getChildren().add(new Label("Đến lượt " + currentPlayer.getName() + " (AI)..."));
                    } else {
                         playerHandBox.getChildren().add(new Label("Chờ lượt..."));
                    }
                    selectedCards.clear();
                    waitingForInput = false;
                }
            }
            newGameButton.setDisable(!isGameOver);
        });
    }

    private void handlePlayButton() {
        if (waitingForInput) {
            TienLenPlayer currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null && !currentPlayer.isAI() && game.isValidPlay(selectedCards)) {
                game.setPlayerInput(new ArrayList<>(selectedCards));
                selectedCards.clear();
                waitingForInput = false;
            } else {
                showMessage("Bài của bạn không hợp lệ. Vui lòng chọn lại.");
            }
        }
    }

    private void handlePassButton() {
        if (waitingForInput) {
            TienLenPlayer currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null && !currentPlayer.isAI() && game.canPass(currentPlayer)) {
                game.setPlayerInput(new ArrayList<>());
                selectedCards.clear();
                waitingForInput = false;
            } else {
                showMessage("Bạn không thể bỏ lượt lúc này!");
            }
        }
    }

    private void handleNewGameButton() {
        selectedCards.clear();
        game.resetGame();        
        playerHandBox.getChildren().clear();
        playedCardsBox.getChildren().clear();
        messageLabel.setText("Chào mừng bạn đến với Tiến Lên Miền Nam!");
        
        playButton.setDisable(true);
        passButton.setDisable(true);
        newGameButton.setDisable(true);

        updateGameState(); 
        game.startGameLoop();
    }

    private void handleBackToMainMenuButton() {
        game.resetGame();
        sceneManager.stopCurrentGame(); 
        sceneManager.showMainMenu();
    }

    @Override
    public void onGameOver(List<TienLenPlayer> winners) {
        StringBuilder sb = new StringBuilder("GAME KẾT THÚC!\n");
        if (winners.isEmpty()) {
            sb.append("Không ai thắng (có thể do lỗi).\n");
        } else {
            sb.append("Thứ hạng:\n");
            for (int i = 0; i < winners.size(); i++) {
                TienLenPlayer p = winners.get(i);
                sb.append(p.getWinnerRank()).append(". ").append(p.getName());
                if (p.getWinnerRank() > 0) {
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

            alert.getButtonTypes().setAll(ButtonType.OK); 

            alert.initOwner(primaryStage); 
            alert.initModality(Modality.APPLICATION_MODAL); 

            alert.showAndWait(); 
            updateGameState(); 
        });
    }

    @Override
    public List<Card> getPlayerCardSelection(TienLenPlayer player) {
        return null;
    }

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
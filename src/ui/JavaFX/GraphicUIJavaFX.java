package ui.JavaFX;

import core.*;
import core.rules.TienLenGame;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphicUIJavaFX extends CardGameGUIJavaFX<TienLenGame> {

    private BorderPane rootLayout;
    private VBox messageBox;
    private Label messageLabel;
    private HBox playedCardsBox;
    private HBox playerHandBox;
    private VBox controlBox;
    private Button passButton;
    private Button playButton;
    private Button newGameButton;

    private List<Card> selectedCards = new ArrayList<>();
    private final Object playerInputLock = new Object();
    private volatile boolean waitingForInput = false;
    private List<Card> playerSelectedInput = null;

    private Map<Player, VBox> playerPanels; // VBox cho AI players

    public GraphicUIJavaFX(TienLenGame game, Stage primaryStage) {
        super(game, primaryStage);
        primaryStage.setMaximized(true); // Đảm bảo cửa sổ được phóng to
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
        BorderPane.setMargin(messageBox, new Insets(0, 0, 20, 0)); // Margin dưới

        // --- AI Players Section (LEFT) ---
        VBox aiPlayersBox = new VBox(15); // VBox cho AI players với spacing 15
        aiPlayersBox.setPadding(new Insets(10));
        aiPlayersBox.setAlignment(Pos.TOP_CENTER); // Căn giữa các AI panel theo chiều ngang trong VBox
        aiPlayersBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-background-color: #f0f0f0;");
        
        // ĐẶT CHIỀU RỘNG ƯU TIÊN VÀ TỐI THIỂU CHO AI PLAYERS BOX
        aiPlayersBox.setPrefWidth(200); 
        aiPlayersBox.setMinWidth(180); // Thêm minWidth để đảm bảo không bị co lại quá nhỏ
        aiPlayersBox.setMaxWidth(250); 
        System.out.println("aiPlayersBox created.");
        
        List<Player> players = game.getPlayers();
        // Bỏ qua người chơi là người (human player) khỏi danh sách AI
        List<Player> aiPlayers = players.stream()
                                       .filter(Player::isAI)
                                       .collect(Collectors.toList());
        System.out.println("Number of AI players found: " + aiPlayers.size());
        
        for (Player p : aiPlayers) {
        	System.out.println("Creating panel for AI: " + p.getName());
            VBox aiPanel = new VBox(5);
            aiPanel.setPadding(new Insets(10));
            aiPanel.setAlignment(Pos.CENTER);
            aiPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f8f8f8;");
            
            // Đặt chiều rộng ưu tiên cho từng AI panel
            aiPanel.setPrefWidth(180); // Ví dụ, nhỏ hơn một chút so với aiPlayersBox
            aiPanel.setMinWidth(150); 
            
            Label aiNameLabel = new Label(p.getName() + " (AI)");
            aiNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            Label aiCardsCountLabel = new Label("Bài: " + p.getHand().size());
            aiPanel.getChildren().addAll(aiNameLabel, aiCardsCountLabel);

            playerPanels.put(p, aiPanel);
            aiPlayersBox.getChildren().add(aiPanel);
            System.out.println("Added AI panel for " + p.getName() + " to aiPlayersBox.");
        }
        rootLayout.setLeft(aiPlayersBox);
        System.out.println("aiPlayersBox set to LEFT.");
        BorderPane.setMargin(aiPlayersBox, new Insets(0, 20, 0, 0)); // Margin phải để tạo khoảng trống

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
        // Margin trên và dưới cho center content
        BorderPane.setMargin(playedCardsContainer, new Insets(0, 0, 20, 0)); 

        // --- Player Hand Panel (BOTTOM) ---
        playerHandBox = new HBox(5);
        playerHandBox.setAlignment(Pos.CENTER);
        playerHandBox.setPadding(new Insets(10));
        playerHandBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f0f0ff;");
        Label playerHandTitle = new Label("Bài của bạn");
        playerHandTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox playerHandContainer = new VBox(5, playerHandTitle, playerHandBox);
        playerHandContainer.setAlignment(Pos.CENTER);
        rootLayout.setBottom(playerHandContainer);

        // --- Control Panel (RIGHT) ---
        controlBox = new VBox(20);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(20, 10, 20, 10));
        controlBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-background-color: #e8e8e8;");

        // ĐẶT CHIỀU RỘNG ƯU TIÊN CHO CONTROL BOX
        controlBox.setPrefWidth(150); // Ví dụ: đặt chiều rộng ưu tiên là 150px
        controlBox.setMaxWidth(180); // Có thể đặt chiều rộng tối đa

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

        controlBox.getChildren().addAll(playButton, passButton, newGameButton);
        rootLayout.setRight(controlBox);
        System.out.println("initGUI finished. RootLayout has " + rootLayout.getChildren().size() + " children.");
        return rootLayout;
    }

    // Các phương thức handlePlayButton, handlePassButton, handleNewGameButton, displayPlayerHand, showMessage, updateGameState, getPlayerCardSelection, CardView class giữ nguyên.
    // Logic trong updateGameState để cập nhật số bài của AI vẫn hoạt động đúng vì nó truy cập playerPanels.
    // LƯU Ý: Phần CardView class đã được cung cấp trong các phản hồi trước, đảm bảo nó vẫn nằm trong GraphicUIJavaFX.java.

    private void handlePlayButton() {
        if (waitingForInput) {
            TienLenGame tienLenGame = (TienLenGame) game;
            if (tienLenGame.isValidPlay(selectedCards)) {
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
            if (tienLenGame.canPass(tienLenGame.getHumanPlayer())) {
                tienLenGame.setPlayerInput(new ArrayList<>()); // Empty list for pass
                selectedCards.clear();
                waitingForInput = false;
            } else {
                showMessage("Bạn không thể bỏ lượt lúc này!");
            }
        }
    }

    private void handleNewGameButton() {
        game.resetGame();
        updateGameState();
    }

    @Override
    public void displayPlayerHand(Player player) {
        playerHandBox.getChildren().clear();
        List<Card> hand = new ArrayList<>(player.getHand());
        hand.sort(((TienLenGame) game).ruleSet.getCardComparator());

        for (Card card : hand) {
            CardView cardView = new CardView(card);
            if (selectedCards.contains(card)) {
                cardView.setSelected(true);
            }

            cardView.setOnMouseClicked(event -> {
                if (((TienLenGame) game).getCurrentState() == TienLenGame.GameState.WAITING_FOR_PLAYER_INPUT) {
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
    }

    @Override
    public void showMessage(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    @Override
    public void updateGameState() {
        Platform.runLater(() -> {
            // Cập nhật bài trên bàn
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

            // Cập nhật bài của người chơi thật
            Player humanPlayer = game.getHumanPlayer();
            if (humanPlayer != null) {
                displayPlayerHand(humanPlayer);

                boolean isHumanTurn = (game.getCurrentPlayer() == humanPlayer);
                boolean isWaitingForInputState = (((TienLenGame) game).getCurrentState() == TienLenGame.GameState.WAITING_FOR_PLAYER_INPUT);

                playButton.setDisable(!(isHumanTurn && isWaitingForInputState));
                passButton.setDisable(!(isHumanTurn && isWaitingForInputState && game.canPass(humanPlayer)));
                newGameButton.setDisable(!(game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER));

                waitingForInput = (isHumanTurn && isWaitingForInputState);
            }

            // Cập nhật bài của các AI và highlight người chơi hiện tại
            for (Player p : game.getPlayers()) {
                if (p.isAI()) {
                    VBox aiPanel = playerPanels.get(p);
                    if (aiPanel != null) {
                        Label aiCardsCountLabel = (Label) aiPanel.getChildren().get(1);
                        aiCardsCountLabel.setText("Bài: " + p.getHand().size());

                        if (game.getCurrentPlayer() == p) {
                            aiPanel.setStyle("-fx-border-color: blue; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-color: #e0e0ff;");
                            Label aiNameLabel = (Label) aiPanel.getChildren().get(0);
                            aiNameLabel.setText(p.getName() + " (AI) - Lượt!");
                            aiNameLabel.setTextFill(Color.BLUE);
                        } else {
                            aiPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f8f8f8;");
                            Label aiNameLabel = (Label) aiPanel.getChildren().get(0);
                            aiNameLabel.setText(p.getName() + " (AI)");
                            aiNameLabel.setTextFill(Color.BLACK);
                        }
                    }
                } else { // Human player panel border
                    VBox playerHandContainer = (VBox) rootLayout.getBottom();
                    Label playerHandTitle = (Label) playerHandContainer.getChildren().get(0);

                    if (game.getCurrentPlayer() == p) {
                        playerHandContainer.setStyle("-fx-border-color: blue; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-color: #e0e0ff;");
                        playerHandTitle.setText(p.getName() + " (Bạn) - Lượt!");
                        playerHandTitle.setTextFill(Color.BLUE);
                    } else {
                        playerHandContainer.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f0f0ff;");
                        playerHandTitle.setText(p.getName() + " (Bạn)");
                        playerHandTitle.setTextFill(Color.BLACK);
                    }
                }
            }
        });
    }

    @Override
    public List<Card> getPlayerCardSelection(Player player) {
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
            if(card.getRank().getValue() == 15) rankStr = (card.getRank().getValue() - 13) + card.suitToString();
            else
                rankStr = card.toString();

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
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

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
    private HBox playerHandBox; // HBox chỉ dành cho các CardView của người chơi human
    private VBox controlBox;
    private Button passButton;
    private Button playButton;
    private Button newGameButton;

    private List<Card> selectedCards = new ArrayList<>();
    // private final Object playerInputLock = new Object(); // Không còn cần thiết lắm cho JavaFX input
    private volatile boolean waitingForInput = false;
    // private List<Card> playerSelectedInput = null; // Không còn cần thiết lắm cho JavaFX input

    private Map<Player, VBox> playerPanels; // VBox cho TẤT CẢ players (AI và Human)

    public GraphicUIJavaFX(TienLenGame game, Stage primaryStage) {
        super(game, primaryStage); // Truyền primaryStage lên lớp cha
        // Setup scene và show stage trong constructor của GUI
        Scene scene = new Scene(initGUI(), 1200, 800); // Kích thước mặc định, sẽ được maximized
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // Phóng to cửa sổ ngay lập tức
    }

    @Override
    protected Parent initGUI() {
        System.out.println("initGUI started.");
        playerPanels = new HashMap<>(); // Khởi tạo map
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
        
        // Sắp xếp người chơi để người chơi human ở trên cùng hoặc cuối cùng trong list UI này nếu muốn
        // Ví dụ: đưa human player lên đầu list để hiển thị trước
        players.sort((p1, p2) -> {
            if (!p1.isAI() && p2.isAI()) return -1; // Human trước AI
            if (p1.isAI() && !p2.isAI()) return 1; // AI sau Human
            return p1.getName().compareTo(p2.getName()); // Sắp xếp theo tên nếu cùng loại
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
                nameLabel = new Label(p.getName()); // Chỉ cần tên, không cần thêm "(Bạn)" ở đây vì nó sẽ hiển thị ở khu vực bài riêng.
            }
            nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            Label cardsCountLabel = new Label("Bài: " + p.getHand().size());
            playerPanel.getChildren().addAll(nameLabel, cardsCountLabel);

            playerPanels.put(p, playerPanel); // Thêm vào map
            allPlayersInfoBox.getChildren().add(playerPanel); // Thêm vào VBox bên trái
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
        // VBox này CHỈ chứa HBox cho bài của người chơi human và title của nó
        VBox humanHandDisplayContainer = new VBox(5); 
        humanHandDisplayContainer.setAlignment(Pos.CENTER);
        humanHandDisplayContainer.setPadding(new Insets(10));
        humanHandDisplayContainer.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f0f0ff;");

        Label humanHandTitle = new Label("Bài của bạn"); // Tiêu đề chung cho khu vực bài của human player
        humanHandTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        playerHandBox = new HBox(5); // HBox thực tế chứa các CardView
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

        controlBox.getChildren().addAll(playButton, passButton, newGameButton);
        rootLayout.setRight(controlBox);
        System.out.println("initGUI finished. RootLayout has " + rootLayout.getChildren().size() + " children.");
        return rootLayout;
    }

    @Override
    public void displayPlayerHand(Player player) {
        // Phương thức này bây giờ sẽ được gọi cho BẤT KỲ người chơi human nào đang đến lượt.
        // Chỉ hiển thị bài của người chơi hiện tại nếu họ không phải AI.
        // Điều kiện game.getCurrentPlayer() == player là quan trọng để không hiển thị bài của người khác.
        if (!player.isAI() && game.getCurrentPlayer() == player) {
            playerHandBox.getChildren().clear(); // Clear HBox for cards
            List<Card> hand = new ArrayList<>(player.getHand());
            hand.sort(((TienLenGame) game).ruleSet.getCardComparator());

            for (Card card : hand) {
                CardView cardView = new CardView(card);
                if (selectedCards.contains(card)) {
                    cardView.setSelected(true);
                }

                cardView.setOnMouseClicked(event -> {
                    // Kiểm tra nếu đây thực sự là lượt của người chơi human này
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
            // Không làm gì, bài AI chỉ hiện số lượng ở panel bên trái
        } else {
            // Nếu là human player nhưng KHÔNG phải lượt của họ, clear khu vực bài
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

            // Cập nhật thông tin và highlight cho TẤT CẢ người chơi (human và AI)
            for (Player p : game.getPlayers()) {
                VBox playerPanel = playerPanels.get(p);
                if (playerPanel != null) {
                    Label cardsCountLabel = (Label) playerPanel.getChildren().get(1); // Label thứ 2 là số bài
                    cardsCountLabel.setText("Bài: " + p.getHand().size());

                    Label nameLabel = (Label) playerPanel.getChildren().get(0);

                    if (game.getCurrentPlayer() == p) {
                        playerPanel.setStyle("-fx-border-color: blue; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-color: #e0e0ff;");
                        nameLabel.setText(p.getName() + (p.isAI() ? " (AI) - Lượt!" : " (Bạn) - Lượt!")); // Hiển thị "(Bạn)" ở đây cho người chơi hiện tại
                        nameLabel.setTextFill(Color.BLUE);
                    } else {
                        playerPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f8f8f8;");
                        nameLabel.setText(p.getName() + (p.isAI() ? " (AI)" : "")); // Không thêm "(Bạn)" nếu không phải lượt, để gọn tên
                        nameLabel.setTextFill(Color.BLACK);
                    }
                }
            }

            // Cập nhật bài của người chơi human hiện tại (nếu có)
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null && !currentPlayer.isAI()) {
                displayPlayerHand(currentPlayer); // Gọi displayPlayerHand với người chơi hiện tại
                // Điều khiển nút bấm dựa trên trạng thái của người chơi human hiện tại
                boolean isHumanTurn = (currentPlayer == game.getCurrentPlayer()); // Luôn là true ở đây
                boolean isWaitingForInputState = (((TienLenGame) game).getCurrentState() == TienLenGame.GameState.WAITING_FOR_PLAYER_INPUT);

                playButton.setDisable(!(isHumanTurn && isWaitingForInputState));
                passButton.setDisable(!(isHumanTurn && isWaitingForInputState && game.canPass(currentPlayer)));
                
                // Nút New Game chỉ enable khi game kết thúc
                newGameButton.setDisable(!(game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER));

                waitingForInput = (isHumanTurn && isWaitingForInputState);
            } else {
                // Nếu không phải lượt của người chơi human, disable các nút và clear khu vực bài
                playButton.setDisable(true);
                passButton.setDisable(true);
                 newGameButton.setDisable(!(game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER));
                playerHandBox.getChildren().clear();
                playerHandBox.getChildren().add(new Label("Chờ lượt của bạn...")); // Thông báo cho người chơi chờ
                selectedCards.clear(); // Xóa lựa chọn nếu không phải lượt của mình
                waitingForInput = false;
            }
        });
    }

    private void handlePlayButton() {
        if (waitingForInput) {
            TienLenGame tienLenGame = (TienLenGame) game;
            // Đảm bảo chỉ người chơi hiện tại mới được đánh bài
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
            // Đảm bảo chỉ người chơi hiện tại mới được bỏ lượt
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
        game.resetGame();
        // Cần khởi tạo lại game để chia bài mới
        // Hoặc bạn có thể thêm logic reset trong TienLenGame để nó tự reset bài và trạng thái
        // Ví dụ: game.resetGame(); // Nếu bạn có phương thức này trong Game/TienLenGame
        // Để đơn giản, nếu bạn chỉ muốn chơi lại, bạn có thể khởi tạo lại game và GUI ở Main
        // hoặc thêm logic reset chi tiết trong TienLenGame
        
        // Hiện tại, nếu game.resetGame() chỉ reset trạng thái game mà chưa chia bài mới:
        // Bạn cần gọi game.dealCards() sau khi reset
        game.dealCards(); // Chia bài lại
        updateGameState(); // Cập nhật GUI cho ván mới
        showMessage("Game mới đã bắt đầu!");
    }

    @Override
    public List<Card> getPlayerCardSelection(Player player) {
        // This method is likely for console UI or specific input logic,
        // for JavaFX the selection is handled via mouse clicks on CardView.
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
            // Xử lý hiển thị giá trị 15 (Quân 2) đúng trong Tiến Lên
            if(card.getRank().getValue() == 15) { // Rank 15 là quân 2
                rankStr = "2" + card.suitToString(); // Hiển thị "2" + chất
            } else if (card.getRank().getValue() == 14) { // Rank 14 là quân A
                rankStr = "A" + card.suitToString();
            } else if (card.getRank().getValue() == 13) { // Rank 13 là quân K
                rankStr = "K" + card.suitToString();
            } else if (card.getRank().getValue() == 12) { // Rank 12 là quân Q
                rankStr = "Q" + card.suitToString();
            } else if (card.getRank().getValue() == 11) { // Rank 11 là quân J
                rankStr = "J" + card.suitToString();
            }
            else {
                rankStr = card.getRank().getValue() + card.suitToString(); // Các quân bài số
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
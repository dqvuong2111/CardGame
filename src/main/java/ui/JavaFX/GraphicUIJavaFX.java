package ui.JavaFX;

import core.*;
import core.games.tienlen.*;
import core.games.tienlen.tienlenmiennam.TienLenMienNamGame;
import core.games.tienlen.tienlenplayer.TienLenPlayer;
import core.games.tienlen.AbstractTienLenGame; // << THÊM IMPORT NÀY
import core.games.tienlen.TienLenVariantRuleSet;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.JavaFX.gamescreencomponents.CardView;
import javafx.scene.Parent; // Required for this.root
import javafx.scene.Scene;  // Required for this.currentScene

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GraphicUIJavaFX extends CardGameGUIJavaFX<AbstractTienLenGame<? extends TienLenVariantRuleSet>> {

    private BorderPane rootLayout;
    private VBox messageBox;
    private Label messageLabel;
    private HBox playedCardsBox;
    private HBox playerHandBox;
    private VBox controlBox;
    private Button passButton;
    private Button playButton;
    private Button newGameButton;
    private Button backToMainMenuButton; // Khai báo nút mới
    private SceneManager sceneManager;   // Tham chiếu đến SceneManager

    private List<Card> selectedCards = new ArrayList<>();
    private volatile boolean waitingForInput = false;

    private Map<TienLenPlayer, VBox> playerPanels;

    public GraphicUIJavaFX(AbstractTienLenGame<? extends TienLenVariantRuleSet> game, Stage primaryStage, SceneManager sceneManager) {
        super(game, primaryStage);
        this.sceneManager = sceneManager;
        this.root = initGUI();
        if (this.root == null) {
            System.err.println("Lỗi: initGUI() trả về root là null trong GraphicUIJavaFX constructor.");
            Platform.exit();
            return;
        }
        this.currentScene = new Scene(this.root);
        primaryStage.setScene(this.currentScene);

        // Constructor của CardGameGUIJavaFX đã gọi game.addGameEventListener(this);
    }


    @Override
    protected Parent initGUI() {
        System.out.println("initGUI started.");
        playerPanels = new HashMap<>();
        rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(20));
        
        String imagePathForGameScreen = "/background/mm3.jpg"; // << THAY BẰNG ĐƯỜNG DẪN CỦA BẠN
        try {
            // Sử dụng getClass().getResource() thay vì CardView.class.getResource()
            // nếu CardView là inner class hoặc bạn muốn đường dẫn rõ ràng từ gốc classpath
            String imageUrl = getClass().getResource(imagePathForGameScreen).toExternalForm();
            if (imageUrl != null) {
                rootLayout.setStyle(
                    "-fx-background-image: url('" + imageUrl + "'); " +
                    "-fx-background-repeat: no-repeat; " +
                    "-fx-background-position: center center; " +
                    "-fx-background-size: cover;" // Phủ kín toàn bộ Pane
                );
            } else {
                System.err.println("Lỗi: Không tìm thấy file ảnh nền cho GameScreen: " + imagePathForGameScreen);
                rootLayout.setStyle("-fx-background-color: #333333;"); // Màu nền tối dự phòng
            }
        } catch (Exception e) {
            System.err.println("Ngoại lệ khi lấy URL ảnh nền GameScreen: " + imagePathForGameScreen + ". " + e.getMessage());
            rootLayout.setStyle("-fx-background-color: #333333;"); // Màu nền tối dự phòng
        }
        

        // --- Message Panel (TOP) ---
        messageBox = new VBox();
        messageLabel = new Label("Chào mừng bạn đến với Tiến Lên Miền Nam!");
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        messageLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        
        messageBox.setAlignment(Pos.CENTER);
        messageBox.getChildren().add(messageLabel);
        messageBox.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0);" + // Nền trắng mờ 70% đục
                "-fx-background-radius: 10;"
            );
        rootLayout.setTop(messageBox);
        BorderPane.setMargin(messageBox, new Insets(0, 0, 20, 0));

        // --- Players Section (LEFT) ---
        VBox allPlayersInfoBox = new VBox(15);
        allPlayersInfoBox.setPadding(new Insets(10));
        allPlayersInfoBox.setAlignment(Pos.TOP_CENTER);
        allPlayersInfoBox.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0);" + // Nền trắng mờ 70% đục
                "-fx-background-radius: 10;"
            );
        allPlayersInfoBox.setPrefWidth(200);
        allPlayersInfoBox.setMinWidth(180);
        allPlayersInfoBox.setMaxWidth(250);
        System.out.println("allPlayersInfoBox created.");
        
        List<TienLenPlayer> players = game.getPlayers();
        System.out.println("Number of players found: " + players.size());
        
        players.sort((p1, p2) -> {
            if (!p1.isAI() && p2.isAI()) return -1;
            if (p1.isAI() && !p2.isAI()) return 1;
            return p1.getName().compareTo(p2.getName());
        });

        for (TienLenPlayer p : players) {
            VBox playerPanel = new VBox(5);
            playerPanel.setPadding(new Insets(10));
            playerPanel.setAlignment(Pos.CENTER);
            playerPanel.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 1);" + // Nền trắng mờ 70% đục
                            "-fx-background-radius: 10;"
                        );
            playerPanel.setPrefWidth(180);
            playerPanel.setMinWidth(150); 
            
            Label nameLabel;
            if (p.isAI()) {
                nameLabel = new Label(p.getName() + " (AI)");
            } else {
                nameLabel = new Label(p.getName());
            }
            nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            nameLabel.setTextFill(javafx.scene.paint.Color.WHITE);
            
            Label cardsCountLabel = new Label("Bài: " + p.getHand().size());
            cardsCountLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13)); // Giữ lại hoặc tùy chỉnh font nếu muốn
            cardsCountLabel.setTextFill(Color.LIGHTGOLDENRODYELLOW);
            
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
        playedCardsContainer.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0);" + // Nền trắng mờ 70% đục
                "-fx-background-radius: 10;"
            );
        Label playedCardsTitle = new Label("Bài trên bàn");
        playedCardsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playedCardsTitle.setTextFill(javafx.scene.paint.Color.WHITE); // <--- THÊM DÒNG NÀY
        
        playedCardsBox = new HBox(5);
        playedCardsBox.setAlignment(Pos.CENTER);
        playedCardsBox.setPadding(new Insets(10));
        playedCardsBox.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0);" + // Nền trắng mờ 70% đục
                "-fx-background-radius: 10;"
            );
        playedCardsContainer.getChildren().addAll(playedCardsTitle, playedCardsBox);
        rootLayout.setCenter(playedCardsContainer);
        BorderPane.setMargin(playedCardsContainer, new Insets(0, 0, 20, 0)); 

        // --- Player Hand Panel (BOTTOM) ---
        VBox humanHandDisplayContainer = new VBox(5); 
        humanHandDisplayContainer.setAlignment(Pos.CENTER);
        humanHandDisplayContainer.setPadding(new Insets(10));
        humanHandDisplayContainer.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0);" + // Nền trắng mờ 70% đục
                "-fx-background-radius: 10;"
            );
        Label humanHandTitle = new Label("Bài của bạn");
        humanHandTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        humanHandTitle.setTextFill(javafx.scene.paint.Color.WHITE);
        
        playerHandBox = new HBox(5);
        playerHandBox.setAlignment(Pos.CENTER);
        playerHandBox.setPadding(new Insets(10, 0, 0, 0));

        humanHandDisplayContainer.getChildren().addAll(humanHandTitle, playerHandBox);
        rootLayout.setBottom(humanHandDisplayContainer);

        // --- Control Panel (RIGHT) ---
        controlBox = new VBox(20);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(20, 10, 20, 10));
        controlBox.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0);" + // Nền trắng mờ 70% đục
                "-fx-background-radius: 10;"
            );
        controlBox.setPrefWidth(150);
        controlBox.setMaxWidth(180);

        playButton = new Button("Đánh bài"); 
        styleGameButtonOnGreenBackground(playButton, true); // Nút chính
        playButton.setOnAction(event -> handlePlayButton());
        playButton.setOnAction(event -> handlePlayButton());

        passButton = new Button("Bỏ lượt"); 
        styleGameButtonOnGreenBackground(passButton, false);
        passButton.setOnAction(event -> handlePassButton());

        newGameButton = new Button("Ván mới"); 
        styleGameButtonOnGreenBackground(newGameButton, false);
        newGameButton.setOnAction(event -> handleNewGameButton());
        newGameButton.setDisable(true);
        
        backToMainMenuButton = new Button("Menu Chính"); 
        styleGameButtonOnGreenBackground(backToMainMenuButton, false);
        backToMainMenuButton.setOnAction(event -> handleNewGameButton());
        backToMainMenuButton.setDisable(true);
        backToMainMenuButton.setOnAction(event -> {
            if (this.sceneManager != null) {
                // Bạn có thể muốn dừng game hiện tại hoàn toàn ở đây nếu cần,
                // ví dụ: this.game.stopGameLoop();
                // Hoặc để SceneManager.stopCurrentGame() xử lý khi showMainMenu().
                this.sceneManager.showMainMenu();
            } else {
                System.err.println("Lỗi: SceneManager là null trong GraphicUIJavaFX.");
                // Có thể hiển thị Alert lỗi
            }
        });
        backToMainMenuButton.setDisable(true); // Ban đầu vô hiệu hóa

        controlBox.getChildren().addAll(playButton, passButton, newGameButton, backToMainMenuButton);
        rootLayout.setRight(controlBox);
        rootLayout.setRight(controlBox);
        System.out.println("initGUI finished. RootLayout has " + rootLayout.getChildren().size() + " children.");
        return rootLayout;
    }

    @Override
    public void displayPlayerHand(TienLenPlayer player) {
        // Sử dụng game.getRuleSet() thay vì ép kiểu
        if (!player.isAI() && game.getCurrentPlayer() == player) {
            playerHandBox.getChildren().clear();
            List<Card> hand = new ArrayList<>(player.getHand());
            // Sắp xếp bài bằng comparator từ ruleSet của game hiện tại
            if (game != null && game.getRuleSet() != null && game.getRuleSet().getCardComparator() != null) {
                hand.sort(game.getRuleSet().getCardComparator());
            } else {
                Collections.sort(hand); // Fallback nếu không có comparator
            }

            for (Card card : hand) {
                CardView cardView = new CardView(card);
                if (selectedCards.contains(card)) {
                    cardView.setSelected(true);
                }

                cardView.setOnMouseClicked(event -> {
                    // Sử dụng game.getCurrentTienLenState() vì game là TienLenGameContext
                    if (game.getCurrentTienLenState() == TienLenGameState.WAITING_FOR_PLAYER_INPUT && game.getCurrentPlayer() == player) {
                        if (cardView.isSelected()) {
                            selectedCards.remove(cardView.getCard()); // Lấy card từ cardView
                        } else {
                            selectedCards.add(cardView.getCard()); // Lấy card từ cardView
                        }
                        cardView.setSelected(!cardView.isSelected());
                    }
                });
                playerHandBox.getChildren().add(cardView);
            }
        } else if (playerHandBox != null) { // Thêm kiểm tra null cho playerHandBox
            playerHandBox.getChildren().clear();
            if (player.isAI() && game.getCurrentPlayer() == player) {
                // Không hiển thị bài AI, có thể hiển thị thông báo "AI đang đánh" ở messageLabel
            } else if (!player.isAI()) { // Người chơi human nhưng không phải lượt
                Label notYourTurnLabel = new Label("Đây không phải lượt của bạn.");
                notYourTurnLabel.setTextFill(Color.WHITE);
                // addDefaultTextEffect(notYourTurnLabel);
                playerHandBox.getChildren().add(notYourTurnLabel);
            }
        }
    }

    @Override
    public void showMessage(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    @Override
    public void updateGameState() {
        Platform.runLater(() -> {
            if (game == null) return; // Kiểm tra game null

            // Cập nhật playedCardsBox
            playedCardsBox.getChildren().clear();
            List<Card> lastPlayed = game.getLastPlayedCards(); // game là AbstractTienLenGame, có phương thức này
            if (lastPlayed != null && !lastPlayed.isEmpty()) {
                for (Card card : lastPlayed) {
                    CardView playedCardView = new CardView(card);
                    playedCardsBox.getChildren().add(playedCardView);
                }
            } else {
            	Label noCardsLabel = new Label("Không có bài trên bàn.");
                noCardsLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                noCardsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                // addDefaultTextEffect(noCardsLabel);
                playedCardsBox.getChildren().add(noCardsLabel);
            }

            // Cập nhật playerPanels
            for (TienLenPlayer p : game.getPlayers()) {
                VBox playerPanel = playerPanels.get(p);
                if (playerPanel != null && playerPanel.getChildren().size() >= 2) {
                    Label cardsCountLabel = (Label) playerPanel.getChildren().get(1);
                    cardsCountLabel.setText("Bài: " + p.getHand().size());
                    cardsCountLabel.setTextFill(Color.LIGHTGOLDENRODYELLOW); // Đã sửa ở lần trước
                    // Thêm hiệu ứng cho cardsCountLabel nếu cần (ví dụ, dựa trên styleSettingsLabel)
                    // addDefaultTextEffect(cardsCountLabel);


                    Label nameLabel = (Label) playerPanel.getChildren().get(0);
                    
                    boolean isCurrent = game.getCurrentPlayer() == p;
                    boolean isGameOver = game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER;

                    if (!isGameOver && isCurrent) {
                    	playerPanel.setStyle(
                                "-fx-border-color: #FFFACD; " + 
                                "-fx-border-width: 3.5; " +     
                                "-fx-border-radius: 10; " +    
                                "-fx-background-radius: 10;" +
                                "-fx-background-color: rgba(0, 100, 0, 0.7);" 
                            );
                        nameLabel.setText(p.getName() + (p.isAI() ? " (AI) - Lượt!" : " (Bạn) - Lượt!"));
                        nameLabel.setTextFill(Color.WHITE); // << ĐỔI THÀNH MÀU BẠN MUỐN CHO NGƯỜI CHƠI CÓ LƯỢT
                                                            // Ví dụ: Color.LIGHTGREEN hoặc Color.YELLOW
                        // addDefaultTextEffect(nameLabel);


                        // cardsCountLabel đã được set màu ở trên, có thể giữ nguyên
                    } else { // Người chơi không có lượt hoặc game đã kết thúc
                        if (p.isAI()) {
                             playerPanel.setStyle(
                                "-fx-border-color: rgba(100, 150, 100, 0.5);" +
                                "-fx-border-width: 1.5; " +
                                "-fx-border-radius: 8; " +
                                "-fx-background-radius: 8;" +
                                "-fx-background-color: rgba(0, 50, 0, 0.5);"
                            );
                            nameLabel.setText(p.getName() + " (AI)");
                            nameLabel.setTextFill(Color.LIGHTGRAY); // Chữ xám nhạt cho AI không có lượt
                        } else { // Người thật không có lượt
                             playerPanel.setStyle(
                               "-fx-border-color: rgba(220, 220, 220, 0.3);" +
                               "-fx-border-width: 1; " +
                               "-fx-border-radius: 8; " +
                               "-fx-background-radius: 8;" +
                               "-fx-background-color: rgba(255, 255, 255, 0.15);" // Nền trắng rất mờ
                           );
                           nameLabel.setText(p.getName());
                           nameLabel.setTextFill(Color.rgb(200,200,200)); // Chữ xám nhạt hơn
                        }
                        nameLabel.setEffect(null); // Bỏ hiệu ứng nếu có
                    }
                }
            }

            // Xử lý trạng thái nút và hiển thị tay bài của human
            boolean isGameOver = (game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER);
            TienLenPlayer currentPlayer = game.getCurrentPlayer();

            if (isGameOver) {
                playButton.setDisable(true);
                passButton.setDisable(true);
                if (playerHandBox != null) playerHandBox.getChildren().clear(); // Kiểm tra null
                selectedCards.clear();
                waitingForInput = false;
            } else {
                if (currentPlayer != null && !currentPlayer.isAI()) {
                    displayPlayerHand(currentPlayer); // Cập nhật tay bài human
                    // Sử dụng game.getCurrentTienLenState() vì game là TienLenGameContext
                    boolean isWaitingForInputState = (game.getCurrentTienLenState() == TienLenGameState.WAITING_FOR_PLAYER_INPUT);
                    playButton.setDisable(!isWaitingForInputState);
                    passButton.setDisable(!(isWaitingForInputState && game.canPass(currentPlayer)));
                    waitingForInput = isWaitingForInputState;
                } else { // Lượt AI hoặc trạng thái khác
                    playButton.setDisable(true);
                    passButton.setDisable(true);
                    if (playerHandBox != null) playerHandBox.getChildren().clear(); // Kiểm tra null

                    if (currentPlayer != null && currentPlayer.isAI()) {
                    	Label aiTurnLabel = new Label("Đến lượt " + currentPlayer.getName() + " (AI)...");
                        aiTurnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                        aiTurnLabel.setTextFill(Color.WHITE);
                        // addDefaultTextEffect(aiTurnLabel);
                        playerHandBox.getChildren().add(aiTurnLabel);
                    } else if (currentPlayer == null || (currentPlayer != null && !currentPlayer.getHand().isEmpty())) { 
                        // Thêm điều kiện currentPlayer.getHand().isEmpty() để không hiện "Chờ lượt" nếu human đã hết bài
                        Label waitingLabel = new Label("Chờ lượt...");
                        waitingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16)); // Chữ nghiêng
                        waitingLabel.setTextFill(Color.WHITE);
                        // addDefaultTextEffect(waitingLabel);
                        playerHandBox.getChildren().add(waitingLabel);
                    }
                    selectedCards.clear();
                    waitingForInput = false;
                }
            }
            newGameButton.setDisable(!isGameOver);
            if(backToMainMenuButton != null) backToMainMenuButton.setDisable(!isGameOver);
        });
    }

    private void handlePlayButton() {
        if (waitingForInput) {
            // TienLenMienNamGame tienLenGame = (TienLenMienNamGame) game; // << BỎ ÉP KIỂU NÀY
            TienLenPlayer currentPlayer = game.getCurrentPlayer();
            // Sử dụng phương thức isValidPlay chung từ lớp Game hoặc AbstractTienLenGame
            // Hoặc tốt hơn là game engine tự validate khi nhận setPlayerInput
            if (currentPlayer != null && !currentPlayer.isAI()) {
                 // game.setPlayerInput() sẽ được gọi. Logic isValidPlay nên nằm trong game engine khi xử lý input.
                 // Giả sử game engine sẽ tự kiểm tra tính hợp lệ của selectedCards khi setPlayerInput.
                 // Hoặc, nếu bạn muốn UI kiểm tra sơ bộ (không khuyến khích vì lặp lại logic):
                 // if (game.getRuleSet().isValidCombination(selectedCards) && 
                 //    (game.getLastPlayedCards().isEmpty() || game.getRuleSet().canPlayAfter(selectedCards, game.getLastPlayedCards()))) {
                
                // Tạm thời giả định game.setPlayerInput sẽ xử lý và có thể game sẽ gửi lại message nếu không hợp lệ
                game.setPlayerInput(new ArrayList<>(selectedCards));
                // selectedCards.clear(); // Nên clear sau khi game xác nhận nước đi thành công
                // waitingForInput = false;
                // } else {
                //     showMessage("Bài của bạn không hợp lệ. Vui lòng chọn lại.");
                // }
            }
        }
    }


    private void handlePassButton() {
        if (waitingForInput) {
            // TienLenMienNamGame tienLenGame = (TienLenMienNamGame) game; // << BỎ ÉP KIỂU NÀY
            TienLenPlayer currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null && !currentPlayer.isAI() && game.canPass(currentPlayer)) {
                game.setPlayerInput(new ArrayList<>()); // Gửi danh sách rỗng cho hành động bỏ lượt
                // selectedCards.clear(); // Không cần thiết nếu bỏ lượt
                // waitingForInput = false;
            } else {
                showMessage("Bạn không thể bỏ lượt lúc này!");
            }
        }
    }


    private void handleNewGameButton() {
        // Reset game state
        game.resetGame();
        game.dealCards();
        
        // Reset UI components
        selectedCards.clear();
        playerHandBox.getChildren().clear();
        playedCardsBox.getChildren().clear();
        messageLabel.setText("Chào mừng bạn đến với Tiến Lên Miền Nam!");
        
        // Disable buttons until human player's turn
        playButton.setDisable(true);
        passButton.setDisable(true);
        newGameButton.setDisable(true);

        // Update player panels (clear highlights, update card counts)
        updateGameState(); 
        
        // Start game loop again
        game.startGameLoop();
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
        	Alert alert = new Alert(AlertType.INFORMATION); //
            alert.setTitle("Game Kết Thúc"); //
            alert.setHeaderText("Kết quả ván đấu:"); //
            alert.setContentText(sb.toString()); //

            // Chỉ cần nút OK (hoặc nút xác nhận tương ứng với ngôn ngữ)
            // ButtonType confirmButton = new ButtonType("OK"); // Hoặc ButtonType.OK
            alert.getButtonTypes().setAll(ButtonType.OK); // (Sửa để chỉ có nút OK)

            alert.initOwner(primaryStage); //
            alert.initModality(Modality.APPLICATION_MODAL); //

            alert.showAndWait(); 
         // Cần cập nhật trạng thái game và UI sau khi game kết thúc
            updateGameState(); 
        });
    }

    @Override
    public List<Card> getPlayerCardSelection(TienLenPlayer player) {
        return null;
    }

    
    private void styleGameButtonOnGreenBackground(Button button, boolean isPrimaryAction) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        button.setPrefHeight(40);

        String baseColor;
        String hoverColor;
        String textColor = "#004D40"; // Xanh lá cây rất đậm cho chữ

        if (isPrimaryAction) { // Ví dụ: nút "Đánh bài"
            baseColor = "#FFFFFF";    // Nền trắng
            hoverColor = "#F5F5F5";   // Trắng xám khi hover
        } else { // Các nút phụ
            baseColor = "#E8F5E9";    // Nền xanh lá rất nhạt (gần như trắng)
            hoverColor = "#C8E6C9";   // Xanh lá nhạt hơn khi hover
        }

        String baseStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-border-color: %s; " + // Viền cùng màu chữ hoặc màu xanh lá đậm hơn
            "-fx-border-width: 1.5px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0.0, 0, 2);",
            baseColor, textColor, textColor
        );
        String hoverStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1.5px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 7, 0.0, 0, 3);",
            hoverColor, textColor, textColor
        );

        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setCursor(javafx.scene.Cursor.HAND);
    }
}
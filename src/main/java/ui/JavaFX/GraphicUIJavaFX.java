// File: ui/JavaFX/GraphicUIJavaFX.java
package ui.JavaFX;

import core.Card;
import core.Game;
import core.games.tienlen.AbstractTienLenGame;
import core.games.tienlen.TienLenGameState;
import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.tienlenplayer.TienLenPlayer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label; // Giữ lại nếu dùng cho addDefaultTextEffect
import javafx.scene.effect.DropShadow; // Giữ lại nếu dùng cho addDefaultTextEffect
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color; // Giữ lại nếu dùng cho addDefaultTextEffect
import javafx.scene.text.Font; // Giữ lại nếu dùng cho addDefaultTextEffect
import javafx.scene.text.FontWeight; // Giữ lại nếu dùng cho addDefaultTextEffect
import javafx.stage.Modality;
import javafx.stage.Stage;

// Import các component bạn đã tạo từ package gamescreencomponents
import ui.JavaFX.gamescreencomponents.CardView;
import ui.JavaFX.gamescreencomponents.GameControlsComponent;
import ui.JavaFX.gamescreencomponents.GameMessageComponent;
import ui.JavaFX.gamescreencomponents.HumanHandComponent;
// PlayerInfoComponent có thể không cần import trực tiếp ở đây nếu PlayersListComponent tự quản lý
import ui.JavaFX.gamescreencomponents.PlayersListComponent;
import ui.JavaFX.gamescreencomponents.TableCardsComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GraphicUIJavaFX extends CardGameGUIJavaFX<AbstractTienLenGame<? extends TienLenVariantRuleSet>> {

    private BorderPane rootLayout;

    // Khai báo các biến cho từng component UI
    private GameMessageComponent gameMessageComponent;
    private PlayersListComponent playersListComponent;
    private TableCardsComponent tableCardsComponent;
    private HumanHandComponent humanHandComponent;
    private GameControlsComponent gameControlsComponent;

    private SceneManager sceneManager;

    private List<Card> selectedCards = new ArrayList<>();
    private volatile boolean waitingForInput = false;

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
        // game.addGameEventListener(this); đã được gọi trong super constructor
    }

    @Override
    protected Parent initGUI() {
        System.out.println("GraphicUIJavaFX: initGUI (phiên bản component hóa) bắt đầu.");
        rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(5, 10, 5, 10));

        String imagePathForGameScreen = "/background/mm3.jpg";
        try {
            String imageUrl = getClass().getResource(imagePathForGameScreen).toExternalForm();
            if (imageUrl != null) {
                rootLayout.setStyle(
                    "-fx-background-image: url('" + imageUrl + "'); " +
                    "-fx-background-repeat: no-repeat; " +
                    "-fx-background-position: center center; " +
                    "-fx-background-size: cover;"
                );
            } else {
                System.err.println("Lỗi: Không tìm thấy ảnh nền GameScreen: " + imagePathForGameScreen);
                rootLayout.setStyle("-fx-background-color: #2c3e50;"); // Màu nền tối dự phòng
            }
        } catch (Exception e) {
            System.err.println("Ngoại lệ khi tải ảnh nền GameScreen: " + imagePathForGameScreen + ". " + e.getMessage());
            rootLayout.setStyle("-fx-background-color: #2c3e50;");
        }

        // --- Khởi tạo các Component View ---
        gameMessageComponent = new GameMessageComponent("Chào mừng đến với Tiến Lên!"); //

        if (game != null && game.getPlayers() != null) {
            // Truyền game.getPlayers() vì constructor PlayersListComponent nhận List<TienLenPlayer>
            playersListComponent = new PlayersListComponent(game.getPlayers()); //
        } else {
            playersListComponent = new PlayersListComponent(new ArrayList<>());
        }
        
        tableCardsComponent = new TableCardsComponent(); //
        
        humanHandComponent = new HumanHandComponent(
                mouseEvent -> {
                    Node sourceNode = (Node) mouseEvent.getSource();
                    if (sourceNode instanceof CardView) {
                        CardView clickedCardView = (CardView) sourceNode;
                        Card card = clickedCardView.getCard();

                        TienLenPlayer currentPlayer = null;
                        if (game != null) {
                            currentPlayer = game.getCurrentPlayer();
                        }

                        // SỬA ĐỔI Ở ĐÂY:
                        // Cho phép chọn bài nếu người chơi hiện tại là Human (không phải AI)
                        // và game đang ở trạng thái chờ input của người chơi đó.
                        if (currentPlayer != null && !currentPlayer.isAI() &&
                            game.getCurrentTienLenState() == TienLenGameState.WAITING_FOR_PLAYER_INPUT) {

                            if (clickedCardView.isSelected()) {
                                selectedCards.remove(card);
                                clickedCardView.setSelected(false);
                            } else {
                                selectedCards.add(card);
                                clickedCardView.setSelected(true);
                            }

                            if (gameControlsComponent != null) {
                                boolean isGameOverForButton = (game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER);
                                gameControlsComponent.updateButtonStates(
                                    !selectedCards.isEmpty(),
                                    game.canPass(currentPlayer), // Sử dụng currentPlayer ở đây
                                    isGameOverForButton
                                );
                            }
                        } else {
                            // Có thể thêm thông báo ở đây nếu muốn, ví dụ:
                            // if (currentPlayer != null && currentPlayer.isAI()) {
                            //     System.out.println("Không thể chọn bài, đang là lượt của AI.");
                            // } else if (game.getCurrentTienLenState() != TienLenGameState.WAITING_FOR_PLAYER_INPUT) {
                            //     System.out.println("Không thể chọn bài, game không ở trạng thái chờ input.");
                            // } else if (currentPlayer == null) {
                            //     System.out.println("Không thể chọn bài, không có người chơi hiện tại.");
                            // }
                        }
                    }
                }
            );

        
        gameControlsComponent = new GameControlsComponent( //
            this::handlePlayButton,   
            this::handlePassButton,    
            this::handleNewGameButton, 
            event -> {                 
                if (this.sceneManager != null) {
                    this.sceneManager.stopCurrentGame(); 
                    this.sceneManager.showMainMenu();
                } else {
                    System.err.println("Lỗi: SceneManager là null trong GraphicUIJavaFX (backToMainMenuButton).");
                }
            }
        );

        // --- Sắp xếp các Component vào rootLayout ---
        rootLayout.setTop(gameMessageComponent); //
        BorderPane.setMargin(gameMessageComponent, new Insets(5, 0, 10, 0));

        rootLayout.setLeft(playersListComponent); //
        BorderPane.setMargin(playersListComponent, new Insets(0, 10, 0, 0));

        rootLayout.setCenter(tableCardsComponent); //
        BorderPane.setMargin(tableCardsComponent, new Insets(0, 10, 10, 0));
        
        rootLayout.setBottom(humanHandComponent); //
        BorderPane.setMargin(humanHandComponent, new Insets(10, 0, 0, 0));

        rootLayout.setRight(gameControlsComponent); //
        BorderPane.setMargin(gameControlsComponent, new Insets(0, 0, 0, 10));
        
        System.out.println("GraphicUIJavaFX: initGUI (component hóa) hoàn tất.");
        return rootLayout;
    }
    
    private TienLenPlayer getHumanPlayerFromGame() {
        if (game != null && game.getPlayers() != null) {
            for (TienLenPlayer p : game.getPlayers()) {
                if (!p.isAI()) return p;
            }
        }
        return null;
    }
    
    private void addDefaultTextEffect(Label label) {
        DropShadow ds = new DropShadow();
        ds.setRadius(2); ds.setOffsetX(1.0); ds.setOffsetY(1.0);
        ds.setColor(Color.rgb(0, 0, 0, 0.7));
        label.setEffect(ds);
    }

    @Override
    public void showMessage(String message) {
        if (gameMessageComponent != null) {
            String safeMessage = message != null ? message : "";
            Platform.runLater(() -> gameMessageComponent.setMessage(safeMessage)); //
        }
    }

    @Override
    public void updateGameState() {
        Platform.runLater(() -> {
            if (game == null) {
                System.err.println("GraphicUIJavaFX.updateGameState: game instance is null!");
                if (gameMessageComponent != null) {
                    // Đảm bảo gameMessageComponent được khởi tạo trước khi sử dụng
                    gameMessageComponent.setMessage("Lỗi: Game không tồn tại.");
                }
                return;
            }

            TienLenPlayer currentPlayer = game.getCurrentPlayer();
            boolean isGameOver = (game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER);

            // Cập nhật các component khác như trước
            if (playersListComponent != null) {
                playersListComponent.updatePlayers(game.getPlayers(), currentPlayer, isGameOver, game.getRuleSet().getCardComparator());
            }
            if (tableCardsComponent != null) {
                tableCardsComponent.displayCards(game.getLastPlayedCards());
            }
            // gameMessageComponent thường được cập nhật bởi các sự kiện cụ thể (onPlayerTurnStarted, onCardsPlayed, etc.)
            // nên có thể không cần cập nhật chung ở đây trừ khi có thông điệp mặc định.

            // --- Cập nhật HumanHandComponent dựa trên người chơi HIỆN TẠI ---
            if (humanHandComponent != null) {
                if (currentPlayer != null && !currentPlayer.isAI()) { // Nếu người chơi hiện tại là HUMAN
                    humanHandComponent.setHandTitle("Bài của " + currentPlayer.getName());
                    if (currentPlayer.getHand().isEmpty()) {
                        humanHandComponent.showFinishedMessage();
                    } else if (game.getCurrentTienLenState() == TienLenGameState.WAITING_FOR_PLAYER_INPUT) {
                        // Đây là lượt của người chơi Human này và game đang chờ họ input
                        humanHandComponent.displayHand(currentPlayer.getHand(), this.selectedCards, game.getRuleSet().getCardComparator());
                    } else {
                        // Lượt của Human nhưng không phải WAITING_FOR_PLAYER_INPUT (ví dụ: game đang xử lý sau khi họ đánh)
                        // Vẫn hiển thị bài của họ, nhưng có thể không cho chọn bài.
                        humanHandComponent.displayHand(currentPlayer.getHand(), new ArrayList<>(), game.getRuleSet().getCardComparator()); // Hiển thị bài, không có lựa chọn nào được đánh dấu
                    }
                } else if (currentPlayer != null && currentPlayer.isAI()) { // Nếu người chơi hiện tại là AI
                    humanHandComponent.setHandTitle("Máy đang chơi");
                    humanHandComponent.showWaitingMessage("Đến lượt " + currentPlayer.getName() + " (AI)...");
                    // Xóa các lựa chọn bài của người chơi Human trước đó (nếu có)
                    if (!selectedCards.isEmpty()) {
                        selectedCards.clear();
                    }
                } else if (isGameOver) {
                    humanHandComponent.setHandTitle("Game Kết Thúc");
                    // Cố gắng tìm một người chơi Human để hiển thị trạng thái bài cuối cùng của họ
                    TienLenPlayer humanToDisplayAtGameOver = null;
                    if (game.getPlayers() != null) {
                        // Ưu tiên người chơi Human cuối cùng đã hành động, hoặc người Human đầu tiên
                        if (game.getLastPlayer() != null && !game.getLastPlayer().isAI()) {
                            humanToDisplayAtGameOver = game.getLastPlayer();
                        } else {
                            // Duyệt tìm người chơi human đầu tiên trong danh sách
                            for (TienLenPlayer p : game.getPlayers()) {
                                if (!p.isAI()) {
                                    humanToDisplayAtGameOver = p;
                                    break;
                                }
                            }
                        }
                    }

                    if (humanToDisplayAtGameOver != null) {
                        humanHandComponent.setHandTitle("Bài của " + humanToDisplayAtGameOver.getName() + " (Kết thúc)");
                        if (humanToDisplayAtGameOver.getHand().isEmpty()) {
                            humanHandComponent.showFinishedMessage();
                        } else {
                            // Hiển thị các lá bài còn lại, không có lựa chọn nào
                            humanHandComponent.displayHand(humanToDisplayAtGameOver.getHand(), new ArrayList<>(), game.getRuleSet().getCardComparator());
                        }
                    } else { // Không có người chơi Human nào trong game
                        humanHandComponent.showWaitingMessage("Game đã kết thúc.");
                    }
                } else { // Không có người chơi hiện tại, hoặc game đang ở trạng thái khởi tạo/chờ...
                    humanHandComponent.setHandTitle("Chờ ván mới...");
                    humanHandComponent.clearHand();
                }
            }

            // --- Cập nhật GameControlsComponent và cờ waitingForInput ---
            if (gameControlsComponent != null) {
                boolean canPlayCurrent = false;
                boolean canPassCurrent = false;
                // Các nút chỉ kích hoạt nếu là lượt của người chơi Human và game đang chờ input
                if (!isGameOver && currentPlayer != null && !currentPlayer.isAI() &&
                    game.getCurrentTienLenState() == TienLenGameState.WAITING_FOR_PLAYER_INPUT) {
                    canPlayCurrent = !selectedCards.isEmpty();
                    canPassCurrent = game.canPass(currentPlayer);
                }
                gameControlsComponent.updateButtonStates(canPlayCurrent, canPassCurrent, isGameOver);

                // Cập nhật cờ waitingForInput
                waitingForInput = !isGameOver && currentPlayer != null && !currentPlayer.isAI() &&
                                  game.getCurrentTienLenState() == TienLenGameState.WAITING_FOR_PLAYER_INPUT;
            }
        });
    }

    
    @Override
    public void onPlayerTurnStarted(TienLenPlayer player) { // player là người chơi có lượt MỚI
        Platform.runLater(() -> {
            if (gameMessageComponent != null) {
                String turnMessage = "Lượt của " + player.getName();
                if (player.isAI()) {
                    turnMessage += " (AI)";
                }
                gameMessageComponent.setMessage(turnMessage);
            }
            // Nếu người chơi BẮT ĐẦU lượt là Human, xóa các lựa chọn bài cũ của người Human trước đó
            if (!player.isAI()) {
                 selectedCards.clear();
            }
            updateGameState(); // Cập nhật toàn bộ UI, bao gồm tay bài (sẽ không có thẻ nào được chọn)
                               // và trạng thái các nút bấm.
        });
    }
    
    @Override
    public void onCardsPlayed(TienLenPlayer player, List<Card> cardsPlayed, List<Card> newLastPlayedCards) { //
        Platform.runLater(() -> { //
            TienLenPlayer humanInterfacePlayer = getHumanPlayerFromGame();
            if (player == humanInterfacePlayer) { 
                 selectedCards.clear(); 
            }
            if (gameMessageComponent != null) {
                 gameMessageComponent.setMessage(player.getName() + " đánh: " + formatCardList(cardsPlayed)); //
            }
            updateGameState(); //
        });
    }
    
    private String formatCardList(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return "";
        return cards.stream().map(Card::toString).collect(Collectors.joining(" ")); //
    }

    @Override
    public void displayPlayerHand(TienLenPlayer player) { //
        TienLenPlayer humanInterfacePlayer = getHumanPlayerFromGame();
        if (humanHandComponent != null && player != null && !player.isAI() && player == humanInterfacePlayer) {
            humanHandComponent.displayHand(player.getHand(), selectedCards, game.getRuleSet().getCardComparator()); //
        } else if (humanHandComponent != null) {
            //  humanHandComponent.clearHand(); // Có thể không cần nếu updateGameState đã xử lý
        }
    }
    
    private void handlePlayButton(ActionEvent event) {
        if (game == null) {
            // Nên sử dụng gameMessageComponent nếu đã khởi tạo
            if (gameMessageComponent != null) gameMessageComponent.setMessage("Lỗi: Game chưa được khởi tạo.");
            else System.err.println("Lỗi: Game chưa được khởi tạo.");
            return;
        }

        TienLenPlayer currentPlayer = game.getCurrentPlayer();

        // Chỉ xử lý nếu là lượt của người chơi Human hiện tại và game đang chờ input từ họ
        if (currentPlayer != null && !currentPlayer.isAI() &&
            game.getCurrentTienLenState() == TienLenGameState.WAITING_FOR_PLAYER_INPUT) {

            if (selectedCards.isEmpty()) {
                if (gameMessageComponent != null) gameMessageComponent.setMessage("Bạn chưa chọn bài để đánh!");
                else System.out.println("Bạn chưa chọn bài để đánh!");
                return;
            }

            // Game engine sẽ kiểm tra tính hợp lệ của selectedCards thông qua PlayValidator
            System.out.println("Người chơi Human " + currentPlayer.getName() + " dự định đánh: " + selectedCards);
            game.setPlayerInput(new ArrayList<>(selectedCards)); // Gửi lựa chọn đến game engine

            // Không nên xóa selectedCards ở đây ngay.
            // Sự kiện onCardsPlayed (nếu đánh thành công) hoặc onPlayerTurnStarted (nếu lượt vẫn là của họ sau khi đánh lỗi)
            // nên chịu trách nhiệm xóa selectedCards hoặc cập nhật lại HumanHandComponent.
            // Tuy nhiên, để tránh việc người dùng nhấn Play nhiều lần với cùng một bộ bài đã gửi,
            // có thể tạm thời vô hiệu hóa nút Play hoặc xóa selectedCards sau khi gửi.
            // Cách tốt hơn là chờ phản hồi từ game engine.
            // Hiện tại, onPlayerTurnStarted và onCardsPlayed đã có logic xóa selectedCards.
        } else {
            // Thông báo nếu không đúng lượt hoặc trạng thái
            String msg = "Không phải lượt của bạn hoặc game không chờ input.";
            if (currentPlayer != null && currentPlayer.isAI()) {
                msg = "Đang là lượt của máy, bạn không thể đánh bài.";
            } else if (game.getCurrentTienLenState() != TienLenGameState.WAITING_FOR_PLAYER_INPUT) {
                msg = "Game không ở trạng thái chờ bạn đánh bài.";
            }
            if (gameMessageComponent != null) gameMessageComponent.setMessage(msg);
            else System.out.println(msg);
        }
    }

    private void handlePassButton(ActionEvent event) {
        if (game == null) {
           if (gameMessageComponent != null) gameMessageComponent.setMessage("Lỗi: Game chưa được khởi tạo.");
           else System.err.println("Lỗi: Game chưa được khởi tạo.");
           return;
       }

       TienLenPlayer currentPlayer = game.getCurrentPlayer();

       // Chỉ xử lý nếu là lượt của người chơi Human hiện tại và game đang chờ input từ họ
       if (currentPlayer != null && !currentPlayer.isAI() &&
           game.getCurrentTienLenState() == TienLenGameState.WAITING_FOR_PLAYER_INPUT) {

           if (game.canPass(currentPlayer)) {
                System.out.println("Người chơi Human " + currentPlayer.getName() + " dự định bỏ lượt.");
                game.setPlayerInput(new ArrayList<>()); // Gửi danh sách rỗng để báo bỏ lượt
           } else {
               if (gameMessageComponent != null) gameMessageComponent.setMessage("Bạn không thể bỏ lượt lúc này!");
               else System.out.println("Bạn không thể bỏ lượt lúc này!");
           }
       } else {
           // Thông báo nếu không đúng lượt hoặc trạng thái
           String msg = "Không phải lượt của bạn hoặc game không chờ input.";
           if (currentPlayer != null && currentPlayer.isAI()) {
               msg = "Đang là lượt của máy, bạn không thể bỏ lượt.";
           } else if (game.getCurrentTienLenState() != TienLenGameState.WAITING_FOR_PLAYER_INPUT) {
               msg = "Game không ở trạng thái chờ bạn bỏ lượt.";
           }
           if (gameMessageComponent != null) gameMessageComponent.setMessage(msg);
           else System.out.println(msg);
       }
   }
    
    private void handleNewGameButton(ActionEvent event) {
        if (game != null) {
            selectedCards.clear(); 
            waitingForInput = false;
            game.resetGame(); //
        }
    }

    @Override
    public void onGameOver(List<TienLenPlayer> winners) { //
    	System.out.println("GraphicUIJavaFX.onGameOver được gọi!");
        String resultMessage = createGameOverMessage(winners);

        Platform.runLater(() -> { //
            if (gameMessageComponent != null) {
                gameMessageComponent.setMessage("GAME ĐÃ KẾT THÚC!"); //
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Kết Thúc");
            alert.setHeaderText("Kết quả ván đấu:");
            alert.setContentText(resultMessage);
            alert.getButtonTypes().setAll(ButtonType.OK);
            if (primaryStage != null) {
                alert.initOwner(primaryStage);
            }
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            // updateGameState() sẽ được gọi tự động khi game engine thay đổi generalState thành GAME_OVER
        });
    }

    private String createGameOverMessage(List<TienLenPlayer> winners){
        StringBuilder sb = new StringBuilder(); 
        if (winners == null || winners.isEmpty()) {
            sb.append("Không ai thắng (có thể do lỗi hoặc hòa).\n");
        } else {
            sb.append("Thứ hạng:\n");
            List<TienLenPlayer> sortedWinners = new ArrayList<>(winners);
            sortedWinners.sort(Comparator.comparingInt(TienLenPlayer::getWinnerRank) //
                                       .thenComparing(p -> p.getHand().size())); //
            
            for (TienLenPlayer p : sortedWinners) {
                if (p.getWinnerRank() > 0) { //
                    sb.append(p.getWinnerRank()).append(". ").append(p.getName()); //
                    sb.append("\n");
                }
            }
            if (game != null && game.getPlayers() != null) {
                for (TienLenPlayer p : game.getPlayers()) { //
                    if (p.getWinnerRank() == 0 && !p.getHand().isEmpty()) { //
                        sb.append("- ").append(p.getName()).append(" (Còn ").append(p.getHand().size()).append(" lá)\n"); //
                    }
                }
            }
        }
        return sb.toString();
    }

    @Override
    public List<Card> getPlayerCardSelection(TienLenPlayer player) {
        // player ở đây là người chơi mà game engine đang yêu cầu input
        if (player != null && !player.isAI() && game.getCurrentPlayer() == player) {
            return new ArrayList<>(selectedCards);
        }
        return new ArrayList<>();
    }
}
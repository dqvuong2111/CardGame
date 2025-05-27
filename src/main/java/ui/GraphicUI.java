package ui;

import core.Card;
import core.Game;
import core.games.AbstractCardGame;
import core.games.GameState;
import core.games.RuleSet;
import core.games.tienlenplayer.TienLenPlayer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.gamescreencomponents.CardView;
import ui.gamescreencomponents.GameControlsComponent;
import ui.gamescreencomponents.GameMessageComponent;
import ui.gamescreencomponents.HumanHandComponent;
import ui.gamescreencomponents.PlayersListComponent;
import ui.gamescreencomponents.TableCardsComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GraphicUI extends CardGameUI<AbstractCardGame<? extends RuleSet>> {

    private BorderPane rootLayout;

    private GameMessageComponent gameMessageComponent;
    private PlayersListComponent playersListComponent;
    private TableCardsComponent tableCardsComponent;
    private HumanHandComponent humanHandComponent;
    private GameControlsComponent gameControlsComponent;

    private SceneManager sceneManager;

    private List<Card> selectedCards = new ArrayList<>();
    private volatile boolean waitingForInput = false;

    public GraphicUI(AbstractCardGame<? extends RuleSet> game, Stage primaryStage, SceneManager sceneManager) {
        super(game, primaryStage);
        this.sceneManager = sceneManager;
        this.root = initGUI();
        if (this.root == null) {
            Platform.exit();
            return;
        }
        this.currentScene = new Scene(this.root);
        primaryStage.setScene(this.currentScene);
    }

    @Override
    protected Parent initGUI() {
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
                rootLayout.setStyle("-fx-background-color: #2c3e50;"); 
            }
        } catch (Exception e) {
            System.err.println("Ngoại lệ khi tải ảnh nền GameScreen: " + imagePathForGameScreen + ". " + e.getMessage());
            rootLayout.setStyle("-fx-background-color: #2c3e50;");
        }

        gameMessageComponent = new GameMessageComponent("Chào mừng đến với Tiến Lên!"); 

        if (game != null && game.getPlayers() != null) {
            playersListComponent = new PlayersListComponent(game.getPlayers()); 
        } else {
            playersListComponent = new PlayersListComponent(new ArrayList<>());
        }
        
        tableCardsComponent = new TableCardsComponent(); 
        
        humanHandComponent = new HumanHandComponent(
                mouseEvent -> {
                    Node sourceNode = (Node) mouseEvent.getSource();
                    if (sourceNode instanceof CardView) {
                        CardView clickedCardView = (CardView) sourceNode;
                        Card card = clickedCardView.getCard();

                        TienLenPlayer currentPlayerWhoIsAttemptingToSelect = null;
                        if (game != null) {
                            currentPlayerWhoIsAttemptingToSelect = game.getCurrentPlayer();
                        }

                        // Cho phép chọn bài nếu người chơi hiện tại là Human (không phải AI)
                        // và game đang ở trạng thái chờ input của người chơi đó.
                        if (currentPlayerWhoIsAttemptingToSelect != null && 
                            !currentPlayerWhoIsAttemptingToSelect.isAI() &&
                            game.getCurrentTienLenState() == GameState.WAITING_FOR_PLAYER_INPUT) {

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
                                    game.canPass(currentPlayerWhoIsAttemptingToSelect), 
                                    isGameOverForButton
                                );
                            }
                        } 
                    }
                }
            );
            
            gameControlsComponent = new GameControlsComponent(
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
        rootLayout.setTop(gameMessageComponent); 
        BorderPane.setMargin(gameMessageComponent, new Insets(5, 0, 10, 0));

        rootLayout.setLeft(playersListComponent); 
        BorderPane.setMargin(playersListComponent, new Insets(0, 10, 0, 0));

        rootLayout.setCenter(tableCardsComponent); 
        BorderPane.setMargin(tableCardsComponent, new Insets(0, 10, 10, 0));
        
        rootLayout.setBottom(humanHandComponent); 
        BorderPane.setMargin(humanHandComponent, new Insets(10, 0, 0, 0));

        rootLayout.setRight(gameControlsComponent); 
        BorderPane.setMargin(gameControlsComponent, new Insets(0, 0, 0, 10));
        
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
                    gameMessageComponent.setMessage("Lỗi: Game không tồn tại.");
                }
                return;
            }

            TienLenPlayer currentPlayer = game.getCurrentPlayer();
            boolean isGameOver = (game.getGeneralGameState() == Game.GeneralGameState.GAME_OVER);

            if (playersListComponent != null) {
                playersListComponent.updatePlayers(game.getPlayers(), currentPlayer, isGameOver, game.getRuleSet().getCardComparator());
            }
            if (tableCardsComponent != null) {
                tableCardsComponent.displayCards(game.getLastPlayedCards());
            }

            if (humanHandComponent != null) {
                if (currentPlayer != null && !currentPlayer.isAI()) { 
                    humanHandComponent.setHandTitle("Bài của " + currentPlayer.getName());
                    if (currentPlayer.getHand().isEmpty()) {
                        humanHandComponent.showFinishedMessage();
                    } else if (game.getCurrentTienLenState() == GameState.WAITING_FOR_PLAYER_INPUT) {
                        humanHandComponent.displayHand(currentPlayer.getHand(), this.selectedCards, game.getRuleSet().getCardComparator());
                    } else {
                        humanHandComponent.displayHand(currentPlayer.getHand(), new ArrayList<>(), game.getRuleSet().getCardComparator()); 
                    }
                } else if (currentPlayer != null && currentPlayer.isAI()) { 
                    humanHandComponent.setHandTitle("Máy đang chơi");
                    humanHandComponent.showWaitingMessage("Đến lượt " + currentPlayer.getName() + " (AI)...");
                    if (!selectedCards.isEmpty()) {
                        selectedCards.clear();
                    }
                } else if (isGameOver) {
                    humanHandComponent.setHandTitle("Game Kết Thúc");
                    TienLenPlayer humanToDisplayAtGameOver = getHumanPlayerFromGame(); // Lấy người human đầu tiên để hiển thị khi game over
                    
                    if (humanToDisplayAtGameOver != null) {
                        humanHandComponent.setHandTitle("Bài của " + humanToDisplayAtGameOver.getName() + " (Kết thúc)");
                        if (humanToDisplayAtGameOver.getHand().isEmpty()) {
                            humanHandComponent.showFinishedMessage();
                        } else {
                            humanHandComponent.displayHand(humanToDisplayAtGameOver.getHand(), new ArrayList<>(), game.getRuleSet().getCardComparator());
                        }
                    } else { 
                        humanHandComponent.showWaitingMessage("Game đã kết thúc.");
                    }
                } else { 
                    humanHandComponent.setHandTitle("Chờ ván mới...");
                    humanHandComponent.clearHand();
                }
            }

            if (gameControlsComponent != null) {
                boolean canPlayCurrent = false;
                boolean canPassCurrent = false;
                if (!isGameOver && currentPlayer != null && !currentPlayer.isAI() &&
                    game.getCurrentTienLenState() == GameState.WAITING_FOR_PLAYER_INPUT) {
                    canPlayCurrent = !selectedCards.isEmpty();
                    canPassCurrent = game.canPass(currentPlayer);
                }
                gameControlsComponent.updateButtonStates(canPlayCurrent, canPassCurrent, isGameOver);
                waitingForInput = !isGameOver && currentPlayer != null && !currentPlayer.isAI() &&
                                  game.getCurrentTienLenState() == GameState.WAITING_FOR_PLAYER_INPUT;
            }
        });
    }

    
    
    @Override
    public void onPlayerTurnStarted(TienLenPlayer player) {
        Platform.runLater(() -> {
            if (gameMessageComponent != null) {
                String turnMessage = "Lượt của " + player.getName();
                if (player.isAI()) {
                    turnMessage += " (AI)";
                }
                gameMessageComponent.setMessage(turnMessage);
            }
            if (!player.isAI()) { 
                 selectedCards.clear(); 
            }
            updateGameState(); 
        });
    }


    @Override
    public void displayPlayerHand(TienLenPlayer player) {
        Platform.runLater(() -> {
            if (humanHandComponent != null && game != null && game.getCurrentPlayer() != null) {
                TienLenPlayer currentTurnPlayer = game.getCurrentPlayer();
                if (player != null && !player.isAI() && player == currentTurnPlayer &&
                    game.getCurrentTienLenState() == GameState.WAITING_FOR_PLAYER_INPUT) {
                    humanHandComponent.setHandTitle("Bài của " + player.getName());
                    humanHandComponent.displayHand(player.getHand(), selectedCards, game.getRuleSet().getCardComparator());
                }
            }
        });
    }
    
    private void handlePlayButton(ActionEvent event) {
        if (game == null) {
            if (gameMessageComponent != null) gameMessageComponent.setMessage("Lỗi: Game chưa được khởi tạo.");
            else System.err.println("Lỗi: Game chưa được khởi tạo.");
            return;
        }
        TienLenPlayer currentPlayer = game.getCurrentPlayer();
        if (currentPlayer != null && !currentPlayer.isAI() &&
            game.getCurrentTienLenState() == GameState.WAITING_FOR_PLAYER_INPUT) {
            if (selectedCards.isEmpty()) {
                if (gameMessageComponent != null) gameMessageComponent.setMessage("Bạn chưa chọn bài để đánh!");
                return;
            }
            game.setPlayerInput(new ArrayList<>(selectedCards));
        } else {
            String msg = "Không phải lượt của bạn hoặc game không chờ input.";
            if (currentPlayer != null && currentPlayer.isAI()) {
                msg = "Đang là lượt của máy, bạn không thể đánh bài.";
            } else if (game != null && game.getCurrentTienLenState() != GameState.WAITING_FOR_PLAYER_INPUT) {
                msg = "Game không ở trạng thái chờ bạn đánh bài.";
            }
            if (gameMessageComponent != null) gameMessageComponent.setMessage(msg);
        }
    }

    private void handlePassButton(ActionEvent event) {
        if (game == null) {
           if (gameMessageComponent != null) gameMessageComponent.setMessage("Lỗi: Game chưa được khởi tạo.");
           else System.err.println("Lỗi: Game chưa được khởi tạo.");
           return;
       }
       TienLenPlayer currentPlayer = game.getCurrentPlayer();
       if (currentPlayer != null && !currentPlayer.isAI() &&
           game.getCurrentTienLenState() == GameState.WAITING_FOR_PLAYER_INPUT) {
           if (game.canPass(currentPlayer)) {
                game.setPlayerInput(new ArrayList<>()); 
           } else {
               if (gameMessageComponent != null) gameMessageComponent.setMessage("Bạn không thể bỏ lượt lúc này!");
           }
       } else {
           String msg = "Không phải lượt của bạn hoặc game không chờ input.";
           if (currentPlayer != null && currentPlayer.isAI()) {
               msg = "Đang là lượt của máy, bạn không thể bỏ lượt.";
           } else if (game != null && game.getCurrentTienLenState() != GameState.WAITING_FOR_PLAYER_INPUT) {
               msg = "Game không ở trạng thái chờ bạn bỏ lượt.";
           }
           if (gameMessageComponent != null) gameMessageComponent.setMessage(msg);
       }
    }

    @Override
    public void onCardsPlayed(TienLenPlayer player, List<Card> cardsPlayed, List<Card> newLastPlayedCards) {
        Platform.runLater(() -> {
            if (player != null && !player.isAI()) { 
                selectedCards.clear(); 
            }
            if (gameMessageComponent != null) {
                 gameMessageComponent.setMessage(player.getName() + " đánh: " + formatCardList(cardsPlayed));
            }
            updateGameState();
        });
    }
    
    private void handleNewGameButton(ActionEvent event) {
    if (game != null) {
        selectedCards.clear(); 
        waitingForInput = false;
        
        game.resetGame(); 

        if (game.getGeneralGameState() == Game.GeneralGameState.RUNNING) {
            game.startGameLoop(); 
        } else {
            String errorMessage = "Lỗi sau khi reset: Game không ở trạng thái RUNNING. Vòng lặp không được khởi động. Trạng thái hiện tại: " + game.getGeneralGameState();
            System.err.println(errorMessage);
            if (gameMessageComponent != null) {
                gameMessageComponent.setMessage(errorMessage);
            }
        }
    } else {
        System.err.println("GraphicUIJavaFX: Nút Ván Mới được nhấn nhưng game là null.");
    }
}

    
    private String formatCardList(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return "";
        return cards.stream().map(Card::toString).collect(Collectors.joining(" "));
    }

    @Override
    public void onGameOver(List<TienLenPlayer> winners) {
        String resultMessage = createGameOverMessage(winners);
        Platform.runLater(() -> {
            if (gameMessageComponent != null) {
                gameMessageComponent.setMessage("GAME ĐÃ KẾT THÚC!");
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
        });
    }

    private String createGameOverMessage(List<TienLenPlayer> winners){
        StringBuilder sb = new StringBuilder(); 
        if (winners == null || winners.isEmpty()) {
            sb.append("Không ai thắng (có thể do lỗi hoặc hòa).\n");
        } else {
            sb.append("Thứ hạng:\n");
            List<TienLenPlayer> sortedWinners = new ArrayList<>(winners);
            sortedWinners.sort(Comparator.comparingInt(TienLenPlayer::getWinnerRank)
                                       .thenComparing(p -> p.getHand().size())); 
            
            for (TienLenPlayer p : sortedWinners) {
                if (p.getWinnerRank() > 0) { 
                    sb.append(p.getWinnerRank()).append(". ").append(p.getName()); 
                    sb.append("\n");
                }
            }
            if (game != null && game.getPlayers() != null) {
                for (TienLenPlayer p : game.getPlayers()) { 
                    if (p.getWinnerRank() == 0 && !p.getHand().isEmpty()) { 
                        sb.append("- ").append(p.getName()).append(" (Còn ").append(p.getHand().size()).append(" lá)\n"); 
                    }
                }
            }
        }
        return sb.toString();
    }

    @Override
    public List<Card> getPlayerCardSelection(TienLenPlayer player) {
        if (player != null && !player.isAI() && game != null && game.getCurrentPlayer() == player &&
            game.getCurrentTienLenState() == GameState.WAITING_FOR_PLAYER_INPUT) {
            return new ArrayList<>(selectedCards);
        }
        return new ArrayList<>();
    }
}
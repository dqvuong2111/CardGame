// core/rules/TienLenGame.java
package core.rules;

import core.*;
import core.Card;
import core.Game;
import core.Player;
import core.rules.TienLenRule; 
import core.Deck; 

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors; 

public class TienLenGame extends Game<TienLenRule> implements Runnable { 
    protected List<Card> lastPlayedCards; 
    protected Player lastPlayer; 
    protected int passCount; 
    protected int roundStarterIndex; 

    private volatile List<Card> playerInputCards; 
    private final Object playerInputLock = new Object(); 
    private volatile boolean waitingForHumanInput = false; 
    
    // THÊM: Biến trạng thái để kiểm tra nếu đây là lượt đầu tiên của game
    private boolean isFirstTurnOfGame = true;
    // THÊM: Danh sách người chiến thắng
    private List<Player> winners;
    private int currentWinnerRank;
    private final long AI_DELAY_SECONDS = 2; // Ví dụ: 2 giây
    protected Player playerWhoPlayedLastValidCards; 
    
    public enum GameState {
        INITIALIZING,
        WAITING_FOR_PLAYER_INPUT,
        AI_THINKING,
        ROUND_IN_PROGRESS,
        GAME_OVER_STATE
    }
    
    protected GameState currentState; 

    public TienLenGame(List<Player> players, TienLenRule ruleSet) {
        super("Tien Len Mien Nam", players, new Deck(), ruleSet);
        this.lastPlayedCards = new ArrayList<>();
        this.passCount = 0;
        this.roundStarterIndex = -1;
        this.currentState = GameState.INITIALIZING;
        this.winners = new ArrayList<>(); // Khởi tạo danh sách người thắng cuộc
        this.currentWinnerRank = 1; // Bắt đầu thứ hạng từ 1 (nhất)
    }

    public GameState getCurrentState() {
        return currentState;
    }

    protected void setCurrentState(GameState newState) {
        this.currentState = newState;
        notifyGameStateUpdated(); 
    }

    @Override
	public void dealCards() {
        deck.reset();
        deck.shuffle();

        int numPlayers = players.size();
        int cardsPerPlayer = 13;

        for (Player player : players) {
            player.getHand().clear();
            player.setHasNoCards(false);
            player.setWinnerRank(0); // Reset hạng thắng cuộc
        }
        this.winners.clear(); // Xóa danh sách người thắng cuộc khi chia bài mới
        this.currentWinnerRank = 1; // Đặt lại thứ hạng

        for (int i = 0; i < cardsPerPlayer; i++) {
            for (int j = 0; j < numPlayers; j++) {
                Player player = players.get(j);
                Card card = deck.drawCard();
                if (card != null) {
                    player.addCard(card);
                }
            }
        }

        for (Player player : players) {
            player.sortHand(ruleSet.getCardComparator());
        }

        findStartingPlayer();
        this.isFirstTurnOfGame = true;
        notifyMessageReceived("Đã chia bài. Bắt đầu game Tiến Lên Miền Nam!");
    }

    @Override
    public void startGame() {
        super.startGame(); 
        if (gameThread == null || !gameThread.isAlive()) {
             gameThread = new Thread(this); 
             gameThread.start();
        }
        notifyPlayerTurnStarted(getCurrentPlayer()); 
    }

    @Override
    public void resetGame() {
        stopGameLoop(); 

        this.generalState = GeneralGameState.INITIALIZING;
        this.currentState = GameState.INITIALIZING;

        for (Player p : players) {
            p.getHand().clear();
            p.setHasNoCards(false); 
            p.clearWinnerRank(); 
        }
        deck.reset(); 
        lastPlayedCards.clear();
        lastPlayer = null;
        passCount = 0;
        currentPlayerIndex = 0; 
        roundStarterIndex = -1; 

        dealCards();

        findStartingPlayer(); 

        this.generalState = GeneralGameState.RUNNING;
        this.isFinished = false;

        notifyMessageReceived("Bắt đầu ván mới! Tìm người đi đầu...");
        notifyGameStateUpdated(); 
        
        if (gameThread == null || !gameThread.isAlive()) {
             gameThread = new Thread(this); 
             gameThread.start();
        }
        notifyPlayerTurnStarted(getCurrentPlayer()); 
    }

    // Phương thức run() từ interface Runnable
    @Override
    public void run() { 
        runGameLoop(); 
    }

    // Phương thức runGameLoop() triển khai abstract method từ lớp Game
    @Override
    public void runGameLoop() {
        // dealCards() và determineFirstPlayer() đã được gọi từ Main.java
        // (Hoặc có thể gọi ở đây nếu muốn quản lý hoàn toàn trong game loop)
        // ensure dealCards() and determineFirstPlayer() are called once before this loop starts
        // as per Main.java, they are called before startGameLoop()

        setGeneralGameState(GeneralGameState.RUNNING);
        notifyMessageReceived("Game Tiến Lên Miền Nam đã bắt đầu!");
        
        while (getGeneralGameState() == GeneralGameState.RUNNING && !checkGameOver()) {
            Player currentPlayer = players.get(currentPlayerIndex);
            
            // Bỏ qua người chơi đã hết bài
            if (currentPlayer.hasNoCards()) {
                // Nếu người chơi đã hết bài mà vẫn đến lượt, chuyển lượt
                // Điều này có thể xảy ra nếu người đó hết bài ở cuối vòng và vòng mới bắt đầu
                // mà không có người chơi nào khác để đi tiếp.
                moveToNextPlayer(); 
                continue; 
            }

            notifyPlayerTurnStarted(currentPlayer);
            notifyGameStateUpdated();
            
            List<Card> cardsToPlay = null;
            boolean passedTurn = false;

            if (currentPlayer.isAI()) {
                currentState = GameState.AI_THINKING;
                try {
                    TimeUnit.SECONDS.sleep(AI_DELAY_SECONDS); // AI "suy nghĩ"
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    notifyMessageReceived("AI bị gián đoạn.");
                }

                AIPlayer aiPlayer = (AIPlayer) currentPlayer;
                
                // Đảm bảo AIPlayer.chooseCards trả về danh sách rỗng nếu không có nước đi
                cardsToPlay = aiPlayer.chooseCards(lastPlayedCards); 
                
                if (cardsToPlay.isEmpty()) {
                    passedTurn = true;
                } else {
                    boolean isValid = isValidPlay(cardsToPlay);
                    if (!isValid) {
                        // AI chọn bài không hợp lệ (lỗi logic AI), coi như bỏ lượt để tránh kẹt game
                        // TODO: Cải thiện AI để nó không bao giờ chọn bài không hợp lệ
                        notifyMessageReceived(aiPlayer.getName() + " chọn bài không hợp lệ, tự động bỏ lượt.");
                        passedTurn = true;
                    }
                }
            } else {
            	 try {
                     TimeUnit.SECONDS.sleep(AI_DELAY_SECONDS); // AI "suy nghĩ"
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                     notifyMessageReceived("AI bị gián đoạn.");
                 }
                // Logic cho người chơi
                currentState = GameState.WAITING_FOR_PLAYER_INPUT;
                waitingForHumanInput = true;
                notifyGameStateUpdated(); // Cập nhật GUI để hiển thị nút Play/Pass và trạng thái chờ
                
                cardsToPlay = getPlayerInput(); // Chờ người chơi chọn bài hoặc bấm Pass
                
                waitingForHumanInput = false; // Đã nhận input
                playerInputCards = null; // Reset input cho lượt sau
                
                if (cardsToPlay != null && cardsToPlay.isEmpty()) { // Người chơi bấm Pass
                    passedTurn = true;
                } else if (cardsToPlay == null) {
                    // Xử lý trường hợp game bị gián đoạn khi chờ input (getplayerInput trả về null)
                    notifyMessageReceived("Người chơi bị gián đoạn, bỏ lượt.");
                    passedTurn = true;
                } else {
                    boolean isValid = isValidPlay(cardsToPlay);
                    if (!isValid) {
                        notifyMessageReceived("Bài đánh không hợp lệ! Hãy thử lại.");
                        // KHÔNG CHUYỂN LƯỢT, giữ nguyên cho người chơi hiện tại
                        continue; 
                    }
                }
            }

            if (passedTurn) {
                // Người chơi bỏ lượt
                notifyPlayerPassed(currentPlayer);
                passCount++;
                
                // Tính số người chơi còn đang trong game (chưa hết bài)
                int activePlayersCount = (int) players.stream().filter(p -> !p.hasNoCards()).count();
                
                // Vòng kết thúc nếu tất cả người chơi còn lại (trừ người đã đánh bài cuối cùng) đều bỏ lượt
                // HOẶC nếu chỉ còn 1 người chơi hoạt động và người đó bỏ lượt
                // (Điều kiện này phức tạp hơn một chút vì nếu chỉ còn 1 người chơi, và họ bỏ lượt,
                // thì không ai đỡ bài được, nên lượt phải về người đã đánh cuối cùng)
                
                // Trường hợp 1: Người đánh bài cuối cùng là chính người hiện tại
                // (Đây là vòng mới, hoặc game mới, và người này bỏ lượt, điều này không nên xảy ra nếu có 3 Bích)
                // Hoặc trường hợp AI ko tìm đc bài nào hợp lệ ở lượt đi đầu tiên của vòng
                if (lastPlayedCards.isEmpty() && passCount == activePlayersCount) {
                    // Nếu chưa có bài nào được đánh trong vòng và tất cả người chơi bỏ lượt
                    // (Điều này thường chỉ xảy ra khi không ai có 3 bích hoặc lỗi logic)
                    // hoặc là một vòng mới và người đi đầu tiên lại bỏ lượt
                    notifyMessageReceived("Không ai có thể đi bài. Chuyển sang người tiếp theo.");
                    moveToNextPlayer(); // Chuyển lượt sang người tiếp theo
                    lastPlayedCards.clear(); // Vòng mới nhưng vẫn chưa có ai đánh
                    lastPlayer = null;
                    passCount = 0; // Reset passCount
                    playerWhoPlayedLastValidCards = null; // Reset
                    roundStarterIndex = currentPlayerIndex; // Người đi đầu vòng mới là người hiện tại
                    notifyRoundStarted(players.get(roundStarterIndex));
                    continue; // Tiếp tục vòng lặp game
                }
                
                // Trường hợp 2: Vòng kết thúc
                // Vòng kết thúc khi số người bỏ lượt (sau người đánh cuối cùng) bằng số người chơi còn lại
                // trừ đi người đã đánh bài cuối cùng và người đang đến lượt (nếu người đó không phải người đánh cuối)
                // hoặc đơn giản hơn là passCount bằng (tổng số người chơi - 1)
                // VÍ DỤ: 4 người. A đánh. B bỏ. C bỏ. D bỏ. PassCount = 3. 
                // Người đã đánh cuối cùng là A. 
                // A là người duy nhất không bỏ lượt trong số những người chơi còn lại.
                // activePlayersCount - 1 (vì người đã đánh cuối cùng không tính vào passCount)
                if (playerWhoPlayedLastValidCards != null && passCount >= activePlayersCount - 1) { 
                    // VÒNG KẾT THÚC
                    notifyMessageReceived("Tất cả người chơi đã bỏ lượt sau " + playerWhoPlayedLastValidCards.getName() + ". Vòng mới bắt đầu!");
                    
                    // Người đi đầu vòng mới sẽ là người đã đánh bài cuối cùng trong vòng trước
                    currentPlayerIndex = players.indexOf(playerWhoPlayedLastValidCards);
                    roundStarterIndex = currentPlayerIndex; // Đặt lại người bắt đầu vòng mới

                    lastPlayedCards.clear(); // Reset bài đã đánh trên bàn
                    lastPlayer = null; // Reset người chơi cuối cùng
                    passCount = 0; // Reset số lượt bỏ
                    isFirstTurnOfGame = false; // Không còn là lượt đầu tiên của game nếu đã có bài được đánh
                    notifyRoundStarted(players.get(roundStarterIndex));
                    
                } else {
                    // Chưa kết thúc vòng, chuyển sang người tiếp theo
                    moveToNextPlayer();
                }

            } else {
                // Người chơi đánh bài hợp lệ
                currentPlayer.removeCards(cardsToPlay); // Xóa bài khỏi tay người chơi
                lastPlayedCards = new ArrayList<>(cardsToPlay); // Cập nhật bài đã đánh trên bàn
                lastPlayer = currentPlayer; // Cập nhật người chơi đánh bài cuối cùng
                playerWhoPlayedLastValidCards = currentPlayer; // Cập nhật người chơi đã đánh bài hợp lệ cuối cùng

                notifyCardsPlayed(currentPlayer, cardsToPlay, lastPlayedCards);
                passCount = 0; // Reset số lượt bỏ khi có người đánh bài thành công
                isFirstTurnOfGame = false; // Không phải lượt đầu tiên nữa

                // Kiểm tra xem người chơi đã hết bài chưa
                if (currentPlayer.hasNoCards()) {
                    currentPlayer.setHasNoCards(true);
                    currentPlayer.setWinnerRank(currentWinnerRank);
                    currentWinnerRank++;
                    winners.add(currentPlayer);
                    notifyPlayerEliminated(currentPlayer);
                    
                    // Sau khi hết bài, chuyển sang người tiếp theo (không phải vòng mới)
                    // Nếu người này là người đánh cuối cùng, và mọi người bỏ lượt, thì vòng mới sẽ bắt đầu với người tiếp theo có bài
                    // Hoặc người tiếp theo trong danh sách nếu không ai còn bài.
                    moveToNextPlayer(); 
                    
                    // Kiểm tra game Over sau khi một người chơi hết bài
                    if (checkGameOver()) { // Sử dụng phương thức checkGameOver của Game
                        setGeneralGameState(GeneralGameState.GAME_OVER);
                        notifyGameOver(determineWinners()); // Gọi determineWinners để lấy danh sách cuối cùng
                    }

                } else {
                    // Nếu chưa hết bài, chuyển sang người tiếp theo
                    moveToNextPlayer();
                }
            }
            
            // Tạm dừng để người chơi có thể thấy lượt đi
            try {
                // Đảm bảo không chờ nếu game đã kết thúc
                if (getGeneralGameState() == GeneralGameState.RUNNING) {
                    TimeUnit.MILLISECONDS.sleep(500); // 0.5 giây
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } // End while loop

        // Khi game loop kết thúc (generalState != RUNNING hoặc checkGameOver() true)
        if (getGeneralGameState() == GeneralGameState.GAME_OVER) {
        	System.out.println("notifyGameOver được gọi trước notifyGameOver(winners);!");
            notifyGameOver(winners); // winners đã được điền trong quá trình game
        }
    }


    @Override
    public void playTurn() {
        if (generalState != GeneralGameState.RUNNING) {
            return;
        }

        Player currentPlayer = players.get(currentPlayerIndex);
        notifyPlayerTurnStarted(currentPlayer);
        notifyGameStateUpdated();

        List<Card> selectedCards = new ArrayList<>();

        if (currentPlayer.isAI()) {
            currentState = GameState.AI_THINKING;
            notifyMessageReceived(currentPlayer.getName() + " đang suy nghĩ...");
            try {
                TimeUnit.SECONDS.sleep(AI_DELAY_SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            selectedCards = ((AIPlayer) currentPlayer).chooseCards(lastPlayedCards);
            // AI có thể trả về null nếu không có nước đi hợp lệ (tức là pass)
            if (selectedCards == null) {
                selectedCards = new ArrayList<>(); // Coi như chọn rỗng để xử lý pass
            }
        } else {
            currentState = GameState.WAITING_FOR_PLAYER_INPUT;
            notifyMessageReceived("Lượt của bạn, " + currentPlayer.getName() + ". Vui lòng chọn bài.");
            selectedCards = getPlayerInput();
            playerInputCards = null;
            waitingForHumanInput = false;
        }

        // --- SỬA ĐỔI LOGIC XỬ LÝ LƯỢT ĐẦU TIÊN CỦA GAME VÀ CÁC TRƯỜNG HỢP KHÁC ---
        if (isFirstTurnOfGame) {
            Card threeClubs = new Card(Card.Suit.CLUBS, Card.Rank.THREE);
            // Đảm bảo người chơi hiện tại CÓ 3 Bích
            if (!currentPlayer.getHand().contains(threeClubs)) {
                // Điều này không nên xảy ra nếu findStartingPlayer() đã tìm đúng người
                // Nhưng là một kiểm tra an toàn. Nếu xảy ra, có thể là lỗi logic hoặc chia bài.
                notifyMessageReceived("Lỗi: Người đi đầu không có 3 Bích. Vui lòng kiểm tra logic.");
                // Bỏ qua lượt này và chuyển sang người chơi tiếp theo
                passCurrentPlayer(currentPlayer);
                isFirstTurnOfGame = false; // Đã xử lý lượt đầu tiên (dù là bỏ lượt do lỗi)
                moveToNextPlayer();
                return;
            }

            // Kiểm tra xem bài đã chọn có chứa 3 Bích và là hợp lệ không
            if (selectedCards == null || selectedCards.isEmpty() || !selectedCards.contains(threeClubs)) {
                if (!currentPlayer.isAI()) {
                    notifyMessageReceived("Bạn phải đánh 3 Bích trong lượt đầu tiên của game!");
                    if (selectedCards != null && !selectedCards.isEmpty()) {
                        // currentPlayer.addCards(selectedCards); // Trả lại bài đã chọn sai
                    }
                    waitingForHumanInput = true;
                    currentState = GameState.WAITING_FOR_PLAYER_INPUT;
                    notifyGameStateUpdated();
                    return;
                } else {
                    // AI không đánh 3 Bích trong lượt đầu tiên, đây là lỗi AI
                    notifyMessageReceived(currentPlayer.getName() + " (AI) không đánh 3 Bích trong lượt đầu tiên. Lỗi AI?");
                    // AI bị coi là bỏ lượt hoặc game có thể tạm dừng để kiểm tra
                    passCurrentPlayer(currentPlayer);
                    isFirstTurnOfGame = false;
                    moveToNextPlayer();
                    return;
                }
            }

            // Nếu đã chọn bài có 3 Bích, kiểm tra tính hợp lệ của tổ hợp
            if (!ruleSet.isValidCombination(selectedCards)) {
                if (!currentPlayer.isAI()) {
                    notifyMessageReceived("Bộ bài bạn chọn có 3 Bích nhưng không phải là tổ hợp hợp lệ. Vui lòng chọn lại.");
                    currentPlayer.addCards(selectedCards);
                    waitingForHumanInput = true;
                    currentState = GameState.WAITING_FOR_PLAYER_INPUT;
                    notifyGameStateUpdated();
                    return;
                } else {
                    notifyMessageReceived(currentPlayer.getName() + " (AI) đánh 3 Bích nhưng tổ hợp không hợp lệ. Lỗi AI?");
                    passCurrentPlayer(currentPlayer);
                    isFirstTurnOfGame = false;
                    moveToNextPlayer();
                    return;
                }
            }

            // Nếu mọi thứ hợp lệ, đánh bài và đánh dấu isFirstTurnOfGame = false
            isFirstTurnOfGame = false;
            lastPlayedCards = new ArrayList<>(selectedCards);
            lastPlayer = currentPlayer;
            currentPlayer.removeCards(selectedCards);
            notifyCardsPlayed(currentPlayer, selectedCards, lastPlayedCards);
            passCount = 0;
        } else { // Không phải lượt đầu tiên của game
            if (selectedCards.isEmpty()) { // Người chơi bỏ lượt (pass)
                // Người đi đầu vòng mới KHÔNG ĐƯỢC bỏ lượt (nếu lastPlayedCards rỗng)
                if (lastPlayedCards.isEmpty() && lastPlayer == null) { // Người đi đầu vòng mới
                    if (!currentPlayer.isAI()) {
                        notifyMessageReceived("Bạn không thể bỏ lượt khi là người đi đầu vòng mới. Vui lòng chọn bài.");
                        waitingForHumanInput = true;
                        currentState = GameState.WAITING_FOR_PLAYER_INPUT;
                        notifyGameStateUpdated();
                        return;
                    } else {
                        notifyMessageReceived(currentPlayer.getName() + " (AI) cố gắng bỏ lượt không hợp lệ khi là người đi đầu vòng mới.");
                        // Coi như AI mắc lỗi và buộc nó phải đánh bài (hoặc pass và xử lý game state)
                        // Tạm thời coi như pass và chuyển lượt, nhưng cần cân nhắc AI logic
                        passCurrentPlayer(currentPlayer);
                    }
                } else {
                    // Có thể pass nếu không phải là người đi đầu vòng mới
                    passCurrentPlayer(currentPlayer);
                }
            } else { // Người chơi đánh bài
                boolean isValidMove = false;
                if (lastPlayedCards.isEmpty()) { // Bắt đầu một vòng mới
                    // Lượt đầu tiên của vòng (không phải lượt đầu tiên của game)
                    if (ruleSet.isValidCombination(selectedCards)) {
                        isValidMove = true;
                    }
                } else { // Đánh sau người khác
                    if (ruleSet.canPlayAfter(selectedCards, lastPlayedCards)) {
                        isValidMove = true;
                    }
                }

                if (isValidMove) {
                    lastPlayedCards = new ArrayList<>(selectedCards);
                    lastPlayer = currentPlayer;
                    currentPlayer.removeCards(selectedCards);
                    notifyCardsPlayed(currentPlayer, selectedCards, lastPlayedCards);
                    passCount = 0;
                } else {
                    if (!currentPlayer.isAI()) {
                        notifyMessageReceived("Bài đánh không hợp lệ. Vui lòng chọn lại.");
                        currentPlayer.addCards(selectedCards);
                        waitingForHumanInput = true;
                        currentState = GameState.WAITING_FOR_PLAYER_INPUT;
                        notifyGameStateUpdated();
                        return;
                    } else {
                        notifyMessageReceived(currentPlayer.getName() + " (AI) đánh bài không hợp lệ. Coi như bỏ lượt.");
                        passCurrentPlayer(currentPlayer);
                    }
                }
            }
        }

        // Kiểm tra nếu người chơi hết bài
        if (currentPlayer.getHand().isEmpty()) {
            currentPlayer.setHasNoCards(true);
            notifyPlayerEliminated(currentPlayer);
            addWinner(currentPlayer); // Thêm người chơi vào danh sách thắng cuộc
        }

        // Kiểm tra điều kiện kết thúc vòng
        // Số người chơi không bị loại = tổng số người chơi - số người chơi đã hết bài
        int activePlayersCount = players.size() - getEliminatedPlayersCount();
        if (passCount == activePlayersCount - 1 && activePlayersCount > 1) { // Tất cả người chơi khác đã bỏ lượt
            startNewRound();
        } else if (activePlayersCount == 1) { // Chỉ còn một người chơi chưa hết bài
            // Người cuối cùng còn lại sẽ bị coi là thua (hoặc không có hạng)
            // Hoặc game kết thúc nếu đã xác định được tất cả người thắng
            checkGameOver(); // Kích hoạt kiểm tra kết thúc game
        } else {
            moveToNextPlayer();
        }
    }
    
    private void startNewRound() {
        notifyMessageReceived("Vòng mới bắt đầu!");
        lastPlayedCards.clear();
        lastPlayer = null;
        passCount = 0;
        // Người bắt đầu vòng mới là người vừa đánh bài cuối cùng của vòng trước
        // hoặc người đi đầu vòng trước nếu tất cả đã pass (lastPlayer == null)
        // Cần tìm người chơi hợp lệ để bắt đầu vòng mới
        if (lastPlayer != null) {
            currentPlayerIndex = players.indexOf(lastPlayer);
        } else {
            // Nếu lastPlayer là null (tất cả đã pass ở vòng trước),
            // người bắt đầu vòng mới là người bắt đầu vòng trước đó (roundStarterIndex)
            // Cần tìm người chơi hợp lệ từ roundStarterIndex
            int originalRoundStarter = roundStarterIndex;
            do {
                currentPlayerIndex = (roundStarterIndex + 1) % players.size();
                roundStarterIndex = currentPlayerIndex; // Cập nhật roundStarterIndex cho vòng mới
                if (players.get(currentPlayerIndex).hasNoCards()) {
                    if (currentPlayerIndex == originalRoundStarter) {
                        // Tất cả người chơi còn lại đã hết bài, hoặc chỉ còn một người chơi cuối cùng
                        // Đây là tình huống game kết thúc
                        checkGameOver();
                        return;
                    }
                    continue;
                }
                break;
            } while (true); // Lặp cho đến khi tìm được người chơi hợp lệ
        }
        notifyRoundStarted(players.get(currentPlayerIndex));
    }

    public int getEliminatedPlayersCount() {
        int count = 0;
        for (Player p : players) {
            if (p.hasNoCards()) {
                count++;
            }
        }
        return count;
    }

    public void addWinner(Player player) {
        if (!winners.contains(player)) { // Đảm bảo không thêm trùng lặp
            player.setWinnerRank(currentWinnerRank);
            winners.add(player);
            currentWinnerRank++; // Tăng thứ hạng cho người tiếp theo
            notifyMessageReceived(player.getName() + " về đích thứ " + player.getWinnerRank() + "!");
        }
    }
    
    private void passCurrentPlayer(Player player) {
        notifyPlayerPassed(player);
        passCount++;
    }
    
    private void moveToNextPlayer() {
        int originalPlayerIndex = currentPlayerIndex;
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            // Nếu đã duyệt hết một vòng mà vẫn quay lại người ban đầu và họ đã hết bài,
            // có thể cần một cơ chế thoát để tránh vòng lặp vô hạn
            if (currentPlayerIndex == originalPlayerIndex && players.get(currentPlayerIndex).hasNoCards()) {
                // Điều này có nghĩa là tất cả người chơi còn lại đều đã hết bài.
                // Game đáng lẽ đã kết thúc thông qua checkGameOver()
                // Đây là một biện pháp bảo vệ.
                setGeneralGameState(GeneralGameState.GAME_OVER); 
                break; 
            }
        } while (players.get(currentPlayerIndex).hasNoCards()); // Bỏ qua người chơi đã hết bài
    }
    
    private void findStartingPlayer() {
        Card threeClubs = new Card(Card.Suit.CLUBS, Card.Rank.THREE);
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getHand().contains(threeClubs)) {
                currentPlayerIndex = i;
                roundStarterIndex = i; // Người bắt đầu vòng đầu tiên của game
                notifyMessageReceived(players.get(i).getName() + " có 3 Bích. Họ sẽ đi đầu!");
                return;
            }
        }
        // Fallback nếu không tìm thấy 3 Bích (không nên xảy ra với bộ bài 52 lá đầy đủ)
        currentPlayerIndex = 0;
        roundStarterIndex = 0;
        notifyMessageReceived("Không tìm thấy 3 Bích. Người chơi đầu tiên sẽ đi đầu.");
    }


    private void handlePlay(Player player, List<Card> cards) {
        player.removeCards(cards);
        lastPlayedCards = new ArrayList<>(cards); 
        lastPlayer = player;
        passCount = 0; 
        notifyCardsPlayed(player, cards, lastPlayedCards);
        notifyMessageReceived(player.getName() + " đã đánh: " + cards);
        notifyGameStateUpdated();
        
        if (player.getHand().isEmpty()) {
            player.setHasNoCards(true); 
            // Gán thứ hạng cho người chơi đã hết bài
            List<Player> playersInRankOrder = players.stream()
                                                    .filter(Player::hasNoCards)
                                                    .sorted(Comparator.comparing(Player::getWinnerRank))
                                                    .collect(Collectors.toList());
            player.setWinnerRank(playersInRankOrder.size() + 1); 
            notifyPlayerEliminated(player); 
        }
    }
    
    @Override
    public String getGameStateDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Trạng thái Game Tiến Lên ---\n");
        sb.append("Lượt của: ").append(getCurrentPlayer().getName()).append("\n");
        sb.append("Bài đánh gần nhất: ").append(lastPlayedCards.isEmpty() ? "Chưa có" : ruleSet.getCardsDisplay(lastPlayedCards)).append("\n");
        sb.append("Người đánh gần nhất: ").append(lastPlayer != null ? lastPlayer.getName() : "Không có").append("\n");
        sb.append("Số lượt bỏ qua: ").append(passCount).append("\n");
        sb.append("Trạng thái game: ").append(currentState).append("\n");
        sb.append("Tổng người chơi: ").append(players.size()).append("\n");
        sb.append("Người chơi đã hết bài: ").append(getEliminatedPlayersCount()).append("\n");
        sb.append("Người thắng cuộc:\n");
        if (winners.isEmpty()) {
            sb.append("  Chưa có");
        } else {
            for (Player winner : winners) {
                sb.append("  - ").append(winner.getName()).append(" (Hạng ").append(winner.getWinnerRank()).append(")\n");
            }
        }
        return sb.toString();
    }

    private void handlePass(Player player) {
        if (!canPass(player)) {
            notifyMessageReceived("Bạn không thể bỏ lượt lúc này!");
            return; 
        }
        passCount++;
        notifyPlayerPassed(player);
        notifyMessageReceived(player.getName() + " đã BỎ LƯỢT. (" + passCount + "/" + (players.size() - 1) + " pass)");
        
        long activePlayers = players.stream().filter(p -> !p.hasNoCards()).count();
        if (passCount >= activePlayers - 1) { 
            lastPlayedCards.clear();
            lastPlayer = null;
            passCount = 0; 
            notifyMessageReceived("Vòng mới bắt đầu!");
            notifyRoundStarted(getCurrentPlayer()); 
            notifyGameStateUpdated();
        }
    }

    @Override
    public boolean checkGameOver() {
        // Game kết thúc khi chỉ còn 1 người chơi chưa hết bài (người thua cuộc)
        // hoặc khi tất cả người chơi đã hết bài (trừ người cuối cùng).
        int activePlayers = 0;
        Player lastActivePlayer = null;
        for (Player p : players) {
            if (!p.hasNoCards()) {
                activePlayers++;
                lastActivePlayer = p;
            }
        }

        if (activePlayers <= 1) { // Chỉ còn 0 hoặc 1 người chơi
            if (lastActivePlayer != null) {
                // Người cuối cùng còn lại sẽ bị coi là thua (không có hạng)
                // hoặc bạn có thể gán hạng cuối cùng cho họ nếu muốn
                if (lastActivePlayer.getWinnerRank() == 0) { // Nếu chưa có hạng
                    // Có thể gán hạng cuối cùng cho người này
                    lastActivePlayer.setWinnerRank(players.size());
                    winners.add(lastActivePlayer);
                }
            }
            isFinished = true;
            generalState = GeneralGameState.GAME_OVER;
            notifyGameOver(determineWinners()); // Gọi determineWinners để lấy danh sách cuối cùng
            return true;
        }
        return false;
    }

    @Override
    protected List<Player> determineWinners() {
        // Sắp xếp danh sách người thắng cuộc theo thứ hạng
        Collections.sort(winners, Comparator.comparingInt(Player::getWinnerRank));
        return new ArrayList<>(winners); // Trả về bản sao để tránh sửa đổi trực tiếp
    }
    



    @Override
    public boolean canPass(Player player) {
        // Chỉ có thể pass nếu không phải là người đi đầu vòng mới
        // Người đi đầu vòng mới là người mà lastPlayedCards rỗng HOẶC lastPlayer là chính người đó (nếu đã đánh hết bài và bắt đầu vòng mới)
        return !lastPlayedCards.isEmpty() || (lastPlayer != null && lastPlayer.equals(player));
    }

    @Override
    public int getPassCount() { 
        return passCount;
    }
    

    @Override
	public void stopGameLoop() {
        // Đặt trạng thái để dừng vòng lặp chính
        setGeneralGameState(GeneralGameState.GAME_OVER);
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt(); // Ngắt thread nếu nó đang chờ
            try {
                gameThread.join(1000); // Chờ thread kết thúc trong 1 giây
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Override
    public List<Card> getLastPlayedCards() {
        return lastPlayedCards;
    }

    @Override
    public Player getLastPlayer() {
        return lastPlayer;
    }

    @Override
    public boolean isValidPlay(List<Card> cards) {
        if (lastPlayedCards.isEmpty()) {
            return ruleSet.isValidCombination(cards);
        } else {
            return ruleSet.canPlayAfter(cards, lastPlayedCards);
        }
    }

    @Override
    public void setPlayerInput(List<Card> cards) {
        synchronized (playerInputLock) {
            this.playerInputCards = cards;
            playerInputLock.notifyAll();
        }
    }

    private List<Card> getPlayerInput() {
        synchronized (playerInputLock) {
            waitingForHumanInput = true;
            notifyGameStateUpdated(); // Cập nhật GUI để hiển thị trạng thái chờ
            while (playerInputCards == null) {
                try {
                    playerInputLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    notifyMessageReceived("Game bị gián đoạn khi chờ input.");
                    return null;
                }
            }
            return new ArrayList<>(playerInputCards); // Trả về bản sao để tránh sửa đổi bên ngoài
        }
    }

    
}
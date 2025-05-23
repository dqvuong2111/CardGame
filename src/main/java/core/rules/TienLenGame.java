// core/rules/TienLenGame.java
package core.rules;

import core.*;
import core.Card;
import core.Game;
import core.Player;
import core.Deck;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class TienLenGame extends Game<TienLenRule> implements Runnable {
    protected List<Card> lastPlayedCards;
    protected Player lastPlayer;
    protected int passCount;
    protected int roundStarterIndex;

    private volatile List<Card> playerInputCards;
    private final Object playerInputLock = new Object();
    private volatile boolean waitingForHumanInput = false;

    private boolean isFirstTurnOfGame = true;
    private List<Player> winners;
    private int currentWinnerRank;
    private final long AI_DELAY_SECONDS = 2; // Thời gian chờ của AI
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
        this.winners = new ArrayList<>();
        this.currentWinnerRank = 1;
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
            player.setWinnerRank(0);
        }
        this.winners.clear();
        this.currentWinnerRank = 1;

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
             gameThread.setDaemon(true); // Nên đặt là daemon
             gameThread.start();
        }
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
        this.isFirstTurnOfGame = true; // Reset cho ván mới
        this.winners.clear();
        this.currentWinnerRank = 1;


        dealCards(); 
        this.generalState = GeneralGameState.RUNNING; // Sẵn sàng chạy
        this.isFinished = false;

        notifyMessageReceived("Bắt đầu ván mới! Tìm người đi đầu...");
        notifyGameStateUpdated();
    }

    @Override
    public void run() {
        runGameLoop();
    }

    @Override
    public void runGameLoop() {
        setGeneralGameState(GeneralGameState.RUNNING);
        notifyMessageReceived("Game Tiến Lên Miền Nam đã bắt đầu!");

        while (getGeneralGameState() == GeneralGameState.RUNNING && !isFinished) {
            Player currentPlayer = players.get(currentPlayerIndex);

            if (currentPlayer.hasNoCards()) {
                moveToNextPlayer();
                if (checkGameOver()) { // Kiểm tra lại sau khi chuyển người
                    setGeneralGameState(GeneralGameState.GAME_OVER); // Đặt trạng thái để thoát vòng lặp
                    break;
                }
                continue;
            }

            notifyPlayerTurnStarted(currentPlayer);

            List<Card> cardsToPlay = null;
            boolean passedTurn = false;

            if (currentPlayer.isAI()) {
                setCurrentState(GameState.AI_THINKING);
                try {
                    // SỬ DỤNG BIẾN AI_DELAY_SECONDS Ở ĐÂY
                    TimeUnit.SECONDS.sleep(AI_DELAY_SECONDS); // AI "suy nghĩ"
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    notifyMessageReceived(currentPlayer.getName() + " (AI) bị gián đoạn.");
                    // Có thể coi như AI bỏ lượt nếu bị gián đoạn
                    passedTurn = true;
                }

                if (!passedTurn) { // Chỉ cho AI chọn bài nếu không bị gián đoạn
                    AIPlayer aiPlayer = (AIPlayer) currentPlayer;
                    // Thêm isFirstTurnOfGame vào chooseCards nếu AI cần thông tin này
                    cardsToPlay = aiPlayer.chooseCards(lastPlayedCards, isFirstTurnOfGame); // Giả sử AIPlayer.chooseCards không cần isFirstTurnOfGame
                                                                       // Nếu cần, bạn phải thêm tham số đó vào AIPlayer.java
                    if (cardsToPlay == null || cardsToPlay.isEmpty()) {
                        passedTurn = true;
                    } else {
                        // Kiểm tra 3 bích cho AI ở lượt đầu
                        if (isFirstTurnOfGame) {
                            Card threeSpadesCard = new Card(Card.Suit.SPADES, Card.Rank.THREE);
                            if (!cardsToPlay.contains(threeSpadesCard)) {
                                notifyMessageReceived(aiPlayer.getName() + " (AI) lỗi: Không đánh 3 Bích ở lượt đầu. Tự động bỏ lượt.");
                                passedTurn = true; // AI mắc lỗi, coi như bỏ lượt
                                cardsToPlay.clear(); // Xóa bài AI đã chọn sai
                            }
                        }
                        if (!passedTurn && !isValidPlay(cardsToPlay)) {
                            notifyMessageReceived(aiPlayer.getName() + " (AI) chọn bài không hợp lệ. Tự động bỏ lượt.");
                            passedTurn = true;
                            cardsToPlay.clear();
                        }
                    }
                }
            } else { // Người chơi thường
                setCurrentState(GameState.WAITING_FOR_PLAYER_INPUT);
                cardsToPlay = getPlayerInput();

                if (cardsToPlay == null) { // Bị gián đoạn từ getPlayerInput
                    passedTurn = true;
                    // notifyMessageReceived(currentPlayer.getName() + " bị gián đoạn, bỏ lượt."); // getPlayerInput đã thông báo
                } else if (cardsToPlay.isEmpty()) { // Người chơi bấm Pass
                    passedTurn = true;
                } else { // Người chơi chọn bài để đánh
                    // Kiểm tra 3 bích cho người chơi ở lượt đầu
                    if (isFirstTurnOfGame) {
                        Card threeSpadesCard = new Card(Card.Suit.SPADES, Card.Rank.THREE);
                        if (!cardsToPlay.contains(threeSpadesCard)) {
                            notifyMessageReceived("Lượt đầu tiên phải đánh bài có 3 Bích!");
                            continue; // Giữ lượt cho người chơi chọn lại
                        }
                    }
                    if (!isValidPlay(cardsToPlay)) {
                        notifyMessageReceived("Bài đánh không hợp lệ! Hãy thử lại.");
                        continue; // Giữ lượt cho người chơi chọn lại
                    }
                }
            }

            // --- Phần xử lý lượt đi (passedTurn hoặc đánh bài) ---
            if (passedTurn) {
                boolean canPassThisTurn = true;
                if (lastPlayedCards.isEmpty()) { // Đầu vòng mới
                    if (isFirstTurnOfGame && currentPlayer.getHand().contains(new Card(Card.Suit.SPADES, Card.Rank.THREE))) {
                        if(!currentPlayer.isAI()){
                            notifyMessageReceived("Bạn phải đánh 3 Bích trong lượt đầu tiên của game!");
                            canPassThisTurn = false;
                        }
                        // AI đã được xử lý ở trên, nếu AI pass ở đây nghĩa là nó được phép (ví dụ bị lỗi không có 3 bích)
                    } else if (!isFirstTurnOfGame) { // Đầu vòng mới, không phải lượt đầu game
                        if(!currentPlayer.isAI()){
                             notifyMessageReceived("Bạn không thể bỏ lượt khi là người đi đầu vòng mới!");
                             canPassThisTurn = false;
                        }
                        // AI được phép pass nếu logic của nó cho vậy
                    }
                }

                if (!canPassThisTurn) {
                    continue; // Quay lại chờ input của người chơi hiện tại
                }

                notifyPlayerPassed(currentPlayer);
                passCount++;
                // playerWhoPlayedLastValidCards không đổi nếu người hiện tại pass

                long activePlayersCount = players.stream().filter(p -> !p.hasNoCards()).count();
                if (playerWhoPlayedLastValidCards != null && passCount >= activePlayersCount -1 && activePlayersCount > 0) { // Đảm bảo activePlayersCount > 0
                    notifyMessageReceived("Vòng mới! " + playerWhoPlayedLastValidCards.getName() + " sẽ đi trước.");
                    currentPlayerIndex = players.indexOf(playerWhoPlayedLastValidCards);
                    roundStarterIndex = currentPlayerIndex;
                    lastPlayedCards.clear();
                    lastPlayer = null; // Reset lastPlayer cho vòng mới
                    passCount = 0;
                    // playerWhoPlayedLastValidCards giữ nguyên là người bắt đầu vòng mới
                    isFirstTurnOfGame = false;
                    if (!players.get(roundStarterIndex).hasNoCards()){ // Chỉ thông báo nếu người đó còn bài
                        notifyRoundStarted(players.get(roundStarterIndex));
                    } else { // Người thắng vòng đã hết bài, tìm người kế tiếp
                        moveToNextPlayer();
                        if(!checkGameOver()){
                            lastPlayedCards.clear(); // Vòng mới thực sự
                            lastPlayer = null;
                            passCount = 0;
                            playerWhoPlayedLastValidCards = players.get(currentPlayerIndex);
                            roundStarterIndex = currentPlayerIndex;
                            notifyRoundStarted(players.get(roundStarterIndex));
                        }
                    }
                } else if (activePlayersCount <=1 && playerWhoPlayedLastValidCards == null && lastPlayedCards.isEmpty()){
                     // Trường hợp chỉ còn 1 người, hoặc không ai có thể đánh
                     if(!checkGameOver()) {
                         moveToNextPlayer();
                         // Nếu vẫn không game over, reset vòng
                         if(!checkGameOver() && players.get(currentPlayerIndex).hasNoCards() == false){
                            lastPlayedCards.clear();
                            lastPlayer = null;
                            passCount = 0;
                            playerWhoPlayedLastValidCards = players.get(currentPlayerIndex);
                            roundStarterIndex = currentPlayerIndex;
                            notifyRoundStarted(players.get(roundStarterIndex));
                         }
                     }
                }
                else {
                    moveToNextPlayer();
                }
            } else { // Người chơi đánh bài (cardsToPlay không rỗng và hợp lệ)
                currentPlayer.removeCards(cardsToPlay);
                lastPlayedCards = new ArrayList<>(cardsToPlay);
                lastPlayer = currentPlayer;
                playerWhoPlayedLastValidCards = currentPlayer;
                passCount = 0;
                boolean wasFirstTurn = isFirstTurnOfGame; // Lưu lại trạng thái trước khi thay đổi
                isFirstTurnOfGame = false;

                notifyCardsPlayed(currentPlayer, cardsToPlay, lastPlayedCards);

                if (currentPlayer.hasNoCards()) {
                    currentPlayer.setHasNoCards(true);
                    if (!winners.contains(currentPlayer)) {
                         currentPlayer.setWinnerRank(currentWinnerRank);
                         winners.add(currentPlayer);
                         currentWinnerRank++;
                    }
                    notifyPlayerEliminated(currentPlayer);
                    if (checkGameOver()) {
                        setGeneralGameState(GeneralGameState.GAME_OVER);
                        break;
                    }
                    // Nếu người vừa đánh hết bài và thắng vòng luôn
                    long activePlayersLeft = players.stream().filter(p -> !p.hasNoCards()).count();
                    if (activePlayersLeft > 0) {
                        // Tìm người kế tiếp để bắt đầu vòng mới (nếu người hết bài là người cuối cùng đánh hợp lệ)
                        // Hoặc chỉ đơn giản là chuyển lượt
                        int originalPlayerIndex = players.indexOf(currentPlayer); // Người vừa hết bài
                        moveToNextPlayer(); // Chuyển sang người tiếp theo trước
                        // Nếu người tiếp theo là người mới (không phải người vừa hết bài)
                        // và người hết bài là người đánh cuối cùng của vòng
                        if ( (currentPlayerIndex != originalPlayerIndex || activePlayersLeft == 1) && playerWhoPlayedLastValidCards == currentPlayer) {
                            if (!checkGameOver()) {
                                notifyMessageReceived("Vòng mới! " + players.get(currentPlayerIndex).getName() + " sẽ đi trước.");
                                roundStarterIndex = currentPlayerIndex;
                                lastPlayedCards.clear();
                                lastPlayer = null;
                                passCount = 0;
                                playerWhoPlayedLastValidCards = players.get(roundStarterIndex); // Người mới bắt đầu vòng
                                notifyRoundStarted(players.get(roundStarterIndex));
                            }
                        }
                        // Nếu không phải trường hợp trên, moveToNextPlayer() đã đủ
                    }
                } else {
                    moveToNextPlayer();
                }
            }

            if (checkGameOver()) { // Kiểm tra lại ở cuối mỗi lượt
                setGeneralGameState(GeneralGameState.GAME_OVER);
                break;
            }

            try {
                if (getGeneralGameState() == GeneralGameState.RUNNING) {
                    TimeUnit.MILLISECONDS.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                notifyMessageReceived("Vòng lặp game bị gián đoạn.");
                setGeneralGameState(GeneralGameState.GAME_OVER);
                break;
            }
        } // Kết thúc while

        if (isFinished || getGeneralGameState() == GeneralGameState.GAME_OVER) {
        	System.out.println("TienLenGame: Game state set to GAME_OVER");
            List<Player> finalWinners = determineWinners();
            if (finalWinners.size() < players.size()) {
                for (Player p : players) {
                    if (p.getWinnerRank() == 0 && !p.hasNoCards()) {
                        p.setWinnerRank(currentWinnerRank); // Gán hạng cuối cho người thua
                        if (!finalWinners.contains(p)) finalWinners.add(p);
                    }
                }
                 Collections.sort(finalWinners, Comparator.comparingInt(Player::getWinnerRank));
            }
            notifyGameOver(finalWinners);
            if (onGameEndCallback != null) {
                 // Đảm bảo callback được chạy trên luồng JavaFX nếu nó tương tác với UI
                javafx.application.Platform.runLater(onGameEndCallback);
            }
        }
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

    public void addWinner(Player player) { // Phương thức này có vẻ không còn dùng trực tiếp trong runGameLoop mới
        if (!winners.contains(player)) {
            player.setWinnerRank(currentWinnerRank);
            winners.add(player);
            currentWinnerRank++;
            notifyMessageReceived(player.getName() + " về đích thứ " + player.getWinnerRank() + "!");
        }
    }

    private void moveToNextPlayer() {
        if (players.stream().filter(p -> !p.hasNoCards()).count() <= 1 && !isFinished) {
            // Nếu chỉ còn 1 hoặc 0 người chơi còn bài, game nên kết thúc
            checkGameOver(); // Gọi để cập nhật isFinished và generalState
            return;
        }

        int originalPlayerIndex = currentPlayerIndex;
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            if (currentPlayerIndex == originalPlayerIndex) { // Đã quay lại người cũ
                if (players.get(currentPlayerIndex).hasNoCards()) {
                    // Tất cả người chơi còn lại (nếu có) đều đã hết bài, hoặc chỉ còn 1 người.
                    // Game nên kết thúc.
                    if(!checkGameOver()) { // Gọi checkGameOver để cập nhật trạng thái isFinished và generalState
                         setGeneralGameState(GeneralGameState.GAME_OVER); // Nếu checkGameOver không làm, ép kết thúc
                    }
                }
                break; // Thoát vòng lặp dù người đó còn bài hay không, để tránh kẹt
            }
        } while (players.get(currentPlayerIndex).hasNoCards());
    }

    private void findStartingPlayer() {
        Card threeSpadesCard = new Card(Card.Suit.SPADES, Card.Rank.THREE); // Sử dụng 3 Bích
        boolean found = false;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getHand().contains(threeSpadesCard)) {
                currentPlayerIndex = i;
                roundStarterIndex = i;
                notifyMessageReceived(players.get(i).getName() + " có 3 Bích. Họ sẽ đi đầu!");
                found = true;
                return;
            }
        }
        if (!found) {
            currentPlayerIndex = 0;
            roundStarterIndex = 0;
            notifyMessageReceived("Không tìm thấy 3 Bích. Người chơi đầu tiên (" + players.get(0).getName() + ") sẽ đi đầu.");
        }
    }

    @Override
    public String getGameStateDisplay() {
        // ... (Giữ nguyên hoặc tùy chỉnh)
        return "Trạng thái game...";
    }

    @Override
    public boolean checkGameOver() {
        if (isFinished) return true; // Tránh xử lý lặp lại nếu đã kết thúc

        long activePlayersCount = players.stream().filter(p -> !p.hasNoCards()).count();
        if (activePlayersCount <= 1) {
            isFinished = true;
            // Gán hạng cho người cuối cùng nếu cần
            if (activePlayersCount == 1) {
                Player lastOne = players.stream().filter(p -> !p.hasNoCards()).findFirst().orElse(null);
                if (lastOne != null && lastOne.getWinnerRank() == 0) {
                    lastOne.setWinnerRank(winners.size() + 1); // Hạng cuối
                    if(!winners.contains(lastOne)) winners.add(lastOne);
                }
            }
            // generalState sẽ được đặt là GAME_OVER trong runGameLoop khi isFinished là true
            return true;
        }
        return false;
    }

    @Override
    protected List<Player> determineWinners() {
        // Đảm bảo tất cả người chơi hết bài đều có hạng
        for(Player p : players){
            if(p.hasNoCards() && p.getWinnerRank() == 0 && !winners.contains(p)){
                // Trường hợp này không nên xảy ra nếu logic gán hạng trong runGameLoop là đúng
                // nhưng đây là một biện pháp an toàn.
                p.setWinnerRank(currentWinnerRank++); // Gán hạng nếu chưa có
                winners.add(p);
            }
        }
        // Sắp xếp người thắng theo hạng
        winners.sort(Comparator.comparingInt(Player::getWinnerRank)
                               .thenComparing(Player::getName)); // Sắp xếp thêm theo tên nếu cùng hạng
        return new ArrayList<>(winners);
    }

    @Override
    public boolean canPass(Player player) {
        // Luật: không được bỏ lượt nếu là người bắt đầu vòng mới (lastPlayedCards rỗng)
        // VÀ không phải lượt đầu tiên của game (vì lượt đầu có 3 bích bắt buộc)
        if (lastPlayedCards.isEmpty()) {
            return isFirstTurnOfGame; // Được phép pass ở lượt đầu game nếu không có 3 bích (findStartingPlayer sẽ chọn người khác)
                                      // Hoặc nếu AI không có 3 bích (lỗi logic AI)
                                      // Người chơi human sẽ bị chặn ở GUI/runGameLoop nếu có 3 bích mà pass.
        }
        return true; // Các trường hợp khác được phép pass
    }

    @Override
    public int getPassCount() {
        return passCount;
    }

    @Override
    public void stopGameLoop() {
        setGeneralGameState(GeneralGameState.GAME_OVER); // Đặt isFinished cũng sẽ dừng vòng lặp
        isFinished = true; // Cách khác để dừng vòng lặp
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
            try {
                gameThread.join(500); // Chờ tối đa 0.5 giây
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public List<Card> getLastPlayedCards() {
        return new ArrayList<>(lastPlayedCards); // Trả về bản sao để tránh sửa đổi từ bên ngoài
    }

    @Override
    public Player getLastPlayer() {
        return lastPlayer;
    }

    @Override
    public boolean isValidPlay(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return false; // Đánh rỗng không hợp lệ (pass được xử lý riêng)

        // Xử lý lượt đầu tiên của game (phải có 3 Bích)
        if (isFirstTurnOfGame) {
            Card threeSpadesCard = new Card(Card.Suit.SPADES, Card.Rank.THREE);
            if (!cards.contains(threeSpadesCard)) {
                return false; // Lượt đầu phải chứa 3 Bích
            }
            // Và phải là một tổ hợp hợp lệ chứa 3 Bích
            return ruleSet.isValidCombination(cards);
        }

        // Các lượt tiếp theo
        if (lastPlayedCards.isEmpty()) { // Bắt đầu vòng mới
            return ruleSet.isValidCombination(cards);
        } else { // Đánh theo người khác
            return ruleSet.canPlayAfter(cards, lastPlayedCards);
        }
    }

    @Override
    public void setPlayerInput(List<Card> cards) {
        synchronized (playerInputLock) {
            this.playerInputCards = cards; // GUI gửi một ArrayList mới
            playerInputLock.notifyAll();
        }
    }

    private List<Card> getPlayerInput() {
        List<Card> inputToReturn;
        synchronized (playerInputLock) {
            // Cờ waitingForHumanInput nên được set bởi GUI hoặc logic gọi getPlayerInput
            // ở đây, nó được set lại ở đầu mỗi lượt người chơi trong runGameLoop
            // setCurrentState(GameState.WAITING_FOR_PLAYER_INPUT); // Đã làm ở runGameLoop

            while (playerInputCards == null && getGeneralGameState() == GeneralGameState.RUNNING && !Thread.currentThread().isInterrupted()) {
                try {
                    playerInputLock.wait(1000); // Chờ với timeout để kiểm tra trạng thái game
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    notifyMessageReceived(getCurrentPlayer().getName() + " bị gián đoạn khi chờ input.");
                    return null; // Trả về null nếu bị gián đoạn
                }
            }
            if (playerInputCards == null) { // Timeout hoặc thread bị interrupt mà không có input
                return null; // Hoặc một danh sách rỗng nếu muốn coi là pass
            }
            inputToReturn = new ArrayList<>(playerInputCards); // Tạo bản sao
            playerInputCards = null; // Reset cho lần chờ input tiếp theo
        }
        return inputToReturn;
    }

}
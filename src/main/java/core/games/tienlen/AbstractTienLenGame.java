// File: core/games/tienlen/AbstractTienLenGame.java
package core.games.tienlen;

import core.*;
import core.games.tienlen.components.PlayValidator;
import core.games.tienlen.components.RoundManager;
import core.games.tienlen.components.TurnProcessor;
import core.games.tienlen.tienlenplayer.TienLenPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTienLenGame<R extends TienLenVariantRuleSet> extends Game<R> implements Runnable, TienLenGameContext {

    protected final TienLenState tienLenState; // Đối tượng quản lý trạng thái Tiến Lên
    private volatile List<Card> playerInputCardsInternal; // Giữ lại cơ chế input
    private final Object playerInputLock = new Object();
    protected final long aiDelaySecondsInternal;

    protected final TurnProcessor turnProcessor;
    protected final PlayValidator playValidator;
    protected final RoundManager roundManager;

    public AbstractTienLenGame(String gameName, List<TienLenPlayer> players, Deck deck, R ruleSet, long aiDelay) {
        super(gameName, players, deck, ruleSet);
        this.tienLenState = new TienLenState(); // Khởi tạo đối tượng trạng thái
        this.aiDelaySecondsInternal = aiDelay;
        this.playerInputCardsInternal = null;

        this.turnProcessor = new TurnProcessor(this, this.aiDelaySecondsInternal);
        this.playValidator = new PlayValidator(this);
        this.roundManager = new RoundManager(this);
    }

    // --- Implement TienLenGameContext bằng cách ủy nhiệm cho tienLenState ---
    @Override public List<Card> getLastPlayedCards() { return tienLenState.getLastPlayedCards(); }
    @Override public void setLastPlayedCards(List<Card> cards) { tienLenState.setLastPlayedCards(cards); }
    @Override public TienLenPlayer getLastPlayer() { return tienLenState.getLastPlayer(); }
    @Override public void setLastPlayer(TienLenPlayer player) { tienLenState.setLastPlayer(player); }
    @Override public int getPassCount() { return tienLenState.getPassCount(); }
    @Override public void incrementPassCount() { tienLenState.incrementPassCount(); }
    @Override public void resetPassCount() { tienLenState.resetPassCount(); }
    @Override public boolean isFirstTurnOfGame() { return tienLenState.isFirstTurnOfGame(); }
    @Override public void setFirstTurnOfGame(boolean isFirst) { tienLenState.setFirstTurnOfGame(isFirst); }
    @Override public TienLenPlayer getPlayerWhoPlayedLastValidCards() { return tienLenState.getPlayerWhoPlayedLastValidCards(); }
    @Override public void setPlayerWhoPlayedLastValidCards(TienLenPlayer player) { tienLenState.setPlayerWhoPlayedLastValidCards(player); }
    @Override public List<TienLenPlayer> getPlayers() { return super.players; }
    @Override public TienLenPlayer getCurrentPlayer() { return super.getCurrentPlayer(); }
    @Override public int getCurrentPlayerIndex() { return super.currentPlayerIndex; }
    @Override public void setCurrentPlayerByIndex(int index) { super.currentPlayerIndex = index; }
    @Override public R getRuleSet() { return super.ruleSet; }
    @Override public List<TienLenPlayer> getWinners() { return tienLenState.getWinners(); }
    @Override public int getCurrentWinnerRank() { return tienLenState.getCurrentWinnerRank(); }
    @Override public void addWinner(TienLenPlayer winner, int rank) { tienLenState.addWinner(winner, rank); }
    @Override public TienLenGameState getCurrentTienLenState() { return tienLenState.getCurrentTienLenGameState(); }
    @Override public void setCurrentTienLenState(TienLenGameState newState) {
        tienLenState.setCurrentTienLenGameState(newState);
        notifyGameStateUpdated(); // Vẫn gọi notify từ Game
    }

    // Các hàm notify vẫn được gọi từ AbstractTienLenGame (lớp cha Game)
    @Override public void notifyMessage(String message) { super.notifyMessageReceived(message); }
    @Override public void notifyPlayerTurnStarted(TienLenPlayer player) { super.notifyPlayerTurnStarted(player); }
    @Override public void notifyCardsPlayed(TienLenPlayer p, List<Card> cards, List<Card> lastPlayed) { super.notifyCardsPlayed(p, cards, lastPlayed); }
    @Override public void notifyPlayerPassed(TienLenPlayer p) { super.notifyPlayerPassed(p); }
    @Override public void notifyRoundStarted(TienLenPlayer p) { super.notifyRoundStarted(p); }
    @Override public void notifyPlayerEliminated(TienLenPlayer p) { super.notifyPlayerEliminated(p); }

    // Giữ lại logic input
    @Override public List<Card> getHumanInputSynchronously() { /* ... như cũ ... */
        synchronized (playerInputLock) {
            while (playerInputCardsInternal == null && getGeneralGameState() == GeneralGameState.RUNNING && !Thread.currentThread().isInterrupted()) {
                try { playerInputLock.wait(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); notifyMessageReceived(getCurrentPlayer().getName() + " bị gián đoạn."); return null; }
            }
            return playerInputCardsInternal != null ? new ArrayList<>(playerInputCardsInternal) : null;
        }
    }
    @Override public void clearHumanInput() { synchronized (playerInputLock) { playerInputCardsInternal = null; } }
    @Override public void setPlayerInput(List<Card> cards) { synchronized (playerInputLock) { this.playerInputCardsInternal = (cards != null) ? new ArrayList<>(cards) : new ArrayList<>(); playerInputLock.notifyAll(); } }


    // --- Các phương thức vòng đời game ---
    @Override
    public void dealCards() {
        deck.reset(); deck.shuffle();
        for (TienLenPlayer player : players) {
            player.getHand().clear(); player.setHasNoCards(false); player.setWinnerRank(0);
        }
        tienLenState.clearWinnersAndRank(); // Reset winners và rank trong tienLenState
        tienLenState.setLastPlayedCards(new ArrayList<>()); // Reset trạng thái vòng
        tienLenState.setLastPlayer(null);
        tienLenState.resetPassCount();
        tienLenState.setPlayerWhoPlayedLastValidCards(null);


        int cardsPerPlayer = 13;
        for (int i = 0; i < cardsPerPlayer; i++) {
            for (TienLenPlayer player : players) {
                Card card = deck.drawCard(); if (card != null) player.addCard(card);
            }
        }
        for (TienLenPlayer player : players) { player.sortHand(ruleSet.getCardComparator()); }

        findStartingPlayerOfGameVariant(); // Gọi phương thức trừu tượng (lớp con cài đặt)
        
        tienLenState.setFirstTurnOfGame(true); // Đặt lại cờ
        setCurrentTienLenState(TienLenGameState.ROUND_IN_PROGRESS); // Sử dụng setter của context
        notifyMessage("Đã chia bài xong. " + getCurrentPlayer().getName() + " đi trước!");
    }

    @Override
    public void resetGame() {
        stopGameLoop();
        super.setGeneralGameState(Game.GeneralGameState.INITIALIZING);
        this.tienLenState.resetForNewGame(); // Gọi hàm reset của tienLenState
        // Reset các trạng thái của Player mà tienLenState không quản lý trực tiếp
        for (TienLenPlayer p : players) {
             p.getHand().clear(); p.setHasNoCards(false); p.clearWinnerRank();
        }
        deck.reset(); // Reset bộ bài
        this.playerInputCardsInternal = null; // Clear input cũ

        dealCards();

        super.setGeneralGameState(Game.GeneralGameState.RUNNING);
        this.isFinished = false;
        notifyMessage("Bắt đầu ván mới!");
        notifyGameStateUpdated();
    }

    // runGameLoop, startGame, stopGameLoop, moveToNextActivePlayer, finalizeGameExecution,
    // getGameStateDisplay, checkGameOver, determineWinners, isValidPlay, canPass
    // về cơ bản giữ nguyên logic như phiên bản "đầy đủ" của TienLenGame bạn đã có,
    // chỉ khác là chúng sẽ tương tác với `this.tienLenState` thông qua các getter/setter
    // (mà `this` cũng chính là `TienLenGameContext` nên các component cũng tương tác đúng).
    // Ví dụ, thay vì `this.lastPlayedCardsInternal` thì dùng `this.tienLenState.getLastPlayedCards()` hoặc `this.getLastPlayedCards()` (qua context).

    @Override public void run() { runGameLoop(); }

    @Override
    public void runGameLoop() {
        while (getGeneralGameState() == GeneralGameState.RUNNING && !isFinished) {
            TienLenPlayer currentPlayer = getCurrentPlayer();
            if (currentPlayer.hasNoCards()) {
                moveToNextActivePlayer();
                if (checkGameOver()) { setGeneralGameState(GeneralGameState.GAME_OVER); break; }
                continue;
            }

            if (currentPlayer.isAI()) setCurrentTienLenState(TienLenGameState.AI_THINKING);
            else setCurrentTienLenState(TienLenGameState.WAITING_FOR_PLAYER_INPUT);
            notifyPlayerTurnStarted(currentPlayer);

            List<Card> cardsAttempted = turnProcessor.getPlayerAction(currentPlayer);

            if (cardsAttempted == null || cardsAttempted.isEmpty()) {
                if (playValidator.canPlayerPass(currentPlayer)) {
                    roundManager.processPassedTurn(currentPlayer);
                    if (roundManager.manageRoundEndAndNewRound()) { /* New round started by RM */ }
                    else { moveToNextActivePlayer(); }
                } else {
                    if (currentPlayer.isAI()) {
                        notifyMessage(currentPlayer.getName() + " (AI) bỏ lượt không hợp lệ. Mất lượt.");
                        roundManager.processPassedTurn(currentPlayer);
                        if (roundManager.manageRoundEndAndNewRound()) {} else { moveToNextActivePlayer(); }
                    }
                    continue;
                }
            } else {
                if (playValidator.isValidPlayForCurrentContext(cardsAttempted, currentPlayer)) {
                    roundManager.processPlayedCards(currentPlayer, cardsAttempted);
                    if (currentPlayer.hasNoCards()) {
                        roundManager.handlePlayerFinish(currentPlayer);
                    }
                    if (roundManager.manageRoundEndAndNewRound()) { /* New round */ }
                    else if (!currentPlayer.hasNoCards()) { moveToNextActivePlayer(); }
                    else if (currentPlayer.hasNoCards() && players.stream().filter(p -> !p.hasNoCards()).count() > 0) {
                        moveToNextActivePlayer();
                    }
                } else {
                    if (currentPlayer.isAI()) {
                        notifyMessage(currentPlayer.getName() + " (AI) chọn bài không hợp lệ. Bỏ lượt.");
                        roundManager.processPassedTurn(currentPlayer);
                        if (roundManager.manageRoundEndAndNewRound()) {} else { moveToNextActivePlayer(); }
                    }
                    continue;
                }
            }
            if (checkGameOver()) { setGeneralGameState(GeneralGameState.GAME_OVER); break; }
            try { if (getGeneralGameState() == GeneralGameState.RUNNING) TimeUnit.MILLISECONDS.sleep(100); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); notifyMessage("Vòng lặp game bị gián đoạn."); setGeneralGameState(GeneralGameState.GAME_OVER); break; }
        }
        finalizeGameExecution();
    }

     protected void moveToNextActivePlayer() {
        if (players.stream().filter(p -> !p.hasNoCards()).count() <= 1 && !isFinished) {
            if(checkGameOver()) setGeneralGameState(GeneralGameState.GAME_OVER); return;
        }
        int originalCI = getCurrentPlayerIndex(); int currentCI = originalCI;
        do {
            currentCI = (currentCI + 1) % players.size();
            if (currentCI == originalCI) { if (players.get(currentCI).hasNoCards()) if(checkGameOver()) setGeneralGameState(GeneralGameState.GAME_OVER); break; }
        } while (players.get(currentCI).hasNoCards());
        setCurrentPlayerByIndex(currentCI);
    }

    @Override
    public void stopGameLoop() {
        setGeneralGameState(GeneralGameState.GAME_OVER); this.isFinished = true;
        if (this.gameThread != null && this.gameThread.isAlive()) { this.gameThread.interrupt(); try { this.gameThread.join(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }}
    }

    protected void finalizeGameExecution() {
        if (this.isFinished || getGeneralGameState() == GeneralGameState.GAME_OVER) {
            List<TienLenPlayer> finalWinnersList = determineWinners();
             if (finalWinnersList.size() < players.size()) { // Gán hạng cho người thua
                for (TienLenPlayer p : players) { if (p.getWinnerRank() == 0 && !p.hasNoCards()) addWinner(p, getCurrentWinnerRank()); } // getCurrentWinnerRank() sẽ lấy từ tienLenState
                // Sắp xếp lại winners sau khi thêm người thua
                finalWinnersList = new ArrayList<>(tienLenState.getWinners()); // Lấy danh sách winners mới nhất
                Collections.sort(finalWinnersList, Comparator.comparingInt(TienLenPlayer::getWinnerRank).thenComparing(TienLenPlayer::getName));
            }
            super.notifyGameOver(finalWinnersList);
            if (super.onGameEndCallback != null) javafx.application.Platform.runLater(super.onGameEndCallback);
        }
    }

    @Override public String getGameStateDisplay() { if (getCurrentPlayer() != null) return "Lượt của: " + getCurrentPlayer().getName(); return "Game đang tải..."; }
    @Override public boolean checkGameOver() {
        if (this.isFinished) return true;
        long activePlayersCount = players.stream().filter(p -> !p.hasNoCards()).count();
        if (activePlayersCount <= 1) { this.isFinished = true;
            if (activePlayersCount == 1) { TienLenPlayer lastOne = players.stream().filter(p -> !p.hasNoCards()).findFirst().orElse(null); if (lastOne != null && lastOne.getWinnerRank() == 0) addWinner(lastOne, getCurrentWinnerRank()); }
            return true;
        } return false;
    }
    @Override protected List<TienLenPlayer> determineWinners() {
        List<TienLenPlayer> finalSortedWinners = new ArrayList<>(tienLenState.getWinners());
        for(TienLenPlayer p : players){ if(!p.hasNoCards() && p.getWinnerRank() == 0 && !finalSortedWinners.contains(p)) addWinner(p, getCurrentWinnerRank()); }
        finalSortedWinners = new ArrayList<>(tienLenState.getWinners());
        Collections.sort(finalSortedWinners, Comparator.comparingInt(TienLenPlayer::getWinnerRank).thenComparing(TienLenPlayer::getName));
        return finalSortedWinners;
    }
    @Override public boolean isValidPlay(List<Card> cards) { return ruleSet.isValidCombination(cards); }
    @Override public boolean canPass(TienLenPlayer player) {
        if (getLastPlayedCards().isEmpty()) { // Sử dụng getter của context
            if (isFirstTurnOfGame() && player.getHand().contains(new Card(Card.Suit.SPADES, Card.Rank.THREE))) return false;
            if(!isFirstTurnOfGame() && !player.isAI()) return false;
        } return true;
    }

    // --- Phương thức trừu tượng cho biến thể game ---
    protected abstract void findStartingPlayerOfGameVariant();
}
package core.games.tienlen.components;


import core.Card;
import core.games.tienlen.TienLenGameContext;
import core.games.tienlen.tienlenplayer.TienLenPlayer;

import java.util.ArrayList;
import java.util.List;

public class RoundManager {
    private final TienLenGameContext gameContext;

    public RoundManager(TienLenGameContext gameContext) {
        this.gameContext = gameContext;
    }

    public void processPlayedCards(TienLenPlayer currentPlayer, List<Card> cardsToPlay) {
        currentPlayer.removeCards(cardsToPlay);
        gameContext.setLastPlayedCards(new ArrayList<>(cardsToPlay));
        gameContext.setLastPlayer(currentPlayer);
        gameContext.setPlayerWhoPlayedLastValidCards(currentPlayer);
        gameContext.resetPassCount();
        if (gameContext.isFirstTurnOfGame()) {
            gameContext.setFirstTurnOfGame(false);
        }
        gameContext.notifyCardsPlayed(currentPlayer, cardsToPlay, gameContext.getLastPlayedCards());
    }

    public void processPassedTurn(TienLenPlayer currentPlayer) {
        gameContext.notifyPlayerPassed(currentPlayer);
        gameContext.incrementPassCount();
        // playerWhoPlayedLastValidCards không đổi
    }

    public boolean manageRoundEndAndNewRound() {
        long activePlayersCount = gameContext.getPlayers().stream().filter(p -> !p.hasNoCards()).count();
        TienLenPlayer lastValidPlayer = gameContext.getPlayerWhoPlayedLastValidCards();

        if (lastValidPlayer != null && gameContext.getPassCount() >= (activePlayersCount - 1) && activePlayersCount > 0) {
            // Nếu tất cả người chơi còn lại (trừ người đánh hợp lệ cuối cùng) đã bỏ lượt
            TienLenPlayer roundStarter = lastValidPlayer;
            if (roundStarter.hasNoCards()) { // Người thắng vòng đã hết bài
                // Cần tìm người kế tiếp để bắt đầu vòng mới
                // Logic này sẽ được xử lý bởi việc chọn next player sau đó
                // Tạm thời, nếu người thắng vòng hết bài, người kế tiếp theo vòng sẽ bắt đầu
                // Điều này cần được làm rõ hơn trong logic chuyển người chơi
                // For now, we assume next player logic will handle this.
                // The round starter should be an active player.
                // This indicates the round ends, and the next turn will be a new round.
                gameContext.notifyMessage("Vòng chơi kết thúc!"); // Thông báo chung
                startNewRoundForNextAvailablePlayer(roundStarter);
            } else {
                startNewRound(roundStarter);
            }
            return true; // Vòng mới đã bắt đầu
        }
        return false; // Vòng hiện tại vẫn tiếp tục hoặc không có điều kiện bắt đầu vòng mới rõ ràng
    }
    
    private void startNewRoundForNextAvailablePlayer(TienLenPlayer previousRoundWinner) {
        List<TienLenPlayer> players = gameContext.getPlayers();
        int winnerIndex = players.indexOf(previousRoundWinner);
        int nextPlayerIndex = (winnerIndex + 1) % players.size();
        int loopCheck = 0;
        while(players.get(nextPlayerIndex).hasNoCards() && loopCheck < players.size()){
            nextPlayerIndex = (nextPlayerIndex + 1) % players.size();
            loopCheck++;
        }
        if(!players.get(nextPlayerIndex).hasNoCards()){
            startNewRound(players.get(nextPlayerIndex));
        } else {
            // Không còn ai để chơi, game nên kết thúc (logic này sẽ do checkGameOver xử lý)
            gameContext.notifyMessage("Không còn người chơi nào để bắt đầu vòng mới.");
        }
    }


    public void startNewRound(TienLenPlayer roundStarter) {
        if (roundStarter == null || roundStarter.hasNoCards()) return; // Không thể bắt đầu nếu người đó đã hết bài

        gameContext.notifyMessage("Vòng mới! " + roundStarter.getName() + " sẽ đi trước.");
        gameContext.setCurrentPlayerByIndex(gameContext.getPlayers().indexOf(roundStarter));
        // gameContext.setRoundStarterIndex(gameContext.getCurrentPlayerIndex()); // Nếu có biến này
        gameContext.setLastPlayedCards(new ArrayList<>()); // Xóa bài trên bàn
        gameContext.setLastPlayer(null);
        gameContext.resetPassCount();
        gameContext.setPlayerWhoPlayedLastValidCards(roundStarter); // Người bắt đầu vòng cũng là người đánh hợp lệ đầu tiên
        gameContext.setFirstTurnOfGame(false); // Chắc chắn không phải lượt đầu game nữa
        gameContext.notifyRoundStarted(roundStarter);
    }

    public void handlePlayerFinish(TienLenPlayer finishedPlayer) {
        if (!gameContext.getWinners().contains(finishedPlayer)) { // Tránh thêm nhiều lần
            finishedPlayer.setHasNoCards(true); // Đánh dấu người chơi đã hết bài
            gameContext.addWinner(finishedPlayer, gameContext.getCurrentWinnerRank());
            gameContext.notifyPlayerEliminated(finishedPlayer);
        }
    }

    public void findStartingPlayerOfGame() {
        List<TienLenPlayer> players = gameContext.getPlayers();
        Card threeSpadesCard = new Card(Card.Suit.SPADES, Card.Rank.THREE);
        int starterIdx = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getHand().contains(threeSpadesCard)) {
                starterIdx = i;
                break;
            }
        }
        if (starterIdx == -1) { // Không tìm thấy 3 bích (nên có cho ván đầu)
            starterIdx = 0; // Người đầu tiên trong danh sách đi trước
            gameContext.notifyMessage("Không tìm thấy 3 Bích. " + players.get(starterIdx).getName() + " sẽ đi đầu.");
        } else {
            gameContext.notifyMessage(players.get(starterIdx).getName() + " có 3 Bích. Họ sẽ đi đầu!");
        }
        gameContext.setCurrentPlayerByIndex(starterIdx);
        // gameContext.setRoundStarterIndex(starterIdx); // Nếu bạn có biến này
        gameContext.setPlayerWhoPlayedLastValidCards(players.get(starterIdx)); // Người đi đầu cũng là người "đánh hợp lệ" đầu tiên của vòng
    }
}
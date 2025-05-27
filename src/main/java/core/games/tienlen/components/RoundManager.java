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
    }

    public boolean manageRoundEndAndNewRound() {
        long activePlayersCount = gameContext.getPlayers().stream().filter(p -> !p.hasNoCards()).count();
        TienLenPlayer lastValidPlayer = gameContext.getPlayerWhoPlayedLastValidCards();

        if (lastValidPlayer != null && gameContext.getPassCount() >= (lastValidPlayer.hasNoCards() ? activePlayersCount : activePlayersCount - 1) && activePlayersCount > 0) {
            TienLenPlayer roundStarter = lastValidPlayer;
            if (roundStarter.hasNoCards()) {
                gameContext.notifyMessage("Vòng chơi kết thúc!");
                startNewRoundForNextAvailablePlayer(roundStarter);
            } else {
                startNewRound(roundStarter);
            }
            return true;
        }
        return false;
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
            gameContext.notifyMessage("Không còn người chơi nào để bắt đầu vòng mới.");
        }
    }


    public void startNewRound(TienLenPlayer roundStarter) {
        if (roundStarter == null || roundStarter.hasNoCards()) return;

        gameContext.notifyMessage("Vòng mới! " + roundStarter.getName() + " sẽ đi trước.");
        gameContext.setCurrentPlayerByIndex(gameContext.getPlayers().indexOf(roundStarter));
        gameContext.setLastPlayedCards(new ArrayList<>());
        gameContext.setLastPlayer(null);
        gameContext.resetPassCount();
        gameContext.setPlayerWhoPlayedLastValidCards(roundStarter);
        gameContext.setFirstTurnOfGame(false);
        gameContext.notifyRoundStarted(roundStarter);
    }

    public void handlePlayerFinish(TienLenPlayer finishedPlayer) {
        if (!gameContext.getWinners().contains(finishedPlayer)) {
            finishedPlayer.setHasNoCards(true);
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
        if (starterIdx == -1) {
            starterIdx = 0;
            gameContext.notifyMessage("Không tìm thấy 3 Bích. " + players.get(starterIdx).getName() + " sẽ đi đầu.");
        } else {
            gameContext.notifyMessage(players.get(starterIdx).getName() + " có 3 Bích. Họ sẽ đi đầu!");
        }
        gameContext.setCurrentPlayerByIndex(starterIdx);
        gameContext.setPlayerWhoPlayedLastValidCards(players.get(starterIdx));
    }
}
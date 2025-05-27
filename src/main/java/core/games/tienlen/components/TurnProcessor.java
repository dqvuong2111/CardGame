package core.games.tienlen.components;

import core.Card;
import core.ai.tienlenai.TienLenAI;
import core.games.tienlen.TienLenGameContext;
import core.games.tienlen.tienlenplayer.TienLenPlayer;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TurnProcessor {
    private final TienLenGameContext gameContext;
    private final long aiDelaySeconds;

    public TurnProcessor(TienLenGameContext gameContext, long aiDelaySeconds) {
        this.gameContext = gameContext;
        this.aiDelaySeconds = aiDelaySeconds;
    }

    public List<Card> getPlayerAction(TienLenPlayer currentPlayer) {
        List<Card> chosenCards = null;
        if (currentPlayer.isAI()) {
            gameContext.notifyMessage(currentPlayer.getName() + " (AI) đang suy nghĩ...");
            try {
                TimeUnit.SECONDS.sleep(aiDelaySeconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                gameContext.notifyMessage(currentPlayer.getName() + " (AI) bị gián đoạn khi suy nghĩ.");
                return null;
            }
            TienLenAI aiPlayer = (TienLenAI) currentPlayer;

            chosenCards = aiPlayer.chooseCards(
                gameContext.getLastPlayedCards(),
                gameContext.isFirstTurnOfGame()
            );

        } else {
            chosenCards = gameContext.getHumanInputSynchronously();
            gameContext.clearHumanInput();
        }
        return chosenCards;
    }
}
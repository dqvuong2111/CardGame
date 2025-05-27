package core.games.tienlen.components;

import core.Card;
import core.ai.tienlenai.TienLenAI;
import core.games.tienlen.TienLenGameContext; // Sử dụng TienLenGameContext
import core.games.tienlen.tienlenplayer.TienLenPlayer;

// import core.games.tienlen.TienLenVariantRuleSet; // Có thể không cần nếu RuleSet trong context đã đủ
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
                return null; // Trả về null (hoặc rỗng) để coi như bỏ lượt
            }
            TienLenAI aiPlayer = (TienLenAI) currentPlayer;

            chosenCards = aiPlayer.chooseCards(
                gameContext.getLastPlayedCards(),   // Bài đã đánh trên bàn
                gameContext.isFirstTurnOfGame()     // Cờ báo lượt đầu tiên của toàn game
            );

        } else { // Người chơi thường
            chosenCards = gameContext.getHumanInputSynchronously();
            gameContext.clearHumanInput();
        }
        return chosenCards;
    }
}
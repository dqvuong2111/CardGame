package core.games.tienlen.components;

import core.Card;
import core.games.tienlen.TienLenGameContext;
import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.tienlenplayer.TienLenPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayValidator {
    private final TienLenGameContext gameContext;
    private final TienLenVariantRuleSet ruleSet;

    public PlayValidator(TienLenGameContext gameContext) {
        this.gameContext = gameContext;
        this.ruleSet = gameContext.getRuleSet();
    }

    public boolean isValidPlayForCurrentContext(List<Card> cardsToPlay, TienLenPlayer currentPlayer) {
        if (cardsToPlay == null || cardsToPlay.isEmpty()) {
            return false;
        }

        if (gameContext.isFirstTurnOfGame()) {
            if (!ruleSet.hasStartingCard(cardsToPlay)) {
                gameContext.notifyMessage("Lượt đầu tiên phải đánh bài có 3 Bích!");
                return false;
            }
            if (!this.ruleSet.isValidCombination(cardsToPlay)) {
                 gameContext.notifyMessage("Tổ hợp bài không hợp lệ!");
                 return false;
            }
            return true;
        }

        if (gameContext.getLastPlayedCards().isEmpty()) {
            if (!this.ruleSet.isValidCombination(cardsToPlay)) {
                gameContext.notifyMessage("Tổ hợp bài không hợp lệ!");
                return false;
            }
        } else {
            if (!this.ruleSet.canPlayAfter(cardsToPlay, gameContext.getLastPlayedCards())) {
                gameContext.notifyMessage("Bài của bạn không thể đánh đè bài trên bàn!");
                return false;
            }
        }
        
     // --- LOGIC NGĂN CHẶN "THỐI 2" (KHÔNG ĐỂ LẠI TOÀN 2 TRÊN TAY MÀ KHÔNG ĐÁNH ĐƯỢC) ---
        List<Card> handAfterPlay = new ArrayList<>(currentPlayer.getHand());
        handAfterPlay.removeAll(cardsToPlay);

        if (!handAfterPlay.isEmpty()) {
            boolean remainingCardsAreAllTwos = true;
            for (Card card : handAfterPlay) {
            	if (ruleSet.getCardRankValue(card) != ruleSet.getTwoRankValue()) {
                    remainingCardsAreAllTwos = false;
                    break;
                }
            }

            if (remainingCardsAreAllTwos) {
            	return false;
            }
        } 
        
        return true;
    
    }

    public boolean canPlayerPass(TienLenPlayer currentPlayer) {
        if (gameContext.getLastPlayedCards().isEmpty()) {
            if (gameContext.isFirstTurnOfGame() && ruleSet.hasStartingCard(currentPlayer.getHand())) {
                if (!currentPlayer.isAI()) {
                    gameContext.notifyMessage("Bạn phải đánh 3 Bích trong lượt đầu tiên của game!");
                    return false;
                }
            } else if (!gameContext.isFirstTurnOfGame()) {
                if (!currentPlayer.isAI()) {
                    gameContext.notifyMessage("Bạn không thể bỏ lượt khi là người đi đầu vòng mới!");
                    return false;
                }
            }
        }
        return true;
    }
}
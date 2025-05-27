package core.games.tienlen.components;

import core.Card;
import core.games.tienlen.TienLenGameContext;
import core.games.tienlen.TienLenVariantRuleSet; // <<--- SỬ DỤNG INTERFACE NÀY
import core.games.tienlen.tienlenplayer.TienLenPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayValidator {
    private final TienLenGameContext gameContext;
    private final TienLenVariantRuleSet ruleSet; // <<--- LƯU TRỮ DƯỚI DẠNG INTERFACE

    public PlayValidator(TienLenGameContext gameContext) {
        this.gameContext = gameContext;
        this.ruleSet = gameContext.getRuleSet(); // <<--- GÁN TRỰC TIẾP, KHÔNG CẦN ÉP KIỂU
    }

    public boolean isValidPlayForCurrentContext(List<Card> cardsToPlay, TienLenPlayer currentPlayer) {
        if (cardsToPlay == null || cardsToPlay.isEmpty()) {
            return false;
        }

        // Sử dụng this.ruleSet (là TienLenVariantRuleSet)
        if (gameContext.isFirstTurnOfGame()) {
            // Logic kiểm tra 3 Bích có thể cần truy cập các hằng số/phương thức cụ thể.
            // Nếu TienLenVariantRuleSet không có phương thức để kiểm tra "lá bài bắt đầu",
            // bạn có thể cần kiểm tra kiểu và ép kiểu một cách cẩn thận, hoặc thêm phương thức đó vào interface.
            // Giả sử hasStartingCard là một phần của TienLenVariantRuleSet (kế thừa từ RuleSet)
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
        List<Card> handAfterPlay = new ArrayList<>(currentPlayer.getHand()); // Tạo bản sao tay bài hiện tại
        handAfterPlay.removeAll(cardsToPlay); // Loại bỏ những lá định đánh

        if (!handAfterPlay.isEmpty()) { // Nếu sau khi đánh, người chơi vẫn còn bài
            boolean remainingCardsAreAllTwos = true;
            for (Card card : handAfterPlay) {
                // Sử dụng TienLenMienNamRule.getTienLenValue(card) == 15 để kiểm tra quân 2
            	if (ruleSet.getCardRankValue(card) != ruleSet.getTwoRankValue()) {
                    remainingCardsAreAllTwos = false;
                    break;
                }
            }

            if (remainingCardsAreAllTwos) {
                // Đây là luật "không được đánh (ra) toàn 2 để thắng":
            	return false;
            } // Kết thúc if (remainingCardsAreAllTwos)
        } // Kết thúc if (!handAfterPlay.isEmpty())
        
        return true;
    
    }

    public boolean canPlayerPass(TienLenPlayer currentPlayer) {
        // Sử dụng this.ruleSet
        if (gameContext.getLastPlayedCards().isEmpty()) {
            // Kiểm tra 3 Bích cho lượt đầu game
            if (gameContext.isFirstTurnOfGame() && ruleSet.hasStartingCard(currentPlayer.getHand())) {
                // Nếu RuleSet có phương thức kiểm tra lá bài bắt buộc, dùng nó ở đây.
                // Giả sử hasStartingCard là một phần của RuleSet mà TienLenVariantRuleSet kế thừa
                // và nó được hiểu là "có lá bài bắt buộc phải đánh".
                // Tuy nhiên, logic canPlayerPass này đã xử lý đúng cho Human.
                if (!currentPlayer.isAI()) {
                    gameContext.notifyMessage("Bạn phải đánh 3 Bích trong lượt đầu tiên của game!");
                    return false;
                }
            } else if (!gameContext.isFirstTurnOfGame()) { // Mở đầu vòng mới (không phải lượt đầu game)
                if (!currentPlayer.isAI()) {
                    gameContext.notifyMessage("Bạn không thể bỏ lượt khi là người đi đầu vòng mới!");
                    return false;
                }
            }
        }
        return true;
    }
}
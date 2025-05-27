package core.ai.utils;

import java.util.ArrayList;
import java.util.List;

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;

public class RemainingCardsValidator {
		public static boolean checkRemainingCard(List<Card> currentHand, List<Card>cardsToPlay) {
			List<Card> handAfterPlay = new ArrayList<>(currentHand); // Tạo bản sao tay bài hiện tại
	        handAfterPlay.removeAll(cardsToPlay); // Loại bỏ những lá định đánh

	        if (!handAfterPlay.isEmpty()) { // Nếu sau khi đánh, người chơi vẫn còn bài
	            boolean remainingCardsAreAllTwos = true;
	            for (Card card : handAfterPlay) {
	                // Sử dụng TienLenMienNamRule.getTienLenValue(card) == 15 để kiểm tra quân 2
	            	if (card.getRank().getValue() == 2) {
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
}

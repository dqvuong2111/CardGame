package core.ai.helpers;

import java.util.ArrayList;
import java.util.List;

import core.Card;

public class RemainingCardsValidator {
		public static boolean checkRemainingCard(List<Card> currentHand, List<Card>cardsToPlay) {
			List<Card> handAfterPlay = new ArrayList<>(currentHand); 
	        handAfterPlay.removeAll(cardsToPlay); 
	        if(handAfterPlay.isEmpty()) return false;
	        if (!handAfterPlay.isEmpty()) { 
	            boolean remainingCardsAreAllTwos = true;
	            for (Card card : handAfterPlay) {
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

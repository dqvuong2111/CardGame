package core.games.samloc;

import core.Card;
import core.games.RuleSet;
import core.games.logic.CombinationLogic;
import core.games.logic.PlayabilityLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SamLocRule implements RuleSet {

    // Giữ lại Comparator và các hàm getTienLenBacValue của Miền Bắc
    private static final Comparator<Card> SAM_LOC_CARD_COMPARATOR = new SamLocCardComparator();
    public static int getTienLenValue(Card card) {
        if (card.getRank() == Card.Rank.TWO) return 15; 
        if (card.getRank() == Card.Rank.ACE) return 14; 
        if (card.getRank() == Card.Rank.KING) return 13; 
        if (card.getRank() == Card.Rank.QUEEN) return 12;
        if (card.getRank() == Card.Rank.JACK) return 11;
        return card.getRank().getValue(); 
    }
    
    private static class SamLocCardComparator implements Comparator<Card> {
        @Override
        public int compare(Card card1, Card card2) {
            int value1 = getTienLenValue(card1);
            int value2 = getTienLenValue(card2);
            if (value1 != value2) {
                return Integer.compare(value1, value2);
            }
            return 0;
        }
    }
    
    @Override
    public Comparator<Card> getCardComparator() {
        return SAM_LOC_CARD_COMPARATOR;
    }

    @Override
    public Object getCombinationIdentifier(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return CombinationType.INVALID;
        List<Card> sortedCards = new ArrayList<>(cards);
        Collections.sort(sortedCards, getCardComparator());

        RuleSet.CombinationType basicType = 
            CombinationLogic.getCombinationType(sortedCards, this);

        if (basicType == RuleSet.CombinationType.STRAIGHT) {

            if (sortedCards.size() < 3) return CombinationType.INVALID; 
  
            return CombinationType.STRAIGHT; 
        }
        
        return basicType;
    }

    @Override
    public boolean isValidCombination(List<Card> cards) {
        return getCombinationIdentifier(cards) != RuleSet.CombinationType.INVALID;
    }

    @Override
    public boolean canPlayAfter(List<Card> newCards, List<Card> previousCards) {
        boolean canPlayGenerally = PlayabilityLogic.canPlayAfter(newCards, previousCards, this);
        if (!canPlayGenerally) return false;

        RuleSet.CombinationType newType = (RuleSet.CombinationType) getCombinationIdentifier(newCards);
        RuleSet.CombinationType prevType = (RuleSet.CombinationType) getCombinationIdentifier(previousCards);

        if (newType != prevType) {
            return false;
        }

        List<Card> sortedNewCards = new ArrayList<>(newCards);
        Collections.sort(sortedNewCards, getCardComparator());
        List<Card> sortedPreviousCards = new ArrayList<>(previousCards);
        Collections.sort(sortedPreviousCards, getCardComparator());

        Card newRep = getRepresentativeCardForCombination(sortedNewCards);
        Card prevRep = getRepresentativeCardForCombination(sortedPreviousCards);

        if (newRep == null || prevRep == null) return false; 

        switch (newType) {
            case SINGLE:
                if (newCards.size() != 1 || previousCards.size() != 1) return false; 
                return getCardComparator().compare(newRep, prevRep) > 0;

            case PAIR:
                if (newCards.size() != 2 || previousCards.size() != 2) return false;
                
                return getCardComparator().compare(newRep, prevRep) > 0;

            case TRIPLE:
                if (newCards.size() != 3 || previousCards.size() != 3) return false;
                
                return getCardComparator().compare(newRep, prevRep) > 0;

            case STRAIGHT:
                if (sortedNewCards.size() != sortedPreviousCards.size()) {
                    return false;
                }
                return getCardComparator().compare(newRep, prevRep) > 0;
            
            default:
                return false; 
        }
    }
    
    @Override
    public Card getRepresentativeCardForCombination(List<Card> combination) {
        if (combination == null || combination.isEmpty()) return null;
        List<Card> sortedCombination = new ArrayList<>(combination);
        Collections.sort(sortedCombination, SAM_LOC_CARD_COMPARATOR); 
        return sortedCombination.get(sortedCombination.size() - 1);
    }

    @Override
    public boolean isCardValidInStraight(Card card) {
        return getTienLenValue(card) < getTwoRankValue(); 
    }

    @Override
    public int getCardRankValue(Card card) {
        return getTienLenValue(card);
    }

    @Override
    public int getTwoRankValue() {
        return 15; 
    }

	@Override
	public boolean hasStartingCard(List<Card> cards) {
		return true;
	}

	@Override
	public int getCardsPerPlayer() {
		return 10;
	}

	@Override
	public Card startingCard() {
		return null;
	}   
    
}
package core.games.tienlen.tienlenmienbac;

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.logic.TienLenCombinationLogic;
import core.games.tienlen.logic.TienLenPlayabilityLogic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TienLenMienBacRule implements TienLenVariantRuleSet {

    private static final Comparator<Card> TIEN_LEN_MIEN_BAC_CARD_COMPARATOR = new TienLenMienBacCardComparator();
    public static int getTienLenValue(Card card) {
        if (card.getRank() == Card.Rank.TWO) return 15; 
        if (card.getRank() == Card.Rank.ACE) return 14; 
        if (card.getRank() == Card.Rank.KING) return 13; 
        if (card.getRank() == Card.Rank.QUEEN) return 12;
        if (card.getRank() == Card.Rank.JACK) return 11;
        return card.getRank().getValue(); 
    }
    public static int getSuitOrderValue(Card.Suit suit) {
        return switch (suit) {
            case SPADES -> 1;   
            case CLUBS -> 2;    
            case DIAMONDS -> 3; 
            case HEARTS -> 4;   
        };
    }
    private static class TienLenMienBacCardComparator implements Comparator<Card> {
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
        return TIEN_LEN_MIEN_BAC_CARD_COMPARATOR;
    }

    @Override
    public Object getCombinationIdentifier(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return CombinationType.INVALID;
        List<Card> sortedCards = new ArrayList<>(cards);
        Collections.sort(sortedCards, getCardComparator()); 

        TienLenVariantRuleSet.CombinationType basicType = 
            TienLenCombinationLogic.getCombinationType(sortedCards, this);

        // --- ÁP DỤNG LUẬT RIÊNG CỦA MIỀN BẮC ---
        if (basicType == TienLenVariantRuleSet.CombinationType.STRAIGHT) {
            // TienLenMienBacRule phải kiểm tra thêm tính đồng chất.
            if (sortedCards.size() < 3) return CombinationType.INVALID; 
            Card.Suit firstSuit = sortedCards.get(0).getSuit();
            for (int i = 1; i < sortedCards.size(); i++) {
                if (sortedCards.get(i).getSuit() != firstSuit) {
                    return CombinationType.INVALID; 
                }
            }
            return CombinationType.STRAIGHT; 
        }
        
        if (basicType == TienLenVariantRuleSet.CombinationType.PAIR) {
            Card card1 = sortedCards.get(0); 
            Card card2 = sortedCards.get(1);

            if (card1.getCardColor() != card2.getCardColor()) {
                return CombinationType.INVALID; 
            }
        }
        
        return basicType;
    }

    @Override
    public boolean isValidCombination(List<Card> cards) {
        return getCombinationIdentifier(cards) != TienLenVariantRuleSet.CombinationType.INVALID;
    }

    @Override
    public boolean canPlayAfter(List<Card> newCards, List<Card> previousCards) {
        boolean canPlayGenerally = TienLenPlayabilityLogic.canPlayAfter(newCards, previousCards, this);
        if (!canPlayGenerally) return false;

        TienLenVariantRuleSet.CombinationType newType = (TienLenVariantRuleSet.CombinationType) getCombinationIdentifier(newCards);
        TienLenVariantRuleSet.CombinationType prevType = (TienLenVariantRuleSet.CombinationType) getCombinationIdentifier(previousCards);

        if (newType != prevType) {
            return false;                // Phải cùng loại bài (đơn với đơn, đôi với đôi, v.v.)
        }

        // Sắp xếp để dễ lấy lá bài (ví dụ lá đầu tiên cho chất của sảnh/đơn, hoặc màu của đôi)
        List<Card> sortedNewCards = new ArrayList<>(newCards);
        Collections.sort(sortedNewCards, getCardComparator());
        List<Card> sortedPreviousCards = new ArrayList<>(previousCards);
        Collections.sort(sortedPreviousCards, getCardComparator());

        Card newRep = getRepresentativeCardForCombination(sortedNewCards);
        Card prevRep = getRepresentativeCardForCombination(sortedPreviousCards);

        if (newRep == null || prevRep == null) return false; // Không có lá đại diện

        switch (newType) {
            case SINGLE:
                if (newCards.size() != 1 || previousCards.size() != 1) return false; // Đảm bảo là đơn
                // 1. Phải đồng chất (cùng suit)
                if (sortedNewCards.get(0).getSuit() != sortedPreviousCards.get(0).getSuit()) {
                    return false;
                }
                // 2. Phải lớn hơn (đã đồng chất, giờ so sánh rank qua comparator)
                return getCardComparator().compare(newRep, prevRep) > 0;

            case PAIR: // Đôi (đã được xác định là cùng rank và cùng màu bởi getCombinationIdentifier)
                if (newCards.size() != 2 || previousCards.size() != 2) return false;
                // 1. Phải đồng màu (hai đôi phải cùng màu đỏ, hoặc cùng màu đen)
                // Màu của đôi được quyết định bởi màu của các lá bài trong đôi (chúng giống nhau)
                if (sortedNewCards.get(0).getCardColor() != sortedPreviousCards.get(0).getCardColor()) {
                    return false;
                }
                // 2. Phải lớn hơn (so sánh lá đại diện)
                return getCardComparator().compare(newRep, prevRep) > 0;

            case TRIPLE:
                if (newCards.size() != 3 || previousCards.size() != 3) return false;
                // Luật "sám cô đồng chất lẻ x" của bạn rất đặc thù và phức tạp để tổng quát hóa
                return getCardComparator().compare(newRep, prevRep) > 0;

            case STRAIGHT: 
                if (sortedNewCards.size() != sortedPreviousCards.size()) {
                    return false;
                }
                if (sortedNewCards.get(0).getSuit() != sortedPreviousCards.get(0).getSuit()) {
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
        Collections.sort(sortedCombination, TIEN_LEN_MIEN_BAC_CARD_COMPARATOR); 
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
		if(cards.contains(new Card(Card.Suit.SPADES, Card.Rank.THREE))) return true;
		else {
		return false;
		}
	}
    
	@Override
	public int getCardsPerPlayer() {
		return 13;
	}
    
	@Override
	public Card startingCard() {
		return new Card(Card.Suit.SPADES, Card.Rank.THREE);
	}   
	
}
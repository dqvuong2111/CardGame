package core.games.tienlen.tienlenmiennam;

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.logic.TienLenCombinationLogic;
import core.games.tienlen.logic.TienLenPlayabilityLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
// ...

public class TienLenMienNamRule implements TienLenVariantRuleSet {

	// Giữ lại Comparator và các hàm getTienLenValue của Miền Nam
	private static final Comparator<Card> TIEN_LEN_CARD_COMPARATOR = new TienLenCardComparator();

	public static int getTienLenValue(Card card) {
		if (card.getRank() == Card.Rank.TWO)
			return 15;
		if (card.getRank() == Card.Rank.ACE)
			return 14;
		if (card.getRank() == Card.Rank.KING)
			return 13;
		if (card.getRank() == Card.Rank.QUEEN)
			return 12;
		if (card.getRank() == Card.Rank.JACK)
			return 11;
		return card.getRank().getValue();
	}

	private static int getSuitOrder(Card.Suit suit) {
		return switch (suit) {
		case SPADES -> 0;
		case CLUBS -> 1;
		case DIAMONDS -> 2;
		case HEARTS -> 3;
		};
	}

	private static class TienLenCardComparator implements Comparator<Card> {
		@Override
		public int compare(Card card1, Card card2) {
			int value1 = getTienLenValue(card1);
			int value2 = getTienLenValue(card2);
			if (value1 != value2)
				return Integer.compare(value1, value2);
			return Integer.compare(getSuitOrder(card1.getSuit()), getSuitOrder(card2.getSuit()));
		}
	}

	@Override
	public Comparator<Card> getCardComparator() {
		return TIEN_LEN_CARD_COMPARATOR;
	}

	@Override
    public Object getCombinationIdentifier(List<Card> cards) {
        // Gọi logic chung để nhận diện cấu trúc cơ bản
        // Lớp logic chung đã được sửa để nhận 'this' (là TienLenMienNamRule)
        TienLenVariantRuleSet.CombinationType identifiedType = 
            TienLenCombinationLogic.getCombinationType(cards, this);

        // Đối với TLMN, hầu hết các cấu trúc cơ bản được xác định bởi TienLenCombinationLogic
        // là đã hợp lệ (ví dụ, sảnh không cần đồng chất).
        // Nếu có các luật đặc biệt khác của TLMN mà TienLenCombinationLogic chưa xử lý,
        // bạn có thể thêm kiểm tra ở đây.
        // Ví dụ: nếu sảnh 13 lá là một loại đặc biệt, bạn có thể check ở đây.
        return identifiedType;
    }

	 @Override
	    public boolean isValidCombination(List<Card> cards) {
	        // Đơn giản là gọi getCombinationIdentifier và kiểm tra INVALID
	        return getCombinationIdentifier(cards) != TienLenVariantRuleSet.CombinationType.INVALID;
	    }

	@Override
	public boolean canPlayAfter(List<Card> newCards, List<Card> previousCards) {
		// Gọi hàm logic (đã được sửa để nhận ruleSet là this)
		return TienLenPlayabilityLogic.canPlayAfter(newCards, previousCards, this);
	}

	@Override
	public Card getRepresentativeCardForCombination(List<Card> combination) {
		if (combination == null || combination.isEmpty())
			return null;
		List<Card> sortedCombination = new ArrayList<>(combination);
		Collections.sort(sortedCombination, TIEN_LEN_CARD_COMPARATOR);
		return sortedCombination.get(sortedCombination.size() - 1); // Thường là lá lớn nhất
	}

	@Override
	public boolean isCardValidInStraight(Card card) {
		return getTienLenValue(card) < 15; // Heo (giá trị 15) không được trong sảnh
	}

	@Override
	public int getCardRankValue(Card card) {
		return getTienLenValue(card);
	}

	@Override
	public int getTwoRankValue() {
		return 15; // Giá trị của quân 2 trong Miền Nam
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
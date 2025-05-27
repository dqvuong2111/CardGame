package core.games.tienlen.samloc;

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.logic.TienLenCombinationLogic;
import core.games.tienlen.logic.TienLenPlayabilityLogic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SamLocRule implements TienLenVariantRuleSet {

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

        TienLenVariantRuleSet.CombinationType basicType = 
            TienLenCombinationLogic.getCombinationType(sortedCards, this);

        if (basicType == TienLenVariantRuleSet.CombinationType.STRAIGHT) {

            if (sortedCards.size() < 3) return CombinationType.INVALID; // Cần thiết cho isMienBacStraight
  
            // Nếu qua được các kiểm tra -> là sảnh TLMB hợp lệ
            return CombinationType.STRAIGHT; // Vẫn là STRAIGHT, nhưng đã được validate đồng chất
        }
        
        // Đối với các bộ khác như PAIR, TRIPLE, FOUR_OF_KIND, THREE_PAIR_STRAIGHT, FOUR_PAIR_STRAIGHT
        // TLMB thường không yêu cầu đồng chất, nên kết quả từ TienLenCombinationLogic có thể là cuối cùng.
        // Nếu có luật nào khác, bạn thêm kiểm tra ở đây.
        return basicType;
    }

    @Override
    public boolean isValidCombination(List<Card> cards) {
        return getCombinationIdentifier(cards) != TienLenVariantRuleSet.CombinationType.INVALID;
    }

    @Override
    public boolean canPlayAfter(List<Card> newCards, List<Card> previousCards) {
        // Gọi hàm logic chung
        boolean canPlayGenerally = TienLenPlayabilityLogic.canPlayAfter(newCards, previousCards, this);
        if (!canPlayGenerally) return false;

        // --- ÁP DỤNG CÁC LUẬT CHẶT CHẼ HƠN CỦA MIỀN BẮC NẾU CẦN ---
        TienLenVariantRuleSet.CombinationType newType = (TienLenVariantRuleSet.CombinationType) getCombinationIdentifier(newCards);
        TienLenVariantRuleSet.CombinationType prevType = (TienLenVariantRuleSet.CombinationType) getCombinationIdentifier(previousCards);

        if (newType != prevType) {
            return false; // Phải cùng loại bài (đơn với đơn, đôi với đôi, v.v.)
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
                // 1. Phải đồng chất (cùng suit
                // 2. Phải lớn hơn (đã đồng chất, giờ so sánh rank qua comparator)
                return getCardComparator().compare(newRep, prevRep) > 0;

            case PAIR: // Đôi (đã được xác định là cùng rank và cùng màu bởi getCombinationIdentifier)
                if (newCards.size() != 2 || previousCards.size() != 2) return false;
                // 1. Phải đồng màu (hai đôi phải cùng màu đỏ, hoặc cùng màu đen)
                // Màu của đôi được quyết định bởi màu của các lá bài trong đôi (chúng giống nhau)

                // 2. Phải lớn hơn (so sánh lá đại diện)
                return getCardComparator().compare(newRep, prevRep) > 0;

            case TRIPLE:
                if (newCards.size() != 3 || previousCards.size() != 3) return false;
                // Luật "sám cô đồng chất lẻ x" của bạn rất đặc thù và phức tạp để tổng quát hóa
                // nếu không có quy tắc rõ ràng về việc xác định "chất lẻ" đó.
                // Hiện tại, chúng ta sẽ bỏ qua yêu cầu "đồng chất" cho sám cô khi chặt nhau
                // và chỉ yêu cầu rank cao hơn.
                // NẾU BẠN MUỐN MỘT QUY TẮC ĐƠN GIẢN HƠN CHO "ĐỒNG CHẤT": ví dụ, lá bài lớn nhất
                // của bộ ba mới phải cùng chất với lá bài lớn nhất của bộ ba cũ.
                // if (newRep.getSuit() != prevRep.getSuit()) {
                //     return false;
                // }
                return getCardComparator().compare(newRep, prevRep) > 0;

            case STRAIGHT: // Sảnh (đã được xác định là đồng chất bởi getCombinationIdentifier)
                // 1. Phải cùng số lá
                if (sortedNewCards.size() != sortedPreviousCards.size()) {
                    return false;
                }
                // 3. Phải lớn hơn (so sánh lá đại diện - lá lớn nhất của sảnh)
                return getCardComparator().compare(newRep, prevRep) > 0;
            
            default:

                return false; // An toàn hơn là không cho phép nếu không có luật rõ ràng
        }
    }
    
    // ... (getRepresentativeCardForCombination, isCardValidInStraight)
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
        return 15; // Giá trị của quân 2
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
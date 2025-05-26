package core.games.tienlen.tienlenmienbac;

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.logic.TienLenCombinationLogic;
import core.games.tienlen.logic.TienLenPlayabilityLogic;
import core.games.tienlen.tienlenmienbac.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TienLenMienBacRule implements TienLenVariantRuleSet {

    // Giữ lại Comparator và các hàm getTienLenBacValue của Miền Bắc
    private static final Comparator<Card> TIEN_LEN_MIEN_BAC_CARD_COMPARATOR = new TienLenMienBacCardComparator();
    public static int getTienLenValue(Card card) {
        if (card.getRank() == Card.Rank.TWO) return 15; // Heo (2) là lớn nhất
        if (card.getRank() == Card.Rank.ACE) return 14; // Át
        if (card.getRank() == Card.Rank.KING) return 13; // Già
        // ... (các rank khác tương tự như TLMN) ...
        if (card.getRank() == Card.Rank.QUEEN) return 12;
        if (card.getRank() == Card.Rank.JACK) return 11;
        return card.getRank().getValue(); // Đối với các lá số từ 3 đến 10
    }
    public static int getSuitOrderValue(Card.Suit suit) {
        return switch (suit) {
            case SPADES -> 1;   // Bích (thấp nhất nếu dùng để so sánh hơn thua trực tiếp)
            case CLUBS -> 2;    // Chuồn/Tép
            case DIAMONDS -> 3; // Rô
            case HEARTS -> 4;   // Cơ (cao nhất)
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
            // Nếu cùng giá trị, TLMB thường không so chất để phân định hơn thua khi chặt.
            // Chất chỉ quan trọng khi xét 3 bích hoặc một số luật đặc biệt.
            // Để comparator này hoàn chỉnh, ta vẫn có thể so sánh chất (ví dụ để sắp xếp tay bài)
            return Integer.compare(getSuitOrderValue(card1.getSuit()), getSuitOrderValue(card2.getSuit()));
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
        Collections.sort(sortedCards, getCardComparator()); // Sort theo luật Bắc

        // Gọi logic chung để nhận diện cấu trúc cơ bản
        TienLenVariantRuleSet.CombinationType basicType = 
            TienLenCombinationLogic.getCombinationType(sortedCards, this);

        // --- ÁP DỤNG LUẬT RIÊNG CỦA MIỀN BẮC ---
        if (basicType == TienLenVariantRuleSet.CombinationType.STRAIGHT) {
            // Logic chung (isStraightStructure) đã kiểm tra không có 2 và liên tiếp.
            // Ở đây, TienLenMienBacRule phải kiểm tra thêm tính đồng chất.
            if (sortedCards.size() < 3) return CombinationType.INVALID; // Cần thiết cho isMienBacStraight
            Card.Suit firstSuit = sortedCards.get(0).getSuit();
            for (int i = 1; i < sortedCards.size(); i++) {
                if (sortedCards.get(i).getSuit() != firstSuit) {
                    return CombinationType.INVALID; // Không đồng chất -> không phải sảnh TLMB hợp lệ
                }
            }
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

        // Ví dụ: TLMB yêu cầu sảnh phải cùng độ dài khi đánh đè (nếu chưa có trong logic chung)
        if (newType == TienLenVariantRuleSet.CombinationType.STRAIGHT && prevType == TienLenVariantRuleSet.CombinationType.STRAIGHT) {
            if (newCards.size() != previousCards.size()) {
                return false; 
            }
        }
        // Các quy tắc chặt heo, tứ quý của TLMB có thể khác TLMN một chút,
        // bạn có thể cần tinh chỉnh logic trong TienLenMienNamPlayabilityLogic
        // hoặc thêm các kiểm tra cụ thể ở đây nếu logic chung không đủ.
        
        return true; // Nếu đã qua các kiểm tra chung và các kiểm tra đặc thù (nếu có)
    }
    
    // ... (getRepresentativeCardForCombination, isCardValidInStraight)
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
        return 15; // Giá trị của quân 2
    }
}
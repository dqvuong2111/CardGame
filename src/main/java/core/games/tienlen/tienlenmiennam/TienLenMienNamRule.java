// File: core/games/tienlen/tienlenmiennam/TienLenMienNamRule.java
package core.games.tienlen.tienlenmiennam;

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;
// Import các lớp delegate mới
import core.games.tienlen.tienlenmiennam.logic.TienLenMienNamCombinationLogic;
import core.games.tienlen.tienlenmiennam.logic.TienLenMienNamPlayabilityLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TienLenMienNamRule implements TienLenVariantRuleSet {

    // Enum CombinationType vẫn ở đây vì nó là một phần của "giao diện" luật này
    public enum CombinationType {
        SINGLE, PAIR, TRIPLE, STRAIGHT, FOUR_OF_KIND, THREE_PAIR_STRAIGHT, FOUR_PAIR_STRAIGHT, INVALID
    }

    // Comparator và các hàm tiện ích giá trị lá bài vẫn có thể ở đây (public static)
    // để các delegate và các phần khác của game có thể sử dụng một cách nhất quán.
    private static final Comparator<Card> TIEN_LEN_CARD_COMPARATOR = new TienLenCardComparator();

    public static int getTienLenValue(Card card) {
        if (card.getRank() == Card.Rank.TWO) return 15;
        if (card.getRank() == Card.Rank.ACE) return 14;
        if (card.getRank() == Card.Rank.KING) return 13;
        if (card.getRank() == Card.Rank.QUEEN) return 12;
        if (card.getRank() == Card.Rank.JACK) return 11;
        return card.getRank().getValue();
    }

    private static int getSuitOrder(Card.Suit suit) {
        return switch (suit) {
            case SPADES -> 0; case CLUBS -> 1; case DIAMONDS -> 2; case HEARTS -> 3;
        };
    }

    private static class TienLenCardComparator implements Comparator<Card> {
        @Override
        public int compare(Card card1, Card card2) {
            int value1 = getTienLenValue(card1);
            int value2 = getTienLenValue(card2);
            if (value1 != value2) return Integer.compare(value1, value2);
            return Integer.compare(getSuitOrder(card1.getSuit()), getSuitOrder(card2.getSuit()));
        }
    }

    // Các phương thức của Interface RuleSet và TienLenVariantRuleSet

    @Override
    public Comparator<Card> getCardComparator() {
        return TIEN_LEN_CARD_COMPARATOR;
    }

    @Override
    public Object getCombinationIdentifier(List<Card> cards) {
        // Ủy nhiệm cho lớp logic mới
        return TienLenMienNamCombinationLogic.getCombinationType(cards, TIEN_LEN_CARD_COMPARATOR);
        // Hoặc nếu các hàm tiện ích getTienLenValue, getSuitOrder được dùng trực tiếp trong delegate:
        // return TienLenMienNamCombinationLogic.getCombinationType(cards);
    }

    @Override
    public boolean isValidCombination(List<Card> cards) {
        // Ủy nhiệm
        return TienLenMienNamCombinationLogic.getCombinationType(cards, TIEN_LEN_CARD_COMPARATOR) != CombinationType.INVALID;
    }

    @Override
    public boolean canPlayAfter(List<Card> newCards, List<Card> previousCards) {
        // Ủy nhiệm cho lớp logic mới
        // Lớp này sẽ cần truy cập getCombinationType, getRepresentativeCardForCombination, comparator
        return TienLenMienNamPlayabilityLogic.canPlayAfter(newCards, previousCards, this);
    }

    @Override
    public Card getRepresentativeCardForCombination(List<Card> combination) {
        if (combination == null || combination.isEmpty()) return null;
        List<Card> sortedCombination = new ArrayList<>(combination);
        Collections.sort(sortedCombination, TIEN_LEN_CARD_COMPARATOR); // Sắp xếp bằng comparator của luật này
        return sortedCombination.get(sortedCombination.size() - 1);
    }

    @Override
    public boolean isCardValidInStraight(Card card) {
        return getTienLenValue(card) < 15; // Heo không được trong sảnh
    }


    public boolean hasStartingCard(List<Card> cards) {
        return cards.contains(new Card(Card.Suit.SPADES, Card.Rank.THREE));
    }

    // Phương thức getCardsDisplay có thể giữ lại hoặc chuyển đi nếu thấy cần thiết
    public String getCardsDisplay(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return "Không có bài nào";
        return cards.stream().map(Card::toString).collect(Collectors.joining(", "));
    }
}
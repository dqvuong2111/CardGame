package core.games.tienlen.tienlenmiennam.logic;

import core.Card;
import core.games.tienlen.tienlenmiennam.TienLenMienNamRule; // Để truy cập CombinationType, getTienLenValue, comparator

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TienLenMienNamCombinationLogic {

    // Sử dụng lại CombinationType từ TienLenMienNamRule
    // Hoặc bạn có thể định nghĩa lại một bản sao ở đây nếu muốn độc lập hoàn toàn (nhưng không cần thiết)
    // public enum CombinationType { SINGLE, PAIR, ... }

    public static TienLenMienNamRule.CombinationType getCombinationType(List<Card> cards, Comparator<Card> cardComparator) {
        if (cards == null || cards.isEmpty()) {
            return TienLenMienNamRule.CombinationType.INVALID;
        }

        List<Card> sortedCards = new ArrayList<>(cards);
        Collections.sort(sortedCards, cardComparator); // Sử dụng comparator được truyền vào

        int size = sortedCards.size();

        switch (size) {
            case 1: return TienLenMienNamRule.CombinationType.SINGLE;
            case 2: if (isPair(sortedCards)) return TienLenMienNamRule.CombinationType.PAIR; break;
            case 3:
                if (isTriple(sortedCards)) return TienLenMienNamRule.CombinationType.TRIPLE;
                if (isStraight(sortedCards)) return TienLenMienNamRule.CombinationType.STRAIGHT;
                break;
            case 4:
                if (isFourOfKind(sortedCards)) return TienLenMienNamRule.CombinationType.FOUR_OF_KIND;
                if (isStraight(sortedCards)) return TienLenMienNamRule.CombinationType.STRAIGHT;
                break;
            case 5: if (isStraight(sortedCards)) return TienLenMienNamRule.CombinationType.STRAIGHT; break;
            case 6:
                if (isStraight(sortedCards)) return TienLenMienNamRule.CombinationType.STRAIGHT;
                if (isThreePairStraight(sortedCards)) return TienLenMienNamRule.CombinationType.THREE_PAIR_STRAIGHT;
                break;
            case 7: if (isStraight(sortedCards)) return TienLenMienNamRule.CombinationType.STRAIGHT; break;
            case 8:
                if (isStraight(sortedCards)) return TienLenMienNamRule.CombinationType.STRAIGHT;
                if (isFourPairStraight(sortedCards)) return TienLenMienNamRule.CombinationType.FOUR_PAIR_STRAIGHT;
                break;
            case 9: if (isStraight(sortedCards)) return TienLenMienNamRule.CombinationType.STRAIGHT; break;
            case 10: if (isStraight(sortedCards)) return TienLenMienNamRule.CombinationType.STRAIGHT; break;
            case 11: if (isStraight(sortedCards)) return TienLenMienNamRule.CombinationType.STRAIGHT; break;
            case 12: if (isStraight(sortedCards)) return TienLenMienNamRule.CombinationType.STRAIGHT; break;
        }
        return TienLenMienNamRule.CombinationType.INVALID;
    }

    // Các phương thức isPair, isTriple, isStraight,... sẽ được chuyển vào đây
    // Chúng sẽ sử dụng TienLenMienNamRule.getTienLenValue()
    private static boolean isPair(List<Card> cards) {
        return cards.size() == 2 && TienLenMienNamRule.getTienLenValue(cards.get(0)) == TienLenMienNamRule.getTienLenValue(cards.get(1));
    }

    private static boolean isTriple(List<Card> cards) {
        return cards.size() == 3 &&
               TienLenMienNamRule.getTienLenValue(cards.get(0)) == TienLenMienNamRule.getTienLenValue(cards.get(1)) &&
               TienLenMienNamRule.getTienLenValue(cards.get(1)) == TienLenMienNamRule.getTienLenValue(cards.get(2));
    }

    private static boolean isFourOfKind(List<Card> cards) {
         return cards.size() == 4 &&
               TienLenMienNamRule.getTienLenValue(cards.get(0)) == TienLenMienNamRule.getTienLenValue(cards.get(1)) &&
               TienLenMienNamRule.getTienLenValue(cards.get(1)) == TienLenMienNamRule.getTienLenValue(cards.get(2)) &&
               TienLenMienNamRule.getTienLenValue(cards.get(2)) == TienLenMienNamRule.getTienLenValue(cards.get(3));
    }

    private static boolean isStraight(List<Card> cards) {
        if (cards.size() < 3) return false;
        for (Card card : cards) {
            if (TienLenMienNamRule.getTienLenValue(card) == 15) return false; // Heo
        }
        for (int i = 0; i < cards.size() - 1; i++) {
            if (TienLenMienNamRule.getTienLenValue(cards.get(i + 1)) - TienLenMienNamRule.getTienLenValue(cards.get(i)) != 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean isThreePairStraight(List<Card> cards) {
        if (cards.size() != 6) return false;
        return isMultiplePairStraight(cards, 3);
    }

    private static boolean isFourPairStraight(List<Card> cards) {
        if (cards.size() != 8) return false;
        return isMultiplePairStraight(cards, 4);
    }

    private static boolean isMultiplePairStraight(List<Card> cards, int numPairs) {
        if (cards.size() != numPairs * 2) return false;
        List<Integer> pairValues = new ArrayList<>();
        for (int i = 0; i < cards.size(); i += 2) {
            if (TienLenMienNamRule.getTienLenValue(cards.get(i)) != TienLenMienNamRule.getTienLenValue(cards.get(i + 1))) return false;
            if (TienLenMienNamRule.getTienLenValue(cards.get(i)) == 15) return false; // Heo
            pairValues.add(TienLenMienNamRule.getTienLenValue(cards.get(i)));
        }
        for (int i = 0; i < pairValues.size() - 1; i++) {
            if (pairValues.get(i + 1) - pairValues.get(i) != 1) return false;
        }
        return true;
    }
}
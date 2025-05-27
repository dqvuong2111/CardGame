package core.games.tienlen.logic; // Hoặc package hiện tại của bạn

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet; // << QUAN TRỌNG: Sử dụng interface này
// Bỏ import trực tiếp TienLenMienNamRule nếu các phương thức không còn gọi tĩnh đến nó nữa
// import core.games.tienlen.tienlenmiennam.TienLenMienNamRule; 

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TienLenCombinationLogic { // Giữ nguyên tên lớp nếu bạn muốn

    // getCombinationType giờ nhận TienLenVariantRuleSet
    // và trả về TienLenVariantRuleSet.CombinationType
	public static TienLenVariantRuleSet.CombinationType getCombinationType(List<Card> cards, TienLenVariantRuleSet ruleSet) {
        if (cards == null || cards.isEmpty()) {
            return TienLenVariantRuleSet.CombinationType.INVALID;
        }

        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort(ruleSet.getCardComparator());

        int size = sortedCards.size();

        // Ưu tiên kiểm tra các bộ nhiều lá và đặc biệt trước
        if (size == 4 && isFourOfKind(sortedCards, ruleSet)) return TienLenVariantRuleSet.CombinationType.FOUR_OF_KIND;
        if (size == 6 && isThreePairStraight(sortedCards, ruleSet)) return TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT;
        if (size == 8 && isFourPairStraight(sortedCards, ruleSet)) return TienLenVariantRuleSet.CombinationType.FOUR_PAIR_STRAIGHT;

        // Kiểm tra sảnh sau các bộ đặc biệt có số lá tương tự
        // isStraightStructure sẽ kiểm tra không có 2 và liên tiếp.
        // Việc sảnh có đồng chất hay không sẽ do RuleSet cụ thể quyết định sau.
        if (size >= 3 && isStraight(sortedCards, ruleSet)) {
             // Nếu ruleSet là TLMB, nó sẽ kiểm tra thêm đồng chất trong isValidCombination của nó.
             // Nếu ruleSet là TLMN, nó sẽ chấp nhận sảnh này.
             // Ở đây, logic chung chỉ nhận diện cấu trúc sảnh.
            return TienLenVariantRuleSet.CombinationType.STRAIGHT;
        }

        if (size == 3 && isTriple(sortedCards, ruleSet)) return TienLenVariantRuleSet.CombinationType.TRIPLE;
        if (size == 2 && isPair(sortedCards, ruleSet)) return TienLenVariantRuleSet.CombinationType.PAIR;
        if (size == 1) return TienLenVariantRuleSet.CombinationType.SINGLE;

        return TienLenVariantRuleSet.CombinationType.INVALID;
    }

    // Các phương thức isPair, isTriple, isStraight,... giờ nhận ruleSet
    // và sử dụng ruleSet.getCardRankValue() thay vì TienLenMienNamRule.getTienLenValue()
    private static boolean isPair(List<Card> cards, TienLenVariantRuleSet ruleSet) {
        return cards.size() == 2 && ruleSet.getCardRankValue(cards.get(0)) == ruleSet.getCardRankValue(cards.get(1));
    }

    private static boolean isTriple(List<Card> cards, TienLenVariantRuleSet ruleSet) {
        return cards.size() == 3 &&
               ruleSet.getCardRankValue(cards.get(0)) == ruleSet.getCardRankValue(cards.get(1)) &&
               ruleSet.getCardRankValue(cards.get(1)) == ruleSet.getCardRankValue(cards.get(2));
    }

    private static boolean isFourOfKind(List<Card> cards, TienLenVariantRuleSet ruleSet) {
         return cards.size() == 4 &&
               ruleSet.getCardRankValue(cards.get(0)) == ruleSet.getCardRankValue(cards.get(1)) &&
               ruleSet.getCardRankValue(cards.get(1)) == ruleSet.getCardRankValue(cards.get(2)) &&
               ruleSet.getCardRankValue(cards.get(2)) == ruleSet.getCardRankValue(cards.get(3));
    }

    // Hàm isStraight này sẽ kiểm tra cấu trúc sảnh cơ bản (liên tiếp, không có 2)
    // Việc kiểm tra đồng chất sẽ do TienLenMienBacRule đảm nhiệm trong phương thức isValidCombination của nó.
    private static boolean isStraight(List<Card> sortedCards, TienLenVariantRuleSet ruleSet) {
        if (sortedCards.size() < 3) return false;
        for (Card card : sortedCards) {
            // Sử dụng getTwoRankValue() từ ruleSet để kiểm tra quân 2
            if (ruleSet.getCardRankValue(card) == ruleSet.getTwoRankValue()) return false; 
        }
        for (int i = 0; i < sortedCards.size() - 1; i++) {
            // Kiểm tra tính liên tiếp dựa trên giá trị rank từ ruleSet
            if (ruleSet.getCardRankValue(sortedCards.get(i + 1)) - ruleSet.getCardRankValue(sortedCards.get(i)) != 1) {
                return false;
            }
        }
        // Nếu là TLMB, ruleSet.isValidCombination(sortedCards) sẽ kiểm tra thêm đồng chất
        return true; // Chỉ trả về true nếu cấu trúc cơ bản là sảnh
    }
    
    // isMultiplePairStraight được đổi tên thành isNConsecutivePairsStructure trong gợi ý trước
    // Giữ tên cũ và thêm ruleSet
    private static boolean isMultiplePairStraight(List<Card> sortedCards, int numPairs, TienLenVariantRuleSet ruleSet) {
        if (sortedCards.size() != numPairs * 2) return false;
        List<Integer> pairRanks = new ArrayList<>();
        for (int i = 0; i < sortedCards.size(); i += 2) {
            if (ruleSet.getCardRankValue(sortedCards.get(i)) != ruleSet.getCardRankValue(sortedCards.get(i + 1))) return false; // Không phải đôi
            if (ruleSet.getCardRankValue(sortedCards.get(i)) == ruleSet.getTwoRankValue()) return false; // Đôi thông không chứa 2
            pairRanks.add(ruleSet.getCardRankValue(sortedCards.get(i)));
        }
        // Kiểm tra tính liên tiếp của các rank của đôi (cần sort pairRanks nếu sortedCards chưa đảm bảo thứ tự đôi)
        Collections.sort(pairRanks); // Đảm bảo các rank của đôi được sắp xếp
        for (int i = 0; i < pairRanks.size() - 1; i++) {
            if (pairRanks.get(i + 1) - pairRanks.get(i) != 1) return false;
        }
        return true;
    }

    private static boolean isThreePairStraight(List<Card> cards, TienLenVariantRuleSet ruleSet) {
        if (cards.size() != 6) return false;
        // Cần sắp xếp lại cards trước khi gọi isMultiplePairStraight nếu cards chưa được sort đúng cách
        // List<Card> sortedCardsForCheck = new ArrayList<>(cards);
        // sortedCardsForCheck.sort(ruleSet.getCardComparator());
        // return isMultiplePairStraight(sortedCardsForCheck, 3, ruleSet);
        return isMultiplePairStraight(cards, 3, ruleSet); // Giả sử cards đã được sort bên ngoài hoặc isMultiplePairStraight tự sort
    }

    private static boolean isFourPairStraight(List<Card> cards, TienLenVariantRuleSet ruleSet) {
        if (cards.size() != 8) return false;
        return isMultiplePairStraight(cards, 4, ruleSet);
    }
}
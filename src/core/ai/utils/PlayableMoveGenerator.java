// File: core/ai/utils/PlayableMoveGenerator.java
package core.ai.utils;

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;

import java.util.ArrayList;
import java.util.List;

public class PlayableMoveGenerator {

    /**
     * Tìm tất cả các lá đơn hợp lệ có thể đánh.
     * @param hand Bài trên tay.
     * @param lastPlayedCards Bài đã đánh trước đó (có thể null hoặc rỗng nếu bắt đầu vòng).
     * @param ruleSet Bộ luật của game.
     * @return Danh sách các List<Card>, mỗi List<Card> con là một lá đơn hợp lệ.
     */
    public static List<List<Card>> findPlayableSingles(List<Card> hand, List<Card> lastPlayedCards, TienLenVariantRuleSet ruleSet) {
        if (hand == null || hand.isEmpty()) {
            return new ArrayList<>();
        }
        List<List<Card>> playableSingles = new ArrayList<>();
        List<List<Card>> allSingles = CombinationFinder.findAllSingles(hand, ruleSet);

        for (List<Card> single : allSingles) {
            if (ruleSet.isValidCombination(single)) { // Kiểm tra nếu lá đơn này là hợp lệ theo luật
                if (lastPlayedCards == null || lastPlayedCards.isEmpty()) { // Bắt đầu vòng mới
                    playableSingles.add(single);
                } else if (ruleSet.canPlayAfter(single, lastPlayedCards)) {
                    playableSingles.add(single);
                }
            }
        }
        return playableSingles;
    }

    /**
     * Tìm tất cả các đôi hợp lệ có thể đánh.
     */
    public static List<List<Card>> findPlayablePairs(List<Card> hand, List<Card> lastPlayedCards, TienLenVariantRuleSet ruleSet) {
        if (hand == null || hand.size() < 2) {
            return new ArrayList<>();
        }
        List<List<Card>> playable = new ArrayList<>();
        List<List<Card>> allCombinations = CombinationFinder.findAllPairs(hand, ruleSet);

        for (List<Card> combo : allCombinations) {
            if (ruleSet.isValidCombination(combo)) {
                if (lastPlayedCards == null || lastPlayedCards.isEmpty()) {
                    playable.add(combo);
                } else if (ruleSet.canPlayAfter(combo, lastPlayedCards)) {
                    playable.add(combo);
                }
            }
        }
        return playable;
    }

    /**
     * Tìm tất cả các bộ ba hợp lệ có thể đánh.
     */
    public static List<List<Card>> findPlayableTriples(List<Card> hand, List<Card> lastPlayedCards, TienLenVariantRuleSet ruleSet) {
        if (hand == null || hand.size() < 3) {
            return new ArrayList<>();
        }
        List<List<Card>> playable = new ArrayList<>();
        List<List<Card>> allCombinations = CombinationFinder.findAllTriples(hand, ruleSet);

        for (List<Card> combo : allCombinations) {
            if (ruleSet.isValidCombination(combo)) {
                if (lastPlayedCards == null || lastPlayedCards.isEmpty()) {
                    playable.add(combo);
                } else if (ruleSet.canPlayAfter(combo, lastPlayedCards)) {
                    playable.add(combo);
                }
            }
        }
        return playable;
    }

    /**
     * Tìm tất cả các sảnh hợp lệ có thể đánh.
     * Sẽ tìm sảnh có cùng độ dài với lastPlayedCards nếu lastPlayedCards là sảnh,
     * hoặc sảnh có độ dài tối thiểu (ví dụ 3 lá) nếu bắt đầu vòng mới.
     */
    public static List<List<Card>> findPlayableStraights(List<Card> hand, List<Card> lastPlayedCards, TienLenVariantRuleSet ruleSet) {
        if (hand == null || hand.size() < 3) { // Sảnh thường có ít nhất 3 lá
            return new ArrayList<>();
        }

        final int defaultMinStraightLength = 3; // Độ dài sảnh tối thiểu khi bắt đầu vòng mới
        int searchLength = defaultMinStraightLength; // Độ dài sảnh cần tìm kiếm
        boolean mustMatchLength = false;

        if (lastPlayedCards != null && !lastPlayedCards.isEmpty()) {
            Object lastPlayedIdentifier = ruleSet.getCombinationIdentifier(lastPlayedCards);
            // Giả định: RuleSet có thể cho biết lastPlayedCards có phải là sảnh không và độ dài của nó.
            // Ví dụ, nếu getCombinationIdentifier trả về một String "STRAIGHT" hoặc một enum cụ thể.
            // Và kích thước của lastPlayedCards chính là độ dài của sảnh đó.

            // CÁCH 1: Dựa vào kích thước và tính hợp lệ cơ bản (cần RuleSet mạnh hơn)
            // boolean lastWasStraight = ruleSet.isValidCombination(lastPlayedCards) &&
            //                          lastPlayedCards.size() >= defaultMinStraightLength &&
            //                          lastPlayedCards.stream().allMatch(ruleSet::isCardValidInStraight);
            // (Và kiểm tra tính liên tục nếu cần)

            // CÁCH 2: Dựa vào định danh từ RuleSet (linh hoạt hơn)
            // Thay thế "STRAIGHT_TYPE_FROM_RULESET" bằng cách bạn xác định sảnh từ RuleSet
            // Ví dụ: if (MyGameSpecificEnumType.STRAIGHT == lastPlayedIdentifier)
            // Hoặc: if ("STRAIGHT".equals(lastPlayedIdentifier.toString()))
            // Đây là ví dụ giả định:
            if (isCombinationTypeStraight(lastPlayedIdentifier)) { // Bạn cần implement isCombinationTypeStraight
                searchLength = lastPlayedCards.size();
                mustMatchLength = true;
            }
        }

        // searchLength bây giờ là final hoặc effectively final cho lệnh gọi CombinationFinder
        List<List<Card>> allPotentialStraightsInHand = CombinationFinder.findAllStraights(hand, ruleSet, searchLength);

        List<List<Card>> playableStraights = new ArrayList<>();
        for (List<Card> potentialStraight : allPotentialStraightsInHand) {
            // Nếu phải khớp độ dài và sảnh tìm được không khớp, bỏ qua
            if (mustMatchLength && potentialStraight.size() != searchLength) {
                continue;
            }

            // Kiểm tra lại tính hợp lệ của tổ hợp (CombinationFinder có thể chỉ tìm cấu trúc)
            if (ruleSet.isValidCombination(potentialStraight)) {
                if (lastPlayedCards == null || lastPlayedCards.isEmpty()) { // Bắt đầu vòng mới
                    playableStraights.add(potentialStraight);
                } else if (ruleSet.canPlayAfter(potentialStraight, lastPlayedCards)) { // Đánh theo
                    playableStraights.add(potentialStraight);
                }
            }
        }
        return playableStraights;
    }
    
    /**
     * Phương thức ví dụ (bạn cần triển khai dựa trên RuleSet của bạn)
     * để kiểm tra xem định danh tổ hợp có phải là Sảnh không.
     */
    private static boolean isCombinationTypeStraight(Object combinationIdentifier) {
        if (combinationIdentifier == null) {
            return false;
        }
        // Ví dụ nếu RuleSet của bạn trả về String:
        // return "STRAIGHT".equalsIgnoreCase(combinationIdentifier.toString());

        // Ví dụ nếu RuleSet của bạn trả về một Enum (ví dụ TienLenRule.CombinationType):
        // return combinationIdentifier == TienLenRule.CombinationType.STRAIGHT;
        // Vì chúng ta đang cố gắng tổng quát hóa, bạn cần một cách chung hơn.
        // Giả sử, nếu nó là một đối tượng có phương thức getName() hoặc tương tự:
        if (combinationIdentifier.toString().toUpperCase().contains("STRAIGHT")) { // Đây là một phỏng đoán rất thô!
            return true;
        }
        // Cách tốt nhất là RuleSet cung cấp một phương thức rõ ràng: ruleSet.isStraightIdentifier(combinationIdentifier)

        return false; // Mặc định
    }

    /**
     * Tìm tất cả các tứ quý hợp lệ có thể đánh.
     */
    public static List<List<Card>> findPlayableFourOfAKinds(List<Card> hand, List<Card> lastPlayedCards, TienLenVariantRuleSet ruleSet) {
         if (hand == null || hand.size() < 4) {
            return new ArrayList<>();
        }
        List<List<Card>> playable = new ArrayList<>();
        List<List<Card>> allCombinations = CombinationFinder.findAllFourOfAKind(hand, ruleSet);

        for (List<Card> combo : allCombinations) {
            if (ruleSet.isValidCombination(combo)) { // Tứ quý thường luôn hợp lệ về cấu trúc
                if (lastPlayedCards == null || lastPlayedCards.isEmpty()) {
                    // AI có thể không muốn mở màn bằng tứ quý, nhưng nó hợp lệ
                    // playable.add(combo);
                } else if (ruleSet.canPlayAfter(combo, lastPlayedCards)) {
                    // RuleSet.canPlayAfter sẽ xử lý việc chặt heo, chặt tứ quý nhỏ hơn
                    playable.add(combo);
                }
            }
        }
        return playable;
    }
}
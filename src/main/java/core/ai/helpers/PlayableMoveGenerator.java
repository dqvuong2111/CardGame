package core.ai.helpers;

import core.Card;
import core.games.RuleSet;

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
    public static List<List<Card>> findPlayableSingles(List<Card> hand, List<Card> lastPlayedCards, RuleSet ruleSet) {
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
    public static List<List<Card>> findPlayablePairs(List<Card> hand, List<Card> lastPlayedCards, RuleSet ruleSet) {
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
    public static List<List<Card>> findPlayableTriples(List<Card> hand, List<Card> lastPlayedCards, RuleSet ruleSet) {
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
    public static List<List<Card>> findPlayableStraights(List<Card> hand, List<Card> lastPlayedCards, RuleSet ruleSet) {
        if (hand == null || hand.size() < 3) { 
            return new ArrayList<>();
        }

        final int defaultMinStraightLength = 3; 
        int searchLength = defaultMinStraightLength; 
        boolean mustMatchLength = false;

        if (lastPlayedCards != null && !lastPlayedCards.isEmpty()) {
            Object lastPlayedIdentifier = ruleSet.getCombinationIdentifier(lastPlayedCards);
            if (isCombinationTypeStraight(lastPlayedIdentifier)) { 
                searchLength = lastPlayedCards.size();
                mustMatchLength = true;
            }
        }

        List<List<Card>> allPotentialStraightsInHand = CombinationFinder.findAllStraights(hand, ruleSet, searchLength);

        List<List<Card>> playableStraights = new ArrayList<>();
        for (List<Card> potentialStraight : allPotentialStraightsInHand) {
            if (mustMatchLength && potentialStraight.size() != searchLength) {
                continue;
            }
            if (ruleSet.isValidCombination(potentialStraight)) {
                if (lastPlayedCards == null || lastPlayedCards.isEmpty()) { 
                    playableStraights.add(potentialStraight);
                } else if (ruleSet.canPlayAfter(potentialStraight, lastPlayedCards)) { 
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

        if (combinationIdentifier.toString().toUpperCase().contains("STRAIGHT")) { 
            return true;
        }

        return false; 
    }

    /**
     * Tìm tất cả các tứ quý hợp lệ có thể đánh.
     */
    public static List<List<Card>> findPlayableFourOfAKinds(List<Card> hand, List<Card> lastPlayedCards, RuleSet ruleSet) {
         if (hand == null || hand.size() < 4) {
            return new ArrayList<>();
        }
        List<List<Card>> playable = new ArrayList<>();
        List<List<Card>> allCombinations = CombinationFinder.findAllFourOfAKind(hand, ruleSet);

        for (List<Card> combo : allCombinations) {
            if (ruleSet.isValidCombination(combo)) { 
                if (lastPlayedCards == null || lastPlayedCards.isEmpty()) {
                    // playable.add(combo);
                } else if (ruleSet.canPlayAfter(combo, lastPlayedCards)) {
                    playable.add(combo);
                }
            }
        }
        return playable;
    }
}
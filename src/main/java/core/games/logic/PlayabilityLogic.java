package core.games.logic;

import core.Card;
import core.games.RuleSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayabilityLogic { 

    public static boolean canPlayAfter(List<Card> newCards, List<Card> previousCards, RuleSet ruleSet) {
        if (newCards == null || newCards.isEmpty()) return false;

        // Sử dụng ruleSet để lấy thông tin và thực hiện so sánh
        // Lấy loại tổ hợp thông qua ruleSet
        Object newTypeObject = ruleSet.getCombinationIdentifier(newCards);
        // Kiểm tra xem newTypeObject có phải là một trong các giá trị của enum CombinationType không
        if (!(newTypeObject instanceof RuleSet.CombinationType) || 
            newTypeObject == RuleSet.CombinationType.INVALID) {
            return false;
        }
        RuleSet.CombinationType newType = (RuleSet.CombinationType) newTypeObject;

        // Nếu không có bài trên bàn, chỉ cần bộ bài mới hợp lệ
        if (previousCards == null || previousCards.isEmpty()) {
            return ruleSet.isValidCombination(newCards); 
        }

        Object prevTypeObject = ruleSet.getCombinationIdentifier(previousCards);
        if (!(prevTypeObject instanceof RuleSet.CombinationType) ||
            prevTypeObject == RuleSet.CombinationType.INVALID) {
            // Nếu bài trên bàn không hợp lệ, cho phép đánh đè nếu bài mới hợp lệ
            return newType != RuleSet.CombinationType.INVALID;
        }
        RuleSet.CombinationType prevType = (RuleSet.CombinationType) prevTypeObject;


        List<Card> sortedNewCards = new ArrayList<>(newCards);
        Collections.sort(sortedNewCards, ruleSet.getCardComparator());
        List<Card> sortedPreviousCards = new ArrayList<>(previousCards);
        Collections.sort(sortedPreviousCards, ruleSet.getCardComparator());

        // Lấy giá trị của quân 2 từ ruleSet
        int valueOfTwo = ruleSet.getTwoRankValue(); 

        boolean prevIsSingleTwo = (prevType == RuleSet.CombinationType.SINGLE &&
                                   sortedPreviousCards.size() == 1 &&
                                   ruleSet.getCardRankValue(sortedPreviousCards.get(0)) == valueOfTwo);
        boolean prevIsPairOfTwos = (prevType == RuleSet.CombinationType.PAIR &&
                                    sortedPreviousCards.size() == 2 &&
                                    ruleSet.getCardRankValue(sortedPreviousCards.get(0)) == valueOfTwo &&
                                    ruleSet.getCardRankValue(sortedPreviousCards.get(1)) == valueOfTwo);

        if (newType == RuleSet.CombinationType.FOUR_OF_KIND) {
            if (prevIsSingleTwo) return true;
            if (prevIsPairOfTwos) return true;

            if (prevType == RuleSet.CombinationType.THREE_PAIR_STRAIGHT && ruleSetCanFourOfAKindBeatThreePairStraight(ruleSet)) return true;
            
            if (prevType == RuleSet.CombinationType.FOUR_OF_KIND) { 
                return ruleSet.getCardComparator().compare(
                    ruleSet.getRepresentativeCardForCombination(sortedNewCards),
                    ruleSet.getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            return false;
        }

        if (newType == RuleSet.CombinationType.THREE_PAIR_STRAIGHT) {
            if (prevIsSingleTwo) return true; 
            if (prevType == RuleSet.CombinationType.THREE_PAIR_STRAIGHT) { 
                return ruleSet.getCardComparator().compare(
                    ruleSet.getRepresentativeCardForCombination(sortedNewCards),
                    ruleSet.getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            return false;
        }

        if (newType == RuleSet.CombinationType.FOUR_PAIR_STRAIGHT) {
            if (prevIsSingleTwo || prevIsPairOfTwos) return true;
            if (prevType == RuleSet.CombinationType.THREE_PAIR_STRAIGHT) return true;
            if (prevType == RuleSet.CombinationType.FOUR_OF_KIND) return true;
            if (prevType == RuleSet.CombinationType.FOUR_PAIR_STRAIGHT) { 
                 return ruleSet.getCardComparator().compare(
                    ruleSet.getRepresentativeCardForCombination(sortedNewCards),
                    ruleSet.getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            return false;
        }

        boolean newIsSpecialChainingOrBomb = newType == RuleSet.CombinationType.FOUR_OF_KIND ||
                                             newType == RuleSet.CombinationType.THREE_PAIR_STRAIGHT ||
                                             newType == RuleSet.CombinationType.FOUR_PAIR_STRAIGHT;
        
        if (newIsSpecialChainingOrBomb) {
            return false;
        }

        if (newType != prevType) {
            return false; 
        }

        if (newCards.size() != previousCards.size()) {
            return false;
        }

        Card newRepCard = ruleSet.getRepresentativeCardForCombination(sortedNewCards);
        Card prevRepCard = ruleSet.getRepresentativeCardForCombination(sortedPreviousCards);

        if (newRepCard == null || prevRepCard == null) return false;

        return ruleSet.getCardComparator().compare(newRepCard, prevRepCard) > 0;
    }

    private static boolean ruleSetCanFourOfAKindBeatThreePairStraight(RuleSet ruleSet) {
        if (ruleSet instanceof core.games.tienlen.tienlenmienbac.TienLenMienBacRule) { 
            return true; 
        }
        return false; 
    }
}
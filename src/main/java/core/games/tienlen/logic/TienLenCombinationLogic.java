package core.games.tienlen.logic; 

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet; 

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TienLenCombinationLogic { 

    public static TienLenVariantRuleSet.CombinationType getCombinationType(List<Card> cards, TienLenVariantRuleSet ruleSet) {
        if (cards == null || cards.isEmpty()) {
            return TienLenVariantRuleSet.CombinationType.INVALID;
        }

        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort(ruleSet.getCardComparator());

        int size = sortedCards.size();

        if (size == 4 && isFourOfKind(sortedCards, ruleSet)) return TienLenVariantRuleSet.CombinationType.FOUR_OF_KIND;
        if (size == 6 && isThreePairStraight(sortedCards, ruleSet)) return TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT;
        if (size == 8 && isFourPairStraight(sortedCards, ruleSet)) return TienLenVariantRuleSet.CombinationType.FOUR_PAIR_STRAIGHT;
        if (size >= 3 && isStraight(sortedCards, ruleSet)) {
             
            return TienLenVariantRuleSet.CombinationType.STRAIGHT;
        }

        if (size == 3 && isTriple(sortedCards, ruleSet)) return TienLenVariantRuleSet.CombinationType.TRIPLE;
        if (size == 2 && isPair(sortedCards, ruleSet)) return TienLenVariantRuleSet.CombinationType.PAIR;
        if (size == 1) return TienLenVariantRuleSet.CombinationType.SINGLE;

        return TienLenVariantRuleSet.CombinationType.INVALID;
    }

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

    private static boolean isStraight(List<Card> sortedCards, TienLenVariantRuleSet ruleSet) {
        if (sortedCards.size() < 3) return false;
        for (Card card : sortedCards) {
            if (ruleSet.getCardRankValue(card) == ruleSet.getTwoRankValue()) return false; 
        }
        for (int i = 0; i < sortedCards.size() - 1; i++) {
            if (ruleSet.getCardRankValue(sortedCards.get(i + 1)) - ruleSet.getCardRankValue(sortedCards.get(i)) != 1) {
                return false;
            }
        }
        return true; 
    }
    
    private static boolean isMultiplePairStraight(List<Card> sortedCards, int numPairs, TienLenVariantRuleSet ruleSet) {
        if (sortedCards.size() != numPairs * 2) return false;
        List<Integer> pairRanks = new ArrayList<>();
        for (int i = 0; i < sortedCards.size(); i += 2) {
            if (ruleSet.getCardRankValue(sortedCards.get(i)) != ruleSet.getCardRankValue(sortedCards.get(i + 1))) return false; 
            if (ruleSet.getCardRankValue(sortedCards.get(i)) == ruleSet.getTwoRankValue()) return false; 
            pairRanks.add(ruleSet.getCardRankValue(sortedCards.get(i)));
        }
        Collections.sort(pairRanks); 
        for (int i = 0; i < pairRanks.size() - 1; i++) {
            if (pairRanks.get(i + 1) - pairRanks.get(i) != 1) return false;
        }
        return true;
    }

    private static boolean isThreePairStraight(List<Card> cards, TienLenVariantRuleSet ruleSet) {
        if (cards.size() != 6) return false;
        return isMultiplePairStraight(cards, 3, ruleSet); 
    }

    private static boolean isFourPairStraight(List<Card> cards, TienLenVariantRuleSet ruleSet) {
        if (cards.size() != 8) return false;
        return isMultiplePairStraight(cards, 4, ruleSet);
    }
}
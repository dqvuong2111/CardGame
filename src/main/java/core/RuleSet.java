package core;

import core.Card;

import java.util.Comparator;
import java.util.List;

public interface RuleSet {
    boolean isValidCombination(List<Card> cards);
    boolean canPlayAfter(List<Card> newCards, List<Card> previousCards);
    Comparator<Card> getCardComparator();
    boolean hasStartingCard(List<Card> cards);  
}
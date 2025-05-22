package core.rules;

import core.Card;
import core.RuleSet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class TienLenRule implements RuleSet {
    public enum CombinationType {
        SINGLE,
        PAIR,
        TRIPLE,
        STRAIGHT,
        FOUR_OF_KIND,
        THREE_PAIR_STRAIGHT,
        FOUR_PAIR_STRAIGHT,
        INVALID
    }

    private static final Comparator<Card> TIEN_LEN_CARD_COMPARATOR = new TienLenCardComparator();

    public static CombinationType getCombinationType(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return CombinationType.INVALID;
        }

        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort(TIEN_LEN_CARD_COMPARATOR);

        int size = sortedCards.size();

        switch (size) {
            case 1:
                return CombinationType.SINGLE;
            case 2:
                if (isPair(sortedCards)) return CombinationType.PAIR;
                break;
            case 3:
                if (isTriple(sortedCards)) return CombinationType.TRIPLE;
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
                break;
            case 4:
                if (isFourOfKind(sortedCards)) return CombinationType.FOUR_OF_KIND;
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
                break;
            case 5:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
                break;
            case 6:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
                if (isThreePairStraight(sortedCards)) return CombinationType.THREE_PAIR_STRAIGHT;
                break;
            case 7:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
                break;
            case 8:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
                if (isFourPairStraight(sortedCards)) return CombinationType.FOUR_PAIR_STRAIGHT;
                break;
            case 9:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
                if (isThreePairStraight(sortedCards)) return CombinationType.THREE_PAIR_STRAIGHT;
                break;
            case 10:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
                break;
            case 11:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
                break;
            case 12:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
                if (isFourPairStraight(sortedCards)) return CombinationType.FOUR_PAIR_STRAIGHT;
                break;
        }

        return CombinationType.INVALID;
    }

    public String getCardsDisplay(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return "Không có bài nào";
        }
        return cards.stream()
                .map(Card::toString)
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean isValidCombination(List<Card> cards) {
        return getCombinationType(cards) != CombinationType.INVALID;
    }

    @Override
    public boolean canPlayAfter(List<Card> newCards, List<Card> previousCards) {
        if (newCards == null || newCards.isEmpty()) {
            return false;
        }
        if (previousCards == null || previousCards.isEmpty()) {
            return false;
        }

        CombinationType newType = getCombinationType(newCards);
        CombinationType prevType = getCombinationType(previousCards);

        if (newType == CombinationType.INVALID || prevType == CombinationType.INVALID) {
            return false;
        }

        List<Card> sortedNewCards = new ArrayList<>(newCards);
        sortedNewCards.sort(TIEN_LEN_CARD_COMPARATOR);
        List<Card> sortedPreviousCards = new ArrayList<>(previousCards);
        sortedPreviousCards.sort(TIEN_LEN_CARD_COMPARATOR);

        if (newType == CombinationType.FOUR_OF_KIND && prevType == CombinationType.SINGLE && getTienLenValue(sortedPreviousCards.get(0)) == 15) {
            return true;
        }
        if (newType == CombinationType.THREE_PAIR_STRAIGHT) {
            if (prevType == CombinationType.SINGLE && getTienLenValue(sortedPreviousCards.get(0)) == 15) return true;
            if (prevType == CombinationType.PAIR && getTienLenValue(sortedPreviousCards.get(0)) == 15) return true;
            if (prevType == CombinationType.THREE_PAIR_STRAIGHT) {
                return getTienLenValue(sortedNewCards.get(sortedNewCards.size() - 1)) > getTienLenValue(sortedPreviousCards.get(sortedPreviousCards.size() - 1));
            }
        }
        if (newType == CombinationType.FOUR_PAIR_STRAIGHT) {
            if (prevType == CombinationType.SINGLE && getTienLenValue(sortedPreviousCards.get(0)) == 15) return true;
            if (prevType == CombinationType.PAIR && getTienLenValue(sortedPreviousCards.get(0)) == 15) return true;
            if (prevType == CombinationType.FOUR_OF_KIND) return true;
            if (prevType == CombinationType.THREE_PAIR_STRAIGHT) return true;
            if (prevType == CombinationType.FOUR_PAIR_STRAIGHT) {
                return getTienLenValue(sortedNewCards.get(sortedNewCards.size() - 1)) > getTienLenValue(sortedPreviousCards.get(sortedPreviousCards.size() - 1));
            }
        }

        if (newType != prevType || newCards.size() != previousCards.size()) {
            return false;
        }

        Card newMaxCard = sortedNewCards.get(sortedNewCards.size() - 1);
        Card prevMaxCard = sortedPreviousCards.get(sortedPreviousCards.size() - 1);

        return TIEN_LEN_CARD_COMPARATOR.compare(newMaxCard, prevMaxCard) > 0;
    }

    private static boolean isPair(List<Card> cards) {
        return cards.size() == 2 && getTienLenValue(cards.get(0)) == getTienLenValue(cards.get(1));
    }

    private static boolean isTriple(List<Card> cards) {
        return cards.size() == 3 && getTienLenValue(cards.get(0)) == getTienLenValue(cards.get(1)) && getTienLenValue(cards.get(1)) == getTienLenValue(cards.get(2));
    }

    private static boolean isFourOfKind(List<Card> cards) {
        return cards.size() == 4 && getTienLenValue(cards.get(0)) == getTienLenValue(cards.get(1)) &&
                getTienLenValue(cards.get(1)) == getTienLenValue(cards.get(2)) &&
                getTienLenValue(cards.get(2)) == getTienLenValue(cards.get(3));
    }

    private static boolean isStraight(List<Card> cards) {
        if (cards.size() < 3) return false;

        for (Card card : cards) {
            if (getTienLenValue(card) == 15) {
                return false;
            }
        }

        List<Card> sortedByTLValue = new ArrayList<>(cards);
        sortedByTLValue.sort(TIEN_LEN_CARD_COMPARATOR);

        for (int i = 0; i < sortedByTLValue.size() - 1; i++) {
            if (getTienLenValue(sortedByTLValue.get(i + 1)) - getTienLenValue(sortedByTLValue.get(i)) != 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean isThreePairStraight(List<Card> cards) {
        if (cards.size() != 6 && cards.size() != 9 && cards.size() != 12) return false;
        return isMultiplePairStraight(cards, 3);
    }

    private static boolean isFourPairStraight(List<Card> cards) {
        if (cards.size() != 8 && cards.size() != 10 && cards.size() != 12) return false;
        return isMultiplePairStraight(cards, 4);
    }

    private static boolean isMultiplePairStraight(List<Card> cards, int numPairs) {
        if (cards.size() % 2 != 0 || cards.size() < numPairs * 2) return false;

        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort(TIEN_LEN_CARD_COMPARATOR);

        List<Integer> pairValues = new ArrayList<>();
        for (int i = 0; i < sortedCards.size(); i += 2) {
            if (i + 1 >= sortedCards.size()) return false;
            Card card1 = sortedCards.get(i);
            Card card2 = sortedCards.get(i + 1);

            if (getTienLenValue(card1) != getTienLenValue(card2)) {
                return false;
            }
            pairValues.add(getTienLenValue(card1));
        }

        if (pairValues.size() < numPairs) return false;

        for (int i = 0; i < pairValues.size() - 1; i++) {
            if (pairValues.get(i + 1) - pairValues.get(i) != 1) {
                return false;
            }
        }

        for(Integer val : pairValues) {
            if (val == 15) return false;
        }

        return true;
    }

    public static int getTienLenValue(Card card) {
        if (card.getRank() == Card.Rank.TWO) {
            return 15;
        } else if (card.getRank() == Card.Rank.ACE) {
            return 14;
        } else if (card.getRank() == Card.Rank.KING) {
            return 13;
        } else if (card.getRank() == Card.Rank.QUEEN) {
            return 12;
        } else if (card.getRank() == Card.Rank.JACK) {
            return 11;
        } else if (card.getRank() == Card.Rank.TEN) {
            return 10;
        }
        return card.getRank().getValue();
    }

    private static class TienLenCardComparator implements Comparator<Card> {
        @Override
        public int compare(Card card1, Card card2) {
            int value1 = getTienLenValue(card1);
            int value2 = getTienLenValue(card2);

            if (value1 != value2) {
                return Integer.compare(value1, value2);
            } else {
                return Integer.compare(getSuitValue(card1.getSuit()), getSuitValue(card2.getSuit()));
            }
        }
    }

    private static int getSuitValue(Card.Suit suit) {
        return switch (suit) {
            case CLUBS -> 0;
            case DIAMONDS -> 1;
            case HEARTS -> 2;
            case SPADES -> 3;
        };
    }

    @Override
    public Comparator<Card> getCardComparator() {
        return TIEN_LEN_CARD_COMPARATOR;
    }

    @Override
    public boolean hasStartingCard(List<Card> cards) {
        return cards.contains(new Card(Card.Suit.SPADES, Card.Rank.THREE));
    }
}
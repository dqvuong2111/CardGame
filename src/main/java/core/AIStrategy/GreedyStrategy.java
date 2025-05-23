package core.AIStrategy;

import core.Card;
import core.RuleSet;
import core.rules.TienLenRule; // Cần để truy cập CombinationType và getTienLenValue
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GreedyStrategy implements AIStrategy {

    @Override
    public List<Card> chooseCards(List<Card> currentHand, List<Card> lastPlayedCards, RuleSet ruleSet, boolean isFirstTurn) {
        if (currentHand.isEmpty()) {
            return new ArrayList<>();
        }

        // Logic từ AIPlayer.chooseGreedyCards
        if (lastPlayedCards == null || lastPlayedCards.isEmpty()) { // Trong code gốc là lastPlayed.isEmpty()
            if (isFirstTurn) { //
                Card threeSpades = new Card(Card.Suit.SPADES, Card.Rank.THREE);
                for (Card card : currentHand) {
                    if (card.equals(threeSpades)) { //
                        return List.of(card); //
                    }
                }
                 // Nếu không có 3 Bích, đánh lá nhỏ nhất (hoặc bỏ lượt nếu muốn chặt chẽ)
                return List.of(findLowestSingleCard(currentHand, ruleSet, false)); // isFirstTurn = false vì đã qua kiểm tra 3 bích
            } else {
                return List.of(findLowestSingleCard(currentHand, ruleSet, false)); //
            }
        }

        TienLenRule.CombinationType lastType = TienLenRule.getCombinationType(lastPlayedCards); //
        if (lastType == null || lastType == TienLenRule.CombinationType.INVALID) { //
            return new ArrayList<>();
        }

        List<Card> bestPlay = null; //
        switch (lastType) { //
            case SINGLE:
                bestPlay = findSmallestPlayableCard(currentHand, lastPlayedCards, ruleSet); //
                break;
            case PAIR:
                bestPlay = findSmallestPlayablePair(currentHand, lastPlayedCards, ruleSet); //
                break;
            case TRIPLE:
                bestPlay = findSmallestPlayableTriple(currentHand, lastPlayedCards, ruleSet); //
                break;
            case STRAIGHT:
                bestPlay = findSmallestPlayableStraight(currentHand, lastPlayedCards, ruleSet); //
                break;
            case FOUR_OF_KIND:
                bestPlay = findPlayableFourOfKind(currentHand, lastPlayedCards, ruleSet); //
                break;
            // Cần thêm các case cho THREE_PAIR_STRAIGHT, FOUR_PAIR_STRAIGHT nếu AI Greedy cần xử lý
            default:
                 return new ArrayList<>(); // Nếu là loại không xử lý, AI bỏ lượt
        }

        return bestPlay != null ? bestPlay : new ArrayList<>(); //
    }

    // CHUYỂN CÁC HÀM HELPER TỪ AIPlayer.java VÀO ĐÂY
    // Các hàm này giờ sẽ nhận 'currentHand' và 'ruleSet' làm tham số thay vì dùng this.hand và this.ruleSet

    private Card findLowestSingleCard(List<Card> currentHand, RuleSet ruleSet, boolean isFirstTurn) { // Thêm currentHand, ruleSet
        // Logic từ AIPlayer.findLowestSingleCard
        // Đã có sửa đổi để nhận isFirstTurn
        if (isFirstTurn) {
            Card threeSpades = new Card(Card.Suit.SPADES, Card.Rank.THREE);
            if(currentHand.contains(threeSpades)) { //
                 return threeSpades; //
            }
        }
        // Nếu không phải lượt đầu hoặc không có 3 bích, trả về lá nhỏ nhất
        // Cần sắp xếp hoặc dùng Collections.min với comparator của ruleSet
        if (currentHand.isEmpty()) return null;
        List<Card> sortedHand = new ArrayList<>(currentHand);
        sortedHand.sort(ruleSet.getCardComparator());
        return sortedHand.get(0); // Giả sử Collections.min(hand) trong code gốc dùng comparator chuẩn
                                 // Tốt hơn là sắp xếp và lấy phần tử đầu tiên theo ruleSet.getCardComparator()
    }

    private List<Card> findSmallestPlayableCard(List<Card> currentHand, List<Card> lastPlayed, RuleSet ruleSet) { // Thêm currentHand, ruleSet
        // Logic từ AIPlayer.findSmallestPlayableCard
        Card lastCard = lastPlayed.get(0);
        Card smallest = null;
        Comparator<Card> tlComparator = ruleSet.getCardComparator();
        for (Card card : currentHand) {
            if (ruleSet.canPlayAfter(List.of(card), lastPlayed)) { // lastPlayed thay vì List.of(lastCard) để đúng luật
                if (smallest == null || tlComparator.compare(card, smallest) < 0) {
                    smallest = card;
                }
            }
        }
        return smallest != null ? List.of(smallest) : new ArrayList<>();
    }

    private List<List<Card>> findAllPairs(List<Card> currentHand, RuleSet ruleSet) { // Thêm currentHand, ruleSet
        // Logic từ AIPlayer.findAllPairs
        List<List<Card>> pairs = new ArrayList<>();
        List<Card> sortedHand = new ArrayList<>(currentHand);
        sortedHand.sort(ruleSet.getCardComparator());
        for (int i = 0; i < sortedHand.size() - 1; i++) {
            if (sortedHand.get(i).getRank() == sortedHand.get(i + 1).getRank()) {
                List<Card> pair = new ArrayList<>();
                pair.add(sortedHand.get(i));
                pair.add(sortedHand.get(i + 1));
                pairs.add(pair);
                i++;
            }
        }
        return pairs;
    }
    // ... Thêm findSmallestPlayablePair, findAllTriples, findSmallestPlayableTriple,
    // findAllStraights, findSmallestPlayableStraight, findAllFourOfKinds, findPlayableFourOfKind
    // và điều chỉnh chúng để nhận currentHand, ruleSet khi cần.
    // Ví dụ:
    private List<Card> findSmallestPlayablePair(List<Card> currentHand, List<Card> lastPlayed, RuleSet ruleSet) {
        List<List<Card>> pairs = findAllPairs(currentHand, ruleSet);
        if (pairs.isEmpty()) return new ArrayList<>();
        pairs.sort(Comparator.comparing(pair -> TienLenRule.getTienLenValue(pair.get(0))));
        for (List<Card> pair : pairs) {
            if (ruleSet.canPlayAfter(pair, lastPlayed)) {
                return pair;
            }
        }
        return new ArrayList<>();
    }

    private List<List<Card>> findAllTriples(List<Card> currentHand, RuleSet ruleSet) {
        List<List<Card>> triples = new ArrayList<>();
        List<Card> sortedHand = new ArrayList<>(currentHand);
        sortedHand.sort(ruleSet.getCardComparator());
        for (int i = 0; i < sortedHand.size() - 2; i++) {
            if (sortedHand.get(i).getRank() == sortedHand.get(i+1).getRank() &&
                sortedHand.get(i).getRank() == sortedHand.get(i+2).getRank()) {
                List<Card> triple = new ArrayList<>();
                triple.add(sortedHand.get(i));
                triple.add(sortedHand.get(i+1));
                triple.add(sortedHand.get(i+2));
                triples.add(triple);
                i += 2;
            }
        }
        return triples;
    }

    private List<Card> findSmallestPlayableTriple(List<Card> currentHand, List<Card> lastPlayed, RuleSet ruleSet) {
        List<List<Card>> triples = findAllTriples(currentHand, ruleSet);
        if (triples.isEmpty()) return new ArrayList<>();
        triples.sort(Comparator.comparing(triple -> TienLenRule.getTienLenValue(triple.get(0))));
        for (List<Card> triple : triples) {
            if (ruleSet.canPlayAfter(triple, lastPlayed)) {
                return triple;
            }
        }
        return new ArrayList<>();
    }

    private List<List<Card>> findAllStraights(List<Card> currentHand, RuleSet ruleSet, int length) { // Thêm currentHand, ruleSet
        // Logic từ AIPlayer.findAllStraights, sử dụng currentHand và ruleSet
        // Cần điều chỉnh lại logic tìm sảnh cho chính xác hơn nếu cần, logic hiện tại trong AIPlayer có chú thích về hạn chế
        if (length < 3 || currentHand.size() < length) {
            return new ArrayList<>();
        }
        List<List<Card>> straights = new ArrayList<>();
        List<Card> sortedHandCopy = new ArrayList<>(currentHand);
        sortedHandCopy.sort(ruleSet.getCardComparator());

        List<Card> uniqueValueCards = new ArrayList<>();
        if (!sortedHandCopy.isEmpty()){
            uniqueValueCards.add(sortedHandCopy.get(0));
            for (int k = 1; k < sortedHandCopy.size(); k++) {
                if (TienLenRule.getTienLenValue(sortedHandCopy.get(k)) > TienLenRule.getTienLenValue(sortedHandCopy.get(k-1))) {
                    uniqueValueCards.add(sortedHandCopy.get(k));
                }
            }
        }

        for (int i = 0; i <= uniqueValueCards.size() - length; i++) {
            boolean isStraight = true;
            for (int j = 1; j < length; j++) {
                 int prevTLValue = TienLenRule.getTienLenValue(uniqueValueCards.get(i+j-1));
                 int currTLValue = TienLenRule.getTienLenValue(uniqueValueCards.get(i+j));
                 if (currTLValue - prevTLValue != 1) {
                     isStraight = false;
                     break;
                 }
            }
            if (isStraight) {
                 int lastStraightValue = TienLenRule.getTienLenValue(uniqueValueCards.get(i + length - 1));
                 if (lastStraightValue == 15) { // Heo không nằm trong sảnh (trừ sảnh rồng đặc biệt)
                     isStraight = false;
                 }
            }
            if (isStraight) {
                List<Card> straight = new ArrayList<>();
                for (int j = 0; j < length; j++) {
                    straight.add(uniqueValueCards.get(i+j));
                }
                straights.add(straight);
            }
        }
        return straights;
    }

    private List<Card> findSmallestPlayableStraight(List<Card> currentHand, List<Card> lastPlayed, RuleSet ruleSet) {
        int length = lastPlayed.size();
        List<List<Card>> straights = findAllStraights(currentHand, ruleSet, length);
        if (straights.isEmpty()) return new ArrayList<>();
        straights.sort(Comparator.comparing(
                straight -> Collections.max(straight, ruleSet.getCardComparator()),
                ruleSet.getCardComparator()
        ));
        for (List<Card> straight : straights) {
            if (ruleSet.canPlayAfter(straight, lastPlayed)) {
                return straight;
            }
        }
        return new ArrayList<>();
    }
     private List<List<Card>> findAllFourOfKinds(List<Card> currentHand, RuleSet ruleSet) { // Thêm currentHand, ruleSet
        // Logic từ AIPlayer.findAllFourOfKinds
        List<List<Card>> fourOfKinds = new ArrayList<>();
        List<Card> sortedHand = new ArrayList<>(currentHand);
        sortedHand.sort(ruleSet.getCardComparator());

        for (int i = 0; i < sortedHand.size() - 3; i++) {
            if (sortedHand.get(i).getRank() == sortedHand.get(i + 1).getRank() &&
                sortedHand.get(i).getRank() == sortedHand.get(i + 2).getRank() &&
                sortedHand.get(i).getRank() == sortedHand.get(i + 3).getRank()) {
                List<Card> fourOfKind = new ArrayList<>();
                fourOfKind.add(sortedHand.get(i));
                fourOfKind.add(sortedHand.get(i + 1));
                fourOfKind.add(sortedHand.get(i + 2));
                fourOfKind.add(sortedHand.get(i + 3));
                fourOfKinds.add(fourOfKind);
                i += 3;
            }
        }
        return fourOfKinds;
    }
    private List<Card> findPlayableFourOfKind(List<Card> currentHand, List<Card> lastPlayed, RuleSet ruleSet) { // Thêm currentHand, ruleSet
        // Logic từ AIPlayer.findPlayableFourOfKind
         List<List<Card>> fourOfKinds = findAllFourOfKinds(currentHand, ruleSet);
        if (fourOfKinds.isEmpty()) return new ArrayList<>();

        // Tứ quý không cần sắp xếp lại vì chỉ so sánh rank
        for (List<Card> fourOfKind : fourOfKinds) {
            if (ruleSet.canPlayAfter(fourOfKind, lastPlayed)) {
                return fourOfKind;
            }
        }
        return new ArrayList<>();
    }
}
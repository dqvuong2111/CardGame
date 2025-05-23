package core.AIStrategy;

import core.Card;
import core.RuleSet;
import core.rules.TienLenRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SmartStrategy implements AIStrategy {

    @Override
    public List<Card> chooseCards(List<Card> currentHand, List<Card> lastPlayedCards, RuleSet ruleSet, boolean isFirstTurn) {
        if (currentHand.isEmpty()) {
            return new ArrayList<>();
        }
        // Logic từ AIPlayer.chooseSmartCards
        if (lastPlayedCards == null || lastPlayedCards.isEmpty()) { //
            // Xử lý lượt đầu tiên (isFirstTurn)
            Card threeSpades = new Card(Card.Suit.SPADES, Card.Rank.THREE);
            if (isFirstTurn && currentHand.contains(threeSpades)) { //
                // Ưu tiên đánh 3 bích nếu là lượt đầu
                // Tìm tổ hợp nhỏ nhất chứa 3 bích
                List<List<Card>> possiblePlays = new ArrayList<>();
                possiblePlays.add(List.of(threeSpades)); // Đánh lẻ 3 bích

                List<List<Card>> pairs = findAllPairs(currentHand, ruleSet);
                for(List<Card> pair : pairs) if(pair.contains(threeSpades)) possiblePlays.add(pair);

                List<List<Card>> triples = findAllTriples(currentHand, ruleSet);
                for(List<Card> triple : triples) if(triple.contains(threeSpades)) possiblePlays.add(triple);

                List<List<Card>> straights = findAllStraights(currentHand, ruleSet, 3); // Sảnh từ 3 lá
                for(List<Card> straight : straights) if(straight.contains(threeSpades)) possiblePlays.add(straight);
                // Thêm logic cho các loại sảnh dài hơn nếu cần

                if (!possiblePlays.isEmpty()) {
                    // Chọn nước đi "tốt nhất" trong các nước có 3 bích (ví dụ: sảnh > bộ > đôi > lẻ)
                    // Tạm thời trả về đánh lẻ 3 bích
                    return List.of(threeSpades);
                }
            }
            // Logic khi không phải lượt đầu hoặc không có 3 bích
            if (currentHand.size() <= 3) { //
                return List.of(findLowestSingleCard(currentHand, ruleSet, isFirstTurn)); // isFirstTurn đã được xử lý
            }
            List<List<Card>> straights = findAllStraights(currentHand, ruleSet, 3); //
            if (!straights.isEmpty()) {
                // Logic chọn sảnh đầu tiên trong code gốc có thể cần xem lại nếu isFirstTurn đã được xử lý
                return straights.get(0); //
            }
            List<List<Card>> pairs = findAllPairs(currentHand, ruleSet); //
            if (!pairs.isEmpty()) {
                 // Logic chọn đôi đầu tiên
                return pairs.get(0); //
            }
            return List.of(findLowestSingleCard(currentHand, ruleSet, isFirstTurn)); //
        }

        // Nếu có bài trước đó
        int cardsLeft = currentHand.size(); //
        if (cardsLeft <= 3) { //
            // Gọi GreedyStrategy (hoặc copy logic)
            // Để đơn giản, ở đây có thể gọi lại logic của Greedy
            GreedyStrategy greedy = new GreedyStrategy(); // Không lý tưởng, nên tái cấu trúc hàm helper
            return greedy.chooseCards(currentHand, lastPlayedCards, ruleSet, isFirstTurn); //
        }

        TienLenRule.CombinationType lastType = TienLenRule.getCombinationType(lastPlayedCards); //
        if (lastType == null || lastType == TienLenRule.CombinationType.INVALID) { //
            return new ArrayList<>();
        }

        switch (lastType) { //
            case SINGLE:
                return findOptimalSinglePlay(currentHand, lastPlayedCards, ruleSet); //
            case PAIR:
                return findOptimalPairPlay(currentHand, lastPlayedCards, ruleSet); //
            case TRIPLE:
                return findSmallestPlayableTriple(currentHand, lastPlayedCards, ruleSet); //
            case STRAIGHT:
                return findSmallestPlayableStraight(currentHand, lastPlayedCards, ruleSet); //
            case FOUR_OF_KIND:
                return findPlayableFourOfKind(currentHand, lastPlayedCards, ruleSet); //
            default:
                return new ArrayList<>(); //
        }
    }

    // CHUYỂN CÁC HÀM HELPER TỪ AIPlayer.java VÀO ĐÂY
    // findOptimalSinglePlay, findOptimalPairPlay, containsAllCards
    // findAllPairs, findAllTriples, findAllFourOfKinds, findAllStraights
    // findLowestSingleCard, findSmallestPlayableCard, findSmallestPlayablePair, ... (nếu Greedy không dùng chung)
    // Các hàm này giờ sẽ nhận 'currentHand' và 'ruleSet' làm tham số.

    private List<Card> findOptimalSinglePlay(List<Card> currentHand, List<Card> lastPlayed, RuleSet ruleSet) { // Thêm currentHand, ruleSet
        // Logic từ AIPlayer.findOptimalSinglePlay
        List<Card> playableCards = new ArrayList<>();
        Card lastCard = lastPlayed.get(0);
        Comparator<Card> tlComparator = ruleSet.getCardComparator();
        for (Card card : currentHand) {
            if (tlComparator.compare(card, lastCard) > 0 && ruleSet.canPlayAfter(List.of(card), lastPlayed)) { // Thêm check canPlayAfter
                playableCards.add(card);
            }
        }
        if (playableCards.isEmpty()) return new ArrayList<>();
        Collections.sort(playableCards, ruleSet.getCardComparator());

        List<List<Card>> pairs = findAllPairs(currentHand, ruleSet);
        List<List<Card>> straights = findAllStraights(currentHand, ruleSet, 3); // Sảnh từ 3 lá

        for (Card card : playableCards) {
            boolean isInCombination = false;
            for (List<Card> pair : pairs) if (pair.contains(card)) {isInCombination = true; break;}
            if (isInCombination) continue;
            for (List<Card> straight : straights) if (straight.contains(card)) {isInCombination = true; break;}
            if (!isInCombination) return List.of(card);
        }
        return List.of(playableCards.get(0));
    }

    private List<Card> findOptimalPairPlay(List<Card> currentHand, List<Card> lastPlayed, RuleSet ruleSet) { // Thêm currentHand, ruleSet
        // Logic từ AIPlayer.findOptimalPairPlay
        List<List<Card>> pairs = findAllPairs(currentHand, ruleSet);
        if (pairs.isEmpty()) return new ArrayList<>();
        pairs.sort(Comparator.comparing(pair -> TienLenRule.getTienLenValue(pair.get(0))));

        List<List<Card>> triples = findAllTriples(currentHand, ruleSet);
        List<List<Card>> fourOfKinds = findAllFourOfKinds(currentHand, ruleSet);

        for (List<Card> pair : pairs) {
            if (ruleSet.canPlayAfter(pair, lastPlayed)) {
                boolean isPartOfHigherCombination = false;
                for (List<Card> triple : triples) if (containsAllCards(triple, pair)) {isPartOfHigherCombination = true; break;}
                if (isPartOfHigherCombination) continue;
                for (List<Card> four : fourOfKinds) if (containsAllCards(four, pair)) {isPartOfHigherCombination = true; break;}
                if (!isPartOfHigherCombination) return pair;
            }
        }
        for (List<Card> pair : pairs) { // Nếu tất cả đều là một phần của tổ hợp cao hơn
            if (ruleSet.canPlayAfter(pair, lastPlayed)) return pair;
        }
        return new ArrayList<>();
    }

    private boolean containsAllCards(List<Card> cards1, List<Card> cards2) { //
        return cards1.containsAll(cards2);
    }

    // Copy các hàm findAll..., findSmallestPlayable... từ GreedyStrategy nếu cần thiết và không muốn tạo instance GreedyStrategy
    // Hoặc tốt hơn là tạo một lớp tiện ích (AIUtils) cho các hàm tìm tổ hợp này.
    // Dưới đây là ví dụ copy một vài hàm, bạn cần hoàn thiện phần này.
    private Card findLowestSingleCard(List<Card> currentHand, RuleSet ruleSet, boolean isFirstTurn) {
        if (isFirstTurn) {
            Card threeSpades = new Card(Card.Suit.SPADES, Card.Rank.THREE);
            if(currentHand.contains(threeSpades)) return threeSpades;
        }
        if (currentHand.isEmpty()) return null;
        List<Card> sortedHand = new ArrayList<>(currentHand);
        sortedHand.sort(ruleSet.getCardComparator());
        return sortedHand.get(0);
    }
    private List<List<Card>> findAllPairs(List<Card> currentHand, RuleSet ruleSet) {
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
    private List<List<Card>> findAllFourOfKinds(List<Card> currentHand, RuleSet ruleSet) {
        List<List<Card>> fourOfKinds = new ArrayList<>();
        List<Card> sortedHand = new ArrayList<>(currentHand);
        sortedHand.sort(ruleSet.getCardComparator());
        for (int i = 0; i < sortedHand.size() - 3; i++) {
            if (sortedHand.get(i).getRank() == sortedHand.get(i+1).getRank() &&
                sortedHand.get(i).getRank() == sortedHand.get(i+2).getRank() &&
                sortedHand.get(i).getRank() == sortedHand.get(i+3).getRank()) {
                List<Card> fourOfKind = new ArrayList<>();
                fourOfKind.add(sortedHand.get(i));
                fourOfKind.add(sortedHand.get(i+1));
                fourOfKind.add(sortedHand.get(i+2));
                fourOfKind.add(sortedHand.get(i+3));
                fourOfKinds.add(fourOfKind);
                i += 3;
            }
        }
        return fourOfKinds;
    }
    private List<List<Card>> findAllStraights(List<Card> currentHand, RuleSet ruleSet, int length) {
        if (length < 3 || currentHand.size() < length) return new ArrayList<>();
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
                 if (currTLValue - prevTLValue != 1) {isStraight = false; break;}
            }
            if (isStraight) {
                 int lastStraightValue = TienLenRule.getTienLenValue(uniqueValueCards.get(i + length - 1));
                 if (lastStraightValue == 15) isStraight = false;
            }
            if (isStraight) {
                List<Card> straight = new ArrayList<>();
                for (int j = 0; j < length; j++) straight.add(uniqueValueCards.get(i+j));
                straights.add(straight);
            }
        }
        return straights;
    }
    // Các hàm findSmallestPlayable... tương tự như GreedyStrategy nếu cần
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
    private List<Card> findPlayableFourOfKind(List<Card> currentHand, List<Card> lastPlayed, RuleSet ruleSet) {
        List<List<Card>> fourOfKinds = findAllFourOfKinds(currentHand, ruleSet);
        if (fourOfKinds.isEmpty()) return new ArrayList<>();
        for (List<Card> fourOfKind : fourOfKinds) {
            if (ruleSet.canPlayAfter(fourOfKind, lastPlayed)) {
                return fourOfKind;
            }
        }
        return new ArrayList<>();
    }
}
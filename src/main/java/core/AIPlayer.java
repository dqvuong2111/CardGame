package core;

import core.rules.TienLenRule;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Lớp AIPlayer mở rộng từ Player để triển khai chiến lược cho máy
 */
public class AIPlayer extends Player {
    public enum AIStrategy {
        RANDOM,   // Chiến lược ngẫu nhiên
        GREEDY,   // Chiến lược tham lam (luôn đánh lá nhỏ nhất có thể)
        SMART     // Chiến lược thông minh hơn
    }
    
    private AIStrategy strategy;
    private RuleSet ruleSet;
    
    public AIPlayer(String name, AIStrategy strategy, RuleSet ruleSet) {
        super(name, true);
        this.strategy = strategy;
        this.ruleSet = ruleSet;
    }
    
    /**
     * Chọn lá bài để đánh dựa trên chiến lược AI
     */
    public List<Card> chooseCards(List<Card> lastPlayed) {
        switch (strategy) {
            case RANDOM:
                return chooseRandomCards(lastPlayed);
            case GREEDY:
                return chooseGreedyCards(lastPlayed);
            case SMART:
                return chooseSmartCards(lastPlayed);
            default:
                return new ArrayList<>();
        }
    }
    
    /**
     * Chiến lược ngẫu nhiên: Chọn bài ngẫu nhiên hợp lệ
     */
    private List<Card> chooseRandomCards(List<Card> lastPlayed) {
        // Nếu không có bài trước đó, đánh một lá bài ngẫu nhiên
        if (lastPlayed == null || lastPlayed.isEmpty()) {
            if (!hand.isEmpty()) {
                return List.of(hand.get(0));
            }
            return new ArrayList<>();
        }
        
        // Thử các tổ hợp bài ngẫu nhiên
        for (Card card : hand) {
            List<Card> singleCard = List.of(card);
            if (this.ruleSet.canPlayAfter(singleCard, lastPlayed)) {
                return singleCard;
            }
        }
        
        // Không có lá nào có thể đánh, bỏ lượt
        return new ArrayList<>();
    }
    
    /**
     * Chiến lược tham lam: Đánh lá nhỏ nhất có thể
     */
    private List<Card> chooseGreedyCards(List<Card> lastPlayed) {
        // Nếu không có bài trước đó, đánh lá nhỏ nhất
        if (lastPlayed == null || lastPlayed.isEmpty()) {
            if (!hand.isEmpty()) {
                return List.of(Collections.min(hand));
            }
            return new ArrayList<>();
        }
        
        TienLenRule.CombinationType lastType = TienLenRule.getCombinationType(lastPlayed);
        if (lastType == null) {
            return new ArrayList<>();
        }
        
        // Tìm tổ hợp nhỏ nhất có thể đánh
        List<Card> bestPlay = null;
        
        // Xử lý từng loại bài
        switch (lastType) {
            case SINGLE:
                bestPlay = findSmallestPlayableCard(lastPlayed);
                break;
            case PAIR:
                bestPlay = findSmallestPlayablePair(lastPlayed);
                break;
            case TRIPLE:
                bestPlay = findSmallestPlayableTriple(lastPlayed);
                break;
            case STRAIGHT:
                bestPlay = findSmallestPlayableStraight(lastPlayed);
                break;
            case FOUR_OF_KIND:
                // Với tứ quý, không có chiến lược cụ thể, chỉ tìm tứ quý lớn hơn
                bestPlay = findPlayableFourOfKind(lastPlayed);
                break;
        }
        
        return bestPlay != null ? bestPlay : new ArrayList<>();
    }
    
    /**
     * Chiến lược thông minh hơn
     */
    private List<Card> chooseSmartCards(List<Card> lastPlayed) {
        // Nếu không có bài trước đó
        if (lastPlayed == null || lastPlayed.isEmpty()) {
            // Nếu có ít bài, đánh các lá đơn lẻ trước
            if (hand.size() <= 3) {
                return List.of(findLowestSingleCard());
            }
            
            // Ưu tiên đánh sảnh nếu có
            List<List<Card>> straights = findAllStraights(3); // Tìm sảnh từ 3 lá trở lên
            if (!straights.isEmpty()) {
                return straights.get(0); // Đánh sảnh đầu tiên
            }
            
            // Rồi đến đôi
            List<List<Card>> pairs = findAllPairs();
            if (!pairs.isEmpty()) {
                return pairs.get(0);
            }
            
            // Cuối cùng mới đánh lá đơn, ưu tiên lá nhỏ trước
            return List.of(findLowestSingleCard());
        }
        
        // Nếu có bài trước đó, chiến lược tùy thuộc vào số bài còn lại
        int cardsLeft = hand.size();
        
        // Nếu còn ít bài, cố gắng đánh hết nhanh nhất có thể
        if (cardsLeft <= 3) {
            return chooseGreedyCards(lastPlayed); // Dùng chiến lược tham lam
        }
        
        // Với số bài nhiều, dùng chiến lược hỗn hợp
        TienLenRule.CombinationType lastType = TienLenRule.getCombinationType(lastPlayed);
        if (lastType == null) {
            return new ArrayList<>();
        }
        
        // Thử tìm tổ hợp có thể đánh mà không phá vỡ các tổ hợp tốt khác
        switch (lastType) {
            case SINGLE:
                // Đối với lá đơn, tìm lá đơn nhỏ nhất mà không phá vỡ đôi hoặc sảnh
                return findOptimalSinglePlay(lastPlayed);
            case PAIR:
                // Đối với đôi, tìm đôi nhỏ nhất mà không phá vỡ bộ ba hoặc tứ quý
                return findOptimalPairPlay(lastPlayed);
            case TRIPLE:
                // Đối với bộ ba, đánh luôn
                return findSmallestPlayableTriple(lastPlayed);
            case STRAIGHT:
                // Đối với sảnh, đánh luôn
                return findSmallestPlayableStraight(lastPlayed);
            case FOUR_OF_KIND:
                // Đối với tứ quý, đánh luôn
                return findPlayableFourOfKind(lastPlayed);
            default:
                return new ArrayList<>();
        }
    }
    
    /**
     * Tìm lá đơn tối ưu để đánh mà không phá vỡ đôi hoặc sảnh
     */
    private List<Card> findOptimalSinglePlay(List<Card> lastPlayed) {
        // Tìm tất cả các lá đơn có thể đánh
        List<Card> playableCards = new ArrayList<>();
        Card lastCard = lastPlayed.get(0);
        
        Comparator<Card> tlComparator = this.ruleSet.getCardComparator();
        
        for (Card card : hand) {
            if (tlComparator.compare(card, lastCard) > 0) {
                playableCards.add(card);
            }
        }
        
        if (playableCards.isEmpty()) {
            return new ArrayList<>(); // Không có lá nào có thể đánh
        }
        
        // Sắp xếp các lá có thể đánh theo thứ tự tăng dần
        Collections.sort(playableCards, this.ruleSet.getCardComparator()); 
        
        // Tìm các tổ hợp hiện có trong bài
        List<List<Card>> pairs = findAllPairs();
        List<List<Card>> straights = findAllStraights(3);
        
        // Tìm lá không nằm trong bất kỳ tổ hợp nào
        for (Card card : playableCards) {
            boolean isInCombination = false;
            
            // Kiểm tra xem lá bài có trong đôi nào không
            for (List<Card> pair : pairs) {
                if (pair.contains(card)) {
                    isInCombination = true;
                    break;
                }
            }
            
            if (isInCombination) continue;
            
            // Kiểm tra xem lá bài có trong sảnh nào không
            for (List<Card> straight : straights) {
                if (straight.contains(card)) {
                    isInCombination = true;
                    break;
                }
            }
            
            if (!isInCombination) {
                return List.of(card); // Trả về lá không nằm trong tổ hợp nào
            }
        }
        
        // Nếu tất cả các lá đều nằm trong tổ hợp, trả về lá nhỏ nhất
        return List.of(playableCards.get(0));
    }
    
    /**
     * Tìm đôi tối ưu để đánh mà không phá vỡ bộ ba hoặc tứ quý
     */
    private List<Card> findOptimalPairPlay(List<Card> lastPlayed) {
        List<List<Card>> pairs = findAllPairs();
        if (pairs.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Sắp xếp các đôi theo giá trị tăng dần
        pairs.sort(Comparator.comparing(pair -> TienLenRule.getTienLenValue(pair.get(0))));
        
        // Tìm các bộ ba và tứ quý hiện có
        List<List<Card>> triples = findAllTriples();
        List<List<Card>> fourOfKinds = findAllFourOfKinds();
        
        for (List<Card> pair : pairs) {
            if (this.ruleSet.canPlayAfter(pair, lastPlayed)) {
                boolean isPartOfHigherCombination = false;
                
                // Kiểm tra xem đôi này có là một phần của bộ ba hoặc tứ quý không
                for (List<Card> triple : triples) {
                    if (containsAllCards(triple, pair)) {
                        isPartOfHigherCombination = true;
                        break;
                    }
                }
                
                if (isPartOfHigherCombination) continue;
                
                for (List<Card> four : fourOfKinds) {
                    if (containsAllCards(four, pair)) {
                        isPartOfHigherCombination = true;
                        break;
                    }
                }
                
                if (!isPartOfHigherCombination) {
                    return pair; // Trả về đôi không thuộc tổ hợp cao hơn
                }
            }
        }
        
        // Nếu tất cả đều là một phần của tổ hợp cao hơn, chọn đôi đầu tiên có thể đánh
        for (List<Card> pair : pairs) {
            if (this.ruleSet.canPlayAfter(pair, lastPlayed)) {
                return pair;
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Kiểm tra xem tổ hợp cards1 có chứa tất cả các lá trong tổ hợp cards2 không
     */
    private boolean containsAllCards(List<Card> cards1, List<Card> cards2) {
        return cards1.containsAll(cards2);
    }
    
    /**
     * Tìm lá bài đơn nhỏ nhất có thể đánh sau lá trước đó
     */
    private List<Card> findSmallestPlayableCard(List<Card> lastPlayed) {
        Card lastCard = lastPlayed.get(0);
        Card smallest = null;
        
        Comparator<Card> tlComparator = this.ruleSet.getCardComparator();
        for (Card card : hand) {
            if (this.ruleSet.canPlayAfter(List.of(card), List.of(lastCard))) {
                if (smallest == null || tlComparator.compare(card, smallest) < 0) {
                    smallest = card;
                }
            }
        }
        
        return smallest != null ? List.of(smallest) : new ArrayList<>();
    }
    
    /**
     * Tìm lá bài đơn nhỏ nhất trong bài
     */
    private Card findLowestSingleCard() {
        return hand.isEmpty() ? null : Collections.min(hand);
    }
    
    /**
     * Tìm đôi nhỏ nhất có thể đánh sau đôi trước đó
     */
    private List<Card> findSmallestPlayablePair(List<Card> lastPlayed) {
        // Tìm tất cả các đôi trong bài
        List<List<Card>> pairs = findAllPairs();
        if (pairs.isEmpty()) return new ArrayList<>();
        
        // Sắp xếp các đôi theo giá trị tăng dần
        pairs.sort(Comparator.comparing(pair -> TienLenRule.getTienLenValue(pair.get(0))));
        
        // Tìm đôi nhỏ nhất có thể đánh
        for (List<Card> pair : pairs) {
            if (this.ruleSet.canPlayAfter(pair, lastPlayed)) {
                return pair;
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Tìm bộ ba nhỏ nhất có thể đánh sau bộ ba trước đó
     */
    private List<Card> findSmallestPlayableTriple(List<Card> lastPlayed) {
        // Tìm tất cả các bộ ba trong bài
        List<List<Card>> triples = findAllTriples();
        if (triples.isEmpty()) return new ArrayList<>();
        
        // Sắp xếp các bộ ba theo giá trị tăng dần
        triples.sort(Comparator.comparing(triple -> TienLenRule.getTienLenValue(triple.get(0))));
        
        // Tìm bộ ba nhỏ nhất có thể đánh
        for (List<Card> triple : triples) {
            if (this.ruleSet.canPlayAfter(triple, lastPlayed)) {
                return triple;
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Tìm sảnh nhỏ nhất có thể đánh sau sảnh trước đó
     */
    private List<Card> findSmallestPlayableStraight(List<Card> lastPlayed) {
        // Sảnh phải cùng độ dài
        int length = lastPlayed.size();
        
        // Tìm tất cả các sảnh có cùng độ dài
        List<List<Card>> straights = findAllStraights(length);
        if (straights.isEmpty()) return new ArrayList<>();
        
        // Sắp xếp các sảnh theo giá trị lá đầu tiên
        straights.sort(Comparator.comparing(
        	    straight -> Collections.max(straight, this.ruleSet.getCardComparator()), // Trích xuất quân bài cao nhất trong sảnh (dùng TLMN comparator) làm khóa sắp xếp
        	    this.ruleSet.getCardComparator() // Sử dụng TLMN comparator để so sánh các quân bài cao nhất đó
        	));
        
        // Tìm sảnh nhỏ nhất có thể đánh
        for (List<Card> straight : straights) {
            if (this.ruleSet.canPlayAfter(straight, lastPlayed)) {
                return straight;
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Tìm tứ quý có thể đánh
     */
    private List<Card> findPlayableFourOfKind(List<Card> lastPlayed) {
        List<List<Card>> fourOfKinds = findAllFourOfKinds();
        if (fourOfKinds.isEmpty()) return new ArrayList<>();
        
        for (List<Card> fourOfKind : fourOfKinds) {
            if (this.ruleSet.canPlayAfter(fourOfKind, lastPlayed)) {
                return fourOfKind;
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Tìm tất cả các đôi trong bài
     */
    private List<List<Card>> findAllPairs() {
        List<List<Card>> pairs = new ArrayList<>();
        
        // Sắp xếp bài theo rank
        List<Card> sortedHand = new ArrayList<>(hand);
        sortedHand.sort(this.ruleSet.getCardComparator());
        
        for (int i = 0; i < sortedHand.size() - 1; i++) {
            if (sortedHand.get(i).getRank() == sortedHand.get(i + 1).getRank()) {
                List<Card> pair = new ArrayList<>();
                pair.add(sortedHand.get(i));
                pair.add(sortedHand.get(i + 1));
                pairs.add(pair);
                i++; // Bỏ qua lá vừa đưa vào đôi
            }
        }
        
        return pairs;
    }
    
    /**
     * Tìm tất cả các bộ ba trong bài
     */
    private List<List<Card>> findAllTriples() {
        List<List<Card>> triples = new ArrayList<>();
        
        // Sắp xếp bài theo rank
        List<Card> sortedHand = new ArrayList<>(hand);
        sortedHand.sort(this.ruleSet.getCardComparator());
        
        for (int i = 0; i < sortedHand.size() - 2; i++) {
            if (sortedHand.get(i).getRank() == sortedHand.get(i + 1).getRank() && 
                sortedHand.get(i).getRank() == sortedHand.get(i + 2).getRank()) {
                List<Card> triple = new ArrayList<>();
                triple.add(sortedHand.get(i));
                triple.add(sortedHand.get(i + 1));
                triple.add(sortedHand.get(i + 2));
                triples.add(triple);
                i += 2; // Bỏ qua 2 lá vừa đưa vào bộ ba
            }
        }
        
        return triples;
    }
    
    /**
     * Tìm tất cả các tứ quý trong bài
     */
    private List<List<Card>> findAllFourOfKinds() {
        List<List<Card>> fourOfKinds = new ArrayList<>();
        
        // Sắp xếp bài theo rank
        List<Card> sortedHand = new ArrayList<>(hand);
        sortedHand.sort(this.ruleSet.getCardComparator());
        
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
                i += 3; // Bỏ qua 3 lá vừa đưa vào tứ quý
            }
        }
        
        return fourOfKinds;
    }
    
    /**
     * Tìm tất cả các sảnh có độ dài xác định
     */
    private List<List<Card>> findAllStraights(int length) {
        if (length < 3 || hand.size() < length) {
            return new ArrayList<>();
        }
        
        List<List<Card>> straights = new ArrayList<>();
        
        // Sắp xếp bài theo rank
        List<Card> sortedHand = new ArrayList<>(hand);
        sortedHand.sort(this.ruleSet.getCardComparator());
        
        // Loại bỏ các lá bài trùng giá trị TLMN (giữ lại 1 lá cho mỗi giá trị TLMN)
        List<Card> uniqueValueCards = new ArrayList<>();
        int lastValue = -1; // Giá trị khởi tạo không trùng với giá trị hợp lệ nào
        for (Card card : sortedHand) { // sortedHand đã sắp xếp đúng TLMN ở trên rồi
             int currentValue = TienLenRule.getTienLenValue(card); // <-- Sử dụng giá trị Tiến Lên
             if (currentValue != lastValue) { // Kiểm tra giá trị TLMN khác với quân trước đó
                 // Thêm quân bài vào danh sách quân bài duy nhất theo giá trị TLMN.
                 // Chú ý: Nếu có nhiều quân cùng giá trị TLMN (ví dụ: 3 cơ 3 rô), chỉ giữ lại một.
                 // Cách này đơn giản nhưng có thể bỏ sót sảnh nếu sảnh cần các quân có cùng giá trị Rank nhưng khác chất.
                 // Tuy nhiên, với AI đơn giản, cách này tạm chấp nhận để tìm sảnh cơ bản.
                 uniqueValueCards.add(card);
                 lastValue = currentValue;
             }
        }
        
     // Tìm tất cả các sảnh có độ dài xác định trong list các quân bài độc nhất TLMN
        for (int i = 0; i <= uniqueValueCards.size() - length; i++) {
            boolean isStraight = true;
            for (int j = 1; j < length; j++) {
                 // Check for consecutive TLMN values
                 int prevTLValue = TienLenRule.getTienLenValue(uniqueValueCards.get(i+j-1)); // <-- Sử dụng giá trị Tiến Lên
                 int currTLValue = TienLenRule.getTienLenValue(uniqueValueCards.get(i+j));   // <-- Sử dụng giá trị Tiến Lên

                 if (currTLValue - prevTLValue != 1) {
                     isStraight = false;
                     break;
                 }
            }

            // Kiểm tra sảnh có chứa hoặc kết thúc bằng 2 (giá trị 15) không hợp lệ trong sảnh đơn
             if (isStraight) {
                 int firstStraightValue = TienLenRule.getTienLenValue(uniqueValueCards.get(i));
                 int lastStraightValue = TienLenRule.getTienLenValue(uniqueValueCards.get(i + length - 1));
                 // Nếu sảnh kết thúc bằng quân 2 (giá trị 15), đó không phải sảnh hợp lệ trong TLMN (sảnh chỉ đến A)
                 if (lastStraightValue == 15) {
                     isStraight = false;
                 }
             }


            if (isStraight) {
                List<Card> straight = new ArrayList<>();
                // Lấy các quân bài tương ứng từ uniqueValueCards
                for (int j = 0; j < length; j++) {
                    straight.add(uniqueValueCards.get(i+j));
                }
                straights.add(straight);
            }
        }
      
        return straights;
    }
}
// File: core/AIStrategy/RandomStrategy.java
package core.AIStrategy;

import core.Card;
import core.RuleSet;
import core.ai.utils.PlayableMoveGenerator; // Sử dụng lớp tiện ích

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomStrategy implements AIStrategy {
    private Random random = new Random();
    private static final Card THREE_SPADES = new Card(Card.Suit.SPADES, Card.Rank.THREE); // Cụ thể cho Tiến Lên

    @Override
    public List<Card> chooseCards(List<Card> currentHand, List<Card> lastPlayedCards, RuleSet ruleSet, boolean isFirstTurnOfEntireGame) {
        if (currentHand.isEmpty()) {
            return new ArrayList<>();
        }

        // Xử lý luật 3 Bích cho lượt đầu tiên của game (Đặc thù Tiến Lên)
        if (isFirstTurnOfEntireGame) {
            if (currentHand.contains(THREE_SPADES)) {
                // RandomStrategy đơn giản có thể chỉ đánh 3 Bích nếu có
                // Kiểm tra xem đánh lẻ 3 bích có hợp lệ không (luôn hợp lệ nếu không có bài trên bàn)
                List<Card> threeSpadePlay = List.of(THREE_SPADES);
                if ((lastPlayedCards == null || lastPlayedCards.isEmpty()) && ruleSet.isValidCombination(threeSpadePlay)) {
                    return threeSpadePlay;
                }
                // Nếu có lỗi hoặc không được đánh (rất hiếm), thì sẽ đi xuống logic random bên dưới
            } else {
                // Nếu không có 3 Bích trong lượt đầu game, Random AI sẽ không đánh gì (bỏ lượt)
                // vì luật Tiến Lên yêu cầu.
                return new ArrayList<>();
            }
        }

        List<List<Card>> allPlayableMoves = new ArrayList<>();

        // Tìm tất cả các nước đi đơn hợp lệ
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableSingles(currentHand, lastPlayedCards, ruleSet));
        // Tìm tất cả các nước đi đôi hợp lệ
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayablePairs(currentHand, lastPlayedCards, ruleSet));
        // Tìm tất cả các nước đi bộ ba hợp lệ
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableTriples(currentHand, lastPlayedCards, ruleSet));
        // Tìm tất cả các nước đi sảnh hợp lệ
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableStraights(currentHand, lastPlayedCards, ruleSet));
        // Tìm các nước đi tứ quý (để chặt heo hoặc tứ quý khác)
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableFourOfAKinds(currentHand, lastPlayedCards, ruleSet));
        // Bạn có thể thêm các loại khác như 3 đôi thông, 4 đôi thông nếu PlayableMoveGenerator hỗ trợ

        if (allPlayableMoves.isEmpty()) {
            return new ArrayList<>(); // Bỏ lượt nếu không có nước nào
        }

        // Chọn ngẫu nhiên một nước đi từ danh sách các nước đi hợp lệ
        return allPlayableMoves.get(random.nextInt(allPlayableMoves.size()));
    }
}
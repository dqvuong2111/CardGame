package core.AIStrategy;

import core.Card;
import core.RuleSet;
import java.util.ArrayList;
import java.util.List;
// import java.util.Random; // Có thể cần nếu logic ngẫu nhiên phức tạp hơn

public class RandomStrategy implements AIStrategy {

    @Override
    public List<Card> chooseCards(List<Card> currentHand, List<Card> lastPlayedCards, RuleSet ruleSet, boolean isFirstTurn) {
        if (currentHand.isEmpty()) {
            return new ArrayList<>();
        }

        // Logic từ AIPlayer.chooseRandomCards
        if (lastPlayedCards == null || lastPlayedCards.isEmpty()) { //
            if (isFirstTurn) { //
                // Trong lượt đầu tiên, AI Random phải đánh 3 Bích nếu có
                Card threeSpades = new Card(Card.Suit.SPADES, Card.Rank.THREE);
                for (Card card : currentHand) {
                    if (card.equals(threeSpades)) { //
                        return List.of(card); //
                    }
                }
                // Nếu không có 3 Bích (trường hợp hiếm/lỗi chia bài), AI Random có thể bỏ lượt hoặc đánh lá khác
                // Để đơn giản, nếu không có 3 bích, nó sẽ không đánh gì (bỏ lượt)
                // Hoặc bạn có thể cho nó đánh lá bài đầu tiên nếu không có 3 bích
                 if (!currentHand.isEmpty()) return List.of(currentHand.get(0)); // Đánh lá đầu tiên nếu không có 3 bích
                 return new ArrayList<>();
            } else {
                return List.of(currentHand.get(0)); // Đánh lá bài đầu tiên trên tay
            }
        }

        // Thử các tổ hợp bài ngẫu nhiên (ở đây là thử từng lá một cách tuần tự)
        for (Card card : currentHand) {
            List<Card> singleCard = List.of(card);
            if (ruleSet.canPlayAfter(singleCard, lastPlayedCards)) { //
                return singleCard; //
            }
        }

        return new ArrayList<>(); // Bỏ lượt nếu không tìm thấy bài hợp lệ
    }
}
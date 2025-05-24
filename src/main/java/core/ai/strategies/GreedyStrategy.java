// File: core/AIStrategy/GreedyStrategy.java
package core.ai.strategies;

import core.Card;
import core.RuleSet;
import core.ai.AIStrategy;
import core.ai.utils.CombinationFinder; // Có thể cần để tìm tổ hợp chứa 3 bích
import core.ai.utils.PlayableMoveGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GreedyStrategy implements AIStrategy {
    private static final Card THREE_SPADES = new Card(Card.Suit.SPADES, Card.Rank.THREE); // Cụ thể cho Tiến Lên

    @Override
    public List<Card> chooseCards(List<Card> currentHand, List<Card> lastPlayedCards, RuleSet ruleSet, boolean isFirstTurnOfEntireGame) {
        if (currentHand.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Xử lý luật 3 Bích cho lượt đầu tiên của toàn bộ ván game
        if (isFirstTurnOfEntireGame) {
            if (currentHand.contains(THREE_SPADES)) {
                // Ưu tiên đánh tổ hợp nhỏ nhất chứa 3 Bích
                List<List<Card>> playsWithThreeSpades = findPlaysContainingCard(currentHand, THREE_SPADES, ruleSet, lastPlayedCards);
                if (!playsWithThreeSpades.isEmpty()) {
                    // Sắp xếp các tổ hợp này và chọn bộ "nhỏ nhất"
                    // (ví dụ: ưu tiên đánh lẻ 3 bích nếu được, rồi đến đôi chứa 3 bích, v.v.)
                    // Đây là một ví dụ đơn giản: chọn tổ hợp có ít lá nhất, rồi đến lá đại diện nhỏ nhất.
                	playsWithThreeSpades.sort(
                		    Comparator.comparingInt((List<Card> list) -> list.size()) // Khai báo rõ ràng kiểu cho lambda này
                		    .thenComparing(
                		        (List<Card> play) -> ruleSet.getRepresentativeCardForCombination(play), // Đảm bảo lambda này cũng được gõ rõ ràng
                		        ruleSet.getCardComparator()
                		    )
                		);
                	return playsWithThreeSpades.get(0);
                }
                // Nếu không tìm được cách đánh 3 bích hợp lệ (rất hiếm), sẽ bỏ lượt
                return new ArrayList<>();
            } else {
                // Không có 3 Bích trong lượt đầu game, AI phải bỏ lượt theo luật Tiến Lên
                return new ArrayList<>();
            }
        }

        // Logic chặt Heo (ví dụ: nếu lá cuối là Heo đơn) - Cần RuleSet cho biết type là gì
        // Giả sử RuleSet.getCombinationIdentifier trả về một enum hoặc string mà ta có thể so sánh
        // Ví dụ: if (lastPlayedType == TienLenRule.CombinationType.SINGLE && ruleSet.getRepresentativeCardForCombination(lastPlayedCards).getRank() == Card.Rank.TWO)
        // (Cần kiểm tra chính xác hơn dựa trên getCombinationIdentifier và getRepresentativeCardForCombination)

        List<List<Card>> playableFours = PlayableMoveGenerator.findPlayableFourOfAKinds(currentHand, lastPlayedCards, ruleSet);
        if (!playableFours.isEmpty()) {
            // Greedy sẽ đánh tứ quý nhỏ nhất có thể để chặt
            playableFours.sort(Comparator.comparing(p -> ruleSet.getRepresentativeCardForCombination(p), ruleSet.getCardComparator()));
            return playableFours.get(0);
        }
        // Tương tự cho 3 đôi thông, 4 đôi thông nếu có...


        // 3. Nếu không phải lượt đầu game và không chặt đặc biệt:
        List<List<Card>> allPlayableRegularMoves = new ArrayList<>();
        if (lastPlayedCards == null || lastPlayedCards.isEmpty()) { // Bắt đầu vòng mới
            // Ưu tiên đánh các bộ nhỏ nhất để thoát bài lẻ hoặc bộ nhỏ
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableSingles(currentHand, null, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayablePairs(currentHand, null, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableTriples(currentHand, null, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableStraights(currentHand, null, ruleSet));
        } else { // Đánh theo người khác
            // Tìm các bộ cùng loại (hoặc loại chặt được theo luật thường)
            // Ví dụ, nếu trên bàn là Đơn, tìm Đơn lớn hơn. Nếu là Đôi, tìm Đôi lớn hơn.
            // PlayableMoveGenerator đã làm điều này bằng cách gọi ruleSet.canPlayAfter.
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableSingles(currentHand, lastPlayedCards, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayablePairs(currentHand, lastPlayedCards, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableTriples(currentHand, lastPlayedCards, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableStraights(currentHand, lastPlayedCards, ruleSet));
        }

        if (allPlayableRegularMoves.isEmpty()) {
            return new ArrayList<>(); // Bỏ lượt
        }

        // Sắp xếp tất cả các nước đi hợp lệ và chọn nước "nhỏ nhất"
        // Tiêu chí "nhỏ nhất" của Greedy:
        // 1. Ưu tiên số lượng lá bài ít nhất (để thoát bài lẻ).
        // 2. Nếu cùng số lượng, ưu tiên bộ có lá bài đại diện nhỏ nhất.
        allPlayableRegularMoves.sort(
        	    Comparator.comparingInt((List<Card> list) -> list.size()) // Lambda được gõ rõ ràng
        	    .thenComparing(
        	        (List<Card> play) -> ruleSet.getRepresentativeCardForCombination(play), // Lambda được gõ rõ ràng
        	        ruleSet.getCardComparator()
        	    )
        	);

        return allPlayableRegularMoves.get(0);
    }

    // Helper để tìm các tổ hợp chứa một lá bài cụ thể (ví dụ 3 Bích)
    private List<List<Card>> findPlaysContainingCard(List<Card> hand, Card specificCard, RuleSet ruleSet, List<Card> lastPlayedCards) {
        List<List<Card>> FinnedPlays = new ArrayList<>();
        // 1. Đánh lẻ lá specificCard
        List<Card> singlePlay = List.of(specificCard);
        if (ruleSet.isValidCombination(singlePlay) && ((lastPlayedCards == null || lastPlayedCards.isEmpty()) || ruleSet.canPlayAfter(singlePlay, lastPlayedCards))) {
            FinnedPlays.add(singlePlay);
        }

        // 2. Tìm đôi chứa specificCard
        List<List<Card>> pairs = CombinationFinder.findAllPairs(hand, ruleSet);
        for (List<Card> pair : pairs) {
            if (pair.contains(specificCard) && ruleSet.isValidCombination(pair) && ((lastPlayedCards == null || lastPlayedCards.isEmpty()) || ruleSet.canPlayAfter(pair, lastPlayedCards))) {
                FinnedPlays.add(pair);
            }
        }
        // 3. Tìm bộ ba chứa specificCard
        List<List<Card>> triples = CombinationFinder.findAllTriples(hand, ruleSet);
        for (List<Card> triple : triples) {
            if (triple.contains(specificCard) && ruleSet.isValidCombination(triple) && ((lastPlayedCards == null || lastPlayedCards.isEmpty()) || ruleSet.canPlayAfter(triple, lastPlayedCards))) {
                FinnedPlays.add(triple);
            }
        }
        // 4. Tìm sảnh chứa specificCard
        List<List<Card>> straights = CombinationFinder.findAllStraights(hand, ruleSet, 3); // Sảnh từ 3 lá
        for (List<Card> straight : straights) {
            if (straight.contains(specificCard) && ruleSet.isValidCombination(straight) && ((lastPlayedCards == null || lastPlayedCards.isEmpty()) || ruleSet.canPlayAfter(straight, lastPlayedCards))) {
                FinnedPlays.add(straight);
            }
        }
        // Có thể thêm các loại khác nếu cần
        return FinnedPlays;
    }
}
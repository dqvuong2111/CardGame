// File: core/AIStrategy/SmartStrategy.java
package core.AIStrategy;

import core.Card;
import core.RuleSet;
import core.ai.utils.CombinationFinder; // Vẫn có thể dùng để phân tích tay bài nếu cần cho các heuristic phụ
import core.ai.utils.PlayableMoveGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SmartStrategy implements AIStrategy {
    private static final Card THREE_SPADES = new Card(Card.Suit.SPADES, Card.Rank.THREE);

    @Override
    public List<Card> chooseCards(List<Card> currentHand, List<Card> lastPlayedCards, RuleSet ruleSet, boolean isFirstTurnOfEntireGame) {
        if (currentHand.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. XỬ LÝ LƯỢT ĐẦU TIÊN CỦA TOÀN BỘ VÁN GAME (3 BÍCH)
        if (isFirstTurnOfEntireGame) {
            // Logic này vẫn giữ: ưu tiên đánh tổ hợp nhỏ nhất chứa 3 Bích để qua lượt đầu
            if (currentHand.contains(THREE_SPADES)) {
                List<List<Card>> playsWithThreeSpades = findPlaysContainingCard(currentHand, THREE_SPADES, ruleSet, lastPlayedCards);
                if (!playsWithThreeSpades.isEmpty()) {
                    playsWithThreeSpades.sort(
                        Comparator.comparingInt((List<Card> list) -> list.size())
                        .thenComparing(
                            (List<Card> play) -> ruleSet.getRepresentativeCardForCombination(play),
                            ruleSet.getCardComparator()
                        )
                    );
                    return playsWithThreeSpades.get(0);
                }
            }
            return new ArrayList<>(); // Không có 3 Bích hoặc không tạo được bộ hợp lệ, phải bỏ lượt
        }

        // 2. ƯU TIÊN CÁC NƯỚC ĐI "CHẶT" ĐẶC BIỆT NẾU CÓ LỢI
        // (Logic này cần được xem xét cẩn thận xem có nên override ưu tiên "nhiều lá nhất" không)
        // Ví dụ: Tứ quý (4 lá) có thể chặt Heo (1 lá). Nếu chỉ xét "nhiều lá nhất", AI có thể bỏ qua cơ hội chặt Heo
        // để đánh một sảnh 5 lá (nếu đang mở đầu vòng).
        // Do đó, logic chặt đặc biệt nên được ưu tiên hơn.
        List<Card> specialChumpingPlay = findSpecialChumpingPlay(currentHand, lastPlayedCards, ruleSet);
        if (!specialChumpingPlay.isEmpty()) {
            return specialChumpingPlay;
        }

        // 3. TÌM TẤT CẢ CÁC NƯỚC ĐI HỢP LỆ THÔNG THƯỜNG
        List<List<Card>> allPlayableMoves = new ArrayList<>();

        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableSingles(currentHand, lastPlayedCards, ruleSet));
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayablePairs(currentHand, lastPlayedCards, ruleSet));
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableTriples(currentHand, lastPlayedCards, ruleSet));
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableStraights(currentHand, lastPlayedCards, ruleSet));
        // Tứ quý thông thường (không phải để chặt heo) cũng là một lựa chọn
        // Nếu findPlayableFourOfAKinds đã bao gồm cả trường hợp đánh thường và chặt, thì không cần gọi lại
        // Nhưng nếu nó chỉ trả về khi có thể chặt, thì cần một hàm khác cho Tứ Quý đánh thường.
        // Giả sử PlayableMoveGenerator.findPlayableFourOfAKinds xử lý cả 2 trường hợp dựa trên lastPlayedCards
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableFourOfAKinds(currentHand, lastPlayedCards, ruleSet));
        // (Thêm các loại đôi thông nếu PlayableMoveGenerator hỗ trợ)

        if (allPlayableMoves.isEmpty()) {
            return new ArrayList<>(); // Không có nước đi nào hợp lệ, bỏ lượt
        }

        // 4. SẮP XẾP CÁC NƯỚC ĐI ƯU TIÊN BỘ NHIỀU LÁ NHẤT
        // Tiêu chí chính: số lượng lá bài giảm dần (đánh bộ nhiều lá nhất)
        // Tiêu chí phụ: nếu cùng số lượng lá, đánh bộ có lá đại diện nhỏ hơn (yếu hơn)
        allPlayableMoves.sort(
            Comparator.comparingInt((List<Card> list) -> list.size()).reversed() // Sắp xếp theo size giảm dần
            .thenComparing(
                (List<Card> play) -> ruleSet.getRepresentativeCardForCombination(play), // Rồi theo lá đại diện tăng dần
                ruleSet.getCardComparator()
            )
        );

        return allPlayableMoves.get(0); // Trả về nước đi đầu tiên (nhiều lá nhất, yếu nhất trong số nhiều lá nhất)
    }

    // Helper để tìm các tổ hợp chứa một lá bài cụ thể (ví dụ 3 Bích)
    // Dùng cho lượt đầu tiên của game
    private List<List<Card>> findPlaysContainingCard(List<Card> hand, Card specificCard, RuleSet ruleSet, List<Card> lastPlayedCards) {
        List<List<Card>> foundPlays = new ArrayList<>();
        // Thử đánh lẻ
        List<Card> singlePlay = List.of(specificCard);
        if (isValidPlay(singlePlay, lastPlayedCards, ruleSet)) foundPlays.add(singlePlay);

        // Thử tìm đôi chứa specificCard
        CombinationFinder.findAllPairs(hand, ruleSet).stream()
            .filter(pair -> pair.contains(specificCard) && isValidPlay(pair, lastPlayedCards, ruleSet))
            .forEach(foundPlays::add);

        // Thử tìm bộ ba chứa specificCard
        CombinationFinder.findAllTriples(hand, ruleSet).stream()
            .filter(triple -> triple.contains(specificCard) && isValidPlay(triple, lastPlayedCards, ruleSet))
            .forEach(foundPlays::add);

        // Thử tìm sảnh (từ 3 lá) chứa specificCard
        CombinationFinder.findAllStraights(hand, ruleSet, 3).stream()
            .filter(straight -> straight.contains(specificCard) && isValidPlay(straight, lastPlayedCards, ruleSet))
            .forEach(foundPlays::add);
        return foundPlays;
    }
    
    // Helper kiểm tra tính hợp lệ của một nước đi
    private boolean isValidPlay(List<Card> play, List<Card> lastPlayedCards, RuleSet ruleSet) {
        if (!ruleSet.isValidCombination(play)) return false;
        if (lastPlayedCards == null || lastPlayedCards.isEmpty()) return true; // Hợp lệ nếu mở đầu vòng
        return ruleSet.canPlayAfter(play, lastPlayedCards);
    }

    // Helper tìm các nước đi "chặt" đặc biệt
    private List<Card> findSpecialChumpingPlay(List<Card> currentHand, List<Card> lastPlayedCards, RuleSet ruleSet) {
        if (lastPlayedCards == null || lastPlayedCards.isEmpty()) {
            return new ArrayList<>();
        }

        // Ưu tiên Tứ Quý nếu có thể chặt Heo hoặc Tứ Quý khác
        List<List<Card>> playableFours = PlayableMoveGenerator.findPlayableFourOfAKinds(currentHand, lastPlayedCards, ruleSet);
        if (!playableFours.isEmpty()) {
            // Kiểm tra xem có đáng để dùng Tứ Quý không
            Object lastPlayedTypeId = ruleSet.getCombinationIdentifier(lastPlayedCards);
            Card representativeLastCard = ruleSet.getRepresentativeCardForCombination(lastPlayedCards);
            boolean isLastCardTwo = representativeLastCard != null && representativeLastCard.getRank() == Card.Rank.TWO;

            // Ví dụ: Chặt Heo hoặc Tứ quý khác
            // Cần một cách chuẩn để xác định loại từ lastPlayedTypeId
            // Giả sử typeIdentifier trả về enum hoặc string dễ so sánh
            String lastTypeString = lastPlayedTypeId != null ? lastPlayedTypeId.toString().toUpperCase() : "";

            if ((lastTypeString.contains("SINGLE") && isLastCardTwo) || // Chặt Heo đơn
                (lastTypeString.contains("PAIR") && isLastCardTwo)   || // Chặt Đôi Heo
                lastTypeString.contains("FOUR_OF_KIND"))              // Chặt Tứ Quý khác
            {
                // Sắp xếp các tứ quý đánh được và chọn tứ quý nhỏ nhất
                playableFours.sort(Comparator.comparing(
                    play -> ruleSet.getRepresentativeCardForCombination(play),
                    ruleSet.getCardComparator()
                ));
                return playableFours.get(0);
            }
        }

        // (Thêm logic cho 3 đôi thông, 4 đôi thông nếu chúng được coi là "chặt" đặc biệt và ưu tiên hơn "nhiều lá nhất")

        return new ArrayList<>(); // Không có nước chặt đặc biệt nào được chọn
    }
}
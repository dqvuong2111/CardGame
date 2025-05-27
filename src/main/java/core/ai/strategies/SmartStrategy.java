package core.ai.strategies;

import core.Card;
import core.ai.AIStrategy;
import core.ai.helpers.CombinationFinder;
import core.ai.helpers.PlayableMoveGenerator;
import core.ai.helpers.RemainingCardsValidator;
import core.games.RuleSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class SmartStrategy implements AIStrategy {
    private Random random = new Random();

    @Override
    public List<Card> chooseCards(List<Card> currentHand, List<Card> lastPlayedCards, RuleSet ruleSet, boolean isFirstTurnOfEntireGame) {
        if (currentHand.isEmpty()) {
            return new ArrayList<>();
        }

        // XỬ LÝ LƯỢT ĐẦU TIÊN CỦA TOÀN BỘ VÁN GAME (3 BÍCH)
        if (isFirstTurnOfEntireGame) {
        	if(ruleSet.startingCard() == null) return List.of(currentHand.get(random.nextInt(currentHand.size())));
            // Logic này vẫn giữ: ưu tiên đánh tổ hợp nhỏ nhất chứa 3 Bích để qua lượt đầu
            if (ruleSet.hasStartingCard(currentHand) && ruleSet.startingCard() != null) {
                List<List<Card>> playsWithThreeSpades = findPlaysContainingCard(currentHand, ruleSet.startingCard(), ruleSet, lastPlayedCards);
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

        // ƯU TIÊN CÁC NƯỚC ĐI "CHẶT" ĐẶC BIỆT NẾU CÓ LỢI
        List<Card> specialChumpingPlay = findSpecialChumpingPlay(currentHand, lastPlayedCards, ruleSet);
        if (!specialChumpingPlay.isEmpty()) {
            if(!RemainingCardsValidator.checkRemainingCard(currentHand, specialChumpingPlay)) return specialChumpingPlay;
        }

        // TÌM TẤT CẢ CÁC NƯỚC ĐI HỢP LỆ THÔNG THƯỜNG
        List<List<Card>> allPlayableMoves = new ArrayList<>();

        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableSingles(currentHand, lastPlayedCards, ruleSet));
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayablePairs(currentHand, lastPlayedCards, ruleSet));
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableTriples(currentHand, lastPlayedCards, ruleSet));
        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableStraights(currentHand, lastPlayedCards, ruleSet));

        allPlayableMoves.addAll(PlayableMoveGenerator.findPlayableFourOfAKinds(currentHand, lastPlayedCards, ruleSet));
  

        if (allPlayableMoves.isEmpty()) {
            return new ArrayList<>(); // Không có nước đi nào hợp lệ, bỏ lượt
        }

        // SẮP XẾP CÁC NƯỚC ĐI ƯU TIÊN BỘ NHIỀU LÁ NHẤT
        // Tiêu chí chính: số lượng lá bài giảm dần (đánh bộ nhiều lá nhất)
        // Tiêu chí phụ: nếu cùng số lượng lá, đánh bộ có lá đại diện nhỏ hơn (yếu hơn)
        allPlayableMoves.sort(
            Comparator.comparingInt((List<Card> list) -> list.size()).reversed()        // Sắp xếp theo size giảm dần
            .thenComparing(
                (List<Card> play) -> ruleSet.getRepresentativeCardForCombination(play), // Rồi theo lá đại diện tăng dần
                ruleSet.getCardComparator()
            )
        );

        for(int i = 0; i < allPlayableMoves.size(); i++) {
        	if(!RemainingCardsValidator.checkRemainingCard(currentHand, allPlayableMoves.get(i))) return allPlayableMoves.get(i);
        }
        
        return new ArrayList<Card>();   // trả về nước đi đầu tiên (nhiều lá nhất, yếu nhất trong số nhiều lá nhất)
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

        // ưu tiên Tứ Quý nếu có thể chặt Heo hoặc Tứ Quý khác
        List<List<Card>> playableFours = PlayableMoveGenerator.findPlayableFourOfAKinds(currentHand, lastPlayedCards, ruleSet);
        if (!playableFours.isEmpty()) {
            // Kiểm tra xem có đáng để dùng Tứ Quý không
            Object lastPlayedTypeId = ruleSet.getCombinationIdentifier(lastPlayedCards);
            Card representativeLastCard = ruleSet.getRepresentativeCardForCombination(lastPlayedCards);
            boolean isLastCardTwo = representativeLastCard != null && representativeLastCard.getRank() == Card.Rank.TWO;

            String lastTypeString = lastPlayedTypeId != null ? lastPlayedTypeId.toString().toUpperCase() : "";

            if ((lastTypeString.contains("SINGLE") && isLastCardTwo) || // chặt heo đơn
                (lastTypeString.contains("PAIR") && isLastCardTwo)   || // chặt đôi heo
                lastTypeString.contains("FOUR_OF_KIND"))                // chặt tứ quý khác
            {
                // Sắp xếp các tứ quý đánh được và chọn tứ quý nhỏ nhất
                playableFours.sort(Comparator.comparing(
                    play -> ruleSet.getRepresentativeCardForCombination(play),
                    ruleSet.getCardComparator()
                ));
                return playableFours.get(0);
            }
        }

        return new ArrayList<>(); // Không có nước chặt đặc biệt nào được chọn
    }
}
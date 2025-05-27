package core.ai.tienlenai.strategies;

import core.Card;
import core.ai.helpers.CombinationFinder;
import core.ai.helpers.PlayableMoveGenerator;
import core.ai.helpers.RemainingCardsValidator;
import core.ai.tienlenai.AIStrategy;
import core.games.RuleSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GreedyStrategy implements AIStrategy {
	private Random random = new Random();
    @Override
    public List<Card> chooseCards(List<Card> currentHand, List<Card> lastPlayedCards, RuleSet ruleSet, boolean isFirstTurnOfEntireGame) {
        if (currentHand.isEmpty()) {
            return new ArrayList<>();
        }

        // Xử lý luật 3 Bích cho lượt đầu tiên của toàn bộ ván game
        if (isFirstTurnOfEntireGame) {
        	if(ruleSet.startingCard() == null) return List.of(currentHand.get(random.nextInt(currentHand.size())));
            if (ruleSet.hasStartingCard(currentHand) && ruleSet.startingCard() != null) {
                // Ưu tiên đánh tổ hợp nhỏ nhất chứa 3 Bích
                List<List<Card>> playsWithThreeSpades = findPlaysContainingCard(currentHand, ruleSet.startingCard(), ruleSet, lastPlayedCards);
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
        
        List<List<Card>> playableFours = PlayableMoveGenerator.findPlayableFourOfAKinds(currentHand, lastPlayedCards, ruleSet);
        if (!playableFours.isEmpty()) {
            // Greedy sẽ đánh tứ quý nhỏ nhất có thể để chặt
            playableFours.sort(Comparator.comparing(p -> ruleSet.getRepresentativeCardForCombination(p), ruleSet.getCardComparator()));
            for(int i = 0 ; i < playableFours.size(); i++) {
            	if(!RemainingCardsValidator.checkRemainingCard(currentHand, playableFours.get(i))) return playableFours.get(i);
            }
        }

        // 3. Nếu không phải lượt đầu game và không chặt đặc biệt:
        List<List<Card>> allPlayableRegularMoves = new ArrayList<>();
        if (lastPlayedCards == null || lastPlayedCards.isEmpty()) { // Bắt đầu vòng mới
            // Ưu tiên đánh các bộ nhỏ nhất để thoát bài lẻ hoặc bộ nhỏ
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableSingles(currentHand, null, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayablePairs(currentHand, null, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableTriples(currentHand, null, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableStraights(currentHand, null, ruleSet));
        } else { 
            // Đánh theo người khác
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableSingles(currentHand, lastPlayedCards, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayablePairs(currentHand, lastPlayedCards, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableTriples(currentHand, lastPlayedCards, ruleSet));
            allPlayableRegularMoves.addAll(PlayableMoveGenerator.findPlayableStraights(currentHand, lastPlayedCards, ruleSet));
        }

        if (allPlayableRegularMoves.isEmpty()) {
            return new ArrayList<>();   // Bỏ lượt
        }

        // Tiêu chí "nhỏ nhất" của Greedy:
        // 1. Ưu tiên số lượng lá bài ít nhất (để thoát bài lẻ).
        // 2. Nếu cùng số lượng, ưu tiên bộ có lá bài đại diện nhỏ nhất.
        allPlayableRegularMoves.sort(
        	    Comparator.comparingInt((List<Card> list) -> list.size())
        	    .thenComparing(
        	        (List<Card> play) -> ruleSet.getRepresentativeCardForCombination(play),
        	        ruleSet.getCardComparator()
        	    )
        	);

        
        for(int i = 0 ; i < allPlayableRegularMoves.size(); i++) {
        	if(!RemainingCardsValidator.checkRemainingCard(currentHand, allPlayableRegularMoves.get(i))) return allPlayableRegularMoves.get(i);
        }
        return new ArrayList<Card>();
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
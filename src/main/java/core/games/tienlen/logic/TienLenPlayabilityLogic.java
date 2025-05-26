// File: core/games/tienlen/logic/TienLenMienNamPlayabilityLogic.java
// HOẶC ĐỔI TÊN THÀNH: core/games/tienlen/logic/TienLenPlayabilityLogic.java
package core.games.tienlen.logic; // Package mới

import core.Card;
// import core.games.tienlen.tienlenmiennam.TienLenMienNamRule; // Bỏ import này
import core.games.tienlen.TienLenVariantRuleSet; // << SỬ DỤNG INTERFACE NÀY

import java.util.ArrayList;
import java.util.Collections;
// import java.util.Comparator; // Không cần trực tiếp nếu lấy từ ruleSet
import java.util.List;

public class TienLenPlayabilityLogic { // Hoặc TienLenPlayabilityLogic

    // canPlayAfter giờ nhận TienLenVariantRuleSet
    public static boolean canPlayAfter(List<Card> newCards, List<Card> previousCards, TienLenVariantRuleSet ruleSet) {
        if (newCards == null || newCards.isEmpty()) return false;
        
        // Sử dụng ruleSet.getCombinationIdentifier() để lấy loại bộ bài
        Object newTypeObject = ruleSet.getCombinationIdentifier(newCards);
        if (newTypeObject == null || newTypeObject == TienLenVariantRuleSet.CombinationType.INVALID) return false;
        TienLenVariantRuleSet.CombinationType newType = (TienLenVariantRuleSet.CombinationType) newTypeObject;

        if (previousCards == null || previousCards.isEmpty()) {
            // Khi bắt đầu vòng mới, chỉ cần bộ bài mới hợp lệ
            return ruleSet.isValidCombination(newCards); // Hoặc newType != INVALID cũng được
        }

        Object prevTypeObject = ruleSet.getCombinationIdentifier(previousCards);
        if (prevTypeObject == null || prevTypeObject == TienLenVariantRuleSet.CombinationType.INVALID) return true; // Bài trên bàn không hợp lệ, cho đánh
        TienLenVariantRuleSet.CombinationType prevType = (TienLenVariantRuleSet.CombinationType) prevTypeObject;

        List<Card> sortedNewCards = new ArrayList<>(newCards);
        sortedNewCards.sort(ruleSet.getCardComparator());
        List<Card> sortedPreviousCards = new ArrayList<>(previousCards);
        sortedPreviousCards.sort(ruleSet.getCardComparator());

        int valueOfTwo = ruleSet.getTwoRankValue();
        boolean prevIsSingleTwo = (prevType == TienLenVariantRuleSet.CombinationType.SINGLE && 
                                   sortedPreviousCards.size() == 1 &&
                                   ruleSet.getCardRankValue(sortedPreviousCards.get(0)) == valueOfTwo);
        boolean prevIsPairOfTwos = (prevType == TienLenVariantRuleSet.CombinationType.PAIR && 
                                    sortedPreviousCards.size() == 2 && 
                                    ruleSet.getCardRankValue(sortedPreviousCards.get(0)) == valueOfTwo &&
                                    ruleSet.getCardRankValue(sortedPreviousCards.get(1)) == valueOfTwo);


        // --- LOGIC CHẶT BÀI ---
        // (Giữ nguyên logic chặt từ lần trước, nhưng đảm bảo nó gọi các phương thức của ruleSet
        // để lấy giá trị rank, comparator, và getCombinationIdentifier)

        // Ví dụ cho Tứ Quý:
        if (newType == TienLenVariantRuleSet.CombinationType.FOUR_OF_KIND) {
            if (prevIsSingleTwo) return true;
            // if (prevIsPairOfTwos && ruleSet.allowsSpecialBeat(newType, prevType)) return true; // Cần phương thức trong RuleSet
            if (prevType == TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT /* && ruleSet.allowsSpecialBeat(newType, prevType) */) return true;
            if (prevType == TienLenVariantRuleSet.CombinationType.FOUR_OF_KIND) {
                return ruleSet.getCardComparator().compare(sortedNewCards.get(0), sortedPreviousCards.get(0)) > 0;
            }
            return false; // Tứ quý thường không đánh vào bộ thường khác loại
        }
        
        // Ví dụ cho Ba Đôi Thông:
        if (newType == TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT) {
            if (prevIsSingleTwo) return true;
            if (prevType == TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT) {
                // So sánh lá đại diện của đôi thông (thường là lá cao nhất của đôi cao nhất)
                return ruleSet.getCardComparator().compare(
                    ruleSet.getRepresentativeCardForCombination(sortedNewCards),
                    ruleSet.getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            return false;
        }
        
        // TODO: Bổ sung cho Bốn Đôi Thông (FOUR_PAIR_STRAIGHT)
        if (newType == TienLenVariantRuleSet.CombinationType.FOUR_PAIR_STRAIGHT) {
            if (prevIsSingleTwo || prevIsPairOfTwos || 
                prevType == TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT || 
                prevType == TienLenVariantRuleSet.CombinationType.FOUR_OF_KIND) return true;
            if (prevType == TienLenVariantRuleSet.CombinationType.FOUR_PAIR_STRAIGHT) {
                 return ruleSet.getCardComparator().compare(
                    ruleSet.getRepresentativeCardForCombination(sortedNewCards),
                    ruleSet.getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            return false; 
        }


        // Nếu newCards là bộ chặt nhưng không rơi vào trường hợp chặt ở trên
        if ((newType == TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT ||
            newType == TienLenVariantRuleSet.CombinationType.FOUR_OF_KIND ||
            newType == TienLenVariantRuleSet.CombinationType.FOUR_PAIR_STRAIGHT) &&
            newType != prevType) { // Và không phải là chặt cùng loại lớn hơn (đã xử lý ở trên)
            return false; 
        }


        // --- ĐÁNH THƯỜNG ---
        if (newType != prevType) return false; // Phải cùng loại

        // Xử lý yêu cầu cùng độ dài của TLMB (RuleSet sẽ quyết định)
        if (newCards.size() != previousCards.size()) {
            // Nếu ruleSet yêu cầu sảnh phải cùng độ dài (ví dụ TLMB) thì sẽ return false
            // Bạn có thể thêm một phương thức vào TienLenVariantRuleSet:
            // boolean mustPlaySameLength(CombinationType type)
            // if (ruleSet.mustPlaySameLengthForType(newType)) return false;
            // Hiện tại, mặc định là phải cùng độ dài cho tất cả các bộ khi đánh thường
            return false;
        }

        Card newRepCard = ruleSet.getRepresentativeCardForCombination(sortedNewCards);
        Card prevRepCard = ruleSet.getRepresentativeCardForCombination(sortedPreviousCards);

        if (newRepCard == null || prevRepCard == null) return false;

        // So sánh dựa trên giá trị rank (không xét chất)
        return ruleSet.getCardRankValue(newRepCard) > ruleSet.getCardRankValue(prevRepCard);
    }
}
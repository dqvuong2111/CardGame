// Đề xuất đổi tên file thành: core/games/tienlen/logic/TienLenPlayabilityLogic.java
package core.games.tienlen.logic; // Hoặc package hiện tại của bạn nếu muốn giữ nguyên

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet; // << SỬ DỤNG INTERFACE NÀY

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TienLenPlayabilityLogic { // Đổi tên lớp cho phù hợp

    public static boolean canPlayAfter(List<Card> newCards, List<Card> previousCards, TienLenVariantRuleSet ruleSet) {
        if (newCards == null || newCards.isEmpty()) return false;

        // Sử dụng ruleSet để lấy thông tin và thực hiện so sánh
        // Lấy loại tổ hợp thông qua ruleSet
        Object newTypeObject = ruleSet.getCombinationIdentifier(newCards);
        // Kiểm tra xem newTypeObject có phải là một trong các giá trị của enum CombinationType không
        if (!(newTypeObject instanceof TienLenVariantRuleSet.CombinationType) || 
            newTypeObject == TienLenVariantRuleSet.CombinationType.INVALID) {
            return false;
        }
        TienLenVariantRuleSet.CombinationType newType = (TienLenVariantRuleSet.CombinationType) newTypeObject;

        // Nếu không có bài trên bàn, chỉ cần bộ bài mới hợp lệ
        if (previousCards == null || previousCards.isEmpty()) {
            return ruleSet.isValidCombination(newCards); // ruleSet.isValidCombination sẽ tự biết luật cụ thể
        }

        Object prevTypeObject = ruleSet.getCombinationIdentifier(previousCards);
        if (!(prevTypeObject instanceof TienLenVariantRuleSet.CombinationType) ||
            prevTypeObject == TienLenVariantRuleSet.CombinationType.INVALID) {
            // Nếu bài trên bàn không hợp lệ, cho phép đánh đè nếu bài mới hợp lệ
            return newType != TienLenVariantRuleSet.CombinationType.INVALID;
        }
        TienLenVariantRuleSet.CombinationType prevType = (TienLenVariantRuleSet.CombinationType) prevTypeObject;


        List<Card> sortedNewCards = new ArrayList<>(newCards);
        Collections.sort(sortedNewCards, ruleSet.getCardComparator());
        List<Card> sortedPreviousCards = new ArrayList<>(previousCards);
        Collections.sort(sortedPreviousCards, ruleSet.getCardComparator());

        // Lấy giá trị của quân 2 từ ruleSet
        int valueOfTwo = ruleSet.getTwoRankValue(); 

        boolean prevIsSingleTwo = (prevType == TienLenVariantRuleSet.CombinationType.SINGLE &&
                                   sortedPreviousCards.size() == 1 && // Đảm bảo là một lá đơn
                                   ruleSet.getCardRankValue(sortedPreviousCards.get(0)) == valueOfTwo);
        boolean prevIsPairOfTwos = (prevType == TienLenVariantRuleSet.CombinationType.PAIR &&
                                    sortedPreviousCards.size() == 2 && // Đảm bảo là đôi
                                    ruleSet.getCardRankValue(sortedPreviousCards.get(0)) == valueOfTwo &&
                                    ruleSet.getCardRankValue(sortedPreviousCards.get(1)) == valueOfTwo);

        // Logic chặt đặc biệt (Tứ quý, Đôi thông)
        // Các ruleSet cụ thể (Miền Nam, Miền Bắc) sẽ định nghĩa getCombinationIdentifier
        // và isValidCombination cho các bộ này.
        // canPlayAfter của ruleSet cụ thể cũng có thể override logic này nếu cần.

        if (newType == TienLenVariantRuleSet.CombinationType.FOUR_OF_KIND) {
            if (prevIsSingleTwo) return true;
            // Để kiểm tra tứ quý chặt đôi heo, ruleSet cần có phương thức quy định điều này
            // Ví dụ: if (prevIsPairOfTwos && ruleSet.canFourOfAKindBeatPairOfTwos()) return true;
            // Tạm thời giả định tứ quý có thể chặt đôi heo nếu ruleSet cho phép (có thể thêm vào interface)
            if (prevIsPairOfTwos) return true; // Giả định chung là được

            if (prevType == TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT && ruleSetCanFourOfAKindBeatThreePairStraight(ruleSet)) return true; // TLMB
            
            if (prevType == TienLenVariantRuleSet.CombinationType.FOUR_OF_KIND) { // Chặt tứ quý nhỏ hơn
                return ruleSet.getCardComparator().compare(
                    ruleSet.getRepresentativeCardForCombination(sortedNewCards), // Lấy lá đại diện để so sánh rank
                    ruleSet.getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            // Theo luật chặt chẽ, tứ quý chỉ để chặt các trường hợp trên hoặc tứ quý khác.
            // Ngăn tứ quý đánh vào các bộ thường khác loại.
            return false;
        }

        if (newType == TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT) {
            if (prevIsSingleTwo) return true; // 3 đôi thông chặt 1 heo
            if (prevType == TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT) { // Chặt 3 đôi thông nhỏ hơn
                return ruleSet.getCardComparator().compare(
                    ruleSet.getRepresentativeCardForCombination(sortedNewCards),
                    ruleSet.getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            // Ngăn 3 đôi thông đánh vào các bộ thường khác loại (trừ heo) hoặc tứ quý.
            return false;
        }

        if (newType == TienLenVariantRuleSet.CombinationType.FOUR_PAIR_STRAIGHT) {
            if (prevIsSingleTwo || prevIsPairOfTwos) return true;
            if (prevType == TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT) return true;
            if (prevType == TienLenVariantRuleSet.CombinationType.FOUR_OF_KIND) return true;
            if (prevType == TienLenVariantRuleSet.CombinationType.FOUR_PAIR_STRAIGHT) { // Chặt 4 đôi thông nhỏ hơn
                 return ruleSet.getCardComparator().compare(
                    ruleSet.getRepresentativeCardForCombination(sortedNewCards),
                    ruleSet.getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            // Ngăn 4 đôi thông đánh vào các bộ thường khác loại (trừ các trường hợp chặt trên).
            return false;
        }

        // Nếu bộ bài mới là một bộ "chặt" đặc biệt (Tứ Quý, Đôi Thông)
        // nhưng không rơi vào các trường hợp chặt đặc biệt đã xử lý ở trên,
        // thì nước đi đó không hợp lệ (ví dụ: dùng Tứ Quý đánh vào một đôi thường).
        boolean newIsSpecialChainingOrBomb = newType == TienLenVariantRuleSet.CombinationType.FOUR_OF_KIND ||
                                             newType == TienLenVariantRuleSet.CombinationType.THREE_PAIR_STRAIGHT ||
                                             newType == TienLenVariantRuleSet.CombinationType.FOUR_PAIR_STRAIGHT;
        
        if (newIsSpecialChainingOrBomb) {
            // Nếu đến đây mà newType là bộ chặt, có nghĩa là nó không thỏa mãn điều kiện chặt nào ở trên.
            // (ví dụ, Tứ Quý không có Heo để chặt, không có Tứ Quý khác để chặt, v.v.)
            // Do đó, nó không được đánh ra trong trường hợp này.
            return false;
        }

        // Đánh thường: cùng loại và cùng số lượng lá bài
        if (newType != prevType) {
             // Kiểm tra xem có phải là sảnh dài hơn chặt sảnh ngắn hơn không (luật TLMN)
             // if (ruleSet.allowLongerStraightToBeatShorter() && 
             //     newType == TienLenVariantRuleSet.CombinationType.STRAIGHT && 
             //     prevType == TienLenVariantRuleSet.CombinationType.STRAIGHT &&
             //     newCards.size() > previousCards.size()) {
             //     // Fall through to rank comparison
             // } else {
                 return false; // Các trường hợp khác phải cùng loại
             // }
        }

        if (newCards.size() != previousCards.size()) {
            // Nếu ruleSet yêu cầu sảnh phải cùng độ dài (ví dụ TLMB)
            // Hoặc nếu là các bộ khác (đôi, ba) thì chắc chắn phải cùng độ dài.
            // Bạn có thể thêm một phương thức vào TienLenVariantRuleSet:
            // boolean mustPlaySameLengthForType(CombinationType type)
            // if (ruleSet.mustPlaySameLengthForType(newType)) return false;
            // Hiện tại, code cũ của bạn mặc định là phải cùng độ dài cho mọi trường hợp đánh thường.
            return false;
        }

        // So sánh lá bài đại diện của hai bộ
        Card newRepCard = ruleSet.getRepresentativeCardForCombination(sortedNewCards);
        Card prevRepCard = ruleSet.getRepresentativeCardForCombination(sortedPreviousCards);

        if (newRepCard == null || prevRepCard == null) return false;

        // Sử dụng comparator của ruleSet để so sánh.
        // Comparator này sẽ tự xử lý việc so sánh rank trước hay suit trước tùy theo luật.
        return ruleSet.getCardComparator().compare(newRepCard, prevRepCard) > 0;
    }

    // Hàm helper ví dụ, bạn cần thêm vào TienLenVariantRuleSet và implement trong các Rule cụ thể
    private static boolean ruleSetCanFourOfAKindBeatThreePairStraight(TienLenVariantRuleSet ruleSet) {
        if (ruleSet instanceof core.games.tienlen.tienlenmienbac.TienLenMienBacRule) { // Ví dụ
            return true; // TLMB: Tứ quý chặt được 3 đôi thông
        }
        // if (ruleSet instanceof core.games.tienlen.tienlenmiennam.TienLenMienNamRule) {
        //     return false; // TLMN: Tứ quý không chặt được 3 đôi thông
        // }
        return false; // Mặc định
    }
}
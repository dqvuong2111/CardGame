package core.games.tienlen.tienlenmiennam.logic;

import core.Card;
import core.games.tienlen.tienlenmiennam.TienLenMienNamRule; // Để truy cập CombinationType, comparator, getTienLenValue, getRepresentativeCardForCombination

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TienLenMienNamPlayabilityLogic {

    public static boolean canPlayAfter(List<Card> newCards, List<Card> previousCards, TienLenMienNamRule ruleInstance) {
        if (newCards == null || newCards.isEmpty()) return false;
        if (previousCards == null || previousCards.isEmpty()) {
            return ruleInstance.isValidCombination(newCards); // Gọi qua instance để dùng getCombinationType đã được ủy nhiệm đúng
        }

        // Sử dụng getCombinationIdentifier của ruleInstance (đã được ủy nhiệm cho TienLenMienNamCombinationLogic)
        TienLenMienNamRule.CombinationType newType = (TienLenMienNamRule.CombinationType) ruleInstance.getCombinationIdentifier(newCards);
        TienLenMienNamRule.CombinationType prevType = (TienLenMienNamRule.CombinationType) ruleInstance.getCombinationIdentifier(previousCards);

        if (newType == TienLenMienNamRule.CombinationType.INVALID) return false;
        if (prevType == TienLenMienNamRule.CombinationType.INVALID && newType != TienLenMienNamRule.CombinationType.INVALID) return true;

        List<Card> sortedNewCards = new ArrayList<>(newCards);
        Collections.sort(sortedNewCards, ruleInstance.getCardComparator());
        List<Card> sortedPreviousCards = new ArrayList<>(previousCards);
        Collections.sort(sortedPreviousCards, ruleInstance.getCardComparator());

        boolean prevIsTwo = (prevType == TienLenMienNamRule.CombinationType.SINGLE && TienLenMienNamRule.getTienLenValue(sortedPreviousCards.get(0)) == 15);
        boolean prevIsPairOfTwos = (prevType == TienLenMienNamRule.CombinationType.PAIR && TienLenMienNamRule.getTienLenValue(sortedPreviousCards.get(0)) == 15);

        // Logic chặt đặc biệt (Tứ quý, Đôi thông)
        if (newType == TienLenMienNamRule.CombinationType.FOUR_OF_KIND) {
            if (prevIsTwo || prevIsPairOfTwos) return true;
            if (prevType == TienLenMienNamRule.CombinationType.FOUR_OF_KIND) {
                return ruleInstance.getCardComparator().compare(sortedNewCards.get(0), sortedPreviousCards.get(0)) > 0;
            }
            // Theo luật chặt chẽ, tứ quý chỉ để chặt heo hoặc tứ quý khác, không đánh thường vào các bộ khác nếu không cùng loại
             if (prevType != TienLenMienNamRule.CombinationType.SINGLE && prevType != TienLenMienNamRule.CombinationType.PAIR) return false; // Chỉ xét chặt heo nếu prev không phải tứ quý
        }
        if (newType == TienLenMienNamRule.CombinationType.THREE_PAIR_STRAIGHT) {
            if (prevIsTwo) return true;
            if (prevType == TienLenMienNamRule.CombinationType.THREE_PAIR_STRAIGHT) {
                return ruleInstance.getCardComparator().compare(
                    ruleInstance.getRepresentativeCardForCombination(sortedNewCards),
                    ruleInstance.getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            if (prevType != TienLenMienNamRule.CombinationType.SINGLE) return false; // Chỉ chặt heo đơn
        }
        if (newType == TienLenMienNamRule.CombinationType.FOUR_PAIR_STRAIGHT) {
            if (prevIsTwo || prevIsPairOfTwos ||
                prevType == TienLenMienNamRule.CombinationType.FOUR_OF_KIND ||
                prevType == TienLenMienNamRule.CombinationType.THREE_PAIR_STRAIGHT) return true;
            if (prevType == TienLenMienNamRule.CombinationType.FOUR_PAIR_STRAIGHT) {
                 return ruleInstance.getCardComparator().compare(
                    ruleInstance.getRepresentativeCardForCombination(sortedNewCards),
                    ruleInstance.getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            // Chỉ chặt các trường hợp đặc biệt hoặc cùng loại
            if (prevType != TienLenMienNamRule.CombinationType.SINGLE && prevType != TienLenMienNamRule.CombinationType.PAIR) return false;

        }

        // Nếu là các bộ "chặt" mà không phải trường hợp chặt đặc biệt ở trên, thì phải cùng loại
        boolean newIsSpecialChump = newType == TienLenMienNamRule.CombinationType.FOUR_OF_KIND ||
                                    newType == TienLenMienNamRule.CombinationType.THREE_PAIR_STRAIGHT ||
                                    newType == TienLenMienNamRule.CombinationType.FOUR_PAIR_STRAIGHT;
        if (newIsSpecialChump && newType != prevType) {
             // Nếu không rơi vào các trường hợp chặt đặc biệt ở trên thì không hợp lệ
             // (ví dụ: Tứ quý không tự nhiên đánh vào đôi thường)
             // Logic này đã được bao gồm trong các điều kiện if ở trên.
        }


        // Đánh thường: cùng loại, cùng số lượng (trừ sảnh có thể khác độ dài)
        if (newType != prevType) return false;

        if (newCards.size() != previousCards.size()) {
            // Luật TLMN thường yêu cầu sảnh phải cùng độ dài.
            // Các bộ khác (đôi, ba) chắc chắn phải cùng độ dài.
            return false;
        }

        Card newRepCard = ruleInstance.getRepresentativeCardForCombination(sortedNewCards);
        Card prevRepCard = ruleInstance.getRepresentativeCardForCombination(sortedPreviousCards);

        return ruleInstance.getCardComparator().compare(newRepCard, prevRepCard) > 0;
    }
}
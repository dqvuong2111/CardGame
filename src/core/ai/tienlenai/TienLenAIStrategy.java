package core.ai.tienlenai;

import java.util.List;
import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;
public interface TienLenAIStrategy {
    /**
     * Chọn các lá bài để đánh dựa trên chiến lược cụ thể.
     *
     * @param currentHand     Các lá bài hiện tại trên tay AI.
     * @param lastPlayedCards Các lá bài đã được đánh ở lượt trước.
     * @param ruleSet         Bộ luật của game để kiểm tra tính hợp lệ.
     * @param isFirstTurn     Cho biết đây có phải là lượt đầu tiên của ván game không.
     * @return Danh sách các lá bài AI chọn để đánh (trả về danh sách rỗng nếu bỏ lượt).
     */
    List<Card> chooseCards(List<Card> currentHand, List<Card> lastPlayedCards, TienLenVariantRuleSet ruleSet, boolean isFirstTurn);
}
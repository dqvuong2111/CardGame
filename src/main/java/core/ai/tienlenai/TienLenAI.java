package core.ai.tienlenai;

import java.util.ArrayList;
import java.util.List;

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.tienlenplayer.TienLenPlayer;

public class TienLenAI extends TienLenPlayer {

    // Enum này có thể vẫn hữu ích cho SceneManager để chọn loại strategy
    public enum StrategyType { // Đổi tên từ AIStrategy để tránh trùng với interface
        RANDOM,
        GREEDY,
        SMART
    }

    private TienLenAIStrategy currentStrategyImplementation; // Tham chiếu đến đối tượng strategy cụ thể
    private TienLenVariantRuleSet ruleSet; // RuleSet vẫn cần thiết cho các strategy

    public TienLenAI(String name, TienLenAIStrategy strategyImplementation, TienLenVariantRuleSet ruleSet) {
        super(name, true);
        this.currentStrategyImplementation = strategyImplementation;
        this.ruleSet = ruleSet;
    }

    public void setStrategy(TienLenAIStrategy strategyImplementation) {
        this.currentStrategyImplementation = strategyImplementation;
    }
    
    

    /**
     * Chọn lá bài để đánh dựa trên chiến lược AI hiện tại.
     * Chữ ký phương thức này cần khớp với cách nó được gọi trong TienLenGame.
     */
    public List<Card> chooseCards(List<Card> lastPlayedCards, boolean isFirstTurn) {
        if (currentStrategyImplementation != null) {
            // Truyền các thông tin cần thiết cho strategy, bao gồm cả tay bài của AI hiện tại (this.getHand())
            return currentStrategyImplementation.chooseCards(this.getHand(), lastPlayedCards, this.ruleSet, isFirstTurn);
        }
        System.err.println("Lỗi: AIPlayer " + getName() + " không có chiến lược nào được thiết lập!");
        return new ArrayList<>(); // Trả về rỗng (bỏ lượt) nếu không có strategy
    }
    
    

    // Các phương thức private chooseRandomCards, chooseGreedyCards, chooseSmartCards
    // và tất cả các hàm helper (findAllPairs, findSmallestPlayableCard, v.v.)
    // SẼ ĐƯỢC XÓA KHỎI ĐÂY vì logic của chúng đã được chuyển vào các lớp Strategy tương ứng.
}
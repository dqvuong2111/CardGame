package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator; // Import Comparator
import java.util.List;

public class Player {
    protected String name;
    protected List<Card> hand;
    protected boolean isAI;
    protected boolean hasNoCards; // Thêm trạng thái này
    protected int winnerRank; // 1 là nhất, 2 là nhì, ...

    public Player(String name, boolean isAI) {
        this.name = name;
        this.isAI = isAI;
        this.hand = new ArrayList<>();
        this.hasNoCards = false; // Khởi tạo ban đầu là chưa hết bài
        this.winnerRank = 0; // 0 nghĩa là chưa có hạng
    }

    public String getName() {
        return name;
    }

    public boolean isAI() {
        return isAI;
    }

    public void addCard(Card card) {
        if (card != null) {
            hand.add(card);
        }
    }

    public void addCards(List<Card> cards) {
        if (cards != null) {
            hand.addAll(cards);
        }
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public void removeCards(List<Card> cardsToRemove) {
        hand.removeAll(cardsToRemove);
    }

    public List<Card> getHand() {
        return hand;
    }

    // Phương thức sắp xếp bài theo thứ tự tự nhiên của Card (compareTo)
    public void sortHand() {
        Collections.sort(hand);
    }

    // PHƯƠNG THỨC MỚI: Sắp xếp bài theo Comparator cụ thể (từ RuleSet)
    public void sortHand(Comparator<Card> comparator) {
        hand.sort(comparator);
    }

    public boolean hasNoCards() {
        return hand.isEmpty() || hasNoCards; // Hết bài hoặc đã đánh dấu là hết bài
    }

    public void setHasNoCards(boolean hasNoCards) {
        this.hasNoCards = hasNoCards;
    }

    public int getWinnerRank() {
        return winnerRank;
    }

    public void setWinnerRank(int winnerRank) {
        this.winnerRank = winnerRank;
    }
    
    public void clearWinnerRank() {
        this.winnerRank = 0; // Đặt lại thứ hạng về 0
    }
}
package core.games.tienlen.tienlenmienbac; // Hoặc package bạn đã chọn

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;
// Import các lớp logic bạn có thể sẽ tạo riêng cho Miền Bắc sau này (tùy chọn)
// import core.games.tienlen.tienlenmienbac.logic.TienLenMienBacCombinationLogic;
// import core.games.tienlen.tienlenmienbac.logic.TienLenMienBacPlayabilityLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TienLenMienBacRule implements TienLenVariantRuleSet {

    // --- Định nghĩa các loại tổ hợp bài cho TLMB ---
    // (Có thể giống hoặc khác TLMN tùy theo mức độ chi tiết bạn muốn)
    public enum CombinationType {
        SINGLE, PAIR, TRIPLE, STRAIGHT, // Sảnh trong TLMB phải đồng chất
        FOUR_OF_KIND, 
        THREE_PAIR_STRAIGHT, // 3 đôi thông đồng chất (ít phổ biến, thường không cần đồng chất)
                                            // Thông thường 3 đôi thông, 4 đôi thông không cần đồng chất để chặt 2
        FOUR_PAIR_STRAIGHT,  // 4 đôi thông đồng chất (rất hiếm)
        // Các bộ đặc biệt khác nếu có
        INVALID
    }

    // --- Comparator cho TLMB ---
    // Giá trị quân bài quan trọng hơn chất khi so sánh sức mạnh
    // Chất chỉ dùng để xác định người đi đầu tiên có 3 bích, hoặc so sánh 2 lá bài cùng giá trị (nếu luật bạn áp dụng)
    private static final Comparator<Card> TIEN_LEN_MIEN_BAC_CARD_COMPARATOR = new TienLenMienBacCardComparator();

    // Hàm lấy giá trị quân bài cho TLMB (có thể giống TLMN)
    public static int getTienLenBacValue(Card card) {
        if (card.getRank() == Card.Rank.TWO) return 15; // Heo (2) là lớn nhất
        if (card.getRank() == Card.Rank.ACE) return 14; // Át
        if (card.getRank() == Card.Rank.KING) return 13; // Già
        // ... (các rank khác tương tự như TLMN) ...
        if (card.getRank() == Card.Rank.QUEEN) return 12;
        if (card.getRank() == Card.Rank.JACK) return 11;
        return card.getRank().getValue(); // Đối với các lá số từ 3 đến 10
    }
    
    // Hàm lấy thứ tự chất (ví dụ: Cơ > Rô > Chuồn > Bích)
    // Thứ tự này có thể dùng để phá hòa khi so sánh 2 lá bài lẻ cùng giá trị (ít dùng khi chặt)
    // Hoặc dùng để xác định lá 3 Bích.
    public static int getSuitOrderValue(Card.Suit suit) {
        return switch (suit) {
            case SPADES -> 1;   // Bích (thấp nhất nếu dùng để so sánh hơn thua trực tiếp)
            case CLUBS -> 2;    // Chuồn/Tép
            case DIAMONDS -> 3; // Rô
            case HEARTS -> 4;   // Cơ (cao nhất)
        };
    }


    private static class TienLenMienBacCardComparator implements Comparator<Card> {
        @Override
        public int compare(Card card1, Card card2) {
            int value1 = getTienLenBacValue(card1);
            int value2 = getTienLenBacValue(card2);
            if (value1 != value2) {
                return Integer.compare(value1, value2);
            }
            // Nếu cùng giá trị, TLMB thường không so chất để phân định hơn thua khi chặt.
            // Chất chỉ quan trọng khi xét 3 bích hoặc một số luật đặc biệt.
            // Để comparator này hoàn chỉnh, ta vẫn có thể so sánh chất (ví dụ để sắp xếp tay bài)
            return Integer.compare(getSuitOrderValue(card1.getSuit()), getSuitOrderValue(card2.getSuit()));
        }
    }

    @Override
    public Comparator<Card> getCardComparator() {
        return TIEN_LEN_MIEN_BAC_CARD_COMPARATOR;
    }

    // --- Các phương thức chính cần implement cho TLMB ---

    @Override
    public Object getCombinationIdentifier(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return CombinationType.INVALID;
        }
        // Sắp xếp để dễ nhận diện
        List<Card> sortedCards = new ArrayList<>(cards);
        Collections.sort(sortedCards, getCardComparator());

        // === LOGIC NHẬN DIỆN BỘ BÀI CHO TLMB SẼ Ở ĐÂY ===
        // Ví dụ đơn giản cho SINGLE, PAIR, TRIPLE, FOUR_OF_KIND (không cần đồng chất)
        int size = sortedCards.size();
        if (size == 1) return CombinationType.SINGLE;

        // Nhận diện SẢNH (phải đồng chất và liên tiếp)
        if (size >= 3 && isMienBacStraight(sortedCards)) {
            return CombinationType.STRAIGHT;
        }
        
        // Kiểm tra Ba Đôi Thông (không yêu cầu đồng chất)
        if (size == 6 && isThreeConsecutivePairs(sortedCards)) {
            return CombinationType.THREE_PAIR_STRAIGHT; // Bạn cần thêm enum này nếu chưa có
        }

        // Kiểm tra Bốn Đôi Thông (không yêu cầu đồng chất)
        if (size == 8 && isFourConsecutivePairs(sortedCards)) {
            return CombinationType.FOUR_PAIR_STRAIGHT; // Bạn cần thêm enum này nếu chưa có
        }
        
        // TODO: Thêm logic nhận diện cho 3 ĐÔI THÔNG, 4 ĐÔI THÔNG (thường không cần đồng chất)
        // Ví dụ: isThreeConsecutivePairs, isFourConsecutivePairs

        boolean allSameRank = true;
        for (int i = 1; i < size; i++) {
            if (sortedCards.get(i).getRank() != sortedCards.get(0).getRank()) {
                allSameRank = false;
                break;
            }
        }
        if (allSameRank) {
            if (size == 2) return CombinationType.PAIR;
            if (size == 3) return CombinationType.TRIPLE;
            if (size == 4) return CombinationType.FOUR_OF_KIND;
        }
        
        
        return CombinationType.INVALID;
    }

    @Override
    public boolean isValidCombination(List<Card> cards) {
        return getCombinationIdentifier(cards) != CombinationType.INVALID;
    }

    // Helper cho nhận diện sảnh TLMB (đồng chất, liên tiếp, không có 2)
    private boolean isMienBacStraight(List<Card> sortedCards) {
        if (sortedCards.size() < 3) return false;
        Card.Suit firstSuit = sortedCards.get(0).getSuit();
        for (int i = 0; i < sortedCards.size(); i++) {
            Card card = sortedCards.get(i);
            if (card.getSuit() != firstSuit) return false; // Phải đồng chất
            if (getTienLenBacValue(card) == 15) return false; // Không được có quân 2 (Heo)
            if (i > 0 && getTienLenBacValue(card) - getTienLenBacValue(sortedCards.get(i-1)) != 1) {
                return false; // Phải liên tiếp về giá trị
            }
        }
        return true;
    }
    
    private boolean isThreeConsecutivePairs(List<Card> sortedCards) {
        if (sortedCards.size() != 6) return false;
        // Kiểm tra có 3 đôi không
        for (int i = 0; i < 6; i += 2) {
            if (sortedCards.get(i).getRank() != sortedCards.get(i+1).getRank()) return false;
            if (getTienLenBacValue(sortedCards.get(i)) == 15) return false; // Không có 2 trong đôi thông
        }
        // Kiểm tra tính liên tiếp của các đôi (dựa trên giá trị rank đã sort)
        if (getTienLenBacValue(sortedCards.get(2)) - getTienLenBacValue(sortedCards.get(0)) != 1) return false;
        if (getTienLenBacValue(sortedCards.get(4)) - getTienLenBacValue(sortedCards.get(2)) != 1) return false;
        return true;
    }

    // Hàm helper cho 4 đôi thông (tương tự)
    private boolean isFourConsecutivePairs(List<Card> sortedCards) {
        if (sortedCards.size() != 8) return false;
        for (int i = 0; i < 8; i += 2) {
            if (sortedCards.get(i).getRank() != sortedCards.get(i+1).getRank()) return false;
            if (getTienLenBacValue(sortedCards.get(i)) == 15) return false;
        }
        if (getTienLenBacValue(sortedCards.get(2)) - getTienLenBacValue(sortedCards.get(0)) != 1) return false;
        if (getTienLenBacValue(sortedCards.get(4)) - getTienLenBacValue(sortedCards.get(2)) != 1) return false;
        if (getTienLenBacValue(sortedCards.get(6)) - getTienLenBacValue(sortedCards.get(4)) != 1) return false;
        return true;
    }


    @Override
    public boolean canPlayAfter(List<Card> newCards, List<Card> previousCards) {
        if (newCards == null || newCards.isEmpty()) return false;

        CombinationType newType = (CombinationType) getCombinationIdentifier(newCards);
        if (newType == CombinationType.INVALID) return false;

        // Trường hợp mở đầu vòng mới, mọi bộ hợp lệ đều được đánh (trừ 3 bích ở lượt đầu game đã xử lý riêng)
        if (previousCards == null || previousCards.isEmpty()) {
            return true; 
        }

        CombinationType prevType = (CombinationType) getCombinationIdentifier(previousCards);
        if (prevType == CombinationType.INVALID) return true; // Bài trên bàn không hợp lệ? Cho phép đánh đè.

        List<Card> sortedNew = new ArrayList<>(newCards);
        Collections.sort(sortedNew, getCardComparator());
        List<Card> sortedPrev = new ArrayList<>(previousCards);
        Collections.sort(sortedPrev, getCardComparator());

        // Xử lý các trường hợp chặt đặc biệt trước
        // 1. Chặt Heo (Quân 2 có giá trị 15)
        boolean prevIsSingleTwo = (prevType == CombinationType.SINGLE && getTienLenBacValue(sortedPrev.get(0)) == 15);
        boolean prevIsPairOfTwos = (prevType == CombinationType.PAIR && sortedPrev.size() == 2 && getTienLenBacValue(sortedPrev.get(0)) == 15);

        if (newType == CombinationType.THREE_PAIR_STRAIGHT) {
            if (prevIsSingleTwo) return true; // 3 đôi thông chặt 1 heo
            // 3 đôi thông chặt 3 đôi thông nhỏ hơn
            if (prevType == CombinationType.THREE_PAIR_STRAIGHT) {
                return getTienLenBacValue(getRepresentativeCardForCombination(sortedNew)) > getTienLenBacValue(getRepresentativeCardForCombination(sortedPrev));
            }
            // 3 đôi thông không chặt các bộ thường khác (trừ khi luật của bạn khác)
            return false; 
        }

        if (newType == CombinationType.FOUR_OF_KIND) { // Tứ quý
            if (prevIsSingleTwo) return true;
            if (prevIsPairOfTwos && ruleSetAllowsFourOfAKindToBeatPairOfTwos()) return true; // Cần hàm này nếu luật cho phép
            if (prevType == CombinationType.THREE_PAIR_STRAIGHT) return true; // Tứ quý chặt 3 đôi thông
            if (prevType == CombinationType.FOUR_OF_KIND) { // Tứ quý chặt tứ quý nhỏ hơn
                return getTienLenBacValue(sortedNew.get(0)) > getTienLenBacValue(sortedPrev.get(0));
            }
            // Tứ quý thường không dùng để đánh các bộ thường khác
            return false; 
        }

        if (newType == CombinationType.FOUR_PAIR_STRAIGHT) {
            if (prevIsSingleTwo || prevIsPairOfTwos || prevType == CombinationType.THREE_PAIR_STRAIGHT || prevType == CombinationType.FOUR_OF_KIND) return true;
            if (prevType == CombinationType.FOUR_PAIR_STRAIGHT) { // Chặt 4 đôi thông nhỏ hơn
                return getTienLenBacValue(getRepresentativeCardForCombination(sortedNew)) > getTienLenBacValue(getRepresentativeCardForCombination(sortedPrev));
            }
            return false; 
        }

        // Nếu newCards là một trong các bộ chặt đặc biệt mà không rơi vào các trường hợp chặt ở trên
        // thì nó không thể đánh vào các bộ thường khác loại.
        if (newType == CombinationType.THREE_PAIR_STRAIGHT || 
            newType == CombinationType.FOUR_OF_KIND || 
            newType == CombinationType.FOUR_PAIR_STRAIGHT) {
            return false; // Chỉ được dùng để chặt hoặc đánh vào loại tương ứng lớn hơn
        }


        // 2. Đánh thường: phải cùng loại, cùng số lượng lá, và lớn hơn về giá trị rank của lá đại diện
        if (newType != prevType || newCards.size() != previousCards.size()) {
            return false;
        }

        Card newRepCard = getRepresentativeCardForCombination(sortedNew);
        Card prevRepCard = getRepresentativeCardForCombination(sortedPrev);

        // So sánh giá trị rank, không xét chất ở đây
        return getTienLenBacValue(newRepCard) > getTienLenBacValue(prevRepCard);
    }
    
    // Ví dụ một hàm kiểm tra luật tứ quý chặt đôi heo (bạn có thể đặt các cờ luật ở đây)
    private boolean ruleSetAllowsFourOfAKindToBeatPairOfTwos() {
        return true; // Mặc định là cho phép, bạn có thể thay đổi
    }


    @Override
    public Card getRepresentativeCardForCombination(List<Card> combination) {
        if (combination == null || combination.isEmpty()) return null;
        List<Card> sortedCombination = new ArrayList<>(combination);
        Collections.sort(sortedCombination, getCardComparator());
        // Với sảnh, đôi, sám, tứ quý, lá bài đại diện thường là lá có giá trị lớn nhất.
        // Chất của lá đại diện thường không quá quan trọng khi so sánh các bộ trong TLMB, trừ khi luật cụ thể.
        return sortedCombination.get(sortedCombination.size() - 1);
    }

    @Override
    public boolean isCardValidInStraight(Card card) {
        return getTienLenBacValue(card) < 15; // Heo (giá trị 15) không được trong sảnh
    }

    // Các phương thức tiện ích khác (nếu cần, tương tự TienLenMienNamRule)
    public boolean hasStartingCard(List<Card> cards) { // Kiểm tra 3 Bích
        return cards.contains(new Card(Card.Suit.SPADES, Card.Rank.THREE));
    }

    public String getCardsDisplay(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return "Không có bài nào";
        return cards.stream().map(Card::toString).collect(Collectors.joining(" ")); // Dùng khoảng trắng thay vì dấu phẩy
    }
    
    @Override
    public int getCardRankValue(Card card) {
        return getTienLenBacValue(card); // Hoặc getTienLenBacValue(card)
    }
    @Override
    public int getTwoRankValue() {
        return 15; // Hoặc giá trị bạn dùng cho quân 2
    }
}
package core.rules;

import core.Card;
import core.RuleSet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Lớp quy định luật chơi cho Tiến Lên Miền Nam
 */
public class TienLenRule implements RuleSet {
    // Định nghĩa các loại bài
    public enum CombinationType {
        SINGLE,       // Đánh 1 lá
        PAIR,         // Đánh đôi
        TRIPLE,       // Đánh 3 con
        STRAIGHT,     // Dây/Sảnh
        FOUR_OF_KIND,  // Tứ quý
        THREE_PAIR_STRAIGHT, // Ba đôi thông
        FOUR_PAIR_STRAIGHT,   // Bốn đôi thông
        INVALID       // Tổ hợp không hợp lệ
    }
    
    // Comparator cho bài Tiến Lên - Sử dụng static final để tái sử dụng
    private static final Comparator<Card> TIEN_LEN_CARD_COMPARATOR = new TienLenCardComparator();
    
    /**
     * Xác định và trả về loại bài của tổ hợp đầu vào
     */
    public static CombinationType getCombinationType(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return CombinationType.INVALID; // Không có bài là không hợp lệ
        }

        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort(TIEN_LEN_CARD_COMPARATOR); // Sắp xếp một lần theo luật TLMN

        int size = sortedCards.size();

        // Kiểm tra các loại tổ hợp theo kích thước
        switch (size) {
            case 1:
                return CombinationType.SINGLE;
            case 2:
                if (isPair(sortedCards)) return CombinationType.PAIR;
                break;
            case 3:
                if (isTriple(sortedCards)) return CombinationType.TRIPLE;
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT; // Dây 3
                break;
            case 4:
                if (isFourOfKind(sortedCards)) return CombinationType.FOUR_OF_KIND;
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT; // Dây 4
                break;
            case 5:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT; // Dây 5
                break;
            case 6:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT; // Dây 6
                if (isThreePairStraight(sortedCards)) return CombinationType.THREE_PAIR_STRAIGHT; // Ba đôi thông
                break;
            case 7:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT; // Dây 7
                break;
            case 8:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT; // Dây 8
                if (isFourPairStraight(sortedCards)) return CombinationType.FOUR_PAIR_STRAIGHT; // Bốn đôi thông
                break;
            case 9:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT; // Dây 9
                if (isThreePairStraight(sortedCards)) return CombinationType.THREE_PAIR_STRAIGHT; // Ba đôi thông (từ 9 lá)
                break;
            case 10:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT; // Dây 10
                break;
            case 11:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT; // Dây 11
                break;
            case 12:
                if (isStraight(sortedCards)) return CombinationType.STRAIGHT; // Dây 12
                if (isFourPairStraight(sortedCards)) return CombinationType.FOUR_PAIR_STRAIGHT; // Bốn đôi thông (từ 12 lá)
                break;
        }

        return CombinationType.INVALID;
    }

    
    public String getCardsDisplay(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return "Không có bài nào";
        }
        // Sử dụng Stream API để chuyển đổi List<Card> thành String
        return cards.stream()
                    .map(Card::toString) // Gọi toString() cho mỗi Card
                    .collect(Collectors.joining(", ")); // Nối các chuỗi bằng ", "
    }
    
    /**
     * Kiểm tra xem tổ hợp bài có hợp lệ về luật (tùy game cụ thể)
     */
    @Override
    public boolean isValidCombination(List<Card> cards) {
        return getCombinationType(cards) != CombinationType.INVALID;
    }

    /**
     * Kiểm tra xem bài mới có thể đè lên bài cũ hay không.
     */
    @Override
    public boolean canPlayAfter(List<Card> newCards, List<Card> previousCards) {
        if (newCards == null || newCards.isEmpty()) {
            return false; // Không thể bỏ lượt khi không có bài cũ trên bàn
        }
        if (previousCards == null || previousCards.isEmpty()) {
            return false; // Phải có bài cũ để đè
        }

        CombinationType newType = getCombinationType(newCards);
        CombinationType prevType = getCombinationType(previousCards);

        if (newType == CombinationType.INVALID || prevType == CombinationType.INVALID) {
            return false; // Một trong hai tổ hợp không hợp lệ
        }

        List<Card> sortedNewCards = new ArrayList<>(newCards);
        sortedNewCards.sort(TIEN_LEN_CARD_COMPARATOR);
        List<Card> sortedPreviousCards = new ArrayList<>(previousCards);
        sortedPreviousCards.sort(TIEN_LEN_CARD_COMPARATOR);

        // Trường hợp đặc biệt: Tứ quý chặt 2, Ba đôi thông chặt 2/ba đôi thông nhỏ hơn, Bốn đôi thông chặt tứ quý/ba đôi thông/bốn đôi thông nhỏ hơn
        // Tứ quý chặt 2
        if (newType == CombinationType.FOUR_OF_KIND && prevType == CombinationType.SINGLE && getTienLenValue(sortedPreviousCards.get(0)) == 15) { // 2 (giá trị 15)
            return true;
        }
        // Ba đôi thông chặt 2 hoặc ba đôi thông nhỏ hơn
        if (newType == CombinationType.THREE_PAIR_STRAIGHT) {
            if (prevType == CombinationType.SINGLE && getTienLenValue(sortedPreviousCards.get(0)) == 15) return true; // Chặt 2
            if (prevType == CombinationType.PAIR && getTienLenValue(sortedPreviousCards.get(0)) == 15) return true; // Chặt đôi 2
            if (prevType == CombinationType.THREE_PAIR_STRAIGHT) {
                // So sánh giá trị bài lớn nhất trong sảnh ba đôi thông
                return getTienLenValue(sortedNewCards.get(sortedNewCards.size() - 1)) > getTienLenValue(sortedPreviousCards.get(sortedPreviousCards.size() - 1));
            }
        }
        // Bốn đôi thông chặt tứ quý hoặc ba/bốn đôi thông nhỏ hơn, chặt 2
        if (newType == CombinationType.FOUR_PAIR_STRAIGHT) {
            if (prevType == CombinationType.SINGLE && getTienLenValue(sortedPreviousCards.get(0)) == 15) return true; // Chặt 2
            if (prevType == CombinationType.PAIR && getTienLenValue(sortedPreviousCards.get(0)) == 15) return true; // Chặt đôi 2
            if (prevType == CombinationType.FOUR_OF_KIND) return true; // Chặt tứ quý
            if (prevType == CombinationType.THREE_PAIR_STRAIGHT) return true; // Chặt ba đôi thông
            if (prevType == CombinationType.FOUR_PAIR_STRAIGHT) {
                // So sánh giá trị bài lớn nhất trong sảnh bốn đôi thông
                return getTienLenValue(sortedNewCards.get(sortedNewCards.size() - 1)) > getTienLenValue(sortedPreviousCards.get(sortedPreviousCards.size() - 1));
            }
        }

        // So sánh các loại bài thông thường
        if (newType != prevType || newCards.size() != previousCards.size()) {
            return false; // Không cùng loại hoặc không cùng số lượng lá bài
        }

        // So sánh giá trị bài lớn nhất trong tổ hợp
        Card newMaxCard = sortedNewCards.get(sortedNewCards.size() - 1);
        Card prevMaxCard = sortedPreviousCards.get(sortedPreviousCards.size() - 1);

        // So sánh theo thứ tự của Tiến Lên (2 là lớn nhất, sau đó đến A, K,...)
        return TIEN_LEN_CARD_COMPARATOR.compare(newMaxCard, prevMaxCard) > 0;
    }

    // --- Các hàm kiểm tra loại tổ hợp bài ---
    private static boolean isPair(List<Card> cards) {
        return cards.size() == 2 && getTienLenValue(cards.get(0)) == getTienLenValue(cards.get(1));
    }

    private static boolean isTriple(List<Card> cards) {
        return cards.size() == 3 && getTienLenValue(cards.get(0)) == getTienLenValue(cards.get(1)) && getTienLenValue(cards.get(1)) == getTienLenValue(cards.get(2));
    }

    private static boolean isFourOfKind(List<Card> cards) {
        return cards.size() == 4 && getTienLenValue(cards.get(0)) == getTienLenValue(cards.get(1)) &&
               getTienLenValue(cards.get(1)) == getTienLenValue(cards.get(2)) &&
               getTienLenValue(cards.get(2)) == getTienLenValue(cards.get(3));
    }

    private static boolean isStraight(List<Card> cards) {
        if (cards.size() < 3) return false; // Sảnh tối thiểu 3 lá

        // Kiểm tra xem có quân 2 trong sảnh không (sảnh không được chứa 2)
        for (Card card : cards) {
            if (getTienLenValue(card) == 15) { // 2 có giá trị 15
                return false;
            }
        }

        // Sắp xếp bài theo giá trị Tiến Lên để kiểm tra sảnh
        List<Card> sortedByTLValue = new ArrayList<>(cards);
        sortedByTLValue.sort(TIEN_LEN_CARD_COMPARATOR); // Dùng comparator của TLMN

        // Kiểm tra liên tiếp (giá trị rank tăng dần 1 đơn vị)
        for (int i = 0; i < sortedByTLValue.size() - 1; i++) {
            if (getTienLenValue(sortedByTLValue.get(i + 1)) - getTienLenValue(sortedByTLValue.get(i)) != 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean isThreePairStraight(List<Card> cards) {
        if (cards.size() != 6 && cards.size() != 9 && cards.size() != 12) return false; // 3, 4, 5 đôi thông
        return isMultiplePairStraight(cards, 3);
    }

    private static boolean isFourPairStraight(List<Card> cards) {
        if (cards.size() != 8 && cards.size() != 10 && cards.size() != 12) return false; // 4, 5, 6 đôi thông (nhưng thường chỉ là 4)
        return isMultiplePairStraight(cards, 4);
    }

    private static boolean isMultiplePairStraight(List<Card> cards, int numPairs) {
        if (cards.size() % 2 != 0 || cards.size() < numPairs * 2) return false;

        // Sắp xếp bài theo thứ tự của Tiến Lên
        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort(TIEN_LEN_CARD_COMPARATOR);

        // Kiểm tra xem mỗi 2 lá có tạo thành một đôi và các đôi đó có tạo thành sảnh không
        List<Integer> pairValues = new ArrayList<>();
        for (int i = 0; i < sortedCards.size(); i += 2) {
            if (i + 1 >= sortedCards.size()) return false; // Lẻ lá
            Card card1 = sortedCards.get(i);
            Card card2 = sortedCards.get(i + 1);

            if (getTienLenValue(card1) != getTienLenValue(card2)) {
                return false; // Không phải đôi
            }
            pairValues.add(getTienLenValue(card1));
        }

        // Kiểm tra xem các giá trị đôi có tạo thành sảnh liên tiếp không
        if (pairValues.size() < numPairs) return false; // Không đủ số đôi

        // Kiểm tra sảnh liên tiếp của các đôi
        for (int i = 0; i < pairValues.size() - 1; i++) {
            if (pairValues.get(i + 1) - pairValues.get(i) != 1) {
                return false;
            }
        }
        
        // Sảnh đôi không được chứa quân 2
        for(Integer val : pairValues) {
            if (val == 15) return false; // 2 có giá trị 15
        }
        
        return true;
    }


    /**
     * Chuyển đổi Rank sang giá trị số trong Tiến Lên Miền Nam.
     * 3=3, 4=4,..., K=13, A=14, 2=15.
     * (Để 2 là cao nhất)
     */
    public static int getTienLenValue(Card card) {
        if (card.getRank() == Card.Rank.TWO) {
            return 15;
        } else if (card.getRank() == Card.Rank.ACE) {
            return 14;
        } else if (card.getRank() == Card.Rank.KING) {
            return 13;
        } else if (card.getRank() == Card.Rank.QUEEN) {
            return 12;
        } else if (card.getRank() == Card.Rank.JACK) {
            return 11;
        } else if (card.getRank() == Card.Rank.TEN) {
            return 10;
        }
        return card.getRank().getValue(); // Đối với 3-9
    }

    /**
     * Comparator riêng cho bài Tiến Lên.
     * Sắp xếp theo giá trị Tiến Lên trước, sau đó đến chất.
     * Ví dụ: 3 Bích < 3 Chuồn; 2 Cơ < 2 Rô; 2 Bích là lớn nhất.
     */
    private static class TienLenCardComparator implements Comparator<Card> {
        @Override
        public int compare(Card card1, Card card2) {
            // So sánh theo giá trị Tiến Lên trước
            int value1 = getTienLenValue(card1);
            int value2 = getTienLenValue(card2);

            if (value1 != value2) {
                return Integer.compare(value1, value2);
            } else {
                // Nếu giá trị bằng nhau, so sánh theo chất (Bích > Cơ > Rô > Tép)
                // Cần đảm bảo hàm getSuitValue trả về giá trị số tương ứng
                return Integer.compare(getSuitValue(card1.getSuit()), getSuitValue(card2.getSuit()));
            }
        }
    }
    
    /**
     * Chuyển đổi chất bài sang giá trị số để so sánh
     * Thứ tự phổ biến trong TLMN: Bích (3) > Cơ (2) > Rô (1) > Tép (1) - Sửa: Tép (0), Rô (1), Cơ (2), Bích (3)
     */
    private static int getSuitValue(Card.Suit suit) {
        return switch (suit) {
            case CLUBS -> 0;    // Tép
            case DIAMONDS -> 1; // Rô
            case HEARTS -> 2;   // Cơ
            case SPADES -> 3;   // Bích
        };
    }


    /**
     * Trả về Comparator để sắp xếp bài theo luật Tiến Lên Miền Nam.
     */
    @Override
    public Comparator<Card> getCardComparator() {
        return TIEN_LEN_CARD_COMPARATOR; // Trả về instance đã tạo sẵn
    }

    /**
     * Kiểm tra xem người chơi có quân bài đặc biệt để đi đầu ván không.
     * Mặc định dùng luật 3 Bích phổ biến.
     */
	@Override
    public boolean hasStartingCard(List<Card> cards) {
        return cards.contains(new Card(Card.Suit.SPADES, Card.Rank.THREE)); // Kiểm tra có 3 Bích
    }
}
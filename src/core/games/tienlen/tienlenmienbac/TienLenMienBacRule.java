package core.games.tienlen.tienlenmienbac;

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.logic.TienLenCombinationLogic;
import core.games.tienlen.logic.TienLenPlayabilityLogic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TienLenMienBacRule implements TienLenVariantRuleSet {

    // Giữ lại Comparator và các hàm getTienLenBacValue của Miền Bắc
    private static final Comparator<Card> TIEN_LEN_MIEN_BAC_CARD_COMPARATOR = new TienLenMienBacCardComparator();
    public static int getTienLenValue(Card card) {
        if (card.getRank() == Card.Rank.TWO) return 15; // Heo (2) là lớn nhất
        if (card.getRank() == Card.Rank.ACE) return 14; // Át
        if (card.getRank() == Card.Rank.KING) return 13; // Già
        // ... (các rank khác tương tự như TLMN) ...
        if (card.getRank() == Card.Rank.QUEEN) return 12;
        if (card.getRank() == Card.Rank.JACK) return 11;
        return card.getRank().getValue(); // Đối với các lá số từ 3 đến 10
    }
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
            int value1 = getTienLenValue(card1);
            int value2 = getTienLenValue(card2);
            if (value1 != value2) {
                return Integer.compare(value1, value2);
            }
            // Nếu cùng giá trị, TLMB thường không so chất để phân định hơn thua khi chặt.
            // Chất chỉ quan trọng khi xét 3 bích hoặc một số luật đặc biệt.
            // Để comparator này hoàn chỉnh, ta vẫn có thể so sánh chất (ví dụ để sắp xếp tay bài)
            return 0;
        }
    }
    

    @Override
    public Comparator<Card> getCardComparator() {
        return TIEN_LEN_MIEN_BAC_CARD_COMPARATOR;
    }

    @Override
    public Object getCombinationIdentifier(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return CombinationType.INVALID;
        List<Card> sortedCards = new ArrayList<>(cards);
        Collections.sort(sortedCards, getCardComparator()); // Sort theo luật Bắc

        // Gọi logic chung để nhận diện cấu trúc cơ bản
        TienLenVariantRuleSet.CombinationType basicType = 
            TienLenCombinationLogic.getCombinationType(sortedCards, this);

        // --- ÁP DỤNG LUẬT RIÊNG CỦA MIỀN BẮC ---
        if (basicType == TienLenVariantRuleSet.CombinationType.STRAIGHT) {
            // Logic chung (isStraightStructure) đã kiểm tra không có 2 và liên tiếp.
            // Ở đây, TienLenMienBacRule phải kiểm tra thêm tính đồng chất.
            if (sortedCards.size() < 3) return CombinationType.INVALID; // Cần thiết cho isMienBacStraight
            Card.Suit firstSuit = sortedCards.get(0).getSuit();
            for (int i = 1; i < sortedCards.size(); i++) {
                if (sortedCards.get(i).getSuit() != firstSuit) {
                    return CombinationType.INVALID; // Không đồng chất -> không phải sảnh TLMB hợp lệ
                }
            }
            // Nếu qua được các kiểm tra -> là sảnh TLMB hợp lệ
            return CombinationType.STRAIGHT; // Vẫn là STRAIGHT, nhưng đã được validate đồng chất
        }
        
        if (basicType == TienLenVariantRuleSet.CombinationType.PAIR) {
            // TienLenCombinationLogic (nếu chưa sửa) xác định đây là đôi về rank.
            // Bây giờ ta kiểm tra thêm màu sắc.
            // Giả định cards.size() == 2 vì basicType là PAIR
            Card card1 = sortedCards.get(0); // Nên dùng sortedCards đã sắp xếp
            Card card2 = sortedCards.get(1);

            // Sử dụng phương thức getCardColor() từ lớp Card
            if (card1.getCardColor() != card2.getCardColor()) {
                return CombinationType.INVALID; // Không phải đôi hợp lệ theo luật này vì khác màu
            }
            // Nếu cùng màu, nó vẫn là CombinationType.PAIR
        }
        
        // Đối với các bộ khác như PAIR, TRIPLE, FOUR_OF_KIND, THREE_PAIR_STRAIGHT, FOUR_PAIR_STRAIGHT
        // TLMB thường không yêu cầu đồng chất, nên kết quả từ TienLenCombinationLogic có thể là cuối cùng.
        // Nếu có luật nào khác, bạn thêm kiểm tra ở đây.
        return basicType;
    }

    @Override
    public boolean isValidCombination(List<Card> cards) {
        return getCombinationIdentifier(cards) != TienLenVariantRuleSet.CombinationType.INVALID;
    }

    @Override
    public boolean canPlayAfter(List<Card> newCards, List<Card> previousCards) {
        // Gọi hàm logic chung
        boolean canPlayGenerally = TienLenPlayabilityLogic.canPlayAfter(newCards, previousCards, this);
        if (!canPlayGenerally) return false;

        // --- ÁP DỤNG CÁC LUẬT CHẶT CHẼ HƠN CỦA MIỀN BẮC NẾU CẦN ---
        TienLenVariantRuleSet.CombinationType newType = (TienLenVariantRuleSet.CombinationType) getCombinationIdentifier(newCards);
        TienLenVariantRuleSet.CombinationType prevType = (TienLenVariantRuleSet.CombinationType) getCombinationIdentifier(previousCards);

        if (newType != prevType) {
            return false; // Phải cùng loại bài (đơn với đơn, đôi với đôi, v.v.)
        }

        // Sắp xếp để dễ lấy lá bài (ví dụ lá đầu tiên cho chất của sảnh/đơn, hoặc màu của đôi)
        List<Card> sortedNewCards = new ArrayList<>(newCards);
        Collections.sort(sortedNewCards, getCardComparator());
        List<Card> sortedPreviousCards = new ArrayList<>(previousCards);
        Collections.sort(sortedPreviousCards, getCardComparator());

        Card newRep = getRepresentativeCardForCombination(sortedNewCards);
        Card prevRep = getRepresentativeCardForCombination(sortedPreviousCards);

        if (newRep == null || prevRep == null) return false; // Không có lá đại diện

        switch (newType) {
            case SINGLE:
                if (newCards.size() != 1 || previousCards.size() != 1) return false; // Đảm bảo là đơn
                // 1. Phải đồng chất (cùng suit)
                if (sortedNewCards.get(0).getSuit() != sortedPreviousCards.get(0).getSuit()) {
                    return false;
                }
                // 2. Phải lớn hơn (đã đồng chất, giờ so sánh rank qua comparator)
                return getCardComparator().compare(newRep, prevRep) > 0;

            case PAIR: // Đôi (đã được xác định là cùng rank và cùng màu bởi getCombinationIdentifier)
                if (newCards.size() != 2 || previousCards.size() != 2) return false;
                // 1. Phải đồng màu (hai đôi phải cùng màu đỏ, hoặc cùng màu đen)
                // Màu của đôi được quyết định bởi màu của các lá bài trong đôi (chúng giống nhau)
                if (sortedNewCards.get(0).getCardColor() != sortedPreviousCards.get(0).getCardColor()) {
                    return false;
                }
                // 2. Phải lớn hơn (so sánh lá đại diện)
                return getCardComparator().compare(newRep, prevRep) > 0;

            case TRIPLE:
                if (newCards.size() != 3 || previousCards.size() != 3) return false;
                // Luật "sám cô đồng chất lẻ x" của bạn rất đặc thù và phức tạp để tổng quát hóa
                // nếu không có quy tắc rõ ràng về việc xác định "chất lẻ" đó.
                // Hiện tại, chúng ta sẽ bỏ qua yêu cầu "đồng chất" cho sám cô khi chặt nhau
                // và chỉ yêu cầu rank cao hơn.
                // NẾU BẠN MUỐN MỘT QUY TẮC ĐƠN GIẢN HƠN CHO "ĐỒNG CHẤT": ví dụ, lá bài lớn nhất
                // của bộ ba mới phải cùng chất với lá bài lớn nhất của bộ ba cũ.
                // if (newRep.getSuit() != prevRep.getSuit()) {
                //     return false;
                // }
                System.out.println("Cảnh báo: Luật 'sám cô đồng chất' phức tạp chưa được triển khai đầy đủ. Hiện tại chỉ so sánh rank cho bộ ba.");
                return getCardComparator().compare(newRep, prevRep) > 0;

            case STRAIGHT: // Sảnh (đã được xác định là đồng chất bởi getCombinationIdentifier)
                // 1. Phải cùng số lá
                if (sortedNewCards.size() != sortedPreviousCards.size()) {
                    return false;
                }
                // 2. Phải đồng chất (hai sảnh phải cùng một suit, ví dụ cùng là sảnh Cơ)
                // Chất của sảnh được quyết định bởi chất của các lá bài trong sảnh (chúng giống nhau)
                if (sortedNewCards.get(0).getSuit() != sortedPreviousCards.get(0).getSuit()) {
                    return false;
                }
                // 3. Phải lớn hơn (so sánh lá đại diện - lá lớn nhất của sảnh)
                return getCardComparator().compare(newRep, prevRep) > 0;
            
            default:
                // Các loại khác không được đề cập (ví dụ FOUR_OF_KIND đánh thường)
                // có thể tuân theo logic chung hơn nếu không có luật đồng chất/đồng màu.
                // Hoặc, nếu chúng cũng phải tuân theo một quy tắc tương tự, bạn cần thêm vào đây.
                // Ví dụ, nếu đánh tứ quý thường (không phải chặt heo), có cần đồng màu/chất không?
                // Theo luật bạn mô tả, có vẻ không.
                // Vậy, nếu là loại khác mà logic chặt đặc biệt không xử lý,
                // chúng ta có thể quay lại so sánh cơ bản của TienLenPlayabilityLogic nếu nó phù hợp.
                // Tuy nhiên, để chặt chẽ, nếu không có luật cụ thể, ta có thể không cho phép.
                // return TienLenPlayabilityLogic.canPlayAfter(newCards, previousCards, this); // Cân nhắc
                return false; // An toàn hơn là không cho phép nếu không có luật rõ ràng
        }
    }
    
    // ... (getRepresentativeCardForCombination, isCardValidInStraight)
    @Override
    public Card getRepresentativeCardForCombination(List<Card> combination) {
        if (combination == null || combination.isEmpty()) return null;
        List<Card> sortedCombination = new ArrayList<>(combination);
        Collections.sort(sortedCombination, TIEN_LEN_MIEN_BAC_CARD_COMPARATOR); 
        return sortedCombination.get(sortedCombination.size() - 1);
    }

    @Override
    public boolean isCardValidInStraight(Card card) {
        return getTienLenValue(card) < getTwoRankValue(); 
    }

    @Override
    public int getCardRankValue(Card card) {
        return getTienLenValue(card);
    }

    @Override
    public int getTwoRankValue() {
        return 15; // Giá trị của quân 2
    }
	@Override
	public boolean hasStartingCard(List<Card> cards) {
		if(cards.contains(new Card(Card.Suit.SPADES, Card.Rank.THREE))) return true;
		else {
		return false;
		}
	}
    
	@Override
	public int getCardsPerPlayer() {
		return 13;
	}
    
	@Override
	public Card startingCard() {
		return new Card(Card.Suit.SPADES, Card.Rank.THREE);
	}   
	
}
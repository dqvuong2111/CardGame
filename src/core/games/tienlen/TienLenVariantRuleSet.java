package core.games.tienlen;

import java.util.Comparator;
import java.util.List;
// Thêm import này nếu bạn quyết định dùng một Enum chung cho CombinationType,
// hoặc mỗi RuleSet sẽ tự định nghĩa Enum riêng và trả về qua một phương thức trừu tượng hơn.
// import core.rules.CombinationType; // Ví dụ

import core.Card;

public interface TienLenVariantRuleSet {
	
	 enum CombinationType {
	        SINGLE, PAIR, TRIPLE, STRAIGHT,
	        FOUR_OF_KIND,
	        THREE_PAIR_STRAIGHT, // Đôi thông 3
	        FOUR_PAIR_STRAIGHT,  // Đôi thông 4
	        INVALID
	    }
	
    boolean isValidCombination(List<Card> cards);
    boolean canPlayAfter(List<Card> newCards, List<Card> previousCards);
    Comparator<Card> getCardComparator();

    /**
     * Kiểm tra xem một lá bài có được phép nằm trong một Sảnh (Straight) hay không.
     * Ví dụ: Trong Tiến Lên, lá 2 không được nằm trong sảnh.
     * @param card Lá bài cần kiểm tra.
     * @return true nếu lá bài được phép, false nếu không.
     */
    boolean isCardValidInStraight(Card card);

    /**
     * Trả về một "định danh" cho loại tổ hợp của một danh sách các lá bài.
     * Định danh này có thể là một Enum, String, hoặc Integer, tùy theo cách bạn muốn triển khai.
     * Nó giúp PlayableMoveGenerator biết được loại bài của lastPlayedCards.
     * Ví dụ: "SINGLE", "PAIR", "STRAIGHT_5", TienLenCombinationType.SINGLE, etc.
     */
    Object getCombinationIdentifier(List<Card> cards); // Hoặc cụ thể hơn: String getCombinationTypeName(List<Card> cards);

    /**
     * Lấy lá bài "đại diện" cho một tổ hợp để so sánh/sắp xếp.
     * Ví dụ: Trong sảnh, đó là lá bài cao nhất. Trong đôi, có thể là lá bài cao hơn về chất.
     * @param combination Danh sách các lá bài tạo thành một tổ hợp.
     * @return Lá bài đại diện.
     */
    Card getRepresentativeCardForCombination(List<Card> combination);

    // Các phương thức liên quan đến luật chơi đặc biệt của lượt đầu tiên (nếu muốn trừu tượng hóa)
    // Ví dụ:
    // boolean mustPlaySpecificCardsOnFirstTurn(List<Card> hand);
    // List<Card> getRequiredCardsForFirstGameTurn(List<Card> hand);
    // boolean isValidPlayForFirstGameTurn(List<Card> play, List<Card> hand);
    
    int getCardRankValue(Card card); // Trả về giá trị số để so sánh rank
    int getTwoRankValue();  
    
    boolean hasStartingCard(List<Card> cards);
    
    int getCardsPerPlayer();
    
    Card startingCard();
    
}
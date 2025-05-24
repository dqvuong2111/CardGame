// File: core/rules/TienLenRule.java
package core.rules;

import core.Card;
import core.RuleSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections; // Thêm Collections để dùng cho getRepresentativeCardForCombination
import java.util.Comparator;

public class TienLenRule implements RuleSet { // Đảm bảo implements RuleSet

	public enum CombinationType {
		SINGLE, PAIR, TRIPLE, STRAIGHT, FOUR_OF_KIND, THREE_PAIR_STRAIGHT, FOUR_PAIR_STRAIGHT, INVALID
	}

	private static final Comparator<Card> TIEN_LEN_CARD_COMPARATOR = new TienLenCardComparator();

	// Phương thức này vẫn là static và public để có thể được gọi từ bên ngoài nếu cần,
	// nhưng getCombinationIdentifier (phương thức của interface) sẽ là phương thức instance gọi nó.
	public static CombinationType getCombinationType(List<Card> cards) {
		if (cards == null || cards.isEmpty()) {
			return CombinationType.INVALID;
		}

		List<Card> sortedCards = new ArrayList<>(cards);
		// Luôn sắp xếp để đảm bảo logic kiểm tra loại tổ hợp là nhất quán
		Collections.sort(sortedCards, TIEN_LEN_CARD_COMPARATOR);


		int size = sortedCards.size();

		// Các trường hợp kiểm tra loại tổ hợp giữ nguyên như bạn đã có
		switch (size) {
		case 1:
			return CombinationType.SINGLE;
		case 2:
			if (isPair(sortedCards)) // isPair sẽ dùng getTienLenValue
				return CombinationType.PAIR;
			break;
		case 3:
			if (isTriple(sortedCards)) // isTriple sẽ dùng getTienLenValue
				return CombinationType.TRIPLE;
			if (isStraight(sortedCards)) // isStraight cũng sẽ dùng getTienLenValue
				return CombinationType.STRAIGHT;
			break;
		case 4:
			if (isFourOfKind(sortedCards)) // isFourOfKind dùng getTienLenValue
				return CombinationType.FOUR_OF_KIND;
			if (isStraight(sortedCards))
				return CombinationType.STRAIGHT;
			break;
		case 5:
			if (isStraight(sortedCards))
				return CombinationType.STRAIGHT;
			break;
		case 6:
			if (isStraight(sortedCards))
				return CombinationType.STRAIGHT;
			if (isThreePairStraight(sortedCards)) // isThreePairStraight dùng getTienLenValue
				return CombinationType.THREE_PAIR_STRAIGHT;
			break;
		case 7:
			if (isStraight(sortedCards))
				return CombinationType.STRAIGHT;
			break;
		case 8:
			if (isStraight(sortedCards))
				return CombinationType.STRAIGHT;
			if (isFourPairStraight(sortedCards)) // isFourPairStraight dùng getTienLenValue
				return CombinationType.FOUR_PAIR_STRAIGHT;
			break;
		// Thêm các case còn lại cho sảnh dài hơn nếu cần
		case 9: // Sảnh hoặc 3 đôi thông (nếu có luật sảnh 9 lá riêng hoặc 3 đôi thông 9 lá)
            if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
            // if (isThreePairStraight(sortedCards)) return CombinationType.THREE_PAIR_STRAIGHT; // 3 đôi thông thường 6 lá
            break;
        case 10:
            if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
            break;
        case 11:
            if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
            break;
        case 12: // Sảnh hoặc 4 đôi thông (nếu có luật sảnh 12 lá riêng hoặc 4 đôi thông 12 lá)
            if (isStraight(sortedCards)) return CombinationType.STRAIGHT;
            // if (isFourPairStraight(sortedCards)) return CombinationType.FOUR_PAIR_STRAIGHT; // 4 đôi thông thường 8 lá
            break;
        // Sảnh rồng 13 lá không có Heo (3->A) là trường hợp đặc biệt, có thể xử lý riêng nếu game có
		}

		return CombinationType.INVALID;
	}

	// getCardsDisplay giữ nguyên
	public String getCardsDisplay(List<Card> cards) {
		if (cards == null || cards.isEmpty()) {
			return "Không có bài nào";
		}
		return cards.stream().map(Card::toString).collect(Collectors.joining(", "));
	}

	@Override
	public boolean isValidCombination(List<Card> cards) {
		return getCombinationType(cards) != CombinationType.INVALID;
	}

	@Override
	public boolean canPlayAfter(List<Card> newCards, List<Card> previousCards) {
		// Logic này của bạn đã khá đầy đủ cho Tiến Lên, bao gồm cả chặt heo, chặt hàng.
		// Đảm bảo nó sử dụng getCombinationType để xác định loại bài.
		if (newCards == null || newCards.isEmpty()) {
			return false;
		}
		// Nếu không có bài trên bàn, chỉ cần newCards là một tổ hợp hợp lệ (trừ trường hợp đặc biệt như 3 bích lượt đầu)
        if (previousCards == null || previousCards.isEmpty()) {
            return isValidCombination(newCards);
        }


		CombinationType newType = getCombinationType(newCards);
		CombinationType prevType = getCombinationType(previousCards);

		if (newType == CombinationType.INVALID) { // Không cần kiểm tra prevType == INVALID ở đây, vì nếu vậy thì newCards (hợp lệ) auto qua
			return false;
		}
        if (prevType == CombinationType.INVALID && newType != CombinationType.INVALID) { // Lỗi ở vòng trước, người chơi mới đánh bộ hợp lệ là được
             return true;
        }


		List<Card> sortedNewCards = new ArrayList<>(newCards);
		Collections.sort(sortedNewCards, TIEN_LEN_CARD_COMPARATOR); // Sắp xếp để lấy lá đại diện
		List<Card> sortedPreviousCards = new ArrayList<>(previousCards);
		Collections.sort(sortedPreviousCards, TIEN_LEN_CARD_COMPARATOR); // Sắp xếp để lấy lá đại diện

		// Luật chặt đặc biệt
		boolean newIsTwo = (newType == CombinationType.SINGLE && getTienLenValue(sortedNewCards.get(0)) == 15);
        boolean prevIsTwo = (prevType == CombinationType.SINGLE && getTienLenValue(sortedPreviousCards.get(0)) == 15);
        boolean prevIsPairOfTwos = (prevType == CombinationType.PAIR && getTienLenValue(sortedPreviousCards.get(0)) == 15);


		// 1. Tứ quý chặt một hoặc đôi Heo
		if (newType == CombinationType.FOUR_OF_KIND) {
			if (prevIsTwo || prevIsPairOfTwos) return true;
            if (prevType == CombinationType.FOUR_OF_KIND) { // Tứ quý chặt tứ quý lớn hơn
                return TIEN_LEN_CARD_COMPARATOR.compare(sortedNewCards.get(0), sortedPreviousCards.get(0)) > 0;
            }
            // Tứ quý không chặt các bộ thường khác (trừ khi có luật riêng)
            // return false; // Tứ quý chỉ dùng để chặt Heo hoặc Tứ quý khác
		}

		// 2. Ba đôi thông chặt một Heo
		if (newType == CombinationType.THREE_PAIR_STRAIGHT) {
			if (prevIsTwo) return true;
            if (prevType == CombinationType.THREE_PAIR_STRAIGHT) { // 3 đôi thông chặt 3 đôi thông lớn hơn
                 return TIEN_LEN_CARD_COMPARATOR.compare(
                    getRepresentativeCardForCombination(sortedNewCards), // So sánh lá cao nhất của đôi cuối
                    getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            // Ba đôi thông không chặt các bộ thường khác (trừ khi có luật riêng)
           // return false;
		}

		// 3. Bốn đôi thông chặt một Heo, đôi Heo, Tứ quý, Ba đôi thông
		if (newType == CombinationType.FOUR_PAIR_STRAIGHT) {
			if (prevIsTwo || prevIsPairOfTwos || prevType == CombinationType.FOUR_OF_KIND || prevType == CombinationType.THREE_PAIR_STRAIGHT) {
				return true;
			}
             if (prevType == CombinationType.FOUR_PAIR_STRAIGHT) { // 4 đôi thông chặt 4 đôi thông lớn hơn
                return TIEN_LEN_CARD_COMPARATOR.compare(
                    getRepresentativeCardForCombination(sortedNewCards),
                    getRepresentativeCardForCombination(sortedPreviousCards)
                ) > 0;
            }
            // Bốn đôi thông không chặt các bộ thường khác (trừ khi có luật riêng)
            // return false;
		}

        // Nếu newCards là bộ chặt đặc biệt nhưng previousCards không phải là thứ bị chặt, thì không hợp lệ (trừ khi là cùng loại)
        if ((newType == CombinationType.FOUR_OF_KIND && prevType != CombinationType.SINGLE && prevType != CombinationType.PAIR && prevType != CombinationType.FOUR_OF_KIND) || // Heo đơn, heo đôi
            (newType == CombinationType.THREE_PAIR_STRAIGHT && prevType != CombinationType.SINGLE && prevType != CombinationType.THREE_PAIR_STRAIGHT) || // Heo đơn
            (newType == CombinationType.FOUR_PAIR_STRAIGHT && prevType != CombinationType.SINGLE && prevType != CombinationType.PAIR && prevType != CombinationType.FOUR_OF_KIND && prevType != CombinationType.THREE_PAIR_STRAIGHT && prevType != CombinationType.FOUR_PAIR_STRAIGHT)
        ) {
            if(newType != prevType) return false; // Nếu không phải cùng loại và không phải chặt đặc biệt thì sai
        }


		// Các trường hợp đánh thường: cùng loại, cùng số lượng (trừ sảnh có thể khác số lượng nếu luật cho phép)
		if (newType != prevType) { // Nếu không cùng loại và không phải trường hợp chặt đặc biệt ở trên
			return false;
		}
		if (newCards.size() != previousCards.size() &&
            !(newType == CombinationType.STRAIGHT && prevType == CombinationType.STRAIGHT)) { // Sảnh có thể khác độ dài nếu luật cho phép (ít phổ biến)
                                                                                             // Luật TLMN thường yêu cầu sảnh cùng độ dài.
             if (newType == CombinationType.STRAIGHT && prevType == CombinationType.STRAIGHT) {
                 // Theo luật phổ thông, sảnh phải cùng độ dài mới được so sánh trực tiếp.
                 // Nếu muốn cho phép sảnh dài hơn đè sảnh ngắn hơn (không phổ biến), thì bỏ điều kiện size.
                 return false; // Mặc định sảnh phải cùng độ dài
             } else if (newType != CombinationType.STRAIGHT) { // Các bộ khác phải cùng size
                 return false;
             }
		}

		// So sánh lá bài đại diện (thường là lá lớn nhất của bộ)
		Card newMaxCard = getRepresentativeCardForCombination(sortedNewCards);
		Card prevMaxCard = getRepresentativeCardForCombination(sortedPreviousCards);

		return TIEN_LEN_CARD_COMPARATOR.compare(newMaxCard, prevMaxCard) > 0;
	}

	// --- Các phương thức private static helper giữ nguyên ---
	private static boolean isPair(List<Card> cards) {
		// Đã sort ở getCombinationType
		return cards.size() == 2 && getTienLenValue(cards.get(0)) == getTienLenValue(cards.get(1));
	}

	private static boolean isTriple(List<Card> cards) {
		// Đã sort
		return cards.size() == 3 && getTienLenValue(cards.get(0)) == getTienLenValue(cards.get(1))
				&& getTienLenValue(cards.get(1)) == getTienLenValue(cards.get(2));
	}

	private static boolean isFourOfKind(List<Card> cards) {
		// Đã sort
		return cards.size() == 4 && getTienLenValue(cards.get(0)) == getTienLenValue(cards.get(1))
				&& getTienLenValue(cards.get(1)) == getTienLenValue(cards.get(2))
				&& getTienLenValue(cards.get(2)) == getTienLenValue(cards.get(3));
	}

	private static boolean isStraight(List<Card> cards) {
		// Đã sort ở getCombinationType
		if (cards.size() < 3)
			return false;

		for (Card card : cards) {
			if (getTienLenValue(card) == 15) { // Heo không được trong sảnh
				return false;
			}
		}
		// sortedByTLValue đã được sort bằng TIEN_LEN_CARD_COMPARATOR ở getCombinationType
		for (int i = 0; i < cards.size() - 1; i++) {
			if (getTienLenValue(cards.get(i + 1)) - getTienLenValue(cards.get(i)) != 1) {
				return false;
			}
		}
		return true;
	}

	private static boolean isThreePairStraight(List<Card> cards) {
		// Đã sort
		if (cards.size() != 6)
			return false;
		return isMultiplePairStraight(cards, 3);
	}

	private static boolean isFourPairStraight(List<Card> cards) {
		// Đã sort
		if (cards.size() != 8)
			return false;
		return isMultiplePairStraight(cards, 4);
	}

	private static boolean isMultiplePairStraight(List<Card> cards, int numPairs) {
		// cards đã được sort ở getCombinationType
		if (cards.size() != numPairs * 2) // Đảm bảo đúng số lượng lá cho số đôi yêu cầu
            return false;

		List<Integer> pairValues = new ArrayList<>();
		for (int i = 0; i < cards.size(); i += 2) {
			// Kiểm tra từng cặp có phải là đôi không
			if (getTienLenValue(cards.get(i)) != getTienLenValue(cards.get(i + 1))) {
				return false; // Không phải đôi
			}
			// Kiểm tra lá bài trong đôi có phải là Heo không
            if (getTienLenValue(cards.get(i)) == 15) {
                return false; // Đôi thông không chứa Heo
            }
			pairValues.add(getTienLenValue(cards.get(i)));
		}

		// Kiểm tra các giá trị của đôi có liên tiếp không
		for (int i = 0; i < pairValues.size() - 1; i++) {
			if (pairValues.get(i + 1) - pairValues.get(i) != 1) {
				return false; // Các đôi không liên tiếp
			}
		}
		return true;
	}

	// getTienLenValue giữ nguyên, là public static để các nơi khác có thể dùng nếu cần
	public static int getTienLenValue(Card card) {
		if (card.getRank() == Card.Rank.TWO) {
			return 15; // Heo
		} else if (card.getRank() == Card.Rank.ACE) {
			return 14; // Xì
		} else if (card.getRank() == Card.Rank.KING) {
			return 13; // Già
		} else if (card.getRank() == Card.Rank.QUEEN) {
			return 12; // Đầm
		} else if (card.getRank() == Card.Rank.JACK) {
			return 11; // Bồi
		}
		// TEN (10) đến THREE (3) giữ nguyên giá trị rank
		return card.getRank().getValue();
	}

	// TienLenCardComparator và getSuitValue giữ nguyên là private static class và method
	private static class TienLenCardComparator implements Comparator<Card> {
		@Override
		public int compare(Card card1, Card card2) {
			int value1 = getTienLenValue(card1);
			int value2 = getTienLenValue(card2);

			if (value1 != value2) {
				return Integer.compare(value1, value2);
			} else {
				// Nếu cùng giá trị, so sánh chất (Suit)
				return Integer.compare(getSuitOrder(card1.getSuit()), getSuitOrder(card2.getSuit()));
			}
		}
	}

	// Đổi tên getSuitValue thành getSuitOrder để rõ ràng hơn là thứ tự ưu tiên
	private static int getSuitOrder(Card.Suit suit) {
		return switch (suit) {
		case SPADES -> 0; // Bích (nhỏ nhất về chất khi cùng giá trị)
		case CLUBS -> 1;  // Chuồn (Tép)
		case DIAMONDS -> 2;// Rô
		case HEARTS -> 3;  // Cơ (lớn nhất về chất khi cùng giá trị)
		};
	}

	@Override
	public Comparator<Card> getCardComparator() {
		return TIEN_LEN_CARD_COMPARATOR;
	}

	public boolean hasStartingCard(List<Card> cards) {
		// Luật Tiến Lên Miền Nam: người có 3 Bích đi trước trong ván đầu
		return cards.contains(new Card(Card.Suit.SPADES, Card.Rank.THREE));
	}

	// --- Implement các phương thức mới của RuleSet ---

	@Override
	public boolean isCardValidInStraight(Card card) {
		// Trong Tiến Lên, lá 2 (Heo) không được nằm trong sảnh thường
		return getTienLenValue(card) < 15; // nhỏ hơn 15 (giá trị của Heo)
	}

	@Override
	public Object getCombinationIdentifier(List<Card> cards) {
		// Trả về enum CombinationType của TienLenRule
		return getCombinationType(cards);
	}

	@Override
	public Card getRepresentativeCardForCombination(List<Card> combination) {
		if (combination == null || combination.isEmpty()) {
			// Hoặc throw IllegalArgumentException, hoặc trả về null và để nơi gọi xử lý
			return null;
		}
		// Đối với Tiến Lên, lá bài đại diện thường là lá cao nhất trong tổ hợp đó.
		// Vì TIEN_LEN_CARD_COMPARATOR đã sắp xếp đúng, chỉ cần lấy lá cuối.
		List<Card> sortedCombination = new ArrayList<>(combination);
		Collections.sort(sortedCombination, TIEN_LEN_CARD_COMPARATOR);
		return sortedCombination.get(sortedCombination.size() - 1);
	}
}
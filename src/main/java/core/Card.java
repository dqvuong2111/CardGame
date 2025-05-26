package core;

public class Card implements Comparable<Card> {
	public enum Suit {
		CLUBS,    // Tép (nhỏ nhất)
        DIAMONDS, // Rô
        HEARTS,   // Cơ
        SPADES    // Bích (lớn nhất)
	}
	
	public enum Rank {
		THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8),
        NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13), ACE(14), TWO(2); 
	
		private final int value;
		Rank(int value){
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	public enum CardColor {
        RED, BLACK
    }
	
	private final Suit suit;
	private final Rank rank;
	
	public Card (Suit suit, Rank rank) {
		this.suit = suit;
		this.rank = rank;
	}

	public Suit getSuit() {
		return suit;
	}

	public Rank getRank() {
		return rank;
	}
	
	public CardColor getCardColor() {
        if (this.suit == Suit.HEARTS || this.suit == Suit.DIAMONDS) {
            return CardColor.RED;
        } else {
            return CardColor.BLACK;
        }
    }

	@Override
	public String toString() {
		return rankToString() + suitToString();
	}
	
	public String suitToString() {
		return switch (suit) {
        case HEARTS -> "♥";
        case DIAMONDS -> "♦";
        case CLUBS -> "♣";
        case SPADES -> "♠";
		};
	}
	
	public String rankToString() {
		return switch(rank) {
		case JACK -> "J";
		case QUEEN -> "Q";
		case KING -> "K";
		case ACE -> "A";
		case TWO -> "2";
		default -> String.valueOf(rank.getValue());
		};
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return suit == card.suit && rank == card.rank;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(suit, rank);
    }

	 @Override
	    public int compareTo(Card other) {
	        // So sánh mặc định (dùng cho Player.sortHand nếu không có RuleSet cụ thể)
	        // Thông thường: Giá trị trước, sau đó chất
	        if (this.rank.getValue() != other.rank.getValue()) {
	            return Integer.compare(this.rank.getValue(), other.rank.getValue());
	        } else {
	            // Thứ tự chất mặc định (tùy game có thể thay đổi)
	            // Ví dụ: Spades > Hearts > Diamonds > Clubs
	            return Integer.compare(this.suit.ordinal(), other.suit.ordinal()); 
	        }
	    }
}
package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
    }

    private void initializeDeck() {
        // Tạo bộ bài 52 lá
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public Card drawCard() {
        if (!isEmpty()) {
            return cards.remove(0); // Rút từ đầu danh sách
        }
        return null;
    }

    public List<Card> drawCards(int count) {
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < count && !isEmpty(); i++) {
            drawn.add(drawCard());
        }
        return drawn;
    }

    public int size() {
        return cards.size();
    }

    public void reset() {
        cards.clear();
        initializeDeck();
        shuffle(); // Xáo trộn sau khi reset
    }
}
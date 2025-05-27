package core.games.tienlen.tienlenmienbac; // Hoặc package của bạn

import core.Card;
import core.Deck;
import core.games.tienlen.AbstractTienLenGame;
import core.games.tienlen.tienlenplayer.TienLenPlayer;
import java.util.List;

public class TienLenMienBacGame extends AbstractTienLenGame<TienLenMienBacRule> {

    public TienLenMienBacGame(List<TienLenPlayer> players, TienLenMienBacRule ruleSet) {
        super("Tiến Lên Miền Bắc", players, new Deck(), ruleSet, 1L); // aiDelay là 1 giây
    }

    @Override
    protected void findStartingPlayerOfGameVariant() {
        // Logic tìm người có 3 Bích (giống TLMN hoặc có thể có biến thể nhỏ)
        List<TienLenPlayer> currentPlayers = getPlayers();
        Card threeSpadesCard = new Card(Card.Suit.SPADES, Card.Rank.THREE);
        int starterIdx = -1;
        for (int i = 0; i < currentPlayers.size(); i++) {
            if (currentPlayers.get(i).getHand().contains(threeSpadesCard)) {
                starterIdx = i;
                break;
            }
        }
        if (starterIdx == -1) {
            starterIdx = 0; 
            notifyMessage("Không tìm thấy 3 Bích. " + currentPlayers.get(starterIdx).getName() + " sẽ đi đầu.");
        } else {
            notifyMessage(currentPlayers.get(starterIdx).getName() + " có 3 Bích. Họ sẽ đi đầu!");
        }
        setCurrentPlayerByIndex(starterIdx);
        setPlayerWhoPlayedLastValidCards(currentPlayers.get(starterIdx));
    }

    // Bạn có thể override các phương thức khác từ AbstractTienLenGame nếu TLMB có logic vòng đời game khác biệt.
}
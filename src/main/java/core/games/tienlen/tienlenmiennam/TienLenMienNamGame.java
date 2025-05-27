package core.games.tienlen.tienlenmiennam; 

import core.Card;
import core.Deck;
import core.games.tienlen.AbstractTienLenGame;
import core.games.tienlen.tienlenplayer.TienLenPlayer;

import java.util.List;

public class TienLenMienNamGame extends AbstractTienLenGame<TienLenMienNamRule> {

    public TienLenMienNamGame(List<TienLenPlayer> players, TienLenMienNamRule ruleSet) {
        super("Tiến Lên Miền Nam", players, new Deck(), ruleSet, 1L); 
    }

    @Override
    protected void findStartingPlayerOfGameVariant() {
        // Logic tìm người có 3 Bích
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
}
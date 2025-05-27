package core.games.samloc;

import core.Deck;
import core.games.tienlen.AbstractTienLenGame;
import core.games.tienlen.tienlenplayer.TienLenPlayer;
import java.util.List;
import java.util.Random;

public class SamLocGame extends AbstractTienLenGame<SamLocRule> {

    public SamLocGame(List<TienLenPlayer> players, SamLocRule ruleSet) {
        super("Sâm Lốc", players, new Deck(), ruleSet, 1L); 
    }

    @Override
    protected void findStartingPlayerOfGameVariant() {
        // Chọn người bắt đầu random
        List<TienLenPlayer> currentPlayers = getPlayers();
        Random random = new Random();
        int starterIdx = random.nextInt(currentPlayers.size());
        setCurrentPlayerByIndex(starterIdx);
        setPlayerWhoPlayedLastValidCards(currentPlayers.get(starterIdx));
    }
}
package core.games.tienlen.samloc; // Hoặc package của bạn

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
        // Logic tìm người có 3 Bích (giống TLMN hoặc có thể có biến thể nhỏ)
        List<TienLenPlayer> currentPlayers = getPlayers();
        Random random = new Random();
        int starterIdx = random.nextInt(currentPlayers.size());
        setCurrentPlayerByIndex(starterIdx);
        setPlayerWhoPlayedLastValidCards(currentPlayers.get(starterIdx));
    }

    // Bạn có thể override các phương thức khác từ AbstractTienLenGame nếu TLMB có logic vòng đời game khác biệt.
}
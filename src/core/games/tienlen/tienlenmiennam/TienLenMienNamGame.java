// File: core/games/tienlen/TienLenMienNamGame.java
package core.games.tienlen.tienlenmiennam; // Package đúng

import core.Card;
import core.Deck;
import core.games.tienlen.AbstractTienLenGame;
// Import TienLenMienNamRule từ package của nó
import core.games.tienlen.tienlenplayer.TienLenPlayer;

import java.util.List;

public class TienLenMienNamGame extends AbstractTienLenGame<TienLenMienNamRule> {

    public TienLenMienNamGame(List<TienLenPlayer> players, TienLenMienNamRule ruleSet) {
        super("Tiến Lên Miền Nam", players, new Deck(), ruleSet, 1L); // aiDelay là 1 giây
    }

    @Override
    protected void findStartingPlayerOfGameVariant() {
        // Logic tìm người có 3 Bích đặc thù của Miền Nam
        // RoundManager có thể đã có logic này, hoặc bạn implement ở đây và gọi qua context
        // Giữ lại logic từ TienLenGame cũ:
        List<TienLenPlayer> currentPlayers = getPlayers(); // Lấy từ context (super)
        Card threeSpadesCard = new Card(Card.Suit.SPADES, Card.Rank.THREE);
        int starterIdx = -1;
        for (int i = 0; i < currentPlayers.size(); i++) {
            if (currentPlayers.get(i).getHand().contains(threeSpadesCard)) {
                starterIdx = i;
                break;
            }
        }
        if (starterIdx == -1) { // Không tìm thấy 3 bích
            starterIdx = 0; // Người đầu tiên trong danh sách đi trước
            notifyMessage("Không tìm thấy 3 Bích. " + currentPlayers.get(starterIdx).getName() + " sẽ đi đầu.");
        } else {
            notifyMessage(currentPlayers.get(starterIdx).getName() + " có 3 Bích. Họ sẽ đi đầu!");
        }
        setCurrentPlayerByIndex(starterIdx); // Set người chơi hiện tại
        setPlayerWhoPlayedLastValidCards(currentPlayers.get(starterIdx)); // Người này cũng là người đánh hợp lệ đầu tiên
    }

    // Các override khác nếu Miền Nam có hành vi đặc biệt
}
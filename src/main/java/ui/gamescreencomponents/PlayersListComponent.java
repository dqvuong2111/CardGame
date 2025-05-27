package ui.gamescreencomponents;

import core.Card;
import core.games.tienlenplayer.TienLenPlayer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayersListComponent extends VBox {
    private Map<TienLenPlayer, PlayerInfoComponent> playerInfoComponents;

    public PlayersListComponent(List<TienLenPlayer> initialPlayers) {
        this.playerInfoComponents = new HashMap<>();
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: transparent;");
        this.setPrefWidth(210);
        initialPlayers.sort((p1, p2) -> {
            if (!p1.isAI() && p2.isAI()) return -1;
            if (p1.isAI() && !p2.isAI()) return 1;
            return p1.getName().compareTo(p2.getName());
        });

        for (TienLenPlayer player : initialPlayers) {
            PlayerInfoComponent pic = new PlayerInfoComponent(player);
            playerInfoComponents.put(player, pic);
            this.getChildren().add(pic);
        }
    }

    public void updatePlayers(List<TienLenPlayer> players, TienLenPlayer currentPlayer, boolean isGameOver, Comparator<Card> cardComparator) {
        for (TienLenPlayer player : players) {
            PlayerInfoComponent pic = playerInfoComponents.get(player);
            if (pic != null) {
                pic.updateData(player, player == currentPlayer, isGameOver);
            } else {
                System.err.println("PlayersListComponent: Không tìm thấy component cho player " + player.getName());
            }
        }
    }
}
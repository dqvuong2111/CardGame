// File: ui/JavaFX/gamescreencomponents/PlayersListComponent.java
package ui.JavaFX.gamescreencomponents;

import core.Card;
import core.games.tienlen.tienlenplayer.TienLenPlayer;
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
        this.setSpacing(10); // Khoảng cách giữa các player panel
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: transparent;"); // Nền trong suốt
        this.setPrefWidth(210);
        // Sắp xếp người chơi (ví dụ: human trước, rồi đến AI)
        // Logic sắp xếp này có thể cần được truyền vào hoặc chuẩn hóa
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
        // Có thể cần cập nhật lại danh sách children nếu số người chơi thay đổi (hiếm khi xảy ra giữa game)
        // Hiện tại, chỉ cập nhật data cho các component đã có
        for (TienLenPlayer player : players) {
            PlayerInfoComponent pic = playerInfoComponents.get(player);
            if (pic != null) {
                pic.updateData(player, player == currentPlayer, isGameOver);
            } else {
                // Xử lý trường hợp player mới được thêm vào game (nếu có)
                // PlayerInfoComponent newPic = new PlayerInfoComponent(player);
                // playerInfoComponents.put(player, newPic);
                // this.getChildren().add(newPic); // Cần logic sắp xếp lại vị trí
                // newPic.updateData(player, player == currentPlayer, isGameOver);
                System.err.println("PlayersListComponent: Không tìm thấy component cho player " + player.getName());
            }
        }
    }
}
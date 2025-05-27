package core.games.tienlen.tienlenplayer;

import core.Player;

public class TienLenPlayer extends Player {
    protected boolean hasNoCards;   // Thêm trạng thái này
    protected int winnerRank;       // 1 là nhất, 2 là nhì, ...

    public TienLenPlayer(String name, boolean isAI) {
        super(name, isAI);
        this.hasNoCards = false;    // Khởi tạo ban đầu là chưa hết bài
        this.winnerRank = 0;        // 0 nghĩa là chưa có hạng
    }

    public boolean hasNoCards() {
        return hand.isEmpty() || hasNoCards;    // Hết bài hoặc đã đánh dấu là hết bài
    }

    public void setHasNoCards(boolean hasNoCards) {
        this.hasNoCards = hasNoCards;
    }

    public int getWinnerRank() {
        return winnerRank;
    }

    public void setWinnerRank(int winnerRank) {
        this.winnerRank = winnerRank;
    }

    public void clearWinnerRank() {
        this.winnerRank = 0;    // Đặt lại thứ hạng về 0
    }

}
// File: core/games/tienlen/TienLenVariantRuleSet.java
package core.games.tienlen;

import core.RuleSet;
// Không cần import Card hay các thứ khác nếu nó chỉ kế thừa RuleSet

public interface TienLenVariantRuleSet extends RuleSet {
    // Hiện tại có thể để trống.
    // Trong tương lai, nếu các biến thể Tiến Lên có những khái niệm luật chung
    // mà không có trong RuleSet cơ bản, bạn có thể thêm vào đây.
    // Ví dụ: getSpecificTienLenCombinationValue(List<Card> cards);
    // hoặc các phương thức liên quan đến "thối", "cóng" nếu chúng có cấu trúc chung.
}
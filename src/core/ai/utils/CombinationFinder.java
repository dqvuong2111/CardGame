// File: core/ai/utils/CombinationFinder.java
package core.ai.utils;

import core.Card;
import core.games.tienlen.TienLenVariantRuleSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CombinationFinder {

    public static List<List<Card>> findAllSingles(List<Card> hand, TienLenVariantRuleSet ruleSet) {
        if (hand == null) return new ArrayList<>();
        return hand.stream().map(List::of).collect(Collectors.toList());
    }

    public static List<List<Card>> findAllPairs(List<Card> hand, TienLenVariantRuleSet ruleSet) {
        if (hand == null || hand.size() < 2) return new ArrayList<>();
        List<List<Card>> pairs = new ArrayList<>();
        List<Card> sortedHand = new ArrayList<>(hand);
        sortedHand.sort(ruleSet.getCardComparator());

        for (int i = 0; i < sortedHand.size() - 1; i++) {
            for (int j = i + 1; j < sortedHand.size(); j++) {
                if (sortedHand.get(i).getRank() == sortedHand.get(j).getRank()) {
                    List<Card> pair = new ArrayList<>();
                    pair.add(sortedHand.get(i));
                    pair.add(sortedHand.get(j));
                    // Đảm bảo rằng tổ hợp này là "hợp lệ" theo định nghĩa cơ bản của RuleSet (nếu cần)
                    // tuy nhiên, isPair thường là một khái niệm chung.
                    // RuleSet.isValidCombination(pair) có thể được gọi ở đây nếu muốn chặt chẽ.
                    pairs.add(pair);
                }
            }
        }
        // Loại bỏ các đôi trùng lặp hoặc chỉ giữ lại các đôi "đẹp nhất" nếu RuleSet có định nghĩa
        // Hiện tại, trả về tất cả các cặp 2 lá cùng rank.
        // Để tránh trùng lặp (ví dụ 3S-3C và 3C-3S), cần logic phức tạp hơn hoặc đảm bảo sortedHand.
        // Hoặc, chỉ tìm các cặp không chồng chéo theo rank như logic cũ:
        List<List<Card>> distinctRankPairs = new ArrayList<>();
        List<Card.Rank> addedRanks = new ArrayList<>();
        List<Card> tempSortedHand = new ArrayList<>(hand); // Sắp xếp lại để lấy đôi mạnh nhất theo suit nếu cần
        tempSortedHand.sort(ruleSet.getCardComparator());

        for (int i = 0; i < tempSortedHand.size() - 1; i++) {
            if (tempSortedHand.get(i).getRank() == tempSortedHand.get(i + 1).getRank()) {
                if (!addedRanks.contains(tempSortedHand.get(i).getRank())) {
                    List<Card> pair = new ArrayList<>();
                    // Lấy 2 lá bất kỳ cùng rank, hoặc 2 lá mạnh nhất theo comparator
                    // Để đơn giản, lấy 2 lá kề nhau sau khi sort
                    pair.add(tempSortedHand.get(i));
                    pair.add(tempSortedHand.get(i+1));
                    distinctRankPairs.add(pair);
                    addedRanks.add(tempSortedHand.get(i).getRank());
                }
                i++; // Bỏ qua lá đã xử lý
            }
        }
        return distinctRankPairs;
    }

    public static List<List<Card>> findAllTriples(List<Card> hand, TienLenVariantRuleSet ruleSet) {
        if (hand == null || hand.size() < 3) return new ArrayList<>();
        List<List<Card>> triples = new ArrayList<>();
        List<Card> sortedHand = new ArrayList<>(hand);
        sortedHand.sort(ruleSet.getCardComparator());

        for (int i = 0; i < sortedHand.size() - 2; i++) {
            if (sortedHand.get(i).getRank() == sortedHand.get(i + 1).getRank() &&
                sortedHand.get(i).getRank() == sortedHand.get(i + 2).getRank()) {
                List<Card> triple = new ArrayList<>();
                triple.add(sortedHand.get(i));
                triple.add(sortedHand.get(i + 1));
                triple.add(sortedHand.get(i + 2));
                triples.add(triple);
                // Để tìm tất cả các bộ ba, không nên i+=2 ở đây nếu có nhiều hơn 3 lá cùng rank
                // Nhưng nếu chỉ muốn các bộ ba không chồng chéo rank, i+=2 là đúng
                i += 2;
            }
        }
        return triples;
    }

    public static List<List<Card>> findAllFourOfAKind(List<Card> hand, TienLenVariantRuleSet ruleSet) {
        if (hand == null || hand.size() < 4) return new ArrayList<>();
        List<List<Card>> quads = new ArrayList<>();
        List<Card> sortedHand = new ArrayList<>(hand);
        sortedHand.sort(ruleSet.getCardComparator());

        for (int i = 0; i < sortedHand.size() - 3; i++) {
            if (sortedHand.get(i).getRank() == sortedHand.get(i + 1).getRank() &&
                sortedHand.get(i).getRank() == sortedHand.get(i + 2).getRank() &&
                sortedHand.get(i).getRank() == sortedHand.get(i + 3).getRank()) {
                List<Card> quad = new ArrayList<>();
                quad.add(sortedHand.get(i));
                quad.add(sortedHand.get(i + 1));
                quad.add(sortedHand.get(i + 2));
                quad.add(sortedHand.get(i + 3));
                quads.add(quad);
                i += 3;
            }
        }
        return quads;
    }

    public static List<List<Card>> findAllStraights(List<Card> hand, TienLenVariantRuleSet ruleSet, int minLength) {
        if (hand == null || hand.size() < minLength || minLength < 3) { // Sảnh thường có độ dài tối thiểu
            return new ArrayList<>();
        }
        List<List<Card>> allStraights = new ArrayList<>();
        List<Card> validCardsForStraight = hand.stream()
                                             .filter(ruleSet::isCardValidInStraight)
                                             .distinct() // Loại bỏ lá trùng nếu có (dựa trên equals của Card)
                                             .sorted(ruleSet.getCardComparator())
                                             .collect(Collectors.toList());

        if (validCardsForStraight.size() < minLength) {
            return allStraights;
        }

        // Duyệt qua tất cả các độ dài sảnh có thể
        for (int length = minLength; length <= validCardsForStraight.size(); length++) {
            // Duyệt qua tất cả các điểm bắt đầu có thể của sảnh
            for (int i = 0; i <= validCardsForStraight.size() - length; i++) {
                List<Card> potentialStraight = new ArrayList<>();
                for (int j = 0; j < length; j++) {
                    potentialStraight.add(validCardsForStraight.get(i + j));
                }

                // Kiểm tra tính liên tục bằng cách dùng comparator.
                // Giả định comparator đã xử lý đúng giá trị của các lá bài cho việc tạo sảnh.
                boolean isSequence = true;
                for (int k = 0; k < potentialStraight.size() - 1; k++) {
                    Card c1 = potentialStraight.get(k);
                    Card c2 = potentialStraight.get(k+1);
                    // Cần một cách để kiểm tra "liền kề về rank cho sảnh" từ RuleSet
                    // Vì getCardComparator() chỉ so sánh hơn kém, không đảm bảo liền kề rank theo kiểu 3-4-5.
                    // Đây là một điểm phức tạp khi tổng quát hóa hoàn toàn.
                    // Tạm thời giả định getRank().getValue() là đủ để kiểm tra liền kề cho các game đơn giản,
                    // nhưng RuleSet nên cung cấp một hàm isConsecutiveForStraight(card1, card2).
                    // Đối với TienLen, bạn có thể dùng TienLenRule.getTienLenValue()
                    if (c2.getRank().getValue() - c1.getRank().getValue() != 1) { // Đây là giả định đơn giản!
                        isSequence = false;
                        break;
                    }
                }

                if (isSequence && ruleSet.isValidCombination(potentialStraight)) { // RuleSet xác nhận đây là sảnh hợp lệ
                    allStraights.add(new ArrayList<>(potentialStraight));
                }
            }
        }
        return allStraights;
    }
}
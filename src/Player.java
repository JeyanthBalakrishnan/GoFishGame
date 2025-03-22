import java.util.*;

public class Player {
    private String name;
    private List<Card> hand;
    private Map<String, Integer> cardCounts;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.cardCounts = new HashMap<>();
    }

    public void addCard(Card card) {
        if (card != null) {
            hand.add(card);
            cardCounts.put(card.getRank(), cardCounts.getOrDefault(card.getRank(), 0) + 1);
        }
    }

    public boolean hasCard(String rank) {
        for (Card card : hand) {
            if (card.getRank().equals(rank)) {
                return true;
            }
        }
        return false;
    }

    public List<Card> giveCards(String rank) {
        List<Card> givenCards = new ArrayList<>();
        hand.removeIf(card -> {
            if (card.getRank().equals(rank)) {
                givenCards.add(card);
                cardCounts.put(card.getRank(), cardCounts.get(card.getRank()) - 1);
                if (cardCounts.get(card.getRank()) == 0) {
                    cardCounts.remove(card.getRank());
                }
                return true;
            }
            return false;
        });
        return givenCards;
    }

    public void removeCards(String rank) {
        hand.removeIf(card -> card.getRank().equals(rank));
        cardCounts.put(rank, cardCounts.get(rank) - 1);
        if (cardCounts.get(rank) == 0) {
            cardCounts.remove(rank);
        }
    }

    public List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    public String getName() {
        return name;
    }

    public void showHand() {
        System.out.println(name + "'s hand: " + hand);
    }

    public List<String> getCompletedSets() {
        List<String> completedSets = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cardCounts.entrySet()) {
            if (entry.getValue() == 4) {
                completedSets.add(entry.getKey());
            }
        }
        return completedSets;
    }
}

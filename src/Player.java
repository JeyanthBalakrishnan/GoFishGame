import java.util.*;

public class Player {
    private String name;
    private List<Card> hand;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public void addCard(Card card) {
        if (card != null) {
            hand.add(card);
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
                return true;
            }
            return false;
        });
        return givenCards;
    }

    public void removeCards(String rank) {
        hand.removeIf(card -> card.getRank().equals(rank));
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
}

import java.util.*;

public class Deck {
    private List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};

        for (String suit : suits) {
            for (String rank : ranks) {
                cards.add(new Card(rank, suit));
            }
        }

        Collections.shuffle(cards);
    }

    public Card drawCard() {
        return cards.isEmpty() ? null : cards.remove(0);
    }

    public int size() {
        return cards.size();
    }
}

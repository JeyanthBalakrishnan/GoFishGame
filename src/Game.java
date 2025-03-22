import java.util.*;

public class Game {
    private List<Player> players;
    private Deck deck;
    private int currentPlayerIndex;
    private boolean gameEnded;
    private Map<Player, Integer> scores;
    private boolean[] isHuman;
    private Scanner scanner;
    private boolean demoMode;
    private int roundsPlayed;
    private static final int DEMO_MODE_ROUNDS = 3;

    public Game(String[] playerNames, boolean[] isHuman, boolean demoMode) {
        this.isHuman = isHuman;
        this.demoMode = demoMode;
        this.roundsPlayed = 0;
        players = new ArrayList<>();
        deck = new Deck();
        scores = new HashMap<>();
        currentPlayerIndex = 0;
        gameEnded = false;
        scanner = new Scanner(System.in);

        for (String name : playerNames) {
            Player player = new Player(name);
            players.add(player);
            scores.put(player, 0);
        }

        dealCards();
        checkInitialSets();
    }

    private void dealCards() {
        for (int i = 0; i < 5; i++) {
            for (Player player : players) {
                player.addCard(deck.drawCard());
            }
        }
    }

    private void checkInitialSets() {
        for (Player player : players) {
            checkAndScoreSet(player);
        }
    }

    private void checkAndScoreSet(Player player) {
        Map<String, Integer> rankCount = new HashMap<>();
        List<Card> hand = player.getHand();
        
        // Count cards of each rank
        for (Card card : hand) {
            rankCount.merge(card.getRank(), 1, Integer::sum);
        }
        
        // Check for sets of 4
        for (Map.Entry<String, Integer> entry : rankCount.entrySet()) {
            if (entry.getValue() == 4) {
                scores.put(player, scores.get(player) + 1);
                player.removeCards(entry.getKey());
                System.out.println(player.getName() + " completed a set of " + entry.getKey() + "s!");
            }
        }
    }

    public void play() {
        System.out.println("Game has started!" + (demoMode ? " (Demo Mode - 3 rounds only)" : ""));
        showAllHands();

        while (!gameEnded) {
            playTurn();
            
            // In demo mode, count completed rounds (a round is when all players have had a turn)
            if (demoMode && currentPlayerIndex == 0) {
                roundsPlayed++;
                if (roundsPlayed >= DEMO_MODE_ROUNDS) {
                    System.out.println("\n=== Demo mode: 3 rounds completed ===");
                    announceWinner();
                    gameEnded = true;
                    continue;
                }
                System.out.println("\n=== Round " + roundsPlayed + " completed ===");
            }

            if (isGameOver()) {
                announceWinner();
                gameEnded = true;
            } else {
                System.out.println("\nPress Enter to continue to next turn...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }

    private void playTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);
        System.out.println("\n=== " + currentPlayer.getName() + "'s turn ===");
        currentPlayer.showHand();
        
        if (currentPlayer.getHand().isEmpty() && deck.size() > 0) {
            Card drawnCard = deck.drawCard();
            currentPlayer.addCard(drawnCard);
            System.out.println(currentPlayer.getName() + " drew a card: " + drawnCard);
        }
        
        if (!currentPlayer.getHand().isEmpty()) {
            String rankToAsk;
            Player targetPlayer;
            
            if (isHuman[currentPlayerIndex]) {
                // Human player's turn
                rankToAsk = getHumanRankChoice(currentPlayer);
                targetPlayer = getHumanTargetChoice(currentPlayer);
            } else {
                // AI player's turn
                rankToAsk = currentPlayer.getHand().get(0).getRank();
                targetPlayer = selectAITarget();
            }
            
            System.out.println(currentPlayer.getName() + " asks " + targetPlayer.getName() + 
                             " for any " + rankToAsk + "s");
            
            if (targetPlayer.hasCard(rankToAsk)) {
                List<Card> receivedCards = targetPlayer.giveCards(rankToAsk);
                for (Card card : receivedCards) {
                    currentPlayer.addCard(card);
                }
                System.out.println(targetPlayer.getName() + " gave " + receivedCards.size() + 
                                 " card(s) to " + currentPlayer.getName());
                checkAndScoreSet(currentPlayer);
            } else {
                System.out.println("Go Fish!");
                Card drawnCard = deck.drawCard();
                if (drawnCard != null) {
                    currentPlayer.addCard(drawnCard);
                    if (isHuman[currentPlayerIndex]) {
                        System.out.println("You drew: " + drawnCard);
                    } else {
                        System.out.println(currentPlayer.getName() + " drew a card");
                    }
                    if (drawnCard.getRank().equals(rankToAsk)) {
                        System.out.println("Lucky draw! Got the card they asked for!");
                        checkAndScoreSet(currentPlayer);
                    }
                }
            }
        }
        
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private String getHumanRankChoice(Player currentPlayer) {
        Set<String> availableRanks = new HashSet<>();
        for (Card card : currentPlayer.getHand()) {
            availableRanks.add(card.getRank());
        }
        
        System.out.println("\nAvailable ranks to ask for: " + availableRanks);
        String rank;
        while (true) {
            System.out.print("Enter the rank you want to ask for: ");
            rank = scanner.nextLine().trim().toUpperCase();
            if (availableRanks.contains(rank)) {
                return rank;
            }
            System.out.println("Invalid rank. You can only ask for ranks you have in your hand.");
        }
    }

    private Player getHumanTargetChoice(Player currentPlayer) {
        List<Player> availablePlayers = new ArrayList<>();
        System.out.println("\nAvailable players to ask:");
        int index = 1;
        for (Player player : players) {
            if (player != currentPlayer && !player.getHand().isEmpty()) {
                System.out.println(index + ". " + player.getName());
                availablePlayers.add(player);
                index++;
            }
        }
        
        if (availablePlayers.isEmpty()) {
            return selectAITarget(); // Fallback to AI selection if no valid targets
        }
        
        while (true) {
            System.out.print("Enter the number of the player you want to ask (1-" + availablePlayers.size() + "): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 1 && choice <= availablePlayers.size()) {
                    return availablePlayers.get(choice - 1);
                }
            } catch (NumberFormatException e) {
                // Handle invalid input
            }
            System.out.println("Invalid choice. Please try again.");
        }
    }

    private Player selectAITarget() {
        Player currentPlayer = players.get(currentPlayerIndex);
        List<Player> validTargets = new ArrayList<>();
        
        for (Player player : players) {
            if (player != currentPlayer && !player.getHand().isEmpty()) {
                validTargets.add(player);
            }
        }
        
        if (validTargets.isEmpty()) {
            return players.get((currentPlayerIndex + 1) % players.size());
        }
        
        return validTargets.get(new Random().nextInt(validTargets.size()));
    }

    private boolean isGameOver() {
        return deck.size() == 0 && players.stream().allMatch(p -> p.getHand().isEmpty());
    }

    private void announceWinner() {
        System.out.println("\n=== Game Over " + (demoMode ? "(Demo Mode)" : "") + " ===");
        for (Player player : players) {
            System.out.println(player.getName() + " score: " + scores.get(player));
        }
        
        Player winner = players.stream()
            .max((p1, p2) -> scores.get(p1).compareTo(scores.get(p2)))
            .orElse(null);
            
        if (winner != null) {
            System.out.println("\nWinner: " + winner.getName() + 
                             " with " + scores.get(winner) + " sets!");
        }
    }

    private void showAllHands() {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (isHuman[i]) {
                player.showHand();
            } else {
                System.out.println(player.getName() + "'s hand: [" + player.getHand().size() + " cards]");
            }
        }
    }
}

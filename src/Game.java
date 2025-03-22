import java.util.*;

public class Game {
    private List<Player> players;
    private Deck deck;
    private int currentPlayerIndex;
    private boolean gameEnded;
    private Map<Player, Integer> scores;
    private boolean[] isHuman;
    private boolean demoMode;
    private int roundsPlayed;
    private static final int DEMO_MODE_ROUNDS = 3;
    private GameGUI gui;
    private boolean waitingForHumanInput;
    private String lastAskedRank;
    private Player lastTargetPlayer;

    public Game(String[] playerNames, boolean[] isHuman, boolean demoMode) {
        this.isHuman = isHuman;
        this.demoMode = demoMode;
        this.roundsPlayed = 0;
        this.waitingForHumanInput = false;
        players = new ArrayList<>();
        deck = new Deck();
        scores = new HashMap<>();
        currentPlayerIndex = 0;
        gameEnded = false;

        for (String name : playerNames) {
            Player player = new Player(name);
            players.add(player);
            scores.put(player, 0);
        }

        dealCards();
        checkInitialSets();
    }

    public void setGUI(GameGUI gui) {
        this.gui = gui;
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
                logMessage(player.getName() + " completed a set of " + entry.getKey() + "s!");
            }
        }
    }

    public void play() {
        logMessage("Game has started!" + (demoMode ? " (Demo Mode - 3 rounds only)" : ""));
        playTurn();
    }

    private void playTurn() {
        if (gameEnded) return;

        Player currentPlayer = players.get(currentPlayerIndex);
        logMessage("\n=== " + currentPlayer.getName() + "'s turn ===");
        
        if (currentPlayer.getHand().isEmpty() && deck.size() > 0) {
            Card drawnCard = deck.drawCard();
            currentPlayer.addCard(drawnCard);
            logMessage(currentPlayer.getName() + " drew a card" + 
                (isHuman[currentPlayerIndex] ? ": " + drawnCard : ""));
        }

        // Update GUI with current game state
        gui.updateGameState(currentPlayer, players, isHuman[currentPlayerIndex]);
        
        if (!currentPlayer.getHand().isEmpty()) {
            if (isHuman[currentPlayerIndex]) {
                waitingForHumanInput = true;
                // Wait for human input through GUI
            } else {
                // AI turn
                playAITurn(currentPlayer);
            }
        } else {
            continueTurn();
        }
    }

    public void makeMove(String rankToAsk, Player targetPlayer) {
        // Only check waitingForHumanInput for human players
        Player currentPlayer = players.get(currentPlayerIndex);
        if (isHuman[currentPlayerIndex] && !waitingForHumanInput) return;
        
        waitingForHumanInput = false;
        
        logMessage(currentPlayer.getName() + " asks " + targetPlayer.getName() + 
                  " for any " + rankToAsk + "s");
        
        if (targetPlayer.hasCard(rankToAsk)) {
            List<Card> receivedCards = targetPlayer.giveCards(rankToAsk);
            for (Card card : receivedCards) {
                currentPlayer.addCard(card);
            }
            logMessage(targetPlayer.getName() + " gave " + receivedCards.size() + 
                      " card(s) to " + currentPlayer.getName());
            checkAndScoreSet(currentPlayer);
            gui.updateGameState(currentPlayer, players, isHuman[currentPlayerIndex]);
        } else {
            logMessage("Go Fish!");
            Card drawnCard = deck.drawCard();
            if (drawnCard != null) {
                currentPlayer.addCard(drawnCard);
                logMessage(currentPlayer.getName() + " drew" + 
                    (isHuman[currentPlayerIndex] ? ": " + drawnCard : " a card"));
                if (drawnCard.getRank().equals(rankToAsk)) {
                    logMessage("Lucky draw! Got the card they asked for!");
                    checkAndScoreSet(currentPlayer);
                }
                gui.updateGameState(currentPlayer, players, isHuman[currentPlayerIndex]);
            }
        }
        
        continueTurn();
    }

    private void playAITurn(Player currentPlayer) {
        if (currentPlayer.getHand().isEmpty() && deck.size() > 0) {
            Card drawnCard = deck.drawCard();
            currentPlayer.addCard(drawnCard);
            logMessage(currentPlayer.getName() + " drew a card");
            gui.updateGameState(currentPlayer, players, false);
            return;
        }

        // Simple AI: ask for the first card's rank from a random player
        if (!currentPlayer.getHand().isEmpty()) {
            String rankToAsk = currentPlayer.getHand().get(0).getRank();
            List<Player> validTargets = new ArrayList<>();
            
            for (Player player : players) {
                if (player != currentPlayer && !player.getHand().isEmpty()) {
                    validTargets.add(player);
                }
            }
            
            if (!validTargets.isEmpty()) {
                Player targetPlayer = validTargets.get(new Random().nextInt(validTargets.size()));
                // Store the AI's move before making it
                this.lastAskedRank = rankToAsk;
                this.lastTargetPlayer = targetPlayer;
                makeMove(rankToAsk, targetPlayer);
            } else {
                this.lastAskedRank = null;
                this.lastTargetPlayer = null;
                if (deck.size() > 0) {
                    Card drawnCard = deck.drawCard();
                    currentPlayer.addCard(drawnCard);
                    logMessage(currentPlayer.getName() + " drew a card");
                }
                gui.updateGameState(currentPlayer, players, false);
                continueTurn();
            }
        } else {
            this.lastAskedRank = null;
            this.lastTargetPlayer = null;
            continueTurn();
        }
    }

    public String getLastAskedRank() {
        return lastAskedRank;
    }

    public Player getLastTargetPlayer() {
        return lastTargetPlayer;
    }

    public void continueTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        
        // In demo mode, count completed rounds
        if (demoMode && currentPlayerIndex == 0) {
            roundsPlayed++;
            if (roundsPlayed >= DEMO_MODE_ROUNDS) {
                logMessage("\n=== Demo mode: 3 rounds completed ===");
                endGame();
                return;
            }
            logMessage("\n=== Round " + roundsPlayed + " completed ===");
        }

        if (isGameOver()) {
            endGame();
        } else {
            playTurn();
        }
    }

    private boolean isGameOver() {
        return deck.size() == 0 && players.stream().allMatch(p -> p.getHand().isEmpty());
    }

    private void endGame() {
        gameEnded = true;
        StringBuilder message = new StringBuilder();
        message.append("\n=== Game Over " + (demoMode ? "(Demo Mode)" : "") + " ===\n");
        
        for (Player player : players) {
            message.append(player.getName() + " score: " + scores.get(player) + "\n");
        }
        
        Player winner = players.stream()
            .max((p1, p2) -> scores.get(p1).compareTo(scores.get(p2)))
            .orElse(null);
            
        if (winner != null) {
            message.append("\nWinner: " + winner.getName() + 
                         " with " + scores.get(winner) + " sets!");
        }
        
        gui.showGameOver(message.toString());
    }

    public int getDeckSize() {
        return deck.size();
    }

    public Map<Player, Integer> getScores() {
        return scores;
    }

    public List<Player> getPlayers() {
        return players;
    }

    private void logMessage(String message) {
        if (gui != null) {
            gui.appendToGameLog(message);
        }
    }
}

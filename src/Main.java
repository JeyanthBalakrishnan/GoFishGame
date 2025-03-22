import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to Go Fish!");
        System.out.print("Do you want to play in demo mode (3 rounds only)? (yes/no): ");
        boolean demoMode = scanner.nextLine().trim().toLowerCase().startsWith("y");
        
        System.out.print("How many human players (1-4)? ");
        int humanPlayers = getValidInput(1, 4);
        
        String[] playerNames = new String[4];
        boolean[] isHuman = new boolean[4];
        
        // Get human player names
        for (int i = 0; i < humanPlayers; i++) {
            System.out.print("Enter name for human player " + (i + 1) + ": ");
            playerNames[i] = scanner.nextLine().trim();
            isHuman[i] = true;
        }
        
        // Fill remaining slots with AI players
        String[] aiNames = {"AI-Alice", "AI-Bob", "AI-Charlie", "AI-Dave"};
        for (int i = humanPlayers; i < 4; i++) {
            playerNames[i] = aiNames[i];
            isHuman[i] = false;
        }

        Game game = new Game(playerNames, isHuman, demoMode);
        game.play();
        
        scanner.close();
    }
    
    private static int getValidInput(int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.print("Please enter a number between " + min + " and " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number between " + min + " and " + max + ": ");
            }
        }
    }
}

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

public class GameGUI extends JFrame {
    private Game game;
    private JPanel mainPanel;
    private JPanel playerHandPanel;
    private JPanel gameControlPanel;
    private JPanel otherPlayersPanel;
    private JPanel deckPanel;
    private Map<String, JButton> playerButtons;
    private List<CardButton> cardButtons;
    private JTextArea gameLog;
    private String selectedRank;
    private Player selectedPlayer;
    private JScrollPane logScrollPane;
    private JButton continueButton;
    private JLabel aiMoveLabel;
    private AnimatedBackground background;
    private Timer waitingAnimationTimer;
    private int waitingDots = 0;
    private JLabel deckCountLabel;
    private JLayeredPane layeredPane;

    private static final int TOP_X = 400;
    private static final int TOP_Y = 50;
    private static final int LEFT_X = 50;
    private static final int LEFT_Y = 275;
    private static final int RIGHT_X = 750;
    private static final int RIGHT_Y = 275;
    private static final int BOTTOM_X = 400;
    private static final int BOTTOM_Y = 500;

    public GameGUI() {
        super("Go Fish Card Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Set up the animated background
        background = new AnimatedBackground();
        setContentPane(background);
        setLayout(new BorderLayout());
        
        createMainMenu();
    }

    private void createMainMenu() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Make panel transparent to show animated background
                setOpaque(false);
                super.paintComponent(g);
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create title with glow effect
        JLabel titleLabel = new GlowLabel("Go Fish", new Color(255, 215, 0));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create stylish menu buttons
        JButton normalGameButton = createMenuButton("Start Normal Game");
        JButton demoGameButton = createMenuButton("Start Demo Game (3 Rounds)");

        normalGameButton.addActionListener(e -> startGameSetup(false));
        demoGameButton.addActionListener(e -> startGameSetup(true));

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 60)));
        mainPanel.add(normalGameButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(demoGameButton);
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(60, 120, 190),
                    0, getHeight(), new Color(40, 80, 140)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Add highlight effect
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 20, 20);

                // Draw text with shadow
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(text, 21, 31);
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, 20, 30);

                g2d.dispose();
            }
        };
        button.setPreferredSize(new Dimension(300, 50));
        button.setMaximumSize(new Dimension(300, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 200));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 120, 190));
            }
        });

        return button;
    }

    private void startGameSetup(boolean demoMode) {
        mainPanel.removeAll();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel playersLabel = new JLabel("Select number of human players (1-4):");
        playersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JComboBox<Integer> playerCount = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        playerCount.setMaximumSize(new Dimension(100, 25));
        playerCount.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new JButton("Start Game");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> {
            int humanPlayers = (Integer) playerCount.getSelectedItem();
            getPlayerNames(humanPlayers, demoMode);
        });

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(playersLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(playerCount);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(startButton);
        mainPanel.add(Box.createVerticalGlue());

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void getPlayerNames(int humanPlayers, boolean demoMode) {
        mainPanel.removeAll();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel[] nameLabels = new JLabel[humanPlayers];
        JTextField[] nameFields = new JTextField[humanPlayers];

        for (int i = 0; i < humanPlayers; i++) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new FlowLayout());
            playerPanel.setMaximumSize(new Dimension(300, 30));
            
            nameLabels[i] = new JLabel("Player " + (i + 1) + " name: ");
            nameFields[i] = new JTextField(15);
            
            playerPanel.add(nameLabels[i]);
            playerPanel.add(nameFields[i]);
            
            mainPanel.add(playerPanel);
        }

        JButton startButton = new JButton("Start Game");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> {
            String[] playerNames = new String[4];
            boolean[] isHuman = new boolean[4];
            
            // Get human player names
            for (int i = 0; i < humanPlayers; i++) {
                String name = nameFields[i].getText().trim();
                if (name.isEmpty()) name = "Player " + (i + 1);
                playerNames[i] = name;
                isHuman[i] = true;
            }
            
            // Fill remaining slots with AI players
            String[] aiNames = {"AI-Alice", "AI-Bob", "AI-Charlie", "AI-Dave"};
            for (int i = humanPlayers; i < 4; i++) {
                playerNames[i] = aiNames[i - humanPlayers];
                isHuman[i] = false;
            }
            
            startGame(playerNames, isHuman, demoMode);
        });

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(startButton);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void startGame(String[] playerNames, boolean[] isHuman, boolean demoMode) {
        game = new Game(playerNames, isHuman, demoMode);
        createGameInterface();
        game.setGUI(this);
        game.play();
    }

    private void createGameInterface() {
        getContentPane().removeAll();
        
        // Create layered pane for the entire game
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(1000, 700));
        setContentPane(layeredPane);
        
        // Add animated background at the bottom layer
        background.setBounds(0, 0, 1000, 700);
        layeredPane.add(background, JLayeredPane.DEFAULT_LAYER);

        // Create main game panel
        JPanel mainGamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                setOpaque(false);
                super.paintComponent(g);
                
                // Draw a semi-transparent green table
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 100, 0, 180));
                g2d.fillRoundRect(50, 50, getWidth() - 100, getHeight() - 100, 30, 30);
                g2d.setColor(new Color(0, 80, 0, 180));
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawRoundRect(50, 50, getWidth() - 100, getHeight() - 100, 30, 30);
                g2d.dispose();
            }
        };
        mainGamePanel.setLayout(null); // Use absolute positioning for table layout
        mainGamePanel.setBounds(0, 0, 1000, 700);
        mainGamePanel.setOpaque(false);

        // Create game log panel at the bottom
        gameLog = new JTextArea(5, 40);
        gameLog.setEditable(false);
        gameLog.setFont(new Font("Monospaced", Font.PLAIN, 14));
        gameLog.setBackground(new Color(0, 0, 0, 180));
        gameLog.setForeground(Color.WHITE);
        gameLog.setCaretColor(Color.WHITE);
        logScrollPane = new JScrollPane(gameLog);
        logScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100)),
            "Game Log",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            Color.WHITE
        ));
        logScrollPane.setOpaque(false);
        logScrollPane.getViewport().setOpaque(false);
        logScrollPane.setBounds(200, 600, 600, 80);
        mainGamePanel.add(logScrollPane);

        // Create deck panel in the center of the table
        deckPanel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                setOpaque(false);
                super.paintComponent(g);
            }
        };
        deckPanel.setOpaque(false);
        deckPanel.setBounds(450, 275, 100, 150);
        
        // Create a face-down card for the deck
        CardButton deckCard = new CardButton(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw card back with pattern
                g2d.setColor(new Color(30, 100, 200));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 12, 12));
                
                // Draw pattern
                g2d.setColor(new Color(20, 60, 150));
                for (int i = 0; i < getWidth(); i += 10) {
                    for (int j = 0; j < getHeight(); j += 10) {
                        g2d.fillOval(i, j, 5, 5);
                    }
                }

                // Draw border
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 12, 12));
                
                g2d.dispose();
            }
        };
        deckCard.setEnabled(false);
        
        // Create deck count label
        deckCountLabel = new JLabel("Cards: 0", SwingConstants.CENTER);
        deckCountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        deckCountLabel.setForeground(Color.WHITE);
        
        deckPanel.add(deckCard, BorderLayout.CENTER);
        deckPanel.add(deckCountLabel, BorderLayout.SOUTH);
        mainGamePanel.add(deckPanel);
        
        // Create player hand panel at the bottom
        playerHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, -20, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                setOpaque(false);
                super.paintComponent(g);
            }
        };
        playerHandPanel.setOpaque(false);
        playerHandPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100)),
            "Your Hand",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            Color.WHITE
        ));
        playerHandPanel.setBounds(50, 550, 900, 150);  // Moved down and made wider
        mainGamePanel.add(playerHandPanel);
        
        // Create other players panels around the table
        otherPlayersPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                setOpaque(false);
                super.paintComponent(g);
            }
        };
        otherPlayersPanel.setLayout(null);
        otherPlayersPanel.setOpaque(false);
        otherPlayersPanel.setBounds(0, 0, 1000, 700);
        mainGamePanel.add(otherPlayersPanel);
        
        // Create game control panel
        gameControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                setOpaque(false);
                super.paintComponent(g);
            }
        };
        gameControlPanel.setOpaque(false);
        gameControlPanel.setBounds(350, 200, 300, 60);
        
        aiMoveLabel = new JLabel();
        aiMoveLabel.setFont(new Font("Arial", Font.BOLD, 16));
        aiMoveLabel.setForeground(Color.WHITE);
        aiMoveLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gameControlPanel.add(aiMoveLabel);
        
        continueButton = createMenuButton("Continue");
        continueButton.setPreferredSize(new Dimension(150, 40));
        continueButton.setMaximumSize(new Dimension(150, 40));
        continueButton.setEnabled(false);
        continueButton.addActionListener(e -> {
            continueButton.setEnabled(false);
            aiMoveLabel.setText("");
            stopWaitingAnimation();
            game.continueTurn();
        });
        gameControlPanel.add(continueButton);
        mainGamePanel.add(gameControlPanel);
        
        layeredPane.add(mainGamePanel, JLayeredPane.PALETTE_LAYER);
        
        playerButtons = new HashMap<>();
        cardButtons = new ArrayList<>();
        
        revalidate();
        repaint();
    }

    public void updateGameState(Player currentPlayer, List<Player> players, boolean isHumanTurn) {
        SwingUtilities.invokeLater(() -> {
            playerHandPanel.removeAll();
            otherPlayersPanel.removeAll();
            cardButtons.clear();
            playerButtons.clear();

            // Update deck count
            deckCountLabel.setText("Cards: " + game.getDeckSize());

            // Assign positions based on number of players and current player
            String[] positions;
            int currentPlayerIndex = players.indexOf(currentPlayer);
            
            if (players.size() == 2) {
                positions = new String[]{"bottom", "top"};
            } else if (players.size() == 3) {
                positions = new String[]{"bottom", "left", "right"};
            } else {
                positions = new String[]{"bottom", "left", "top", "right"};
            }
            
            // Rotate positions so current player is always at their assigned position
            String[] rotatedPositions = new String[positions.length];
            for (int i = 0; i < positions.length; i++) {
                int newIndex = (i - currentPlayerIndex + positions.length) % positions.length;
                rotatedPositions[i] = positions[newIndex];
            }

            // Position players around the table
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                if (player != currentPlayer || !isHumanTurn) {
                    JButton playerButton = createPlayerButton(player);
                    playerButton.addActionListener(e -> {
                        selectedPlayer = player;
                        updateSelectedPlayer(playerButton);
                        if (selectedRank != null && selectedPlayer != null) {
                            game.makeMove(selectedRank, selectedPlayer);
                            selectedRank = null;
                            selectedPlayer = null;
                        }
                    });
                    playerButtons.put(player.getName(), playerButton);
                    
                    // Get position for this player
                    String position = rotatedPositions[i];
                    int x, y;
                    
                    // Set coordinates based on position
                    switch (position) {
                        case "top":
                            x = TOP_X;
                            y = TOP_Y;
                            break;
                        case "left":
                            x = LEFT_X;
                            y = LEFT_Y;
                            break;
                        case "right":
                            x = RIGHT_X;
                            y = RIGHT_Y;
                            break;
                        case "bottom":
                            x = BOTTOM_X;
                            y = BOTTOM_Y;
                            break;
                        default:
                            x = 0;
                            y = 0;
                    }
                    
                    playerButton.setBounds(x, y, 150, 100);
                    otherPlayersPanel.add(playerButton);

                    // If this is the current AI player, animate their new cards
                    if (player == currentPlayer && !isHumanTurn) {
                        List<Card> hand = player.getHand();
                        if (!hand.isEmpty()) {
                            Card lastCard = hand.get(hand.size() - 1);
                            Point start = new Point(deckPanel.getX() + deckPanel.getWidth()/2, 
                                                  deckPanel.getY() + deckPanel.getHeight()/2);
                            Point end = new Point(x + 75, y + 50);
                            animateCardDraw(lastCard, start, end);
                        }
                    }
                }
            }

            if (isHumanTurn) {
                // Position the hand panel based on current player's position
                String currentPosition = rotatedPositions[currentPlayerIndex];
                int handX, handY, handWidth, handHeight;
                
                switch (currentPosition) {
                    case "left":
                        handX = 50;
                        handY = 100;
                        handWidth = 150;
                        handHeight = 400;
                        break;
                    case "right":
                        handX = 800;
                        handY = 100;
                        handWidth = 150;
                        handHeight = 400;
                        break;
                    case "top":
                        handX = 50;
                        handY = 50;
                        handWidth = 900;
                        handHeight = 150;
                        break;
                    case "bottom":
                    default:
                        handX = 50;
                        handY = 550;
                        handWidth = 900;
                        handHeight = 150;
                }
                
                playerHandPanel.setBounds(handX, handY, handWidth, handHeight);

                // Show current player's hand with animation
                List<Card> hand = currentPlayer.getHand();
                int delay = 0;
                boolean isVertical = currentPosition.equals("left") || currentPosition.equals("right");
                int cardSpacing = isVertical ? 
                    Math.min(60, (handHeight - 100) / Math.max(1, hand.size())) :
                    Math.min(80, (handWidth - 100) / Math.max(1, hand.size()));
                
                for (Card card : hand) {
                    CardButton cardButton = new CardButton(card);
                    cardButton.setVisible(false);
                    cardButton.addActionListener(e -> {
                        selectedRank = card.getRank();
                        updateSelectedCard(cardButton);
                    });
                    cardButtons.add(cardButton);
                    playerHandPanel.add(cardButton);
                    
                    // Position each card with proper spacing
                    if (isVertical) {
                        cardButton.setBounds(25, cardSpacing * cardButtons.size() - cardSpacing, 
                                          CardButton.CARD_WIDTH, CardButton.CARD_HEIGHT);
                    } else {
                        cardButton.setBounds(cardSpacing * cardButtons.size() - cardSpacing, 25, 
                                          CardButton.CARD_WIDTH, CardButton.CARD_HEIGHT);
                    }
                    
                    // Animate card appearance
                    Timer timer = new Timer(delay, e -> {
                        cardButton.setVisible(true);
                        Point start = new Point(deckPanel.getX() + deckPanel.getWidth()/2, 
                                              deckPanel.getY() + deckPanel.getHeight()/2);
                        Point end = SwingUtilities.convertPoint(playerHandPanel, 
                                                              cardButton.getLocation(), 
                                                              layeredPane);
                        animateCardDraw(card, start, end);
                    });
                    timer.setRepeats(false);
                    timer.start();
                    delay += 200;
                }
            } else {
                // Show AI player's turn info
                startWaitingAnimation(currentPlayer.getName());
                String aiMove = String.format("<html><div style='text-align: center;'>%s's turn<br>Asking for: <span style='color: yellow'>%s</span><br>Target: <span style='color: yellow'>%s</span></div></html>",
                    currentPlayer.getName(),
                    game.getLastAskedRank(),
                    game.getLastTargetPlayer() != null ? game.getLastTargetPlayer().getName() : "");
                aiMoveLabel.setText(aiMove);
                continueButton.setEnabled(true);
            }

            playerHandPanel.revalidate();
            playerHandPanel.repaint();
            otherPlayersPanel.revalidate();
            otherPlayersPanel.repaint();
        });
    }

    private JButton createPlayerButton(Player player) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        
        int handSize = player.getHand().size();
        String handText = handSize + " card" + (handSize != 1 ? "s" : "");
        int score = game.getScores().get(player);
        String scoreText = score + " set" + (score != 1 ? "s" : "");
        
        String text = String.format("<html><div style='text-align: center;'><b>%s</b><br>%s<br>%s</div></html>", 
            player.getName(), handText, scoreText);
        
        button.setText(text);
        button.setBackground(new Color(50, 120, 50));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 70, 30), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        return button;
    }

    private void updateSelectedCard(CardButton selectedButton) {
        for (CardButton button : cardButtons) {
            button.setSelected(button == selectedButton);
        }
    }

    private void updateSelectedPlayer(JButton selectedButton) {
        for (JButton button : playerButtons.values()) {
            button.setBackground(button == selectedButton ? new Color(255, 255, 150) : new Color(200, 200, 200));
        }
        
        if (selectedButton != null) {
            for (Map.Entry<String, JButton> entry : playerButtons.entrySet()) {
                if (entry.getValue() == selectedButton) {
                    for (Player p : game.getPlayers()) {
                        if (p.getName().equals(entry.getKey())) {
                            selectedPlayer = p;
                            if (selectedRank != null) {
                                makeMove(selectedRank, selectedPlayer);
                                selectedRank = null;
                                selectedPlayer = null;
                                updateSelectedCard(null);
                                updateSelectedPlayer(null);
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    public void appendToGameLog(String message) {
        SwingUtilities.invokeLater(() -> {
            gameLog.append(message + "\n");
            gameLog.setCaretPosition(gameLog.getDocument().getLength());
        });
    }

    public void showGameOver(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            int choice = JOptionPane.showConfirmDialog(this, 
                "Would you like to play again?", 
                "Play Again?", 
                JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // Clean up old game state
                if (waitingAnimationTimer != null) {
                    waitingAnimationTimer.stop();
                    waitingAnimationTimer = null;
                }
                if (layeredPane != null) {
                    layeredPane.removeAll();
                }
                selectedRank = null;
                selectedPlayer = null;
                cardButtons = new ArrayList<>();
                playerButtons = new HashMap<>();
                game = null;
                
                // Reset the frame completely
                getContentPane().removeAll();
                background = new AnimatedBackground();
                setContentPane(background);
                setLayout(new BorderLayout());
                mainPanel = null;
                playerHandPanel = null;
                gameControlPanel = null;
                otherPlayersPanel = null;
                deckPanel = null;
                gameLog = null;
                logScrollPane = null;
                continueButton = null;
                aiMoveLabel = null;
                deckCountLabel = null;
                layeredPane = null;
                
                // Create fresh main menu
                createMainMenu();
                revalidate();
                repaint();
            } else {
                dispose();
            }
        });
    }

    private void startWaitingAnimation(String playerName) {
        if (waitingAnimationTimer != null) {
            waitingAnimationTimer.stop();
        }
        
        waitingAnimationTimer = new Timer(500, e -> {
            waitingDots = (waitingDots + 1) % 4;
            String dots = ".".repeat(waitingDots);
            aiMoveLabel.setText(playerName + " thinking" + dots);
        });
        waitingAnimationTimer.start();
    }

    private void stopWaitingAnimation() {
        if (waitingAnimationTimer != null) {
            waitingAnimationTimer.stop();
            waitingAnimationTimer = null;
        }
        waitingDots = 0;
    }

    // Improved card draw animation
    private void animateCardDraw(Card card, Point start, Point end) {
        CardButton animatedCard = new CardButton(card);
        animatedCard.setBounds(start.x - CardButton.CARD_WIDTH/2, 
                             start.y - CardButton.CARD_HEIGHT/2,
                             CardButton.CARD_WIDTH,
                             CardButton.CARD_HEIGHT);
        
        layeredPane.add(animatedCard, JLayeredPane.DRAG_LAYER);
        
        Timer timer = new Timer(16, null);
        final long startTime = System.currentTimeMillis();
        final int duration = 500;
        
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, (float) elapsed / duration);
            
            // Use easing function for smooth animation
            float easedProgress = (float) (1 - Math.pow(1 - progress, 3));
            
            int newX = start.x + (int) ((end.x - start.x) * easedProgress);
            int newY = start.y + (int) ((end.y - start.y) * easedProgress);
            
            animatedCard.setLocation(newX - CardButton.CARD_WIDTH/2, 
                                   newY - CardButton.CARD_HEIGHT/2);
            
            if (progress >= 1f) {
                timer.stop();
                layeredPane.remove(animatedCard);
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });
        
        timer.start();
    }

    private void showFeedbackAnimation(boolean success, Point center) {
        JPanel animationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                float alpha = 1.0f;
                if (getClientProperty("alpha") != null) {
                    alpha = (float) getClientProperty("alpha");
                }
                
                Color color = success ? new Color(0, 255, 0, (int)(alpha * 255)) : 
                                      new Color(255, 0, 0, (int)(alpha * 255));
                g2d.setColor(color);
                
                int size = 100;
                if (getClientProperty("size") != null) {
                    size = (int) getClientProperty("size");
                }
                
                g2d.setStroke(new BasicStroke(3));
                if (success) {
                    // Draw checkmark
                    int[] xPoints = {getWidth()/2 - size/4, getWidth()/2, getWidth()/2 + size/2};
                    int[] yPoints = {getHeight()/2, getHeight()/2 + size/4, getHeight()/2 - size/4};
                    g2d.drawPolyline(xPoints, yPoints, 3);
                } else {
                    // Draw X
                    g2d.drawLine(getWidth()/2 - size/2, getHeight()/2 - size/2,
                               getWidth()/2 + size/2, getHeight()/2 + size/2);
                    g2d.drawLine(getWidth()/2 - size/2, getHeight()/2 + size/2,
                               getWidth()/2 + size/2, getHeight()/2 - size/2);
                }
                g2d.dispose();
            }
        };
        
        animationPanel.setOpaque(false);
        animationPanel.setBounds(center.x - 75, center.y - 75, 150, 150);
        layeredPane.add(animationPanel, JLayeredPane.POPUP_LAYER);
        
        Timer timer = new Timer(16, null);
        final long startTime = System.currentTimeMillis();
        final int duration = 1000;
        
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, (float) elapsed / duration);
            
            // Use easing function for smooth animation
            float easedProgress = (float) (1 - Math.pow(1 - progress, 3));
            
            // Scale up and fade out
            int size = (int) (100 + 50 * easedProgress);
            float alpha = 1.0f - easedProgress;
            
            animationPanel.putClientProperty("size", size);
            animationPanel.putClientProperty("alpha", alpha);
            animationPanel.repaint();
            
            if (progress >= 1f) {
                timer.stop();
                layeredPane.remove(animationPanel);
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });
        
        timer.start();
    }

    private void makeMove(String rank, Player targetPlayer) {
        if (game != null && rank != null && targetPlayer != null) {
            Point center = new Point(
                getWidth() / 2,
                getHeight() / 2
            );
            boolean hadCard = targetPlayer.hasCard(rank);
            game.makeMove(rank, targetPlayer);
            showFeedbackAnimation(hadCard, center);
        }
    }

    class GlowLabel extends JLabel {
        private Color glowColor;
        private static final int GLOW_SIZE = 10;

        public GlowLabel(String text, Color glowColor) {
            super(text);
            this.glowColor = glowColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw glow effect
            for (int i = GLOW_SIZE; i > 0; i--) {
                float alpha = 1.0f / (i * 2);
                g2d.setColor(new Color(
                    glowColor.getRed() / 255f,
                    glowColor.getGreen() / 255f,
                    glowColor.getBlue() / 255f,
                    alpha
                ));
                g2d.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize() + i));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent()
                );
            }

            // Draw main text
            g2d.setColor(getForeground());
            g2d.setFont(getFont());
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(getText(),
                (getWidth() - fm.stringWidth(getText())) / 2,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent()
            );

            g2d.dispose();
        }
    }

    class AnimatedBackground extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw animated background
            g2d.setColor(new Color(40, 110, 40)); // Dark green background
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.dispose();
        }
    }

    class CardAnimation {
        private CardButton cardButton;
        private Point start;
        private Point end;
        private int duration = 500; // milliseconds
        private long startTime;

        public CardAnimation(CardButton cardButton, Point start, Point end) {
            this.cardButton = cardButton;
            this.start = start;
            this.end = end;
        }

        public void start() {
            startTime = System.currentTimeMillis();
            Timer timer = new Timer(16, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    long currentTime = System.currentTimeMillis();
                    float progress = (currentTime - startTime) / (float) duration;
                    if (progress >= 1) {
                        cardButton.setLocation(end);
                        ((Timer) e.getSource()).stop();
                    } else {
                        int x = (int) (start.x + (end.x - start.x) * progress);
                        int y = (int) (start.y + (end.y - start.y) * progress);
                        cardButton.setLocation(x, y);
                    }
                    cardButton.getParent().repaint();
                }
            });
            timer.start();
        }
    }
}

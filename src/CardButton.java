import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CardButton extends JButton {
    public static final int CARD_WIDTH = 100;
    public static final int CARD_HEIGHT = 140;
    private static final int CORNER_RADIUS = 12;
    private final Card card;
    private boolean isSelected;
    private static final Color HEART_DIAMOND_COLOR = new Color(220, 0, 0);
    private static final Color SPADE_CLUB_COLOR = Color.BLACK;
    private static final Color SELECTED_COLOR = new Color(255, 255, 150);

    public CardButton(Card card) {
        this.card = card;
        this.isSelected = false;
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw card shadow
        if (!isSelected) {
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fill(new RoundRectangle2D.Float(3, 3, getWidth() - 4, getHeight() - 4, CORNER_RADIUS, CORNER_RADIUS));
        }

        // Draw card background
        if (isSelected) {
            g2d.setColor(SELECTED_COLOR);
        } else {
            g2d.setColor(Color.WHITE);
        }
        g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, CORNER_RADIUS, CORNER_RADIUS));

        // Draw card border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, CORNER_RADIUS, CORNER_RADIUS));

        // Set color based on suit
        Color cardColor = card.getSuit().equals("Hearts") || card.getSuit().equals("Diamonds") 
            ? HEART_DIAMOND_COLOR : SPADE_CLUB_COLOR;
        g2d.setColor(cardColor);

        // Draw rank and suit
        String rank = card.getRank();
        String suitSymbol = getSuitSymbol(card.getSuit());

        // Draw in top-left corner
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(rank, 8, 25);
        g2d.setFont(new Font("Dialog", Font.PLAIN, 20));
        g2d.drawString(suitSymbol, 8, 45);

        // Draw in center (larger)
        g2d.setFont(new Font("Dialog", Font.BOLD, 32));
        FontMetrics fm = g2d.getFontMetrics();
        String centerText = rank + suitSymbol;
        int centerX = (getWidth() - fm.stringWidth(centerText)) / 2;
        int centerY = (getHeight() + fm.getAscent()) / 2;
        g2d.drawString(centerText, centerX, centerY);

        // Draw in bottom-right corner (rotated 180 degrees)
        g2d.translate(getWidth() - 3, getHeight() - 3);
        g2d.rotate(Math.PI);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(rank, 8, 25);
        g2d.setFont(new Font("Dialog", Font.PLAIN, 20));
        g2d.drawString(suitSymbol, 8, 45);

        g2d.dispose();
    }

    private String getSuitSymbol(String suit) {
        return switch (suit) {
            case "Hearts" -> "♥";
            case "Diamonds" -> "♦";
            case "Clubs" -> "♣";
            case "Spades" -> "♠";
            default -> suit;
        };
    }

    @Override
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint();
    }

    public Card getCard() {
        return card;
    }

    @Override
    public boolean contains(int x, int y) {
        // Make the hit area slightly larger than the visible card
        return new RoundRectangle2D.Float(-5, -5, getWidth() + 5, getHeight() + 5, CORNER_RADIUS, CORNER_RADIUS)
            .contains(x, y);
    }
}

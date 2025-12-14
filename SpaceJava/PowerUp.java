package SpaceJava;

import java.awt.*;
import java.util.Random;

public class PowerUp {
    
    public int x, y;
    public boolean active = true;
    private double ySpeed = 2;
    private PowerUpType type; // <--- ADD THIS
    
    // Animation variables
    private float floatOffset = 0;
    private float pulse = 0;

    public PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
        this.pulse = (float) (Math.random() * Math.PI * 2);
        
        // --- ASSIGN A RANDOM POWERUP TYPE ---
        PowerUpType[] types = PowerUpType.values();
        this.type = types[new Random().nextInt(types.length)];
    }
    
    // Getter so GamePanel knows what this is
    public PowerUpType getType() {
        return type;
    }

    public void update() {
        y += ySpeed;
        floatOffset += 0.1f;
        x += Math.sin(floatOffset) * 0.5; 
        pulse += 0.1f;
        
        if (y > 600) active = false;
    }
    
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double scale = 1.0 + Math.sin(pulse) * 0.1; 
        int size = (int) (24 * scale);
        int offset = (30 - size) / 2; 

        int drawX = x + offset;
        int drawY = y + offset;

        // Color based on type (Optional: Distinct colors for different powerups)
        Color c = Color.CYAN;
        if(type == PowerUpType.MULTI_SHOT) c = Color.YELLOW;
        if(type == PowerUpType.POWER_UP_2) c = Color.GREEN;
        if(type == PowerUpType.POWER_UP_5) c = Color.MAGENTA;

        // Outer Glow
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60)); 
        g2.fillOval(drawX - 5, drawY - 5, size + 10, size + 10);

        // Core
        g2.setColor(c);
        g2.fillOval(drawX, drawY, size, size);
        g2.setColor(Color.WHITE);
        g2.fillOval(drawX + 5, drawY + 5, size / 3, size / 3);
        
        // Symbol
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        // Draw first letter of powerup name
        g2.drawString(type.getName().substring(0,1), drawX + 8, drawY + 16);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, 24, 24);
    }
}
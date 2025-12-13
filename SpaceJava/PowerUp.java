package SpaceJava;

import java.awt.*;

public class PowerUp {
    
    public int x, y;
    public boolean active = true;
    private int speed = 2;
    
    public PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void update() {
        y += speed;
        
        // Remove if off screen
        if (y > 600) {
            active = false;
        }
    }
    
    public void draw(Graphics g) {
        // Draw a glowing power-up (green star-like shape)
        g.setColor(Color.green);
        g.fillOval(x, y, 30, 30);
        
        // Add a border for visibility
        g.setColor(Color.white);
        g.drawOval(x, y, 30, 30);
        
        // Draw a "P" or star symbol
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.white);
        g.drawString("P", x + 8, y + 22);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, 30, 30);
    }
}


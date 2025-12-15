package SpaceJava;

import java.awt.Color;
import java.awt.Graphics2D;

public class Bullet {
    // Coordinates as double for smooth vector movement
    public double x, y;
    public double velX, velY;
    public boolean active = true;
    public boolean isEnemy;

    // Constructor for simple vertical bullets
    public Bullet(double x, double y, double speed, boolean isEnemy) {
        this.x = x;
        this.y = y;
        this.isEnemy = isEnemy;
        this.velX = 0;
        // Enemy shoots down (+), Player shoots up (-)
        this.velY = isEnemy ? speed : -Math.abs(speed);
    }

    // Constructor for complex directional bullets (Spread shots)
    public Bullet(double x, double y, double velX, double velY, boolean isEnemy) {
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.isEnemy = isEnemy;
    }

    public void update() {
        x += velX;
        y += velY;

        // Deactivate if off-screen
        if (y < -50 || y > 850 || x < -50 || x > 850) {
            active = false;
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(isEnemy ? new Color(255, 50, 50) : new Color(255, 255, 0));
        // Draw slightly larger bullets
        g.fillOval((int)x, (int)y, 6, 12);
        
        // Add a glow effect
        g.setColor(isEnemy ? new Color(255, 0, 0, 100) : new Color(255, 200, 0, 100));
        g.fillOval((int)x - 2, (int)y - 2, 10, 16);
    }
}
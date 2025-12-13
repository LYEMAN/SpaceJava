package SpaceJava;

import java.awt.*;

public class Bullet {

    public int x, y;
    public int speed;
    public double velocityX = 0; // For angled shots
    public double velocityY; // For angled shots
    public boolean active = true;
    public boolean fromPlayer; // true = player bullet, false = enemy bullet

    // Constructor for straight shots (backward compatible)
    public Bullet(int x, int y, int speed, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.velocityY = speed;
        this.fromPlayer = fromPlayer;
    }
    
    // Constructor for angled shots
    public Bullet(int x, int y, double velocityX, double velocityY, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.speed = (int)Math.abs(velocityY); // Keep for compatibility
        this.fromPlayer = fromPlayer;
    }

    public void update() {
        x += velocityX;
        y += velocityY;

        if (y < 0 || y > 600 || x < 0 || x > 800) {
            active = false;
        }
    }

    public void draw(Graphics g) {
        if (fromPlayer) {
            g.setColor(Color.yellow);
        } else {
            g.setColor(Color.orange);
        }
        g.fillRect(x, y, 6, 12);
    }
}


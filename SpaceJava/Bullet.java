package SpaceJava;

import java.awt.*;

public class Bullet {

    public int x, y;
    public int speed;
    public boolean active = true;
    public boolean fromPlayer; // true = player bullet, false = enemy bullet

    public Bullet(int x, int y, int speed, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.fromPlayer = fromPlayer;
    }

    public void update() {
        y += speed;

        if (y < 0 || y > 600) {
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


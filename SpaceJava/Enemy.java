package SpaceJava;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;


public class Enemy {

    public int x, y;
    private int speed = 2;
    public boolean dead = false;


    public ArrayList<Bullet> bullets = new ArrayList<>();
    private Random random = new Random();

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y += speed;

        // Loop enemy to top after reaching bottom
        if (y > 600) {
            y = -40;
        }

        // Random shooting (1% chance each frame)
        if (random.nextInt(100) == 0) {
            bullets.add(new Bullet(x + 18, y + 40, 5, false));
        }

        // Update bullets
        bullets.removeIf(b -> !b.active);
        for (Bullet b : bullets) {
            b.update();
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.red);
        g.fillRect(x, y, 40, 40);

        for (Bullet b : bullets) {
            b.draw(g);
        }
    }
}

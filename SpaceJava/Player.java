package SpaceJava;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;


public class Player {

    private int x, y;
    private int speed = 6;

    public int lives = 3;
    public boolean alive = true;

    public int getX(){
        return x;
    }

    public int getY() {
        return y;
    }

    private boolean left, right, up, down, shooting;

    public ArrayList<Bullet> bullets = new ArrayList<>();
    private int shootCooldown = 0;
    Sound shootSound = new Sound("src/SpaceJava/Sound Files/Laser_Gun_Sound_Effect.wav");

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void respawn() {
        x = 400;
        y = 500;
        alive = true;
    }

    public void update() {

        // Movement
        if (left) x -= speed;
        if (right) x += speed;
        if (up) y -= speed;
        if (down) y += speed;

        // Limit movement inside screen
        x = Math.max(0, Math.min(x, 760));
        y = Math.max(0, Math.min(y, 560));

        // Shooting rate limiter (cooldown)
        if (shootCooldown > 0)
            shootCooldown--;

        if (shooting && shootCooldown == 0) {
            bullets.add(new Bullet(x + 18, y, -10, true));
            shootSound.play();
            shootCooldown = 15;
        }

        // Update all bullets
        bullets.removeIf(b -> !b.active);
        for (Bullet b : bullets) {
            b.update();
        }
    }

    public void draw(Graphics g) {
        if (!alive) return;

        g.setColor(Color.cyan);
        g.fillRect(x, y, 40, 40);

        for (Bullet b : bullets) {
            b.draw(g);
        }
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> left = true;
            case KeyEvent.VK_RIGHT -> right = true;
            case KeyEvent.VK_UP -> up = true;
            case KeyEvent.VK_DOWN -> down = true;
            case KeyEvent.VK_SPACE -> shooting = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> left = false;
            case KeyEvent.VK_RIGHT -> right = false;
            case KeyEvent.VK_UP -> up = false;
            case KeyEvent.VK_DOWN -> down = false;
            case KeyEvent.VK_SPACE -> shooting = false;
        }
    }
}

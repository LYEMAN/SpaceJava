package SpaceJava;

import java.awt.*;

public class Explosion {

    public int x, y;
    private int timer = 20;     // lasts 20 frames
    public boolean active = true;

    public Explosion(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        timer--;
        if (timer <= 0) {
            active = false;
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.orange);
        g.fillOval(x - 20, y - 20, 60, 60);

        g.setColor(Color.red);
        g.fillOval(x - 10, y - 10, 40, 40);
    }
}


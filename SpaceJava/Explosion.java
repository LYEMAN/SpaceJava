package SpaceJava;

import java.awt.Color;
import java.awt.Graphics2D;

public class Explosion {
    private int x, y;
    private int life = 20;
    public boolean active = true;

    public Explosion(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        life--;
        if (life <= 0) active = false;
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 100, 0, Math.min(255, life * 12)));
        int size = 40 - life;
        g.fillOval(x - size/2, y - size/2, size*2, size*2);
    }
}
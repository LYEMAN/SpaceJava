package SpaceJava;

import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class Enemy extends Entity {

    public int x, y;
    private int speed = 2;
    public int type;

    private Random random = new Random();
    private int shootCooldown = 0;
    
    private static final int TYPE1_MIN_COOLDOWN = 45;
    private static final int TYPE1_MAX_COOLDOWN = 120;
    private static final int TYPE2_MIN_COOLDOWN = 25;
    private static final int TYPE2_MAX_COOLDOWN = 60;
    private static final int TYPE3_MIN_COOLDOWN = 60;
    private static final int TYPE3_MAX_COOLDOWN = 120;

    private float engineTick = 0;

    public Enemy(int x, int y) {
        super(1);
        this.x = x;
        this.y = y;
        int rand = random.nextInt(100);
        if (rand < 60) {
            this.type = 1;
        } else if (rand < 80) {
            this.type = 2;
        } else {
            this.type = 3;
        }
        shootCooldown = random.nextInt(getMaxCooldownForType());
    }

    public void update() {
        y += speed;
        if (y > 600) y = -40;

        engineTick += 0.2f;

        if (shootCooldown > 0) {
            shootCooldown--;
        } else {
            shoot();
            resetCooldown();
        }

        updateBullets();
    }

    public void increaseHealth(int amount) {
        setHealth(getHealth() + amount);
    }

    public boolean isDead() {
        return getHealth() <= 0;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform old = g2.getTransform();
        g2.translate(x, y);

        switch (type) {
            case 1 -> drawInterceptor(g2);
            case 2 -> drawScout(g2);
            case 3 -> drawHeavy(g2);
        }

        g2.setTransform(old);
        drawBullets(g);
    }

    private void drawInterceptor(Graphics2D g2) {
        GradientPaint bodyPaint = new GradientPaint(0, 0, new Color(180, 50, 50), 40, 40, new Color(100, 20, 20));
        g2.setPaint(bodyPaint);
        
        int[] xPoints = {20, 5, 20, 35};
        int[] yPoints = {40, 10, 0, 10}; 
        g2.fillPolygon(xPoints, yPoints, 4);

        g2.setColor(new Color(120, 30, 30));
        g2.fillPolygon(new int[]{5, -5, 5}, new int[]{10, 20, 30}, 3);
        g2.fillPolygon(new int[]{35, 45, 35}, new int[]{10, 20, 30}, 3);

        drawEngineGlow(g2, 20, 35, Color.ORANGE);
        g2.setColor(new Color(255, 200, 200));
        g2.fillOval(15, 15, 10, 10);
    }

    private void drawScout(Graphics2D g2) {
        GradientPaint bodyPaint = new GradientPaint(0, 0, new Color(50, 120, 220), 40, 40, new Color(20, 60, 140));
        g2.setPaint(bodyPaint);
        
        g2.fillOval(0, 5, 40, 20);
        g2.setColor(new Color(30, 80, 160));
        g2.fillOval(10, 0, 20, 30); 

        drawEngineGlow(g2, 20, 25, Color.CYAN);
        g2.setColor(Color.WHITE);
        g2.fillOval(2, 12, 3, 3);
        g2.fillOval(35, 12, 3, 3);
    }

    private void drawHeavy(Graphics2D g2) {
        GradientPaint bodyPaint = new GradientPaint(0, 0, new Color(60, 180, 100), 40, 40, new Color(20, 80, 40));
        g2.setPaint(bodyPaint);
        
        g2.fillRoundRect(10, 0, 20, 40, 5, 5);
        g2.setColor(new Color(40, 140, 70));
        g2.fillRect(0, 10, 10, 20);
        g2.fillRect(30, 10, 10, 20);

        drawEngineGlow(g2, 15, 38, Color.MAGENTA);
        drawEngineGlow(g2, 25, 38, Color.MAGENTA);
        g2.setColor(new Color(255, 255, 0, 150));
        g2.fillRect(15, 10, 10, 5);
    }

    private void drawEngineGlow(Graphics2D g2, int ex, int ey, Color c) {
        float flicker = (float) (Math.sin(engineTick) * 0.2 + 0.8);
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
        int w = (int)(10 * flicker);
        int h = (int)(15 * flicker);
        g2.fillOval(ex - w/2, ey, w, h);
        g2.setColor(Color.WHITE);
        g2.fillOval(ex - 3, ey + 2, 6, 8);
    }

    private void shoot() {
        int centerX = x + 18;
        int centerY = y + 40;
        int bulletSpeed = 5;

        switch (type) {
            case 1, 2 -> addBullet(new Bullet(centerX, centerY, bulletSpeed, true));
            case 3 -> {
                double spread = Math.PI / 12;
                addBullet(new Bullet(centerX, centerY, 0, bulletSpeed, true));
                addBullet(new Bullet(centerX, centerY,
                        Math.sin(-spread) * bulletSpeed,
                        Math.cos(-spread) * bulletSpeed,
                        true));
                addBullet(new Bullet(centerX, centerY,
                        Math.sin(spread) * bulletSpeed,
                        Math.cos(spread) * bulletSpeed,
                        true));
            }
        }
    }

    private void resetCooldown() {
        int min, max;
        switch (type) {
            case 1 -> { min = TYPE1_MIN_COOLDOWN; max = TYPE1_MAX_COOLDOWN; }
            case 2 -> { min = TYPE2_MIN_COOLDOWN; max = TYPE2_MAX_COOLDOWN; }
            case 3 -> { min = TYPE3_MIN_COOLDOWN; max = TYPE3_MAX_COOLDOWN; }
            default -> { min = TYPE1_MIN_COOLDOWN; max = TYPE1_MAX_COOLDOWN; }
        }
        shootCooldown = min + random.nextInt(max - min);
    }

    private int getMaxCooldownForType() {
        return switch (type) {
            case 1 -> TYPE1_MAX_COOLDOWN;
            case 2 -> TYPE2_MAX_COOLDOWN;
            case 3 -> TYPE3_MAX_COOLDOWN;
            default -> TYPE1_MAX_COOLDOWN;
        };
    }
}
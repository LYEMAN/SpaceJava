package SpaceJava;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends Entity {

    private int x, y;
    public int speed = 6;

    public int lives = 1;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    private boolean left, right, up, down, shooting;

    private int shootCooldown = 0;
    Sound shootSound = new Sound("Sound Files/Laser_Gun_Sound_Effect.wav");

    // Power-up system - permanent
    public int multiShotLevel = 0; // 0 = single shot, 1+ = multi-shot with spread
    private static final int MAX_MULTI_SHOT_LEVEL = 20; // Maximum bullets

    // Placeholder power-up states (for you to implement later)
    public int healthRegenLvl = 0;
    private static final int MAX_REG_LEVEL = 3;
    private int healthRegenTimer = 0;
    private static final int REGEN_INTERVAL = 600; // 10 seconds at 60 FPd
    public int atkSpeedLvl = 0;
    private static final int MAX_ATKSPD_LVL = 10;
    public int speedlvl = 0;
    private static final int MAX_SPD_LVL = 4;
    public int ShldLvl = 0;
    private static final int MAX_SHLD_LVL = 3;
    private double shieldAngle = 0; // Rotation angle for orbiting shields
    private static final double SHIELD_ORBIT_RADIUS = 60; // Distance from player center
    private static final int SHIELD_SIZE = 40; // Size of each shield circle

    public Player(int x, int y) {
        super(5); // Initial health of 3
        this.x = x;
        this.y = y;
    }

    public void respawn() {
        x = 400;
        y = 500;
        setAlive(true);
    }

    public void activateMultiShot() {
        // Permanent power-up: increase level
        if (multiShotLevel < MAX_MULTI_SHOT_LEVEL) {
            multiShotLevel++;
        }
    }

    public boolean isMultiShotActive() {
        return multiShotLevel > 0;
    }

    public void activateHPReg() {
        if (healthRegenLvl < MAX_REG_LEVEL) {
            healthRegenLvl++;
        }
    }

    public void activateAtkSpd() {
        // powerUp3Active = true;
        if (atkSpeedLvl < MAX_ATKSPD_LVL) {
            atkSpeedLvl++;
        }

    }

    public void activateSpd() {
        if (atkSpeedLvl < MAX_SPD_LVL) {
            speedlvl++;
            speed = speed + speedlvl;
        }
    }

    public void activateShld() {
        if (ShldLvl < MAX_SHLD_LVL) {
            ShldLvl++;
        }
    }

    public void update() {

        // Update shield rotation
        if (ShldLvl > 0) {
            shieldAngle += 0.05; // Rotation speed
            if (shieldAngle > Math.PI * 2) {
                shieldAngle -= Math.PI * 2;
            }
        }

        // Movement
        if (left)
            x -= speed;
        if (right)
            x += speed;
        if (up)
            y -= speed;
        if (down)
            y += speed;

        // Limit movement inside screen
        x = Math.max(0, Math.min(x, 760));
        y = Math.max(0, Math.min(y, 560));

        // Shooting rate limiter (cooldown)
        if (shootCooldown > 0)
            shootCooldown--;

        // Health regeneration based on level
        if (healthRegenLvl > 0 && healthRegenLvl <= MAX_REG_LEVEL) {
            if (healthRegenTimer > 0) {
                healthRegenTimer--;
            } else {

                if (getHealth() < 3) {
                    heal(1);
                }

                switch (healthRegenLvl) {
                    case 1:
                        healthRegenTimer = REGEN_INTERVAL;
                        break;
                    case 2:
                        healthRegenTimer = REGEN_INTERVAL / 2;
                        break;
                    case 3:
                        healthRegenTimer = REGEN_INTERVAL / 5;
                        break;
                }
            }
        }

        if (shooting && shootCooldown == 0) {
            if (multiShotLevel > 0) {
                // Multi-shot with spread pattern
                int bulletCount = multiShotLevel + 1; // Level 1 = 2 bullets, Level 2 = 3 bullets, etc.
                double baseSpeed = -10.0; // Upward speed
                double maxSpreadAngle = Math.PI / 4; // 45 degrees max spread

                if (bulletCount == 1) {
                    // Single center shot (shouldn't happen with multiShotLevel > 0, but just in
                    // case)
                    addBullet(new Bullet(x + 18, y, 0, baseSpeed, true));
                } else {
                    // Spread pattern: create a fan of bullets
                    if (bulletCount % 2 == 1) {
                        // Odd number: center bullet + symmetric spread
                        addBullet(new Bullet(x + 18, y, 0, baseSpeed, true)); // Center
                        int sideBullets = (bulletCount - 1) / 2;
                        double angleStep = maxSpreadAngle / (sideBullets + 1);
                        for (int i = 1; i <= sideBullets; i++) {
                            // Left side
                            double angle = -angleStep * i;
                            double velX = Math.sin(angle) * Math.abs(baseSpeed);
                            double velY = Math.cos(angle) * baseSpeed;
                            addBullet(new Bullet(x + 18, y, velX, velY, true));
                            // Right side
                            angle = angleStep * i;
                            velX = Math.sin(angle) * Math.abs(baseSpeed);
                            velY = Math.cos(angle) * baseSpeed;
                            addBullet(new Bullet(x + 18, y, velX, velY, true));
                        }
                    } else {
                        // Even number: symmetric spread without center
                        int sideBullets = bulletCount / 2;
                        double angleStep = maxSpreadAngle / (sideBullets + 1);
                        for (int i = 1; i <= sideBullets; i++) {
                            // Left side
                            double angle = -angleStep * i;
                            double velX = Math.sin(angle) * Math.abs(baseSpeed);
                            double velY = Math.cos(angle) * baseSpeed;
                            addBullet(new Bullet(x + 18, y, velX, velY, true));
                            // Right side
                            angle = angleStep * i;
                            velX = Math.sin(angle) * Math.abs(baseSpeed);
                            velY = Math.cos(angle) * baseSpeed;
                            addBullet(new Bullet(x + 18, y, velX, velY, true));
                        }
                    }
                }
            } else {
                // Normal single shot
                addBullet(new Bullet(x + 18, y, -10, true));
            }
            shootSound.play();

            shootCooldown = (atkSpeedLvl == 0) ? 15 : 14 / atkSpeedLvl;
        }

        // Update all bullets
        updateBullets();
    }

    public void draw(Graphics g) {
        if (!isAlive())
            return;

        Graphics2D g2 = (Graphics2D) g;

        // --- SHIP BODY ---
        g2.setColor(new Color(80, 200, 255)); // Light blue hull
        int[] bodyX = { x + 20, x + 5, x + 35 };
        int[] bodyY = { y, y + 35, y + 35 };
        g2.fillPolygon(bodyX, bodyY, 3);

        // --- COCKPIT ---
        g2.setColor(Color.WHITE);
        g2.fillOval(x + 15, y + 12, 10, 10);

        // --- WINGS ---
        g2.setColor(new Color(50, 150, 220));
        g2.fillRect(x + 2, y + 20, 10, 12); // Left wing
        g2.fillRect(x + 28, y + 20, 10, 12); // Right wing

        // --- ENGINE FLAME ---
        g2.setColor(Color.ORANGE);
        g2.fillOval(x + 16, y + 35, 8, 6);

        g2.setColor(Color.RED);
        g2.fillOval(x + 18, y + 38, 4, 6);

        // --- OUTLINE ---
        g2.setColor(Color.BLACK);
        g2.drawPolygon(bodyX, bodyY, 3);

        // --- SHIELDS ---
        if (ShldLvl > 0) {
            drawShields(g);
        }

        // --- BULLETS ---
        drawBullets(g);
    }

    private void drawShields(Graphics g) {
        int centerX = x + 20; // Player center X
        int centerY = y + 20; // Player center Y

        g.setColor(Color.CYAN);
        for (int i = 0; i < ShldLvl; i++) {
            // Calculate position for each shield circle
            double angle = shieldAngle + (i * (Math.PI * 2 / ShldLvl));
            int shieldX = (int) (centerX + Math.cos(angle) * SHIELD_ORBIT_RADIUS - SHIELD_SIZE / 2);
            int shieldY = (int) (centerY + Math.sin(angle) * SHIELD_ORBIT_RADIUS - SHIELD_SIZE / 2);

            // Draw shield circle
            g.fillOval(shieldX, shieldY, SHIELD_SIZE, SHIELD_SIZE);
            // Draw border for visibility
            g.setColor(Color.WHITE);
            g.drawOval(shieldX, shieldY, SHIELD_SIZE, SHIELD_SIZE);
            g.setColor(Color.CYAN);
        }
    }

    public java.util.List<java.awt.Rectangle> getShieldBounds() {
        java.util.List<java.awt.Rectangle> shields = new java.util.ArrayList<>();
        if (ShldLvl > 0) {
            int centerX = x + 20;
            int centerY = y + 20;

            for (int i = 0; i < ShldLvl; i++) {
                double angle = shieldAngle + (i * (Math.PI * 2 / ShldLvl));
                int shieldX = (int) (centerX + Math.cos(angle) * SHIELD_ORBIT_RADIUS - SHIELD_SIZE / 2);
                int shieldY = (int) (centerY + Math.sin(angle) * SHIELD_ORBIT_RADIUS - SHIELD_SIZE / 2);
                shields.add(new java.awt.Rectangle(shieldX, shieldY, SHIELD_SIZE, SHIELD_SIZE));
            }
        }
        return shields;
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

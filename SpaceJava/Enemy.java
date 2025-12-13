package SpaceJava;

import java.awt.*;
import java.util.Random;


public class Enemy extends Entity {

    public int x, y;
    private int speed = 2;
    public int type;

    private Random random = new Random();
    private int shootCooldown = 0;
    // Cooldown values by type
    private static final int TYPE1_MIN_COOLDOWN = 45; // Type 1: Normal speed
    private static final int TYPE1_MAX_COOLDOWN = 120;
    private static final int TYPE2_MIN_COOLDOWN = 25; // Type 2: Faster shooting
    private static final int TYPE2_MAX_COOLDOWN = 60;
    private static final int TYPE3_MIN_COOLDOWN = 60; // Type 3: Triple shot (slower rate)
    private static final int TYPE3_MAX_COOLDOWN = 120;

    public Enemy(int x, int y) {
        super(1); // Enemies have 1 health
        this.x = x;
        this.y = y;
        // Weighted random type assignment: Type 1 = 60%, Type 2 = 20%, Type 3 = 20%
        int rand = random.nextInt(100);
        if (rand < 60) {
            this.type = 1; // 60% chance
        } else if (rand < 80) {
            this.type = 2; // 20% chance (60-80)
        } else {
            this.type = 3; // 20% chance (80-100)
        }
        // Random initial cooldown so enemies don't all shoot at once
        int maxCooldown = getMaxCooldownForType();
        this.shootCooldown = random.nextInt(maxCooldown);
    }

    public void update() {
        y += speed;

        // Loop enemy to top after reaching bottom
        if (y > 600) {
            y = -40;
        }

        // Shooting with cooldown
        if (shootCooldown > 0) {
            shootCooldown--;
        } else {
            // Shoot based on enemy type
            shoot();
            // Reset cooldown based on type
            resetCooldown();
        }

        // Update bullets
        updateBullets();
    } 

    public void increaseHealth(int amount) {
    setHealth(getHealth() + amount);
    }

    public void draw(Graphics g) { 
        switch (type) {
            case 1: 
                g.setColor(Color.red);
                g.fillRect(x, y, 40, 40);
                break; 
            case 2: 
                g.setColor(Color.blue);
                g.fillRect(x, y, 40, 40);
                break;  
            case 3: 
                g.setColor(Color.green);
                g.fillRect(x, y, 40, 40);
                break;
            default:
                g.setColor(Color.red);
                g.fillRect(x, y, 40, 40);
                break;
        }
        
        // Draw bullets for all types
        drawBullets(g);
    }
    
    private void shoot() {
        int centerX = x + 18;
        int centerY = y + 40;
        int bulletSpeed = 5;
        
        switch (type) {
            case 1:
                // Type 1: Single shot
                addBullet(new Bullet(centerX, centerY, bulletSpeed, false));
                break;
            case 2:
                // Type 2: Single shot (but shoots faster due to cooldown)
                addBullet(new Bullet(centerX, centerY, bulletSpeed, false));
                break;
            case 3:
                // Type 3: Triple shot spread
                double spreadAngle = Math.PI / 12; // 15 degrees spread
                // Center bullet
                addBullet(new Bullet(centerX, centerY, 0, bulletSpeed, false));
                // Left bullet
                double leftAngle = -spreadAngle;
                double leftVelX = Math.sin(leftAngle) * bulletSpeed;
                double leftVelY = Math.cos(leftAngle) * bulletSpeed;
                addBullet(new Bullet(centerX, centerY, leftVelX, leftVelY, false));
                // Right bullet
                double rightAngle = spreadAngle;
                double rightVelX = Math.sin(rightAngle) * bulletSpeed;
                double rightVelY = Math.cos(rightAngle) * bulletSpeed;
                addBullet(new Bullet(centerX, centerY, rightVelX, rightVelY, false));
                break;
        }
    }
    
    private void resetCooldown() {
        int minCooldown, maxCooldown;
        switch (type) {
            case 1:
                minCooldown = TYPE1_MIN_COOLDOWN;
                maxCooldown = TYPE1_MAX_COOLDOWN;
                break;
            case 2:
                minCooldown = TYPE2_MIN_COOLDOWN;
                maxCooldown = TYPE2_MAX_COOLDOWN;
                break;
            case 3:
                minCooldown = TYPE3_MIN_COOLDOWN;
                maxCooldown = TYPE3_MAX_COOLDOWN;
                break;
            default:
                minCooldown = TYPE1_MIN_COOLDOWN;
                maxCooldown = TYPE1_MAX_COOLDOWN;
                break;
        }
        shootCooldown = minCooldown + random.nextInt(maxCooldown - minCooldown);
    }
    
    private int getMaxCooldownForType() {
        switch (type) {
            case 1:
                return TYPE1_MAX_COOLDOWN;
            case 2:
                return TYPE2_MAX_COOLDOWN;
            case 3:
                return TYPE3_MAX_COOLDOWN;
            default:
                return TYPE1_MAX_COOLDOWN;
        }
    }
}

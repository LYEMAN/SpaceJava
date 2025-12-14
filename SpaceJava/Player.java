package SpaceJava;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.ArrayList;

public class Player extends Entity {

    // Made public so GamePanel can access them directly if needed
    public int x, y; 
    private final int width = 40;
    private final int height = 40;
    public int speed = 6;
    public int lives = 1;

    // Input flags for GamePanel
    private boolean left, right, up, down;
    
    private int shootCooldown = 0;
    Sound shootSound = new Sound("Sound Files/Laser_Gun_Sound_Effect.wav");

    // Power-ups
    public int multiShotLevel = 0;
    private static final int MAX_MULTI_SHOT_LEVEL = 20;
    public int healthRegenLvl = 0;
    private static final int MAX_REG_LEVEL = 3;
    private int healthRegenTimer = 0;
    private static final int REGEN_INTERVAL = 600;
    public int atkSpeedLvl = 0;
    private static final int MAX_ATKSPD_LVL = 10;
    public int speedLvl = 0;
    private static final int MAX_SPD_LVL = 4;
    public int ShldLvl = 0;
    private static final int MAX_SHLD_LVL = 3;

    // Visuals
    private double shieldAngle = 0;
    private static final double SHIELD_ORBIT_RADIUS = 50;
    private float bankAngle = 0; 
    private float engineTick = 0;
    private boolean hitEffect = false;
    private int hitTimer = 0;
    private static final int HIT_DURATION = 15;

    public Player(int x, int y) {
        super(5); 
        this.x = x;
        this.y = y;
    }

    // --- Input Setters for GamePanel ---
    public void setLeft(boolean b) { left = b; }
    public void setRight(boolean b) { right = b; }
    public void setUp(boolean b) { up = b; }
    public void setDown(boolean b) { down = b; }
    
    // --- Getters ---
    public int getX() { return x; }
    public int getY() { return y; }

    public void respawn() {
        x = 400;
        y = 500;
        setAlive(true);
        setHealth(5);
        bankAngle = 0;
        hitEffect = false;
        lives = 1;
        speed = 6;
        multiShotLevel = 0;
    }

    // --- Power-up methods ---
    public void activateMultiShot() { if (multiShotLevel < MAX_MULTI_SHOT_LEVEL) multiShotLevel++; }
    public boolean isMultiShotActive() { return multiShotLevel > 0; }
    public void activateHPReg() { if (healthRegenLvl < MAX_REG_LEVEL) healthRegenLvl++; }
    public void activateAtkSpd() { if (atkSpeedLvl < MAX_ATKSPD_LVL) atkSpeedLvl++; }
    public void activateSpd() { if (speedLvl < MAX_SPD_LVL) { speedLvl++; speed += speedLvl; } }
    public void activateShld() { if (ShldLvl < MAX_SHLD_LVL) ShldLvl++; }

    // --- Hit method ---
    public void gotHit(int damage) {
        if (!hitEffect) {
            hitEffect = true;
            hitTimer = HIT_DURATION;
            takeDamage(damage);
            // Decrease shield on hit if active
            if(ShldLvl > 0) ShldLvl--;
        }
    }

    // --- Collision detection helper ---
    public boolean isHitBy(Enemy enemy) {
        Rectangle playerRect = new Rectangle(x, y, width, height);
        Rectangle enemyRect = new Rectangle(enemy.x, enemy.y, 40, 40);

        if (playerRect.intersects(enemyRect)) return true;
        
        // Check enemy bullets against player
        for (Bullet b : enemy.getBullets()) {
            Rectangle bulletRect = new Rectangle(b.x, b.y, 6, 12);
            if (playerRect.intersects(bulletRect)) return true;
        }
        return false;
    }

    public void update() {
        if (hitEffect) {
            hitTimer--;
            if (hitTimer <= 0) hitEffect = false;
        }

        // Animation updates
        engineTick += 0.3f;
        shieldAngle += 0.08;
        if (shieldAngle > Math.PI * 2) shieldAngle -= Math.PI * 2;

        // Banking logic
        float targetBank = 0;
        if (left) targetBank = -20;
        if (right) targetBank = 20;
        bankAngle += (targetBank - bankAngle) * 0.15f;

        // Movement
        if (left) x -= speed;
        if (right) x += speed;
        if (up) y -= speed;
        if (down) y += speed;

        x = Math.max(0, Math.min(x, 760));
        y = Math.max(0, Math.min(y, 560));

        if (shootCooldown > 0) shootCooldown--;

        // Auto Shooting
        if (shootCooldown == 0) shoot();

        updateBullets();
    }

    private void shoot() {
        if (multiShotLevel > 0) {
            int bulletCount = multiShotLevel + 1;
            double baseSpeed = -10.0;
            double maxSpreadAngle = Math.PI / 4;

            if (bulletCount % 2 == 1) addBullet(new Bullet(x + 18, y, 0, baseSpeed, false));

            int sideBullets = bulletCount / 2;
            double angleStep = maxSpreadAngle / (sideBullets + 1);

            for (int i = 1; i <= sideBullets; i++) {
                double angle = angleStep * i;
                double velX = Math.sin(angle) * Math.abs(baseSpeed);
                double velY = Math.cos(angle) * baseSpeed;
                addBullet(new Bullet(x + 18, y, -velX, velY, false));
                addBullet(new Bullet(x + 18, y, velX, velY, false));
            }
        } else {
            addBullet(new Bullet(x + 18, y, -10, false));
        }

        shootSound.play();
        shootCooldown = Math.max(3, 15 - atkSpeedLvl);
    }

    public void draw(Graphics g) {
        if (!isAlive()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform original = g2.getTransform();

        // Position and Rotate
        g2.translate(x + width / 2, y + height / 2); 
        
        if (hitEffect) {
            g2.translate((Math.random() * 8 - 4), (Math.random() * 8 - 4));
        }
        
        g2.rotate(Math.toRadians(bankAngle)); 

        drawEngineFlames(g2);
        drawBody(g2); 
        
        g2.setTransform(original);
        
        drawShields(g2);
        drawBullets(g);
    }

    private void drawEngineFlames(Graphics2D g2) {
        float flicker = (float) (Math.sin(engineTick) * 0.15 + 0.85); 
        int flameH = (int)(25 * flicker);
        
        g2.setColor(new Color(255, 100, 0, 180));
        g2.fillOval(-6, 15, 12, flameH);
        g2.setColor(new Color(255, 200, 50, 200));
        g2.fillOval(-3, 15, 6, flameH - 5);
        
        if(speedLvl > 0) {
            g2.setColor(new Color(50, 200, 255, 150));
            g2.fillOval(-18, 10, 6, 12);
            g2.fillOval(12, 10, 6, 12);
        }
    }

    private void drawBody(Graphics2D g2) {
        if (hitEffect) {
            g2.setColor(Color.WHITE);
            g2.fillPolygon(new int[]{0, -20, 20}, new int[]{-20, 20, 20}, 3);
            return;
        }

        GradientPaint paint = new GradientPaint(0, -20, new Color(200, 200, 220), 0, 20, new Color(60, 60, 80));
        g2.setPaint(paint);
        
        GeneralPath ship = new GeneralPath();
        ship.moveTo(0, -20); 
        ship.lineTo(10, 0);
        ship.lineTo(20, 20); 
        ship.lineTo(5, 15);  
        ship.lineTo(0, 20);  
        ship.lineTo(-5, 15); 
        ship.lineTo(-20, 20);
        ship.lineTo(-10, 0);
        ship.closePath();
        
        g2.fill(ship);
        
        g2.setColor(new Color(50, 150, 255));
        g2.fillPolygon(new int[]{-18, -8, -8}, new int[]{18, 18, 5}, 3);
        g2.fillPolygon(new int[]{18, 8, 8}, new int[]{18, 18, 5}, 3);

        g2.setColor(new Color(20, 20, 40));
        g2.fillOval(-4, -5, 8, 12);
        g2.setColor(new Color(100, 200, 255, 150)); 
        g2.fillOval(-2, -2, 3, 5);
        
        g2.setColor(new Color(20, 20, 50));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(ship);
    }
    
    private void drawShields(Graphics2D g2) {
        if (ShldLvl <= 0) return;
        
        int centerX = x + 20;
        int centerY = y + 20;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        g2.setColor(new Color(0, 255, 255));
        
        for (int i = 0; i < ShldLvl; i++) {
            double angle = shieldAngle + (i * (Math.PI * 2 / ShldLvl));
            int sx = (int)(centerX + Math.cos(angle) * SHIELD_ORBIT_RADIUS);
            int sy = (int)(centerY + Math.sin(angle) * SHIELD_ORBIT_RADIUS);
            
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(sx - 10, sy - 10, 20, 20); 
            
            g2.setStroke(new BasicStroke(1));
            g2.setColor(new Color(0, 200, 255, 50));
            g2.drawLine(centerX, centerY, sx, sy);
            g2.setColor(new Color(0, 255, 255));
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public List<Rectangle> getShieldBounds() {
        List<Rectangle> shields = new ArrayList<>();
        if (ShldLvl <= 0) return shields;

        int centerX = x + 20;
        int centerY = y + 20;
        for (int i = 0; i < ShldLvl; i++) {
            double angle = shieldAngle + (i * (Math.PI * 2 / ShldLvl));
            int shieldX = (int)(centerX + Math.cos(angle) * SHIELD_ORBIT_RADIUS) - 10;
            int shieldY = (int)(centerY + Math.sin(angle) * SHIELD_ORBIT_RADIUS) - 10;
            shields.add(new Rectangle(shieldX, shieldY, 20, 20));
        }
        return shields;
    }
}
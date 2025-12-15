package SpaceJava;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*; 

public class GamePanel extends JPanel implements ActionListener {

    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;

    // Game Constants
    private static final int POWER_UP_MILESTONE = 1000;
    private static final int SPAWN_RATE_MILESTONE = 300;
    private static final int MIN_ENEMIES = 3;
    private static final int MAX_ENEMIES = 50;
    private static final int STAR_COUNT = 180;

    // Background Stars
    private final Point[] stars = new Point[STAR_COUNT];
    private final float[] starSpeeds = new float[STAR_COUNT];

    private final Timer timer;
    private Player player;
    private final ArrayList<Enemy> enemies;
    private final ArrayList<Explosion> explosions;
    private final ArrayList<PowerUp> powerUps;

    // Game State
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private boolean powerUpMenuShowing = false;

    private int respawnTimer = 0;
    private int score = 0;
    private int lastPowerUpMilestone = 0;
    private int lastSpawnRateMilestone = 0;
    private int enemySpawnCooldown = 0;
    private int targetEnemyCount = MIN_ENEMIES;
    private int baseSpawnCooldownMin = 30;
    private int baseSpawnCooldownMax = 90;

    private final PowerUpType[] menuChoices = new PowerUpType[3];

    // Sounds
    private final Sound explosionSound     = new Sound("Sound Files/Spacecraft_Explosion_Blow_Up_Sound_Effect.wav");
    private final Sound gameStartSound     = new Sound("Sound Files/Game_Start_Sound_Effect.wav");
    private final Sound gameOverSound      = new Sound("Sound Files/Game_Over_Sound_Effect.wav");
    private final Sound powerUpSound       = new Sound("Sound Files/PowerUp_Sound_Effect.wav");
    private final Sound choosePowerUpSound = new Sound("Sound Files/Choose_Power_Up_Sound_Effect.wav");

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setDoubleBuffered(true);

        player = new Player(400, 500);
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        powerUps = new ArrayList<>();

        initStarField();

        for (int i = 0; i < MIN_ENEMIES; i++) {
            spawnRandomEnemy();
        }

        setupKeyBindings();

        timer = new Timer(16, this); // ~60 FPS
        timer.setInitialDelay(0);
        timer.start();
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // Game Control Keys
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startGame");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "restartGame");
        
        am.put("startGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameStarted) {
                    gameStarted = true;
                    gameStartSound.play();
                }
            }
        });
        
        am.put("restartGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameOver) restartGame();
            }
        });

        // Movement Keys
        im.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
        im.put(KeyStroke.getKeyStroke("UP"), "moveUp");
        im.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        im.put(KeyStroke.getKeyStroke("released LEFT"), "stopLeft");
        im.put(KeyStroke.getKeyStroke("released RIGHT"), "stopRight");
        im.put(KeyStroke.getKeyStroke("released UP"), "stopUp");
        im.put(KeyStroke.getKeyStroke("released DOWN"), "stopDown");

        am.put("moveLeft", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setLeft(true); } });
        am.put("moveRight", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setRight(true); } });
        am.put("moveUp", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setUp(true); } });
        am.put("moveDown", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setDown(true); } });
        am.put("stopLeft", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setLeft(false); } });
        am.put("stopRight", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setRight(false); } });
        am.put("stopUp", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setUp(false); } });
        am.put("stopDown", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setDown(false); } });

        // Power Up Selection Keys (1, 2, 3)
        for (int i = 0; i < 3; i++) {
            final int index = i;
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, 0), "powerUp" + i);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1 + i, 0), "powerUp" + i);
            am.put("powerUp" + i, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (powerUpMenuShowing && menuChoices[index] != null) {
                        applyPowerUp(menuChoices[index]);
                        powerUpMenuShowing = false;
                    }
                }
            });
        }
    }

    private void initStarField() {
        for (int i = 0; i < STAR_COUNT; i++) {
            stars[i] = new Point((int) (Math.random() * PANEL_WIDTH), (int) (Math.random() * PANEL_HEIGHT));
            starSpeeds[i] = 0.4f + (float) Math.random() * 1.6f;
        }
    }

    private void updateStarField() {
        for (int i = 0; i < STAR_COUNT; i++) {
            Point star = stars[i];
            star.y += starSpeeds[i];
            if (star.y > PANEL_HEIGHT) {
                star.x = (int) (Math.random() * PANEL_WIDTH);
                star.y = -10 - (int) (Math.random() * 40);
                starSpeeds[i] = 0.4f + (float) Math.random() * 1.6f;
            }
        }
    }

    private void spawnRandomEnemy() {
        int randomX = 40 + (int) (Math.random() * (PANEL_WIDTH - 120));
        int randomY = -40 - (int) (Math.random() * 100);
        Enemy enemy = new Enemy(randomX, randomY);
        enemy.increaseHealth(score / 2000);

        enemies.add(enemy);
    }

    private void increaseSpawnRate() {
        baseSpawnCooldownMin = Math.max(1, baseSpawnCooldownMin - 3);
        baseSpawnCooldownMax = Math.max(15, baseSpawnCooldownMax - 5);
    }

    public void killPlayer() {
        player.setAlive(false);
        explosions.add(new Explosion(player.getX(), player.getY()));

        // Reduce lives
        player.lives--;

        // Check if Game Over
        if (player.lives <= 0) {
            gameOver = true;
            gameOverSound.play();
        } else {
            // Respawn after delay
            respawnTimer = 60;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateStarField();

        if (!gameStarted || powerUpMenuShowing) {
            repaint();
            return;
        }

        if (!gameOver) {
            if (player.isAlive()) {
                player.update();
            } else {
                respawnTimer--;
                if (respawnTimer <= 0 && !gameOver) player.respawn();
            }

            for (Enemy enemy : enemies) enemy.update();
            
            handleCollisions();
            handleEnemySpawning();
            updateExplosions();
            updatePowerUps();
        }

        repaint();
    }

    private void handleCollisions() {
        Rectangle playerRect = new Rectangle(player.getX(), player.getY(), 40, 40);

        // 1. Player Bullets hitting Enemies
        for (Enemy enemy : enemies) {
            Rectangle enemyRect = new Rectangle(enemy.x, enemy.y, 40, 40);

            for (Bullet pb : player.getBullets()) {
                if (!pb.active) continue;
                Rectangle bulletRect = new Rectangle((int)pb.x, (int)pb.y, 6, 12);
                
                if (bulletRect.intersects(enemyRect)) {
                    pb.active = false;
                    enemy.takeDamage(1);
                    explosions.add(new Explosion(enemy.x, enemy.y));
                    explosionSound.play();

                    if (enemy.getHealth() <= 0) score += 100;

                    // Power Up Logic
                    int milestone = score / POWER_UP_MILESTONE;
                    if (score > 5000 ? milestone >= lastPowerUpMilestone + 3 : milestone > lastPowerUpMilestone) {
                        lastPowerUpMilestone = milestone;
                        showPowerUpMenu();
                    }

                    // Spawn Rate Logic
                    int spawnMilestone = score / SPAWN_RATE_MILESTONE;
                    if (spawnMilestone > lastSpawnRateMilestone) {
                        lastSpawnRateMilestone = spawnMilestone;
                        increaseSpawnRate();
                    }
                }
            }

            // 2. Player Body hitting Enemy Body
            if (player.isAlive() && playerRect.intersects(enemyRect)) {
                 player.gotHit(1);
                 enemy.takeDamage(100); 
                 explosions.add(new Explosion(enemy.x, enemy.y));
                 if (player.getHealth() <= 0) killPlayer();
            }
        }

        // Cleanup Dead Enemies
        enemies.removeIf(Enemy::isDead);

        // 3. Enemy Bullets hitting Player
        for (Enemy enemy : enemies) {
            for (Bullet eb : enemy.getBullets()) {
                if (!eb.active) continue;
                Rectangle bulletRect = new Rectangle((int)eb.x, (int)eb.y, 6, 12);

                boolean blocked = false;
                // Check Shields
                if (player.ShldLvl > 0) {
                    for (Rectangle shieldRect : player.getShieldBounds()) {
                        if (bulletRect.intersects(shieldRect)) {
                            eb.active = false;
                            blocked = true;
                            break;
                        }
                    }
                }

                // Check Hull
                if (!blocked && player.isAlive() && bulletRect.intersects(playerRect)) {
                    eb.active = false;
                    player.gotHit(1);
                    if (player.getHealth() <= 0) killPlayer();
                }
            }
        }
    }

    private void handleEnemySpawning() {
        if (enemySpawnCooldown > 0) enemySpawnCooldown--;

        if (Math.random() < 0.01) {
            targetEnemyCount = MIN_ENEMIES + (int) (Math.random() * (MAX_ENEMIES - MIN_ENEMIES + 1));
        }

        if (enemies.size() < targetEnemyCount && enemySpawnCooldown <= 0) {
            spawnRandomEnemy();
            int cooldownRange = Math.max(5, baseSpawnCooldownMax - baseSpawnCooldownMin); 
            enemySpawnCooldown = baseSpawnCooldownMin + (int) (Math.random() * cooldownRange); 

        }
    }

    private void updateExplosions() {
        explosions.removeIf(ex -> !ex.active);
        for (Explosion ex : explosions) ex.update();
    }

    private void updatePowerUps() {
        Rectangle playerRect = new Rectangle(player.getX(), player.getY(), 40, 40);

        for (PowerUp powerUp : powerUps) {
            powerUp.update();
            
            if (player.isAlive() && powerUp.getBounds().intersects(playerRect)) {
                
                applyPowerUp(powerUp.getType());
                
                powerUpSound.play();
                
                powerUp.active = false;
            }
        }
        powerUps.removeIf(pu -> !pu.active);
    }

    private void showPowerUpMenu() {
        choosePowerUpSound.play();
        PowerUpType[] all = PowerUpType.values();
        List<PowerUpType> list = new ArrayList<>();
        Collections.addAll(list, all);
        Collections.shuffle(list);

        for (int i = 0; i < 3 && i < list.size(); i++) {
            menuChoices[i] = list.get(i);
        }
        powerUpMenuShowing = true;
    }

    private void applyPowerUp(PowerUpType type) {
        powerUpSound.play();
        switch (type) {
            case MULTI_SHOT -> player.activateMultiShot();
            case POWER_UP_2 -> player.activateHPReg();
            case POWER_UP_3 -> player.activateAtkSpd();
            case POWER_UP_4 -> player.activateSpd();
            case POWER_UP_5 -> player.activateShld();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Gradient Background
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(10, 10, 35),
                0, PANEL_HEIGHT, new Color(5, 5, 15)
        );
        g2.setPaint(bg);
        g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        // Draw Stars
        g2.setColor(new Color(255, 255, 255, 90));
        for (Point star : stars) if (star != null) g2.fillOval(star.x, star.y, 2, 2);

        if (!gameStarted) { drawStartScreen(g2); g2.dispose(); return; }

        // Draw Entities
        if (player != null) player.draw(g2);
        for (Enemy e : enemies) e.draw(g2);
        for (Explosion ex : explosions) ex.draw(g2);
        for (PowerUp pu : powerUps) pu.draw(g2);

        // Draw UI Layers
        drawHud(g2);

        if (player != null && player.isMultiShotActive()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.setColor(new Color(120, 255, 180));
            g2.drawString("MULTI-SHOT x" + (player.multiShotLevel + 1), 25, 130);
        }

        if (powerUpMenuShowing && !gameOver) drawPowerUpMenu(g2);
        if (gameOver) drawGameOverOverlay(g2);

        g2.dispose();
    }

    private void drawStartScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(140, 170, 520, 240, 30, 30);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 56));
        String title = "JAVA IN SPACE";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (PANEL_WIDTH - tw) / 2, 260);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 26));
        g2.setColor(new Color(180, 220, 255));
        String sub = "Press ENTER to Launch";
        int sw = g2.getFontMetrics().stringWidth(sub);
        g2.drawString(sub, (PANEL_WIDTH - sw) / 2, 320);
    }

    // --- NEW AESTHETIC HUD ---
    private void drawHud(Graphics2D g2) {
        // 1. SCORE DISPLAY (Top Left, Digital Look)
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        String scoreLabel = "SCORE";
        String scoreVal = String.format("%06d", score); // 000500 format
        
        // Glow effect
        g2.setColor(new Color(0, 150, 255, 50)); // Faint glow
        g2.drawString(scoreLabel, 22, 32);
        g2.drawString(scoreVal, 22, 57);

        g2.setColor(new Color(100, 200, 255));
        g2.drawString(scoreLabel, 20, 30);
        g2.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2.setColor(Color.WHITE);
        g2.drawString(scoreVal, 20, 55);

        // 2.HEALTH DISPLAY (Top Right, Segmented Bars)
        if (player != null) {
            int maxHp = Math.max(1, player.getMaxHealth());
            int hp = Math.max(0, player.getHealth());
            
            // Text Label
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            String hullLabel = "HEALTH STATUS";
            int labelWidth = g2.getFontMetrics().stringWidth(hullLabel);
            int rightEdge = PANEL_WIDTH - 20;
            
            g2.setColor(new Color(100, 200, 255));
            g2.drawString(hullLabel, rightEdge - labelWidth, 30);

            // Segmented Health Bar
            int barWidth = 200;
            int barHeight = 15;
            int segmentGap = 4;
            int segmentCount = maxHp;
            int segmentWidth = (barWidth - (segmentGap * (segmentCount - 1))) / segmentCount;
            
            int startX = rightEdge - barWidth;
            int startY = 40;

            for (int i = 0; i < segmentCount; i++) {
                // Determine color based on health remaining
                if (i < hp) {
                    float hpPercent = (float)hp / maxHp;
                    if (hpPercent > 0.6) g2.setColor(new Color(0, 255, 200)); // Cyan/Green
                    else if (hpPercent > 0.3) g2.setColor(new Color(255, 180, 0)); // Orange
                    else g2.setColor(new Color(255, 50, 50)); // Red
                    
                    // Add glowing effect to active segments
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
                    g2.fillRoundRect(startX + (i * (segmentWidth + segmentGap)), startY, segmentWidth, barHeight, 4, 4);
                    
                } else {
                    // Empty Segment (Dark gray)
                    g2.setColor(new Color(50, 50, 60, 150));
                    g2.fillRoundRect(startX + (i * (segmentWidth + segmentGap)), startY, segmentWidth, barHeight, 4, 4);
                }
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Reset
        }
    }

    private void drawPowerUpMenu(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 210));
        g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        int mw = 520, mh = 320;
        int mx = (PANEL_WIDTH - mw) / 2;
        int my = (PANEL_HEIGHT - mh) / 2;

        g2.setColor(new Color(20, 25, 60));
        g2.fillRoundRect(mx, my, mw, mh, 30, 30);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(mx, my, mw, mh, 30, 30);

        g2.setFont(new Font("SansSerif", Font.BOLD, 30));
        g2.setColor(new Color(255, 215, 90));
        g2.drawString("Choose a Power-Up", mx + 90, my + 50);

        int y = my + 110;
        for (int i = 0; i < 3; i++) {
            if (menuChoices[i] == null) continue;

            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillRoundRect(mx + 30, y - 30, mw - 60, 60, 20, 20);

            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            g2.setColor(new Color(120, 220, 255));
            g2.drawString((i + 1) + " ▸ " + menuChoices[i].getName(), mx + 50, y);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g2.setColor(Color.WHITE);
            g2.drawString(menuChoices[i].getDescription(), mx + 50, y + 22);

            y += 70;
        }
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        // Dark Overlay
        g2.setColor(new Color(10, 0, 0, 220));
        g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        // "MISSION FAILED"
        g2.setFont(new Font("Monospaced", Font.BOLD, 60));
        g2.setColor(new Color(255, 50, 50));
        String over = "GAME OVER";
        int ow = g2.getFontMetrics().stringWidth(over);
        g2.drawString(over, (PANEL_WIDTH - ow) / 2, 250);

        // Score
        g2.setFont(new Font("Monospaced", Font.BOLD, 30));
        g2.setColor(Color.WHITE);
        String finalScore = "FINAL SCORE: " + String.format("%,d", score);
        int sw = g2.getFontMetrics().stringWidth(finalScore);
        g2.drawString(finalScore, (PANEL_WIDTH - sw) / 2, 320);

        // Retry Instruction
        g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
        g2.setColor(new Color(200, 200, 200));
        String retry = "Press [SPACE] to Retry";
        int rw = g2.getFontMetrics().stringWidth(retry);
        g2.drawString(retry, (PANEL_WIDTH - rw) / 2, 380);
    }

    private void restartGame() {
        player.respawn();
        player.lives = 1; // Reset lives
        enemies.clear();
        explosions.clear();
        powerUps.clear();

        score = 0;
        lastPowerUpMilestone = 0;
        lastSpawnRateMilestone = 0;
        baseSpawnCooldownMin = 30;
        baseSpawnCooldownMax = 90;
        targetEnemyCount = MIN_ENEMIES;
        enemySpawnCooldown = 0;

        powerUpMenuShowing = false;
        gameOver = false;
        gameStarted = true;

        initStarField();
        requestFocusInWindow();
    }
}

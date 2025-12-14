package SpaceJava;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {

    private final int PANEL_WIDTH = 800;
    private final int PANEL_HEIGHT = 600;

    boolean gameOver = false;
    int respawnTimer = 0;

    Timer timer;
    Player player;
    ArrayList<Enemy> enemies;
    ArrayList<Explosion> explosions;
    // new
    ArrayList<PowerUp> powerUps;

    boolean gameStarted = false;

    int score = 0;
    private int lastPowerUpMilestone = 0; // Track last milestone for power-up menu
    private static final int POWER_UP_MILESTONE = 500; // Show menu every 1500 points
    private int lastSpawnRateMilestone = 0; // Track last milestone for spawn rate increase
    private static final int SPAWN_RATE_MILESTONE = 300; // Increase spawn rate every 300 points

    // Power-up menu overlay
    private boolean powerUpMenuShowing = false;
    private PowerUpType[] menuChoices = new PowerUpType[3];

    // Enemy spawning variables

    private int enemySpawnCooldown = 0;
    private static final int MIN_ENEMIES = 3;
    private static final int MAX_ENEMIES = 50;
    private int targetEnemyCount = MIN_ENEMIES;
    private int baseSpawnCooldownMin = 30; // Base minimum cooldown
    private int baseSpawnCooldownMax = 90; // Base maximum cooldown

    // Sounds
    Sound explosionSound = new Sound("Sound Files/Spacecraft_Explosion_Blow_Up_Sound_Effect.wav");
    Sound gameStartSound = new Sound("Sound Files/Game_Start_Sound_Effect.wav");
    Sound gameOverSound = new Sound("Sound Files/Game_Over_Sound_Effect.wav");
    Sound powerUpSound = new Sound("Sound Files/PowerUp_Sound_Effect.wav");
    Sound choosePowerUpSound = new Sound("Sound Files/Choose_Power_Up_Sound_Effect.wav");

    public GamePanel() {
        this.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);

        player = new Player(400, 500);
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        // new
        powerUps = new ArrayList<>();

        // Create initial enemies at random positions
        for (int i = 0; i < MIN_ENEMIES; i++) {
            spawnRandomEnemy();
        }

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                // RESTART GAME WITH SPACE AFTER GAME OVER
                if (gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    restartGame();
                    return;
                }

                // START GAME WITH ENTER (initial start)
                if (!gameStarted && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    gameStarted = true;
                    gameStartSound.play();
                }

                // POWER-UP MENU INPUT
                if (powerUpMenuShowing) {
                    int selection = -1;
                    if (e.getKeyCode() == KeyEvent.VK_1 || e.getKeyCode() == KeyEvent.VK_NUMPAD1) {
                        selection = 0;
                    } else if (e.getKeyCode() == KeyEvent.VK_2 || e.getKeyCode() == KeyEvent.VK_NUMPAD2) {
                        selection = 1;
                    } else if (e.getKeyCode() == KeyEvent.VK_3 || e.getKeyCode() == KeyEvent.VK_NUMPAD3) {
                        selection = 2;
                    }

                    if (selection >= 0) {
                        applyPowerUp(menuChoices[selection]);
                        powerUpMenuShowing = false;
                    }
                    return;
                }

                if (gameStarted && !gameOver) {
                    player.keyPressed(e);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (gameStarted && !gameOver) {
                    player.keyReleased(e);
                }
            }
        });

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                player.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.keyReleased(e);
            }
        });

        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    private void spawnRandomEnemy() {
        // Random X position (keeping enemies within screen bounds)
        int randomX = 40 + (int) (Math.random() * (PANEL_WIDTH - 120));
        // Random Y starting position (spawn from top, with some variation)
        int randomY = -40 - (int) (Math.random() * 100); // Spawn above screen with random offset
        Enemy enemy = new Enemy(randomX, randomY);
        int bonusHealth = score / 700;
        int bonusbonus = score / 2000;
        if (score > 5000)
            bonusHealth *= bonusbonus;
        enemy.increaseHealth(bonusHealth);
        enemies.add(enemy);

    }

    private void increaseSpawnRate() {
        // Decrease spawn cooldown (faster spawning)
        baseSpawnCooldownMin = Math.max(1, baseSpawnCooldownMin - 3); // Minimum 1 frames
        baseSpawnCooldownMax = Math.max(15, baseSpawnCooldownMax - 5); // Minimum 15 frames

        // Optionally increase max enemy count
        if (lastSpawnRateMilestone % 2 == 0) { // Every other milestone (every 1400 points)
            // Already at max, but we could increase MAX_ENEMIES if needed
        }
    }

    public void killPlayer() {
        player.setAlive(false);
        // Only decrease health if it's still above 0 (to avoid going negative)
        if (player.getHealth() > 0) {
            player.takeDamage(1);
        }

        explosions.add(new Explosion(player.getX(), player.getY()));

        if (player.getHealth() <= 0) {
            gameOver = true;
            gameOverSound.play();
        } else {
            respawnTimer = 60;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!gameStarted) {
            repaint();
            return;
        }

        // Pause game updates when menu is showing
        if (powerUpMenuShowing) {
            repaint();
            return;
        }

        if (!gameOver) {

            // PLAYER UPDATES
            if (player.isAlive()) {
                player.update();
            } else {
                respawnTimer--;
                if (respawnTimer <= 0) {
                    player.respawn();
                }
            }

            // ENEMY UPDATES
            for (Enemy enemy : enemies) {
                enemy.update();
            }

            // ---------------------------
            // COLLISION CHECKS
            // ---------------------------

            Rectangle playerRect = new Rectangle(player.getX(), player.getY(), 40, 40);

            // PLAYER BULLETS → ENEMIES
            for (Enemy enemy : enemies) {
                Rectangle enemyRect = new Rectangle(enemy.x, enemy.y, 40, 40);

                for (Bullet pb : player.getBullets()) {
                    Rectangle bulletRect = new Rectangle(pb.x, pb.y, 6, 12);

                    if (bulletRect.intersects(enemyRect)) {
                        pb.active = false;
                        enemy.takeDamage(1);

                        // Explosion + Sound
                        explosions.add(new Explosion(enemy.x, enemy.y));
                        explosionSound.play();

                        // Score

                        if (enemy.getHealth() <= 0)
                            score += 100;

                        int currentMilestone = score / POWER_UP_MILESTONE;
                        if (score > 5000) {
                            if (currentMilestone == lastPowerUpMilestone + 3)
                                showPowerUpMenu();
                        } else if (currentMilestone > lastPowerUpMilestone) {
                            lastPowerUpMilestone = currentMilestone;
                            showPowerUpMenu();
                        }

                        int currentSpawnMilestone = score / SPAWN_RATE_MILESTONE;
                        if (currentSpawnMilestone > lastSpawnRateMilestone) {
                            lastSpawnRateMilestone = currentSpawnMilestone;
                            increaseSpawnRate();
                        }
                    }
                }

                // PLAYER COLLIDES WITH ENEMY
                if (player.isAlive() && playerRect.intersects(enemyRect)) {
                    player.takeDamage(1);
                    if (player.getHealth() <= 0) {
                        killPlayer();
                    }

                }
            }

            // Remove dead enemies
            enemies.removeIf(en -> en.isDead());

            // Update spawn timer
            if (enemySpawnCooldown > 0) {
                enemySpawnCooldown--;
            }

            // Randomly adjust target enemy count
            if (Math.random() < 0.01) { // 1% chance per frame
                targetEnemyCount = MIN_ENEMIES + (int) (Math.random() * (MAX_ENEMIES - MIN_ENEMIES + 1));
            }

            // Spawn enemies with random timing
            if (enemies.size() < targetEnemyCount && enemySpawnCooldown <= 0) {
                spawnRandomEnemy();
                // Dynamic cooldown that decreases as score increases
                int cooldownRange = Math.max(5, baseSpawnCooldownMax - baseSpawnCooldownMin); // Minimum 5 frame range
                enemySpawnCooldown = baseSpawnCooldownMin + (int) (Math.random() * cooldownRange);
            }

            // ENEMY BULLETS -> PLAYER
            for (Enemy enemy : enemies) {
                for (Bullet eb : enemy.getBullets()) {
                    Rectangle bulletRect = new Rectangle(eb.x, eb.y, 6, 12);

                    // Check if bullet hits shield first
                    boolean blockedByShield = false;
                    if (player.ShldLvl > 0) {
                        for (Rectangle shieldRect : player.getShieldBounds()) {
                            if (bulletRect.intersects(shieldRect)) {
                                eb.active = false;
                                blockedByShield = true;
                                break;
                            }
                        }
                    }

                    // Only damage player if bullet wasn't blocked by shield
                    if (!blockedByShield && player.isAlive() && bulletRect.intersects(playerRect)) {
                        eb.active = false;
                        // decrease player health when hit with the bullets
                        player.takeDamage(1);
                        if (player.getHealth() <= 0) {
                            killPlayer();
                        }
                    }
                }
            }

            // EXPLOSIONS
            explosions.removeIf(ex -> !ex.active);
            for (Explosion ex : explosions) {
                ex.update();
            }

            // new
            // POWER-UPS
            for (PowerUp powerUp : powerUps) {
                powerUp.update();

                // Check collision with player
                if (player.isAlive() && powerUp.getBounds().intersects(playerRect)) {
                    powerUp.active = false;
                }
            }
            powerUps.removeIf(pu -> !pu.active);
        }

        repaint();
    }

    private void showPowerUpMenu() {
        // Get all power-up types

        choosePowerUpSound.play();

        PowerUpType[] allPowerUps = PowerUpType.values();
        List<PowerUpType> powerUpList = new ArrayList<>();
        for (PowerUpType type : allPowerUps) {
            powerUpList.add(type);
        }

        // Shuffle and pick 3 random power-ups
        Collections.shuffle(powerUpList);
        for (int i = 0; i < 3 && i < powerUpList.size(); i++) {
            menuChoices[i] = powerUpList.get(i);
        }

        // Show menu overlay
        powerUpMenuShowing = true;
    }

    private void applyPowerUp(PowerUpType type) {
        switch (type) {
            case MULTI_SHOT:
                player.activateMultiShot();
                break;
            case POWER_UP_2:
                player.activateHPReg();
                break;
            case POWER_UP_3:
                player.activateAtkSpd();
                break;
            case POWER_UP_4:
                player.activateSpd();
                break;
            case POWER_UP_5:
                player.activateShld();
                break;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // START MENU
        if (!gameStarted) {
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("JAVA IN SPACE", 230, 260);

            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.drawString("Press ENTER to Start", 270, 320);
            return;
        }

        // Draw player
        player.draw(g);

        // Draw enemies
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }

        // Draw explosions
        for (Explosion ex : explosions) {
            ex.draw(g);
        }
        // new
        // Draw power-ups
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g);
        }

        // SCORE
        g.setColor(Color.yellow);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 25);

        // Health
        g.setColor(Color.red);
        g.drawString("Health: " + player.getHealth(), 10, 50);

        // new
        // POWER-UP STATUS
        if (player.isMultiShotActive()) {
            int bulletCount = player.multiShotLevel + 1;
            g.setColor(Color.green);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("MULTI-SHOT x" + bulletCount, 10, 100);
        }

        // POWER-UP MENU OVERLAY
        if (powerUpMenuShowing) {
            // Semi-transparent dark overlay
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

            // Menu box
            int menuX = 150;
            int menuY = 150;
            int menuWidth = 500;
            int menuHeight = 300;

            g.setColor(Color.DARK_GRAY);
            g.fillRect(menuX, menuY, menuWidth, menuHeight);
            g.setColor(Color.WHITE);
            g.drawRect(menuX, menuY, menuWidth, menuHeight);

            // Title
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Choose a Power-Up!", menuX + 80, menuY + 40);

            // Options
            g.setFont(new Font("Arial", Font.BOLD, 20));
            int optionY = menuY + 90;
            int optionSpacing = 70;

            for (int i = 0; i < 3; i++) {
                g.setColor(Color.CYAN);
                g.drawString((i + 1) + ". " + menuChoices[i].getName(), menuX + 50, optionY + (i * optionSpacing));
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 16));
                g.drawString("   " + menuChoices[i].getDescription(), menuX + 50, optionY + (i * optionSpacing) + 25);
                g.setFont(new Font("Arial", Font.BOLD, 20));
            }

            // Instructions
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Press 1, 2, or 3 to select", menuX + 140, menuY + menuHeight + 30);
            powerUpSound.play();
        }

        // GAME OVER
        if (gameOver) {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", 250, 300);

            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.drawString("Score: " + score, 350, 350);

            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.drawString("Space to play again", 300, 450);

        }
    }

    private void restartGame() {
        player = new Player(400, 500);
        player.lives = 1;
        player.setHealth(5);

        enemies.clear();
        explosions.clear();
        powerUps.clear();

        score = 0;

        // 🔥 RESET MILESTONE TRACKERS
        lastPowerUpMilestone = 0;
        lastSpawnRateMilestone = 0;

        // 🔥 RESET SPAWN VALUES
        baseSpawnCooldownMin = 30;
        baseSpawnCooldownMax = 90;

        gameOver = false;
        gameStarted = true;
    }

}

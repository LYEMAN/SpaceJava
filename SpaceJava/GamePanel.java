package SpaceJava;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements ActionListener {

    private final int PANEL_WIDTH = 800;
    private final int PANEL_HEIGHT = 600;

    boolean gameOver = false;
    int respawnTimer = 0;

    Timer timer;
    Player player;
    ArrayList<Enemy> enemies;
    ArrayList<Explosion> explosions;

    boolean gameStarted = false;

    int score = 0;

    // Sounds
    Sound explosionSound = new Sound("src/SpaceJava/Sound Files/Spacecraft_Explosion_Blow_Up_Sound_Effect.wav");
    Sound gameStartSound = new Sound("src/SpaceJava/Sound Files/Game_Start_Sound_Effect.wav");

    public GamePanel() {
        this.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);

        player = new Player(400, 500);
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();

        // Create some enemies
        for (int i = 0; i < 5; i++) {
            enemies.add(new Enemy(100 + (i * 120), 50));
        }

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (!gameStarted && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    gameStarted = true;
                    gameStartSound.play();
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

    public void killPlayer() {
        player.alive = false;
        player.lives--;

        explosions.add(new Explosion(player.getX(), player.getY()));
        explosionSound.play();

        if (player.lives <= 0) {
            gameOver = true;
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

        if (!gameOver) {

            // PLAYER UPDATES
            if (player.alive) {
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
            //      COLLISION CHECKS
            // ---------------------------

            Rectangle playerRect = new Rectangle(player.getX(), player.getY(), 40, 40);

            // PLAYER BULLETS → ENEMIES
            for (Enemy enemy : enemies) {
                Rectangle enemyRect = new Rectangle(enemy.x, enemy.y, 40, 40);

                for (Bullet pb : player.bullets) {
                    Rectangle bulletRect = new Rectangle(pb.x, pb.y, 6, 12);

                    if (bulletRect.intersects(enemyRect)) {
                        pb.active = false;
                        enemy.dead = true;

                        // Explosion + Sound
                        explosions.add(new Explosion(enemy.x, enemy.y));
                        explosionSound.play();

                        // Score
                        score += 100;
                    }
                }

                // PLAYER COLLIDES WITH ENEMY
                if (player.alive && playerRect.intersects(enemyRect)) {
                    killPlayer();
                }
            }

            // Remove dead enemies and respawn new ones
            enemies.removeIf(en -> en.dead);
            while (enemies.size() < 5) {
                enemies.add(new Enemy((int)(Math.random()*700+50), -40));
            }

            // ENEMY BULLETS → PLAYER
            for (Enemy enemy : enemies) {
                for (Bullet eb : enemy.bullets) {
                    Rectangle bulletRect = new Rectangle(eb.x, eb.y, 6, 12);

                    if (player.alive && bulletRect.intersects(playerRect)) {
                        eb.active = false;
                        killPlayer();
                    }
                }
            }

            // EXPLOSIONS
            explosions.removeIf(ex -> !ex.active);
            for (Explosion ex : explosions) {
                ex.update();
            }
        }

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // START MENU
        if (!gameStarted) {
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("SPACE SHOOTER", 230, 260);

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

        // SCORE
        g.setColor(Color.yellow);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 25);

        // LIVES
        g.setColor(Color.white);
        g.drawString("Lives: " + player.lives, 10, 50);

        // GAME OVER
        if (gameOver) {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", 250, 300);

            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.drawString("Score: " + score, 350, 350);
        }
    }
}

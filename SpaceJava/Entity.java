package SpaceJava;

import java.awt.Graphics;
import java.util.ArrayList;

public abstract class Entity {
    protected int health;
    protected int maxHealth;
    protected boolean alive = true;
    protected ArrayList<Bullet> bullets = new ArrayList<>();

    public Entity(int health) {
        this.health = health;
        this.maxHealth = health;
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    
    public void setHealth(int health) { 
        this.health = health; 
        if(this.health > maxHealth) this.maxHealth = this.health; 
    }
    
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    public void takeDamage(int damage) {
        health -= damage;
    }

    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    public ArrayList<Bullet> getBullets() {
        return bullets;
    }

    public void addBullet(Bullet b) {
        bullets.add(b);
    }

    // Handles bullet movement and cleanup
    public void updateBullets() {
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.update();
        }
        bullets.removeIf(b -> !b.active);
    }

    public void drawBullets(Graphics g) {
        for (Bullet b : bullets) {
            b.draw((java.awt.Graphics2D)g);
        }
    }
}
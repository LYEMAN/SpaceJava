package SpaceJava;

import java.awt.*;
import java.util.ArrayList;

public abstract class Entity {
    protected int health;
    protected boolean alive;
    protected ArrayList<Bullet> bullets;
    
    public Entity(int initialHealth) {
        this.health = initialHealth;
        this.alive = true;
        this.bullets = new ArrayList<>();
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
        if (this.health <= 0) {
            this.alive = false;
        }
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            alive = false;
        }
    }
    
    public void heal(int amount) {
        health += amount;
    }
    
    public boolean isDead() {
        return !alive || health <= 0;
    }
    
    // Shooting methods
    public ArrayList<Bullet> getBullets() {
        return bullets;
    }
    
    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }
    
    public void updateBullets() {
        bullets.removeIf(b -> !b.active);
        for (Bullet b : bullets) {
            b.update();
        }
    }
    
    public void drawBullets(Graphics g) {
        for (Bullet b : bullets) {
            b.draw(g);
        }
    }
    
    public void clearBullets() {
        bullets.clear();
    }
}


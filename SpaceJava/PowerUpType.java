package SpaceJava;

public enum PowerUpType {
    MULTI_SHOT("Multi-Shot", "Increases bullet spread"),
    POWER_UP_2("Health Regen", "Regenerates health over time"),
    POWER_UP_3("Shot Speed Up", "Shoot Faster"),
    POWER_UP_4("Move Speed Up", "Move Faster"),
    POWER_UP_5("Shield", "Orbiting Shield that blocks bullets");
    
    private final String name;
    private final String description;
    
    PowerUpType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}


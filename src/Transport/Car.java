package Transport;


import Player.Player;
import World.World;
import World.TileType;

public class Car {

    private double x, y;
    private double vx, vy;
    private double maxSpeed = 1.5;

    //Health
    private int health;

    //animation
    public boolean onFire = false;

    private Player driver;

    private boolean isDestroyed = false;

    private boolean isRepair = false;
    private int repairCooldown = 0;

    public Car(double x, double y) {
        this.x = x;
        this.y = y;
        health = 100;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void update(World world)
    {
        if(isDestroyed) return;

        if(repairCooldown > 0){
            repairCooldown--;
            if(repairCooldown == 0) isRepair = false;
            return;
        }

        if(driver != null && !isRepair){
            move(world);
            chanceBreak();
            checkHealth();
        }
    }

    private void move(World world)
    {
        x += vx;
        y += vy;

        vx *= 0.95;
        vy *= 0.95;

        double speed = Math.sqrt(vx * vx + vy * vy);
        if(speed > maxSpeed){
            vx = (vx / speed) * maxSpeed;
            vy = (vy / speed) * maxSpeed;
        }

        driver.setX(x);
        driver.setY(y);
    }

    public boolean isAvailable(){
        return driver == null && !isDestroyed;
    }

    public void assignDriver(Player player){
        this.driver = player;
        player.enterVehicle(this);
    }

    public void removeDriver(){
        if (driver != null) {
            driver.exitVehicle();
        }
        driver = null;
        vx = 0;
        vy = 0;
    }


    public boolean canPark(byte value){
        return value == TileType.GRASS;
    }

    private void chanceBreak(){
        if(!isDestroyed){
            double change = Math.random();
            if(change > 0.995) repair();
        }
    }

    private void repair(){
        isRepair = true;
        double problem = Math.random();
        if(problem < 0.6) repairCooldown = 5;
        else if(problem < 0.8) repairCooldown = 6;
        else repairCooldown = 7;
    }

    private void checkHealth(){
        if (health < 30 && !onFire) {
            onFire = true;
        }

        if (onFire) {
            health--;
        }

    }
}

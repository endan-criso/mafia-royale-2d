package Player;

import Logs.Logger;
import Render.PlayerState;
import Transport.Car;

public class Player {

    // Position in the world
    double x;                      // X coordinate
    double y;                      // Y coordinate

    // Movement
    double vx;                     // Velocity in X direction
    double vy;                     // Velocity in Y direction
    private double lastSentX, lastSentY; // cache
    private final double EPSILON = 0.01; // Minimum movement threshold

    // Identity
    String name;                   // Agent's name
    long id;

    // Life stats
    private boolean alive = true;          // Is this agent alive?
    private int health;                    // Current health
    private int maxHealth;                 // Maximum health
    double maxSpeed = 0.5;

    private PlayerState state = null;

    //Vehicle
    private Car car;
    private boolean isInVehicle;

    // Power & Combat
    int kills = 0;                 // How many agents this one killed

    public double getX() {return x;}
    public void setX(double x) {this.x = x;}
    public double getY() {return y;}
    public void setY(double y) {this.y = y;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public long getId() {return id;}
    public void setId(long id) {this.id = id;}
    public double getVx(){return vx;}
    public void setVx(double vx) {this.vx = vx;}
    public double getVy() {return vy;}
    public void setVy(double vy) {this.vy = vy;}
    public double getMaxSpeed() {return maxSpeed;}

    public void enterVehicle(Car car){
        this.car = car;
        isInVehicle = true;
    }

    public void exitVehicle(){
        this.car = null;
        isInVehicle = false;
    }

    public Player(long id, String name){
        this.id = id;
        this.name = name;
        maxHealth = 100;
        health = maxHealth;
        vx = 0;
        vy = 0;
        state = PlayerState.IDLE;

    }

    public boolean hasMoved(){
        return Math.abs(x -  lastSentX) > EPSILON || Math.abs(y - lastSentY) > EPSILON;
    }

    public void markPosition(){
        this.lastSentY = this.y;
        this.lastSentX = this.x;
    }

    public void update(){

        double speed = Math.sqrt(vx * vx + vy * vy);

        if(speed > maxSpeed)
        {
            // This math keeps the direction but sets the length to maxSpeed
            vx = (vx / speed) * maxSpeed;
            vy = (vy / speed) * maxSpeed;
        }

        this.x += vx;
        this.y += vy;
    }

    public void hurt(int damage){

        if(!alive) return;

        health -= damage;

        if(health <= 0)
        {
            state = PlayerState.DEAD;
            alive = false;
            Logger.get().info(name + " : is Killed");
            return;
        }

        state = PlayerState.HURT;
    }
}

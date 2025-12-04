package gui.models;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Represents a single airplane flying between two airports in the simulation.
 * Stores departure time, duration, current position, and active state.
 */
public class Airplane extends Figure {

    private Airport from;
    private Airport to;
    private int hours;
    private int minutes;
    private int duration; // in minutes
    private boolean visible = true;

    private long startSimTimeInMinutes = -1;
    private boolean active = false;

    private double startX, startY, endX, endY;
    private double vx, vy;
    private long lastUpdateSimTime = -1;

    /**
     * Constructs a new Airplane.
     * 
     * @param width drawing width
     * @param srcAirport departure airport
     * @param destAirport destination airport
     * @param takeOffTime "HH:MM" format
     * @param duration flight duration in minutes
     */
    public Airplane(int width, Airport srcAirport, Airport destAirport, String takeOffTime, int duration) {
        super(srcAirport.getX(), srcAirport.getY(), width);
        this.from = srcAirport;
        this.to = destAirport;
        String[] parts = takeOffTime.split(":");
        this.hours = Integer.parseInt(parts[0]);
        this.minutes = Integer.parseInt(parts[1]);
        this.duration = duration;
    }

    /** Returns departure time in minutes from 00:00. */
    public int getDepartureInMinutes() {
        return hours * 60 + minutes;
    }

    public int getDuration() { return duration; }
    public boolean isActive() { return active; }
    public void setVisible(boolean v) { this.visible = v; }

    /**
     * Activates the airplane in the simulation.
     * Sets its start/end coordinates and velocity.
     */
    public void activate(long simNowInMinutes) {
        this.startSimTimeInMinutes = simNowInMinutes;
        this.active = true;

        this.startX = from.getX();
        this.startY = from.getY();
        this.endX = to.getX();
        this.endY = to.getY();

        this.x = startX;
        this.y = startY;

        if (duration > 0) {
            this.vx = (endX - startX) / (double) duration;
            this.vy = (endY - startY) / (double) duration;
        } else {
            this.vx = this.vy = 0.0;
        }

        this.lastUpdateSimTime = simNowInMinutes;
    }

    /**
     * Updates the airplane's position based on elapsed simulation time.
     * Automatically deactivates the airplane when it reaches its destination.
     */
    public void updatePosition(long simNowInMinutes) {
        if (!active) return;
        visible = from.isVisible() && to.isVisible();
        long delta = simNowInMinutes - lastUpdateSimTime;
        if (delta <= 0) return;

        x += vx * delta;
        y += vy * delta;
        lastUpdateSimTime = simNowInMinutes;

        long elapsed = simNowInMinutes - startSimTimeInMinutes;
        if (elapsed >= duration) {
            x = endX;
            y = endY;
            active = false;
        }
    }

    @Override
    public void paint(Graphics g) {
        if (!visible || !active) return;
        Color prev = g.getColor();
        g.setColor(Color.BLUE);
        g.fillOval((int)Math.round(x - width/2), (int)Math.round(y - width/2), width, width);
        g.setColor(prev);
    }

    @Override
    public String toString() {
        return String.format("[%s -> %s] %02d:%02d | %d min",
                from.getCode(), to.getCode(), hours, minutes, duration);
    }

    // Getters and setters
    public Airport getFrom() { return from; }
    public void setFrom(Airport from) { this.from = from; }
    public Airport getTo() { return to; }
    public void setTo(Airport to) { this.to = to; }
    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }
    public int getMinutes() { return minutes; }
    public void setMinutes(int minutes) { this.minutes = minutes; }
    public void setDuration(int duration) { this.duration = duration; }

    /** Resets airplane to its original position and deactivates it. */
    public void restoreOriginalPosition() {
        this.x = from.getX();
        this.y = from.getY();
        this.active = false;
        this.lastUpdateSimTime = -1;
        this.startSimTimeInMinutes = -1;        
    }
}

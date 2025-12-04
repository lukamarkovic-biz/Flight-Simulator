package gui.models;

import java.awt.Graphics;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Represents an airport in the simulation.
 * Stores its name, code, coordinates, visibility, and the queue of airplanes.
 * Manages airplane departures according to simulation rules.
 */
public class Airport extends Figure {

    private String name;
    private String code;
    private double nsX;
    private double nsY;
    private boolean visible = true;
    private boolean selected = false;

    // Time of last airplane sent, in simulated minutes
    private long lastSentTime = Long.MIN_VALUE / 2;

    // PriorityQueue sorted by departure time (earliest first)
    private Queue<Airplane> airplanes = new PriorityQueue<>(Comparator.comparingInt(Airplane::getDepartureInMinutes));
    private Queue<Airplane> backup = new PriorityQueue<>(Comparator.comparingInt(Airplane::getDepartureInMinutes));

    private static boolean blinkOn;

    // Scaling factors for drawing on canvas
    private static double widthFactor = 1.0;
    private static double heightFactor = 1.0;

    /**
     * Rescales coordinates when the canvas is resized.
     */
    public void scaleCoordinates() {
        this.x = nsX * widthFactor;
        this.y = -nsY * heightFactor;
    }

    /** Sets the scaling factors used by all airports for drawing. */
    public static void setScaleFactors(double wFactor, double hFactor) {
        widthFactor = wFactor;
        heightFactor = hFactor;
    }

    public Airport(double x, double y, int width, String name, String code) {
        super(x * widthFactor, y * heightFactor, width);
        this.nsX = x;
        this.nsY = y;
        this.name = name;
        this.code = code;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%.3f, %.3f)", code, name, nsX, nsY);
    }

    public synchronized void addFlight(Airplane a) {
        airplanes.offer(a);
        backup.offer(a);
    }

    public synchronized boolean removeFlight(Airplane a) {
        return airplanes.remove(a) && backup.remove(a);
    }

    /**
     * Attempts to send an airplane from this airport.
     * @param currentSimTimeInMinutes - current simulated time in minutes
     * @return the airplane ready to take off, or null if none can take off yet
     */
    public synchronized Airplane sendAirplane(long currentSimTimeInMinutes) {
        if (airplanes.isEmpty()) return null;

        Airplane next = airplanes.peek();

        // Rule: minimum 10 minutes between departures from the same airport
        if (currentSimTimeInMinutes - lastSentTime < 10) {
            return null;
        }

        if (next.getDepartureInMinutes() <= currentSimTimeInMinutes) {
            airplanes.poll();
            lastSentTime = currentSimTimeInMinutes;
            return next;
        }

        return null;
    }

    public void toggleSelected() {
        this.selected = !this.selected;
    }

    @Override
    public void paint(Graphics g) {
        java.awt.Color prev = g.getColor();
        if (selected) {
            g.setColor(blinkOn ? java.awt.Color.RED : java.awt.Color.GRAY);
        } else {
            g.setColor(java.awt.Color.GRAY);
        }
        g.fillRect((int)x - width/2, (int)y - width/2, width, width);
        g.setColor(java.awt.Color.BLACK);
        g.drawString(this.code, (int)x + width/2, (int)y);
        g.setColor(prev);
    }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { this.visible = v; }

    public static void toggleBlinkOn() { blinkOn = !blinkOn; }

    public String getCode() { return code; }
    public double getNsX() { return nsX; }
    public double getNsY() { return nsY; }
    public String getName() { return name; }

    /** Restores the airport state to its initial condition (all flights reset). */
    public void restoreOriginalState() {
        Queue<Airplane> newQ = new PriorityQueue<>(Comparator.comparingInt(Airplane::getDepartureInMinutes));
        if (backup != null) newQ.addAll(backup);
        this.airplanes = newQ;
        this.lastSentTime = Long.MIN_VALUE / 2;
    }
}

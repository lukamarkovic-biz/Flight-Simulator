package gui.models;

import java.awt.Graphics;

/**
 * Abstract base class for drawable figures in the simulation.
 * Provides position, size, and a method to paint on a Graphics object.
 */
public abstract class Figure {

    protected double x;
    protected double y;
    protected int width;

    public Figure(double x, double y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    /** Paints the figure using the given Graphics context. */
    public abstract void paint(Graphics g);

    public double getX() { return x; }
    public void setX(int x) { this.x = x; }

    public double getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    /**
     * Checks if a given point (px, py) is within the figure's bounds.
     */
    public boolean containsPoint(int px, int py) {
        int half = width / 2;
        int left = (int) this.x - half;
        int right = (int) this.x + half;
        int top = (int) this.y - half;
        int bottom = (int) this.y + half;
        return px >= left && px <= right && py >= top && py <= bottom;
    }
}

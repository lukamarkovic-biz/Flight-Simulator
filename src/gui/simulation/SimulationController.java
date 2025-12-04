package gui.simulation;

import gui.models.SimulationModel;
import gui.models.Airplane;
import gui.models.Airport;
import gui.timer.MyTimer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Simulation owner: manages the simulation loop using MyTimer for fixed ticks.
 * Tracks active airplanes and updates positions on each tick.
 */
public class SimulationController {
    private final SimulationModel model;
    private final Scene sceneForRepaint;
    private final List<Airplane> activeAirplanes = new ArrayList<>();

    private MyTimer simTimer;

    private final long simTickMs = 200L;       // Tick duration in milliseconds
    private final double realToSimRatio = 10.0; // 1s real time = 10 simulation minutes

    private boolean running = false;
    private boolean paused = false;

    public SimulationController(SimulationModel model, Scene sceneForRepaint) {
        this.model = model;
        this.sceneForRepaint = sceneForRepaint;
    }

    /** Starts the simulation if not already running. */
    public synchronized void start() {
        if (running) return;

        running = true;
        paused = false;
        synchronized (activeAirplanes) { activeAirplanes.clear(); }

        // Create and start a new timer
        simTimer = new MyTimer(simTickMs, realToSimRatio, this::tick);
        simTimer.start();
    }

    /** Toggles pause/resume state of the simulation. */
    public synchronized void pauseToggle() {
        if (!running) return;

        paused = !paused;
        if (paused) {
            simTimer.pauseTimer();
        } else {
            simTimer.resumeTimer();
        }
    }

    /** Stops the simulation and resets all states. */
    public synchronized void stop() {
        if (!running) return;

        running = false;
        paused = false;

        if (simTimer != null) {
            simTimer.stopTimer();
            simTimer = null;
        }

        synchronized (activeAirplanes) {
            for (Airplane a : activeAirplanes)
                a.restoreOriginalPosition();
            activeAirplanes.clear();
        }

        model.reset();

        if (sceneForRepaint != null) {
            sceneForRepaint.repaint();
        }
    }

    public synchronized boolean isRunning() { return running; }
    public synchronized boolean isPaused() { return paused; }

    public synchronized long getSimTimeInMinutes() {
        return (simTimer != null) ? simTimer.getSimTimeInMinutes() : 0;
    }

    /**
     * Tick method called by the timer on each interval.
     * Updates airplane launches and positions.
     */
    private void tick() {
        if (!running || paused) return;

        long simTimeInMinutes = simTimer.getSimTimeInMinutes();

        // Airports launch airplanes
        Collection<Airport> airports = model.getAirports();
        for (Airport a : airports) {
            Airplane next = a.sendAirplane(simTimeInMinutes);
            if (next != null) {
                next.activate(simTimeInMinutes);
                synchronized (activeAirplanes) { activeAirplanes.add(next); }
            }
        }

        // Update active airplanes
        synchronized (activeAirplanes) {
            Iterator<Airplane> it = activeAirplanes.iterator();
            while (it.hasNext()) {
                Airplane ap = it.next();
                ap.updatePosition(simTimeInMinutes);
                if (!ap.isActive()) it.remove();
            }
        }

        if (sceneForRepaint != null) {
            sceneForRepaint.repaint();
        }
    }

    /**
     * Returns a snapshot of active airplanes for rendering.
     */
    public List<Airplane> getActiveAirplanesSnapshot() {
        synchronized (activeAirplanes) {
            return new ArrayList<>(activeAirplanes);
        }
    }
}

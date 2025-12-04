package gui.models;

import java.util.*;

/**
 * Notifies registered ModelListeners when the simulation state changes.
 */
public class SimulationModel {

    /** Listener interface for model change notifications. */
    public interface ModelListener {
        void modelChanged();
    }

    private final Map<String, Airport> airports = new HashMap<>(); // key = uppercase code
    private final List<Airplane> flights = new ArrayList<>();
    private final List<ModelListener> listeners = new ArrayList<>();

    /** Adds an airport to the model and notifies listeners. */
    public synchronized void addAirport(Airport a) {
        if (a == null) return;
        airports.put(a.getCode(), a);
        fireChanged();
    }

    /** Removes an airport by code and notifies listeners. */
    public synchronized boolean removeAirport(String code) {
        if (code == null) return false;
        Airport removed = airports.remove(code.trim().toUpperCase());
        if (removed != null) {
            fireChanged();
            return true;
        }
        return false;
    }

    /** Returns an airport by code, or null if not found. */
    public synchronized Airport getAirport(String code) {
        if (code == null) return null;
        return airports.get(code.trim().toUpperCase());
    }

    /** Returns a snapshot of all airports. */
    public synchronized Collection<Airport> getAirports() {
        return new ArrayList<>(airports.values());
    }

    /** Adds a flight to the model and registers it with its source airport. */
    public synchronized void addFlight(Airplane f) {
        if (f == null) return;
        flights.add(f);
        Airport src = airports.get(f.getFrom().getCode());
        if (src != null) src.addFlight(f);
        fireChanged();
    }

    /** Returns a snapshot of all flights. */
    public synchronized List<Airplane> getFlights() {
        return new ArrayList<>(flights);
    }

    /** Removes a flight and updates its source airport. */
    public synchronized boolean removeFlight(Airplane f) {
        if (f == null) return false;
        boolean removed = flights.remove(f);
        if (removed) {
            Airport src = airports.get(f.getFrom().getCode());
            if (src != null) src.removeFlight(f);
            fireChanged();
        }
        return removed;
    }

    // ---- listener management ----

    public void addListener(ModelListener l) {
        if (l == null) return;
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeListener(ModelListener l) {
        if (l == null) return;
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /** Notifies all registered listeners about a change. */
    private void fireChanged() {
        List<ModelListener> copy;
        synchronized (listeners) {
            copy = new ArrayList<>(listeners);
        }
        for (ModelListener l : copy) {
            try {
                l.modelChanged();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /** Clears all airports and flights, and notifies listeners. */
    public synchronized void clearAll() {
        flights.clear();
        airports.clear();
        fireChanged();
    }

    /** Resets all airports to their original state (flight queues, last sent time). */
    public void reset() {
        for (Airport a : airports.values()) {
            a.restoreOriginalState();
        }
    }
}

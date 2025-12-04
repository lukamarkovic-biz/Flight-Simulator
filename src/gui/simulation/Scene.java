package gui.simulation;

import gui.models.SimulationModel;
import gui.timer.MyTimer;
import gui.models.Airplane;
import gui.models.Airport;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.List;

/**
 * Canvas that renders the current state of the simulation model
 * and active airplanes from the controller. Does not modify the model.
 */
public class Scene extends Canvas implements SimulationModel.ModelListener {

    private final SimulationModel model;
    private SimulationController controller;

    private MyTimer blinkTimer;

    /**
     * Constructs a Scene canvas for a given simulation model.
     * Registers for model updates and starts the airport blink timer.
     */
    public Scene(SimulationModel model) {
        this.model = model;

        // Register for model changes
        this.model.addListener(this);

        // Blink timer for airports
        blinkTimer = new MyTimer(200, 1, () -> {
            Airport.toggleBlinkOn();
            repaint();
        });
        blinkTimer.start();

        // Handle resizing to update scaling
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateScaling();
                repaint();
            }
        });

        // Mouse click selects airport
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectAirport(e.getX(), e.getY());
            }
        });

        updateScaling();
    }

    /** Updates scaling factors based on current canvas size. */
    private void updateScaling() {
        double wFactor = (getWidth() / 2.0) / 100.0;
        double hFactor = (getHeight() / 2.0) / 100.0;
        Airport.setScaleFactors(wFactor, hFactor);
        for (Airport a : model.getAirports()) a.scaleCoordinates();
    }

    /** Selects an airport at the given pixel coordinates. */
    private void selectAirport(int px, int py) {
        int tx = px - (getWidth() / 2);
        int ty = py - (getHeight() / 2);
        Collection<Airport> airports = model.getAirports();
        for (Airport a : airports) {
            if (a.containsPoint(tx, ty)) {
                a.toggleSelected();
            }
        }
    }

    /** Called when the model changes; triggers repaint. */
    @Override
    public void modelChanged() {
        repaint();
    }

    /** Paints the airports and active airplanes. */
    @Override
    public void paint(Graphics g) {
        g.translate(getWidth() / 2, getHeight() / 2);

        for (Airport a : model.getAirports()) {
            if (a.isVisible()) a.paint(g);
        }

        if (controller != null) {
            List<Airplane> active = controller.getActiveAirplanesSnapshot();
            for (Airplane ap : active) {
                ap.paint(g);
            }
        }
    }

    /** Stops the blink timer and cleans up resources. */
    public void dispose() {
        if (blinkTimer != null) blinkTimer.stopTimer();
    }

    /** Sets the simulation controller used to access active airplanes. */
    public void setController(SimulationController controller) {
        this.controller = controller;
    }
}

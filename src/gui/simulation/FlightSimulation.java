package gui.simulation;

import java.awt.*;
import java.awt.event.*;

import gui.dialogs.airport.ExportAirportsDialog;
import gui.dialogs.airport.ImportAirportsDialog;
import gui.dialogs.airport.NewAirportDialog;
import gui.dialogs.flight.ExportFlightsDialog;
import gui.dialogs.flight.ImportFlightsDialog;
import gui.dialogs.flight.NewFlightDialog;
import gui.idle.IdleManager;
import gui.models.Airplane;
import gui.models.Airport;
import gui.models.SimulationModel;
import gui.service.RegistrationService;
import gui.timer.MyTimer;

/**
 * Main flight simulation window. Combines model, controller, and view.
 * Provides UI for managing airports, flights, and simulation controls.
 * Integrates idle manager to auto-close the program after user inactivity.
 */
public class FlightSimulation extends Frame {

    // Model + controller + map view
    private final SimulationModel model = new SimulationModel();
    private Scene map;
    private SimulationController controller;

    // Central + right panels
    private final Panel centerPanel = new Panel();
    private final Panel rightPanel = new Panel();

    // Right panel sub-panels
    private final Panel airportPanel = new Panel(); 
    private final Panel flightsPanel = new Panel(); 
    
    private Button startBtn;
    private Button pauseBtn;
    private Button stopBtn;
    
    private Label timeLabel;
    
    private IdleManager idleManager;
    
    private MyTimer myTimer;
    private final RegistrationService service;

    public FlightSimulation() {
        super("Flight Simulation");
        setBounds(250, 100, 1000, 650);
        setResizable(true);

        // Create scene and controller
        map = new Scene(model);
        controller = new SimulationController(model, map);
        map.setController(controller);

        idleManager = new IdleManager(this);
        service = new RegistrationService(model);

        populateWindow();
        setupWindowClosing();

        setVisible(true);
    }

    private void populateWindow() {
        // Menu bar
        MenuBar menuBar = new MenuBar();
        Menu airportMenu = new Menu("Airport");
        MenuItem insertAirport = new MenuItem("New Airport");
        MenuItem importAirport = new MenuItem("Import from CSV");
        MenuItem exportAirport = new MenuItem("Export to CSV");

        insertAirport.addActionListener(ae -> new NewAirportDialog(this, service));
        importAirport.addActionListener(ae -> new ImportAirportsDialog(this, service));
        exportAirport.addActionListener(ae -> new ExportAirportsDialog(this, service));

        airportMenu.add(insertAirport);
        airportMenu.add(importAirport);
        airportMenu.add(exportAirport);
        menuBar.add(airportMenu);

        Menu flightsMenu = new Menu("Flights");
        MenuItem insertFlight = new MenuItem("New Flight");
        MenuItem importFlight = new MenuItem("Import from CSV");
        MenuItem exportFlight = new MenuItem("Export to CSV");

        insertFlight.addActionListener(ae -> new NewFlightDialog(this, service));
        importFlight.addActionListener(ae -> new ImportFlightsDialog(this, service));
        exportFlight.addActionListener(ae -> new ExportFlightsDialog(this, service));

        flightsMenu.add(insertFlight);
        flightsMenu.add(importFlight);
        flightsMenu.add(exportFlight);

        menuBar.add(flightsMenu);
        setMenuBar(menuBar);

        // Center panel: map
        centerPanel.setLayout(new BorderLayout());
        map.setBackground(Color.GREEN);
        centerPanel.add(map, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Right panel layout
        rightPanel.setLayout(new BorderLayout(6,6));
        rightPanel.setPreferredSize(new Dimension(240,0));

        // Airports section
        Panel apTitle = new Panel(new FlowLayout(FlowLayout.LEFT));
        apTitle.add(new Label("Airports"));
        rightPanel.add(apTitle, BorderLayout.NORTH);

        airportPanel.setLayout(new GridLayout(0,1,4,4));
        ScrollPane airportScroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        Panel airportContainer = new Panel(new BorderLayout());
        airportContainer.add(airportPanel, BorderLayout.NORTH);
        airportScroll.add(airportContainer);
        rightPanel.add(airportScroll, BorderLayout.CENTER);

        // Flights section
        Panel flightsContainer = new Panel(new BorderLayout());
        Panel flightsTitle = new Panel(new FlowLayout(FlowLayout.LEFT));
        flightsTitle.add(new Label("Flights"));
        flightsContainer.add(flightsTitle, BorderLayout.NORTH);

        flightsPanel.setLayout(new GridLayout(0,1));
        Panel flightsListHolder = new Panel(new BorderLayout());
        flightsListHolder.add(flightsPanel, BorderLayout.NORTH);

        ScrollPane flightsScroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        flightsScroll.add(flightsListHolder);

        Panel flightsSouth = new Panel(new BorderLayout());
        flightsSouth.add(flightsScroll, BorderLayout.CENTER);
        flightsSouth.setPreferredSize(new Dimension(240,250));

        // Controls
        Panel controlsPanel = new Panel(new BorderLayout());
        Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.CENTER,10,5));
        startBtn = new Button("Start");
        pauseBtn = new Button("Pause");
        stopBtn = new Button("Stop");

        startBtn.setEnabled(false);
        pauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);

        buttonsPanel.add(startBtn);
        buttonsPanel.add(pauseBtn);
        buttonsPanel.add(stopBtn);

        timeLabel = new Label("Time: 00:00");
        Panel timePanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        timePanel.add(timeLabel);

        controlsPanel.add(buttonsPanel, BorderLayout.NORTH);
        controlsPanel.add(timePanel, BorderLayout.SOUTH);
        flightsSouth.add(controlsPanel, BorderLayout.SOUTH);
        flightsContainer.add(flightsSouth, BorderLayout.CENTER);

        rightPanel.add(flightsContainer, BorderLayout.SOUTH);

        Panel rightWrapper = new Panel(new BorderLayout());
        rightWrapper.add(rightPanel, BorderLayout.CENTER);
        add(rightWrapper, BorderLayout.EAST);

        setupButtonActions();
        validate();
        repaint();
    }

    private void setupButtonActions() {
        startBtn.addActionListener(ae -> {
            setupTimer();
            idleManager.pause();
            controller.start();
            updateControlButtons();
        });

        pauseBtn.addActionListener(ae -> {
            if (controller.isPaused()) {
                myTimer.resumeTimer();
                idleManager.pause();
            } else {
                myTimer.pauseTimer();
                idleManager.resume();
            }
            controller.pauseToggle();
            updateControlButtons();
        });

        stopBtn.addActionListener(ae -> {
            timeLabel.setText("Time: 00:00");
            myTimer.stopTimer();
            idleManager.resume();
            controller.stop();
            updateControlButtons();
        });
    }

    private void setupTimer() {
        myTimer = new MyTimer(
            200,              // tickMs
            10.0,             // realToSimRatio
            () -> {
                final long simMin = controller.getSimTimeInMinutes();
                final long hours = (simMin / 60) % 24;
                final long minutes = simMin % 60;
                final String text = String.format("Time: %02d:%02d", hours, minutes);
                EventQueue.invokeLater(() -> timeLabel.setText(text));
            }
        );
        myTimer.start();
    }

    private void setupWindowClosing() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (myTimer != null) myTimer.stopTimer();
                if (controller != null) controller.stop();
                if (map != null) map.dispose();
                if (idleManager != null) idleManager.stop();
                dispose();
            }
        });
    }

    public void registerAirport(Airport a) {
        Checkbox cb = new Checkbox(a.toString(), true);
        airportPanel.add(cb);
        cb.addItemListener(e -> {
            boolean checked = cb.getState();
            a.setVisible(checked);
            map.repaint();
        });

        map.repaint();
        validate();
        repaint();
    }

    public void registerFlight(Airplane a) {
        flightsPanel.add(new Label(a.toString()));
        updateControlButtons();
        validate();
        repaint();
    }

    private void updateControlButtons() {
        boolean running = controller.isRunning();
        boolean paused = controller.isPaused();

        startBtn.setEnabled(!running && !model.getAirports().isEmpty());
        pauseBtn.setEnabled(running);
        stopBtn.setEnabled(running);
        pauseBtn.setLabel(paused ? "Resume" : "Pause");
    }

    public static void main(String[] args) {
        new FlightSimulation();
    }
}
